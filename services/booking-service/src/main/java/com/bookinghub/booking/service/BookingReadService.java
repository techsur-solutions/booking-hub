package com.bookinghub.booking.service;

import com.bookinghub.booking.client.CustomFieldClient;
import com.bookinghub.booking.client.LocationsResourcesClient;
import com.bookinghub.booking.client.dto.ClientDtos.CustomFieldDefinition;
import com.bookinghub.booking.client.dto.ClientDtos.ResourceResponse;
import com.bookinghub.booking.conflict.ConflictDetectionService;
import com.bookinghub.booking.conflict.dto.ConflictDtos.ConflictCheckResult;
import com.bookinghub.booking.conflict.dto.ConflictDtos.ConflictFlag;
import com.bookinghub.booking.domain.Booking;
import com.bookinghub.booking.domain.BookingCustomFieldValue;
import com.bookinghub.booking.domain.BookingResource;
import com.bookinghub.booking.dto.BookingReadDtos;
import com.bookinghub.booking.dto.BookingReadDtos.BookingDetailResponse;
import com.bookinghub.booking.dto.BookingReadDtos.CustomFieldValueOutput;
import com.bookinghub.booking.error.BookingNotFoundException;
import com.bookinghub.booking.repository.BookingCustomFieldValueRepository;
import com.bookinghub.booking.repository.BookingRepository;
import com.bookinghub.booking.repository.BookingResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Business logic for booking read operations — calendar/day/list views and
 * the AJAX-style detail endpoint (plan 05-04).
 *
 * Central design principle: conflict detection is called in PURE READ-ONLY mode
 * via the SAME ConflictDetectionService create/edit use (plan 05-02/05-03). The
 * result is ONLY ever attached as conflictFlags on the response, NEVER used to
 * block or reject anything. This surfaces already-saved soft-warning conflicts
 * (an approver who chose to save under a flagged conflict) and conflicts that
 * emerged later from an independent edit, in calendar/list views.
 *
 * Named decision (is_unique filtering): For each booking in the list, this service
 * resolves each resource's is_unique flag via LocationsResourcesClient before calling
 * ConflictDetectionService — exactly as BookingWriteService does on create/edit.
 * This is an accepted N+1-style read, explicitly named as a performance tradeoff for
 * this phase's scope (THREAT T-05-04-04).
 *
 * Named decision (getById soft-delete policy): Uses plain findById (NOT
 * findByIdAndDeletedAtIsNull) — mirrors Phase 4's exact precedent for
 * Locations/Resources (04-02-PLAN.md's getById). A soft-deleted booking returns
 * 200 with last-known values (deletedAt populated); 404 only for a genuinely
 * nonexistent id. conflictFlags are [] for soft-deleted bookings.
 */
@Service
public class BookingReadService {

    private static final Logger log = LoggerFactory.getLogger(BookingReadService.class);

    private final BookingRepository bookingRepository;
    private final BookingResourceRepository bookingResourceRepository;
    private final BookingCustomFieldValueRepository bookingCustomFieldValueRepository;
    private final ConflictDetectionService conflictDetectionService;
    private final LocationsResourcesClient locationsResourcesClient;
    private final CustomFieldClient customFieldClient;

    public BookingReadService(
            BookingRepository bookingRepository,
            BookingResourceRepository bookingResourceRepository,
            BookingCustomFieldValueRepository bookingCustomFieldValueRepository,
            ConflictDetectionService conflictDetectionService,
            LocationsResourcesClient locationsResourcesClient,
            CustomFieldClient customFieldClient) {
        this.bookingRepository = bookingRepository;
        this.bookingResourceRepository = bookingResourceRepository;
        this.bookingCustomFieldValueRepository = bookingCustomFieldValueRepository;
        this.conflictDetectionService = conflictDetectionService;
        this.locationsResourcesClient = locationsResourcesClient;
        this.customFieldClient = customFieldClient;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Lists bookings within a date range with optional filters.
     *
     * Uses the SAME ConflictDetectionService as create/edit (plan 05-02/05-03),
     * invoked here in pure read-only mode. Conflict flags are attached to every
     * returned booking — NEVER used to block or reject anything.
     *
     * Named decision (calendar/day/list as query-parameter variants of one endpoint):
     * Legacy's own index/day/list/building/location actions all queried the same Event
     * model with different where-clause compositions. TechArch's API table lists exactly
     * one GET /bookings entry. The frontend composes calendar/day/list UX entirely from
     * how it calls this one endpoint.
     *
     * @param from       Required. Bookings overlapping this window are returned.
     * @param to         Required. Half-open interval: booking.startTime < to AND from < booking.endTime
     * @param locationId Optional. Filter to bookings at this location.
     * @param resourceId Optional. Further filter to bookings whose resource set contains this id.
     * @param status     Optional. Filter by booking status (e.g. "pending", "approved", "denied").
     * @param q          Optional. Case-insensitive keyword search on title OR description.
     * @return List of BookingDetailResponse ordered by startTime ASC, with conflictFlags attached.
     */
    @Transactional(readOnly = true)
    public List<BookingDetailResponse> listBookings(
            Instant from, Instant to, UUID locationId, UUID resourceId, String status, String q) {

        // Step 1: Bulk date-range query with optional location/status/keyword filters
        List<Booking> bookings = bookingRepository.findInRange(from, to, locationId, status, q);

        // Step 2: If resourceId filter is present, further filter by resource membership
        if (resourceId != null) {
            UUID filterResourceId = resourceId;
            bookings = bookings.stream()
                    .filter(b -> bookingResourceRepository.findByBookingId(b.getId())
                            .stream()
                            .anyMatch(br -> filterResourceId.equals(br.getResourceId())))
                    .collect(Collectors.toList());
        }

        // Step 3: Map each to response with conflict flags (read-only, never blocking)
        return bookings.stream()
                .map(this::toDetailResponseWithConflicts)
                .collect(Collectors.toList());
    }

    /**
     * Returns full booking details by id, using the last-known-values policy.
     *
     * Named decision: uses plain findById (NOT findByIdAndDeletedAtIsNull).
     * A soft-deleted booking returns 200 with deletedAt populated; 404 only for a
     * genuinely nonexistent id. Mirrors Phase 4's precedent for Locations/Resources.
     *
     * conflictFlags are [] for soft-deleted bookings — a deleted booking cannot
     * meaningfully conflict with anything live.
     *
     * @param id Booking UUID
     * @return BookingDetailResponse with last-known values and conflictFlags
     * @throws BookingNotFoundException (404) if id never existed (findById returns empty)
     */
    @Transactional(readOnly = true)
    public BookingDetailResponse getById(UUID id) {
        // Named decision: plain findById, NOT findByIdAndDeletedAtIsNull
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id.toString()));

        // Conflict flags: skip for soft-deleted bookings (they don't occupy the slot)
        List<BookingReadDtos.ConflictFlag> conflictFlags;
        if (booking.getDeletedAt() != null) {
            conflictFlags = List.of(); // deleted booking cannot conflict with anything live
        } else {
            conflictFlags = computeConflictFlags(booking);
        }

        return toDetailResponse(booking, conflictFlags);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Maps a booking to its detail response with conflict flags computed in read-only mode.
     */
    private BookingDetailResponse toDetailResponseWithConflicts(Booking booking) {
        List<BookingReadDtos.ConflictFlag> conflictFlags = computeConflictFlags(booking);
        return toDetailResponse(booking, conflictFlags);
    }

    /**
     * Computes conflict flags for a booking by calling ConflictDetectionService
     * in pure read-only mode — the SAME engine create/edit uses (plan 05-02/05-03),
     * invoked here without any enforcement logic. The result is only for display.
     *
     * Named decision (N+1 tradeoff): each booking requires per-resource lookups to
     * resolve is_unique flags. Explicitly accepted as a performance tradeoff for this
     * phase's scope (THREAT T-05-04-04, not optimized here).
     */
    private List<BookingReadDtos.ConflictFlag> computeConflictFlags(Booking booking) {
        // Resolve attached resources and filter to unique ones
        List<BookingResource> resources = bookingResourceRepository.findByBookingId(booking.getId());
        List<UUID> uniqueResourceIds = new ArrayList<>();
        for (BookingResource br : resources) {
            try {
                Optional<ResourceResponse> resourceOpt = locationsResourcesClient.getResource(br.getResourceId());
                if (resourceOpt.isPresent() && resourceOpt.get().isUnique()) {
                    uniqueResourceIds.add(br.getResourceId());
                }
            } catch (Exception e) {
                // Graceful degradation: if resource lookup fails, skip this resource
                // (cannot determine is_unique; err on side of not flagging a resource conflict)
                log.warn("Failed to resolve resource {} for conflict detection on booking {}: {}",
                         br.getResourceId(), booking.getId(), e.getMessage());
            }
        }

        // Call the SAME ConflictDetectionService in read-only mode (never enforces)
        ConflictCheckResult result = conflictDetectionService.checkConflicts(
                booking.getLocationId(),
                uniqueResourceIds,
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getId() // exclude this booking's own slot
        );

        // Map ConflictDtos.ConflictFlag to BookingReadDtos.ConflictFlag
        return result.conflicts().stream()
                .map(cf -> new BookingReadDtos.ConflictFlag(
                        cf.conflictingBookingId(),
                        cf.conflictType(),
                        cf.locationId(),
                        cf.resourceId()))
                .collect(Collectors.toList());
    }

    /**
     * Maps a Booking entity to a BookingDetailResponse DTO.
     * Resource IDs and custom field values are loaded from the repository.
     * Custom field values are enriched with label/fieldType when CustomFieldClient succeeds.
     *
     * @param booking      The booking entity
     * @param conflictFlags Pre-computed conflict flags (read-only, never blocking)
     */
    private BookingDetailResponse toDetailResponse(
            Booking booking, List<BookingReadDtos.ConflictFlag> conflictFlags) {

        // Load resource IDs
        List<UUID> resourceIds = bookingResourceRepository.findByBookingId(booking.getId())
                .stream().map(BookingResource::getResourceId).collect(Collectors.toList());

        // Load custom field values, enriched with label/fieldType when available
        List<BookingCustomFieldValue> cfvs = bookingCustomFieldValueRepository.findByBookingId(booking.getId());
        List<CustomFieldValueOutput> cfvOutputs = enrichCustomFieldValues(cfvs, booking.getLocationId());

        return new BookingDetailResponse(
                booking.getId(),
                booking.getSeriesId(),
                booking.getTitle(),
                booking.getLocationId(),
                booking.getStartTime(),
                booking.getEndTime(),
                resourceIds,
                cfvOutputs,
                booking.getStatus(),
                booking.getOwnerId(),
                booking.getApprovedBy(),
                booking.getApprovedAt(),
                booking.getDeniedBy(),
                booking.getDeniedAt(),
                booking.getDenialReason(),
                booking.isAllDay(),
                booking.getDescription(),
                booking.getLayoutStyle(),
                booking.getContactName(),
                booking.getContactEmail(),
                booking.getContactNo(),
                conflictFlags != null ? conflictFlags : List.of(),
                booking.getCreatedAt(),
                booking.getUpdatedAt(),
                booking.getDeletedAt()
        );
    }

    /**
     * Enriches raw BookingCustomFieldValue rows with label/fieldType from CustomFieldClient.
     *
     * Named decision (graceful degradation): if CustomFieldClient returns empty (transient
     * outage or graceful fallback), raw values are returned with null label/fieldType —
     * same degradation policy as BookingWriteService.toResponse().
     */
    private List<CustomFieldValueOutput> enrichCustomFieldValues(
            List<BookingCustomFieldValue> cfvs, UUID locationId) {
        if (cfvs.isEmpty()) {
            return List.of();
        }

        // Attempt to fetch applicable field definitions for enrichment
        List<CustomFieldDefinition> definitions = locationId != null
                ? customFieldClient.getApplicableFields(locationId)
                : List.of();

        if (definitions.isEmpty()) {
            // Graceful degradation: no definitions available, return raw values
            return cfvs.stream()
                    .map(cfv -> new CustomFieldValueOutput(cfv.getCustomFieldId(), cfv.getValue(), null, null))
                    .collect(Collectors.toList());
        }

        // Build lookup map for enrichment
        java.util.Map<UUID, CustomFieldDefinition> defMap = definitions.stream()
                .collect(Collectors.toMap(CustomFieldDefinition::id, d -> d));

        return cfvs.stream()
                .map(cfv -> {
                    CustomFieldDefinition def = defMap.get(cfv.getCustomFieldId());
                    return new CustomFieldValueOutput(
                            cfv.getCustomFieldId(),
                            cfv.getValue(),
                            def != null ? def.label() : null,
                            def != null ? def.fieldType() : null);
                })
                .collect(Collectors.toList());
    }
}
