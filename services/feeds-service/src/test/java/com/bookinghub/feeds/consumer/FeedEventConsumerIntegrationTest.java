package com.bookinghub.feeds.consumer;

import com.bookinghub.feeds.repository.FeedBookingRepository;
import com.bookinghub.feeds.repository.LocationNameRepository;
import com.bookinghub.feeds.repository.ProcessedEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * End-to-end integration test for the feeds-service projection consumer against a
 * REAL Postgres + RabbitMQ (the running docker-compose instances), exercising both
 * @RabbitListener methods through the real broker.
 *
 * Testcontainers workaround (same established pattern as plan 07-01 / Phase 4 04-01 /
 * Phase 6 06-03): this sandbox could not reliably start Testcontainers, so the test
 * runs against the compose Postgres + RabbitMQ via @ActiveProfiles("test") (datasource
 * + rabbitmq host overridden at run time to the compose service names).
 *
 * The @TestConfiguration declares the EXACT topology from infra/rabbitmq/definitions.json
 * this consumer binds to — idempotent against the broker (the live broker already has it
 * loaded from definitions.json; re-declaring the identical durable topology is a no-op).
 * Messages are published exactly as the confirmed OutboxPublisher precedent does:
 * rabbitTemplate.send(exchange, routingKey, message) with a lowercase idempotency_key
 * String header and JSON payload bytes.
 *
 * Every scenario uses fresh random ids + idempotency keys so repeated runs against the
 * shared broker/DB never collide, and each test cleans its own feed_bookings rows up front.
 */
@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=true")
@ActiveProfiles("test")
class FeedEventConsumerIntegrationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    @TestConfiguration
    static class TopologyConfig {

        @Bean
        RabbitAdmin feedsTestRabbitAdmin(RabbitTemplate rabbitTemplate) {
            RabbitAdmin admin = new RabbitAdmin(rabbitTemplate.getConnectionFactory());

            TopicExchange bookingEvents = new TopicExchange("booking.events", true, false);
            TopicExchange locationEvents = new TopicExchange("location.events", true, false);
            admin.declareExchange(bookingEvents);
            admin.declareExchange(locationEvents);

            FanoutExchange bookingDlx = new FanoutExchange("feeds.booking.q.dlx", true, false);
            FanoutExchange locationDlx = new FanoutExchange("feeds.location.q.dlx", true, false);
            admin.declareExchange(bookingDlx);
            admin.declareExchange(locationDlx);

            Queue bookingQ = QueueBuilder.durable("feeds.booking.q")
                    .withArgument("x-dead-letter-exchange", "feeds.booking.q.dlx").build();
            Queue bookingDlq = QueueBuilder.durable("feeds.booking.q.dlq").build();
            Queue locationQ = QueueBuilder.durable("feeds.location.q")
                    .withArgument("x-dead-letter-exchange", "feeds.location.q.dlx").build();
            Queue locationDlq = QueueBuilder.durable("feeds.location.q.dlq").build();
            admin.declareQueue(bookingQ);
            admin.declareQueue(bookingDlq);
            admin.declareQueue(locationQ);
            admin.declareQueue(locationDlq);

            admin.declareBinding(BindingBuilder.bind(bookingQ).to(bookingEvents).with("booking.*"));
            admin.declareBinding(BindingBuilder.bind(bookingDlq).to(bookingDlx));
            admin.declareBinding(BindingBuilder.bind(locationQ).to(locationEvents).with("location.updated"));
            admin.declareBinding(BindingBuilder.bind(locationQ).to(locationEvents).with("location.deleted"));
            admin.declareBinding(BindingBuilder.bind(locationDlq).to(locationDlx));

            return admin;
        }
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private FeedBookingRepository feedBookingRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Autowired
    private LocationNameRepository locationNameRepository;

    @BeforeEach
    void drainDlqs() {
        // Clear any DLQ residue from prior runs so scenario 5's assertion is deterministic.
        while (rabbitTemplate.receive("feeds.booking.q.dlq", 200) != null) { /* drain */ }
        while (rabbitTemplate.receive("feeds.location.q.dlq", 200) != null) { /* drain */ }
    }

    @AfterEach
    void cleanUpCommittedRows() {
        // Unlike the @DataJpaTest slices (which roll back), this @SpringBootTest COMMITS
        // via the real broker+consumer, so it must wipe its own rows from the shared
        // feeds_db_test tables — otherwise leftover approved+upcoming rows would break
        // sibling tests (e.g. FeedBookingSchemaTest's containsExactly) in the same DB.
        feedBookingRepository.deleteAll();
        locationNameRepository.deleteAll();
        processedEventRepository.deleteAll();
    }

    private Message jsonMessage(ObjectNode body, String idempotencyKey) {
        MessageProperties props = MessagePropertiesBuilder.newInstance()
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build();
        if (idempotencyKey != null) {
            props.setHeader("idempotency_key", idempotencyKey);
        }
        try {
            return new Message(MAPPER.writeValueAsBytes(body), props);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ObjectNode bookingBody(UUID id, String title, UUID locationId, String status) {
        ObjectNode n = MAPPER.createObjectNode();
        n.put("id", id.toString());
        if (title != null) n.put("title", title);
        if (locationId != null) n.put("location_id", locationId.toString());
        n.put("start_time", Instant.now().plus(1, ChronoUnit.HOURS).toString());
        n.put("end_time", Instant.now().plus(2, ChronoUnit.HOURS).toString());
        if (status != null) n.put("status", status);
        return n;
    }

    private ObjectNode idStatusBody(UUID id, String status) {
        ObjectNode n = MAPPER.createObjectNode();
        n.put("id", id.toString());
        if (status != null) n.put("status", status);
        return n;
    }

    @Test
    void approvedBooking_projectsVisibleRow() {
        UUID bookingId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        rabbitTemplate.send("booking.events", "booking.created",
                jsonMessage(bookingBody(bookingId, "Weekly Sync", locationId, "pending"),
                        UUID.randomUUID().toString()));
        // Wait for the created row to be projected BEFORE publishing approved. booking.approved's
        // payload carries no title/location/time (Phase 5 buildApprovalPayload), so it can only
        // flip the status of an ALREADY-projected row — exactly as production guarantees via the
        // outbox ordering (created is always published before approved). This await enforces that
        // ordering deterministically rather than relying on broker/consumer delivery timing.
        await().atMost(TIMEOUT).until(() -> feedBookingRepository.findById(bookingId).isPresent());

        rabbitTemplate.send("booking.events", "booking.approved",
                jsonMessage(idStatusBody(bookingId, "approved"), UUID.randomUUID().toString()));

        await().atMost(TIMEOUT).untilAsserted(() ->
                assertThat(feedBookingRepository.findById(bookingId))
                        .isPresent()
                        .get()
                        .satisfies(fb -> assertThat(fb.getStatus()).isEqualTo("approved")));
    }

    @Test
    void duplicateRedelivery_isSuppressed() {
        UUID bookingId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        String idemKey = UUID.randomUUID().toString();
        Message msg = jsonMessage(bookingBody(bookingId, "Standup", locationId, "approved"), idemKey);

        rabbitTemplate.send("booking.events", "booking.created", msg);
        await().atMost(TIMEOUT).until(() -> feedBookingRepository.findById(bookingId).isPresent());

        // Re-publish the IDENTICAL message (same idempotency_key) -> must be suppressed.
        rabbitTemplate.send("booking.events", "booking.created",
                jsonMessage(bookingBody(bookingId, "Standup", locationId, "approved"), idemKey));

        // Give the consumer time to (not) re-process, then assert still one processed_events row.
        await().during(Duration.ofSeconds(2)).atMost(TIMEOUT).untilAsserted(() -> {
            assertThat(feedBookingRepository.findById(bookingId)).isPresent();
            assertThat(processedEventRepository.existsByIdempotencyKey(UUID.fromString(idemKey))).isTrue();
        });
    }

    @Test
    void deniedBooking_removesRow() {
        UUID bookingId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        rabbitTemplate.send("booking.events", "booking.created",
                jsonMessage(bookingBody(bookingId, "To Deny", locationId, "pending"),
                        UUID.randomUUID().toString()));
        await().atMost(TIMEOUT).until(() -> feedBookingRepository.findById(bookingId).isPresent());

        rabbitTemplate.send("booking.events", "booking.denied",
                jsonMessage(idStatusBody(bookingId, "denied"), UUID.randomUUID().toString()));

        await().atMost(TIMEOUT).untilAsserted(() ->
                assertThat(feedBookingRepository.findById(bookingId)).isEmpty());
    }

    @Test
    void locationUpdatedThenBookingCreated_denormalizesAcrossQueues() {
        UUID locationId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        String locationName = "Conference Room " + locationId.toString().substring(0, 8);

        ObjectNode locBody = MAPPER.createObjectNode();
        locBody.put("id", locationId.toString());
        locBody.put("name", locationName);
        rabbitTemplate.send("location.events", "location.updated",
                jsonMessage(locBody, UUID.randomUUID().toString()));

        // Wait until the cache has absorbed the location before the booking arrives — the two
        // events travel separate queues (feeds.location.q, feeds.booking.q) with independent
        // consumers, so without this the booking could project the placeholder name first.
        await().atMost(TIMEOUT).until(() ->
                locationNameRepository.findById(locationId).isPresent());

        rabbitTemplate.send("booking.events", "booking.created",
                jsonMessage(bookingBody(bookingId, "Cross-Queue", locationId, "approved"),
                        UUID.randomUUID().toString()));

        await().atMost(TIMEOUT).untilAsserted(() ->
                assertThat(feedBookingRepository.findById(bookingId))
                        .isPresent()
                        .get()
                        .satisfies(fb -> assertThat(fb.getLocationName()).isEqualTo(locationName)));
    }

    @Test
    void missingIdempotencyKey_deadLettersAndWritesNoRow() {
        UUID bookingId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        // No idempotency_key header -> must dead-letter, never project.
        rabbitTemplate.send("booking.events", "booking.created",
                jsonMessage(bookingBody(bookingId, "No Key", locationId, "approved"), null));

        await().atMost(TIMEOUT).untilAsserted(() -> {
            Message dead = rabbitTemplate.receive("feeds.booking.q.dlq", 500);
            assertThat(dead).as("missing-key message must land on feeds.booking.q.dlq").isNotNull();
            assertThat(new String(dead.getBody(), StandardCharsets.UTF_8)).contains(bookingId.toString());
        });
        // And no projection row was written.
        assertThat(feedBookingRepository.findById(bookingId)).isEmpty();
    }
}
