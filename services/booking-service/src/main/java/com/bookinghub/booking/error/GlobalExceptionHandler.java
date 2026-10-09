package com.bookinghub.booking.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

/**
 * Global exception handler for all controllers in this service.
 *
 * Two handlers:
 *
 * (a) ApiException → handles all 13 concrete subclasses polymorphically
 *     via a single handler method. No per-exception-type duplication.
 *     Returns the exception's httpStatus + errorCode + message as ApiError.
 *
 * (b) MethodArgumentNotValidException → REQUEST_MALFORMED (400). FRD's error
 *     tables do not catalogue a code for malformed DTO shapes (e.g., an
 *     unparseable recurrence pattern, a non-ISO datetime string). Rather than
 *     inventing ad-hoc codes, this handler produces a 400 ApiError with
 *     error_code="REQUEST_MALFORMED" — the FRD's own platform-level cross-cutting
 *     code (TechArch §4.12), exactly as Phase 4 plan 04-04 established for this
 *     same class of problem (named decision matching Phase 4's precedent).
 *
 * Unhandled exceptions fall through to Spring Boot's default 500 handler —
 * this handler never leaks a stack trace (T-05-01-04).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles all 13 ApiException subclasses polymorphically.
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
     * Handles DTO validation failures (Bean Validation / @RequestBody deserialization).
     * Produces 400 REQUEST_MALFORMED — the FRD's own cross-cutting platform code
     * (TechArch §4.12), same as Phase 4 plan 04-04's precedent.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String firstError = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .orElse("Request validation failed");

        ApiError apiError = new ApiError(
            "REQUEST_MALFORMED",
            firstError,
            Instant.now(),
            request.getRequestURI()
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(apiError);
    }
}
