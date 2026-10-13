package com.bookinghub.auditlog.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Provider for extracting authenticated user information from JWT principal.
 *
 * Reads from SecurityContextHolder's Jwt principal:
 * - sub claim -> UUID user id (getCurrentUserId)
 * - realm_access.roles claim (nested map) -> Set<String> realm roles (getCurrentRoles)
 * - hasRole(String) -> boolean for manual role checks (plan 06-04's controller)
 *
 * Named decision (identical shape to Phase 3 plan 03-02's precedent):
 * plan 06-04's AuditLogController needs hasRole() for its manual
 * AUDIT_LOG_FORBIDDEN check, per the integration contract in this plan's
 * plan frontmatter.
 *
 * Identical shape to Phase 3 plan 03-02's CurrentUserProvider, adapted to
 * this service's package (com.bookinghub.auditlog).
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
     * Extracts the authenticated user's realm roles from the JWT 'realm_access.roles'
     * claim (Keycloak's nested role format).
     *
     * @return Set of realm role names (e.g., "role_audit_viewer")
     */
    @SuppressWarnings("unchecked")
    public Set<String> getCurrentRoles() {
        Jwt jwt = getJwtPrincipal();

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
     * Used by plan 06-04's AuditLogController for manual AUDIT_LOG_FORBIDDEN checks,
     * identical to Phase 3 PermissionController precedent.
     *
     * @param role The role name to check (e.g., "role_audit_viewer")
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(String role) {
        return getCurrentRoles().contains(role);
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
