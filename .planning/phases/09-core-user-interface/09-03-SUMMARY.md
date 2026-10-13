---
phase: 09-core-user-interface
plan: 03
subsystem: ui
tags: [react, typescript, vitest, testing-library, keycloak, permissions, accessibility]

# Dependency graph
requires:
  - phase: 02-platform-foundation-infrastructure
    provides: React + TypeScript SPA foundation, Keycloak auth integration
  - phase: 03-identity-access-control
    provides: Keycloak realm roles (realmAccess.roles) used by PermissionGate
provides:
  - StateConfirmationBanner reusable component (action, entity, timestamp, auto-dismiss)
  - ConflictBadge reusable component (ConflictFlag[], onClick, size variants)
  - PermissionGate HOC (requireRole, requireAnyRole, fallback, structural absence)
  - permissions.ts utility functions (hasRole, hasAnyRole, hasAllRoles, hasApprovePermission, hasAnyAdminPermission)
  - Vitest unit test framework configured for frontend (jsdom, @testing-library/react)
affects: [UI-CAL-01, UI-BOOK-01, UI-APPR-01, all subsequent Phase 9 plans]

# Tech tracking
tech-stack:
  added: [vitest, @vitest/coverage-v8, @testing-library/react, @testing-library/jest-dom, @testing-library/user-event, jsdom]
  patterns:
    - Reusable pattern components for cross-cutting UX concerns
    - vi.mock() for keycloak singleton isolation in tests
    - fireEvent for synchronous click testing with fake timers
    - Structural absence (returns null) not disabled state for permission gating

key-files:
  created:
    - frontend/src/components/StateConfirmationBanner.tsx
    - frontend/src/components/StateConfirmationBanner.test.tsx
    - frontend/src/components/ConflictBadge.tsx
    - frontend/src/components/ConflictBadge.test.tsx
    - frontend/src/components/PermissionGate.tsx
    - frontend/src/components/PermissionGate.test.tsx
    - frontend/src/utils/permissions.ts
    - frontend/src/test-setup.ts
  modified:
    - frontend/package.json
    - frontend/vite.config.ts
    - frontend/tsconfig.app.json

key-decisions:
  - "Vitest chosen over Jest for unit tests (native Vite integration, no config overhead)"
  - "fireEvent used over userEvent for click tests with fake timers (avoids timeout issues)"
  - "vi.mock('../auth/keycloak') pattern established for all Keycloak-dependent utils"
  - "Vitest include pattern set to src/**/*.test.{ts,tsx} to exclude e2e/ Playwright specs"

patterns-established:
  - "Pattern 1: StateConfirmationBanner — explicit state confirmation after every mutation"
  - "Pattern 2: ConflictBadge — single component reused across Calendar/Day/List/Queue/Detail"
  - "Pattern 3: PermissionGate — deny-by-default structural absence (null, not disabled)"
  - "Pattern 4: permissions.ts utilities — centralised role-check helpers used by all components"

# Metrics
duration: 4min
completed: 2026-10-13
---

# Phase 9 Plan 3: UI Pattern Components Summary

**Three reusable cross-cutting UI components (StateConfirmationBanner, ConflictBadge, PermissionGate) built with Vitest test framework, 40 unit tests across all components**

## Performance

- **Duration:** 4 min
- **Started:** 2026-10-13T17:28:06Z
- **Completed:** 2026-10-13T17:32:37Z
- **Tasks:** 3 (+ Vitest setup)
- **Files modified:** 12

## Accomplishments

- StateConfirmationBanner with auto-dismiss (5s default), ARIA attributes (role=alert, aria-live=polite), action/entity/timestamp/variant props
- ConflictBadge single component receiving ConflictFlag[] — renders count + ⚠ icon, tooltip, keyboard accessible, click opens detail modal
- PermissionGate HOC with requireRole/requireAnyRole/fallback — structurally omits children (null) when permission denied
- permissions.ts utility library: hasRole, hasAnyRole, hasAllRoles, hasApprovePermission, hasAnyAdminPermission
- Vitest unit test framework configured: jsdom, @testing-library/react, @testing-library/jest-dom, 40 tests passing

## Task Commits

Each task was committed atomically:

1. **Task 1: StateConfirmationBanner + Vitest setup** - `6e41625` (feat)
2. **Task 2: ConflictBadge component** - `7db1b0d` (feat)
3. **Task 3: PermissionGate + permissions.ts** - `5631cbc` (feat)

**Plan metadata:** _(docs commit)_

## Files Created/Modified

- `frontend/src/components/StateConfirmationBanner.tsx` — Confirmation banner with auto-dismiss and ARIA support
- `frontend/src/components/StateConfirmationBanner.test.tsx` — 13 unit tests
- `frontend/src/components/ConflictBadge.tsx` — Conflict badge reused across all views
- `frontend/src/components/ConflictBadge.test.tsx` — 17 unit tests
- `frontend/src/components/PermissionGate.tsx` — Permission-gated rendering HOC
- `frontend/src/components/PermissionGate.test.tsx` — 10 unit tests
- `frontend/src/utils/permissions.ts` — Role-check utility functions
- `frontend/src/test-setup.ts` — Vitest global setup (@testing-library/jest-dom)
- `frontend/package.json` — Added vitest scripts and dev dependencies
- `frontend/vite.config.ts` — Added Vitest config (jsdom, setupFiles, include/exclude)
- `frontend/tsconfig.app.json` — Added vitest/globals type reference

## Decisions Made

- Used Vitest over Jest — native Vite integration, no transform config needed, faster
- Used `fireEvent` (not `userEvent`) for click testing with `vi.useFakeTimers()` — avoids timing issues from `userEvent.setup({ advanceTimers })` which caused test timeout
- Set `include: ['src/**/*.test.{ts,tsx}']` to exclude `e2e/` Playwright specs from Vitest run
- Established `vi.mock('../auth/keycloak')` as the mocking pattern for Keycloak-dependent utils

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Set up Vitest test framework (not in plan but required for test verification)**
- **Found during:** Pre-task 1
- **Issue:** Plan's verify commands ran `npm test -- ComponentName.test.tsx` but no test script or framework existed in `package.json`
- **Fix:** Installed vitest, @testing-library/react, @testing-library/jest-dom, jsdom; configured `vite.config.ts` test section; added `test` script to package.json
- **Files modified:** frontend/package.json, frontend/vite.config.ts, frontend/tsconfig.app.json, frontend/src/test-setup.ts
- **Verification:** `npx vitest run` ran with 0 test files found (clean empty start)
- **Committed in:** 6e41625 (Task 1 commit)

**2. [Rule 1 - Bug] Replaced `userEvent` with `fireEvent` for dismiss button click test**
- **Found during:** Task 1 verification
- **Issue:** `userEvent.setup({ advanceTimers: vi.advanceTimersByTime })` with `vi.useFakeTimers()` caused test timeout (5s) — the user-event timer advancement pattern doesn't work cleanly with Vitest's fake timer implementation in this configuration
- **Fix:** Replaced async `userEvent.click()` with synchronous `fireEvent.click()` for the dismiss button test
- **Files modified:** frontend/src/components/StateConfirmationBanner.test.tsx
- **Verification:** Test passed without timeout after change
- **Committed in:** 6e41625 (Task 1 commit)

---

**Total deviations:** 2 auto-fixed (1 blocking infra, 1 bug)
**Impact on plan:** Both necessary for correct test execution. Vitest setup is prerequisite for all future Phase 9 test plans.

## Issues Encountered

None after deviations resolved.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- StateConfirmationBanner, ConflictBadge, PermissionGate exported and ready for use by UI-CAL-01, UI-BOOK-01, UI-APPR-01
- Vitest test framework operational — future plans can add tests without setup overhead
- ConflictBadge contract verified: `export function ConflictBadge` + `ConflictFlag` present
- StateConfirmationBanner contract verified: `StateConfirmationBanner` + `autoDismiss` present

## Known Stubs

None found — all components have complete implementations.

## Self-Check: PASSED

- ✅ StateConfirmationBanner.tsx exists
- ✅ ConflictBadge.tsx exists
- ✅ PermissionGate.tsx exists
- ✅ permissions.ts exists
- ✅ SUMMARY.md exists
- ✅ Commits 6e41625, 7db1b0d, 5631cbc all found in git log
- ✅ Build check: `tsc --noEmit` → exit 0
- ✅ No blocking stubs found

---
*Phase: 09-core-user-interface*
*Completed: 2026-10-13*
