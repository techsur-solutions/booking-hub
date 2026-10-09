package com.bookinghub.booking.conflict;

import com.bookinghub.booking.conflict.dto.ConflictDtos.ConflictCheckResult;
import com.bookinghub.booking.domain.Booking;
import com.bookinghub.booking.domain.BookingResource;
import com.bookinghub.booking.repository.BookingRepository;
import com.bookinghub.booking.repository.BookingResourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ConflictDetectionService integration test proving overlap semantics against real Postgres.
 *
 * Uses the running Postgres from docker-compose (same sandbox Docker-API-version
 * workaround as SchemaCompletionTest — Testcontainers requires Docker API >=1.40,
 * this sandbox reports 1.32). @DataJpaTest wraps each test in a rolled-back transaction
 * so tests do not contaminate each other despite sharing the persistent booking_db_test.
 *
 * Named decision (zero HTTP mocking): ConflictDetectionService has ZERO cross-service
 * HTTP dependencies — it only queries bookings/booking_resources tables owned by this
 * service. No WireMock, no @MockBean, no external service needed for these tests.
 *
 * Covers all 6 scenarios from the plan:
 * 1. Two bookings in the same location with overlapping times → location conflict detected
 * 2. Adjacent bookings (one ends exactly when the other starts) → NO conflict (half-open interval)
 * 3. A denied booking that would otherwise overlap → excluded (no conflict)
 * 4. A soft-deleted booking that would otherwise overlap → excluded (no conflict)
 * 5. Edit scenario (excludeBookingId = own id) against its own unchanged time range → NO self-conflict
 * 6. Two bookings sharing a unique resource with overlapping times → resource conflict detected
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ConflictDetectionService.class)
class ConflictDetectionServiceTest {

    @Autowired
    private ConflictDetectionService conflictDetectionService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingResourceRepository bookingResourceRepository;

    // ── Scenario 1: Location-level overlap detected ───────────────────────

    @Test
    void locationOverlap_detectsConflict() {
        UUID locationId = UUID.randomUUID();
        // existing booking: 10:00–11:00 (status=approved — a live booking claiming the slot)
        saveBooking(locationId, hour(10), hour(11), "approved", null, null);

        // proposed: 10:30–11:30 — overlaps
        ConflictCheckResult result = conflictDetectionService.checkConflicts(
                locationId, List.of(), hour(10, 30), hour(11, 30), null);

        assertThat(result.hasConflict()).isTrue();
        assertThat(result.conflicts()).hasSize(1);
        assertThat(result.conflicts().get(0).conflictType()).isEqualTo("location");
        assertThat(result.conflicts().get(0).locationId()).isEqualTo(locationId);
        assertThat(result.conflicts().get(0).resourceId()).isNull();
    }

    // ── Scenario 2: Adjacent bookings do NOT conflict (half-open interval) ──

    @Test
    void adjacentBookings_doNotConflict() {
        UUID locationId = UUID.randomUUID();
        // existing booking: 09:00–10:00 (status=approved — a live booking)
        saveBooking(locationId, hour(9), hour(10), "approved", null, null);

        // proposed: 10:00–11:00 — starts exactly when existing ends → NO conflict
        ConflictCheckResult result = conflictDetectionService.checkConflicts(
                locationId, List.of(), hour(10), hour(11), null);

        assertThat(result.hasConflict()).isFalse();
        assertThat(result.conflicts()).isEmpty();
    }

    // ── Scenario 3: Denied booking is excluded ────────────────────────────

    @Test
    void deniedBooking_isExcludedFromConflictCheck() {
        UUID locationId = UUID.randomUUID();
        // existing booking: 10:00–11:00, status=denied
        saveBooking(locationId, hour(10), hour(11), "denied", null, null);

        // proposed overlaps the denied booking's slot
        ConflictCheckResult result = conflictDetectionService.checkConflicts(
                locationId, List.of(), hour(10), hour(11), null);

        assertThat(result.hasConflict()).isFalse();
        assertThat(result.conflicts()).isEmpty();
    }

    // ── Scenario 4: Soft-deleted booking is excluded ─────────────────────

    @Test
    void softDeletedBooking_isExcludedFromConflictCheck() {
        UUID locationId = UUID.randomUUID();
        // existing booking: 10:00–11:00, deleted_at IS NOT NULL (status=approved, soft-deleted)
        saveBooking(locationId, hour(10), hour(11), "approved", null, Instant.now());

        // proposed overlaps the soft-deleted booking's slot
        ConflictCheckResult result = conflictDetectionService.checkConflicts(
                locationId, List.of(), hour(10), hour(11), null);

        assertThat(result.hasConflict()).isFalse();
        assertThat(result.conflicts()).isEmpty();
    }

    // ── Scenario 5: Edit scenario — booking does not conflict with itself ─

    @Test
    void editScenario_doesNotConflictWithOwnId() {
        UUID locationId = UUID.randomUUID();
        // The booking being edited occupies 10:00–11:00 (status=approved)
        Booking existingBooking = saveBooking(locationId, hour(10), hour(11), "approved", null, null);
        UUID ownId = existingBooking.getId();

        // Re-checking the same slot with own id excluded → no self-conflict
        ConflictCheckResult result = conflictDetectionService.checkConflicts(
                locationId, List.of(), hour(10), hour(11), ownId);

        assertThat(result.hasConflict()).isFalse();
        assertThat(result.conflicts()).isEmpty();
    }

    // ── Scenario 6: Resource-level conflict detected ──────────────────────

    @Test
    void resourceOverlap_detectsConflict() {
        UUID locationId = UUID.randomUUID();
        UUID uniqueResourceId = UUID.randomUUID();

        // existing booking at 10:00–11:00 with the unique resource attached (status=approved)
        Booking existing = saveBooking(locationId, hour(10), hour(11), "approved", null, null);
        bookingResourceRepository.save(new BookingResource(existing.getId(), uniqueResourceId));

        // proposed: 10:30–11:30 — overlaps; caller already determined uniqueResourceId is_unique=true
        ConflictCheckResult result = conflictDetectionService.checkConflicts(
                null,                           // no location check needed here
                List.of(uniqueResourceId),      // pre-filtered unique resource list
                hour(10, 30), hour(11, 30),
                null);

        assertThat(result.hasConflict()).isTrue();
        assertThat(result.conflicts()).hasSize(1);
        assertThat(result.conflicts().get(0).conflictType()).isEqualTo("resource");
        assertThat(result.conflicts().get(0).resourceId()).isEqualTo(uniqueResourceId);
        assertThat(result.conflicts().get(0).locationId()).isNull();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /** Creates and persists a booking with the given parameters. */
    private Booking saveBooking(UUID locationId, Instant start, Instant end,
                                 String status, @SuppressWarnings("unused") UUID unused, Instant deletedAt) {
        Booking b = new Booking();
        b.setTitle("Test booking");
        b.setLocationId(locationId);
        b.setStartTime(start);
        b.setEndTime(end);
        b.setOwnerId(UUID.randomUUID());
        b.setStatus(status);
        if (deletedAt != null) {
            b.setDeletedAt(deletedAt);
        }
        Booking saved = bookingRepository.save(b);
        bookingRepository.flush();
        return saved;
    }

    /** Returns an Instant at the given hour on a fixed reference date (2030-01-01). */
    private static Instant hour(int hour) {
        return Instant.parse(String.format("2030-01-01T%02d:00:00Z", hour));
    }

    /** Returns an Instant at the given hour and minute on the reference date. */
    private static Instant hour(int hour, int minute) {
        return Instant.parse(String.format("2030-01-01T%02d:%02d:00Z", hour, minute));
    }
}
