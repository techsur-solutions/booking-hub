---
phase: 10-admin-interface-polish
plan: 12
subsystem: testing
tags: [playwright, e2e, ci, github-actions, critical-flows, keycloak, booking, approval, conflict-detection, recurring]

# Dependency graph
requires:
  - phase: 10-11
    provides: Playwright test setup and config (playwright.config.ts)
  - phase: 09-core-user-interface
    provides: UI components under test (BookingForm, ApprovalQueue, ConflictWarning, RecurringScopeDialog)
provides:
  - E2E test suite for 5 critical user flows (55 tests, all passing)
  - CI GitHub Actions workflow running tests on every PR
  - Conflict handling role-differentiation coverage (hard-block vs soft-warn)
  - Scope selection coverage (this-only vs entire-series PATCH param verification)
affects:
  - CI/CD pipeline (branch protection can now be enabled on "Critical Flow E2E Tests")

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Single catch-all route handler pattern for Playwright API mocking — avoids route ordering bugs"
    - "window.__E2E_USER_ID__ injection for owner-check in BookingDetailModal canEdit"
    - "Critical-flows directory isolation — separate from existing e2e/ test suites"

key-files:
  created:
    - frontend/e2e/critical-flows/login.spec.ts
    - frontend/e2e/critical-flows/booking-crud.spec.ts
    - frontend/e2e/critical-flows/conflict-handling.spec.ts
    - frontend/e2e/critical-flows/approval.spec.ts
    - frontend/e2e/critical-flows/recurring-scope.spec.ts
    - frontend/.github/workflows/e2e-tests.yml
  modified: []

key-decisions:
  - "Single catch-all route handler (instead of per-endpoint) prevents Playwright route registration ordering bugs where /bookings/** intercepted /bookings/check-conflicts POST"
  - "window.__E2E_USER_ID__ must be injected alongside __E2E_ROLES__ for BookingDetailModal delete button to appear (canEdit = owner_id === keycloak.subject)"
  - "Scope PATCH verification tests URL query params (?scope=this-only|entire-series) because patchBooking() appends scope to the URL, not the request body"
  - "Approval queue testids: btn-approve-{id}/btn-deny-{id} (not approve-btn-/deny-btn-); approve-confirm-dialog (not approve-dialog); recurring-scope-dialog (not scope-confirm-dialog)"
  - "CI workflow run in separate steps per spec file for clear failure reporting (not combined run)"

# Metrics
duration: 18min
completed: 2026-10-15
---

# Phase 10 Plan 12: Critical Flow E2E Tests + CI Integration Summary

**55-test E2E critical-flow suite (login, booking CRUD, conflict handling, approval, recurring scope) with GitHub Actions CI workflow running on every PR**

## Performance

- **Duration:** 18 min
- **Started:** 2026-10-15T03:02:34Z
- **Completed:** 2026-10-15T03:21:11Z
- **Tasks:** 3 completed
- **Files created:** 6

## Accomplishments

- Created `frontend/e2e/critical-flows/` directory with 5 spec files covering all Phase 10 success criteria
- 55 tests written and all passing (login: 8, booking-crud: 9, conflict-handling: 16, approval: 12, recurring-scope: 10)
- All minimum line requirements met (login 260≥40, booking-crud 587≥100, conflict 392≥60, approval 494≥60, recurring 396≥50, CI 123≥50)
- GitHub Actions workflow at `frontend/.github/workflows/e2e-tests.yml` runs on PR/push to main
- CI contract verified: `grep -q 'playwright test.*critical-flows' e2e-tests.yml && echo CONTRACT_OK`

## Task Commits

Each task was committed atomically:

1. **Task 1: E2E tests for login and booking CRUD flows** - `89e073d` (feat)
2. **Task 2: E2E tests for conflict handling, approval, recurring scope** - `675e9d3` (feat)
3. **Task 3: CI integration with GitHub Actions** - `0c9e86f` (feat)

**Plan metadata:** _(docs commit — see below)_

## Files Created/Modified

- `frontend/e2e/critical-flows/login.spec.ts` — 8 tests: Keycloak button, PKCE flow initiation, landing page, calendar grid, sidebar (UI-AUTH-01)
- `frontend/e2e/critical-flows/booking-crud.spec.ts` — 9 tests: form renders, fill+submit, banner, location dropdown, pre-fill, edit+save, delete button (UI-BOOK-01/02)
- `frontend/e2e/critical-flows/conflict-handling.spec.ts` — 16 tests: hard-block (non-approver), soft-warn (approver), icon grouping, Save Anyway dialog, role differentiation (UI-BOOK-04)
- `frontend/e2e/critical-flows/approval.spec.ts` — 12 tests: queue renders, approve/deny with optimistic removal, toast, non-approver redirect (UI-APPR-01)
- `frontend/e2e/critical-flows/recurring-scope.spec.ts` — 10 tests: scope dialog appears, non-dismissible, this-only vs entire-series, PATCH scope param, post-save banner (UI-BOOK-02)
- `frontend/.github/workflows/e2e-tests.yml` — CI workflow: PR/push trigger, per-spec test steps, artifact upload on failure, branch protection documentation

## Decisions Made

- **Single catch-all route handler** instead of per-endpoint `page.route()` calls: Playwright processes routes in last-registered-first order, so `/bookings/**` would intercept `/bookings/check-conflicts` POST before the specific handler. Using a URL path check inside one handler avoids this entirely.
- **window.__E2E_USER_ID__ injection**: BookingDetailModal's `canEdit` check compares `booking.owner_id === keycloak.subject`. In E2E mode, `keycloak.subject` is set from `__E2E_USER_ID__`. Tests that needed the delete button set `owner_id: 'user-me'` and injected `__E2E_USER_ID__ = 'user-me'`.
- **Approval testid discovery**: The approval components use `btn-approve-{id}`, `btn-deny-{id}`, `approve-confirm-dialog`, `btn-confirm-approve`, `btn-confirm-deny` — different from what the plan specified (`approve-btn-`, `approve-dialog`, etc.). Updated tests to match actual source.
- **RecurringScopeDialog testid**: Uses `recurring-scope-dialog` (not `scope-confirm-dialog`). Updated tests to match actual source.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Playwright route ordering caused check-conflicts to fail**
- **Found during:** Task 1 (Create booking test)
- **Issue:** Registering `/bookings/**` route after `/bookings/check-conflicts` meant the wildcard caught the check-conflicts POST (Playwright matches last-registered routes first). The mock returned a booking object instead of empty array, triggering false `isBlocked=true` and disabling Save.
- **Fix:** Replaced separate per-endpoint mocks with a single catch-all handler that dispatches by URL path internally, eliminating ordering dependencies.
- **Files modified:** `frontend/e2e/critical-flows/booking-crud.spec.ts`
- **Verification:** Save button tests now pass (not disabled for no-conflict scenario)
- **Committed in:** 89e073d (Task 1 commit)

**2. [Rule 1 - Bug] Wrong testids for approval components**
- **Found during:** Task 2 (Approval tests)
- **Issue:** Plan specified `approve-btn-{id}` / `deny-btn-{id}` / `approve-dialog` / `confirm-approve-btn` but actual source uses `btn-approve-{id}` / `btn-deny-{id}` / `approve-confirm-dialog` / `btn-confirm-approve`.
- **Fix:** Discovered actual testids from source files, updated spec to match.
- **Files modified:** `frontend/e2e/critical-flows/approval.spec.ts`
- **Verification:** All 12 approval tests pass
- **Committed in:** 675e9d3 (Task 2 commit)

**3. [Rule 1 - Bug] Wrong testid for recurring scope dialog**
- **Found during:** Task 2 (Recurring scope tests)
- **Issue:** Plan specified `scope-confirm-dialog` but actual source uses `recurring-scope-dialog`.
- **Fix:** sed replace across the spec file.
- **Files modified:** `frontend/e2e/critical-flows/recurring-scope.spec.ts`
- **Verification:** All 10 recurring scope tests pass
- **Committed in:** 675e9d3 (Task 2 commit)

---

**Total deviations:** 3 auto-fixed (all Rule 1 - Bug: testid/route ordering mismatches)
**Impact on plan:** All fixes necessary for test correctness. No scope creep. Tests now match actual component testids which are the authoritative source.

## Known Stubs

None found. All test assertions verify real application behavior through Playwright route interception. No hardcoded/static stub responses that bypass the actual component logic.

## Issues Encountered

- Playwright route ordering: last-registered matches first — documented in key-decisions for future test authors
- `window.__E2E_USER_ID__` requirement: not obvious from plan, discovered from AuthProvider source

## User Setup Required

None — no external service configuration required. The CI workflow runs with mocked API responses and no real backend dependency for the critical-flow tests.

## Next Phase Readiness

- All 5 critical user flows have E2E coverage with 55 consistently passing tests
- CI workflow ready — enable "Critical Flow E2E Tests" status check in GitHub branch protection settings to enforce merge blocking
- Phase 10 (Admin Interface Polish) plans complete

## Self-Check: PASSED

- ✅ All 6 files exist on disk (5 spec files + 1 CI workflow)
- ✅ All 3 task commits exist (89e073d, 675e9d3, 0c9e86f)
- ✅ Build check: `tsc --noEmit --skipLibCheck` → exit 0
- ✅ E2E suite: 55 tests registered, 55 passed (verified two full runs)
- ✅ No stubs found in created files
- ✅ CI contract verified: workflow contains `playwright test.*critical-flows`

---
*Phase: 10-admin-interface-polish*
*Completed: 2026-10-15*
