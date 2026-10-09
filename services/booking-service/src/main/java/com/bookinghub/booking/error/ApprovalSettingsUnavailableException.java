package com.bookinghub.booking.error;

import org.springframework.http.HttpStatus;

/**
 * 503 APPROVAL_SETTINGS_UNAVAILABLE — FRD F3 error table, settings-service unreachable
 * when fetching the approveBooking flag at booking creation time.
 * This is the booking-service's own named code for when IT cannot reach settings-service
 * (per Phase 04-05's named decision: "SETTINGS_UNAVAILABLE deliberately not implemented
 * in settings-service — it is the calling service's error when this service is unreachable").
 */
public class ApprovalSettingsUnavailableException extends ApiException {
    public ApprovalSettingsUnavailableException() {
        super(HttpStatus.SERVICE_UNAVAILABLE, "APPROVAL_SETTINGS_UNAVAILABLE",
            "Approval settings temporarily unavailable");
    }
}
