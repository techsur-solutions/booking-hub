package com.bookinghub.auditlog.security;

import com.bookinghub.auditlog.error.ApiError;
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
 * Fires when an AUTHENTICATED caller lacks role_audit_viewer — returns HTTP 403
 * with AUDIT_LOG_FORBIDDEN ApiError JSON shape, not Spring Security's default
 * error body.
 *
 * Named decision: AUDIT_LOG_FORBIDDEN matches the FRD's F11 §Error States table
 * code for an authenticated-but-unprivileged caller. This handler fires for ALL
 * routes on this service (every non-actuator route requires role_audit_viewer,
 * with no per-action split unlike booking-service).
 *
 * Distinct from JwksOutageAuthenticationEntryPoint (which handles UNauthenticated
 * callers -> 401, or JWKS-unreachable -> 503).
 *
 * Identical shape to Phase 3 plan 03-02's precedent, adapted to this service's
 * error code and package.
 */
@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public ApiAccessDeniedHandler(ObjectMapper objectMapper) {
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
            "AUDIT_LOG_FORBIDDEN",
            "Insufficient permissions to access audit log",
            Instant.now(),
            request.getRequestURI()
        );

        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
