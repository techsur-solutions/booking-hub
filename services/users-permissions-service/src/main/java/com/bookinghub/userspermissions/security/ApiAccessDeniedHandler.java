package com.bookinghub.userspermissions.security;

import com.bookinghub.userspermissions.error.ApiError;
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
 * F6.6 Access-denied handler: structured ApiError response for insufficient role.
 * 
 * This is the named-decision translation of legacy's blank denied.cfm page
 * into a structured API-first response.
 * 
 * Fires when an AUTHENTICATED caller lacks the required role for a resource
 * (e.g., @PreAuthorize check fails). Returns 403 with ApiError JSON shape
 * carrying AUTH_FORBIDDEN code, not Spring Security's default error body.
 * 
 * Distinct from JwksOutageAuthenticationEntryPoint (which handles UNauthenticated
 * callers → 401, or JWKS-unreachable → 503).
 * 
 * NOTE: PermissionController (plan 03-05) deliberately bypasses this handler by
 * performing manual CurrentUserProvider.hasRole(...) checks and throwing
 * PermissionsForbiddenException directly (caught by GlobalExceptionHandler),
 * because it needs PERMISSIONS_FORBIDDEN code instead of the generic AUTH_FORBIDDEN
 * this handler produces. Every OTHER controller uses @PreAuthorize checks and
 * falls through to this handler correctly.
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
