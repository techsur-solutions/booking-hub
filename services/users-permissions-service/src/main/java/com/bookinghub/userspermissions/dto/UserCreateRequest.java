package com.bookinghub.userspermissions.dto;

/**
 * Request for creating a new user account (admin only).
 * Admin provides email and initial role; initial password is auto-generated
 * with temporary=true (user must set own password on first login).
 */
public record UserCreateRequest(String email, String initialRole) {}
