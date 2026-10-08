package com.bookinghub.customfield.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * CustomField entity mapping to the custom_fields table (V1 DDL + V2's additive
 * `required` column).
 *
 * `fieldType` is deliberately a plain String, NOT a JPA enum type — application-level
 * validation of the allowed value set happens in plan 04-04's service layer, not at
 * the entity/DB level, matching legacy's own confirmed "UI-only enum, no DB CHECK
 * constraint" behavior per F0 findings/03-custom-fields.md.
 *
 * `required` is storage-only in this service: this service never reads or enforces
 * this flag itself (named decision, see V2 migration comment) — Phase 5's
 * booking-service is responsible for enforcement at submission time.
 */
@Entity
@Table(name = "custom_fields")
public class CustomField {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "field_type", nullable = false, length = 32)
    private String fieldType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options", columnDefinition = "jsonb")
    private List<String> options;

    @Column(name = "required", nullable = false)
    private boolean required;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected CustomField() {
        // JPA requires no-arg constructor
    }

    public CustomField(String label, String fieldType, List<String> options, boolean required) {
        this.label = label;
        this.fieldType = fieldType;
        this.options = options;
        this.required = required;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Getters
    public UUID getId() { return id; }
    public String getLabel() { return label; }
    public String getFieldType() { return fieldType; }
    public List<String> getOptions() { return options; }
    public boolean isRequired() { return required; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getDeletedAt() { return deletedAt; }

    // Setters for mutable fields
    public void setLabel(String label) {
        this.label = label;
        this.updatedAt = Instant.now();
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
        this.updatedAt = Instant.now();
    }

    public void setOptions(List<String> options) {
        this.options = options;
        this.updatedAt = Instant.now();
    }

    public void setRequired(boolean required) {
        this.required = required;
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
