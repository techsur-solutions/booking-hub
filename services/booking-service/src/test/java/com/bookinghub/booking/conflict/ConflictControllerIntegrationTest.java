package com.bookinghub.booking.conflict;

import com.bookinghub.booking.client.LocationsResourcesClient;
import com.bookinghub.booking.conflict.dto.ConflictDtos.ConflictCheckResult;
import com.bookinghub.booking.domain.Booking;
import com.bookinghub.booking.repository.BookingRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for POST /bookings/check-conflicts (plan 05-02).
 *
 * Uses a running Postgres (same sandbox Docker-API-version workaround as all
 * other @SpringBootTest tests in this service — Testcontainers requires Docker
 * API >=1.40, this sandbox reports 1.32).
 *
 * LocationsResourcesClient's downstream HTTP calls are mocked via @MockBean
 * (Task 2 already proved the client itself works against WireMock).
 *
 * JWT authentication is provided via Spring Security Test's jwt() post-processor —
 * no real Keycloak required. A WireMock JWKS server is started to satisfy the
 * OAuth2 resource server's eager issuer-uri validation on context startup.
 *
 * 5 scenarios per the plan:
 * 1. Creator with no conflicting bookings → 200 {has_conflict: false}
 * 2. Creator with a conflicting booking seeded → 200 {has_conflict: true} (NOT 409)
 * 3. Neither location_id nor resource_ids → 400 CONFLICT_CHECK_INVALID_INPUT
 * 4. No booking role at all → 403 BOOKING_FORBIDDEN
 * 5. Only role_booking_approver (not role_booking_creator) → 403 BOOKING_FORBIDDEN
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ConflictControllerIntegrationTest {

    private static WireMockServer wireMock;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @MockBean
    private LocationsResourcesClient locationsResourcesClient;

    @BeforeAll
    static void startWireMock() {
        wireMock = new WireMockServer(0);
        wireMock.start();
        WireMock.configureFor("localhost", wireMock.port());

        // Minimal JWKS stub to satisfy Spring Security's issuer-uri validation
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
    void setUpMocks() {
        // By default, getResource returns Optional.empty() (resource not found / not unique)
        when(locationsResourcesClient.getResource(any(UUID.class)))
                .thenReturn(Optional.empty());
    }

    // ── Scenario 1: Creator, no conflicts → 200 {has_conflict: false} ────────

    @Test
    void creatorWithNoConflicts_returns200WithNoConflict() throws Exception {
        UUID locationId = UUID.randomUUID();

        String requestBody = """
                {
                    "locationId": "%s",
                    "startTime": "2035-06-01T10:00:00Z",
                    "endTime": "2035-06-01T11:00:00Z"
                }
                """.formatted(locationId);

        MvcResult result = mockMvc.perform(post("/bookings/check-conflicts")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_role_booking_creator")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        ConflictCheckResult response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ConflictCheckResult.class);

        assertThat(response.hasConflict()).isFalse();
        assertThat(response.conflicts()).isEmpty();
    }

    // ── Scenario 2: Creator, conflicting booking exists → 200 (NOT 409) ──────

    @Test
    void creatorWithConflictingBooking_returns200WithConflict_NEVER409() throws Exception {
        UUID locationId = UUID.randomUUID();

        // Seed a conflicting booking for the same location/time slot
        Booking existing = new Booking();
        existing.setTitle("Existing booking");
        existing.setLocationId(locationId);
        existing.setStartTime(Instant.parse("2035-06-02T10:00:00Z"));
        existing.setEndTime(Instant.parse("2035-06-02T11:00:00Z"));
        existing.setOwnerId(UUID.randomUUID());
        existing.setStatus("pending");
        bookingRepository.save(existing);
        bookingRepository.flush();

        String requestBody = """
                {
                    "locationId": "%s",
                    "startTime": "2035-06-02T10:30:00Z",
                    "endTime": "2035-06-02T11:30:00Z"
                }
                """.formatted(locationId);

        MvcResult result = mockMvc.perform(post("/bookings/check-conflicts")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_role_booking_creator")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk()) // 200, NOT 409 — purely informational endpoint
                .andReturn();

        ConflictCheckResult response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ConflictCheckResult.class);

        assertThat(response.hasConflict()).isTrue();
        assertThat(response.conflicts()).isNotEmpty();
        assertThat(response.conflicts().get(0).conflictType()).isEqualTo("location");
    }

    // ── Scenario 3: Neither location_id nor resource_ids → 400 ───────────────

    @Test
    void missingLocationAndResources_returns400ConflictCheckInvalidInput() throws Exception {
        String requestBody = """
                {
                    "startTime": "2035-06-01T10:00:00Z",
                    "endTime": "2035-06-01T11:00:00Z"
                }
                """;

        mockMvc.perform(post("/bookings/check-conflicts")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_role_booking_creator")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value("CONFLICT_CHECK_INVALID_INPUT"));
    }

    // ── Scenario 4: No booking role → 403 BOOKING_FORBIDDEN ─────────────────

    @Test
    void callerWithNoBookingRole_returns403() throws Exception {
        String requestBody = """
                {
                    "locationId": "%s",
                    "startTime": "2035-06-01T10:00:00Z",
                    "endTime": "2035-06-01T11:00:00Z"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/bookings/check-conflicts")
                        .with(jwt())  // authenticated, no booking roles
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error_code").value("BOOKING_FORBIDDEN"));
    }

    // ── Scenario 5: Only role_booking_approver → 403 (not creator-gated) ─────

    @Test
    void callerWithOnlyApproverRole_returns403() throws Exception {
        String requestBody = """
                {
                    "locationId": "%s",
                    "startTime": "2035-06-01T10:00:00Z",
                    "endTime": "2035-06-01T11:00:00Z"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/bookings/check-conflicts")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_role_booking_approver")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error_code").value("BOOKING_FORBIDDEN"));
    }
}
