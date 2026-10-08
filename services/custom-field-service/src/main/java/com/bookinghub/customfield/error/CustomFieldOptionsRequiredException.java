package com.bookinghub.customfield.error;

import org.springframework.http.HttpStatus;

/**
 * Custom field of a choice-based type (e.g. "select") is missing its options
 * array — 400 BAD REQUEST.
 */
public class CustomFieldOptionsRequiredException extends ApiException {
    public CustomFieldOptionsRequiredException(String message) {
        super(HttpStatus.BAD_REQUEST, "CUSTOM_FIELD_OPTIONS_REQUIRED", message);
    }
}
