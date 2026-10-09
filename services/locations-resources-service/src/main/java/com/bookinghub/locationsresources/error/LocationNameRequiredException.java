package com.bookinghub.locationsresources.error;

import org.springframework.http.HttpStatus;

/**
 * Location name is required but was missing/blank — 400 BAD REQUEST.
 *
 * Split out of ApiException.java (plan 04-02, [Rule 3 - Blocking]) so
 * LocationService (a different package) can reference it — a package-private
 * class declared in ApiException.java was not visible outside the `error`
 * package.
 */
public class LocationNameRequiredException extends ApiException {
    public LocationNameRequiredException(String message) {
        super(HttpStatus.BAD_REQUEST, "LOCATION_NAME_REQUIRED", message);
    }
}
