package com.bookinghub.customfield.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * OutboxEvent entity mapping to the outbox table created by
 * V2__add_outbox_and_required_flag.sql.
 *
 * Implements the transactional outbox pattern (TechArch Y3): every domain event
 * is written here in the SAME transaction as the business change, then relayed
 * to RabbitMQ by a scheduled polling publisher (plan 04-04/later), never published
 * directly during the request.
 */
@Entity
@Table(name = "outbox")
public class OutboxEvent {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 64)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "exchange", nullable = false, length = 100)
    private String exchange;

    @Column(name = "routing_key", nullable = false, length = 100)
    private String routingKey;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private UUID idempotencyKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Column(name = "status", nullable = false, length = 16)
    private String status = "pending";

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    protected OutboxEvent() {
        // JPA requires no-arg constructor
    }

    public OutboxEvent(String aggregateType, UUID aggregateId, String exchange,
                      String routingKey, String payload) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.payload = payload;
        this.idempotencyKey = UUID.randomUUID();
        this.status = "pending";
        this.attemptCount = 0;
        this.createdAt = Instant.now();
    }

    // Getters
    public UUID getId() { return id; }
    public String getAggregateType() { return aggregateType; }
    public UUID getAggregateId() { return aggregateId; }
    public String getExchange() { return exchange; }
    public String getRoutingKey() { return routingKey; }
    public UUID getIdempotencyKey() { return idempotencyKey; }
    public String getPayload() { return payload; }
    public String getStatus() { return status; }
    public int getAttemptCount() { return attemptCount; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPublishedAt() { return publishedAt; }

    // Setters for publishing state
    public void setStatus(String status) {
        this.status = status;
    }

    public void incrementAttemptCount() {
        this.attemptCount++;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }
}
