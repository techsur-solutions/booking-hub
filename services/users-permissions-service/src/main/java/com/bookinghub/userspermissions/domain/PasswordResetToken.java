package com.bookinghub.userspermissions.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * PasswordResetToken entity mapping to the password_reset_tokens table created by V2.
 * 
 * Closes F0 Open Question #19: stores hashed tokens with expires_at/used_at validation
 * at submission time (POST /auth/password-reset/complete), not just at form-load time
 * like the legacy system. Raw token exists only in the emailed link, never stored.
 * 2-hour expiry window preserved from legacy.
 */
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;
    
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    
    @Column(name = "used_at")
    private Instant usedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    protected PasswordResetToken() {
        // JPA requires no-arg constructor
    }
    
    public PasswordResetToken(UUID userId, String tokenHash, Instant expiresAt) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }
    
    // Getters
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getTokenHash() { return tokenHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getUsedAt() { return usedAt; }
    public Instant getCreatedAt() { return createdAt; }
    
    // Mark token as used (single-use enforcement)
    public void markAsUsed() {
        this.usedAt = Instant.now();
    }
    
    // Check if token is expired
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
    
    // Check if token is already used
    public boolean isUsed() {
        return usedAt != null;
    }
}
