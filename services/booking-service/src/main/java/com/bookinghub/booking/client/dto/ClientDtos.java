package com.bookinghub.booking.client.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTOs for deserializing responses from downstream services (plan 05-02).
 *
 * Named decision (deletedAt on LocationResponse/ResourceResponse): Per Phase 4 plan
 * 04-02's named decision, getById() on Location/Resource returns 200 with last-known
 * values even for soft-deleted records (deletedAt populated), and 404 only for a
 * genuinely nonexistent id. LocationsResourcesClient maps 404 → Optional.empty().
 *
 * Named decision (isUnique/restrictLocations on ResourceResponse): These are the fields
 * ConflictController and plan 05-03's BookingWriteService use to determine which resource
 * ids to pass into ConflictDetectionService.checkConflicts() as the uniqueResourceIds
 * pre-filtered list (Task 1's named decision: is_unique filtering is the caller's job).
 */
public class ClientDtos {

    /**
     * Response from GET /locations/{id} on locations-resources-service.
     * deletedAt non-null means soft-deleted but last-known-values still returned (200 not 404).
     */
    public record LocationResponse(
            UUID id,
            String name,
            Instant deletedAt) {
    }

    /**
     * Response from GET /resources/{id} on locations-resources-service.
     * isUnique: true → resource-level conflicts are checked for this resource (passed in
     *   uniqueResourceIds by the caller, per Task 1's named decision).
     * restrictLocations: list of location ids this resource is restricted to (null/empty = unrestricted).
     * deletedAt non-null means soft-deleted (200 with last-known values, not 404).
     */
    public record ResourceResponse(
            UUID id,
            String name,
            boolean isUnique,
            List<UUID> restrictLocations,
            Instant deletedAt) {
    }

    /**
     * A single custom field definition from GET /custom-fields?context_id={contextId}
     * on custom-field-service.
     */
    public record CustomFieldDefinition(
            UUID id,
            String label,
            String fieldType) {
    }

    /**
     * Response from GET /settings on settings-service.
     * approveBooking: the F3 auto-approve flag — SettingsClient.getApproveBookingFlag() extracts this.
     * calendarSlotSize, calendarMinTime, calendarMaxTime: UI/calendar fields (not used here).
     */
    public record SettingsResponse(
            boolean approveBooking,
            int calendarSlotSize,
            String calendarMinTime,
            String calendarMaxTime) {
    }
}
