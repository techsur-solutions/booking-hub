package com.bookinghub.booking.security;

import com.bookinghub.booking.error.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Access-denied handler: structured ApiError response for insufficient role.
 *
 * Fires when an AUTHENTICATED caller's @PreAuthorize role check fails — returns
 * HTTP 403 with BOOKING_FORBIDDEN ApiError JSON shape (the named addition from
 * Task 2), not Spring Security's default error body.
 *
 * Named decision (APPROVE/DENY uses APPROVAL_FORBIDDEN instead): the FRD names a
 * distinct code for approve/deny access failures. BookingApprovalController (plan
 * 05-04) performs a manual currentUserProvider.hasRole("role_booking_approver")
 * check and throws ApprovalForbiddenException directly (same pattern as Phase 3
 * plan 03-02's PermissionController precedent), so the FRD's distinct
 * APPROVAL_FORBIDDEN code is preserved for that specific action pair and never
 * falls through to this generic handler. Every OTHER controller (BookingController,
 * BookingQueryController, ConflictController) uses plain @PreAuthorize and falls
 * through to this handler correctly.
 *
 * Distinct from JwksOutageAuthenticationEntryPoint (which handles UNauthenticated
 * callers -> 401, or JWKS-unreachable -> 503).
 */
@Component
public class BookingAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public BookingAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiError error = new ApiError(
            "BOOKING_FORBIDDEN",
            "Insufficient permissions for this booking operation",
            Instant.now(),
            request.getRequestURI()
        );

        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
