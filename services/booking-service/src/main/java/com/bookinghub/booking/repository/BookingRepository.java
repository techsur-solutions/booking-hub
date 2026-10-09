package com.bookinghub.booking.repository;

import com.bookinghub.booking.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Booking entities.
 *
 * findByIdAndDeletedAtIsNull: soft-delete-aware lookup — used by all read/write
 * operations that must treat soft-deleted bookings as absent (plan 05-03/05-04).
 *
 * Plan 05-02 will add native conflict-detection queries (time-range overlap checks
 * against location_id and resource_id). Plan 05-04 will add date-range bulk-read
 * queries. Both are sequential additions to this file in later waves, which is
 * correct since those plans depend on this one.
 */
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    /**
     * Finds a non-soft-deleted booking by id. Returns empty if booking does not
     * exist OR if it has been soft-deleted (deleted_at IS NOT NULL).
     */
    Optional<Booking> findByIdAndDeletedAtIsNull(UUID id);
}
