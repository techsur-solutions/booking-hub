package com.bookinghub.auditlog.rabbit;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;

/**
 * Thrown when a RabbitMQ message is missing its idempotency_key header or payload field.
 *
 * Extends AmqpRejectAndDontRequeueException so Spring AMQP's error handler automatically
 * routes the message to the broker's existing dead-letter exchange without any retry
 * attempts — a message structurally missing its idempotency key can NEVER succeed
 * regardless of how many times it's retried.
 *
 * Named decision (non-retryable): this exception is also registered explicitly as
 * non-retryable in RabbitConfig.java's SimpleRetryPolicy map to ensure the retry
 * interceptor skips it even if the AmqpRejectAndDontRequeueException mechanism is
 * somehow bypassed. Belt-and-suspenders safety — same approach as notifications-service
 * plan 06-01 Task 3's MissingIdempotencyKeyException.
 *
 * Plan 06-04's 7-queue consumer throws this for the "missing idempotency key" FRD F11
 * Error States row.
 */
public class MissingIdempotencyKeyException extends AmqpRejectAndDontRequeueException {

    public MissingIdempotencyKeyException(String message) {
        super(message);
    }
}
