package com.bookinghub.locationsresources.error;

import org.springframework.http.HttpStatus;

/**
 * Base class for all API exceptions in this service.
 * Each subclass hard-codes its FRD-specified HTTP status and error_code.
 *
 * GlobalExceptionHandler catches ApiException polymorphically and constructs
 * ApiError responses with the exception's httpStatus/errorCode/message.
 *
 * Named decision: no LOCATION_IN_USE/RESOURCE_IN_USE (409) code implemented —
 * the FRD catalogues these (F4 Error States, line 351) for the case where
 * delete is blocked because the entity is referenced by active bookings.
 * This phase's soft-delete policy (plan 04-02) never blocks a delete regardless
 * of references, so these two error codes are deliberately unreachable under
 * that policy, not an oversight.
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    protected ApiException(HttpStatus httpStatus, String errorCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

// NOTE: the 5 concrete ApiException subclasses that used to live inline in this
// file (package-private) were split into their own top-level files
// (LocationNameRequiredException.java, ResourceNameRequiredException.java,
// LocationResourceForbiddenException.java, LocationNotFoundException.java,
// ResourceNotFoundException.java) as `public class` — plan 04-02's
// LocationService/ResourceService/LocationController/ResourceController live in
// different packages (service/controller) and must be able to reference these
// exception types, which a package-private class in the `error` package cannot
// provide. Java also permits only one public top-level class per file, so they
// could not become `public` in place here. [Rule 3 - Blocking]
