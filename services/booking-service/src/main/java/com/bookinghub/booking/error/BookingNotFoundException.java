package com.bookinghub.booking.error;

import org.springframework.http.HttpStatus;

/**
 * 404 BOOKING_NOT_FOUND — Named addition beyond FRD F1 catalogue.
 * Used for plain "booking id not found" on GET/PUT/DELETE /bookings/{id},
 * distinct from BOOKING_SOURCE_NOT_FOUND (clone) and APPROVAL_BOOKING_NOT_FOUND
 * (approve/deny) to keep each entry point's error unambiguous.
 */
public class BookingNotFoundException extends ApiException {
    public BookingNotFoundException(String bookingId) {
        super(HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND",
            "Booking not found: " + bookingId);
    }
}
