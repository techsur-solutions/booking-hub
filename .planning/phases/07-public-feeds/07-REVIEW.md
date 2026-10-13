---
phase: 7
status: clean
blockers: 0
warnings: 0
files_reviewed: 4
files_reviewed_list:
  - services/feeds-service/src/main/java/com/bookinghub/feeds/consumer/FeedProjectionService.java
  - services/feeds-service/src/main/java/com/bookinghub/feeds/consumer/FeedEventConsumer.java
  - services/feeds-service/src/test/java/com/bookinghub/feeds/consumer/FeedProjectionServiceTest.java
  - infra/rabbitmq/definitions.json
reviewed_at: 2026-10-13T03:09:46Z
iteration: 2
---

# Phase 7 Code Review

Re-review (iteration 2) verifying the fixer's resolution of the two WARNINGs from iteration 1
across commits ec0ec1e (W1) and a17ec1a (W2). Scope: the files those commits touched plus their
cross-file seams — `FeedProjectionService` (both fixes), `FeedEventConsumer` (caller of the
visibility-changed methods), `FeedProjectionServiceTest` (same-package caller + new W1 test), and
`infra/rabbitmq/definitions.json` (new binding). The deliberate absence of a security layer on
feeds-service remains BY DESIGN (F9.6 / Success Criterion 3) and is NOT a finding.

Both previous WARNINGs are verified fixed by reading the resulting code (not trusting the commit
messages), and no fix-introduced regressions were found. Status upgraded to **clean**.

## BLOCKERs

None.

## WARNINGs

None.

Both iteration-1 WARNINGs verified RESOLVED:

- **W1 (location.created cache seeding) — FIXED (ec0ec1e), confirmed by reading the code.**
  `FeedProjectionService.applyLocationEvent` now matches `"created".equals(action) || "updated".equals(action)`
  (FeedProjectionService.java:163), and on that branch reads `name`, upserts the `location_names`
  cache, and back-fills existing `feed_bookings` rows (lines 164-175) — identical treatment to the
  prior `updated`-only path. Both payloads carry `id`+`name` (LocationService.writeOutboxEvent), so
  the create path has the data it needs; the null-name guard (lines 165-167) still protects against a
  malformed event. The infra binding `feeds.location.q <- location.created` was added
  (definitions.json:422-429) as a distinct explicit per-key binding alongside the existing
  `location.updated` (430-437) and `location.deleted` (438-445) bindings — the JSON is well-formed and
  the binding set is internally consistent. Crucially, `feeds.location.q` uses three DISTINCT explicit
  routing keys (not a `location.*` wildcard), so each message is delivered to the queue exactly once —
  no duplicate-delivery regression. The unrelated `location.*` wildcard at line 419 targets a
  DIFFERENT queue (`audit.location.q`) and is unaffected. The new test
  `locationCreated_seedsCacheSoLaterBookingResolvesRealName` (FeedProjectionServiceTest.java:132-154)
  proves the create-then-book path resolves the real name rather than the `"Location <uuid>"`
  placeholder. The create-then-book-never-edit gap that made the placeholder effectively permanent is
  closed.

- **W2 (ineffective self-invocation @Transactional) — FIXED (a17ec1a + ec0ec1e), confirmed by reading
  the code.** Both `applyBookingEvent` (FeedProjectionService.java:125) and `applyLocationEvent`
  (line 154) are now package-visible (no modifier) and no longer annotated `@Transactional`, so the
  annotation no longer advertises a self-invocation boundary that proxy-based AOP cannot provide. They
  run solely inside the outer transaction of `applyBookingEventIdempotent`/`applyLocationEventIdempotent`
  (lines 96, 109), which retain their `@Transactional` — the single, real boundary. Verified by
  enumerating every caller (grep): the only production caller is `FeedEventConsumer`, which invokes the
  public `*Idempotent` wrappers (FeedEventConsumer.java:68,78), never the demoted helpers; the only
  other callers are in `FeedProjectionServiceTest`, which is in the same package
  (`com.bookinghub.feeds.consumer`, line 1) so package visibility compiles, and which runs under
  `@DataJpaTest`'s own test transaction so the removed annotation is harmless there. The
  `@Transactional` import is still consumed by the two wrappers, so no unused-import / compile
  regression. No behavior change for the current call graph.

## Cross-file seams checked (re-review scope)
- Demoted helpers ↔ callers: `applyBookingEvent`/`applyLocationEvent` now package-private; grep
  confirms the only external caller is `FeedEventConsumer` via the public `*Idempotent` wrappers, and
  the only direct callers are the same-package `FeedProjectionServiceTest`. No cross-package / proxy
  caller relies on the dropped `public`/`@Transactional`. OK.
- New routing-key binding ↔ consumer handler: `feeds.location.q <- location.created` (definitions.json)
  now routes to `onLocationEvent` → `applyLocationEventIdempotent` → `applyLocationEvent`, whose switch
  now handles `created`. Binding and handler are in sync. OK.
- Binding fan-out ↔ duplicate delivery: `feeds.location.q` bound by three distinct explicit keys
  (created/updated/deleted), not a wildcard, so no double delivery; `location.*` wildcard targets the
  separate `audit.location.q`. OK.
- Payload contract ↔ created handler: location.created carries `id`+`name` (same as location.updated),
  matching the fields the shared branch reads; null-name guard preserved. OK.
- Test package ↔ demoted visibility: FeedProjectionServiceTest in `com.bookinghub.feeds.consumer`
  matches the service package, so package-private method access compiles. OK.
- @Transactional import ↔ usage: still referenced by the two `*Idempotent` wrappers; not orphaned. OK.
