package com.bookinghub.feeds.feed;

import com.bookinghub.feeds.domain.FeedBooking;
import com.bookinghub.feeds.dto.FeedBookingSummary;
import com.bookinghub.feeds.error.ApiException.FeedLocationFilterInvalidException;
import com.bookinghub.feeds.repository.FeedBookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * The SINGLE shared access/filter path every feed format calls.
 *
 * All four public endpoints (RSS2, iCal, JSON, display-board) delegate to
 * {@link #getApprovedUpcoming(String)}, so the access rule (none) and the filter
 * rule (status = {@code approved} AND {@code start_time >= now}, ordered by
 * start_time, with an optional per-location filter) live in exactly one place.
 * This is what makes Phase 7 Success Criterion 3 ("access rules are identical
 * across all four formats") structurally true rather than something to re-verify
 * format by format.
 *
 * Behavior pinned here, identical for every format:
 * <ul>
 *   <li>no location_id → approved + upcoming, all locations</li>
 *   <li>well-formed but UNKNOWN location_id → matches no rows → empty feed, HTTP 200</li>
 *   <li>MALFORMED (non-UUID) location_id → {@link FeedLocationFilterInvalidException}
 *       (400 FEED_LOCATION_FILTER_INVALID), thrown at parse time before any DB hit
 *       so a malformed filter never produces a 500</li>
 * </ul>
 *
 * This is a pure query/mapping component — it has NO header or caching concerns.
 * The ETag is computed in the controller from the rendered response body.
 */
@Service
public class FeedQueryService {

    private final FeedBookingRepository repo;

    public FeedQueryService(FeedBookingRepository repo) {
        this.repo = repo;
    }

    /**
     * Returns the approved, upcoming (start_time &gt;= now) bookings ordered by
     * start_time, optionally scoped to a single location.
     *
     * @param locationIdParam the raw {@code location_id} query param (may be null
     *                        or blank for "all locations"); a non-null, non-blank
     *                        value must parse as a UUID
     * @return the matching bookings mapped to the public §4.8 DTO shape; empty if
     *         none match (including an unknown-but-well-formed location_id)
     * @throws FeedLocationFilterInvalidException if a present location_id is not a
     *         parseable UUID (400, never a 500)
     */
    @Transactional(readOnly = true)
    public List<FeedBookingSummary> getApprovedUpcoming(String locationIdParam) {
        Instant now = Instant.now();
        List<FeedBooking> rows;
        if (locationIdParam == null || locationIdParam.isBlank()) {
            rows = repo.findByStatusAndStartTimeGreaterThanEqualOrderByStartTimeAsc("approved", now);
        } else {
            UUID locationId;
            try {
                locationId = UUID.fromString(locationIdParam);
            } catch (IllegalArgumentException e) {
                // Malformed filter: a clean 400, not a 500 — caught before any DB access.
                throw new FeedLocationFilterInvalidException("location_id is not a valid UUID");
            }
            // A well-formed but unknown location_id simply matches no rows → empty
            // list → empty feed, HTTP 200 (Phase 7 Success Criterion 2).
            rows = repo.findByStatusAndLocationIdAndStartTimeGreaterThanEqualOrderByStartTimeAsc(
                "approved", locationId, now);
        }
        return rows.stream().map(FeedQueryService::toSummary).toList();
    }

    /**
     * Maps a projection row to the public §4.8 DTO, nesting the denormalized
     * location id/name into the {@code location} object.
     */
    private static FeedBookingSummary toSummary(FeedBooking b) {
        return new FeedBookingSummary(
            b.getBookingId(),
            b.getTitle(),
            new FeedBookingSummary.LocationRef(b.getLocationId(), b.getLocationName()),
            b.getStartTime(),
            b.getEndTime()
        );
    }
}
