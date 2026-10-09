package com.bookinghub.customfield.controller;

import com.bookinghub.customfield.dto.CustomFieldDtos.CustomFieldResponse;
import com.bookinghub.customfield.dto.CustomFieldDtos.CustomFieldUpsertRequest;
import com.bookinghub.customfield.service.CustomFieldService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * CustomField controller implementing F5.1's definition CRUD plus the F5.3
 * applicability-query endpoint.
 *
 * EVERY method requires role_customfield_admin (no read/write split, per plan
 * 04-03's named decision and TechArch §4.4's uniform "admin (custom field mgmt)"
 * permission column) — proven to exactly match the Gateway's Tier-1 route table
 * by Tier1Tier2ConsistencyTest.
 */
@RestController
@RequestMapping("/custom-fields")
public class CustomFieldController {

    private final CustomFieldService customFieldService;

    public CustomFieldController(CustomFieldService customFieldService) {
        this.customFieldService = customFieldService;
    }

    /**
     * GET /custom-fields?context_id={id}
     *
     * Without context_id: returns all non-deleted custom fields (existing
     * admin-CRUD-UI behavior). With context_id: returns exactly the custom
     * fields applicable to that booking context (global templates plus the
     * context-specific template) — the F5.3/TechArch §2.4 applicability-query
     * mechanism Phase 5's booking-service will call.
     */
    @GetMapping
    @PreAuthorize("hasRole('role_customfield_admin')")
    public List<CustomFieldResponse> list(@RequestParam(name = "context_id", required = false) UUID contextId) {
        return customFieldService.listApplicableToContext(contextId);
    }

    @PostMapping
    @PreAuthorize("hasRole('role_customfield_admin')")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomFieldResponse create(@Valid @RequestBody CustomFieldUpsertRequest request) {
        return customFieldService.create(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('role_customfield_admin')")
    public CustomFieldResponse get(@PathVariable UUID id) {
        return customFieldService.get(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('role_customfield_admin')")
    public CustomFieldResponse update(@PathVariable UUID id, @Valid @RequestBody CustomFieldUpsertRequest request) {
        return customFieldService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('role_customfield_admin')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        customFieldService.delete(id);
    }
}
