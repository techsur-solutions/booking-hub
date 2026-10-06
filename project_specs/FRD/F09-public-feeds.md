## F9: Public Feeds

**Description:** The Public Feed Service exposes read-only, read-optimized views of approved upcoming Bookings in multiple formats — RSS2, iCal, JSON, and a digital-signage "display board" view — equivalent to legacy `Api` controller, with optional per-location filtering and access control equivalent to legacy `allowAPI`.

**Terminology:**
- **Approved upcoming booking:** A Booking with `status = approved` and `start_time` (or `end_time`) in the future relative to the feed request time.
- **Display board:** An auto-refreshing, screen-friendly HTML/visual view intended for lobby/corridor digital signage, not a machine-readable feed format.
- **`allowAPI` permission:** The legacy permission flag gating non-public feed access, re-implemented via Keycloak (F7).

**Sub-features:**
- RSS2 feed
- iCal (subscribable calendar) feed
- JSON/API feed
- Digital-signage display board view
- Per-location filtering on all feed formats
- Feed access control via `allowAPI`

**Process:**
1. Client requests a feed in a given format (RSS2, iCal, JSON, or display board), optionally with a `location_id` filter query parameter.
2. Service determines access control for the request: `[OPEN QUESTION — deferred to F0: are feeds fully public by default, or always gated behind allowAPI/a token? — PRD Open Question #7]`; interim default pending F0 confirmation: feeds require a valid `allowAPI`-scoped token or a feed-specific access token, consistent with the conservative default-deny posture used elsewhere in this FRD for unconfirmed access rules.
3. If access is granted, service queries the Booking Service's read model (or a denormalized feed-optimized view, per PRD Risk mitigation on cross-domain read patterns) for all Bookings matching `status = approved` and `start_time >= now` (or configured lookback/lookahead window), filtered by `location_id` if supplied.
4. Service renders the result in the requested format:
   - **RSS2:** standard RSS 2.0 XML with one `<item>` per Booking (title, location, time, link).
   - **iCal:** standard iCalendar (`.ics`) format with one `VEVENT` per Booking, subscribable by calendar clients.
   - **JSON:** structured JSON array of Booking summaries.
   - **Display board:** server-rendered HTML view styled for signage display, auto-refreshing client-side (e.g., via meta-refresh or polling) to stay current.
5. Per-location filtering applies uniformly across all four formats `[OPEN QUESTION — deferred to F0: confirm uniform applicability — PRD Open Question #7]`.
6. Custom field values are included in feed output only if confirmed exposable per F5 §Process step 6 (pending F0 confirmation); otherwise feeds expose only standard Booking fields (title, location, time).
7. Feed responses are cacheable (e.g., with appropriate `Cache-Control`/`ETag` headers) given their read-heavy, publicly-consumed nature, without compromising the access-control check in step 2.

**Inputs:**
- `format` (enum, required, typically via route/path or `Accept` header): `rss2` | `ical` | `json` | `display-board`.
- `location_id` (UUID/long, optional): Filter to a single Location.
- `access_token` (string, required if feeds are gated per step 2 interim default): Credential proving `allowAPI` access.

**Outputs:**
- RSS2 XML document.
- iCal `.ics` document.
- JSON array: `[{booking_id, title, location, start_time, end_time}, ...]`.
- Display board HTML page.

**Validation:**
- Only Bookings with `status = approved` ever appear in any feed format — `pending` and `denied` Bookings are never exposed, regardless of access level (no role sees unapproved bookings via the public feed surface; internal views use F1's endpoints instead).
- `location_id`, if supplied, must reference an existing Location; an unknown `location_id` returns an empty feed (not an error) to avoid leaking location-existence information differently across feed vs. internal endpoints `[confirm this parity choice against legacy behavior in F0]`.
- Feed access control (step 2) is evaluated identically regardless of format — a caller denied JSON access is equally denied RSS2/iCal/display-board access for the same scope, per the single-conflict-logic-path principle applied analogously here.

**Error States:**
| Scenario | HTTP Status | Error Code | Message |
|---|---|---|---|
| Feed requested without required `allowAPI` access (pending F0 confirmation of default-public vs. gated) | 401 or 403 | FEED_FORBIDDEN | "Access to this feed requires API access" |
| Unsupported `format` requested | 400 | FEED_FORMAT_UNSUPPORTED | "Requested feed format is not supported" |
| `location_id` filter references a non-existent Location | 200 (empty feed, per Validation) | — | — |
| Upstream Booking read model unavailable | 503 | FEED_SOURCE_UNAVAILABLE | "Unable to retrieve booking feed at this time" |

**API Surface (this feature):** see `Y1-api.md` §Feeds (`GET /feeds/rss2`, `GET /feeds/ical`, `GET /feeds/json`, `GET /feeds/display-board`).

**Schema Surface (this feature):** no independently owned transactional table; reads a denormalized/read-optimized view sourced from the Booking Service's `bookings` data (via API composition or an event-driven read-model projection, per PRD's cross-domain read-pattern mitigation) — see `Y0-schema.md` §Feed for the projected read-model shape, if materialized.
