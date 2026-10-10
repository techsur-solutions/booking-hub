package com.bookinghub.booking.error;

import org.springframework.http.HttpStatus;

/**
 * 400 BOOKING_CUSTOM_FIELD_NOT_APPLICABLE — Named addition beyond FRD catalogue.
 * Used by plan 05-03's create()/update() when plan 05-02's CustomFieldClient
 * returns that a submitted custom_field_id is not applicable to this booking's
 * context. No corresponding FRD code exists (FRD's F5 error table is
 * custom-field-service's own error contract).
 */
public class BookingCustomFieldNotApplicableException extends ApiException {
    public BookingCustomFieldNotApplicableException(String customFieldId) {
        super(HttpStatus.BAD_REQUEST, "BOOKING_CUSTOM_FIELD_NOT_APPLICABLE",
            "Custom field not applicable to this booking context: " + customFieldId);
    }
}
