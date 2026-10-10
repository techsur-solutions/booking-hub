package com.bookinghub.notifications.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * Shared error response record for all API errors from this service.
 * Maps to FRD's exact snake_case contract: error_code, message, timestamp, path.
 *
 * Byte-for-byte the same shape as Phase 3 plan 03-01's ApiError
 * (every service in this project owns its own copy per service-isolation — established precedent).
 */
public record ApiError(
    @JsonProperty("error_code") String errorCode,
    @JsonProperty("message") String message,
    @JsonProperty("timestamp") Instant timestamp,
    @JsonProperty("path") String path
) {}
