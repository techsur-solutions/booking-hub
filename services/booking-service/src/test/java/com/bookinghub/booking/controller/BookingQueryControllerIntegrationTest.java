package com.bookinghub.booking.controller;

import com.bookinghub.booking.client.CustomFieldClient;
import com.bookinghub.booking.client.LocationsResourcesClient;
import com.bookinghub.booking.client.SettingsClient;
import com.bookinghub.booking.client.dto.ClientDtos.ResourceResponse;
import com.bookinghub.booking.domain.Booking;
import com.bookinghub.booking.domain.BookingResource;
import com.bookinghub.booking.repository.BookingRepository;
import com.bookinghub.booking.repository.BookingResourceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for GET /bookings (calendar/day/list) and GET /bookings/{id} (plan 05-04).
 *
 * IMPORTANT: JWT setup — CurrentUserProvider reads realm_access.roles from JWT claims directly.
 * Both jwt().authorities() (for @PreAuthorize) AND jwt().jwt(j -> j.claim("realm_access", ...))
 * (for hasRole() in service) must be set, plus the sub claim for getCurrentUserId().
 *
 * 8 scenarios per the plan:
 * 1. Broad range query → all 3 seeded bookings returned, ordered by start_time
 * 2. Narrow range (day-view) → only 2 bookings within the window
 * 3. location_id filter → only bookings at that location
 * 4. status=pending filter → only pending bookings
 * 5. q=keyword filter → case-insensitive match on title
 * 6. Two overlapping bookings → both returned with non-empty conflict_flags (read-only, 200)
 * 7. Soft-deleted booking → 200 with deleted_at populated (last-known-values); random UUID → 404
 * 8. No booking role → 403 BOOKING_FORBIDDEN
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class BookingQueryControllerIntegrationTest {

    private static WireMockServer wireMock;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingResourceRepository bookingResourceRepository;

    @MockBean
    private LocationsResourcesClient locationsResourcesClient;

    @MockBean
    private CustomFieldClient customFieldClient;

    @MockBean
    private SettingsClient settingsClient;

    private static final UUID VIEWER_ID = UUID.fromString("aa000000-0000-0000-0000-000000000001");

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
        // Default: no resources (is_unique=false)
        when(locationsResourcesClient.getResource(any(UUID.class)))
                .thenReturn(Optional.empty());
        // Default: no custom fields
        when(customFieldClient.getApplicableFields(any(UUID.class)))
                .thenReturn(List.of());
        // Default: approveBooking=false
        when(settingsClient.getApproveBookingFlag()).thenReturn(false);
    }

    // ── Helper: build jwt with viewer role ──────────────────────────────────

    private static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor
            viewerJwt(UUID userId) {
        return jwt()
                .authorities(new SimpleGrantedAuthority("ROLE_role_booking_viewer"))
                .jwt(j -> j
                        .claim("sub", userId.toString())
                        .claim("realm_access", Map.of("roles", List.of("role_booking_viewer"))));
    }

    // ── Helper: create a booking directly via repository ────────────────────

    private Booking seedBooking(UUID locationId, Instant start, Instant end, String status, String title) {
        Booking b = new Booking();
        b.setTitle(title);
        b.setLocationId(locationId);
        b.setStartTime(start);
        b.setEndTime(end);
        b.setStatus(status);
        b.setOwnerId(VIEWER_ID);
        Booking saved = bookingRepository.save(b);
        bookingRepository.flush();
        return saved;
    }

    // ── Scenario 1: Broad range → all 3 returned, ordered by start_time ─────

    @Test
    void scenario1_broadRange_returns3BookingsOrderedByStartTime() throws Exception {
        UUID loc1 = UUID.fromString("11111111-0000-0000-0000-000000000001");
        UUID loc2 = UUID.fromString("11111111-0000-0000-0000-000000000002");

        // Seed 3 bookings at different times (out of order) in 2 locations
        seedBooking(loc2, Instant.parse("2035-08-02T10:00:00Z"), Instant.parse("2035-08-02T11:00:00Z"), "approved", "Second Booking");
        seedBooking(loc1, Instant.parse("2035-08-01T09:00:00Z"), Instant.parse("2035-08-01T10:00:00Z"), "approved", "First Booking");
        seedBooking(loc1, Instant.parse("2035-08-03T14:00:00Z"), Instant.parse("2035-08-03T15:00:00Z"), "pending", "Third Booking");

        MvcResult result = mockMvc.perform(get("/bookings")
                        .with(viewerJwt(VIEWER_ID))
                        .param("from", "2035-07-31T00:00:00Z")
                        .param("to", "2035-08-31T00:00:00Z"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(response.isArray()).isTrue();
        assertThat(response.size()).isEqualTo(3);
        // Ordered by start_time ASC
        assertThat(response.get(0).get("title").asText()).isEqualTo("First Booking");
        assertThat(response.get(1).get("title").asText()).isEqualTo("Second Booking");
        assertThat(response.get(2).get("title").asText()).isEqualTo("Third Booking");
    }

    // ── Scenario 2: Narrow range (day-view) excludes one booking ────────────

    @Test
    void scenario2_narrowRange_dayViewStyle_excludesOutOfWindowBooking() throws Exception {
        UUID loc = UUID.fromString("22222222-0000-0000-0000-000000000001");

        seedBooking(loc, Instant.parse("2035-09-01T09:00:00Z"), Instant.parse("2035-09-01T10:00:00Z"), "approved", "In Window 1");
        seedBooking(loc, Instant.parse("2035-09-01T11:00:00Z"), Instant.parse("2035-09-01T12:00:00Z"), "approved", "In Window 2");
        seedBooking(loc, Instant.parse("2035-09-02T09:00:00Z"), Instant.parse("2035-09-02T10:00:00Z"), "approved", "Out of Window");

        MvcResult result = mockMvc.perform(get("/bookings")
                        .with(viewerJwt(VIEWER_ID))
                        .param("from", "2035-09-01T00:00:00Z")
                        .param("to", "2035-09-02T00:00:00Z"))  // Only Sep 1
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.get(0).get("title").asText()).isEqualTo("In Window 1");
        assertThat(response.get(1).get("title").asText()).isEqualTo("In Window 2");
    }

    // ── Scenario 3: location_id filter ──────────────────────────────────────

    @Test
    void scenario3_locationIdFilter_returnsOnlyMatchingLocation() throws Exception {
        UUID locA = UUID.fromString("33333333-0000-0000-0000-000000000001");
        UUID locB = UUID.fromString("33333333-0000-0000-0000-000000000002");

        seedBooking(locA, Instant.parse("2035-10-01T09:00:00Z"), Instant.parse("2035-10-01T10:00:00Z"), "approved", "At Location A");
        seedBooking(locB, Instant.parse("2035-10-01T10:00:00Z"), Instant.parse("2035-10-01T11:00:00Z"), "approved", "At Location B");

        MvcResult result = mockMvc.perform(get("/bookings")
                        .with(viewerJwt(VIEWER_ID))
                        .param("from", "2035-10-01T00:00:00Z")
                        .param("to", "2035-10-02T00:00:00Z")
                        .param("location_id", locA.toString()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(response.size()).isEqualTo(1);
        assertThat(response.get(0).get("title").asText()).isEqualTo("At Location A");
    }

    // ── Scenario 4: status=pending filter ───────────────────────────────────

    @Test
    void scenario4_statusFilter_returnsOnlyPendingBookings() throws Exception {
        UUID loc = UUID.fromString("44444444-0000-0000-0000-000000000001");

        seedBooking(loc, Instant.parse("2035-11-01T09:00:00Z"), Instant.parse("2035-11-01T10:00:00Z"), "pending", "Pending One");
        seedBooking(loc, Instant.parse("2035-11-01T10:00:00Z"), Instant.parse("2035-11-01T11:00:00Z"), "approved", "Approved One");
        seedBooking(loc, Instant.parse("2035-11-01T11:00:00Z"), Instant.parse("2035-11-01T12:00:00Z"), "denied", "Denied One");

        MvcResult result = mockMvc.perform(get("/bookings")
                        .with(viewerJwt(VIEWER_ID))
                        .param("from", "2035-11-01T00:00:00Z")
                        .param("to", "2035-11-02T00:00:00Z")
                        .param("status", "pending"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(response.size()).isEqualTo(1);
        assertThat(response.get(0).get("title").asText()).isEqualTo("Pending One");
    }

    // ── Scenario 5: q=keyword case-insensitive title search ─────────────────

    @Test
    void scenario5_keywordFilter_caseInsensitiveTitleSearch() throws Exception {
        UUID loc = UUID.fromString("55555555-0000-0000-0000-000000000001");

        seedBooking(loc, Instant.parse("2035-12-01T09:00:00Z"), Instant.parse("2035-12-01T10:00:00Z"), "approved", "Board Meeting");
        seedBooking(loc, Instant.parse("2035-12-01T11:00:00Z"), Instant.parse("2035-12-01T12:00:00Z"), "approved", "Team Standup");

        MvcResult result = mockMvc.perform(get("/bookings")
                        .with(viewerJwt(VIEWER_ID))
                        .param("from", "2035-12-01T00:00:00Z")
                        .param("to", "2035-12-02T00:00:00Z")
                        .param("q", "BOARD"))  // uppercase — should match "Board Meeting" case-insensitively
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(response.size()).isEqualTo(1);
        assertThat(response.get(0).get("title").asText()).isEqualTo("Board Meeting");
    }

    // ── Scenario 6: Overlapping bookings → both returned with conflict_flags ─

    @Test
    void scenario6_overlappingBookings_bothReturnedWithConflictFlags_neverBlocking() throws Exception {
        // Key assertion: bulk reads NEVER block (always 200), but DO attach conflict_flags
        // proving the shared ConflictDetectionService is called in pure read-only mode.
        UUID loc = UUID.fromString("66666666-0000-0000-0000-000000000001");

        // Seed two overlapping bookings at the SAME location (10:00-11:00 and 10:30-11:30)
        Booking b1 = seedBooking(loc, Instant.parse("2036-01-01T10:00:00Z"), Instant.parse("2036-01-01T11:00:00Z"), "approved", "Overlap A");
        Booking b2 = seedBooking(loc, Instant.parse("2036-01-01T10:30:00Z"), Instant.parse("2036-01-01T11:30:00Z"), "approved", "Overlap B");

        // No resource lookup needed for location-level conflict
        when(locationsResourcesClient.getResource(any(UUID.class))).thenReturn(Optional.empty());

        MvcResult result = mockMvc.perform(get("/bookings")
                        .with(viewerJwt(VIEWER_ID))
                        .param("from", "2036-01-01T00:00:00Z")
                        .param("to", "2036-01-02T00:00:00Z"))
                .andExpect(status().isOk())  // ALWAYS 200 — read-only, never blocks
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(response.size()).isEqualTo(2);

        // BOTH bookings must carry non-empty conflict_flags (each cites the other)
        JsonNode booking1 = response.get(0).get("title").asText().equals("Overlap A") ? response.get(0) : response.get(1);
        JsonNode booking2 = response.get(0).get("title").asText().equals("Overlap B") ? response.get(0) : response.get(1);

        assertThat(booking1.get("conflict_flags").size()).isGreaterThan(0);
        assertThat(booking2.get("conflict_flags").size()).isGreaterThan(0);

        // Each booking's conflict_flags should cite the OTHER booking
        assertThat(booking1.get("conflict_flags").get(0).get("conflicting_booking_id").asText())
                .isEqualTo(b2.getId().toString());
        assertThat(booking2.get("conflict_flags").get(0).get("conflicting_booking_id").asText())
                .isEqualTo(b1.getId().toString());
    }

    // ── Scenario 7: Soft-deleted → 200 with deleted_at; nonexistent id → 404 ─

    @Test
    void scenario7_getById_softDeleted_returns200WithDeletedAt_nonexistentReturns404() throws Exception {
        UUID loc = UUID.fromString("77777777-0000-0000-0000-000000000001");

        // Create and soft-delete a booking
        Booking b = seedBooking(loc, Instant.parse("2036-02-01T10:00:00Z"), Instant.parse("2036-02-01T11:00:00Z"), "approved", "Soft Delete Test");

        // First: GET before soft-delete → 200, deleted_at null
        MvcResult result1 = mockMvc.perform(get("/bookings/" + b.getId())
                        .with(viewerJwt(VIEWER_ID)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode r1 = objectMapper.readTree(result1.getResponse().getContentAsString());
        assertThat(r1.get("deleted_at").isNull()).isTrue();
        assertThat(r1.get("title").asText()).isEqualTo("Soft Delete Test");

        // Soft-delete directly via repository
        b.setDeletedAt(Instant.now());
        bookingRepository.save(b);
        bookingRepository.flush();

        // Second: GET after soft-delete → 200, deleted_at now populated, all other fields preserved
        MvcResult result2 = mockMvc.perform(get("/bookings/" + b.getId())
                        .with(viewerJwt(VIEWER_ID)))
                .andExpect(status().isOk())  // NOT 404 — last-known-values policy
                .andReturn();
        JsonNode r2 = objectMapper.readTree(result2.getResponse().getContentAsString());
        assertThat(r2.get("deleted_at").isNull()).isFalse();  // deleted_at now populated
        assertThat(r2.get("title").asText()).isEqualTo("Soft Delete Test");  // last-known title preserved
        assertThat(r2.get("conflict_flags").size()).isEqualTo(0);  // no conflict flags for deleted booking

        // Third: GET with a truly nonexistent UUID → 404 BOOKING_NOT_FOUND
        UUID neverExisted = UUID.randomUUID();
        mockMvc.perform(get("/bookings/" + neverExisted)
                        .with(viewerJwt(VIEWER_ID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_code").value("BOOKING_NOT_FOUND"));
    }

    // ── Scenario 8: No booking role → 403 ───────────────────────────────────

    @Test
    void scenario8_noBookingRole_returns403() throws Exception {
        // Caller has NO booking role — a pure "no-role" caller
        var noRoleJwt = jwt()
                .jwt(j -> j
                        .claim("sub", UUID.randomUUID().toString())
                        .claim("realm_access", Map.of("roles", List.of()))); // empty roles

        mockMvc.perform(get("/bookings")
                        .with(noRoleJwt)
                        .param("from", "2036-01-01T00:00:00Z")
                        .param("to", "2036-01-02T00:00:00Z"))
                .andExpect(status().isForbidden());
    }
}
