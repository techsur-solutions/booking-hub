package com.bookinghub.userspermissions.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;

/**
 * Permission entity mapping to the permissions table (V1 DDL + V2 seed data).
 * Maps legacy permission flags to Keycloak roles with confirmation tracking.
 * 
 * The gated_actions JSONB column stores structured permission metadata.
 * Rows with confirmed=false MUST enforce deny-by-default in Tier-2 checks.
 */
@Entity
@Table(name = "permissions")
public class Permission {
    
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private UUID id;
    
    @Column(name = "legacy_flag", nullable = false, unique = true)
    private String legacyFlag;
    
    @Column(name = "keycloak_role", nullable = false)
    private String keycloakRole;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "gated_actions", columnDefinition = "jsonb", nullable = false)
    private String gatedActions;
    
    @Column(name = "confirmed", nullable = false)
    private boolean confirmed;
    
    protected Permission() {
        // JPA requires no-arg constructor
    }
    
    public Permission(String legacyFlag, String keycloakRole, String gatedActions, boolean confirmed) {
        this.legacyFlag = legacyFlag;
        this.keycloakRole = keycloakRole;
        this.gatedActions = gatedActions;
        this.confirmed = confirmed;
    }
    
    // Getters
    public UUID getId() { return id; }
    public String getLegacyFlag() { return legacyFlag; }
    public String getKeycloakRole() { return keycloakRole; }
    public String getGatedActions() { return gatedActions; }
    public boolean isConfirmed() { return confirmed; }
    
    // Setters for mutable fields
    public void setKeycloakRole(String keycloakRole) {
        this.keycloakRole = keycloakRole;
    }
    
    public void setGatedActions(String gatedActions) {
        this.gatedActions = gatedActions;
    }
    
    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
}
