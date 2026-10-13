package com.bookinghub.feeds.repository;

import com.bookinghub.feeds.domain.FeedBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data repository over the feed_bookings projection.
 *
 * This file is owned by exactly ONE plan (07-01): it declares every query the
 * downstream plans need, so neither plan 07-02 nor 07-03 ever has to edit it.
 *
 * - Read queries (approved + upcoming + ordered, with and without location
 *   filter): plan 07-03's public feed endpoints call these.
 * - updateLocationName @Modifying mutation: plan 07-02's consumer back-fill
 *   hook, invoked when a location.updated event renames a location so every
 *   cached projection row for that location gets the new name.
 */
public interface FeedBookingRepository extends JpaRepository<FeedBooking, UUID> {

    List<FeedBooking> findByStatusAndStartTimeGreaterThanEqualOrderByStartTimeAsc(
        String status, Instant now);

    List<FeedBooking> findByStatusAndLocationIdAndStartTimeGreaterThanEqualOrderByStartTimeAsc(
        String status, UUID locationId, Instant now);

    @Modifying
    @Query("update FeedBooking f set f.locationName = :name where f.locationId = :locationId")
    int updateLocationName(@Param("locationId") UUID locationId, @Param("name") String name);
}
