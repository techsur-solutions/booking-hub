package com.bookinghub.customfield.controller;

import com.bookinghub.customfield.dto.FieldTemplateDtos.FieldTemplateResponse;
import com.bookinghub.customfield.dto.FieldTemplateDtos.FieldTemplateUpsertRequest;
import com.bookinghub.customfield.service.FieldTemplateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * FieldTemplate controller implementing F5.2's template CRUD with
 * Custom-Field-join management.
 *
 * EVERY method requires role_customfield_admin (no read/write split, per plan
 * 04-03's named decision and TechArch §4.4's uniform "admin (custom field mgmt)"
 * permission column) — proven to exactly match the Gateway's Tier-1 route table
 * by Tier1Tier2ConsistencyTest.
 */
@RestController
@RequestMapping("/field-templates")
public class FieldTemplateController {

    private final FieldTemplateService fieldTemplateService;

    public FieldTemplateController(FieldTemplateService fieldTemplateService) {
        this.fieldTemplateService = fieldTemplateService;
    }

    @GetMapping
    @PreAuthorize("hasRole('role_customfield_admin')")
    public List<FieldTemplateResponse> list() {
        return fieldTemplateService.list();
    }

    @PostMapping
    @PreAuthorize("hasRole('role_customfield_admin')")
    @ResponseStatus(HttpStatus.CREATED)
    public FieldTemplateResponse create(@Valid @RequestBody FieldTemplateUpsertRequest request) {
        return fieldTemplateService.create(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('role_customfield_admin')")
    public FieldTemplateResponse get(@PathVariable UUID id) {
        return fieldTemplateService.get(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('role_customfield_admin')")
    public FieldTemplateResponse update(@PathVariable UUID id, @Valid @RequestBody FieldTemplateUpsertRequest request) {
        return fieldTemplateService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('role_customfield_admin')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        fieldTemplateService.delete(id);
    }
}
