package com.bookinghub.locationsresources.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Resource entity mapping to the resources table (V1 DDL + V2 F0-completion migration).
 *
 * F0-confirmed full field set: name, type, description, isUnique, restrictLocations
 * (plus system-managed id/createdAt/updatedAt/deletedAt). TechArch's V1 DDL only had
 * `name` — this entity's remaining fields map to the V2 migration's additive columns.
 *
 * `isUnique` governs per-resource double-booking restriction semantics, consumed by
 * Phase 5's conflict-detection logic. `restrictLocations` is an optional list of
 * Location ids this Resource may be booked at, stored as JSONB via Hibernate 6's
 * native JSON mapping (same pattern as Location.layout).
 */
@Entity
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type")
    private String type;

    @Column(name = "description")
    private String description;

    @Column(name = "is_unique", nullable = false)
    private boolean isUnique;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "restrict_locations", columnDefinition = "jsonb")
    private List<UUID> restrictLocations;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected Resource() {
        // JPA requires no-arg constructor
    }

    public Resource(String name, String type, String description, boolean isUnique,
                     List<UUID> restrictLocations) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.isUnique = isUnique;
        this.restrictLocations = restrictLocations;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isUnique() {
        return isUnique;
    }

    public void setUnique(boolean isUnique) {
        this.isUnique = isUnique;
    }

    public List<UUID> getRestrictLocations() {
        return restrictLocations;
    }

    public void setRestrictLocations(List<UUID> restrictLocations) {
        this.restrictLocations = restrictLocations;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }
}
