package com.bookinghub.settings.outbox;

import com.bookinghub.settings.domain.OutboxEvent;
import com.bookinghub.settings.repository.OutboxEventRepository;
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
 * Integration test for OutboxPublisher.
 *
 * Uses the running docker-compose RabbitMQ (localhost:5672, guest:guest) and
 * Postgres (settings_db_test) instead of Testcontainers — same established
 * workaround as SettingsSingletonTest/SettingsControllerIntegrationTest: this
 * sandbox's Docker daemon API version is incompatible with Testcontainers'
 * bundled client. This is a substrate swap, not a mock — the publisher
 * really connects to a real RabbitMQ broker and the assertions consume a
 * real message off a real queue.
 *
 * Proves the full event chain: a pending OutboxEvent row is actually
 * delivered to its declared exchange/routing-key with the correct
 * idempotency_key header, and is marked published.
 */
@SpringBootTest
@ActiveProfiles("test")
class OutboxPublisherTest {

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private OutboxPublisher outboxPublisher;

    @BeforeEach
    void cleanOutbox() {
        outboxEventRepository.deleteAll();
    }

    /**
     * Scenario 1: successful publish. Insert a pending OutboxEvent row with a
     * dedicated test exchange/queue → invoke relayPendingEvents() → assert
     * the row is marked published AND a real consumer bound to that
     * exchange/routing-key actually receives the message with the correct
     * idempotency_key header.
     */
    @Test
    void testSuccessfulPublish() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            String exchange = "test.settings.events." + UUID.randomUUID();
            String routingKey = "test.settings.updated";
            String queueName = "test.settings.queue." + UUID.randomUUID();

            channel.exchangeDeclare(exchange, "topic", false, true, null);
            channel.queueDeclare(queueName, false, false, true, null);
            channel.queueBind(queueName, exchange, routingKey);

            String payload = "{\"approve_booking\":false}";
            OutboxEvent event = new OutboxEvent(
                    "settings",
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

            // Postgres's jsonb column re-serializes with a space after each colon,
            // so compare against the re-fetched entity's payload (what the
            // publisher actually read and sent), not the original pre-persist
            // literal — this is a whitespace normalization, not a content change.
            String receivedPayload = new String(response.getBody(), StandardCharsets.UTF_8);
            assertThat(receivedPayload).isEqualTo(published.getPayload());
            assertThat(receivedPayload).contains("\"approve_booking\"").contains("false");

            String receivedIdempotencyKey = response.getProps()
                    .getHeaders()
                    .get("idempotency_key")
                    .toString();
            assertThat(receivedIdempotencyKey).isEqualTo(idempotencyKey.toString());

            channel.exchangeDelete(exchange);
        }
    }

    /**
     * Scenario 2: a pending event targeting the REAL already-declared
     * settings.events exchange (Phase 2 plan 02-09) with the real
     * settings.updated routing key round-trips correctly — proving this
     * publisher works against the actual production topology, not just a
     * disposable test exchange.
     */
    @Test
    void testPublishesToRealSettingsEventsExchange() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // Bind a throwaway queue to the REAL settings.events exchange so we
            // can observe delivery without disturbing the already-declared
            // audit.settings.q binding.
            String queueName = "test.settings.observer." + UUID.randomUUID();
            channel.queueDeclare(queueName, false, false, true, null);
            channel.queueBind(queueName, "settings.events", "settings.updated");

            String payload = "{\"calendar_slot_size\":15}";
            OutboxEvent event = new OutboxEvent(
                    "settings",
                    UUID.randomUUID(),
                    "settings.events",
                    "settings.updated",
                    payload
            );
            outboxEventRepository.save(event);

            outboxPublisher.relayPendingEvents();

            OutboxEvent published = outboxEventRepository.findById(event.getId()).orElseThrow();
            assertThat(published.getStatus()).isEqualTo("published");

            // The real OutboxPublisher bean's own @Scheduled poll (every 2s,
            // @EnableScheduling) runs concurrently with this manual invocation
            // in the same live Spring context, so delivery can race slightly
            // behind our manual relayPendingEvents() call returning — poll
            // basicGet with a short retry loop rather than asserting on the
            // very first attempt.
            GetResponse response = pollForMessage(channel, queueName);
            assertThat(response).as("message delivered to settings.events/settings.updated").isNotNull();
            // Compare against the re-fetched entity's payload (postgres's jsonb
            // column re-serializes with whitespace differences from the literal).
            assertThat(new String(response.getBody(), StandardCharsets.UTF_8))
                    .isEqualTo(published.getPayload());
        }
    }

    private GetResponse pollForMessage(Channel channel, String queueName) throws IOException {
        for (int attempt = 0; attempt < 20; attempt++) {
            GetResponse response = channel.basicGet(queueName, true);
            if (response != null) {
                return response;
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null;
    }
}
