package com.bookinghub.notifications.rabbit;

import com.bookinghub.notifications.domain.NotificationDelivery;
import com.bookinghub.notifications.repository.NotificationDeliveryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Marks a delivery row {@code dead_lettered} before letting the message be rejected to
 * the broker's DLQ.
 *
 * <p>This is what turns a dead-letter from an invisible event into an auditable one.
 * Plan 06-01 wired a bare {@link RejectAndDontRequeueRecoverer}, which correctly routes
 * the message to {@code <queue>.dlx → <queue>.dlq} but leaves
 * {@code notification_deliveries} frozen at whatever status the last attempt set —
 * typically {@code retrying}. An operator reading
 * {@code GET /notifications/delivery-status} would then see a row stuck mid-flight with
 * no indication that the system had given up on it, which is exactly the
 * "permanently-stuck row with no further action" state F8 forbids.
 *
 * <p><b>Ordering matters.</b> The row is updated FIRST, then the delegate rejects the
 * message. Rejecting first would open a window where the message is already in the DLQ
 * while the row still claims {@code retrying}; if the process died in that window the
 * database would permanently disagree with the broker. Doing the DB write first means the
 * worst case is a row marked {@code dead_lettered} for a message that was not yet
 * rejected — self-correcting, because the message is redelivered and reprocessed.
 *
 * <p><b>Never throws.</b> A failure to record the status must not prevent the rejection
 * itself; the DLQ routing is the stronger guarantee of the two and is preserved
 * unconditionally in a {@code finally}-equivalent path. A tracking failure is logged at
 * ERROR for the operator rather than escalated into a stuck consumer.
 *
 * <p>Messages with no {@code idempotency_key} header are rejected without any row being
 * written. That is correct, not a gap: the key is the only handle the table is keyed by,
 * so there is nothing to track against. The DLQ message itself is the record in that case
 * — matching the plan's "no notification_deliveries row is written at all" expectation.
 */
@Component
public class DatabaseTrackingMessageRecoverer implements MessageRecoverer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseTrackingMessageRecoverer.class);

    private final NotificationDeliveryRepository notificationDeliveryRepository;
    private final RejectAndDontRequeueRecoverer delegate = new RejectAndDontRequeueRecoverer();

    public DatabaseTrackingMessageRecoverer(NotificationDeliveryRepository notificationDeliveryRepository) {
        this.notificationDeliveryRepository = notificationDeliveryRepository;
    }

    @Override
    public void recover(Message message, Throwable cause) {
        try {
            markDeadLettered(message, cause);
        } catch (Exception trackingFailure) {
            // Deliberately swallowed: the broker-level rejection below is the guarantee
            // that actually matters, and it must happen regardless.
            log.error("failed to record dead_lettered status; proceeding with DLQ rejection anyway",
                    trackingFailure);
        }

        // Always delegate — this performs the basic.reject(requeue=false) that the queue's
        // x-dead-letter-exchange argument (infra/rabbitmq/definitions.json) turns into
        // DLQ routing. No new topology is declared here.
        delegate.recover(message, cause);
    }

    private void markDeadLettered(Message message, Throwable cause) {
        Object header = message.getMessageProperties().getHeader("idempotency_key");
        if (header == null) {
            log.warn("message dead-lettered with no idempotency_key header; "
                    + "no delivery row to update (the DLQ message is the record). reason={}",
                    cause == null ? "unknown" : cause.toString());
            return;
        }

        // The AMQP client decodes table header values as LongString, not java.lang.String —
        // toString() yields the text content for both, so normalise through it.
        String idempotencyKey = header.toString();

        notificationDeliveryRepository.findByIdempotencyKey(idempotencyKey).ifPresentOrElse(
                row -> {
                    row.setStatus("dead_lettered");
                    row.setLastAttemptedAt(Instant.now());
                    notificationDeliveryRepository.save(row);
                    log.warn("delivery dead-lettered after {} attempt(s), idempotency_key={}, reason={}",
                            row.getAttemptCount(), idempotencyKey,
                            cause == null ? "unknown" : cause.toString());
                },
                () -> log.warn("message dead-lettered but no delivery row exists for idempotency_key={}; "
                        + "the failure occurred before the row was inserted. reason={}",
                        idempotencyKey, cause == null ? "unknown" : cause.toString()));
    }

    /**
     * Records a dead-letter for a message whose failure is non-retryable and therefore
     * never reaches the retry interceptor's recoverer.
     *
     * <p>{@code MissingIdempotencyKeyException} and {@code UnresolvableRecipientException}
     * extend {@code AmqpRejectAndDontRequeueException}, which Spring AMQP's container
     * intercepts <i>before</i> the retry advice chain — so {@link #recover} is never
     * invoked for them and, without this hook, an unresolvable-recipient message would
     * reach the DLQ while its row sat at {@code pending} forever. The consumer calls this
     * directly on that path so both routes converge on the same observable outcome.
     */
    public void markDeadLetteredByKey(String idempotencyKey, String reason) {
        try {
            notificationDeliveryRepository.findByIdempotencyKey(idempotencyKey).ifPresent(row -> {
                row.setStatus("dead_lettered");
                row.setAttemptCount(row.getAttemptCount() == null ? 1 : row.getAttemptCount() + 1);
                row.setLastAttemptedAt(Instant.now());
                notificationDeliveryRepository.save(row);
                log.warn("delivery dead-lettered (non-retryable), idempotency_key={}, reason={}",
                        idempotencyKey, reason);
            });
        } catch (Exception trackingFailure) {
            log.error("failed to record dead_lettered status for idempotency_key={}",
                    idempotencyKey, trackingFailure);
        }
    }
}
