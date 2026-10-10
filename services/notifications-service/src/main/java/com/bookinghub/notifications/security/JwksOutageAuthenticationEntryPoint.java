package com.bookinghub.notifications.security;

import com.bookinghub.notifications.error.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.concurrent.TimeoutException;

/**
 * Fail-closed authentication entry point for Tier-2 JWT validation.
 *
 * Mirrors the Gateway's GatewayFailClosedTest behavior (Phase 2 plan 02-10)
 * and is identical in structure to Phase 3 plan 03-02's precedent:
 * - JWKS connectivity failure → 503 SERVICE_UNAVAILABLE (fail-closed)
 * - Invalid/expired/missing token → 401 AUTH_UNAUTHENTICATED
 *
 * This ensures Tier 1 (Gateway) and Tier 2 (this service) never disagree about
 * failing closed when the IdP is unreachable.
 *
 * Both outcomes produce the shared ApiError JSON shape,
 * not Spring Security's default error bodies.
 */
@Component
public class JwksOutageAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwksOutageAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        // Inspect the cause chain for network-class failures reaching JWKS endpoint
        boolean isNetworkFailure = isJwksConnectivityFailure(authException);

        if (isNetworkFailure) {
            // JWKS unreachable → fail closed with 503
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ApiError error = new ApiError(
                "SERVICE_UNAVAILABLE",
                "Authentication service temporarily unavailable",
                Instant.now(),
                request.getRequestURI()
            );

            objectMapper.writeValue(response.getOutputStream(), error);
        } else {
            // Invalid/expired/missing token → 401
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ApiError error = new ApiError(
                "AUTH_UNAUTHENTICATED",
                "Authentication required",
                Instant.now(),
                request.getRequestURI()
            );

            objectMapper.writeValue(response.getOutputStream(), error);
        }
    }

    /**
     * Recursively inspects the exception cause chain for network-class failures
     * that indicate JWKS endpoint unreachability.
     *
     * Distinguishes genuine connectivity failures (unreachable IdP) from
     * merely-invalid-token failures (expired/malformed JWT).
     */
    private boolean isJwksConnectivityFailure(Throwable throwable) {
        if (throwable == null) {
            return false;
        }

        // Check if this level of the cause chain is a network-class exception
        if (throwable instanceof ConnectException
                || throwable instanceof UnknownHostException
                || throwable instanceof TimeoutException) {
            return true;
        }

        // Check for Nimbus BadJOSEException wrapping an I/O failure
        if (throwable.getClass().getName().equals("com.nimbusds.jose.proc.BadJOSEException")) {
            Throwable cause = throwable.getCause();
            if (cause instanceof IOException || cause instanceof ConnectException) {
                return true;
            }
        }

        // Recurse into the cause chain
        return isJwksConnectivityFailure(throwable.getCause());
    }
}
