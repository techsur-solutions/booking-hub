package com.bookinghub.customfield.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;

/**
 * Global exception handler for all controllers in this service.
 *
 * Maps ApiException (and its subclasses) to ApiError responses with the
 * FRD-specified HTTP status and error_code. Single handler covers all subtypes
 * polymorphically — no per-exception-type duplication.
 *
 * Named decision (plan 04-04 Task 1) — a second handler for
 * MethodArgumentNotValidException shapes Bean Validation failures (e.g. an
 * out-of-enum field_type, a blank Field Template name) as 400 REQUEST_MALFORMED
 * (TechArch §4.12's platform-level cross-cutting code) rather than Spring's
 * default validation-error body. This keeps the FRD's two named F5 codes
 * (CUSTOM_FIELD_LABEL_REQUIRED, CUSTOM_FIELD_OPTIONS_REQUIRED) reserved for
 * exactly the two cases they describe.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(FieldError::getDefaultMessage)
            .orElse("Request payload failed validation");

        ApiError apiError = new ApiError(
            "REQUEST_MALFORMED",
            message,
            Instant.now(),
            request.getRequestURI()
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(apiError);
    }
}
