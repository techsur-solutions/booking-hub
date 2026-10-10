package com.bookinghub.notifications.consumer;

import com.bookinghub.notifications.domain.NotificationDelivery;
import com.bookinghub.notifications.email.EmailSenderService;
import com.bookinghub.notifications.rabbit.MissingIdempotencyKeyException;
import com.bookinghub.notifications.rabbit.UnresolvableRecipientException;
import com.bookinghub.notifications.repository.NotificationDeliveryRepository;
import com.bookinghub.notifications.template.NotificationTemplateService;
import com.bookinghub.notifications.template.NotificationTemplateService.RenderedEmail;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * RabbitMQ consumer for notification events.
 *
 * Two listeners are bound to exactly the queue names from infra/rabbitmq/definitions.json:
 * - notifications.booking.q  → booking.created / booking.approved / booking.denied
 * - notifications.passwordreset.q → password.reset.requested
 *
 * Both delegate to a shared private processEvent(...) method.
 *
 * Idempotency (lookup-first pattern — plan 06-02 requirement):
 * On every invocation (including Spring Retry re-invocations of the same message),
 * we first look up the row by idempotency_key:
 * - DOES NOT EXIST → insert fresh at status='pending', proceed to send.
 * - EXISTS and status='sent' → no-op return (idempotent redelivery suppression).
 * - EXISTS and status='pending'/'retrying' → this is a retry of the SAME message;
 *   set status='retrying', increment attempt_count, attempt send, then set status='sent'.
 *
 * The table's UNIQUE constraint on idempotency_key is the race-safe backstop for the
 * window between "does not exist" and "insert", if two truly concurrent deliveries
 * of the same message arrive simultaneously. A DataIntegrityViolationException from
 * the concurrent insert is caught and treated as "already processed" (no-op).
 *
 * Non-retryable short-circuit paths (both go straight to DLQ per RabbitConfig):
 * - Missing idempotency_key header → MissingIdempotencyKeyException (no row created)
 * - Missing/blank recipient address → UnresolvableRecipientException (row exists in pending/retrying)
 *
 * Named decisions from plan 06-02:
 * - contact_email carries the booking recipient (payload-embedded, no cross-service call).
 * - email carries the password-reset recipient (FRD F8 §Inputs literal example).
 * - entity_id is extracted from payload.id; falls back to UUID of zeros with a warning
 *   if absent (missing entity id does NOT prevent sending the email).
 */
@Component
@Slf4j
public class NotificationConsumerService {

    private final NotificationDeliveryRepository notificationDeliveryRepository;
    private final NotificationTemplateService notificationTemplateService;
    private final EmailSenderService emailSenderService;
    private final ObjectMapper objectMapper;

    private static final UUID ZERO_UUID = new UUID(0L, 0L);

    public NotificationConsumerService(
            NotificationDeliveryRepository notificationDeliveryRepository,
            NotificationTemplateService notificationTemplateService,
            EmailSenderService emailSenderService,
            ObjectMapper objectMapper) {
        this.notificationDeliveryRepository = notificationDeliveryRepository;
        this.notificationTemplateService = notificationTemplateService;
        this.emailSenderService = emailSenderService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "notifications.booking.q")
    public void onBookingEvent(
            Message message,
            @Header("idempotency_key") String idempotencyKey,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        processEvent(message, idempotencyKey, routingKey);
    }

    @RabbitListener(queues = "notifications.passwordreset.q")
    public void onPasswordResetEvent(
            Message message,
            @Header("idempotency_key") String idempotencyKey,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        processEvent(message, idempotencyKey, routingKey);
    }

    // -----------------------------------------------------------------------
    // Core processing
    // -----------------------------------------------------------------------

    private void processEvent(Message message, String idempotencyKey, String routingKey) {
        // --- Guard: missing idempotency key ---
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new MissingIdempotencyKeyException(
                    "Event missing idempotency_key header, routing_key=" + routingKey);
        }

        // --- Parse JSON body ---
        JsonNode eventData;
        try {
            eventData = objectMapper.readTree(message.getBody());
        } catch (Exception e) {
            // Malformed JSON: treat as retryable processing failure
            throw new RuntimeException("Failed to parse event body, routing_key=" + routingKey, e);
        }

        // --- Extract entity id (best-effort; missing id does NOT prevent email send) ---
        UUID entityId = extractEntityId(eventData, routingKey);

        // --- Lookup-first idempotency check ---
        Optional<NotificationDelivery> existingOpt =
                notificationDeliveryRepository.findByIdempotencyKey(idempotencyKey);

        NotificationDelivery row;
        if (existingOpt.isPresent()) {
            row = existingOpt.get();
            if ("sent".equals(row.getStatus())) {
                // Already successfully delivered — idempotent no-op
                log.info("Duplicate delivery suppressed (status=sent) for idempotency_key={}", idempotencyKey);
                return;
            }
            // Status is 'pending' or 'retrying': this is a Spring Retry re-invocation
            // of the SAME message. Mark as 'retrying' before the send attempt.
            row.setStatus("retrying");
            row.setAttemptCount(row.getAttemptCount() + 1);
            row.setLastAttemptedAt(Instant.now());
            notificationDeliveryRepository.save(row);
            log.info("Retrying delivery for idempotency_key={}, attempt={}", idempotencyKey, row.getAttemptCount());
        } else {
            // First-time processing: insert at 'pending'.
            row = new NotificationDelivery();
            row.setIdempotencyKey(idempotencyKey);
            row.setEventType(routingKey);
            row.setEntityId(entityId);
            row.setStatus("pending");
            row.setAttemptCount(0);
            try {
                notificationDeliveryRepository.saveAndFlush(row);
            } catch (DataIntegrityViolationException concurrentDuplicate) {
                // Race: concurrent delivery of the same message; the other invocation
                // already inserted the row — treat as "already processed".
                log.info("Concurrent duplicate suppressed for idempotency_key={}", idempotencyKey);
                return;
            }
        }

        // --- Resolve recipient ---
        String recipient = "password.reset.requested".equals(routingKey)
                ? textOrNull(eventData, "email")
                : textOrNull(eventData, "contact_email");

        if (recipient == null || recipient.isBlank()) {
            // Non-retryable: no recipient can ever be resolved from this message.
            // DatabaseTrackingMessageRecoverer will mark the row 'dead_lettered'.
            throw new UnresolvableRecipientException(
                    "No resolvable recipient for idempotency_key=" + idempotencyKey
                    + ", routing_key=" + routingKey);
        }

        // --- Render template and send (both are retryable on exception) ---
        RenderedEmail rendered = notificationTemplateService.render(routingKey, eventData);
        emailSenderService.send(recipient, rendered.subject(), rendered.body());

        // --- Mark row as sent ---
        row.setStatus("sent");
        row.setAttemptCount(row.getAttemptCount() + 1);
        row.setLastAttemptedAt(Instant.now());
        notificationDeliveryRepository.save(row);

        log.info("Notification sent for idempotency_key={}, routing_key={}, to={}",
                idempotencyKey, routingKey, recipient);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Extracts the entity UUID from eventData.id.
     * Falls back to UUID-of-zeros only if truly absent, logging a warning.
     * A missing entity id does NOT prevent sending the email.
     */
    private UUID extractEntityId(JsonNode eventData, String routingKey) {
        JsonNode idNode = eventData.get("id");
        if (idNode == null || idNode.isNull()) {
            log.warn("Entity id absent from event payload, routing_key={} — falling back to zero UUID", routingKey);
            return ZERO_UUID;
        }
        try {
            return UUID.fromString(idNode.asText());
        } catch (IllegalArgumentException e) {
            log.warn("Entity id is not a valid UUID: '{}', routing_key={} — falling back to zero UUID",
                    idNode.asText(), routingKey);
            return ZERO_UUID;
        }
    }

    /**
     * Returns the text value of a field, or null if absent/null/blank.
     */
    private String textOrNull(JsonNode data, String field) {
        JsonNode node = data.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        String text = node.asText("");
        return text.isBlank() ? null : text;
    }
}
