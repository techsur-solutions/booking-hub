package com.bookinghub.userspermissions.dto;

import java.util.List;

/**
 * Request for updating a user's role set (PUT /users/{id}/roles, admin only).
 * Replaces the full role set (not additive) per plan 03-02's named decision.
 */
public record UserRolesUpdateRequest(List<String> roles) {}
