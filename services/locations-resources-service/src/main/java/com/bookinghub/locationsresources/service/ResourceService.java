package com.bookinghub.locationsresources.service;

import com.bookinghub.locationsresources.domain.OutboxEvent;
import com.bookinghub.locationsresources.domain.Resource;
import com.bookinghub.locationsresources.dto.ResourceDtos.ResourceResponse;
import com.bookinghub.locationsresources.dto.ResourceDtos.ResourceUpsertRequest;
import com.bookinghub.locationsresources.error.ResourceNameRequiredException;
import com.bookinghub.locationsresources.error.ResourceNotFoundException;
import com.bookinghub.locationsresources.repository.OutboxEventRepository;
import com.bookinghub.locationsresources.repository.ResourceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for Resource CRUD (F4.2) — same shape as LocationService, including
 * the identical soft-delete-always-succeeds policy (F4.3). F0 confirmed
 * Resource's legacy hard-delete was even MORE permissive than Location's (no
 * "last one standing" check at all), so this service extends the same policy
 * identically.
 */
@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public ResourceService(ResourceRepository resourceRepository,
                            OutboxEventRepository outboxEventRepository,
                            ObjectMapper objectMapper) {
        this.resourceRepository = resourceRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Lists all ACTIVE (non-soft-deleted) Resources. Backs GET /resources.
     */
    public List<ResourceResponse> listActive() {
        return resourceRepository.findAllByDeletedAtIsNull().stream()
            .map(this::toResponse)
            .toList();
    }

    /**
     * Reads a single Resource by id — the Success Criterion 2 last-known-values
     * lookup path. Plain resourceRepository#findById (NOT
     * findByIdAndDeletedAtIsNull): returns 200 with last-known values
     * including a populated deletedAt for a soft-deleted row;
     * ResourceNotFoundException only when the id never existed at all —
     * identical reasoning to LocationService.getById.
     */
    public ResourceResponse getById(UUID id) {
        Resource resource = resourceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Resource not found: " + id
            ));
        return toResponse(resource);
    }

    /**
     * Creates a new Resource. Validates name non-empty; writes outbox
     * resource.created.
     */
    @Transactional
    public ResourceResponse create(ResourceUpsertRequest request) {
        validateName(request.name());

        boolean isUnique = request.isUnique() != null && request.isUnique();

        Resource resource = new Resource(
            request.name(),
            request.type(),
            request.description(),
            isUnique,
            request.restrictLocations()
        );
        Resource saved = resourceRepository.save(resource);

        writeOutboxEvent(saved, "resource.created");

        return toResponse(saved);
    }

    /**
     * Updates an existing Resource. findByIdAndDeletedAtIsNull else
     * ResourceNotFoundException if missing/deleted — update stays
     * active-only. Writes outbox resource.updated.
     */
    @Transactional
    public ResourceResponse update(UUID id, ResourceUpsertRequest request) {
        Resource resource = resourceRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Resource not found or already deleted: " + id
            ));

        validateName(request.name());

        boolean isUnique = request.isUnique() != null && request.isUnique();

        resource.setName(request.name());
        resource.setType(request.type());
        resource.setDescription(request.description());
        resource.setUnique(isUnique);
        resource.setRestrictLocations(request.restrictLocations());
        resource.setUpdatedAt(Instant.now());

        Resource saved = resourceRepository.save(resource);

        writeOutboxEvent(saved, "resource.updated");

        return toResponse(saved);
    }

    /**
     * Deletes a Resource — same soft-delete-always-succeeds policy as
     * Location: findByIdAndDeletedAtIsNull else ResourceNotFoundException for
     * the purpose of deleting, sets deletedAt, outbox resource.deleted, no
     * referencing-booking check, no 409 path. Same F0 open-questions.md #14
     * decision, extended identically to Resource.
     */
    @Transactional
    public void delete(UUID id) {
        Resource resource = resourceRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Resource not found or already deleted: " + id
            ));

        Instant now = Instant.now();
        resource.setDeletedAt(now);
        resource.setUpdatedAt(now);
        Resource saved = resourceRepository.save(resource);

        writeOutboxEvent(saved, "resource.deleted");
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ResourceNameRequiredException("Resource name is required");
        }
    }

    private void writeOutboxEvent(Resource resource, String routingKey) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", resource.getId());
        payload.put("name", resource.getName());
        payload.put("type", resource.getType());
        payload.put("description", resource.getDescription());
        payload.put("is_unique", resource.isUnique());
        payload.put("restrict_locations", resource.getRestrictLocations());
        payload.put("deleted_at", resource.getDeletedAt());

        OutboxEvent event = new OutboxEvent(
            "resource",
            resource.getId(),
            "resource.events",
            routingKey,
            serializePayload(payload)
        );
        outboxEventRepository.save(event);
    }

    private String serializePayload(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize outbox payload", e);
        }
    }

    private ResourceResponse toResponse(Resource resource) {
        return new ResourceResponse(
            resource.getId(),
            resource.getName(),
            resource.getType(),
            resource.getDescription(),
            resource.isUnique(),
            resource.getRestrictLocations(),
            resource.getCreatedAt(),
            resource.getUpdatedAt(),
            resource.getDeletedAt()
        );
    }
}
