package com.bookinghub.locationsresources.controller;

import com.bookinghub.locationsresources.dto.LocationDtos.LocationResponse;
import com.bookinghub.locationsresources.dto.LocationDtos.LocationUpsertRequest;
import com.bookinghub.locationsresources.service.LocationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Location CRUD controller (F4.1) — exact TechArch §4.3 shapes extended with
 * colour/description (this phase's F0-driven additions).
 *
 * Role gating (Tier 2, re-validated independently of the Gateway's Tier 1):
 * - Reads (GET): role_calendar_viewer — deliberately STRICTER than Tier 1's
 *   "any authenticated", per SecurityConfig's named decision (plan 04-01),
 *   proven never-weaker by Tier1Tier2ConsistencyTest.
 * - Writes (POST/PUT/DELETE): role_location_admin — matches Tier 1 exactly.
 */
@RestController
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * GET /locations — 200, active (non-soft-deleted) Locations only.
     */
    @GetMapping("/locations")
    @PreAuthorize("hasRole('role_calendar_viewer')")
    public ResponseEntity<List<LocationResponse>> list() {
        return ResponseEntity.ok(locationService.listActive());
    }

    /**
     * POST /locations — 201, creates a new Location.
     */
    @PostMapping("/locations")
    @PreAuthorize("hasRole('role_location_admin')")
    public ResponseEntity<LocationResponse> create(@RequestBody LocationUpsertRequest request) {
        LocationResponse response = locationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /locations/{id} — 200 even when the row is soft-deleted (its
     * deletedAt field will simply be populated in the response body) —
     * LocationNotFoundException (404) is thrown ONLY when the id never
     * existed at all, never for a soft-deleted-but-real id. This is the
     * Success Criterion 2 cross-service "last-known values" lookup path.
     */
    @GetMapping("/locations/{id}")
    @PreAuthorize("hasRole('role_calendar_viewer')")
    public ResponseEntity<LocationResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(locationService.getById(id));
    }

    /**
     * PUT /locations/{id} — 200, updates an active (non-soft-deleted) Location.
     */
    @PutMapping("/locations/{id}")
    @PreAuthorize("hasRole('role_location_admin')")
    public ResponseEntity<LocationResponse> update(
            @PathVariable UUID id,
            @RequestBody LocationUpsertRequest request) {
        LocationResponse response = locationService.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /locations/{id} — 204. Soft-delete-always-succeeds policy
     * (F4.3): sets deleted_at, never issues a SQL DELETE, never blocks on
     * referencing bookings.
     */
    @DeleteMapping("/locations/{id}")
    @PreAuthorize("hasRole('role_location_admin')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        locationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
