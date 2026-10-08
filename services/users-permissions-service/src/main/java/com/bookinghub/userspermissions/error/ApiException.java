package com.bookinghub.userspermissions.error;

import org.springframework.http.HttpStatus;

/**
 * Base class for all API exceptions in this service.
 * Each subclass hard-codes its FRD-specified HTTP status and error_code.
 * 
 * GlobalExceptionHandler catches ApiException polymorphically and constructs
 * ApiError responses with the exception's httpStatus/errorCode/message.
 */
public abstract class ApiException extends RuntimeException {
    
    private final HttpStatus httpStatus;
    private final String errorCode;
    
    protected ApiException(HttpStatus httpStatus, String errorCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }
    
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

/**
 * User already exists (email conflict) — 409 CONFLICT
 */
class UserAlreadyExistsException extends ApiException {
    public UserAlreadyExistsException(String message) {
        super(HttpStatus.CONFLICT, "USER_ALREADY_EXISTS", message);
    }
}

/**
 * Password reset token is invalid, expired, or already used — 400 BAD REQUEST
 */
class PasswordResetTokenInvalidException extends ApiException {
    public PasswordResetTokenInvalidException(String message) {
        super(HttpStatus.BAD_REQUEST, "PASSWORD_RESET_TOKEN_INVALID", message);
    }
}

/**
 * Password change failed: current password incorrect — 401 UNAUTHORIZED
 */
class PasswordChangeInvalidCurrentException extends ApiException {
    public PasswordChangeInvalidCurrentException(String message) {
        super(HttpStatus.UNAUTHORIZED, "PASSWORD_CHANGE_INVALID_CURRENT", message);
    }
}

/**
 * New password violates policy (length, complexity) — 400 BAD REQUEST
 */
class PasswordPolicyViolationException extends ApiException {
    public PasswordPolicyViolationException(String message) {
        super(HttpStatus.BAD_REQUEST, "PASSWORD_POLICY_VIOLATION", message);
    }
}

/**
 * Generic action forbidden (user lacks permission) — 403 FORBIDDEN
 */
class ActionForbiddenException extends ApiException {
    public ActionForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, "ACTION_FORBIDDEN", message);
    }
}

/**
 * Permission-specific forbidden (explicit permission check failed) — 403 FORBIDDEN
 */
class PermissionsForbiddenException extends ApiException {
    public PermissionsForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, "PERMISSIONS_FORBIDDEN", message);
    }
}

/**
 * Permission flag referenced but not defined in permissions table — 400 BAD REQUEST
 */
class PermissionFlagUndefinedException extends ApiException {
    public PermissionFlagUndefinedException(String message) {
        super(HttpStatus.BAD_REQUEST, "PERMISSION_FLAG_UNDEFINED", message);
    }
}

/**
 * Authentication failed: invalid credentials — 401 UNAUTHORIZED
 */
class AuthInvalidCredentialsException extends ApiException {
    public AuthInvalidCredentialsException(String message) {
        super(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", message);
    }
}
