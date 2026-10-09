package com.bookinghub.locationsresources.service;

import com.bookinghub.locationsresources.domain.Location;
import com.bookinghub.locationsresources.domain.OutboxEvent;
import com.bookinghub.locationsresources.dto.LocationDtos.LocationResponse;
import com.bookinghub.locationsresources.dto.LocationDtos.LocationUpsertRequest;
import com.bookinghub.locationsresources.error.LocationNameRequiredException;
import com.bookinghub.locationsresources.error.LocationNotFoundException;
import com.bookinghub.locationsresources.repository.LocationRepository;
import com.bookinghub.locationsresources.repository.OutboxEventRepository;
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
 * Service for Location CRUD (F4.1), implementing the soft-delete-always-succeeds
 * policy (F4.3, closing open-questions.md #14 as a deliberate improvement over
 * legacy's confirmed orphan-allow-via-hard-delete behavior).
 */
@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public LocationService(LocationRepository locationRepository,
                            OutboxEventRepository outboxEventRepository,
                            ObjectMapper objectMapper) {
        this.locationRepository = locationRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Lists all ACTIVE (non-soft-deleted) Locations. Backs GET /locations.
     * Admins browsing the Locations list should not see deleted entries by default.
     */
    public List<LocationResponse> listActive() {
        return locationRepository.findAllByDeletedAtIsNull().stream()
            .map(this::toResponse)
            .toList();
    }

    /**
     * Reads a single Location by id — the Success Criterion 2 "last-known
     * values" lookup path. NOT scoped to active rows: uses the plain
     * JpaRepository#findById (not findByIdAndDeletedAtIsNull), so a soft-deleted
     * row's response simply has deletedAt populated rather than 404-ing.
     *
     * Named decision, closing the Success-Criterion-2 gap: this is the one read
     * path in this service that must remain reachable after soft-delete —
     * Phase 5's booking-service needs to resolve a booking's location_id to a
     * last-known name/colour/building even after the Location is deleted, and
     * this single-by-id GET is the only mechanism it has (TechArch's bookings
     * table stores location_id by reference only, no denormalized copy) — so
     * 404-ing a soft-deleted id here would make the Success Criterion's
     * promised capability architecturally unreachable.
     *
     * LocationNotFoundException is thrown ONLY when findById returns empty
     * (the id never existed at all).
     */
    public LocationResponse getById(UUID id) {
        Location location = locationRepository.findById(id)
            .orElseThrow(() -> new LocationNotFoundException(
                "Location not found: " + id
            ));
        return toResponse(location);
    }

    /**
     * Creates a new Location. Validates name non-empty, persists, and writes
     * an OutboxEvent (location.created) in the SAME transaction.
     */
    @Transactional
    public LocationResponse create(LocationUpsertRequest request) {
        validateName(request.name());

        Location location = new Location(
            request.name(),
            request.cssClass(),
            request.colour(),
            request.description(),
            request.building(),
            request.layout()
        );
        Location saved = locationRepository.save(location);

        writeOutboxEvent(saved, "location.created");

        return toResponse(saved);
    }

    /**
     * Updates an existing Location. Update stays active-only: an
     * already-deleted row cannot be edited — findByIdAndDeletedAtIsNull else
     * LocationNotFoundException (unlike getById's separate active-or-deleted
     * read behavior above). Re-validates name; writes outbox location.updated.
     */
    @Transactional
    public LocationResponse update(UUID id, LocationUpsertRequest request) {
        Location location = locationRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new LocationNotFoundException(
                "Location not found or already deleted: " + id
            ));

        validateName(request.name());

        location.setName(request.name());
        location.setCssClass(request.cssClass());
        location.setColour(request.colour());
        location.setDescription(request.description());
        location.setBuilding(request.building());
        location.setLayout(request.layout());
        location.setUpdatedAt(Instant.now());

        Location saved = locationRepository.save(location);

        writeOutboxEvent(saved, "location.updated");

        return toResponse(saved);
    }

    /**
     * Deletes a Location — the F4.3/open-questions.md#14 policy implementation.
     *
     * findByIdAndDeletedAtIsNull(id) else LocationNotFoundException
     * (already-deleted or never-existed both read as not-found FOR THE PURPOSE
     * OF DELETING — delete stays active-only, you cannot delete what's already
     * deleted — this does NOT affect getById's separate active-or-deleted read
     * behavior above).
     *
     * Sets deletedAt = Instant.now() — NEVER issues a SQL DELETE, never checks
     * for referencing bookings, never blocks. Writes outbox location.deleted.
     *
     * Named decision: this is a deliberate improvement over legacy's confirmed
     * orphan-allow-via-hard-delete behavior (F0 open-questions.md #14) — legacy
     * performed a true hard DELETE with zero booking-reference guard; this
     * system instead marks the row deleted and retains it, so existing
     * bookings.location_id references (Phase 5) continue to resolve against a
     * real (if flagged-deleted) row rather than a dangling id, and remain fully
     * readable via getById above. There is no 409 LOCATION_IN_USE path —
     * deletion always succeeds.
     */
    @Transactional
    public void delete(UUID id) {
        Location location = locationRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new LocationNotFoundException(
                "Location not found or already deleted: " + id
            ));

        Instant now = Instant.now();
        location.setDeletedAt(now);
        location.setUpdatedAt(now);
        Location saved = locationRepository.save(location);

        writeOutboxEvent(saved, "location.deleted");
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new LocationNameRequiredException("Location name is required");
        }
    }

    private void writeOutboxEvent(Location location, String routingKey) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", location.getId());
        payload.put("name", location.getName());
        payload.put("css_class", location.getCssClass());
        payload.put("colour", location.getColour());
        payload.put("description", location.getDescription());
        payload.put("building", location.getBuilding());
        payload.put("layout", location.getLayout());
        payload.put("deleted_at", location.getDeletedAt());

        OutboxEvent event = new OutboxEvent(
            "location",
            location.getId(),
            "location.events",
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

    private LocationResponse toResponse(Location location) {
        return new LocationResponse(
            location.getId(),
            location.getName(),
            location.getCssClass(),
            location.getColour(),
            location.getDescription(),
            location.getBuilding(),
            location.getLayout(),
            location.getCreatedAt(),
            location.getUpdatedAt(),
            location.getDeletedAt()
        );
    }
}
