package com.bookinghub.auditlog.event;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Parses inbound domain events from the message broker into structured InboundDomainEvent objects.
 *
 * <p>Routing key parsing: entity_type is everything before the LAST dot, action_type is everything after.
 * This uniform approach works for all 23 routing keys across all 7 queues without special-casing.
 *
 * <p>Payload field extraction uses defensive fallback chains:
 * - entity_id: tries "id", falls back to nil-UUID (00000000-0000-0000-0000-000000000000)
 * - actor_id: tries "actor_id", "performed_by", then action-specific fields (approved_by, denied_by, etc.)
 * - occurred_at: tries "occurred_at", "timestamp", then action-specific timestamp fields, then Instant.now()
 *
 * <p>This parser NEVER throws for missing or malformed data — only logs warnings and uses documented placeholders.
 */
@Component
public class DomainEventParser {

    private static final Logger log = LoggerFactory.getLogger(DomainEventParser.class);
    private static final UUID NIL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    /**
     * Parses a domain event from its routing key and message body.
     *
     * @param routingKey the RabbitMQ routing key (e.g., "booking.approved", "role.assigned")
     * @param body the message body as a JsonNode
     * @return a fully populated InboundDomainEvent with graceful fallback chains
     */
    public InboundDomainEvent parse(String routingKey, JsonNode body) {
        // Extract entity_type and action_type from routing key
        int lastDot = routingKey.lastIndexOf('.');
        String entityType = routingKey.substring(0, lastDot);
        String actionType = routingKey.substring(lastDot + 1);

        // Extract entity_id: try "id", fall back to nil-UUID
        UUID entityId = optionalUuid(body, "id").orElseGet(() -> {
            log.warn("event on routing_key={} missing 'id' field; using nil UUID placeholder", routingKey);
            return NIL_UUID;
        });

        // Extract actor_id: try standard fields, then action-specific, fall back to nil-UUID
        UUID actorId = optionalUuid(body, "actor_id")
            .or(() -> optionalUuid(body, "performed_by"))
            .or(() -> actionSpecificActorField(body, actionType))
            .orElse(NIL_UUID);

        // Extract occurred_at: try standard fields, then action-specific, fall back to now
        Instant occurredAt = optionalInstant(body, "occurred_at")
            .or(() -> optionalInstant(body, "timestamp"))
            .or(() -> actionSpecificTimestampField(body, actionType))
            .orElseGet(Instant::now);

        return new InboundDomainEvent(entityType, actionType, entityId, actorId, occurredAt, body);
    }

    /**
     * Attempts to extract a UUID from a specific field in the JSON body.
     * Returns empty if the field is missing, null, or not a valid UUID string.
     */
    private Optional<UUID> optionalUuid(JsonNode body, String fieldName) {
        if (body == null || !body.has(fieldName) || body.get(fieldName).isNull()) {
            return Optional.empty();
        }
        try {
            String value = body.get(fieldName).asText();
            if (value.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            log.debug("Failed to parse UUID from field '{}': {}", fieldName, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Attempts to extract an Instant from a specific field in the JSON body.
     * Accepts ISO-8601 formatted strings.
     * Returns empty if the field is missing, null, or not a valid ISO-8601 string.
     */
    private Optional<Instant> optionalInstant(JsonNode body, String fieldName) {
        if (body == null || !body.has(fieldName) || body.get(fieldName).isNull()) {
            return Optional.empty();
        }
        try {
            String value = body.get(fieldName).asText();
            if (value.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(Instant.parse(value));
        } catch (DateTimeParseException e) {
            log.debug("Failed to parse Instant from field '{}': {}", fieldName, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Attempts to extract actor_id using action-type-specific field names.
     * For "approved" actions, tries "approved_by".
     * For "denied" actions, tries "denied_by".
     * For all other actions, tries "owner_id", "updated_by", "created_by" in that order.
     */
    private Optional<UUID> actionSpecificActorField(JsonNode body, String actionType) {
        if (body == null) {
            return Optional.empty();
        }

        if ("approved".equals(actionType)) {
            return optionalUuid(body, "approved_by");
        }
        if ("denied".equals(actionType)) {
            return optionalUuid(body, "denied_by");
        }

        // Try generic fallback chain
        return optionalUuid(body, "owner_id")
            .or(() -> optionalUuid(body, "updated_by"))
            .or(() -> optionalUuid(body, "created_by"));
    }

    /**
     * Attempts to extract occurred_at using action-type-specific field names.
     * Looks for "{actionType}_at" (e.g., "approved_at", "denied_at", "updated_at").
     * Falls back to "created_at" and "deleted_at" for other actions.
     */
    private Optional<Instant> actionSpecificTimestampField(JsonNode body, String actionType) {
        if (body == null) {
            return Optional.empty();
        }

        // Try action-specific timestamp field (e.g., "approved_at", "denied_at")
        String actionSpecificField = actionType + "_at";
        Optional<Instant> result = optionalInstant(body, actionSpecificField);
        if (result.isPresent()) {
            return result;
        }

        // Try common timestamp fields
        return optionalInstant(body, "created_at")
            .or(() -> optionalInstant(body, "deleted_at"));
    }
}
