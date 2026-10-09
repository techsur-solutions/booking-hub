package com.bookinghub.locationsresources.controller;

import com.bookinghub.locationsresources.domain.Location;
import com.bookinghub.locationsresources.repository.LocationRepository;
import com.bookinghub.locationsresources.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for LocationController (F4.1 + F4.3).
 *
 * Runs against the real docker-compose Postgres (via application-test.properties)
 * instead of Testcontainers — this sandbox's Docker daemon API version is
 * incompatible with Testcontainers' bundled client, same workaround already
 * established by SchemaCompletionTest/Tier2FailClosedTest (plan 04-01).
 *
 * Verifies:
 * 1. Admin create -> 201, fields echoed back correctly
 * 2. Admin create with no name -> 400 LOCATION_NAME_REQUIRED
 * 3. Non-admin (calendar-viewer-only) create -> 403 LOCATION_RESOURCE_FORBIDDEN
 * 4. Calendar-viewer list -> 200, includes the created location
 * 5. Admin delete -> 204; list excludes it (active-only); direct GET by id ->
 *    200 with deletedAt populated and last-known field values (Success
 *    Criterion 2 proof) — NOT 404; GET on a genuinely nonexistent random id ->
 *    404 LOCATION_NOT_FOUND (proving 404 is reserved for never-existed ids).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class LocationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
        locationRepository.deleteAll();
    }

    /**
     * Builds a jwt() post-processor carrying a ROLE_-prefixed authority
     * matching what SecurityConfig's realmRoleJwtAuthenticationConverter
     * derives from a real realm_access.roles claim in production.
     *
     * Spring Security Test's jwt() post-processor sets authorities directly on
     * the mock JwtAuthenticationToken via .authorities() — it does NOT invoke
     * the application's configured JwtAuthenticationConverter bean against a
     * .jwt(...) claim (that conversion only happens in the real OAuth2
     * resource-server filter chain, which this post-processor replaces for
     * the test). So .authorities(new SimpleGrantedAuthority("ROLE_" + role))
     * is the correct way to simulate "a caller whose JWT, once converted,
     * carries this role" — matching the same pattern api-gateway's
     * GatewaySecurityTest uses for its own SCOPE_-prefixed authorities.
     */
    private static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor withRole(String role) {
        return jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role));
    }

    /**
     * Scenario 1: Admin caller creates a Location -> 201, all fields echoed
     * back correctly.
     */
    @Test
    void adminCreatesLocation_returns201WithAllFieldsEchoed() throws Exception {
        String requestBody = """
                {
                    "name": "Main Boardroom",
                    "css_class": "room-blue",
                    "colour": "#FF0000",
                    "description": "Top floor boardroom",
                    "building": "HQ Building A",
                    "layout": ["boardroom", "lecture"]
                }
                """;

        mockMvc.perform(post("/locations")
                        .with(withRole("role_location_admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Main Boardroom"))
                .andExpect(jsonPath("$.css_class").value("room-blue"))
                .andExpect(jsonPath("$.colour").value("#FF0000"))
                .andExpect(jsonPath("$.description").value("Top floor boardroom"))
                .andExpect(jsonPath("$.building").value("HQ Building A"))
                .andExpect(jsonPath("$.layout[0]").value("boardroom"))
                .andExpect(jsonPath("$.layout[1]").value("lecture"))
                .andExpect(jsonPath("$.deleted_at").doesNotExist());
    }

    /**
     * Scenario 2: Admin caller creates a Location with no name -> 400
     * LOCATION_NAME_REQUIRED.
     */
    @Test
    void adminCreatesLocationWithNoName_returns400LocationNameRequired() throws Exception {
        mockMvc.perform(post("/locations")
                        .with(withRole("role_location_admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value("LOCATION_NAME_REQUIRED"));
    }

    /**
     * Scenario 3: Caller with only role_calendar_viewer (no admin role)
     * attempting a write -> 403 LOCATION_RESOURCE_FORBIDDEN.
     */
    @Test
    void nonAdminCreatesLocation_returns403LocationResourceForbidden() throws Exception {
        mockMvc.perform(post("/locations")
                        .with(withRole("role_calendar_viewer"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Room\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error_code").value("LOCATION_RESOURCE_FORBIDDEN"));
    }

    /**
     * Scenario 4: Caller with role_calendar_viewer lists Locations -> 200,
     * includes the created location.
     */
    @Test
    void calendarViewerListsLocations_includesCreatedLocation() throws Exception {
        Location location = new Location(
                "Seminar Room A", "room-green", "#00FF00", "desc",
                "HQ Building B", List.of("seminar")
        );
        locationRepository.save(location);

        mockMvc.perform(get("/locations")
                        .with(withRole("role_calendar_viewer")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name=='Seminar Room A')]").exists());
    }

    /**
     * Scenario 5: Admin deletes a Location -> 204; list excludes it
     * (active-only); direct GET by id -> 200 with deletedAt populated and
     * last-known field values (Success Criterion 2 proof — NOT 404);
     * GET on a genuinely nonexistent random id -> 404 LOCATION_NOT_FOUND.
     */
    @Test
    void adminDeletesLocation_softDeletesAndRemainsReadableWithLastKnownValues() throws Exception {
        Location location = new Location(
                "Deletable Room", "room-red", "#AA0000", "will be deleted",
                "HQ Building C", List.of("meeting")
        );
        Location saved = locationRepository.save(location);
        UUID locationId = saved.getId();

        // Delete -> 204
        mockMvc.perform(delete("/locations/" + locationId)
                        .with(withRole("role_location_admin")))
                .andExpect(status().isNoContent());

        // List -> active-only, deleted location ABSENT
        mockMvc.perform(get("/locations")
                        .with(withRole("role_calendar_viewer")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='" + locationId + "')]").doesNotExist());

        // Direct GET by id -> 200 with last-known values, deletedAt populated
        // (Success Criterion 2 proof: soft-deleted rows remain readable)
        mockMvc.perform(get("/locations/" + locationId)
                        .with(withRole("role_calendar_viewer")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(locationId.toString()))
                .andExpect(jsonPath("$.name").value("Deletable Room"))
                .andExpect(jsonPath("$.css_class").value("room-red"))
                .andExpect(jsonPath("$.colour").value("#AA0000"))
                .andExpect(jsonPath("$.description").value("will be deleted"))
                .andExpect(jsonPath("$.building").value("HQ Building C"))
                .andExpect(jsonPath("$.deleted_at").exists())
                .andExpect(jsonPath("$.deleted_at").isNotEmpty());

        // GET on a genuinely nonexistent random id -> 404, proving 404 is
        // reserved for never-existed ids, not reused for soft-deleted ones.
        UUID neverExistedId = UUID.randomUUID();
        mockMvc.perform(get("/locations/" + neverExistedId)
                        .with(withRole("role_calendar_viewer")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_code").value("LOCATION_NOT_FOUND"));

        // Also assert the row was NOT physically removed (soft-delete, not
        // hard-delete) — directly via repository.
        assertThat(locationRepository.findById(locationId)).isPresent();
    }
}
