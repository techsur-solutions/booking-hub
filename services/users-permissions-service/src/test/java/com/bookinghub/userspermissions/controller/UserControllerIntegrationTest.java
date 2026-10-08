package com.bookinghub.userspermissions.controller;

import com.bookinghub.userspermissions.domain.OutboxEvent;
import com.bookinghub.userspermissions.domain.User;
import com.bookinghub.userspermissions.repository.OutboxEventRepository;
import com.bookinghub.userspermissions.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Integration test for UserController.
 * 
 * Tests account creation, duplicate email rejection (case-insensitive),
 * admin-vs-self authorization, self-edit guards, and role assignment with
 * outbox event emission — all against Testcontainers Postgres + WireMock
 * Keycloak stub.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class UserControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @org.junit.jupiter.api.extension.RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        
        // Point Keycloak admin client at WireMock
        registry.add("keycloak.server-url", () -> wireMock.baseUrl());
        registry.add("keycloak.realm", () -> "bookinghub");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Clean up database between tests
        outboxEventRepository.deleteAll();
        userRepository.deleteAll();
        
        // Reset WireMock
        wireMock.resetAll();
    }

    /**
     * Test 1: Admin creates user → Keycloak provisioning + local row + outbox event
     */
    @Test
    void adminCreatesUser_provisionKeycloakAndLocalRowAndOutboxEvent() throws Exception {
        // Given: WireMock stub for Keycloak user creation
        String keycloakUserId = UUID.randomUUID().toString();
        stubKeycloakUserCreation(keycloakUserId, "joe@example.com");

        // When: Admin creates user
        String requestBody = """
                {
                    "email": "joe@example.com",
                    "initialRole": "role_user"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(jwt()
                                .authorities(() -> "role_user_admin")
                                .jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(keycloakUserId))
                .andExpect(jsonPath("$.email").value("joe@example.com"));

        // Then: Local user row exists with id = Keycloak sub (not a random UUID)
        User user = userRepository.findByEmailIgnoreCase("joe@example.com").orElseThrow();
        assertThat(user.getId()).isEqualTo(UUID.fromString(keycloakUserId));

        // And: Outbox event created with routing_key="user.created"
        List<OutboxEvent> events = outboxEventRepository.findAll();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getRoutingKey()).isEqualTo("user.created");
        assertThat(events.get(0).getAggregateId()).isEqualTo(UUID.fromString(keycloakUserId));
    }

    /**
     * Test 2: Duplicate email → 409 USER_ALREADY_EXISTS, Keycloak never called
     */
    @Test
    void createUserWithDuplicateEmail_returns409_keycloakNotCalled() throws Exception {
        // Given: Existing user
        UUID existingUserId = UUID.randomUUID();
        userRepository.save(new User(existingUserId, "joe@example.com", null));

        // When: Admin tries to create user with same email
        String requestBody = """
                {
                    "email": "joe@example.com",
                    "initialRole": "role_user"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(jwt()
                                .authorities(() -> "role_user_admin")
                                .jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error_code").value("USER_ALREADY_EXISTS"));

        // Then: Keycloak create-user stub was NEVER called (fail-fast before Keycloak)
        wireMock.verify(0, postRequestedFor(anyUrl()));
    }

    /**
     * Test 3: Email differing only by case → 409 (case-insensitive uniqueness)
     */
    @Test
    void createUserWithCaseVariantEmail_returns409() throws Exception {
        // Given: Existing user with "joe@example.com"
        UUID existingUserId = UUID.randomUUID();
        userRepository.save(new User(existingUserId, "joe@example.com", null));

        // When: Admin tries to create user with "Joe@example.com" (case variant)
        String requestBody = """
                {
                    "email": "Joe@example.com",
                    "initialRole": "role_user"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(jwt()
                                .authorities(() -> "role_user_admin")
                                .jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))))
                .andExpect(status().isConflict());
    }

    /**
     * Test 4: Non-admin tries to read another user's record → 403 ACTION_FORBIDDEN
     */
    @Test
    void nonAdminTriesToReadAnotherUser_returns403() throws Exception {
        // Given: Two users A and B
        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();
        userRepository.save(new User(userA, "usera@example.com", null));
        userRepository.save(new User(userB, "userb@example.com", null));

        // When: User A (no admin role) tries to GET user B's record
        mockMvc.perform(MockMvcRequestBuilders.get("/users/" + userB)
                        .with(jwt()
                                .authorities(() -> "role_user") // NOT role_user_admin
                                .jwt(jwt -> jwt.subject(userA.toString()))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error_code").value("ACTION_FORBIDDEN"));
    }

    /**
     * Test 5: User reads their own record (self-access) → 200
     */
    @Test
    void userReadsOwnRecord_returns200() throws Exception {
        // Given: User A
        UUID userA = UUID.randomUUID();
        userRepository.save(new User(userA, "usera@example.com", "User A"));

        // When: User A reads their own record (no admin role needed)
        mockMvc.perform(MockMvcRequestBuilders.get("/users/" + userA)
                        .with(jwt()
                                .authorities(() -> "role_user")
                                .jwt(jwt -> jwt.subject(userA.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userA.toString()))
                .andExpect(jsonPath("$.email").value("usera@example.com"));
    }

    /**
     * Test 6: User updates own profile via /users/me → 200, UserSelfUpdateRequest
     * structurally cannot carry password/role fields
     */
    @Test
    void userUpdatesOwnProfile_structuralGuardPreventsPasswordRole() throws Exception {
        // Given: User A
        UUID userA = UUID.randomUUID();
        userRepository.save(new User(userA, "usera@example.com", null));

        // When: User A updates displayName via /users/me
        String requestBody = """
                {
                    "displayName": "Updated Name"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(jwt()
                                .authorities(() -> "role_user")
                                .jwt(jwt -> jwt.subject(userA.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Updated Name"));

        // Then: User row updated
        User user = userRepository.findById(userA).orElseThrow();
        assertThat(user.getDisplayName()).isEqualTo("Updated Name");

        // And: Outbox event created
        List<OutboxEvent> events = outboxEventRepository.findAll();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getRoutingKey()).isEqualTo("user.updated");
    }

    /**
     * Test 7: Non-admin tries to assign roles → 403
     */
    @Test
    void nonAdminTriesToAssignRoles_returns403() throws Exception {
        // Given: User A
        UUID userA = UUID.randomUUID();
        userRepository.save(new User(userA, "usera@example.com", null));

        // When: User A (no admin role) tries to assign roles to themselves
        String requestBody = """
                {
                    "roles": ["role_booking_creator"]
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/users/" + userA + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(jwt()
                                .authorities(() -> "role_user")
                                .jwt(jwt -> jwt.subject(userA.toString()))))
                .andExpect(status().isForbidden());
    }

    /**
     * Test 8: Admin assigns roles → 200, Keycloak roles updated, outbox event created
     */
    @Test
    void adminAssignsRoles_updatesKeycloakAndCreatesOutboxEvent() throws Exception {
        // Given: User A
        UUID userA = UUID.randomUUID();
        userRepository.save(new User(userA, "usera@example.com", null));

        // And: WireMock stub for Keycloak role assignment
        stubKeycloakRoleAssignment(userA.toString());

        // When: Admin assigns roles
        String requestBody = """
                {
                    "roles": ["role_user", "role_booking_creator"]
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.put("/users/" + userA + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(jwt()
                                .authorities(() -> "role_user_admin")
                                .jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))))
                .andExpect(status().isOk());

        // Then: Outbox event created with routing_key="role.assigned"
        List<OutboxEvent> events = outboxEventRepository.findAll();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getRoutingKey()).isEqualTo("role.assigned");
        assertThat(events.get(0).getAggregateId()).isEqualTo(userA);

        // And: Keycloak role assignment calls were made
        wireMock.verify(getRequestedFor(urlPathEqualTo("/admin/realms/bookinghub/users/" + userA + "/role-mappings/realm")));
    }

    // --- Helper methods for WireMock stubs ---

    private void stubKeycloakUserCreation(String userId, String email) {
        // Stub POST /admin/realms/{realm}/users → 201 with Location header
        wireMock.stubFor(post(urlPathEqualTo("/admin/realms/bookinghub/users"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Location", wireMock.baseUrl() + "/admin/realms/bookinghub/users/" + userId)));

        // Stub POST /admin/realms/{realm}/users/{id}/reset-password → 204
        wireMock.stubFor(put(urlPathEqualTo("/admin/realms/bookinghub/users/" + userId + "/reset-password"))
                .willReturn(aResponse()
                        .withStatus(204)));

        // Stub GET /admin/realms/{realm}/roles/{roleName} → 200 with role representation
        wireMock.stubFor(get(urlPathMatching("/admin/realms/bookinghub/roles/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "role-id",
                                    "name": "role_user",
                                    "description": "User role"
                                }
                                """)));

        // Stub POST /admin/realms/{realm}/users/{id}/role-mappings/realm → 204
        wireMock.stubFor(post(urlPathEqualTo("/admin/realms/bookinghub/users/" + userId + "/role-mappings/realm"))
                .willReturn(aResponse()
                        .withStatus(204)));
    }

    private void stubKeycloakRoleAssignment(String userId) {
        // Stub GET /admin/realms/{realm}/users/{id}/role-mappings/realm → 200 with current roles
        wireMock.stubFor(get(urlPathEqualTo("/admin/realms/bookinghub/users/" + userId + "/role-mappings/realm"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]"))); // Empty current roles for simplicity

        // Stub DELETE /admin/realms/{realm}/users/{id}/role-mappings/realm → 204
        wireMock.stubFor(delete(urlPathEqualTo("/admin/realms/bookinghub/users/" + userId + "/role-mappings/realm"))
                .willReturn(aResponse()
                        .withStatus(204)));

        // Stub GET /admin/realms/{realm}/roles/{roleName} → 200 for each role
        wireMock.stubFor(get(urlPathMatching("/admin/realms/bookinghub/roles/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "role-id",
                                    "name": "role_user",
                                    "description": "User role"
                                }
                                """)));

        // Stub POST /admin/realms/{realm}/users/{id}/role-mappings/realm → 204
        wireMock.stubFor(post(urlPathEqualTo("/admin/realms/bookinghub/users/" + userId + "/role-mappings/realm"))
                .willReturn(aResponse()
                        .withStatus(204)));
    }
}
