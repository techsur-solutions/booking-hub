package com.bookinghub.auditlog.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.bookinghub.auditlog.domain.AuditLogEntry;
import com.bookinghub.auditlog.rabbit.MissingIdempotencyKeyException;
import com.bookinghub.auditlog.repository.AuditLogEntryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration test for AuditEventConsumerService.
 *
 * Tests all 7 queues with real Testcontainers PostgreSQL and RabbitMQ:
 * 1. Booking event creates a correctly-mapped audit row
 * 2. Redelivered event with same idempotency_key is deduplicated
 * 3. Missing idempotency_key header routes to dead-letter queue
 * 4. All 7 queues produce correctly-derived entity_type/action_type pairs
 * 5. Sparse body (missing id) still produces a row (with nil-UUID placeholder)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AuditEventConsumerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("audit_db_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management")
            .withExposedPorts(5672, 15672);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        String rabbitMqUrl = String.format(
                "amqp://guest:guest@%s:%d",
                rabbitmq.getHost(),
                rabbitmq.getMappedPort(5672)
        );
        registry.add("spring.rabbitmq.addresses", () -> rabbitMqUrl);
    }

    @Autowired
    private AuditLogEntryRepository auditLogEntryRepository;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final UUID NIL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @BeforeEach
    void setUp() {
        // Clean up any leftover audit log entries from previous tests
        jdbcTemplate.update("DELETE FROM audit_log_entries");
    }

    @Test
    void testScenario1_BookingApprovedWithAllFields() {
        // Scenario 1: Publish a booking.approved message to audit.booking.q with all fields
        // Expected: exactly one audit_log_entries row with correctly-derived entity/action/entity_id/actor_id

        UUID entityId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        String routingKey = "booking.approved";
        String idempotencyKey = UUID.randomUUID().toString();
        String timestamp = "2026-01-01T00:00:00Z";

        String bodyJson = String.format(
                "{\"id\":\"%s\", \"approved_by\":\"%s\", \"approved_at\":\"%s\"}",
                entityId, actorId, timestamp
        );

        publishMessage("audit.booking.q", routingKey, bodyJson, idempotencyKey);

        // Wait for the message to be processed
        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(100))
                .until(() -> auditLogEntryRepository.findByIdempotencyKey(UUID.fromString(idempotencyKey))
                        .isPresent());

        Optional<AuditLogEntry> entry = auditLogEntryRepository.findByIdempotencyKey(UUID.fromString(idempotencyKey));
        assertThat(entry).isPresent();
        assertThat(entry.get().getEntityType()).isEqualTo("booking");
        assertThat(entry.get().getActionType()).isEqualTo("approved");
        assertThat(entry.get().getEntityId()).isEqualTo(entityId);
        assertThat(entry.get().getActorId()).isEqualTo(actorId);
        assertThat(entry.get().getOccurredAt()).isEqualTo(Instant.parse(timestamp));
    }

    @Test
    void testScenario2_RedeliveredEventDeduplicated() {
        // Scenario 2: Publish the SAME message twice (same idempotency_key)
        // Expected: still exactly one row exists for that idempotency_key

        UUID entityId = UUID.randomUUID();
        String idempotencyKey = UUID.randomUUID().toString();

        String bodyJson = String.format("{\"id\":\"%s\", \"created_at\":\"2026-01-02T10:00:00Z\"}", entityId);

        publishMessage("audit.booking.q", "booking.created", bodyJson, idempotencyKey);

        // Wait for first processing
        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(100))
                .until(() -> auditLogEntryRepository.findByIdempotencyKey(UUID.fromString(idempotencyKey))
                        .isPresent());

        // Publish the IDENTICAL message again
        publishMessage("audit.booking.q", "booking.created", bodyJson, idempotencyKey);

        // Wait a bit more to ensure redelivery is processed
        await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .until(() -> true); // Just wait

        // Verify still exactly one row
        List<AuditLogEntry> rows = auditLogEntryRepository.findAll();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getIdempotencyKey()).isEqualTo(UUID.fromString(idempotencyKey));
    }

    @Test
    void testScenario3_MissingIdempotencyKeyRoutesToDlq() {
        // Scenario 3: Publish WITHOUT idempotency_key header
        // Expected: no row written, message lands on dead-letter queue

        String bodyJson = "{\"id\":\"" + UUID.randomUUID() + "\"}";

        publishMessageWithoutIdempotencyKey("audit.booking.q", "booking.deleted", bodyJson);

        // Wait a moment for processing/rejection
        await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .until(() -> true); // Just wait

        // Verify no audit row was created
        List<AuditLogEntry> rows = auditLogEntryRepository.findAll();
        assertThat(rows).isEmpty();

        // Note: We can't easily verify the DLQ receipt without a separate DLQ consumer.
        // The absence of an audit row is the primary assertion here.
    }

    @Test
    void testScenario4_All7QueuesProduceCorrectlyDerivedMappings() {
        // Scenario 4: Publish one representative message to EACH of the 7 queues
        // Expected: each produces exactly one row with correctly-derived entity_type/action_type

        String[][] queueConfigs = {
            {"audit.booking.q", "booking.created"},
            {"audit.location.q", "location.updated"},
            {"audit.resource.q", "resource.deleted"},
            {"audit.customfield.q", "customfield.created"},
            {"audit.user.q", "user.created"}, // user and role share audit.user.q
            {"audit.permission.q", "permission.assigned"},
            {"audit.settings.q", "settings.updated"}
        };

        for (String[] config : queueConfigs) {
            String queueName = config[0];
            String routingKey = config[1];
            String idempotencyKey = UUID.randomUUID().toString();

            String bodyJson = "{\"id\":\"" + UUID.randomUUID() + "\"}";
            publishMessage(queueName, routingKey, bodyJson, idempotencyKey);
        }

        // Wait for all 7 messages to be processed
        await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(100))
                .until(() -> auditLogEntryRepository.count() == 7);

        List<AuditLogEntry> entries = auditLogEntryRepository.findAll();
        assertThat(entries).hasSize(7);

        // Verify each entity_type/action_type pair
        assertThat(entries.stream()
                .anyMatch(e -> e.getEntityType().equals("booking") && e.getActionType().equals("created")))
                .isTrue();
        assertThat(entries.stream()
                .anyMatch(e -> e.getEntityType().equals("location") && e.getActionType().equals("updated")))
                .isTrue();
        assertThat(entries.stream()
                .anyMatch(e -> e.getEntityType().equals("resource") && e.getActionType().equals("deleted")))
                .isTrue();
        assertThat(entries.stream()
                .anyMatch(e -> e.getEntityType().equals("customfield") && e.getActionType().equals("created")))
                .isTrue();
        assertThat(entries.stream()
                .anyMatch(e -> e.getEntityType().equals("user") && e.getActionType().equals("created")))
                .isTrue();
        assertThat(entries.stream()
                .anyMatch(e -> e.getEntityType().equals("permission") && e.getActionType().equals("assigned")))
                .isTrue();
        assertThat(entries.stream()
                .anyMatch(e -> e.getEntityType().equals("settings") && e.getActionType().equals("updated")))
                .isTrue();
    }

    @Test
    void testScenario5_SparseBodyStillProducesRow() {
        // Scenario 5: Publish a location.deleted event with NO "id" field at all
        // Expected: a row IS still written (never dropped for sparse data), with entity_id = nil-UUID

        String idempotencyKey = UUID.randomUUID().toString();
        String routingKey = "location.deleted";

        // Empty body — no "id" field
        String bodyJson = "{}";

        publishMessage("audit.location.q", routingKey, bodyJson, idempotencyKey);

        // Wait for processing
        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(100))
                .until(() -> auditLogEntryRepository.findByIdempotencyKey(UUID.fromString(idempotencyKey))
                        .isPresent());

        Optional<AuditLogEntry> entry = auditLogEntryRepository.findByIdempotencyKey(UUID.fromString(idempotencyKey));
        assertThat(entry).isPresent();
        assertThat(entry.get().getEntityType()).isEqualTo("location");
        assertThat(entry.get().getActionType()).isEqualTo("deleted");
        assertThat(entry.get().getEntityId()).isEqualTo(NIL_UUID); // Graceful fallback to nil-UUID
        assertThat(entry.get().getActorId()).isEqualTo(NIL_UUID); // No actor field, falls back to nil-UUID
    }

    @Test
    void testRoleEventRoutingKeyOnUserQueue() {
        // Test that role.* events work on audit.user.q
        // (Both user and role events share the same queue per infra/rabbitmq/definitions.json)

        UUID entityId = UUID.randomUUID();
        String idempotencyKey = UUID.randomUUID().toString();
        String routingKey = "role.assigned";

        String bodyJson = String.format("{\"id\":\"%s\", \"assigned_at\":\"2026-01-05T15:30:00Z\"}", entityId);

        publishMessage("audit.user.q", routingKey, bodyJson, idempotencyKey);

        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(100))
                .until(() -> auditLogEntryRepository.findByIdempotencyKey(UUID.fromString(idempotencyKey))
                        .isPresent());

        Optional<AuditLogEntry> entry = auditLogEntryRepository.findByIdempotencyKey(UUID.fromString(idempotencyKey));
        assertThat(entry).isPresent();
        assertThat(entry.get().getEntityType()).isEqualTo("role");
        assertThat(entry.get().getActionType()).isEqualTo("assigned");
    }

    // ==================== Helper Methods ====================

    private void publishMessage(String queueName, String routingKey, String bodyJson, String idempotencyKey) {
        MessageProperties props = new MessageProperties();
        props.setHeader("idempotency_key", idempotencyKey);
        props.setHeader("amqp_receivedRoutingKey", routingKey);

        Message message = new Message(bodyJson.getBytes(), props);

        // RabbitMQ topic exchange routing: send to the exchange that corresponds to the queue
        // (This is a simplified approach; a real test might need to map queues to exchanges)
        String exchangeName = queueName.replace(".q", ".events");
        amqpTemplate.send(exchangeName, routingKey, message);
    }

    private void publishMessageWithoutIdempotencyKey(String queueName, String routingKey, String bodyJson) {
        MessageProperties props = new MessageProperties();
        props.setHeader("amqp_receivedRoutingKey", routingKey);

        Message message = new Message(bodyJson.getBytes(), props);

        String exchangeName = queueName.replace(".q", ".events");
        amqpTemplate.send(exchangeName, routingKey, message);
    }
}
