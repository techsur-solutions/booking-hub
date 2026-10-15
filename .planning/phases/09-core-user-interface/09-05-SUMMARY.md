---
phase: 09-core-user-interface
plan: "05"
subsystem: ui
tags: [react, typescript, fullcalendar, playwright, e2e, calendar, bookings]

# Dependency graph
requires:
  - phase: 09-core-user-interface
    provides: useBookings hook (09-02), useLocations hook (09-02), ConflictBadge component (09-03)
provides:
  - FullCalendarWrapper: React wrapper integrating @fullcalendar/react with booking data and location colour-coding
  - BookingEvent: FullCalendar eventContent renderer with ConflictBadge integration
  - CalendarView: Page component wiring FullCalendarWrapper with dateClick/eventClick handlers
  - E2E Playwright tests covering calendar render, event display, conflict badges, click interactions
affects:
  - 09-06 (DayView — shares BookingsView container)
  - 09-07 (ListView — shares BookingsView container)
  - UI-CAL-04 (View state persistence, consumes this view)
  - UI-BOOK-01 (Booking Create Form, receives pre-filled slot from CalendarView.handleDateClick)

# Tech tracking
tech-stack:
  added: ["@fullcalendar/react@^6.1.0", "@fullcalendar/daygrid@^6.1.0", "@fullcalendar/timegrid@^6.1.0", "@fullcalendar/interaction@^6.1.0"]
  patterns:
    - FullCalendarWrapper maps Booking[] to FullCalendar EventInput[] using location.colour as backgroundColor
    - BookingEvent as eventContent renderer — receives EventApi + Booking, delegates to ConflictBadge
    - page.route('*://localhost:8080/**') glob pattern for mocking API gateway in Playwright E2E tests

key-files:
  created:
    - frontend/src/components/calendar/FullCalendarWrapper.tsx
    - frontend/src/components/calendar/BookingEvent.tsx
    - frontend/e2e/calendar-view.spec.ts
  modified:
    - frontend/src/pages/CalendarView.tsx
    - frontend/src/pages/BookingsView.tsx
    - frontend/package.json
    - frontend/package-lock.json

key-decisions:
  - "FullCalendarWrapper exposes onDateClick/onEventClick props for modal integration in later plans (09-06/09-07)"
  - "sr-only h1 'Calendar View' added to CalendarView for screen readers and navigation test compatibility"
  - "page.route('*://localhost:8080/**') glob pattern catches all API gateway calls in E2E tests"
  - "Booking events fixed to mid-month dates so they appear in current month view regardless of test run date"

patterns-established:
  - "Pattern: FullCalendar eventContent = React component (BookingEvent) that can render arbitrary JSX including ConflictBadge"
  - "Pattern: E2E API mocking via page.route with URL pathname dispatch (single handler, multiple endpoints)"

# Metrics
duration: 22min
completed: 2026-10-13
---

# Phase 9 Plan 05: Calendar View with FullCalendar Summary

**FullCalendar v6 integrated with booking data from useBookings/useLocations hooks, location colour-coded events, ConflictBadge rendering in BookingEvent component, 10 Playwright E2E tests passing**

## Performance

- **Duration:** 22 min
- **Started:** 2026-10-13T17:42:08Z
- **Completed:** 2026-10-13T18:04:15Z
- **Tasks:** 3
- **Files modified:** 8

## Accomplishments

- `@fullcalendar/react@^6.1.0` + daygrid/timegrid/interaction plugins installed
- `FullCalendarWrapper` maps `Booking[]` → FullCalendar events with location `colour` as `backgroundColor`; month/week views; prev/next/today navigation
- `BookingEvent` renders event title + `ConflictBadge` when `conflict_flags.length > 0`
- `CalendarView` wires `onDateClick`/`onEventClick` handlers ready for modal integration (09-06/09-07)
- `BookingsView` updated to render `CalendarView` for `view=month` (alongside `DayView`/`ListView`)
- 10 Playwright E2E tests passing: grid render, heading, tabs, toolbar, event display, conflict badge, click interactions

## Task Commits

Each task was committed atomically:

1. **Task 1: FullCalendar integration with booking data** - `e22c1a0` (feat)
2. **Task 2: Booking event rendering with conflict badges** - `e22c1a0` (feat, committed together with Task 1 as they are logically cohesive)
3. **Task 3: Playwright test for calendar rendering** - `c2c49ff` (test)

**Plan metadata:** _(docs commit — this SUMMARY)_

## Files Created/Modified

- `frontend/src/components/calendar/FullCalendarWrapper.tsx` — FullCalendar React wrapper; useBookings/useLocations integration; location colour-coded events; dateClick/eventClick handlers
- `frontend/src/components/calendar/BookingEvent.tsx` — eventContent renderer with ConflictBadge
- `frontend/src/pages/CalendarView.tsx` — Page using FullCalendarWrapper; sr-only h1; onDateClick/onEventClick stubs for later modal integration
- `frontend/src/pages/BookingsView.tsx` — Updated to render `<CalendarView />` for `view=month`
- `frontend/package.json` — Added @fullcalendar packages
- `frontend/e2e/calendar-view.spec.ts` — 10 Playwright E2E tests (3 test suites)

## Decisions Made

- `onDateClick`/`onEventClick` props on `FullCalendarWrapper` — modal integration deferred to plans 09-06/09-07; handlers are wired and ready
- `sr-only` h1 with text "Calendar View" — required for navigation spec compatibility and ARIA accessibility
- Playwright route pattern `*://localhost:8080/**` with pathname dispatch — catches all API gateway calls in one handler, avoiding per-endpoint patterns that fail for unknown paths

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] BookingsView.tsx rendered inline placeholder instead of CalendarView**
- **Found during:** Task 3 (E2E test debugging)
- **Issue:** Subsequent commits from plans 09-06 through 09-15 had not yet integrated `CalendarView` into `BookingsView.tsx` at their original authoring time; the working tree showed an old placeholder for `view=month`
- **Fix:** Updated `BookingsView.tsx` to `import { CalendarView } from './CalendarView'` and render `<CalendarView />` for `view=month`, while keeping `DayView` and `ListView` from other plans
- **Files modified:** `frontend/src/pages/BookingsView.tsx`
- **Verification:** E2E debug test confirmed `.fc` DOM element renders; all 10 Playwright tests pass
- **Committed in:** Already at HEAD (`9a0a991` had the correct version; local write matched HEAD exactly)

**2. [Rule 1 - Bug] E2E API route pattern needed glob prefix for cross-protocol matching**
- **Found during:** Task 3 (E2E test investigation)
- **Issue:** Initial pattern `http://localhost:8080/bookings**` was not intercepting API calls; `*://localhost:8080/**` with URL pathname dispatch correctly intercepts all API gateway requests
- **Fix:** Changed to single catch-all route handler with pathname-based dispatch
- **Files modified:** `frontend/e2e/calendar-view.spec.ts`
- **Verification:** All 10 tests pass after fix

---

**Total deviations:** 2 auto-fixed (both Rule 1 bugs)
**Impact on plan:** Both fixes necessary for E2E tests to pass. No scope creep.

## Known Stubs

- `frontend/src/pages/CalendarView.tsx:7` — `// TODO: Open Booking Create Form pre-filled with slot.start — integrated in 09-06` — **Cosmetic**: click handler logs debug; modal integration is explicitly planned for 09-06
- `frontend/src/pages/CalendarView.tsx:12` — `// TODO: Open Booking Detail Modal — integrated in 09-07` — **Cosmetic**: click handler logs debug; modal integration is explicitly planned for 09-07

Neither stub is blocking: the calendar renders, events display, conflict badges show, and click handlers are wired. The TODOs document integration points for subsequent plans.

## Issues Encountered

- Stale Playwright/Chromium processes from earlier tool calls caused resource contention during E2E debugging; resolved by forcefully terminating background processes
- Playwright `webServer` config starts vite with `reuseExistingServer: !process.env.CI` — tests used the running server automatically

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- `CalendarView` complete with FullCalendar, location colours, conflict badges, click hooks
- `FullCalendarWrapper.onDateClick` prop ready for Booking Create Form (09-06)
- `FullCalendarWrapper.onEventClick` prop ready for Booking Detail Modal (09-07)
- 10 E2E tests protect calendar rendering and interaction contracts

---
*Phase: 09-core-user-interface*
*Completed: 2026-10-13*

## Self-Check: PASSED

- ✅ `frontend/src/components/calendar/FullCalendarWrapper.tsx` — FOUND
- ✅ `frontend/src/components/calendar/BookingEvent.tsx` — FOUND
- ✅ `frontend/src/pages/CalendarView.tsx` — updated with FullCalendarWrapper
- ✅ `frontend/e2e/calendar-view.spec.ts` — FOUND
- ✅ Commits `e22c1a0`, `c2c49ff` — FOUND in git log
- ✅ Build check: `npm run build` → exit 0 (188 modules, dist/ generated)
- ✅ E2E tests: 10/10 passing (3.2s runtime)
- ✅ No blocking stubs found
