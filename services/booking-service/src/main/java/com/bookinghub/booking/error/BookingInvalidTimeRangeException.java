package com.bookinghub.booking.error;

import org.springframework.http.HttpStatus;

/** 400 BOOKING_INVALID_TIME_RANGE — FRD F1 error table, end_time <= start_time. */
public class BookingInvalidTimeRangeException extends ApiException {
    public BookingInvalidTimeRangeException() {
        super(HttpStatus.BAD_REQUEST, "BOOKING_INVALID_TIME_RANGE",
            "Booking end time must be after start time");
    }
}
