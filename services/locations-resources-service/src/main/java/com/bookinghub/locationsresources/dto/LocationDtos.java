package com.bookinghub.locationsresources.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Request/response DTOs for Location CRUD (F4.1).
 *
 * Field names are mapped to TechArch §4.3's snake_case wire shape via
 * @JsonProperty — Java record field names stay camelCase internally.
 * `colour`/`description` are this phase's F0-driven additions to TechArch's
 * original `LocationUpsertRequest{name, css_class?, building?, layout?}`
 * (see V2 migration's named decision, open-questions.md #12).
 */
public class LocationDtos {

    private LocationDtos() {
        // Namespace holder, not instantiable
    }

    public record LocationUpsertRequest(
        @JsonProperty("name") String name,
        @JsonProperty("css_class") String cssClass,
        @JsonProperty("colour") String colour,
        @JsonProperty("description") String description,
        @JsonProperty("building") String building,
        @JsonProperty("layout") List<String> layout
    ) {}

    public record LocationResponse(
        @JsonProperty("id") UUID id,
        @JsonProperty("name") String name,
        @JsonProperty("css_class") String cssClass,
        @JsonProperty("colour") String colour,
        @JsonProperty("description") String description,
        @JsonProperty("building") String building,
        @JsonProperty("layout") List<String> layout,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("deleted_at") Instant deletedAt
    ) {}
}
