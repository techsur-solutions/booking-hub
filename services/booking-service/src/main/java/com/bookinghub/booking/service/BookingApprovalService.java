package com.bookinghub.booking.service;

import com.bookinghub.booking.domain.Booking;
import com.bookinghub.booking.domain.OutboxEvent;
import com.bookinghub.booking.dto.BookingReadDtos.BookingDetailResponse;
import com.bookinghub.booking.error.ApprovalBookingNotFoundException;
import com.bookinghub.booking.error.ApprovalInvalidStateException;
import com.bookinghub.booking.repository.BookingRepository;
import com.bookinghub.booking.repository.OutboxEventRepository;
import com.bookinghub.booking.security.CurrentUserProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Business logic for one-way approve/deny booking state transitions (plan 05-04).
 *
 * Central design principle (one-way transitions, never re-toggleable):
 * Both approve() and deny() enforce that the booking MUST be in "pending" status before
 * any mutation is applied. Attempting to approve an already-approved or already-denied
 * booking is rejected 409 APPROVAL_INVALID_STATE — identically for both same-direction
 * re-approval (approved→approved) and cross-direction re-toggling (approved→denied).
 * This is the FRD's intended constraint: approve and deny are one-way state transitions.
 *
 * Named decision (distinct from bypass-approve at creation time):
 * Plan 05-03's auto-approve-at-creation (bypass) produces booking.created outbox events
 * for bookings that were NEVER pending. A booking that was bypass-approved at creation
 * time already has status="approved" — it will NEVER reach this service's pending-check,
 * since it bypassed the pending state entirely. These two code paths are mutually exclusive
 * on the SAME booking row: a bypass-approved booking skips pending, so approve() and deny()
 * can never be called on it with a pending status. No risk of duplicate events.
 *
 * Publish both booking.approved and booking.denied as DISTINCT routing keys — never the
 * same as booking.created. The outbox pattern (transactional, at-least-once delivery) is
 * the same shape used by BookingWriteService (plan 05-03) and every other service in the
 * project (Phase 3/4).
 *
 * Named decision (response via BookingReadService.getById):
 * After mutating the booking, this service delegates to BookingReadService.getById() for
 * the response — ensuring the full BookingDetailResponse shape (with enriched custom field
 * values and conflict flags) is returned from the approval/denial endpoint, matching the
 * AJAX-style detail endpoint's shape exactly.
 */
@Service
public class BookingApprovalService {

    private static final Logger log = LoggerFactory.getLogger(BookingApprovalService.class);

    private final BookingRepository bookingRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final CurrentUserProvider currentUserProvider;
    private final BookingReadService bookingReadService;
    private final ObjectMapper objectMapper;

    public BookingApprovalService(
            BookingRepository bookingRepository,
            OutboxEventRepository outboxEventRepository,
            CurrentUserProvider currentUserProvider,
            BookingReadService bookingReadService,
            ObjectMapper objectMapper) {
        this.bookingRepository = bookingRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.currentUserProvider = currentUserProvider;
        this.bookingReadService = bookingReadService;
        this.objectMapper = objectMapper;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Approves a pending booking.
     *
     * One-way transition: only PENDING bookings may be approved. Approving an
     * already-approved or already-denied booking is rejected 409 APPROVAL_INVALID_STATE.
     *
     * @param id Booking UUID
     * @return Full BookingDetailResponse with updated status/approvedBy/approvedAt
     * @throws ApprovalBookingNotFoundException (404) if id does not exist or is soft-deleted
     * @throws ApprovalInvalidStateException    (409) if booking is not in pending status
     */
    @Transactional
    public BookingDetailResponse approve(UUID id) {
        // Step 1: Look up booking (soft-delete aware — soft-deleted = not found for approval)
        Booking booking = bookingRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApprovalBookingNotFoundException(id.toString()));

        // Step 2: One-way-transition enforcement — MUST be pending
        if (!"pending".equals(booking.getStatus())) {
            throw new ApprovalInvalidStateException(booking.getStatus());
        }

        // Step 3: Apply approval mutation
        booking.setStatus("approved");
        booking.setApprovedBy(currentUserProvider.getCurrentUserId());
        booking.setApprovedAt(Instant.now());
        bookingRepository.save(booking);

        // Step 4: Publish booking.approved event (distinct from booking.created)
        publishOutboxEvent(booking, "booking.approved", buildApprovalPayload(booking, null));

        // Step 5: Return full detail response via BookingReadService
        return bookingReadService.getById(id);
    }

    /**
     * Denies a pending booking.
     *
     * One-way transition: only PENDING bookings may be denied. Denying an already-approved
     * or already-denied booking is rejected 409 APPROVAL_INVALID_STATE.
     *
     * @param id           Booking UUID
     * @param denialReason Optional free-text reason (nullable — F0 confirmed no format requirement)
     * @return Full BookingDetailResponse with updated status/deniedBy/deniedAt/denialReason
     * @throws ApprovalBookingNotFoundException (404) if id does not exist or is soft-deleted
     * @throws ApprovalInvalidStateException    (409) if booking is not in pending status
     */
    @Transactional
    public BookingDetailResponse deny(UUID id, String denialReason) {
        // Step 1: Look up booking (soft-delete aware)
        Booking booking = bookingRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApprovalBookingNotFoundException(id.toString()));

        // Step 2: One-way-transition enforcement — MUST be pending (same check as approve)
        if (!"pending".equals(booking.getStatus())) {
            throw new ApprovalInvalidStateException(booking.getStatus());
        }

        // Step 3: Apply denial mutation
        booking.setStatus("denied");
        booking.setDeniedBy(currentUserProvider.getCurrentUserId());
        booking.setDeniedAt(Instant.now());
        booking.setDenialReason(denialReason); // nullable — F0: no format requirement
        bookingRepository.save(booking);

        // Step 4: Publish booking.denied event (distinct from booking.created)
        publishOutboxEvent(booking, "booking.denied", buildApprovalPayload(booking, denialReason));

        // Step 5: Return full detail response via BookingReadService
        return bookingReadService.getById(id);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private void publishOutboxEvent(Booking booking, String routingKey, String payload) {
        OutboxEvent event = new OutboxEvent(
                "booking",
                booking.getId(),
                "booking.events",
                routingKey,
                payload);
        outboxEventRepository.save(event);
    }

    private String buildApprovalPayload(Booking booking, String denialReason) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", booking.getId());
            payload.put("status", booking.getStatus());
            payload.put("approved_by", booking.getApprovedBy());
            payload.put("approved_at", booking.getApprovedAt());
            payload.put("denied_by", booking.getDeniedBy());
            payload.put("denied_at", booking.getDeniedAt());
            payload.put("denial_reason", denialReason);
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize approval payload for booking {}", booking.getId(), e);
            return "{}";
        }
    }
}
