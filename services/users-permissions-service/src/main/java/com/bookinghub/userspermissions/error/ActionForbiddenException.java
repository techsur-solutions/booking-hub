package com.bookinghub.userspermissions.error;

import org.springframework.http.HttpStatus;

/**
 * Generic action forbidden (user lacks permission) — 403 FORBIDDEN
 */
public class ActionForbiddenException extends ApiException {
    public ActionForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, "ACTION_FORBIDDEN", message);
    }
}
