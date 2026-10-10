package com.bookinghub.booking.error;

import org.springframework.http.HttpStatus;

/** 400 BOOKING_TITLE_REQUIRED — FRD F1 error table, booking title is empty/null. */
public class BookingTitleRequiredException extends ApiException {
    public BookingTitleRequiredException() {
        super(HttpStatus.BAD_REQUEST, "BOOKING_TITLE_REQUIRED", "Booking title is required");
    }
}
