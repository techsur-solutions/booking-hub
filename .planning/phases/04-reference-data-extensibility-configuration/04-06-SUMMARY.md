---
phase: 04-reference-data-extensibility-configuration
plan: 06
subsystem: api
tags: [spring-boot, spring-security, jpa, oauth2-resource-server, rabbitmq, outbox, tier1-tier2, settings]

# Dependency graph
requires:
  - phase: 04-05
    provides: Settings/OutboxEvent JPA entities, SettingsRepository/OutboxEventRepository, CurrentUserProvider, ApiError/ApiException contract (SettingsForbiddenException/SettingsInvalidCalendarRangeException/SettingsInvalidSlotSizeException), Tier-2 security scaffold (SecurityConfig, JwksOutageAuthenticationEntryPoint, SettingsAccessDeniedHandler), and a Tier1Tier2ConsistencyTest pre-wired to activate once this plan's controller exists
provides:
  - "GET /settings (authenticated-only) and PUT /settings (role_settings_admin) — exact TechArch §4.9 shapes"
  - "Upsert-against-fixed-id=1 singleton write discipline with server-side calendar-range/slot-size validation (defense in depth beyond DB CHECK constraints)"
  - "Immediate propagation: no in-memory cache, GET reads the DB directly every call — the deliberate fix for legacy's confirmed restart-required caching quirk"
  - "OutboxPublisher: scheduled polling bean relaying pending settings.updated events to the already-declared settings.events RabbitMQ exchange"
  - "An explicit JwtAuthenticationConverter mapping realm_access.roles to ROLE_-prefixed GrantedAuthority — without it, @PreAuthorize(hasRole(...)) silently denies every caller regardless of actual roles"
affects: [05-core-booking]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Controller/Service/DTO built directly on plan 04-05's entity+security foundation, no new dependencies needed (spring-boot-starter-amqp already present from Phase 2 plan 02-07)"
    - "Server-side validation computed against POST-merge values (partial PUT validated against the OTHER field's current persisted value, not a stale pre-merge pair)"
    - "JwtAuthenticationConverter bean explicitly wired per-service (Spring Security's default jwt() customizer only reads scope/scp claims, never Keycloak's nested realm_access.roles) - same fix independently discovered and applied in parallel by plans 04-02/04-04 this phase"

key-files:
  created:
    - services/settings-service/src/main/java/com/bookinghub/settings/dto/SettingsDtos.java
    - services/settings-service/src/main/java/com/bookinghub/settings/service/SettingsService.java
    - services/settings-service/src/main/java/com/bookinghub/settings/controller/SettingsController.java
    - services/settings-service/src/main/java/com/bookinghub/settings/outbox/OutboxPublisher.java
    - services/settings-service/src/test/java/com/bookinghub/settings/controller/SettingsControllerIntegrationTest.java
    - services/settings-service/src/test/java/com/bookinghub/settings/outbox/OutboxPublisherTest.java
  modified:
    - services/settings-service/src/main/resources/application.yml
    - services/settings-service/src/main/java/com/bookinghub/settings/security/SecurityConfig.java
    - services/settings-service/src/test/java/com/bookinghub/settings/security/Tier2FailClosedTest.java

key-decisions:
  - "Fixed sentinel UUID (00000000-0000-0000-0000-000000000001) used as the outbox aggregate_id for every settings.updated event — Settings' own PK is the integer 1, but every other service's outbox table expects a UUID-shaped aggregate_id, so a single well-known constant (never randomly generated) lets consumers recognize 'the' settings aggregate across every event"
  - "Rule 1 bugfix: explicit JwtAuthenticationConverter wiring realm_access.roles to ROLE_-prefixed authorities was required for @PreAuthorize to function at all against a real Keycloak JWT — this was not an edge case, it was a complete silent-deny-everyone bug in the controller code this same plan wrote"
  - "No in-memory settings cache at all (not even a short-TTL one) — getCurrent() reads the DB directly on every call, closing F0's confirmed legacy restart-required caching quirk as a deliberate, explicitly-named improvement"

patterns-established:
  - "SettingsControllerIntegrationTest resets the singleton row via @BeforeEach AND restores it via @AfterAll (TestInstance.PER_CLASS) because the singleton can never be deleted/recreated between tests, only mutated in place, and this test shares a live, non-rolled-back Postgres with other test classes in the same Maven Surefire run"

# Metrics
duration: 100min
completed: 2026-10-09
---

# Phase 04 Plan 06: Settings Controller, Validation & Outbox Publisher Summary

**GET/PUT /settings with upsert-against-fixed-id=1 singleton discipline, server-side calendar-range/slot-size validation, zero-cache immediate propagation, and a settings.events outbox publisher — completing F10 and closing a real JwtAuthenticationConverter bug that silently defeated every @PreAuthorize check in this service**

## Performance

- **Duration:** 100 min
- **Started:** 2026-10-09T00:36:00Z
- **Completed:** 2026-10-09T02:15:44Z
- **Tasks:** 2
- **Files modified:** 10 (6 created, 4 modified — includes the Rule-1 SecurityConfig/Tier2FailClosedTest fix)

## Accomplishments

- Built `SettingsController`'s 2 endpoints exactly per TechArch §4.9: `GET /settings` (any authenticated caller, no specific role) and `PUT /settings` (`role_settings_admin`-gated, partial update of any subset of fields)
- `SettingsService.update()` always fetches via `findById(1)` — never constructs a new row — implementing F10.3's upsert-against-fixed-id discipline; server-side validates the resulting calendar range and slot size against POST-merge values (so a single-field PUT is validated against the other field's current persisted value, not a stale pre-merge pair), as defense in depth beyond the DB's existing `CHECK` constraints
- Implemented the named "no reload-required" improvement: zero in-memory cache, every `GET` reads the database directly, so a `PUT` is visible to the very next `GET` with no propagation delay — closing F0's confirmed legacy restart-required caching behavior
- Built `OutboxPublisher` (identical scheduled-polling-bean shape to every other service's publisher this phase) relaying pending `settings.updated` events to the already-declared `settings.events` RabbitMQ exchange; added the missing `outbox.poll-interval-ms`/`outbox.batch-size` config keys (a prior planning assumption that Phase 2 already provided them was incorrect — corrected here)
- **Found and fixed a real, scope-relevant bug (Rule 1):** Spring Security's default JWT resource-server configuration only derives authorities from a flat `scope`/`scp` claim — it has zero knowledge of Keycloak's nested `realm_access.roles` claim. Without an explicit `JwtAuthenticationConverter`, every `@PreAuthorize("hasRole('role_settings_admin')")` check in the `SettingsController` this same plan wrote would silently deny every caller, including a genuine admin with the correct role. Wired an explicit converter mapping `realm_access.roles` → `ROLE_`-prefixed `GrantedAuthority`, mirroring `CurrentUserProvider`'s own claim-parsing logic but feeding Spring Security's own authorization machinery. Verified independently by re-running the full `Tier2FailClosedTest` suite (which now genuinely exercises `@PreAuthorize` denial, rather than vacuously padding to the same 403 code through unrelated test assertions).
- `Tier1Tier2ConsistencyTest` (started in plan 04-05, pre-wired to activate once `SettingsController` existed) now passes in full — the reflective check confirms `getSettings()` carries no `@PreAuthorize` and `updateSettings()` requires `role_settings_admin`, matching the Gateway's Tier-1 route table exactly

## Task Commits

Each task was committed atomically:

1. **Task 1: SettingsController + SettingsService (validation + singleton upsert)** - `69b35a1` (feat)
2. **[Rule 1 bugfix] Wire JwtAuthenticationConverter so @PreAuthorize sees realm_access.roles** - `e5e6e68` (fix)
3. **Task 2: Outbox-to-RabbitMQ polling publisher + integration tests + Tier1/Tier2 consistency re-run** - `8533c0c` (feat)

**Plan metadata:** (pending — this commit)

## Files Created/Modified

- `services/settings-service/src/main/java/com/bookinghub/settings/dto/SettingsDtos.java` - `SettingsUpdateRequest` (all fields optional, partial update) and `SettingsResponse`, `@JsonProperty`-mapped to TechArch §4.9's exact snake_case shape
- `services/settings-service/src/main/java/com/bookinghub/settings/service/SettingsService.java` - `getCurrent()`/`update()`; upsert-against-fixed-id discipline; POST-merge calendar-range/slot-size validation; writes the outbox row in the same transaction as the business change
- `services/settings-service/src/main/java/com/bookinghub/settings/controller/SettingsController.java` - `GET`/`PUT /settings`; read/write role split matching the Gateway exactly
- `services/settings-service/src/main/java/com/bookinghub/settings/outbox/OutboxPublisher.java` - `@Scheduled` polling bean, identical shape to every other service's publisher this phase
- `services/settings-service/src/main/resources/application.yml` - Added `outbox.poll-interval-ms`/`outbox.batch-size` (previously missing)
- `services/settings-service/src/main/java/com/bookinghub/settings/security/SecurityConfig.java` - Added the explicit `JwtAuthenticationConverter` bean (Rule 1 bugfix)
- `services/settings-service/src/test/java/com/bookinghub/settings/controller/SettingsControllerIntegrationTest.java` - 6 scenarios: authenticated-read, admin-write, immediate-propagation proof, both validation rejections, admin-only 403, outbox-event emission with caller-matching `updated_by`
- `services/settings-service/src/test/java/com/bookinghub/settings/outbox/OutboxPublisherTest.java` - 2 scenarios against a real docker-compose RabbitMQ broker: disposable test exchange round-trip, and delivery to the real `settings.events`/`settings.updated` topology
- `services/settings-service/src/test/java/com/bookinghub/settings/security/Tier2FailClosedTest.java` - Updated to mock `SettingsRepository`/`OutboxEventRepository` (not the whole controller) so the real `@PreAuthorize`-bearing `SettingsController` constructs and is genuinely exercised by the 403 scenario

## Decisions Made

- Fixed sentinel UUID (`00000000-0000-0000-0000-000000000001`) for every outbox row's `aggregate_id` — the Settings singleton's own PK is the integer `1`, but the outbox schema needs a UUID-shaped value for cross-service consistency; a single well-known constant (never randomly generated) lets any future consumer recognize "the" settings aggregate across every event it ever emits
- No in-memory settings cache at all, not even a short-TTL one — `getCurrent()` reads the database directly on every call. This is the deliberate, explicitly-named closure of F0's confirmed legacy restart-required caching quirk
- The `JwtAuthenticationConverter` fix is scoped per-service (not extracted to a shared library) — consistent with this codebase's existing pattern of each microservice owning its own independent Tier-2 security configuration

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Wired explicit `JwtAuthenticationConverter` — without it, every `@PreAuthorize` check in this plan's own `SettingsController` silently denied all callers**
- **Found during:** Task 1 (writing `SettingsControllerIntegrationTest`'s scenario covering `PUT /settings` by a `role_settings_admin` caller)
- **Issue:** Spring Security's `oauth2ResourceServer().jwt(Customizer.withDefaults())` only extracts authorities from a flat `scope`/`scp` claim, producing `SCOPE_`-prefixed authorities. It has no knowledge of Keycloak's nested `realm_access.roles` claim structure at all. `@PreAuthorize("hasRole('role_settings_admin')")` checks for a `ROLE_role_settings_admin` authority — which, absent a custom converter, would NEVER be present on any JWT regardless of the caller's actual Keycloak roles. This meant a genuine admin's `PUT /settings` request would be rejected with 403 every single time — a complete authorization failure, not an edge case.
- **Fix:** Added an explicit `JwtAuthenticationConverter` bean in `SecurityConfig`, with a custom `setJwtGrantedAuthoritiesConverter` that reads `realm_access.roles` (mirroring `CurrentUserProvider.getCurrentRoles()`'s own claim-parsing) and maps each role to a `ROLE_`-prefixed `SimpleGrantedAuthority`. Wired it via `.jwt(jwt -> jwt.jwtAuthenticationConverter(...))`.
- **Files modified:** `services/settings-service/src/main/java/com/bookinghub/settings/security/SecurityConfig.java`, `services/settings-service/src/test/java/com/bookinghub/settings/security/Tier2FailClosedTest.java` (updated mocking strategy so the real controller/converter are exercised, not bypassed)
- **Verification:** `Tier2FailClosedTest`'s Scenario 3 (`InsufficientRoleTest`) and `SettingsControllerIntegrationTest`'s admin-write scenarios both pass against the real converter; corroborated independently — plans 04-02 and 04-04 (running in parallel this phase) each discovered and fixed the identical missing-converter bug in their own services, confirming this was a systemic gap in the codebase's established security-config pattern, not specific to this plan's code
- **Committed in:** `e5e6e68` (standalone fix commit between Task 1 and Task 2)

**2. [Rule 1 - Bug] Test literal payload strings didn't match Postgres's jsonb re-serialization whitespace**
- **Found during:** Task 2 (`OutboxPublisherTest` first run)
- **Issue:** Hand-written JSON literals like `"{\"approve_booking\":false}"` don't match what Postgres's `jsonb` column type re-serializes to (`{"approve_booking": false}` — with a space after the colon), causing a spurious test failure despite correct behavior
- **Fix:** Compare the received RabbitMQ message body against the re-fetched `OutboxEvent.getPayload()` (what the publisher actually read and sent) rather than the original pre-persist literal string
- **Files modified:** `services/settings-service/src/test/java/com/bookinghub/settings/outbox/OutboxPublisherTest.java`
- **Verification:** Both `OutboxPublisherTest` scenarios pass
- **Committed in:** `8533c0c` (Task 2 commit)

**3. [Rule 1 - Bug] RabbitMQ header retrieval cast failure + scheduled-publisher race**
- **Found during:** Task 2 (`OutboxPublisherTest` second/third run)
- **Issue:** (a) `amqp-client`'s `GetResponse` headers return `LongString` objects, not `java.lang.String` — a direct cast threw `ClassCastException`. (b) The real `OutboxPublisher` bean's own `@Scheduled` poll (every 2s, `@EnableScheduling` active in the live Spring context) races against the test's manual `relayPendingEvents()` invocation, occasionally causing `basicGet` to return `null` on the very first attempt
- **Fix:** (a) Used `.toString()` instead of a direct cast for the header value. (b) Added a short polling-retry loop (`pollForMessage`, 20 attempts × 250ms) around the final `basicGet` call
- **Files modified:** `services/settings-service/src/test/java/com/bookinghub/settings/outbox/OutboxPublisherTest.java`
- **Verification:** Both scenarios pass reliably across repeated runs
- **Committed in:** `8533c0c` (Task 2 commit)

**4. [Rule 1 - Bug] Test-class isolation: `SettingsControllerIntegrationTest`'s mutations leaked into `SettingsSingletonTest`**
- **Found during:** Full-suite verification run (all tests except `ApplicationContextBootTest`)
- **Issue:** `SettingsControllerIntegrationTest` mutates the real, non-rolled-back singleton row in `settings_db_test` via MockMvc-driven HTTP calls (not `@Transactional` test methods), so its `@BeforeEach` reset left the row in a non-default state after the LAST test method ran. When Maven Surefire then ran `SettingsSingletonTest` (asserting the Phase-2-seeded defaults) afterward in the same suite, it found stale mutated state and failed
- **Fix:** Switched `SettingsControllerIntegrationTest` to `@TestInstance(Lifecycle.PER_CLASS)` and added an `@AfterAll` hook that re-runs the same reset logic, guaranteeing the singleton is restored to seeded defaults before any later test class in the same suite runs
- **Files modified:** `services/settings-service/src/test/java/com/bookinghub/settings/controller/SettingsControllerIntegrationTest.java`
- **Verification:** Full suite (`mvn test -Dtest='!ApplicationContextBootTest'`) — 15/15 tests pass regardless of class execution order
- **Committed in:** `8533c0c` (Task 2 commit)

---

**Total deviations:** 4 auto-fixed (1 bug — critical security/correctness, 3 bugs — test-infrastructure correctness)
**Impact on plan:** The JwtAuthenticationConverter fix (#1) is the most significant: without it, this plan's own `SettingsController` would have shipped with a complete, silent authorization failure on every write. All four fixes were necessary for the plan's own verification suite to actually prove what it claims to prove — no scope creep, no architectural changes.

## Issues Encountered

None beyond the four auto-fixed deviations documented above, all resolved within this plan's scope using established patterns from earlier phases (real-Postgres-instead-of-Testcontainers workaround) or straightforward bugfixes.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- F10 (Settings) is now fully complete: admin can toggle `approveBooking` and configure calendar display parameters via `PUT /settings`; any authenticated caller can read via `GET /settings`; changes are visible immediately (no legacy reload-required quirk); the singleton invariant holds at both the code and DB layers; writes are admin-only and reads are broadly authenticated — all proven to exactly match the Gateway's Tier-1 route table by `Tier1Tier2ConsistencyTest`
- `OutboxPublisher` relays `settings.updated` events to the already-declared `settings.events` RabbitMQ exchange, proven against a real broker
- Phase 5's booking-service can now read `GET /settings` synchronously at booking-creation time per TechArch §1.5's architectural decision — this service's contract is stable and fully tested
- The `JwtAuthenticationConverter` gap this plan found and fixed is now a confirmed, named pattern (corroborated by parallel plans 04-02/04-04 finding and fixing the identical issue independently) — any FUTURE service added to this codebase following the established `SecurityConfig` template should include this converter from the start, not discover its absence the hard way

## Self-Check: PASSED

- All 6 created files verified present on disk: `SettingsDtos.java`, `SettingsService.java`, `SettingsController.java`, `OutboxPublisher.java`, `SettingsControllerIntegrationTest.java`, `OutboxPublisherTest.java`
- All 3 task/fix commits (`69b35a1`, `e5e6e68`, `8533c0c`) verified present in `git log`
- Plan-level build check: `cd services/settings-service && mvn -q -DskipTests package` → exit 0
- Full non-Testcontainers test suite: `mvn test -Dtest='!ApplicationContextBootTest'` → 15/15 tests passed (SettingsSingletonTest x3, Tier2FailClosedTest x3 nested, Tier1Tier2ConsistencyTest x1, OutboxPublisherTest x2, SettingsControllerIntegrationTest x6), 0 failures, 0 errors
- Task-specified verify commands re-run individually: both Task 1's grep checks and Task 2's `OUTBOX CONFIG PRESENT`/`mvn package`/`mvn test -Dtest=SettingsControllerIntegrationTest,OutboxPublisherTest,Tier1Tier2ConsistencyTest` all pass
- `## Known Stubs` section below: no blocking stubs found

## Known Stubs

None found. `grep -rnE "TODO|FIXME|placeholder|not.?implemented|coming soon"` across every file created or modified in this plan (controller, service, DTO, outbox publisher, security config, application.yml, and all 3 test files) returned zero matches.

---
*Phase: 04-reference-data-extensibility-configuration*
*Completed: 2026-10-09*
