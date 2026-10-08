package com.bookinghub.userspermissions.outbox;

import com.bookinghub.userspermissions.domain.OutboxEvent;
import com.bookinghub.userspermissions.repository.OutboxEventRepository;
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
 * Outbox-to-RabbitMQ polling publisher (F7.3 event relay).
 * 
 * Implements the outbox pattern's relay component: every 2 seconds, polls for
 * pending OutboxEvent rows and publishes them to RabbitMQ using the exchange
 * and routing key stored in each event.
 * 
 * NAMED TECH DECISION (TechArch §5-tech-stack explicitly offers a choice):
 * This plan chooses a scheduled polling-publisher bean, NOT Debezium CDC.
 * Simpler, zero new infrastructure (no Debezium Connect cluster, no Kafka
 * Connect), and this is the FIRST service in the codebase to implement the
 * outbox relay, setting the precedent other services' future phases should
 * follow unless a specific later service has a volume/latency reason to adopt
 * Debezium instead.
 * 
 * Exchange/routing-key values are read directly off each OutboxEvent row
 * (written by plans 03-03/03-04/03-05) — this bean declares NO new RabbitMQ
 * topology; it publishes to the exchanges/routing-keys Phase 2 plan 02-09
 * already declared (user.events, permission.events) verbatim.
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
     * Runs every 2 seconds (configurable via outbox.poll-interval-ms).
     * Processes up to 100 events per batch (configurable via outbox.batch-size).
     * 
     * On successful publish: marks event status='published' and sets publishedAt.
     * On AmqpException: increments attemptCount, leaves status='pending' for retry
     * on next poll — RabbitMQ being down delays, never drops, the event (TechArch
     * Y3 Publisher guarantee).
     */
    @Scheduled(fixedDelayString = "${outbox.poll-interval-ms}")
    @Transactional
    public void relayPendingEvents() {
        List<OutboxEvent> batch = outboxEventRepository
            .findTop100ByStatusOrderByCreatedAtAsc("pending");
        
        for (OutboxEvent event : batch) {
            try {
                // Build message with idempotency_key header for consumer deduplication
                MessageProperties props = MessagePropertiesBuilder.newInstance()
                    .setHeader("idempotency_key", event.getIdempotencyKey().toString())
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .build();
                
                Message message = new Message(
                    event.getPayload().getBytes(StandardCharsets.UTF_8),
                    props
                );
                
                // Publish to exchange/routing-key from outbox row
                rabbitTemplate.send(event.getExchange(), event.getRoutingKey(), message);
                
                // Mark as published
                event.setStatus("published");
                event.setPublishedAt(Instant.now());
                
            } catch (AmqpException ex) {
                // RabbitMQ unreachable: increment attempt count, leave status pending
                // Next poll will retry — delays event, never loses it
                event.incrementAttemptCount();
                // status stays "pending"
            }
            
            outboxEventRepository.save(event);
        }
    }
}
