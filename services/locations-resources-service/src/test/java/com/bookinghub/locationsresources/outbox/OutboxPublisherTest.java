package com.bookinghub.locationsresources.outbox;

import com.bookinghub.locationsresources.domain.OutboxEvent;
import com.bookinghub.locationsresources.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
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
 * Integration test for OutboxPublisher (F4's event relay).
 *
 * Runs against the real docker-compose RabbitMQ broker (localhost:5672, see
 * application-test.properties) rather than Testcontainers' RabbitMQContainer
 * — same Docker-API-version-incompatibility workaround as the Postgres tests
 * in this service (SchemaCompletionTest, LocationControllerIntegrationTest,
 * ResourceControllerIntegrationTest). Uses dedicated test.* exchange/routing-key
 * names (declared ad hoc via the raw RabbitMQ client, same pattern as Phase 3
 * plan 03-05's OutboxPublisherTest) rather than the real location.events/
 * resource.events topology, so this test never collides with the shared
 * broker's real queues/bindings.
 *
 * Proves:
 * 1. A pending row is actually delivered to its declared exchange/routing-key
 *    with the correct idempotency_key header and marked published.
 * 2. relayPendingEvents() is safe to invoke directly (bypassing the
 *    @Scheduled trigger, which is slowed to 10 minutes in the test profile
 *    specifically so it never races this test's explicit invocation).
 */
@SpringBootTest
@ActiveProfiles("test")
class OutboxPublisherTest {

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private OutboxPublisher outboxPublisher;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
    }

    /**
     * Scenario 1: Successful publish.
     *
     * Insert a pending OutboxEvent row (via LocationService's outbox-write
     * shape, directly via repository here for test isolation) -> invoke
     * relayPendingEvents() -> assert the row's status is now 'published' and
     * publishedAt is set -> assert (via a raw RabbitMQ consumer) that the
     * message was actually received with the correct idempotency_key header.
     */
    @Test
    void relayPendingEvents_publishesToRabbitMqAndMarksPublished() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            String exchange = "test.locationresources.events";
            String routingKey = "test.location.created";
            String queueName = "test.locationresources.queue";

            channel.exchangeDeclare(exchange, "topic", true);
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, exchange, routingKey);

            String payload = "{\"id\":\"" + UUID.randomUUID() + "\",\"name\":\"Test Location\"}";
            OutboxEvent event = new OutboxEvent(
                "location",
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

            // Compare semantically (deserialize both sides), not as exact
            // strings: the payload round-trips through a Postgres jsonb
            // column (OutboxEvent.payload is @JdbcTypeCode(SqlTypes.JSON)),
            // and Postgres's jsonb storage normalizes whitespace (e.g. adds a
            // space after each ':'), so the byte-for-byte string the
            // publisher reads back from the DB and republishes is NOT
            // guaranteed to equal the exact literal this test wrote — only
            // its parsed JSON content is.
            String receivedPayload = new String(response.getBody(), StandardCharsets.UTF_8);
            assertThat(objectMapper.readTree(receivedPayload))
                .isEqualTo(objectMapper.readTree(payload));

            // The RabbitMQ Java client decodes an AMQP table header value as
            // its own LongString type (not java.lang.String) when read back
            // via the raw client API used here — .toString() on it yields the
            // header's text content.
            Object receivedIdempotencyKeyHeader = response.getProps()
                .getHeaders()
                .get("idempotency_key");
            assertThat(receivedIdempotencyKeyHeader.toString()).isEqualTo(idempotencyKey.toString());

            // Cleanup
            channel.queueDelete(queueName);
            channel.exchangeDelete(exchange);
        }
    }

    /**
     * Scenario 2: Multiple pending rows are all relayed in a single poll
     * (batch behavior) — proves findTop100ByStatusOrderByCreatedAtAsc("pending")
     * is actually wired into the scheduled method, not just a dangling
     * repository method.
     */
    @Test
    void relayPendingEvents_relaysMultiplePendingRowsInOneBatch() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            String exchange = "test.locationresources.batch.events";
            String routingKey = "test.resource.created";
            String queueName = "test.locationresources.batch.queue";

            channel.exchangeDeclare(exchange, "topic", true);
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, exchange, routingKey);

            for (int i = 0; i < 3; i++) {
                OutboxEvent event = new OutboxEvent(
                    "resource",
                    UUID.randomUUID(),
                    exchange,
                    routingKey,
                    "{\"seq\":" + i + "}"
                );
                outboxEventRepository.save(event);
            }

            outboxPublisher.relayPendingEvents();

            long publishedCount = outboxEventRepository.findAll().stream()
                .filter(e -> "published".equals(e.getStatus()))
                .count();
            assertThat(publishedCount).isEqualTo(3);

            // Drain all 3 messages
            int received = 0;
            for (int i = 0; i < 3; i++) {
                GetResponse response = channel.basicGet(queueName, true);
                if (response != null) {
                    received++;
                }
            }
            assertThat(received).isEqualTo(3);

            channel.queueDelete(queueName);
            channel.exchangeDelete(exchange);
        }
    }
}
