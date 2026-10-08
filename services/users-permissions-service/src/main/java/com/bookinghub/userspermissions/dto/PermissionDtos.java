package com.bookinghub.userspermissions.dto;

import java.util.List;

/**
 * Container class for permission management DTOs (F7.2).
 * 
 * These record types define the shapes specified in FRD Y1-api.md §Permissions.
 */
public class PermissionDtos {
    
    /**
     * Single entry in the full permission mapping table.
     * Returned by GET /permissions.
     */
    public record PermissionMappingEntry(
        String legacyFlag,
        String keycloakRole,
        Object gatedActions,
        boolean confirmed
    ) {}
    
    /**
     * Role-to-permission mapping response.
     * Returned by GET /roles/{role}/permissions.
     */
    public record RolePermissionsResponse(
        String roleName,
        List<String> permissionFlags
    ) {}
    
    /**
     * Request body for updating role-to-permission mapping.
     * Used by PUT /roles/{role}/permissions.
     */
    public record RolePermissionsUpdateRequest(
        List<String> permissionFlags
    ) {}
}
