package com.bookinghub.booking.error;

import org.springframework.http.HttpStatus;

/** 404 BOOKING_LOCATION_NOT_FOUND — FRD F1 error table, location_id not found in locations-resources-service. */
public class BookingLocationNotFoundException extends ApiException {
    public BookingLocationNotFoundException(String locationId) {
        super(HttpStatus.NOT_FOUND, "BOOKING_LOCATION_NOT_FOUND",
            "Location not found: " + locationId);
    }
}
