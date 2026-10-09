package com.bookinghub.booking.error;

import org.springframework.http.HttpStatus;

/**
 * 403 BOOKING_FORBIDDEN — Named addition beyond FRD catalogue.
 * Used by BookingAccessDeniedHandler when a @PreAuthorize role check fails,
 * and by plan 05-03's BookingWriteService when an authenticated caller
 * with role_booking_creator tries to update/delete a booking they don't own
 * (and doesn't hold role_booking_approver). Required by TechArch §4.2's
 * "(owner) or admin" text for PUT/DELETE /bookings/{id}.
 */
public class BookingForbiddenException extends ApiException {
    public BookingForbiddenException() {
        super(HttpStatus.FORBIDDEN, "BOOKING_FORBIDDEN",
            "Insufficient permissions for this booking operation");
    }
}
