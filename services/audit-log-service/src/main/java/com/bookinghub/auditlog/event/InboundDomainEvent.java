package com.bookinghub.auditlog.event;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

/**
 * A domain event, fully parsed into the shape audit_log_entries needs.
 *
 * Produced by {@link DomainEventParser} from a RabbitMQ routing key plus the
 * message's JSON body. Every field is guaranteed non-null — the parser's fallback
 * chains substitute documented placeholders (nil UUID, receipt-time) rather than
 * ever returning null or throwing, per F11 §Validation's "never fail the write
 * because they're absent" philosophy.
 *
 * @param entityType  derived from the routing key's segment(s) BEFORE the last dot
 *                    (e.g. "booking" from "booking.approved")
 * @param actionType  derived from the routing key's segment AFTER the last dot
 *                    (e.g. "approved" from "booking.approved")
 * @param entityId    the changed entity's id, from body."id" — NIL_UUID if absent
 * @param actorId     who performed the action, from a documented fallback chain —
 *                    NIL_UUID if no actor field is present (system/unknown actor)
 * @param occurredAt  when the action happened, from a documented fallback chain —
 *                    message receipt time if the body carries no timestamp
 * @param rawBody     the entire parsed body, stored verbatim as after_values JSONB
 */
public record InboundDomainEvent(
        String entityType,
        String actionType,
        UUID entityId,
        UUID actorId,
        Instant occurredAt,
        JsonNode rawBody
) {
}
