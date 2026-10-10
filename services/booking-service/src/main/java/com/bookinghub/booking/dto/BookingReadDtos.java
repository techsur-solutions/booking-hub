package com.bookinghub.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTOs for the booking READ operations — calendar/day/list views and the AJAX detail
 * endpoint (plan 05-04).
 *
 * Named decision (self-contained, not a cross-plan import):
 * These DTOs are intentionally independent of plan 05-03's BookingDtos. Both plans run
 * in the SAME wave (3) with no dependency on each other. The ConflictFlag record here is
 * structurally equivalent to ConflictDtos.ConflictFlag (both carry the same 4 fields)
 * but is independently defined, not re-exported — this plan's parallel-safety note in
 * the plan context explains the design.
 *
 * Wire shape: TechArch §4.2's exact snake_case convention (same @JsonProperty mapping
 * as plan 05-03's BookingDtos) for all fields.
 */
public class BookingReadDtos {

    /**
     * A single conflict flag in a read response — independently defined to match
     * ConflictDtos.ConflictFlag's wire shape without creating a cross-package import
     * from this plan's read DTOs into the conflict package.
     */
    public record ConflictFlag(
            @JsonProperty("conflicting_booking_id") UUID conflictingBookingId,
            @JsonProperty("conflict_type") String conflictType,
            @JsonProperty("location_id") UUID locationId,
            @JsonProperty("resource_id") UUID resourceId
    ) {}

    /**
     * Enriched custom field value for the read response — includes label and fieldType
     * when CustomFieldClient returned data; null when the client's graceful-degradation
     * path returned nothing, per plan 05-02's explicit handling.
     */
    public record CustomFieldValueOutput(
            @JsonProperty("field_id") UUID fieldId,
            @JsonProperty("value") String value,
            @JsonProperty("label") String label,
            @JsonProperty("field_type") String fieldType
    ) {}

    /**
     * Full booking detail response — used by both list reads (GET /bookings) and the
     * AJAX-style single detail read (GET /bookings/{id}).
     *
     * Named decision (deletedAt included, last-known-values policy):
     * getById uses plain findById (not findByIdAndDeletedAtIsNull) — a soft-deleted booking
     * returns 200 with deletedAt populated and every other field showing last-known values;
     * 404 only for a genuinely nonexistent id. This mirrors Phase 4's exact precedent for
     * Locations/Resources (04-02-PLAN.md's getById), establishing cross-system consistency.
     *
     * conflictFlags: populated by calling ConflictDetectionService in pure read-only mode
     * (NEVER blocks or rejects — only surfaces already-saved or newly-emerged conflicts for
     * the UI to display). Empty when the booking is soft-deleted (a deleted booking cannot
     * meaningfully conflict with anything live).
     */
    public record BookingDetailResponse(
            @JsonProperty("id") UUID id,
            @JsonProperty("series_id") UUID seriesId,
            @JsonProperty("title") String title,
            @JsonProperty("location_id") UUID locationId,
            @JsonProperty("start_time") Instant startTime,
            @JsonProperty("end_time") Instant endTime,
            @JsonProperty("resource_ids") List<UUID> resourceIds,
            @JsonProperty("custom_field_values") List<CustomFieldValueOutput> customFieldValues,
            @JsonProperty("status") String status,
            @JsonProperty("owner_id") UUID ownerId,
            @JsonProperty("approved_by") UUID approvedBy,
            @JsonProperty("approved_at") Instant approvedAt,
            @JsonProperty("denied_by") UUID deniedBy,
            @JsonProperty("denied_at") Instant deniedAt,
            @JsonProperty("denial_reason") String denialReason,
            @JsonProperty("all_day") boolean allDay,
            @JsonProperty("description") String description,
            @JsonProperty("layout_style") String layoutStyle,
            @JsonProperty("contact_name") String contactName,
            @JsonProperty("contact_email") String contactEmail,
            @JsonProperty("contact_no") String contactNo,
            @JsonProperty("conflict_flags") List<ConflictFlag> conflictFlags,
            @JsonProperty("created_at") Instant createdAt,
            @JsonProperty("updated_at") Instant updatedAt,
            @JsonProperty("deleted_at") Instant deletedAt
    ) {}

    /**
     * Request body for POST /bookings/{id}/deny.
     *
     * denialReason is optional (free text, nullable) — F0 confirmed legacy requires no
     * particular format for denial reasons.
     */
    public record DenyBookingRequest(
            @JsonProperty("denial_reason") String denialReason
    ) {}
}
