package com.bookinghub.notifications.controller;

import com.bookinghub.notifications.domain.NotificationDelivery;
import com.bookinghub.notifications.dto.NotificationDtos;
import com.bookinghub.notifications.repository.NotificationDeliveryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin-only observability endpoints for notification delivery status.
 *
 * Both endpoints require role_audit_viewer (enforced by SecurityConfig's blanket
 * filter-chain requirement on all non-actuator routes). No per-method @PreAuthorize needed.
 *
 * Endpoints:
 * - GET /notifications/delivery-status — all deliveries (optionally filtered by event_type/status)
 * - GET /notifications/dead-letter — dead-lettered deliveries only
 */
@Slf4j
@RestController
@RequestMapping("/notifications")
public class NotificationStatusController {

    private final NotificationDeliveryRepository notificationDeliveryRepository;

    public NotificationStatusController(NotificationDeliveryRepository notificationDeliveryRepository) {
        this.notificationDeliveryRepository = notificationDeliveryRepository;
    }

    /**
     * Get notification delivery status records with optional filtering.
     *
     * Query parameters:
     * - event_type: filter by event type (booking.created, etc.) — optional
     * - status: filter by status (pending, sent, retrying, dead_lettered) — optional
     *
     * @param eventType optional event type filter
     * @param status optional status filter
     * @return list of delivery status records
     */
    @GetMapping("/delivery-status")
    public ResponseEntity<List<NotificationDtos.DeliveryStatusResponse>> getDeliveryStatus(
            @RequestParam(name = "event_type", required = false) String eventType,
            @RequestParam(name = "status", required = false) String status) {

        // Build dynamic Specification based on provided filters
        Specification<NotificationDelivery> spec = Specification.where(null);

        if (eventType != null && !eventType.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("eventType"), eventType));
        }

        if (status != null && !status.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        List<NotificationDelivery> deliveries = notificationDeliveryRepository.findAll(spec);
        List<NotificationDtos.DeliveryStatusResponse> responses = deliveries.stream()
            .map(NotificationDtos.DeliveryStatusResponse::fromEntity)
            .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * Get all dead-lettered notification records (status = 'dead_lettered').
     *
     * @return list of dead-lettered delivery records
     */
    @GetMapping("/dead-letter")
    public ResponseEntity<List<NotificationDtos.DeliveryStatusResponse>> getDeadLettered() {
        // Fixed filter: only dead_lettered status
        Specification<NotificationDelivery> spec =
            (root, query, cb) -> cb.equal(root.get("status"), "dead_lettered");

        List<NotificationDelivery> deliveries = notificationDeliveryRepository.findAll(spec);
        List<NotificationDtos.DeliveryStatusResponse> responses = deliveries.stream()
            .map(NotificationDtos.DeliveryStatusResponse::fromEntity)
            .toList();

        return ResponseEntity.ok(responses);
    }
}
