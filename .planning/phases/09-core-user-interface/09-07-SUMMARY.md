---
phase: 09-core-user-interface
plan: "07"
subsystem: ui
tags: [react, typescript, playwright, vitest, react-query, react-router]

# Dependency graph
requires:
  - phase: 09-core-user-interface
    plan: "02"
    provides: useBookings hook, useLocations hook, API client layer
  - phase: 09-core-user-interface
    plan: "03"
    provides: ConflictBadge component, PermissionGate component

provides:
  - ListView page component with filterable BookingTable and URL param sync
  - BookingTable sortable 7-column table component
  - TableFilters component with date range, location multi-select, status multi-select, debounced search
  - exportToCSV / exportToJSON utility functions
  - useResources hook for resource data fetching
  - 10 Playwright E2E tests covering all List View acceptance criteria
  - 30 Vitest unit tests (14 ListView + 16 exportBookings)

affects:
  - 09-core-user-interface
  - UI-CAL-04 (view state persistence — URL filter params implemented)

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "URL query param sync: TableFilters reads/writes ?start_date, ?end_date, ?location_id, ?status, ?q (bookmarkable filter state)"
    - "Client-side post-filter: status and location filters applied client-side after API fetch for multi-select correctness"
    - "E2E API mocking: page.route(http://localhost:8080/...) intercepts backend calls without real backend"

key-files:
  created:
    - frontend/src/pages/ListView.tsx
    - frontend/src/components/list/BookingTable.tsx
    - frontend/src/components/list/TableFilters.tsx
    - frontend/src/utils/exportBookings.ts
    - frontend/src/hooks/useResources.ts
    - frontend/src/pages/ListView.test.tsx
    - frontend/src/utils/exportBookings.test.tsx
    - frontend/e2e/list-view.spec.ts
  modified:
    - frontend/src/pages/BookingsView.tsx

key-decisions:
  - "Client-side status filter applied for ALL selections (not just multi-select) — ensures correctness when API returns unfiltered data in mocked/E2E contexts"
  - "E2E route patterns use full base URL http://localhost:8080/... not wildcard **/path** — wildcard matches page navigation causing test data to be served as HTML"
  - "useResources hook extracted to separate file (hooks/useResources.ts) — mirrors useLocations pattern, reusable by future plans"
  - "Status filter sent to API as optimization hint (apiParams.status) AND applied client-side for defensive correctness"

patterns-established:
  - "Pattern: E2E API mocking with page.route(${API_BASE}/endpoint**) — target backend port not wildcard to avoid intercepting SPA navigation"

# Metrics
duration: 21min
completed: "2026-10-13"
---

# Phase 9 Plan 07: List View Summary

**List View with sortable 7-column BookingTable, multi-select TableFilters with URL sync, ConflictBadge rendering, CSV/JSON export, and 10 Playwright E2E tests (all passing)**

## Performance

- **Duration:** 21 min
- **Started:** 2026-10-13T17:42:33Z
- **Completed:** 2026-10-13T18:03:38Z
- **Tasks:** 3
- **Files modified:** 9

## Accomplishments

- BookingTable: 7-column sortable table (Title, Location, Resources, Start/End Time, Status, Conflict Flags, Actions), client-side sort by header click
- TableFilters: date range pickers, location multi-select with colour dots, status checkboxes, 300ms debounced search input, URL query param sync for bookmarkable filter state
- ConflictBadge renders identically to Calendar/Day views (same ConflictBadge component) in Conflict Flags column
- Row click opens Booking Detail Modal with booking title in modal heading
- exportToCSV/exportToJSON: correct headers, CSV escaping, full booking objects, filename "bookings.csv"/"bookings.json"
- 10 Playwright E2E tests passing: all acceptance criteria covered
- 30 Vitest unit tests (14 ListView + 16 exportBookings)

## Task Commits

Each task was committed atomically:

1. **Task 1: Filterable booking table with ConflictBadge rendering** - `3f9524b` (feat)
2. **Task 2: CSV and JSON export functionality** - `80fa3ca` (feat)
3. **Task 3: Playwright E2E tests for List View** - `a75dfb5` (feat)

**Plan metadata:** _(in docs commit)_

## Files Created/Modified

- `frontend/src/pages/ListView.tsx` — Full List View page with header, filters, table, export buttons, detail modal
- `frontend/src/components/list/BookingTable.tsx` — 7-column sortable table with ConflictBadge + PermissionGate actions
- `frontend/src/components/list/TableFilters.tsx` — Date range, location multi-select, status checkboxes, debounced search, URL sync
- `frontend/src/utils/exportBookings.ts` — exportToCSV, exportToJSON, downloadFile utilities
- `frontend/src/hooks/useResources.ts` — React Query hook for resources data
- `frontend/src/pages/ListView.test.tsx` — 14 Vitest unit tests
- `frontend/src/utils/exportBookings.test.ts` — 16 Vitest unit tests
- `frontend/e2e/list-view.spec.ts` — 10 Playwright E2E tests
- `frontend/src/pages/BookingsView.tsx` — Wired actual ListView component for ?view=list

## Decisions Made

- Client-side status filter applied for all selections (not just multi-select) — ensures correctness when API returns unfiltered data
- E2E route patterns use full base URL `http://localhost:8080/...` instead of wildcard `**/bookings**` — wildcard patterns intercept the SPA page navigation (`/bookings?view=list`) and serve JSON as the HTML page
- useResources hook extracted as separate file following useLocations pattern
- Status filter sent to API as hint AND applied client-side for defense-in-depth correctness

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Wildcard route pattern intercepted SPA navigation in E2E tests**
- **Found during:** Task 3 (Playwright test debugging)
- **Issue:** `page.route('**/bookings**')` matched the page navigation to `/bookings?view=list`, serving JSON `[]` as HTML content — the whole React app was replaced with raw API response
- **Fix:** Changed all route intercepts to use full backend URL pattern: `page.route('http://localhost:8080/bookings**', ...)`
- **Files modified:** `frontend/e2e/list-view.spec.ts`
- **Verification:** All 10 Playwright tests pass
- **Committed in:** a75dfb5 (Task 3 commit)

**2. [Rule 1 - Bug] Status filter applied only for multi-select (>1 item), not single selection**
- **Found during:** Task 3 (E2E filter-by-status test)
- **Issue:** With 1 status selected, filtering was delegated to API which returns unfiltered results in E2E mock context; approved/denied bookings remained visible when only 'pending' was checked
- **Fix:** Changed `filters.statuses.length > 1` to `filters.statuses.length > 0` — client-side always applies status filter as the authoritative filter
- **Files modified:** `frontend/src/pages/ListView.tsx`
- **Verification:** Playwright status filter test passes; all 14 unit tests still pass
- **Committed in:** a75dfb5 (Task 3 commit)

**3. [Rule 2 - Missing Critical] Added useResources hook (not in plan but required by BookingTable)**
- **Found during:** Task 1 (implementation)
- **Issue:** BookingTable needs resources data to display resource names from booking.resource_ids; no useResources hook existed (plan assumed it existed)
- **Fix:** Created `frontend/src/hooks/useResources.ts` following useLocations pattern
- **Files modified:** `frontend/src/hooks/useResources.ts`
- **Verification:** Resource names render correctly in table; TypeScript builds without errors
- **Committed in:** 3f9524b (Task 1 commit)

---

**Total deviations:** 3 auto-fixed (2 bugs, 1 missing critical)
**Impact on plan:** All necessary for correctness and test reliability. No scope creep.

## Known Stubs

None found — all functions fully implemented.

## Issues Encountered

None after deviations resolved.

## User Setup Required

None — no external service configuration required.

## Next Phase Readiness

- List View fully implemented — accessible via BookingsView `?view=list`
- URL filter params implemented (UI-CAL-04 partially complete — filter params synced)
- ConflictBadge renders identically across Calendar/Day/List views
- Export utilities ready for reuse by other views if needed
- Remaining phase 09 plans can build on this pattern for other views

---
*Phase: 09-core-user-interface*
*Completed: 2026-10-13*

## Self-Check: PASSED

- ✅ `frontend/src/pages/ListView.tsx` — FOUND
- ✅ `frontend/src/components/list/BookingTable.tsx` — FOUND
- ✅ `frontend/src/components/list/TableFilters.tsx` — FOUND
- ✅ `frontend/src/utils/exportBookings.ts` — FOUND
- ✅ `frontend/e2e/list-view.spec.ts` — FOUND
- ✅ Commits 3f9524b, 80fa3ca, a75dfb5 — FOUND
- ✅ Build check: `npm run build` → exit 0 (172 modules transformed, dist/ generated)
- ✅ Unit tests: 74/74 passing (6 test files)
- ✅ Playwright E2E: 10/10 passing
- ✅ Contract verification: CONTRACT_OK for all integration dependencies
- ✅ No blocking stubs found
