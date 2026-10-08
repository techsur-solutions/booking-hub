package com.bookinghub.userspermissions.keycloak;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.*;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * WireMock-backed tests for KeycloakAdminService.
 * 
 * Proves the Keycloak integration layer works entirely against WireMock stand-ins
 * with ZERO live Keycloak dependency.
 * 
 * Each test stubs the relevant Keycloak Admin REST API endpoints and asserts
 * the service makes the correct calls.
 */
class KeycloakAdminServiceTest {

    private static WireMockServer wireMockServer;
    private KeycloakAdminService adminService;
    private Keycloak keycloak;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(0); // dynamic port
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

        // Build Keycloak client pointing at WireMock
        String baseUrl = "http://localhost:" + wireMockServer.port();
        
        // Stub token endpoint for client_credentials grant (Keycloak admin client authenticates)
        stubFor(post(urlEqualTo("/realms/bookinghub/protocol/openid-connect/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"access_token\":\"test-token\",\"expires_in\":300,\"token_type\":\"Bearer\"}")));
        
        keycloak = KeycloakBuilder.builder()
                .serverUrl(baseUrl)
                .realm("bookinghub")
                .clientId("test-admin-client")
                .clientSecret("test-secret")
                .grantType("client_credentials")
                .build();

        adminService = new KeycloakAdminService(keycloak, "bookinghub");
    }

    @Test
    @DisplayName("createUser returns user ID from Location header and sets temporary password")
    void createUser_returnsUserIdAndSetsTemporaryPassword() {
        String fakeUserId = "test-user-id-123";

        // Stub user creation → 201 with Location header
        stubFor(post(urlEqualTo("/admin/realms/bookinghub/users"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Location", "http://localhost/users/" + fakeUserId)));

        // Stub password reset
        stubFor(put(urlEqualTo("/admin/realms/bookinghub/users/" + fakeUserId + "/reset-password"))
                .willReturn(aResponse().withStatus(204)));

        // Stub role fetch
        stubFor(get(urlEqualTo("/admin/realms/bookinghub/roles/role_user"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"role-uuid\",\"name\":\"role_user\"}")));

        // Stub role assignment
        stubFor(post(urlEqualTo("/admin/realms/bookinghub/users/" + fakeUserId + "/role-mappings/realm"))
                .willReturn(aResponse().withStatus(204)));

        // Execute
        String userId = adminService.createUser("test@example.com", "password123", true, "role_user");

        // Assert
        assertEquals(fakeUserId, userId);

        // Verify password reset was called with temporary=true
        verify(putRequestedFor(urlEqualTo("/admin/realms/bookinghub/users/" + fakeUserId + "/reset-password"))
                .withRequestBody(containing("\"temporary\":true")));
    }

    @Test
    @DisplayName("createUser throws UserAlreadyExistsException on 409")
    void createUser_throws409() {
        stubFor(post(urlEqualTo("/admin/realms/bookinghub/users"))
                .willReturn(aResponse().withStatus(409)));

        assertThrows(UserAlreadyExistsException.class, () ->
                adminService.createUser("existing@example.com", "password", false, "role_user"));
    }

    @Test
    @DisplayName("resetPassword calls Keycloak reset-password endpoint")
    void resetPassword_callsKeycloakEndpoint() {
        String userId = "test-user-id";

        stubFor(put(urlEqualTo("/admin/realms/bookinghub/users/" + userId + "/reset-password"))
                .willReturn(aResponse().withStatus(204)));

        adminService.resetPassword(userId, "newPassword456", false);

        verify(putRequestedFor(urlEqualTo("/admin/realms/bookinghub/users/" + userId + "/reset-password"))
                .withRequestBody(containing("\"temporary\":false")));
    }

    @Test
    @DisplayName("updateUserRealmRoles removes old roles and adds new ones")
    void updateUserRealmRoles_replacesRoleSet() {
        String userId = "test-user-id";

        // Stub GET current roles
        stubFor(get(urlEqualTo("/admin/realms/bookinghub/users/" + userId + "/role-mappings/realm"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":\"old-role-id\",\"name\":\"old_role\"}]")));

        // Stub DELETE old roles
        stubFor(delete(urlEqualTo("/admin/realms/bookinghub/users/" + userId + "/role-mappings/realm"))
                .willReturn(aResponse().withStatus(204)));

        // Stub GET new role representations
        stubFor(get(urlEqualTo("/admin/realms/bookinghub/roles/new_role"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"new-role-id\",\"name\":\"new_role\"}")));

        // Stub POST new roles
        stubFor(post(urlEqualTo("/admin/realms/bookinghub/users/" + userId + "/role-mappings/realm"))
                .willReturn(aResponse().withStatus(204)));

        adminService.updateUserRealmRoles(userId, List.of("new_role"));

        // Verify both DELETE (removal) and POST (addition) were called
        verify(deleteRequestedFor(urlEqualTo("/admin/realms/bookinghub/users/" + userId + "/role-mappings/realm")));
        verify(postRequestedFor(urlEqualTo("/admin/realms/bookinghub/users/" + userId + "/role-mappings/realm")));
    }

    @Test
    @DisplayName("logoutUser calls Keycloak logout endpoint")
    void logoutUser_callsKeycloakLogout() {
        String userId = "test-user-id";

        stubFor(post(urlEqualTo("/admin/realms/bookinghub/users/" + userId + "/logout"))
                .willReturn(aResponse().withStatus(204)));

        adminService.logoutUser(userId);

        verify(postRequestedFor(urlEqualTo("/admin/realms/bookinghub/users/" + userId + "/logout")));
    }

    @Test
    @DisplayName("findUserIdByEmail returns user ID when found")
    void findUserIdByEmail_returnsUserIdWhenFound() {
        String email = "found@example.com";
        String userId = "found-user-id";

        stubFor(get(urlMatching("/admin/realms/bookinghub/users.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":\"" + userId + "\",\"email\":\"" + email + "\"}]")));

        var result = adminService.findUserIdByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get());
    }

    @Test
    @DisplayName("findUserIdByEmail returns empty Optional when not found")
    void findUserIdByEmail_returnsEmptyWhenNotFound() {
        String email = "notfound@example.com";

        stubFor(get(urlMatching("/admin/realms/bookinghub/users.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")));

        var result = adminService.findUserIdByEmail(email);

        assertFalse(result.isPresent());
    }
}
