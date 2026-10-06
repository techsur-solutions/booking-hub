## F6: User & Role Management

**Description:** User & Role Management covers account creation/administration, self-service profile/password management, password reset, and login/logout/session behavior — equivalent to legacy `Users`, `Sessions`, and `PasswordResets` controllers — re-platformed onto Keycloak so that authentication, credential storage, and session/token management are standards-based rather than hand-rolled, while preserving equivalent user-facing capability.

**Terminology:**
- **Keycloak realm:** The identity domain hosting Booking-Hub user accounts, credentials, and roles, replacing legacy `User.password`/salt storage.
- **Profile-adjacent data:** Non-credential user data (display name, contact email for notifications, etc.) that may be owned by a lightweight User/Identity-adjacent Service rather than Keycloak itself, if Keycloak's user-attribute model is insufficient `[to be confirmed in TechArch]`.
- **Access token / Refresh token:** Keycloak-issued JWT tokens used by the client and validated at the Gateway and per-service.
- **Remember me:** Extended session/token lifetime behavior equivalent to legacy "remember me" checkbox.

**Sub-features:**
- Admin-facing account creation/management
- Self-service "my account" profile editing
- Self-service password change
- Password reset flow (request → emailed token/link → reset)
- Login/logout and "remember me" session behavior
- Access-denied handling
- Role assignment per user (feeding F7)

**Process:**
1. Admin user (holding the appropriate permission, F7) creates a new user account by submitting `email`/`username` and initial role assignment; service provisions the account in the Keycloak realm (via Keycloak Admin API) rather than storing a password hash locally.
2. New user receives an invitation/initial-credential mechanism equivalent to legacy account creation `[OPEN QUESTION — deferred to F0: does legacy auto-generate a password, send an invite link, or require admin-set initial password? Unconfirmed]`.
3. User logs in by submitting credentials; the client authenticates against Keycloak directly (or via a backend-for-frontend flow) and receives an access token and refresh token.
4. If the user selects "remember me", the issued refresh token (or session) has an extended lifetime equivalent to the legacy "remember me" duration `[OPEN QUESTION — deferred to F0: exact legacy remember-me duration unconfirmed]`.
5. User logs out; client discards tokens and/or the service revokes the Keycloak session/refresh token.
6. User accesses "my account" and edits profile-adjacent fields (e.g., display name); service persists changes either to Keycloak user attributes or a dedicated profile-adjacent store, publishing a profile-updated event if other services need to react.
7. User requests a password change (while authenticated) by submitting current and new password; request is delegated to Keycloak's credential-update mechanism, replacing legacy salted/hashed password validation and storage.
8. User requests a password reset (unauthenticated) by submitting their email/username; service triggers Keycloak's reset-credential flow (or a custom flow backed by Keycloak), publishing a `password.reset.requested` domain event consumed by the Notification Service (F8) to send the reset link/token email.
9. User follows the reset link/token and submits a new password; Keycloak validates the token and updates the credential.
10. Any request to a protected resource by an unauthenticated or insufficiently-privileged user triggers access-denied handling equivalent to legacy `Sessions` access-denied behavior (redirect to login, or 401/403 response for API calls).
11. Admin assigns/changes a user's role(s); this updates the user's Keycloak role mappings, which F7's Permission System reads to gate subsequent actions.

**Inputs:**
- `email` / `username` (string, required): Account identifier.
- `initial_role` (string, required for admin-created accounts): Role to assign at creation.
- `current_password` (string, required for authenticated password change).
- `new_password` (string, required for password change/reset).
- `remember_me` (boolean, optional, default `false`): Extended session flag at login.
- `reset_token` (string, required for completing a password reset): Token from the emailed reset link.

**Outputs:**
- User representation: `id`, `email`/`username`, `display_name`, `roles[]`, `created_at`, `updated_at`.
- Login response: access token, refresh token, token expiry, assigned roles/scopes.
- Password reset request response: generic success acknowledgment (does not reveal whether the email/username exists, to avoid user enumeration — standard security practice; `[confirm legacy parity in F0]`).

**Validation:**
- `email`/`username` is required and must be unique among active accounts.
- `new_password` must meet Keycloak's configured password policy (replacing legacy hand-rolled complexity rules) — the policy's effective strictness should be configured to be no weaker than the legacy complexity rules `[OPEN QUESTION — deferred to F0: legacy password complexity rules unconfirmed]`.
- `reset_token` must be valid (unexpired, unused) at the time of reset completion; expired/used tokens are rejected.
- Only users holding the admin permission (F7) may create accounts or assign roles to other users; a user may always edit their own profile and change their own password.

**Error States:**
| Scenario | HTTP Status | Error Code | Message |
|---|---|---|---|
| Login with invalid credentials | 401 | AUTH_INVALID_CREDENTIALS | "Invalid username or password" |
| Access to protected resource without valid token | 401 | AUTH_UNAUTHENTICATED | "Authentication required" |
| Access to protected resource with valid token but insufficient role | 403 | AUTH_FORBIDDEN | "You do not have permission to perform this action" |
| Account creation with duplicate `email`/`username` | 409 | USER_ALREADY_EXISTS | "An account with this email/username already exists" |
| Password reset completion with expired/invalid/used token | 400 | PASSWORD_RESET_TOKEN_INVALID | "This password reset link is invalid or has expired" |
| Password change with incorrect `current_password` | 401 | PASSWORD_CHANGE_INVALID_CURRENT | "Current password is incorrect" |
| New password fails Keycloak policy | 400 | PASSWORD_POLICY_VIOLATION | "New password does not meet complexity requirements" |

**API Surface (this feature):** see `Y1-api.md` §Auth and §Users (`POST /auth/login`, `POST /auth/logout`, `POST /auth/password-reset/request`, `POST /auth/password-reset/complete`, `GET/POST /users`, `GET/PUT /users/{id}`, `PUT /users/{id}/roles`, `GET/PUT /users/me`).

**Schema Surface (this feature):** credentials and core identity are owned by Keycloak's own realm database (not a Booking-Hub-owned schema). Any profile-adjacent data not modeled in Keycloak is owned by a dedicated `users` table in a User/Identity-adjacent Service — see `Y0-schema.md` §User. No service stores password hashes directly, per the "Standards-based identity" NFR.
