package com.bookinghub.locationsresources.security;

import com.bookinghub.locationsresources.error.ApiError;
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
 * and Phase 3's identical JwksOutageAuthenticationEntryPoint:
 * - JWKS connectivity failure -> 503 SERVICE_UNAVAILABLE (fail-closed)
 * - Invalid/expired/missing token -> 401 AUTH_UNAUTHENTICATED
 *
 * This ensures Tier 1 (Gateway) and Tier 2 (this service) never disagree about
 * failing closed when the IdP is unreachable.
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

        boolean isNetworkFailure = isJwksConnectivityFailure(authException);

        if (isNetworkFailure) {
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
     */
    private boolean isJwksConnectivityFailure(Throwable throwable) {
        if (throwable == null) {
            return false;
        }

        if (throwable instanceof ConnectException
                || throwable instanceof UnknownHostException
                || throwable instanceof TimeoutException) {
            return true;
        }

        if (throwable.getClass().getName().equals("com.nimbusds.jose.proc.BadJOSEException")) {
            Throwable cause = throwable.getCause();
            if (cause instanceof IOException || cause instanceof ConnectException) {
                return true;
            }
        }

        return isJwksConnectivityFailure(throwable.getCause());
    }
}
