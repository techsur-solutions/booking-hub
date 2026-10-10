package com.bookinghub.booking.controller;

import com.bookinghub.booking.client.CustomFieldClient;
import com.bookinghub.booking.client.LocationsResourcesClient;
import com.bookinghub.booking.client.SettingsClient;
import com.bookinghub.booking.client.dto.ClientDtos.*;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for POST /bookings, PUT /bookings/{id}, DELETE /bookings/{id},
 * POST /bookings/{id}/clone (plan 05-03, Task 3).
 *
 * MockBean stubs for LocationsResourcesClient, CustomFieldClient, SettingsClient
 * (plan 05-02 already proved these clients work against real WireMock; this test
 * proves the BUSINESS LOGIC built on top of them).
 *
 * A WireMock JWKS server is started to satisfy Spring Security's issuer-uri validation.
 *
 * Uses the running Postgres's booking_db_test (same sandbox workaround as plan 05-02
 * ConflictControllerIntegrationTest — avoids Docker API version incompatibility with Testcontainers).
 *
 * IMPORTANT: JWT setup in tests — CurrentUserProvider reads realm_access.roles directly
 * from the JWT claims (not from Spring Security's GrantedAuthority list). Both must be set:
 * 1. jwt().authorities() for @PreAuthorize checks (Spring Security GrantedAuthority)
 * 2. jwt().jwt(j -> j.claim("realm_access", Map.of("roles", List.of(...)))) for hasRole() in service
 * 3. jwt().jwt(j -> j.claim("sub", callerId.toString())) for getCurrentUserId() in service
 *
 * 11 scenarios per the plan.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class BookingControllerIntegrationTest {

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

    /** A fixed UUID used as the default caller's user id across tests. */
    private static final UUID DEFAULT_CALLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

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
        // Default: location exists and is active
        when(locationsResourcesClient.getLocation(any(UUID.class)))
                .thenReturn(Optional.of(new LocationResponse(UUID.randomUUID(), "Test Location", null)));

        // Default: no resources
        when(locationsResourcesClient.getResource(any(UUID.class)))
                .thenReturn(Optional.empty());

        // Default: custom fields return empty (no custom fields applicable)
        when(customFieldClient.getApplicableFields(any(UUID.class)))
                .thenReturn(List.of());

        // Default: approveBooking = false (bookings auto-approved)
        when(settingsClient.getApproveBookingFlag()).thenReturn(false);
    }

    // ── Helper: build jwt() with creator role and UUID sub claim ──

    private static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor
            creatorJwt(UUID userId) {
        return jwt()
                .authorities(new SimpleGrantedAuthority("ROLE_role_booking_creator"))
                .jwt(j -> j
                        .claim("sub", userId.toString())
                        .claim("realm_access", Map.of("roles", List.of("role_booking_creator"))));
    }

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

    // ── Scenario 1: Creator, no end_time, approveBooking=false → 201, end=start+1h, status=approved ──

    @Test
    void scenario1_creatorNoEndTime_approveFlagFalse_returns201WithDefaults() throws Exception {
        UUID locationId = UUID.randomUUID();

        when(locationsResourcesClient.getLocation(locationId))
                .thenReturn(Optional.of(new LocationResponse(locationId, "Meeting Room", null)));
        when(settingsClient.getApproveBookingFlag()).thenReturn(false);

        String body = """
                {
                    "title": "Team Meeting",
                    "location_id": "%s",
                    "start_time": "2035-07-01T10:00:00Z"
                }
                """.formatted(locationId);

        MvcResult result = mockMvc.perform(post("/bookings")
                        .with(creatorJwt(DEFAULT_CALLER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(response.get("end_time").asText()).isEqualTo("2035-07-01T11:00:00Z");
        assertThat(response.get("status").asText()).isEqualTo("approved");
        assertThat(response.get("id").isNull()).isFalse();
    }

    // ── Scenario 2: Creator, approveBooking=true, no approver → 201 status=pending ──

    @Test
    void scenario2_creatorNoApproverRole_approveFlagTrue_returns201Pending() throws Exception {
        UUID locationId = UUID.randomUUID();
        when(locationsResourcesClient.getLocation(locationId))
                .thenReturn(Optional.of(new LocationResponse(locationId, "Room A", null)));
        when(settingsClient.getApproveBookingFlag()).thenReturn(true);

        String body = """
                {
                    "title": "Pending Booking",
                    "location_id": "%s",
                    "start_time": "2035-07-02T10:00:00Z",
                    "end_time": "2035-07-02T11:00:00Z"
                }
                """.formatted(locationId);

        MvcResult result = mockMvc.perform(post("/bookings")
                        .with(creatorJwt(DEFAULT_CALLER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(response.get("status").asText()).isEqualTo("pending");
    }

    // ── Scenario 3: Creator+Approver, approveBooking=true → 201 status=approved, ONLY booking.created ──

    @Test
    void scenario3_creatorAndApprover_approveFlagTrue_bypassToApproved_onlyCreatedEvent() throws Exception {
        UUID locationId = UUID.randomUUID();
        when(locationsResourcesClient.getLocation(locationId))
                .thenReturn(Optional.of(new LocationResponse(locationId, "Bypass Room", null)));
        when(settingsClient.getApproveBookingFlag()).thenReturn(true);

        String body = """
                {
                    "title": "Bypass Test",
                    "location_id": "%s",
                    "start_time": "2035-07-03T10:00:00Z",
                    "end_time": "2035-07-03T11:00:00Z"
                }
                """.formatted(locationId);

        MvcResult result = mockMvc.perform(post("/bookings")
                        .with(approverJwt(DEFAULT_CALLER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(response.get("status").asText()).isEqualTo("approved");

        // Verify ONLY booking.created in outbox (no booking.approved — F0 point 4 RESOLVED)
        UUID bookingId = UUID.fromString(response.get("id").asText());
        List<com.bookinghub.booking.domain.OutboxEvent> events = outboxEventRepository.findAll();
        long bookingEvents = events.stream()
                .filter(e -> e.getAggregateId().equals(bookingId))
                .count();
        assertThat(bookingEvents).isEqualTo(1L);
        String routingKey = events.stream()
                .filter(e -> e.getAggregateId().equals(bookingId))
                .findFirst().get().getRoutingKey();
        assertThat(routingKey).isEqualTo("booking.created");
    }

    // ── Scenario 4: Zero duration → 400 BOOKING_INVALID_TIME_RANGE ──

    @Test
    void scenario4_zeroDuration_returns400InvalidTimeRange() throws Exception {
        UUID locationId = UUID.randomUUID();
        when(locationsResourcesClient.getLocation(locationId))
                .thenReturn(Optional.of(new LocationResponse(locationId, "Room Z", null)));

        String body = """
                {
                    "title": "Bad Duration",
                    "location_id": "%s",
                    "start_time": "2035-07-04T10:00:00Z",
                    "end_time": "2035-07-04T10:00:00Z"
                }
                """.formatted(locationId);

        mockMvc.perform(post("/bookings")
                        .with(creatorJwt(DEFAULT_CALLER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value("BOOKING_INVALID_TIME_RANGE"));
    }

    // ── Scenario 5: No title → 400 BOOKING_TITLE_REQUIRED ──

    @Test
    void scenario5_noTitle_returns400TitleRequired() throws Exception {
        String body = """
                {
                    "location_id": "%s",
                    "start_time": "2035-07-05T10:00:00Z",
                    "end_time": "2035-07-05T11:00:00Z"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/bookings")
                        .with(creatorJwt(DEFAULT_CALLER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value("BOOKING_TITLE_REQUIRED"));
    }

    // ── Scenario 6: Nonexistent location → 404 BOOKING_LOCATION_NOT_FOUND ──

    @Test
    void scenario6_nonexistentLocation_returns404LocationNotFound() throws Exception {
        UUID badLocationId = UUID.randomUUID();
        when(locationsResourcesClient.getLocation(badLocationId))
                .thenReturn(Optional.empty());

        String body = """
                {
                    "title": "Bad Location",
                    "location_id": "%s",
                    "start_time": "2035-07-06T10:00:00Z",
                    "end_time": "2035-07-06T11:00:00Z"
                }
                """.formatted(badLocationId);

        mockMvc.perform(post("/bookings")
                        .with(creatorJwt(DEFAULT_CALLER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_code").value("BOOKING_LOCATION_NOT_FOUND"));
    }

    // ── Scenario 7: Ownership — non-owner creator → 403; approver → 200 ──

    @Test
    void scenario7_nonOwnerCreator_returns403_approverOverride_returns200() throws Exception {
        UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID differentCallerId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID locationId = UUID.randomUUID();

        when(locationsResourcesClient.getLocation(locationId))
                .thenReturn(Optional.of(new LocationResponse(locationId, "Owner Room", null)));

        // Create booking as owner A
        Booking booking = new Booking();
        booking.setTitle("Owner's Booking");
        booking.setLocationId(locationId);
        booking.setStartTime(Instant.parse("2035-07-07T10:00:00Z"));
        booking.setEndTime(Instant.parse("2035-07-07T11:00:00Z"));
        booking.setOwnerId(ownerId);
        booking.setStatus("approved");
        Booking saved = bookingRepository.save(booking);
        bookingRepository.flush();

        // Non-owner creator tries to update → 403
        String updateBody = """
                {
                    "title": "Hijacked Title"
                }
                """;

        mockMvc.perform(put("/bookings/" + saved.getId())
                        .with(creatorJwt(differentCallerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error_code").value("BOOKING_FORBIDDEN"));

        // Approver (different from owner) → override allowed → 200
        mockMvc.perform(put("/bookings/" + saved.getId())
                        .with(approverJwt(differentCallerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());
    }

    // ── Scenario 8: scope field is simply ignored, no BOOKING_SCOPE_REQUIRED error ──

    @Test
    void scenario8_scopeFieldIgnored_noError() throws Exception {
        UUID callerId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID locationId = UUID.randomUUID();

        when(locationsResourcesClient.getLocation(locationId))
                .thenReturn(Optional.of(new LocationResponse(locationId, "Scope Room", null)));

        // Create a booking first to update (owned by callerId)
        Booking existing = new Booking();
        existing.setTitle("Existing");
        existing.setLocationId(locationId);
        existing.setStartTime(Instant.parse("2035-07-08T10:00:00Z"));
        existing.setEndTime(Instant.parse("2035-07-08T11:00:00Z"));
        existing.setOwnerId(callerId);
        existing.setStatus("approved");
        Booking saved = bookingRepository.save(existing);
        bookingRepository.flush();

        // Send a PUT with a scope field that should be silently ignored
        String body = """
                {
                    "title": "Updated Title",
                    "scope": "whole_series"
                }
                """;

        mockMvc.perform(put("/bookings/" + saved.getId())
                        .with(creatorJwt(callerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk()); // Not 400 BOOKING_SCOPE_REQUIRED — scope field dropped
    }

    // ── Scenario 9: Clone → 201, id=null, series_id=null, owner_id=caller ──

    @Test
    void scenario9_clone_returnsNonPersistedDraftWithResetFields() throws Exception {
        UUID originalOwnerId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        UUID callerUserId = UUID.fromString("55555555-5555-5555-5555-555555555555");

        Booking source = new Booking();
        source.setTitle("Original Booking");
        source.setLocationId(UUID.randomUUID());
        source.setStartTime(Instant.parse("2035-07-09T10:00:00Z"));
        source.setEndTime(Instant.parse("2035-07-09T11:00:00Z"));
        source.setOwnerId(originalOwnerId);
        source.setStatus("approved");
        Booking saved = bookingRepository.save(source);
        bookingRepository.flush();

        MvcResult result = mockMvc.perform(post("/bookings/" + saved.getId() + "/clone")
                        .with(creatorJwt(callerUserId)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());

        // id must be null (draft marker)
        assertThat(response.get("id").isNull()).isTrue();

        // series_id must be null
        assertThat(response.get("series_id").isNull()).isTrue();

        // owner_id must be the CALLING user, not the original owner
        assertThat(response.get("owner_id").asText()).isEqualTo(callerUserId.toString());

        // Title/content preserved from source
        assertThat(response.get("title").asText()).isEqualTo("Original Booking");

        // Verify the draft was NOT persisted (only the source booking exists)
        assertThat(bookingRepository.count()).isEqualTo(1L);
    }

    // ── Scenario 10: Recurrence → list of BookingResponse with same series_id ──

    @Test
    void scenario10_recurrence_returnsListWithSharedSeriesId() throws Exception {
        UUID locationId = UUID.randomUUID();
        when(locationsResourcesClient.getLocation(locationId))
                .thenReturn(Optional.of(new LocationResponse(locationId, "Series Room", null)));
        when(settingsClient.getApproveBookingFlag()).thenReturn(false);

        // Base: Monday 2035-07-07 (a Monday), recurrence Mon/Wed for 1 week
        String body = """
                {
                    "title": "Series Booking",
                    "location_id": "%s",
                    "start_time": "2035-07-07T10:00:00Z",
                    "end_time": "2035-07-07T11:00:00Z",
                    "recurrence": {
                        "pattern": "weekly",
                        "days_of_week": [1, 3],
                        "end_date": "2035-07-09"
                    }
                }
                """.formatted(locationId);

        MvcResult result = mockMvc.perform(post("/bookings")
                        .with(creatorJwt(DEFAULT_CALLER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());

        // Should return a list (multiple occurrences)
        assertThat(response.isArray()).isTrue();
        assertThat(response.size()).isGreaterThan(1);

        // All should share the same non-null series_id
        String seriesId = response.get(0).get("series_id").asText();
        assertThat(seriesId).isNotBlank();
        for (JsonNode occ : response) {
            assertThat(occ.get("series_id").asText()).isEqualTo(seriesId);
        }
    }

    // ── Scenario 11: Custom field applicability enforcement and graceful degradation ──

    @Test
    void scenario11_customFieldApplicabilityEnforcement_andGracefulDegradation() throws Exception {
        UUID locationId = UUID.randomUUID();
        UUID fieldAId = UUID.randomUUID();
        UUID fieldBId = UUID.randomUUID();

        when(locationsResourcesClient.getLocation(locationId))
                .thenReturn(Optional.of(new LocationResponse(locationId, "Custom Room", null)));
        when(settingsClient.getApproveBookingFlag()).thenReturn(false);

        // Mock: only field A is applicable
        when(customFieldClient.getApplicableFields(locationId))
                .thenReturn(List.of(new CustomFieldDefinition(fieldAId, "Room Type", "select")));

        // Sub-scenario A: Inapplicable field B → 400 BOOKING_CUSTOM_FIELD_NOT_APPLICABLE
        String bodyWithBadField = """
                {
                    "title": "Custom Field Test A",
                    "location_id": "%s",
                    "start_time": "2035-07-10T10:00:00Z",
                    "end_time": "2035-07-10T11:00:00Z",
                    "custom_field_values": [
                        {"field_id": "%s", "value": "bad-field"}
                    ]
                }
                """.formatted(locationId, fieldBId);

        mockMvc.perform(post("/bookings")
                        .with(creatorJwt(DEFAULT_CALLER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyWithBadField))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value("BOOKING_CUSTOM_FIELD_NOT_APPLICABLE"));

        // Sub-scenario B: Applicable field A → 201 success
        String bodyWithGoodField = """
                {
                    "title": "Custom Field Test B",
                    "location_id": "%s",
                    "start_time": "2035-07-11T10:00:00Z",
                    "end_time": "2035-07-11T11:00:00Z",
                    "custom_field_values": [
                        {"field_id": "%s", "value": "conference-room"}
                    ]
                }
                """.formatted(locationId, fieldAId);

        mockMvc.perform(post("/bookings")
                        .with(creatorJwt(DEFAULT_CALLER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyWithGoodField))
                .andExpect(status().isCreated());

        // Sub-scenario C: Empty fields list (graceful degradation) → 201 success
        when(customFieldClient.getApplicableFields(locationId))
                .thenReturn(List.of()); // Empty = graceful degradation (client logged warning)

        String bodyWithAnyField = """
                {
                    "title": "Custom Field Test C - Graceful",
                    "location_id": "%s",
                    "start_time": "2035-07-12T10:00:00Z",
                    "end_time": "2035-07-12T11:00:00Z",
                    "custom_field_values": [
                        {"field_id": "%s", "value": "anything"}
                    ]
                }
                """.formatted(locationId, fieldBId);

        mockMvc.perform(post("/bookings")
                        .with(creatorJwt(DEFAULT_CALLER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyWithAnyField))
                .andExpect(status().isCreated()); // Graceful degradation: validation skipped
    }
}
