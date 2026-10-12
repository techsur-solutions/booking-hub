package com.bookinghub.notifications.consumer;

import com.bookinghub.notifications.domain.NotificationDelivery;
import com.bookinghub.notifications.email.EmailSenderService;
import com.bookinghub.notifications.rabbit.DatabaseTrackingMessageRecoverer;
import com.bookinghub.notifications.rabbit.MissingIdempotencyKeyException;
import com.bookinghub.notifications.rabbit.UnresolvableRecipientException;
import com.bookinghub.notifications.repository.NotificationDeliveryRepository;
import com.bookinghub.notifications.template.NotificationTemplateService;
import com.bookinghub.notifications.template.NotificationTemplateService.RenderedEmail;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Consumes booking and password-reset events and turns them into emails, durably.
 *
 * <p>This class is where F8's core guarantee becomes executable: every event ends in a
 * sent email or an explicitly recorded dead-letter — never a silent drop, and never a
 * duplicate send.
 *
 * <h2>Idempotency: lookup-first, not insert-first</h2>
 *
 * <p>The obvious implementation — insert the row and treat a unique-constraint violation
 * as "already processed" — is subtly and seriously wrong here, and plan 06-02 calls this
 * out explicitly. Spring Retry re-invokes the <i>entire listener method</i> on the same
 * message (the interceptor wraps the whole call). So on attempt #2 after a transient SMTP
 * failure, the insert would collide with the row attempt #1 already wrote, be
 * misinterpreted as a duplicate delivery, and return early — acking a message whose email
 * was never sent. The retry policy would appear to work while silently guaranteeing
 * failure.
 *
 * <p>So the row is looked up first and the three cases are distinguished explicitly:
 *
 * <ul>
 *   <li>{@code sent} → genuine redelivery of an already-handled event. No-op, ack.</li>
 *   <li>{@code pending}/{@code retrying}/{@code dead_lettered} → this is a retry of
 *       <i>this</i> message. Proceed with the send, incrementing the attempt count.</li>
 *   <li>absent → genuinely new. Insert at {@code pending} and proceed.</li>
 * </ul>
 *
 * <p>The lookup cannot close the race between two <i>truly concurrent</i> deliveries of
 * the same message — both could look up, both miss, both insert. That remaining window is
 * closed by the table's UNIQUE constraint on {@code idempotency_key}: the loser catches
 * {@link DataIntegrityViolationException} and treats it as "already processed", exactly as
 * the insert-first approach would have. Lookup-first for the common path, constraint for
 * the race.
 *
 * <h2>Failure routing</h2>
 *
 * <ul>
 *   <li><b>Non-retryable</b> ({@link MissingIdempotencyKeyException},
 *       {@link UnresolvableRecipientException}) — structurally impossible to ever succeed,
 *       so they skip the retry budget entirely and go straight to the DLQ.</li>
 *   <li><b>Retryable</b> (SMTP outage, template rendering, malformed JSON) — the 4-attempt
 *       5s/30s/2min policy runs; on exhaustion
 *       {@code DatabaseTrackingMessageRecoverer} records {@code dead_lettered} and rejects
 *       to the DLQ.</li>
 * </ul>
 *
 * <p>Nothing is ever caught-and-swallowed. An exception reaching the container is what
 * drives retry and DLQ routing; catching one would ack an undelivered message.
 */
@Component
public class NotificationConsumerService {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumerService.class);

    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_RETRYING = "retrying";
    private static final String STATUS_SENT = "sent";

    /**
     * Fallback for an event carrying no usable entity id. The column is NOT NULL, but a
     * missing id must not stop the email: the recipient cares about the booking, not its
     * primary key. Using an all-zero sentinel keeps the row insertable and makes the
     * anomaly obvious to anyone reading the table, rather than failing the send outright.
     */
    private static final UUID UNKNOWN_ENTITY_ID = new UUID(0L, 0L);

    private final NotificationDeliveryRepository notificationDeliveryRepository;
    private final NotificationTemplateService notificationTemplateService;
    private final EmailSenderService emailSenderService;
    private final DatabaseTrackingMessageRecoverer databaseTrackingMessageRecoverer;
    private final ObjectMapper objectMapper;

    public NotificationConsumerService(
            NotificationDeliveryRepository notificationDeliveryRepository,
            NotificationTemplateService notificationTemplateService,
            EmailSenderService emailSenderService,
            DatabaseTrackingMessageRecoverer databaseTrackingMessageRecoverer,
            ObjectMapper objectMapper) {
        this.notificationDeliveryRepository = notificationDeliveryRepository;
        this.notificationTemplateService = notificationTemplateService;
        this.emailSenderService = emailSenderService;
        this.databaseTrackingMessageRecoverer = databaseTrackingMessageRecoverer;
        this.objectMapper = objectMapper;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // LISTENERS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Handles {@code booking.created}, {@code booking.approved} and {@code booking.denied}
     * (all three are bound to this one queue by infra/rabbitmq/definitions.json).
     *
     * <p>{@code idempotency_key} is declared {@code required = false} so that a message
     * missing the header reaches the method body and raises
     * {@link MissingIdempotencyKeyException} — a typed, DLQ-routed failure. Declaring it
     * required would instead produce a header-conversion error before the body ran,
     * turning a specified error state into a generic listener failure.
     */
    @RabbitListener(queues = "notifications.booking.q")
    public void onBookingEvent(Message message,
                               @Header(name = "idempotency_key", required = false) String idempotencyKey,
                               @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        processEvent(message, idempotencyKey, routingKey);
    }

    /** Handles {@code password.reset.requested}. */
    @RabbitListener(queues = "notifications.passwordreset.q")
    public void onPasswordResetEvent(Message message,
                                     @Header(name = "idempotency_key", required = false) String idempotencyKey,
                                     @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        processEvent(message, idempotencyKey, routingKey);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SHARED PROCESSING
    // ═══════════════════════════════════════════════════════════════════════════

    private void processEvent(Message message, String idempotencyKey, String routingKey) {
        // ── Gate 1: no idempotency key → cannot deduplicate, cannot track. Non-retryable.
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new MissingIdempotencyKeyException(
                    "event missing idempotency_key header, routing_key=" + routingKey);
        }

        JsonNode eventData = parsePayload(message, idempotencyKey);

        // ── Gate 2: idempotency boundary (see class javadoc for why lookup-first).
        Optional<NotificationDelivery> existing =
                notificationDeliveryRepository.findByIdempotencyKey(idempotencyKey);

        NotificationDelivery row;
        if (existing.isPresent()) {
            row = existing.get();
            if (STATUS_SENT.equals(row.getStatus())) {
                log.info("duplicate delivery suppressed (already sent) for idempotency_key={}",
                        idempotencyKey);
                return; // ack as a no-op — never send a second email
            }
            // pending / retrying / dead_lettered: this is a retry of THIS message.
            log.debug("resuming delivery in status={} for idempotency_key={}",
                    row.getStatus(), idempotencyKey);
        } else {
            row = newRow(idempotencyKey, routingKey, eventData);
            try {
                notificationDeliveryRepository.saveAndFlush(row);
            } catch (DataIntegrityViolationException concurrentInsert) {
                // Lost the race against a concurrent delivery of the same message. The
                // winner owns this send; treat as already processed rather than risking
                // a duplicate email.
                log.info("duplicate delivery suppressed (concurrent insert) for idempotency_key={}",
                        idempotencyKey);
                return;
            }
        }

        // ── Gate 3: recipient resolution (named decision — read from the event payload,
        // never a synchronous call to another service).
        String recipient = resolveRecipient(routingKey, eventData);
        if (recipient == null || recipient.isBlank()) {
            // FRD F8 Error States names this exact case: "Recipient address cannot be
            // resolved... Route to DLQ". Record it before throwing, because the
            // non-retryable exception bypasses the retry interceptor's recoverer entirely
            // and the row would otherwise sit at 'pending' forever.
            databaseTrackingMessageRecoverer.markDeadLetteredByKey(
                    idempotencyKey, "unresolvable recipient for routing_key=" + routingKey);
            throw new UnresolvableRecipientException(
                    "no resolvable recipient for idempotency_key=" + idempotencyKey);
        }

        // ── Mark the attempt BEFORE the send, so a crash mid-send still leaves evidence
        // that an attempt was made rather than a row that looks untouched.
        row.setStatus(STATUS_RETRYING);
        row.setAttemptCount(row.getAttemptCount() == null ? 1 : row.getAttemptCount() + 1);
        row.setLastAttemptedAt(Instant.now());
        notificationDeliveryRepository.saveAndFlush(row);

        // ── Render + send. Both may throw; both are retryable by design, which hands
        // control to the retry/backoff policy rather than losing the notification.
        RenderedEmail rendered = notificationTemplateService.render(routingKey, eventData);
        emailSenderService.send(recipient, rendered.subject(), rendered.body());

        // ── Success.
        row.setStatus(STATUS_SENT);
        row.setLastAttemptedAt(Instant.now());
        notificationDeliveryRepository.saveAndFlush(row);

        log.info("notification sent for event_type={}, idempotency_key={}, attempt={}",
                routingKey, idempotencyKey, row.getAttemptCount());
    }

    private NotificationDelivery newRow(String idempotencyKey, String routingKey, JsonNode eventData) {
        NotificationDelivery row = new NotificationDelivery();
        row.setIdempotencyKey(idempotencyKey);
        row.setEventType(routingKey);
        row.setEntityId(extractEntityId(eventData, idempotencyKey));
        row.setStatus(STATUS_PENDING);
        row.setAttemptCount(0);
        return row;
    }

    /**
     * Resolves the recipient from the event's own payload.
     *
     * <p><b>Named decision (plan 06-02 objective).</b> {@code password.reset.requested}
     * carries {@code email} (FRD F8 §Inputs states this literally); {@code booking.*}
     * carries {@code contact_email}, the persisted {@code bookings.contact_email} column.
     * No cross-service call is made to resolve an address: TechArch §2.6 architects this
     * service as a pure consumer, and a notification must never fail because the user
     * service is unavailable.
     */
    private String resolveRecipient(String routingKey, JsonNode eventData) {
        if ("password.reset.requested".equals(routingKey)) {
            return text(eventData, "email");
        }
        return text(eventData, "contact_email");
    }

    /**
     * Parses the message body.
     *
     * <p>Wrapped into a retryable {@link IllegalStateException} rather than being allowed
     * to surface as a raw Jackson error: T-06-02-01 in this plan's threat model requires
     * that untrusted payload shapes can never crash or wedge the listener container. A
     * malformed body follows the ordinary retry-then-DLQ path like any other processing
     * failure.
     */
    private JsonNode parsePayload(Message message, String idempotencyKey) {
        try {
            return objectMapper.readTree(new String(message.getBody(), StandardCharsets.UTF_8));
        } catch (Exception malformed) {
            throw new IllegalStateException(
                    "unparseable event payload for idempotency_key=" + idempotencyKey, malformed);
        }
    }

    /**
     * Extracts the aggregate id. Never throws — a missing entity id is a tracking-quality
     * problem, not a reason to withhold someone's email. Falls back to
     * {@link #UNKNOWN_ENTITY_ID} with a warning.
     */
    private UUID extractEntityId(JsonNode eventData, String idempotencyKey) {
        String raw = text(eventData, "id");
        if (raw == null || raw.isBlank()) {
            log.warn("event has no 'id' field; recording entity_id as zero-UUID for idempotency_key={}",
                    idempotencyKey);
            return UNKNOWN_ENTITY_ID;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException notAUuid) {
            log.warn("event 'id' is not a UUID ('{}'); recording entity_id as zero-UUID for idempotency_key={}",
                    raw, idempotencyKey);
            return UNKNOWN_ENTITY_ID;
        }
    }

    /** Null-safe text extraction that never yields the literal string "null". */
    private String text(JsonNode data, String field) {
        if (data == null) {
            return null;
        }
        JsonNode node = data.get(field);
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        return node.asText();
    }
}
