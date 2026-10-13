package com.bookinghub.feeds.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity over the V3-added location_names cache table.
 *
 * This is feeds-service's event-fed denormalization cache (location_id -> name).
 * It is populated from location.updated events by {@code FeedProjectionService}
 * and consulted when projecting a booking so that feed_bookings.location_name
 * (NOT NULL) can always be satisfied with the last-known real name — without
 * feeds-service ever querying locations-resources-service's database
 * (database-per-service invariant).
 *
 * location_id is NOT @GeneratedValue — it is the location's own id carried in the
 * inbound location.* event payload, set explicitly by the consumer.
 *
 * updated_at is DB-default-managed, so it is read-only from JPA
 * (insertable=false, updatable=false).
 */
@Entity
@Table(name = "location_names")
@Getter
@Setter
@NoArgsConstructor
public class LocationName {

    @Id
    @Column(name = "location_id")
    private UUID locationId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}
