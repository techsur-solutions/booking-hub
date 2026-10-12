package com.bookinghub.notifications.template;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Template rendering service for all four notification event types.
 *
 * Renders subject + body from event data, throwing TemplateRenderingException
 * on missing required fields (treated as retryable processing failure).
 *
 * Event type dispatch:
 * - booking.created, booking.approved, booking.denied: booking lifecycle events with contact_email
 * - password.reset.requested: password reset request with email + reset_token
 */
@Slf4j
@Service
public class NotificationTemplateService {

    @Value("${notifications.password-reset-link-base-url:http://localhost:3000/reset-password}")
    private String passwordResetLinkBaseUrl;

    public record RenderedEmail(String subject, String body) {}

    public RenderedEmail render(String eventType, JsonNode eventData) {
        return switch (eventType) {
            case "booking.created" -> renderBookingCreated(eventData);
            case "booking.approved" -> renderBookingApproved(eventData);
            case "booking.denied" -> renderBookingDenied(eventData);
            case "password.reset.requested" -> renderPasswordResetRequested(eventData);
            default -> throw new TemplateRenderingException("Unknown event type: " + eventType);
        };
    }

    private RenderedEmail renderBookingCreated(JsonNode eventData) {
        String title = requireText(eventData, "title", "booking.created");
        String status = textOrDefault(eventData, "status", "pending");
        String startTime = textOrDefault(eventData, "start_time", "");
        String endTime = textOrDefault(eventData, "end_time", "");
        String locationId = textOrDefault(eventData, "location_id", "");

        String subject = "Booking submitted: " + title;
        String body = String.format(
            "Your booking has been submitted.\n\n" +
            "Title: %s\n" +
            "Status: %s\n" +
            "Start: %s\n" +
            "End: %s\n" +
            "Location: %s\n\n" +
            "Thank you for booking with us.",
            title, status, startTime, endTime, locationId
        );

        return new RenderedEmail(subject, body);
    }

    private RenderedEmail renderBookingApproved(JsonNode eventData) {
        String title = requireText(eventData, "title", "booking.approved");
        String startTime = textOrDefault(eventData, "start_time", "");
        String endTime = textOrDefault(eventData, "end_time", "");
        String locationId = textOrDefault(eventData, "location_id", "");

        String subject = "Booking approved: " + title;
        String body = String.format(
            "Your booking has been approved!\n\n" +
            "Title: %s\n" +
            "Start: %s\n" +
            "End: %s\n" +
            "Location: %s\n\n" +
            "Your booking is confirmed.",
            title, startTime, endTime, locationId
        );

        return new RenderedEmail(subject, body);
    }

    private RenderedEmail renderBookingDenied(JsonNode eventData) {
        String title = requireText(eventData, "title", "booking.denied");
        String denialReason = textOrDefault(eventData, "denial_reason", "");

        String subject = "Booking denied: " + title;
        String body = String.format(
            "Your booking has been denied.\n\n" +
            "Title: %s\n" +
            "%s" +
            "\nWe regret that we cannot accommodate this booking at this time.",
            title,
            denialReason.isEmpty() ? "" : ("Reason: " + denialReason + "\n")
        );

        return new RenderedEmail(subject, body);
    }

    private RenderedEmail renderPasswordResetRequested(JsonNode eventData) {
        String resetToken = requireText(eventData, "reset_token", "password.reset.requested");
        String resetLink = passwordResetLinkBaseUrl + "?token=" + resetToken;

        String subject = "Password reset requested";
        String body = String.format(
            "A password reset has been requested for your account.\n\n" +
            "If you did not request this, you can ignore this email.\n\n" +
            "To reset your password, click the link below:\n" +
            "%s\n\n" +
            "This link will expire in 24 hours.",
            resetLink
        );

        return new RenderedEmail(subject, body);
    }

    private String requireText(JsonNode eventData, String fieldName, String eventType) {
        JsonNode node = eventData.get(fieldName);
        if (node == null || node.isNull() || node.asText().isBlank()) {
            throw new TemplateRenderingException(
                "Missing required field '" + fieldName + "' for event type '" + eventType + "'"
            );
        }
        return node.asText();
    }

    private String textOrDefault(JsonNode eventData, String fieldName, String defaultValue) {
        JsonNode node = eventData.get(fieldName);
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        String text = node.asText();
        return text.isBlank() ? defaultValue : text;
    }

    public static class TemplateRenderingException extends RuntimeException {
        public TemplateRenderingException(String message) {
            super(message);
        }
    }
}
