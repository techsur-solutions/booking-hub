package com.bookinghub.feeds.feed;

import com.bookinghub.feeds.dto.FeedBookingSummary;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Hand-built full-screen "display board" HTML renderer — the kiosk signage view
 * (TechArch §2.7/§2.9). A self-contained, server-rendered HTML page loaded by
 * pasting its URL into a kiosk browser; it is NOT wired into the React app's nav.
 *
 * Auto-refresh is a {@code <meta http-equiv="refresh" content="60">} with NO
 * {@code url=} — so the browser re-fetches the CURRENT URL, which already carries
 * any {@code ?location_id=}. This preserves the location filter across refreshes
 * with zero URL reconstruction and no client JS.
 *
 * ALL title/location text is HTML-entity-escaped because it is upstream-controlled
 * (threat T-07-03-03). An empty list renders a friendly "No upcoming bookings"
 * state, never a blank screen.
 */
@Component
public class DisplayBoardRenderer {

    private static final DateTimeFormatter HUMAN_TIME =
        DateTimeFormatter.ofPattern("EEE d MMM, HH:mm 'UTC'").withZone(ZoneOffset.UTC);

    /**
     * Renders the signage HTML page.
     *
     * @param items      approved-upcoming bookings (may be empty)
     * @param locationId the raw location_id param, if any — not re-emitted into the
     *                   refresh (the meta refresh reuses the current URL), kept in
     *                   the signature so callers can pass it without special-casing
     */
    public String render(List<FeedBookingSummary> items, String locationId) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang=\"en\">\n");
        sb.append("<head>\n");
        sb.append("  <meta charset=\"UTF-8\">\n");
        // Auto-refresh every 60s; no url= so it refreshes the current URL (preserving ?location_id=).
        sb.append("  <meta http-equiv=\"refresh\" content=\"60\">\n");
        sb.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
        sb.append("  <title>Upcoming Approved Bookings</title>\n");
        sb.append("  <style>\n");
        sb.append("    * { box-sizing: border-box; margin: 0; padding: 0; }\n");
        sb.append("    html, body { width: 100%; min-height: 100vh; background: #0b1020; color: #f5f7ff;\n");
        sb.append("      font-family: 'Segoe UI', Arial, sans-serif; }\n");
        sb.append("    header { padding: 2rem 3rem 1rem; border-bottom: 2px solid #2a3356; }\n");
        sb.append("    header h1 { font-size: 3rem; letter-spacing: 0.02em; }\n");
        sb.append("    main { padding: 1.5rem 3rem; }\n");
        sb.append("    .row { display: flex; justify-content: space-between; align-items: baseline;\n");
        sb.append("      padding: 1.25rem 0; border-bottom: 1px solid #1c2440; }\n");
        sb.append("    .title { font-size: 2.6rem; font-weight: 600; }\n");
        sb.append("    .location { font-size: 1.8rem; color: #9fb3ff; }\n");
        sb.append("    .time { font-size: 1.8rem; color: #c9d4ff; white-space: nowrap; }\n");
        sb.append("    .empty { font-size: 2.4rem; color: #9fb3ff; padding: 4rem 0; text-align: center; }\n");
        sb.append("  </style>\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append("  <header><h1>Upcoming Approved Bookings</h1></header>\n");
        sb.append("  <main>\n");
        if (items.isEmpty()) {
            sb.append("    <p class=\"empty\">No upcoming bookings</p>\n");
        } else {
            for (FeedBookingSummary item : items) {
                String locationName = item.location() != null ? item.location().name() : "";
                String time = HUMAN_TIME.format(item.startTime()) + " – " + HUMAN_TIME.format(item.endTime());
                sb.append("    <div class=\"row\">\n");
                sb.append("      <span class=\"title\">").append(htmlEscape(item.title())).append("</span>\n");
                sb.append("      <span class=\"location\">").append(htmlEscape(locationName)).append("</span>\n");
                sb.append("      <span class=\"time\">").append(htmlEscape(time)).append("</span>\n");
                sb.append("    </div>\n");
            }
        }
        sb.append("  </main>\n");
        sb.append("</body>\n");
        sb.append("</html>");
        return sb.toString();
    }

    /**
     * HTML-entity-escapes the five characters that matter in element text and
     * attribute contexts. Applied to EVERY upstream-controlled string.
     */
    static String htmlEscape(String s) {
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
                case '\'' -> out.append("&#39;");
                default -> out.append(c);
            }
        }
        return out.toString();
    }
}
