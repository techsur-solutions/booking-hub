package com.bookinghub.auditlog.event;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable value object representing a parsed inbound domain event.
 *
 * Produced by DomainEventParser.parse() from:
 * - The RabbitMQ routing key (→ entityType + actionType)
 * - The JSON message body (→ entityId, actorId, occurredAt via fallback chains)
 * - The raw body itself (→ rawBody for storage as after_values JSONB)
 *
 * All fields are guaranteed non-null (entityId/actorId fall back to NIL_UUID,
 * occurredAt falls back to receipt time — the parser never throws).
 *
 * Named decision (Java record): immutable by construction, zero boilerplate.
 * Comparable to Phase 3's BookingRequest DTOs as records — same pattern.
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
