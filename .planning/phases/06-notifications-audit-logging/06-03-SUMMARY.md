---
phase: 06-notifications-audit-logging
plan: 03
subsystem: audit
tags: [spring-security, oauth2, jwt, rabbitmq, flyway, jpa, testcontainers, wiremock, spring-retry]

# Dependency graph
requires:
  - phase: 02-platform-foundation-infrastructure
    provides: audit-log-service scaffold, audit_db schema, RabbitMQ topology (audit.*.q queues)
  - phase: 03-identity-access-control
    provides: Keycloak realm with role_audit_viewer, Tier-2 JWT validation pattern
provides:
  - Tier-2 JWT security (SecurityConfig + JwksOutageAuthenticationEntryPoint + ApiAccessDeniedHandler)
  - CurrentUserProvider (hasRole/getCurrentUserId/getCurrentRoles — identical shape to Phase 3)
  - V2 Flyway migration adding idempotency_key UUID + partial unique index to audit_log_entries
  - AuditLogEntry JPA entity + AuditLogEntryRepository (JpaRepository + JpaSpecificationExecutor + findByIdempotencyKey)
  - ApiError record + ApiException base + AuditLogForbiddenException (403) + AuditLogInvalidDateRangeException (400)
  - GlobalExceptionHandler (polymorphic ApiException handler + REQUEST_MALFORMED 400)
  - rabbitListenerContainerFactory bean (4-attempt retry, 5s/30s/2min backoff, RejectAndDontRequeueRecoverer)
  - MissingIdempotencyKeyException (AmqpRejectAndDontRequeueException, non-retryable)
  - Tier2FailClosedTest: 503/401/403 all proven under real conditions
  - AuditLogEntrySchemaTest: V2 partial-unique-index semantics proven (round-trip, duplicate fails, dual-null succeeds)
affects:
  - 06-04 (audit-log-service consumer + controller — builds on this infrastructure directly)

# Tech tracking
tech-stack:
  added:
    - spring-boot-starter-security
    - spring-boot-starter-oauth2-resource-server
    - spring-retry + spring-aspects
    - wiremock-standalone:3.9.2 (test)
    - spring-security-test (test)
  patterns:
    - Tier-2 JWT fail-closed (503/401/403 via JwksOutageAuthenticationEntryPoint + ApiAccessDeniedHandler)
    - hasAuthority("role_audit_viewer") rather than hasRole (avoids ROLE_-prefix double-counting)
    - JwtAuthenticationConverter mapping realm_access.roles to plain GrantedAuthority (same bugfix as Phase 4)
    - Additive Flyway migration (V2 = ADD COLUMN only, no touch to V1's REVOKE UPDATE/DELETE)
    - Partial unique index WHERE idempotency_key IS NOT NULL (allows multiple nulls)
    - @DataJpaTest + @ActiveProfiles("test") + running docker-compose Postgres (Testcontainers Docker API workaround)
    - SimpleRabbitListenerContainerFactory with RetryInterceptorBuilder + RejectAndDontRequeueRecoverer

key-files:
  created:
    - services/audit-log-service/pom.xml (modified — security/retry deps added)
    - services/audit-log-service/src/main/resources/application.yml (modified — OIDC issuer + listener enabled)
    - k8s/audit-log-service/configmap.yaml (modified — KEYCLOAK_* env vars added)
    - services/audit-log-service/src/main/resources/db/migration/V2__add_idempotency_key.sql
    - services/audit-log-service/src/main/java/com/bookinghub/auditlog/error/ApiError.java
    - services/audit-log-service/src/main/java/com/bookinghub/auditlog/error/ApiException.java
    - services/audit-log-service/src/main/java/com/bookinghub/auditlog/error/GlobalExceptionHandler.java
    - services/audit-log-service/src/main/java/com/bookinghub/auditlog/security/SecurityConfig.java
    - services/audit-log-service/src/main/java/com/bookinghub/auditlog/security/JwksOutageAuthenticationEntryPoint.java
    - services/audit-log-service/src/main/java/com/bookinghub/auditlog/security/ApiAccessDeniedHandler.java
    - services/audit-log-service/src/main/java/com/bookinghub/auditlog/security/CurrentUserProvider.java
    - services/audit-log-service/src/main/java/com/bookinghub/auditlog/domain/AuditLogEntry.java
    - services/audit-log-service/src/main/java/com/bookinghub/auditlog/repository/AuditLogEntryRepository.java
    - services/audit-log-service/src/main/java/com/bookinghub/auditlog/rabbit/RabbitConfig.java
    - services/audit-log-service/src/main/java/com/bookinghub/auditlog/rabbit/RabbitRetryBackOffPolicy.java
    - services/audit-log-service/src/main/java/com/bookinghub/auditlog/rabbit/MissingIdempotencyKeyException.java
    - services/audit-log-service/src/test/java/com/bookinghub/auditlog/security/Tier2FailClosedTest.java
    - services/audit-log-service/src/test/java/com/bookinghub/auditlog/repository/AuditLogEntrySchemaTest.java
    - services/audit-log-service/src/test/resources/application-test.properties
  modified: []

key-decisions:
  - "hasAuthority('role_audit_viewer') used (not hasRole) — SecurityConfig grants authorities WITHOUT ROLE_ prefix, so hasAuthority matches the Keycloak realm role name verbatim; avoids the double-prefix confusion plan 06-01 noted"
  - "V2 migration is ADDITIVE only — ALTER TABLE ADD COLUMN NULL avoids blocking NOT NULL constraint on existing rows; partial unique index WHERE idempotency_key IS NOT NULL is the correct shape for an append-only table being retroactively given idempotency"
  - "No custom recoverer in RabbitConfig — audit_log_entries is pure append-only fact table (no pending/status concept); the DLQ message IS the record of the gap; F11 §Error States says 'surfaced via observability/alerting, not client-facing error'"
  - "Switched AuditLogEntrySchemaTest from Testcontainers to running docker-compose Postgres — Docker client version 1.32 below Testcontainers 1.20.4 minimum (1.40); same pre-existing incompatibility workaround as Phase 4 plan 04-01 SchemaCompletionTest"

patterns-established:
  - "Tier-2 JWT security pattern for audit services: hasAuthority (not hasRole) matches Keycloak role names verbatim"
  - "Additive V2 migrations: ADD COLUMN NULL + partial index — never blocker constraints on additive changes"
  - "Append-only audit service RabbitMQ config: RejectAndDontRequeueRecoverer only (no custom status-tracking recoverer)"

# Metrics
duration: 10min
completed: 2026-10-10
---

# Phase 06 Plan 03: audit-log-service Tier-2 Security, Idempotency Schema, and RabbitMQ Infrastructure

**Tier-2 JWT resource server (role_audit_viewer-gated, fail-closed), additive V2 idempotency_key migration closing the TechArch DDL gap, FRD-named error contract, and 4-attempt retry/DLQ RabbitMQ factory — all proven by Tier2FailClosedTest and AuditLogEntrySchemaTest against real conditions**

## Performance

- **Duration:** 10 min
- **Started:** 2026-10-10T03:13:43Z
- **Completed:** 2026-10-10T03:23:53Z
- **Tasks:** 3
- **Files modified:** 19

## Accomplishments

- Tier-2 JWT security with role_audit_viewer gating on all routes except /actuator/health/** — proven by Tier2FailClosedTest (401/403/503 all fire as ApiError JSON, not Spring whitelabel error)
- V2 Flyway migration closes the TechArch DDL gap: audit_log_entries gains idempotency_key UUID with a partial unique index (WHERE NOT NULL), enabling idempotent consumption for F11 without touching V1's REVOKE UPDATE/DELETE immutability grant
- AuditLogEntry JPA entity + AuditLogEntryRepository with JpaSpecificationExecutor and findByIdempotencyKey() — ready for plan 06-04's 7-queue consumer and filterable GET /audit-log endpoint
- rabbitListenerContainerFactory bean: 4-attempt retry (5s/30s/2min backoff), MissingIdempotencyKeyException non-retryable, RejectAndDontRequeueRecoverer final step — identical to notifications-service plan 06-01

## Task Commits

Each task was committed atomically:

1. **Task 1: Dependencies, config, and Tier-2 JWT security** - `a0e3cc1` (feat)
2. **Task 2: V2 idempotency migration + entity/repository + shared error contract** - `5622f66` (feat)
3. **Task 3: RabbitMQ retry/DLQ container factory + Tier-2 fail-closed proof** - `5f5cc41` (feat)

**Plan metadata:** (docs commit — see final)

## Files Created/Modified

- `services/audit-log-service/pom.xml` — Added spring-security, oauth2-resource-server, spring-retry, wiremock, spring-security-test
- `services/audit-log-service/src/main/resources/application.yml` — OIDC issuer-uri config, auto-startup: true
- `k8s/audit-log-service/configmap.yaml` — KEYCLOAK_HOST/PORT/REALM env vars
- `services/audit-log-service/src/main/resources/db/migration/V2__add_idempotency_key.sql` — Additive migration (ADD COLUMN + partial unique index)
- `services/audit-log-service/src/main/java/com/bookinghub/auditlog/error/ApiError.java` — snake_case JSON error record
- `services/audit-log-service/src/main/java/com/bookinghub/auditlog/error/ApiException.java` — Base + AuditLogForbiddenException + AuditLogInvalidDateRangeException
- `services/audit-log-service/src/main/java/com/bookinghub/auditlog/error/GlobalExceptionHandler.java` — Polymorphic handler
- `services/audit-log-service/src/main/java/com/bookinghub/auditlog/security/SecurityConfig.java` — Tier-2 SecurityFilterChain
- `services/audit-log-service/src/main/java/com/bookinghub/auditlog/security/JwksOutageAuthenticationEntryPoint.java` — 503/401 fail-closed
- `services/audit-log-service/src/main/java/com/bookinghub/auditlog/security/ApiAccessDeniedHandler.java` — 403 AUDIT_LOG_FORBIDDEN
- `services/audit-log-service/src/main/java/com/bookinghub/auditlog/security/CurrentUserProvider.java` — getCurrentUserId/getCurrentRoles/hasRole
- `services/audit-log-service/src/main/java/com/bookinghub/auditlog/domain/AuditLogEntry.java` — JPA entity (V1+V2 columns)
- `services/audit-log-service/src/main/java/com/bookinghub/auditlog/repository/AuditLogEntryRepository.java` — JpaRepository + JpaSpecificationExecutor + findByIdempotencyKey
- `services/audit-log-service/src/main/java/com/bookinghub/auditlog/rabbit/RabbitConfig.java` — rabbitListenerContainerFactory bean
- `services/audit-log-service/src/main/java/com/bookinghub/auditlog/rabbit/RabbitRetryBackOffPolicy.java` — 5s/30s/2min backoff
- `services/audit-log-service/src/main/java/com/bookinghub/auditlog/rabbit/MissingIdempotencyKeyException.java` — Non-retryable DLQ route
- `services/audit-log-service/src/test/java/com/bookinghub/auditlog/security/Tier2FailClosedTest.java` — 3 scenarios proven
- `services/audit-log-service/src/test/java/com/bookinghub/auditlog/repository/AuditLogEntrySchemaTest.java` — 3 partial-index cases proven
- `services/audit-log-service/src/test/resources/application-test.properties` — Test profile config

## Decisions Made

1. **hasAuthority vs hasRole**: Used `hasAuthority("role_audit_viewer")` (not `hasRole("audit_viewer")`) — SecurityConfig's JwtAuthenticationConverter grants realm roles WITHOUT the ROLE_ prefix, so the authority string matches the Keycloak role name verbatim. Plan 06-01's notes on the double-prefix issue informed this choice.

2. **V2 migration shape**: `ALTER TABLE ADD COLUMN idempotency_key UUID NULL` + `CREATE UNIQUE INDEX ... WHERE idempotency_key IS NOT NULL`. No NOT NULL constraint (additive-safe on existing tables), partial index (multiple nulls allowed, consistent with the append-only history of the table before this column existed).

3. **No custom recoverer**: `audit_log_entries` is pure append-only — there is no "pending" or "status" column to update on dead-letter. `RejectAndDontRequeueRecoverer` alone is correct. (Notifications-service needed a custom recoverer for its `notification_deliveries.status` field; audit-log-service has no equivalent.)

4. **Testcontainers workaround**: `AuditLogEntrySchemaTest` uses `@ActiveProfiles("test")` + running docker-compose Postgres instead of `@Testcontainers` — Docker client version 1.32 is below Testcontainers 1.20.4's minimum (1.40). Same pre-existing incompatibility, same workaround as Phase 4 plan 04-01 (locations-resources-service precedent).

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Switched AuditLogEntrySchemaTest from Testcontainers to running Postgres**
- **Found during:** Task 2 (AuditLogEntrySchemaTest execution)
- **Issue:** Testcontainers 1.20.4 requires Docker client API version ≥1.40; this sandbox's Docker daemon reports version 1.32 (`client version 1.32 is too old. Minimum supported API version is 1.40`). PostgreSQLContainer cannot start.
- **Fix:** Switched to `@DataJpaTest @ActiveProfiles("test") @AutoConfigureTestDatabase(Replace.NONE)` with `application-test.properties` pointing at the running docker-compose Postgres (audit_db_test database created via `docker exec project-postgres-1 psql ... CREATE DATABASE`). Identical workaround to Phase 4 plan 04-01's SchemaCompletionTest for locations-resources-service.
- **Files modified:** `AuditLogEntrySchemaTest.java`, added `src/test/resources/application-test.properties`
- **Verification:** All 3 test cases pass: round-trip, duplicate-key DataIntegrityViolationException, dual-null success.
- **Committed in:** 5622f66 (Task 2 commit)

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** Fix was necessary for test execution in this sandbox. The test proves identical semantics to what Testcontainers would have proven. No scope creep.

## Issues Encountered

None — plan executed smoothly once the Testcontainers incompatibility was detected and worked around using the established sandbox pattern.

## Known Stubs

None found — all implementations are complete. `AuditLogForbiddenException` and `AuditLogInvalidDateRangeException` are defined and compile-ready for plan 06-04's controller. `RabbitConfig`'s factory bean is wired with real retry/DLQ behavior matching plan 06-01's precedent exactly.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- audit-log-service has a complete, proven Tier-2 security layer, idempotency-capable schema, and RabbitMQ retry/DLQ infrastructure
- Plan 06-04 can immediately add the 7 `@RabbitListener` methods (they will auto-pick-up the `rabbitListenerContainerFactory` bean by default name) and the `GET /audit-log` controller (using `AuditLogEntryRepository.findAll(Specification, Pageable)` and `hasRole("role_audit_viewer")`)
- All integration contracts in this plan's frontmatter are satisfied and verified

---
*Phase: 06-notifications-audit-logging*
*Completed: 2026-10-10*

## Self-Check: PASSED

- [x] All 19 files verified on disk
- [x] All 3 task commits exist: a0e3cc1, 5622f66, 5f5cc41
- [x] Final build (mvn -q -DskipTests package): MAVEN BUILD OK
- [x] AuditLogEntrySchemaTest: 3/3 tests PASSED
- [x] Tier2FailClosedTest: 3/3 scenarios PASSED
- [x] No blocking stubs found
