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
 * Turns a RabbitMQ routing key + JSON body into an {@link InboundDomainEvent}.
 *
 * <h2>Why routing-key-derived entity_type/action_type</h2>
 * Every one of the 23 routing keys bound to this service's 7 queues
 * (infra/rabbitmq/definitions.json) has the shape {@code <entity>.<action>}:
 * {@code booking.created}, {@code booking.approved}, {@code booking.denied},
 * {@code location.updated}, {@code resource.deleted}, {@code customfield.created},
 * {@code template.updated}, {@code user.created}, {@code role.assigned},
 * {@code permission.updated}, {@code settings.updated}, … Splitting on the LAST dot
 * therefore derives both columns uniformly with zero per-queue special-casing, and an
 * 8th future event category needs only a new {@code @RabbitListener} binding — no new
 * entity-type-specific code here.
 *
 * <h2>Why fallback chains for entity_id / actor_id / occurred_at</h2>
 * Neither Phase 3's nor Phase 5's outbox-publishing plans pin a literal envelope around
 * the message body. The {@code idempotency_key} travels as a message HEADER (confirmed:
 * every {@code OutboxPublisher} in this codebase sets {@code setHeader("idempotency_key", …)}
 * and sends the aggregate's JSON as the raw body), and entity/action are derivable from the
 * routing key — but {@code entity_id}/{@code actor_id}/{@code occurred_at} must come from
 * the body, whose exact key names the publish-side plans never fixed.
 *
 * This parser therefore reads them defensively and NEVER throws:
 * <ul>
 *   <li><b>entity_id</b>: {@code id} — the overwhelmingly likely shape, since every entity
 *       serializes its own primary key as {@code id}. Absent → warn + {@link #NIL_UUID}.</li>
 *   <li><b>actor_id</b>: {@code actor_id} → {@code performed_by} → an action-specific field
 *       ({@code approved_by} for {@code *.approved}, {@code denied_by} for {@code *.denied},
 *       else {@code owner_id} → {@code updated_by} → {@code created_by}). None → {@link #NIL_UUID},
 *       a documented "system/unknown actor" placeholder.</li>
 *   <li><b>occurred_at</b>: {@code occurred_at} → {@code timestamp} → {@code {action}_at}
 *       (e.g. {@code approved_at}, {@code updated_at}) → message receipt time.</li>
 * </ul>
 *
 * Degrading to a placeholder rather than throwing is deliberate (threat T-06-04-01): a sparse
 * or malformed body from any of the 7 upstream publishers must never crash a listener nor lose
 * the audit FACT that something happened. The fallback is visible in the data (nil UUID) and in
 * the logs (warning), never silent.
 */
@Component
public class DomainEventParser {

    private static final Logger log = LoggerFactory.getLogger(DomainEventParser.class);

    /**
     * Documented placeholder for an unresolvable entity_id/actor_id.
     * The columns are NOT NULL in V1's DDL, so a placeholder is required — this value is
     * deliberately recognisable rather than a random UUID, so a query for it surfaces every
     * event whose publisher did not carry the expected field.
     */
    public static final UUID NIL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    /**
     * Parses one domain event.
     *
     * @param routingKey the AMQP routing key the message arrived on (e.g. "booking.approved")
     * @param body       the message body parsed as JSON (may be an empty object)
     * @return a fully-populated event — never null, never with null fields
     */
    public InboundDomainEvent parse(String routingKey, JsonNode body) {
        int lastDot = routingKey == null ? -1 : routingKey.lastIndexOf('.');
        String entityType;
        String actionType;
        if (lastDot <= 0 || lastDot == routingKey.length() - 1) {
            // Defensive: a routing key without a usable dot can only happen if a publisher
            // deviates from the documented <entity>.<action> convention. Record the fact
            // rather than dropping the event.
            log.warn("routing_key={} does not match the <entity>.<action> convention; "
                    + "recording entity_type=unknown", routingKey);
            entityType = "unknown";
            actionType = (routingKey == null || routingKey.isBlank()) ? "unknown" : routingKey;
        } else {
            entityType = routingKey.substring(0, lastDot);
            actionType = routingKey.substring(lastDot + 1);
        }

        UUID entityId = optionalUuid(body, "id").orElseGet(() -> {
            log.warn("event on routing_key={} missing 'id' field; using nil UUID placeholder", routingKey);
            return NIL_UUID;
        });

        UUID actorId = optionalUuid(body, "actor_id")
                .or(() -> optionalUuid(body, "performed_by"))
                .or(() -> actionSpecificActorField(body, actionType))
                .orElseGet(() -> {
                    log.warn("event on routing_key={} carries no resolvable actor field; "
                            + "recording nil UUID (system/unknown actor)", routingKey);
                    return NIL_UUID;
                });

        Instant occurredAt = optionalInstant(body, "occurred_at")
                .or(() -> optionalInstant(body, "timestamp"))
                .or(() -> actionSpecificTimestampField(body, actionType))
                .orElseGet(Instant::now);

        return new InboundDomainEvent(entityType, actionType, entityId, actorId, occurredAt, body);
    }

    /**
     * Action-aware actor resolution, tried only after the generic {@code actor_id} /
     * {@code performed_by} fields are absent.
     *
     * {@code *.approved} → {@code approved_by}; {@code *.denied} → {@code denied_by};
     * anything else → {@code owner_id} → {@code updated_by} → {@code created_by}.
     */
    private Optional<UUID> actionSpecificActorField(JsonNode body, String actionType) {
        if ("approved".equals(actionType)) {
            return optionalUuid(body, "approved_by");
        }
        if ("denied".equals(actionType)) {
            return optionalUuid(body, "denied_by");
        }
        return optionalUuid(body, "owner_id")
                .or(() -> optionalUuid(body, "updated_by"))
                .or(() -> optionalUuid(body, "created_by"));
    }

    /**
     * Action-aware timestamp resolution: {@code {actionType}_at}, so an
     * {@code approved} event reads {@code approved_at}, a {@code deleted} event
     * {@code deleted_at}, and so on, with no per-action enumeration needed.
     */
    private Optional<Instant> actionSpecificTimestampField(JsonNode body, String actionType) {
        if (actionType == null || actionType.isBlank()) {
            return Optional.empty();
        }
        return optionalInstant(body, actionType + "_at");
    }

    /**
     * Reads a UUID-valued field, returning empty (never throwing) for a missing, null,
     * blank, or unparseable value.
     */
    private Optional<UUID> optionalUuid(JsonNode body, String field) {
        String raw = textValue(body, field);
        if (raw == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(raw));
        } catch (IllegalArgumentException ex) {
            log.warn("field '{}' value '{}' is not a valid UUID; treating as absent", field, raw);
            return Optional.empty();
        }
    }

    /**
     * Reads an ISO-8601 instant-valued field, returning empty (never throwing) for a missing,
     * null, blank, or unparseable value.
     */
    private Optional<Instant> optionalInstant(JsonNode body, String field) {
        String raw = textValue(body, field);
        if (raw == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Instant.parse(raw));
        } catch (DateTimeParseException ex) {
            log.warn("field '{}' value '{}' is not a parseable ISO-8601 instant; treating as absent",
                    field, raw);
            return Optional.empty();
        }
    }

    /**
     * Null-safe text extraction: returns null for an absent node, an explicit JSON null,
     * or a blank string.
     */
    private String textValue(JsonNode body, String field) {
        if (body == null || field == null) {
            return null;
        }
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        String raw = node.asText();
        return (raw == null || raw.isBlank()) ? null : raw;
    }
}
