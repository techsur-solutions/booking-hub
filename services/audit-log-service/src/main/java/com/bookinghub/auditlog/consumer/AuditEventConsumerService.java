package com.bookinghub.auditlog.consumer;

import com.bookinghub.auditlog.domain.AuditLogEntry;
import com.bookinghub.auditlog.event.DomainEventParser;
import com.bookinghub.auditlog.event.InboundDomainEvent;
import com.bookinghub.auditlog.rabbit.MissingIdempotencyKeyException;
import com.bookinghub.auditlog.repository.AuditLogEntryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Event consumer for all 7 audit event queues.
 *
 * Binds exactly 7 @RabbitListener methods (one per queue): audit.booking.q, audit.location.q,
 * audit.resource.q, audit.customfield.q, audit.user.q, audit.permission.q, audit.settings.q.
 *
 * <p>Each listener:
 * 1. Validates the idempotency_key header is present (routes missing to DLQ immediately via
 *    MissingIdempotencyKeyException — non-retryable per RabbitConfig)
 * 2. Checks if an entry with this idempotency_key already exists (lookup-first idempotency)
 * 3. If new, parses the routing key + body via DomainEventParser, builds an AuditLogEntry, and saves
 * 4. If duplicate, logs and returns silently (no-op ack)
 *
 * <p>The partial unique index on idempotency_key WHERE idempotency_key IS NOT NULL (from V2
 * migration) is the final safety net against concurrent duplicate writes (though the lookup-first
 * check is the happy-path optimization, matching notifications-service's precedent).
 */
@Component
public class AuditEventConsumerService {

    private static final Logger log = LoggerFactory.getLogger(AuditEventConsumerService.class);

    private final AuditLogEntryRepository auditLogEntryRepository;
    private final DomainEventParser domainEventParser;
    private final ObjectMapper objectMapper;

    public AuditEventConsumerService(
            AuditLogEntryRepository auditLogEntryRepository,
            DomainEventParser domainEventParser,
            ObjectMapper objectMapper) {
        this.auditLogEntryRepository = auditLogEntryRepository;
        this.domainEventParser = domainEventParser;
        this.objectMapper = objectMapper;
    }

    // ==================== 7 Queues — One Listener Per Queue ====================

    /**
     * Handles booking.* events from audit.booking.q
     */
    @RabbitListener(queues = "audit.booking.q")
    public void onBookingEvent(
            Message message,
            @Header(name = "idempotency_key", required = false) String idempotencyKey,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        processEvent(message, idempotencyKey, routingKey);
    }

    /**
     * Handles location.* events from audit.location.q
     */
    @RabbitListener(queues = "audit.location.q")
    public void onLocationEvent(
            Message message,
            @Header(name = "idempotency_key", required = false) String idempotencyKey,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        processEvent(message, idempotencyKey, routingKey);
    }

    /**
     * Handles resource.* events from audit.resource.q
     */
    @RabbitListener(queues = "audit.resource.q")
    public void onResourceEvent(
            Message message,
            @Header(name = "idempotency_key", required = false) String idempotencyKey,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        processEvent(message, idempotencyKey, routingKey);
    }

    /**
     * Handles customfield.* events from audit.customfield.q
     */
    @RabbitListener(queues = "audit.customfield.q")
    public void onCustomFieldEvent(
            Message message,
            @Header(name = "idempotency_key", required = false) String idempotencyKey,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        processEvent(message, idempotencyKey, routingKey);
    }

    /**
     * Handles user.* and role.* events from audit.user.q
     * (Both user and role events share the same queue per infra/rabbitmq/definitions.json)
     */
    @RabbitListener(queues = "audit.user.q")
    public void onUserEvent(
            Message message,
            @Header(name = "idempotency_key", required = false) String idempotencyKey,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        processEvent(message, idempotencyKey, routingKey);
    }

    /**
     * Handles permission.* events from audit.permission.q
     */
    @RabbitListener(queues = "audit.permission.q")
    public void onPermissionEvent(
            Message message,
            @Header(name = "idempotency_key", required = false) String idempotencyKey,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        processEvent(message, idempotencyKey, routingKey);
    }

    /**
     * Handles settings.* events from audit.settings.q
     */
    @RabbitListener(queues = "audit.settings.q")
    public void onSettingsEvent(
            Message message,
            @Header(name = "idempotency_key", required = false) String idempotencyKey,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        processEvent(message, idempotencyKey, routingKey);
    }

    // ==================== Shared Processing Logic ====================

    /**
     * Processes an audit event: validates idempotency_key, checks for duplicates,
     * parses the event, and writes it to audit_log_entries.
     *
     * <p>Uses lookup-first idempotency: if a row with this key exists, return silently (no-op).
     * If missing, parse the event and save a new row.
     *
     * <p>Throws MissingIdempotencyKeyException if idempotency_key is absent/blank — this is
     * a non-retryable exception per RabbitConfig, so it routes straight to DLQ without retry.
     *
     * @param message the RabbitMQ Message (body is the JSON event payload)
     * @param idempotencyKey the idempotency_key header value (null if absent)
     * @param routingKey the routing key (e.g., "booking.approved", "role.assigned")
     * @throws MissingIdempotencyKeyException if idempotency_key header is missing or blank
     */
    private void processEvent(Message message, String idempotencyKey, String routingKey) {
        // Step 1: Validate idempotency_key header presence
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new MissingIdempotencyKeyException(
                    "event missing idempotency_key header, routing_key=" + routingKey);
        }

        // Parse idempotency_key as UUID
        UUID idempotencyKeyUuid;
        try {
            idempotencyKeyUuid = UUID.fromString(idempotencyKey);
        } catch (IllegalArgumentException e) {
            throw new MissingIdempotencyKeyException(
                    "idempotency_key is not a valid UUID: " + idempotencyKey);
        }

        // Step 2: Check for duplicate (lookup-first idempotency)
        if (auditLogEntryRepository.findByIdempotencyKey(idempotencyKeyUuid).isPresent()) {
            log.info("duplicate audit event suppressed for idempotency_key={}", idempotencyKey);
            return; // ack as a no-op — never write a second row
        }

        // Step 3: Parse the event from routing key + body
        JsonNode body;
        try {
            body = objectMapper.readTree(message.getBody());
        } catch (Exception e) {
            log.error(
                    "failed to parse message body as JSON, routing_key={}, idempotency_key={}",
                    routingKey,
                    idempotencyKey,
                    e);
            throw new RuntimeException("invalid message body", e);
        }

        InboundDomainEvent parsed = domainEventParser.parse(routingKey, body);

        // Step 4: Build and save the AuditLogEntry
        AuditLogEntry entry = new AuditLogEntry();
        entry.setIdempotencyKey(idempotencyKeyUuid);
        entry.setActorId(parsed.actorId());
        entry.setOccurredAt(parsed.occurredAt());
        entry.setEntityType(parsed.entityType());
        entry.setEntityId(parsed.entityId());
        entry.setActionType(parsed.actionType());
        entry.setBeforeValues(null); // per this plan's named decision — no publisher in this project
                                       // emits a distinct before-snapshot yet
        entry.setAfterValues(parsed.rawBody().toString());

        try {
            auditLogEntryRepository.save(entry);
            log.debug(
                    "audit event saved: entity_type={}, action_type={}, entity_id={}, actor_id={}, idempotency_key={}",
                    parsed.entityType(),
                    parsed.actionType(),
                    parsed.entityId(),
                    parsed.actorId(),
                    idempotencyKey);
        } catch (Exception e) {
            log.error(
                    "failed to save audit log entry for idempotency_key={}, routing_key={}",
                    idempotencyKey,
                    routingKey,
                    e);
            throw new RuntimeException("failed to save audit log entry", e);
        }
    }
}
