package com.bookinghub.userspermissions.dto;

import java.time.Instant;
import java.util.List;

/**
 * User response DTO (hand-assembled from User entity + Keycloak roles + Keycloak enabled flag).
 * Never serializes the User entity directly — prevents accidental exposure
 * of any future-added sensitive columns.
 *
 * The {@code enabled} field mirrors Keycloak's enabled flag:
 * {@code true} = active (can log in), {@code false} = deactivated.
 */
public record UserResponse(
        String id,
        String email,
        String displayName,
        List<String> roles,
        Instant createdAt,
        Instant updatedAt,
        boolean enabled
) {}
