package com.bookinghub.settings.repository;

import com.bookinghub.settings.domain.Settings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Proves the singleton invariant holds at BOTH the code layer (Settings.id has
 * no @GeneratedValue) and the DB layer (chk_settings_singleton CHECK constraint),
 * against the ACTUAL V1 migration's seeded defaults (approveBooking=true,
 * calendarSlotSize=30, calendarMinTime=08:00, calendarMaxTime=18:00 — Phase 2
 * plan 02-07's INSERT INTO settings (id) VALUES (1) ON CONFLICT (id) DO NOTHING).
 *
 * Uses a real Testcontainers PostgreSQL instance (postgres:16) so Flyway runs
 * the actual V1 + V2 migrations exactly as production boot would, rather than
 * an in-memory substitute that might silently diverge from the CHECK constraint
 * behavior under test.
 */
@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@Testcontainers
class SettingsSingletonTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("settings_db_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private SettingsRepository settingsRepository;

    @Test
    void findById1ReturnsThePhase2SeededRowWithDocumentedDefaults() {
        Settings settings = settingsRepository.findById(1)
                .orElseThrow(() -> new AssertionError("Seeded singleton row (id=1) not found"));

        assertThat(settings.isApproveBooking()).isTrue();
        assertThat(settings.getCalendarSlotSize()).isEqualTo(30);
        assertThat(settings.getCalendarMinTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(settings.getCalendarMaxTime()).isEqualTo(LocalTime.of(18, 0));
    }

    @Test
    void exactlyOneRowExistsAfterSetup() {
        assertThat(settingsRepository.count()).isEqualTo(1);
    }

    @Test
    void attemptingToInsertASecondRowViolatesTheSingletonCheckConstraint() {
        // Settings.id has NO @GeneratedValue, so JPA's save() for a non-existent
        // id (2) attempts a literal INSERT — which the DB's chk_settings_singleton
        // CHECK (id = 1) constraint must reject. This proves the second,
        // independent layer of defense: even if a code path somehow constructed
        // a Settings instance with id != 1, the database itself refuses it.
        Settings secondRow = new Settings(2, true, 30, LocalTime.of(8, 0), LocalTime.of(18, 0));

        assertThatThrownBy(() -> {
            settingsRepository.saveAndFlush(secondRow);
        }).isInstanceOf(DataIntegrityViolationException.class)
          .hasMessageContaining("chk_settings_singleton");

        // The singleton invariant held: still exactly one row after the failed attempt.
        assertThat(settingsRepository.count()).isEqualTo(1);
    }
}
