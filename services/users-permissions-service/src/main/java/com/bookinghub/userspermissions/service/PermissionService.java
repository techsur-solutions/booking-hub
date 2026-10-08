package com.bookinghub.userspermissions.service;

import com.bookinghub.userspermissions.domain.OutboxEvent;
import com.bookinghub.userspermissions.domain.Permission;
import com.bookinghub.userspermissions.dto.PermissionDtos.PermissionMappingEntry;
import com.bookinghub.userspermissions.dto.PermissionDtos.RolePermissionsResponse;
import com.bookinghub.userspermissions.dto.PermissionDtos.RolePermissionsUpdateRequest;
import com.bookinghub.userspermissions.error.PermissionFlagUndefinedException;
import com.bookinghub.userspermissions.repository.OutboxEventRepository;
import com.bookinghub.userspermissions.repository.PermissionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for permission mapping administration (F7.2).
 * 
 * Implements deny-by-default enforcement: any flag that is undefined OR unconfirmed
 * is rejected with PERMISSION_FLAG_UNDEFINED when attempted to be assigned to a role.
 * 
 * Named decision (F0 Open Question #18): this service does NOT implement legacy's
 * "reload required" in-memory permission cache. A role-to-permission mapping change
 * takes effect the moment a user's NEXT Keycloak token is issued — every @PreAuthorize
 * check reads the role directly off the JWT. This is a deliberate improvement over
 * legacy's restart-required behavior.
 */
@Service
public class PermissionService {
    
    private final PermissionRepository permissionRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    
    public PermissionService(PermissionRepository permissionRepository,
                            OutboxEventRepository outboxEventRepository,
                            ObjectMapper objectMapper) {
        this.permissionRepository = permissionRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Returns all permission mappings in the table.
     * Used by GET /permissions to show admins the full mapping table.
     */
    public List<PermissionMappingEntry> listPermissions() {
        return permissionRepository.findAll().stream()
            .map(p -> new PermissionMappingEntry(
                p.getLegacyFlag(),
                p.getKeycloakRole(),
                parseGatedActions(p.getGatedActions()),
                p.isConfirmed()
            ))
            .toList();
    }
    
    /**
     * Returns the permission flags currently assigned to a role.
     * Used by GET /roles/{role}/permissions.
     */
    public RolePermissionsResponse getRolePermissions(String roleName) {
        List<String> flags = permissionRepository.findByKeycloakRole(roleName)
            .stream()
            .map(Permission::getLegacyFlag)
            .toList();
        
        return new RolePermissionsResponse(roleName, flags);
    }
    
    /**
     * Updates the permission flags assigned to a role.
     * 
     * F7.4/F0-mandated deny-by-default enforcement: for EVERY flag in requestedFlags,
     * this method looks it up in the permissions table. If the flag:
     * - does not exist at all → throw PermissionFlagUndefinedException (400)
     * - exists but confirmed=false → ALSO throw PermissionFlagUndefinedException
     * 
     * An unconfirmed flag MUST never be assignable to a role, as doing so would be
     * the exact "silently treat unconfirmed as no restriction" failure mode FRD F7
     * §Validation explicitly forbids.
     * 
     * Only flags that exist AND confirmed=true may have their keycloakRole column
     * updated to roleName. This is how the mapping is "assigned" to a role, matching
     * legacy's per-flag role-checkbox-row editing model (see F0 audit's "Permissions
     * controller" section).
     * 
     * Writes an OutboxEvent in the same transaction as the permissions table updates.
     */
    @Transactional
    public RolePermissionsResponse updateRolePermissions(String roleName, 
                                                         RolePermissionsUpdateRequest request) {
        // DENY-BY-DEFAULT ENFORCEMENT: validate ALL requested flags first
        for (String flag : request.permissionFlags()) {
            Permission perm = permissionRepository.findByLegacyFlag(flag)
                .orElseThrow(() -> new PermissionFlagUndefinedException(
                    "Permission flag '" + flag + "' does not exist in the permissions table"
                ));
            
            if (!perm.isConfirmed()) {
                throw new PermissionFlagUndefinedException(
                    "Permission flag '" + flag + "' exists but is not confirmed (confirmed=false). " +
                    "Unconfirmed flags cannot be assigned to roles."
                );
            }
        }
        
        // Clear existing mappings for this role (remove role from all flags)
        List<Permission> existingPerms = permissionRepository.findByKeycloakRole(roleName);
        for (Permission perm : existingPerms) {
            perm.setKeycloakRole(""); // Clear the role mapping
        }
        permissionRepository.saveAll(existingPerms);
        
        // Assign new mappings (set keycloakRole = roleName for each requested flag)
        for (String flag : request.permissionFlags()) {
            Permission perm = permissionRepository.findByLegacyFlag(flag).get(); // Already validated above
            perm.setKeycloakRole(roleName);
        }
        permissionRepository.flush();
        
        // Write outbox event for eventual publication to permission.events exchange
        Map<String, Object> payload = new HashMap<>();
        payload.put("roleName", roleName);
        payload.put("permissionFlags", request.permissionFlags());
        payload.put("action", "role_permissions_updated");
        
        OutboxEvent event = new OutboxEvent(
            "permission",
            UUID.randomUUID(), // No single permission aggregate id for a role update
            "permission.events",
            "permission.updated",
            serializePayload(payload)
        );
        outboxEventRepository.save(event);
        
        return new RolePermissionsResponse(roleName, request.permissionFlags());
    }
    
    /**
     * Parse gated_actions JSONB back to Object for DTO.
     * Returns the parsed JSON structure or the raw string if parsing fails.
     */
    private Object parseGatedActions(String gatedActionsJson) {
        try {
            return objectMapper.readValue(gatedActionsJson, Object.class);
        } catch (JsonProcessingException e) {
            return gatedActionsJson; // Fallback to raw string
        }
    }
    
    /**
     * Serialize payload map to JSON string for outbox storage.
     */
    private String serializePayload(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize outbox payload", e);
        }
    }
}
