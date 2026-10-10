package com.bookinghub.booking.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapping to the bookings table (V1 schema from Phase 2 plan 02-01
 * + V2 F0 schema-completion additions from this plan 05-01).
 *
 * Maps 1:1 to the full V1+V2 bookings schema including the six F0-confirmed
 * legacy Event fields that TechArch's placeholder DDL omitted:
 * allDay, description, layoutStyle, contactName, contactEmail, contactNo.
 *
 * Named decision (id generation): Unlike Phase 3's User.id (which must be set
 * to the Keycloak sub claim at insert time), Booking.id has no external-identity
 * constraint. The DB defaults gen_random_uuid() and Hibernate's
 * @GeneratedValue(strategy = GenerationType.UUID) both produce UUIDs — using
 * Hibernate's generation is correct here (no external system dictates the value).
 *
 * Named decision (no @ManyToOne associations): booking_resources and
 * booking_custom_field_values are related by simple bookingId UUID fields,
 * following Phase 3's PasswordResetToken.userId precedent — "an internal
 * single-service relationship, a full JPA association is unnecessary ceremony".
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "series_id")
    private UUID seriesId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "location_id", nullable = false)
    private UUID locationId;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "denied_by")
    private UUID deniedBy;

    @Column(name = "denied_at")
    private Instant deniedAt;

    @Column(name = "denial_reason")
    private String denialReason;

    // ── F0 schema-completion fields (V2 migration, plan 05-01) ─────────────
    // TechArch's V1 DDL omitted these 6 confirmed legacy Event fields entirely.

    @Column(name = "all_day", nullable = false)
    private boolean allDay = false;

    @Column(name = "description")
    private String description;

    @Column(name = "layout_style", length = 255)
    private String layoutStyle;

    @Column(name = "contact_name", length = 255)
    private String contactName;

    @Column(name = "contact_email", length = 320)
    private String contactEmail;

    @Column(name = "contact_no", length = 50)
    private String contactNo;

    // ── Audit timestamps ────────────────────────────────────────────────────

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public Booking() {
        // JPA requires no-arg constructor
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
        if (status == null) {
            status = "pending";
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getSeriesId() { return seriesId; }
    public String getTitle() { return title; }
    public UUID getLocationId() { return locationId; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public String getStatus() { return status; }
    public UUID getOwnerId() { return ownerId; }
    public UUID getApprovedBy() { return approvedBy; }
    public Instant getApprovedAt() { return approvedAt; }
    public UUID getDeniedBy() { return deniedBy; }
    public Instant getDeniedAt() { return deniedAt; }
    public String getDenialReason() { return denialReason; }
    public boolean isAllDay() { return allDay; }
    public String getDescription() { return description; }
    public String getLayoutStyle() { return layoutStyle; }
    public String getContactName() { return contactName; }
    public String getContactEmail() { return contactEmail; }
    public String getContactNo() { return contactNo; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getDeletedAt() { return deletedAt; }

    // Setters
    public void setSeriesId(UUID seriesId) { this.seriesId = seriesId; }
    public void setTitle(String title) { this.title = title; }
    public void setLocationId(UUID locationId) { this.locationId = locationId; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
    public void setStatus(String status) { this.status = status; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
    public void setApprovedBy(UUID approvedBy) { this.approvedBy = approvedBy; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }
    public void setDeniedBy(UUID deniedBy) { this.deniedBy = deniedBy; }
    public void setDeniedAt(Instant deniedAt) { this.deniedAt = deniedAt; }
    public void setDenialReason(String denialReason) { this.denialReason = denialReason; }
    public void setAllDay(boolean allDay) { this.allDay = allDay; }
    public void setDescription(String description) { this.description = description; }
    public void setLayoutStyle(String layoutStyle) { this.layoutStyle = layoutStyle; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
}
