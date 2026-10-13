package com.bookinghub.feeds.consumer;

import com.bookinghub.feeds.domain.FeedBooking;
import com.bookinghub.feeds.repository.FeedBookingRepository;
import com.bookinghub.feeds.repository.LocationNameRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the FeedProjectionService state machine end-to-end against a real Postgres
 * (the running docker-compose instance via application-test.properties):
 *  1. booking.created (pending) -> row with status=pending, placeholder location_name
 *  2. location.updated -> back-fills the existing row's location_name
 *  3. booking.approved -> status flips to approved
 *  4. booking.denied -> row physically removed
 *  5. booking.created(approved) then booking.deleted -> row physically removed
 *  6. location.deleted -> booking row + its location_name UNCHANGED (graceful degradation)
 *
 * Testcontainers workaround (same established pattern as plan 07-01's FeedBookingSchemaTest,
 * Phase 4 plan 04-01, and Phase 6 plan 06-03): run against the running docker-compose
 * Postgres via @ActiveProfiles("test") + Replace.NONE rather than @Testcontainers, which
 * this sandbox's Docker daemon has historically been unable to start.
 *
 * @Import brings the production FeedProjectionService (and the LocationNameRepository it
 * depends on, auto-detected by @DataJpaTest) into the slice context.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(FeedProjectionService.class)
class FeedProjectionServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private FeedProjectionService projectionService;

    @Autowired
    private FeedBookingRepository feedBookingRepository;

    @Autowired
    private LocationNameRepository locationNameRepository;

    @Autowired
    private TestEntityManager em;

    private JsonNode bookingPayload(UUID id, String title, UUID locationId,
                                    Instant start, Instant end, String status) {
        ObjectNode n = MAPPER.createObjectNode();
        n.put("id", id.toString());
        if (title != null) n.put("title", title);
        if (locationId != null) n.put("location_id", locationId.toString());
        if (start != null) n.put("start_time", start.toString());
        if (end != null) n.put("end_time", end.toString());
        if (status != null) n.put("status", status);
        return n;
    }

    private JsonNode idStatusPayload(UUID id, String status) {
        ObjectNode n = MAPPER.createObjectNode();
        n.put("id", id.toString());
        if (status != null) n.put("status", status);
        return n;
    }

    private JsonNode locationPayload(UUID id, String name) {
        ObjectNode n = MAPPER.createObjectNode();
        n.put("id", id.toString());
        if (name != null) n.put("name", name);
        return n;
    }

    private void flushClear() {
        em.flush();
        em.clear();
    }

    @Test
    void bookingCreatedPending_writesRowWithPlaceholderLocationName() {
        UUID bookingId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
        Instant end = Instant.now().plus(2, ChronoUnit.HOURS);

        projectionService.applyBookingEvent("booking.created",
            bookingPayload(bookingId, "Weekly Sync", locationId, start, end, "pending"));
        flushClear();

        FeedBooking row = feedBookingRepository.findById(bookingId).orElseThrow();
        assertThat(row.getStatus()).isEqualTo("pending");
        assertThat(row.getTitle()).isEqualTo("Weekly Sync");
        assertThat(row.getLocationId()).isEqualTo(locationId);
        assertThat(row.getLocationName()).isEqualTo("Location " + locationId); // placeholder, no cache yet
    }

    @Test
    void locationUpdated_backfillsExistingBookingRowLocationName() {
        UUID bookingId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
        Instant end = Instant.now().plus(2, ChronoUnit.HOURS);

        projectionService.applyBookingEvent("booking.created",
            bookingPayload(bookingId, "Weekly Sync", locationId, start, end, "pending"));
        flushClear();

        projectionService.applyLocationEvent("location.updated",
            locationPayload(locationId, "Conference Room A"));
        flushClear();

        FeedBooking row = feedBookingRepository.findById(bookingId).orElseThrow();
        assertThat(row.getLocationName()).isEqualTo("Conference Room A"); // back-fill worked
        assertThat(locationNameRepository.findById(locationId).orElseThrow().getName())
            .isEqualTo("Conference Room A"); // cache populated
    }

    @Test
    void bookingApproved_flipsStatusToApproved() {
        UUID bookingId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
        Instant end = Instant.now().plus(2, ChronoUnit.HOURS);

        projectionService.applyBookingEvent("booking.created",
            bookingPayload(bookingId, "Weekly Sync", locationId, start, end, "pending"));
        flushClear();

        // approved payload carries only id + status metadata (no title/location/time)
        projectionService.applyBookingEvent("booking.approved",
            idStatusPayload(bookingId, "approved"));
        flushClear();

        FeedBooking row = feedBookingRepository.findById(bookingId).orElseThrow();
        assertThat(row.getStatus()).isEqualTo("approved");
    }

    @Test
    void bookingDenied_physicallyRemovesRow() {
        UUID bookingId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
        Instant end = Instant.now().plus(2, ChronoUnit.HOURS);

        projectionService.applyBookingEvent("booking.created",
            bookingPayload(bookingId, "Weekly Sync", locationId, start, end, "pending"));
        flushClear();

        // denied payload carries only id + status metadata
        projectionService.applyBookingEvent("booking.denied", idStatusPayload(bookingId, "denied"));
        flushClear();

        assertThat(feedBookingRepository.findById(bookingId)).isEmpty();
    }

    @Test
    void bookingDeleted_physicallyRemovesRow() {
        UUID bookingId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
        Instant end = Instant.now().plus(2, ChronoUnit.HOURS);

        projectionService.applyBookingEvent("booking.created",
            bookingPayload(bookingId, "Standup", locationId, start, end, "approved"));
        flushClear();
        assertThat(feedBookingRepository.findById(bookingId)).isPresent();

        // deleted payload carries only id + deleted_at
        ObjectNode deletedPayload = MAPPER.createObjectNode();
        deletedPayload.put("id", bookingId.toString());
        deletedPayload.put("deleted_at", Instant.now().toString());
        projectionService.applyBookingEvent("booking.deleted", deletedPayload);
        flushClear();

        assertThat(feedBookingRepository.findById(bookingId)).isEmpty();
    }

    @Test
    void locationDeleted_keepsLastKnownNameAndRow() {
        UUID bookingId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
        Instant end = Instant.now().plus(2, ChronoUnit.HOURS);

        projectionService.applyBookingEvent("booking.created",
            bookingPayload(bookingId, "Weekly Sync", locationId, start, end, "approved"));
        flushClear();
        projectionService.applyLocationEvent("location.updated",
            locationPayload(locationId, "Conference Room A"));
        flushClear();

        // location.deleted carries id + name, but we must NOT blank or delete anything
        projectionService.applyLocationEvent("location.deleted",
            locationPayload(locationId, "Conference Room A"));
        flushClear();

        FeedBooking row = feedBookingRepository.findById(bookingId).orElseThrow();
        assertThat(row.getLocationName()).isEqualTo("Conference Room A"); // last-known kept
    }
}
