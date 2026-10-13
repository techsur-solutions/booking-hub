package com.bookinghub.feeds.controller;

import com.bookinghub.feeds.dto.FeedBookingSummary;
import com.bookinghub.feeds.feed.DisplayBoardRenderer;
import com.bookinghub.feeds.feed.FeedQueryService;
import com.bookinghub.feeds.feed.ICalRenderer;
import com.bookinghub.feeds.feed.Rss2Renderer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

/**
 * The four PUBLIC feed endpoints — the user-facing outcome of Phase 7.
 *
 * Every endpoint is a GET mapping with NO security annotation and there is
 * NO SecurityFilterChain in this service: the endpoints are public by construction
 * (F9.6), matching the gateway's existing permitAll on {@code /feeds/**}. The
 * GET-only surface means a public caller has read-only access by construction
 * (threat T-07-03-01).
 *
 * All four delegate to the SAME {@link FeedQueryService#getApprovedUpcoming(String)}
 * — so the access rule (none), the approved+upcoming filter, the optional location
 * filter, unknown→empty-200 and malformed→400 behavior are byte-for-byte identical
 * across rss2/ical/json/display-board (Phase 7 Success Criteria 2 & 3). A
 * malformed location_id throws FeedLocationFilterInvalidException before any header
 * is set, so the 400 ApiError (via plan 07-01's GlobalExceptionHandler) is the same
 * for every format and no partial/cached error response is possible.
 *
 * ETag strategy (pinned): a SHA-256 hash of the rendered response body, hex-encoded,
 * returned as a quoted strong validator. It is deterministic across replicas — the
 * same projection state renders the same body, hence the same ETag on any
 * load-balanced pod (enables CDN/proxy caching per TechArch §2.7).
 */
@RestController
@RequestMapping("/feeds")
public class FeedController {

    private final FeedQueryService feedQueryService;
    private final Rss2Renderer rss2Renderer;
    private final ICalRenderer iCalRenderer;
    private final DisplayBoardRenderer displayBoardRenderer;

    public FeedController(FeedQueryService feedQueryService,
                         Rss2Renderer rss2Renderer,
                         ICalRenderer iCalRenderer,
                         DisplayBoardRenderer displayBoardRenderer) {
        this.feedQueryService = feedQueryService;
        this.rss2Renderer = rss2Renderer;
        this.iCalRenderer = iCalRenderer;
        this.displayBoardRenderer = displayBoardRenderer;
    }

    @GetMapping(value = "/rss2", produces = "application/rss+xml;charset=UTF-8")
    public ResponseEntity<String> rss2(@RequestParam(name = "location_id", required = false) String locationId) {
        List<FeedBookingSummary> items = feedQueryService.getApprovedUpcoming(locationId);
        String body = rss2Renderer.render(items);
        return cachedString(body, "application/rss+xml;charset=UTF-8");
    }

    @GetMapping(value = "/ical", produces = "text/calendar;charset=UTF-8")
    public ResponseEntity<String> ical(@RequestParam(name = "location_id", required = false) String locationId) {
        List<FeedBookingSummary> items = feedQueryService.getApprovedUpcoming(locationId);
        String body = iCalRenderer.render(items);
        return ResponseEntity.ok()
            .eTag(etagOf(body))
            .header(HttpHeaders.CACHE_CONTROL, "public, max-age=60")
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"bookings.ics\"")
            .contentType(MediaType.parseMediaType("text/calendar;charset=UTF-8"))
            .body(body);
    }

    @GetMapping(value = "/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<FeedBookingSummary>> json(@RequestParam(name = "location_id", required = false) String locationId) {
        List<FeedBookingSummary> items = feedQueryService.getApprovedUpcoming(locationId);
        return ResponseEntity.ok()
            .eTag(etagOf(items.toString()))
            .header(HttpHeaders.CACHE_CONTROL, "public, max-age=60")
            .contentType(MediaType.APPLICATION_JSON)
            .body(items);
    }

    @GetMapping(value = "/display-board", produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> displayBoard(@RequestParam(name = "location_id", required = false) String locationId) {
        List<FeedBookingSummary> items = feedQueryService.getApprovedUpcoming(locationId);
        // Live signage view: a short max-age, no strong ETag (it is expected to re-fetch).
        return ResponseEntity.ok()
            .header(HttpHeaders.CACHE_CONTROL, "public, max-age=30")
            .contentType(MediaType.parseMediaType("text/html;charset=UTF-8"))
            .body(displayBoardRenderer.render(items, locationId));
    }

    // --- private helpers ---

    /**
     * Wraps a rendered string body with the standard public cache headers and a
     * deterministic SHA-256-of-body ETag. Used by the rss2 endpoint directly; the
     * ical endpoint builds its own so it can add Content-Disposition.
     */
    private ResponseEntity<String> cachedString(String body, String contentType) {
        return ResponseEntity.ok()
            .eTag(etagOf(body))
            .header(HttpHeaders.CACHE_CONTROL, "public, max-age=60")
            .contentType(MediaType.parseMediaType(contentType))
            .body(body);
    }

    /**
     * Returns the bare hex SHA-256 of the given content. {@code .eTag(...)} already
     * wraps the value in the required double-quotes, so this returns the unquoted hex.
     */
    private String etagOf(String body) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(body.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e); // never on a standard JRE
        }
    }
}
