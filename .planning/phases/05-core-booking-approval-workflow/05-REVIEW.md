---
phase: 5
status: issues_found
blockers: 0
warnings: 2
files_reviewed: 1
files_reviewed_list:
  - services/booking-service/src/main/java/com/bookinghub/booking/service/BookingWriteService.java
reviewed_at: 2026-10-09T20:00:00Z
iteration: 2
---

# Phase 5 Code Review

## BLOCKERs

_None._

---

## WARNINGs

### W1: `clone()` Javadoc still missing `@throws ApprovalSettingsUnavailableException` (carried from iteration 1, resolution unchanged)

- **File:** `services/booking-service/src/main/java/com/bookinghub/booking/service/BookingWriteService.java:322`
- **Evidence:**

  ```java
  /**
   * ...
   * @throws BookingSourceNotFoundException if source booking does not exist or is soft-deleted (404)
   */
  @Transactional(readOnly = true)
  public BookingResponse clone(UUID sourceId) {
      ...
      boolean approveBooking = settingsClient.getApproveBookingFlag(); // ← can throw 503
      ...
  }
  ```

  The iteration-1 resolution (disputed/doc-only) stands: the 503 propagation is correct behavior;
  the `@throws` contract gap is a documentation defect only. No behavioral regression introduced.
  One-line Javadoc addition to match `create()` remains outstanding.

---

### W2: Fix-introduced regression — `resolveAndValidateResources()` now throws `BookingResourceNotFoundException` (404) on a time-only `PUT` when an attached resource has been soft-deleted since booking creation

- **File:** `services/booking-service/src/main/java/com/bookinghub/booking/service/BookingWriteService.java:236–242`
- **Evidence:**

  The B1 fix correctly collapsed the two-variable pattern into a single `candidateResourceIds`
  and always routes it through `resolveAndValidateResources()`. However, `resolveAndValidateResources()`
  treats `deletedAt != null` as a 404 (lines 470–472):

  ```java
  // resolveAndValidateResources — line 470–472
  if (resource.deletedAt() != null) {
      throw new BookingResourceNotFoundException(resourceId.toString());
  }
  ```

  When the request sends **no `resource_ids`** (a time-only `PUT`), `candidateResourceIds` is
  loaded from `bookingResourceRepository.findByBookingId(id)` — the IDs that were attached at
  booking creation and already passed validation at that time. If any of those resources has
  since been soft-deleted in locations-resources-service, `resolveAndValidateResources()` will
  now throw a `BookingResourceNotFoundException` (mapped to 404) **on an operation that does not
  touch resources at all**.

  **Concrete failing scenario:**
  1. Resource R exists and `is_unique=true`; Booking A is created with R attached — valid.
  2. An admin soft-deletes Resource R in locations-resources-service.
  3. User submits `PUT /bookings/A` with only `start_time` changed (no `resource_ids` field).
  4. `candidateResourceIds = [R]` (from DB) → `resolveAndValidateResources([R])` → `getResource(R)`
     returns 200 with `deletedAt != null` → throws `BookingResourceNotFoundException`.
  5. User receives a confusing 404 for what looks like a simple time-shift edit. The only workaround
     is to explicitly resend `resource_ids: []` to drop the now-deleted resource.

  **Pre-fix behavior** on the unchanged-resource path was `List.of()` (no 404 risk, but also no
  conflict check — which was B1). The iteration-1 fix direction explicitly flagged this trade-off:
  *"or a lighter filter-to-unique variant that skips the existence 404 check since the resources
  are unchanged"*. The fixer chose full re-validation instead.

  **Assessment:** This is a real defect on a legitimate user path. The correct fix is to skip the
  `deletedAt` existence check (lines 470–472) when operating on unchanged DB resource IDs, and
  still apply the `is_unique` filter for the conflict query. The conflict-detection path only needs
  `isUnique()` — it does not need the resource to be non-deleted to check scheduling overlap against
  it; what matters is whether unique resources are double-booked.

  Classified as **WARNING** (not BLOCKER) because: (a) the soft-delete-after-booking-creation window
  is a non-common race condition rather than a normal code path; (b) the booking itself is not
  corrupted — only a subset of time-only edits fail with an unintuitive error; (c) users can
  work around by explicitly resending `resource_ids`. Promoted to BLOCKER if a resource lifecycle
  policy guarantees soft-deletion without migrating attached bookings.

---

## Cross-file seams checked (iteration 2)

| Seam | Result |
|------|--------|
| B1 fix: `candidateResourceIds` resolves to `request.resourceIds()` OR existing DB IDs via `findByBookingId()` | Fixed — correct in both branches |
| B1 fix: `candidateResourceIds` always routed through `resolveAndValidateResources()` → `is_unique` filter applied | Fixed — `uniqueResourceIds` is never `List.of()` due to skipped validation |
| B1 fix: `uniqueResourceIds` passed to `conflictDetectionService.checkConflicts(locationId, uniqueResourceIds, startTime, endTime, id)` | Fixed — resource-level conflict loop runs for unchanged resources |
| W2 (iteration 1) dead variable: `resourceIdsToUse` removed | Fixed — variable gone, no dead code |
| W1 (iteration 1) `clone()` missing `@throws ApprovalSettingsUnavailableException` | Unchanged — still absent; doc defect only, no code path impact |
| Fix regression: `resolveAndValidateResources()` called on unchanged DB IDs, throws 404 if any are soft-deleted | W2 (this iteration) — see above |
| All other previously-checked seams (controllers, approval service, read service, conflict engine, outbox) | Not touched by fixer — OK per iteration 1 |
