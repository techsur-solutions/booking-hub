package com.bookinghub.notifications.template;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Renders the plain-text email subject/body for each of F8's four event types.
 *
 * <p><b>Named decision (template content, from plan 06-02's objective):</b>
 * Y3-integrations.md states "exact legacy template content is an F0 audit input, not
 * invented here", and no such content audit exists among this project's F0 deliverables.
 * These templates are therefore minimal and <i>functionally equivalent</i> — they meet
 * F8.4's "content/intent" bar rather than attempting byte-identical legacy replication.
 *
 * <p><b>No cross-service lookups.</b> {@code location_id} renders as a raw UUID, never a
 * resolved location name. TechArch §2.6 architects this service as a pure RabbitMQ
 * consumer with no synchronous client-facing API; this plan extends that to mean no NEW
 * synchronous outbound dependency either. A notification must never fail because
 * locations-resources-service is down — that would convert a notification problem into a
 * new runtime coupling, contradicting the event-driven-decoupling NFR.
 *
 * <p><b>Required vs. optional fields.</b> A field whose absence would make the email
 * meaningless (a booking email with no booking title, a reset email with no token) raises
 * {@link TemplateRenderingException}. Everything else degrades gracefully to a
 * "(not specified)" placeholder, because a partially-detailed email that arrives beats a
 * dead-lettered one that does not.
 */
@Service
public class NotificationTemplateService {

    private static final String NOT_SPECIFIED = "(not specified)";

    private final String passwordResetLinkBaseUrl;

    public NotificationTemplateService(
            @Value("${notifications.password-reset-link-base-url}") String passwordResetLinkBaseUrl) {
        this.passwordResetLinkBaseUrl = passwordResetLinkBaseUrl;
    }

    /**
     * A fully-rendered plain-text email, ready to hand to
     * {@code EmailSenderService}. Recipient resolution is the consumer's job, not the
     * template's — this record carries only what the template layer decides.
     */
    public record RenderedEmail(String subject, String body) {
    }

    /**
     * Renders the email for the given event type.
     *
     * @param eventType the AMQP routing key: {@code booking.created},
     *                  {@code booking.approved}, {@code booking.denied}, or
     *                  {@code password.reset.requested}
     * @param eventData the event's JSON payload
     * @throws TemplateRenderingException if a required field is missing, or if the event
     *                                    type has no template (retryable — see that class)
     */
    public RenderedEmail render(String eventType, JsonNode eventData) {
        if (eventData == null) {
            throw new TemplateRenderingException(
                    "event payload was null or unparseable for event_type=" + eventType);
        }

        return switch (eventType) {
            case "booking.created" -> renderBookingCreated(eventData);
            case "booking.approved" -> renderBookingApproved(eventData);
            case "booking.denied" -> renderBookingDenied(eventData);
            case "password.reset.requested" -> renderPasswordReset(eventData);
            default -> throw new TemplateRenderingException(
                    "no template registered for event_type=" + eventType);
        };
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BOOKING TEMPLATES
    // ═══════════════════════════════════════════════════════════════════════════

    private RenderedEmail renderBookingCreated(JsonNode data) {
        String title = required(data, "title", "booking.created");
        // The payload carries the real status, which differs by path: a booking created
        // under auto-approve (Phase 5's bypass-approve) arrives already 'approved', while
        // a normal one arrives 'pending'. Render what actually happened rather than
        // assuming "pending" and telling the recipient something untrue.
        String status = optional(data, "status");

        String subject = "Booking submitted: " + title;
        String body = """
                Your booking has been submitted.

                Title:    %s
                Status:   %s
                Starts:   %s
                Ends:     %s
                Location: %s

                %s

                --
                This is an automated message from Booking Hub. Please do not reply.
                """.formatted(
                title,
                status,
                optional(data, "start_time"),
                optional(data, "end_time"),
                optional(data, "location_id"),
                "approved".equalsIgnoreCase(status)
                        ? "No further action is needed - this booking is already confirmed."
                        : "You will receive another email once it has been reviewed.");

        return new RenderedEmail(subject, body);
    }

    private RenderedEmail renderBookingApproved(JsonNode data) {
        String title = required(data, "title", "booking.approved");

        String subject = "Booking approved: " + title;
        String body = """
                Your booking has been approved.

                Title:    %s
                Starts:   %s
                Ends:     %s
                Location: %s

                No further action is needed - this booking is confirmed.

                --
                This is an automated message from Booking Hub. Please do not reply.
                """.formatted(
                title,
                optional(data, "start_time"),
                optional(data, "end_time"),
                optional(data, "location_id"));

        return new RenderedEmail(subject, body);
    }

    private RenderedEmail renderBookingDenied(JsonNode data) {
        String title = required(data, "title", "booking.denied");

        // denial_reason is explicitly nullable — F0 confirmed the legacy system imposes no
        // format requirement and never obliged an approver to give one. Omit the whole
        // line when absent rather than printing an empty "Reason:" label.
        String denialReason = text(data, "denial_reason");
        String reasonBlock = (denialReason == null || denialReason.isBlank())
                ? ""
                : "Reason:   " + denialReason + "\n";

        String subject = "Booking denied: " + title;
        String body = """
                Your booking has been denied.

                Title:    %s
                Starts:   %s
                Ends:     %s
                Location: %s
                %s
                Please contact an administrator if you believe this was in error.

                --
                This is an automated message from Booking Hub. Please do not reply.
                """.formatted(
                title,
                optional(data, "start_time"),
                optional(data, "end_time"),
                optional(data, "location_id"),
                reasonBlock);

        return new RenderedEmail(subject, body);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PASSWORD RESET TEMPLATE
    // ═══════════════════════════════════════════════════════════════════════════

    private RenderedEmail renderPasswordReset(JsonNode data) {
        // The raw token exists in exactly one place outside its SHA-256 hash: this event
        // payload (see users-permissions-service AuthService.requestPasswordReset). An
        // email without it is useless, so it is strictly required.
        String resetToken = required(data, "reset_token", "password.reset.requested");

        String resetLink = passwordResetLinkBaseUrl + "?token=" + resetToken;

        String subject = "Password reset requested";
        String body = """
                A password reset was requested for your Booking Hub account.

                To choose a new password, open this link:

                %s

                This link expires in 2 hours and can only be used once.

                If you did not request this, you can ignore this email - your current
                password remains unchanged.

                --
                This is an automated message from Booking Hub. Please do not reply.
                """.formatted(resetLink);

        return new RenderedEmail(subject, body);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FIELD ACCESS HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Reads a field that the template cannot do without. Missing, null, or blank all
     * raise {@link TemplateRenderingException} — a JSON {@code null} is as useless to a
     * subject line as an absent key, so they are treated identically.
     */
    private String required(JsonNode data, String field, String eventType) {
        String value = text(data, field);
        if (value == null || value.isBlank()) {
            throw new TemplateRenderingException(
                    "required field '" + field + "' missing or blank for event_type=" + eventType);
        }
        return value;
    }

    /** Reads an optional field, degrading to a placeholder rather than failing. */
    private String optional(JsonNode data, String field) {
        String value = text(data, field);
        return (value == null || value.isBlank()) ? NOT_SPECIFIED : value;
    }

    /**
     * Null-safe text extraction. Returns {@code null} for an absent key, an explicit JSON
     * {@code null}, or a node that carries no text — never throws, never returns the
     * literal string {@code "null"} (which {@code JsonNode.asText()} would).
     */
    private String text(JsonNode data, String field) {
        JsonNode node = data.get(field);
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        return node.asText();
    }
}
