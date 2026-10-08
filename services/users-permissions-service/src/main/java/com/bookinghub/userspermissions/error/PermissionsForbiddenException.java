package com.bookinghub.userspermissions.error;

import org.springframework.http.HttpStatus;

/**
 * Permission-specific forbidden (explicit permission check failed) — 403 FORBIDDEN
 * 
 * Thrown by PermissionController when a non-admin attempts to access admin-only endpoints.
 * Returns PERMISSIONS_FORBIDDEN specifically, not the generic AUTH_FORBIDDEN.
 */
public class PermissionsForbiddenException extends ApiException {
    public PermissionsForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, "PERMISSIONS_FORBIDDEN", message);
    }
}
