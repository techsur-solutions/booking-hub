---
phase: 05-core-booking-approval-workflow
plan: 03
subsystem: api
tags: [spring-boot, jpa, booking, conflict-detection, recurrence, integration-tests]

requires:
  - phase: 05-01
    provides: Booking entity, repositories, error hierarchy (BookingConflictException, BookingForbiddenException, etc.)
  - phase: 05-02
    provides: ConflictDetectionService, LocationsResourcesClient, CustomFieldClient, SettingsClient

provides:
  - POST /bookings (single and recurring series) with full legacy field set
  - PUT /bookings/{id} with partial update semantics and ownership check
  - DELETE /bookings/{id} with soft-delete and ownership check
  - POST /bookings/{id}/clone returning a non-persisted draft response
  - BookingDtos (CreateRequest, UpdateRequest, Response, RecurrenceDefinition, custom field types)
  - RecurrenceExpander: weekly-by-day-of-week expansion with FRD's interim shape
  - The single conflict-enforcement-policy applyConflictPolicy() call site (shared by create and update)
  - 11-scenario BookingControllerIntegrationTest covering all create/edit/delete/clone behaviors
  - 4-scenario ConflictEnforcementPolicyTest pinning the hard-block/soft-warning policy decision

affects: [05-04, 05-05, 05-06]

tech-stack:
  added: []
  patterns:
    - "Permission-conditional conflict policy: single applyConflictPolicy() shared by create+update"
    - "Bypass-approve via role_booking_approver (Phase 3's folded role)"
    - "Recurring series: bypass applied only to occurrence #1 (F0 legacy parity)"
    - "Clone returns non-persisted draft with null id (F0 confirmed legacy behavior)"
    - "JWT claim dual-path in tests: jwt().authorities() for @PreAuthorize + realm_access.roles claim for CurrentUserProvider.hasRole()"

key-files:
  created:
    - services/booking-service/src/main/java/com/bookinghub/booking/dto/BookingDtos.java
    - services/booking-service/src/main/java/com/bookinghub/booking/recurrence/RecurrenceExpander.java
    - services/booking-service/src/main/java/com/bookinghub/booking/service/BookingWriteService.java
    - services/booking-service/src/main/java/com/bookinghub/booking/controller/BookingController.java
    - services/booking-service/src/test/java/com/bookinghub/booking/recurrence/RecurrenceExpanderTest.java
    - services/booking-service/src/test/java/com/bookinghub/booking/controller/BookingControllerIntegrationTest.java
    - services/booking-service/src/test/java/com/bookinghub/booking/controller/ConflictEnforcementPolicyTest.java
  modified:
    - services/booking-service/src/main/java/com/bookinghub/booking/conflict/dto/ConflictDtos.java

key-decisions:
  - "Conflict-enforcement-policy: permission-conditional (role_booking_approver = soft warning, no role = 409 hard block) — DIVERGES from TechArch §1.5's uniform hard-block in favor of ROADMAP's literal Success Criterion 4 wording"
  - "applyConflictPolicy() defined ONCE as a private method, called identically from create, update, and recurring-series helper — never duplicated per entry point"
  - "Bypass-approve applies ONLY to occurrence #1 in a recurring series (confirmed F0 legacy parity from findings/01-booking-core.md)"
  - "Clone returns non-persisted draft (null id, null seriesId, caller's ownerId, fresh status) per F0's confirmed legacy behavior — client must POST to persist"
  - "No scope/EditScope field anywhere in controller or DTOs — F0 confirmed legacy has no series-scoped edit concept past creation time (open-questions.md #1)"
  - "emailContact transient: CreateRequest field only, never persisted, flows only into booking.created outbox payload"
  - "Custom field validation real enforcement (not no-op) now that plan 04-04's carve-out makes normal callers succeed; graceful degradation only on genuine connectivity failures"
  - "ConflictFlag/ConflictCheckResult given snake_case @JsonProperty for TechArch §4.2 wire consistency"

patterns-established:
  - "JWT test setup requires BOTH jwt().authorities() (for @PreAuthorize) AND realm_access.roles claim (for CurrentUserProvider.hasRole())"
  - "UUID sub claim must be explicitly set in test JWTs — Spring Security Test defaults to 'user' (invalid UUID)"

duration: 23min
completed: 2026-10-09
---

# Phase 5 Plan 3: Booking Write Service with Permission-Conditional Conflict Enforcement Summary

**Permission-conditional conflict enforcement (hard-block for non-approver, soft-warning for role_booking_approver) through a single shared applyConflictPolicy() call site, with full booking create/edit/delete/clone API and weekly recurrence expansion**

## Performance

- **Duration:** 23 min
- **Started:** 2026-10-09T18:54:48Z
- **Completed:** 2026-10-09T19:18:01Z
- **Tasks:** 3
- **Files modified:** 8

## Accomplishments

- Implemented the plan's central design decision: the conflict-enforcement-policy is applied through exactly ONE private `applyConflictPolicy()` method shared by `create`, `update`, and the recurring series helper — never duplicated per entry point
- Full booking CRUD API: `POST /bookings` (single + recurring series), `PUT /bookings/{id}`, `DELETE /bookings/{id}`, `POST /bookings/{id}/clone` with the complete legacy field set
- 20 tests pass: 5 unit tests (RecurrenceExpanderTest), 11 integration scenarios (BookingControllerIntegrationTest), 4 conflict-policy scenarios (ConflictEnforcementPolicyTest) — all proving the permission-conditional split identically for create and edit

## Task Commits

Each task was committed atomically:

1. **Task 1: BookingDtos + RecurrenceExpander + RecurrenceExpanderTest** - `9feee0c` (feat)
2. **Task 2: BookingWriteService** - `9ff02d7` (feat)
3. **Task 3: BookingController + Integration Tests** - `07ddafb` (feat)

## Files Created/Modified

- `services/booking-service/src/main/java/com/bookinghub/booking/dto/BookingDtos.java` — BookingCreateRequest, BookingUpdateRequest, BookingResponse, RecurrenceDefinition, CustomFieldValueInput/Output with snake_case @JsonProperty
- `services/booking-service/src/main/java/com/bookinghub/booking/recurrence/RecurrenceExpander.java` — FRD's interim weekly shape: 0=Sun day-of-week numbering, excludes base occurrence, preserves time-of-day span
- `services/booking-service/src/main/java/com/bookinghub/booking/service/BookingWriteService.java` — The plan's central implementation: single applyConflictPolicy(), bypass-approve, ownership check, clone as draft
- `services/booking-service/src/main/java/com/bookinghub/booking/controller/BookingController.java` — @PreAuthorize role gating per-HTTP-method, no scope field anywhere
- `services/booking-service/src/main/java/com/bookinghub/booking/conflict/dto/ConflictDtos.java` — Added snake_case @JsonProperty to ConflictFlag and ConflictCheckResult
- `services/booking-service/src/test/java/com/bookinghub/booking/recurrence/RecurrenceExpanderTest.java` — 5 unit tests for weekly expansion
- `services/booking-service/src/test/java/com/bookinghub/booking/controller/BookingControllerIntegrationTest.java` — 11 integration scenarios
- `services/booking-service/src/test/java/com/bookinghub/booking/controller/ConflictEnforcementPolicyTest.java` — 4 policy scenarios

## Decisions Made

1. **Conflict-enforcement-policy (ROADMAP vs TechArch divergence, the plan's core decision):** Implemented ROADMAP's literal Success Criterion 4 wording (permission-conditional: hard-block for non-approver, soft-warning for role_booking_approver). This DIVERGES from TechArch §1.5's documented "uniform hard-block for ALL roles" decision. The ROADMAP is this plan's own current, most-specific directive. TechArch §1.5 should be corrected in a future documentation pass.

2. **Single applyConflictPolicy() call site:** Private method defined ONCE, invoked identically from `create`, `update`, and the recurring series helper. ConflictEnforcementPolicyTest's parallel create/edit scenarios prove zero behavioral divergence.

3. **Bypass-approve to occurrence #1 only (F0 legacy parity):** F0 findings/01-booking-core.md confirmed legacy's bypass only runs once, on the original event object, before the loop. Subsequent recurring occurrences receive the plain approveBooking-driven default. This specific quirk is preserved deliberately.

4. **Clone as non-persisted draft:** F0 confirmed legacy clone "renders a pre-filled add form, does not persist." Returns 201 with null id. The F0 clone/conflict-check quirk (legacy preserved source id to exclude it from conflict check) cannot mechanically recur with this design — a fresh POST /bookings conflict-checks against ALL existing bookings, which is more correct.

5. **No scope/EditScope field:** F0 confirmed legacy's update()/delete() always target a single row by id unconditionally. The BookingUpdateRequest has no scope field — this is intentional design, not an oversight, documented in both the DTO javadoc and ConflictControllerIntegrationTest scenario 8 proof.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Exception constructors require String argument**
- **Found during:** Task 2 (BookingWriteService implementation)
- **Issue:** Plan specified no-arg exception throws (e.g., `new BookingLocationNotFoundException()`) but all exception classes in plan 05-01 require a String argument
- **Fix:** Updated all throws to pass the relevant ID as string: `new BookingLocationNotFoundException(locationId.toString())`
- **Files modified:** `BookingWriteService.java`
- **Verification:** Build succeeds with all exception calls
- **Committed in:** `9ff02d7`

**2. [Rule 1 - Bug] ConflictFlag JSON field names used camelCase by default (not snake_case)**
- **Found during:** Task 3 (ConflictEnforcementPolicyTest)
- **Issue:** Test asserted `conflicting_booking_id` and `conflict_type` (snake_case per TechArch §4.2 convention) but Jackson serialized the ConflictFlag record component names as camelCase (`conflictingBookingId`, `conflictType`)
- **Fix:** Added `@JsonProperty` snake_case annotations to `ConflictFlag` and `ConflictCheckResult` records in ConflictDtos.java. Existing `ConflictControllerIntegrationTest` was re-run to confirm no regression (all 5 scenarios still pass)
- **Files modified:** `ConflictDtos.java`
- **Verification:** All 15 BookingControllerIntegrationTest + ConflictEnforcementPolicyTest scenarios pass; ConflictControllerIntegrationTest 5 scenarios still pass
- **Committed in:** `07ddafb`

**3. [Rule 1 - Bug] Spring Security Test JWT default sub claim "user" is not a valid UUID**
- **Found during:** Task 3 (integration tests running)
- **Issue:** `jwt()` postprocessor in Spring Security Test defaults to `sub=user`. `CurrentUserProvider.getCurrentUserId()` calls `UUID.fromString(sub)`, throwing `IllegalArgumentException` for the string "user"
- **Fix:** All tests now explicitly set `sub` to a UUID string via `.jwt(j -> j.claim("sub", callerId.toString()))`. Tests also explicitly set `realm_access.roles` claim since `CurrentUserProvider.hasRole()` reads JWT claims directly, not Spring Security's GrantedAuthority list
- **Files modified:** `BookingControllerIntegrationTest.java`, `ConflictEnforcementPolicyTest.java`
- **Verification:** All 15 tests pass after fix
- **Committed in:** `07ddafb`

---

**Total deviations:** 3 auto-fixed (1 missing critical, 2 bugs)
**Impact on plan:** All auto-fixes necessary for correctness and test execution. No scope creep. The ConflictFlag snake_case fix is a correctness improvement aligned with the plan's own TechArch §4.2 wire convention mandate.

## Issues Encountered

**Maven build via Docker:** Java and Maven are not natively installed in the sandbox. The project uses Docker for builds. Maven Central was rate-limited (429) when Docker tried to fetch dependencies. Resolution: Used the `maven-repo` Docker volume (populated during plan 05-02's build) by removing `_remote.repositories` files and running Maven offline (`-o`) with the volume as the local repository.

## User Setup Required

None - no external service configuration required.

## Known Stubs

None found — all handlers implement real behavior. The `BookingWriteService.clone()` method's status recomputation and the graceful degradation path in `validateCustomFieldValues()` are real implementations, not stubs.

## Next Phase Readiness

- Plan 05-03 complete: POST /bookings, PUT /bookings/{id}, DELETE /bookings/{id}, POST /bookings/{id}/clone all implemented and tested
- ROADMAP Success Criteria 1, 3, 4, and 5 (bypass-approve stage) are real, callable behavior
- Plan 05-04 can proceed: it independently owns `BookingQueryController` and read-side DTOs, consuming the same 05-01/05-02 dependencies (no dependency on 05-03 — deliberate parallel-safe file ownership design)
- TechArch §1.5 needs a documentation-pass correction: its "uniform hard-block" row should be updated to record the permission-conditional policy as final, citing this plan

## Self-Check: PASSED

- BookingDtos.java: FOUND
- RecurrenceExpander.java: FOUND
- BookingWriteService.java: FOUND
- BookingController.java: FOUND
- RecurrenceExpanderTest.java: FOUND (5 tests passing)
- BookingControllerIntegrationTest.java: FOUND (11 tests passing)
- ConflictEnforcementPolicyTest.java: FOUND (4 tests passing)
- Commits: 9feee0c, 9ff02d7, 07ddafb — all present in git log
- Build: `mvn -s /tmp/maven-settings.xml -o -q -DskipTests package` → exit 0 (MAVEN BUILD OK)
- No blocking stubs found

---
*Phase: 05-core-booking-approval-workflow*
*Completed: 2026-10-09*
