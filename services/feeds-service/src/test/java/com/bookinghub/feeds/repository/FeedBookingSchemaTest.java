package com.bookinghub.feeds.repository;

import com.bookinghub.feeds.domain.FeedBooking;
import com.bookinghub.feeds.domain.ProcessedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import jakarta.persistence.PersistenceException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Proves the FeedBooking / ProcessedEvent entity mappings are correct and that
 * every query FeedBookingRepository / ProcessedEventRepository declares behaves
 * as plan 07-02 (consumer) and plan 07-03 (public endpoints) will rely on:
 *
 *  - approved + upcoming (>= now) ordered query, with and without a location filter
 *  - the idempotency-key uniqueness backstop on processed_events
 *  - the updateLocationName @Modifying back-fill query (valid JPQL + correct WHERE scoping)
 *
 * Testcontainers workaround (same as Phase 4 plan 04-01 / Phase 6 plan 06-03):
 * this sandbox's Docker daemon reports client API version 1.32, below
 * Testcontainers' 1.40 minimum, so @Testcontainers/PostgreSQLContainer cannot
 * start. The test instead runs against the running docker-compose Postgres via
 * application-test.properties (@ActiveProfiles("test") + Replace.NONE).
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FeedBookingSchemaTest {

    @Autowired
    private FeedBookingRepository feedBookingRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Autowired
    private TestEntityManager em;

    private FeedBooking booking(String status, UUID locationId, String locationName, Instant startTime) {
        FeedBooking b = new FeedBooking();
        b.setBookingId(UUID.randomUUID());
        b.setTitle("Booking " + UUID.randomUUID());
        b.setLocationId(locationId);
        b.setLocationName(locationName);
        b.setStartTime(startTime);
        b.setEndTime(startTime.plus(1, ChronoUnit.HOURS));
        b.setStatus(status);
        return b;
    }

    @Test
    void approvedUpcomingQuery_returnsOnlyApprovedFutureRows() {
        Instant now = Instant.now();
        UUID loc = UUID.randomUUID();

        FeedBooking upcomingApproved = feedBookingRepository.save(
            booking("approved", loc, "Room A", now.plus(1, ChronoUnit.HOURS)));
        // Case 2: approved but in the PAST -> must NOT surface (proves >= now boundary).
        feedBookingRepository.save(
            booking("approved", loc, "Room A", now.minus(1, ChronoUnit.HOURS)));
        // Case 3: upcoming but pending -> must NOT surface (proves status filter).
        feedBookingRepository.save(
            booking("pending", loc, "Room A", now.plus(1, ChronoUnit.HOURS)));

        List<FeedBooking> result = feedBookingRepository
            .findByStatusAndStartTimeGreaterThanEqualOrderByStartTimeAsc("approved", now);

        assertThat(result)
            .extracting(FeedBooking::getBookingId)
            .containsExactly(upcomingApproved.getBookingId());
    }

    @Test
    void approvedUpcomingByLocationQuery_scopesToOneLocation() {
        Instant now = Instant.now();
        UUID locA = UUID.randomUUID();
        UUID locB = UUID.randomUUID();

        FeedBooking atA = feedBookingRepository.save(
            booking("approved", locA, "Room A", now.plus(1, ChronoUnit.HOURS)));
        feedBookingRepository.save(
            booking("approved", locB, "Room B", now.plus(2, ChronoUnit.HOURS)));

        List<FeedBooking> result = feedBookingRepository
            .findByStatusAndLocationIdAndStartTimeGreaterThanEqualOrderByStartTimeAsc("approved", locA, now);

        assertThat(result)
            .extracting(FeedBooking::getBookingId)
            .containsExactly(atA.getBookingId());
    }

    @Test
    void processedEvent_idempotencyKeyIsUniqueBackstop() {
        UUID key = UUID.randomUUID();
        ProcessedEvent pe = new ProcessedEvent();
        pe.setIdempotencyKey(key);
        processedEventRepository.saveAndFlush(pe);

        assertThat(processedEventRepository.existsByIdempotencyKey(key)).isTrue();
        assertThat(processedEventRepository.existsByIdempotencyKey(UUID.randomUUID())).isFalse();

        // A redelivered key violates the PRIMARY KEY on insert — the race-safe backstop.
        // Note: ProcessedEvent has an ASSIGNED id (not @GeneratedValue), so
        // JpaRepository.save() on an existing key would MERGE (SELECT+UPDATE), not
        // INSERT, and would NOT surface the constraint. em.persist() always issues an
        // INSERT, which is exactly the redelivery path plan 07-02's consumer hits when
        // two deliveries of the same event race past the lookup-first check.
        ProcessedEvent duplicate = new ProcessedEvent();
        duplicate.setIdempotencyKey(key);
        assertThatThrownBy(() -> {
            em.persist(duplicate);
            em.flush();
        }).satisfiesAnyOf(
            t -> assertThat(t).isInstanceOf(PersistenceException.class),
            t -> assertThat(t).isInstanceOf(DataIntegrityViolationException.class),
            t -> assertThat(t).isInstanceOf(ConstraintViolationException.class)
        );
    }

    @Test
    void updateLocationName_scopesToLocationAndReturnsAffectedCount() {
        Instant now = Instant.now();
        UUID locA = UUID.randomUUID();
        UUID locB = UUID.randomUUID();

        FeedBooking a1 = feedBookingRepository.save(
            booking("approved", locA, "Old Name", now.plus(1, ChronoUnit.HOURS)));
        FeedBooking a2 = feedBookingRepository.save(
            booking("approved", locA, "Old Name", now.plus(2, ChronoUnit.HOURS)));
        FeedBooking b1 = feedBookingRepository.save(
            booking("approved", locB, "Other Name", now.plus(3, ChronoUnit.HOURS)));

        int affected = feedBookingRepository.updateLocationName(locA, "New Name");

        // The @Modifying mutation bypasses the first-level cache; flush+clear so the
        // re-reads come from the DB, not stale managed entities.
        em.flush();
        em.clear();

        assertThat(affected).isEqualTo(2);
        assertThat(feedBookingRepository.findById(a1.getBookingId()).orElseThrow().getLocationName())
            .isEqualTo("New Name");
        assertThat(feedBookingRepository.findById(a2.getBookingId()).orElseThrow().getLocationName())
            .isEqualTo("New Name");
        assertThat(feedBookingRepository.findById(b1.getBookingId()).orElseThrow().getLocationName())
            .isEqualTo("Other Name");
    }
}
