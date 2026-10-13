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
 * JPA entity mapping 1:1 to the V1 feed_bookings projection table.
 *
 * This denormalized projection is written by plan 07-02's RabbitMQ consumer
 * (from booking.* and location.* events) and read by plan 07-03's four public
 * feed endpoints (RSS2, iCal, JSON, display-board).
 *
 * booking_id is NOT @GeneratedValue — it is the booking's own id carried in the
 * inbound event payload, set explicitly by the consumer (plan 07-02).
 *
 * updated_at is DB-default + trigger-managed (V1's set_updated_at trigger), so
 * it is read-only from JPA (insertable=false, updatable=false).
 */
@Entity
@Table(name = "feed_bookings")
@Getter
@Setter
@NoArgsConstructor
public class FeedBooking {

    @Id
    @Column(name = "booking_id")
    private UUID bookingId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "location_id", nullable = false)
    private UUID locationId;

    @Column(name = "location_name", nullable = false)
    private String locationName;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}
