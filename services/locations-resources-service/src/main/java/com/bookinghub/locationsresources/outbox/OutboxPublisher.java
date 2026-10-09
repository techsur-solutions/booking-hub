package com.bookinghub.locationsresources.outbox;

import com.bookinghub.locationsresources.domain.OutboxEvent;
import com.bookinghub.locationsresources.repository.OutboxEventRepository;
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
 * Outbox-to-RabbitMQ polling publisher (F4's event relay).
 *
 * Same shape as Phase 3 plan 03-05's OutboxPublisher (the established
 * precedent for this codebase's outbox-relay pattern — scheduled polling
 * bean, not Debezium CDC): every outbox.poll-interval-ms, polls for pending
 * OutboxEvent rows and publishes them to RabbitMQ using the exchange and
 * routing key stored in each event.
 *
 * NAMED TECH DECISION (TechArch §5-tech-stack explicitly offers a choice,
 * same as Phase 3 plan 03-05): scheduled polling-publisher, NOT Debezium CDC.
 *
 * Publishes to the already-declared location.events/resource.events
 * exchanges (Phase 2 plan 02-09) verbatim — no new RabbitMQ topology
 * declared here.
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
     * 100 events per batch (outbox.batch-size).
     *
     * On successful publish: marks event status='published' and sets
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

            } catch (AmqpException ex) {
                // RabbitMQ unreachable: increment attempt count, leave status
                // pending. Next poll will retry — delays event, never loses it.
                event.incrementAttemptCount();
            }

            outboxEventRepository.save(event);
        }
    }
}
