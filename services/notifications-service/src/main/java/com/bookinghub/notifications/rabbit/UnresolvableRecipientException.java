package com.bookinghub.notifications.rabbit;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;

/**
 * Thrown by plan 06-02's consumer when the recipient of a notification
 * cannot be resolved (e.g., user not found, no email address).
 *
 * Extends AmqpRejectAndDontRequeueException so Spring AMQP skips all retry attempts
 * and routes straight to the broker's dead-letter exchange — a message whose
 * recipient cannot be resolved is structurally unsatisfiable and retrying it
 * would waste all retry cycles before reaching the DLQ.
 *
 * Registered as non-retryable in RabbitConfig's SimpleRetryPolicy so the
 * RejectAndDontRequeueRecoverer fires immediately on the first attempt.
 */
public class UnresolvableRecipientException extends AmqpRejectAndDontRequeueException {

    public UnresolvableRecipientException(String message) {
        super(message);
    }
}
