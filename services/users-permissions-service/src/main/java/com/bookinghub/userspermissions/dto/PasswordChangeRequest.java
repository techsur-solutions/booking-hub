package com.bookinghub.userspermissions.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Password change request (POST /auth/password-change).
 */
public record PasswordChangeRequest(
        @JsonProperty("current_password") String currentPassword,
        @JsonProperty("new_password") String newPassword
) {}
