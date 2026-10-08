package com.bookinghub.userspermissions.dto;

/**
 * Request for admin editing another user's profile (PUT /users/{id}).
 * 
 * Still no password field (password reset via dedicated /auth flows or future
 * admin-reset endpoint, not this profile-update endpoint). Role changes go
 * through PUT /users/{id}/roles per FRD's API shape, not embedded here.
 */
public record UserAdminUpdateRequest(String displayName) {}
