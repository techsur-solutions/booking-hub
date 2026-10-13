package com.bookinghub.auditlog.event;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;

/**
 * Immutable representation of a domain event received from the message broker.
 * All fields are derived either from the routing key (entity_type, action_type)
 * or extracted from the message body with documented fallback chains.
 */
public record InboundDomainEvent(
    String entityType,
    String actionType,
    UUID entityId,
    UUID actorId,
    Instant occurredAt,
    JsonNode rawBody
) {}
