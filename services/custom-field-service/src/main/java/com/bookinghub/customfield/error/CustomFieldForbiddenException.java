package com.bookinghub.customfield.error;

import org.springframework.http.HttpStatus;

/**
 * Authenticated but insufficient role for custom-field/template management —
 * 403 FORBIDDEN.
 */
public class CustomFieldForbiddenException extends ApiException {
    public CustomFieldForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, "CUSTOM_FIELD_FORBIDDEN", message);
    }
}
