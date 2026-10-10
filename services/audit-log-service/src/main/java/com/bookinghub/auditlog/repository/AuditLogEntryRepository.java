package com.bookinghub.auditlog.repository;

import com.bookinghub.auditlog.domain.AuditLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for AuditLogEntry.
 *
 * Extends both JpaRepository (basic CRUD) and JpaSpecificationExecutor (for
 * plan 06-04's controller to build optional-filter queries on entity_type, action_type,
 * actor_id, occurred_at ranges, etc.).
 *
 * findByIdempotencyKey: plan 06-04's 7-queue consumer uses this for its lookup-first
 * idempotency check — if a row with this key already exists, skip processing the
 * redelivered message. Same pattern as notifications-service (plan 06-01's precedent).
 *
 * The audit_log_entries table has a partial unique index on idempotency_key WHERE
 * idempotency_key IS NOT NULL (V2 migration), so the DB also catches any concurrent
 * duplicate inserts as DataIntegrityViolationException — the application-level
 * lookup-first is the happy-path optimization, the DB constraint is the safety net.
 */
public interface AuditLogEntryRepository extends JpaRepository<AuditLogEntry, UUID>,
        JpaSpecificationExecutor<AuditLogEntry> {

    /**
     * Looks up an existing entry by its idempotency key.
     * Used by plan 06-04's RabbitMQ consumer to detect and skip redelivered events.
     *
     * @param idempotencyKey UUID from the event's idempotency header
     * @return existing entry if already processed, empty if not yet seen
     */
    Optional<AuditLogEntry> findByIdempotencyKey(UUID idempotencyKey);
}
