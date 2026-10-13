package com.bookinghub.feeds.consumer;

import com.bookinghub.feeds.domain.FeedBooking;
import com.bookinghub.feeds.domain.LocationName;
import com.bookinghub.feeds.domain.ProcessedEvent;
import com.bookinghub.feeds.repository.FeedBookingRepository;
import com.bookinghub.feeds.repository.LocationNameRepository;
import com.bookinghub.feeds.repository.ProcessedEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * The projection state machine for feeds-service.
 *
 * Derives Phase 7 Success Criterion 1 ("pending and denied bookings never appear
 * in any format") at the DATA level, not merely the query level: a denied/deleted
 * booking is PHYSICALLY removed from feed_bookings, so no feed renderer in plan
 * 07-03 can ever surface it.
 *
 * Also solves the cross-service denormalization problem: feeds-service must show
 * location NAMES but owns only location_ids in booking events and may never query
 * locations-resources-service's database. location_name is sourced ONLY from
 * location.updated events into the location_names cache (V3).
 *
 * ---
 * action_type is the routing-key suffix: {@code routingKey.substring(routingKey.lastIndexOf('.') + 1)}
 * (so booking.approved -> approved). The suffix maps to a transition:
 *
 *  - booking.created / booking.updated -> UPSERT the feed_bookings row from the full
 *    payload (id/title/location_id/resolved location_name/start_time/end_time/status).
 *    A created event for a not-yet-approved booking writes status="pending", which is
 *    correct and harmless: plan 07-03's queries filter status='approved' so it is
 *    invisible until approved, and keeping it lets a later booking.updated find a row.
 *  - booking.approved -> set the EXISTING row's status="approved" (making it feed-visible).
 *  - booking.denied / booking.deleted -> DELETE the feed_bookings row.
 *
 * RECONCILED UPSTREAM PAYLOAD CONTRACT (per the plan's <assumptions> — reconciled at
 * execution time against the real Phase 5 booking-service and Phase 4 locations-service
 * publishers):
 *  - booking.created  (BookingWriteService.buildCreatePayload): id, title, location_id,
 *    start_time, end_time, status, owner_id, series_id, email_contact   [FULL]
 *  - booking.updated  (BookingWriteService.buildUpdatePayload):  id, title, location_id,
 *    start_time, end_time, status, owner_id                              [FULL]
 *  - booking.approved / booking.denied (BookingApprovalService.buildApprovalPayload):
 *    id, status, approved_by, approved_at, denied_by, denied_at, denial_reason
 *    -> carries NO title/location_id/start_time/end_time. The plan's draft pseudo-code
 *       upsert(...)-from-payload for "approved" would NPE on these absent fields. The
 *       correct, payload-faithful transition is a STATUS FLIP on the already-projected
 *       row (booking.created always precedes booking.approved), which this class does.
 *  - booking.deleted  (BookingWriteService.buildDeletePayload): id, deleted_at
 *    -> only id is needed (deleteById), matches.
 *  - location.updated / location.deleted (LocationService.writeOutboxEvent): id, name, ...
 *
 * All field reads are defensive (null-checked, log-and-skip) so a single malformed
 * message can never wedge the queue or corrupt the projection — EXCEPT a missing
 * idempotency_key, which is the consumer's concern (FeedEventConsumer), not here.
 */
@Service
public class FeedProjectionService {

    private static final Logger log = LoggerFactory.getLogger(FeedProjectionService.class);

    private final FeedBookingRepository feedBookingRepository;
    private final LocationNameRepository locationNameRepository;
    private final ProcessedEventRepository processedEventRepository;

    public FeedProjectionService(FeedBookingRepository feedBookingRepository,
                                 LocationNameRepository locationNameRepository,
                                 ProcessedEventRepository processedEventRepository) {
        this.feedBookingRepository = feedBookingRepository;
        this.locationNameRepository = locationNameRepository;
        this.processedEventRepository = processedEventRepository;
    }

    /**
     * Idempotently applies a booking event: lookup-first dedup, projection mutation, and
     * processed_events insert — all in ONE transaction so a crash between them rolls back
     * cleanly and the event safely redelivers.
     *
     * The transaction boundary lives HERE (not on the consumer's @RabbitListener method)
     * so that a MissingIdempotencyKeyException thrown by the consumer BEFORE this call
     * reaches plan 07-01's retry interceptor unwrapped — letting its non-retryable
     * classification dead-letter the message immediately instead of burning the full
     * 5s/30s/2min retry backoff. (Established pattern: Phase 6 audit-log-service keeps its
     * listener non-transactional and its DB-mutating service method transactional.)
     *
     * @return true if applied, false if suppressed as a duplicate redelivery
     */
    @Transactional
    public boolean applyBookingEventIdempotent(UUID idempotencyKey, String routingKey, JsonNode body) {
        if (processedEventRepository.existsByIdempotencyKey(idempotencyKey)) {
            return false; // duplicate redelivery -> safe no-op, projection never double-applied
        }
        applyBookingEvent(routingKey, body);
        recordProcessed(idempotencyKey);
        return true;
    }

    /**
     * Idempotent location-event counterpart of {@link #applyBookingEventIdempotent}.
     */
    @Transactional
    public boolean applyLocationEventIdempotent(UUID idempotencyKey, String routingKey, JsonNode body) {
        if (processedEventRepository.existsByIdempotencyKey(idempotencyKey)) {
            return false;
        }
        applyLocationEvent(routingKey, body);
        recordProcessed(idempotencyKey);
        return true;
    }

    private void recordProcessed(UUID idempotencyKey) {
        ProcessedEvent pe = new ProcessedEvent();
        pe.setIdempotencyKey(idempotencyKey);
        processedEventRepository.save(pe);
    }

    @Transactional
    public void applyBookingEvent(String routingKey, JsonNode body) {
        String action = actionOf(routingKey); // created|updated|approved|denied|deleted
        UUID bookingId = readUuid(body, "id");
        if (bookingId == null) {
            log.warn("booking.{} event missing 'id' field; skipping (routing_key={})", action, routingKey);
            return;
        }
        switch (action) {
            case "denied", "deleted" ->
                // A denied/deleted booking must NEVER appear in any feed — physically remove it.
                feedBookingRepository.deleteById(bookingId);
            case "approved" ->
                // approved payload carries only id + status metadata (no title/location/time);
                // flip the status on the already-projected row (booking.created precedes approved).
                approve(bookingId);
            case "created", "updated" -> {
                String status = readText(body, "status");
                if (status == null) {
                    log.warn("booking.{} event {} missing 'status'; skipping", action, bookingId);
                    return;
                }
                upsert(bookingId, body, status);
            }
            default ->
                // Unknown booking action: never throw (would poison the queue), just note it.
                log.warn("Unknown booking action '{}' (routing_key={}); no-op", action, routingKey);
        }
    }

    @Transactional
    public void applyLocationEvent(String routingKey, JsonNode body) {
        String action = actionOf(routingKey); // updated|deleted
        UUID locationId = readUuid(body, "id");
        if (locationId == null) {
            log.warn("location.{} event missing 'id' field; skipping (routing_key={})", action, routingKey);
            return;
        }
        if ("updated".equals(action)) {
            String name = readText(body, "name");
            if (name == null) {
                log.warn("location.updated event {} missing 'name'; skipping", locationId);
                return;
            }
            // Upsert the cache so future bookings at this location resolve the real name.
            LocationName cache = locationNameRepository.findById(locationId).orElseGet(LocationName::new);
            cache.setLocationId(locationId);
            cache.setName(name);
            locationNameRepository.save(cache);
            // Back-fill every already-projected booking at this location so feed labels stay fresh.
            feedBookingRepository.updateLocationName(locationId, name);
        }
        // "deleted": deliberately KEEP the last-known name in both the cache and the
        // feed_bookings rows (graceful degradation — a deleted location does not
        // retroactively un-book an approved meeting, and the feed must never blank).
    }

    /**
     * Sets an existing projected booking's status to "approved", making it feed-visible.
     * If the row is somehow absent (approved arrived before created — should not happen
     * under the outbox ordering, but defensive), this is a no-op: there is no payload
     * data to materialize a full row from, and a later booking.updated would re-project it.
     */
    private void approve(UUID bookingId) {
        feedBookingRepository.findById(bookingId).ifPresentOrElse(
            fb -> {
                fb.setStatus("approved");
                feedBookingRepository.save(fb);
            },
            () -> log.warn("booking.approved for {} but no projected row exists; cannot "
                    + "materialize (approved payload carries no title/location/time); no-op", bookingId));
    }

    private void upsert(UUID bookingId, JsonNode body, String status) {
        UUID locationId = readUuid(body, "location_id");
        String title = readText(body, "title");
        String startText = readText(body, "start_time");
        String endText = readText(body, "end_time");
        if (locationId == null || title == null || startText == null || endText == null) {
            log.warn("booking event {} malformed (title/location_id/start_time/end_time missing); skipping",
                    bookingId);
            return;
        }
        FeedBooking fb = feedBookingRepository.findById(bookingId).orElseGet(FeedBooking::new);
        fb.setBookingId(bookingId);
        fb.setTitle(title);
        fb.setLocationId(locationId);
        fb.setLocationName(resolveLocationName(locationId)); // cache hit -> real name; miss -> placeholder
        fb.setStartTime(Instant.parse(startText));
        fb.setEndTime(Instant.parse(endText));
        fb.setStatus(status);
        feedBookingRepository.save(fb);
    }

    private String resolveLocationName(UUID locationId) {
        return locationNameRepository.findById(locationId)
                .map(LocationName::getName)
                .orElse("Location " + locationId); // graceful placeholder; a later location.updated back-fills
    }

    private static String actionOf(String routingKey) {
        if (routingKey == null) {
            return "";
        }
        int dot = routingKey.lastIndexOf('.');
        return dot >= 0 ? routingKey.substring(dot + 1) : routingKey;
    }

    private static UUID readUuid(JsonNode body, String field) {
        String text = readText(body, field);
        if (text == null) {
            return null;
        }
        try {
            return UUID.fromString(text);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static String readText(JsonNode body, String field) {
        if (body == null) {
            return null;
        }
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asText();
    }
}
