## F7: Permission System

**Description:** The Permission System re-implements the legacy fine-grained permission matrix as Keycloak roles/scopes, enforced both coarsely at the Spring Cloud Gateway (route-level access) and finely within individual services (action-level checks), preserving the same effective access-control behavior users experience in the legacy system while replacing hand-rolled permission storage.

**Terminology:**
- **Permission flag:** A named legacy access gate (e.g., `accessCalendar`, `allowRoomBooking`, `viewRoomBooking`, `allowApproveBooking`, `accessPermissions`, `allowAPI`). This set is explicitly non-exhaustive per PRD Section 5 F7 and PRD Open Question #9.
- **Keycloak role/scope:** The new-system equivalent of a permission flag, issued as a claim in the user's access token.
- **Coarse-grained enforcement:** Route-level allow/deny at the Gateway based on token role/scope presence, before a request reaches a backend service.
- **Fine-grained enforcement:** Action-level checks within a service (e.g., "can this user approve this specific booking") that may require more context than the Gateway has.
- **Permission mapping table:** The explicit 1:1 (or documented N:1) mapping from every legacy permission flag to its Keycloak role/scope equivalent, a required F0/F7 output.

**Sub-features:**
- Permission matrix definition (legacy flags → Keycloak roles/scopes)
- Role-to-permission mapping administration
- Gateway-level coarse-grained enforcement
- Service-level fine-grained enforcement
- Full legacy-to-Keycloak permission mapping table (F0 audit output)

**Process:**
1. F0 audit produces the complete, confirmed list of legacy permission flags and the exact controller action(s) each one gates (PRD Open Question #9) — this FRD lists the known non-exhaustive seed set (`accessCalendar`, `allowRoomBooking`, `viewRoomBooking`, `allowApproveBooking`, `accessPermissions`, `allowAPI`) pending that confirmation.
2. Each confirmed legacy permission flag is mapped 1:1 to a Keycloak client role or scope, recorded in the permission mapping table; any flag whose gating scope cannot be confirmed is flagged `[OPEN QUESTION — deferred to F0]` and not implemented until resolved, rather than approximated.
3. Admin user (holding `accessPermissions` or its Keycloak equivalent) manages role-to-permission mappings via an admin screen equivalent to legacy `Permissions` controller — assigning which roles carry which permission flags.
4. Spring Cloud Gateway is configured with route-to-role/scope rules so that requests lacking the required role/scope for a route are rejected at the edge (401/403) before reaching a backend service.
5. Each backend service additionally performs fine-grained checks for actions where Gateway-level routing alone is insufficient (e.g., `allowApproveBooking` gates the specific approve/deny action within the Booking Service, not just a route prefix; a user might have access to `/bookings` generally but not to the approve sub-action).
6. Every feature chunk in this FRD (F1, F3, F4, F5, F6, F9, F10, F11) that gates an action by permission references this feature for the enforcement mechanism and cites the specific legacy permission flag involved.
7. Changes to a user's role (F6 §Process step 11) take effect on the user's next issued token (immediate for new logins; existing tokens remain valid until natural expiry or explicit revocation, per standard JWT semantics) `[confirm whether legacy had equivalent immediate-vs-next-session semantics in F0]`.

**Inputs:**
- `role_name` (string, required): Role being configured.
- `permission_flags` (array of string, required): Set of permission flags assigned to the role.

**Outputs:**
- Permission mapping table representation: `{legacy_flag, keycloak_role_or_scope, gated_actions[], confirmed: boolean}`.
- Role representation: `{role_name, permission_flags[]}`.

**Validation:**
- Every permission flag referenced by any other feature chunk in this FRD must appear in the permission mapping table before that feature's permission-gated action is implemented.
- A permission flag whose gating scope is marked unconfirmed (`confirmed: false`) must not be silently treated as "no restriction" — the safe interim default is to treat it as restrictive (deny by default) until F0 confirms otherwise, per PRD's ambiguity-handling NFR.
- Only users holding `accessPermissions` (or its confirmed Keycloak equivalent) may modify role-to-permission mappings.
- Gateway-level and service-level enforcement must agree: a service must never grant an action that the Gateway would have blocked for the same role, and a service's fine-grained check must never be weaker than what the Gateway's coarse-grained check implies.

**Error States:**
| Scenario | HTTP Status | Error Code | Message |
|---|---|---|---|
| Request to a Gateway-protected route without required role/scope | 401 or 403 | GATEWAY_FORBIDDEN | "Access denied for this resource" |
| Service-level fine-grained check fails despite passing Gateway routing | 403 | ACTION_FORBIDDEN | "You do not have permission to perform this action" |
| Attempt to modify role-permission mapping without `accessPermissions` | 403 | PERMISSIONS_FORBIDDEN | "You do not have permission to manage permissions" |
| Reference to an unconfirmed/undefined permission flag in a mapping update | 400 | PERMISSION_FLAG_UNDEFINED | "Unknown or unconfirmed permission flag" |

**API Surface (this feature):** see `Y1-api.md` §Permissions (`GET/PUT /roles/{role}/permissions`, `GET /permissions`). Enforcement itself is cross-cutting (Gateway filter + per-service interceptor/annotation), not a single feature-owned endpoint set.

**Schema Surface (this feature):** role definitions and role-to-permission mappings are primarily modeled as Keycloak realm roles/client scopes (owned by Keycloak, not a Booking-Hub service database). If a supplementary mapping-metadata table is needed (e.g., to store the legacy-flag-to-Keycloak-role mapping table itself for admin UI display), it is owned by a dedicated `permissions` table — see `Y0-schema.md` §Permission.
