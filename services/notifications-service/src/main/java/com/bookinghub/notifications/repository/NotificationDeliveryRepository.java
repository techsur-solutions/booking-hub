package com.bookinghub.notifications.repository;

import com.bookinghub.notifications.domain.NotificationDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for NotificationDelivery entities.
 *
 * Extends both JpaRepository and JpaSpecificationExecutor:
 * - JpaRepository: save(), findById(), etc. — plan 06-02's consumer uses save()
 *   inside a try/catch for DataIntegrityViolationException as the idempotency
 *   boundary (idempotency_key UNIQUE constraint).
 * - JpaSpecificationExecutor: Specification-based dynamic filtering — plan 06-02's
 *   controller uses this to build the optional event_type/status filters for
 *   GET /notifications/delivery-status and the status='dead_lettered' filter
 *   for GET /notifications/dead-letter.
 *
 * No extra derived-query methods needed beyond what both interfaces provide.
 */
public interface NotificationDeliveryRepository
        extends JpaRepository<NotificationDelivery, UUID>, JpaSpecificationExecutor<NotificationDelivery> {

    /**
     * Find deliveries by status. Used by plan 06-02's controller for the
     * dead-letter endpoint (status = 'dead_lettered') and delivery-status
     * endpoint (optional status filter).
     */
    List<NotificationDelivery> findByStatus(String status);

    /**
     * Look up a delivery row by its idempotency key — the consumer's authoritative
     * idempotency boundary.
     *
     * <p>{@code NotificationConsumerService} calls this BEFORE inserting anything, so it
     * can distinguish three cases that must behave very differently: an already-{@code sent}
     * message (no-op, never send twice), a {@code pending}/{@code retrying} row belonging
     * to the retry of this same message (continue the send attempt without re-inserting),
     * and a genuinely new message (insert fresh). The table's UNIQUE constraint on
     * {@code idempotency_key} remains the race-safe backstop for two truly concurrent
     * deliveries, which no lookup can close on its own.
     *
     * <p>{@code DatabaseTrackingMessageRecoverer} also uses this to find the row it must
     * mark {@code dead_lettered} before the message is rejected to the DLQ.
     */
    Optional<NotificationDelivery> findByIdempotencyKey(String idempotencyKey);
}
