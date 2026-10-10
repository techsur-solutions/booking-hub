package com.bookinghub.booking.dto;

import com.bookinghub.booking.conflict.dto.ConflictDtos.ConflictFlag;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTOs for the booking CRUD write operations (plan 05-03).
 *
 * Named decision (no scope/EditScope field): plan 05-03's central decision, closing F0
 * open-questions.md #1. Legacy's update()/delete() always target a single row by its own id
 * unconditionally — no series-scoped edit concept exists past creation time. BookingUpdateRequest
 * does not expose any scope field, reflecting that directly.
 *
 * Named decision (emailContact transient): emailContact on BookingCreateRequest is a VIRTUAL
 * flag — never persisted as a column (confirmed by plan 05-01's migration containing no
 * email_contact column), carried only into the booking.created outbox event payload for a
 * future notifications-service. It does NOT appear on BookingUpdateRequest because only
 * the initial create event may carry it.
 *
 * Named decision (RecurrenceDefinition): Uses FRD's own already-adopted interim shape
 * (pattern: "weekly", days_of_week: int[] where 0=Sunday..6=Saturday, end_date: LocalDate).
 * TechArch §4.1's type comment explicitly flags this as an "interim assumption pending F0
 * confirmation". F0 did not further resolve the exact grammar beyond what legacy's own
 * weekly/monthly-N-times loop showed — this plan uses the FRD's already-adopted shape as-is.
 *
 * Named decision (BookingResponse id nullable): id is nullable specifically to represent
 * clone's non-persisted draft response (Task 2 sets it null for that one case only). All
 * real persisted bookings have a non-null id.
 *
 * Wire shape: TechArch §4.2's exact snake_case convention for all fields. The 6 F0-confirmed
 * schema-completion fields (allDay, layoutStyle, contactName, contactEmail, contactNo,
 * emailContact) follow the same snake_case convention.
 */
public class BookingDtos {

    // ── Input: custom field value ────────────────────────────────────────────

    public record CustomFieldValueInput(
            @JsonProperty("field_id") UUID fieldId,
            @JsonProperty("value") String value
    ) {}

    // ── Output: custom field value (enriched when CustomFieldClient succeeds) ─

    /**
     * label/fieldType are enriched via CustomFieldClient when available;
     * null when the client's graceful-degradation path (plan 05-02) returned no definitions.
     */
    public record CustomFieldValueOutput(
            @JsonProperty("field_id") UUID fieldId,
            @JsonProperty("value") String value,
            @JsonProperty("label") String label,
            @JsonProperty("field_type") String fieldType
    ) {}

    // ── Recurrence definition ────────────────────────────────────────────────

    /**
     * FRD's own already-adopted interim recurrence shape:
     * - pattern: "weekly" (only supported pattern; field reserved for future extension)
     * - daysOfWeek: 0=Sunday, 1=Monday, ..., 6=Saturday
     * - endDate: last calendar date to generate occurrences up to and including
     */
    public record RecurrenceDefinition(
            @JsonProperty("pattern") String pattern,
            @JsonProperty("days_of_week") List<Integer> daysOfWeek,
            @JsonProperty("end_date") LocalDate endDate
    ) {}

    // ── Create request ───────────────────────────────────────────────────────

    /**
     * Request body for POST /bookings.
     *
     * endTime: if null, defaults to startTime + 1 hour in BookingWriteService (F0-confirmed legacy default).
     * resourceIds: null/empty means no resources attached to this booking.
     * customFieldValues: null/empty means no custom field values.
     * recurrence: null means standalone (single) booking; non-null triggers recurring bulk-create (F1.3).
     * emailContact: TRANSIENT ONLY — never persisted; carried through to booking.created event payload only.
     */
    public record BookingCreateRequest(
            @JsonProperty("title") String title,
            @JsonProperty("location_id") UUID locationId,
            @JsonProperty("start_time") Instant startTime,
            @JsonProperty("end_time") Instant endTime,
            @JsonProperty("resource_ids") List<UUID> resourceIds,
            @JsonProperty("custom_field_values") List<CustomFieldValueInput> customFieldValues,
            @JsonProperty("recurrence") RecurrenceDefinition recurrence,
            @JsonProperty("all_day") Boolean allDay,
            @JsonProperty("description") String description,
            @JsonProperty("layout_style") String layoutStyle,
            @JsonProperty("contact_name") String contactName,
            @JsonProperty("contact_email") String contactEmail,
            @JsonProperty("contact_no") String contactNo,
            @JsonProperty("email_contact") Boolean emailContact
    ) {}

    // ── Update request ───────────────────────────────────────────────────────

    /**
     * Request body for PUT /bookings/{id}.
     *
     * Named decision, closing F0 open-questions.md #1: NO scope/EditScope field anywhere in
     * this request. Legacy has no series-scoped edit concept past creation time — update()/delete()
     * always target the single row by its own id unconditionally.
     *
     * Partial update semantics: only fields present in the request body are applied;
     * null fields are ignored (not overwritten). Status is never touched by update per F0.
     */
    public record BookingUpdateRequest(
            @JsonProperty("title") String title,
            @JsonProperty("location_id") UUID locationId,
            @JsonProperty("start_time") Instant startTime,
            @JsonProperty("end_time") Instant endTime,
            @JsonProperty("resource_ids") List<UUID> resourceIds,
            @JsonProperty("custom_field_values") List<CustomFieldValueInput> customFieldValues,
            @JsonProperty("all_day") Boolean allDay,
            @JsonProperty("description") String description,
            @JsonProperty("layout_style") String layoutStyle,
            @JsonProperty("contact_name") String contactName,
            @JsonProperty("contact_email") String contactEmail,
            @JsonProperty("contact_no") String contactNo
    ) {}

    // ── Response ─────────────────────────────────────────────────────────────

    /**
     * Response body for POST /bookings, PUT /bookings/{id}, POST /bookings/{id}/clone.
     *
     * id: nullable for clone's non-persisted draft response (clone sets id=null; all real
     *     persisted bookings have a non-null id).
     * conflictFlags: populated with conflict details when a soft-warning applies (caller holds
     *                role_booking_approver and booking was saved despite conflict); empty otherwise.
     */
    public record BookingResponse(
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
}
