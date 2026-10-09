package com.bookinghub.booking.client;

import com.bookinghub.booking.client.dto.ClientDtos.LocationResponse;
import com.bookinghub.booking.client.dto.ClientDtos.ResourceResponse;
import com.bookinghub.booking.security.CurrentUserProvider;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.jupiter.api.*;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * WireMock-backed tests for LocationsResourcesClient (plan 05-02).
 *
 * No Spring context needed — RestClient is instantiated directly against the
 * WireMock server. CurrentUserProvider is mocked to return a fixed token,
 * allowing the token-relay assertion to verify the exact Authorization header
 * the client sends.
 *
 * Scenarios:
 * 1. GET /locations/{id} → 200 with soft-deleted body (deletedAt populated) →
 *    assert Optional is PRESENT (not empty) — proving last-known-values-not-404
 * 2. GET /locations/{id} → 404 → assert Optional.empty()
 * 3. Token relay: assert the Authorization header sent to WireMock matches
 *    the token CurrentUserProvider.getRawBearerToken() returned
 * 4. GET /resources/{id} → 200 with isUnique=true → assert parsed correctly
 */
class LocationsResourcesClientTest {

    private static WireMockServer wireMock;
    private static final String TEST_TOKEN = "test-bearer-token-12345";

    private LocationsResourcesClient client;

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

        client = new LocationsResourcesClient(restClient, mockUserProvider);
    }

    // ── Scenario 1: Soft-deleted location returns Optional.present (not empty) ──

    @Test
    void getLocation_softDeleted_returnsPopulatedOptional() {
        UUID locationId = UUID.randomUUID();
        stubFor(get(urlPathEqualTo("/locations/" + locationId))
                .willReturn(okJson("""
                        {
                            "id": "%s",
                            "name": "Conference Room A",
                            "deletedAt": "2030-01-01T00:00:00Z"
                        }
                        """.formatted(locationId))));

        Optional<LocationResponse> result = client.getLocation(locationId);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(locationId);
        assertThat(result.get().name()).isEqualTo("Conference Room A");
        assertThat(result.get().deletedAt()).isNotNull();
    }

    // ── Scenario 2: 404 → Optional.empty() ───────────────────────────────────

    @Test
    void getLocation_notFound_returnsEmpty() {
        UUID locationId = UUID.randomUUID();
        stubFor(get(urlPathEqualTo("/locations/" + locationId))
                .willReturn(aResponse().withStatus(404)));

        Optional<LocationResponse> result = client.getLocation(locationId);

        assertThat(result).isEmpty();
    }

    // ── Scenario 3: Token relay — Authorization header carries relayed token ──

    @Test
    void getLocation_relaysCallerBearerToken() {
        UUID locationId = UUID.randomUUID();
        stubFor(get(urlPathEqualTo("/locations/" + locationId))
                .willReturn(okJson("""
                        {"id": "%s", "name": "Room B", "deletedAt": null}
                        """.formatted(locationId))));

        client.getLocation(locationId);

        // Verify the request WireMock received carried the exact relayed token
        List<LoggedRequest> requests = wireMock.findAll(getRequestedFor(urlPathEqualTo("/locations/" + locationId)));
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getHeader("Authorization"))
                .isEqualTo("Bearer " + TEST_TOKEN);
    }

    // ── Scenario 4: getResource parses isUnique=true correctly ────────────────

    @Test
    void getResource_withIsUniqueTrue_parsedCorrectly() {
        UUID resourceId = UUID.randomUUID();
        stubFor(get(urlPathEqualTo("/resources/" + resourceId))
                .willReturn(okJson("""
                        {
                            "id": "%s",
                            "name": "Projector",
                            "isUnique": true,
                            "restrictLocations": [],
                            "deletedAt": null
                        }
                        """.formatted(resourceId))));

        Optional<ResourceResponse> result = client.getResource(resourceId);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(resourceId);
        assertThat(result.get().isUnique()).isTrue();
        assertThat(result.get().restrictLocations()).isEmpty();
        assertThat(result.get().deletedAt()).isNull();
    }

    // ── Scenario 5: getResource 404 → Optional.empty() ───────────────────────

    @Test
    void getResource_notFound_returnsEmpty() {
        UUID resourceId = UUID.randomUUID();
        stubFor(get(urlPathEqualTo("/resources/" + resourceId))
                .willReturn(aResponse().withStatus(404)));

        Optional<ResourceResponse> result = client.getResource(resourceId);

        assertThat(result).isEmpty();
    }
}
