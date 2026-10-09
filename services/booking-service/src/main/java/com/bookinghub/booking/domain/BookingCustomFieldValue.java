package com.bookinghub.booking.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapping to the booking_custom_field_values table (V2 migration, plan 05-01).
 *
 * Named decision (DB-ownership fix): This table lives in booking_db (this service's
 * own database). custom_field_id is a UUID-only cross-service reference to custom-field-service,
 * with NO database FK (the referenced table lives in customfld_db — a different service's
 * database). This matches the identical pattern already established for location_id/resource_id
 * on the bookings table.
 *
 * Applicability validation (was the submitted custom_field_id actually applicable to
 * this booking's context?) is enforced by plan 05-02's synchronous CustomFieldClient call —
 * not by a DB-level FK constraint, by design per the database-per-service NFR.
 *
 * Named decision (no @ManyToOne): Same precedent as BookingResource — plain UUID field,
 * not a @ManyToOne association, for the same reasons.
 */
@Entity
@Table(name = "booking_custom_field_values")
public class BookingCustomFieldValue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    /**
     * Cross-service reference to custom-field-service's custom_fields.id.
     * No FK constraint — different database (customfld_db). Validated via
     * plan 05-02's CustomFieldClient applicability call.
     */
    @Column(name = "custom_field_id", nullable = false)
    private UUID customFieldId;

    @Column(name = "value")
    private String value;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected BookingCustomFieldValue() {
        // JPA requires no-arg constructor
    }

    public BookingCustomFieldValue(UUID bookingId, UUID customFieldId, String value) {
        this.bookingId = bookingId;
        this.customFieldId = customFieldId;
        this.value = value;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getBookingId() { return bookingId; }
    public UUID getCustomFieldId() { return customFieldId; }
    public String getValue() { return value; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // Setters
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }
    public void setCustomFieldId(UUID customFieldId) { this.customFieldId = customFieldId; }
    public void setValue(String value) { this.value = value; }
}
