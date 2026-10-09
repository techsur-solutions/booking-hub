package com.bookinghub.locationsresources.controller;

import com.bookinghub.locationsresources.dto.ResourceDtos.ResourceResponse;
import com.bookinghub.locationsresources.dto.ResourceDtos.ResourceUpsertRequest;
import com.bookinghub.locationsresources.service.ResourceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Resource CRUD controller (F4.2) — extended with type/description/is_unique/
 * restrict_locations (F0-confirmed full field set).
 *
 * Named decision: per the Keycloak realm/Phase 3 permission seed, Resource
 * management (accessresources) is provisionally folded into
 * role_location_admin (seeded confirmed=false in Phase 3's V2 migration) —
 * this controller's implementation is the practical act of exercising that
 * mapping. A future product decision could flip the seed's confirmed flag to
 * true once explicitly confirmed; that data change is out of this phase's
 * file scope (owned by users-permissions-service) and is NOT made by this
 * plan.
 *
 * Scope boundary, named explicitly (closes open-questions.md #15 as
 * out-of-scope-for-this-phase, not silently dropped): F0 flagged legacy's
 * checkavailability Ajax endpoint (admin-gated resource-conflict check) as a
 * design-smell worth cross-referencing with Phase 5. This phase does NOT
 * implement any checkavailability-equivalent endpoint or conflict-detection
 * logic — that belongs entirely to Phase 5's booking-service conflict
 * detection, which will read this service's is_unique/restrict_locations
 * fields (both exposed by ResourceResponse) via the plain
 * GET /resources/GET /resources/{id} read endpoints already built here.
 * This plan's job is only to expose those two fields correctly; evaluating
 * them for conflicts is out of scope here.
 */
@RestController
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    /**
     * GET /resources — 200, active (non-soft-deleted) Resources only.
     */
    @GetMapping("/resources")
    @PreAuthorize("hasRole('role_calendar_viewer')")
    public ResponseEntity<List<ResourceResponse>> list() {
        return ResponseEntity.ok(resourceService.listActive());
    }

    /**
     * POST /resources — 201, creates a new Resource.
     */
    @PostMapping("/resources")
    @PreAuthorize("hasRole('role_location_admin')")
    public ResponseEntity<ResourceResponse> create(@RequestBody ResourceUpsertRequest request) {
        ResourceResponse response = resourceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /resources/{id} — 200 even when soft-deleted (last-known values,
     * deletedAt populated) — ResourceNotFoundException (404) only for an id
     * that never existed, matching Location's behavior exactly.
     */
    @GetMapping("/resources/{id}")
    @PreAuthorize("hasRole('role_calendar_viewer')")
    public ResponseEntity<ResourceResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(resourceService.getById(id));
    }

    /**
     * PUT /resources/{id} — 200, updates an active (non-soft-deleted) Resource.
     */
    @PutMapping("/resources/{id}")
    @PreAuthorize("hasRole('role_location_admin')")
    public ResponseEntity<ResourceResponse> update(
            @PathVariable UUID id,
            @RequestBody ResourceUpsertRequest request) {
        ResourceResponse response = resourceService.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /resources/{id} — 204. Soft-delete-always-succeeds policy,
     * identical to Location's.
     */
    @DeleteMapping("/resources/{id}")
    @PreAuthorize("hasRole('role_location_admin')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        resourceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
