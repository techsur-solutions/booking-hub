package com.bookinghub.feeds.rabbit;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;

/**
 * Thrown when an inbound RabbitMQ event is missing its idempotency_key header or
 * payload field.
 *
 * Extends AmqpRejectAndDontRequeueException so Spring AMQP routes the message
 * straight to the broker's existing dead-letter exchange without any retry
 * attempts — a message structurally missing its idempotency key can NEVER
 * succeed no matter how many times it is retried.
 *
 * It is ALSO registered explicitly as non-retryable in RabbitConfig's
 * SimpleRetryPolicy map (belt-and-suspenders, same approach as Phase 6's
 * audit-log-service / notifications-service).
 *
 * Plan 07-02's projection consumer throws this when an event has no
 * idempotency_key.
 */
public class MissingIdempotencyKeyException extends AmqpRejectAndDontRequeueException {

    public MissingIdempotencyKeyException(String message) {
        super(message);
    }
}
