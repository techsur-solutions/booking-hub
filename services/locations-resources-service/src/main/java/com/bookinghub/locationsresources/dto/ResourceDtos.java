package com.bookinghub.locationsresources.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Request/response DTOs for Resource CRUD (F4.2).
 *
 * Field names are mapped to the F0-confirmed wire shape (is_unique,
 * restrict_locations, created_at, etc.) via @JsonProperty — this phase's
 * F0-driven extension of TechArch's original ResourceUpsertRequest{name}.
 */
public class ResourceDtos {

    private ResourceDtos() {
        // Namespace holder, not instantiable
    }

    public record ResourceUpsertRequest(
        @JsonProperty("name") String name,
        @JsonProperty("type") String type,
        @JsonProperty("description") String description,
        @JsonProperty("is_unique") Boolean isUnique,
        @JsonProperty("restrict_locations") List<UUID> restrictLocations
    ) {}

    public record ResourceResponse(
        @JsonProperty("id") UUID id,
        @JsonProperty("name") String name,
        @JsonProperty("type") String type,
        @JsonProperty("description") String description,
        @JsonProperty("is_unique") boolean isUnique,
        @JsonProperty("restrict_locations") List<UUID> restrictLocations,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("deleted_at") Instant deletedAt
    ) {}
}
