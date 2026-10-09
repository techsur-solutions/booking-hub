package com.bookinghub.booking.error;

import org.springframework.http.HttpStatus;

/** 409 BOOKING_CONFLICT — FRD F1 error table, booking overlaps with an existing confirmed booking. */
public class BookingConflictException extends ApiException {
    public BookingConflictException() {
        super(HttpStatus.CONFLICT, "BOOKING_CONFLICT",
            "Booking conflicts with an existing booking");
    }
}
