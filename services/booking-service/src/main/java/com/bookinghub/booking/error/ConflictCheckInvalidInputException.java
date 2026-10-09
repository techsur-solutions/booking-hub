package com.bookinghub.booking.error;

import org.springframework.http.HttpStatus;

/** 400 CONFLICT_CHECK_INVALID_INPUT — FRD F2 error table, POST /bookings/check-conflicts with invalid or incomplete input. */
public class ConflictCheckInvalidInputException extends ApiException {
    public ConflictCheckInvalidInputException(String message) {
        super(HttpStatus.BAD_REQUEST, "CONFLICT_CHECK_INVALID_INPUT", message);
    }
}
