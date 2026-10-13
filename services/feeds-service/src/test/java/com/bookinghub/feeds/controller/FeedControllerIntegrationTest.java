package com.bookinghub.feeds.controller;

import com.bookinghub.feeds.domain.FeedBooking;
import com.bookinghub.feeds.dto.FeedBookingSummary;
import com.bookinghub.feeds.repository.FeedBookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-stack integration suite for the four public feed endpoints — the end-to-end
 * proof of Phase 7's three Success Criteria.
 *
 * Seeds feed_bookings rows DIRECTLY via FeedBookingRepository (this is the READ
 * side — it bypasses plan 07-02's consumer) and drives real HTTP requests with
 * NO auth header via TestRestTemplate. It asserts, for every format identically:
 *   (1) only approved + upcoming bookings surface (never pending/denied/past),
 *   (2) the per-location filter is applied consistently, unknown→empty-200,
 *       malformed→400 FEED_LOCATION_FILTER_INVALID,
 *   (3) all four are reachable with zero authentication,
 * plus the cacheability headers (Cache-Control, deterministic SHA-256 ETag) and
 * the ical Content-Disposition.
 *
 * Testcontainers workaround (same as Phase 4 plan 04-01 / Phase 6 plan 06-03 and
 * this service's own FeedBookingSchemaTest): this sandbox's Docker daemon reports
 * client API version 1.32, below Testcontainers' 1.40 minimum, so
 * {@code @Testcontainers}/PostgreSQLContainer cannot start. The test runs against
 * the running docker-compose Postgres via application-test.properties
 * ({@code @ActiveProfiles("test")}). Flyway builds the real V1+V2 schema there.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FeedControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FeedBookingRepository repo;

    private UUID locationL1;
    private UUID locationL2;
    private String titleA;
    private String titleB;

    private FeedBooking row(String title, String status, UUID locationId, String locationName, Instant start) {
        FeedBooking b = new FeedBooking();
        b.setBookingId(UUID.randomUUID());
        b.setTitle(title);
        b.setLocationId(locationId);
        b.setLocationName(locationName);
        b.setStartTime(start);
        b.setEndTime(start.plus(1, ChronoUnit.HOURS));
        b.setStatus(status);
        return b;
    }

    @BeforeEach
    void seed() {
        // Clean slate: these are real cross-transaction HTTP calls, so wipe first.
        repo.deleteAll();

        Instant now = Instant.now();
        locationL1 = UUID.randomUUID();
        locationL2 = UUID.randomUUID();
        titleA = "Standup A " + UUID.randomUUID();
        titleB = "Design B " + UUID.randomUUID();

        // (A) approved + upcoming at L1  -> MUST appear
        repo.save(row(titleA, "approved", locationL1, "Room One", now.plus(1, ChronoUnit.HOURS)));
        // (B) approved + upcoming at L2  -> MUST appear
        repo.save(row(titleB, "approved", locationL2, "Room Two", now.plus(2, ChronoUnit.HOURS)));
        // (C) approved but PAST          -> MUST NOT appear (proves >= now boundary)
        repo.save(row("Past C " + UUID.randomUUID(), "approved", locationL1, "Room One", now.minus(1, ChronoUnit.HOURS)));
        // (D) pending + upcoming         -> MUST NOT appear (proves status filter)
        repo.save(row("Pending D " + UUID.randomUUID(), "pending", locationL1, "Room One", now.plus(3, ChronoUnit.HOURS)));
        // (E) denied + upcoming          -> MUST NOT appear (defense in depth atop 07-02's projection removal)
        repo.save(row("Denied E " + UUID.randomUUID(), "denied", locationL1, "Room One", now.plus(4, ChronoUnit.HOURS)));
    }

    // ---- 1. JSON: no auth, only approved+upcoming, exact §4.8 shape ----

    @Test
    void json_noAuth_returnsOnlyApprovedUpcoming_inExactShape() {
        ResponseEntity<List<FeedBookingSummary>> resp = restTemplate.exchange(
            "/feeds/json", org.springframework.http.HttpMethod.GET, null,
            new ParameterizedTypeReference<List<FeedBookingSummary>>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getHeaders().getContentType().toString()).startsWith("application/json");
        List<FeedBookingSummary> body = resp.getBody();
        assertThat(body).extracting(FeedBookingSummary::title).containsExactlyInAnyOrder(titleA, titleB);
        // nested location shape populated
        assertThat(body).allSatisfy(s -> {
            assertThat(s.bookingId()).isNotNull();
            assertThat(s.location()).isNotNull();
            assertThat(s.location().id()).isNotNull();
            assertThat(s.location().name()).isNotBlank();
            assertThat(s.startTime()).isNotNull();
            assertThat(s.endTime()).isNotNull();
        });
    }

    @Test
    void json_serializesTimesAsIso8601Strings_notNumericTimestamps() {
        ResponseEntity<String> raw = restTemplate.getForEntity("/feeds/json", String.class);
        assertThat(raw.getStatusCode()).isEqualTo(HttpStatus.OK);
        // ISO-8601 instants contain a 'T' and end in 'Z'; numeric timestamps would be bare digits.
        assertThat(raw.getBody()).contains("\"start_time\":\"");
        assertThat(raw.getBody()).contains("\"booking_id\":\"");
        assertThat(raw.getBody()).contains("\"location\":{");
        assertThat(raw.getBody()).doesNotContain("\"start_time\":1"); // not an epoch-millis number
    }

    // ---- 2. RSS2: no auth, only approved+upcoming ----

    @Test
    void rss2_noAuth_returnsApprovedUpcomingOnly() {
        ResponseEntity<String> resp = restTemplate.getForEntity("/feeds/rss2", String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getHeaders().getContentType().toString()).startsWith("application/rss+xml");
        assertThat(resp.getBody()).contains("<rss");
        assertThat(resp.getBody()).contains(titleA).contains(titleB);
        assertThat(resp.getBody()).doesNotContain("Past C").doesNotContain("Pending D").doesNotContain("Denied E");
    }

    // ---- 3. iCal: no auth, two VEVENTs ----

    @Test
    void ical_noAuth_returnsTwoVevents() {
        ResponseEntity<String> resp = restTemplate.getForEntity("/feeds/ical", String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getHeaders().getContentType().toString()).startsWith("text/calendar");
        assertThat(resp.getBody()).contains("BEGIN:VCALENDAR");
        assertThat(countOccurrences(resp.getBody(), "BEGIN:VEVENT")).isEqualTo(2);
        assertThat(resp.getBody()).doesNotContain("Past C").doesNotContain("Pending D").doesNotContain("Denied E");
    }

    // ---- 4. display-board: no auth, auto-refresh, renders ----

    @Test
    void displayBoard_noAuth_rendersAutoRefreshWithBookings() {
        ResponseEntity<String> resp = restTemplate.getForEntity("/feeds/display-board", String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getHeaders().getContentType().toString()).startsWith("text/html");
        assertThat(resp.getBody()).contains("<meta http-equiv=\"refresh\"");
        assertThat(resp.getBody()).contains(titleA).contains(titleB);
    }

    // ---- 5. per-location filter applied consistently across formats ----

    @Test
    void locationFilter_appliedConsistentlyAcrossFormats() {
        // JSON: L1 -> only A
        ResponseEntity<List<FeedBookingSummary>> json = restTemplate.exchange(
            "/feeds/json?location_id=" + locationL1, org.springframework.http.HttpMethod.GET, null,
            new ParameterizedTypeReference<List<FeedBookingSummary>>() {});
        assertThat(json.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(json.getBody()).extracting(FeedBookingSummary::title).containsExactly(titleA);

        // RSS2: L1 -> contains A, not B
        ResponseEntity<String> rss = restTemplate.getForEntity("/feeds/rss2?location_id=" + locationL1, String.class);
        assertThat(rss.getBody()).contains(titleA).doesNotContain(titleB);

        // iCal: L1 -> exactly one VEVENT (A)
        ResponseEntity<String> ical = restTemplate.getForEntity("/feeds/ical?location_id=" + locationL1, String.class);
        assertThat(countOccurrences(ical.getBody(), "BEGIN:VEVENT")).isEqualTo(1);
        assertThat(ical.getBody()).contains(titleA);
    }

    // ---- 6. unknown location_id -> empty feed, 200 (not an error) ----

    @Test
    void unknownLocationId_returnsEmptyFeed200() {
        ResponseEntity<List<FeedBookingSummary>> json = restTemplate.exchange(
            "/feeds/json?location_id=" + UUID.randomUUID(), org.springframework.http.HttpMethod.GET, null,
            new ParameterizedTypeReference<List<FeedBookingSummary>>() {});
        assertThat(json.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(json.getBody()).isEmpty();
    }

    // ---- 7. malformed location_id -> clean 400, identical code across formats ----

    @Test
    void malformedLocationId_returns400SameCodeAcrossFormats() {
        ResponseEntity<String> json = restTemplate.getForEntity("/feeds/json?location_id=not-a-uuid", String.class);
        assertThat(json.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(json.getBody()).contains("\"error_code\":\"FEED_LOCATION_FILTER_INVALID\"");

        ResponseEntity<String> rss = restTemplate.getForEntity("/feeds/rss2?location_id=not-a-uuid", String.class);
        assertThat(rss.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(rss.getBody()).contains("\"error_code\":\"FEED_LOCATION_FILTER_INVALID\"");
    }

    // ---- 8. cacheability headers + ETag determinism + ical Content-Disposition ----

    @Test
    void cacheHeaders_present_andEtagDeterministic() {
        ResponseEntity<String> rss1 = restTemplate.getForEntity("/feeds/rss2", String.class);
        ResponseEntity<String> rss2 = restTemplate.getForEntity("/feeds/rss2", String.class);

        assertThat(rss1.getHeaders().getCacheControl()).isNotBlank();
        assertThat(rss1.getHeaders().getETag()).isNotBlank();
        // Same unchanged projection -> identical strong validator across repeat requests.
        assertThat(rss1.getHeaders().getETag()).isEqualTo(rss2.getHeaders().getETag());

        ResponseEntity<String> json = restTemplate.getForEntity("/feeds/json", String.class);
        assertThat(json.getHeaders().getCacheControl()).isNotBlank();
        assertThat(json.getHeaders().getETag()).isNotBlank();

        ResponseEntity<String> ical = restTemplate.getForEntity("/feeds/ical", String.class);
        assertThat(ical.getHeaders().getCacheControl()).isNotBlank();
        assertThat(ical.getHeaders().getETag()).isNotBlank();
        assertThat(ical.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
            .isEqualTo("inline; filename=\"bookings.ics\"");

        ResponseEntity<String> board = restTemplate.getForEntity("/feeds/display-board", String.class);
        assertThat(board.getHeaders().getCacheControl()).isNotBlank();
    }

    private static int countOccurrences(String haystack, String needle) {
        int count = 0;
        int idx = 0;
        while ((idx = haystack.indexOf(needle, idx)) != -1) {
            count++;
            idx += needle.length();
        }
        return count;
    }
}
