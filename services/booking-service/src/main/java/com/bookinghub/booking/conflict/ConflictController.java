package com.bookinghub.booking.conflict;

import com.bookinghub.booking.client.LocationsResourcesClient;
import com.bookinghub.booking.client.dto.ClientDtos.ResourceResponse;
import com.bookinghub.booking.conflict.dto.ConflictDtos.ConflictCheckRequest;
import com.bookinghub.booking.conflict.dto.ConflictDtos.ConflictCheckResult;
import com.bookinghub.booking.error.ConflictCheckInvalidInputException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Standalone POST /bookings/check-conflicts endpoint (plan 05-02).
 *
 * Named decision (purely informational — NEVER enforces a block):
 * This endpoint always returns 200 with the ConflictCheckResult, regardless of whether
 * conflicts are detected. Per TechArch §4.2's API table (200 ConflictCheckResult, no 409
 * listed for this endpoint) and the FRD's own Error States table (only the 400
 * CONFLICT_CHECK_INVALID_INPUT case listed). The 409 BOOKING_CONFLICT enforcement case
 * belongs exclusively to POST/PUT /bookings, built in plan 05-03 — never duplicated here.
 *
 * Named decision (is_unique-aware resource filtering at controller level):
 * Per Task 1's named decision, ConflictDetectionService.checkConflicts() receives a
 * pre-filtered uniqueResourceIds list (only is_unique=true resources). This controller
 * performs that filtering by calling LocationsResourcesClient.getResource() for each
 * submitted resource_id and filtering down to those where isUnique()==true.
 * Resource ids that return 404 (genuinely nonexistent) are treated as not found and
 * excluded from conflict checking (the caller gets no resource conflict for a nonexistent
 * resource — the create/edit path in plan 05-03 will reject nonexistent resources, but
 * this informational-only endpoint is more lenient).
 *
 * Role gating: @PreAuthorize("hasRole('role_booking_creator')") per TechArch §4.2's
 * allowRoomBooking permission column mapped to role_booking_creator. This is a subset
 * of the Gateway's Tier-1 3-role coarse gate (Tier1Tier2ConsistencyTest).
 */
@RestController
public class ConflictController {

    private final ConflictDetectionService conflictDetectionService;
    private final LocationsResourcesClient locationsResourcesClient;

    public ConflictController(
            ConflictDetectionService conflictDetectionService,
            LocationsResourcesClient locationsResourcesClient) {
        this.conflictDetectionService = conflictDetectionService;
        this.locationsResourcesClient = locationsResourcesClient;
    }

    /**
     * POST /bookings/check-conflicts — purely informational conflict check.
     *
     * Never returns 409. Returns 200 with ConflictCheckResult regardless of detected conflicts.
     * The 400 CONFLICT_CHECK_INVALID_INPUT case is the only error this endpoint produces.
     *
     * @param request The conflict check request (location_id, resource_ids, start_time, end_time, exclude_booking_id)
     * @return 200 ConflictCheckResult — always, never 409
     */
    @PostMapping("/bookings/check-conflicts")
    @PreAuthorize("hasRole('role_booking_creator')")
    public ResponseEntity<ConflictCheckResult> checkConflicts(@RequestBody ConflictCheckRequest request) {

        // Step 1: Validate — at least one of location_id or non-empty resource_ids must be present
        boolean hasLocation = request.locationId() != null;
        boolean hasResources = request.resourceIds() != null && !request.resourceIds().isEmpty();

        if (!hasLocation && !hasResources) {
            throw new ConflictCheckInvalidInputException(
                    "At least one of location_id or resource_ids must be provided");
        }

        // Step 2: If resource_ids present, resolve each via LocationsResourcesClient,
        // filter down to only is_unique=true resources (Task 1's named decision).
        List<UUID> uniqueResourceIds = new ArrayList<>();
        if (hasResources) {
            for (UUID resourceId : request.resourceIds()) {
                locationsResourcesClient.getResource(resourceId)
                        .filter(ResourceResponse::isUnique)
                        .ifPresent(r -> uniqueResourceIds.add(r.id()));
            }
        }

        // Step 3: Run conflict check (pure local-database call, zero outbound HTTP inside)
        ConflictCheckResult result = conflictDetectionService.checkConflicts(
                request.locationId(),
                uniqueResourceIds,
                request.startTime(),
                request.endTime(),
                request.excludeBookingId());

        // Step 4: Return 200 always — never 409 (informational endpoint only)
        return ResponseEntity.ok(result);
    }
}
