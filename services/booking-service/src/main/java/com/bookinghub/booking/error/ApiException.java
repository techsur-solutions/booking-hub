package com.bookinghub.booking.error;

import org.springframework.http.HttpStatus;

/**
 * Base class for all API exceptions in this service.
 * Each subclass hard-codes its FRD-specified HTTP status and error_code.
 *
 * GlobalExceptionHandler catches ApiException polymorphically and constructs
 * ApiError responses with the exception's httpStatus/errorCode/message.
 *
 * 13 concrete subclasses in total:
 *   - 11 from the FRD's own F1/F2/F3 error-code tables verbatim
 *   - 3 named additions (BOOKING_NOT_FOUND, BOOKING_FORBIDDEN,
 *     BOOKING_CUSTOM_FIELD_NOT_APPLICABLE) required by this phase's
 *     explicitly-chosen behavior but absent from the FRD's own catalogue.
 *
 * Named decisions (3 additions beyond FRD catalogue):
 *
 * BookingNotFoundException (404, BOOKING_NOT_FOUND) — FRD's F1 error table
 *   never enumerates a plain "booking id not found" case for GET/PUT/DELETE
 *   /bookings/{id} (it only names BOOKING_SOURCE_NOT_FOUND for clone and
 *   APPROVAL_BOOKING_NOT_FOUND for approve/deny, leaving ordinary CRUD
 *   lookups uncovered). Added here to fill that gap, kept distinct so each
 *   entry point's error stays unambiguous about which action failed.
 *
 * BookingForbiddenException (403, BOOKING_FORBIDDEN) — required by TechArch
 *   §4.2's own API table which specifies PUT/DELETE /bookings/{id} as
 *   "(owner) or admin" — an ownership restriction with no corresponding FRD
 *   error code. Used both by BookingAccessDeniedHandler (Task 3) and manually
 *   thrown by plan 05-03's BookingWriteService for the owner-but-not-approver
 *   case on another user's booking.
 *
 * BookingCustomFieldNotApplicableException (400, BOOKING_CUSTOM_FIELD_NOT_APPLICABLE)
 *   — plan 05-02's CustomFieldClient validates custom_field_values[] field-id
 *   applicability for real (per the 04-04 carve-out fix). Plan 05-03's
 *   create()/update() need a real rejection code for the case where a submitted
 *   field_id does not appear in the context's applicable-fields list. No
 *   corresponding FRD code exists (FRD's F5 error table is custom-field-service's
 *   own error contract, not booking-service's) — this is booking-service's own
 *   named addition for its consuming side of that validation.
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    protected ApiException(HttpStatus httpStatus, String errorCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

// ── FRD F1 error codes (booking creation/edit) ──────────────────────────────

/**
 * 400 BOOKING_TITLE_REQUIRED — FRD F1 error table, booking title is empty/null.
 */
class BookingTitleRequiredException extends ApiException {
    public BookingTitleRequiredException() {
        super(HttpStatus.BAD_REQUEST, "BOOKING_TITLE_REQUIRED", "Booking title is required");
    }
}

/**
 * 400 BOOKING_INVALID_TIME_RANGE — FRD F1 error table, end_time <= start_time.
 */
class BookingInvalidTimeRangeException extends ApiException {
    public BookingInvalidTimeRangeException() {
        super(HttpStatus.BAD_REQUEST, "BOOKING_INVALID_TIME_RANGE",
            "Booking end time must be after start time");
    }
}

/**
 * 404 BOOKING_LOCATION_NOT_FOUND — FRD F1 error table, location_id not found
 * in locations-resources-service.
 */
class BookingLocationNotFoundException extends ApiException {
    public BookingLocationNotFoundException(String locationId) {
        super(HttpStatus.NOT_FOUND, "BOOKING_LOCATION_NOT_FOUND",
            "Location not found: " + locationId);
    }
}

/**
 * 404 BOOKING_RESOURCE_NOT_FOUND — FRD F1 error table, a resource_id in
 * the request not found in locations-resources-service.
 */
class BookingResourceNotFoundException extends ApiException {
    public BookingResourceNotFoundException(String resourceId) {
        super(HttpStatus.NOT_FOUND, "BOOKING_RESOURCE_NOT_FOUND",
            "Resource not found: " + resourceId);
    }
}

/**
 * 404 BOOKING_SOURCE_NOT_FOUND — FRD F1 error table, clone source booking
 * id not found (for POST /bookings/{id}/clone).
 */
class BookingSourceNotFoundException extends ApiException {
    public BookingSourceNotFoundException(String bookingId) {
        super(HttpStatus.NOT_FOUND, "BOOKING_SOURCE_NOT_FOUND",
            "Source booking not found: " + bookingId);
    }
}

/**
 * 409 BOOKING_CONFLICT — FRD F1 error table, booking overlaps with an
 * existing confirmed booking for the same location or resource.
 */
class BookingConflictException extends ApiException {
    public BookingConflictException() {
        super(HttpStatus.CONFLICT, "BOOKING_CONFLICT",
            "Booking conflicts with an existing booking");
    }
}

// ── FRD F2 error codes (conflict detection) ─────────────────────────────────

/**
 * 400 CONFLICT_CHECK_INVALID_INPUT — FRD F2 error table, POST
 * /bookings/check-conflicts with invalid or incomplete input.
 */
class ConflictCheckInvalidInputException extends ApiException {
    public ConflictCheckInvalidInputException(String message) {
        super(HttpStatus.BAD_REQUEST, "CONFLICT_CHECK_INVALID_INPUT", message);
    }
}

// ── FRD F3 error codes (approval workflow) ───────────────────────────────────

/**
 * 403 APPROVAL_FORBIDDEN — FRD F3 error table, caller lacks role_booking_approver.
 * Used by BookingApprovalController (plan 05-04) via manual hasRole check +
 * throw (NOT via BookingAccessDeniedHandler), so the FRD's distinct code is
 * preserved for the approve/deny action pair. Mirrors Phase 3 plan 03-02's
 * PermissionController precedent exactly.
 */
class ApprovalForbiddenException extends ApiException {
    public ApprovalForbiddenException() {
        super(HttpStatus.FORBIDDEN, "APPROVAL_FORBIDDEN",
            "Only approvers can approve or deny bookings");
    }
}

/**
 * 409 APPROVAL_INVALID_STATE — FRD F3 error table, booking is already
 * approved or denied (not in 'pending' state).
 */
class ApprovalInvalidStateException extends ApiException {
    public ApprovalInvalidStateException(String currentStatus) {
        super(HttpStatus.CONFLICT, "APPROVAL_INVALID_STATE",
            "Booking cannot be approved or denied in status: " + currentStatus);
    }
}

/**
 * 404 APPROVAL_BOOKING_NOT_FOUND — FRD F3 error table, booking id not found
 * for POST /bookings/{id}/approve or /deny.
 */
class ApprovalBookingNotFoundException extends ApiException {
    public ApprovalBookingNotFoundException(String bookingId) {
        super(HttpStatus.NOT_FOUND, "APPROVAL_BOOKING_NOT_FOUND",
            "Booking not found for approval: " + bookingId);
    }
}

/**
 * 503 APPROVAL_SETTINGS_UNAVAILABLE — FRD F3 error table, settings-service
 * unreachable when fetching the approveBooking flag at booking creation time.
 * This is the booking-service's own named code for when IT cannot reach
 * settings-service (per Phase 04-05's named decision: "SETTINGS_UNAVAILABLE
 * (503) deliberately not implemented in settings-service — it is the calling
 * service's error when this service is unreachable").
 */
class ApprovalSettingsUnavailableException extends ApiException {
    public ApprovalSettingsUnavailableException() {
        super(HttpStatus.SERVICE_UNAVAILABLE, "APPROVAL_SETTINGS_UNAVAILABLE",
            "Approval settings temporarily unavailable");
    }
}

// ── Named additions (3 codes beyond FRD catalogue) ──────────────────────────

/**
 * 404 BOOKING_NOT_FOUND — Named addition beyond FRD F1 catalogue.
 * Used for plain "booking id not found" on GET/PUT/DELETE /bookings/{id},
 * distinct from BOOKING_SOURCE_NOT_FOUND (clone) and APPROVAL_BOOKING_NOT_FOUND
 * (approve/deny) to keep each entry point's error unambiguous.
 */
class BookingNotFoundException extends ApiException {
    public BookingNotFoundException(String bookingId) {
        super(HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND",
            "Booking not found: " + bookingId);
    }
}

/**
 * 403 BOOKING_FORBIDDEN — Named addition beyond FRD catalogue.
 * Used by BookingAccessDeniedHandler (Task 3) when a @PreAuthorize role check
 * fails, and by plan 05-03's BookingWriteService when an authenticated caller
 * with role_booking_creator tries to update/delete a booking they don't own
 * (and doesn't hold role_booking_approver). Required by TechArch §4.2's
 * "(owner) or admin" text for PUT/DELETE /bookings/{id}.
 */
class BookingForbiddenException extends ApiException {
    public BookingForbiddenException() {
        super(HttpStatus.FORBIDDEN, "BOOKING_FORBIDDEN",
            "Insufficient permissions for this booking operation");
    }
}

/**
 * 400 BOOKING_CUSTOM_FIELD_NOT_APPLICABLE — Named addition beyond FRD catalogue.
 * Used by plan 05-03's create()/update() when plan 05-02's CustomFieldClient
 * returns that a submitted custom_field_id is not applicable to this booking's
 * context. No corresponding FRD code exists (FRD's F5 error table is
 * custom-field-service's own error contract).
 */
class BookingCustomFieldNotApplicableException extends ApiException {
    public BookingCustomFieldNotApplicableException(String customFieldId) {
        super(HttpStatus.BAD_REQUEST, "BOOKING_CUSTOM_FIELD_NOT_APPLICABLE",
            "Custom field not applicable to this booking context: " + customFieldId);
    }
}
