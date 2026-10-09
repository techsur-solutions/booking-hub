package com.bookinghub.settings.controller;

import com.bookinghub.settings.domain.OutboxEvent;
import com.bookinghub.settings.domain.Settings;
import com.bookinghub.settings.repository.OutboxEventRepository;
import com.bookinghub.settings.repository.SettingsRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for SettingsController.
 *
 * Uses the running docker-compose Postgres (settings_db_test) instead of
 * Testcontainers: this sandbox's Docker daemon API version is incompatible
 * with Testcontainers' bundled client — same established workaround as
 * SettingsSingletonTest (plan 04-05) and every other service's equivalent
 * test this phase.
 *
 * Each test resets the singleton row back to its Phase-2-seeded defaults in
 * @BeforeEach so tests are independent of execution order (the singleton
 * row can never be deleted/recreated, only mutated in place).
 *
 * Covers all 6 scenarios from this plan's done-criteria:
 * 1. Any authenticated caller (no specific role) → GET /settings → 200 with seeded defaults
 * 2. role_settings_admin → PUT {approve_booking:false} → 200, then GET reflects it immediately (no-reload-required proof)
 * 3. role_settings_admin → PUT {min after max} → 400 SETTINGS_INVALID_CALENDAR_RANGE
 * 4. role_settings_admin → PUT {calendar_slot_size:0} → 400 SETTINGS_INVALID_SLOT_SIZE
 * 5. Caller WITHOUT role_settings_admin → PUT → 403 SETTINGS_FORBIDDEN
 * 6. After a successful PUT, an OutboxEvent row exists with routing_key="settings.updated"
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SettingsControllerIntegrationTest {

    private static final int SINGLETON_ID = 1;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SettingsRepository settingsRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    // This test mutates the real (non-rolled-back) singleton row in
    // settings_db_test via MockMvc-driven HTTP calls, not a @Transactional
    // test method — so changes persist across test methods AND across test
    // classes in the same Maven Surefire run (e.g. SettingsSingletonTest,
    // which asserts the Phase-2-seeded defaults). @BeforeEach resets state
    // before every method in THIS class; @AfterAll restores it once more
    // after the LAST method, so any class that runs after this one in the
    // same suite still finds the documented seeded defaults.
    @BeforeEach
    void resetSingletonToSeededDefaults() {
        outboxEventRepository.deleteAll();

        Settings settings = settingsRepository.findById(SINGLETON_ID).orElseThrow();
        settings.setApproveBooking(true);
        settings.setCalendarSlotSize(30);
        settings.setCalendarMinTime(LocalTime.of(8, 0));
        settings.setCalendarMaxTime(LocalTime.of(18, 0));
        settings.setUpdatedBy(null);
        settingsRepository.save(settings);
    }

    @AfterAll
    void restoreSeededDefaultsForLaterTestClasses() {
        resetSingletonToSeededDefaults();
    }

    /**
     * Scenario 1: any authenticated caller (no specific role) can read current
     * settings and sees the Phase-2-seeded defaults.
     */
    @Test
    void anyAuthenticatedCaller_getSettings_returns200WithSeededDefaults() throws Exception {
        mockMvc.perform(get("/settings")
                        .with(jwt().authorities(() -> "role_user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approve_booking").value(true))
                .andExpect(jsonPath("$.calendar_slot_size").value(30))
                .andExpect(jsonPath("$.calendar_min_time").value("08:00:00"))
                .andExpect(jsonPath("$.calendar_max_time").value("18:00:00"));
    }

    /**
     * Scenario 2: admin PUT takes effect immediately — the very next GET
     * reflects it, with zero propagation delay (no legacy reload-required
     * quirk). A single in-request round-trip, not a polling wait.
     */
    @Test
    void adminUpdatesApproveBooking_immediatelyVisibleOnNextGet() throws Exception {
        UUID callerId = UUID.randomUUID();

        mockMvc.perform(put("/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approve_booking\": false}")
                        .with(jwt()
                                .authorities(() -> "ROLE_role_settings_admin")
                                .jwt(jwt -> jwt.subject(callerId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approve_booking").value(false));

        // Immediately GET — same caller-agnostic read, proving zero-delay propagation
        mockMvc.perform(get("/settings")
                        .with(jwt().authorities(() -> "role_user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approve_booking").value(false));
    }

    /**
     * Scenario 3: calendar_min_time after calendar_max_time is rejected
     * server-side (defense in depth beyond the DB CHECK constraint).
     */
    @Test
    void adminSetsInvalidCalendarRange_returns400() throws Exception {
        mockMvc.perform(put("/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"calendar_min_time\": \"19:00:00\", \"calendar_max_time\": \"08:00:00\"}")
                        .with(jwt().authorities(() -> "ROLE_role_settings_admin")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value("SETTINGS_INVALID_CALENDAR_RANGE"));
    }

    /**
     * Scenario 4: calendar_slot_size of zero is rejected server-side.
     */
    @Test
    void adminSetsZeroSlotSize_returns400() throws Exception {
        mockMvc.perform(put("/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"calendar_slot_size\": 0}")
                        .with(jwt().authorities(() -> "ROLE_role_settings_admin")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value("SETTINGS_INVALID_SLOT_SIZE"));
    }

    /**
     * Scenario 5: a caller without role_settings_admin cannot write, even
     * though they CAN read (GET requires no specific role here).
     */
    @Test
    void nonAdminCaller_putSettings_returns403() throws Exception {
        mockMvc.perform(put("/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approve_booking\": false}")
                        .with(jwt().authorities(() -> "role_user")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error_code").value("SETTINGS_FORBIDDEN"));
    }

    /**
     * Scenario 6: a successful PUT writes an OutboxEvent in the same
     * transaction, with routing_key="settings.updated" and updated_by
     * matching the caller's JWT sub claim.
     */
    @Test
    void successfulUpdate_writesOutboxEventWithCallerAsUpdatedBy() throws Exception {
        UUID callerId = UUID.randomUUID();

        mockMvc.perform(put("/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approve_booking\": false}")
                        .with(jwt()
                                .authorities(() -> "ROLE_role_settings_admin")
                                .jwt(jwt -> jwt.subject(callerId.toString()))))
                .andExpect(status().isOk());

        List<OutboxEvent> events = outboxEventRepository.findAll();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getRoutingKey()).isEqualTo("settings.updated");
        assertThat(events.get(0).getExchange()).isEqualTo("settings.events");
        assertThat(events.get(0).getAggregateType()).isEqualTo("settings");
        assertThat(events.get(0).getPayload()).contains(callerId.toString());

        // Also assert updated_by on the Settings row itself matches the caller
        Settings settings = settingsRepository.findById(SINGLETON_ID).orElseThrow();
        assertThat(settings.getUpdatedBy()).isEqualTo(callerId);
    }
}
