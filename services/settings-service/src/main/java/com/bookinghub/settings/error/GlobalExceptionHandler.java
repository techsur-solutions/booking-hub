package com.bookinghub.settings.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;

/**
 * Global exception handler for all controllers in this service.
 *
 * Maps ApiException (and its 3 subclasses) to ApiError responses with the
 * FRD-specified HTTP status and error_code. Single handler covers all subtypes
 * polymorphically — no per-exception-type duplication. Any unhandled exception
 * falls through to Spring Boot's default 500 handler (no information
 * disclosure beyond the named 3 error codes).
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
}
