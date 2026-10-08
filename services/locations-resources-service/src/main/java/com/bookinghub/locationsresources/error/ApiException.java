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

/**
 * Location name is required but was missing/blank — 400 BAD REQUEST
 */
class LocationNameRequiredException extends ApiException {
    public LocationNameRequiredException(String message) {
        super(HttpStatus.BAD_REQUEST, "LOCATION_NAME_REQUIRED", message);
    }
}

/**
 * Resource name is required but was missing/blank — 400 BAD REQUEST
 */
class ResourceNameRequiredException extends ApiException {
    public ResourceNameRequiredException(String message) {
        super(HttpStatus.BAD_REQUEST, "RESOURCE_NAME_REQUIRED", message);
    }
}

/**
 * Authenticated caller lacks the required role for a Location/Resource
 * write operation — 403 FORBIDDEN
 */
class LocationResourceForbiddenException extends ApiException {
    public LocationResourceForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, "LOCATION_RESOURCE_FORBIDDEN", message);
    }
}

/**
 * Requested Location does not exist (or is soft-deleted) — 404 NOT FOUND
 */
class LocationNotFoundException extends ApiException {
    public LocationNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "LOCATION_NOT_FOUND", message);
    }
}

/**
 * Requested Resource does not exist (or is soft-deleted) — 404 NOT FOUND
 */
class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", message);
    }
}
