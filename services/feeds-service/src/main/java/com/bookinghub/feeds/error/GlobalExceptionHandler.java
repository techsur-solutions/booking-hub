package com.bookinghub.feeds.error;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

/**
 * Global exception handler for all controllers in feeds-service.
 *
 * Two handlers:
 *
 * (a) ApiException → handles concrete subclasses (FeedLocationFilterInvalidException)
 *     polymorphically via a single handler method. Returns the exception's
 *     httpStatus + errorCode + message as ApiError JSON. Identical shape to Phase 3
 *     plan 03-01 / Phase 6 plan 06-03.
 *
 * (b) Exception (fallback) → 500 INTERNAL_ERROR. CRITICAL for this service:
 *     feeds-service is the intentionally-PUBLIC service (F9.6), so this handler
 *     faces the open internet — unlike the other services' authenticated-only
 *     handlers. The fallback returns a generic ApiError and NEVER leaks a raw
 *     stack trace or SQL error to the caller (threat T-07-01-04).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles all ApiException subclasses polymorphically.
     * The httpStatus, errorCode, and message are all encoded in the exception
     * subclass — no switching or instanceof needed here.
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException ex, HttpServletRequest request) {
        ApiError apiError = new ApiError(
            ex.getErrorCode(),
            ex.getMessage(),
            Instant.now(),
            request.getRequestURI()
        );

        return ResponseEntity
            .status(ex.getHttpStatus())
            .body(apiError);
    }

    /**
     * Catch-all fallback for any unhandled exception. Returns a generic
     * 500 INTERNAL_ERROR ApiError so this public service never leaks internal
     * details (stack traces, SQL errors) to the open internet.
     * The actual exception is logged server-side for diagnosis.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception serving {}", request.getRequestURI(), ex);

        ApiError apiError = new ApiError(
            "INTERNAL_ERROR",
            "An unexpected error occurred.",
            Instant.now(),
            request.getRequestURI()
        );

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(apiError);
    }
}
