package com.bookinghub.booking.client;

import com.bookinghub.booking.client.dto.ClientDtos.CustomFieldDefinition;
import com.bookinghub.booking.security.CurrentUserProvider;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.jupiter.api.*;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * WireMock-backed tests for CustomFieldClient (plan 05-02).
 *
 * Scenarios:
 * 1. Normal case (200) — returns field definitions correctly, token relay proven
 *    (the EXPECTED case after plan 04-04's role_calendar_viewer carve-out fix)
 * 2. Connectivity failure (5xx) → returns empty list (graceful degradation fallback)
 * 3. 403 response → returns empty list (rare mis-provisioning edge case, still handled)
 */
class CustomFieldClientTest {

    private static WireMockServer wireMock;
    private static final String TEST_TOKEN = "customfield-test-token";

    private CustomFieldClient client;

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

        client = new CustomFieldClient(restClient, mockUserProvider);
    }

    // ── Scenario 1: Normal 200 — returns field definitions + token relay ──────

    @Test
    void normalCase_returnsFieldDefinitionsAndRelaysToken() {
        UUID contextId = UUID.randomUUID();
        stubFor(get(urlPathEqualTo("/custom-fields"))
                .withQueryParam("context_id", equalTo(contextId.toString()))
                .willReturn(okJson("""
                        [
                            {"id": "11111111-1111-1111-1111-111111111111", "label": "Purpose", "fieldType": "textfield"},
                            {"id": "22222222-2222-2222-2222-222222222222", "label": "Department", "fieldType": "select"}
                        ]
                        """)));

        List<CustomFieldDefinition> result = client.getApplicableFields(contextId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).label()).isEqualTo("Purpose");
        assertThat(result.get(0).fieldType()).isEqualTo("textfield");
        assertThat(result.get(1).label()).isEqualTo("Department");
        assertThat(result.get(1).fieldType()).isEqualTo("select");

        // Verify token relay
        List<LoggedRequest> requests = wireMock.findAll(
                getRequestedFor(urlPathEqualTo("/custom-fields")));
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getHeader("Authorization"))
                .isEqualTo("Bearer " + TEST_TOKEN);
    }

    // ── Scenario 2: Connectivity failure → empty list (graceful degradation) ──

    @Test
    void connectivityFailure_returnsEmptyListGracefully() {
        UUID contextId = UUID.randomUUID();
        stubFor(get(urlPathEqualTo("/custom-fields"))
                .withQueryParam("context_id", equalTo(contextId.toString()))
                .willReturn(aResponse().withStatus(503)));

        List<CustomFieldDefinition> result = client.getApplicableFields(contextId);

        assertThat(result).isEmpty();
    }

    // ── Scenario 3: 403 → empty list (rare mis-provisioning edge case) ────────

    @Test
    void forbidden_returnsEmptyListGracefully() {
        UUID contextId = UUID.randomUUID();
        stubFor(get(urlPathEqualTo("/custom-fields"))
                .withQueryParam("context_id", equalTo(contextId.toString()))
                .willReturn(aResponse().withStatus(403)));

        List<CustomFieldDefinition> result = client.getApplicableFields(contextId);

        assertThat(result).isEmpty();
    }
}
