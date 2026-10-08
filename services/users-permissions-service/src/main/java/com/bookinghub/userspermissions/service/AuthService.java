package com.bookinghub.userspermissions.service;

import com.bookinghub.userspermissions.domain.OutboxEvent;
import com.bookinghub.userspermissions.domain.PasswordResetToken;
import com.bookinghub.userspermissions.domain.User;
import com.bookinghub.userspermissions.dto.LoginRequest;
import com.bookinghub.userspermissions.dto.LoginResponse;
import com.bookinghub.userspermissions.dto.PasswordResetRequestRequest;
import com.bookinghub.userspermissions.dto.PasswordResetCompleteRequest;
import com.bookinghub.userspermissions.dto.PasswordChangeRequest;
import com.bookinghub.userspermissions.error.ApiException;
import com.bookinghub.userspermissions.keycloak.KeycloakAdminService;
import com.bookinghub.userspermissions.keycloak.KeycloakTokenClient;
import com.bookinghub.userspermissions.repository.OutboxEventRepository;
import com.bookinghub.userspermissions.repository.PasswordResetTokenRepository;
import com.bookinghub.userspermissions.repository.UserRepository;
import com.bookinghub.userspermissions.security.CurrentUserProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

/**
 * Authentication service implementing F6 auth endpoints:
 * login, logout, password-reset request/complete, self-service password change.
 * 
 * Closes three F0 open questions in code:
 * - F0 #17: Session timeout values (30 min idle, 10h max) — fresh decision, not parity
 * - F0 #19: Password-reset token expiry re-check at submit time (not just page load)
 * - F0 #20: Generic password-reset-request response regardless of email existence
 */
@Service
public class AuthService {

    private final KeycloakTokenClient keycloakTokenClient;
    private final KeycloakAdminService keycloakAdminService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final CurrentUserProvider currentUserProvider;
    private final JwtDecoder jwtDecoder;
    private final ObjectMapper objectMapper;

    public AuthService(
            KeycloakTokenClient keycloakTokenClient,
            KeycloakAdminService keycloakAdminService,
            PasswordResetTokenRepository passwordResetTokenRepository,
            UserRepository userRepository,
            OutboxEventRepository outboxEventRepository,
            CurrentUserProvider currentUserProvider,
            JwtDecoder jwtDecoder,
            ObjectMapper objectMapper) {
        this.keycloakTokenClient = keycloakTokenClient;
        this.keycloakAdminService = keycloakAdminService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userRepository = userRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.currentUserProvider = currentUserProvider;
        this.jwtDecoder = jwtDecoder;
        this.objectMapper = objectMapper;
    }

    /**
     * Login with username/password, returning tokens and roles.
     * 
     * Uses Keycloak direct-grant flow (Resource Owner Password Credentials).
     * Routes to standard or remember-me client based on rememberMe flag.
     * 
     * @param username Email (username)
     * @param password User's password
     * @param rememberMe If true, extends session to 30 days; if false, 10 hours
     * @return LoginResponse with access_token, refresh_token, expires_in, roles
     * @throws AuthInvalidCredentialsException if credentials are invalid (401)
     */
    public LoginResponse login(String username, String password, Boolean rememberMe) {
        boolean remember = rememberMe != null && rememberMe;
        
        // Perform direct-grant login via Keycloak
        KeycloakTokenClient.TokenResponse tokenResponse = 
                keycloakTokenClient.directGrantLogin(username, password, remember);
        
        // Decode the access token to extract roles from realm_access.roles claim
        Jwt jwt = jwtDecoder.decode(tokenResponse.accessToken());
        List<String> roles = extractRolesFromJwt(jwt);
        
        return new LoginResponse(
                tokenResponse.accessToken(),
                tokenResponse.refreshToken(),
                tokenResponse.expiresIn(),
                roles
        );
    }

    /**
     * Logout the currently authenticated user.
     * 
     * Revokes the user's Keycloak session server-side (not merely a client-side token discard).
     */
    @Transactional
    public void logout() {
        UUID userId = currentUserProvider.getCurrentUserId();
        keycloakAdminService.logoutUser(userId.toString());
    }

    /**
     * Request a password reset link.
     * 
     * F0 Open Question #20: Email-enumeration inconsistency — CLOSED by making this response
     * generic regardless of account existence. Both found and not-found cases return 202
     * with identical response body.
     * 
     * If account exists: generates token, writes outbox event.
     * If account does NOT exist: no-op (no row, no event), but response is identical.
     * 
     * @param email Email address to send reset link to
     */
    @Transactional
    public void requestPasswordReset(String email) {
        // Look up user by email (case-insensitive)
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Generate raw UUID token
            String rawToken = UUID.randomUUID().toString();
            
            // SHA-256 hash for storage (only hash is persisted)
            String tokenHash = sha256Hash(rawToken);
            
            // Token expires in 2 hours (F0-confirmed legacy window)
            Instant expiresAt = Instant.now().plusSeconds(2 * 60 * 60);
            
            // Insert password_reset_token row
            PasswordResetToken resetToken = new PasswordResetToken(user.getId(), tokenHash, expiresAt);
            passwordResetTokenRepository.save(resetToken);
            
            // Create outbox event for notification pipeline (contains raw token for email link)
            Map<String, Object> payload = new HashMap<>();
            payload.put("user_id", user.getId().toString());
            payload.put("email", user.getEmail());
            payload.put("reset_token", rawToken); // Raw token (only place it exists outside hash)
            
            OutboxEvent event = new OutboxEvent(
                    "user",
                    user.getId(),
                    "user.events",
                    "password.reset.requested",
                    toJson(payload)
            );
            outboxEventRepository.save(event);
        }
        
        // If user NOT found: do nothing (no row, no event)
        // But caller receives the same 202 response regardless
    }

    /**
     * Complete a password reset by validating token and setting new password.
     * 
     * F0 Open Question #19: Expiry re-check gap — CLOSED by re-validating expiry, used status,
     * and existence at THIS submit point, not trusting any earlier/cached check.
     * 
     * @param resetToken Raw token from email link
     * @param newPassword New password to set
     * @throws PasswordResetTokenInvalidException if token is invalid, expired, or already used (400)
     * @throws PasswordPolicyViolationException if new password violates Keycloak policy (400)
     */
    @Transactional
    public void completePasswordReset(String resetToken, String newPassword) {
        // SHA-256 hash the incoming token to look up
        String tokenHash = sha256Hash(resetToken);
        
        // Look up token by hash
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByTokenHash(tokenHash);
        
        if (tokenOpt.isEmpty()) {
            throw new PasswordResetTokenInvalidException("Password reset token is invalid");
        }
        
        PasswordResetToken token = tokenOpt.get();
        
        // Re-check expiry at THIS moment (not trusting any earlier check)
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new PasswordResetTokenInvalidException("Password reset token has expired");
        }
        
        // Re-check used status
        if (token.getUsedAt() != null) {
            throw new PasswordResetTokenInvalidException("Password reset token has already been used");
        }
        
        // Token is valid — reset password via Keycloak Admin API
        // temporary=false: user should NOT be forced to change password again after successful reset
        keycloakAdminService.resetPassword(token.getUserId().toString(), newPassword, false);
        
        // Mark token as used (single-use enforcement)
        token.markAsUsed();
        passwordResetTokenRepository.save(token);
    }

    /**
     * Change the authenticated user's password.
     * 
     * Requires current password verification (proving user knows current password).
     * 
     * @param currentPassword Current password (must verify successfully)
     * @param newPassword New password to set
     * @throws PasswordChangeInvalidCurrentException if current password is wrong (401)
     * @throws PasswordPolicyViolationException if new password violates policy (400)
     */
    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        UUID userId = currentUserProvider.getCurrentUserId();
        
        // Look up user's email (needed for direct-grant verification call)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
        
        // Verify current password via direct-grant token attempt
        boolean currentPasswordValid = keycloakTokenClient.verifyCurrentPassword(user.getEmail(), currentPassword);
        
        if (!currentPasswordValid) {
            // DISTINCT error code from AUTH_INVALID_CREDENTIALS per FRD Y1-api.md
            throw new PasswordChangeInvalidCurrentException("Current password is incorrect");
        }
        
        // Current password verified — reset to new password
        // temporary=false: no forced password change required after this self-service change
        keycloakAdminService.resetPassword(userId.toString(), newPassword, false);
    }

    /**
     * Extracts realm roles from JWT's realm_access.roles claim.
     */
    @SuppressWarnings("unchecked")
    private List<String> extractRolesFromJwt(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return List.of();
        }
        
        Object rolesObj = realmAccess.get("roles");
        if (rolesObj instanceof List) {
            return new ArrayList<>((List<String>) rolesObj);
        }
        
        return List.of();
    }

    /**
     * SHA-256 hash utility for token hashing.
     */
    private String sha256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Converts object to JSON string for outbox payload.
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize to JSON", e);
        }
    }
}

/**
 * Authentication failed: invalid credentials — 401 UNAUTHORIZED
 */
class AuthInvalidCredentialsException extends ApiException {
    public AuthInvalidCredentialsException(String message) {
        super(org.springframework.http.HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", message);
    }
}

/**
 * Password reset token is invalid, expired, or already used — 400 BAD REQUEST
 */
class PasswordResetTokenInvalidException extends ApiException {
    public PasswordResetTokenInvalidException(String message) {
        super(org.springframework.http.HttpStatus.BAD_REQUEST, "PASSWORD_RESET_TOKEN_INVALID", message);
    }
}

/**
 * Password change failed: current password incorrect — 401 UNAUTHORIZED
 */
class PasswordChangeInvalidCurrentException extends ApiException {
    public PasswordChangeInvalidCurrentException(String message) {
        super(org.springframework.http.HttpStatus.UNAUTHORIZED, "PASSWORD_CHANGE_INVALID_CURRENT", message);
    }
}

/**
 * New password violates policy (length, complexity) — 400 BAD REQUEST
 */
class PasswordPolicyViolationException extends ApiException {
    public PasswordPolicyViolationException(String message) {
        super(org.springframework.http.HttpStatus.BAD_REQUEST, "PASSWORD_POLICY_VIOLATION", message);
    }
}
