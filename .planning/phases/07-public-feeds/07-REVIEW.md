---
phase: 7
status: issues_found
blockers: 0
warnings: 2
files_reviewed: 24
files_reviewed_list:
  - services/feeds-service/pom.xml
  - services/feeds-service/src/main/java/com/bookinghub/feeds/consumer/FeedEventConsumer.java
  - services/feeds-service/src/main/java/com/bookinghub/feeds/consumer/FeedProjectionService.java
  - services/feeds-service/src/main/java/com/bookinghub/feeds/controller/FeedController.java
  - services/feeds-service/src/main/java/com/bookinghub/feeds/domain/FeedBooking.java
  - services/feeds-service/src/main/java/com/bookinghub/feeds/domain/LocationName.java
  - services/feeds-service/src/main/java/com/bookinghub/feeds/domain/ProcessedEvent.java
  - services/feeds-service/src/main/java/com/bookinghub/feeds/dto/FeedBookingSummary.java
  - services/feeds-service/src/main/java/com/bookinghub/feeds/error/ApiError.java
  - services/feeds-service/src/main/java/com/bookinghub/feeds/error/ApiException.java
  - services/feeds-service/src/main/java/com/bookinghub/feeds/error/GlobalExceptionHandler.java
  - services/feeds-service/src/main/java/com/bookinghub/feeds/feed/DisplayBoardRenderer.java
  - services/feeds-service/src/main/java/com/bookinghub/feeds/feed/FeedQueryService.java
  - services/feeds-service/src/main/java/com/bookinghub/feeds/feed/ICalRenderer.java
  - services/feeds-service/src/main/java/com/bookinghub/feeds/feed/Rss2Renderer.java
  - services/feeds-service/src/main/java/com/bookinghub/feeds/rabbit/MissingIdempotencyKeyException.java
  - services/feeds-service/src/main/java/com/bookinghub/feeds/rabbit/RabbitConfig.java
  - services/feeds-service/src/main/java/com/bookinghub/feeds/rabbit/RabbitRetryBackOffPolicy.java
  - services/feeds-service/src/main/java/com/bookinghub/feeds/repository/FeedBookingRepository.java
  - services/feeds-service/src/main/java/com/bookinghub/feeds/repository/LocationNameRepository.java
  - services/feeds-service/src/main/java/com/bookinghub/feeds/repository/ProcessedEventRepository.java
  - services/feeds-service/src/main/resources/application.yml
  - services/feeds-service/src/main/resources/db/migration/V2__add_idempotency_key.sql
  - services/feeds-service/src/main/resources/db/migration/V3__add_location_names.sql
reviewed_at: 2026-10-13T03:04:31Z
iteration: 1
---

# Phase 7 Code Review

Scope reviewed: all `services/feeds-service` source committed for Phase 7 (24a944e → HEAD on
`phase-7`), focused on the four named risk areas: injection safety in the hand-built
RSS2/iCal/display-board renderers, idempotency correctness in the RabbitMQ consumer,
approved-upcoming query correctness (pending/denied must never leak), and location-filter
consistency across all four formats. The deliberate absence of a security layer (no
spring-security dep, no SecurityConfig, no JWT) is BY DESIGN per Success Criterion 3 / plan
07-01 and is NOT treated as a finding. Cross-service event payload/routing-key contracts were
verified against the real Phase 5 booking-service and Phase 4 locations-resources-service
publishers.

No BLOCKERs were found. The four highest-risk areas are implemented correctly. Two WARNINGs
record real-but-non-blocking gaps.

## BLOCKERs

None.

The scrutiny targets all held up under refutation:

- **Injection safety (all three text renderers): correct.** `Rss2Renderer.xmlEscape` escapes
  `& < > " '`; `ICalRenderer.icalEscape` escapes `\ ; , \n` per RFC 5545 §3.3.11 AND drops bare
  `\r`, so no raw CR/LF can reach the output to forge a new iCal property line (the CRLF-injection
  vector) — confirmed by reading the switch at ICalRenderer.java:66-73; `DisplayBoardRenderer.htmlEscape`
  escapes `& < > " '` (apostrophe as `&#39;`). Every upstream-controlled value (title, location
  name, and the composed description/time strings) passes through its format's escaper before
  concatenation. The only raw-appended values are `item.bookingId()` (a `UUID`, hex+hyphens only)
  and server-formatted timestamps — neither attacker-controllable.
- **Approved-upcoming query: correct, defense-in-depth.** `FeedQueryService.getApprovedUpcoming`
  filters `status="approved"` AND `start_time >= now` via the derived repository queries
  (FeedBookingRepository.java:27-31), and `FeedProjectionService` *physically deletes* denied/deleted
  rows (lines 133-135) so pending/denied cannot leak even if the query filter were bypassed. The
  literal `"approved"` matches booking-service's lowercase status values (verified in
  BookingWriteService/BookingApprovalService payloads). Integration test seeds past/pending/denied
  rows and asserts their absence in every format.
- **Idempotency: correct.** Lookup-first `existsByIdempotencyKey` → apply → `recordProcessed`, all
  inside one `@Transactional` boundary on `applyBookingEventIdempotent`/`applyLocationEventIdempotent`
  (lines 95-116), so a crash before commit rolls back the projection AND the processed_events insert
  together and the message safely redelivers; the `processed_events` UUID PK (V2) is the race-safe
  backstop for concurrent duplicate delivery. Missing/blank `idempotency_key` throws
  `MissingIdempotencyKeyException` (extends `AmqpRejectAndDontRequeueException`, classified
  non-retryable with `traverseCauses=true` in RabbitConfig) → immediate DLQ, no backoff, no row
  written — verified end-to-end by FeedEventConsumerIntegrationTest.
- **Location-filter consistency: correct.** All four endpoints delegate to the single
  `getApprovedUpcoming(String)`; malformed location_id throws before any DB hit (clean 400,
  identical FEED_LOCATION_FILTER_INVALID code), unknown-but-well-formed → empty 200. Asserted
  across formats in FeedControllerIntegrationTest §5–§7.

## WARNINGs

### W1: A newly-created location whose booking is fed before any `location.updated` renders the `"Location <uuid>"` placeholder — and `location.created` is never consumed, so an un-edited location shows the placeholder indefinitely on the public feed
- **File:** services/feeds-service/src/main/java/com/bookinghub/feeds/consumer/FeedProjectionService.java:162-179, 218-222 ; infra/rabbitmq/definitions.json (feeds.location.q bindings)
- **Category:** integration
- **Evidence:** `feeds.location.q` is bound ONLY to `location.updated` and `location.deleted`
  (definitions.json bindings at lines 427/435) — NOT to `location.created`, even though
  locations-resources-service emits `location.created` with the full payload including `name`
  (LocationService.writeOutboxEvent includes `name` for every location event).
  `FeedProjectionService.applyLocationEvent` likewise only handles `"updated"` (line 162). The
  `location_names` cache is therefore populated exclusively on *edits*. Consequence: for a location
  that is created and immediately booked but never subsequently edited, `resolveLocationName`
  (line 218) misses the cache and persists the `"Location <uuid>"` placeholder into
  `feed_bookings.location_name`, which then surfaces verbatim in all four public feeds with no event
  that will ever back-fill it. This is the documented graceful-degradation placeholder (07-02-SUMMARY
  "Known Stubs"), so it is non-blocking — the feed never blanks and NOT NULL is always satisfied — but
  the plan docs describe the placeholder as transient ("back-filled by a later location.updated"),
  whereas in the create-then-book-never-edit path it is effectively permanent. This is a user-visible
  quality gap on an internet-facing surface, not a correctness/security failure.
- **Fix direction:** Add a `location.created` binding to `feeds.location.q` and treat `created` the
  same as `updated` in `applyLocationEvent` (both carry `id`+`name`), so the cache is seeded at
  location creation rather than only on first edit. (Binding change is infra, outside this service's
  source; the consumer-side one-line action-match widening is in scope here.)
- **Resolution:** fixed (ec0ec1e) — `applyLocationEvent` now seeds/back-fills the cache on
  `location.created` as well as `location.updated` (both payloads carry `id`+`name`, verified against
  LocationService.writeOutboxEvent); added the `feeds.location.q <- location.created` binding to
  infra/rabbitmq/definitions.json (tracked file, kept idempotent/consistent with the existing
  `location.updated`/`location.deleted` bindings); added `locationCreated_seedsCacheSoLaterBookingResolvesRealName`
  proving the create-then-book path resolves the real name, not the placeholder. All 32 feeds-service
  tests pass (31 prior + 1 new).

### W2: Inner `@Transactional` on `applyBookingEvent`/`applyLocationEvent` is ineffective via self-invocation — harmless today, but a latent trap if these methods are ever called directly in production
- **File:** services/feeds-service/src/main/java/com/bookinghub/feeds/consumer/FeedProjectionService.java:124,154
- **Category:** bug (latent)
- **Evidence:** `applyBookingEventIdempotent` (line 100) and `applyLocationEventIdempotent`
  (line 113) call `applyBookingEvent`/`applyLocationEvent` as plain in-class method calls. Because
  Spring's `@Transactional` is proxy-based, this self-invocation bypasses the proxy, so the inner
  `@Transactional` annotations are no-ops — the mutations run inside the OUTER transaction instead,
  which is exactly the desired behavior, so there is no defect in the current call graph. The risk is
  purely latent: these methods are `public` and annotated `@Transactional`, which advertises a
  standalone transactional boundary that does not actually exist on self-invocation. The production
  entry point is always the idempotent wrapper, and the unit tests invoke them directly only under
  `@DataJpaTest`'s own test transaction, so nothing currently breaks. Flagged so a future caller does
  not assume these create their own transaction.
- **Fix direction:** Either drop `@Transactional` from the two package-visible `applyBookingEvent`/
  `applyLocationEvent` methods (relying on the wrapper's boundary), or make them non-public helpers,
  so the annotation does not imply a boundary that self-invocation cannot provide. No behavior change
  required for the current call graph.
- **Resolution:** fixed (a17ec1a + ec0ec1e) — both `applyBookingEvent`/`applyLocationEvent` dropped
  `@Transactional` AND were demoted from `public` to package-visible helpers, so the annotation no
  longer advertises a self-invocation boundary that cannot exist; they now run solely inside the
  outer transaction of the `apply*EventIdempotent` wrappers. All callers are the in-class wrappers and
  the same-package `FeedProjectionServiceTest`, so package visibility is sufficient. (The
  `applyLocationEvent` half of this change rode in with the W1 commit ec0ec1e since both edits touch
  the same contiguous hunk; the `applyBookingEvent` half is a17ec1a.) No behavior change; all 32 tests
  pass.

## Cross-file seams checked
- Routes ↔ frontend callers: feeds are public backend-only (RSS/iCal/display-board are consumed by
  external readers/kiosks, not the React app, per DisplayBoardRenderer doc). No frontend caller drift
  in scope. OK.
- Event routing keys ↔ consumer `actionOf` suffixes: booking.created/updated/approved/denied/deleted
  (BookingWriteService + BookingApprovalService) and location.updated/deleted all map to the switch
  arms in FeedProjectionService. `location.created` is emitted but intentionally unbound → W1. OK (W1).
- Event payload fields ↔ consumer reads: id/title/location_id/start_time/end_time/status match
  buildCreatePayload/buildUpdatePayload; booking.approved/denied carry only id+status (status-flip /
  delete path is payload-faithful — no NPE on absent title/time); location.updated carries id+name.
  Verified against Phase 5/Phase 4 publishers. OK.
- Entity ↔ V1/V2/V3 schema: FeedBooking/ProcessedEvent/LocationName columns + read-only
  insertable=false/updatable=false on DB-managed timestamps match the DDL. OK.
- Repository derived-query method names ↔ entity fields: status/startTime/locationId resolve against
  FeedBooking; `updateLocationName` @Modifying JPQL references valid fields. OK.
- DTO ↔ §4.8 JSON contract: FeedBookingSummary snake_case @JsonProperty names
  (booking_id/title/location{id,name}/start_time/end_time) match the spec and the integration test's
  raw-JSON assertions; Instants serialize as ISO-8601 (asserted). OK.
- ApiException ↔ GlobalExceptionHandler: FeedLocationFilterInvalidException (400,
  FEED_LOCATION_FILTER_INVALID) handled polymorphically; Exception fallback returns generic 500
  INTERNAL_ERROR with no stack-trace leak (correct posture for a public service). OK.
- RabbitConfig non-retryable classification ↔ MissingIdempotencyKeyException hierarchy:
  AmqpRejectAndDontRequeueException→false with defaultValue=true + traverseCauses=true correctly
  dead-letters the wrapped missing-key cause with no backoff; DLQ landing proven by integration test.
  OK.
- pom.xml ↔ usage: spring-retry + spring-aspects present for RetryInterceptorBuilder; no
  spring-security starter (by design). OK.
