package com.bookinghub.auditlog.error;

import org.springframework.http.HttpStatus;

/**
 * 400 AUDIT_LOG_INVALID_DATE_RANGE — from date is after to date, or date format invalid.
 * Thrown by plan 06-04's AuditLogController during parameter validation.
 */
public class AuditLogInvalidDateRangeException extends ApiException {
    public AuditLogInvalidDateRangeException(String message) {
        super(HttpStatus.BAD_REQUEST, "AUDIT_LOG_INVALID_DATE_RANGE", message);
    }
}
