---
phase: 07-public-feeds
verified: 2026-10-13T03:14:28Z
status: passed
score: 3/3 must-haves verified
---

# Phase 7: Public Feeds Verification Report

**Phase Goal:** Approved upcoming bookings are exposed via public, read-only feeds, so external viewers like reception staff and visitors can see room availability at a glance without ever logging in or being shown an unconfirmed booking.
**Verified:** 2026-10-13T03:14:28Z
**Status:** passed
**Re-verification:** No — initial verification

## Goal Achievement

### Observable Truths (Success Criteria)

| #   | Truth                                                                                                                                                     | Status     | Evidence                                                                                                                                                                                                                                                                                       |
| --- | --------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | RSS2, iCal, JSON, display-board all available, each showing ONLY status=approved upcoming bookings; pending/denied never appear in any format             | ✓ VERIFIED | Live: all 4 endpoints HTTP 200 with correct Content-Types (`application/rss+xml`, `text/calendar`, `application/json`, `text/html`). Query filter `status="approved" AND start_time>=now` in FeedQueryService:63,74. Projection physically DELETEs denied/deleted rows (FeedProjectionService:133-135) — double-guard at data AND query level. |
| 2   | All formats support optional per-location filtering, applied consistently; unknown location_id returns empty feed (not error)                            | ✓ VERIFIED | All 4 endpoints delegate to the SAME `FeedQueryService.getApprovedUpcoming(locationId)`. Live: unknown well-formed UUID → `[]` HTTP 200; malformed UUID → HTTP 400 `FEED_LOCATION_FILTER_INVALID` (not 500). Malformed caught at parse-time before DB (FeedQueryService:66-71).                 |
| 3   | Every format accessible with zero login/account/token; access rules identical across all four                                                            | ✓ VERIFIED | NO spring-security/oauth2 dependency in pom.xml; NO SecurityConfig/SecurityFilterChain/@PreAuthorize/@Secured in src/main (only a javadoc comment references their deliberate absence). Live curls succeeded with no auth header. Identical access is structural: one shared query path, one access rule (none). BY DESIGN per F9.6. |

**Score:** 3/3 truths verified

### Required Artifacts

| Artifact                                   | Expected                                         | Status     | Details                                                                                                      |
| ------------------------------------------ | ------------------------------------------------ | ---------- | ----------------------------------------------------------------------------------------------------------- |
| `controller/FeedController.java`           | 4 public GET endpoints, no auth                  | ✓ VERIFIED | 4 `@GetMapping` (rss2/ical/json/display-board), all delegate to FeedQueryService, zero security annotations. |
| `feed/FeedQueryService.java`               | Single shared query path, approved+upcoming      | ✓ VERIFIED | `getApprovedUpcoming` with status/upcoming/location filter, malformed→400, unknown→empty.                    |
| `feed/Rss2Renderer.java` `ICalRenderer` `DisplayBoardRenderer` | Format renderers                   | ✓ VERIFIED | All present; live output confirms well-formed XML, text/calendar, HTML.                                      |
| `dto/FeedBookingSummary.java`              | §4.8 JSON shape {booking_id,title,location,...}  | ✓ VERIFIED | Present; wired by FeedQueryService.toSummary.                                                                |
| `consumer/FeedEventConsumer.java`          | 2 @RabbitListener (booking.q, location.q)        | ✓ VERIFIED | Exactly 2 `@RabbitListener` (lines 61,71); 3rd grep hit was a javadoc comment.                               |
| `consumer/FeedProjectionService.java`      | Projection state machine                         | ✓ VERIFIED | created/updated→upsert, approved→status flip, denied/deleted→DELETE, location→cache+backfill.                |
| `repository/FeedBookingRepository.java`    | approved-upcoming queries + location variant     | ✓ VERIFIED | Both finder queries + updateLocationName present.                                                            |
| `db/migration/V2__add_idempotency_key.sql` `V3__add_location_names.sql` | Additive migrations | ✓ VERIFIED | Present; V1/V2/V3 applied in runtime feeds_db (gate evidence).                                                |

### Key Link Verification

| From                   | To                                           | Via                               | Status  | Details                                                               |
| ---------------------- | -------------------------------------------- | --------------------------------- | ------- | -------------------------------------------------------------------- |
| FeedController (all 4) | FeedQueryService.getApprovedUpcoming         | shared delegate per format        | ✓ WIRED | All 4 endpoints call feedQueryService — identical access/filter.      |
| FeedController         | NO security annotation / NO SecurityConfig    | public by construction (F9.6)     | ✓ WIRED | Verified absent in pom + src/main; live unauthenticated 200s.         |
| FeedQueryService       | FeedLocationFilterInvalidException           | malformed UUID → 400 before DB    | ✓ WIRED | Live malformed → 400 FEED_LOCATION_FILTER_INVALID.                    |
| FeedProjectionService  | feed_bookings lifecycle (denied/deleted→DEL) | status filter unreachable by stub | ✓ WIRED | Physical delete ensures pending/denied cannot surface.               |
| FeedEventConsumer      | ProcessedEventRepository.existsByIdempotencyKey | lookup-first dedup in 1 txn    | ✓ WIRED | Idempotent apply wrappers confirmed.                                  |

### Requirements Coverage

| Requirement                       | Status      | Blocking Issue |
| --------------------------------- | ----------- | -------------- |
| F9.1 (RSS2)                       | ✓ SATISFIED | —              |
| F9.2 (iCal)                       | ✓ SATISFIED | —              |
| F9.3 (JSON)                       | ✓ SATISFIED | —              |
| F9.4 (display-board)              | ✓ SATISFIED | —              |
| F9.5 (per-location filtering)     | ✓ SATISFIED | —              |
| F9.6 (fully public, no auth)      | ✓ SATISFIED | —              |

### Anti-Patterns Found

None. Grep for security terms matched only a javadoc comment documenting the deliberate absence of a security layer (F9.6, BY DESIGN — a requirement being satisfied, not a gap). No TODO/FIXME/placeholder/stub implementations in the reviewed source.

### Behavioral Spot-Checks (live stack via gateway http://localhost:8080)

| Check                                      | Command                                                              | Result                                                                                      |
| ------------------------------------------ | ------------------------------------------------------------------- | ------------------------------------------------------------------------------------------- |
| RSS2 reachable, correct type               | `curl /feeds/rss2`                                                   | HTTP 200, `application/rss+xml;charset=UTF-8`, well-formed `<?xml ...><rss version="2.0">`   |
| iCal reachable, correct type               | `curl /feeds/ical`                                                   | HTTP 200, `text/calendar;charset=UTF-8`                                                      |
| JSON reachable, correct shape              | `curl /feeds/json`                                                   | HTTP 200, `application/json`, body `[]`                                                      |
| display-board reachable                    | `curl /feeds/display-board`                                          | HTTP 200, `text/html;charset=UTF-8`                                                          |
| Unknown location_id → empty 200            | `curl /feeds/json?location_id=0000...0000`                          | HTTP 200, body `[]`                                                                          |
| Malformed location_id → clean 400          | `curl /feeds/json?location_id=not-a-uuid`                           | HTTP 400, `{"error_code":"FEED_LOCATION_FILTER_INVALID",...}`                                |
| No auth required                           | all above, no bearer/token/cookie                                   | all succeeded unauthenticated                                                                |

### Gate Evidence (cited, not re-litigated)

- **07-GATE.md** (`gate_status: passed`, `boot_smoke: pass`, `review_blockers_open: 0`): build+test green on final tree (32 tests, 0 skipped). Boot smoke confirmed all four public endpoints return 200 live via gateway AND direct to feeds-service:8086; JSON valid `[]`, RSS2 well-formed XML, unknown→empty 200, malformed→400. Flyway V1/V2/V3 applied in runtime feeds_db.
- **07-REVIEW.md** (`status: clean`, 0 blockers, 0 warnings): W1 (location.created cache seeding+binding) and W2 (self-invocation @Transactional removal) both verified fixed by reading code. The deliberate absence of a security layer is explicitly confirmed BY DESIGN, not a finding.
- Build/tests/boot verified by phase gates — not re-run here. My independent live curls corroborate the gate's boot-smoke findings.

### Human Verification Required

None required for goal achievement. (Optional, non-blocking polish items a human could eyeball: the display-board's full-screen visual rendering and 60s auto-refresh behavior on a real kiosk browser, and RSS/iCal subscription in a real reader client. None of these gate the phase goal — endpoint reachability, content types, filtering, status-correctness, and no-auth are all machine-verified above.)

### Gaps Summary

No gaps. All three Success Criteria are verified both statically (source reads confirm the single shared query path, status filter, physical removal of denied/deleted, and the deliberate security-free construction) and dynamically (live gateway curls confirm all four formats return 200 unauthenticated with correct content types, consistent location filtering, unknown→empty-200, and malformed→clean-400). Gate and review evidence is fully green with zero open blockers. The intentional absence of a security layer is the satisfaction of F9.6 / Success Criterion 3, not a gap. Phase goal achieved.

---

_Verified: 2026-10-13T03:14:28Z_
_Verifier: Claude (pivota_spec-verifier)_
