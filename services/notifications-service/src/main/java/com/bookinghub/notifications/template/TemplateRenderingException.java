package com.bookinghub.notifications.template;

/**
 * Thrown by {@link NotificationTemplateService} when an event payload is missing a
 * field that is REQUIRED to render that event type's template (e.g. {@code title}
 * for booking events, {@code reset_token} for password resets).
 *
 * <p>Deliberately a plain {@link RuntimeException} — NOT an
 * {@code AmqpRejectAndDontRequeueException} subclass like
 * {@code MissingIdempotencyKeyException}/{@code UnresolvableRecipientException}.
 * That distinction is the whole point:
 *
 * <ul>
 *   <li><b>Non-retryable</b> (missing idempotency key, unresolvable recipient):
 *       structurally impossible to ever succeed → straight to DLQ, no wasted retries.</li>
 *   <li><b>Retryable</b> (this exception): FRD F8 §Validation treats template rendering
 *       failures as ordinary processing failures. A malformed payload usually will not
 *       fix itself, but it is cheap to let the standard 4-attempt policy run and land in
 *       the DLQ via the normal exhaustion path, where
 *       {@code DatabaseTrackingMessageRecoverer} records it as {@code dead_lettered}.
 *       Treating it as retryable keeps this failure mode on the SAME observable path as
 *       an SMTP outage rather than inventing a third category.</li>
 * </ul>
 *
 * <p>Because {@code RabbitConfig}'s retry policy maps {@code Exception.class → true},
 * this type is retryable by default without needing explicit registration.
 */
public class TemplateRenderingException extends RuntimeException {

    public TemplateRenderingException(String message) {
        super(message);
    }

    public TemplateRenderingException(String message, Throwable cause) {
        super(message, cause);
    }
}
