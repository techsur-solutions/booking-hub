## Y3: Integration Points (External Systems)

This chunk documents every external system Booking-Hub depends on, the contract each feature relies on, and failure-handling expectations. All integrations are owned by F12 (platform) at the infrastructure level, but consumed functionally by the features noted.

### Keycloak (Identity & Access Management)

- **Consumed by:** F6 (User & Role Management), F7 (Permission System), F12 (Gateway auth enforcement).
- **Contract:** Keycloak realm exposes OIDC endpoints (`/auth/realms/{realm}/protocol/openid-connect/token`, `/userinfo`, etc.) and an Admin REST API for account/role provisioning. Booking-Hub services validate bearer JWTs against the realm's published JWKS; no service independently verifies passwords.
- **Roles/scopes:** Keycloak client roles map 1:1 (or documented N:1) to legacy permission flags per F7's permission mapping table (e.g., `accessCalendar`, `allowRoomBooking`, `viewRoomBooking`, `allowApproveBooking`, `accessPermissions`, `allowAPI`).
- **Failure handling:** If Keycloak is unreachable, the Gateway and all services must fail closed (reject requests with `503`), never fail open (never grant access when the identity provider cannot be consulted) — this is a hard requirement given the "Standards-based identity" NFR.
- **Token refresh:** Access tokens are short-lived; refresh tokens (extended lifetime if `remember_me` was set per F6) are used to obtain new access tokens without re-prompting credentials.

### RabbitMQ (Messaging)

- **Consumed by:** F1/F3 (publish `booking.created`, `booking.updated`, `booking.deleted`, `booking.approved`, `booking.denied`), F4 (publish `location.*`, `resource.*`), F5 (publish custom-field change events), F6 (publish `user.*`, `role.assigned`, `password.reset.requested`), F7 (publish `permission.updated`), F10 (publish `settings.updated`), F8 (consume booking/password-reset events), F11 (consume all change events).
- **Contract:** Each event is published to a durable exchange with a stable routing key per event type (see F12 §Process step 3 for the full event inventory). Each consuming service binds its own durable queue to the relevant routing keys — publishers never address consumer queues directly (decoupled, per "Event-driven decoupling" NFR).
- **Delivery semantics:** At-least-once delivery; consumers (F8, F11) must be idempotent (idempotency key on each event payload) to tolerate redelivery without duplicate side effects.
- **Failure handling:** Failed message processing is retried with backoff; retry exhaustion routes to a dead-letter queue per event type, surfaced via observability/alerting rather than silently dropped (per F8 §Process steps 7–8, F11 §Process step 6).
- **Publisher guarantee:** A publishing service's own transaction (e.g., Booking Service persisting a new Booking) must not be blocked or rolled back by a RabbitMQ outage — events are published after the local transaction commits (outbox pattern or equivalent), per "Event-driven decoupling" NFR ensuring a notification/audit failure cannot block the originating transaction.

### SMTP / Email Provider

- **Consumed by:** F8 (Notifications) exclusively.
- **Contract:** Notification Service sends rendered email templates via a configured SMTP relay or transactional email API (exact provider TBD in TechArch).
- **Failure handling:** Transient provider failures trigger the retry/DLQ path defined in F8 §Process steps 7–8; the provider integration itself does not implement business logic (recipient resolution, template selection happen in the Notification Service before the provider call).
- **Content parity:** Notification templates must be equivalent in content/intent to legacy email templates (F8 §Description); exact legacy template content is an F0 audit input, not invented here.

### Public Demo / Documentation (reference-only, not a runtime dependency)

- **Consumed by:** F0 (Legacy Functional Audit) only, as a corroboration source.
- **`roombooking.oxalto.co.uk`:** Legacy demo site, used to observe actual UI/UX behavior where code alone is ambiguous.
- **`roombooking.readme.io`:** Legacy documentation site, used to corroborate code-derived findings.
- **Note:** Neither is a runtime integration of Booking-Hub itself; both are inputs to the F0 audit process only and have no bearing on F1–F13 runtime behavior beyond the findings they produce.

### Kubernetes / Docker (Deployment Platform)

- **Consumed by:** F12 exclusively, as the deployment substrate for every other feature's owning service.
- **Contract:** Each service ships a Docker image and a set of Kubernetes manifests (Deployment, Service, ConfigMap, Secret) enabling independent build/deploy/scale, per the "Deployability" NFR.
- **Failure handling:** Standard Kubernetes liveness/readiness probes per service; a single service's pod failures must not cascade to other services (enforced by the service-isolation and single-ingress architectural rules in F12).
