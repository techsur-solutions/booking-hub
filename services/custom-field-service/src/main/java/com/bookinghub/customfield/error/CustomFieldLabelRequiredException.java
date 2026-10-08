package com.bookinghub.customfield.error;

import org.springframework.http.HttpStatus;

/**
 * Custom field label is missing or blank — 400 BAD REQUEST.
 */
public class CustomFieldLabelRequiredException extends ApiException {
    public CustomFieldLabelRequiredException(String message) {
        super(HttpStatus.BAD_REQUEST, "CUSTOM_FIELD_LABEL_REQUIRED", message);
    }
}
