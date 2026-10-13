package com.bookinghub.feeds.consumer;

import com.bookinghub.feeds.rabbit.MissingIdempotencyKeyException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;

/**
 * RabbitMQ projection consumer for feeds-service.
 *
 * Exactly 2 @RabbitListener methods, one per already-declared queue
 * (infra/rabbitmq/definitions.json): feeds.booking.q and feeds.location.q. Both
 * share the lookup-first idempotency guard and delegate the actual projection to
 * {@link FeedProjectionService}.
 *
 * ---
 * TRANSACTION BOUNDARY (deliberately NOT on these listener methods):
 * The idempotency check + projection mutation + processed_events insert run together
 * inside FeedProjectionService.apply*EventIdempotent(...), which carries the single
 * @Transactional boundary. Keeping THIS consumer non-transactional is what lets a
 * MissingIdempotencyKeyException propagate UNWRAPPED to plan 07-01's retry interceptor,
 * so its non-retryable classification dead-letters the message immediately rather than
 * burning the full 5s/30s/2min retry backoff. (Established Phase 6 pattern:
 * audit-log-service's listener is non-transactional; its DB-mutating service method is.)
 *
 * IDEMPOTENCY (lookup-first, not insert-then-catch):
 * plan 07-01's rabbitListenerContainerFactory retry interceptor re-invokes the listener
 * on a transient failure, so an insert-first/catch-duplicate approach would misfire on
 * its own retry. FeedProjectionService checks existsByIdempotencyKey BEFORE applying; the
 * processed_events PK (plan 07-01) is the final race-safe backstop.
 *
 * MISSING KEY -> DLQ:
 * A missing/blank idempotency_key throws MissingIdempotencyKeyException
 * (AmqpRejectAndDontRequeueException + registered non-retryable in RabbitConfig), so the
 * broker's x-dead-letter-exchange routes it straight to feeds.*.q.dlq — never silently
 * dropped, never written as a corrupt projection row.
 *
 * NO SECURITY: feeds-service is the intentionally-public service (F9.6) — this consumer
 * adds no auth of any kind.
 */
@Component
public class FeedEventConsumer {

    private final FeedProjectionService projectionService;
    private final ObjectMapper objectMapper;

    public FeedEventConsumer(FeedProjectionService projectionService,
                             ObjectMapper objectMapper) {
        this.projectionService = projectionService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "feeds.booking.q")
    public void onBookingEvent(
            Message message,
            @Header(name = "idempotency_key", required = false) String idempotencyKey,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        UUID key = requireKey(idempotencyKey, routingKey);
        JsonNode body = parseBody(message, routingKey);
        projectionService.applyBookingEventIdempotent(key, routingKey, body);
    }

    @RabbitListener(queues = "feeds.location.q")
    public void onLocationEvent(
            Message message,
            @Header(name = "idempotency_key", required = false) String idempotencyKey,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        UUID key = requireKey(idempotencyKey, routingKey);
        JsonNode body = parseBody(message, routingKey);
        projectionService.applyLocationEventIdempotent(key, routingKey, body);
    }

    private UUID requireKey(String idempotencyKey, String routingKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            // Non-retryable (RabbitConfig) -> straight to feeds.*.q.dlx -> *.dlq, never dropped.
            throw new MissingIdempotencyKeyException(
                    "event missing idempotency_key header, routing_key=" + routingKey);
        }
        return UUID.fromString(idempotencyKey);
    }

    private JsonNode parseBody(Message message, String routingKey) {
        try {
            return objectMapper.readTree(message.getBody());
        } catch (IOException ex) {
            // Malformed JSON can never succeed: unchecked so the broker dead-letters it
            // (RejectAndDontRequeueRecoverer) rather than hot-looping.
            throw new UncheckedIOException(
                    "unparseable event body, routing_key=" + routingKey, ex);
        }
    }
}
