package com.bookinghub.settings.error;

import org.springframework.http.HttpStatus;

/**
 * Calendar slot size is invalid (e.g. zero or negative minutes) — 400 BAD
 * REQUEST, SETTINGS_INVALID_SLOT_SIZE (FRD F10 Error States). Thrown by plan
 * 04-06's PUT /settings validation before persisting.
 */
public class SettingsInvalidSlotSizeException extends ApiException {
    public SettingsInvalidSlotSizeException(String message) {
        super(HttpStatus.BAD_REQUEST, "SETTINGS_INVALID_SLOT_SIZE", message);
    }
}
