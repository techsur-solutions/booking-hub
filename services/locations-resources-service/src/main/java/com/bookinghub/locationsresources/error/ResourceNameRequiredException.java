package com.bookinghub.locationsresources.error;

import org.springframework.http.HttpStatus;

/**
 * Resource name is required but was missing/blank — 400 BAD REQUEST.
 *
 * Split out of ApiException.java (plan 04-02, [Rule 3 - Blocking]) so
 * ResourceService (a different package) can reference it.
 */
public class ResourceNameRequiredException extends ApiException {
    public ResourceNameRequiredException(String message) {
        super(HttpStatus.BAD_REQUEST, "RESOURCE_NAME_REQUIRED", message);
    }
}
