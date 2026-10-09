package com.bookinghub.locationsresources.error;

import org.springframework.http.HttpStatus;

/**
 * Authenticated caller lacks the required role for a Location/Resource
 * write operation — 403 FORBIDDEN.
 *
 * Split out of ApiException.java (plan 04-02, [Rule 3 - Blocking]). Note:
 * under normal operation this is produced by LocationResourceAccessDeniedHandler
 * (filter-chain level, via @PreAuthorize) rather than thrown directly by
 * controller/service code — kept here as a public ApiException subtype for
 * consistency and in case a future manual check needs to throw it directly.
 */
public class LocationResourceForbiddenException extends ApiException {
    public LocationResourceForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, "LOCATION_RESOURCE_FORBIDDEN", message);
    }
}
