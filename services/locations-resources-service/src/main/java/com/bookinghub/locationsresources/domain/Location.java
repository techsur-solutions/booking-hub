package com.bookinghub.locationsresources.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Location entity mapping to the locations table (V1 DDL + V2 F0-completion migration).
 *
 * F0-confirmed full field set: name, cssClass, colour, description, building, layout
 * (plus system-managed id/createdAt/updatedAt/deletedAt).
 *
 * `colour` is a distinct hex-colour field (UI widget: colourpicker) from `cssClass` —
 * the FRD's Inputs section incorrectly conflated the two, corrected by F0's audit
 * (findings/02-reference-data.md).
 *
 * `layout` (singular column name, already executed by Phase 2's V1 migration — not
 * renamed) stores legacy's free-text comma-separated `layouts` field as a JSON array
 * of strings via Hibernate 6's native JSON mapping (same pattern as Phase 3's
 * Permission.gatedActions, extended here to a typed List<String>).
 */
@Entity
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "css_class")
    private String cssClass;

    @Column(name = "colour")
    private String colour;

    @Column(name = "description")
    private String description;

    @Column(name = "building")
    private String building;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "layout", columnDefinition = "jsonb")
    private List<String> layout;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected Location() {
        // JPA requires no-arg constructor
    }

    public Location(String name, String cssClass, String colour, String description,
                     String building, List<String> layout) {
        this.name = name;
        this.cssClass = cssClass;
        this.colour = colour;
        this.description = description;
        this.building = building;
        this.layout = layout;
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

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public List<String> getLayout() {
        return layout;
    }

    public void setLayout(List<String> layout) {
        this.layout = layout;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }
}
