package com.bookinghub.settings.error;

import org.springframework.http.HttpStatus;

/**
 * Authenticated caller lacks role_settings_admin for a write operation on
 * /settings — 403 FORBIDDEN, SETTINGS_FORBIDDEN (FRD F10 Error States).
 *
 * Public top-level class (not package-private) because SettingsAccessDeniedHandler
 * (security package) constructs this exception's shape directly for the
 * AccessDeniedHandler path.
 */
public class SettingsForbiddenException extends ApiException {
    public SettingsForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, "SETTINGS_FORBIDDEN", message);
    }
}
