package com.bookinghub.locationsresources.security;

import com.bookinghub.locationsresources.error.ApiError;
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
 * Simpler than Phase 3's ApiAccessDeniedHandler: this service has exactly ONE
 * admin-forbidden error code (LOCATION_RESOURCE_FORBIDDEN, unlike Phase 3's
 * users-permissions-service which needed two different codes across two
 * controllers) — so a single global handler producing LOCATION_RESOURCE_FORBIDDEN
 * is sufficient; no manual per-controller hasRole check workaround is needed
 * here, @PreAuthorize + this handler is enough.
 *
 * Fires when an AUTHENTICATED caller lacks the required role for a resource
 * (e.g., @PreAuthorize check fails). Returns 403 with ApiError JSON shape,
 * not Spring Security's default error body.
 *
 * Distinct from JwksOutageAuthenticationEntryPoint (which handles UNauthenticated
 * callers -> 401, or JWKS-unreachable -> 503).
 */
@Component
public class LocationResourceAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public LocationResourceAccessDeniedHandler(ObjectMapper objectMapper) {
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
            "LOCATION_RESOURCE_FORBIDDEN",
            "Insufficient permissions for this resource",
            Instant.now(),
            request.getRequestURI()
        );

        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
