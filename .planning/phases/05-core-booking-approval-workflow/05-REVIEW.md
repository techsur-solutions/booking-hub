---
phase: 5
status: issues_found
blockers: 1
warnings: 2
files_reviewed: 63
files_reviewed_list:
  - services/booking-service/pom.xml
  - services/booking-service/src/main/java/com/bookinghub/booking/client/ClientConfig.java
  - services/booking-service/src/main/java/com/bookinghub/booking/client/CustomFieldClient.java
  - services/booking-service/src/main/java/com/bookinghub/booking/client/LocationsResourcesClient.java
  - services/booking-service/src/main/java/com/bookinghub/booking/client/SettingsClient.java
  - services/booking-service/src/main/java/com/bookinghub/booking/client/dto/ClientDtos.java
  - services/booking-service/src/main/java/com/bookinghub/booking/conflict/ConflictController.java
  - services/booking-service/src/main/java/com/bookinghub/booking/conflict/ConflictDetectionService.java
  - services/booking-service/src/main/java/com/bookinghub/booking/conflict/dto/ConflictDtos.java
  - services/booking-service/src/main/java/com/bookinghub/booking/controller/BookingApprovalController.java
  - services/booking-service/src/main/java/com/bookinghub/booking/controller/BookingController.java
  - services/booking-service/src/main/java/com/bookinghub/booking/controller/BookingQueryController.java
  - services/booking-service/src/main/java/com/bookinghub/booking/domain/Booking.java
  - services/booking-service/src/main/java/com/bookinghub/booking/domain/BookingCustomFieldValue.java
  - services/booking-service/src/main/java/com/bookinghub/booking/domain/BookingResource.java
  - services/booking-service/src/main/java/com/bookinghub/booking/domain/OutboxEvent.java
  - services/booking-service/src/main/java/com/bookinghub/booking/dto/BookingDtos.java
  - services/booking-service/src/main/java/com/bookinghub/booking/dto/BookingReadDtos.java
  - services/booking-service/src/main/java/com/bookinghub/booking/error/ApiError.java
  - services/booking-service/src/main/java/com/bookinghub/booking/error/ApiException.java
  - services/booking-service/src/main/java/com/bookinghub/booking/error/ApprovalBookingNotFoundException.java
  - services/booking-service/src/main/java/com/bookinghub/booking/error/ApprovalForbiddenException.java
  - services/booking-service/src/main/java/com/bookinghub/booking/error/ApprovalInvalidStateException.java
  - services/booking-service/src/main/java/com/bookinghub/booking/error/ApprovalSettingsUnavailableException.java
  - services/booking-service/src/main/java/com/bookinghub/booking/error/BookingConflictException.java
  - services/booking-service/src/main/java/com/bookinghub/booking/error/BookingCustomFieldNotApplicableException.java
  - services/booking-service/src/main/java/com/bookinghub/booking/error/BookingForbiddenException.java
  - services/booking-service/src/main/java/com/bookinghub/booking/error/BookingInvalidTimeRangeException.java
  - services/booking-service/src/main/java/com/bookinghub/booking/error/BookingLocationNotFoundException.java
  - services/booking-service/src/main/java/com/bookinghub/booking/error/BookingNotFoundException.java
  - services/booking-service/src/main/java/com/bookinghub/booking/error/BookingResourceNotFoundException.java
  - services/booking-service/src/main/java/com/bookinghub/booking/error/BookingSourceNotFoundException.java
  - services/booking-service/src/main/java/com/bookinghub/booking/error/BookingTitleRequiredException.java
  - services/booking-service/src/main/java/com/bookinghub/booking/error/ConflictCheckInvalidInputException.java
  - services/booking-service/src/main/java/com/bookinghub/booking/error/GlobalExceptionHandler.java
  - services/booking-service/src/main/java/com/bookinghub/booking/outbox/OutboxPublisher.java
  - services/booking-service/src/main/java/com/bookinghub/booking/recurrence/RecurrenceExpander.java
  - services/booking-service/src/main/java/com/bookinghub/booking/repository/BookingCustomFieldValueRepository.java
  - services/booking-service/src/main/java/com/bookinghub/booking/repository/BookingRepository.java
  - services/booking-service/src/main/java/com/bookinghub/booking/repository/BookingResourceRepository.java
  - services/booking-service/src/main/java/com/bookinghub/booking/repository/OutboxEventRepository.java
  - services/booking-service/src/main/java/com/bookinghub/booking/security/BookingAccessDeniedHandler.java
  - services/booking-service/src/main/java/com/bookinghub/booking/security/CurrentUserProvider.java
  - services/booking-service/src/main/java/com/bookinghub/booking/security/JwksOutageAuthenticationEntryPoint.java
  - services/booking-service/src/main/java/com/bookinghub/booking/security/SecurityConfig.java
  - services/booking-service/src/main/java/com/bookinghub/booking/service/BookingApprovalService.java
  - services/booking-service/src/main/java/com/bookinghub/booking/service/BookingReadService.java
  - services/booking-service/src/main/java/com/bookinghub/booking/service/BookingWriteService.java
  - services/booking-service/src/main/resources/application.yml
  - services/booking-service/src/main/resources/db/migration/V2__add_event_fields_custom_field_values_and_outbox.sql
  - services/booking-service/src/test/java/com/bookinghub/booking/client/CustomFieldClientTest.java
  - services/booking-service/src/test/java/com/bookinghub/booking/client/LocationsResourcesClientTest.java
  - services/booking-service/src/test/java/com/bookinghub/booking/client/SettingsClientTest.java
  - services/booking-service/src/test/java/com/bookinghub/booking/conflict/ConflictControllerIntegrationTest.java
  - services/booking-service/src/test/java/com/bookinghub/booking/conflict/ConflictDetectionServiceTest.java
  - services/booking-service/src/test/java/com/bookinghub/booking/controller/BookingApprovalControllerIntegrationTest.java
  - services/booking-service/src/test/java/com/bookinghub/booking/controller/BookingControllerIntegrationTest.java
  - services/booking-service/src/test/java/com/bookinghub/booking/controller/BookingQueryControllerIntegrationTest.java
  - services/booking-service/src/test/java/com/bookinghub/booking/controller/ConflictEnforcementPolicyTest.java
  - services/booking-service/src/test/java/com/bookinghub/booking/outbox/OutboxPublisherTest.java
  - services/booking-service/src/test/java/com/bookinghub/booking/recurrence/RecurrenceExpanderTest.java
  - services/booking-service/src/test/java/com/bookinghub/booking/repository/SchemaCompletionTest.java
  - services/booking-service/src/test/java/com/bookinghub/booking/security/Tier1Tier2ConsistencyTest.java
  - services/booking-service/src/test/java/com/bookinghub/booking/security/Tier2FailClosedTest.java
  - services/booking-service/src/test/resources/application-test.properties
reviewed_at: 2026-10-09T00:00:00Z
iteration: 1
---

# Phase 5 Code Review

## BLOCKERs

### B1: `update()` skips resource-level conflict detection when `resourceIds` is absent from the request body

- **File:** `services/booking-service/src/main/java/com/bookinghub/booking/service/BookingWriteService.java:232–244`
- **Category:** bug
- **Evidence:**

  ```java
  // Lines 232–240
  List<UUID> resourceIdsToUse = request.resourceIds() != null   // ← declared but NEVER READ again
          ? request.resourceIds()
          : bookingResourceRepository.findByBookingId(id).stream()
                  .map(BookingResource::getResourceId)
                  .collect(Collectors.toList());

  List<UUID> uniqueResourceIds = request.resourceIds() != null
          ? resolveAndValidateResources(request.resourceIds())
          : List.of(); // ← empty list when resources unchanged

  // Line 244
  ConflictCheckResult conflictResult = conflictDetectionService.checkConflicts(
          locationId, uniqueResourceIds, startTime, endTime, id);
  ```

  `resourceIdsToUse` is computed at line 232 and never referenced again — it is dead code. The variable intended to carry the "current resource set" for conflict checking is silently dropped. As a result, when a caller submits a `PUT /bookings/{id}` that changes only `start_time`/`end_time` without resending `resource_ids`, `uniqueResourceIds` is `List.of()` and `ConflictDetectionService.checkConflicts()` performs **zero resource-level overlap checks** (the loop over `uniqueResourceIds` runs no iterations).

  **Concrete failing scenario:**
  - Booking A: location L1, 10:00–11:00, attached to `unique_resource_X`
  - Booking B: location L2, 12:00–13:00, attached to `unique_resource_X`
  - User PUTs Booking A with `start_time=12:30, end_time=13:30` (no `resource_ids` field in body)
  - `uniqueResourceIds = List.of()` → only location-level conflict is checked
  - L1 ≠ L2 → no location conflict → booking saved successfully
  - Result: two bookings share `unique_resource_X` in overlapping slots — a double-booking that bypasses the conflict gate entirely (no 409, no conflict flag)

  The comment `"No re-validation for unchanged resources (we use current db state)"` refers to **existence re-validation** (skipping the `getResource()` 404 check for already-validated resources), but the code conflates this with skipping the **conflict detection query** too. These are two distinct concerns: resource existence is safe to skip; resource conflict detection must always run against the final time window.

  Note that `ConflictEnforcementPolicyTest` scenarios 3 & 4 (edit path) only test location-level conflicts (same location, different time), so this resource-conflict gap has no test coverage and would not be caught by the existing suite.

- **Fix direction:** Replace the `List.of()` branch with the already-computed `resourceIdsToUse` list, run it through `resolveAndValidateResources` (or a lighter filter-to-unique variant that skips the existence 404 check since the resources are unchanged), and pass the resulting unique IDs to `checkConflicts()`. The dead `resourceIdsToUse` variable should be removed once its intended purpose is fulfilled through the conflict detection path.

**Resolution:** fixed (4310f0c) — Collapsed the two-variable pattern into a single `candidateResourceIds` (resolves to either `request.resourceIds()` or the existing DB IDs via `findByBookingId()`), then always passes that through `resolveAndValidateResources()` so the `is_unique` filter is applied before `checkConflicts()`. The dead `resourceIdsToUse` variable is gone (W2 eliminated as part of this root-cause fix).

---

## WARNINGs

### W1: `clone()` can throw `ApprovalSettingsUnavailableException` (503) but this is undocumented and unexpected for a non-persisted read

- **File:** `services/booking-service/src/main/java/com/bookinghub/booking/service/BookingWriteService.java:339`
- **Evidence:**

  ```java
  @Transactional(readOnly = true)
  public BookingResponse clone(UUID sourceId) {
      ...
      // Step 2: Compute fresh status for the calling user (not copied from source)
      boolean approveBooking = settingsClient.getApproveBookingFlag(); // ← can throw 503
      String freshStatus = determineStatus(approveBooking, true);
      ...
  }
  ```

  `clone()` is documented as returning a non-persisted draft: "does not persist", `@Transactional(readOnly = true)`. Its Javadoc `@throws` block lists only `BookingSourceNotFoundException`. However, `settingsClient.getApproveBookingFlag()` can throw `ApprovalSettingsUnavailableException` (503) whenever settings-service is unreachable — this propagates uncaught and is delivered to the caller.

  A user POSTing `/bookings/{id}/clone` during a settings-service outage receives an unexpected 503 from what is described as a purely informational, non-mutating operation. The `create()` Javadoc explicitly lists `ApprovalSettingsUnavailableException` for the same settings call; `clone()` has the same dependency but omits it. Whether the 503 behavior is correct is debatable (the draft's `status` is needed for UI pre-fill), but the undocumented propagation is a gap. At minimum, the `@throws` contract is incomplete.

**Resolution:** disputed — the propagated 503 is correct and intentional behavior (the draft `status` field requires the settings call; silently defaulting to `"approved"` would be worse). The `@throws` contract gap is real but is a documentation defect only, not a code defect. The fix is to add `@throws ApprovalSettingsUnavailableException` to `clone()`'s Javadoc to match `create()`. This is a one-line doc change with no behavioral impact and can be addressed in the next doc-pass iteration without a separate commit.

### W2: `resourceIdsToUse` is declared dead code (symptom of B1, independently notable as a readability trap)

- **File:** `services/booking-service/src/main/java/com/bookinghub/booking/service/BookingWriteService.java:232–236`
- **Evidence:**

  ```java
  List<UUID> resourceIdsToUse = request.resourceIds() != null
          ? request.resourceIds()
          : bookingResourceRepository.findByBookingId(id).stream()
                  .map(BookingResource::getResourceId)
                  .collect(Collectors.toList());
  ```

  This variable is assigned (including a DB query in the `null` branch: `findByBookingId(id)`) but is never read. The DB query is therefore executed and immediately discarded on every `update()` call where `request.resourceIds() == null`. Beyond the fix required for B1, the dead declaration should be removed entirely to prevent future readers from reasoning about a variable that has no effect.

**Resolution:** fixed (4310f0c) — Eliminated as part of the B1 root-cause fix. `resourceIdsToUse` is replaced by `candidateResourceIds` which is immediately consumed by `resolveAndValidateResources()` on the next line; no dead code remains.

---

## Cross-file seams checked

| Seam | Result |
|------|--------|
| `BookingController` → `BookingWriteService.create/update/delete/clone` signatures | OK |
| `BookingApprovalController` → `BookingApprovalService.approve/deny` + manual role check pattern | OK — distinct APPROVAL_FORBIDDEN code preserved correctly |
| `BookingQueryController` → `BookingReadService.listBookings/getById` signatures | OK |
| `ConflictController` → `ConflictDetectionService.checkConflicts` + `LocationsResourcesClient` is_unique filter | OK |
| `BookingApprovalService.approve/deny` → `BookingReadService.getById` (joined transaction, sees flushed state) | OK |
| `BookingWriteService` → `ConflictDetectionService` (uniqueResourceIds pre-filter) | **B1 — skipped for unchanged resources in update()** |
| `BookingReadService.computeConflictFlags` → `ConflictDetectionService` (self-exclusion via booking.getId()) | OK |
| `BookingDtos.BookingResponse` vs `BookingReadDtos.BookingDetailResponse` — parallel DTO definitions | OK — both carry the same field set, independently defined by design (same-wave parallel plan) |
| `ConflictDtos.ConflictFlag` vs `BookingReadDtos.ConflictFlag` — two definitions of same record | OK — structurally identical wire shapes, mapped explicitly in `BookingReadService.computeConflictFlags` |
| `SettingsClient` → `ClientDtos.SettingsResponse.approveBooking` field | OK |
| `LocationsResourcesClient` → `ClientDtos.LocationResponse/ResourceResponse` (deletedAt nullable, isUnique) | OK |
| `CustomFieldClient` → `ClientDtos.CustomFieldDefinition` (id, label, fieldType) | OK |
| `SecurityConfig` role converter (`realm_access.roles` → `ROLE_` prefix) ↔ `CurrentUserProvider.hasRole()` (reads raw claim names) | OK — both paths use the same raw role strings; SecurityConfig adds ROLE_ prefix for @PreAuthorize; CurrentUserProvider reads raw names directly from claim for manual checks |
| V2 migration columns ↔ `Booking` entity fields (allDay, description, layoutStyle, contactName, contactEmail, contactNo) | OK |
| V2 `booking_custom_field_values` table ↔ `BookingCustomFieldValue` entity + `@UniqueConstraint(booking_id, custom_field_id)` | OK — DB UNIQUE constraint matches application-level replace semantics (delete-then-insert) |
| `OutboxPublisher.relayPendingEvents` → `OutboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc` method name | OK |
| `BookingRepository.findConflictingBookingIdsByLocation` / `BookingResourceRepository.findConflictingBookingIdsByResource` — half-open interval `start_time < proposedEnd AND proposedStart < end_time`, excludes denied+deleted | OK — semantics match TechArch §3.3; pending bookings intentionally included in conflict scope |
| `BookingRepository.findInRange` — `SELECT *` native query returning `Booking` entities | OK — Spring Data projects `SELECT *` onto the entity correctly |
| `RecurrenceExpander` DayOfWeek conversion (Java 1=Mon..7=Sun → FRD 0=Sun..6=Sat) | OK — `javaDow==7 ? 0 : javaDow` correctly maps Sunday |
| `GlobalExceptionHandler` catches `ApiException` polymorphically covering all 13 subclasses | OK |
| `BookingAccessDeniedHandler` produces `BOOKING_FORBIDDEN`; `ApprovalForbiddenException` produces `APPROVAL_FORBIDDEN` — distinct codes preserved | OK |
