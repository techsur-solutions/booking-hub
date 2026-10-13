package com.bookinghub.auditlog.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapping 1:1 to audit_log_entries (V1 columns + V2 idempotency_key).
 *
 * Schema ownership:
 * - V1 (already applied): id, actor_id, occurred_at, entity_type, entity_id, action_type,
 *   before_values, after_values, created_at — plus REVOKE UPDATE, DELETE immutability grant
 * - V2 (this plan): idempotency_key UUID NULL + partial unique index
 *
 * Named decision (JdbcTypeCode SqlTypes.JSON for JSONB columns): same Hibernate 6
 * native-JSON pattern established in Phase 3/4/5 (booking-service's before/after values
 * pattern). Using SqlTypes.JSON maps to Postgres JSONB natively in Hibernate 6.
 *
 * Named decision (created_at insertable=false/updatable=false): created_at has a DB
 * DEFAULT now() in V1 — JPA must not attempt to set it on INSERT (it would conflict).
 * Same pattern as notification_deliveries.created_at in plan 06-01.
 *
 * Named decision (idempotency_key nullable in entity): the column is NULL-allowed in
 * the DB (additive migration can't add NOT NULL without a DEFAULT on a non-empty table
 * in a safe migration), but plan 06-04's consumer MUST always supply it for new rows.
 */
@Entity
@Table(name = "audit_log_entries")
public class AuditLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "before_values")
    private String beforeValues;   // nullable JSONB, Hibernate 6 native-JSON pattern

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "after_values")
    private String afterValues;    // nullable JSONB, same pattern

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;     // DB default now(), read-only from JPA's side

    @Column(name = "idempotency_key")
    private UUID idempotencyKey;   // V2 addition — partial unique index enforces uniqueness on non-null values

    // Default constructor required by JPA
    public AuditLogEntry() {}

    public UUID getId() {
        return id;
    }

    public UUID getActorId() {
        return actorId;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getBeforeValues() {
        return beforeValues;
    }

    public void setBeforeValues(String beforeValues) {
        this.beforeValues = beforeValues;
    }

    public String getAfterValues() {
        return afterValues;
    }

    public void setAfterValues(String afterValues) {
        this.afterValues = afterValues;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UUID getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(UUID idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
