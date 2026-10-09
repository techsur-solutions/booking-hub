package com.bookinghub.customfield.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * CustomFieldTemplate entity mapping to the custom_field_templates table.
 *
 * `contextId` is nullable — NULL means global (applies to every context), per
 * TechArch's own column comment on the V1 DDL.
 *
 * NOTE: No `deletedAt` field — the V1 schema's custom_field_templates table has
 * no deleted_at column (unlike custom_fields), so Template deletion in plan 04-04
 * is necessarily a hard delete, not soft.
 */
@Entity
@Table(name = "custom_field_templates")
public class CustomFieldTemplate {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "context_id")
    private UUID contextId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected CustomFieldTemplate() {
        // JPA requires no-arg constructor
    }

    public CustomFieldTemplate(String name, UUID contextId) {
        this.name = name;
        this.contextId = contextId;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Getters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public UUID getContextId() { return contextId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // Setters for mutable fields
    public void setName(String name) {
        this.name = name;
        this.updatedAt = Instant.now();
    }

    public void setContextId(UUID contextId) {
        this.contextId = contextId;
        this.updatedAt = Instant.now();
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
