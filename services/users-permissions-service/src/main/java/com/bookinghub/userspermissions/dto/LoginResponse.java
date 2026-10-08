package com.bookinghub.userspermissions.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Login response (POST /auth/login).
 */
public record LoginResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("expires_in") long expiresIn,
        List<String> roles
) {}
