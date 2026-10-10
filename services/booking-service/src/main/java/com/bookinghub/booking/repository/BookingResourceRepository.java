package com.bookinghub.booking.repository;

import com.bookinghub.booking.domain.BookingResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for BookingResource entities.
 *
 * findByBookingId: used by booking read operations to load associated resources.
 * deleteByBookingId: used by booking delete/update operations to remove stale resource links.
 * findConflictingBookingIdsByResource: conflict-detection query (plan 05-02) —
 *   uses TechArch §3.3's exact half-open-interval predicate shape verbatim.
 *   Called ONCE PER resource id in a loop in ConflictDetectionService (named decision:
 *   simpler than a single IN/ANY array query, avoids JDBC UUID-array binding complexity,
 *   produces the identical UNION of per-resource results TechArch's text describes).
 */
public interface BookingResourceRepository extends JpaRepository<BookingResource, UUID> {

    List<BookingResource> findByBookingId(UUID bookingId);

    void deleteByBookingId(UUID bookingId);

    /**
     * Returns booking IDs that use the given resource and overlap the proposed time range.
     *
     * Half-open interval semantics (TechArch §3.3 verbatim):
     *   b.start_time < :proposedEndTime AND :proposedStartTime < b.end_time
     *
     * Excludes:
     * - soft-deleted parent bookings (b.deleted_at IS NULL)
     * - denied parent bookings (b.status <> 'denied')
     * - the booking's own prior state on edit (:excludeBookingId)
     *
     * When excludeBookingId is null, COALESCE sentinel prevents self-exclusion effect.
     */
    @Query(value = """
        SELECT br.booking_id FROM booking_resources br
        JOIN bookings b ON b.id = br.booking_id
        WHERE br.resource_id = :resourceId
          AND b.deleted_at IS NULL
          AND b.status <> 'denied'
          AND b.id <> COALESCE(:excludeBookingId, '00000000-0000-0000-0000-000000000000'::uuid)
          AND b.start_time < :proposedEndTime
          AND :proposedStartTime < b.end_time
        """, nativeQuery = true)
    List<UUID> findConflictingBookingIdsByResource(
            @Param("resourceId") UUID resourceId,
            @Param("proposedStartTime") Instant proposedStartTime,
            @Param("proposedEndTime") Instant proposedEndTime,
            @Param("excludeBookingId") UUID excludeBookingId);
}
