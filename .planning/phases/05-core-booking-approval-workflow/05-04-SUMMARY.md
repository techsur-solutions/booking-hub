---
phase: 05-core-booking-approval-workflow
plan: "04"
subsystem: api
tags: [spring-boot, jpa, postgresql, rest-api, booking, conflict-detection, approval-workflow, outbox]

# Dependency graph
requires:
  - phase: 05-01
    provides: "Booking entity, repositories, error exceptions (ApprovalForbiddenException, ApprovalInvalidStateException, ApprovalBookingNotFoundException, BookingNotFoundException)"
  - phase: 05-02
    provides: "ConflictDetectionService (shared engine), LocationsResourcesClient, CustomFieldClient"
  - phase: 05-03
    provides: "OutboxEvent publishing pattern (BookingWriteService precedent)"
provides:
  - "GET /bookings (calendar/day/list views as query-parameter variants, with conflict_flags via shared engine in read-only mode)"
  - "GET /bookings/{id} (AJAX-style detail, last-known-values for soft-deleted, 200 not 404)"
  - "POST /bookings/{id}/approve (one-way pending→approved transition, booking.approved outbox event)"
  - "POST /bookings/{id}/deny (one-way pending→denied transition, booking.denied outbox event)"
  - "BookingReadService, BookingApprovalService, BookingQueryController, BookingApprovalController"
  - "BookingReadDtos (self-contained read DTOs: BookingDetailResponse, ConflictFlag, DenyBookingRequest)"
affects: [phase-06-notifications, phase-07-calendar-ui, integration-tests]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Native query with CAST(:param AS text) for nullable string parameters in PostgreSQL JPQL"
    - "Read-only conflict detection: same ConflictDetectionService as create/edit, invoked in pure read mode (never blocks)"
    - "Last-known-values detail read: plain findById (not findByIdAndDeletedAtIsNull) mirroring Phase 4's precedent"
    - "Manual hasRole check without @PreAuthorize for FRD-named APPROVAL_FORBIDDEN code (Phase 3 precedent)"
    - "One-way state transitions: pending-only enforcement blocks both same-direction and cross-direction re-toggling"

key-files:
  created:
    - services/booking-service/src/main/java/com/bookinghub/booking/dto/BookingReadDtos.java
    - services/booking-service/src/main/java/com/bookinghub/booking/service/BookingReadService.java
    - services/booking-service/src/main/java/com/bookinghub/booking/service/BookingApprovalService.java
    - services/booking-service/src/main/java/com/bookinghub/booking/controller/BookingQueryController.java
    - services/booking-service/src/main/java/com/bookinghub/booking/controller/BookingApprovalController.java
    - services/booking-service/src/test/java/com/bookinghub/booking/controller/BookingQueryControllerIntegrationTest.java
    - services/booking-service/src/test/java/com/bookinghub/booking/controller/BookingApprovalControllerIntegrationTest.java
  modified:
    - services/booking-service/src/main/java/com/bookinghub/booking/repository/BookingRepository.java

key-decisions:
  - "calendar/day/list views are query-parameter variants of ONE GET /bookings endpoint (named addition: status, q beyond TechArch §4.2's literal 4-param table) — frontend composes views from params, no separate routes"
  - "getById uses plain findById (not findByIdAndDeletedAtIsNull) → 200 with last-known values for soft-deleted, 404 only for genuinely nonexistent id — mirroring Phase 4's precedent for cross-system consistency"
  - "BookingApprovalController has NO @PreAuthorize — manual hasRole check produces APPROVAL_FORBIDDEN code instead of generic BOOKING_FORBIDDEN (Phase 3 PermissionController precedent)"
  - "One-way transitions block BOTH same-direction (approved→approved) AND cross-direction (approved→denied) re-toggling with identical 409 APPROVAL_INVALID_STATE"
  - "ConflictDetectionService is called in pure read-only mode for bulk reads — result is only attached as conflict_flags, never used to block or reject (proves already-saved and later-emerged conflicts are visible in calendar)"

patterns-established:
  - "Native query with CAST(:q AS text) + COALESCE for nullable text column searches in PostgreSQL"
  - "Per-booking N+1 conflict-detection in list reads (accepted tradeoff per THREAT T-05-04-04)"

# Metrics
duration: 13min
completed: 2026-10-09
---

# Phase 5 Plan 04: Booking Read Side + Approval Workflow Summary

**GET /bookings (calendar/day/list with conflict_flags), GET /bookings/{id} (last-known-values), POST approve/deny (one-way pending→approved/denied transitions with distinct booking.approved/booking.denied domain events) — 14 integration tests passing**

## Performance

- **Duration:** 13 min
- **Started:** 2026-10-09T19:21:54Z
- **Completed:** 2026-10-09T19:35:23Z
- **Tasks:** 2 completed
- **Files modified:** 8 (1 modified + 7 created)

## Accomplishments

- **Calendar/day/list read API**: `GET /bookings` with half-open range-overlap query (pure JPQL/native) and optional location_id, resource_id, status, q filters — all as query-parameter variants of ONE endpoint, no separate routes
- **Bulk conflict-flagging**: Each returned booking carries conflict_flags computed by the SAME ConflictDetectionService create/edit uses, invoked in pure read-only mode — proves already-saved soft-warning conflicts and later-emerged conflicts are visible in calendar views (ROADMAP Success Criterion 2)
- **Last-known-values detail read**: `GET /bookings/{id}` returns 200 with deletedAt populated for soft-deleted bookings; 404 only for genuinely nonexistent ids — mirroring Phase 4's established Locations/Resources precedent
- **One-way approve/deny workflow**: `POST /bookings/{id}/approve` and `POST /bookings/{id}/deny` enforce pending-only state before mutating, blocking BOTH same-direction re-approval AND cross-direction approved→denied with 409 APPROVAL_INVALID_STATE (ROADMAP Success Criterion 5)
- **FRD-named APPROVAL_FORBIDDEN code**: Manual hasRole check (NO @PreAuthorize) in BookingApprovalController produces the FRD-exact error code, not the generic BOOKING_FORBIDDEN — matching Phase 3's PermissionController precedent
- **14 integration tests**: 8 scenarios for GET /bookings (range/day filters, location/status/keyword, conflict-flagging, last-known-values, auth) + 6 scenarios for approve/deny (approve, one-way proof, deny with reason, cross-direction one-way proof, APPROVAL_FORBIDDEN, APPROVAL_BOOKING_NOT_FOUND)

## Task Commits

Each task was committed atomically:

1. **Task 1: BookingReadDtos + findInRange query + BookingReadService** - `361a56d` (feat)
2. **Task 2: BookingApprovalService + controllers + integration tests** - `d214ecb` (feat)

**Plan metadata:** `[pending docs commit]` (docs: complete plan)

## Files Created/Modified

- `services/booking-service/src/main/java/com/bookinghub/booking/repository/BookingRepository.java` - Added findInRange native query with range-overlap semantics and optional filters
- `services/booking-service/src/main/java/com/bookinghub/booking/dto/BookingReadDtos.java` - BookingDetailResponse, ConflictFlag, DenyBookingRequest (self-contained, not cross-plan imports)
- `services/booking-service/src/main/java/com/bookinghub/booking/service/BookingReadService.java` - listBookings (bulk read with conflict-flagging) and getById (last-known-values)
- `services/booking-service/src/main/java/com/bookinghub/booking/service/BookingApprovalService.java` - approve/deny with one-way pending-only enforcement and distinct outbox events
- `services/booking-service/src/main/java/com/bookinghub/booking/controller/BookingQueryController.java` - GET /bookings and GET /bookings/{id}
- `services/booking-service/src/main/java/com/bookinghub/booking/controller/BookingApprovalController.java` - POST /bookings/{id}/approve and POST /bookings/{id}/deny (NO @PreAuthorize)
- `services/booking-service/src/test/java/com/bookinghub/booking/controller/BookingQueryControllerIntegrationTest.java` - 8 query integration test scenarios
- `services/booking-service/src/test/java/com/bookinghub/booking/controller/BookingApprovalControllerIntegrationTest.java` - 6 approval integration test scenarios

## Decisions Made

- **Calendar/day/list as one endpoint**: Legacy's index/day/list/building/location actions all queried the same Event model with different where-clause compositions. TechArch lists exactly one `GET /bookings` entry. Status and q are named additions beyond TechArch's literal 4-param table, per F0 findings/01-booking-core.md's confirmed legacy list-view capabilities.
- **getById last-known-values policy**: Plain `findById` (not `findByIdAndDeletedAtIsNull`) returns 200 with last-known values for soft-deleted bookings — mirrors Phase 4's exact precedent for Locations/Resources, establishing cross-system consistency. Deliberate extension of an adopted system-wide pattern.
- **NO @PreAuthorize on BookingApprovalController**: Manual `hasRole` check produces `APPROVAL_FORBIDDEN` (FRD-named), not generic `BOOKING_FORBIDDEN` (what Spring Security's AccessDeniedException path would produce). Same design as Phase 3's PermissionController.
- **One-way transitions block cross-direction re-toggling**: approved→denied is also 409 APPROVAL_INVALID_STATE (not just approved→approved). Both directions are proven by integration tests.
- **BookingReadDtos self-contained**: ConflictFlag defined independently in BookingReadDtos (not imported from ConflictDtos) per the parallel-safety design note — plans 05-03 and 05-04 run in the same wave with separate file ownership.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Fixed JPQL findInRange query — PostgreSQL lower(bytea) type error for nullable description**
- **Found during:** Task 2 (integration test execution, scenario 1-5 all failed with `ERROR: function lower(bytea) does not exist`)
- **Issue:** The initial JPQL query used `LOWER(b.description)` which, when the description column is NULL and the parameter `:q` is also nullable, caused PostgreSQL to resolve to `lower(bytea)` instead of `lower(text)` — a type resolution failure in the JDBC binding layer
- **Fix:** Converted to native SQL with `CAST(:q AS text)` for the nullable keyword parameter and `COALESCE(description, '')` for the nullable description column, avoiding the type ambiguity entirely
- **Files modified:** services/booking-service/src/main/java/com/bookinghub/booking/repository/BookingRepository.java
- **Verification:** All 8 BookingQueryControllerIntegrationTest scenarios passed after the fix
- **Committed in:** d214ecb (Task 2 commit)

---

**Total deviations:** 1 auto-fixed (1 bug)
**Impact on plan:** The fix was necessary for correctness — the JPQL query would have failed on any booking with a null description. Switching to a native SQL query with explicit type casts is the standard PostgreSQL approach for nullable parameter handling. No scope creep.

## Issues Encountered

None - all scenarios passed after the findInRange query bug fix.

## User Setup Required

None - no external service configuration required.

## Known Stubs

None found — all handlers implement real logic:
- `listBookings` calls real ConflictDetectionService, real BookingRepository.findInRange, real resource lookups
- `getById` uses real findById with conflict detection
- `approve` and `deny` mutate real DB rows and write real outbox events

## Next Phase Readiness

- ROADMAP Success Criteria 2 (read views with conflict_flags) and 5 (approve/deny domain events) are fully implemented and proven by 14 integration tests
- Phase 5 core booking workflow is now complete (plans 05-01 through 05-04)
- Plan 05-04 provides the read endpoint pattern (BookingDetailResponse shape) that approval endpoints reuse — BookingApprovalService.approve/deny return full BookingDetailResponse via BookingReadService.getById
- No blockers for downstream phases

---
*Phase: 05-core-booking-approval-workflow*
*Completed: 2026-10-09*

## Self-Check: PASSED

- [x] BookingReadDtos.java exists: FOUND
- [x] BookingReadService.java exists: FOUND
- [x] BookingApprovalService.java exists: FOUND
- [x] BookingQueryController.java exists: FOUND
- [x] BookingApprovalController.java exists: FOUND
- [x] BookingQueryControllerIntegrationTest.java exists: FOUND
- [x] BookingApprovalControllerIntegrationTest.java exists: FOUND
- [x] Commits exist: 361a56d (Task 1), d214ecb (Task 2)
- [x] Build check: `mvn -q -DskipTests package` → exit 0 (PASSED)
- [x] Integration tests: `mvn test -Dtest=BookingQueryControllerIntegrationTest,BookingApprovalControllerIntegrationTest` → 14 tests run, 0 failures, 0 errors (PASSED)
- [x] Known Stubs: None found
