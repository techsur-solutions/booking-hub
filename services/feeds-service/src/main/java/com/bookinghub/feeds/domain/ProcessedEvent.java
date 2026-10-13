package com.bookinghub.feeds.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity over the V2-added processed_events dedup table.
 *
 * Plan 07-02's consumer records each successfully-processed event's
 * idempotency_key here; its lookup-first check
 * (ProcessedEventRepository.existsByIdempotencyKey) skips redelivered events,
 * and the UUID primary key is the race-safe backstop (a concurrent redelivery
 * that passes the lookup still fails on insert).
 *
 * processed_at is DB-default-managed, so it is read-only from JPA.
 */
@Entity
@Table(name = "processed_events")
@Getter
@Setter
@NoArgsConstructor
public class ProcessedEvent {

    @Id
    @Column(name = "idempotency_key")
    private UUID idempotencyKey;

    @Column(name = "processed_at", insertable = false, updatable = false)
    private Instant processedAt;
}
