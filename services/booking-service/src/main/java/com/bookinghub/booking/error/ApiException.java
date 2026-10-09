package com.bookinghub.booking.error;

import org.springframework.http.HttpStatus;

/**
 * Base class for all API exceptions in this service.
 * Each subclass hard-codes its FRD-specified HTTP status and error_code.
 *
 * GlobalExceptionHandler catches ApiException polymorphically and constructs
 * ApiError responses with the exception's httpStatus/errorCode/message.
 *
 * 13 concrete subclasses in total (each in its own file per Java conventions):
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
 *   error code. Used both by BookingAccessDeniedHandler and manually
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
 *
 * Plan 05-02 deviation note: Originally all exception subclasses were in this
 * single file as package-private classes. They were extracted to individual public
 * class files so cross-package callers (client/, conflict/ packages) can import them.
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
