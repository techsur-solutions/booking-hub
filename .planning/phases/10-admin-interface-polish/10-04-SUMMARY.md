---
phase: 10-admin-interface-polish
plan: 04
subsystem: ui
tags: [react, typescript, keycloak, playwright, admin, user-management, role-assignment, privilege-escalation]

# Dependency graph
requires:
  - phase: 09-core-user-interface
    provides: App shell navigation (09-01), StateConfirmationBanner (09-03), PermissionGate (09-03)
  - phase: 03-identity-access-control
    provides: UserController endpoints (/users, /users/me), UserSelfUpdateRequest type guard (03-04)
provides:
  - UsersAdmin page at /admin/users with Name/Email/Roles/Active/Actions table
  - UserCreateForm with 17 Keycloak roles multi-select and inline validation
  - UserEditForm with conditional field rendering (admin vs self restrictions)
  - UserCreate page (/admin/users/new) calling POST /users
  - UserEdit page (/admin/users/:id/edit) calling PUT /users/:id or PUT /users/me
  - usersApi client with getAll, getById, create, update, updateSelf, updateRoles methods
  - Playwright tests covering CRUD and admin vs self field restrictions
affects: [10-05, future user management features]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Admin vs self edit restriction: isSelfEdit = keycloak.tokenParsed.sub === userId from route param"
    - "Conditional field rendering: roles/password structurally absent (not disabled) in self-edit mode"
    - "UserSelfUpdateRequest type-level guard: no password/roles fields at compile time (mirrors Phase 3 backend type)"
    - "StateConfirmationBanner after every mutation with navigate-back-on-success pattern"
    - "E2E role + user ID injection via window.__E2E_ROLES__ and window.__E2E_USER_ID__ for hermetic tests"

key-files:
  created:
    - frontend/src/components/admin/UserTable.tsx
    - frontend/src/components/admin/UserCreateForm.tsx
    - frontend/src/components/admin/UserEditForm.tsx
    - frontend/src/pages/admin/UserCreate.tsx
    - frontend/src/pages/admin/UserEdit.tsx
    - frontend/e2e/admin/users-admin.spec.ts
  modified:
    - frontend/src/api/users.ts
    - frontend/src/pages/admin/UsersAdmin.tsx
    - frontend/src/routes/index.tsx

key-decisions:
  - "isSelfEdit determined by comparing keycloak.tokenParsed.sub (via keycloak.subject in E2E) to userId route param — reads Keycloak sub claim directly, consistent with Phase 3 backend isSelf() pattern"
  - "Roles fieldset structurally absent (null) in self-edit mode, not disabled — matches UI-PATTERN-03 deny-by-default structural absence pattern"
  - "17 Keycloak roles hardcoded in form components (no /roles API call) — avoids extra API call on form load; list matches F0 audit BF-062 Phase 1 audit finding"
  - "No password field in either edit mode (admin or self) — password changes via My Account → Change Password or admin reset flow only; creates intentional asymmetry with Create form"
  - "UserTable manages own data fetch (usersApi.getAll) — avoids prop-drilling from page; consistent with UserEdit fetching its own user via usersApi.getById"

patterns-established:
  - "Pattern: Admin-vs-self guard in frontend: isSelfEdit = currentUserId === userId; conditional field rendering removes roles/password structurally"
  - "Pattern: E2E user ID injection via window.__E2E_USER_ID__ injected by AuthProvider applyE2EMockRoles, readable as keycloak.subject"
  - "Pattern: Route pages for Create/Edit (/admin/users/new, /admin/users/:id/edit) — matches LocationCreate/LocationEdit pattern from Phase 10-01"

# Metrics
duration: 5min
completed: 2026-10-15
---

# Phase 10 Plan 04: Users Admin with Admin vs Self Restrictions Summary

**Users Admin table with CRUD forms enforcing admin vs self field restrictions: admins can edit all fields for other users (name/email/roles), while self-edit hides roles and password fields to prevent privilege escalation via the compile-time UserSelfUpdateRequest type guard and structural DOM absence**

## Performance

- **Duration:** 5 min
- **Started:** 2026-10-15T02:15:57Z
- **Completed:** 2026-10-15T02:20:39Z
- **Tasks:** 4 completed
- **Files modified:** 9

## Accomplishments

- Users Admin table at `/admin/users`: Name, Email, Roles (comma-separated), Active badge, Edit/Deactivate actions
- Create form for admins: display_name (optional), email (required), temporary password, 17 Keycloak roles multi-select (from F0 audit BF-062); inline validation per F0 BF-077 baseline (≥6 chars, ≥1 digit, ≥1 lowercase)
- Edit form with conditional rendering: self-edit shows display_name + read-only email; admin edit shows all fields; NO password field in either mode
- `usersApi` client expanded with typed request interfaces (UserCreateRequest, UserUpdateRequest, UserSelfUpdateRequest) and methods (getAll, getById, create, update, updateSelf, updateRoles)
- 3 Playwright tests all passing: create user with roles, admin editing another user, user editing self with restrictions (verifying /users/me endpoint is used)

## Task Commits

Each task was committed atomically:

1. **Task 1: Users Admin table view** — `010dd72` (feat)
2. **Task 2: User Create form with role assignment** — `ca556f7` (feat)
3. **Task 3: User Edit form with admin vs self restrictions** — `43ae458` (feat)
4. **Task 4: Playwright tests for Users Admin CRUD and admin vs self restrictions** — `5773d98` (feat)

**Plan metadata:** (docs commit)

## Files Created/Modified

### Created (6 files)
- `frontend/src/components/admin/UserTable.tsx` — Data table with Name/Email/Roles/Active badge columns; fetches via `usersApi.getAll()`; Edit → `/admin/users/:id/edit`; Deactivate/Activate toggle
- `frontend/src/components/admin/UserCreateForm.tsx` — Admin-only create form with all 17 Keycloak roles multi-select, inline validation, temporary password label
- `frontend/src/components/admin/UserEditForm.tsx` — Edit form with conditional fields based on `isSelfEdit`; uses `keycloak.tokenParsed.sub === userId` guard; roles absent in self-edit
- `frontend/src/pages/admin/UserCreate.tsx` — Route page for `/admin/users/new`; calls `usersApi.create()`; shows `StateConfirmationBanner("User created — temporary password set")`
- `frontend/src/pages/admin/UserEdit.tsx` — Route page for `/admin/users/:id/edit`; fetches user; routes to `usersApi.updateSelf()` or `usersApi.update()` based on `isSelfEdit`
- `frontend/e2e/admin/users-admin.spec.ts` — 3 Playwright tests covering create with roles, admin edit, self edit with restricted fields

### Modified (3 files)
- `frontend/src/api/users.ts` — Expanded with typed interfaces and aliased API methods; `UserSelfUpdateRequest` has no password/roles fields (compile-time guard)
- `frontend/src/pages/admin/UsersAdmin.tsx` — Replaced placeholder with real page using `UserTable` and "Create User" button
- `frontend/src/routes/index.tsx` — Added `/admin/users/new` (UserCreate) and `/admin/users/:id/edit` (UserEdit) routes

## Decisions Made

1. **isSelfEdit via keycloak.tokenParsed.sub:** Reading `keycloak.tokenParsed?.sub ?? keycloak.subject` (subject fallback for E2E mode where AuthProvider injects via `window.__E2E_USER_ID__`) and comparing to the userId route param is the frontend mirror of Phase 3's `CurrentUserProvider.isSelf()` pattern. Consistent approach avoids additional API calls.

2. **17 roles hardcoded in form components:** Rather than fetching from a `/roles` API endpoint (which may not exist yet), the forms hardcode the complete 17-role list from Phase 1 F0 audit BF-062. This is a reasonable choice since the role set is stable in v1.0 and avoids an extra network request on form load.

3. **No password field in edit forms:** Both self-edit and admin-edit forms omit the password field entirely. Password changes go through the dedicated My Account → Change Password flow (Phase 9) or an admin password reset action (separate from edit). This is deliberately stricter than the Create form which requires a temporary password.

4. **UserTable manages its own data fetch:** The table component calls `usersApi.getAll()` internally rather than receiving data as props from `UsersAdmin`. This reduces prop-drilling and mirrors the pattern of other components (e.g., `BookingCreate` fetching its own locations). The component also accepts optional `users[]` props for cases where parent needs control.

## Deviations from Plan

None — plan executed exactly as written. All verify commands pass:

- `BUILD OK`: `npm run build` → exit 0
- `API CLIENT WIRED`: `grep -q "usersApi.getAll" UserTable.tsx`
- `ROLES COLUMN PRESENT`: `grep -q "Roles" UserTable.tsx`
- `ROLE MULTI-SELECT PRESENT`: `grep -q "role_calendar_viewer\|role_booking_approver" UserCreateForm.tsx`
- `TEMPORARY PASSWORD NOTED`: `grep -q "Temporary" UserCreateForm.tsx`
- `ADMIN VS SELF LOGIC PRESENT`: `grep -q "isSelfEdit\|currentUserId" UserEditForm.tsx`
- `SELF-UPDATE API WIRED`: `grep -q "updateSelf\|UserSelfUpdateRequest" UserEditForm.tsx`
- `PLAYWRIGHT PASSED`: 3/3 tests passing

## Known Stubs

None found — all components have complete implementations with real API calls.

## Issues Encountered

None.

## User Setup Required

None — no external service configuration required.

## Next Phase Readiness

- Users Admin fully functional: table, create, edit with admin vs self restrictions
- `usersApi` client is now the canonical integration point for Phase 3 user management backend endpoints
- Playwright test pattern extended: `window.__E2E_USER_ID__` injection enables testing of per-user-id routing decisions
- Ready for remaining Phase 10 admin interface polish plans

## Self-Check

- [x] `frontend/src/components/admin/UserTable.tsx` — EXISTS
- [x] `frontend/src/components/admin/UserCreateForm.tsx` — EXISTS
- [x] `frontend/src/components/admin/UserEditForm.tsx` — EXISTS
- [x] `frontend/src/pages/admin/UserCreate.tsx` — EXISTS
- [x] `frontend/src/pages/admin/UserEdit.tsx` — EXISTS
- [x] `frontend/e2e/admin/users-admin.spec.ts` — EXISTS
- [x] Commits 010dd72, ca556f7, 43ae458, 5773d98 — all found in git log
- [x] Build check: `npm run build` → exit 0 ✓
- [x] Playwright: 3/3 tests pass ✓
- [x] No blocking stubs found

## Self-Check: PASSED

---
*Phase: 10-admin-interface-polish*
*Completed: 2026-10-15*
