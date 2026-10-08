package com.bookinghub.userspermissions.dto;

/**
 * Password reset request (POST /auth/password-reset/request).
 */
public record PasswordResetRequestRequest(String email) {}
