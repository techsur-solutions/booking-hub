package com.bookinghub.userspermissions.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Provider for extracting authenticated user information from JWT principal.
 * 
 * Every controller in this service can call getCurrentUserId(), getCurrentRoles(),
 * hasRole(), or isSelf() to perform authorization checks without hand-parsing
 * the JWT themselves.
 * 
 * Reads from SecurityContextHolder's Jwt principal:
 * - sub claim → UUID user id
 * - realm_access.roles claim (nested map) → Set<String> realm roles
 */
@Component
public class CurrentUserProvider {

    /**
     * Extracts the authenticated user's Keycloak user ID from the JWT 'sub' claim.
     * 
     * @return User UUID
     * @throws IllegalStateException if called without authenticated context
     */
    public UUID getCurrentUserId() {
        Jwt jwt = getJwtPrincipal();
        String sub = jwt.getSubject();
        if (sub == null) {
            throw new IllegalStateException("JWT sub claim is null");
        }
        return UUID.fromString(sub);
    }

    /**
     * Extracts the authenticated user's realm roles from the JWT 'realm_access.roles' claim.
     * 
     * @return Set of realm role names (e.g., "role_user_admin", "role_location_admin")
     */
    @SuppressWarnings("unchecked")
    public Set<String> getCurrentRoles() {
        Jwt jwt = getJwtPrincipal();
        
        // JWT realm_access claim structure: { "realm_access": { "roles": ["role1", "role2"] } }
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return Set.of();
        }

        Object rolesObj = realmAccess.get("roles");
        if (rolesObj instanceof List) {
            List<String> rolesList = (List<String>) rolesObj;
            return new HashSet<>(rolesList);
        }

        return Set.of();
    }

    /**
     * Checks if the authenticated user holds a specific realm role.
     * 
     * @param role The role name to check (e.g., "role_user_admin")
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(String role) {
        return getCurrentRoles().contains(role);
    }

    /**
     * Checks if the authenticated user is the same as the target user ID.
     * 
     * Useful for "self-or-admin" authorization patterns:
     * if (currentUserProvider.isSelf(targetUserId) || currentUserProvider.hasRole("role_user_admin")) { ... }
     * 
     * @param targetUserId The user ID to compare against
     * @return true if getCurrentUserId() equals targetUserId
     */
    public boolean isSelf(UUID targetUserId) {
        return getCurrentUserId().equals(targetUserId);
    }

    /**
     * Retrieves the JWT principal from Spring Security context.
     * 
     * @return Jwt principal
     * @throws IllegalStateException if no authenticated Jwt principal exists
     */
    private Jwt getJwtPrincipal() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            throw new IllegalStateException("No authenticated JWT principal found in security context");
        }
        return (Jwt) authentication.getPrincipal();
    }
}
