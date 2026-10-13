package com.bookinghub.notifications.security;

import com.bookinghub.notifications.error.ApiError;
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
 * Identical shape and logic to Phase 3 plan 03-02's precedent (adapted to this
 * service's package name).
 *
 * Fires when an AUTHENTICATED caller lacks the required role for a resource
 * (anyRequest().hasAuthority("role_audit_viewer") check fails). Returns 403 with
 * ApiError JSON shape carrying AUTH_FORBIDDEN code, not Spring Security's default
 * error body.
 *
 * Distinct from JwksOutageAuthenticationEntryPoint (which handles UNauthenticated
 * callers → 401, or JWKS-unreachable → 503).
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
            "AUTH_FORBIDDEN",
            "Insufficient permissions for this resource",
            Instant.now(),
            request.getRequestURI()
        );

        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
