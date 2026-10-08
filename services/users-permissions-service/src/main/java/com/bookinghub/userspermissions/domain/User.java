package com.bookinghub.userspermissions.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * User entity mapping to the users table created by V1__init_schema.sql.
 * 
 * CRITICAL: id field has NO @GeneratedValue — it MUST be explicitly set to the
 * Keycloak sub claim at first-sync/creation time (per TechArch §2.2 and Phase 2
 * explicit decision). The V1 DDL deliberately omitted a DEFAULT for this reason.
 * No password/hash/salt fields exist — authentication is delegated to Keycloak.
 */
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;  // NO @GeneratedValue — explicitly set from Keycloak sub claim
    
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @Column(name = "display_name")
    private String displayName;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "deleted_at")
    private Instant deletedAt;
    
    protected User() {
        // JPA requires no-arg constructor
    }
    
    public User(UUID id, String email, String displayName) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    // Getters
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getDeletedAt() { return deletedAt; }
    
    // Setters for mutable fields
    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = Instant.now();
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        this.updatedAt = Instant.now();
    }
    
    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
        this.updatedAt = Instant.now();
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
