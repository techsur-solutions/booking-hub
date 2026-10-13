---
phase: 07-public-feeds
plan: 02
subsystem: api
tags: [rabbitmq, projection, idempotency, denormalization, cqrs, spring-amqp, postgres, flyway]

requires:
  - phase: 07-public-feeds (plan 07-01)
    provides: FeedBooking/ProcessedEvent entities + repositories, updateLocationName @Modifying back-fill, RabbitConfig rabbitListenerContainerFactory, MissingIdempotencyKeyException, processed_events dedup table
  - phase: 05-core-booking-approval-workflow
    provides: booking.created/updated/approved/denied/deleted outbox events (payload shapes reconciled at execution time)
  - phase: 04-reference-data-extensibility-configuration
    provides: location.updated/location.deleted outbox events (id + name payload)
provides:
  - FeedEventConsumer with 2 @RabbitListener methods (feeds.booking.q, feeds.location.q)
  - FeedProjectionService projection state machine (created/updated upsert, approved status-flip, denied/deleted physical delete, location back-fill, location.deleted graceful keep)
  - V3 location_names event-fed denormalization cache + LocationName entity/repository
  - Exactly-effectively-once projection application (lookup-first idempotency + atomic processed_events insert)
  - Missing-idempotency-key dead-lettering (never silently dropped)
affects: [07-03 public feed endpoints read this projection]

tech-stack:
  added: [awaitility (transitive via spring-boot-starter-test), Spring AMQP @RabbitListener consumer]
  patterns:
    - "Projection consumer: routing-key suffix -> lifecycle transition on feed_bookings by booking_id"
    - "Event-fed denormalization cache (location_names) replaces any cross-service DB read"
    - "Transaction boundary on the DB-mutating service method, NOT the @RabbitListener method, so AmqpRejectAndDontRequeueException propagates unwrapped to the retry interceptor"
    - "Non-retryable classification on the AmqpRejectAndDontRequeueException supertype with defaultValue=true + traverseCauses=true"

key-files:
  created:
    - services/feeds-service/src/main/resources/db/migration/V3__add_location_names.sql
    - services/feeds-service/src/main/java/com/bookinghub/feeds/domain/LocationName.java
    - services/feeds-service/src/main/java/com/bookinghub/feeds/repository/LocationNameRepository.java
    - services/feeds-service/src/main/java/com/bookinghub/feeds/consumer/FeedProjectionService.java
    - services/feeds-service/src/main/java/com/bookinghub/feeds/consumer/FeedEventConsumer.java
    - services/feeds-service/src/test/java/com/bookinghub/feeds/consumer/FeedProjectionServiceTest.java
    - services/feeds-service/src/test/java/com/bookinghub/feeds/consumer/FeedEventConsumerIntegrationTest.java
  modified:
    - services/feeds-service/src/main/java/com/bookinghub/feeds/rabbit/RabbitConfig.java
    - services/feeds-service/pom.xml

key-decisions:
  - "approved/denied/deleted booking payloads carry only id (+status/approval metadata), so approved flips status on the already-projected row instead of upserting from absent title/location/time fields — reconciled against Phase 5 BookingApprovalService.buildApprovalPayload"
  - "location_name denormalized via an event-fed V3 location_names cache; cache miss yields a 'Location <uuid>' placeholder (satisfies NOT NULL) that a later location.updated back-fills — zero cross-service DB reads"
  - "RabbitConfig retry policy classifies AmqpRejectAndDontRequeueException (not a blanket Exception->true) as non-retryable so missing-key messages DLQ immediately rather than after the full 5s/30s/2min backoff"

patterns-established:
  - "Projection state machine keyed off the routing-key action suffix — denied/deleted physically remove the row (Success Criterion 1 enforced at the data layer, not just the query layer)"
  - "Shared-broker integration test runs the consumer's own context as the sole listener (per-test auto-startup=true) and cleans its committed rows in @AfterEach"

duration: 24min
completed: 2026-10-13
---

# Phase 7 Plan 02: Feeds Projection Consumer Summary

**Event-driven feed_bookings projection consumer — 2 @RabbitListener methods applying the booking/location lifecycle state machine idempotently, with an event-fed location_names denormalization cache and missing-key dead-lettering, all proven against live Postgres + RabbitMQ.**

## Performance

- **Duration:** 24 min
- **Started:** 2026-10-13T02:34:31Z
- **Completed:** 2026-10-13T02:58:53Z
- **Tasks:** 2
- **Files modified:** 9 (7 created, 2 modified)

## Accomplishments

- **Projection state machine (Success Criterion 1 at the data layer):** `booking.created/updated` upsert the row; `booking.approved` flips status to approved (making it feed-visible); `booking.denied`/`booking.deleted` **physically delete** the row, so no feed renderer in plan 07-03 can ever surface a pending/denied/deleted booking.
- **Cross-service denormalization without a cross-service read:** V3 `location_names` cache is fed by `location.updated` events; booking projection resolves the name from the cache (placeholder on miss, back-filled by a later `location.updated`); `location.deleted` keeps the last-known name (graceful degradation).
- **Exactly-effectively-once:** lookup-first `existsByIdempotencyKey` + atomic `processed_events` insert in the same transaction as the projection mutation; a duplicate redelivery is a safe no-op.
- **Missing idempotency_key dead-letters** to `feeds.booking.q.dlq` immediately, never silently dropped, never written as a corrupt row.
- Full feeds-service suite **31/31 green** (Task 1: 6, Task 2: 5, plus 07-01 + parallel 07-03 tests) against live compose Postgres + RabbitMQ, deterministic across repeated runs.

## Task Commits

1. **Task 1: V3 location_names cache + FeedProjectionService state machine** - `cc15679` (feat)
2. **Task 2: FeedEventConsumer (2 @RabbitListener) + idempotent projection + DLQ fix** - `9e0d0c4` (feat)

**Plan metadata:** (docs commit, see below)

## Files Created/Modified

- `db/migration/V3__add_location_names.sql` - Additive location_names cache table (location_id PK, name, updated_at)
- `domain/LocationName.java` - JPA entity over the cache
- `repository/LocationNameRepository.java` - JpaRepository for the cache
- `consumer/FeedProjectionService.java` - Projection state machine + idempotent orchestration methods (single @Transactional boundary)
- `consumer/FeedEventConsumer.java` - 2 @RabbitListener methods (feeds.booking.q, feeds.location.q)
- `test/.../FeedProjectionServiceTest.java` - 6 state-machine scenarios (@DataJpaTest vs compose Postgres)
- `test/.../FeedEventConsumerIntegrationTest.java` - 5 end-to-end scenarios (@SpringBootTest vs compose Postgres + RabbitMQ)
- `rabbit/RabbitConfig.java` - **modified** retry classification (Rule 1 fix)
- `pom.xml` - **modified** surefire includes for *IntegrationTest (Rule 3 fix)

## Decisions Made

- **approved/denied/deleted payload reconciliation:** Phase 5's `BookingApprovalService.buildApprovalPayload` emits only `id` + status/approval metadata (no title/location/time), and `buildDeletePayload` emits only `id` + `deleted_at`. The plan's draft pseudo-code `upsert(...)` for `approved` would NPE on the absent fields; the payload-faithful transition is a **status flip on the already-projected row** (booking.created always precedes booking.approved via outbox ordering). `denied`/`deleted` need only `id` (deleteById).
- **location event payload confirmed:** Phase 4's `LocationService.writeOutboxEvent` emits `id` + `name` (plus others) for both `location.updated` and `location.deleted` — matches the plan's assumption exactly.
- **Testcontainers convention:** followed the established 07-01 / Phase 4 / Phase 6 workaround (run against the running docker-compose Postgres + RabbitMQ via `@ActiveProfiles("test")`) rather than `@Testcontainers`, consistent with every prior feeds-service test.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Missing-idempotency-key message was retried instead of dead-lettered**
- **Found during:** Task 2 (FeedEventConsumerIntegrationTest scenario 5)
- **Issue:** plan 07-01's `RabbitConfig` retry policy used `{MissingIdempotencyKeyException->false, Exception->true}`. Spring AMQP wraps a listener throwable in `ListenerExecutionFailedException`; the `BinaryExceptionClassifier` matched the wrapper's own hierarchy (`...->Exception == true`) and returned retryable BEFORE ever inspecting the wrapped cause. A missing-key message was therefore retried through the full 5s/30s/2min backoff instead of dead-lettering — directly defeating must-have truth #4 ("routed straight to the dead-letter queue, never silently dropped").
- **Fix:** classify ONLY `AmqpRejectAndDontRequeueException` (the `MissingIdempotencyKeyException` supertype) as non-retryable, with `defaultValue=true` (every other exception stays retryable) and `traverseCauses=true` (the classifier walks from the wrapper down to the real cause). Missing-key now DLQs immediately with no backoff.
- **Files modified:** services/feeds-service/src/main/java/com/bookinghub/feeds/rabbit/RabbitConfig.java
- **Verification:** FeedEventConsumerIntegrationTest.missingIdempotencyKey_deadLettersAndWritesNoRow passes in ~8s (was a 20s+ timeout before); full suite 31/31 green.
- **Committed in:** 9e0d0c4 (Task 2 commit)
- **Note:** RabbitConfig is feeds-service's own file (07-01), NOT in plan 07-03's ownership set — no coordination boundary crossed.

**2. [Rule 3 - Blocking] *IntegrationTest excluded from `mvn test` by Spring Boot parent surefire config**
- **Found during:** Task 2
- **Issue:** the Spring Boot starter-parent surefire configuration excludes `**/*IntegrationTest.java` (reserving that suffix for the failsafe plugin), so the plan's verify command `mvn test -Dtest=FeedEventConsumerIntegrationTest` ran 0 tests ("No tests matching pattern were executed").
- **Fix:** override the surefire `<includes>` in pom.xml to pick up `*IntegrationTest` alongside `*Test`/`*Tests`.
- **Files modified:** services/feeds-service/pom.xml
- **Verification:** the integration suite now executes under `mvn test`.
- **Committed in:** 9e0d0c4 (Task 2 commit)

**3. [Rule 1 - Bug] Transaction boundary moved off the @RabbitListener method**
- **Found during:** Task 2
- **Issue:** the plan's draft put `@Transactional` on the listener `process(...)` method (a private helper called internally — Spring would not even proxy it) / on the listener method itself, which interfered with how the thrown `MissingIdempotencyKeyException` reached the retry interceptor.
- **Fix:** the single transaction now wraps `FeedProjectionService.apply*EventIdempotent(...)` (dedup-check + projection + processed_events insert), and the consumer's `@RabbitListener` methods are non-transactional — matching Phase 6 audit-log-service's established pattern and ensuring atomicity is preserved.
- **Files modified:** FeedProjectionService.java, FeedEventConsumer.java
- **Verification:** duplicate-suppression and crash-rollback behavior proven by integration scenarios 1-5; full suite green.
- **Committed in:** 9e0d0c4 (Task 2 commit)

---

**Total deviations:** 3 auto-fixed (2 Rule 1 bug, 1 Rule 3 blocking)
**Impact on plan:** All three were necessary to satisfy the plan's own must-have truths (idempotency atomicity + missing-key DLQ) and to make the specified verify command runnable. No scope creep; the projection logic (upsert/delete/back-fill/status-flip) is exactly as specified. The `approved` status-flip (vs full upsert) is a faithful reconciliation of the real upstream payload, explicitly mandated by the plan's `<assumptions>` directive.

## Known Stubs

None found. The only `placeholder` reference is the intentional `"Location <uuid>"` graceful-degradation value for an unknown location (a designed behavior, not an incomplete implementation), back-filled by a later `location.updated`.

## Issues Encountered

- **Shared-broker / shared-DB test interference (resolved):** the full suite runs alongside plan 07-03's `@SpringBootTest` controller test against the same compose broker + `feeds_db_test`. Resolved by (a) scoping listener auto-startup to this test's own context via `@SpringBootTest(properties=...)` so sibling contexts raise no competing consumers, (b) an `@AfterEach` that wipes this committing test's rows from the shared tables, and (c) deterministic cross-queue/lifecycle ordering awaits (wait for the created row before publishing approved; wait for the location cache before publishing the booking). Suite is deterministic across repeated runs.
- The running `project-feeds-service-1` container was briefly stopped during integration testing (to make the test's listener the sole consumer of feeds.booking.q) and restarted afterward.

## Next Phase Readiness

- The feed_bookings projection is now kept correct and current: only approved upcoming bookings are present, pending/denied/deleted are absent, and location labels are fresh via the event-fed cache — exactly what plan 07-03's four public feed endpoints (RSS2, iCal, JSON, display-board) read.
- Plan 07-03 (parallel sibling in this wave) is independently complete against 07-01's entity; its controller/renderer tests pass unchanged alongside this plan's consumer tests.
- No blockers. Phase 7 remaining: confirm 07-03 summary, then phase transition.

---
*Phase: 07-public-feeds*
*Completed: 2026-10-13*

## Self-Check: PASSED

- All 7 created files present on disk.
- Both task commits present (cc15679, 9e0d0c4).
- Plan-level build: `mvn package` (in maven:3.9-eclipse-temurin-21 on compose network) -> BUILD SUCCESS, 31/31 tests, jar built at target/feeds-service-0.0.1-SNAPSHOT.jar.
- `## Known Stubs`: none blocking (only the intentional graceful-degradation location placeholder).
