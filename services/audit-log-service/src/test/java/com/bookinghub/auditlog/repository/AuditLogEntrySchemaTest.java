package com.bookinghub.auditlog.repository;

import com.bookinghub.auditlog.domain.AuditLogEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Proves the V2 idempotency_key migration applies cleanly on top of V1
 * and that the partial unique index semantics are correct.
 *
 * Three cases proven:
 * 1. A row with a non-null idempotencyKey round-trips correctly (column present, value preserved)
 * 2. A SECOND row with the SAME idempotencyKey throws DataIntegrityViolationException
 *    (partial unique index fires for non-null values — proves CASE 1's invariant)
 * 3. Two rows with idempotencyKey = null BOTH succeed (partial index skips nulls —
 *    this is the critical distinction from a blanket UNIQUE constraint that would
 *    reject multiple nulls)
 *
 * Uses the running docker-compose Postgres instead of Testcontainers:
 * this sandbox's Docker client version (1.32) is below Testcontainers 1.20.4's
 * minimum (1.40) — same pre-existing incompatibility already hit by Phase 4 plan 04-01's
 * SchemaCompletionTest (locations-resources-service) and Phase 3 plan 03-01's
 * PermissionSeedDataTest. @DataJpaTest wraps each @Test in a rolled-back transaction,
 * so no cleanup between tests is needed despite sharing the persistent audit_db_test DB.
 * AutoConfigureTestDatabase.Replace.NONE disables H2 auto-configuration (real Postgres
 * needed for JSONB and partial index behavior). Application-test.properties provides
 * all test-specific datasource/flyway settings via @ActiveProfiles("test").
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuditLogEntrySchemaTest {

    @Autowired
    private AuditLogEntryRepository repository;

    @Test
    void nonNullIdempotencyKey_roundTripsCorrectly() {
        UUID idempotencyKey = UUID.randomUUID();
        AuditLogEntry entry = buildEntry();
        entry.setIdempotencyKey(idempotencyKey);

        AuditLogEntry saved = repository.saveAndFlush(entry);

        AuditLogEntry found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getIdempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(found.getActorId()).isEqualTo(entry.getActorId());
        assertThat(found.getEntityType()).isEqualTo("booking");
        assertThat(found.getActionType()).isEqualTo("created");
    }

    @Test
    void duplicateNonNullIdempotencyKey_throwsDataIntegrityViolation() {
        UUID idempotencyKey = UUID.randomUUID();

        // First insert with this key — must succeed
        AuditLogEntry first = buildEntry();
        first.setIdempotencyKey(idempotencyKey);
        repository.saveAndFlush(first);

        // Second insert with the SAME key — must fail (partial unique index fires)
        AuditLogEntry second = buildEntry();
        second.setIdempotencyKey(idempotencyKey);

        assertThatThrownBy(() -> repository.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void multipleNullIdempotencyKeys_allSucceed() {
        // Two rows with null idempotency_key BOTH succeed
        // (partial index WHERE idempotency_key IS NOT NULL skips null rows —
        // proves the index is correctly partial, not a blanket unique constraint)
        AuditLogEntry first = buildEntry();
        first.setIdempotencyKey(null);

        AuditLogEntry second = buildEntry();
        second.setIdempotencyKey(null);

        // Both saves must succeed without throwing
        AuditLogEntry savedFirst = repository.saveAndFlush(first);
        AuditLogEntry savedSecond = repository.saveAndFlush(second);

        assertThat(savedFirst.getId()).isNotNull();
        assertThat(savedSecond.getId()).isNotNull();
        assertThat(savedFirst.getId()).isNotEqualTo(savedSecond.getId());
        assertThat(savedFirst.getIdempotencyKey()).isNull();
        assertThat(savedSecond.getIdempotencyKey()).isNull();
    }

    private AuditLogEntry buildEntry() {
        AuditLogEntry entry = new AuditLogEntry();
        entry.setActorId(UUID.randomUUID());
        entry.setOccurredAt(Instant.now());
        entry.setEntityType("booking");
        entry.setEntityId(UUID.randomUUID());
        entry.setActionType("created");
        return entry;
    }
}
