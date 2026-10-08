package com.bookinghub.customfield.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * Shared error response record for all API errors from this service.
 * Maps to FRD's exact snake_case contract: error_code, message, timestamp, path.
 *
 * Every controller/handler in this service returns ApiError bodies via
 * GlobalExceptionHandler when ApiException is thrown.
 */
public record ApiError(
    @JsonProperty("error_code") String errorCode,
    @JsonProperty("message") String message,
    @JsonProperty("timestamp") Instant timestamp,
    @JsonProperty("path") String path
) {}
