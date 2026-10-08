package com.bookinghub.userspermissions.service;

import com.bookinghub.userspermissions.domain.OutboxEvent;
import com.bookinghub.userspermissions.domain.User;
import com.bookinghub.userspermissions.dto.*;
import com.bookinghub.userspermissions.error.ActionForbiddenException;
import com.bookinghub.userspermissions.error.ApiException;
import com.bookinghub.userspermissions.keycloak.KeycloakAdminService;
import com.bookinghub.userspermissions.repository.OutboxEventRepository;
import com.bookinghub.userspermissions.repository.UserRepository;
import com.bookinghub.userspermissions.security.CurrentUserProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;

/**
 * User service implementing F6's account CRUD, self-vs-admin authorization,
 * and role assignment — publishing user.created/user.updated/role.assigned
 * domain events via the transactional outbox pattern.
 * 
 * All mutating methods are @Transactional to ensure local DB writes and outbox
 * event writes occur in the same ACID boundary.
 * 
 * Authorization note: This service does NOT check authorization — the CONTROLLER
 * layer (via @PreAuthorize guards) proves the caller holds required roles before
 * invoking these methods.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final CurrentUserProvider currentUserProvider;
    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom;

    public UserService(
            UserRepository userRepository,
            OutboxEventRepository outboxEventRepository,
            KeycloakAdminService keycloakAdminService,
            CurrentUserProvider currentUserProvider,
            ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.keycloakAdminService = keycloakAdminService;
        this.currentUserProvider = currentUserProvider;
        this.objectMapper = objectMapper;
        this.secureRandom = new SecureRandom();
    }

    /**
     * Lists all users (admin only — enforced at controller layer).
     */
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .toList();
    }

    /**
     * Creates a new user account:
     * 1. Checks for duplicate email (case-insensitive) — fail fast before Keycloak call
     * 2. Provisions Keycloak identity with auto-generated initial password (temporary=true)
     * 3. Inserts local User row with id = Keycloak sub (never a local UUID)
     * 4. Writes user.created outbox event in same transaction
     * 
     * @param email User email
     * @param initialRole Initial realm role (e.g., "role_user")
     * @return UserResponse
     * @throws UserAlreadyExistsException if email exists (409)
     */
    @Transactional
    public UserResponse createUser(String email, String initialRole) {
        // Check for duplicate email first (fail fast before calling Keycloak)
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new UserAlreadyExistsException("User with email " + email + " already exists");
        }

        // Generate random initial password (user will be forced to change on first login)
        String initialPassword = generateSecurePassword();

        // Create Keycloak user with temporary password
        String keycloakUserId = keycloakAdminService.createUser(
                email,
                initialPassword,
                true, // temporary=true — user must set own password on first login (improvement over legacy)
                initialRole
        );

        // Insert local User row with id = Keycloak sub (never a locally-generated UUID)
        User user = new User(UUID.fromString(keycloakUserId), email, null);
        userRepository.save(user);

        // Write outbox event (same transaction as User insert)
        String payload = buildEventPayload(Map.of(
                "id", keycloakUserId,
                "email", email,
                "initial_role", initialRole
        ));
        OutboxEvent event = new OutboxEvent(
                "user",
                user.getId(),
                "user.events",
                "user.created",
                payload
        );
        outboxEventRepository.save(event);

        // Fetch roles from Keycloak for response
        return toUserResponse(user);
    }

    /**
     * Gets a user by ID (admin-or-self — enforced at controller layer).
     */
    public UserResponse getUser(UUID targetId) {
        User user = userRepository.findById(targetId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + targetId));
        return toUserResponse(user);
    }

    /**
     * Updates a user as admin (admin-or-self — enforced at controller layer).
     * Writes user.updated outbox event in same transaction.
     */
    @Transactional
    public UserResponse updateUserAsAdmin(UUID targetId, UserAdminUpdateRequest request) {
        User user = userRepository.findById(targetId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + targetId));

        user.setDisplayName(request.displayName());
        userRepository.save(user);

        // Write outbox event
        String payload = buildEventPayload(Map.of(
                "id", user.getId().toString(),
                "email", user.getEmail(),
                "display_name", Optional.ofNullable(user.getDisplayName()).orElse("")
        ));
        OutboxEvent event = new OutboxEvent(
                "user",
                user.getId(),
                "user.events",
                "user.updated",
                payload
        );
        outboxEventRepository.save(event);

        return toUserResponse(user);
    }

    /**
     * Gets the authenticated user's own profile.
     * By construction this can NEVER target another user's row (id from JWT, not path param).
     */
    public UserResponse getOwnProfile() {
        UUID userId = currentUserProvider.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        return toUserResponse(user);
    }

    /**
     * Updates the authenticated user's own profile.
     * Request type (UserSelfUpdateRequest) structurally prevents password/role changes.
     * Writes user.updated outbox event in same transaction.
     */
    @Transactional
    public UserResponse updateOwnProfile(UserSelfUpdateRequest request) {
        UUID userId = currentUserProvider.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        user.setDisplayName(request.displayName());
        userRepository.save(user);

        // Write outbox event
        String payload = buildEventPayload(Map.of(
                "id", user.getId().toString(),
                "email", user.getEmail(),
                "display_name", Optional.ofNullable(user.getDisplayName()).orElse("")
        ));
        OutboxEvent event = new OutboxEvent(
                "user",
                user.getId(),
                "user.events",
                "user.updated",
                payload
        );
        outboxEventRepository.save(event);

        return toUserResponse(user);
    }

    /**
     * Assigns roles to a user (admin only — enforced at controller layer).
     * Replaces the full role set in Keycloak, then writes role.assigned outbox event.
     * 
     * Note: roles live in Keycloak, not the local users table, so the outbox write
     * stands alone in its own transaction (no co-located business-row change).
     */
    @Transactional
    public UserResponse assignRoles(UUID targetId, List<String> roles) {
        User user = userRepository.findById(targetId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + targetId));

        // Update roles in Keycloak (replace full set)
        keycloakAdminService.updateUserRealmRoles(targetId.toString(), roles);

        // Write outbox event (no local DB row change, just the event)
        String payload = buildEventPayload(Map.of(
                "user_id", targetId.toString(),
                "roles", roles
        ));
        OutboxEvent event = new OutboxEvent(
                "user",
                targetId,
                "user.events",
                "role.assigned",
                payload
        );
        outboxEventRepository.save(event);

        return toUserResponse(user);
    }

    /**
     * Converts User entity to UserResponse DTO.
     * Fetches roles from CurrentUserProvider (reads from JWT) for the response.
     * 
     * IMPORTANT: This assumes the caller's roles are being used for the response.
     * For admin listing users, this will need to be enhanced to fetch each user's
     * roles from Keycloak. For now (MVP), returns empty roles list when building
     * responses for other users.
     */
    private UserResponse toUserResponse(User user) {
        // For self-responses, include roles from JWT
        List<String> roles = new ArrayList<>();
        try {
            UUID currentUserId = currentUserProvider.getCurrentUserId();
            if (currentUserId.equals(user.getId())) {
                roles = new ArrayList<>(currentUserProvider.getCurrentRoles());
            }
        } catch (Exception e) {
            // Not in authenticated context or not self — return empty roles
            // TODO: For admin user-listing, fetch roles from Keycloak per user
        }

        return new UserResponse(
                user.getId().toString(),
                user.getEmail(),
                user.getDisplayName(),
                roles,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    /**
     * Builds JSON payload for outbox events.
     */
    private String buildEventPayload(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event payload", e);
        }
    }

    /**
     * Generates a secure random password for initial user setup.
     * 16 characters, alphanumeric + symbols.
     */
    private String generateSecurePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            password.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        return password.toString();
    }
}

/**
 * User not found — 404 NOT FOUND
 */
class UserNotFoundException extends ApiException {
    public UserNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", message);
    }
}

/**
 * User already exists (email conflict) — 409 CONFLICT
 * Declared here for service layer; also used in KeycloakAdminService
 */
class UserAlreadyExistsException extends ApiException {
    public UserAlreadyExistsException(String message) {
        super(HttpStatus.CONFLICT, "USER_ALREADY_EXISTS", message);
    }
}
