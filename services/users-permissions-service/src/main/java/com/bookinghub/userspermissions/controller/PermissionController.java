package com.bookinghub.userspermissions.controller;

import com.bookinghub.userspermissions.dto.PermissionDtos.PermissionMappingEntry;
import com.bookinghub.userspermissions.dto.PermissionDtos.RolePermissionsResponse;
import com.bookinghub.userspermissions.dto.PermissionDtos.RolePermissionsUpdateRequest;
import com.bookinghub.userspermissions.error.PermissionsForbiddenException;
import com.bookinghub.userspermissions.security.CurrentUserProvider;
import com.bookinghub.userspermissions.service.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Permission mapping administration controller (F7.2).
 * 
 * ALL THREE ENDPOINTS require role_permissions_admin and enforce it via MANUAL
 * currentUserProvider.hasRole("role_permissions_admin") checks (no @PreAuthorize
 * annotations on this controller).
 * 
 * RATIONALE (per plan 03-02 ApiAccessDeniedHandler note and this plan's Task 1):
 * These endpoints must return the FRD's distinct PERMISSIONS_FORBIDDEN (403) error,
 * not the generic AUTH_FORBIDDEN that plan 03-02's shared ApiAccessDeniedHandler
 * produces for every @PreAuthorize-guarded controller. Since a single global
 * accessDeniedHandler bean cannot distinguish which controller triggered it, this
 * controller bypasses @PreAuthorize/AccessDeniedException entirely: each method's
 * FIRST line manually checks hasRole("role_permissions_admin") and throws
 * PermissionsForbiddenException directly (an ordinary ApiException), which plan
 * 03-01's GlobalExceptionHandler catches normally as a controller-thrown exception,
 * unaffected by the filter-chain-level accessDeniedHandler's generic mapping.
 */
@RestController
@RequestMapping("/api")
public class PermissionController {
    
    private final PermissionService permissionService;
    private final CurrentUserProvider currentUserProvider;
    
    public PermissionController(PermissionService permissionService,
                               CurrentUserProvider currentUserProvider) {
        this.permissionService = permissionService;
        this.currentUserProvider = currentUserProvider;
    }
    
    /**
     * GET /permissions
     * 
     * Returns the full permission mapping table (all 17 seeded rows).
     * Admin-only: requires role_permissions_admin.
     */
    @GetMapping("/permissions")
    public ResponseEntity<List<PermissionMappingEntry>> getPermissions() {
        // Manual admin check (no @PreAuthorize) to produce PERMISSIONS_FORBIDDEN specifically
        if (!currentUserProvider.hasRole("role_permissions_admin")) {
            throw new PermissionsForbiddenException(
                "Access denied: role_permissions_admin required to view permission mappings"
            );
        }
        
        List<PermissionMappingEntry> permissions = permissionService.listPermissions();
        return ResponseEntity.ok(permissions);
    }
    
    /**
     * GET /roles/{role}/permissions
     * 
     * Returns the permission flags currently assigned to a specific role.
     * Admin-only: requires role_permissions_admin.
     */
    @GetMapping("/roles/{role}/permissions")
    public ResponseEntity<RolePermissionsResponse> getRolePermissions(@PathVariable String role) {
        // Manual admin check (no @PreAuthorize) to produce PERMISSIONS_FORBIDDEN specifically
        if (!currentUserProvider.hasRole("role_permissions_admin")) {
            throw new PermissionsForbiddenException(
                "Access denied: role_permissions_admin required to view role permissions"
            );
        }
        
        RolePermissionsResponse response = permissionService.getRolePermissions(role);
        return ResponseEntity.ok(response);
    }
    
    /**
     * PUT /roles/{role}/permissions
     * 
     * Updates the permission flags assigned to a role.
     * Admin-only: requires role_permissions_admin.
     * 
     * F7.4/F0 deny-by-default enforcement happens in PermissionService: any flag
     * that is undefined OR unconfirmed is rejected with 400 PERMISSION_FLAG_UNDEFINED.
     */
    @PutMapping("/roles/{role}/permissions")
    public ResponseEntity<RolePermissionsResponse> updateRolePermissions(
        @PathVariable String role,
        @RequestBody RolePermissionsUpdateRequest request
    ) {
        // Manual admin check (no @PreAuthorize) to produce PERMISSIONS_FORBIDDEN specifically
        if (!currentUserProvider.hasRole("role_permissions_admin")) {
            throw new PermissionsForbiddenException(
                "Access denied: role_permissions_admin required to update role permissions"
            );
        }
        
        RolePermissionsResponse response = permissionService.updateRolePermissions(role, request);
        return ResponseEntity.ok(response);
    }
}
