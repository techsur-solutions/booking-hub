package com.bookinghub.booking.error;

import org.springframework.http.HttpStatus;

/** 409 APPROVAL_INVALID_STATE — FRD F3 error table, booking is already approved or denied. */
public class ApprovalInvalidStateException extends ApiException {
    public ApprovalInvalidStateException(String currentStatus) {
        super(HttpStatus.CONFLICT, "APPROVAL_INVALID_STATE",
            "Booking cannot be approved or denied in status: " + currentStatus);
    }
}
