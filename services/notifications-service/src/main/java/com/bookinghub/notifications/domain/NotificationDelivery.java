package com.bookinghub.notifications.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapping 1:1 to the already-applied notification_deliveries table (V1 migration).
 *
 * Does NOT re-specify or alter the DDL — this entity just maps to the existing schema.
 * The table was created by V1__init_schema.sql which includes the trigger-maintained
 * updated_at column and the idempotency_key UNIQUE constraint.
 *
 * Status values: 'pending' | 'sent' | 'retrying' | 'dead_lettered'
 * Event types: 'booking.created' | 'booking.approved' | 'booking.denied' | 'password.reset.requested'
 *
 * Plan 06-02's consumer uses save() inside a try/catch for DataIntegrityViolationException
 * as the idempotency boundary (idempotency_key UNIQUE constraint). Its controller uses
 * the Specification-based filter for delivery-status/dead-letter queries.
 */
@Entity
@Table(name = "notification_deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "idempotency_key", unique = true, nullable = false)
    private String idempotencyKey;

    @Column(name = "event_type", nullable = false)
    private String eventType;   // 'booking.created' | 'booking.approved' | 'booking.denied' | 'password.reset.requested'

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "status", nullable = false)
    private String status;      // 'pending' | 'sent' | 'retrying' | 'dead_lettered'

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount;

    @Column(name = "last_attempted_at")
    private Instant lastAttemptedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;  // DB default (now()), read-only from JPA's side

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;  // trigger-maintained, read-only from JPA's side
}
