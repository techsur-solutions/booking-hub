package com.bookinghub.notifications.template;

/**
 * Thrown by NotificationTemplateService when a required field for an event type
 * is missing from the event payload.
 *
 * This is a plain RuntimeException (retryable) — template rendering failures
 * are treated as processing failures per FRD F8 §Validation, not swallowed.
 * They propagate through the consumer's retry/DLQ path exactly like SMTP failures.
 */
public class TemplateRenderingException extends RuntimeException {

    public TemplateRenderingException(String message) {
        super(message);
    }
}
