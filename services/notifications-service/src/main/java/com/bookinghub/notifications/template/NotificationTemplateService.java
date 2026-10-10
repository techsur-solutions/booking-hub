package com.bookinghub.notifications.template;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Renders email subject and body for each of the 4 supported notification event types.
 *
 * Named decision (template content, 06-02 objective): Templates are minimal and
 * functionally-equivalent (F8.4's "content/intent" bar, not byte-identical legacy
 * replication). They render the booking title/time/status or the reset link, and
 * render location_id by raw ID (no cross-service name lookup — same no-new-sync-dependency
 * reasoning as recipient resolution).
 *
 * Throws TemplateRenderingException (retryable RuntimeException) when a REQUIRED field
 * for the event type is missing from eventData — this is a processing failure, not a
 * structural non-retryable error (unlike MissingIdempotencyKeyException), so it goes
 * through the full retry/DLQ path.
 */
@Service
public class NotificationTemplateService {

    private final String passwordResetLinkBaseUrl;

    public NotificationTemplateService(
            @Value("${notifications.password-reset-link-base-url:http://localhost:3000/reset-password}")
            String passwordResetLinkBaseUrl) {
        this.passwordResetLinkBaseUrl = passwordResetLinkBaseUrl;
    }

    /**
     * Renders a subject and body for the given event type and payload.
     *
     * @param eventType   routing key / event type (e.g. "booking.created")
     * @param eventData   parsed JSON payload from the RabbitMQ message body
     * @return            a RenderedEmail record with subject and body
     * @throws TemplateRenderingException if a required field is absent from eventData
     */
    public RenderedEmail render(String eventType, JsonNode eventData) {
        return switch (eventType) {
            case "booking.created"  -> renderBookingCreated(eventData);
            case "booking.approved" -> renderBookingApproved(eventData);
            case "booking.denied"   -> renderBookingDenied(eventData);
            case "password.reset.requested" -> renderPasswordReset(eventData);
            default -> throw new TemplateRenderingException(
                    "Unknown event type: " + eventType);
        };
    }

    // -----------------------------------------------------------------------
    // Per-event-type renderers
    // -----------------------------------------------------------------------

    private RenderedEmail renderBookingCreated(JsonNode data) {
        String title = requireText(data, "title", "booking.created");
        String status = textOrDefault(data, "status", "pending");
        String startTime  = textOrDefault(data, "start_time", "N/A");
        String endTime    = textOrDefault(data, "end_time",   "N/A");
        String locationId = textOrDefault(data, "location_id", "N/A");

        String subject = "Booking submitted: " + title;
        String body = String.format("""
                Your booking has been submitted and is currently %s.

                Title:       %s
                Start time:  %s
                End time:    %s
                Location ID: %s
                """,
                status, title, startTime, endTime, locationId);

        return new RenderedEmail(subject, body);
    }

    private RenderedEmail renderBookingApproved(JsonNode data) {
        String title = requireText(data, "title", "booking.approved");
        String startTime  = textOrDefault(data, "start_time", "N/A");
        String endTime    = textOrDefault(data, "end_time",   "N/A");
        String locationId = textOrDefault(data, "location_id", "N/A");

        String subject = "Booking approved: " + title;
        String body = String.format("""
                Your booking has been approved.

                Title:       %s
                Start time:  %s
                End time:    %s
                Location ID: %s
                """,
                title, startTime, endTime, locationId);

        return new RenderedEmail(subject, body);
    }

    private RenderedEmail renderBookingDenied(JsonNode data) {
        String title = requireText(data, "title", "booking.denied");
        String startTime  = textOrDefault(data, "start_time", "N/A");
        String endTime    = textOrDefault(data, "end_time",   "N/A");
        String locationId = textOrDefault(data, "location_id", "N/A");

        // denial_reason is optional (nullable)
        String denialReason = textOrDefault(data, "denial_reason", null);
        String reasonLine = (denialReason != null && !denialReason.isBlank())
                ? "Reason:      " + denialReason + "\n"
                : "";

        String subject = "Booking denied: " + title;
        String body = String.format("""
                Your booking has been denied.

                Title:       %s
                Start time:  %s
                End time:    %s
                Location ID: %s
                %s""",
                title, startTime, endTime, locationId, reasonLine);

        return new RenderedEmail(subject, body);
    }

    private RenderedEmail renderPasswordReset(JsonNode data) {
        String resetToken = requireText(data, "reset_token", "password.reset.requested");

        String resetLink = passwordResetLinkBaseUrl + "?token=" + resetToken;
        String subject = "Password reset requested";
        String body = String.format("""
                A password reset was requested for your account.

                Click the link below to reset your password:
                %s

                If you did not request a password reset, you can safely ignore this email.
                """,
                resetLink);

        return new RenderedEmail(subject, body);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Requires a non-null, non-blank text field from the event payload.
     * Throws TemplateRenderingException if absent (retryable processing failure).
     */
    private String requireText(JsonNode data, String field, String eventType) {
        JsonNode node = data.get(field);
        if (node == null || node.isNull() || node.asText("").isBlank()) {
            throw new TemplateRenderingException(
                    "Required field '" + field + "' missing or blank in " + eventType + " event");
        }
        return node.asText();
    }

    /**
     * Returns the text value of a field, or the given default if absent/null.
     */
    private String textOrDefault(JsonNode data, String field, String defaultValue) {
        JsonNode node = data.get(field);
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        String text = node.asText("");
        return text.isBlank() ? defaultValue : text;
    }

    // -----------------------------------------------------------------------
    // Output record
    // -----------------------------------------------------------------------

    /**
     * Rendered email content — subject and plain-text body.
     */
    public record RenderedEmail(String subject, String body) {}
}
