package com.bookinghub.booking.controller;

import com.bookinghub.booking.client.CustomFieldClient;
import com.bookinghub.booking.client.LocationsResourcesClient;
import com.bookinghub.booking.client.SettingsClient;
import com.bookinghub.booking.domain.Booking;
import com.bookinghub.booking.repository.BookingRepository;
import com.bookinghub.booking.repository.OutboxEventRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for POST /bookings/{id}/approve and POST /bookings/{id}/deny (plan 05-04).
 *
 * IMPORTANT: JWT setup — CurrentUserProvider reads realm_access.roles directly from JWT
 * claims (not Spring Security GrantedAuthority list). Both must be set:
 * 1. jwt().authorities() for Spring Security's @PreAuthorize (BookingApprovalController
 *    has NO @PreAuthorize, but WireMock JWKS validation still requires a valid principal)
 * 2. jwt().jwt(j -> j.claim("realm_access", ...)) for hasRole() manual check in controller
 * 3. jwt().jwt(j -> j.claim("sub", ...)) for getCurrentUserId() in service
 *
 * 6 scenarios per the plan:
 * 1. Pending booking + approver → 200, status=approved, approved_by/approved_at set, booking.approved outbox row
 * 2. Same booking, approve again (already approved) → 409 APPROVAL_INVALID_STATE (one-way proof)
 * 3. Separate pending booking + approver + deny with reason → 200, status=denied, outbox booking.denied
 * 4. Deny the already-approved booking from scenario 1 (approved→denied) → 409 APPROVAL_INVALID_STATE
 * 5. Non-approver caller + approve → 403 APPROVAL_FORBIDDEN (FRD-named code, NOT BOOKING_FORBIDDEN)
 * 6. Nonexistent UUID + approve → 404 APPROVAL_BOOKING_NOT_FOUND
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class BookingApprovalControllerIntegrationTest {

    private static WireMockServer wireMock;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @MockBean
    private LocationsResourcesClient locationsResourcesClient;

    @MockBean
    private CustomFieldClient customFieldClient;

    @MockBean
    private SettingsClient settingsClient;

    private static final UUID APPROVER_ID = UUID.fromString("cc000000-0000-0000-0000-000000000001");
    private static final UUID CREATOR_ID  = UUID.fromString("cc000000-0000-0000-0000-000000000002");

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
        when(locationsResourcesClient.getResource(any(UUID.class))).thenReturn(java.util.Optional.empty());
        when(customFieldClient.getApplicableFields(any(UUID.class))).thenReturn(List.of());
        when(settingsClient.getApproveBookingFlag()).thenReturn(false);
    }

    // ── JWT builders ─────────────────────────────────────────────────────────

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

    private static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor
            creatorJwt(UUID userId) {
        return jwt()
                .authorities(new SimpleGrantedAuthority("ROLE_role_booking_creator"))
                .jwt(j -> j
                        .claim("sub", userId.toString())
                        .claim("realm_access", Map.of("roles", List.of("role_booking_creator"))));
    }

    // ── Helper: seed a pending booking ──────────────────────────────────────

    private Booking seedPendingBooking(String title) {
        Booking b = new Booking();
        b.setTitle(title);
        b.setLocationId(UUID.fromString("dd000000-0000-0000-0000-000000000001"));
        b.setStartTime(Instant.parse("2036-03-01T10:00:00Z"));
        b.setEndTime(Instant.parse("2036-03-01T11:00:00Z"));
        b.setStatus("pending");
        b.setOwnerId(CREATOR_ID);
        Booking saved = bookingRepository.save(b);
        bookingRepository.flush();
        return saved;
    }

    // ── Scenario 1: Approve a pending booking → 200, outbox booking.approved ─

    @Test
    void scenario1_approvePendingBooking_returns200WithApprovedStatus_andOutboxRow() throws Exception {
        Booking pending = seedPendingBooking("Approval Test Booking");

        MvcResult result = mockMvc.perform(post("/bookings/" + pending.getId() + "/approve")
                        .with(approverJwt(APPROVER_ID)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(response.get("status").asText()).isEqualTo("approved");
        assertThat(response.get("approved_by").asText()).isEqualTo(APPROVER_ID.toString());
        assertThat(response.get("approved_at").isNull()).isFalse();

        // Verify outbox row with routing_key="booking.approved" was created
        var outboxRows = outboxEventRepository.findAll();
        long approvedEvents = outboxRows.stream()
                .filter(e -> e.getAggregateId().equals(pending.getId()))
                .filter(e -> "booking.approved".equals(e.getRoutingKey()))
                .count();
        assertThat(approvedEvents).isEqualTo(1L);
    }

    // ── Scenario 2: Approve already-approved → 409 APPROVAL_INVALID_STATE ───

    @Test
    void scenario2_approveAlreadyApproved_returns409InvalidState_oneWayProof() throws Exception {
        Booking pending = seedPendingBooking("Re-Approval Test");

        // First approval → 200
        mockMvc.perform(post("/bookings/" + pending.getId() + "/approve")
                        .with(approverJwt(APPROVER_ID)))
                .andExpect(status().isOk());

        // Second approval → 409 APPROVAL_INVALID_STATE (one-way transition proof)
        mockMvc.perform(post("/bookings/" + pending.getId() + "/approve")
                        .with(approverJwt(APPROVER_ID)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error_code").value("APPROVAL_INVALID_STATE"));
    }

    // ── Scenario 3: Deny pending booking → 200, denial_reason, outbox booking.denied ─

    @Test
    void scenario3_denyPendingBooking_returns200WithDenialReason_andOutboxRow() throws Exception {
        Booking pending = seedPendingBooking("Denial Test Booking");

        String denialBody = """
                {"denial_reason": "Room needed for maintenance"}
                """;

        MvcResult result = mockMvc.perform(post("/bookings/" + pending.getId() + "/deny")
                        .with(approverJwt(APPROVER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(denialBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(response.get("status").asText()).isEqualTo("denied");
        assertThat(response.get("denied_by").asText()).isEqualTo(APPROVER_ID.toString());
        assertThat(response.get("denied_at").isNull()).isFalse();
        assertThat(response.get("denial_reason").asText()).isEqualTo("Room needed for maintenance");

        // Verify outbox row with routing_key="booking.denied"
        var outboxRows = outboxEventRepository.findAll();
        long deniedEvents = outboxRows.stream()
                .filter(e -> e.getAggregateId().equals(pending.getId()))
                .filter(e -> "booking.denied".equals(e.getRoutingKey()))
                .count();
        assertThat(deniedEvents).isEqualTo(1L);
    }

    // ── Scenario 4: Deny already-approved (cross-direction re-toggle) → 409 ─

    @Test
    void scenario4_denyAlreadyApproved_returns409InvalidState_crossDirectionProof() throws Exception {
        Booking pending = seedPendingBooking("Cross-Direction Test");

        // Approve first
        mockMvc.perform(post("/bookings/" + pending.getId() + "/approve")
                        .with(approverJwt(APPROVER_ID)))
                .andExpect(status().isOk());

        // Now deny the already-approved booking → 409 APPROVAL_INVALID_STATE
        // This proves the one-way rule blocks BOTH same-direction AND cross-direction re-toggling
        mockMvc.perform(post("/bookings/" + pending.getId() + "/deny")
                        .with(approverJwt(APPROVER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error_code").value("APPROVAL_INVALID_STATE"));
    }

    // ── Scenario 5: Non-approver → 403 APPROVAL_FORBIDDEN (FRD-named code) ─

    @Test
    void scenario5_nonApprover_returns403WithApprovalForbiddenCode_notGenericBookingForbidden() throws Exception {
        Booking pending = seedPendingBooking("Forbidden Approval Test");

        // Caller has only role_booking_creator (no role_booking_approver)
        mockMvc.perform(post("/bookings/" + pending.getId() + "/approve")
                        .with(creatorJwt(CREATOR_ID)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error_code").value("APPROVAL_FORBIDDEN"));
        // NOTE: APPROVAL_FORBIDDEN is the FRD-named code, NOT the generic BOOKING_FORBIDDEN.
        // This verifies the manual-check/no-@PreAuthorize design decision produced the correct code.
    }

    // ── Scenario 6: Nonexistent UUID → 404 APPROVAL_BOOKING_NOT_FOUND ────────

    @Test
    void scenario6_nonexistentBookingId_returns404ApprovalBookingNotFound() throws Exception {
        UUID neverExisted = UUID.randomUUID();

        mockMvc.perform(post("/bookings/" + neverExisted + "/approve")
                        .with(approverJwt(APPROVER_ID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_code").value("APPROVAL_BOOKING_NOT_FOUND"));
    }
}
