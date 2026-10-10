package com.bookinghub.booking.error;

import org.springframework.http.HttpStatus;

/** 404 APPROVAL_BOOKING_NOT_FOUND — FRD F3 error table, booking id not found for approve/deny. */
public class ApprovalBookingNotFoundException extends ApiException {
    public ApprovalBookingNotFoundException(String bookingId) {
        super(HttpStatus.NOT_FOUND, "APPROVAL_BOOKING_NOT_FOUND",
            "Booking not found for approval: " + bookingId);
    }
}
