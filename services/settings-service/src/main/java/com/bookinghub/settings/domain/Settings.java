package com.bookinghub.settings.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Settings entity mapping to the singleton settings table created by Phase 2
 * plan 02-07's V1__init_schema.sql (seed row id=1 already inserted there).
 *
 * CRITICAL: id field has NO @GeneratedValue — this is the code-level enforcement
 * of the singleton invariant (same reasoning as users-permissions-service's
 * User.id). The DB's chk_settings_singleton CHECK (id = 1) constraint is the
 * independent, second layer of defense: no code path can ever produce a row
 * with any id other than the one explicitly set to 1, and even if it tried,
 * the DB would reject it.
 *
 * Scope is intentionally narrower than legacy's full 33-row settings table
 * (F0 findings/05-platform-settings.md catalogued all 33 legacy rows across
 * General/Email/Calendar/Locations categories) — this entity carries forward
 * only the 4 fields F10's success criteria require: approveBooking,
 * calendarSlotSize, calendarMinTime, calendarMaxTime. Site branding, email
 * config, and the remaining legacy rows are deliberately out of scope for
 * this service.
 */
@Entity
@Table(name = "settings")
public class Settings {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id; // NO @GeneratedValue — fixed singleton id=1, never auto-generated

    @Column(name = "approve_booking", nullable = false)
    private boolean approveBooking;

    @Column(name = "calendar_slot_size", nullable = false)
    private int calendarSlotSize;

    @Column(name = "calendar_min_time", nullable = false)
    private LocalTime calendarMinTime;

    @Column(name = "calendar_max_time", nullable = false)
    private LocalTime calendarMaxTime;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    protected Settings() {
        // JPA requires no-arg constructor
    }

    public Settings(Integer id, boolean approveBooking, int calendarSlotSize,
                     LocalTime calendarMinTime, LocalTime calendarMaxTime) {
        this.id = id;
        this.approveBooking = approveBooking;
        this.calendarSlotSize = calendarSlotSize;
        this.calendarMinTime = calendarMinTime;
        this.calendarMaxTime = calendarMaxTime;
        this.updatedAt = Instant.now();
    }

    // Getters
    public Integer getId() { return id; }
    public boolean isApproveBooking() { return approveBooking; }
    public int getCalendarSlotSize() { return calendarSlotSize; }
    public LocalTime getCalendarMinTime() { return calendarMinTime; }
    public LocalTime getCalendarMaxTime() { return calendarMaxTime; }
    public Instant getUpdatedAt() { return updatedAt; }
    public UUID getUpdatedBy() { return updatedBy; }

    // Setters for mutable fields (plan 04-06's PUT handler uses these)
    public void setApproveBooking(boolean approveBooking) {
        this.approveBooking = approveBooking;
        this.updatedAt = Instant.now();
    }

    public void setCalendarSlotSize(int calendarSlotSize) {
        this.calendarSlotSize = calendarSlotSize;
        this.updatedAt = Instant.now();
    }

    public void setCalendarMinTime(LocalTime calendarMinTime) {
        this.calendarMinTime = calendarMinTime;
        this.updatedAt = Instant.now();
    }

    public void setCalendarMaxTime(LocalTime calendarMaxTime) {
        this.calendarMaxTime = calendarMaxTime;
        this.updatedAt = Instant.now();
    }

    public void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
