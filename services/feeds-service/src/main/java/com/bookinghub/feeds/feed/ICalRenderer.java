package com.bookinghub.feeds.feed;

import com.bookinghub.feeds.dto.FeedBookingSummary;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Hand-built iCalendar (RFC 5545) renderer (named decision: no ical4j dependency).
 *
 * Emits a VCALENDAR with one VEVENT per approved-upcoming booking, using only the
 * required, subscribable VEVENT fields (UID/DTSTAMP/DTSTART/DTEND/SUMMARY/LOCATION).
 * Lines are CRLF-terminated per RFC 5545 §3.1; times are UTC "Z" basic format.
 * ALL title/location text is iCal-escaped (RFC 5545 §3.3.11: backslash, semicolon,
 * comma, newline) because it is upstream-controlled (threat T-07-03-03).
 */
@Component
public class ICalRenderer {

    private static final DateTimeFormatter ICAL_UTC =
        DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    /** RFC 5545 line terminator. */
    private static final String CRLF = "\r\n";

    /**
     * Renders the given bookings as an iCalendar document.
     * The list may be empty — the result is a valid VCALENDAR with zero VEVENTs.
     */
    public String render(List<FeedBookingSummary> items) {
        String dtstamp = ICAL_UTC.format(Instant.now());
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR").append(CRLF);
        sb.append("VERSION:2.0").append(CRLF);
        sb.append("PRODID:-//Booking-Hub//Feeds//EN").append(CRLF);
        sb.append("CALSCALE:GREGORIAN").append(CRLF);
        for (FeedBookingSummary item : items) {
            String locationName = item.location() != null ? item.location().name() : "";
            sb.append("BEGIN:VEVENT").append(CRLF);
            sb.append("UID:").append(item.bookingId()).append("@booking-hub").append(CRLF);
            sb.append("DTSTAMP:").append(dtstamp).append(CRLF);
            sb.append("DTSTART:").append(ICAL_UTC.format(item.startTime())).append(CRLF);
            sb.append("DTEND:").append(ICAL_UTC.format(item.endTime())).append(CRLF);
            sb.append("SUMMARY:").append(icalEscape(item.title())).append(CRLF);
            sb.append("LOCATION:").append(icalEscape(locationName)).append(CRLF);
            sb.append("END:VEVENT").append(CRLF);
        }
        sb.append("END:VCALENDAR").append(CRLF);
        return sb.toString();
    }

    /**
     * Escapes a text value per RFC 5545 §3.3.11: backslash first, then semicolon,
     * comma, and newline. Applied to EVERY upstream-controlled string.
     */
    static String icalEscape(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\' -> out.append("\\\\");
                case ';' -> out.append("\\;");
                case ',' -> out.append("\\,");
                case '\n' -> out.append("\\n");
                case '\r' -> { /* drop bare CR; a following \n produces the escaped newline */ }
                default -> out.append(c);
            }
        }
        return out.toString();
    }
}
