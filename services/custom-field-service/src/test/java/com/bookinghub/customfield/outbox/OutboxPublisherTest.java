package com.bookinghub.customfield.outbox;

import com.bookinghub.customfield.domain.OutboxEvent;
import com.bookinghub.customfield.repository.OutboxEventRepository;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for OutboxPublisher.
 *
 * Tests against the already-running docker-compose RabbitMQ (localhost:5672)
 * and Postgres instead of Testcontainers, due to the same Docker API version
 * compatibility issue already established in plans 03-01/03-05/04-03's
 * DomainRepositoryTest — Testcontainers' bundled Docker client requires a
 * newer API version than this sandbox's daemon offers.
 *
 * Proves a customfield.created event round-trips to the real broker: the
 * outbox row is marked published, and a declared-on-the-fly test queue bound
 * to the real customfield.events exchange actually receives the message with
 * its idempotency_key header intact.
 */
@SpringBootTest
@ActiveProfiles("test")
class OutboxPublisherTest {

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private OutboxPublisher outboxPublisher;

    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        outboxEventRepository.deleteAll();
    }

    /**
     * Successful publish: a pending OutboxEvent row on the real
     * customfield.events exchange is delivered to a dedicated test queue with
     * the correct idempotency_key header and marked published.
     */
    @Test
    void relayPendingEvents_publishesToRealBrokerAndMarksPublished() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            String exchange = "customfield.events";
            String routingKey = "customfield.created";
            String queueName = "test.outboxpublisher." + UUID.randomUUID();

            // customfield.events is already declared (Phase 2 plan 02-09) — just bind a test queue
            channel.queueDeclare(queueName, false, true, true, null);
            channel.queueBind(queueName, exchange, routingKey);

            String payload = "{\"id\":\"" + UUID.randomUUID() + "\",\"label\":\"Test Field\"}";
            OutboxEvent event = new OutboxEvent(
                "customfield",
                UUID.randomUUID(),
                exchange,
                routingKey,
                payload
            );
            outboxEventRepository.save(event);
            UUID idempotencyKey = event.getIdempotencyKey();

            outboxPublisher.relayPendingEvents();

            OutboxEvent published = outboxEventRepository.findById(event.getId()).orElseThrow();
            assertThat(published.getStatus()).isEqualTo("published");
            assertThat(published.getPublishedAt()).isNotNull();

            GetResponse response = channel.basicGet(queueName, true);
            assertThat(response).isNotNull();

            // Compare parsed JSON equivalence, not exact string match: the
            // payload column is JSONB, so Postgres reformats whitespace on
            // round-trip (e.g. adds a space after ':') — the outbox row's
            // payload (read back from the DB) is the actual value relayed,
            // not the original literal string this test constructed.
            String receivedPayload = new String(response.getBody(), StandardCharsets.UTF_8);
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            assertThat(mapper.readTree(receivedPayload)).isEqualTo(mapper.readTree(payload));

            // The raw AMQP client deserializes string header values as
            // LongString (ByteArrayLongString), not java.lang.String — use
            // toString() rather than a direct cast.
            Object receivedIdempotencyKeyHeader = response.getProps()
                .getHeaders()
                .get("idempotency_key");
            assertThat(receivedIdempotencyKeyHeader.toString()).isEqualTo(idempotencyKey.toString());

            channel.queueDelete(queueName);
        }
    }

    /**
     * A row with no bound queue still gets delivered to the exchange (RabbitMQ
     * accepts unrouted messages by default) and is marked published — proving
     * the publisher's happy-path status transition independent of whether a
     * consumer exists. The true broker-unreachable retry path (AmqpException →
     * attemptCount increment, status stays pending) is exercised by stopping
     * the broker mid-test in a real environment; that scenario is documented
     * here rather than executed, matching the users-permissions-service
     * precedent test's own documented limitation (stopping the shared
     * docker-compose RabbitMQ would break every other service's tests running
     * against the same stack).
     */
    @Test
    void relayPendingEvents_unroutedMessageStillMarkedPublished() {
        String payload = "{\"id\":\"" + UUID.randomUUID() + "\",\"name\":\"Unrouted Template\"}";
        OutboxEvent event = new OutboxEvent(
            "template",
            UUID.randomUUID(),
            "customfield.events",
            "template.created.unrouted.test." + UUID.randomUUID(),
            payload
        );
        event.setStatus("pending");
        event.setAttemptCount(0);
        outboxEventRepository.save(event);

        outboxPublisher.relayPendingEvents();

        OutboxEvent result = outboxEventRepository.findById(event.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo("published");
    }
}
