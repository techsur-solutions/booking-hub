---
phase: 09-core-user-interface
plan: "15"
subsystem: ui
tags: [react, typescript, keycloak, playwright, password-reset, remember-me, vitest]

# Dependency graph
requires:
  - phase: 09-core-user-interface
    provides: App shell (09-01), API client with usersApi methods (09-02)

provides:
  - Login page with Keycloak PKCE redirect and Remember Me 360-day cookie (UI-AUTH-01)
  - rememberMe.ts utility: saveRememberMe/getRememberMeEmail/clearRememberMe
  - My Account self-service page: editable name, read-only email/roles, Change Password link (UI-AUTH-02)
  - Password Reset request page: identical success message prevents email enumeration (UI-AUTH-03)
  - Password Reset complete page: token validation, password requirements, error handling (UI-AUTH-03)
  - Playwright E2E suite (18 tests) covering all 4 auth flows

affects:
  - All routes using /login, /my-account (/account), /password-reset paths

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "window.__E2E_LOGIN_TEST__ flag: suppresses auto-redirect on Login page for E2E test isolation"
    - "window.__E2E_ROLES__ pattern (from 09-01) extended to My Account tests"
    - "Identical error messages for password reset request: security prevents email enumeration (T-09-15-01)"
    - "Token-level vs form-level error distinction in PasswordResetComplete: expired/used/invalid = token error (no form), passwords_mismatch/requirements = form error"

key-files:
  created:
    - frontend/src/pages/Login.tsx
    - frontend/src/utils/rememberMe.ts
    - frontend/e2e/auth-flows.spec.ts
  modified:
    - frontend/src/pages/MyAccount.tsx
    - frontend/src/pages/PasswordReset.tsx
    - frontend/src/pages/PasswordResetComplete.tsx
    - frontend/src/api/users.ts
    - frontend/src/App.tsx
    - frontend/src/pages/Login.tsx

key-decisions:
  - "window.__E2E_LOGIN_TEST__ flag added to Login.tsx: E2E mode sets authenticated=true, which causes Login to redirect; test flag suppresses redirect so tests can interact with login form"
  - "ToastProvider added to App.tsx: PasswordResetComplete uses useToast() for success notification, requires provider in component tree"
  - "PasswordReset.tsx always shows identical success message even on 404/error responses: prevents email enumeration attacks (T-09-15-01, FRD §6.3)"
  - "usersApi.getMe() and usersApi.updateMe() added as named aliases alongside existing getCurrentUser() for MyAccount readability"
  - "Password requirements: min 8 chars, 1 uppercase, 1 lowercase, 1 digit — stricter than legacy (>=6, >=1 digit, >=1 lowercase, no uppercase) per F0 Open Question #4"

patterns-established:
  - "E2E login test isolation: window.__E2E_LOGIN_TEST__=true via page.addInitScript() before page.goto('/login')"
  - "Password reset error classification: token errors (expired/used/invalid) hide the form; form errors (validation/unknown) keep form visible"

# Metrics
duration: 29min
completed: 2026-10-13
---

# Phase 9 Plan 15: Authentication Flows (Login, My Account, Password Reset) Summary

**Login page with Keycloak PKCE redirect and 360-day Remember Me cookie, My Account self-service with inline name editing and StateConfirmationBanner, Password Reset request/complete with token validation and email enumeration prevention, 18 Playwright E2E tests all passing**

## Performance

- **Duration:** 29 min
- **Started:** 2026-10-13T17:43:00Z
- **Completed:** 2026-10-13T18:12:39Z
- **Tasks:** 4
- **Files modified:** 8

## Accomplishments

- Login page (`/login`) with Keycloak PKCE redirect via `keycloak.login()`, Remember Me checkbox storing email in 360-day `SameSite=Strict` cookie, post-login redirect to `?redirect=` param or `/bookings?view=month`
- `rememberMe.ts` utility with `saveRememberMe`/`getRememberMeEmail`/`clearRememberMe` functions
- My Account page (`/account`) with React Query user fetch, inline name edit/save/cancel with `StateConfirmationBanner` on success, read-only email with "Contact admin" tooltip, Keycloak roles as chips, Change Password via `keycloak.accountManagement()`
- Password Reset request page: POST `/users/password-reset/request`, always shows identical "If that email exists, a reset link has been sent." message (T-09-15-01 information disclosure mitigation)
- Password Reset complete page: reads `?token=` from URL, validates passwords (min 8, uppercase, lowercase, digit), POST `/users/password-reset/complete`, classifies errors (token_expired/token_used/invalid_token vs form validation), redirects to `/login` on success
- 18 Playwright E2E tests: all 18 passing (Login UI ×4, My Account ×4, Password Reset request ×3, Password Reset complete ×7)

## Task Commits

Each task was committed atomically:

1. **Task 1: Login page with Keycloak redirect and Remember Me** — `751a349` (feat)
2. **Task 2: My Account self-service page** — `f21a891` (feat)
3. **Task 3: Password Reset request and complete pages** — `a07f068` (feat)
4. **Task 4: Playwright E2E tests for authentication flows** — `5a3b129` (feat)

**Plan metadata:** (docs commit to follow)

## Files Created/Modified

### Created (3 files)
- `frontend/src/pages/Login.tsx` — Login page with Keycloak redirect, Remember Me checkbox, pre-fills email from cookie, `__E2E_LOGIN_TEST__` flag for test isolation
- `frontend/src/utils/rememberMe.ts` — Cookie utilities: `saveRememberMe`/`getRememberMeEmail`/`clearRememberMe` with 360-day expiry and `SameSite=Strict`
- `frontend/e2e/auth-flows.spec.ts` — 18 Playwright tests for all 4 auth flows

### Modified (5 files)
- `frontend/src/pages/MyAccount.tsx` — Replaced stub with full self-service page (React Query, edit/save/cancel, StateConfirmationBanner, Change Password, Audit History link)
- `frontend/src/pages/PasswordReset.tsx` — Replaced stub with request form (POST /users/password-reset/request, identical success message)
- `frontend/src/pages/PasswordResetComplete.tsx` — Replaced stub with complete form (token reading, password validation, error classification)
- `frontend/src/api/users.ts` — Added `getMe()`, `updateMe()`, `requestPasswordReset()`, `completePasswordReset()` methods
- `frontend/src/App.tsx` — Added `ToastProvider` wrapper (required by PasswordResetComplete for success toast)
- `frontend/src/routes/index.tsx` — Already had Login/PasswordReset routes from prior commit (09-14)

## Decisions Made

- **`window.__E2E_LOGIN_TEST__` flag:** E2E mode sets `authenticated=true` immediately, which causes Login to redirect. Added a test-only flag to suppress the redirect so Playwright tests can interact with the login form UI.
- **`ToastProvider` in App.tsx:** `PasswordResetComplete.tsx` uses `useToast()` for the "Password reset successful" toast. The provider was missing from the component tree (Rule 2 - Missing Critical).
- **Identical password reset success message:** Follows FRD §6.3 and STRIDE threat T-09-15-01 — always returns "If that email exists, a reset link has been sent." regardless of whether the email exists. Errors are swallowed client-side.
- **Password requirements (client-side):** min 8 chars, 1 uppercase, 1 lowercase, 1 digit — stricter than legacy (>=6, >=1 digit, >=1 lowercase). This is intentional per Phase 1 F0 audit finding (improvements are allowed over legacy).

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Pre-existing `ListView.test.tsx` TypeScript errors blocked build**
- **Found during:** Task 1 (first `npm run build`)
- **Issue:** `ListView.test.tsx` used `as ReturnType<typeof useBookings>` with minimal objects, which TypeScript 6.x rejects due to React Query v5's stricter `UseQueryResult` union type (14 occurrences)
- **Fix:** Changed to `as unknown as ReturnType<typeof useBookings>` (double assertion) in all 14 locations
- **Files modified:** `frontend/src/pages/ListView.test.tsx`
- **Verification:** `npm run build` passes, `npm test` 74/74 passing
- **Committed in:** `751a349` (Task 1 commit — part of route changes)

**2. [Rule 2 - Missing Critical] ToastProvider missing from component tree**
- **Found during:** Task 4 (Playwright tests — PasswordResetComplete crashed with "useToast must be used inside ToastProvider")**
- **Issue:** `PasswordResetComplete.tsx` calls `useToast()` but `App.tsx` had no `ToastProvider` in the tree
- **Fix:** Added `<ToastProvider>` wrapper around `<RouteRenderer />` in `App.tsx`
- **Files modified:** `frontend/src/App.tsx`
- **Verification:** All 18 Playwright tests pass; `npm test` 74/74 passing
- **Committed in:** `5a3b129` (Task 4 commit)

**3. [Rule 2 - Missing Critical] Login page E2E test isolation needed for auth redirect suppression**
- **Found during:** Task 4 (Playwright tests — Login page tests failed because E2E mode sets `authenticated=true`, causing Login to redirect immediately)**
- **Issue:** In VITE_E2E_MODE, `AuthProvider` sets `authenticated=true` so Login redirects away before tests can interact with the form
- **Fix:** Added `window.__E2E_LOGIN_TEST__` flag check in `Login.tsx` to suppress redirect; Playwright tests set this via `page.addInitScript()`
- **Files modified:** `frontend/src/pages/Login.tsx`
- **Verification:** All 4 Login page tests pass
- **Committed in:** `5a3b129` (Task 4 commit)

---

**Total deviations:** 3 auto-fixed (1 blocking pre-existing build error, 2 missing critical runtime requirements)
**Impact on plan:** All three fixes were necessary for correctness. No scope creep.

## Known Stubs

None found. All stub placeholders replaced with real implementations:
- `MyAccount.tsx`: replaced placeholder with full React Query-powered self-service page
- `PasswordReset.tsx`: replaced stub with working request form
- `PasswordResetComplete.tsx`: replaced stub with full token-validation and password-reset flow

## Issues Encountered

- Pre-existing `ListView.test.tsx` TypeScript errors (from 09-07) blocked first build — auto-fixed as Rule 3 Blocking deviation
- Login E2E tests required E2E-specific isolation flag due to `VITE_E2E_MODE=true` authentication bypass

## User Setup Required

None - no external service configuration required (Keycloak integration uses the existing Phase 2/3 backend).

## Next Phase Readiness

- Authentication UI flows complete — Login, My Account, Password Reset all implemented and tested
- Phase 9 plan 15 is the final plan in this phase; authentication flows are the foundation for all protected routes
- All integration contracts verified:
  - `keycloak.login` in Login.tsx ✓
  - `rememberMe` in Login.tsx ✓
  - `updateMe` in MyAccount.tsx ✓
  - `Change Password` in MyAccount.tsx ✓
  - `/users/password-reset/request` in PasswordReset.tsx ✓
  - `/users/password-reset/complete` in PasswordResetComplete.tsx ✓

---
*Phase: 09-core-user-interface*
*Completed: 2026-10-13*

## Self-Check

- [x] `frontend/src/pages/Login.tsx` — EXISTS
- [x] `frontend/src/utils/rememberMe.ts` — EXISTS
- [x] `frontend/src/pages/MyAccount.tsx` — EXISTS (fully implemented)
- [x] `frontend/src/pages/PasswordReset.tsx` — EXISTS (fully implemented)
- [x] `frontend/src/pages/PasswordResetComplete.tsx` — EXISTS (fully implemented)
- [x] `frontend/e2e/auth-flows.spec.ts` — EXISTS (18 tests)
- [x] Commits `751a349`, `f21a891`, `a07f068`, `5a3b129` — all in git log
- [x] Build: `npm run build` → exit 0 ✓
- [x] Unit tests: 74/74 passing ✓
- [x] Playwright E2E: 18/18 passing ✓
- [x] Known Stubs section: None found (all stubs replaced with real implementations)

## Self-Check: PASSED
