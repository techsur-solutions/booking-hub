
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
