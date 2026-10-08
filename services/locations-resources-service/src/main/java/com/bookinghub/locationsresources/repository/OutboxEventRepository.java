package com.bookinghub.locationsresources.repository;

import com.bookinghub.locationsresources.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for OutboxEvent entity.
 *
 * Provides query for the scheduled polling publisher to batch-process pending
 * events (same pattern as Phase 3 plan 03-05's OutboxPublisher).
 */
@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * Find up to 100 pending events ordered by creation time (oldest first).
     * Used by the scheduled polling publisher to batch-relay events to RabbitMQ.
     */
    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(String status);
}
