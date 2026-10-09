package com.bookinghub.settings.outbox;

import com.bookinghub.settings.domain.OutboxEvent;
import com.bookinghub.settings.repository.OutboxEventRepository;
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
 * Outbox-to-RabbitMQ polling publisher completing F10's event chain.
 *
 * Identical shape to every other service's OutboxPublisher this phase
 * (established precedent: Phase 3 plan 03-05's users-permissions-service
 * OutboxPublisher — scheduled polling bean, not Debezium CDC). Polls for
 * pending OutboxEvent rows and publishes them to the already-declared
 * settings.events exchange (Phase 2 plan 02-09) with routing key
 * settings.updated — this bean declares NO new RabbitMQ topology.
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
     * Runs every outbox.poll-interval-ms (2000ms), processes up to
     * outbox.batch-size (100) events per batch.
     *
     * On successful publish: marks event status='published', sets
     * publishedAt. On AmqpException: increments attemptCount, leaves
     * status='pending' for retry on next poll — RabbitMQ being down delays,
     * never drops, the event (TechArch Y3 Publisher guarantee).
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

                outboxEventRepository.save(event);

            } catch (AmqpException ex) {
                // RabbitMQ unreachable: increment attempt count, leave status pending.
                // Next poll will retry — delays event, never loses it.
                event.incrementAttemptCount();
                outboxEventRepository.save(event);
            }
        }
    }
}
