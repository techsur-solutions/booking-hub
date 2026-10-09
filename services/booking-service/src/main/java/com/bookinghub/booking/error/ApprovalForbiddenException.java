package com.bookinghub.booking.error;

import org.springframework.http.HttpStatus;

/**
 * 403 APPROVAL_FORBIDDEN — FRD F3 error table, caller lacks role_booking_approver.
 * Used by BookingApprovalController (plan 05-04) via manual hasRole check + throw
 * (NOT via BookingAccessDeniedHandler), so the FRD's distinct code is preserved
 * for the approve/deny action pair. Mirrors Phase 3 plan 03-02's
 * PermissionController precedent exactly.
 */
public class ApprovalForbiddenException extends ApiException {
    public ApprovalForbiddenException() {
        super(HttpStatus.FORBIDDEN, "APPROVAL_FORBIDDEN",
            "Only approvers can approve or deny bookings");
    }
}
