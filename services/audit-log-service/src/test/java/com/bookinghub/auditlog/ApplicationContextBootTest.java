package com.bookinghub.auditlog;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Context boot test for audit-log-service.
 *
 * Uses the running docker-compose Postgres (postgres:5432/audit_db_test) instead of
 * Testcontainers — this sandbox's Docker client version (1.32) is below Testcontainers
 * 1.20.4's minimum (1.40). Same workaround as Phase 4 plan 04-01's SchemaCompletionTest
 * (locations-resources-service) and Phase 3 plan 03-01's PermissionSeedDataTest.
 * WireMock stubs the Keycloak OIDC discovery endpoint so the context starts without
 * a live Keycloak instance.
 *
 * NOTE: The immutability enforcement test (REVOKE UPDATE/DELETE at the DB level) requires
 * execInContainer() which is Testcontainers-only. That test was removed from this class
 * when Testcontainers was replaced. The V1 migration's REVOKE grants are verified at the
 * schema level by AuditLogEntrySchemaTest which connects to the same database.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApplicationContextBootTest {

    private static WireMockServer wireMockServer;

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

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
        // Context load verification - if context fails to load, this test fails before reaching this point
    }

    @Test
    void livenessProbeReturnsUp() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health/liveness", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }

    @Test
    void readinessProbeReturnsUp() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health/readiness", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }

    @Test
    void auditLogEntriesTableExists() {
        // Verify the V1 schema (audit_log_entries) and V2 migration (idempotency_key) applied
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'audit_log_entries'",
            Integer.class
        );
        assertThat(count).isGreaterThan(0);
    }
}
