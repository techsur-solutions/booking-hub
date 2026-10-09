package com.bookinghub.booking.client;

import com.bookinghub.booking.error.ApprovalSettingsUnavailableException;
import com.bookinghub.booking.security.CurrentUserProvider;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.*;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * WireMock-backed tests for SettingsClient (plan 05-02).
 *
 * Scenarios:
 * 1. First call hits WireMock stub (cache miss) → returns approveBooking flag correctly
 * 2. Second call within 5 seconds does NOT hit WireMock again (cache hit) — proven
 *    by WireMock request count assertion (stub was only called once)
 * 3. Force cache expiry → third call DOES re-hit WireMock (cache miss after expiry)
 * 4. Connection failure (WireMock returns connection refused / 500) →
 *    ApprovalSettingsUnavailableException is thrown
 */
class SettingsClientTest {

    private static WireMockServer wireMock;
    private static final String TEST_TOKEN = "settings-test-token";

    private SettingsClient client;

    @BeforeAll
    static void startWireMock() {
        wireMock = new WireMockServer(0);
        wireMock.start();
        WireMock.configureFor("localhost", wireMock.port());
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @BeforeEach
    void setUp() {
        wireMock.resetAll();

        CurrentUserProvider mockUserProvider = mock(CurrentUserProvider.class);
        when(mockUserProvider.getRawBearerToken()).thenReturn(TEST_TOKEN);

        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + wireMock.port())
                .build();

        client = new SettingsClient(restClient, mockUserProvider);
    }

    // ── Scenario 1: First call hits WireMock (cache miss) ────────────────────

    @Test
    void firstCall_hitsDownstreamAndReturnsApproveBookingFlag() {
        stubFor(get(urlPathEqualTo("/settings"))
                .willReturn(okJson("""
                        {
                            "approveBooking": true,
                            "calendarSlotSize": 30,
                            "calendarMinTime": "08:00",
                            "calendarMaxTime": "18:00"
                        }
                        """)));

        boolean result = client.getApproveBookingFlag();

        assertThat(result).isTrue();
        verify(1, getRequestedFor(urlPathEqualTo("/settings")));
    }

    // ── Scenario 2: Second call within TTL uses cache (no second WireMock hit) ─

    @Test
    void secondCallWithinTtl_usesCacheNoSecondHttpCall() {
        stubFor(get(urlPathEqualTo("/settings"))
                .willReturn(okJson("""
                        {
                            "approveBooking": false,
                            "calendarSlotSize": 15,
                            "calendarMinTime": "07:00",
                            "calendarMaxTime": "20:00"
                        }
                        """)));

        // First call — populates cache
        boolean first = client.getApproveBookingFlag();
        // Second call — immediately after (well within 5-second TTL)
        boolean second = client.getApproveBookingFlag();

        assertThat(first).isFalse();
        assertThat(second).isFalse();
        // WireMock was only called ONCE — the cache served the second call
        verify(1, getRequestedFor(urlPathEqualTo("/settings")));
    }

    // ── Scenario 3: After cache expiry, third call re-fetches ─────────────────

    @Test
    void afterCacheExpiry_nextCallRefetchesFromDownstream() {
        stubFor(get(urlPathEqualTo("/settings"))
                .willReturn(okJson("""
                        {
                            "approveBooking": true,
                            "calendarSlotSize": 30,
                            "calendarMinTime": "08:00",
                            "calendarMaxTime": "18:00"
                        }
                        """)));

        // Populate cache
        client.getApproveBookingFlag();
        verify(1, getRequestedFor(urlPathEqualTo("/settings")));

        // Expire the cache via the package-visible test hook
        client.expireCache();

        // Next call should re-fetch (cache expired)
        client.getApproveBookingFlag();
        verify(2, getRequestedFor(urlPathEqualTo("/settings")));
    }

    // ── Scenario 4: Connection failure → ApprovalSettingsUnavailableException ──

    @Test
    void connectionFailure_throwsApprovalSettingsUnavailableException() {
        // Stub WireMock to return a 503 (simulates settings-service being down)
        stubFor(get(urlPathEqualTo("/settings"))
                .willReturn(aResponse().withStatus(503)));

        assertThatThrownBy(() -> client.getApproveBookingFlag())
                .isInstanceOf(ApprovalSettingsUnavailableException.class);
    }
}
