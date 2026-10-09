package com.bookinghub.booking.error;

import org.springframework.http.HttpStatus;

/** 404 BOOKING_SOURCE_NOT_FOUND — FRD F1 error table, clone source booking id not found. */
public class BookingSourceNotFoundException extends ApiException {
    public BookingSourceNotFoundException(String bookingId) {
        super(HttpStatus.NOT_FOUND, "BOOKING_SOURCE_NOT_FOUND",
            "Source booking not found: " + bookingId);
    }
}
