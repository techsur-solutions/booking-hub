package com.bookinghub.customfield.outbox;

import com.bookinghub.customfield.domain.OutboxEvent;
import com.bookinghub.customfield.repository.OutboxEventRepository;
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
 * Outbox-to-RabbitMQ polling publisher (F5's event relay).
 *
 * Identical shape to every other service's OutboxPublisher this phase
 * (users-permissions-service's being the precedent-setting first implementation,
 * Phase 3 plan 03-05): every outbox.poll-interval-ms, polls for pending
 * OutboxEvent rows and publishes them to RabbitMQ using the exchange and
 * routing key stored in each event. Six possible routing_key values for this
 * service: customfield.created/updated/deleted, template.created/updated/deleted,
 * all on the SAME already-declared customfield.events exchange (Phase 2 plan
 * 02-09) — this bean declares NO new RabbitMQ topology.
 *
 * On AmqpException: increments attemptCount, leaves status='pending' for retry
 * on next poll — RabbitMQ being down delays, never drops, the event (TechArch
 * Y3 Publisher guarantee).
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
                event.incrementAttemptCount();
                // status stays "pending" — next poll retries
                outboxEventRepository.save(event);
            }
        }
    }
}
