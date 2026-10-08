package com.bookinghub.userspermissions.dto;

/**
 * Request for self-service profile update (PUT /users/me).
 * 
 * CRITICAL: This type has NO password or role field at all — compile-time guard
 * against privilege escalation via profile edit, structurally equivalent to
 * legacy's runtime structDelete(password,passwordConfirmation,role) from params.
 */
public record UserSelfUpdateRequest(String displayName) {}
