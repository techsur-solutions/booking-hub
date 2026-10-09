package com.bookinghub.customfield.service;

import com.bookinghub.customfield.domain.CustomFieldJoin;
import com.bookinghub.customfield.domain.CustomFieldTemplate;
import com.bookinghub.customfield.domain.OutboxEvent;
import com.bookinghub.customfield.dto.FieldTemplateDtos.FieldTemplateResponse;
import com.bookinghub.customfield.dto.FieldTemplateDtos.FieldTemplateUpsertRequest;
import com.bookinghub.customfield.error.CustomFieldNotFoundException;
import com.bookinghub.customfield.repository.CustomFieldJoinRepository;
import com.bookinghub.customfield.repository.CustomFieldRepository;
import com.bookinghub.customfield.repository.CustomFieldTemplateRepository;
import com.bookinghub.customfield.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * FieldTemplate service implementing F5.2's template CRUD and
 * Custom-Field-to-Template join management.
 *
 * Named decision — context/template model simplification (closes
 * open-questions.md #6's join-model sub-question): legacy's model is two-tier
 * (Customfield.parentmodel scopes a field DEFINITION to an entire model type —
 * 'event' or 'location' — with no per-specific-Location scoping at all). This
 * system's custom_field_templates.context_id instead references a SPECIFIC
 * Location id directly: null = global (all bookings), a real Location id =
 * that Location only — more granular than legacy's all-locations-blanket
 * scoping, a deliberate improvement, not a parity claim.
 *
 * Authorization note: this service does NOT check authorization — the
 * CONTROLLER layer (via @PreAuthorize guards) proves the caller holds
 * role_customfield_admin before invoking these methods.
 */
@Service
public class FieldTemplateService {

    private final CustomFieldTemplateRepository customFieldTemplateRepository;
    private final CustomFieldJoinRepository customFieldJoinRepository;
    private final CustomFieldRepository customFieldRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public FieldTemplateService(
            CustomFieldTemplateRepository customFieldTemplateRepository,
            CustomFieldJoinRepository customFieldJoinRepository,
            CustomFieldRepository customFieldRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {
        this.customFieldTemplateRepository = customFieldTemplateRepository;
        this.customFieldJoinRepository = customFieldJoinRepository;
        this.customFieldRepository = customFieldRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public List<FieldTemplateResponse> list() {
        return customFieldTemplateRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    public FieldTemplateResponse get(UUID id) {
        CustomFieldTemplate template = findTemplateOrThrow(id);
        return toResponse(template);
    }

    /**
     * Creates a new FieldTemplate, persisting a CustomFieldJoin row for each
     * id in fieldIds. Each referenced fieldId is validated to exist and be
     * non-deleted (CUSTOM_FIELD_NOT_FOUND 404 otherwise) before any join is
     * created.
     */
    @Transactional
    public FieldTemplateResponse create(FieldTemplateUpsertRequest request) {
        List<UUID> fieldIds = request.fieldIds() != null ? request.fieldIds() : List.of();
        validateFieldIdsExist(fieldIds);

        CustomFieldTemplate template = new CustomFieldTemplate(request.name(), request.contextId());
        customFieldTemplateRepository.save(template);

        createJoins(template.getId(), fieldIds);

        writeOutboxEvent("template", template.getId(), "template.created", Map.of(
            "id", template.getId().toString(),
            "name", template.getName()
        ));

        return toResponse(template);
    }

    /**
     * Updates name/contextId and REPLACES (not appends to) the join set —
     * matching the API contract's array-shape field_ids[], same reasoning as
     * Phase 3's updateUserRealmRoles REPLACE-not-additive precedent.
     */
    @Transactional
    public FieldTemplateResponse update(UUID id, FieldTemplateUpsertRequest request) {
        CustomFieldTemplate template = findTemplateOrThrow(id);

        List<UUID> fieldIds = request.fieldIds() != null ? request.fieldIds() : List.of();
        validateFieldIdsExist(fieldIds);

        template.setName(request.name());
        template.setContextId(request.contextId());
        customFieldTemplateRepository.save(template);

        // REPLACE semantics: delete the entire existing join set, then re-create.
        customFieldJoinRepository.deleteByCustomFieldTemplateId(id);
        createJoins(id, fieldIds);

        writeOutboxEvent("template", template.getId(), "template.updated", Map.of(
            "id", template.getId().toString(),
            "name", template.getName()
        ));

        return toResponse(template);
    }

    /**
     * Hard delete — no deleted_at column exists on custom_field_templates
     * (named distinction from CustomField's soft-delete, explicitly called
     * out in plan 04-03 Task 1). Relies on the V1 schema's ON DELETE CASCADE
     * on custom_field_joins.custom_field_template_id rather than manually
     * deleting joins first, to avoid double-work.
     */
    @Transactional
    public void delete(UUID id) {
        CustomFieldTemplate template = findTemplateOrThrow(id);

        customFieldTemplateRepository.deleteById(id);

        writeOutboxEvent("template", id, "template.deleted", Map.of(
            "id", id.toString()
        ));
    }

    private CustomFieldTemplate findTemplateOrThrow(UUID id) {
        return customFieldTemplateRepository.findById(id)
            .orElseThrow(() -> new CustomFieldNotFoundException("Field template not found: " + id));
    }

    private void validateFieldIdsExist(List<UUID> fieldIds) {
        for (UUID fieldId : fieldIds) {
            customFieldRepository.findByIdAndDeletedAtIsNull(fieldId)
                .orElseThrow(() -> new CustomFieldNotFoundException(
                    "Custom field not found: " + fieldId));
        }
    }

    private void createJoins(UUID templateId, List<UUID> fieldIds) {
        for (UUID fieldId : fieldIds) {
            customFieldJoinRepository.save(new CustomFieldJoin(templateId, fieldId));
        }
    }

    private FieldTemplateResponse toResponse(CustomFieldTemplate template) {
        List<UUID> fieldIds = customFieldJoinRepository.findByCustomFieldTemplateId(template.getId()).stream()
            .map(CustomFieldJoin::getCustomFieldId)
            .toList();

        return new FieldTemplateResponse(
            template.getId(),
            template.getName(),
            template.getContextId(),
            fieldIds,
            template.getCreatedAt(),
            template.getUpdatedAt()
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
