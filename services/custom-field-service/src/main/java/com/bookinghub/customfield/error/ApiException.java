package com.bookinghub.customfield.error;

import org.springframework.http.HttpStatus;

/**
 * Base class for all API exceptions in this service.
 * Each subclass hard-codes its FRD-specified HTTP status and error_code.
 *
 * GlobalExceptionHandler catches ApiException polymorphically and constructs
 * ApiError responses with the exception's httpStatus/errorCode/message.
 *
 * Named decision — CUSTOM_FIELD_VALUE_INVALID (400) is deliberately NOT
 * implemented here. The FRD catalogues this code for "submitted
 * custom_field_values[] references unknown field_id" — but per
 * open-questions.md item 6 and this phase's scope boundary, validating/writing
 * custom_field_values happens at booking-submission time, which is Phase 5's
 * booking-service responsibility, not this service's. This service's job in F5
 * is definitions/templates only.
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
