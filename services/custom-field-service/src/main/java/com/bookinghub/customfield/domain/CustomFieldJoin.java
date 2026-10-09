package com.bookinghub.customfield.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * CustomFieldJoin entity mapping to the custom_field_joins table.
 *
 * Thin association row between a CustomFieldTemplate and a CustomField. Uses
 * plain UUID fields (no @ManyToOne) — full JPA relationship mapping is
 * unnecessary ceremony here, consistent with Phase 3's PasswordResetToken.userId
 * precedent. No updatedAt — the V1 table has none.
 */
@Entity
@Table(name = "custom_field_joins")
public class CustomFieldJoin {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "custom_field_template_id", nullable = false)
    private UUID customFieldTemplateId;

    @Column(name = "custom_field_id", nullable = false)
    private UUID customFieldId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected CustomFieldJoin() {
        // JPA requires no-arg constructor
    }

    public CustomFieldJoin(UUID customFieldTemplateId, UUID customFieldId) {
        this.customFieldTemplateId = customFieldTemplateId;
        this.customFieldId = customFieldId;
        this.createdAt = Instant.now();
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getCustomFieldTemplateId() { return customFieldTemplateId; }
    public UUID getCustomFieldId() { return customFieldId; }
    public Instant getCreatedAt() { return createdAt; }
}
