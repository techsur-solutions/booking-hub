package com.bookinghub.userspermissions.controller;

import com.bookinghub.userspermissions.dto.LoginRequest;
import com.bookinghub.userspermissions.dto.LoginResponse;
import com.bookinghub.userspermissions.dto.PasswordResetRequestRequest;
import com.bookinghub.userspermissions.dto.PasswordResetCompleteRequest;
import com.bookinghub.userspermissions.dto.PasswordChangeRequest;
import com.bookinghub.userspermissions.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AuthController - F6 authentication endpoints.
 * 
 * Implements:
 * - POST /auth/login: Login with username/password, return tokens + roles
 * - POST /auth/logout: Logout (revoke Keycloak session server-side)
 * - POST /auth/password-reset/request: Request password reset link (generic 202 response)
 * - POST /auth/password-reset/complete: Complete password reset with token
 * - POST /auth/password-change: Self-service password change (requires current password)
 * 
 * Closes F0 Open Questions #17, #19, #20 in implementation, backed by AuthService.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /auth/login - Authenticate with username/password.
     * 
     * Public endpoint (no JWT required).
     * 
     * @param request LoginRequest with username, password, remember_me
     * @return 200 with LoginResponse (access_token, refresh_token, expires_in, roles)
     * @throws AuthInvalidCredentialsException if credentials invalid (401)
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(
                request.username(),
                request.password(),
                request.rememberMe()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * POST /auth/logout - Logout current user.
     * 
     * Authenticated endpoint (JWT required).
     * Revokes the user's Keycloak session server-side.
     * 
     * @return 204 No Content
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /auth/password-reset/request - Request password reset link.
     * 
     * Public endpoint (no JWT required).
     * 
     * F0 Open Question #20: Returns SAME generic 202 response regardless of whether
     * the email exists — no email-enumeration leak.
     * 
     * @param request PasswordResetRequestRequest with email
     * @return 202 Accepted with generic acknowledgment message
     */
    @PostMapping("/password-reset/request")
    public ResponseEntity<Map<String, String>> requestPasswordReset(
            @RequestBody PasswordResetRequestRequest request) {
        authService.requestPasswordReset(request.email());
        
        // Generic response (identical whether account exists or not)
        Map<String, String> response = Map.of(
                "message", "If an account exists for that address, a reset link has been sent."
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * POST /auth/password-reset/complete - Complete password reset with token.
     * 
     * Public endpoint (no JWT required).
     * 
     * F0 Open Question #19: Token expiry and used status are re-validated at THIS
     * submit moment, not trusting any earlier check.
     * 
     * @param request PasswordResetCompleteRequest with reset_token and new_password
     * @return 200 OK
     * @throws PasswordResetTokenInvalidException if token invalid/expired/used (400)
     * @throws PasswordPolicyViolationException if new password violates policy (400)
     */
    @PostMapping("/password-reset/complete")
    public ResponseEntity<Void> completePasswordReset(
            @RequestBody PasswordResetCompleteRequest request) {
        authService.completePasswordReset(request.resetToken(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    /**
     * POST /auth/password-change - Self-service password change.
     * 
     * Authenticated endpoint (JWT required).
     * Requires proving current password knowledge.
     * 
     * @param request PasswordChangeRequest with current_password and new_password
     * @return 200 OK
     * @throws PasswordChangeInvalidCurrentException if current password wrong (401)
     * @throws PasswordPolicyViolationException if new password violates policy (400)
     */
    @PostMapping("/password-change")
    public ResponseEntity<Void> changePassword(@RequestBody PasswordChangeRequest request) {
        authService.changePassword(request.currentPassword(), request.newPassword());
        return ResponseEntity.ok().build();
    }
}
