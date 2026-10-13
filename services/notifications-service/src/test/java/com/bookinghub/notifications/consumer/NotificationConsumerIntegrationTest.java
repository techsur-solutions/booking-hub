package com.bookinghub.notifications.consumer;

import com.bookinghub.notifications.domain.NotificationDelivery;
import com.bookinghub.notifications.repository.NotificationDeliveryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration tests for NotificationConsumerService.
 *
 * Tests the end-to-end flow: message → consumer → template rendering → SMTP send → DB update
 *
 * Scenarios:
 * 1. Happy path: booking.created → email sent → status='sent'
 * 2. Idempotent redelivery: same idempotency_key → no duplicate send
 * 3. Missing idempotency key: → DLQ, no delivery record
 * 4. Missing recipient: → DLQ, delivery record with status='dead_lettered'
 * 5. Transient SMTP failure → retry → success: multiple attempts → status='sent'
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class NotificationConsumerIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private NotificationDeliveryRepository notificationDeliveryRepository;

    @MockBean
    private JavaMailSender mailSender;

    @Autowired
    private ObjectMapper objectMapper;

    private String testIdempotencyKey;
    private UUID testEntityId;

    @BeforeEach
    void setUp() {
        testIdempotencyKey = UUID.randomUUID().toString();
        testEntityId = UUID.randomUUID();
        notificationDeliveryRepository.deleteAll();
        reset(mailSender);
    }

    @Test
    void scenario1_bookingCreatedHappyPath() throws Exception {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        String eventBody = objectMapper.writeValueAsString(
            new BookingEventFixture()
                .id(testEntityId.toString())
                .title("Team Sync")
                .contactEmail("owner@example.com")
                .startTime("2025-01-20T10:00:00Z")
                .endTime("2025-01-20T11:00:00Z")
                .status("pending")
                .build()
        );

        // Act
        publishBookingEvent("booking.created", eventBody);

        // Assert: within timeout, mailSender.send was called once
        await()
            .atMost(5, TimeUnit.SECONDS)
            .pollDelay(100, TimeUnit.MILLISECONDS)
            .until(() -> {
                try {
                    verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
                    return true;
                } catch (AssertionError e) {
                    return false;
                }
            });

        // Assert: delivery record exists with status='sent'
        NotificationDelivery delivery = notificationDeliveryRepository.findByIdempotencyKey(testIdempotencyKey)
            .orElseThrow(() -> new AssertionError("No delivery record found"));
        assertThat(delivery.getStatus()).isEqualTo("sent");
        assertThat(delivery.getAttemptCount()).isGreaterThan(0);
    }

    @Test
    void scenario2_idempotentRedeliveryNoOp() throws Exception {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        String eventBody = objectMapper.writeValueAsString(
            new BookingEventFixture()
                .id(testEntityId.toString())
                .title("Team Sync")
                .contactEmail("owner@example.com")
                .startTime("2025-01-20T10:00:00Z")
                .endTime("2025-01-20T11:00:00Z")
                .status("pending")
                .build()
        );

        // Act: publish the message
        publishBookingEvent("booking.created", eventBody);

        // Assert: first send succeeds
        await()
            .atMost(5, TimeUnit.SECONDS)
            .pollDelay(100, TimeUnit.MILLISECONDS)
            .until(() -> {
                try {
                    verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
                    return true;
                } catch (AssertionError e) {
                    return false;
                }
            });

        reset(mailSender);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act: publish the SAME message again (same idempotency key)
        publishBookingEvent("booking.created", eventBody);

        // Assert: wait briefly to ensure no second send occurs
        Thread.sleep(1000);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));

        // Assert: still exactly one delivery record for this idempotency_key
        var deliveries = notificationDeliveryRepository.findAll();
        var thisKeyDeliveries = deliveries.stream()
            .filter(d -> d.getIdempotencyKey().equals(testIdempotencyKey))
            .toList();
        assertThat(thisKeyDeliveries).hasSize(1);
    }

    @Test
    void scenario3_missingIdempotencyKeySkipsDLQ() throws Exception {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        String eventBody = objectMapper.writeValueAsString(
            new BookingEventFixture()
                .id(testEntityId.toString())
                .title("Team Sync")
                .contactEmail("owner@example.com")
                .build()
        );

        // Act: publish WITHOUT idempotency_key header
        Message message = new Message(eventBody.getBytes(), new MessageProperties());
        message.getMessageProperties().setReceivedRoutingKey("booking.created");
        rabbitTemplate.convertAndSend("booking.events", "booking.created", message);

        // Assert: after a brief wait, mailSender was never called
        Thread.sleep(1000);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));

        // Assert: no delivery record created at all (no idempotency key to track)
        var deliveries = notificationDeliveryRepository.findAll();
        assertThat(deliveries).isEmpty();
    }

    @Test
    void scenario4_missingRecipientMarksDeadLettered() throws Exception {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        String eventBody = objectMapper.writeValueAsString(
            new BookingEventFixture()
                .id(testEntityId.toString())
                .title("Team Sync")
                // NO contact_email
                .build()
        );

        // Act
        publishBookingEvent("booking.approved", eventBody);

        // Assert: after brief wait, mailSender was never called
        Thread.sleep(1000);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));

        // Assert: delivery record exists with status='dead_lettered'
        await()
            .atMost(5, TimeUnit.SECONDS)
            .pollDelay(100, TimeUnit.MILLISECONDS)
            .until(() -> {
                NotificationDelivery delivery = notificationDeliveryRepository
                    .findByIdempotencyKey(testIdempotencyKey)
                    .orElse(null);
                return delivery != null && "dead_lettered".equals(delivery.getStatus());
            });
    }

    @Test
    void scenario5_transientFailureThenRecovery() throws Exception {
        // Arrange: fail 3 times, succeed on 4th
        doThrow(new MailException("Transient SMTP failure") {})
            .doThrow(new MailException("Transient SMTP failure") {})
            .doThrow(new MailException("Transient SMTP failure") {})
            .doNothing()
            .when(mailSender).send(any(SimpleMailMessage.class));

        String eventBody = objectMapper.writeValueAsString(
            new PasswordResetEventFixture()
                .email("user@example.com")
                .resetToken(UUID.randomUUID().toString())
                .build()
        );

        // Act
        publishPasswordResetEvent("password.reset.requested", eventBody);

        // Assert: after retries, eventually succeeds with status='sent'
        await()
            .atMost(15, TimeUnit.SECONDS)
            .pollDelay(200, TimeUnit.MILLISECONDS)
            .until(() -> {
                NotificationDelivery delivery = notificationDeliveryRepository
                    .findByIdempotencyKey(testIdempotencyKey)
                    .orElse(null);
                return delivery != null && "sent".equals(delivery.getStatus());
            });

        // Assert: attempt_count reflects multiple retries
        NotificationDelivery delivery = notificationDeliveryRepository.findByIdempotencyKey(testIdempotencyKey)
            .orElseThrow();
        assertThat(delivery.getAttemptCount()).isGreaterThanOrEqualTo(4);
    }

    private void publishBookingEvent(String routingKey, String eventBody) {
        Message message = new Message(eventBody.getBytes(), new MessageProperties());
        message.getMessageProperties().setHeader("idempotency_key", testIdempotencyKey);
        message.getMessageProperties().setReceivedRoutingKey(routingKey);
        rabbitTemplate.convertAndSend("booking.events", routingKey, message);
    }

    private void publishPasswordResetEvent(String routingKey, String eventBody) {
        Message message = new Message(eventBody.getBytes(), new MessageProperties());
        message.getMessageProperties().setHeader("idempotency_key", testIdempotencyKey);
        message.getMessageProperties().setReceivedRoutingKey(routingKey);
        rabbitTemplate.convertAndSend("password.events", routingKey, message);
    }

    // Test fixtures for event payloads
    static class BookingEventFixture {
        private String id;
        private String title;
        private String contactEmail;
        private String startTime;
        private String endTime;
        private String status;

        BookingEventFixture id(String id) { this.id = id; return this; }
        BookingEventFixture title(String title) { this.title = title; return this; }
        BookingEventFixture contactEmail(String contactEmail) { this.contactEmail = contactEmail; return this; }
        BookingEventFixture startTime(String startTime) { this.startTime = startTime; return this; }
        BookingEventFixture endTime(String endTime) { this.endTime = endTime; return this; }
        BookingEventFixture status(String status) { this.status = status; return this; }

        String build() throws Exception {
            return new ObjectMapper().writeValueAsString(new Object() {
                public String id = BookingEventFixture.this.id;
                public String title = BookingEventFixture.this.title;
                public String contact_email = BookingEventFixture.this.contactEmail;
                public String start_time = BookingEventFixture.this.startTime;
                public String end_time = BookingEventFixture.this.endTime;
                public String status = BookingEventFixture.this.status;
            });
        }
    }

    static class PasswordResetEventFixture {
        private String email;
        private String resetToken;

        PasswordResetEventFixture email(String email) { this.email = email; return this; }
        PasswordResetEventFixture resetToken(String resetToken) { this.resetToken = resetToken; return this; }

        String build() throws Exception {
            return new ObjectMapper().writeValueAsString(new Object() {
                public String email = PasswordResetEventFixture.this.email;
                public String reset_token = PasswordResetEventFixture.this.resetToken;
            });
        }
    }
}
