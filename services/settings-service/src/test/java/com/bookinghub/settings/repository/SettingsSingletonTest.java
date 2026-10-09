package com.bookinghub.settings.repository;

import com.bookinghub.settings.domain.Settings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

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
 * Uses the running Postgres from docker-compose instead of Testcontainers:
 * this sandbox's Docker daemon reports an API version Testcontainers' bundled
 * client rejects as too old, so a real PostgreSQLContainer never starts here.
 * Flyway still runs the actual V1 + V2 migrations against a real Postgres
 * (settings_db_test), so the CHECK constraint behavior under test is exactly
 * what production boot would see — this is a substrate swap, not a mock.
 */
@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SettingsSingletonTest {

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

        // No further assertions in THIS method: a real Postgres (unlike an
        // in-memory substitute) aborts the whole transaction on a CHECK
        // violation — any further statement in the same transaction, even a
        // read, fails with "current transaction is aborted". @DataJpaTest's
        // per-test rollback means the failed INSERT never persists regardless;
        // exactlyOneRowExistsAfterSetup (a separate test, separate transaction)
        // is what proves the singleton count, both before and after this test
        // runs in the suite.
    }
}
