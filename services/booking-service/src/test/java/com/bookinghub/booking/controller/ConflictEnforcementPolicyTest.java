package com.bookinghub.booking.controller;

import com.bookinghub.booking.client.CustomFieldClient;
import com.bookinghub.booking.client.LocationsResourcesClient;
import com.bookinghub.booking.client.SettingsClient;
import com.bookinghub.booking.client.dto.ClientDtos.LocationResponse;
import com.bookinghub.booking.domain.Booking;
import com.bookinghub.booking.repository.BookingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Dedicated test pinning down the plan 05-03 conflict-enforcement-policy decision
 * unambiguously (plan 05-03, Task 3).
 *
 * THE DECISION (ROADMAP Success Criterion 4, implementing permission-conditional split):
 * - Non-approver + conflict → 409 BOOKING_CONFLICT (hard block)
 * - Approver + conflict → 200/201 success with conflict_flags populated (soft warning)
 * - Applied identically for BOTH create (POST) and edit (PUT), through the ONE shared
 *   applyConflictPolicy() call site in BookingWriteService
 *
 * IMPORTANT: JWT setup — CurrentUserProvider reads realm_access.roles from JWT claims.
 * Both jwt().authorities() AND jwt().jwt(j -> j.claim("realm_access", ...)) must be set.
 *
 * 4 scenarios per the plan:
 * 1. Existing booking at L 10:00-11:00; non-approver overlaps 10:30-11:30 → 409 BOOKING_CONFLICT
 * 2. Same overlap, caller has role_booking_approver → 201 with conflict_flags populated
 * 3. PUT that moves an existing booking into conflict; non-approver → 409
 * 4. Same PUT, caller has role_booking_approver → 200 with conflict_flags populated
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ConflictEnforcementPolicyTest {

    private static WireMockServer wireMock;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @MockBean
    private LocationsResourcesClient locationsResourcesClient;

    @MockBean
    private CustomFieldClient customFieldClient;

    @MockBean
    private SettingsClient settingsClient;

    private static final UUID CREATOR_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID APPROVER_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @BeforeAll
    static void startWireMock() {
        wireMock = new WireMockServer(0);
        wireMock.start();
        WireMock.configureFor("localhost", wireMock.port());

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/realms/bookinghub/.well-known/openid-configuration"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "issuer": "http://localhost:%d/realms/bookinghub",
                                    "jwks_uri": "http://localhost:%d/realms/bookinghub/protocol/openid-connect/certs"
                                }
                                """.formatted(wireMock.port(), wireMock.port()))));

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/realms/bookinghub/protocol/openid-connect/certs"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"keys\":[]}")));
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> "http://localhost:" + wireMock.port() + "/realms/bookinghub");
    }

    @BeforeEach
    void setUpDefaultMocks() {
        when(locationsResourcesClient.getResource(any(UUID.class))).thenReturn(Optional.empty());
        when(customFieldClient.getApplicableFields(any(UUID.class))).thenReturn(List.of());
        when(settingsClient.getApproveBookingFlag()).thenReturn(false);
    }

    /** Creator-only JWT with realm_access.roles claim set */
    private static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor
            creatorJwt(UUID userId) {
        return jwt()
                .authorities(new SimpleGrantedAuthority("ROLE_role_booking_creator"))
                .jwt(j -> j
                        .claim("sub", userId.toString())
                        .claim("realm_access", Map.of("roles", List.of("role_booking_creator"))));
    }

    /** Approver JWT with realm_access.roles claim set for both creator and approver */
    private static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor
            approverJwt(UUID userId) {
        return jwt()
                .authorities(
                        new SimpleGrantedAuthority("ROLE_role_booking_creator"),
                        new SimpleGrantedAuthority("ROLE_role_booking_approver"))
                .jwt(j -> j
                        .claim("sub", userId.toString())
                        .claim("realm_access", Map.of("roles",
                                List.of("role_booking_creator", "role_booking_approver"))));
    }

    // ── Scenario 1: Create — non-approver + conflict → 409 BOOKING_CONFLICT ──

    @Test
    void scenario1_createWithConflict_nonApprover_returns409() throws Exception {
        UUID locationId = UUID.randomUUID();

        when(locationsResourcesClient.getLocation(locationId))
                .thenReturn(Optional.of(new LocationResponse(locationId, "Room L", null)));

        // Seed an existing APPROVED booking
        Booking existing = new Booking();
        existing.setTitle("Existing Booking");
        existing.setLocationId(locationId);
        existing.setStartTime(Instant.parse("2035-08-01T10:00:00Z"));
        existing.setEndTime(Instant.parse("2035-08-01T11:00:00Z"));
        existing.setOwnerId(UUID.randomUUID());
        existing.setStatus("approved");
        bookingRepository.save(existing);
        bookingRepository.flush();

        // Non-approver overlaps (10:30-11:30) — HARD BLOCK
        String body = """
                {
                    "title": "Conflicting Booking",
                    "location_id": "%s",
                    "start_time": "2035-08-01T10:30:00Z",
                    "end_time": "2035-08-01T11:30:00Z"
                }
                """.formatted(locationId);

        mockMvc.perform(post("/bookings")
                        .with(creatorJwt(CREATOR_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error_code").value("BOOKING_CONFLICT"));
    }

    // ── Scenario 2: Create — approver + conflict → 201 with conflict_flags ──

    @Test
    void scenario2_createWithConflict_approver_returns201WithConflictFlags() throws Exception {
        UUID locationId = UUID.randomUUID();

        when(locationsResourcesClient.getLocation(locationId))
                .thenReturn(Optional.of(new LocationResponse(locationId, "Room L", null)));

        // Seed an existing APPROVED booking
        Booking existing = new Booking();
        existing.setTitle("Existing Booking");
        existing.setLocationId(locationId);
        existing.setStartTime(Instant.parse("2035-08-02T10:00:00Z"));
        existing.setEndTime(Instant.parse("2035-08-02T11:00:00Z"));
        existing.setOwnerId(UUID.randomUUID());
        existing.setStatus("approved");
        Booking savedExisting = bookingRepository.save(existing);
        bookingRepository.flush();

        // Approver with same overlapping window — SOFT WARNING, proceeds
        String body = """
                {
                    "title": "Override Booking",
                    "location_id": "%s",
                    "start_time": "2035-08-02T10:30:00Z",
                    "end_time": "2035-08-02T11:30:00Z"
                }
                """.formatted(locationId);

        MvcResult result = mockMvc.perform(post("/bookings")
                        .with(approverJwt(APPROVER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());

        // Must have conflict_flags populated
        JsonNode conflictFlags = response.get("conflict_flags");
        assertThat(conflictFlags).isNotNull();
        assertThat(conflictFlags.isArray()).isTrue();
        assertThat(conflictFlags.size()).isGreaterThan(0);

        // The conflict flag must cite the existing booking's id and type "location"
        JsonNode firstFlag = conflictFlags.get(0);
        assertThat(firstFlag.get("conflicting_booking_id").asText())
                .isEqualTo(savedExisting.getId().toString());
        assertThat(firstFlag.get("conflict_type").asText()).isEqualTo("location");
    }

    // ── Scenario 3: Edit — non-approver moves booking into conflict → 409 ──

    @Test
    void scenario3_editWithConflict_nonApprover_returns409() throws Exception {
        UUID locationId = UUID.randomUUID();

        when(locationsResourcesClient.getLocation(locationId))
                .thenReturn(Optional.of(new LocationResponse(locationId, "Edit Room", null)));

        // Seed an existing booking to be edited (owned by CREATOR_ID)
        Booking bookingToEdit = new Booking();
        bookingToEdit.setTitle("To Edit");
        bookingToEdit.setLocationId(locationId);
        bookingToEdit.setStartTime(Instant.parse("2035-08-03T08:00:00Z"));
        bookingToEdit.setEndTime(Instant.parse("2035-08-03T09:00:00Z"));
        bookingToEdit.setOwnerId(CREATOR_ID);
        bookingToEdit.setStatus("approved");
        Booking savedToEdit = bookingRepository.save(bookingToEdit);

        // Seed the conflicting booking it will clash with after the edit
        Booking conflicting = new Booking();
        conflicting.setTitle("Conflicting");
        conflicting.setLocationId(locationId);
        conflicting.setStartTime(Instant.parse("2035-08-03T10:00:00Z"));
        conflicting.setEndTime(Instant.parse("2035-08-03T11:00:00Z"));
        conflicting.setOwnerId(UUID.randomUUID());
        conflicting.setStatus("approved");
        bookingRepository.save(conflicting);
        bookingRepository.flush();

        // Non-approver moves the booking into conflict — HARD BLOCK
        String updateBody = """
                {
                    "title": "Moved to Conflict",
                    "start_time": "2035-08-03T10:30:00Z",
                    "end_time": "2035-08-03T11:30:00Z"
                }
                """;

        mockMvc.perform(put("/bookings/" + savedToEdit.getId())
                        .with(creatorJwt(CREATOR_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error_code").value("BOOKING_CONFLICT"));
    }

    // ── Scenario 4: Edit — approver moves booking into conflict → 200 with conflict_flags ──

    @Test
    void scenario4_editWithConflict_approver_returns200WithConflictFlags() throws Exception {
        UUID locationId = UUID.randomUUID();

        when(locationsResourcesClient.getLocation(locationId))
                .thenReturn(Optional.of(new LocationResponse(locationId, "Approver Edit Room", null)));

        // Seed an existing booking to be edited (owned by APPROVER_ID)
        Booking bookingToEdit = new Booking();
        bookingToEdit.setTitle("Approver's Booking");
        bookingToEdit.setLocationId(locationId);
        bookingToEdit.setStartTime(Instant.parse("2035-08-04T08:00:00Z"));
        bookingToEdit.setEndTime(Instant.parse("2035-08-04T09:00:00Z"));
        bookingToEdit.setOwnerId(APPROVER_ID);
        bookingToEdit.setStatus("approved");
        Booking savedToEdit = bookingRepository.save(bookingToEdit);

        // Seed the conflicting booking
        Booking conflicting = new Booking();
        conflicting.setTitle("Conflicting");
        conflicting.setLocationId(locationId);
        conflicting.setStartTime(Instant.parse("2035-08-04T10:00:00Z"));
        conflicting.setEndTime(Instant.parse("2035-08-04T11:00:00Z"));
        conflicting.setOwnerId(UUID.randomUUID());
        conflicting.setStatus("approved");
        Booking savedConflicting = bookingRepository.save(conflicting);
        bookingRepository.flush();

        // Approver moves their booking into conflict — SOFT WARNING, proceeds with conflict_flags
        String updateBody = """
                {
                    "title": "Override Edit",
                    "start_time": "2035-08-04T10:30:00Z",
                    "end_time": "2035-08-04T11:30:00Z"
                }
                """;

        MvcResult result = mockMvc.perform(put("/bookings/" + savedToEdit.getId())
                        .with(approverJwt(APPROVER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());

        // Must have conflict_flags populated (same policy as create)
        JsonNode conflictFlags = response.get("conflict_flags");
        assertThat(conflictFlags).isNotNull();
        assertThat(conflictFlags.isArray()).isTrue();
        assertThat(conflictFlags.size()).isGreaterThan(0);

        // The flag cites the conflicting booking with conflict_type=location
        JsonNode firstFlag = conflictFlags.get(0);
        assertThat(firstFlag.get("conflicting_booking_id").asText())
                .isEqualTo(savedConflicting.getId().toString());
        assertThat(firstFlag.get("conflict_type").asText()).isEqualTo("location");
    }
}
