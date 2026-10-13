package com.bookinghub.feeds.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * Shared error response record for all API errors from feeds-service.
 * Maps to the FRD's exact snake_case contract (Y2-errors.md):
 * {error_code, message, timestamp, path}.
 *
 * Identical record shape to every other service in this codebase — feeds-service
 * is the intentionally-PUBLIC service (F9.6), so this handler faces the open
 * internet and MUST never leak a raw stack trace (see GlobalExceptionHandler's
 * Exception fallback).
 *
 * Named decision (own copy, no shared library): per the microservice isolation
 * principle, each service keeps its own copy rather than sharing via a library —
 * same pattern as every prior-phase service.
 */
public record ApiError(
    @JsonProperty("error_code") String errorCode,
    @JsonProperty("message") String message,
    @JsonProperty("timestamp") Instant timestamp,
    @JsonProperty("path") String path
) {}
