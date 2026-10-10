package com.bookinghub.notifications.rabbit;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;

/**
 * Thrown by plan 06-02's consumer when a message is missing its idempotency_key header.
 *
 * Extends AmqpRejectAndDontRequeueException so Spring AMQP skips all retry attempts
 * and routes straight to the broker's dead-letter exchange — a message that has
 * NO idempotency key can never succeed (structural missing data), so consuming all
 * retry cycles before DLQ routing would be wasteful.
 *
 * Registered as non-retryable in RabbitConfig's SimpleRetryPolicy so the
 * RejectAndDontRequeueRecoverer fires immediately on the first attempt.
 */
public class MissingIdempotencyKeyException extends AmqpRejectAndDontRequeueException {

    public MissingIdempotencyKeyException(String message) {
        super(message);
    }
}
