package com.bookinghub.feeds.repository;

import com.bookinghub.feeds.domain.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data repository over the processed_events dedup table.
 *
 * existsByIdempotencyKey is plan 07-02's consumer lookup-first idempotency
 * check: before processing an inbound event it asks this, and skips the event
 * if the key has already been recorded.
 */
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {

    boolean existsByIdempotencyKey(UUID idempotencyKey);
}
