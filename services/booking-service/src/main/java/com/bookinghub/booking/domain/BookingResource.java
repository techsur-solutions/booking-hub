package com.bookinghub.booking.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapping to the booking_resources table (V1 schema, Phase 2 plan 02-01).
 *
 * Named decision (no @ManyToOne): bookingId is a plain UUID field, not a @ManyToOne
 * association to Booking — following Phase 3's PasswordResetToken.userId precedent:
 * "an internal single-service relationship, a full JPA association is unnecessary
 * ceremony". This avoids eager-loading, bidirectional-association complexity, and
 * cascade pitfalls — a simple UUID reference is sufficient for this service's reads.
 */
@Entity
@Table(name = "booking_resources")
public class BookingResource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected BookingResource() {
        // JPA requires no-arg constructor
    }

    public BookingResource(UUID bookingId, UUID resourceId) {
        this.bookingId = bookingId;
        this.resourceId = resourceId;
        this.createdAt = Instant.now();
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getBookingId() { return bookingId; }
    public UUID getResourceId() { return resourceId; }
    public Instant getCreatedAt() { return createdAt; }

    // Setters
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }
    public void setResourceId(UUID resourceId) { this.resourceId = resourceId; }
}
