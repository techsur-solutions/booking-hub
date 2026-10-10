package com.bookinghub.notifications.repository;

import com.bookinghub.notifications.domain.NotificationDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
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
}
