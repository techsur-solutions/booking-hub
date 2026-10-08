package com.bookinghub.userspermissions.keycloak;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.*;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * WireMock-backed tests for KeycloakTokenClient.
 * 
 * Proves the token client works entirely against WireMock with no live Keycloak dependency.
 * 
 * Tests the direct-grant flow using both standard and remember-me client credentials,
 * and verifies error handling for invalid_grant responses.
 */
class KeycloakTokenClientTest {

    private static WireMockServer wireMockServer;
    private KeycloakTokenClient tokenClient;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setup() {
        wireMockServer.resetAll();

        String baseUrl = "http://localhost:" + wireMockServer.port();
        tokenClient = new KeycloakTokenClient(
                RestClient.builder(),
                baseUrl,
                "bookinghub",
                "userperm-direct-grant-client",
                "direct-secret",
                "userperm-direct-grant-remember-client",
                "remember-secret"
        );
    }

    @Test
    @DisplayName("directGrantLogin with rememberMe=false uses standard client")
    void directGrantLogin_withoutRememberMe_usesStandardClient() {
        stubFor(post(urlEqualTo("/realms/bookinghub/protocol/openid-connect/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "access_token": "eyJhbGc...",
                                    "refresh_token": "eyJhbGc...",
                                    "expires_in": 300
                                }
                                """)));

        var response = tokenClient.directGrantLogin("user@example.com", "password123", false);

        assertNotNull(response);
        assertEquals("eyJhbGc...", response.accessToken());
        assertEquals("eyJhbGc...", response.refreshToken());
        assertEquals(300, response.expiresIn());

        // Verify request body contained standard client_id
        verify(postRequestedFor(urlEqualTo("/realms/bookinghub/protocol/openid-connect/token"))
                .withRequestBody(containing("client_id=userperm-direct-grant-client")));
    }

    @Test
    @DisplayName("directGrantLogin with rememberMe=true uses remember-me client")
    void directGrantLogin_withRememberMe_usesRememberClient() {
        stubFor(post(urlEqualTo("/realms/bookinghub/protocol/openid-connect/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "access_token": "eyJhbGc...",
                                    "refresh_token": "eyJhbGc...",
                                    "expires_in": 2592000
                                }
                                """)));

        var response = tokenClient.directGrantLogin("user@example.com", "password123", true);

        assertNotNull(response);
        assertEquals(2592000, response.expiresIn()); // 30 days

        // Verify request body contained remember-me client_id
        verify(postRequestedFor(urlEqualTo("/realms/bookinghub/protocol/openid-connect/token"))
                .withRequestBody(containing("client_id=userperm-direct-grant-remember-client")));
    }

    @Test
    @DisplayName("directGrantLogin throws AuthInvalidCredentialsException on invalid_grant")
    void directGrantLogin_throwsOnInvalidGrant() {
        stubFor(post(urlEqualTo("/realms/bookinghub/protocol/openid-connect/token"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "error": "invalid_grant",
                                    "error_description": "Invalid user credentials"
                                }
                                """)));

        assertThrows(AuthInvalidCredentialsException.class, () ->
                tokenClient.directGrantLogin("user@example.com", "wrongpassword", false));
    }

    @Test
    @DisplayName("verifyCurrentPassword returns true on valid password")
    void verifyCurrentPassword_returnsTrueOnValidPassword() {
        stubFor(post(urlEqualTo("/realms/bookinghub/protocol/openid-connect/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "access_token": "eyJhbGc...",
                                    "refresh_token": "eyJhbGc...",
                                    "expires_in": 300
                                }
                                """)));

        boolean result = tokenClient.verifyCurrentPassword("user@example.com", "correctpassword");

        assertTrue(result);
    }

    @Test
    @DisplayName("verifyCurrentPassword returns false on invalid_grant (not thrown exception)")
    void verifyCurrentPassword_returnsFalseOnInvalidGrant() {
        stubFor(post(urlEqualTo("/realms/bookinghub/protocol/openid-connect/token"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "error": "invalid_grant",
                                    "error_description": "Invalid user credentials"
                                }
                                """)));

        boolean result = tokenClient.verifyCurrentPassword("user@example.com", "wrongpassword");

        assertFalse(result); // Returns false, does NOT throw exception
    }
}
