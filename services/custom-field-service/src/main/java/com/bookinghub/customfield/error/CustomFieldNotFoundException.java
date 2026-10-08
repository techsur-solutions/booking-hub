package com.bookinghub.customfield.error;

import org.springframework.http.HttpStatus;

/**
 * Target custom field or template id not found — 404 NOT FOUND.
 *
 * Used for BOTH custom-field-not-found and template-not-found, per the FRD's
 * single shared row for "Target Custom Field or Template id not found".
 */
public class CustomFieldNotFoundException extends ApiException {
    public CustomFieldNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "CUSTOM_FIELD_NOT_FOUND", message);
    }
}
