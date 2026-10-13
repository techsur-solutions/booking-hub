package com.bookinghub.feeds.feed;

import com.bookinghub.feeds.dto.FeedBookingSummary;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Hand-built RSS 2.0 renderer (named decision: no ical4j/rome dependency on this
 * single internet-facing public service — minimal attack surface, exact control
 * over escaping of upstream-controlled title/location text).
 *
 * Produces a {@code channel} with one {@code item} per approved-upcoming booking.
 * ALL title/location strings are XML-entity-escaped ({@code & < > " '}) because
 * booking titles and location names are upstream-controlled and must not break
 * the XML or inject markup into a consumer (threat T-07-03-03).
 */
@Component
public class Rss2Renderer {

    private static final DateTimeFormatter RFC_1123 =
        DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneOffset.UTC);

    private static final DateTimeFormatter HUMAN_TIME =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'").withZone(ZoneOffset.UTC);

    /**
     * Renders the given bookings as an RSS 2.0 document.
     * The list may be empty — the result is a valid channel with zero items.
     */
    public String render(List<FeedBookingSummary> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<rss version=\"2.0\"><channel>\n");
        sb.append("  <title>Booking-Hub — Upcoming Approved Bookings</title>\n");
        sb.append("  <link>/feeds/rss2</link>\n");
        sb.append("  <description>Approved upcoming room bookings</description>\n");
        for (FeedBookingSummary item : items) {
            String locationName = item.location() != null ? item.location().name() : "";
            String desc = locationName
                + " — "
                + HUMAN_TIME.format(item.startTime())
                + " to "
                + HUMAN_TIME.format(item.endTime());
            sb.append("  <item>\n");
            sb.append("    <title>").append(xmlEscape(item.title())).append("</title>\n");
            sb.append("    <description>").append(xmlEscape(desc)).append("</description>\n");
            sb.append("    <guid isPermaLink=\"false\">").append(xmlEscape(String.valueOf(item.bookingId()))).append("</guid>\n");
            sb.append("    <pubDate>").append(RFC_1123.format(item.startTime())).append("</pubDate>\n");
            sb.append("  </item>\n");
        }
        sb.append("</channel></rss>");
        return sb.toString();
    }

    /**
     * Escapes the five XML predefined entities. Applied to EVERY upstream-controlled
     * string that lands in the document — the injection-safety guarantee.
     */
    static String xmlEscape(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                case '\'' -> out.append("&apos;");
                default -> out.append(c);
            }
        }
        return out.toString();
    }
}
