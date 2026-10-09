package com.bookinghub.customfield.service;

import com.bookinghub.customfield.domain.CustomField;
import com.bookinghub.customfield.domain.CustomFieldJoin;
import com.bookinghub.customfield.domain.CustomFieldTemplate;
import com.bookinghub.customfield.domain.OutboxEvent;
import com.bookinghub.customfield.dto.CustomFieldDtos.CustomFieldResponse;
import com.bookinghub.customfield.dto.CustomFieldDtos.CustomFieldUpsertRequest;
import com.bookinghub.customfield.error.CustomFieldLabelRequiredException;
import com.bookinghub.customfield.error.CustomFieldNotFoundException;
import com.bookinghub.customfield.error.CustomFieldOptionsRequiredException;
import com.bookinghub.customfield.repository.CustomFieldJoinRepository;
import com.bookinghub.customfield.repository.CustomFieldRepository;
import com.bookinghub.customfield.repository.CustomFieldTemplateRepository;
import com.bookinghub.customfield.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * CustomField service implementing F5.1's definition CRUD, field_type enum
 * validation (F0-confirmed 5-value set), choice-based-type options[] validation
 * (a deliberate improvement over legacy's confirmed zero-validation), soft-delete
 * (legacy parity), and the F5.3/TechArch §2.4 applicability-query mechanism.
 *
 * Authorization note: this service does NOT check authorization — the CONTROLLER
 * layer (via @PreAuthorize guards) proves the caller holds role_customfield_admin
 * before invoking these methods, matching Phase 3's established pattern.
 */
@Service
public class CustomFieldService {

    /**
     * F0-confirmed 5-value field_type enum (findings/03-custom-fields.md, confirmed
     * via TWO independent citations — the admin form's <select> options AND the
     * render-time cfswitch): textfield, select, textarea, radio, checkbox.
     * Corrects TechArch's pre-F0 placeholder guess of 'text'|'number'|'date'|'select'
     * (no number/date type exists for custom fields at all). Primary enforcement
     * of this set happens at the DTO layer via @Pattern (see
     * CustomFieldUpsertRequest.fieldType), but this service-layer constant is kept
     * as a second, defense-in-depth line matching it exactly — CHOICE_BASED_TYPES
     * below is this set's strict subset requiring non-empty options[].
     */
    private static final Set<String> VALID_FIELD_TYPES =
        Set.of("textfield", "select", "textarea", "radio", "checkbox");

    private static final Set<String> CHOICE_BASED_TYPES = Set.of("select", "radio", "checkbox");

    private final CustomFieldRepository customFieldRepository;
    private final CustomFieldTemplateRepository customFieldTemplateRepository;
    private final CustomFieldJoinRepository customFieldJoinRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public CustomFieldService(
            CustomFieldRepository customFieldRepository,
            CustomFieldTemplateRepository customFieldTemplateRepository,
            CustomFieldJoinRepository customFieldJoinRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {
        this.customFieldRepository = customFieldRepository;
        this.customFieldTemplateRepository = customFieldTemplateRepository;
        this.customFieldJoinRepository = customFieldJoinRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Lists all non-deleted custom fields (contextId == null, unfiltered) or
     * exactly those applicable to a specific booking context.
     *
     * The F5.3/TechArch §2.4 applicability-query mechanism: resolves templates
     * where context_id = contextId OR context_id IS NULL (global), then joins
     * to each template's associated CustomFieldJoin rows, collects the distinct
     * field ids, and returns those non-deleted CustomFields. Phase 5's
     * booking-service calls GET /custom-fields?context_id={locationId} to
     * discover which fields are applicable before rendering/validating a
     * booking's custom_field_values[].
     */
    public List<CustomFieldResponse> listApplicableToContext(UUID contextId) {
        if (contextId == null) {
            return customFieldRepository.findAllByDeletedAtIsNull().stream()
                .map(this::toResponse)
                .toList();
        }

        // Optimized single-query fetch to avoid N+1 problem (W2 fix)
        return customFieldRepository.findApplicableToContext(contextId).stream()
            .map(this::toResponse)
            .toList();
    }

    public CustomFieldResponse get(UUID id) {
        CustomField field = customFieldRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new CustomFieldNotFoundException("Custom field not found: " + id));
        return toResponse(field);
    }

    /**
     * Creates a new CustomField definition.
     *
     * label non-empty → CUSTOM_FIELD_LABEL_REQUIRED (400) otherwise. field_type
     * enum membership is enforced at the DTO layer via @Pattern, so this method
     * can assume fieldType is one of the F0-confirmed 5 values by the time it
     * runs. options[] required and non-empty when fieldType is one of the 3
     * choice-based types (select/radio/checkbox) → CUSTOM_FIELD_OPTIONS_REQUIRED
     * (400) otherwise — a deliberate improvement over legacy's confirmed
     * zero-validation here, applied consistently to all 3 choice-based types
     * (not just select) since legacy's own admin form documents the identical
     * options[] JSON shape expectation for all three.
     */
    @Transactional
    public CustomFieldResponse create(CustomFieldUpsertRequest request) {
        validateLabel(request.label());
        validateOptions(request.fieldType(), request.options());

        CustomField field = new CustomField(
            request.label(),
            request.fieldType(),
            request.options(),
            Boolean.TRUE.equals(request.required())
        );
        customFieldRepository.save(field);

        writeOutboxEvent("customfield", field.getId(), "customfield.created", Map.of(
            "id", field.getId().toString(),
            "label", field.getLabel(),
            "field_type", field.getFieldType()
        ));

        return toResponse(field);
    }

    /**
     * Updates an existing CustomField definition. Same label/options validations
     * as create(). CUSTOM_FIELD_NOT_FOUND (404) if missing or already soft-deleted.
     */
    @Transactional
    public CustomFieldResponse update(UUID id, CustomFieldUpsertRequest request) {
        CustomField field = customFieldRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new CustomFieldNotFoundException("Custom field not found: " + id));

        validateLabel(request.label());
        validateOptions(request.fieldType(), request.options());

        field.setLabel(request.label());
        field.setFieldType(request.fieldType());
        field.setOptions(request.options());
        if (request.required() != null) {
            field.setRequired(request.required());
        }
        customFieldRepository.save(field);

        writeOutboxEvent("customfield", field.getId(), "customfield.updated", Map.of(
            "id", field.getId().toString(),
            "label", field.getLabel(),
            "field_type", field.getFieldType()
        ));

        return toResponse(field);
    }

    /**
     * Soft-deletes a CustomField definition — matching legacy parity (F0's
     * confirmed retain-don't-purge behavior, not an improvement). Sets
     * deletedAt; does NOT touch custom_field_joins/custom_field_values rows
     * referencing this field — they become orphaned-but-retained, exactly
     * matching legacy's confirmed behavior.
     */
    @Transactional
    public void delete(UUID id) {
        CustomField field = customFieldRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new CustomFieldNotFoundException("Custom field not found: " + id));

        field.setDeletedAt(Instant.now());
        customFieldRepository.save(field);

        writeOutboxEvent("customfield", field.getId(), "customfield.deleted", Map.of(
            "id", field.getId().toString()
        ));
    }

    private void validateLabel(String label) {
        if (!StringUtils.hasText(label)) {
            throw new CustomFieldLabelRequiredException("Custom field label is required");
        }
    }

    private void validateOptions(String fieldType, List<String> options) {
        // Defense-in-depth: the DTO's @Pattern already enforces VALID_FIELD_TYPES
        // membership before this method runs, but asserting it here means this
        // service layer never silently trusts an out-of-enum value even if some
        // future caller bypasses the DTO validation path.
        if (!VALID_FIELD_TYPES.contains(fieldType)) {
            throw new IllegalStateException("Unexpected field_type reached service layer: " + fieldType);
        }

        if (CHOICE_BASED_TYPES.contains(fieldType) && (options == null || options.isEmpty())) {
            throw new CustomFieldOptionsRequiredException(
                "Options are required for selection-type fields");
        }
    }

    private CustomFieldResponse toResponse(CustomField field) {
        return new CustomFieldResponse(
            field.getId(),
            field.getLabel(),
            field.getFieldType(),
            field.getOptions(),
            field.isRequired(),
            field.getCreatedAt(),
            field.getUpdatedAt(),
            field.getDeletedAt()
        );
    }

    private void writeOutboxEvent(String aggregateType, UUID aggregateId, String routingKey,
                                    Map<String, Object> payloadData) {
        String payload = buildEventPayload(payloadData);
        OutboxEvent event = new OutboxEvent(
            aggregateType,
            aggregateId,
            "customfield.events",
            routingKey,
            payload
        );
        outboxEventRepository.save(event);
    }

    private String buildEventPayload(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event payload", e);
        }
    }
}
