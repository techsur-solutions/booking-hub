package com.bookinghub.settings.error;

import org.springframework.http.HttpStatus;

/**
 * Calendar min/max time range is invalid (e.g. min >= max) — 400 BAD REQUEST,
 * SETTINGS_INVALID_CALENDAR_RANGE (FRD F10 Error States). Thrown by plan
 * 04-06's PUT /settings validation before persisting.
 */
public class SettingsInvalidCalendarRangeException extends ApiException {
    public SettingsInvalidCalendarRangeException(String message) {
        super(HttpStatus.BAD_REQUEST, "SETTINGS_INVALID_CALENDAR_RANGE", message);
    }
}
