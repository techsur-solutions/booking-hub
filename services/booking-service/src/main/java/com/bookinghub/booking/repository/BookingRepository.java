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
 *
 * findInRange: date-range bulk-read query (plan 05-04) — half-open RANGE OVERLAP
 * test with optional location/status/keyword filters for calendar/day/list views.
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

    /**
     * Bulk date-range read for calendar/day/list views (plan 05-04).
     *
     * Half-open RANGE OVERLAP test (not containment): a booking partially inside the
     * requested window is still returned, matching how a calendar needs to render
     * partially-visible events. Excludes only soft-deleted bookings (deleted_at IS NULL) —
     * unlike conflict detection, denied bookings ARE returned (they are relevant as
     * historical/calendar context, just not occupying the slot for conflict purposes).
     *
     * Optional filters: locationId, status, q (keyword search on title/description).
     * All optional filters use JPQL coalesce-style null bypass (IS NULL OR b.field = :param).
     *
     * Named decision: status and q are NAMED ADDITIONS beyond TechArch §4.2's literal
     * 4-parameter table (from, to, location_id?, resource_id?) — restoring legacy's own
     * list()-action filter capabilities (status= and title/description LIKE keyword search
     * confirmed by F0 findings/01-booking-core.md) as query params on the SAME endpoint.
     */
    @Query("SELECT b FROM Booking b WHERE b.deletedAt IS NULL " +
           "AND b.startTime < :to AND :from < b.endTime " +
           "AND (:locationId IS NULL OR b.locationId = :locationId) " +
           "AND (:status IS NULL OR b.status = :status) " +
           "AND (:q IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "     OR LOWER(b.description) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "ORDER BY b.startTime ASC")
    List<Booking> findInRange(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("locationId") UUID locationId,
            @Param("status") String status,
            @Param("q") String q);
}
