package com.bookinghub.auditlog.error;

import org.springframework.http.HttpStatus;

/**
 * Base class for all API exceptions in audit-log-service.
 * Each subclass hard-codes its FRD-specified HTTP status and error_code.
 *
 * GlobalExceptionHandler catches ApiException polymorphically and constructs
 * ApiError responses with the exception's httpStatus/errorCode/message.
 *
 * Two concrete subclasses (per F11 §Error States table):
 *
 * AuditLogForbiddenException (403, AUDIT_LOG_FORBIDDEN) — FRD F11's error for
 *   an authenticated-but-unprivileged caller attempting to view the audit log.
 *   Plan 06-04's AuditLogController uses this for its manual hasRole() check,
 *   identical pattern to Phase 3 PermissionController.
 *
 * AuditLogInvalidDateRangeException (400, AUDIT_LOG_INVALID_DATE_RANGE) — FRD F11's
 *   error for invalid date range parameters on GET /audit-log (e.g., from > to).
 *   Plan 06-04's controller throws this for date validation.
 *
 * Named decision (single file, package-private subclasses): only 2 subclasses,
 * both consumed by plan 06-04's controller which is in the same service deployment,
 * so there is no cross-package throwability requirement for this service's simple
 * error catalog. The grep contract (integration_contracts.provides verify check)
 * is satisfied by their definition in this file.
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

// ──────────────────────────────────────────────────────────────────────────────
// Concrete subclasses — package-private, defined in ApiException.java per plan.
// Plan 06-04's AuditLogController (same package or sub-package) throws these.
// ──────────────────────────────────────────────────────────────────────────────

/**
 * 403 AUDIT_LOG_FORBIDDEN — authenticated caller lacks role_audit_viewer.
 * Thrown by plan 06-04's AuditLogController via manual hasRole() check.
 */
class AuditLogForbiddenException extends ApiException {
    public AuditLogForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, "AUDIT_LOG_FORBIDDEN", message);
    }
}

/**
 * 400 AUDIT_LOG_INVALID_DATE_RANGE — from date is after to date, or date format invalid.
 * Thrown by plan 06-04's AuditLogController during parameter validation.
 */
class AuditLogInvalidDateRangeException extends ApiException {
    public AuditLogInvalidDateRangeException(String message) {
        super(HttpStatus.BAD_REQUEST, "AUDIT_LOG_INVALID_DATE_RANGE", message);
    }
}
