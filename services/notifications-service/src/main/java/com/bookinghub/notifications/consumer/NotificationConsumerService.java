package com.bookinghub.notifications.consumer;

import com.bookinghub.notifications.domain.NotificationDelivery;
import com.bookinghub.notifications.email.EmailSenderService;
import com.bookinghub.notifications.rabbit.MissingIdempotencyKeyException;
import com.bookinghub.notifications.rabbit.UnresolvableRecipientException;
import com.bookinghub.notifications.repository.NotificationDeliveryRepository;
import com.bookinghub.notifications.template.NotificationTemplateService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpHeaders;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * RabbitMQ consumer for notification events.
 *
 * Implements the idempotency-first pattern with lookup-before-insert to handle
 * both true message redelivery and Spring Retry's in-process re-invocation:
 *
 * 1. Lookup by idempotency_key:
 *    - If found with status='sent': no-op (already processed)
 *    - If found with status='pending'/'retrying': proceed to send without re-insert,
 *      update status and attempt_count
 *    - If not found: insert fresh at status='pending' then proceed to send
 *
 * 2. Recipient resolution: read from event payload
 *    - booking.* events: contact_email field
 *    - password.reset.requested: email field
 *    - Missing/blank → UnresolvableRecipientException (non-retryable, → DLQ)
 *
 * 3. Template rendering + SMTP send via injected services
 *
 * 4. Final status update: 'sent' on success, or exception propagates for retry/DLQ
 *
 * Non-retryable failures (MissingIdempotencyKeyException, UnresolvableRecipientException)
 * skip all retry cycles and go straight to DLQ via RabbitConfig's SimpleRetryPolicy.
 *
 * Transient failures (TemplateRenderingException, MailException) propagate as retryable,
 * allowing Spring Retry to re-invoke this listener per the backoff policy.
 */
@Slf4j
@Component
public class NotificationConsumerService {

    private final NotificationDeliveryRepository notificationDeliveryRepository;
    private final NotificationTemplateService notificationTemplateService;
    private final EmailSenderService emailSenderService;
    private final ObjectMapper objectMapper;

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

    /**
     * Listen to booking lifecycle events: booking.created, booking.approved, booking.denied
     */
    @RabbitListener(queues = "notifications.booking.q")
    public void onBookingEvent(
            Message message,
            @Header("idempotency_key") String idempotencyKey,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        processEvent(message, idempotencyKey, routingKey);
    }

    /**
     * Listen to password reset events
     */
    @RabbitListener(queues = "notifications.passwordreset.q")
    public void onPasswordResetEvent(
            Message message,
            @Header("idempotency_key") String idempotencyKey,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        processEvent(message, idempotencyKey, routingKey);
    }

    /**
     * Process a notification event with lookup-first idempotency pattern.
     *
     * @param message the AMQP message containing the event payload
     * @param idempotencyKey unique key for this event (from message header)
     * @param routingKey the routing key (event type)
     * @throws MissingIdempotencyKeyException if idempotency_key is missing/blank (non-retryable)
     * @throws UnresolvableRecipientException if recipient cannot be resolved (non-retryable)
     * @throws Exception on template rendering or SMTP send failures (retryable)
     */
    private void processEvent(Message message, String idempotencyKey, String routingKey) {
        // Non-retryable: validate idempotency key
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new MissingIdempotencyKeyException(
                "event missing idempotency_key header, routing_key=" + routingKey
            );
        }

        // Parse event payload
        JsonNode eventData;
        try {
            eventData = objectMapper.readTree(message.getBody());
        } catch (Exception e) {
            log.error("Failed to parse event JSON for idempotency_key={}", idempotencyKey, e);
            throw new RuntimeException("Failed to parse event JSON", e);
        }

        UUID entityId = extractEntityId(eventData);

        // Lookup-first idempotency pattern: check if already processed
        NotificationDelivery row = notificationDeliveryRepository.findByIdempotencyKey(idempotencyKey)
            .orElse(null);

        if (row != null) {
            if ("sent".equals(row.getStatus())) {
                // Already successfully processed — no-op
                log.info("Idempotent no-op for idempotency_key={}", idempotencyKey);
                return;
            }
            // Existing row with status pending/retrying: proceed to re-attempt send
            log.debug("Retrying send for idempotency_key={} (current status={})", 
                idempotencyKey, row.getStatus());
        } else {
            // First time: create new row
            row = new NotificationDelivery();
            row.setIdempotencyKey(idempotencyKey);
            row.setEventType(routingKey);
            row.setEntityId(entityId);
            row.setStatus("pending");
            row.setAttemptCount(0);
            notificationDeliveryRepository.save(row);
            log.debug("Created delivery record for idempotency_key={}", idempotencyKey);
        }

        // Non-retryable: resolve recipient
        String recipient = extractRecipient(routingKey, eventData);
        if (recipient == null || recipient.isBlank()) {
            throw new UnresolvableRecipientException(
                "no resolvable recipient for idempotency_key=" + idempotencyKey
            );
        }

        // Retryable: render template
        NotificationTemplateService.RenderedEmail rendered =
            notificationTemplateService.render(routingKey, eventData);

        // Retryable: send email
        emailSenderService.send(recipient, rendered.subject(), rendered.body());

        // Success: update status
        row.setStatus("sent");
        row.setAttemptCount(row.getAttemptCount() + 1);
        row.setLastAttemptedAt(Instant.now());
        notificationDeliveryRepository.save(row);
        log.info("Notification sent successfully for idempotency_key={}", idempotencyKey);
    }

    /**
     * Extract recipient email from event data based on event type.
     *
     * @param routingKey the event type
     * @param eventData the event payload
     * @return recipient email or null if not found
     */
    private String extractRecipient(String routingKey, JsonNode eventData) {
        if ("password.reset.requested".equals(routingKey)) {
            return textOrNull(eventData, "email");
        } else {
            // booking.* events use contact_email
            return textOrNull(eventData, "contact_email");
        }
    }

    /**
     * Extract entity ID from event data, with fallback to a zero UUID if absent.
     *
     * @param eventData the event payload
     * @return entity UUID or zero UUID if not found
     */
    private UUID extractEntityId(JsonNode eventData) {
        JsonNode idNode = eventData.get("id");
        if (idNode != null && !idNode.isNull()) {
            try {
                return UUID.fromString(idNode.asText());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid UUID in event id field: {}", idNode.asText());
            }
        }
        log.warn("No entity id in event payload, using zero UUID");
        return new UUID(0, 0);
    }

    /**
     * Extract text field from event data, returning null if absent/null/blank.
     */
    private String textOrNull(JsonNode eventData, String fieldName) {
        JsonNode node = eventData.get(fieldName);
        if (node == null || node.isNull()) {
            return null;
        }
        String text = node.asText();
        return text.isBlank() ? null : text;
    }
}
