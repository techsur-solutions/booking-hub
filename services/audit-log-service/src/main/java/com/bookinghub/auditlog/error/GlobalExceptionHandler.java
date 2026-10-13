package com.bookinghub.auditlog.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

/**
 * Global exception handler for all controllers in audit-log-service.
 *
 * Two handlers:
 *
 * (a) ApiException → handles both concrete subclasses (AuditLogForbiddenException,
 *     AuditLogInvalidDateRangeException) polymorphically via a single handler method.
 *     Returns the exception's httpStatus + errorCode + message as ApiError JSON.
 *
 * (b) MethodArgumentNotValidException → REQUEST_MALFORMED (400). For malformed DTO
 *     shapes (unparseable query params, invalid formats) — produces 400 ApiError with
 *     error_code="REQUEST_MALFORMED" per TechArch §4.12 cross-cutting code.
 *     Same pattern as Phase 4 plan 04-04 and Phase 5 plan 05-01's precedents.
 *
 * Unhandled exceptions fall through to Spring Boot's default 500 handler.
 * This handler never leaks a stack trace (T-06-03-01 threat mitigation).
 *
 * Identical shape to Phase 3 plan 03-01's GlobalExceptionHandler, adapted to
 * this service's error types and package.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

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
