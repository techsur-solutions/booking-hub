package com.bookinghub.auditlog.consumer;

import com.bookinghub.auditlog.domain.AuditLogEntry;
import com.bookinghub.auditlog.event.DomainEventParser;
import com.bookinghub.auditlog.repository.AuditLogEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end proof of the 7-queue consumer against a REAL broker and a REAL database.
 *
 * <h2>Why the running docker-compose stack rather than Testcontainers</h2>
 * Same workaround this project adopted in plan 06-03 (AuditLogEntrySchemaTest), Phase 4 plan
 * 04-01 (SchemaCompletionTest) and Phase 3 plan 03-01. Here it is additionally the STRONGER
 * choice: the compose broker boots with {@code infra/rabbitmq/definitions.json} loaded, so this
 * test exercises the project's actual production topology — all 7 {@code audit.*.q} queues, their
 * real exchange bindings, and their real {@code x-dead-letter-exchange} wiring — instead of a
 * hand-declared approximation of it. A binding typo in {@code definitions.json} would fail this
 * test; against a hand-rolled test topology it would not.
 *
 * <h2>What is proven</h2>
 * <ol>
 *   <li>A well-formed event produces exactly one correctly-mapped row</li>
 *   <li>A redelivery of the same idempotency_key never produces a second row</li>
 *   <li>An event with no idempotency_key writes NO row and lands on the DLQ</li>
 *   <li>All 7 queues are genuinely wired, each deriving its own entity_type/action_type</li>
 *   <li>A sparse body still produces a row (never silently dropped)</li>
 * </ol>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        // The shared test profile disables listener startup (plan 06-03 had no listeners).
        // This test is specifically ABOUT the listeners, so it turns them back on.
        "spring.rabbitmq.listener.simple.auto-startup=true"
})
class AuditEventConsumerIntegrationTest {

    /** Every queue this service consumes, with the exchange + routing key that reaches it. */
    private static final List<QueueBinding> ALL_BINDINGS = List.of(
            new QueueBinding("audit.booking.q", "booking.events", "booking.approved", "booking", "approved"),
            new QueueBinding("audit.location.q", "location.events", "location.created", "location", "created"),
            new QueueBinding("audit.resource.q", "resource.events", "resource.updated", "resource", "updated"),
            new QueueBinding("audit.customfield.q", "customfield.events", "customfield.deleted", "customfield", "deleted"),
            new QueueBinding("audit.user.q", "user.events", "role.assigned", "role", "assigned"),
            new QueueBinding("audit.permission.q", "permission.events", "permission.updated", "permission", "updated"),
            new QueueBinding("audit.settings.q", "settings.events", "settings.updated", "settings", "updated")
    );

    private static final Duration POLL_TIMEOUT = Duration.ofSeconds(20);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private AuditLogEntryRepository auditLogEntryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void purgeStateBetweenTests() {
        // The audit table is append-only for the audit_svc runtime role, but the test
        // datasource connects as the superuser, so DELETE is permitted here for isolation.
        jdbcTemplate.update("DELETE FROM audit_log_entries");

        for (QueueBinding binding : ALL_BINDINGS) {
            rabbitAdmin.purgeQueue(binding.queue(), false);
            rabbitAdmin.purgeQueue(binding.queue() + ".dlq", false);
        }
    }

    @Test
    @DisplayName("1. A booking.approved event produces exactly one correctly-mapped audit row")
    void wellFormedEvent_producesOneCorrectlyMappedRow() {
        UUID idempotencyKey = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        UUID approverId = UUID.randomUUID();
        Instant approvedAt = Instant.parse("2026-02-02T10:15:00Z");

        publish("booking.events", "booking.approved", idempotencyKey, """
                {
                  "id": "%s",
                  "approved_by": "%s",
                  "approved_at": "2026-02-02T10:15:00Z",
                  "title": "Board meeting"
                }
                """.formatted(bookingId, approverId));

        AuditLogEntry entry = awaitEntry(idempotencyKey);

        assertThat(entry.getEntityType()).isEqualTo("booking");
        assertThat(entry.getActionType()).isEqualTo("approved");
        assertThat(entry.getEntityId()).isEqualTo(bookingId);
        assertThat(entry.getActorId()).isEqualTo(approverId);
        assertThat(entry.getOccurredAt()).isEqualTo(approvedAt);
        // after_values stores the whole body verbatim; before_values stays null this phase
        assertThat(entry.getAfterValues()).contains("Board meeting");
        assertThat(entry.getBeforeValues()).isNull();

        assertThat(countRows()).isEqualTo(1);
    }

    @Test
    @DisplayName("2. Redelivering the identical message never produces a second row")
    void redeliveredEvent_neverDuplicates() {
        UUID idempotencyKey = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        String payload = """
                { "id": "%s", "actor_id": "%s", "occurred_at": "2026-02-02T10:15:00Z" }
                """.formatted(bookingId, UUID.randomUUID());

        publish("booking.events", "booking.approved", idempotencyKey, payload);
        awaitEntry(idempotencyKey);

        // Exact same idempotency_key — the broker redelivery case
        publish("booking.events", "booking.approved", idempotencyKey, payload);

        // Give the consumer time to process (and suppress) the duplicate before asserting
        sleep(Duration.ofSeconds(3));

        assertThat(countRowsFor(idempotencyKey)).isEqualTo(1);
        assertThat(countRows()).isEqualTo(1);
    }

    @Test
    @DisplayName("3. An event with no idempotency_key writes no row and lands on the DLQ")
    void missingIdempotencyKey_deadLettersWithoutWriting() {
        MessageProperties props = MessagePropertiesBuilder.newInstance()
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build(); // deliberately NO idempotency_key header

        Message message = new Message(
                "{\"id\":\"%s\"}".formatted(UUID.randomUUID()).getBytes(StandardCharsets.UTF_8),
                props);

        Instant publishedAt = Instant.now();
        rabbitTemplate.send("booking.events", "booking.approved", message);

        // The message must reach the DLQ — MissingIdempotencyKeyException is classified
        // non-retryable, so it bypasses the retry budget and rejects immediately.
        Message deadLettered = await(
                () -> Optional.ofNullable(rabbitTemplate.receive("audit.booking.q.dlq")),
                "message on audit.booking.q.dlq");

        assertThat(deadLettered).isNotNull();

        // IMMEDIATELY, not eventually. The retry backoff is 5s + 30s + 2min = 155s, so anything
        // under a few seconds proves the non-retryable classification actually fired rather than
        // the message merely surviving the full retry budget. This bound is the regression guard
        // for exactly that bug: the classifier used to match the ListenerExecutionFailedException
        // wrapper against an Exception -> true entry and never reach the real cause.
        Duration timeToDeadLetter = Duration.between(publishedAt, Instant.now());
        assertThat(timeToDeadLetter)
                .as("time to dead-letter (must bypass the 155s retry budget entirely)")
                .isLessThan(Duration.ofSeconds(10));
        // Never silently dropped AND never written as an incomplete row
        assertThat(countRows()).isZero();
    }

    @Test
    @DisplayName("4. All 7 queues are wired, each deriving its own entity_type/action_type")
    void allSevenQueuesProduceCorrectlyDerivedRows() {
        record Published(QueueBinding binding, UUID idempotencyKey, UUID entityId) {}

        List<Published> published = ALL_BINDINGS.stream()
                .map(binding -> {
                    UUID idempotencyKey = UUID.randomUUID();
                    UUID entityId = UUID.randomUUID();
                    publish(binding.exchange(), binding.routingKey(), idempotencyKey, """
                            { "id": "%s", "actor_id": "%s", "occurred_at": "2026-04-04T08:00:00Z" }
                            """.formatted(entityId, UUID.randomUUID()));
                    return new Published(binding, idempotencyKey, entityId);
                })
                .toList();

        for (Published p : published) {
            AuditLogEntry entry = awaitEntry(p.idempotencyKey());

            assertThat(entry.getEntityType())
                    .as("entity_type for queue %s (routing key %s)",
                            p.binding().queue(), p.binding().routingKey())
                    .isEqualTo(p.binding().expectedEntityType());
            assertThat(entry.getActionType())
                    .as("action_type for queue %s (routing key %s)",
                            p.binding().queue(), p.binding().routingKey())
                    .isEqualTo(p.binding().expectedActionType());
            assertThat(entry.getEntityId()).isEqualTo(p.entityId());
        }

        // Exactly one row per queue — no queue double-consumed, none missed
        assertThat(countRows()).isEqualTo(ALL_BINDINGS.size());
    }

    @Test
    @DisplayName("5. A body with no 'id' field still produces a row with the nil-UUID placeholder")
    void sparseBody_stillProducesRow() {
        UUID idempotencyKey = UUID.randomUUID();

        // location.deleted carrying NO id at all — the graceful-degradation contract,
        // proven end-to-end through the real consumer rather than only in the parser.
        publish("location.events", "location.deleted", idempotencyKey,
                "{\"reason\":\"decommissioned\"}");

        AuditLogEntry entry = awaitEntry(idempotencyKey);

        assertThat(entry.getEntityType()).isEqualTo("location");
        assertThat(entry.getActionType()).isEqualTo("deleted");
        assertThat(entry.getEntityId()).isEqualTo(DomainEventParser.NIL_UUID);
        assertThat(entry.getActorId()).isEqualTo(DomainEventParser.NIL_UUID);
        assertThat(entry.getOccurredAt()).isNotNull();
        assertThat(entry.getAfterValues()).contains("decommissioned");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    private void publish(String exchange, String routingKey, UUID idempotencyKey, String jsonBody) {
        MessageProperties props = MessagePropertiesBuilder.newInstance()
                .setHeader("idempotency_key", idempotencyKey.toString())
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build();

        rabbitTemplate.send(exchange, routingKey,
                new Message(jsonBody.getBytes(StandardCharsets.UTF_8), props));
    }

    private AuditLogEntry awaitEntry(UUID idempotencyKey) {
        return await(() -> auditLogEntryRepository.findByIdempotencyKey(idempotencyKey),
                "audit row for idempotency_key=" + idempotencyKey);
    }

    /** Polls until the supplier yields a value or {@link #POLL_TIMEOUT} elapses. */
    private <T> T await(Supplier<Optional<T>> supplier, String what) {
        Instant deadline = Instant.now().plus(POLL_TIMEOUT);
        while (Instant.now().isBefore(deadline)) {
            Optional<T> result = supplier.get();
            if (result.isPresent()) {
                return result.get();
            }
            sleep(Duration.ofMillis(250));
        }
        throw new AssertionError("Timed out after " + POLL_TIMEOUT + " waiting for " + what);
    }

    private int countRows() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM audit_log_entries", Integer.class);
        return count == null ? 0 : count;
    }

    private int countRowsFor(UUID idempotencyKey) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM audit_log_entries WHERE idempotency_key = ?",
                Integer.class, idempotencyKey);
        return count == null ? 0 : count;
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }

    private record QueueBinding(
            String queue,
            String exchange,
            String routingKey,
            String expectedEntityType,
            String expectedActionType) {
    }
}
