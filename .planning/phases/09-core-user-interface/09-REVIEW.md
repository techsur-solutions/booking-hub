---
phase: 9
status: issues_found
blockers: 1
warnings: 0
files_reviewed: 11
files_reviewed_list:
  - frontend/src/pages/BookingsView.tsx
  - frontend/src/api/client.ts
  - frontend/src/hooks/useLocations.ts
  - frontend/src/components/booking/BookingForm.tsx
  - frontend/src/pages/MyAccount.tsx
  - frontend/src/api/types.ts
  - frontend/src/api/users.ts
  - services/users-permissions-service/src/main/resources/db/migration/V3__seed_test_user.sql
  - docker-compose.yml
  - frontend/e2e/booking-create.spec.ts
  - frontend/e2e/auth-flows.spec.ts
reviewed_at: 2026-10-14T19:45:00Z
iteration: 2
---

# Phase 9 Code Review — Iteration 2 (Re-review)

## Previous Iteration Summary

Iteration 1 found 3 blockers and 2 warnings. Fixes were applied in commits:
- `3a76411` — B1: V3 migration schema mismatch
- `d2fc069` — B2, B3: Frontend/backend type mismatch and HTTP method mismatch
- `23d892e` — W1: Fragile 404 error detection
- `73fde83` — W2: Keycloak port documentation mismatch

## Fix Verification

### B1: Schema mismatch — FIXED ✓
**File:** services/users-permissions-service/src/main/resources/db/migration/V3__seed_test_user.sql:13-20, 24-31

V3 migration now correctly inserts into `display_name` column (lines 17, 28) matching V1 schema definition (V1__init_schema.sql:7). Values concatenated as "Test User" and "Booking Approver". No schema errors will occur at Flyway migration runtime.

### B2: Frontend/backend type mismatch — PARTIALLY FIXED ⚠️
**Files:**
- frontend/src/api/types.ts:68 — FIXED: User interface now declares `display_name: string`
- frontend/src/api/users.ts:35-36 — FIXED: updateMe uses `display_name`
- frontend/src/pages/MyAccount.tsx:54, 64, 81 — FIXED: uses `display_name` correctly
- **frontend/e2e/auth-flows.spec.ts:132-290 — INCOMPLETE FIX** (see BLOCKER B4 below)

Source code alignment verified:
- Backend UserResponse.java:14 → `String displayName`
- Backend UserSelfUpdateRequest.java:10 → `String displayName`
- Frontend User interface → `display_name: string`
- MyAccount component reads `user?.display_name` (line 81), sends `{ display_name: ... }` (line 54)

### B3: HTTP method mismatch — FIXED ✓
**File:** frontend/src/api/users.ts:36

usersApi.updateMe now uses `apiClient.put<User>('/users/me', data)` matching backend UserController.java:132 `@PutMapping("/me")`. No 405 Method Not Allowed errors will occur.

### W1: Fragile 404 error detection — FIXED ✓
**File:** frontend/src/pages/MyAccount.tsx:122, 130

Lines 122 and 130 now check `(error as any)?.response?.status === 404` instead of `error.message.includes('404')`. Direct HTTP status inspection is stable and won't break if Axios changes error message format.

### W2: Keycloak admin console URL mismatch — FIXED ✓
**Files:**
- docker-compose.yml:59 — FIXED: comment now shows `http://localhost:8180/admin`
- V3__seed_test_user.sql:5 — FIXED: comment updated to port 8180

Port matches actual mapping at docker-compose.yml:75 (`8180:8080`). No developer confusion.

---

## BLOCKERs

### B4: E2E test mocks incompatible with fixed User type — fix-introduced regression
- **File:** frontend/e2e/auth-flows.spec.ts:132-290
- **Category:** integration
- **Evidence:**
  
  **Lines 132-165** — Test "renders name (editable), email (read-only), and roles (chips)":
  - Mock at lines 138-144 returns `{ id, email, first_name: 'Alice', last_name: 'Smith', deleted_at }` 
  - But frontend User type (types.ts:65-70) declares only `display_name: string`, NOT `first_name` or `last_name`
  - MyAccount.tsx:81 reads `user?.display_name ?? '—'` — since mock has no `display_name` field, component displays fallback "—"
  - Test line 153 expects `toContainText('Alice Smith')` → will FAIL, displays "—" instead
  
  **Lines 167-223** — Test "Edit/Save/Cancel name editing flow":
  - Mock at lines 170-199 checks `route.request().method() === 'PATCH'` (line 183) but usersApi.updateMe uses PUT (users.ts:36)
  - Mock route will never match → actual PUT request goes unmocked → test may timeout or hit real backend
  - Mock returns `{ first_name: 'Alice', last_name: 'Smith' }` and `{ first_name: 'Alice', last_name: 'Updated' }` (lines 178-179, 191-192)
  - MyAccount.tsx:54 sends `{ display_name: name.trim() }` but test lines 221-222 expect `patchBody.first_name` and `patchBody.last_name` → assertions will FAIL
  
  **Lines 225-253** — Test "Cancel editing restores previous name":
  - Mock at lines 226-237 returns `{ first_name, last_name }` instead of `{ display_name }` → same issue
  
  **Lines 255-290** — Test "Change Password button" and similar:
  - Mocks at lines 256-267, 262-266 still use `first_name`/`last_name` → same issue
  
  **Only lines 292-318 were fixed** (test "loads successfully when user record exists" correctly mocks `display_name: 'Test User'` at line 301).
  
  The d2fc069 fix commit only updated ONE test mock (lines 298-304) but left FIVE other test mocks (lines 138-144, 174-195, 228-236, 258-266) with the old `first_name`/`last_name` schema. These tests will fail at runtime because:
  1. Mocks provide fields that don't exist in the User interface (TypeScript may not catch this since route mocks are untyped JSON)
  2. Component expects `display_name` field which mocks don't provide → displays fallback "—"
  3. Test expects PATCH method but code uses PUT → route never matches
  4. Test asserts on `patchBody.first_name` but actual body contains `display_name`

- **Fix direction:**
  
  Update ALL test mocks in auth-flows.spec.ts lines 132-290:
  1. Replace ALL occurrences of `first_name: 'X', last_name: 'Y'` with `display_name: 'X Y'` (lines 138-144, 174-195, 228-236, 258-266)
  2. Change line 183 from `method() === 'PATCH'` to `method() === 'PUT'`
  3. Replace lines 221-222 assertions:
     ```typescript
     // OLD (wrong):
     expect(patchBody.first_name).toBe('Alice');
     expect(patchBody.last_name).toBe('Updated');
     
     // NEW (correct):
     expect(patchBody.display_name).toBe('Alice Updated');
     ```
  4. Run `npm test -- auth-flows.spec.ts` to verify all tests pass

---

## WARNINGs

(none)

---

## Cross-file seams checked

- **Plan 09-16 - BookingsView → BookingCreate navigation:**
  - BookingsView.tsx:47 builds URL `/bookings/create?start_time=${currentDate}T09:00` → OK
  - Routes define `/bookings/create` → OK (verified in previous iteration)
  - BookingCreate reads `start_time` param → OK
  - E2E test booking-create.spec.ts:645-649 verifies param preservation → OK

- **Plan 09-17 - JWT token refresh interceptor:**
  - client.ts:16 calls `keycloak.updateToken(30)` before each request → OK
  - MyAccount.tsx:29 comment confirms centralized pattern → OK
  - useLocations.ts, BookingForm.tsx all use apiClient → OK, inherit refresh
  - No duplicate updateToken() calls found → OK

- **Plan 09-17 - BookingForm location dropdown:**
  - BookingForm.tsx:91 calls `useLocations()` → OK
  - Lines 174-201 render loading/error/empty/populated states → OK
  - useLocations.ts:5-15 returns React Query result → OK
  - E2E test booking-create.spec.ts:662-699 verifies dropdown population → OK

- **Plan 09-18 - My Account user record seed:**
  - V3 migration inserts display_name → OK (B1 fixed)
  - docker-compose.yml:48-59 documents UUIDs → matches V3 UUIDs → OK
  - **E2E test auth-flows.spec.ts mocks → BLOCKER B4** (5 of 6 test mocks still use wrong schema)

- **Type safety (frontend ↔ backend):**
  - Frontend User.display_name vs Backend UserResponse.displayName → OK (camelCase/snake_case conversion handled by serialization)
  - V1 schema display_name column vs V3 seed values → OK (B1 fixed)
  - PUT /users/me contract → OK (B3 fixed, frontend uses PUT)
  - **E2E test mocks vs User interface → BLOCKER B4** (mocks incompatible with fixed User type)

- **Method routing:**
  - usersApi.updateMe PUT → UserController @PutMapping("/me") → OK (B2/B3 fixed)
  - usersApi.createUser POST → UserController @PostMapping → OK
  - usersApi.updateUser PUT → UserController @PutMapping("/{id}") → OK

---

## Re-review scope

Per instructions, iteration 2 reviewed:
1. All files from iteration 1's `files_reviewed_list` (11 files)
2. All files modified by fix commits (same 6 files as listed in git diff output)
3. Verified each previous finding was actually fixed (read the code, not just commit messages)
4. Checked for regressions introduced by fixes → **found B4**

---

## Regression analysis

The d2fc069 fix commit message states "E2E test mock: corrected to match backend response shape" but the git diff shows only ONE test mock was updated (lines 298-304 in the "loads successfully" test). The commit did NOT update the other FIVE test mocks in the same file that still reference first_name/last_name. This is a partial fix that will cause test failures.

The source code (MyAccount.tsx, users.ts, types.ts) was fixed correctly. Only the E2E test mocks are incorrect.
