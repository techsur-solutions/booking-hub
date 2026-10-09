package com.bookinghub.settings.controller;

import com.bookinghub.settings.dto.SettingsDtos.SettingsResponse;
import com.bookinghub.settings.dto.SettingsDtos.SettingsUpdateRequest;
import com.bookinghub.settings.service.SettingsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Settings controller implementing F10's two endpoints (TechArch §4.9's
 * exact shapes):
 * - GET /settings: broadly readable, any authenticated caller, any role.
 * - PUT /settings: admin-only (role_settings_admin), partial update of the
 *   singleton row.
 *
 * No create/delete endpoints exist for Settings at all — matching the FRD's
 * explicit statement that this is a fixed singleton with only read/update
 * operations.
 */
@RestController
@RequestMapping("/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    /**
     * GET /settings
     *
     * No @PreAuthorize beyond the class-level authenticated() baseline
     * already enforced by SecurityConfig — any authenticated caller, any
     * role, may read. Reads the database directly on every call (no
     * in-memory cache), so a prior PUT is visible immediately — see
     * SettingsService's named decision on immediate propagation.
     */
    @GetMapping
    public SettingsResponse getSettings() {
        return settingsService.getCurrent();
    }

    /**
     * PUT /settings
     *
     * Requires role_settings_admin. Every field in the request body is
     * optional — a caller may update any subset of the singleton's fields.
     * Server-side validation (calendar range, slot size) happens in
     * SettingsService, in addition to the DB's CHECK constraints.
     */
    @PutMapping
    @PreAuthorize("hasRole('role_settings_admin')")
    public SettingsResponse updateSettings(@RequestBody SettingsUpdateRequest request) {
        return settingsService.update(request);
    }
}
