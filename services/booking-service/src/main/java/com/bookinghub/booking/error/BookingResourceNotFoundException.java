package com.bookinghub.booking.error;

import org.springframework.http.HttpStatus;

/** 404 BOOKING_RESOURCE_NOT_FOUND — FRD F1 error table, a resource_id in the request not found in locations-resources-service. */
public class BookingResourceNotFoundException extends ApiException {
    public BookingResourceNotFoundException(String resourceId) {
        super(HttpStatus.NOT_FOUND, "BOOKING_RESOURCE_NOT_FOUND",
            "Resource not found: " + resourceId);
    }
}
