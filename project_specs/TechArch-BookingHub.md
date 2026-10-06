# Technical Architecture: Booking-Hub

**Project Acronym:** BookingHub
**Document Type:** TechArch (Technical Architecture Document)
**Status:** Draft — derived from PRD-BookingHub.md and FRD-BookingHub.md
**Last Updated:** 2026-10-06

---

## 0. How This Document Relates to the FRD

This TechArch operationalizes the bounded-context decomposition, schema (`Y0-schema.md`), API catalog (`Y1-api.md`), error catalog (`Y2-errors.md`), and integration points (`Y3-integrations.md`) already specified in `FRD-BookingHub.md`. Where the FRD already pins down a concrete decision (e.g., exact DDL, exact endpoint shapes), this document reuses it verbatim and adds the structural/runtime detail (service topology, deployment, security enforcement, messaging topology) the FRD explicitly deferred to TechArch. Every `[OPEN QUESTION — deferred to F0]` flag in the FRD is preserved here, not silently resolved — the interim/default behavior documented in the FRD is what this architecture implements until F0 closes the question.

All tech stack choices below are **hard constraints, not recommendations** — mandated in `PROJECT.md` and PRD Section 4: React + TypeScript frontend; Java 21 + Spring Boot 3 backend services; PostgreSQL, one database per microservice; RabbitMQ for inter-service events; Keycloak for authentication/authorization; Spring Cloud Gateway as the single API gateway entry point; Docker + Kubernetes for deployment.

---

## 1. Architectural Overview

### 1.1 Pattern

Booking-Hub is a **bounded-context microservice architecture** with:

- A single public ingress (Spring Cloud Gateway) — no backend service is externally addressable.
- **Database-per-service** — nine independently owned PostgreSQL databases, zero shared schemas, zero cross-service database joins. Cross-service data needs are resolved either by synchronous REST calls between services (when strict consistency is required) or by consuming RabbitMQ domain events into a service-local, denormalized read model (when eventual consistency is acceptable) — see §1.4.
- **Event-driven decoupling** — side effects that must never block the originating transaction (notifications, audit logging) are triggered exclusively via durable RabbitMQ domain events, published using the **transactional outbox pattern** so a RabbitMQ outage cannot roll back a booking creation.
- **Standards-based identity** — Keycloak is the sole source of truth for credentials, sessions, and role claims. No service stores a password hash. Every protected request carries a Keycloak-issued JWT validated against the realm's published JWKS.
- **Independent deployability** — every service has its own Docker image, its own Kubernetes Deployment/Service/ConfigMap/Secret set, and can be built, deployed, and scaled without coordinating with any other service's release.

### 1.2 Bounded-Context Service Decomposition

Nine deployable units, aligned 1:1 with the FRD's per-service schema ownership (`Y0-schema.md`) and the PRD's indicative decomposition (confirmed, not altered, by this TechArch):

| # | Service | Legacy Equivalent | Owns (DB tables) | FRD Features |
|---|---|---|---|---|
| 1 | **booking-service** | `Bookings`, `Eventdata` controllers; `Event`, `Eventresource` models | `bookings`, `booking_resources` | F1, F2, F3 |
| 2 | **locations-resources-service** | `Locations`, `Resources` controllers; `Location`, `Resource` models | `locations`, `resources` | F4 |
| 3 | **custom-field-service** | `Customfields` controller; `Customfield`/`Customfieldjoin`/`Customfieldvalue` models | `custom_fields`, `custom_field_templates`, `custom_field_joins`, `custom_field_values` | F5 |
| 4 | **users-permissions-service** | `Users`, `Sessions`, `PasswordResets`, `Permissions` controllers; `User` model | `users`, `permissions` | F6, F7 |
| 5 | **notifications-service** | Email logic embedded in `Bookings`/`PasswordResets` | `notification_deliveries` | F8 |
| 6 | **feeds-service** | `Api` controller | `feed_bookings` (optional projection) | F9 |
| 7 | **settings-service** | `Settings` controller/model | `settings` | F10 |
| 8 | **audit-log-service** | `Logfiles` controller | `audit_log_entries` | F11 |
| 9 | **(Keycloak — not a Booking-Hub-authored service)** | hand-rolled auth | realm DB (Keycloak-managed, opaque to Booking-Hub) | F6, F7 (identity half) |

`users-permissions-service` combines F6 (user/role management) and F7 (permission mapping metadata) into one deployable because both own small, tightly related reference tables (`users`, `permissions`) with no independent scaling profile — see §2 Component Architecture for the internal module boundary kept between them (they remain separately testable and could be split later without a schema change, since neither table references the other via FK).

### 1.3 Deployment Topology (ASCII)

```
                                   ┌─────────────────────────┐
                                   │   React + TypeScript    │
                                   │   SPA (static, CDN/     │
                                   │   nginx pod)            │
                                   └────────────┬────────────┘
                                                │ HTTPS (OIDC/PKCE for
                                                │ browser-facing login)
                                                ▼
                                   ┌─────────────────────────┐
                                   │  Spring Cloud Gateway   │◄──────┐
                                   │  (single ingress,       │       │ JWKS fetch /
                                   │  JWT validation,        │       │ token introspection
                                   │  coarse-grained RBAC)   │       │
                                   └────────────┬────────────┘       │
                                                │ internal cluster   │
                                                │ traffic only       │
              ┌───────────────┬────────────────┼──────────────┬─────┴────────┬───────────────┬────────────────┐
              ▼               ▼                ▼              ▼              ▼               ▼                ▼
      ┌───────────────┐┌─────────────┐┌────────────────┐┌───────────┐┌──────────────┐┌─────────────┐┌─────────────────┐
      │ booking-      ││ locations-  ││ custom-field-  ││ users-     ││ notifications││ feeds-      ││ settings-       │
      │ service       ││ resources-  ││ service        ││ permissions││ -service     ││ service     ││ service         │
      │               ││ service     ││                ││ -service   ││              ││             ││                 │
      └───┬───────┬───┘└──────┬──────┘└───────┬────────┘└─────┬──────┘└──────┬───────┘└──────┬──────┘└────────┬────────┘
          │       │           │               │               │              │               │                │
          ▼       │           ▼               ▼               ▼              │               │                ▼
    ┌──────────┐  │     ┌──────────┐    ┌──────────┐    ┌──────────┐         │               │          ┌──────────┐
    │ booking_ │  │     │ locres_  │    │ customfld│    │ userperm_│         │               │          │ settings_│
    │ db (PG)  │  │     │ db (PG)  │    │ _db (PG) │    │ db (PG)  │         │               │          │ db (PG)  │
    └──────────┘  │     └──────────┘    └──────────┘    └──────────┘         │               │          └──────────┘
                  │                                                           ▼               ▼
                  │                                                     ┌──────────┐    ┌──────────┐
                  │                                                     │ notif_db │    │ feeds_db │
                  │                                                     │ (PG)     │    │ (PG,     │
                  │                                                     └──────────┘    │ optional)│
                  │                                                                      └──────────┘
                  └──────────────────────┐
                                          ▼
                                 ┌─────────────────┐        ┌───────────────────┐
                                 │  audit-log-      │        │     RabbitMQ       │
                                 │  service         │◄───────┤  (durable exchanges│
                                 │  (event consumer │        │  + per-consumer    │
                                 │  only)           │        │  queues + DLQs)    │
                                 └────────┬─────────┘        └─────────▲──────────┘
                                          ▼                            │ publishes
                                   ┌──────────────┐                    │ booking.*, location.*,
                                   │ audit_db (PG)│                    │ resource.*, user.*, role.*,
                                   └──────────────┘                    │ settings.updated,
                                                                        │ permission.updated,
                                                                        │ password.reset.requested
                                                                        │
                                                            (all 8 business services
                                                             publish; notifications-service
                                                             and audit-log-service consume)

                                   ┌─────────────────────────┐
                                   │        Keycloak          │  (separate realm DB,
                                   │  (OIDC provider, realm   │   not a Booking-Hub
                                   │  roles/scopes, Admin API)│   service database)
                                   └─────────────────────────┘
                                   Consulted by: Gateway (JWT validation),
                                   users-permissions-service (Admin API calls
                                   for provisioning/credential-reset),
                                   every service (JWKS-based token verification)
```

Key read: every arrow into Spring Cloud Gateway is the **only** externally reachable path into the cluster. Every PostgreSQL instance pictured is a logically (and in production, physically) separate database with its own connection pool, credentials, and Kubernetes Secret — no service's connection string references another service's database. RabbitMQ and Keycloak are shared infrastructure, not owned by any single bounded context, consumed by multiple services through well-defined contracts (AMQP topology; OIDC/Admin API respectively).

### 1.4 Cross-Domain Read Pattern Decisions (PRD Risk Mitigation)

The PRD explicitly flags that database-per-service makes cross-domain reads (e.g., Booking + Custom Field + Location in one list view) harder to replicate from the legacy single-database system. This TechArch resolves that risk per read pattern as follows:

| Read Pattern | Services Involved | Strategy Chosen | Rationale |
|---|---|---|---|
| Booking calendar/list/detail view showing location name + colour alongside booking fields | booking-service, locations-resources-service | **Synchronous API composition at the BFF/Gateway layer** (or client-side parallel fetch): booking-service returns `location_id` only; frontend/BFF joins against `GET /locations` cache | Location data changes infrequently; always-fresh location names outweigh the complexity of a projection |
| Booking detail view showing custom field values | booking-service, custom-field-service | **Synchronous API composition**: booking-service persists `custom_field_values` rows itself (it owns the join to its own `bookings.id`), custom-field-service is only consulted for field *definitions* (label, type) at render time, cached client-side | Avoids a sync call per booking read; booking-service already stores submitted values at write time (F1 §Process step 7) |
| Public feed (RSS2/iCal/JSON/display board) over approved upcoming bookings, filterable by location, needs to be fast and public-read-heavy | booking-service, locations-resources-service, feeds-service | **Event-driven denormalized projection** (`feed_bookings` table in feeds-service, per `Y0-schema.md` §Feed) populated by consuming `booking.created`/`booking.updated`/`booking.approved`/`booking.denied`/`booking.deleted` and `location.updated`/`location.deleted` events | Public feeds must not place synchronous load on the transactional booking-service, and must survive booking-service being temporarily unavailable |
| Audit log entries referencing entity details beyond id/type | all services → audit-log-service | **Event-sourced, denormalized by design**: the audit log stores whatever `before_values`/`after_values` JSONB the publishing service included in its domain event payload; it never calls back to the source service to enrich a record | Keeps audit-log-service a pure event consumer with zero outbound dependencies, per F11's "never performs a direct read of another service's database" rule |

This table is the TechArch-level resolution the FRD (PRD Risk table, F12 §Process step 8) deferred here.

### 1.5 Architectural Decision Log

| Decision | Options Considered | Choice | Rationale |
|---|---|---|---|
| Settings propagation (FRD F10 §Process step 4, deferred to TechArch) | (a) synchronous read of Settings Service API per Booking creation; (b) `settings.changed` broadcast cached locally | **(a) Synchronous read, with a short-TTL local cache (5s) in booking-service** | `approveBooking` is read exactly once per booking creation (F3 §Validation: "read at creation time only, non-retroactive") — a stale cache for a few seconds has no correctness impact, and avoids the complexity of a broadcast-and-invalidate scheme for a single boolean+three small fields |
| Notification/audit decoupling mechanism | synchronous call vs. async event | **Async RabbitMQ events exclusively**, per NFR "Event-driven decoupling" | Hard requirement in PRD/PROJECT.md: a notification or audit failure must never block the originating transaction |
| Outbox vs. direct publish-on-commit | direct `channel.publish()` inside the request thread vs. transactional outbox table + relay | **Transactional outbox pattern** in every event-publishing service | Guarantees the domain event is published if-and-only-if the local DB transaction committed, per Y3 "Publisher guarantee" — a mid-transaction RabbitMQ outage cannot silently drop an event nor roll back a successful booking |
| users-permissions-service as one deployable vs. two | single service vs. split `users-service` + `permissions-service` | **Single deployable, two internal modules** | Both own small reference tables with no independent scaling need today; FRD's schema ownership keeps `users` and `permissions` as separate tables with no FK between them, so a future split requires no schema migration — see §2.4 |
| Gateway framework | Spring Cloud Gateway (mandated) | **Spring Cloud Gateway**, reactive (WebFlux-based) | Hard constraint from PROJECT.md/PRD |
| Conflict-detection enforcement default | hard block vs. soft warning (FRD F2 §Process step 6, `[OPEN QUESTION — deferred to F0]`) | **Hard block (HTTP 409) for all roles**, interim, until F0 confirms otherwise | Matches FRD's stated conservative interim default; implemented as a single code path in booking-service per F2 §Process step 8 |

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

## 3. Data Model

### 3.1 Service-Level Entity-Relationship Overview (ASCII)

Each box is an independently owned PostgreSQL database. Solid lines within a box are real foreign keys. Dashed lines crossing box boundaries are **ID-only references**, resolved at runtime via REST call or event payload — never a database-level foreign key, per the service-isolation NFR.

```
┌─────────────────────────────┐        ┌──────────────────────────────┐
│   booking_db                │        │   locres_db                  │
│  ┌─────────────┐            │        │  ┌───────────┐  ┌───────────┐│
│  │ bookings    │            │ ─ ─ ─ ▶│  │ locations │  │ resources ││
│  │ id (PK)     │  location_id (ref)  │  └───────────┘  └───────────┘│
│  │ series_id   │            │        └──────────────────────────────┘
│  │ status      │            │
│  │ owner_id ───┼─ ─ ─ ─ ─ ─ ─┼ ─ ─ ─ ▶ users_db.users.id
│  └──────┬──────┘            │
│         │ 1:N (real FK)     │        ┌──────────────────────────────┐
│         ▼                   │        │   customfld_db               │
│  ┌─────────────┐            │        │  ┌─────────────┐             │
│  │ booking_    │ resource_id│ ─ ─ ─ ▶│  │ custom_      │             │
│  │ resources   │  (ref)     │        │  │ fields       │             │
│  └─────────────┘            │        │  └──────┬──────┘             │
└──────────────┬───────────────┘        │         │ 1:N (real FK)      │
               │ booking_id (ref)        │         ▼                   │
               └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─▶│  ┌─────────────────┐        │
                                         │  │ custom_field_    │        │
                                         │  │ joins            │        │
                                         │  └─────────────────┘        │
                                         │  ┌─────────────────┐        │
                                         │  │ custom_field_    │        │
                                         │  │ values           │        │
                                         │  │  booking_id(ref) │        │
                                         │  └─────────────────┘        │
                                         └──────────────────────────────┘

┌──────────────────────────────┐  ┌──────────────────────┐  ┌──────────────────────┐
│   userperm_db                 │  │   notif_db            │  │   settings_db         │
│  ┌───────────┐ ┌────────────┐ │  │  ┌──────────────────┐│  │  ┌──────────────────┐ │
│  │ users     │ │ permissions│ │  │  │ notification_     ││  │  │ settings         │ │
│  │ id = KC   │ │ (no FK to  │ │  │  │ deliveries        ││  │  │ (singleton row)  │ │
│  │ subject   │ │  users)    │ │  │  └──────────────────┘│  │  └──────────────────┘ │
│  └───────────┘ └────────────┘ │  └──────────────────────┘  └──────────────────────┘
└──────────────────────────────┘

┌──────────────────────────────┐  ┌──────────────────────────────┐
│   feeds_db (optional          │  │   audit_db                    │
│   projection)                 │  │  ┌──────────────────────────┐│
│  ┌──────────────────────────┐│  │  │ audit_log_entries         ││
│  │ feed_bookings             ││  │  │  entity_id (ref, any svc) ││
│  │  booking_id (ref)         ││  │  │  INSERT-only, no UPDATE/  ││
│  │  location_id (ref)        ││  │  │  DELETE grants             ││
│  └──────────────────────────┘│  │  └──────────────────────────┘│
└──────────────────────────────┘  └──────────────────────────────┘

Keycloak realm DB (opaque, Keycloak-managed — not pictured in detail):
  stores credentials, sessions, refresh tokens, realm/client roles.
  users_db.users.id == Keycloak `sub` claim (see §User DDL note).
```

### 3.2 Conventions

- All tables use `UUID` primary keys (`gen_random_uuid()` default via the `pgcrypto` or `uuid-ossp` extension), except `settings` (deliberate `INTEGER` singleton) and Keycloak-aligned `users.id` (set explicitly to the Keycloak `sub` claim, not independently generated).
- Every table carries `created_at`/`updated_at` (`TIMESTAMPTZ`, UTC); `updated_at` is trigger-maintained (`BEFORE UPDATE` trigger invoking a shared `set_updated_at()` function per service).
- Soft-delete (`deleted_at TIMESTAMPTZ NULL`) is used wherever the FRD's interim deletion-policy default applies (bookings, locations, resources) so audit history and existing references remain intact.
- No table in one service's database may be referenced by a `FOREIGN KEY` from another service's database — every cross-service reference in the diagram above is a plain `UUID` column with no referential-integrity constraint at the database level; integrity is enforced at the application layer (existence-check API calls at write time) and reconciled via domain events.

### 3.3 Per-Service DDL

The following DDL is authoritative and matches `FRD-BookingHub.md` §Y0 verbatim (TechArch does not alter any FRD-specified column, type, or constraint — it adds deployment/ownership framing only).

#### booking-service — database `booking_db`

```sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE bookings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    series_id       UUID NULL,                       -- shared across occurrences of a recurring series
    title           VARCHAR(255) NOT NULL,
    location_id     UUID NOT NULL,                   -- cross-service reference to locations-resources-service
    start_time      TIMESTAMPTZ NOT NULL,
    end_time        TIMESTAMPTZ NOT NULL,
    status          VARCHAR(16) NOT NULL DEFAULT 'pending', -- 'pending' | 'approved' | 'denied'
    owner_id        UUID NOT NULL,                   -- cross-service reference to Keycloak subject / users-permissions-service
    approved_by     UUID NULL,
    approved_at     TIMESTAMPTZ NULL,
    denied_by       UUID NULL,
    denied_at       TIMESTAMPTZ NULL,
    denial_reason   TEXT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ NULL,
    CONSTRAINT chk_booking_time_range CHECK (end_time > start_time),
    CONSTRAINT chk_booking_status CHECK (status IN ('pending','approved','denied'))
);
CREATE INDEX idx_bookings_location_time ON bookings (location_id, start_time, end_time) WHERE deleted_at IS NULL;
CREATE INDEX idx_bookings_series ON bookings (series_id) WHERE series_id IS NOT NULL;
CREATE INDEX idx_bookings_status ON bookings (status) WHERE deleted_at IS NULL;

CREATE TABLE booking_resources (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id      UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    resource_id     UUID NOT NULL,                   -- cross-service reference to locations-resources-service
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_booking_resource UNIQUE (booking_id, resource_id)
);
CREATE INDEX idx_booking_resources_resource_time ON booking_resources (resource_id, booking_id);
```

**Conflict-detection query shape** (not a table — the indexed query pattern `ConflictDetectionService` runs on every create/edit/bulk-read, per FRD F2 §Schema Surface):

```sql
-- Location-level overlap (excludes self on edit; excludes deleted/denied per F2 §Validation interim default)
SELECT id FROM bookings
WHERE location_id = :location_id
  AND deleted_at IS NULL
  AND status <> 'denied'
  AND id <> COALESCE(:exclude_booking_id, '00000000-0000-0000-0000-000000000000'::uuid)
  AND start_time < :proposed_end_time
  AND :proposed_start_time < end_time;

-- Resource-level overlap, per attached resource
SELECT br.booking_id FROM booking_resources br
JOIN bookings b ON b.id = br.booking_id
WHERE br.resource_id = ANY(:resource_ids)
  AND b.deleted_at IS NULL
  AND b.status <> 'denied'
  AND b.id <> COALESCE(:exclude_booking_id, '00000000-0000-0000-0000-000000000000'::uuid)
  AND b.start_time < :proposed_end_time
  AND :proposed_start_time < b.end_time;
```

#### locations-resources-service — database `locres_db`

```sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE locations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    css_class       VARCHAR(100) NULL,
    building        VARCHAR(255) NULL,
    layout          JSONB NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ NULL
);
CREATE INDEX idx_locations_active ON locations (id) WHERE deleted_at IS NULL;

CREATE TABLE resources (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ NULL
);
CREATE INDEX idx_resources_active ON resources (id) WHERE deleted_at IS NULL;
```

#### custom-field-service — database `customfld_db`

```sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE custom_fields (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    label           VARCHAR(255) NOT NULL,
    field_type      VARCHAR(32) NOT NULL,             -- 'text' | 'number' | 'date' | 'select' (pending F0 confirmation)
    options         JSONB NULL,                        -- required when field_type = 'select'
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ NULL
);

CREATE TABLE custom_field_templates (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    context_id      UUID NULL,                         -- e.g., Location id this template applies to; NULL = global
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE custom_field_joins (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    custom_field_template_id UUID NOT NULL REFERENCES custom_field_templates(id) ON DELETE CASCADE,
    custom_field_id          UUID NOT NULL REFERENCES custom_fields(id) ON DELETE CASCADE,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_template_field UNIQUE (custom_field_template_id, custom_field_id)
);

CREATE TABLE custom_field_values (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id      UUID NOT NULL,                     -- cross-service reference to booking-service
    custom_field_id UUID NOT NULL REFERENCES custom_fields(id),
    value           TEXT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_custom_field_values_booking ON custom_field_values (booking_id);
```

#### users-permissions-service — database `userperm_db`

```sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- user module
CREATE TABLE users (
    id              UUID PRIMARY KEY,                  -- matches Keycloak subject (sub claim), not independently generated
    email           VARCHAR(320) NOT NULL UNIQUE,
    display_name    VARCHAR(255) NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ NULL
);
-- No password/hash/salt columns: credential storage is delegated entirely to Keycloak, per "Standards-based identity" NFR.

-- permission module (no FK to users — intentionally decoupled, see §2.5)
CREATE TABLE permissions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    legacy_flag         VARCHAR(100) NOT NULL UNIQUE,   -- e.g., 'accessCalendar', 'allowRoomBooking'
    keycloak_role       VARCHAR(100) NOT NULL,
    gated_actions       JSONB NULL,
    confirmed           BOOLEAN NOT NULL DEFAULT false, -- true only once F0 confirms exact gating scope
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

#### notifications-service — database `notif_db`

```sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE notification_deliveries (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key     VARCHAR(255) NOT NULL UNIQUE,
    event_type          VARCHAR(64) NOT NULL,            -- 'booking.created' | 'booking.approved' | 'booking.denied' | 'password.reset.requested'
    entity_id           UUID NOT NULL,
    status              VARCHAR(16) NOT NULL DEFAULT 'pending', -- 'pending' | 'sent' | 'retrying' | 'dead_lettered'
    attempt_count       INTEGER NOT NULL DEFAULT 0,
    last_attempted_at   TIMESTAMPTZ NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_notification_status ON notification_deliveries (status);
```

#### feeds-service — database `feeds_db` (optional materialized projection)

```sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Denormalized projection, populated by consuming booking.created/updated/approved/denied/deleted
-- and location.updated/deleted events, per §1.4 read-pattern decision.
CREATE TABLE feed_bookings (
    booking_id      UUID PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    location_id     UUID NOT NULL,
    location_name   VARCHAR(255) NOT NULL,
    start_time      TIMESTAMPTZ NOT NULL,
    end_time        TIMESTAMPTZ NOT NULL,
    status          VARCHAR(16) NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_feed_bookings_upcoming ON feed_bookings (status, start_time);
CREATE INDEX idx_feed_bookings_location ON feed_bookings (location_id, start_time);
```

#### settings-service — database `settings_db`

```sql
CREATE TABLE settings (
    id                   INTEGER PRIMARY KEY DEFAULT 1, -- singleton row enforced by CHECK below
    approve_booking      BOOLEAN NOT NULL DEFAULT true,
    calendar_slot_size   INTEGER NOT NULL DEFAULT 30,    -- minutes
    calendar_min_time    TIME NOT NULL DEFAULT '08:00',
    calendar_max_time    TIME NOT NULL DEFAULT '18:00',
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by           UUID NULL,
    CONSTRAINT chk_settings_singleton CHECK (id = 1),
    CONSTRAINT chk_settings_calendar_range CHECK (calendar_min_time < calendar_max_time),
    CONSTRAINT chk_settings_slot_size CHECK (calendar_slot_size > 0)
);
INSERT INTO settings (id) VALUES (1) ON CONFLICT (id) DO NOTHING;
```

#### audit-log-service — database `audit_db`

```sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE audit_log_entries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id        UUID NOT NULL,
    occurred_at     TIMESTAMPTZ NOT NULL,
    entity_type     VARCHAR(64) NOT NULL,              -- 'booking' | 'location' | 'resource' | 'user' | 'role' | 'settings' | 'permission' | ...
    entity_id       UUID NOT NULL,
    action_type     VARCHAR(64) NOT NULL,              -- 'created' | 'updated' | 'deleted' | 'approved' | 'denied' | ...
    before_values   JSONB NULL,
    after_values    JSONB NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_entity ON audit_log_entries (entity_type, entity_id);
CREATE INDEX idx_audit_actor ON audit_log_entries (actor_id);
CREATE INDEX idx_audit_occurred_at ON audit_log_entries (occurred_at);
-- Immutability enforced at the Postgres role level: the application's runtime role
-- is granted INSERT, SELECT only on audit_log_entries — no UPDATE/DELETE grant exists.
REVOKE UPDATE, DELETE ON audit_log_entries FROM PUBLIC;
```

#### F13 traceability tooling — database `traceability_db` (optional; only if not satisfied by external test-management SaaS)

```sql
CREATE TABLE traceability_entries (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    audit_finding_ref   VARCHAR(255) NOT NULL,          -- reference into F0 findings document
    requirement_ref     VARCHAR(255) NOT NULL,          -- e.g., 'FRD F2 §Validation bullet 3'
    implementation_ref  VARCHAR(255) NULL,
    test_ref            VARCHAR(255) NULL,
    status              VARCHAR(16) NOT NULL DEFAULT 'not_started', -- 'not_started'|'implemented'|'tested'|'verified'
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

### 3.4 `updated_at` Trigger (applied per-database)

```sql
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Example attachment (repeated per table with an updated_at column, per service):
CREATE TRIGGER trg_bookings_updated_at BEFORE UPDATE ON bookings
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
```

## 4. API Design

All endpoints are reached exclusively through **Spring Cloud Gateway** (single ingress, per §2.1). The path prefixes below are the logical API paths exposed at the Gateway; the Gateway's route table (§4.9) maps each prefix to its owning backend service. All request/response bodies are JSON unless noted (feed formats are the documented exception). Every endpoint except `GET /feeds/*` and `POST /auth/login`/`POST /auth/password-reset/*` requires a valid Keycloak-issued bearer token. Permission names reference the FRD F7 seed set (`accessCalendar`, `allowRoomBooking`, `viewRoomBooking`, `allowApproveBooking`, `accessPermissions`, `allowAPI`) pending F0's confirmation of the complete, exhaustive set.

### 4.1 Shared TypeScript Types

```typescript
// Shared primitives used across every service's API contracts.
type UUID = string;
type ISODateTime = string; // ISO-8601, e.g. "2026-11-03T14:00:00Z"
type ISODate = string;     // "2026-11-03"
type ISOTime = string;     // "08:00"

interface ApiError {
  error_code: string;
  message: string;
  timestamp: ISODateTime;
  path: string;
}

interface PaginatedResponse<T> {
  items: T[];
  total: number;
  page: number;
  page_size: number;
}

type BookingStatus = "pending" | "approved" | "denied";
type ConflictType = "location" | "resource";
type EditScope = "this_occurrence" | "whole_series";
type CustomFieldType = "text" | "number" | "date" | "select"; // exact enum pending F0 confirmation
type NotificationStatus = "pending" | "sent" | "retrying" | "dead_lettered";
type FeedFormat = "rss2" | "ical" | "json" | "display-board";
```

### 4.2 §Booking (booking-service — FRD F1, F2, F3)

```typescript
interface ConflictFlag {
  conflicting_booking_id: UUID;
  conflict_type: ConflictType;
  location_id?: UUID;
  resource_id?: UUID;
}

interface CustomFieldValueInput {
  field_id: UUID;
  value: string;
}

interface CustomFieldValue extends CustomFieldValueInput {
  label: string;
  field_type: CustomFieldType;
}

interface RecurrenceDefinition {
  // Exact shape pending F0 confirmation (PRD Open Question #1).
  // Interim assumption: weekly-by-day-of-week with an end date.
  pattern: "weekly";
  days_of_week: number[]; // 0 = Sunday .. 6 = Saturday
  end_date: ISODate;
}

interface BookingCreateRequest {
  title: string;
  location_id: UUID;
  start_time: ISODateTime;
  end_time?: ISODateTime; // defaults to start_time + 1h if omitted
  resource_ids?: UUID[];
  custom_field_values?: CustomFieldValueInput[];
  recurrence?: RecurrenceDefinition;
}

interface BookingUpdateRequest {
  title?: string;
  location_id?: UUID;
  start_time?: ISODateTime;
  end_time?: ISODateTime;
  resource_ids?: UUID[];
  custom_field_values?: CustomFieldValueInput[];
  scope?: EditScope; // required if booking belongs to a series
}

interface Booking {
  id: UUID;
  series_id: UUID | null;
  title: string;
  location_id: UUID;
  start_time: ISODateTime;
  end_time: ISODateTime;
  resources: { resource_id: UUID }[];
  custom_field_values: CustomFieldValue[];
  status: BookingStatus;
  owner_id: UUID;
  approved_by: UUID | null;
  approved_at: ISODateTime | null;
  denied_by: UUID | null;
  denied_at: ISODateTime | null;
  denial_reason: string | null;
  conflict_flags: ConflictFlag[];
  created_at: ISODateTime;
  updated_at: ISODateTime;
  deleted_at: ISODateTime | null;
}

interface ConflictCheckRequest {
  location_id?: UUID;
  resource_ids?: UUID[];
  start_time: ISODateTime;
  end_time: ISODateTime;
  exclude_booking_id?: UUID;
}

interface ConflictCheckResult {
  has_conflict: boolean;
  conflicts: ConflictFlag[];
}

interface DenyBookingRequest {
  denial_reason?: string;
}
```

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `POST /bookings` | `BookingCreateRequest` | `201 Booking` (or `Booking[]` for a series) | `allowRoomBooking` |
| `GET /bookings` | query: `from, to, location_id?, resource_id?` | `200 Booking[]` | `viewRoomBooking` |
| `GET /bookings/{id}` | — | `200 Booking` | `viewRoomBooking` |
| `PUT /bookings/{id}` | `BookingUpdateRequest` | `200 Booking` | `allowRoomBooking` (owner) or admin |
| `DELETE /bookings/{id}` | query/body: `scope?: EditScope` | `204` | `allowRoomBooking` (owner) or admin |
| `POST /bookings/{id}/clone` | — | `201 Booking` (draft) | `allowRoomBooking` |
| `POST /bookings/{id}/approve` | — | `200 Booking` | `allowApproveBooking` |
| `POST /bookings/{id}/deny` | `DenyBookingRequest` | `200 Booking` | `allowApproveBooking` |
| `POST /bookings/check-conflicts` | `ConflictCheckRequest` | `200 ConflictCheckResult` | `allowRoomBooking` |

### 4.3 §Locations / §Resources (locations-resources-service — FRD F4)

```typescript
interface LocationUpsertRequest {
  name: string;
  css_class?: string;
  building?: string;
  layout?: Record<string, unknown>; // exact shape pending F0 confirmation
}

interface Location {
  id: UUID;
  name: string;
  css_class: string | null;
  building: string | null;
  layout: Record<string, unknown> | null;
  created_at: ISODateTime;
  updated_at: ISODateTime;
  deleted_at: ISODateTime | null;
}

interface ResourceUpsertRequest {
  name: string;
}

interface Resource {
  id: UUID;
  name: string;
  created_at: ISODateTime;
  updated_at: ISODateTime;
  deleted_at: ISODateTime | null;
}
```

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /locations` | — | `200 Location[]` | `accessCalendar` (read) |
| `POST /locations` | `LocationUpsertRequest` | `201 Location` | admin (location mgmt) |
| `GET /locations/{id}` | — | `200 Location` | `accessCalendar` (read) |
| `PUT /locations/{id}` | `Partial<LocationUpsertRequest>` | `200 Location` | admin (location mgmt) |
| `DELETE /locations/{id}` | — | `204` | admin (location mgmt) |
| `GET /resources` | — | `200 Resource[]` | `accessCalendar` (read) |
| `POST /resources` | `ResourceUpsertRequest` | `201 Resource` | admin (resource mgmt) |
| `GET /resources/{id}` | — | `200 Resource` | `accessCalendar` (read) |
| `PUT /resources/{id}` | `Partial<ResourceUpsertRequest>` | `200 Resource` | admin (resource mgmt) |
| `DELETE /resources/{id}` | — | `204` | admin (resource mgmt) |

### 4.4 §CustomFields (custom-field-service — FRD F5)

```typescript
interface CustomFieldUpsertRequest {
  label: string;
  field_type: CustomFieldType;
  options?: string[]; // required when field_type === "select"
}

interface CustomField {
  id: UUID;
  label: string;
  field_type: CustomFieldType;
  options: string[] | null;
  created_at: ISODateTime;
  updated_at: ISODateTime;
}

interface FieldTemplateUpsertRequest {
  name: string;
  context_id?: UUID; // e.g. a Location id; omitted = global
  field_ids?: UUID[];
}

interface FieldTemplate {
  id: UUID;
  name: string;
  context_id: UUID | null;
  field_ids: UUID[];
  created_at: ISODateTime;
  updated_at: ISODateTime;
}
```

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /custom-fields` | — | `200 CustomField[]` | admin (custom field mgmt) |
| `POST /custom-fields` | `CustomFieldUpsertRequest` | `201 CustomField` | admin (custom field mgmt) |
| `GET /custom-fields/{id}` | — | `200 CustomField` | admin (custom field mgmt) |
| `PUT /custom-fields/{id}` | `Partial<CustomFieldUpsertRequest>` | `200 CustomField` | admin (custom field mgmt) |
| `DELETE /custom-fields/{id}` | — | `204` | admin (custom field mgmt) |
| `GET /field-templates` | — | `200 FieldTemplate[]` | admin (custom field mgmt) |
| `POST /field-templates` | `FieldTemplateUpsertRequest` | `201 FieldTemplate` | admin (custom field mgmt) |
| `GET /field-templates/{id}` | — | `200 FieldTemplate` | admin (custom field mgmt) |
| `PUT /field-templates/{id}` | `Partial<FieldTemplateUpsertRequest>` | `200 FieldTemplate` | admin (custom field mgmt) |
| `DELETE /field-templates/{id}` | — | `204` | admin (custom field mgmt) |

### 4.5 §Auth / §Users (users-permissions-service + Keycloak — FRD F6)

```typescript
interface LoginRequest {
  username: string;
  password: string;
  remember_me?: boolean;
}

interface LoginResponse {
  access_token: string;
  refresh_token: string;
  expires_in: number; // seconds
  roles: string[];
}

interface PasswordResetRequest {
  email: string;
}

interface PasswordResetCompleteRequest {
  reset_token: string;
  new_password: string;
}

interface PasswordChangeRequest {
  current_password: string;
  new_password: string;
}

interface UserCreateRequest {
  email: string;
  initial_role: string;
}

interface UserUpdateRequest {
  display_name?: string;
}

interface User {
  id: UUID; // == Keycloak `sub` claim
  email: string;
  display_name: string | null;
  roles: string[];
  created_at: ISODateTime;
  updated_at: ISODateTime;
}

interface RoleAssignmentRequest {
  roles: string[];
}
```

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `POST /auth/login` | `LoginRequest` | `200 LoginResponse` | public |
| `POST /auth/logout` | — | `204` | authenticated |
| `POST /auth/password-reset/request` | `PasswordResetRequest` | `202` (generic ack) | public |
| `POST /auth/password-reset/complete` | `PasswordResetCompleteRequest` | `200` | public (token-bearing) |
| `POST /auth/password-change` | `PasswordChangeRequest` | `200` | authenticated (self) |
| `GET /users` | — | `200 User[]` | admin (user mgmt) |
| `POST /users` | `UserCreateRequest` | `201 User` | admin (user mgmt) |
| `GET /users/{id}` | — | `200 User` | admin (user mgmt) or self |
| `PUT /users/{id}` | `UserUpdateRequest` | `200 User` | admin (user mgmt) or self |
| `PUT /users/{id}/roles` | `RoleAssignmentRequest` | `200 User` | admin (user mgmt) |
| `GET /users/me` | — | `200 User` | authenticated (self) |
| `PUT /users/me` | `UserUpdateRequest` | `200 User` | authenticated (self) |

### 4.6 §Permissions (users-permissions-service, permission module — FRD F7)

```typescript
interface PermissionMappingEntry {
  legacy_flag: string;
  keycloak_role: string;
  gated_actions: string[];
  confirmed: boolean;
}

interface RolePermissions {
  role_name: string;
  permission_flags: string[];
}

interface RolePermissionsUpdateRequest {
  permission_flags: string[];
}
```

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /permissions` | — | `200 PermissionMappingEntry[]` | `accessPermissions` |
| `GET /roles/{role}/permissions` | — | `200 RolePermissions` | `accessPermissions` |
| `PUT /roles/{role}/permissions` | `RolePermissionsUpdateRequest` | `200 RolePermissions` | `accessPermissions` |

### 4.7 §Notifications (notifications-service — FRD F8; observability-only, no client write API)

```typescript
interface NotificationDeliveryStatus {
  event_id: UUID;
  event_type: string;
  entity_id: UUID;
  status: NotificationStatus;
  attempt_count: number;
  last_attempted_at: ISODateTime | null;
}
```

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /notifications/delivery-status` | query: `event_type?, status?` | `200 NotificationDeliveryStatus[]` | admin (ops/observability) |
| `GET /notifications/dead-letter` | — | `200 NotificationDeliveryStatus[]` | admin (ops/observability) |

### 4.8 §Feeds (feeds-service — FRD F9)

```typescript
interface FeedBookingSummary {
  booking_id: UUID;
  title: string;
  location: { id: UUID; name: string };
  start_time: ISODateTime;
  end_time: ISODateTime;
}
```

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /feeds/rss2` | query: `location_id?` | `200` RSS2 XML | `allowAPI` (pending F0 default-public confirmation) |
| `GET /feeds/ical` | query: `location_id?` | `200` iCal `.ics` | `allowAPI` (pending F0 default-public confirmation) |
| `GET /feeds/json` | query: `location_id?` | `200 FeedBookingSummary[]` | `allowAPI` (pending F0 default-public confirmation) |
| `GET /feeds/display-board` | query: `location_id?` | `200` HTML | `allowAPI` (pending F0 default-public confirmation) |

### 4.9 §Settings (settings-service — FRD F10)

```typescript
interface SettingsUpdateRequest {
  approve_booking?: boolean;
  calendar_slot_size?: number; // minutes, positive integer
  calendar_min_time?: ISOTime;
  calendar_max_time?: ISOTime;
}

interface Settings {
  approve_booking: boolean;
  calendar_slot_size: number;
  calendar_min_time: ISOTime;
  calendar_max_time: ISOTime;
  updated_at: ISODateTime;
  updated_by: UUID | null;
}
```

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /settings` | — | `200 Settings` | authenticated (read scope TBD per F0) |
| `PUT /settings` | `SettingsUpdateRequest` | `200 Settings` | admin (settings mgmt) |

### 4.10 §AuditLog (audit-log-service — FRD F11)

```typescript
interface AuditLogEntry {
  id: UUID;
  actor_id: UUID;
  occurred_at: ISODateTime;
  entity_type: string;
  entity_id: UUID;
  action_type: string;
  before_values: Record<string, unknown> | null;
  after_values: Record<string, unknown> | null;
}

interface AuditLogQuery {
  entity_type?: string;
  entity_id?: UUID;
  actor?: UUID;
  date_from?: ISODate;
  date_to?: ISODate;
}
```

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /audit-log` | query: `AuditLogQuery` | `200 PaginatedResponse<AuditLogEntry>` | admin (log viewing, legacy `Logfiles` equivalent) |

### 4.11 Gateway Routing Table

| Route Prefix | Target Service | Coarse-Grained Role/Scope Required |
|---|---|---|
| `/bookings/**` | booking-service | authenticated with `viewRoomBooking` or `allowRoomBooking` |
| `/locations/**`, `/resources/**` | locations-resources-service | authenticated (read); admin role for write methods |
| `/custom-fields/**`, `/field-templates/**` | custom-field-service | admin role |
| `/auth/**` | users-permissions-service (+ Keycloak) | public (login/reset) or authenticated (logout/change) |
| `/users/**` | users-permissions-service | admin role or self |
| `/permissions/**`, `/roles/**` | users-permissions-service | `accessPermissions` |
| `/notifications/**` | notifications-service | admin/ops role |
| `/feeds/**` | feeds-service | `allowAPI` or public, per F9 pending confirmation |
| `/settings/**` | settings-service | authenticated (read); admin (write) |
| `/audit-log/**` | audit-log-service | admin role |

### 4.12 Cross-Cutting Error Contract

Every endpoint returns errors in the shared `ApiError` shape (§4.1). Platform-level errors (apply to every route) and cross-feature errors are cataloged in full in `FRD-BookingHub.md` §Y2 (`AUTH_UNAUTHENTICATED` 401, `AUTH_TOKEN_EXPIRED` 401, `GATEWAY_FORBIDDEN` 403, `ACTION_FORBIDDEN` 403, `REQUEST_MALFORMED` 400, `ROUTE_NOT_FOUND` 404, `SERVICE_UNAVAILABLE` 503, `RATE_LIMIT_EXCEEDED` 429, `INTERNAL_ERROR` 500, plus every feature-specific code listed per-feature in the FRD). TechArch does not redefine these — the Gateway and every service implement a shared `ApiError`-shaped exception handler (`@ControllerAdvice` in each Spring Boot service; a `GlobalFilter`/`ErrorWebExceptionHandler` in the WebFlux-based Gateway) so the contract is identical regardless of which layer raises the error.

## 5. Security Architecture

### 5.1 Authentication

- **Identity provider:** Keycloak is the sole source of truth for credentials, sessions, and refresh tokens. No Booking-Hub service stores a password hash, salt, or implements its own session mechanism — this is a hard NFR ("Standards-based identity") carried unchanged from the PRD/FRD into this architecture.
- **Frontend flow:** The React SPA is registered as a **public Keycloak client** using the **Authorization Code + PKCE** flow (no client secret in browser-shipped code). On successful login, the SPA holds an access token (short-lived JWT) and a refresh token (longer-lived; extended further when `remember_me` is set, per FRD F6 §Process step 4).
- **Service-to-service flow:** Each backend service is registered as a **confidential, bearer-only Keycloak client** — it never initiates its own login flow; it only validates tokens presented by the Gateway/callers and, where it needs to call another service synchronously (e.g., booking-service validating a `location_id` against locations-resources-service), it forwards the caller's original bearer token (token relay), preserving the original principal's identity and scopes for the downstream fine-grained check.
- **Admin API usage:** users-permissions-service additionally holds a **Keycloak Admin API service account** (client credentials grant, cluster-internal only, never exposed to the frontend) used exclusively for account provisioning, role-mapping updates, and triggering credential-reset flows — the one place in the architecture a service calls Keycloak as an administrative actor rather than just validating tokens.
- **Token validation:** Every component that accepts a bearer token (Gateway and, redundantly, every backend service per §5.2) validates the JWT signature against Keycloak's published JWKS endpoint (cached with standard rotation-aware refresh), checks `exp`/`nbf`/`iss`/`aud` claims, and extracts the `sub` (maps 1:1 to `users.id`) and realm/client role claims.
- **Fail-closed on IdP outage:** If Keycloak (or its JWKS endpoint) is unreachable, the Gateway and every service **reject the request with 503** rather than treat an unverifiable token as valid — the architecture never fails open, per `Y3-integrations.md`'s explicit requirement.
- **Logout:** `POST /auth/logout` triggers Keycloak session/refresh-token revocation (not merely client-side token discard), so a stolen refresh token cannot be used after explicit logout.

### 5.2 Authorization (Two-Tier Enforcement)

Booking-Hub enforces access control at two tiers, matching FRD F7's "coarse-grained at the Gateway, fine-grained per-service" model, with the invariant that **a service must never grant an action the Gateway would have blocked, and a service's fine-grained check must never be weaker than what Gateway routing implies** (FRD F7 §Validation, carried into this architecture as an ArchUnit/contract-test-enforced CI rule).

**Tier 1 — Gateway (coarse-grained, route-level):** Spring Cloud Gateway inspects the validated JWT's realm/client roles against the route table (§4.11) before proxying the request. A request lacking the minimum role for a route prefix is rejected at the edge (401/403) and never reaches a backend pod — this is also a security/cost control (unauthorized traffic never consumes backend compute).

**Tier 2 — Service (fine-grained, action-level):** Each backend service re-validates the token (defense in depth — never trusts the Gateway's decision alone) and applies action-specific checks that require context the Gateway doesn't have, e.g.:
- booking-service: a user with `allowRoomBooking` can edit/delete only bookings they own, *unless* they also hold an admin role; `allowApproveBooking` gates the approve/deny sub-actions specifically, independent of general `/bookings` route access (FRD F7 §Process step 5's explicit example).
- users-permissions-service: a user may always read/edit their *own* profile (`/users/me`) regardless of admin role, but only an admin-role holder may read/edit another user's record or change role mappings.
- settings-service: `GET /settings` is available to any authenticated principal (the calendar needs it to render); `PUT /settings` requires the admin/settings-management role.

**Legacy permission flag → Keycloak role mapping (seed set, pending F0's exhaustive confirmation per PRD Open Question #9):**

| Legacy Permission Flag | Keycloak Realm/Client Role | Enforced At | Gates |
|---|---|---|---|
| `accessCalendar` | `role_calendar_viewer` | Gateway + booking-service, locations-resources-service | Read access to calendar/locations/resources |
| `allowRoomBooking` | `role_booking_creator` | Gateway + booking-service | Create/edit/delete/clone own bookings |
| `viewRoomBooking` | `role_booking_viewer` | Gateway + booking-service | Read bookings (list/calendar/detail) |
| `allowApproveBooking` | `role_booking_approver` | booking-service (fine-grained) | Approve/deny pending bookings |
| `accessPermissions` | `role_permissions_admin` | Gateway + users-permissions-service | View/edit role-to-permission mappings |
| `allowAPI` | `role_feed_api` | Gateway + feeds-service | Non-public feed access (pending F0 default-public confirmation) |
| *(location/resource mgmt — name pending F0)* | `role_location_admin` | Gateway + locations-resources-service | Location/Resource CRUD |
| *(custom field mgmt — name pending F0)* | `role_customfield_admin` | Gateway + custom-field-service | Custom field/template CRUD |
| *(user mgmt — name pending F0)* | `role_user_admin` | Gateway + users-permissions-service | Account creation, role assignment |
| *(settings mgmt — name pending F0)* | `role_settings_admin` | Gateway + settings-service | `PUT /settings` |
| *(audit log viewing — name pending F0)* | `role_audit_viewer` | Gateway + audit-log-service | `GET /audit-log` |

Per FRD F7 §Validation: any flag whose exact gating scope is not yet `confirmed = true` in the `permissions` table is treated as **restrictive (deny-by-default)**, never as "no restriction," until F0 closes the question — this default-deny posture is implemented as the literal default branch in every service's authorization interceptor.

**Role-change propagation:** Per FRD F7 §Process step 7, a role change takes effect on the user's **next issued token** — existing tokens remain valid (and carry the old roles) until natural expiry or explicit revocation, standard JWT semantics. Booking-Hub does not implement token-revocation-on-role-change beyond Keycloak's standard session-invalidation hooks, consistent with the FRD's "confirm legacy parity in F0" flag on this exact point.

### 5.3 Data Protection

- **In transit:** TLS terminated at Spring Cloud Gateway (external) and at Keycloak's own ingress; internal cluster traffic (Gateway → services, service → service, service → RabbitMQ, service → Postgres) runs over the cluster's service mesh / Kubernetes network policy boundary, with mutual TLS between pods where the cluster's service mesh (if adopted) supports it, or at minimum Kubernetes `NetworkPolicy` restricting which pods may reach which — no service's Postgres port is reachable from outside its own service's pod(s) and the Gateway is never a valid source for a direct database connection.
- **At rest:** Each service's PostgreSQL credentials and connection strings are stored as Kubernetes `Secret` objects (not ConfigMaps, not environment variables baked into images), scoped so only that service's Deployment can mount them. `pgcrypto`/native Postgres encryption-at-rest (cloud-provider-managed disk encryption) protects data at the storage layer.
- **No local credential storage:** Reinforcing §5.1 — no table in any Booking-Hub-owned schema (`Y0-schema.md`) contains a password, password hash, or salt column; the `users` table explicitly omits them by design.
- **PII minimization:** `users` stores only `email` and `display_name` — no additional PII beyond what F6 explicitly requires; any additional profile field discovered during F0 is added deliberately, not assumed.
- **Audit immutability:** `audit_log_entries` has no `UPDATE`/`DELETE` grant for any application-level Postgres role (§3.3), making tampering require superuser/DBA-level access outside the application's normal operating path — satisfying the "Auditability" NFR's implicit tamper-resistance expectation.
- **Secrets management:** Keycloak client secrets (for confidential clients), RabbitMQ credentials, and SMTP provider credentials are Kubernetes `Secret`s, optionally backed by an external secret manager (e.g., Vault, cloud KMS-integrated secret store) if the deployment target supports it — not hardcoded in any service's `application.yml` committed to source control.
- **Idempotency keys (notifications):** `notification_deliveries.idempotency_key` doubles as a security control against replay-triggered duplicate emails, not just a reliability control.

### 5.4 Network Exposure Boundary

Per the "Single ingress" NFR, the **only** Kubernetes workload with an externally-reachable `Service`/`Ingress` is Spring Cloud Gateway (and, separately, Keycloak's own login/account UI, which per standard OIDC practice must be reachable by end-user browsers for the redirect-based login flow — this is Keycloak's own ingress, not a Booking-Hub service, and is the one documented exception to "only the Gateway is externally reachable," scoped exclusively to Keycloak's `/auth/realms/*` login/account endpoints). Every one of the nine Booking-Hub-authored services is deployed with `ClusterIP`-only Kubernetes `Service` objects and no `Ingress` resource of their own — verified in CI via a Kubernetes manifest lint rule (FRD F12 §Validation: "No backend service is reachable from outside the cluster except through Spring Cloud Gateway").

## 6. Technology Stack

All entries below marked **(mandated)** are hard constraints from `PROJECT.md`/PRD Section 4 and are not open for substitution. Entries marked **(TechArch choice)** are implementation-level decisions made within those constraints.

| Layer | Technology | Version | Purpose |
|---|---|---|---|
| Frontend framework | React | 18.x | Single-page application UI |
| Frontend language | TypeScript | 5.x | Type-safe frontend development **(mandated)** |
| Frontend build tooling | Vite | 5.x | Dev server + production bundling (TechArch choice) |
| Frontend calendar rendering | FullCalendar (React wrapper) | 6.x | Calendar/day view rendering — functional equivalent of legacy FullCalendar.js, not a pixel-match requirement |
| Frontend OIDC client | `keycloak-js` (or equivalent PKCE-capable OIDC client) | latest stable | Authorization Code + PKCE login flow against Keycloak |
| API Gateway | Spring Cloud Gateway (WebFlux) | Spring Cloud 2023.x / Spring Boot 3.3.x | Single ingress, JWT validation, coarse-grained routing/RBAC **(mandated)** |
| Backend language/runtime | Java | 21 (LTS) | Backend service implementation language **(mandated)** |
| Backend framework | Spring Boot | 3.3.x | Each bounded-context microservice **(mandated)** |
| Backend web layer (business services) | Spring Web MVC (`spring-boot-starter-web`) | bundled w/ Spring Boot 3.3.x | Synchronous REST controllers (all services except Gateway) |
| Backend data access | Spring Data JPA + Hibernate | bundled w/ Spring Boot 3.3.x | Repository layer over each service's PostgreSQL database |
| Database migrations | Flyway | 10.x | Versioned DDL migrations per service (one Flyway history per database) |
| Database | PostgreSQL | 16.x | One database per microservice, no shared schema **(mandated)** |
| DB extensions | `pgcrypto` | bundled w/ PG 16 | `gen_random_uuid()` for UUID primary keys |
| Messaging broker | RabbitMQ | 3.13.x | Inter-service domain events, durable queues, DLQs **(mandated)** |
| Messaging client | Spring AMQP (`spring-boot-starter-amqp`) | bundled w/ Spring Boot 3.3.x | Publisher (outbox relay) and consumer bindings in every event-participating service |
| Outbox relay | Debezium (Postgres CDC) **or** a scheduled polling-publisher bean | Debezium 2.x (if adopted) | Transactional outbox → RabbitMQ relay, per §1.5 decision (TechArch choice between CDC-based or poll-based outbox, finalized during implementation) |
| Identity & Access Management | Keycloak | 25.x | Authentication, session/token management, realm roles/scopes **(mandated)** |
| Keycloak protocol | OpenID Connect (OIDC) / OAuth2, JWT (RS256) | OIDC 1.0 | Frontend login (PKCE), service-to-service bearer validation |
| API documentation | springdoc-openapi (OpenAPI 3) | 2.x | Per-service OpenAPI spec, aggregated at the Gateway for a unified API catalog |
| Containerization | Docker | 24.x+ | One image per service, per the frontend, and reference Dockerfiles for local Keycloak/RabbitMQ/Postgres **(mandated)** |
| Orchestration | Kubernetes | 1.29+ | Deployment/Service/ConfigMap/Secret manifests per service, independent scaling **(mandated)** |
| Package manifests | Helm charts (one per service, or a shared chart with per-service values) | Helm 3.x | Templated Kubernetes manifests (TechArch choice) |
| Email delivery | SMTP relay or transactional email API (provider TBD — e.g., SendGrid/SES/Postmark) | — | notifications-service outbound email (per `Y3-integrations.md`, "exact provider TBD in TechArch" — left as an infrastructure/ops decision, not a functional one) |
| Observability — metrics | Micrometer + Prometheus | bundled w/ Spring Boot 3.3.x / Prometheus 2.x | Per-service metrics export |
| Observability — tracing | Micrometer Tracing (Brave or OpenTelemetry bridge) | bundled w/ Spring Boot 3.3.x | Distributed trace correlation across Gateway → service → RabbitMQ consumer hops |
| Observability — logging | Structured JSON logging (Logback + `logstash-logback-encoder`) shipped to a log aggregator (e.g., ELK/Loki) | — | Centralized log search, correlated via trace ID |
| CI/CD | GitHub Actions (or equivalent) running: build → test → ArchUnit architecture-rule checks → Docker build → Helm/K8s deploy | — | Enforces F12's architectural invariants (no cross-service DB access, no non-Gateway ingress) as automated CI gates, not just documentation |
| Testing — unit/integration | JUnit 5, Mockito, Testcontainers (Postgres + RabbitMQ) | JUnit 5.10.x, Testcontainers 1.19.x | Backend test pyramid per F13's traceability requirement |
| Testing — frontend | Vitest + React Testing Library | latest stable | Frontend component/unit tests |
| Testing — end-to-end | Playwright | latest stable | Full booking-creation-to-notification flow tests (F13 high-risk coverage) |
| Testing — architecture rules | ArchUnit | 1.3.x | CI-enforced checks: no cross-service DB dependency, no non-Gateway `Ingress`, permission-flag-to-mapping-table consistency |

### 6.1 Per-Service Database Naming Convention

| Service | Database Name |
|---|---|
| booking-service | `booking_db` |
| locations-resources-service | `locres_db` |
| custom-field-service | `customfld_db` |
| users-permissions-service | `userperm_db` |
| notifications-service | `notif_db` |
| feeds-service | `feeds_db` |
| settings-service | `settings_db` |
| audit-log-service | `audit_db` |
| (optional) F13 traceability tooling | `traceability_db` |

Each database runs as its own PostgreSQL instance (or, at minimum, its own logical database within a shared PostgreSQL cluster with per-database credentials and no cross-database `postgres_fdw`/`dblink` configured) — the deployment topology diagram in §1.3 treats "one database per service" as a hard boundary enforced at the infrastructure level, not merely a naming convention.

## 7. Integration Points

### 7.1 Keycloak (Identity & Access Management)

- **Consumed by:** Spring Cloud Gateway (JWT validation on every request), every backend service (redundant JWT re-validation per §5.2), users-permissions-service (Admin API for provisioning/role-mapping/credential-reset), React frontend (PKCE login flow).
- **Contract:** Standard OIDC discovery (`/.well-known/openid-configuration`), token endpoint, JWKS endpoint, and userinfo endpoint under the Booking-Hub realm; Keycloak Admin REST API (`/admin/realms/{realm}/...`) for account/role provisioning, consumed only by users-permissions-service's dedicated service-account client.
- **Realm/client configuration:** One public client (frontend, PKCE, no secret), one confidential client per backend service (bearer-only, used solely for token re-validation, not for initiating logins), one confidential service-account client for users-permissions-service's Admin API access. Realm roles/client scopes are defined per the permission mapping table (§5.2) and kept in sync with the `permissions` table via a CI check (FRD F7 §Validation: "every permission flag... must appear in the permission mapping table before implementation").
- **Failure handling:** Fail closed — any service/Gateway component that cannot reach Keycloak's JWKS endpoint (or gets a non-200 from it) treats every token as unverifiable and returns `503`, never `200`/success. This is a hard requirement, not a tunable, per the "Standards-based identity" NFR.
- **Token refresh:** Access tokens are short-lived (recommended 5–15 min); refresh tokens have a longer, configurable lifetime, extended further when `remember_me` was set at login (exact legacy-equivalent duration is an F0 open item — interim: a configurable realm-level "remember me" session/token lifetime override, not hardcoded in application code).

### 7.2 RabbitMQ (Messaging)

**Topology** — one topic exchange per bounded context publishing events, with durable per-consumer queues bound by routing key, plus a dead-letter exchange/queue pair per consumer queue:

| Exchange | Routing Keys Published | Publisher | Consumers (Queue) |
|---|---|---|---|
| `booking.events` (topic) | `booking.created`, `booking.updated`, `booking.deleted`, `booking.approved`, `booking.denied` | booking-service | `notifications.booking.q` (notifications-service), `audit.booking.q` (audit-log-service), `feeds.booking.q` (feeds-service) |
| `location.events` (topic) | `location.created`, `location.updated`, `location.deleted` | locations-resources-service | `audit.location.q` (audit-log-service), `feeds.location.q` (feeds-service) |
| `resource.events` (topic) | `resource.created`, `resource.updated`, `resource.deleted` | locations-resources-service | `audit.resource.q` (audit-log-service) |
| `customfield.events` (topic) | `customfield.created`, `customfield.updated`, `customfield.deleted`, `template.created`, `template.updated`, `template.deleted` | custom-field-service | `audit.customfield.q` (audit-log-service) |
| `user.events` (topic) | `user.created`, `user.updated`, `role.assigned`, `password.reset.requested` | users-permissions-service | `audit.user.q` (audit-log-service); `notifications.passwordreset.q` (notifications-service, bound to `password.reset.requested` only) |
| `permission.events` (topic) | `permission.updated` | users-permissions-service | `audit.permission.q` (audit-log-service) |
| `settings.events` (topic) | `settings.updated` | settings-service | `audit.settings.q` (audit-log-service); optionally consumed by booking-service if the broadcast-cache-invalidation path (§1.5, rejected default) is later adopted |

**Per-queue dead-letter configuration:** every consumer queue above (`notifications.*.q`, `audit.*.q`, `feeds.*.q`) is declared with `x-dead-letter-exchange` pointing to a matching `*.dlq` exchange/queue pair. Message processing failures are retried with exponential backoff (e.g., 3 attempts: 5s/30s/2min) via a retry-count header; exhaustion routes to the DLQ, surfaced via a monitoring alert on DLQ depth > 0 — never a silent drop, per F8/F11's explicit requirement.

- **Delivery semantics:** At-least-once. Every event payload carries an `idempotency_key` (UUID, generated by the publisher at outbox-write time). notifications-service and audit-log-service (and feeds-service's projection updater) are idempotent consumers — redelivery of an already-processed `idempotency_key` is a safe no-op (checked against `notification_deliveries.idempotency_key` for notifications-service; audit-log-service and feeds-service use their own dedup mechanism keyed the same way, e.g. an `ON CONFLICT DO NOTHING` upsert or a processed-keys lookup table).
- **Publisher guarantee (outbox pattern):** Every publishing service writes the domain event to a local `outbox` table **in the same database transaction** as the business state change (e.g., booking-service inserts into `bookings` and `outbox` atomically). A separate relay process (Debezium CDC tailing the outbox table, or a scheduled polling-publisher bean — see §6 tech stack) reads committed outbox rows and publishes them to RabbitMQ, marking them relayed. This guarantees: (a) an event is never published for a transaction that rolled back, and (b) a RabbitMQ outage delays but never loses or blocks the originating business transaction — directly satisfying the "Event-driven decoupling" NFR's requirement that "a notification or audit failure cannot block the originating transaction."
- **Queue durability:** All exchanges/queues are declared `durable: true`; messages are published `persistent: true`, so a RabbitMQ node restart does not lose queued-but-unconsumed events.

### 7.3 SMTP / Transactional Email Provider

- **Consumed by:** notifications-service exclusively — no other service sends email directly.
- **Contract:** notifications-service renders a notification template (plain text + HTML parts) per event type and dispatches via a configured SMTP relay or transactional-email HTTP API (provider selection is an operational/infrastructure decision, left open per `Y3-integrations.md` — "exact provider TBD in TechArch" — and parameterized via Kubernetes `ConfigMap`/`Secret` so swapping providers requires no code change, only configuration).
- **Failure handling:** Transient provider failures (timeout, 5xx) trigger the RabbitMQ-level retry/backoff/DLQ path described in §7.2 — the provider integration itself contains no business logic (recipient resolution and template selection happen inside notifications-service before the provider call), keeping the provider swap blast radius minimal.
- **Template parity:** Notification template content/intent must match legacy email templates (booking created/approved/denied, password reset) — exact legacy copy is an F0 audit input, not invented by this architecture; templates are stored as versioned resources (e.g., Thymeleaf or FreeMarker templates bundled in notifications-service) so content changes don't require a schema change.

### 7.4 Docker / Kubernetes (Deployment Platform)

- **Consumed by:** every Booking-Hub-authored service, the frontend static-asset server, and (as third-party workloads deployed into the same cluster) Keycloak, RabbitMQ, and the nine PostgreSQL instances.
- **Per-service artifacts:** one multi-stage `Dockerfile` (Java services: Maven build stage → minimal JRE 21 runtime stage, e.g. `eclipse-temurin:21-jre-alpine`; frontend: Vite build stage → `nginx:alpine` static-serve stage) and one Helm chart (or raw manifest set) containing `Deployment`, `Service` (`ClusterIP` for all nine business services; `LoadBalancer`/`Ingress` for Gateway and Keycloak only), `ConfigMap` (non-secret config, e.g. `approve_booking` defaults, RabbitMQ exchange names), and `Secret` (DB credentials, Keycloak client secrets, SMTP credentials, RabbitMQ credentials).
- **Independent deployability:** Each service's Helm release is versioned and deployed independently — no shared "deploy-all" manifest, no implicit startup-order dependency enforced by Kubernetes ordering (services handle a temporarily-unavailable dependency via the `503`/retry contract in `Y2-errors.md`, not by assuming deploy order). Liveness/readiness probes are defined per service (`/actuator/health/liveness`, `/actuator/health/readiness` via Spring Boot Actuator) so Kubernetes can restart/route around an unhealthy pod of one service without affecting others.
- **Database provisioning:** Each service's Flyway migrations run as a Kubernetes `Job` (init-container or pre-upgrade Helm hook) against that service's own database only — no migration job is ever granted credentials to another service's database.

### 7.5 Public Demo / Documentation (reference-only, not a runtime dependency)

- **Consumed by:** F0 (Legacy Functional Audit) exclusively, as a corroboration source during the audit phase. `roombooking.oxalto.co.uk` (legacy demo) and `roombooking.readme.io` (legacy docs) have no runtime integration with Booking-Hub and are out of scope for this architecture beyond being inputs to the F0 findings document that grounds this TechArch and the FRD it implements.
