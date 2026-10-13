package com.bookinghub.feeds;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Highest-yield test: boots the full Spring context (now including RabbitConfig's
 * rabbitListenerContainerFactory and the JPA layer) and asserts both actuator
 * health probes return UP.
 *
 * Broker-free: application-test.properties sets
 * spring.rabbitmq.listener.simple.auto-startup=false for the test, so the context
 * boots without a live RabbitMQ broker even though production flips it to true
 * (plan 07-01 Task 1). This matches Phase 6's audit-log-service boot-test
 * precedent — the real broker is exercised by plan 07-02's dedicated consumer
 * integration test.
 *
 * Testcontainers workaround (same as Phase 4/6): this sandbox's Docker daemon
 * reports client API 1.32, below Testcontainers' 1.40 minimum, so this test runs
 * against the running docker-compose Postgres via application-test.properties
 * rather than PostgreSQLContainer.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApplicationContextBootTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
        // Verifies the full Spring context (JPA + RabbitConfig) loads successfully
        // against the running docker-compose Postgres, broker-free.
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
}
