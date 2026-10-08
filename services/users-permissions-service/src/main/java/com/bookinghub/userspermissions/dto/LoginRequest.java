package com.bookinghub.userspermissions.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Login request (POST /auth/login).
 */
public record LoginRequest(
        String username,
        String password,
        @JsonProperty("remember_me") Boolean rememberMe
) {}
