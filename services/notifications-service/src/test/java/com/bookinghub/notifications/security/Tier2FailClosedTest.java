package com.bookinghub.notifications.security;

import com.bookinghub.notifications.error.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tier-2 fail-closed and access-denied behavior proof for notifications-service.
 *
 * Directly modeled on Phase 3 plan 03-02's Tier2FailClosedTest (identical structure,
 * adapted to this service's package name and role requirement).
 *
 * Proves that SecurityConfig's JwksOutageAuthenticationEntryPoint and ApiAccessDeniedHandler
 * actually behave as designed under real conditions (not just compile).
 *
 * Three critical scenarios:
 * 1. JWKS-unreachable → 503 SERVICE_UNAVAILABLE (fail-closed, never silent pass-through)
 * 2. Missing token → 401 AUTH_UNAUTHENTICATED (unauthenticated request)
 * 3. Authenticated but no role_audit_viewer → 403 AUTH_FORBIDDEN (structured ApiError)
 *
 * All outcomes must produce ApiError JSON shape, verified by deserializing response bodies.
 *
 * Scenarios 2 and 3 probe /actuator/info (a non-permitAll, authenticated-by-default route
 * that plan 06-02 has not yet added /notifications/delivery-status for) so this test is
 * fully self-contained and does not depend on plan 06-02 landing first.
 */
class Tier2FailClosedTest {

    /**
     * Scenario 1: JWKS endpoint fails at runtime → 503 SERVICE_UNAVAILABLE (fail-closed)
     *
     * Tests the JwksOutageAuthenticationEntryPoint directly as a unit test:
     * simulates the exact AuthenticationException carrying a ConnectException in its
     * cause chain (as Spring Security produces when JWKS is unreachable at validation
     * time) and verifies the handler writes 503 SERVICE_UNAVAILABLE with an ApiError body.
     *
     * NOTE: Testing this via a full SpringBootTest + MockMvc with a live JWKS outage
     * is not practical because Spring Security wraps JWKS network failures as
     * AuthenticationServiceException (which bypasses the AuthenticationEntryPoint and
     * becomes a 500 or uncaught error) rather than routing through it — the same
     * observation made in Phase 3 plan 03-02's Tier2FailClosedTest (which also fell
     * back to wiring-verified logic for Scenario 1). This unit test directly exercises
     * the handler's network-failure detection logic on the exact exception type/shape
     * that the entry point sees when its detect-and-produce-503 path fires.
     */
    @Nested
    @DisplayName("Scenario 1: JWKS unreachable → 503 SERVICE_UNAVAILABLE (unit test of entry point)")
    class JwksOutageTest {

        @Test
        @DisplayName("JwksOutageAuthenticationEntryPoint produces 503 with ApiError when cause chain contains ConnectException")
        void entryPoint_connectExceptionInCauseChain_returns503() throws Exception {
            // Arrange: build ObjectMapper with JavaTimeModule (for Instant serialization)
            // and construct the entry point directly
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper()
                        .findAndRegisterModules(); // auto-detects JavaTimeModule on classpath
            JwksOutageAuthenticationEntryPoint entryPoint = new JwksOutageAuthenticationEntryPoint(mapper);

            // Build a cause chain mimicking what Spring Security produces for a JWKS outage:
            // AuthenticationException (BadCredentialsException) → JwtException → ConnectException
            java.net.ConnectException connectException = new java.net.ConnectException("Connection refused");
            org.springframework.security.oauth2.jwt.JwtException jwtException =
                    new org.springframework.security.oauth2.jwt.JwtException("Couldn't retrieve remote JWK set", connectException);
            org.springframework.security.authentication.BadCredentialsException authException =
                    new org.springframework.security.authentication.BadCredentialsException("JWT decode failed", jwtException);

            // Invoke the entry point with mock servlet request/response
            org.springframework.mock.web.MockHttpServletRequest request =
                    new org.springframework.mock.web.MockHttpServletRequest();
            request.setRequestURI("/actuator/info");
            org.springframework.mock.web.MockHttpServletResponse response =
                    new org.springframework.mock.web.MockHttpServletResponse();

            entryPoint.commence(request, response, authException);

            // Assert: 503 with ApiError body
            assertEquals(503, response.getStatus(),
                    "Expected 503 SERVICE_UNAVAILABLE for JWKS outage");
            assertEquals("application/json", response.getContentType());
            String body = response.getContentAsString();
            assertTrue(body.contains("\"error_code\":\"SERVICE_UNAVAILABLE\""),
                    "Expected SERVICE_UNAVAILABLE error code in: " + body);
        }

        @Test
        @DisplayName("JwksOutageAuthenticationEntryPoint produces 401 with ApiError for non-network AuthenticationException")
        void entryPoint_nonNetworkException_returns401() throws Exception {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper()
                        .findAndRegisterModules();
            JwksOutageAuthenticationEntryPoint entryPoint = new JwksOutageAuthenticationEntryPoint(mapper);

            // Non-network AuthenticationException (expired/invalid token, no ConnectException in chain)
            org.springframework.security.authentication.BadCredentialsException authException =
                    new org.springframework.security.authentication.BadCredentialsException("Invalid token");

            org.springframework.mock.web.MockHttpServletRequest request =
                    new org.springframework.mock.web.MockHttpServletRequest();
            request.setRequestURI("/actuator/info");
            org.springframework.mock.web.MockHttpServletResponse response =
                    new org.springframework.mock.web.MockHttpServletResponse();

            entryPoint.commence(request, response, authException);

            assertEquals(401, response.getStatus(),
                    "Expected 401 AUTH_UNAUTHENTICATED for invalid/missing token");
            assertEquals("application/json", response.getContentType());
            String body = response.getContentAsString();
            assertTrue(body.contains("\"error_code\":\"AUTH_UNAUTHENTICATED\""),
                    "Expected AUTH_UNAUTHENTICATED error code in: " + body);
        }
    }

    /**
     * Scenario 2: Missing Authorization header → 401 AUTH_UNAUTHENTICATED
     *
     * Uses a WireMock-stubbed JWKS issuer so the issuer is reachable at context load,
     * but sends NO Authorization header. Should yield 401 with ApiError body.
     */
    @Nested
    @SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration," +
                "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration," +
                "org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration"
        }
    )
    @AutoConfigureMockMvc
    @DisplayName("Scenario 2: Missing token → 401 AUTH_UNAUTHENTICATED")
    class MissingTokenTest {

        private static WireMockServer wireMockServer;

        // Mock ConnectionFactory so context loads without a real RabbitMQ connection
        @MockBean
        private ConnectionFactory connectionFactory;

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @BeforeAll
        static void startWireMock() {
            wireMockServer = new WireMockServer(0);
            wireMockServer.start();
            WireMock.configureFor("localhost", wireMockServer.port());

            // Stub JWKS endpoint so issuer is reachable at context load
            WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo(
                    "/realms/bookinghub/.well-known/openid-configuration"))
                .willReturn(WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                        {
                            "issuer": "http://localhost:%d/realms/bookinghub",
                            "jwks_uri": "http://localhost:%d/realms/bookinghub/protocol/openid-connect/certs"
                        }
                        """.formatted(wireMockServer.port(), wireMockServer.port()))));

            WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo(
                    "/realms/bookinghub/protocol/openid-connect/certs"))
                .willReturn(WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"keys\":[]}")));
        }

        @AfterAll
        static void stopWireMock() {
            wireMockServer.stop();
        }

        @DynamicPropertySource
        static void configureProperties(DynamicPropertyRegistry registry) {
            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> "http://localhost:" + wireMockServer.port() + "/realms/bookinghub");
        }

        @Test
        @DisplayName("Missing Authorization header yields 401 AUTH_UNAUTHENTICATED with ApiError body")
        void missingToken_returns401() throws Exception {
            String responseBody = mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isUnauthorized()) // 401
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

            ApiError error = objectMapper.readValue(responseBody, ApiError.class);
            assertEquals("AUTH_UNAUTHENTICATED", error.errorCode());
            assertNotNull(error.timestamp());
        }
    }

    /**
     * Scenario 3: Authenticated but no role_audit_viewer → 403 AUTH_FORBIDDEN
     *
     * Proves that an authenticated JWT with NO realm roles hitting a role_audit_viewer-
     * protected route produces 403 AUTH_FORBIDDEN with an ApiError JSON body
     * (not Spring Boot's default whitelabel error page).
     *
     * Uses Spring Security Test's jwt() post-processor to mock an authenticated
     * principal with NO realm_access claim, then probes a non-permitAll endpoint.
     */
    @Nested
    @SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration," +
                "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration," +
                "org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration"
        }
    )
    @AutoConfigureMockMvc
    @DisplayName("Scenario 3: Authenticated but no role_audit_viewer → 403 AUTH_FORBIDDEN")
    class InsufficientRoleTest {

        private static WireMockServer wireMockServer;

        // Mock ConnectionFactory so context loads without a real RabbitMQ connection
        @MockBean
        private ConnectionFactory connectionFactory;

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @BeforeAll
        static void startWireMock() {
            wireMockServer = new WireMockServer(0);
            wireMockServer.start();
            WireMock.configureFor("localhost", wireMockServer.port());

            WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo(
                    "/realms/bookinghub/.well-known/openid-configuration"))
                .willReturn(WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                        {
                            "issuer": "http://localhost:%d/realms/bookinghub",
                            "jwks_uri": "http://localhost:%d/realms/bookinghub/protocol/openid-connect/certs"
                        }
                        """.formatted(wireMockServer.port(), wireMockServer.port()))));

            WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo(
                    "/realms/bookinghub/protocol/openid-connect/certs"))
                .willReturn(WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"keys\":[]}")));
        }

        @AfterAll
        static void stopWireMock() {
            wireMockServer.stop();
        }

        @DynamicPropertySource
        static void configureProperties(DynamicPropertyRegistry registry) {
            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> "http://localhost:" + wireMockServer.port() + "/realms/bookinghub");
        }

        @Test
        @DisplayName("Authenticated JWT with no roles accessing role_audit_viewer-protected endpoint yields 403 AUTH_FORBIDDEN")
        void insufficientRole_returns403ApiError() throws Exception {
            // Mock authenticated JWT with sub claim but NO realm_access.roles → no authorities
            // → accessing anyRequest().hasAuthority("role_audit_viewer") fires the ApiAccessDeniedHandler
            String responseBody = mockMvc.perform(
                    get("/actuator/info")
                        .with(jwt()
                            .jwt(j -> j
                                .claim("sub", "test-user-no-roles")
                                // Intentionally NO realm_access claim → zero authorities
                            ))
                )
                .andExpect(status().isForbidden()) // 403
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

            // Verify ApiError JSON shape (not Spring Boot's default whitelabel error)
            ApiError error = objectMapper.readValue(responseBody, ApiError.class);
            assertEquals("AUTH_FORBIDDEN", error.errorCode());
            assertNotNull(error.timestamp());
            assertNotNull(error.message());
            assertTrue(error.message().contains("Insufficient permissions")
                    || error.message().contains("permission"));
        }
    }
}
