---
phase: 09-core-user-interface
plan: "17"
subsystem: ui
tags: [react, jwt, keycloak, react-query, playwright, authentication, booking-form]

requires:
  - phase: 09-core-user-interface
    provides: Plan 09-02 (API client with JWT interceptor), Plan 09-09 (BookingForm component), Plan 09-02 (useLocations hook)

provides:
  - API client with automatic JWT token refresh before every request (keycloak.updateToken(30) in request interceptor)
  - Global authentication fix affecting ALL API calls app-wide (not just locations)
  - Enhanced useLocations hook with retry: 2 and staleTime: 5min for transient failure resilience
  - BookingForm location dropdown with loading/error/empty state handling
  - Playwright tests verifying location dropdown population (3 new test scenarios)

affects:
  - 09-core-user-interface (Plan 09-18 My Account and all future API consumers inherit JWT token refresh)
  - All components using apiClient (centralized auth logic prevents per-hook duplication)

tech-stack:
  added: []
  patterns:
    - "Centralized JWT token refresh in apiClient request interceptor (not per-hook duplication)"
    - "React Query retry and staleTime for defensive caching against transient API failures"
    - "Loading/error/empty state pattern for dropdown data fetching"

key-files:
  created: []
  modified:
    - frontend/src/api/client.ts
    - frontend/src/hooks/useLocations.ts
    - frontend/src/components/booking/BookingForm.tsx
    - frontend/e2e/booking-create.spec.ts

key-decisions:
  - "JWT token refresh centralized in client.ts interceptor (applies to ALL API calls) instead of per-hook implementation — prevents duplication and ensures consistent auth behavior app-wide"
  - "Used keycloak.updateToken(30) which refreshes if token expires within 30 seconds — proactive refresh prevents race conditions where token expires between check and use"
  - "Added retry: 2 to useLocations as secondary defense (primary fix is centralized token refresh)"
  - "BookingForm shows structural absence (conditional rendering) for location dropdown in loading/error/empty states instead of disabled select"

patterns-established:
  - "Pattern 1: Centralized auth interceptor handles token lifecycle for ALL requests — hooks/components never call keycloak.updateToken() directly"
  - "Pattern 2: Loading/error/empty state trinity for data-dependent dropdowns — always handle all three states with user-friendly messages"

duration: 3min
completed: 2026-10-13
---

# Phase 9 Plan 17: Location Dropdown Data Loading Fix Summary

**Global JWT token refresh interceptor with automatic expiry handling, fixing location dropdown and ALL future authenticated API calls app-wide**

## Performance

- **Duration:** 3 min
- **Started:** 2026-10-13T20:18:00Z
- **Completed:** 2026-10-13T20:21:30Z
- **Tasks:** 2
- **Files modified:** 4

## Accomplishments

- Fixed JWT authentication globally for ALL API calls (not just locations) via centralized keycloak.updateToken(30) in request interceptor
- Location dropdown now loads data successfully with proper token refresh before API call
- Added defensive retry logic (retry: 2) and caching (staleTime: 5min) to useLocations hook
- BookingForm handles loading/error/empty location states with user-friendly messages
- 3 new Playwright tests verify location dropdown population, loading state, and error handling
- Closes UAT Test #6 blocker: "new bookings page does not have any locations so I am not able to create one"

## Task Commits

Each task was committed atomically:

1. **Task 1: Fix API client JWT interceptor to refresh expired tokens** - `cc7604b` (feat)
2. **Task 2: Update Playwright test to verify location dropdown population** - `f63fb11` (test)

**Plan metadata:** (included in final metadata commit)

_Note: Task 1 provides global authentication fix affecting ALL API calls, not just locations — Plan 09-18 (My Account) and future plans inherit this fix automatically_

## Files Created/Modified

- `frontend/src/api/client.ts` - Added keycloak.updateToken(30) in request interceptor to refresh expired tokens before attaching Authorization header (GLOBAL — affects ALL API calls)
- `frontend/src/hooks/useLocations.ts` - Added retry: 2, staleTime: 5min, and console.log debugging for locations API calls
- `frontend/src/components/booking/BookingForm.tsx` - Added locationsLoading/locationsError state extraction and conditional rendering for loading/error/empty states
- `frontend/e2e/booking-create.spec.ts` - Added 3 location dropdown test scenarios (populated, loading state, error state)

## Decisions Made

**JWT token refresh centralization:** Implemented keycloak.updateToken(30) in apiClient request interceptor (not in individual hooks) to ensure ALL API calls benefit from automatic token refresh. This prevents duplication across every useQuery hook and guarantees consistent authentication behavior app-wide. Plan 09-18 (My Account) and all future API consumers inherit this fix automatically.

**updateToken(30) parameter choice:** 30-second threshold ensures token refresh happens proactively before expiry. Refreshing only when <30s remaining prevents race conditions where token expires between validity check and actual API call. Keycloak SDK handles the refresh logic internally (no-op if token still valid, silent refresh via refresh token if expiring soon, redirect to login if refresh fails).

**Secondary defense pattern:** Added retry: 2 and staleTime: 5min to useLocations hook even though primary fix is centralized token refresh. Retry handles transient network failures (not just auth); staleTime reduces redundant API calls for rarely-changing location data (same pattern as useSettings from 09-02).

**Structural absence over disabled state:** BookingForm conditionally renders location dropdown only when locations.length > 0 (with separate loading/error/empty messages), instead of always rendering a disabled select. This follows UI-PATTERN-03 deny-by-default principle established in 09-03 and provides clearer user feedback.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

Plan 09-17 complete. Ready for Plan 09-18 (My Account page).

**Global authentication fix ready:** All API consumers (including Plan 09-18 My Account and future plans) automatically inherit JWT token refresh interceptor — no per-component auth logic required.

**UAT Test #6 blocker resolved:** Location dropdown now properly populated with available locations. User can create bookings successfully.

---
*Phase: 09-core-user-interface*
*Completed: 2026-10-13*
