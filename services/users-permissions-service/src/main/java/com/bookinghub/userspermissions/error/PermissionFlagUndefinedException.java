package com.bookinghub.userspermissions.error;

import org.springframework.http.HttpStatus;

/**
 * Permission flag referenced but not defined/confirmed in permissions table — 400 BAD REQUEST
 * 
 * Thrown when attempting to assign a permission flag to a role where the flag either:
 * - does not exist in the permissions table at all, or
 * - exists but confirmed=false (unconfirmed flags cannot be assigned)
 * 
 * Implements F7.4/F0-mandated deny-by-default enforcement.
 */
public class PermissionFlagUndefinedException extends ApiException {
    public PermissionFlagUndefinedException(String message) {
        super(HttpStatus.BAD_REQUEST, "PERMISSION_FLAG_UNDEFINED", message);
    }
}
