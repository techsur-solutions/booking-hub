package com.bookinghub.locationsresources.error;

import org.springframework.http.HttpStatus;

/**
 * Requested Location does not exist at all — 404 NOT FOUND.
 *
 * Split out of ApiException.java (plan 04-02, [Rule 3 - Blocking]) so
 * LocationService (a different package) can reference it.
 *
 * Named decision (plan 04-02 Task 1, closing the Success-Criterion-2 gap):
 * this exception is thrown ONLY when a Location id never existed at all.
 * A soft-deleted-but-real Location id returns 200 with last-known values
 * (deletedAt populated) from GET /locations/{id} — it does NOT 404. See
 * LocationService.getById's Javadoc for the full rationale.
 */
public class LocationNotFoundException extends ApiException {
    public LocationNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "LOCATION_NOT_FOUND", message);
    }
}
