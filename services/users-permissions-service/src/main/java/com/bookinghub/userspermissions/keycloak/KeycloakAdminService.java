package com.bookinghub.userspermissions.keycloak;

import com.bookinghub.userspermissions.error.ApiException;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Keycloak Admin API wrapper service.
 * 
 * Provides high-level user/role management operations against Keycloak's Admin REST API
 * via the userperm-admin-client service account.
 * 
 * Zero live Keycloak dependency in unit tests — all operations are tested against WireMock.
 * 
 * Authorization note: This class performs NO authorization checks — it is a pure API wrapper.
 * The CALLING controller (with @PreAuthorize guards) is responsible for proving the caller
 * holds the required role (e.g., role_user_admin) before invoking these methods.
 */
@Service
public class KeycloakAdminService {

    private final Keycloak keycloak;
    private final String realm;

    public KeycloakAdminService(
            Keycloak keycloak,
            @Value("${keycloak.realm}") String realm) {
        this.keycloak = keycloak;
        this.realm = realm;
    }

    /**
     * Creates a new Keycloak user with initial password and realm role.
     * 
     * @param email User's email (used as both username and email)
     * @param initialPassword Initial password
     * @param temporary If true, user must change password on first login
     * @param initialRealmRole Initial realm role to assign (e.g., "role_user")
     * @return Keycloak user ID (UUID string)
     * @throws UserAlreadyExistsException if user with this email already exists (409)
     * @throws PasswordPolicyViolationException if password violates Keycloak policy (400)
     */
    public String createUser(String email, String initialPassword, boolean temporary, String initialRealmRole) {
        // Build user representation
        UserRepresentation user = new UserRepresentation();
        user.setUsername(email);
        user.setEmail(email);
        user.setEnabled(true);
        user.setEmailVerified(true);

        // Create user
        Response response = keycloak.realm(realm).users().create(user);
        
        try {
            if (response.getStatus() == 409) {
                throw new UserAlreadyExistsException("User with email " + email + " already exists");
            }
            
            if (response.getStatus() != 201) {
                throw new RuntimeException("Failed to create user: " + response.getStatusInfo());
            }

            // Extract user ID from Location header
            String userId = CreatedResponseUtil.getCreatedId(response);

            // Set initial password
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(initialPassword);
            credential.setTemporary(temporary);

            try {
                keycloak.realm(realm).users().get(userId).resetPassword(credential);
            } catch (Exception e) {
                // Password policy violation from Keycloak typically arrives as BadRequestException
                if (e.getMessage() != null && e.getMessage().contains("violates password policy")) {
                    throw new PasswordPolicyViolationException("Password does not meet complexity requirements");
                }
                throw e;
            }

            // Assign initial realm role
            RoleRepresentation roleRepresentation = keycloak.realm(realm)
                    .roles()
                    .get(initialRealmRole)
                    .toRepresentation();

            keycloak.realm(realm).users().get(userId)
                    .roles()
                    .realmLevel()
                    .add(List.of(roleRepresentation));

            return userId;
        } finally {
            response.close();
        }
    }

    /**
     * Resets a user's password.
     * 
     * @param keycloakUserId Keycloak user ID
     * @param newPassword New password
     * @param temporary If true, user must change password on next login
     * @throws PasswordPolicyViolationException if password violates Keycloak policy
     */
    public void resetPassword(String keycloakUserId, String newPassword, boolean temporary) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(newPassword);
        credential.setTemporary(temporary);

        try {
            keycloak.realm(realm).users().get(keycloakUserId).resetPassword(credential);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("violates password policy")) {
                throw new PasswordPolicyViolationException("Password does not meet complexity requirements");
            }
            throw e;
        }
    }

    /**
     * Replaces a user's full realm-role set.
     * 
     * Named decision: Legacy users.role was a single string column (one role per user).
     * The new system follows FRD F6.5's "one or more roles" and PUT /users/{id}/roles {roles[]}
     * array-shaped API contract, so this method's semantics are REPLACE-the-set (not additive).
     * 
     * This is a deliberate improvement over legacy's single-role model, not a parity claim.
     * 
     * @param keycloakUserId Keycloak user ID
     * @param newRoles New set of realm role names (e.g., ["role_user", "role_location_admin"])
     */
    public void updateUserRealmRoles(String keycloakUserId, List<String> newRoles) {
        var userResource = keycloak.realm(realm).users().get(keycloakUserId);
        var realmRolesResource = userResource.roles().realmLevel();

        // Remove all current realm roles
        List<RoleRepresentation> currentRoles = realmRolesResource.listAll();
        if (!currentRoles.isEmpty()) {
            realmRolesResource.remove(currentRoles);
        }

        // Add new roles
        if (!newRoles.isEmpty()) {
            List<RoleRepresentation> rolesToAdd = newRoles.stream()
                    .map(roleName -> keycloak.realm(realm).roles().get(roleName).toRepresentation())
                    .collect(Collectors.toList());
            realmRolesResource.add(rolesToAdd);
        }
    }

    /**
     * Force-logout a user by revoking ALL their active sessions.
     * 
     * Named decision: This revokes ALL of the user's sessions (not one targeted session),
     * because the POST /auth/logout API contract (Y1-api.md) carries no session/refresh-token
     * identifier to target narrowly.
     * 
     * Residual risk (a legitimate session on another device gets force-logged-out too) is
     * accepted as the safer security default for a "logout" operation with no session scope.
     */
    public void logoutUser(String keycloakUserId) {
        keycloak.realm(realm).users().get(keycloakUserId).logout();
    }

    /**
     * Finds a Keycloak user ID by email address.
     * 
     * @param email User email to search (exact match)
     * @return Optional containing user ID if found, empty otherwise
     */
    public Optional<String> findUserIdByEmail(String email) {
        List<UserRepresentation> users = keycloak.realm(realm)
                .users()
                .search(email, true); // exact match

        if (users.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(users.get(0).getId());
    }
}

/**
 * User already exists (email conflict) — 409 CONFLICT
 */
class UserAlreadyExistsException extends ApiException {
    public UserAlreadyExistsException(String message) {
        super(HttpStatus.CONFLICT, "USER_ALREADY_EXISTS", message);
    }
}

/**
 * New password violates policy (length, complexity) — 400 BAD REQUEST
 */
class PasswordPolicyViolationException extends ApiException {
    public PasswordPolicyViolationException(String message) {
        super(HttpStatus.BAD_REQUEST, "PASSWORD_POLICY_VIOLATION", message);
    }
}
