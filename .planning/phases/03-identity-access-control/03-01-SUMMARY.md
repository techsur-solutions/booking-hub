---
phase: 03-identity-access-control
plan: 01
subsystem: identity
tags: [jpa, flyway, postgres, permissions, outbox, password-reset, domain-model]

# Dependency graph
requires:
  - phase: 02-platform-foundation-infrastructure
    provides: V1 migration with users and permissions tables (empty schema)
provides:
  - V2 migration adding outbox and password_reset_tokens tables
  - Case-insensitive email uniqueness enforcement (uq_users_email_lower index)
  - Complete 17-row permission seed from F0 audit (15 active + 2 dead/reserved)
  - JPA entity/repository layer for User, Permission, OutboxEvent, PasswordResetToken
  - Shared ApiError/ApiException/GlobalExceptionHandler error contract for all controllers
affects: [03-identity-access-control, 04-user-role-management, 05-core-booking, 06-approval-workflow, 07-permission-system]

# Tech tracking
tech-stack:
  added: [Hibernate 6.5 @JdbcTypeCode(SqlTypes.JSON) for JSONB, Spring Data JPA repositories, Flyway V2 migration, Testcontainers-style tests with real Postgres]
  patterns: [Transactional outbox pattern for domain events, deny-by-default permission confirmation flag, case-insensitive email uniqueness via functional index, hashed password reset tokens with expiry/used_at validation]

key-files:
  created:
    - services/users-permissions-service/src/main/resources/db/migration/V2__add_outbox_password_reset_and_seed_permissions.sql
    - services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/domain/User.java
    - services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/domain/Permission.java
    - services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/domain/OutboxEvent.java
    - services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/domain/PasswordResetToken.java
    - services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/repository/UserRepository.java
    - services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/repository/PermissionRepository.java
    - services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/repository/OutboxEventRepository.java
    - services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/repository/PasswordResetTokenRepository.java
    - services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/error/ApiError.java
    - services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/error/ApiException.java
    - services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/error/GlobalExceptionHandler.java
    - services/users-permissions-service/src/test/java/com/bookinghub/userspermissions/repository/PermissionSeedDataTest.java
    - services/users-permissions-service/src/test/java/com/bookinghub/userspermissions/repository/OutboxAndResetTokenRepositoryTest.java
  modified:
    - services/users-permissions-service/pom.xml

key-decisions:
  - "Used functional index (lower(email)) for case-insensitive email uniqueness rather than a stored generated column, following PostgreSQL's idiomatic pattern and Phase 2's established approach"
  - "Stored exactly 17 permissions from F0 audit (not just FRD's 6), with accessresources/allowiCal/allowRSS marked confirmed=false to enforce explicit product owner approval before use"
  - "Used Hibernate 6's @JdbcTypeCode(SqlTypes.JSON) for JSONB columns (Permission.gatedActions, OutboxEvent.payload), avoiding custom converters while maintaining type safety"
  - "User.id has NO @GeneratedValue annotation (must be explicitly set from Keycloak sub claim), enforcing Phase 2's deliberate decision to prevent self-assigned/random IDs"
  - "Tests use the running Postgres from docker-compose instead of Testcontainers due to Docker API version compatibility issues in the sandbox environment (client 1.32 vs daemon minimum 1.40)"
  - "Installed Java 21 and Maven in the sandbox as blocking dependencies (Rule 3 deviation)"

patterns-established:
  - "All 8 ApiException subtypes declared as package-private classes in the same ApiException.java file (valid Java pattern), mapped polymorphically by a single @ExceptionHandler method"
  - "Repository tests validate both business logic (17-row seed, confirmed flags) and infrastructure (case-insensitive uniqueness, JSONB columns) using real Postgres with Flyway migrations"
  - "Password reset token hash stored as SHA-256 hex, never plaintext, closing F0 OQ #19's timing gap by validating expires_at/used_at at submission time"

# Metrics
duration: 8min
completed: 2026-10-08
---

# Phase 3 Plan 1: Domain Foundation Summary

**V2 migration adds outbox/password-reset tables, case-insensitive email uniqueness, and 17-row permission seed; JPA entities/repositories map to migrated schema; shared ApiError contract ready for all Phase 3 controllers**

## Performance

- **Duration:** 8 min
- **Started:** 2026-10-08T14:43:31Z
- **Completed:** 2026-10-08T14:52:19Z
- **Tasks:** 3
- **Files modified:** 15

## Accomplishments
- V2 Flyway migration creates outbox and password_reset_tokens tables, adds case-insensitive email uniqueness index, seeds 17 permissions
- JPA entities (User, Permission, OutboxEvent, PasswordResetToken) with Spring Data repositories expose F0-audit-derived business queries
- Shared ApiError/ApiException/GlobalExceptionHandler contract maps all 8 FRD error codes to consistent {error_code, message, timestamp, path} responses

## Task Commits

Each task was committed atomically:

1. **Task 1: V2 Flyway migration** - `b2df6bf` (feat)
2. **Task 2: JPA entities + Spring Data repositories** - `58cd884` (feat)
3. **Task 3: Shared ApiError/ApiException contract + repository tests** - `7fc9535` (feat)

**Plan metadata:** (committed with final summary)

## Files Created/Modified

### Migration
- `V2__add_outbox_password_reset_and_seed_permissions.sql` - Additive migration: outbox/password_reset_tokens tables, uq_users_email_lower index, 17-row permissions seed with confirmed flags

### Domain Entities
- `User.java` - id with NO @GeneratedValue (Keycloak sub claim), email/displayName/timestamps
- `Permission.java` - legacyFlag/keycloakRole/gatedActions (JSONB)/confirmed mapping
- `OutboxEvent.java` - Transactional outbox for domain events (aggregateType/aggregateId/exchange/routingKey/payload)
- `PasswordResetToken.java` - tokenHash (SHA-256)/expiresAt/usedAt for secure reset flow

### Repositories
- `UserRepository.java` - findByEmailIgnoreCase for case-insensitive lookups
- `PermissionRepository.java` - findByLegacyFlag, findByKeycloakRole
- `OutboxEventRepository.java` - findTop100ByStatusOrderByCreatedAtAsc for polling publisher
- `PasswordResetTokenRepository.java` - findByTokenHash for reset validation

### Error Handling
- `ApiError.java` - Shared {error_code, message, timestamp, path} record with @JsonProperty snake_case mapping
- `ApiException.java` - Abstract base + 8 package-private concrete subtypes (UserAlreadyExistsException, PasswordResetTokenInvalidException, PasswordChangeInvalidCurrentException, PasswordPolicyViolationException, ActionForbiddenException, PermissionsForbiddenException, PermissionFlagUndefinedException, AuthInvalidCredentialsException)
- `GlobalExceptionHandler.java` - @RestControllerAdvice with single @ExceptionHandler(ApiException.class) covering all subtypes polymorphically

### Tests
- `PermissionSeedDataTest.java` - Proves V2 migration seeds exactly 17 permissions, accessresources/allowiCal/allowRSS marked unconfirmed
- `OutboxAndResetTokenRepositoryTest.java` - Proves outbox/reset-token tables work, case-insensitive email uniqueness enforced (uq_users_email_lower)
- `application-test.properties` - Test profile using running Postgres from docker-compose
- `pom.xml` - Updated Testcontainers (1.20.4) and docker-java (3.4.1) versions

## Decisions Made

**Case-insensitive email uniqueness via functional index:** Used `CREATE UNIQUE INDEX uq_users_email_lower ON users (lower(email))` following PostgreSQL's idiomatic pattern. This is additive to V1's plain UNIQUE constraint (which remains, harmlessly stricter) and backs UserRepository.findByEmailIgnoreCase.

**17-row permission seed (not just FRD's 6):** Seeded all permissions from F0 audit's actual inventory (15 actively enforced + 2 dead/reserved). Three rows deliberately marked `confirmed = false` (accessresources, allowiCal, allowRSS) to enforce deny-by-default until a product owner explicitly approves the mapping — never silently approximated.

**User.id with NO @GeneratedValue:** Enforces Phase 2's decision that id MUST be explicitly set from the Keycloak sub claim at first-sync time, preventing any code path from inserting a User row with a self-assigned or random id. The V1 DDL's deliberate omission of a DEFAULT matches this constraint.

**Hibernate 6 @JdbcTypeCode(SqlTypes.JSON) for JSONB:** Used Hibernate 6.5's native JSONB support for Permission.gatedActions and OutboxEvent.payload columns, avoiding custom JPA converters while maintaining type safety and PostgreSQL-native storage.

**Tests use running Postgres, not Testcontainers:** Testcontainers failed with "client version 1.32 is too old. Minimum supported API version is 1.40" due to Docker daemon/client mismatch in the sandbox. Tests instead use the Postgres from docker-compose (userperm_test database), which is the actual deployment target and provides identical validation.

**All ApiException subtypes in one file:** Declared all 8 FRD-specified exception classes as package-private in ApiException.java (valid Java — only one top-level class need be public). GlobalExceptionHandler maps them polymorphically with a single `@ExceptionHandler(ApiException.class)` method, eliminating duplication.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Installed Java 21 and Maven**
- **Found during:** Task 1 (V2 migration verification)
- **Issue:** mvn command not found; java --version returned nothing. Project requires Java 21 (pom.xml), but sandbox had neither Java nor Maven installed.
- **Fix:** `apt-get install openjdk-21-jdk maven`, then `update-alternatives --set java` to use Java 21. Verified with `mvn --version` and re-ran build.
- **Files modified:** none (system dependencies)
- **Verification:** `mvn -q -DskipTests package` succeeded, producing target/users-permissions-service-0.0.1-SNAPSHOT.jar
- **Committed in:** b2df6bf (Task 1 commit — build verified before committing migration)

**2. [Rule 3 - Blocking] Upgraded docker-java library for Test containers API compatibility**
- **Found during:** Task 3 (repository tests)
- **Issue:** Testcontainers failed with "client version 1.32 is too old. Minimum supported API version is 1.40". Docker daemon supports API 1.56 (minimum 1.40), but Testcontainers' bundled docker-java client used obsolete API 1.32.
- **Fix:** Added explicit `<testcontainers.version>1.20.4</testcontainers.version>` and `<docker-java.version>3.4.1</docker-java.version>` properties to pom.xml, upgraded docker-java-core/api/transport-httpclient5 to 3.4.1.
- **Files modified:** pom.xml
- **Verification:** Still failed (docker-java 3.4.1 still reports API 1.32 internally). Switched to using running Postgres from docker-compose instead, which is the actual deployment target and provides identical validation.
- **Committed in:** 7fc9535 (Task 3 commit)

**3. [Rule 1 - Bug] Fixed test database password mismatch**
- **Found during:** Task 3 (repository tests)
- **Issue:** Tests failed with "password authentication failed for user postgres". application-test.properties used `devpass`, but docker-compose.yml defines `POSTGRES_PASSWORD: postgres_root_pw`.
- **Fix:** Updated application-test.properties with correct password `postgres_root_pw`.
- **Files modified:** services/users-permissions-service/src/test/resources/application-test.properties
- **Verification:** Tests connected successfully, Flyway migrations applied, all 8 repository tests passed (2 test classes × 4 assertions each).
- **Committed in:** 7fc9535 (Task 3 commit)

---

**Total deviations:** 3 auto-fixed (2 blocking, 1 bug)
**Impact on plan:** All auto-fixes necessary for task completion. Java/Maven installation is a sandbox environment gap (Rule 3). Testcontainers workaround uses the actual deployment database (docker-compose Postgres), which is semantically equivalent and arguably more realistic than an ephemeral container. No functional deviation from plan's intent.

## Issues Encountered

**Testcontainers Docker API version incompatibility:** The Testcontainers library (even at 1.20.4 with docker-java 3.4.1) failed to connect to the sandbox's Docker daemon (API 1.56, minimum 1.40) due to client reporting obsolete API version 1.32. Root cause appears to be Testcontainers' internal version negotiation logic, not the docker-java library version itself. Resolution: use the running Postgres from docker-compose (which is the actual deployment target) instead of spinning up an ephemeral Testcontainers instance. This provides identical validation (real Postgres 16, real Flyway migrations, real JPA mappings) and is arguably more realistic than an isolated test container.

## User Setup Required

None - no external service configuration required.

## Known Stubs

None found. All domain entities, repositories, migrations, and error contracts are fully implemented.

## Next Phase Readiness

Domain foundation complete. Phase 3 plans 02-05 can now build:
- Keycloak integration (03-02): has User entity with id field ready for sub claim mapping
- Auth flows (03-03): has PasswordResetToken entity/repository and ApiError contract
- User management (03-04): has User/OutboxEvent entities and ApiException subtypes (UserAlreadyExistsException, etc.)
- Permission service (03-05): has Permission entity with confirmed flag and OutboxEvent for publishing

Ready for plan 03-02.

## Self-Check: PASSED

- ✓ V2 migration file exists: `V2__add_outbox_password_reset_and_seed_permissions.sql` (67 lines)
- ✓ Contains 17 permission VALUES rows (verified by grep -c)
- ✓ Maven build succeeds: target/users-permissions-service-0.0.1-SNAPSHOT.jar created
- ✓ All 8 repository tests pass (PermissionSeedDataTest: 4 tests, OutboxAndResetTokenRepositoryTest: 4 tests)
- ✓ Commits exist: b2df6bf (V2 migration), 58cd884 (entities/repositories), 7fc9535 (error contract/tests)
- ✓ No blocking stubs found

---
*Phase: 03-identity-access-control*
*Completed: 2026-10-08*
