---
phase: 09-core-user-interface
plan: "11"
subsystem: ui
tags: [react, typescript, playwright, vitest, modal, permissions, booking]

requires:
  - phase: 09-core-user-interface
    provides: API client layer (09-02), StateConfirmationBanner (09-03), BookingCreate (09-09), BookingEdit (09-10)
  - phase: 05-core-booking-approval-workflow
    provides: Booking entity with status, conflict_flags, series_id, owner_id fields

provides:
  - BookingDetailModal component: fetches and renders all booking fields, status badge, conflict info, audit link, permission-gated actions
  - StatusBadge component: colored status badge (pending=yellow, approved=green, denied=red) with relative timestamp tooltip
  - Playwright E2E test: 19 tests covering all 9 acceptance criteria for UI-BOOK-03
  - CalendarView integration: clicking booking events opens BookingDetailModal
  - getById alias on bookingsApi for UI-BOOK-03 contract compliance
  - deleteBookingWithScope API method for DELETE /bookings/{id}?scope={scope}
  - /admin/audit route for audit trail navigation from modal

affects:
  - All views that display bookings (CalendarView, DayView, ListView, ApprovalsQueue)
  - Any plan consuming the bookings API (getById alias)

tech-stack:
  added: []
  patterns:
    - "BookingDetailModal: nested modal pattern (conflict booking ID opens nested detail)"
    - "Permission gate: structural absence (returns null) not disabled — deny-by-default UI-PATTERN-03"
    - "E2E userId injection: window.__E2E_USER_ID__ via page.addInitScript() for owner-check tests"
    - "keycloak.subject mock in AuthProvider E2E mode for per-test user identity"

key-files:
  created:
    - frontend/src/components/booking/BookingDetailModal.tsx
    - frontend/src/components/booking/StatusBadge.tsx
    - frontend/e2e/booking-detail.spec.ts
  modified:
    - frontend/src/api/bookings.ts
    - frontend/src/components/booking/BookingForm.tsx
    - frontend/src/components/booking/CustomFieldRenderer.tsx
    - frontend/src/pages/BookingCreate.tsx
    - frontend/src/pages/BookingEdit.tsx
    - frontend/src/pages/CalendarView.tsx
    - frontend/src/auth/AuthProvider.tsx
    - frontend/src/routes/index.tsx

key-decisions:
  - "keycloak.subject used for owner_id comparison in permission check (owner_id = booking.created_by)"
  - "Conflict booking click opens nested BookingDetailModal (same component, recursive)"
  - "Audit link navigates to /admin/audit?entity_type=booking&entity_id={id} via React Router (not href)"
  - "AuthProvider E2E mode now sets keycloak.subject from window.__E2E_USER_ID__ for per-test identity"
  - "getById alias added to bookingsApi alongside getBookingById for contract compliance"
  - "Pre-existing build errors from 09-09/09-10 plans fixed as Rule 3 blocking deviation"

patterns-established:
  - "BookingDetailModal accepts bookingId prop (null = closed) and onClose/onDeleted callbacks"
  - "StatusBadge: status prop (pending|approved|denied) + timestamp prop → badge with relative time tooltip"

duration: 14min
completed: "2026-10-13"
---

# Phase 9 Plan 11: Booking Detail Modal Summary

**BookingDetailModal (UI-BOOK-03) with StatusBadge, conflict info display, permission-gated Edit/Delete/Clone actions, and 19-test Playwright E2E suite integrated into CalendarView**

## Performance

- **Duration:** 14 min
- **Started:** 2026-10-13T18:17:34Z
- **Completed:** 2026-10-13T18:31:33Z
- **Tasks:** 3
- **Files modified:** 10

## Accomplishments

- `BookingDetailModal.tsx` — modal with all booking fields: title, StatusBadge, Recurring chip, location (with colour dot), start/end times, duration, resources, description, custom field values, conflict section, audit trail link
- `StatusBadge.tsx` — yellow/green/red badge (pending/approved/denied) with relative timestamp text and absolute tooltip
- Conflict section: red "Conflicts detected" banner, clickable booking IDs opening nested detail modal
- Permission-gated actions: Edit/Delete structurally absent for non-owner/non-admin (deny-by-default UI-PATTERN-03), Clone always visible
- Delete flow: confirmation dialog for non-recurring; scope selection (this occurrence / entire series) for recurring
- Clone flow: navigates to `/bookings/create?title={title} (Copy)&location_id=...&start_time=+1day&end_time=+1day`
- Playwright E2E test: 19 tests, all passing, covering all 9 plan scenarios
- CalendarView integrated: `onEventClick` callback opens BookingDetailModal

## Task Commits

1. **Task 1: BookingDetailModal + StatusBadge** - `d165b67` (feat)
2. **Task 2: Permission-gated Edit/Delete/Clone** - `96e5a47` (feat)
3. **Task 3: Playwright E2E tests** - `752533f` (test)

**Plan metadata:** (committed in docs commit)

## Files Created/Modified

- `frontend/src/components/booking/BookingDetailModal.tsx` — Full modal implementation (Tasks 1+2)
- `frontend/src/components/booking/StatusBadge.tsx` — Status badge with relative time
- `frontend/e2e/booking-detail.spec.ts` — 19 Playwright E2E tests (all passing)
- `frontend/src/pages/CalendarView.tsx` — Integrates BookingDetailModal on event click
- `frontend/src/auth/AuthProvider.tsx` — E2E mode: set keycloak.subject from __E2E_USER_ID__
- `frontend/src/api/bookings.ts` — Added getById alias, deleteBookingWithScope, patchBooking methods
- `frontend/src/components/booking/BookingForm.tsx` — Fixed pre-existing type errors (Rule 3)
- `frontend/src/components/booking/CustomFieldRenderer.tsx` — Exported CustomFieldValues type (Rule 3)
- `frontend/src/pages/BookingCreate.tsx` — Fixed props incompatibility with current BookingForm (Rule 3)
- `frontend/src/pages/BookingEdit.tsx` — Fixed allowApproveBooking prop removed (Rule 3)
- `frontend/src/routes/index.tsx` — Added BookingCreate import + /admin/audit route

## Decisions Made

- Used `keycloak.subject` for `owner_id` comparison. The `Booking.owner_id` field is the same as `created_by` (plan spec) — the backend uses `owner_id`, which matches the Keycloak `sub` claim
- Conflict booking link opens a **nested** `BookingDetailModal` (same component recursively) instead of replacing current — allows inspecting both conflicting bookings simultaneously
- `/admin/audit` route added as a separate route (renders AuditLogViewer) so the audit link works with query params (`entity_type`, `entity_id`)
- `window.__E2E_USER_ID__` injection mechanism added to AuthProvider E2E mode for per-test user identity control (parallel to existing `__E2E_ROLES__`)

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Pre-existing TypeScript build errors from plans 09-09/09-10**
- **Found during:** Task 1 (build verification)
- **Issue:** `CustomFieldRenderer.tsx` missing `CustomFieldValues` type export; `BookingForm.tsx` passing wrong props to ConflictWarning; `BookingCreate.tsx` using non-existent `BookingFormErrors` type and wrong `BookingForm` props interface
- **Fix:** Exported `CustomFieldValues` type from CustomFieldRenderer; updated BookingForm's ConflictWarning usage to match actual ConflictWarning props; rewrote BookingCreate to use actual BookingForm API
- **Files modified:** BookingForm.tsx, CustomFieldRenderer.tsx, BookingCreate.tsx, BookingEdit.tsx
- **Verification:** `npm run build` exits 0, all 74 unit tests pass
- **Committed in:** `d165b67` (Task 1 commit)

**2. [Rule 3 - Blocking] Missing `getById` API method (contract requires `getById`, actual is `getBookingById`)**
- **Found during:** Task 1 (contract verification check)
- **Issue:** Plan's integration contract checks for `getById` but the existing API used `getBookingById`
- **Fix:** Added `getById` as alias on `bookingsApi` pointing to same endpoint
- **Files modified:** frontend/src/api/bookings.ts
- **Committed in:** `d165b67` (Task 1 commit)

**3. [Rule 3 - Blocking] Missing `patchBooking` method needed by BookingEdit (pre-existing)**
- **Found during:** Task 1 (build verification)
- **Issue:** BookingEdit referenced `bookingsApi.patchBooking` which didn't exist
- **Fix:** Added `patchBooking` to bookingsApi (PATCH with optional scope query param)
- **Files modified:** frontend/src/api/bookings.ts
- **Committed in:** `d165b67` (Task 1 commit)

**4. [Rule 3 - Blocking] Missing BookingCreate import in routes/index.tsx**
- **Found during:** Task 1 (build verification)
- **Issue:** routes/index.tsx used `<BookingCreate />` without importing it
- **Fix:** Added import for BookingCreate from ../pages/BookingCreate
- **Files modified:** frontend/src/routes/index.tsx
- **Committed in:** `d165b67` (Task 1 commit)

---

**Total deviations:** 4 auto-fixed (all Rule 3 — Blocking)
**Impact on plan:** All fixes necessary to unblock build. Fixed pre-existing integration mismatches between 09-09/09-10 implementations. No scope creep.

## Known Stubs

None found.

## Issues Encountered

- Playwright test #15 (recurring scope DELETE) failed on first run due to route handler ordering (second `page.route()` overrode first catch-all, blocking `GET /bookings/bk-recurring`). Fixed by registering all API mocks in a single handler before `page.goto()`.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- BookingDetailModal complete and integrated into CalendarView — DayView and ListView can add the same integration by importing and using the modal with `onEventClick`
- All 9 UI-BOOK-03 acceptance criteria verified by Playwright tests
- StatusBadge reusable in ApprovalQueue and other views that need status display
- Plan 09-11 is a leaf plan (enables: none) — all dependent views can now use the modal

---
*Phase: 09-core-user-interface*
*Completed: 2026-10-13*

## Self-Check: PASSED

- `frontend/src/components/booking/BookingDetailModal.tsx` — FOUND
- `frontend/src/components/booking/StatusBadge.tsx` — FOUND
- `frontend/e2e/booking-detail.spec.ts` — FOUND
- Commits `d165b67`, `96e5a47`, `752533f` — FOUND
- Build check: `npm run build -- --mode development` → exit 0 (204 modules transformed)
- Unit tests: 74/74 passing
- Playwright E2E: 19/19 passing
