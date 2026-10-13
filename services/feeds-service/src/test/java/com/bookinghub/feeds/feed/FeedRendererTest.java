package com.bookinghub.feeds.feed;

import com.bookinghub.feeds.dto.FeedBookingSummary;
import com.bookinghub.feeds.dto.FeedBookingSummary.LocationRef;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Plain unit tests (no Spring context) for the three hand-built renderers.
 *
 * They prove the two guarantees that matter for this public, internet-facing
 * service: structural correctness of each format, and format-correct escaping of
 * upstream-controlled title/location text (threat T-07-03-03). The empty-list
 * case is asserted for the display board (graceful signage state).
 */
class FeedRendererTest {

    private static FeedBookingSummary booking(String title, String locationName, Instant start) {
        return new FeedBookingSummary(
            UUID.randomUUID(),
            title,
            new LocationRef(UUID.randomUUID(), locationName),
            start,
            start.plus(1, ChronoUnit.HOURS)
        );
    }

    @Test
    void rss2_rendersTwoItemsWithEscapedUpstreamText() {
        Rss2Renderer renderer = new Rss2Renderer();
        Instant now = Instant.now();
        FeedBookingSummary a = booking("Standup & <Review>", "Room A", now.plus(1, ChronoUnit.HOURS));
        FeedBookingSummary b = booking("Design Sync", "Room B", now.plus(2, ChronoUnit.HOURS));

        String xml = renderer.render(List.of(a, b));

        assertThat(xml).contains("<rss version=\"2.0\">");
        // exactly two items
        assertThat(countOccurrences(xml, "<item>")).isEqualTo(2);
        // each booking's guid present
        assertThat(xml).contains(a.bookingId().toString());
        assertThat(xml).contains(b.bookingId().toString());
        // injection-safety: & and < appear escaped, never raw in the title
        assertThat(xml).contains("Standup &amp; &lt;Review&gt;");
        assertThat(xml).doesNotContain("Standup & <Review>");
        // the plain title is present (escaped form proven above)
        assertThat(xml).contains("Design Sync");
    }

    @Test
    void ical_rendersTwoVeventsWithCrlfAndRfc5545Escaping() {
        ICalRenderer renderer = new ICalRenderer();
        Instant now = Instant.now();
        FeedBookingSummary a = booking("Lunch, with team; notes", "Room A", now.plus(1, ChronoUnit.HOURS));
        FeedBookingSummary b = booking("Design Sync", "Room B", now.plus(2, ChronoUnit.HOURS));

        String ics = renderer.render(List.of(a, b));

        assertThat(ics).contains("BEGIN:VCALENDAR");
        assertThat(ics).contains("END:VCALENDAR");
        assertThat(countOccurrences(ics, "BEGIN:VEVENT")).isEqualTo(2);
        // required VEVENT fields present
        assertThat(ics).contains("UID:" + a.bookingId() + "@booking-hub");
        assertThat(ics).contains("DTSTART:");
        assertThat(ics).contains("DTEND:");
        assertThat(ics).contains("SUMMARY:");
        // RFC 5545 escaping: comma and semicolon backslash-escaped in the summary
        assertThat(ics).contains("SUMMARY:Lunch\\, with team\\; notes");
        // lines are CRLF-terminated
        assertThat(ics).contains("BEGIN:VCALENDAR\r\n");
        assertThat(ics).contains("END:VEVENT\r\n");
    }

    @Test
    void displayBoard_rendersAutoRefreshEscapedItems() {
        DisplayBoardRenderer renderer = new DisplayBoardRenderer();
        Instant now = Instant.now();
        FeedBookingSummary a = booking("Q&A <session>", "Room A", now.plus(1, ChronoUnit.HOURS));

        String html = renderer.render(List.of(a), null);

        assertThat(html).contains("<meta http-equiv=\"refresh\"");
        // upstream text HTML-escaped
        assertThat(html).contains("Q&amp;A &lt;session&gt;");
        assertThat(html).doesNotContain("Q&A <session>");
        assertThat(html).contains("Room A");
    }

    @Test
    void displayBoard_rendersGracefulEmptyState() {
        DisplayBoardRenderer renderer = new DisplayBoardRenderer();

        String html = renderer.render(List.of(), null);

        assertThat(html).contains("<meta http-equiv=\"refresh\"");
        assertThat(html).contains("No upcoming bookings");
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
