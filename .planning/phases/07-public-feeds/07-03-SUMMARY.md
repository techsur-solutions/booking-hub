---
phase: 07-public-feeds
plan: 03
subsystem: api
tags: [feeds, rss2, ical, json, display-board, public, spring-boot, etag, caching]

# Dependency graph
requires:
  - phase: 07-public-feeds (plan 07-01)
    provides: FeedBooking entity + FeedBookingRepository (approved-upcoming queries), ApiException.FeedLocationFilterInvalidException, GlobalExceptionHandler, public (security-free) service foundation
provides:
  - GET /feeds/rss2 — public RSS 2.0 feed of approved upcoming bookings
  - GET /feeds/ical — public iCalendar subscription (one VEVENT per booking)
  - GET /feeds/json — public JSON feed in exact TechArch §4.8 shape
  - GET /feeds/display-board — public auto-refreshing full-screen kiosk HTML
  - FeedQueryService — single shared access/filter path for all four formats
  - FeedBookingSummary — the public §4.8 DTO contract
affects: [frontend public-feed landing page, any downstream feed consumer]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Single shared query path (FeedQueryService) so access/filter rules are identical across all formats by construction"
    - "Hand-built RSS2/iCal/HTML renderers (no feed library) with format-correct escaping of upstream-controlled text"
    - "Deterministic SHA-256-of-body strong ETag for CDN/proxy cacheability across replicas"

key-files:
  created:
    - services/feeds-service/src/main/java/com/bookinghub/feeds/dto/FeedBookingSummary.java
    - services/feeds-service/src/main/java/com/bookinghub/feeds/feed/FeedQueryService.java
    - services/feeds-service/src/main/java/com/bookinghub/feeds/feed/Rss2Renderer.java
    - services/feeds-service/src/main/java/com/bookinghub/feeds/feed/ICalRenderer.java
    - services/feeds-service/src/main/java/com/bookinghub/feeds/feed/DisplayBoardRenderer.java
    - services/feeds-service/src/main/java/com/bookinghub/feeds/controller/FeedController.java
    - services/feeds-service/src/test/java/com/bookinghub/feeds/feed/FeedRendererTest.java
    - services/feeds-service/src/test/java/com/bookinghub/feeds/controller/FeedControllerIntegrationTest.java
  modified: []

key-decisions:
  - "Hand-built RSS2 and iCal (no ical4j/rome): minimal dependency/attack surface on the one public internet-facing service, exact control over field escaping"
  - "Display board is a standalone server-rendered kiosk HTML route with a 60s meta-refresh (no client JS), not a React nav page"
  - "ETag = SHA-256 hex of the rendered body (quoted strong validator): deterministic across load-balanced replicas for an unchanged projection"
  - "Integration test uses @ActiveProfiles(test) against docker-compose Postgres (not @Testcontainers): same sandbox Docker-API-version workaround as plan 07-01/Phase 4/Phase 6"

patterns-established:
  - "One access rule + one filter rule in one place: every format calls FeedQueryService.getApprovedUpcoming, making 'identical access across formats' structurally true"
  - "Upstream-controlled strings are format-escaped at render time (XML entity / RFC-5545 backslash / HTML entity), proven by unit tests feeding & < , ;"

# Metrics
duration: 8min
completed: 2026-10-13
---

# Phase 7 Plan 03: Public Feed Endpoints Summary

**Four public feed endpoints (RSS2, iCal, JSON, auto-refreshing display board) over a single shared approved-upcoming query path, with hand-built injection-safe renderers and deterministic SHA-256 ETags — zero authentication by construction.**

## Performance

- **Duration:** 8 min
- **Started:** 2026-10-13T02:33:00Z
- **Completed:** 2026-10-13T02:40:44Z
- **Tasks:** 3
- **Files modified:** 8 (all created)

## Accomplishments
- All four `/feeds/*` endpoints are reachable with ZERO authentication (no `@PreAuthorize`/`@Secured`, no SecurityFilterChain) — Success Criterion 3, proven end to end.
- Every format shows ONLY `status=approved AND start_time>=now` bookings ordered by start_time — pending/denied/past never appear (Success Criterion 1).
- Optional `?location_id=` applied identically across all four: unknown→empty 200, malformed→400 `FEED_LOCATION_FILTER_INVALID`, never a 500 (Success Criterion 2 + error contract).
- RSS2 (`application/rss+xml`), iCal (`text/calendar`, one VEVENT/booking, CRLF, `Content-Disposition: inline; filename="bookings.ics"`), JSON (exact §4.8 shape, ISO-8601 times), display-board (auto-refreshing HTML) — all carry Cache-Control; rss2/ical/json carry a deterministic SHA-256-of-body ETag.
- Hand-built renderers escape upstream-controlled title/location text per format (XML / RFC-5545 / HTML), proven by unit tests.

## Task Commits

1. **Task 1: FeedBookingSummary DTO + shared FeedQueryService** — `63e1b00` (feat)
2. **Task 2: RSS2 / iCal / display-board renderers + unit tests** — `ad4eadd` (feat)
3. **Task 3: FeedController 4 public endpoints + integration tests** — `d4a039c` (feat)

## Files Created/Modified
- `dto/FeedBookingSummary.java` — the exact TechArch §4.8 JSON contract `{booking_id, title, location:{id,name}, start_time, end_time}`
- `feed/FeedQueryService.java` — single shared query: approved + upcoming + optional-location; malformed→400, unknown→empty
- `feed/Rss2Renderer.java` — hand-built RSS 2.0, XML-entity-escaped
- `feed/ICalRenderer.java` — hand-built iCalendar, CRLF VEVENTs, RFC-5545 escaping
- `feed/DisplayBoardRenderer.java` — full-screen signage HTML, 60s meta-refresh, HTML-escaped, graceful empty state
- `controller/FeedController.java` — 4 public GET endpoints, SHA-256-of-body ETag, cache headers
- `feed/FeedRendererTest.java` — 4 pure unit tests (structure + injection-safe escaping + empty state)
- `controller/FeedControllerIntegrationTest.java` — full-stack suite (9 tests) against real Postgres

## Decisions Made
- Hand-built RSS2/iCal with no external feed library (minimal attack surface on the single public service; exact escaping control).
- Display board is a standalone kiosk HTML route with meta-refresh, not wired into React nav.
- ETag = SHA-256 hex of the rendered body (deterministic across replicas).
- Integration test uses `@ActiveProfiles("test")` against docker-compose Postgres (Testcontainers unusable in sandbox — Docker client API 1.32 < 1.40 minimum), consistent with plan 07-01/Phase 4/Phase 6 precedent.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Integration test uses live docker-compose Postgres instead of @Testcontainers**
- **Found during:** Task 3
- **Issue:** The plan specified `@Testcontainers`/`PostgreSQLContainer("postgres:16")`, but this sandbox's Docker daemon reports client API version 1.32, below Testcontainers' 1.40 minimum, so a container cannot start. This is the identical, already-documented constraint plan 07-01 (and Phase 4/6) hit.
- **Fix:** Wrote `FeedControllerIntegrationTest` as `@SpringBootTest(RANDOM_PORT)` + `@ActiveProfiles("test")` against the running docker-compose Postgres (`feeds_db_test`), seeding directly via `FeedBookingRepository` — matching this service's own `FeedBookingSchemaTest`/`ApplicationContextBootTest` pattern. Flyway builds the real V1+V2 schema. All 9 tests pass.
- **Files modified:** `controller/FeedControllerIntegrationTest.java`
- **Verification:** `mvn test -Dtest=FeedControllerIntegrationTest` → 9/9 pass against real Postgres.
- **Committed in:** `d4a039c`

**2. [Rule 3 - Blocking] Removed a `@GetMapping` literal from a Javadoc comment so the contract verify is exact**
- **Found during:** Task 3
- **Issue:** The plan's `provides.verify` check is `grep -c "@GetMapping" FeedController.java | grep -q "^4$"`. A `{@code @GetMapping}` reference in the class Javadoc made the count 5, which would fail the exact-4 contract check despite there being exactly 4 real annotations.
- **Fix:** Reworded the Javadoc to "GET mapping" so the literal `@GetMapping` appears exactly on the 4 real endpoint annotations.
- **Files modified:** `controller/FeedController.java`
- **Verification:** `grep -c "@GetMapping" ... | grep -q "^4$"` → FOUR ENDPOINTS.
- **Committed in:** `d4a039c`

---

**Total deviations:** 2 auto-fixed (both Rule 3 - Blocking).
**Impact on plan:** Both are environmental/mechanical adaptations with no change to the delivered behavior or contract. All three Success Criteria are proven by the integration suite exactly as the plan intended.

## Known Stubs
None found. Scanned all 6 created main-source files for TODO/FIXME/placeholder/not-implemented — none present; no empty bodies or hardcoded responses.

## Issues Encountered
- **Build tooling:** No host Maven/JDK; the `maven:3.9-eclipse-temurin-21` image (used by the service Dockerfile's builder stage) was run as a throwaway container mounting the repo + a persistent `.m2` cache volume, on the `project_default` network so tests reach `postgres:5432`. The test datasource URL was overridden to `jdbc:postgresql://postgres:5432/feeds_db_test` on the Maven command (the committed test properties still point at `localhost:5432`, correct for a host runner and untouched for other plans' tests).
- **Parallel plan 07-02 coordination:** Early in Task 3, plan 07-02's then-uncommitted `FeedEventConsumerIntegrationTest.java` had a transient compile error that blocked test compilation. I did NOT modify it (out of scope). By the final run, 07-02 had fixed/committed it and the full module compiled; my suite was verified against the clean tree. Strictly stayed within this plan's files (dto/, feed/ renderers + FeedQueryService, controller/FeedController, feed/controller tests).

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- feeds-service is feature-complete for Phase 7's read side: all four public formats implemented and proven.
- Terminal plan for feeds-service in this phase (no downstream plan depends on 07-03).
- Full verification (`/pivota_spec-verify-work 07`) can confirm the phase once plan 07-02 (consumer/projection) is also complete.

## Self-Check: PASSED

- All 8 created source/test files exist on disk.
- All 3 task commits present (`63e1b00`, `ad4eadd`, `d4a039c`).
- Plan-level build `mvn -q -DskipTests package` → exit 0.
- Full test run: 13/13 pass (9 integration + 4 renderer) against real Postgres.
- Contract checks: exactly 4 `@GetMapping`, no auth annotations, SHA-256 ETag, `bookings.ics` disposition.
- Known Stubs: none found (no blocking stubs).

---
*Phase: 07-public-feeds*
*Completed: 2026-10-13*
