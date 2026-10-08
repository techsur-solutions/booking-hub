package com.bookinghub.userspermissions.controller;

import com.bookinghub.userspermissions.dto.*;
import com.bookinghub.userspermissions.error.ActionForbiddenException;
import com.bookinghub.userspermissions.security.CurrentUserProvider;
import com.bookinghub.userspermissions.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * User controller implementing F6's admin-facing and self-service user management:
 * - Account creation (admin only): POST /users
 * - User listing (admin only): GET /users
 * - Read/update with admin-or-self guard: GET/PUT /users/{id}
 * - Role assignment (admin only): PUT /users/{id}/roles
 * - Self-service profile: GET/PUT /users/me
 * 
 * All endpoints require Tier-2 JWT validation (enforced by SecurityConfig).
 * Fine-grained authorization (admin-vs-self) is enforced via @PreAuthorize
 * and method-level checks using CurrentUserProvider.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;

    public UserController(UserService userService, CurrentUserProvider currentUserProvider) {
        this.userService = userService;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Lists all users (admin only).
     * GET /users
     */
    @GetMapping
    @PreAuthorize("hasRole('role_user_admin')")
    public List<UserResponse> listUsers() {
        return userService.listUsers();
    }

    /**
     * Creates a new user account (admin only).
     * POST /users
     * 
     * Provisions Keycloak identity with auto-generated initial password (temporary=true),
     * inserts local user row with id = Keycloak sub, publishes user.created outbox event.
     */
    @PostMapping
    @PreAuthorize("hasRole('role_user_admin')")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@RequestBody UserCreateRequest request) {
        return userService.createUser(request.email(), request.initialRole());
    }

    /**
     * Gets a user by ID (admin or self).
     * GET /users/{id}
     * 
     * Admin can read any user; non-admin can only read their own record.
     */
    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable String id) {
        UUID targetId = UUID.fromString(id);
        
        // Admin-or-self check
        if (!currentUserProvider.hasRole("role_user_admin") && 
            !currentUserProvider.isSelf(targetId)) {
            throw new ActionForbiddenException("Cannot access another user's record");
        }
        
        return userService.getUser(targetId);
    }

    /**
     * Updates a user by ID (admin or self).
     * PUT /users/{id}
     * 
     * Admin can edit any user's profile; non-admin can only edit their own.
     * Request DTO (UserAdminUpdateRequest) has no password/role fields.
     */
    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable String id, @RequestBody UserAdminUpdateRequest request) {
        UUID targetId = UUID.fromString(id);
        
        // Admin-or-self check
        if (!currentUserProvider.hasRole("role_user_admin") && 
            !currentUserProvider.isSelf(targetId)) {
            throw new ActionForbiddenException("Cannot update another user's record");
        }
        
        return userService.updateUserAsAdmin(targetId, request);
    }

    /**
     * Assigns roles to a user (admin only, never self).
     * PUT /users/{id}/roles
     * 
     * Replaces the user's full role set in Keycloak, publishes role.assigned outbox event.
     */
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('role_user_admin')")
    public UserResponse assignRoles(@PathVariable String id, @RequestBody UserRolesUpdateRequest request) {
        UUID targetId = UUID.fromString(id);
        return userService.assignRoles(targetId, request.roles());
    }

    /**
     * Gets the authenticated user's own profile.
     * GET /users/me
     * 
     * Always returns the caller's own record (id from JWT, never path param).
     */
    @GetMapping("/me")
    public UserResponse getOwnProfile() {
        return userService.getOwnProfile();
    }

    /**
     * Updates the authenticated user's own profile.
     * PUT /users/me
     * 
     * Request type (UserSelfUpdateRequest) structurally prevents password/role changes
     * via compile-time guard (no such fields exist on the DTO).
     */
    @PutMapping("/me")
    public UserResponse updateOwnProfile(@RequestBody UserSelfUpdateRequest request) {
        return userService.updateOwnProfile(request);
    }
}
