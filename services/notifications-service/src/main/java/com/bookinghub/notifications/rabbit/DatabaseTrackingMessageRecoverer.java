package com.bookinghub.notifications.rabbit;

import com.bookinghub.notifications.repository.NotificationDeliveryRepository;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Custom MessageRecoverer that:
 * 1. Looks up the notification_deliveries row by idempotency_key from the message header.
 * 2. Sets its status to 'dead_lettered' with the current timestamp.
 * 3. Delegates to RejectAndDontRequeueRecoverer for the actual broker-level DLQ routing.
 *
 * This fulfills FRD F8 Error States: "Route to DLQ and record as dead_lettered" for
 * any message that has exhausted retries or triggered a non-retryable exception.
 *
 * If the idempotency_key header is absent (e.g. MissingIdempotencyKeyException was
 * the cause — the message had NO key), there is no row to update — we simply skip
 * the DB update and still delegate the reject-to-DLQ, so the message is still
 * routed correctly at the broker level.
 *
 * Wire: This bean is used as the recoverer in RabbitConfig.rabbitListenerContainerFactory,
 * replacing plan 06-01's placeholder new RejectAndDontRequeueRecoverer().
 */
@Component
public class DatabaseTrackingMessageRecoverer implements MessageRecoverer {

    private final NotificationDeliveryRepository notificationDeliveryRepository;

    public DatabaseTrackingMessageRecoverer(NotificationDeliveryRepository notificationDeliveryRepository) {
        this.notificationDeliveryRepository = notificationDeliveryRepository;
    }

    @Override
    public void recover(Message message, Throwable cause) {
        String idempotencyKey = message.getMessageProperties().getHeader("idempotency_key");
        if (idempotencyKey != null) {
            notificationDeliveryRepository.findByIdempotencyKey(idempotencyKey)
                .ifPresent(row -> {
                    row.setStatus("dead_lettered");
                    row.setLastAttemptedAt(Instant.now());
                    notificationDeliveryRepository.save(row);
                });
        }
        // Delegate the actual broker-level reject-without-requeue to DLQ
        new RejectAndDontRequeueRecoverer().recover(message, cause);
    }
}
