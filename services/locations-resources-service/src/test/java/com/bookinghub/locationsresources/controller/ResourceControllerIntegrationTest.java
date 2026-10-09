package com.bookinghub.locationsresources.controller;

import com.bookinghub.locationsresources.domain.Resource;
import com.bookinghub.locationsresources.repository.OutboxEventRepository;
import com.bookinghub.locationsresources.repository.ResourceRepository;
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
 * Integration test for ResourceController (F4.2 + F4.3).
 *
 * Same shape as LocationControllerIntegrationTest — 5 scenarios, including
 * the corrected scenario 5 proving a soft-deleted Resource remains readable
 * by direct id lookup with last-known values (never 404s). Additionally
 * asserts is_unique/restrict_locations round-trip correctly through
 * create/read, since Phase 5's booking-service conflict detection depends on
 * reading those two fields verbatim.
 *
 * Runs against the real docker-compose Postgres (application-test.properties),
 * same Testcontainers-Docker-API-incompatibility workaround as
 * LocationControllerIntegrationTest.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ResourceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
        resourceRepository.deleteAll();
    }

    /**
     * Builds a jwt() post-processor carrying a ROLE_-prefixed authority
     * matching what SecurityConfig's realmRoleJwtAuthenticationConverter
     * derives from a real realm_access.roles claim in production — same
     * rationale as LocationControllerIntegrationTest's identical helper.
     */
    private static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor withRole(String role) {
        return jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role));
    }

    /**
     * Scenario 1: Admin caller creates a Resource -> 201, all fields echoed
     * back correctly, including is_unique/restrict_locations round-tripping.
     */
    @Test
    void adminCreatesResource_returns201WithAllFieldsEchoed() throws Exception {
        UUID restrictedLocationId = UUID.randomUUID();
        String requestBody = """
                {
                    "name": "HD Projector",
                    "type": "Audio Visual",
                    "description": "HDMI/VGA capable projector",
                    "is_unique": true,
                    "restrict_locations": ["%s"]
                }
                """.formatted(restrictedLocationId);

        mockMvc.perform(post("/resources")
                        .with(withRole("role_location_admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("HD Projector"))
                .andExpect(jsonPath("$.type").value("Audio Visual"))
                .andExpect(jsonPath("$.description").value("HDMI/VGA capable projector"))
                .andExpect(jsonPath("$.is_unique").value(true))
                .andExpect(jsonPath("$.restrict_locations[0]").value(restrictedLocationId.toString()))
                .andExpect(jsonPath("$.deleted_at").doesNotExist());
    }

    /**
     * Scenario 2: Admin caller creates a Resource with no name -> 400
     * RESOURCE_NAME_REQUIRED.
     */
    @Test
    void adminCreatesResourceWithNoName_returns400ResourceNameRequired() throws Exception {
        mockMvc.perform(post("/resources")
                        .with(withRole("role_location_admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value("RESOURCE_NAME_REQUIRED"));
    }

    /**
     * Scenario 3: Caller with only role_calendar_viewer (no admin role)
     * attempting a write -> 403 LOCATION_RESOURCE_FORBIDDEN (Resource shares
     * the same error code as Location — both live under the same
     * role_location_admin gate per the permission-seed named decision).
     */
    @Test
    void nonAdminCreatesResource_returns403LocationResourceForbidden() throws Exception {
        mockMvc.perform(post("/resources")
                        .with(withRole("role_calendar_viewer"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Resource\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error_code").value("LOCATION_RESOURCE_FORBIDDEN"));
    }

    /**
     * Scenario 4: Caller with role_calendar_viewer lists Resources -> 200,
     * includes the created resource.
     */
    @Test
    void calendarViewerListsResources_includesCreatedResource() throws Exception {
        Resource resource = new Resource(
                "Whiteboard", "Furniture", "Mobile whiteboard", false, null
        );
        resourceRepository.save(resource);

        mockMvc.perform(get("/resources")
                        .with(withRole("role_calendar_viewer")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name=='Whiteboard')]").exists());
    }

    /**
     * Scenario 5: Admin deletes a Resource -> 204; list excludes it
     * (active-only); direct GET by id -> 200 with deletedAt populated and
     * last-known field values (Success Criterion 2 proof — NOT 404); GET on
     * a genuinely nonexistent random id -> 404 RESOURCE_NOT_FOUND.
     */
    @Test
    void adminDeletesResource_softDeletesAndRemainsReadableWithLastKnownValues() throws Exception {
        UUID restrictedLocationId = UUID.randomUUID();
        Resource resource = new Resource(
                "Deletable Projector", "Audio Visual", "will be deleted",
                true, List.of(restrictedLocationId)
        );
        Resource saved = resourceRepository.save(resource);
        UUID resourceId = saved.getId();

        // Delete -> 204
        mockMvc.perform(delete("/resources/" + resourceId)
                        .with(withRole("role_location_admin")))
                .andExpect(status().isNoContent());

        // List -> active-only, deleted resource ABSENT
        mockMvc.perform(get("/resources")
                        .with(withRole("role_calendar_viewer")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='" + resourceId + "')]").doesNotExist());

        // Direct GET by id -> 200 with last-known values, deletedAt populated
        // (Success Criterion 2 proof: soft-deleted rows remain readable)
        mockMvc.perform(get("/resources/" + resourceId)
                        .with(withRole("role_calendar_viewer")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(resourceId.toString()))
                .andExpect(jsonPath("$.name").value("Deletable Projector"))
                .andExpect(jsonPath("$.type").value("Audio Visual"))
                .andExpect(jsonPath("$.description").value("will be deleted"))
                .andExpect(jsonPath("$.is_unique").value(true))
                .andExpect(jsonPath("$.restrict_locations[0]").value(restrictedLocationId.toString()))
                .andExpect(jsonPath("$.deleted_at").exists())
                .andExpect(jsonPath("$.deleted_at").isNotEmpty());

        // GET on a genuinely nonexistent random id -> 404, proving 404 is
        // reserved for never-existed ids, not reused for soft-deleted ones.
        UUID neverExistedId = UUID.randomUUID();
        mockMvc.perform(get("/resources/" + neverExistedId)
                        .with(withRole("role_calendar_viewer")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_code").value("RESOURCE_NOT_FOUND"));

        // Also assert the row was NOT physically removed (soft-delete, not
        // hard-delete) — directly via repository.
        assertThat(resourceRepository.findById(resourceId)).isPresent();
    }
}
