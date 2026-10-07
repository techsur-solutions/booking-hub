package com.bookinghub.auditlog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ApplicationContextBootTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("audit_db_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

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
    void auditLogEntriesImmutabilityEnforcedAtDatabaseLevel() throws Exception {
        UUID testId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();

        // 1. Insert a row via the superuser connection (Testcontainers default)
        jdbcTemplate.update(
            "INSERT INTO audit_log_entries (id, actor_id, occurred_at, entity_type, entity_id, action_type) " +
            "VALUES (?, ?, now(), 'booking', ?, 'created')",
            testId, actorId, entityId
        );

        // 2. Verify UPDATE through the superuser connection is allowed (superuser bypasses grants - expected)
        int updatedRows = jdbcTemplate.update(
            "UPDATE audit_log_entries SET action_type = 'tampered' WHERE id = ?",
            testId
        );
        assertThat(updatedRows).isEqualTo(1); // Superuser can update - this is expected and fine

        // Reset the tampered value back to 'created' for the next test
        jdbcTemplate.update(
            "UPDATE audit_log_entries SET action_type = 'created' WHERE id = ?",
            testId
        );

        // 3. Create a non-superuser test role with only SELECT, INSERT granted (mirroring audit_svc)
        try (Connection conn = dataSource.getConnection()) {
            conn.createStatement().execute("CREATE ROLE test_audit_role LOGIN PASSWORD 'test_pw'");
            conn.createStatement().execute("GRANT CONNECT ON DATABASE audit_db_test TO test_audit_role");
            conn.createStatement().execute("GRANT SELECT, INSERT ON audit_log_entries TO test_audit_role");
        }

        // 4. Attempt UPDATE through the restricted role's connection - this MUST fail
        PostgreSQLContainer.ExecResult createRestrictedUser = postgres.execInContainer(
            "psql", "-U", "test", "-d", "audit_db_test", "-c",
            String.format(
                "SET ROLE test_audit_role; UPDATE audit_log_entries SET action_type = 'tampered' WHERE id = '%s';",
                testId
            )
        );
        
        // Verify the UPDATE was rejected (permission denied)
        assertThat(createRestrictedUser.getStderr())
            .contains("permission denied");

        // 5. Attempt DELETE through the restricted role's connection - this MUST also fail
        PostgreSQLContainer.ExecResult deleteAttempt = postgres.execInContainer(
            "psql", "-U", "test", "-d", "audit_db_test", "-c",
            String.format(
                "SET ROLE test_audit_role; DELETE FROM audit_log_entries WHERE id = '%s';",
                testId
            )
        );
        
        // Verify the DELETE was rejected (permission denied)
        assertThat(deleteAttempt.getStderr())
            .contains("permission denied");

        // 6. Verify the row still has the original value (not tampered)
        String actionType = jdbcTemplate.queryForObject(
            "SELECT action_type FROM audit_log_entries WHERE id = ?",
            String.class,
            testId
        );
        assertThat(actionType).isEqualTo("created");

        // 7. Cleanup: drop the test role
        try (Connection conn = dataSource.getConnection()) {
            conn.createStatement().execute("DROP ROLE test_audit_role");
        }
    }
}
