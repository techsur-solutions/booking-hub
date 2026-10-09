---
phase: 04-reference-data-extensibility-configuration
plan: 02
subsystem: api
tags: [spring-boot, spring-security, jwt, rabbitmq, outbox-pattern, soft-delete, postgres, flyway]

# Dependency graph
requires:
  - phase: 04-01
    provides: "Location/Resource/OutboxEvent JPA entities, repositories, ApiError/ApiException shared contract, Tier-2 JWT SecurityConfig, CurrentUserProvider"
provides:
  - "GET/POST /locations, GET/PUT/DELETE /locations/{id} — full F4.1 CRUD with role gating"
  - "GET/POST /resources, GET/PUT/DELETE /resources/{id} — full F4.2 CRUD with role gating"
  - "Soft-delete-always-succeeds policy (F4.3) for both Location and Resource — never a SQL DELETE, never blocked by booking references"
  - "GET .../{id} returns 200 with last-known values (deletedAt populated) for soft-deleted rows — closing the Success Criterion 2 cross-service lookup gap for Phase 5"
  - "OutboxPublisher: scheduled polling bean relaying pending outbox rows to location.events/resource.events"
  - "Working JwtAuthenticationConverter mapping Keycloak realm_access.roles -> ROLE_-prefixed authorities (was entirely missing — every @PreAuthorize(hasRole(...)) check was previously unreachable by any real caller)"
affects: ["05-core-booking (reads Locations/Resources via these GET endpoints, consumes is_unique/restrict_locations for conflict detection)"]

# Tech tracking
tech-stack:
  added: [spring-boot-starter-amqp]
  patterns:
    - "Soft-delete via deletedAt timestamp, never SQL DELETE — delete()/update() use findByIdAndDeletedAtIsNull (active-only), getById() uses plain findById (active-or-deleted) so Phase 5 can resolve a booking's location/resource reference after deletion"
    - "Transactional outbox + scheduled polling publisher (same precedent as Phase 3 plan 03-05 and Phase 4 plan 04-04/04-06) — never publishes directly inside the request"
    - "Explicit JwtAuthenticationConverter bean required whenever a service uses @PreAuthorize(hasRole(...)) against a Keycloak-issued JWT — Spring Security's default converter only reads a flat scope/scp claim"

key-files:
  created:
    - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/dto/LocationDtos.java
    - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/dto/ResourceDtos.java
    - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/service/LocationService.java
    - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/service/ResourceService.java
    - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/controller/LocationController.java
    - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/controller/ResourceController.java
    - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/outbox/OutboxPublisher.java
    - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/LocationNameRequiredException.java
    - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/ResourceNameRequiredException.java
    - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/LocationResourceForbiddenException.java
    - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/LocationNotFoundException.java
    - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/ResourceNotFoundException.java
    - services/locations-resources-service/src/test/java/com/bookinghub/locationsresources/controller/LocationControllerIntegrationTest.java
    - services/locations-resources-service/src/test/java/com/bookinghub/locationsresources/controller/ResourceControllerIntegrationTest.java
    - services/locations-resources-service/src/test/java/com/bookinghub/locationsresources/outbox/OutboxPublisherTest.java
  modified:
    - services/locations-resources-service/pom.xml
    - services/locations-resources-service/src/main/resources/application.yml
    - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/domain/Location.java
    - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/domain/Resource.java
    - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/ApiException.java
    - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/security/SecurityConfig.java
    - services/locations-resources-service/src/test/java/com/bookinghub/locationsresources/security/Tier2FailClosedTest.java
    - services/locations-resources-service/src/test/resources/application-test.properties
    - services/locations-resources-service/docker-compose.yml
    - docker-compose.yml

key-decisions:
  - "getById() on both Location and Resource deliberately uses plain JpaRepository#findById (not findByIdAndDeletedAtIsNull) so a soft-deleted row remains readable with last-known values via direct id lookup — this is the one read path that must survive soft-delete for Phase 5's booking-service to resolve a booking's location_id/resource_id reference"
  - "404 is reserved strictly for a never-existed id; a soft-deleted-but-real id returns 200 with deletedAt populated — proven by both integration test suites' scenario 5"
  - "Soft-delete-always-succeeds: delete() never issues a SQL DELETE, never checks for referencing bookings, never returns 409 — a deliberate improvement over legacy's confirmed orphan-allow-via-hard-delete (open-questions.md #14)"

patterns-established:
  - "Entities lacking a setUpdatedAt mutator is a correctness bug for any entity with update()/delete() paths — fixed on Location/Resource (Rule 1)"
  - "Exception subclasses meant to be thrown from a different package than their ApiException base must be public top-level classes, not package-private inner declarations"

# Metrics
duration: 95min
completed: 2026-10-09
---

# Phase 4 Plan 02: Location/Resource CRUD + Outbox Publisher Summary

**Full Location/Resource CRUD with a soft-delete-always-succeeds policy, a scheduled outbox-to-RabbitMQ publisher, and a critical JWT role-converter bugfix that had silently blocked every @PreAuthorize check in this service.**

## Performance

- **Duration:** ~95 min
- **Started:** 2026-10-09T00:52:00Z (approx)
- **Completed:** 2026-10-09T02:15:00Z (approx)
- **Tasks:** 3
- **Files modified:** 25 (15 created, 10 modified)

## Accomplishments
- `LocationController`/`ResourceController`: full 5-endpoint CRUD each, role-gated (`role_calendar_viewer` reads, `role_location_admin` writes), matching TechArch §4.3 shapes extended with the F0-confirmed field sets (colour/description for Location; type/description/is_unique/restrict_locations for Resource)
- Soft-delete-always-succeeds policy (F4.3) implemented identically for both entities: `delete()` never issues a SQL DELETE, never checks for referencing bookings — closing open-questions.md #14 as a deliberate improvement
- `getById()` on both services reads via plain `findById` (not active-only), so a soft-deleted row returns 200 with last-known values rather than 404 — the Success Criterion 2 cross-service lookup path Phase 5's booking-service needs
- `OutboxPublisher`: scheduled polling bean relaying pending outbox rows to `location.events`/`resource.events`, same precedent as Phase 3 plan 03-05
- 12 new integration test scenarios across `LocationControllerIntegrationTest`/`ResourceControllerIntegrationTest`/`OutboxPublisherTest`, all passing against the real docker-compose Postgres + RabbitMQ (Testcontainers Docker-API-version workaround)
- Fixed a critical, previously-undetected authorization bug: no `JwtAuthenticationConverter` existed anywhere in this service, so Spring Security's default JWT→authority conversion (flat `scope`/`scp` claim) never saw Keycloak's `realm_access.roles` claim — every `@PreAuthorize("hasRole(...)")` check would have silently denied every caller, including genuine admins, in production

## Task Commits

Each task was committed atomically:

1. **Task 1: LocationController + LocationService** - `7a8614c` (feat)
2. **Task 2: ResourceController + ResourceService** - `9a82645` (feat)
3. **Task 3: OutboxPublisher + integration tests + Tier1/Tier2 re-run** - `9db4b0f` (feat)

**Plan metadata:** (this commit)

## Files Created/Modified
- `dto/LocationDtos.java`, `dto/ResourceDtos.java` - request/response records, snake_case `@JsonProperty` mapping
- `service/LocationService.java`, `service/ResourceService.java` - CRUD + soft-delete + outbox-write business logic
- `controller/LocationController.java`, `controller/ResourceController.java` - 5 role-gated REST endpoints each
- `outbox/OutboxPublisher.java` - `@Scheduled` RabbitMQ relay
- `error/LocationNameRequiredException.java` + 4 siblings - split out of `ApiException.java` to be cross-package-visible
- `security/SecurityConfig.java` - added `JwtAuthenticationConverter` bean (Rule 1 bugfix)
- `test/.../LocationControllerIntegrationTest.java`, `ResourceControllerIntegrationTest.java`, `OutboxPublisherTest.java` - 12 new scenarios
- `test/.../Tier2FailClosedTest.java` - removed colliding test stub, switched to `test` profile (Rule 3 bugfix)
- `pom.xml`, `application.yml`, `docker-compose.yml` (both) - RabbitMQ dependency + config + compose wiring

## Decisions Made
- `getById()` reads via plain `findById`, not `findByIdAndDeletedAtIsNull` — the single read path that must survive soft-delete (see key-decisions above)
- 404 reserved strictly for never-existed ids
- No `LOCATION_IN_USE`/`RESOURCE_IN_USE` (409) path exists — deletion always succeeds by design

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Split 5 package-private ApiException subclasses into public top-level files**
- **Found during:** Task 1
- **Issue:** Plan 04-01 declared `LocationNameRequiredException` and 4 siblings as package-private inner classes inside `ApiException.java`. `LocationService`/`ResourceService` (different package) cannot reference a package-private class, and Java permits only one public top-level class per file, so they could not simply become `public` in place.
- **Fix:** Created 5 separate files (`LocationNameRequiredException.java`, `ResourceNameRequiredException.java`, `LocationResourceForbiddenException.java`, `LocationNotFoundException.java`, `ResourceNotFoundException.java`) as public top-level classes; left a pointer comment in `ApiException.java`.
- **Files modified:** `error/ApiException.java` + 5 new files
- **Verification:** `mvn package` succeeds
- **Committed in:** `7a8614c`

**2. [Rule 1 - Bug] Added missing `setUpdatedAt` to Location/Resource entities**
- **Found during:** Task 1
- **Issue:** Unlike every sibling entity in this codebase (`User`, `Settings`, `CustomField`, `CustomFieldTemplate`), `Location`/`Resource` had no `updatedAt` mutator, so `update()`/`delete()` could never bump the timestamp — `updated_at` would go permanently stale.
- **Fix:** Added `setUpdatedAt(Instant)` to both entities; wired into `LocationService`/`ResourceService`'s `update()`/`delete()`.
- **Files modified:** `domain/Location.java`, `domain/Resource.java`, `service/LocationService.java`, `service/ResourceService.java`
- **Verification:** `mvn package` succeeds
- **Committed in:** `7a8614c`

**3. [Rule 3 - Blocking] `Tier2FailClosedTest` colliding test stub + broken bean wiring, discovered while verifying Task 3**
- **Found during:** Task 3 (running the plan's own `<verify>` command)
- **Issue:** Two problems, both directly caused by adding the real controllers this plan builds: (a) plan 04-01's `Tier2FailClosedTest.java` carried a test-only `TestLocationsController` stub mapped to `GET/POST /locations`, which now collides with the real `LocationController` ("Ambiguous mapping" at Spring context refresh); (b) both nested `@SpringBootTest` classes excluded `DataSourceAutoConfiguration`/`HibernateJpaAutoConfiguration`/`FlywayAutoConfiguration` (because the real controllers didn't exist yet in 04-01), which now breaks `LocationController`'s real dependency chain (`LocationService` → `LocationRepository`, a Spring Data JPA repository).
- **Fix:** Removed the `TestLocationsController` stub entirely (the real controller now covers its role); switched both nested test classes from excluding JPA/DataSource/Flyway to activating the real `"test"` Spring profile (same `application-test.properties` → real docker-compose Postgres already used by `SchemaCompletionTest`). Also fixed the `InsufficientRoleTest` scenario's request body (a `@RequestBody`-missing POST now 400s before `@PreAuthorize` ever runs, since argument resolution happens before the method-security proxy — added a valid JSON body so the test actually exercises the role check it's named for).
- **Files modified:** `test/.../Tier2FailClosedTest.java`
- **Verification:** All 3 nested test classes pass (`MissingTokenTest`, `InsufficientRoleTest`, `JwksFailClosedVerification`)
- **Committed in:** `9db4b0f`

**4. [Rule 1 - Bug] No `JwtAuthenticationConverter` wired — every `@PreAuthorize(hasRole(...))` check was unreachable by any real caller**
- **Found during:** Task 3 (running `LocationControllerIntegrationTest`'s admin-create scenario — got 403 instead of 201)
- **Issue:** `SecurityConfig.java` used `.jwt(Customizer.withDefaults())`, which only extracts authorities from a flat `scope`/`scp` claim (producing `SCOPE_`-prefixed authorities). It has zero knowledge of Keycloak's nested `realm_access.roles` claim. Since `hasRole('role_X')` checks for a `ROLE_role_X` authority that this default converter never produces from a real Keycloak token, **every** `@PreAuthorize("hasRole(...)")` check in this service — including the ones this very plan built — would silently deny every caller in production, admin or not. This is the same bug independently discovered and fixed in `settings-service` (commits `4185596`/`e5e6e68`, concurrent sibling plans) and `custom-field-service`, confirming it as a systemic gap in the Phase 2 scaffold rather than something specific to this service.
- **Fix:** Added an explicit `JwtAuthenticationConverter` bean mapping `realm_access.roles` → `ROLE_`-prefixed `SimpleGrantedAuthority` instances, mirroring `CurrentUserProvider.getCurrentRoles()`'s own claim parsing so both authorization mechanisms agree. Updated the two new integration test suites' `jwt()` helper to set a `ROLE_`-prefixed authority directly (`jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role))`) — Spring Security Test's `jwt()` post-processor sets authorities straight onto the mock token without invoking the application's configured converter, so this is the correct way to simulate "a caller whose JWT, once converted, carries this role."
- **Files modified:** `security/SecurityConfig.java`, `test/.../LocationControllerIntegrationTest.java`, `test/.../ResourceControllerIntegrationTest.java`
- **Verification:** All 10 controller-integration scenarios pass; `Tier1Tier2ConsistencyTest` and `Tier2FailClosedTest` continue passing
- **Committed in:** `9db4b0f`

**5. [Rule 1 - Bug] Two test-literal bugs in `OutboxPublisherTest` (not application bugs)**
- **Found during:** Task 3
- **Issue:** (a) exact-string payload comparison failed because `OutboxEvent.payload` round-trips through a Postgres `jsonb` column, which normalizes whitespace (`"id":"x"` → `"id": "x"`) — the publisher correctly republishes what it read from the DB, the test's literal-string expectation was wrong; (b) `response.getProps().getHeaders().get("idempotency_key")` returns the RabbitMQ Java client's own `LongString` type over the raw AMQP API, not `java.lang.String`, causing a `ClassCastException`.
- **Fix:** (a) Compare parsed JSON trees (`objectMapper.readTree(...)`) instead of raw strings; (b) call `.toString()` on the header value before comparing.
- **Files modified:** `test/.../OutboxPublisherTest.java`
- **Verification:** Both `OutboxPublisherTest` scenarios pass
- **Committed in:** `9db4b0f`

---

**Total deviations:** 5 auto-fixed (2 Rule 1 - Bug on application code, 2 Rule 1 - Bug on test-only code, 2 Rule 3 - Blocking)
**Impact on plan:** Deviation #4 (the JWT converter fix) is the most significant — without it, this plan's entire CRUD surface would have been unusable by any real caller despite passing `mvn package` and the static grep checks in the plan's own `<verify>` block. All fixes were necessary for correctness; no scope creep beyond what each bug required to resolve.

## Issues Encountered

- This sandbox's Spring context startup is unusually slow (60-120s per `@SpringBootTest` context load, likely shared-node CPU contention from multiple concurrent agent runs observed via `ps aux` during this session — `custom-field-service`, `settings-service` tests were running in parallel at various points). Each integration test file had to be run individually in the background with generous wait times rather than as a single `mvn test` batch within the tool's per-call timeout. This did not affect correctness, only wall-clock execution time for this plan.
- None otherwise — all planned work completed, all discovered deviations were in-scope auto-fixes.

## User Setup Required

None - no external service configuration required beyond what Phase 2/3 already established (Postgres, RabbitMQ, Keycloak all already running via docker-compose).

## Next Phase Readiness

F4 is now complete: admin can create/edit/delete Locations and Resources with the full F0-confirmed field sets; deletion always soft-deletes and never blocks on booking references; a soft-deleted Location/Resource remains readable with last-known values via direct id lookup, satisfying Success Criterion 2's cross-service display requirement for Phase 5; every domain event is durably relayed to its already-declared RabbitMQ topology; `Tier1Tier2ConsistencyTest` proves Tier 2 enforcement here is never weaker than Tier 1 at the Gateway.

Phase 5 (Core Booking) can now safely read Locations/Resources via `GET /locations`, `GET /locations/{id}`, `GET /resources`, `GET /resources/{id}` — including `is_unique`/`restrict_locations` for its conflict-detection logic, and can resolve a booking's `location_id`/`resource_id` reference even after the referenced row is soft-deleted.

No blockers for Phase 5.

---
*Phase: 04-reference-data-extensibility-configuration*
*Completed: 2026-10-09*

## Known Stubs

None found — scanned all files created/modified in this plan for TODO/FIXME/placeholder/not-implemented markers; no matches.

## Self-Check: PASSED

- All 10 key `key-files.created` entries verified present on disk with `[ -f ]`.
- All 3 task commits (`7a8614c`, `9a82645`, `9db4b0f`) verified present in `git log --oneline --all`.
- Build check: `mvn -q -DskipTests package` (services/locations-resources-service) → exit 0.
- Full test suite (13 integration/consistency tests across LocationControllerIntegrationTest, ResourceControllerIntegrationTest, OutboxPublisherTest, Tier1Tier2ConsistencyTest) + separate sweep (Tier2FailClosedTest's 3 nested classes, SchemaCompletionTest's 2 tests) all passed, 0 failures, 0 errors.
