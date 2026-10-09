package com.bookinghub.locationsresources.error;

import org.springframework.http.HttpStatus;

/**
 * Requested Resource does not exist at all — 404 NOT FOUND.
 *
 * Split out of ApiException.java (plan 04-02, [Rule 3 - Blocking]) so
 * ResourceService (a different package) can reference it.
 *
 * Named decision: thrown ONLY when a Resource id never existed at all — a
 * soft-deleted-but-real Resource id returns 200 with last-known values from
 * GET /resources/{id}, matching LocationNotFoundException's rationale exactly.
 */
public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", message);
    }
}
