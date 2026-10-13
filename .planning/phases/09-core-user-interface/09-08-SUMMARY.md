---
phase: 09-core-user-interface
plan: "08"
subsystem: ui
tags: [react, typescript, react-router, url-params, playwright, e2e, view-state]

# Dependency graph
requires:
  - phase: 09-core-user-interface
    plan: "05"
    provides: CalendarView component with FullCalendarWrapper
  - phase: 09-core-user-interface
    plan: "06"
    provides: DayView component with HourlySlotGrid
  - phase: 09-core-user-interface
    plan: "07"
    provides: ListView component with TableFilters

provides:
  - useViewState hook: reads/writes date, view, locationId, status, query, startDate, endDate to URL params
  - URL-synced CalendarView: reads ?date= on mount, updates on dateClick
  - URL-synced DayView: reads ?date= and ?location_id= on mount, updates on picker/select changes
  - URL-synced ListView: initializes filter state from URL params on mount
  - BookingsView: switchView preserves all URL params when switching views
  - TableFilters: preserves ?date= param when syncing list filter state
  - 7 Playwright E2E tests for view state persistence (all passing)

affects:
  - All subsequent plans that use CalendarView/DayView/ListView
  - Any plan that needs URL-based bookmarking of calendar state

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "useViewState wraps useSearchParams for validated URL-based state management"
    - "All URL params validated before use — invalid params fall back to defaults (T-09-08-01)"
    - "switchView() merges view param into existing URL params, preserving date/filters"
    - "TableFilters.syncFilters() preserves non-filter params (?date=, ?view=) when syncing"
    - "locationId validator accepts UUID and short IDs (loc-a, loc-001) — not UUID-only"

key-files:
  created:
    - frontend/src/hooks/useViewState.ts
    - frontend/e2e/view-state-persistence.spec.ts
  modified:
    - frontend/src/pages/CalendarView.tsx
    - frontend/src/pages/DayView.tsx
    - frontend/src/pages/ListView.tsx
    - frontend/src/pages/BookingsView.tsx
    - frontend/src/components/calendar/FullCalendarWrapper.tsx
    - frontend/src/components/list/TableFilters.tsx

key-decisions:
  - "useViewState locationId validator uses [\w-]+ pattern (not UUID-only) — actual IDs use both UUIDs and short strings like loc-a"
  - "CalendarView does not use datesSet callback — writing start of visible range (e.g. Jan 28 for Feb) to ?date= was incorrect; only dateClick updates the date param"
  - "TableFilters.syncFilters preserves ?date= and ?view= when syncing filter params — avoids dropping cross-view date on List View mount"
  - "BookingsView.switchView() uses Object.fromEntries(searchParams.entries()) + merge — preserves all URL params across view switches"

patterns-established:
  - "Pattern: useViewState for URL-based state — hook validates and sanitises all params; use updateState() for atomic URL merges"
  - "Pattern: URL param preservation — each component only clears params it owns; preserves params from other view-state owners"

# Metrics
duration: 9min
completed: 2026-10-13
---

# Phase 9 Plan 08: View State Persistence Summary

**URL-based view state via useViewState hook: date, view mode, and filters persist across Calendar/Day/List navigation with bookmarkable URLs and browser back/forward support**

## Performance

- **Duration:** 9 min
- **Started:** 2026-10-13T18:16:46Z
- **Completed:** 2026-10-13T18:26:21Z
- **Tasks:** 3
- **Files modified:** 8

## Accomplishments

- `useViewState` hook wrapping `useSearchParams` for validated URL-based state (date, view, locationId, status, query, startDate, endDate)
- All 3 views (CalendarView, DayView, ListView) integrated with `useViewState` — date/filters read from URL on mount
- `BookingsView.switchView()` fixed to merge view param into URL without clearing date/filter params
- `TableFilters.syncFilters()` fixed to preserve `?date=` and `?view=` when syncing list filter state
- `FullCalendarWrapper` updated with `initialDate` prop for URL-driven date seeding
- 7 Playwright E2E tests: all 4 plan scenarios covered, all 7 passing

## Task Commits

Each task was committed atomically:

1. **Task 1: useViewState hook for URL query param management** - `b61e2a5` (feat)
2. **Task 2: Integrate useViewState into Calendar, Day, and List views** - `d4ae4c5` (feat)
3. **Task 3: Playwright E2E tests for view state persistence** - `f4685bc` (feat)

**Plan metadata:** _(docs commit — this SUMMARY)_

## Files Created/Modified

- `frontend/src/hooks/useViewState.ts` — URL state hook; validates all params; updateState merges into URL
- `frontend/src/pages/CalendarView.tsx` — reads ?date= from hook; updates on dateClick
- `frontend/src/pages/DayView.tsx` — replaces useState(date/locationId) with useViewState
- `frontend/src/pages/ListView.tsx` — initializes filter state from URL params via useViewState
- `frontend/src/pages/BookingsView.tsx` — switchView() preserves all URL params
- `frontend/src/components/calendar/FullCalendarWrapper.tsx` — adds initialDate prop and onDateNavigate callback
- `frontend/src/components/list/TableFilters.tsx` — syncFilters/handleClear preserve ?date= and ?view=
- `frontend/e2e/view-state-persistence.spec.ts` — 7 Playwright E2E tests for all 4 plan scenarios

## Decisions Made

- **locationId validator uses `[\w-]+` not UUID-only**: Existing test data uses short IDs (`loc-a`, `loc-001`). A UUID-only validator broke the existing day-view E2E test. The validator accepts any word+hyphen string ≤256 chars; backend validates actual ID existence.
- **No datesSet callback in CalendarView**: Writing the FullCalendar grid start date (e.g. Jan 28 for Feb month view) to `?date=` was semantically wrong. Only `dateClick` updates `?date=` in CalendarView.
- **TableFilters preserves non-owned params**: `syncFilters` now carries forward `?date=` and `?view=` it doesn't own, preventing cross-view date loss on List View mount.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] useViewState locationId validator rejected non-UUID IDs**
- **Found during:** Task 3 (running existing day-view E2E tests as regression check)
- **Issue:** Initial `isValidUuid()` check rejected IDs like `loc-002` and `loc-a`, causing DayView location filter to silently fail — selecting a location wrote nothing to URL, never triggered API refetch
- **Fix:** Replaced UUID regex with `isValidLocationId()` accepting `[\w\-]+` strings ≤256 chars
- **Files modified:** `frontend/src/hooks/useViewState.ts`
- **Verification:** Existing day-view location filter test (10/10) passes; new view-state tests pass
- **Committed in:** `f4685bc` (Task 3 commit)

**2. [Rule 1 - Bug] CalendarView datesSet wrote grid range start instead of selected date**
- **Found during:** Task 3 (first view-state-persistence test run)
- **Issue:** `datesSet` fires immediately on mount with the FullCalendar grid range start (e.g. `2024-01-28` for February), overwriting the `?date=2024-02-15` URL param with a wrong value
- **Fix:** Removed `onDateNavigate` prop from CalendarView; only `handleDateClick` updates `?date=` (user-selected date)
- **Files modified:** `frontend/src/pages/CalendarView.tsx`
- **Verification:** Bookmarkable URL test loads calendar at correct date; date persists when switching views
- **Committed in:** `f4685bc` (Task 3 commit)

**3. [Rule 1 - Bug] TableFilters.syncFilters dropped ?date= param when syncing list filters**
- **Found during:** Task 3 (Scenario 1: date persistence across Calendar/Day/List views)
- **Issue:** When ListView mounts, TableFilters calls syncFilters which only preserved `?view=`, dropping `?date=` — switching from DayView (with `?date=2024-02-15`) to ListView lost the date param
- **Fix:** Added `date = searchParams.get('date')` preservation to `syncFilters` and `handleClear`
- **Files modified:** `frontend/src/components/list/TableFilters.tsx`
- **Verification:** All 7 view-state E2E tests pass, including date-across-views scenario
- **Committed in:** `f4685bc` (Task 3 commit)

**4. [Rule 1 - Bug] Browser back/forward test needed restructure — history stack had 3 not 2 entries**
- **Found during:** Task 3 (Scenario 4 test debug)
- **Issue:** Test filled the date picker (creating a 3rd history entry: month→day→day-with-new-date), then goBack went to day (not calendar). Back/forward test should only have 2 history entries to test the month↔day transition.
- **Fix:** Simplified Scenario 4 test to: goto month → click Day tab (1 history push) → goBack → calendar → goForward → day. No date picker fill.
- **Files modified:** `frontend/e2e/view-state-persistence.spec.ts`
- **Verification:** All 7 tests pass
- **Committed in:** `f4685bc` (Task 3 commit)

---

**Total deviations:** 4 auto-fixed (4 Rule 1 bugs)
**Impact on plan:** All fixes required for correct URL state behaviour. No scope creep.

## Known Stubs

- `frontend/src/pages/DayView.tsx:108` — `CreateFormModal` with placeholder text "Full booking form (UI-BOOK-01) will replace this placeholder when that plan is executed." — **Cosmetic**: pre-existing stub from Plan 09-06; slot click still correctly opens the modal and pre-fills time. Does not block view state persistence objective.

No blocking stubs.

## Issues Encountered

None after deviations resolved.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- `useViewState` hook ready for any new view or component needing URL-based state
- All 3 views fully URL-driven: bookmarkable, refreshable, browser-navigation-compatible
- 7 E2E tests protecting view state persistence contracts
- Contract satisfied: `grep -q 'useSearchParams' frontend/src/hooks/useViewState.ts && grep -q 'updateState' ... && echo CONTRACT_OK`

---
*Phase: 09-core-user-interface*
*Completed: 2026-10-13*

## Self-Check: PASSED

- ✅ `frontend/src/hooks/useViewState.ts` — FOUND
- ✅ `frontend/src/pages/CalendarView.tsx` — updated with useViewState
- ✅ `frontend/src/pages/DayView.tsx` — updated with useViewState
- ✅ `frontend/src/pages/ListView.tsx` — updated with useViewState
- ✅ `frontend/e2e/view-state-persistence.spec.ts` — FOUND (7 tests)
- ✅ Commits `b61e2a5`, `d4ae4c5`, `f4685bc` — FOUND in git log
- ✅ Build check: `npm run build` → exit 0 (204 modules, dist/ generated)
- ✅ Unit tests: 74/74 passing (6 test files)
- ✅ E2E tests (new): 7/7 passing
- ✅ E2E tests (existing, regression): 43/43 passing (list-view, day-view, navigation, calendar-view)
- ✅ No blocking stubs found
