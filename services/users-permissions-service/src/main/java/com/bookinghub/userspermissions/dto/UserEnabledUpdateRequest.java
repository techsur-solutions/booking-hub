package com.bookinghub.userspermissions.dto;

/**
 * Request body for PATCH /users/{id}/enabled (admin only).
 * Sets the Keycloak enabled flag — true = active, false = deactivated.
 */
public record UserEnabledUpdateRequest(boolean enabled) {}
