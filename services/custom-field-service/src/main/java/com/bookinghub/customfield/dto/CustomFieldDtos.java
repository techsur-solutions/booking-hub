package com.bookinghub.customfield.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Container class for CustomField DTOs (F5.1).
 *
 * Shapes match TechArch §4.4's CustomFieldUpsertRequest{label, field_type, options?}
 * exactly, extended with `required` per plan 04-03's F0-driven schema completion
 * (storage/round-trip only — see CustomField entity's javadoc for the enforcement
 * scope boundary).
 */
public class CustomFieldDtos {

    /**
     * Request body for POST/PUT /custom-fields.
     *
     * Named decision — out-of-enum field_type handling: the FRD's F5 error table
     * has no dedicated code for "unrecognized field_type value", so this is
     * enforced via standard Bean Validation (@Pattern) rather than a hand-thrown
     * ApiException. GlobalExceptionHandler's MethodArgumentNotValidException
     * handler shapes a failing match as 400 REQUEST_MALFORMED (TechArch §4.12's
     * platform-level cross-cutting code), keeping CUSTOM_FIELD_LABEL_REQUIRED and
     * CUSTOM_FIELD_OPTIONS_REQUIRED reserved for exactly the two cases the FRD
     * names them for.
     */
    public record CustomFieldUpsertRequest(
        String label,
        @JsonProperty("field_type")
        @Pattern(regexp = "textfield|select|textarea|radio|checkbox",
                 message = "field_type must be one of: textfield, select, textarea, radio, checkbox")
        String fieldType,
        List<String> options,
        Boolean required
    ) {}

    /**
     * Response body for all CustomField read/write endpoints.
     */
    public record CustomFieldResponse(
        UUID id,
        String label,
        @JsonProperty("field_type") String fieldType,
        List<String> options,
        boolean required,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("deleted_at") Instant deletedAt
    ) {}
}
