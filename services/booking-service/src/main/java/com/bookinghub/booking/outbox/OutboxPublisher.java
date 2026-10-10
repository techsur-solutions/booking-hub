package com.bookinghub.booking.outbox;

import com.bookinghub.booking.domain.OutboxEvent;
import com.bookinghub.booking.repository.OutboxEventRepository;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

/**
 * Outbox-to-RabbitMQ polling publisher for booking events.
 *
 * Identical shape to every other service's publisher in this project (Phase 3
 * plan 03-05, Phase 4 plans 04-02/04-04/04-06, locations-resources-service):
 * scheduled polling bean, not Debezium CDC.
 *
 * Created in plan 05-01 (before any business logic exists) specifically so this
 * component is independently testable — plans 05-03 and 05-04 run in the SAME
 * wave in parallel and both need to publish outbox rows; centralizing the
 * publisher here avoids a file-ownership race.
 *
 * Publishes to the already-declared booking.events exchange (Phase 2 plan 02-09)
 * using the routing_key stored in each row — no new RabbitMQ topology declared here.
 *
 * On AmqpException: leaves row status='pending', increments attemptCount — delays,
 * never drops the event (TechArch Y3 Publisher guarantee, proven by OutboxPublisherTest
 * against a real Testcontainers broker using a directly-inserted row).
 */
@Component
@EnableScheduling
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;

    public OutboxPublisher(OutboxEventRepository outboxEventRepository,
                            RabbitTemplate rabbitTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Polls for pending outbox events and publishes them to RabbitMQ.
     *
     * Runs every outbox.poll-interval-ms (default 2000ms). Processes up to
     * 100 events per batch (findTop100ByStatusOrderByCreatedAtAsc).
     *
     * On successful publish: marks event status='published' and sets publishedAt.
     * On AmqpException: increments attemptCount, leaves status='pending' for retry
     * on next poll — RabbitMQ being down delays, never drops, the event.
     */
    @Scheduled(fixedDelayString = "${outbox.poll-interval-ms}")
    @Transactional
    public void relayPendingEvents() {
        List<OutboxEvent> batch = outboxEventRepository
            .findTop100ByStatusOrderByCreatedAtAsc("pending");

        for (OutboxEvent event : batch) {
            try {
                MessageProperties props = MessagePropertiesBuilder.newInstance()
                    .setHeader("idempotency_key", event.getIdempotencyKey().toString())
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .build();

                Message message = new Message(
                    event.getPayload().getBytes(StandardCharsets.UTF_8),
                    props
                );

                rabbitTemplate.send(event.getExchange(), event.getRoutingKey(), message);

                event.setStatus("published");
                event.setPublishedAt(Instant.now());

            } catch (AmqpException ex) {
                // RabbitMQ unreachable: increment attempt count, leave status
                // pending. Next poll will retry — delays event, never loses it.
                event.incrementAttemptCount();
            }

            outboxEventRepository.save(event);
        }
    }
}
