package com.bookinghub.userspermissions.repository;

import com.bookinghub.userspermissions.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for OutboxEvent entity.
 * 
 * Provides query for polling publisher (plan 03-05) to batch-process pending events.
 */
@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    
    /**
     * Find up to 100 pending events ordered by creation time (oldest first).
     * Used by the scheduled polling publisher to batch-relay events to RabbitMQ.
     */
    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(String status);
}
