package com.bookinghub.booking.repository;

import com.bookinghub.booking.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for OutboxEvent entities.
 *
 * findTop100ByStatusOrderByCreatedAtAsc: the OutboxPublisher's polling query —
 * reads up to 100 pending events ordered by creation time (oldest first) for
 * ordered, at-most-once-per-poll relay to RabbitMQ.
 */
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(String status);
}
