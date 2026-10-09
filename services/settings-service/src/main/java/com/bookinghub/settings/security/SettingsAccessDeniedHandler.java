package com.bookinghub.settings.security;

import com.bookinghub.settings.error.ApiError;
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
 * Fires when an AUTHENTICATED caller lacks the required role for a resource
 * (e.g. plan 04-06's updateSettings() @PreAuthorize("hasRole('role_settings_admin')")
 * check fails on PUT /settings). Returns 403 with ApiError JSON shape carrying
 * SETTINGS_FORBIDDEN code — this service has only one admin-forbidden code
 * (the write path), so a single global handler is sufficient (unlike
 * users-permissions-service, which needed a manual-check carve-out for a
 * second distinct forbidden code).
 *
 * Distinct from JwksOutageAuthenticationEntryPoint (which handles UNauthenticated
 * callers → 401, or JWKS-unreachable → 503).
 */
@Component
public class SettingsAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public SettingsAccessDeniedHandler(ObjectMapper objectMapper) {
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
            "SETTINGS_FORBIDDEN",
            "Insufficient permissions for this resource",
            Instant.now(),
            request.getRequestURI()
        );

        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
