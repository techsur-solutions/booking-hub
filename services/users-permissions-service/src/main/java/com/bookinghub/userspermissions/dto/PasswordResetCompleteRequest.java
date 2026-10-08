package com.bookinghub.userspermissions.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Password reset complete request (POST /auth/password-reset/complete).
 */
public record PasswordResetCompleteRequest(
        @JsonProperty("reset_token") String resetToken,
        @JsonProperty("new_password") String newPassword
) {}
