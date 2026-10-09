package com.bookinghub.booking.conflict.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTOs for the conflict detection result model (plan 05-02).
 *
 * ConflictFlag: a single detected conflict, indicating which booking it
 * conflicts with, the type of conflict (location or resource), and the
 * relevant entity id.
 *
 * ConflictCheckResult: the full conflict evaluation result returned by
 * ConflictDetectionService.checkConflicts() — the UNION of location-level
 * and resource-level conflicts per TechArch §3.3.
 *
 * Also contains ConflictCheckRequest: the inbound DTO for
 * POST /bookings/check-conflicts. All fields are optional by themselves
 * but at least location_id or non-empty resource_ids must be present
 * (validated in ConflictController).
 */
public class ConflictDtos {

    /**
     * A single conflict flag identifying a specific booking that conflicts.
     *
     * conflictType: "location" (location-level overlap) or "resource" (resource-level overlap)
     * locationId:  present and equal to the checked location when conflictType="location", null for resource
     * resourceId:  present and equal to the specific unique resource when conflictType="resource", null for location
     */
    public record ConflictFlag(
            UUID conflictingBookingId,
            String conflictType,
            UUID locationId,
            UUID resourceId) {
    }

    /**
     * Full result of a conflict check. Returned by ConflictDetectionService and by
     * POST /bookings/check-conflicts (always 200, never 409 — this endpoint is purely informational).
     *
     * hasConflict: true if any location-level or resource-level conflict was detected.
     * conflicts: the union of all detected conflicts; empty list when hasConflict=false.
     */
    public record ConflictCheckResult(
            boolean hasConflict,
            java.util.List<ConflictFlag> conflicts) {
    }

    /**
     * Inbound request body for POST /bookings/check-conflicts.
     *
     * locationId: optional — if present, a location-level overlap check is run
     * resourceIds: optional — if present and non-empty, per-resource overlap checks are run
     *              (caller is responsible for passing only is_unique=true resource ids per
     *              Task 1's named decision; the ConflictController does this filtering)
     * startTime: required — proposed booking start (Instant, ISO-8601 in JSON)
     * endTime: required — proposed booking end (Instant, ISO-8601 in JSON)
     * excludeBookingId: optional — when editing, the booking's own id to self-exclude
     *
     * Validation: at least one of locationId or non-empty resourceIds must be present;
     * ConflictController throws ConflictCheckInvalidInputException (400) if neither is.
     */
    public record ConflictCheckRequest(
            UUID locationId,
            List<UUID> resourceIds,
            java.time.Instant startTime,
            java.time.Instant endTime,
            UUID excludeBookingId) {
    }
}
