package com.bookinghub.auditlog.repository;

import com.bookinghub.auditlog.domain.AuditLogEntry;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Spring Data Specifications for building dynamic filter predicates on AuditLogEntry.
 *
 * Used by AuditLogController to construct optional WHERE clauses for:
 * - entity_type: filter by entity type (e.g., "booking", "location")
 * - entity_id: filter by specific entity ID
 * - actor: filter by actor ID
 * - occurredBetween: filter by date range (inclusive on both ends)
 *
 * Specifications are chained via Specification.where(...).and(...).and(...) in the
 * controller, so absent filters produce null specifications that are safely ignored.
 */
public class AuditLogSpecifications {

    /**
     * Returns a Specification matching entries with the given entity_type.
     * Returns null (no-op) if entityType is null or blank.
     */
    public static Specification<AuditLogEntry> entityType(String entityType) {
        if (entityType == null || entityType.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("entityType"), entityType);
    }

    /**
     * Returns a Specification matching entries with the given entity_id.
     * Returns null (no-op) if entityId is null.
     */
    public static Specification<AuditLogEntry> entityId(UUID entityId) {
        if (entityId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("entityId"), entityId);
    }

    /**
     * Returns a Specification matching entries with the given actor_id.
     * Returns null (no-op) if actorId is null.
     */
    public static Specification<AuditLogEntry> actor(UUID actorId) {
        if (actorId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("actorId"), actorId);
    }

    /**
     * Returns a Specification matching entries with occurred_at between the given boundaries (inclusive).
     * Returns null (no-op) if both from and to are null.
     *
     * @param from the start of the range (inclusive), or null for no lower bound
     * @param to the end of the range (inclusive), or null for no upper bound
     */
    public static Specification<AuditLogEntry> occurredBetween(Instant from, Instant to) {
        if (from == null && to == null) {
            return null;
        }

        return (root, query, cb) -> {
            if (from != null && to != null) {
                return cb.between(root.get("occurredAt"), from, to);
            } else if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("occurredAt"), from);
            } else {
                return cb.lessThanOrEqualTo(root.get("occurredAt"), to);
            }
        };
    }
}
