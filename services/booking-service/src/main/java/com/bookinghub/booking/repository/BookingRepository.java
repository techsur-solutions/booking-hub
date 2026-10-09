package com.bookinghub.booking.repository;

import com.bookinghub.booking.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Booking entities.
 *
 * findByIdAndDeletedAtIsNull: soft-delete-aware lookup — used by all read/write
 * operations that must treat soft-deleted bookings as absent (plan 05-03/05-04).
 *
 * findConflictingBookingIdsByLocation: conflict-detection query (plan 05-02) —
 * uses TechArch §3.3's exact half-open-interval predicate shape verbatim.
 * Plan 05-04 will add date-range bulk-read queries.
 */
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    /**
     * Finds a non-soft-deleted booking by id. Returns empty if booking does not
     * exist OR if it has been soft-deleted (deleted_at IS NOT NULL).
     */
    Optional<Booking> findByIdAndDeletedAtIsNull(UUID id);

    /**
     * Returns IDs of bookings at the same location that overlap the proposed time range.
     *
     * Half-open interval semantics (TechArch §3.3 verbatim):
     *   start_time < :proposedEndTime AND :proposedStartTime < end_time
     * A booking ending at exactly proposedStartTime does NOT overlap (boundary is excluded).
     *
     * Excludes:
     * - soft-deleted bookings (deleted_at IS NOT NULL)
     * - denied bookings (status = 'denied') — they no longer claim the slot
     * - the booking's own prior state when editing (:excludeBookingId)
     *
     * When excludeBookingId is null, COALESCE replaces it with a sentinel UUID that
     * no real booking can have, so the self-exclusion predicate has no effect.
     */
    @Query(value = """
        SELECT id FROM bookings
        WHERE location_id = :locationId
          AND deleted_at IS NULL
          AND status <> 'denied'
          AND id <> COALESCE(:excludeBookingId, '00000000-0000-0000-0000-000000000000'::uuid)
          AND start_time < :proposedEndTime
          AND :proposedStartTime < end_time
        """, nativeQuery = true)
    List<UUID> findConflictingBookingIdsByLocation(
            @Param("locationId") UUID locationId,
            @Param("proposedStartTime") Instant proposedStartTime,
            @Param("proposedEndTime") Instant proposedEndTime,
            @Param("excludeBookingId") UUID excludeBookingId);
}
