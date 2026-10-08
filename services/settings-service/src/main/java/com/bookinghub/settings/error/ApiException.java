package com.bookinghub.settings.error;

import org.springframework.http.HttpStatus;

/**
 * Base class for all API exceptions in this service.
 * Each subclass hard-codes its FRD-specified HTTP status and error_code.
 *
 * GlobalExceptionHandler catches ApiException polymorphically and constructs
 * ApiError responses with the exception's httpStatus/errorCode/message.
 *
 * Exactly 3 concrete subclasses exist per FRD F10 Error States (lines 684-686):
 * SettingsForbiddenException, SettingsInvalidCalendarRangeException,
 * SettingsInvalidSlotSizeException — each in its own public file (Java requires
 * public top-level classes to match their filename; SettingsForbiddenException
 * in particular is also referenced from the security package).
 *
 * Named decision: SETTINGS_UNAVAILABLE (503) is deliberately NOT a subclass
 * here. The FRD catalogues this code (line 687) for "Settings Service
 * unavailable when a dependent service reads approveBooking" — that is thrown
 * by the CALLING service (Phase 5's booking-service) when THIS service is
 * unreachable, not by this service about itself.
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    protected ApiException(HttpStatus httpStatus, String errorCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
