package com.bookinghub.notifications.dto;

import com.bookinghub.notifications.domain.NotificationDelivery;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/**
 * DTOs for notification status queries.
 */
public class NotificationDtos {

    /**
     * API response for a delivery status record.
     * Mapped from NotificationDelivery entity for the two admin-only endpoints:
     * - GET /notifications/delivery-status (with optional event_type/status filters)
     * - GET /notifications/dead-letter (implicit status='dead_lettered' filter)
     */
    public record DeliveryStatusResponse(
            UUID id,
            @JsonProperty("event_type")
            String eventType,
            @JsonProperty("entity_id")
            UUID entityId,
            String status,
            @JsonProperty("attempt_count")
            int attemptCount,
            @JsonProperty("last_attempted_at")
            Instant lastAttemptedAt,
            @JsonProperty("created_at")
            Instant createdAt
    ) {

        /**
         * Map from NotificationDelivery entity to response DTO.
         */
        public static DeliveryStatusResponse fromEntity(NotificationDelivery entity) {
            return new DeliveryStatusResponse(
                entity.getId(),
                entity.getEventType(),
                entity.getEntityId(),
                entity.getStatus(),
                entity.getAttemptCount(),
                entity.getLastAttemptedAt(),
                entity.getCreatedAt()
            );
        }
    }
}
