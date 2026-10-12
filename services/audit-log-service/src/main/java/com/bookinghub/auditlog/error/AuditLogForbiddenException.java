package com.bookinghub.auditlog.error;

import org.springframework.http.HttpStatus;

/**
 * 403 AUDIT_LOG_FORBIDDEN — authenticated caller lacks role_audit_viewer.
 * Thrown by plan 06-04's AuditLogController via manual hasRole() check.
 */
public class AuditLogForbiddenException extends ApiException {
    public AuditLogForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, "AUDIT_LOG_FORBIDDEN", message);
    }
}
