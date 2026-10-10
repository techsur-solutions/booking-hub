package com.bookinghub.booking.controller;

import com.bookinghub.booking.dto.BookingReadDtos.BookingDetailResponse;
import com.bookinghub.booking.dto.BookingReadDtos.DenyBookingRequest;
import com.bookinghub.booking.error.ApprovalForbiddenException;
import com.bookinghub.booking.security.CurrentUserProvider;
import com.bookinghub.booking.service.BookingApprovalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for one-way approve/deny booking state transitions (plan 05-04).
 *
 * Endpoints:
 * - POST /bookings/{id}/approve
 * - POST /bookings/{id}/deny
 *
 * CRITICAL DESIGN DECISION — NO @PreAuthorize on either method:
 * This controller performs a MANUAL hasRole check producing ApprovalForbiddenException
 * (403 APPROVAL_FORBIDDEN) rather than using @PreAuthorize. The reason: Spring Security's
 * AccessDeniedException path produces a generic BookingAccessDeniedHandler response with
 * the generic BOOKING_FORBIDDEN code — but the FRD's F3 error table specifies the DISTINCT
 * APPROVAL_FORBIDDEN code for this specific action pair. The manual check is the ONLY way
 * to preserve that FRD-exact code.
 *
 * This matches Phase 3's PermissionController precedent exactly (plan 03-05): permissions
 * management operations use the same pattern for the same reason — the manual check is
 * cross-referenced explicitly in plan 05-01's Tier1Tier2ConsistencyTest.
 *
 * The plan 05-01 integration test (Tier1Tier2ConsistencyTest) verifies that this controller
 * has NO @PreAuthorize annotation AND DOES have a manual hasRole check — any regression
 * introducing @PreAuthorize would be caught at the integration test level.
 */
@RestController
@RequestMapping("/bookings")
public class BookingApprovalController {

    private final BookingApprovalService bookingApprovalService;
    private final CurrentUserProvider currentUserProvider;

    public BookingApprovalController(
            BookingApprovalService bookingApprovalService,
            CurrentUserProvider currentUserProvider) {
        this.bookingApprovalService = bookingApprovalService;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * POST /bookings/{id}/approve — approve a pending booking.
     *
     * One-way transition: only PENDING bookings may be approved.
     * Returns 200 with full BookingDetailResponse on success.
     *
     * NO @PreAuthorize — manual hasRole check below preserves APPROVAL_FORBIDDEN code.
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<BookingDetailResponse> approve(@PathVariable UUID id) {
        // Manual role check — NOT @PreAuthorize — to preserve APPROVAL_FORBIDDEN (not BOOKING_FORBIDDEN)
        if (!currentUserProvider.hasRole("role_booking_approver")) {
            throw new ApprovalForbiddenException();
        }
        BookingDetailResponse response = bookingApprovalService.approve(id);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /bookings/{id}/deny — deny a pending booking.
     *
     * One-way transition: only PENDING bookings may be denied.
     * Request body is optional (denial_reason is free-text and nullable).
     * Returns 200 with full BookingDetailResponse on success.
     *
     * NO @PreAuthorize — manual hasRole check below preserves APPROVAL_FORBIDDEN (not BOOKING_FORBIDDEN)
     */
    @PostMapping("/{id}/deny")
    public ResponseEntity<BookingDetailResponse> deny(
            @PathVariable UUID id,
            @RequestBody(required = false) DenyBookingRequest request) {
        // Manual role check — NOT @PreAuthorize — to preserve APPROVAL_FORBIDDEN (not BOOKING_FORBIDDEN)
        if (!currentUserProvider.hasRole("role_booking_approver")) {
            throw new ApprovalForbiddenException();
        }
        String denialReason = request != null ? request.denialReason() : null;
        BookingDetailResponse response = bookingApprovalService.deny(id, denialReason);
        return ResponseEntity.ok(response);
    }
}
