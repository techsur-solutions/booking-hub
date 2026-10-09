package com.bookinghub.booking.security;

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
 * - raw validated JWT token value -> String for token relay (getRawBearerToken)
 *
 * Named decision (getRawBearerToken): this is the token-relay mechanism plan 05-02's
 * outbound HTTP clients use to forward the CALLING user's own already-validated
 * credential as "Authorization: Bearer <token>" on outbound calls to
 * locations-resources-service, custom-field-service, and settings-service —
 * avoiding a second service-account credential. The method returns the Jwt's
 * raw compact string (Jwt.getTokenValue()), which Spring Security has already
 * validated before this method is ever called.
 *
 * Named decision (scope boundary — custom-field-service GET exception): token relay
 * alone is insufficient for custom-field-service's GET /custom-fields (see plan
 * 05-02's explicit handling of that gap — it requires additional context in the
 * request beyond just the bearer token).
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
     * @return Set of realm role names (e.g., "role_booking_viewer", "role_booking_creator")
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
     *
     * @param role The role name to check (e.g., "role_booking_approver")
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(String role) {
        return getCurrentRoles().contains(role);
    }

    /**
     * Returns the raw compact JWT string for token relay to downstream services.
     *
     * The value is the original validated JWT compact string (the Bearer token
     * the caller sent), which Spring Security has already validated before this
     * method is ever reachable. Plan 05-02's outbound clients use this to set
     * "Authorization: Bearer <token>" on their calls to locations-resources-service,
     * custom-field-service, and settings-service.
     *
     * @return Raw JWT compact string (e.g., "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
     * @throws IllegalStateException if called without authenticated context
     */
    public String getRawBearerToken() {
        return getJwtPrincipal().getTokenValue();
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
