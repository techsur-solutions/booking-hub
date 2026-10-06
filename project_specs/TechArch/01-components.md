
## 2. Component Architecture

Every backend component below is a Java 21 + Spring Boot 3 application unless noted. Each has its own Maven build, Docker image, and Kubernetes manifest set. Internal layering within each service follows a conventional Controller → Service → Repository structure (Spring MVC for synchronous REST services; Spring Cloud Gateway uses WebFlux/reactive, noted separately).

### 2.1 Spring Cloud Gateway (API Gateway)

**Responsibilities:**
- Sole externally-reachable ingress; terminates TLS; the only component with a Kubernetes `Ingress`/external `Service` of type `LoadBalancer`.
- Validates every incoming bearer JWT against Keycloak's published JWKS (cached, refreshed per standard JWKS rotation interval); rejects (401) any request with a missing/expired/malformed token on a protected route.
- Coarse-grained route-to-role mapping (per FRD `Y1-api.md` §Gateway Routing table) — e.g., `/bookings/**` requires `viewRoomBooking` or `allowRoomBooking`; `/permissions/**` requires `accessPermissions`.
- Routes `/feeds/**` and `POST /auth/login`/`POST /auth/password-reset/*` through without requiring a pre-existing token (public per FRD Y1), while still applying rate limiting.
- Strips/forwards the validated JWT (or re-signed internal token) downstream so backend services can perform fine-grained checks without re-validating signature from scratch (though each service independently re-validates per "fail closed" rule — see §4 Security Architecture).
- Applies global rate limiting (`RATE_LIMIT_EXCEEDED`, 429, per `Y2-errors.md`) and circuit-breaking toward unavailable backends (`SERVICE_UNAVAILABLE`, 503).
- Implementation: **Spring Cloud Gateway (WebFlux/reactive)**, `spring-cloud-starter-gateway`, with `spring-cloud-gateway-server-webflux` route predicates/filters defined in `application.yml` (or a `RouteLocator` bean) per the routing table in §API Design.

**Does not own:** any business data; stateless except for short-lived JWKS/route-config caches.

### 2.2 booking-service

**Responsibilities (FRD F1, F2, F3):**
- Owns `bookings`, `booking_resources` (database `booking_db`).
- Create/edit/delete/clone bookings; recurrence expansion into series occurrences sharing `series_id`.
- Default-duration rule (`end_time = start_time + 1h` when omitted); time-range validation.
- Conflict Detection: single internal code path (`ConflictDetectionService`) invoked identically by create, edit, bulk calendar/list reads, and the standalone `POST /bookings/check-conflicts` endpoint — per FRD F2 §Process step 8's "exactly one conflict-evaluation code path" rule.
- Status workflow: reads `approveBooking` from settings-service (synchronous call, short-TTL cache per §1.5) to set initial `pending`/`approved` status; exposes approve/deny actions gated by `allowApproveBooking`.
- Publishes domain events via transactional outbox: `booking.created`, `booking.updated`, `booking.deleted`, `booking.approved`, `booking.denied`.
- Serves calendar/day/list read endpoints and the Eventdata-equivalent detail endpoint, each returning `conflict_flags[]`.
- Calls locations-resources-service (read-only, by ID) to validate `location_id`/`resource_ids` exist and are non-deleted at create/edit time; calls custom-field-service (read-only) to validate submitted `custom_field_values[]` reference fields applicable to the booking's location context.

**Internal modules:** `booking` (aggregate root + lifecycle), `conflict` (overlap-detection engine), `recurrence` (series expansion — interim weekly-by-day-of-week-with-end-date per FRD F1 §Process step 5 `[OPEN QUESTION]`), `outbox` (transactional event relay).

### 2.3 locations-resources-service

**Responsibilities (FRD F4):**
- Owns `locations`, `resources` (database `locres_db`).
- CRUD for both entities; admin-only writes (gated by the location/resource management permission, F7).
- Soft-delete only (`deleted_at`), per FRD F4 §Process step 6 interim default — existing booking references remain intact and display using last-known values.
- Exposes read endpoints consumed by booking-service for validation and by the frontend calendar for CSS-class/colour rendering.
- Publishes `location.created`/`location.updated`/`location.deleted`, `resource.created`/`resource.updated`/`resource.deleted` domain events (consumed by audit-log-service, and by feeds-service to keep its `feed_bookings` projection's denormalized `location_name` current).

### 2.4 custom-field-service

**Responsibilities (FRD F5):**
- Owns `custom_fields`, `custom_field_templates`, `custom_field_joins`, `custom_field_values` (database `customfld_db`).
- Admin management of field definitions and templates; template-to-context (Location or global) association via `custom_field_joins`.
- Exposes a read endpoint returning the applicable fields for a given context (Location `id` or global) so booking-service/frontend can render the dynamic booking form.
- `custom_field_values` rows are written by **booking-service** at booking-submission time (the FRD assigns `custom_field_values.booking_id` as a cross-service reference resolved by ID only — no DB-level FK), but the field-definition validation (does `field_id` exist, is it applicable to context) is delegated back to custom-field-service via a synchronous validation call from booking-service at submission time.
- Publishes custom-field change events (definition/template changes) for audit-log-service.

### 2.5 users-permissions-service

**Responsibilities (FRD F6, F7):**
- Owns `users` (profile-adjacent data only — no credentials) and `permissions` (legacy-flag-to-Keycloak-role mapping metadata), in one database `userperm_db`, as two independently-migrated internal modules (`user` module, `permission` module) with no FK between their tables — chosen so a future split into two deployables requires no data migration (see §1.5 decision log).
- **`user` module:** admin account creation (provisions the account in the Keycloak realm via the Keycloak Admin API — no local password storage), self-service profile edit (`display_name`), role assignment (writes Keycloak role mappings via Admin API, mirrors a denormalized `roles[]` read into `users` only if needed for list-view performance), publishes `user.created`/`user.updated`/`role.assigned` events.
- **`permission` module:** stores the legacy-flag → Keycloak-role mapping table (`GET /permissions`, `GET/PUT /roles/{role}/permissions`) for admin UI display and as the authoritative reference the Gateway's route config and every service's fine-grained checks are generated/validated against in CI (an ArchUnit-style check, per F12 §Validation, that no service hardcodes a permission flag absent from this table). Publishes `permission.updated` events.
- Does **not** implement login, logout, password reset, or password change directly — these are Keycloak protocol flows. This service's `POST /auth/*` endpoints (per `Y1-api.md` §Auth) are thin proxies/BFF-style wrappers: `POST /auth/login` forwards to Keycloak's token endpoint; `POST /auth/password-reset/request` triggers Keycloak's credential-reset flow and publishes `password.reset.requested` for notifications-service to pick up the token/link email (Keycloak does not send email itself in this architecture — see §6 Integration Points); `POST /auth/password-reset/complete` and `POST /auth/password-change` forward to Keycloak's credential-update endpoints.

### 2.6 notifications-service

**Responsibilities (FRD F8):**
- Owns `notification_deliveries` (database `notif_db`) — tracks idempotency keys, delivery status, attempt counts; owns no booking/user data (consumes event payloads only).
- Pure RabbitMQ consumer (no synchronous client-facing write API): consumes `booking.created`, `booking.approved`, `booking.denied`, `password.reset.requested`.
- Recipient resolution (interim default: booking owner only / requesting user only, per FRD F8 §Process step 3 `[OPEN QUESTION]`), template rendering, SMTP dispatch, retry-with-backoff, dead-letter routing on exhaustion.
- Idempotency enforced via unique `idempotency_key` constraint — a redelivered event with an already-`sent` key is a no-op.
- Exposes admin/observability-only read endpoints: `GET /notifications/delivery-status`, `GET /notifications/dead-letter`.

### 2.7 feeds-service

**Responsibilities (FRD F9):**
- Owns the optional denormalized `feed_bookings` projection (database `feeds_db`), populated by consuming `booking.created`/`booking.updated`/`booking.approved`/`booking.denied`/`booking.deleted` events plus `location.updated`/`location.deleted` (to keep `location_name` fresh) — per the §1.4 read-pattern decision.
- Serves `GET /feeds/rss2`, `/ical`, `/json`, `/display-board`, each filtering `feed_bookings WHERE status = 'approved' AND start_time >= now()` and optional `location_id`.
- Enforces `allowAPI` gating per the interim default-deny posture (FRD F9 §Process step 2 `[OPEN QUESTION]`) until F0 confirms feeds are public by default.
- Sets `Cache-Control`/`ETag` headers for CDN/reverse-proxy cacheability without bypassing the access-control check.
- Read-only toward every other service; never receives direct client writes.

### 2.8 settings-service

**Responsibilities (FRD F10):**
- Owns the `settings` singleton table (database `settings_db`).
- `GET /settings` (broad read access — any authenticated user, since the calendar needs slot-size/min/max to render); `PUT /settings` (admin-only).
- Synchronously queried by booking-service at booking-creation time for `approveBooking` (per §1.5 decision); also publishes `settings.updated` for audit-log-service and for any service choosing to cache-and-invalidate rather than poll.
- Enforces singleton invariant (`CHECK (id = 1)`) and calendar-range/slot-size validation server-side, independent of frontend validation.

### 2.9 audit-log-service

**Responsibilities (FRD F11):**
- Owns `audit_log_entries` (database `audit_db`).
- Pure event consumer across every domain event published by every other service (`booking.*`, `location.*`, `resource.*`, `user.*`, `role.assigned`, `settings.updated`, `permission.updated`, custom-field change events) — never performs a direct read of another service's database, per the service-isolation NFR.
- Writes one immutable row per event: `{actor, timestamp, entity_type, entity_id, action_type, before_values, after_values}`; no UPDATE/DELETE grants on `audit_log_entries` for any application role (enforced at the Postgres role-permission level, not just application code).
- Exposes `GET /audit-log` with `entity_type`/`entity_id`/`actor`/`date_from`/`date_to` filters, admin-only (legacy `Logfiles`-equivalent permission).
- A missed/failed event consumption is retried via durable queue + DLQ (same mechanism as notifications-service), never silently dropped.

### 2.10 Frontend: React + TypeScript SPA

**Responsibilities (FRD F12 §Process step 6):**
- Single-page application covering: calendar/day/list booking views with conflict indicators, booking create/edit/detail modal (dynamic custom-field rendering per context), admin CRUD screens (locations, resources, custom fields/templates, users/roles, settings), audit log viewer, public feed landing pages (RSS2/iCal links, embedded JSON-driven widgets), and the digital-signage display-board view (full-screen, auto-refreshing route, typically loaded on a kiosk browser rather than via normal nav).
- Communicates exclusively through Spring Cloud Gateway — no direct service URLs are ever configured in the frontend.
- Authenticates via Keycloak's public client (Authorization Code + PKCE flow) for interactive login; stores access/refresh tokens per standard OIDC client practice (e.g., `keycloak-js` adapter or a thin custom OIDC client), attaches the bearer token to every API call.
- Built as static assets served from an nginx (or equivalent) pod/CDN — not a Spring Boot application; does not have its own backend logic beyond light client-side composition for the cross-domain reads described in §1.4 (e.g., joining a booking's `location_id` against a client-side cached `GET /locations` response).

### 2.11 Keycloak (Identity Provider — not a Booking-Hub-authored service)

**Responsibilities:**
- Hosts the Booking-Hub realm: all user credentials, sessions, refresh tokens, and role/client-scope definitions. Replaces legacy `User.password`/salt storage entirely.
- Exposes OIDC endpoints consumed by the frontend (PKCE login) and by the Gateway/every service (JWKS-based signature verification).
- Exposes an Admin REST API consumed by users-permissions-service for account provisioning and credential-reset/invite flows.
- Deployed as its own Kubernetes workload (StatefulSet or Deployment + its own Postgres-backed realm database) — architecturally present in the cluster but outside the Booking-Hub service boundary; not one of the nine bounded-context services and not subject to the "one PostgreSQL database per Booking-Hub-authored microservice" accounting (its realm DB is Keycloak-internal infrastructure, still logically separate from every business database).
