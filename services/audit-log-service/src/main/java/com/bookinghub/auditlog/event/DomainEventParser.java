package com.bookinghub.auditlog.event;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.UUID;

/**
 * Parses an inbound RabbitMQ domain event into an InboundDomainEvent value object.
 *
 * Design decisions (per plan 06-04's named decision block):
 *
 * entity_type / action_type:
 *   Derived from the routing key by splitting on the LAST dot:
 *   "booking.approved" → entity_type="booking", action_type="approved"
 *   "template.updated" → entity_type="template", action_type="updated"
 *   This works uniformly for all 23 routing keys across all 7 queues.
 *
 * entity_id:
 *   body.get("id") — the overwhelmingly likely shape (every entity serializes its PK as "id").
 *   If absent, logs a warning and returns NIL_UUID — never fails the write.
 *
 * actor_id (fallback chain):
 *   1. body.get("actor_id")
 *   2. body.get("performed_by")
 *   3. action-specific field: "approved_by" for "approved", "denied_by" for "denied",
 *      then "owner_id" / "updated_by" / "created_by" (in that order)
 *   4. NIL_UUID — documented "system/unknown actor" placeholder
 *
 * occurred_at (fallback chain):
 *   1. body.get("occurred_at")
 *   2. body.get("timestamp")
 *   3. "{action_type}_at" (e.g. "approved_at", "updated_at", "created_at", "deleted_at")
 *   4. Instant.now() — message receipt time as final fallback
 *
 * This parser NEVER throws for missing or malformed data — it degrades gracefully
 * per F11 §Validation's "never fail the write because they're absent" philosophy.
 */
@Component
public class DomainEventParser {

    private static final Logger log = LoggerFactory.getLogger(DomainEventParser.class);

    /** Nil UUID — documented "unknown/system" placeholder for missing entity/actor IDs. */
    static final UUID NIL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    /**
     * Parses a routing key + JSON body into an InboundDomainEvent.
     * Never throws — all missing fields degrade to documented placeholders.
     *
     * @param routingKey  RabbitMQ routing key (e.g. "booking.approved", "role.assigned")
     * @param body        Parsed JSON body of the message
     * @return            Fully-populated InboundDomainEvent (no nulls)
     */
    public InboundDomainEvent parse(String routingKey, JsonNode body) {
        // Split on last dot: "booking.approved" → ["booking", "approved"]
        int lastDot = routingKey.lastIndexOf('.');
        String entityType = routingKey.substring(0, lastDot);
        String actionType = routingKey.substring(lastDot + 1);

        UUID entityId = optionalUuid(body, "id").orElseGet(() -> {
            log.warn("event on routing_key={} missing 'id' field; using nil UUID placeholder", routingKey);
            return NIL_UUID;
        });

        UUID actorId = optionalUuid(body, "actor_id")
                .or(() -> optionalUuid(body, "performed_by"))
                .or(() -> actionSpecificActorField(body, actionType))
                .orElse(NIL_UUID);

        Instant occurredAt = optionalInstant(body, "occurred_at")
                .or(() -> optionalInstant(body, "timestamp"))
                .or(() -> actionSpecificTimestampField(body, actionType))
                .orElseGet(Instant::now);

        return new InboundDomainEvent(entityType, actionType, entityId, actorId, occurredAt, body);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Action-specific fallback helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns action-specific actor field based on action type.
     * "approved" → try "approved_by"
     * "denied"   → try "denied_by"
     * otherwise  → try "owner_id", "updated_by", "created_by" in order
     */
    private Optional<UUID> actionSpecificActorField(JsonNode body, String actionType) {
        if ("approved".equals(actionType)) {
            return optionalUuid(body, "approved_by");
        } else if ("denied".equals(actionType)) {
            return optionalUuid(body, "denied_by");
        } else {
            return optionalUuid(body, "owner_id")
                    .or(() -> optionalUuid(body, "updated_by"))
                    .or(() -> optionalUuid(body, "created_by"));
        }
    }

    /**
     * Returns action-specific timestamp field: tries "{action_type}_at" (e.g. "approved_at",
     * "updated_at", "created_at", "deleted_at", "denied_at").
     */
    private Optional<Instant> actionSpecificTimestampField(JsonNode body, String actionType) {
        return optionalInstant(body, actionType + "_at");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private extraction helpers — return Optional.empty() on any problem
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Extracts a UUID from the named field. Returns empty on missing/null/non-parseable field.
     * Never throws.
     */
    private Optional<UUID> optionalUuid(JsonNode body, String field) {
        if (body == null || !body.has(field) || body.get(field).isNull()) {
            return Optional.empty();
        }
        String text = body.get(field).asText(null);
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(text));
        } catch (IllegalArgumentException e) {
            log.debug("field '{}' value '{}' is not a valid UUID — skipping", field, text);
            return Optional.empty();
        }
    }

    /**
     * Extracts an Instant from the named field. Returns empty on missing/null/non-parseable field.
     * Expects ISO-8601 format (Instant.parse). Never throws.
     */
    private Optional<Instant> optionalInstant(JsonNode body, String field) {
        if (body == null || !body.has(field) || body.get(field).isNull()) {
            return Optional.empty();
        }
        String text = body.get(field).asText(null);
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Instant.parse(text));
        } catch (DateTimeParseException e) {
            log.debug("field '{}' value '{}' is not a valid ISO-8601 Instant — skipping", field, text);
            return Optional.empty();
        }
    }
}
