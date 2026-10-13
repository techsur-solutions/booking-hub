package com.bookinghub.feeds.repository;

import com.bookinghub.feeds.domain.LocationName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data repository over the V3-added location_names cache table.
 *
 * findById + save (inherited from JpaRepository) are sufficient:
 * - FeedProjectionService.resolveLocationName looks up the last-known name when
 *   projecting a booking (cache hit -> real name; miss -> graceful placeholder).
 * - FeedProjectionService.applyLocationEvent upserts the cache on location.updated.
 */
public interface LocationNameRepository extends JpaRepository<LocationName, UUID> {
}
