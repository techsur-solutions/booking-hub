package com.bookinghub.notifications.rabbit;

import com.bookinghub.notifications.domain.NotificationDelivery;
import com.bookinghub.notifications.repository.NotificationDeliveryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Custom message recoverer for RabbitMQ listener exhaustion.
 *
 * On final retry exhaustion or immediate non-retryable exception:
 * 1. Looks up the notification_deliveries row by idempotency_key (from message headers)
 * 2. If found, marks its status as 'dead_lettered' with current timestamp
 * 3. Delegates to RejectAndDontRequeueRecoverer for broker-level DLQ routing
 *
 * This gives the admin observability endpoints (GET /notifications/dead-letter)
 * a durable record of every message that failed to send.
 */
@Slf4j
@Component
public class DatabaseTrackingMessageRecoverer extends RejectAndDontRequeueRecoverer {

    private final NotificationDeliveryRepository notificationDeliveryRepository;

    public DatabaseTrackingMessageRecoverer(NotificationDeliveryRepository notificationDeliveryRepository) {
        this.notificationDeliveryRepository = notificationDeliveryRepository;
    }

    @Override
    public void recover(Message message, Throwable cause) {
        String idempotencyKey = (String) message.getMessageProperties().getHeader("idempotency_key");
        
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            try {
                notificationDeliveryRepository.findByIdempotencyKey(idempotencyKey)
                    .ifPresent(row -> {
                        row.setStatus("dead_lettered");
                        row.setLastAttemptedAt(Instant.now());
                        notificationDeliveryRepository.save(row);
                        log.info("Marked delivery dead_lettered for idempotency_key={}", idempotencyKey);
                    });
            } catch (Exception e) {
                log.error("Error marking delivery dead_lettered for idempotency_key={}", idempotencyKey, e);
            }
        } else {
            log.warn("No idempotency_key in message headers during recovery");
        }

        // Delegate to the actual broker-level DLQ rejection
        super.recover(message, cause);
    }
}
