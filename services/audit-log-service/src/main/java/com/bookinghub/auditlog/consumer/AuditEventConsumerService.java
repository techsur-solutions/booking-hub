package com.bookinghub.auditlog.consumer;

import com.bookinghub.auditlog.domain.AuditLogEntry;
import com.bookinghub.auditlog.event.DomainEventParser;
import com.bookinghub.auditlog.event.InboundDomainEvent;
import com.bookinghub.auditlog.rabbit.MissingIdempotencyKeyException;
import com.bookinghub.auditlog.repository.AuditLogEntryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * The audit trail's single write path: 7 listener methods, one per {@code audit.*.q} queue
 * declared in {@code infra/rabbitmq/definitions.json}, all delegating to one shared
 * {@link #processEvent} handler.
 *
 * <p>(Prose in this file deliberately avoids spelling the listener annotation out, so that
 * {@code grep -c} over this file counts exactly the 7 real bindings — that count is this
 * plan's machine-checked integration contract.)</p>
 *
 * <h2>Coverage (F11.1)</h2>
 * The 7 queues between them carry every change-event category F11 names:
 * <table>
 *   <tr><th>Queue</th><th>Exchange</th><th>Routing keys</th></tr>
 *   <tr><td>audit.booking.q</td><td>booking.events</td><td>booking.* (created/updated/deleted/approved/denied)</td></tr>
 *   <tr><td>audit.location.q</td><td>location.events</td><td>location.*</td></tr>
 *   <tr><td>audit.resource.q</td><td>resource.events</td><td>resource.*</td></tr>
 *   <tr><td>audit.customfield.q</td><td>customfield.events</td><td>customfield.*, template.*</td></tr>
 *   <tr><td>audit.user.q</td><td>user.events</td><td>user.created, user.updated, role.assigned</td></tr>
 *   <tr><td>audit.permission.q</td><td>permission.events</td><td>permission.updated</td></tr>
 *   <tr><td>audit.settings.q</td><td>settings.events</td><td>settings.updated</td></tr>
 * </table>
 * The listeners differ ONLY in their queue name — all entity/action derivation happens in
 * {@link DomainEventParser} from the routing key, so an 8th category needs only an 8th
 * binding here, never new mapping code.
 *
 * <p>Note {@code audit.customfield.q} carries BOTH {@code customfield.*} and {@code template.*},
 * and {@code audit.user.q} carries both {@code user.*} and {@code role.assigned}: entity_type
 * comes from the routing key, never from the queue, so one listener correctly records two
 * different entity types.</p>
 *
 * <h2>Idempotency: lookup-first, not insert-then-catch</h2>
 * {@link #processEvent} looks the {@code idempotency_key} up BEFORE inserting, and returns a
 * silent no-op (ack) when a row already exists. This is deliberately the same corrected shape
 * plan 06-02 settled on for notifications-service, for the identical reason: Spring Retry's
 * interceptor re-invokes this ENTIRE method on a transient failure, so an insert-first /
 * catch-the-constraint-violation approach would misfire on its own retry and mistake its own
 * earlier partial work for a duplicate delivery. The partial unique index on
 * {@code idempotency_key} (plan 06-03's V2 migration) remains the race-safe backstop for two
 * genuinely concurrent redeliveries.
 *
 * <h2>Never silently drop</h2>
 * A message that is structurally unprocessable — missing/blank/malformed idempotency key, or a
 * body that is not JSON — throws {@link MissingIdempotencyKeyException}
 * ({@code AmqpRejectAndDontRequeueException}), which skips retries entirely and lands the
 * message on the queue's DLQ via the {@code x-dead-letter-exchange} already declared on every
 * {@code audit.*.q}. The dead-lettered message IS the record of the gap, per F11 §Error States'
 * "surfaced via observability/alerting, not a client-facing error". Nothing is ever dropped
 * quietly, and an incomplete row is never written.
 *
 * A merely SPARSE body (valid JSON, missing fields) is a different case entirely: it is written,
 * with {@link DomainEventParser}'s documented placeholders. The audit fact that something
 * happened outranks the completeness of its detail.
 */
@Component
public class AuditEventConsumerService {

    private static final Logger log = LoggerFactory.getLogger(AuditEventConsumerService.class);

    private final AuditLogEntryRepository auditLogEntryRepository;
    private final DomainEventParser domainEventParser;
    private final ObjectMapper objectMapper;

    public AuditEventConsumerService(AuditLogEntryRepository auditLogEntryRepository,
                                     DomainEventParser domainEventParser,
                                     ObjectMapper objectMapper) {
        this.auditLogEntryRepository = auditLogEntryRepository;
        this.domainEventParser = domainEventParser;
        this.objectMapper = objectMapper;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // The 7 bindings. Queue names copied verbatim from infra/rabbitmq/definitions.json.
    // ──────────────────────────────────────────────────────────────────────────

    @RabbitListener(queues = "audit.booking.q")
    public void onBookingEvent(Message message,
                               @Header(name = "idempotency_key", required = false) String idempotencyKey,
                               @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        processEvent(message, idempotencyKey, routingKey);
    }

    @RabbitListener(queues = "audit.location.q")
    public void onLocationEvent(Message message,
                                @Header(name = "idempotency_key", required = false) String idempotencyKey,
                                @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        processEvent(message, idempotencyKey, routingKey);
    }

    @RabbitListener(queues = "audit.resource.q")
    public void onResourceEvent(Message message,
                                @Header(name = "idempotency_key", required = false) String idempotencyKey,
                                @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        processEvent(message, idempotencyKey, routingKey);
    }

    @RabbitListener(queues = "audit.customfield.q")
    public void onCustomFieldEvent(Message message,
                                   @Header(name = "idempotency_key", required = false) String idempotencyKey,
                                   @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        processEvent(message, idempotencyKey, routingKey);
    }

    @RabbitListener(queues = "audit.user.q")
    public void onUserEvent(Message message,
                            @Header(name = "idempotency_key", required = false) String idempotencyKey,
                            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        processEvent(message, idempotencyKey, routingKey);
    }

    @RabbitListener(queues = "audit.permission.q")
    public void onPermissionEvent(Message message,
                                  @Header(name = "idempotency_key", required = false) String idempotencyKey,
                                  @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        processEvent(message, idempotencyKey, routingKey);
    }

    @RabbitListener(queues = "audit.settings.q")
    public void onSettingsEvent(Message message,
                                @Header(name = "idempotency_key", required = false) String idempotencyKey,
                                @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        processEvent(message, idempotencyKey, routingKey);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // The one shared handler.
    // ──────────────────────────────────────────────────────────────────────────

    private void processEvent(Message message, String idempotencyKey, String routingKey) {
        UUID idempotencyKeyUuid = requireIdempotencyKey(idempotencyKey, routingKey);

        // Lookup-first: a redelivery (broker-level OR Spring Retry's own re-invocation)
        // finds the existing row and acks without writing a second one.
        if (auditLogEntryRepository.findByIdempotencyKey(idempotencyKeyUuid).isPresent()) {
            log.info("duplicate audit event suppressed for idempotency_key={}, routing_key={}",
                    idempotencyKey, routingKey);
            return;
        }

        JsonNode body = readBody(message, routingKey);
        InboundDomainEvent parsed = domainEventParser.parse(routingKey, body);

        AuditLogEntry entry = new AuditLogEntry();
        entry.setIdempotencyKey(idempotencyKeyUuid);
        entry.setActorId(parsed.actorId());
        entry.setOccurredAt(parsed.occurredAt());
        entry.setEntityType(parsed.entityType());
        entry.setEntityId(parsed.entityId());
        entry.setActionType(parsed.actionType());
        // before_values stays NULL for every event in this phase: no publisher in this project
        // emits a distinct before-snapshot, so there is nothing more specific to extract.
        // after_values stores the ENTIRE parsed body verbatim, per F11 §Process step 3's own
        // interim default ("never fail the write because they're absent").
        entry.setBeforeValues(null);
        entry.setAfterValues(parsed.rawBody().toString());

        // The partial unique index on idempotency_key is the final race-safe backstop against
        // two genuinely concurrent redeliveries both passing the lookup above.
        auditLogEntryRepository.save(entry);

        log.debug("audit entry written: entity_type={} action_type={} entity_id={} idempotency_key={}",
                parsed.entityType(), parsed.actionType(), parsed.entityId(), idempotencyKey);
    }

    /**
     * Validates the idempotency key, treating a missing, blank, OR malformed value identically:
     * all three are structurally unprocessable no matter how often they are retried, so all three
     * go straight to the DLQ rather than burning the retry budget first.
     */
    private UUID requireIdempotencyKey(String idempotencyKey, String routingKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new MissingIdempotencyKeyException(
                    "event missing idempotency_key header, routing_key=" + routingKey);
        }
        try {
            return UUID.fromString(idempotencyKey);
        } catch (IllegalArgumentException ex) {
            throw new MissingIdempotencyKeyException(
                    "event carries a non-UUID idempotency_key header '" + idempotencyKey
                            + "', routing_key=" + routingKey);
        }
    }

    /**
     * Parses the message body as JSON. A body that is not JSON at all can never succeed on retry,
     * so it dead-letters immediately — distinct from a VALID-but-sparse body, which is written
     * with {@link DomainEventParser}'s placeholders rather than rejected.
     */
    private JsonNode readBody(Message message, String routingKey) {
        try {
            return objectMapper.readTree(new String(message.getBody(), StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new MissingIdempotencyKeyException(
                    "event body is not parseable JSON, routing_key=" + routingKey
                            + ", cause=" + ex.getMessage());
        }
    }
}
