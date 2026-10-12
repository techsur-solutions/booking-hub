package com.bookinghub.auditlog.error;

import org.springframework.http.HttpStatus;

/**
 * Base class for all API exceptions in audit-log-service.
 * Each subclass hard-codes its FRD-specified HTTP status and error_code.
 *
 * GlobalExceptionHandler catches ApiException polymorphically and constructs
 * ApiError responses with the exception's httpStatus/errorCode/message.
 *
 * Concrete subclasses are defined in separate files (per Java convention):
 * - AuditLogForbiddenException.java (403, AUDIT_LOG_FORBIDDEN)
 * - AuditLogInvalidDateRangeException.java (400, AUDIT_LOG_INVALID_DATE_RANGE)
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
