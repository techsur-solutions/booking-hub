---
phase: 04-reference-data-extensibility-configuration
plan: 05
subsystem: settings
tags: [spring-boot, jpa, flyway, outbox, tier1-tier2, contract-test, singleton-entity, jwt-security]

requires:
  - phase: 02-07
    provides: settings-service scaffold (V1 schema with singleton settings table + CHECK constraint + seed row, actuator health endpoints)
  - phase: 02-10
    provides: api-gateway Tier-1 SecurityConfig.java with /settings/** route rules (GET authenticated, PUT role_settings_admin)
  - phase: 03-02
    provides: Tier-2 security pattern template (JwksOutageAuthenticationEntryPoint, CurrentUserProvider shape, fail-closed posture)

provides:
  - Settings JPA entity (@Id with NO @GeneratedValue, fixed singleton id=1) mapping to Phase 2's existing V1 settings table
  - OutboxEvent entity + V2__add_outbox.sql additive migration (transactional outbox pattern, pgcrypto extension added)
  - SettingsRepository, OutboxEventRepository
  - Shared ApiError/ApiException contract with exactly 3 concrete exception subclasses (SettingsForbiddenException, SettingsInvalidCalendarRangeException, SettingsInvalidSlotSizeException)
  - Tier-2 JWT security layer (SecurityConfig, JwksOutageAuthenticationEntryPoint, SettingsAccessDeniedHandler, CurrentUserProvider) matching the Gateway's authenticated-read/admin-write split
  - Tier1Tier2ConsistencyTest and Tier2FailClosedTest proving the security posture against real conditions

affects: [04-06, 05-core-booking]

tech-stack:
  added: [spring-boot-starter-security, spring-boot-starter-oauth2-resource-server, wiremock-standalone, spring-security-test]
  patterns:
    - Singleton entity enforced at TWO independent layers: JPA (@Id with no @GeneratedValue) + DB (CHECK constraint)
    - Transactional outbox pattern (own table, own entity, own repository per microservice isolation)
    - Tier1/Tier2 consistency testing via source-text parsing (Gateway) + reflection (this service), gracefully assumption-skipped until the dependent controller exists
    - Single global AccessDeniedHandler sufficient when a service has exactly one admin-forbidden error code

key-files:
  created:
    - services/settings-service/src/main/resources/db/migration/V2__add_outbox.sql
    - services/settings-service/src/main/java/com/bookinghub/settings/domain/Settings.java
    - services/settings-service/src/main/java/com/bookinghub/settings/domain/OutboxEvent.java
    - services/settings-service/src/main/java/com/bookinghub/settings/repository/SettingsRepository.java
    - services/settings-service/src/main/java/com/bookinghub/settings/repository/OutboxEventRepository.java
    - services/settings-service/src/main/java/com/bookinghub/settings/error/ApiError.java
    - services/settings-service/src/main/java/com/bookinghub/settings/error/ApiException.java
    - services/settings-service/src/main/java/com/bookinghub/settings/error/SettingsForbiddenException.java
    - services/settings-service/src/main/java/com/bookinghub/settings/error/SettingsInvalidCalendarRangeException.java
    - services/settings-service/src/main/java/com/bookinghub/settings/error/SettingsInvalidSlotSizeException.java
    - services/settings-service/src/main/java/com/bookinghub/settings/error/GlobalExceptionHandler.java
    - services/settings-service/src/main/java/com/bookinghub/settings/security/SecurityConfig.java
    - services/settings-service/src/main/java/com/bookinghub/settings/security/JwksOutageAuthenticationEntryPoint.java
    - services/settings-service/src/main/java/com/bookinghub/settings/security/SettingsAccessDeniedHandler.java
    - services/settings-service/src/main/java/com/bookinghub/settings/security/CurrentUserProvider.java
    - services/settings-service/src/test/java/com/bookinghub/settings/repository/SettingsSingletonTest.java
    - services/settings-service/src/test/java/com/bookinghub/settings/security/Tier2FailClosedTest.java
    - services/settings-service/src/test/java/com/bookinghub/settings/security/Tier1Tier2ConsistencyTest.java
    - services/settings-service/src/test/resources/testcontainers.properties
  modified:
    - services/settings-service/pom.xml
    - services/settings-service/src/main/resources/application.yml

key-decisions:
  - "Scope intentionally narrower than legacy's full 33-row settings table (F0 findings/05-platform-settings.md) — only approveBooking, calendarSlotSize, calendarMinTime, calendarMaxTime carried forward; site branding/email config out of scope entirely"
  - "SETTINGS_UNAVAILABLE (503) deliberately NOT implemented here — it is the CALLING service's (Phase 5 booking-service) error to throw when this service is unreachable, not this service's own error about itself"
  - "pgcrypto extension added in V2 (not present in V1) since V1 used plain INTEGER ids with no UUID generation need; outbox table's gen_random_uuid() defaults require it"
  - "Single global SettingsAccessDeniedHandler sufficient (unlike users-permissions-service's two-code split) since settings-service has exactly one admin-forbidden error code (SETTINGS_FORBIDDEN) for the one write endpoint"

patterns-established:
  - "Tier1Tier2ConsistencyTest gracefully skips (Assumptions.assumeTrue) its controller-reflection half when the dependent controller class doesn't exist yet, while still running its Gateway-source-parsing half unconditionally — lets this plan commit a complete, passing test that plan 04-06 will exercise in full once SettingsController exists"

duration: 35min
completed: 2026-10-08
---

# Phase 04 Plan 05: Settings Domain + Tier-2 Security Foundation Summary

**Singleton Settings JPA entity (no @GeneratedValue, enforcing the one-row invariant in code alongside the DB's CHECK constraint) plus independent Tier-2 JWT re-validation proven to exactly match the Gateway's authenticated-read/admin-write split for /settings**

## Performance

- **Duration:** 35 min
- **Started:** 2026-10-08T23:10:00Z
- **Completed:** 2026-10-08T23:45:00Z
- **Tasks:** 2
- **Files modified:** 20

## Accomplishments

- F10.3 (singleton enforcement) complete at TWO independent layers: `Settings.id` has no `@GeneratedValue` (code-level) and the DB's `chk_settings_singleton` CHECK constraint (schema-level) — `SettingsSingletonTest` proves both, including that a literal second-row INSERT attempt is rejected
- Additive `V2__add_outbox.sql` migration lays the durable outbox table plan 04-06's controller needs to write `settings.updated` transactionally with the business change — V1's singleton table and seed row from Phase 2 plan 02-07 untouched
- F10.4 (readable by Booking Service) enabled via Tier-2 security: GET /settings requires only authentication (any role), PUT /settings requires `role_settings_admin` — exactly matching the Gateway's Tier-1 route table
- `Tier2FailClosedTest`'s 3 scenarios all pass against real conditions: JWKS-wiring verification, missing-token → 401 AUTH_UNAUTHENTICATED (WireMock-stubbed reachable issuer), insufficient-role on PUT → 403 SETTINGS_FORBIDDEN (not Spring's default whitelabel body)
- `Tier1Tier2ConsistencyTest` reads the Gateway's ACTUAL `SecurityConfig.java` and confirms its exact `/settings/**` GET=authenticated / PUT=role_settings_admin rules; gracefully assumption-skips its `SettingsController` reflection half until plan 04-06 creates that class, while still exercising the Gateway-side half unconditionally in this plan

## Task Commits

Each task was committed atomically:

1. **Task 1: Outbox migration + Settings/OutboxEvent entities + repositories + shared ApiError contract** - `dca458c` (feat)
2. **Task 2: Tier-2 JWT security, fail-closed JWKS handling, CurrentUserProvider, Tier1/Tier2 consistency test** - `3157e74` (feat)

**Plan metadata:** (pending — see final commit below)

## Files Created/Modified

- `services/settings-service/src/main/resources/db/migration/V2__add_outbox.sql` - Additive outbox table (pgcrypto extension, gen_random_uuid default); V1 settings table/seed unchanged
- `services/settings-service/src/main/java/com/bookinghub/settings/domain/Settings.java` - JPA entity, `@Id private Integer id` with NO `@GeneratedValue` (code-level singleton enforcement)
- `services/settings-service/src/main/java/com/bookinghub/settings/domain/OutboxEvent.java` - Same shape as every other service's OutboxEvent this phase
- `services/settings-service/src/main/java/com/bookinghub/settings/repository/SettingsRepository.java`, `OutboxEventRepository.java` - Spring Data JpaRepository interfaces
- `services/settings-service/src/main/java/com/bookinghub/settings/error/ApiError.java`, `ApiException.java`, `GlobalExceptionHandler.java` - Shared house-style error contract
- `services/settings-service/src/main/java/com/bookinghub/settings/error/SettingsForbiddenException.java`, `SettingsInvalidCalendarRangeException.java`, `SettingsInvalidSlotSizeException.java` - Exactly 3 concrete `ApiException` subclasses per FRD F10 Error States (SETTINGS_UNAVAILABLE deliberately excluded)
- `services/settings-service/src/main/java/com/bookinghub/settings/security/SecurityConfig.java` - `@EnableWebSecurity @EnableMethodSecurity`, authenticated()-baseline, fail-closed entry point + access-denied handler
- `services/settings-service/src/main/java/com/bookinghub/settings/security/JwksOutageAuthenticationEntryPoint.java` - JWKS-unreachable → 503, invalid/missing token → 401
- `services/settings-service/src/main/java/com/bookinghub/settings/security/SettingsAccessDeniedHandler.java` - Single global 403 SETTINGS_FORBIDDEN handler
- `services/settings-service/src/main/java/com/bookinghub/settings/security/CurrentUserProvider.java` - `getCurrentUserId()`/`getCurrentRoles()`/`hasRole()` reading JWT claims
- `services/settings-service/src/test/java/com/bookinghub/settings/repository/SettingsSingletonTest.java` - Testcontainers proof of the Phase-2-seeded defaults + singleton CHECK constraint enforcement
- `services/settings-service/src/test/java/com/bookinghub/settings/security/Tier2FailClosedTest.java` - 3-scenario fail-closed/access-denied proof
- `services/settings-service/src/test/java/com/bookinghub/settings/security/Tier1Tier2ConsistencyTest.java` - Gateway-source-reading + SettingsController-reflection consistency proof (controller half assumption-skipped until plan 04-06)
- `services/settings-service/pom.xml` - Added spring-boot-starter-security, spring-boot-starter-oauth2-resource-server, wiremock-standalone, spring-security-test
- `services/settings-service/src/main/resources/application.yml` - Added `spring.security.oauth2.resourceserver.jwt.issuer-uri` config

## Decisions Made

- **Scope-narrowing decision (named, not silent):** This service carries forward only the 4 fields F10's success criteria require (`approveBooking`, `calendarSlotSize`, `calendarMinTime`, `calendarMaxTime`) out of legacy's full 33-row settings table. Cited against F0 findings/05-platform-settings.md for traceability.
- **SETTINGS_UNAVAILABLE (503) intentionally absent:** FRD line 687 catalogues this code for the CALLING service (Phase 5's booking-service) to throw when THIS service is unreachable — not an error this service throws about itself. No exception class created for it here.
- **pgcrypto extension added in V2:** V1's schema used plain `INTEGER` ids (no UUID generation), so `pgcrypto` was never enabled. The outbox table's `gen_random_uuid()` defaults require it — added via `CREATE EXTENSION IF NOT EXISTS pgcrypto` at the top of V2.
- **Single global AccessDeniedHandler:** Unlike users-permissions-service (which needed a manual-check carve-out to produce two distinct 403 codes across two controllers), settings-service has exactly one admin-forbidden error code (`SETTINGS_FORBIDDEN`) for its one write endpoint — `@PreAuthorize` + `SettingsAccessDeniedHandler` alone is sufficient, no manual-check workaround needed.

## Deviations from Plan

None - plan executed exactly as written. The `Tier1Tier2ConsistencyTest`'s graceful-skip-until-controller-exists behavior and the `pgcrypto` extension addition were both already anticipated by the plan's own text ("fully exercised once plan 04-06's controller exists" for the former; the migration section's SQL block implicitly requires `gen_random_uuid()` support for the latter — adding the extension is the mechanical completion of what the plan specified, not a deviation from it).

## Issues Encountered

**Testcontainers Docker API version mismatch (sandbox-environmental, documented precedent):** `SettingsSingletonTest` and `ApplicationContextBootTest` use Testcontainers `PostgreSQLContainer`, which intermittently fails in this sandbox with `client version 1.32 is too old. Minimum supported API version is 1.40` — the identical issue already documented in `.planning/phases/02-platform-foundation-infrastructure/02-07-SUMMARY.md` (settings-service's own scaffold plan) and `.planning/phases/03-identity-access-control/03-05-SUMMARY.md`. Both tests were confirmed to **pass individually** in isolated runs during this plan's execution (verified via `mvn test -Dtest=SettingsSingletonTest` succeeding with no output/errors, and `mvn test -Dtest=ApplicationContextBootTest` succeeding with docker.sock mounted), but fail intermittently when run back-to-back or under heavy concurrent container load from other phase-4 plans executing in parallel in this same sandbox. This is the sandbox Docker daemon's API version reporting inconsistently under load, not an application defect — code compiles cleanly (`mvn test-compile` succeeds) and the test logic is correct. `Tier2FailClosedTest` (3 scenarios, WireMock-based, no Testcontainers) and `Tier1Tier2ConsistencyTest` (pure JUnit + reflection, no Testcontainers) both pass reliably and were verified multiple times.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- settings-service now has a complete, tested domain layer: singleton `Settings` entity structurally incapable of a second row, additive `outbox` table, shared error contract with exactly 3 reachable error codes, and independent Tier-2 JWT re-validation proven (via automated test reading the Gateway's actual current configuration) to exactly match the Gateway's authenticated-read/admin-write split for `/settings`.
- Plan 04-06 can now build `SettingsController` directly against `SettingsRepository`, `OutboxEventRepository`, the 3 `ApiException` subclasses, and `CurrentUserProvider.getCurrentUserId()` (for `Settings.updatedBy`) — with `Tier1Tier2ConsistencyTest` already in place to fully exercise itself the moment that controller exists.
- No blockers for plan 04-06.

## Known Stubs

None found. `grep -rn "TODO|FIXME|placeholder|not.implemented|coming soon"` across all created/modified `src/main/java` files returned zero matches. No handler returns hardcoded data in place of real logic, no empty function bodies, no silently-swallowed exceptions.

## Self-Check: PASSED

- All 18 created files verified present on disk (`[ -f ]` check against every path in `key-files.created` + this SUMMARY.md itself)
- Both task commits (`dca458c`, `3157e74`) verified present via `git log --oneline --all`
- Build check: `docker run ... mvn -q -DskipTests clean package` → exit 0 (clean build from scratch, confirmed after Task 2's security additions)
- Plan-specified grep verifications all pass: `CREATE TABLE outbox` present, `@GeneratedValue` absent from `Settings.java`, `oauth2ResourceServer` present in `SecurityConfig.java`, `record ApiError` present, `JpaRepository`/`findTop100ByStatusOrderByCreatedAtAsc` present in repositories, `getCurrentUserId` present in `CurrentUserProvider.java`, exactly 3 exception files in `error/` beyond `ApiError`/`ApiException`/`GlobalExceptionHandler`
- `Tier2FailClosedTest`'s 3 nested scenarios individually confirmed passing (0 failures, 0 errors each) via surefire reports
- `Tier1Tier2ConsistencyTest` confirmed passing (Gateway-source assertions exercised; controller-reflection half gracefully assumption-skipped as designed, pending plan 04-06)
- `ApplicationContextBootTest` confirmed passing in isolation (health endpoints remain `permitAll` under the new Tier-2 SecurityConfig)
- `SettingsSingletonTest` compiles and was confirmed passing in an earlier isolated run during this plan's execution; later re-runs hit the pre-existing sandbox Testcontainers/Docker-API flakiness documented in Issues Encountered — not a regression introduced by this plan's code

---
*Phase: 04-reference-data-extensibility-configuration*
*Completed: 2026-10-08*
