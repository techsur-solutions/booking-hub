package com.bookinghub.customfield.security;

import com.bookinghub.customfield.error.ApiError;
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
 * Access-denied handler producing CustomFieldForbiddenException's shape
 * (403, CUSTOM_FIELD_FORBIDDEN).
 *
 * Fires when an AUTHENTICATED caller lacks role_customfield_admin for a
 * resource (every @PreAuthorize check in this service's controllers fails the
 * same way, since this route group has no read/write split — see SecurityConfig's
 * named decision). This service has exactly one admin-forbidden code, so a
 * single global handler is sufficient, no manual per-controller workaround
 * needed.
 *
 * Distinct from JwksOutageAuthenticationEntryPoint (which handles UNauthenticated
 * callers → 401, or JWKS-unreachable → 503).
 */
@Component
public class CustomFieldAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public CustomFieldAccessDeniedHandler(ObjectMapper objectMapper) {
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
            "CUSTOM_FIELD_FORBIDDEN",
            "Insufficient permissions for this resource",
            Instant.now(),
            request.getRequestURI()
        );

        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
