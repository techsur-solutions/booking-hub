package com.bookinghub.customfield.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Container class for FieldTemplate DTOs (F5.2).
 *
 * Shapes match TechArch §4.4's FieldTemplateUpsertRequest{name, context_id?,
 * field_ids?} exactly. contextId == null means global (applies to every
 * booking context) — see the context/template model simplification named
 * decision in plan 04-04 Task 2: this is a deliberate improvement over
 * legacy's coarser two-tier (all-events/all-locations) scoping, not a parity
 * claim.
 */
public class FieldTemplateDtos {

    /**
     * Request body for POST/PUT /field-templates.
     *
     * Named decision: `name` is validated via plain Bean Validation (@NotBlank),
     * not a hand-thrown ApiException — the FRD's F5 error table has no
     * template-specific "name required" code, so reusing
     * CUSTOM_FIELD_LABEL_REQUIRED here would be inconsistent (that code is
     * reserved for Custom Field definitions specifically). A blank name maps
     * to 400 REQUEST_MALFORMED via GlobalExceptionHandler's
     * MethodArgumentNotValidException handler (added in Task 1).
     */
    public record FieldTemplateUpsertRequest(
        @NotBlank(message = "Field Template name is required")
        String name,
        @JsonProperty("context_id") UUID contextId,
        @JsonProperty("field_ids") List<UUID> fieldIds
    ) {}

    /**
     * Response body for all FieldTemplate read/write endpoints.
     */
    public record FieldTemplateResponse(
        UUID id,
        String name,
        @JsonProperty("context_id") UUID contextId,
        @JsonProperty("field_ids") List<UUID> fieldIds,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt
    ) {}
}
