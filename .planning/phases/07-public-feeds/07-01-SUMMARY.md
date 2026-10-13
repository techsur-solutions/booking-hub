---
phase: 07-public-feeds
plan: 01
subsystem: feeds
tags: [rabbitmq, flyway, jpa, spring-retry, idempotency, dlq, public-service, lombok]

# Dependency graph
requires:
  - phase: 02-platform-foundation-infrastructure
    provides: feeds-service scaffold (pom, application.yml, V1 feed_bookings schema, FeedsServiceApplication, ApplicationContextBootTest), feeds.*.q RabbitMQ topology in definitions.json, feeds_db/feeds_svc Postgres role
  - phase: 06-notifications-audit-logging
    provides: audit-log-service plan 06-03 retry/DLQ container-factory pattern, mirrored here
provides:
  - FeedBooking JPA entity + FeedBookingRepository (approved-upcoming + per-location read queries, updateLocationName @Modifying back-fill)
  - ProcessedEvent entity + ProcessedEventRepository (existsByIdempotencyKey lookup-first idempotency check)
  - V2 processed_events dedup table (additive migration closing Y3-integrations.md idempotent-consumption gap)
  - rabbitListenerContainerFactory bean (4-attempt 5s/30s/2min retry, RejectAndDontRequeueRecoverer, MissingIdempotencyKeyException non-retryable)
  - Shared ApiError contract + FeedLocationFilterInvalidException (400 FEED_LOCATION_FILTER_INVALID) + GlobalExceptionHandler (500 INTERNAL_ERROR fallback, no stack-trace leak)
  - A public, security-free foundation (NO spring-security / oauth2 anywhere) per F9.6
affects: [07-02-consumers, 07-03-public-endpoints]

# Tech tracking
tech-stack:
  added:
    - spring-retry (RabbitConfig RetryInterceptorBuilder)
    - spring-aspects (required by spring-retry)
  patterns:
    - Intentionally-public service — zero auth starters/config, deliberate exception to Tier-2 JWT norm (F9.6)
    - Lookup-first idempotency via processed_events dedup table (not upsert-only), keyed on event idempotency_key
    - Consumer-only RabbitMQ retry/DLQ factory binding by queue name, declaring no topology
    - Repository single-owner convention: every query (incl. consumer back-fill mutation) declared in the owning plan, proven by its test

key-files:
  created:
    - services/feeds-service/src/main/resources/db/migration/V2__add_idempotency_key.sql
    - services/feeds-service/src/main/java/com/bookinghub/feeds/domain/FeedBooking.java
    - services/feeds-service/src/main/java/com/bookinghub/feeds/domain/ProcessedEvent.java
    - services/feeds-service/src/main/java/com/bookinghub/feeds/repository/FeedBookingRepository.java
    - services/feeds-service/src/main/java/com/bookinghub/feeds/repository/ProcessedEventRepository.java
    - services/feeds-service/src/main/java/com/bookinghub/feeds/error/ApiError.java
    - services/feeds-service/src/main/java/com/bookinghub/feeds/error/ApiException.java
    - services/feeds-service/src/main/java/com/bookinghub/feeds/error/GlobalExceptionHandler.java
    - services/feeds-service/src/main/java/com/bookinghub/feeds/rabbit/RabbitConfig.java
    - services/feeds-service/src/main/java/com/bookinghub/feeds/rabbit/RabbitRetryBackOffPolicy.java
    - services/feeds-service/src/main/java/com/bookinghub/feeds/rabbit/MissingIdempotencyKeyException.java
    - services/feeds-service/src/test/java/com/bookinghub/feeds/repository/FeedBookingSchemaTest.java
    - services/feeds-service/src/test/resources/application-test.properties
  modified:
    - services/feeds-service/pom.xml
    - services/feeds-service/src/main/resources/application.yml
    - services/feeds-service/src/test/java/com/bookinghub/feeds/ApplicationContextBootTest.java

key-decisions:
  - "processed_events lookup table (not upsert-only) for idempotency: a booking's lifecycle emits multiple distinct events that each legitimately mutate the same projection row by booking_id, so only a key on the event's own idempotency_key can distinguish redelivery from a later legitimate event"
  - "NO security layer of any kind (no spring-security/oauth2 deps, no SecurityConfig, no CurrentUserProvider) — feeds-service is the single intentionally-public service per F9.6; gateway permitAll on /feeds/** already owns authz"
  - "FeedLocationFilterInvalidException defined as a nested static class on ApiException (satisfies the plan's grep contract); FEED_FORBIDDEN/FEED_FORMAT_UNSUPPORTED deliberately NOT defined"
  - "Rule 3 deviation: tests run against running docker-compose Postgres via @ActiveProfiles(test), not Testcontainers — sandbox Docker API 1.32 < Testcontainers 1.40 minimum; same workaround as Phase 4 plan 04-01 / Phase 6 plan 06-03"

patterns-established:
  - "Intentionally-public service: explicit, documented omission of all auth starters and config"
  - "Consumer back-fill mutation (updateLocationName) declared and proven in the repository's owning plan so downstream plans never edit the file"

# Metrics
duration: 11 min
completed: 2026-10-13
---

# Phase 7 Plan 01: feeds-service Public Foundation Summary

**A public (zero-auth) feeds-service foundation: FeedBooking/ProcessedEvent JPA layer, an additive processed_events idempotency-dedup migration, a 4-attempt retry/DLQ RabbitMQ container factory mirroring Phase 6, and the shared snake_case error contract — all proven by a 7-test suite against real Postgres.**

## Performance

- **Duration:** 11 min
- **Started:** 2026-10-13T02:22:00Z
- **Completed:** 2026-10-13T02:33:00Z
- **Tasks:** 3
- **Files modified:** 16 (13 created, 3 modified)

## Accomplishments
- Idempotency-capable projection schema: V2 `processed_events` dedup table (idempotency_key UUID PK), additive-only, touching no V1 object — closes the genuine Y3-integrations.md idempotent-consumption gap V1 left open.
- FeedBooking entity (1:1 with V1) + FeedBookingRepository with the approved-upcoming and per-location read queries (for plan 07-03) and the `updateLocationName` consumer back-fill mutation (for plan 07-02), all declared in this single owning plan.
- ProcessedEvent entity + `existsByIdempotencyKey` lookup-first check for plan 07-02's consumer.
- `rabbitListenerContainerFactory` with the identical Phase 6 posture: 4 attempts (1 + 3 retries) at 5s/30s/2min, `RejectAndDontRequeueRecoverer`, `MissingIdempotencyKeyException` non-retryable, `setDefaultRequeueRejected(false)`; declares no RabbitMQ topology (definitions.json owns feeds.*.q).
- Shared `ApiError` + `FeedLocationFilterInvalidException` (400) + `GlobalExceptionHandler` with an `Exception` fallback that returns a generic 500 `INTERNAL_ERROR` and never leaks a stack trace on this public surface.
- Provably NO security: no spring-security/oauth2 dependency, no SecurityConfig, no oauth2 config anywhere under src/main; listener auto-startup flipped to true.

## Task Commits

1. **Task 1: Config, pom (NO security), listener flip, shared error contract** - `24a944e` (feat)
2. **Task 2: V2 migration, entities, repositories, schema test** - `59d7987` (feat)
3. **Task 3: RabbitMQ retry/DLQ container factory + boot test update** - `9381e17` (feat)

## Files Created/Modified
- `db/migration/V2__add_idempotency_key.sql` — additive processed_events dedup table
- `domain/FeedBooking.java` — entity 1:1 with V1 feed_bookings (assigned-id booking_id, read-only updated_at)
- `domain/ProcessedEvent.java` — entity over processed_events (assigned-id idempotency_key)
- `repository/FeedBookingRepository.java` — approved-upcoming + per-location queries + updateLocationName @Modifying
- `repository/ProcessedEventRepository.java` — existsByIdempotencyKey
- `error/ApiError.java`, `error/ApiException.java`, `error/GlobalExceptionHandler.java` — shared snake_case error contract + 500 fallback
- `rabbit/RabbitConfig.java`, `rabbit/RabbitRetryBackOffPolicy.java`, `rabbit/MissingIdempotencyKeyException.java` — consumer retry/DLQ factory
- `test/.../FeedBookingSchemaTest.java` — 4 tests (queries, dedup PK backstop, updateLocationName scoping)
- `test/resources/application-test.properties` — test profile against compose Postgres
- `pom.xml` — +spring-retry, +spring-aspects (NO security)
- `application.yml` — listener auto-startup false->true
- `test/.../ApplicationContextBootTest.java` — switched to compose Postgres, broker-free, 3 tests

## Decisions Made
- **Lookup table over upsert for idempotency** — a booking's lifecycle (created→approved→updated→deleted) emits multiple distinct events mutating the same row by booking_id; only a dedup key on the event's own idempotency_key distinguishes a redelivery from a legitimate later event.
- **No security at all** — F9.6 / Phase 7 Success Criterion 3 make feeds public; the gateway's permitAll on /feeds/** owns the (absence of) authz. Adding security here would be a defect.
- **FeedLocationFilterInvalidException as a nested static class on ApiException** — satisfies the plan's `grep "class FeedLocationFilterInvalidException"` contract while keeping the single-exception-this-phase footprint minimal.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Switched FeedBookingSchemaTest and ApplicationContextBootTest from Testcontainers to running docker-compose Postgres**
- **Found during:** Task 2 (verified the existing boot test fails with Testcontainers)
- **Issue:** The sandbox's Docker daemon reports client API version 1.32; Testcontainers 1.20.4 requires ≥1.40 (`client version 1.32 is too old. Minimum supported API version is 1.40`). `PostgreSQLContainer` cannot start, so the plan's prescribed `@Testcontainers`/`PostgreSQLContainer` wiring for the schema test (and the pre-existing boot test) is non-functional here.
- **Fix:** Used `@DataJpaTest @ActiveProfiles("test") @AutoConfigureTestDatabase(Replace.NONE)` (schema test) and `@ActiveProfiles("test")` (boot test) with a new `application-test.properties` pointing at the running compose Postgres. Created `feeds_db_test` via `docker exec project-postgres-1 psql -U postgres -c "CREATE DATABASE feeds_db_test;"`. Identical workaround to Phase 4 plan 04-01 and Phase 6 plan 06-03.
- **Files modified:** `FeedBookingSchemaTest.java`, `ApplicationContextBootTest.java`, `application-test.properties` (new)
- **Verification:** 7/7 tests pass against real Postgres; proves identical semantics to what Testcontainers would have.

**2. [Rule 1 - Bug] Duplicate-key dedup assertion used save() on an assigned-id entity (would MERGE, not INSERT)**
- **Found during:** Task 2 (first schema-test run — 3/4 passed)
- **Issue:** `ProcessedEvent` has an assigned (non-@GeneratedValue) id, so `JpaRepository.save()` on an existing key issues a SELECT-then-UPDATE (merge), never an INSERT, so the PRIMARY KEY constraint was never exercised and the test expected an exception that was never thrown.
- **Fix:** Switched the duplicate-insert assertion to `TestEntityManager.persist()` + `flush()` (always an INSERT — the exact race-past-lookup redelivery path), accepting `PersistenceException` / `ConstraintViolationException` / `DataIntegrityViolationException`.
- **Files modified:** `FeedBookingSchemaTest.java`
- **Verification:** 4/4 schema tests pass; the dedup backstop now genuinely triggers the constraint.

---

**Total deviations:** 2 auto-fixed (1 blocking environment workaround, 1 test bug).
**Impact on plan:** Both necessary for test execution/correctness in this sandbox. The adapted tests prove the same semantics the plan intended. No production code behavior changed by the deviations; no scope creep.

## Issues Encountered
- Maven/Java are not installed on the host sandbox; all builds/tests run inside the project's own `maven:3.9-eclipse-temurin-21` Docker image (`--network host` for DB-backed tests). This is the project's established build path (its Dockerfile uses the same image) — not a deviation, just the execution mechanism.

## Known Stubs
None found — no controllers, no mutating endpoints, no TODO/FIXME code (the only "placeholder" string is a comment documenting the removed Phase-2 auto-startup placeholder). Verified `! grep -rq "@PostMapping|@PutMapping|@DeleteMapping" src/main` (T-07-01-01).

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- feeds-service foundation is complete and fully green: public (security-free), idempotency-ready schema, proven FeedBooking/ProcessedEvent repositories, retry/DLQ container factory, shared error contract.
- Plan 07-02 (2-queue projection consumer) and plan 07-03 (4 public feed endpoints) can now build in parallel on this foundation — 07-02 calls `updateLocationName`/`existsByIdempotencyKey`, 07-03 calls the read queries and throws `FeedLocationFilterInvalidException`.

## Self-Check: PASSED
- All 13 created files exist on disk (verified).
- All 3 task commits present: `24a944e`, `59d7987`, `9381e17`.
- Plan-level build ran and passed: `mvn package` → BUILD SUCCESS, 7 tests (3 boot + 4 schema), 0 failures, exit 0.
- `## Known Stubs` section present; no blocking stubs.

---
*Phase: 07-public-feeds*
*Completed: 2026-10-13*
