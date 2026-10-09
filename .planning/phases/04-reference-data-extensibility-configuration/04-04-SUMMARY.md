---
phase: 04-reference-data-extensibility-configuration
plan: 04
subsystem: api
tags: [spring-boot, spring-security, jpa, rabbitmq, outbox-pattern, custom-fields, keycloak]

# Dependency graph
requires:
  - phase: 04-reference-data-extensibility-configuration (plan 04-03)
    provides: CustomField/CustomFieldTemplate/CustomFieldJoin/OutboxEvent JPA entities and repositories, ApiError/ApiException error contract, Tier-2 JWT security skeleton (SecurityConfig, CurrentUserProvider, JwksOutageAuthenticationEntryPoint, CustomFieldAccessDeniedHandler), Tier1Tier2ConsistencyTest and Tier2FailClosedTest scaffolding
provides:
  - "CustomFieldController: GET/POST /custom-fields (with context_id applicability filter), GET/PUT/DELETE /custom-fields/{id}"
  - "FieldTemplateController: GET/POST /field-templates, GET/PUT/DELETE /field-templates/{id}"
  - "CustomFieldService.listApplicableToContext(contextId) — the concrete applicability-query mechanism for TechArch §2.4's booking-time custom-field discovery"
  - "FieldTemplateService's REPLACE-not-additive join management (PUT replaces the full field_ids[] set)"
  - "OutboxPublisher — scheduled polling relay of customfield.*/template.* events to the customfield.events exchange"
  - "A real JwtAuthenticationConverter bean mapping Keycloak's realm_access.roles to ROLE_-prefixed authorities (previously missing — every @PreAuthorize check would have silently denied all real callers)"
affects: [phase-5-core-booking]

# Tech tracking
tech-stack:
  added: [spring-boot-starter-validation, spring-boot-starter-amqp, testcontainers-rabbitmq]
  patterns:
    - "Bean Validation (@Pattern/@NotBlank) for structurally-uncatalogued 400s, reserving FRD-named error codes for exactly their described cases"
    - "Explicit JwtAuthenticationConverter mapping realm_access.roles -> ROLE_-prefixed GrantedAuthority (required for @PreAuthorize/hasRole to work against real Keycloak tokens; Spring's default only reads flat scope/scp claims)"

key-files:
  created:
    - services/custom-field-service/src/main/java/com/bookinghub/customfield/dto/CustomFieldDtos.java
    - services/custom-field-service/src/main/java/com/bookinghub/customfield/dto/FieldTemplateDtos.java
    - services/custom-field-service/src/main/java/com/bookinghub/customfield/service/CustomFieldService.java
    - services/custom-field-service/src/main/java/com/bookinghub/customfield/service/FieldTemplateService.java
    - services/custom-field-service/src/main/java/com/bookinghub/customfield/controller/CustomFieldController.java
    - services/custom-field-service/src/main/java/com/bookinghub/customfield/controller/FieldTemplateController.java
    - services/custom-field-service/src/main/java/com/bookinghub/customfield/outbox/OutboxPublisher.java
    - services/custom-field-service/src/test/java/com/bookinghub/customfield/controller/CustomFieldControllerIntegrationTest.java
    - services/custom-field-service/src/test/java/com/bookinghub/customfield/controller/FieldTemplateControllerIntegrationTest.java
    - services/custom-field-service/src/test/java/com/bookinghub/customfield/outbox/OutboxPublisherTest.java
  modified:
    - services/custom-field-service/pom.xml
    - services/custom-field-service/src/main/resources/application.yml
    - services/custom-field-service/src/main/java/com/bookinghub/customfield/error/GlobalExceptionHandler.java
    - services/custom-field-service/src/main/java/com/bookinghub/customfield/security/SecurityConfig.java
    - services/custom-field-service/src/test/java/com/bookinghub/customfield/security/Tier2FailClosedTest.java
    - docker-compose.yml
    - infra/rabbitmq/definitions.json

key-decisions:
  - "F0-confirmed 5-value field_type enum (textfield/select/textarea/radio/checkbox) enforced via DTO @Pattern, correcting TechArch's pre-F0 4-value placeholder guess"
  - "options[] required for all 3 choice-based types (select/radio/checkbox), not just select — deliberate improvement over legacy's confirmed zero-validation, applied consistently"
  - "Out-of-enum field_type and blank Field Template name map to 400 REQUEST_MALFORMED via Bean Validation, reserving CUSTOM_FIELD_LABEL_REQUIRED/CUSTOM_FIELD_OPTIONS_REQUIRED for exactly the FRD's two named cases"
  - "Field Template context_id references a SPECIFIC Location id (null=global) — more granular than legacy's all-locations-blanket scoping, a deliberate improvement, not a parity claim"
  - "Field Template update REPLACES the join set (delete-then-recreate), matching the field_ids[] array-shape contract and Phase 3's updateUserRealmRoles precedent"
  - "Field Template delete is a real hard delete (no deleted_at column) relying on the V1 schema's ON DELETE CASCADE, not manual join deletion"
  - "[Rule 1 - Bug] Added explicit JwtAuthenticationConverter mapping realm_access.roles to ROLE_-prefixed authorities — Spring Security's default JWT converter only reads flat scope/scp claims, so every @PreAuthorize('hasRole(...)') check in this service would have silently denied every real Keycloak-issued token regardless of actual roles. Same root cause independently found and fixed in parallel plan 04-06's settings-service."

patterns-established:
  - "Regression-guard test pattern: autowire the real JwtAuthenticationConverter bean and feed its extracted authorities into the jwt() test post-processor explicitly — jwt().authorities() alone bypasses conversion entirely and cannot prove a converter bean works against a realistic nested realm_access.roles claim"

# Metrics
duration: 95min
completed: 2026-10-09
---

# Phase 4 Plan 04: Custom Field & Field Template CRUD + Applicability Query + Outbox Publisher Summary

**Full F5 CRUD surface (CustomField/FieldTemplate controllers), the F0-confirmed 5-value field_type enum replacing TechArch's placeholder guess, a context-aware applicability-query endpoint for Phase 5's booking form, and a RabbitMQ outbox publisher — plus a critical JwtAuthenticationConverter bugfix that would otherwise have denied every real admin caller.**

## Performance

- **Duration:** 95 min
- **Started:** 2026-10-09T00:33:00Z
- **Completed:** 2026-10-09T02:13:14Z
- **Tasks:** 3 (plus 1 post-task Rule 1 bugfix)
- **Files modified:** 17 (10 created, 7 modified) across the 3 task commits, plus 2 files in the bugfix commit

## Accomplishments

- `CustomFieldController`'s 5 endpoints including the `GET /custom-fields?context_id=` applicability filter
- `FieldTemplateController`'s 5 endpoints with REPLACE-semantics join management
- F0-confirmed `field_type` enum (`textfield`/`select`/`textarea`/`radio`/`checkbox`) enforced via Bean Validation, correcting TechArch's pre-F0 4-value placeholder
- `options[]`-required-for-choice-types validation extended to all 3 choice-based types (select/radio/checkbox) — a deliberate improvement over legacy's confirmed zero-validation
- `OutboxPublisher` relaying `customfield.*`/`template.*` events to the already-declared `customfield.events` exchange
- `Tier1Tier2ConsistencyTest` now fully exercised (10 `@PreAuthorize` methods checked, all requiring `role_customfield_admin`, proven to exactly match the Gateway's Tier-1 route table)
- **Critical fix:** wired an explicit `JwtAuthenticationConverter` so `@PreAuthorize("hasRole(...)")` actually reads Keycloak's `realm_access.roles` claim — without it, every real admin caller would have been silently denied

## Task Commits

Each task was committed atomically:

1. **Task 1: CustomFieldController + CustomFieldService** - `084a831` (feat)
2. **Task 2: FieldTemplateController + FieldTemplateService** - `13e550b` (feat)
3. **Task 3: Outbox publisher + integration tests + Tier1/Tier2 consistency** - `450aa8d` (feat)
4. **Post-task fix: JwtAuthenticationConverter wiring** - `4185596` (fix)

**Plan metadata:** (this summary's commit)

## Files Created/Modified

- `dto/CustomFieldDtos.java` - Request/response records with `@Pattern`-enforced field_type enum
- `dto/FieldTemplateDtos.java` - Request/response records, `@NotBlank` name
- `service/CustomFieldService.java` - Label/options validation, soft-delete, applicability-query resolution
- `service/FieldTemplateService.java` - Join-set REPLACE semantics, hard delete
- `controller/CustomFieldController.java` - 5 endpoints, uniform `role_customfield_admin` gating
- `controller/FieldTemplateController.java` - 5 endpoints, uniform `role_customfield_admin` gating
- `outbox/OutboxPublisher.java` - Scheduled polling relay to RabbitMQ
- `error/GlobalExceptionHandler.java` - Added `MethodArgumentNotValidException` handler → 400 `REQUEST_MALFORMED`
- `security/SecurityConfig.java` - Added `JwtAuthenticationConverter` bean (critical fix)
- Three integration test files + updated `Tier2FailClosedTest.java`
- `infra/rabbitmq/definitions.json` - Added missing `guest` user (environment-wide pre-existing gap)
- `docker-compose.yml` - Added RabbitMQ dependency/env vars to `custom-field-service`

## Decisions Made

See frontmatter `key-decisions`. Most significant: the F0-confirmed enum correction, the options-validation scope extension to all 3 choice-based types, the context/template model's increased granularity over legacy, and the critical `JwtAuthenticationConverter` fix.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Tier2FailClosedTest's JPA/DataSource/Flyway exclusion broke context load once real controllers existed**
- **Found during:** Task 3 verification run
- **Issue:** Plan 04-03 wrote `Tier2FailClosedTest` before `CustomFieldController`/`FieldTemplateController` existed, excluding `DataSourceAutoConfiguration`/`HibernateJpaAutoConfiguration`/`FlywayAutoConfiguration` so the context could load without Postgres. Once this plan's real controllers (with real repository-backed services) existed, that exclusion caused `NoSuchBeanDefinitionException` for `CustomFieldRepository` — complete context-load failure.
- **Fix:** Replaced the exclusion properties with `@ActiveProfiles("test")`, matching every other integration test's docker-compose-Postgres pattern, on the top-level class and all 3 `@Nested` classes.
- **Files modified:** `src/test/java/com/bookinghub/customfield/security/Tier2FailClosedTest.java`
- **Verification:** All 3 scenarios pass (`mvn test -Dtest=Tier2FailClosedTest` → 3/3 passed)
- **Committed in:** `450aa8d` (Task 3 commit)

**2. [Rule 3 - Blocking] RabbitMQ's `definitions.json` had an empty `users[]` array**
- **Found during:** Task 3's `OutboxPublisherTest` first run
- **Issue:** An empty `users: []` in the mounted `definitions.json` suppresses RabbitMQ's default guest-user seeding entirely, leaving the broker with **no authenticatable user at all** — `guest:guest` (used by every other service's `OutboxPublisher` and the management UI) was rejected with `PLAIN login refused`. Confirmed pre-existing (predates this plan, `git log` shows the file last touched by phase-07 commit `6ff0897`) and environment-wide (every service's outbox publisher was silently failing to connect).
- **Fix:** Added a `guest`/`guest` administrator user with full `.*`/`.*`/`.*` permissions to `definitions.json`, restarted the RabbitMQ container to reload definitions, verified via `curl -u guest:guest .../api/whoami` → 200.
- **Files modified:** `infra/rabbitmq/definitions.json`
- **Verification:** `OutboxPublisherTest` now connects and both scenarios pass against the real broker.
- **Committed in:** `450aa8d` (Task 3 commit)

**3. [Rule 3 - Blocking] `docker-compose.yml`'s `custom-field-service` block had no RabbitMQ dependency or env vars**
- **Found during:** Task 3, adding `spring-boot-starter-amqp`
- **Issue:** The compose service definition for `custom-field-service` never declared a `rabbitmq` dependency or `RABBITMQ_*` env vars (Phase 2's scaffold predated this service needing messaging) — the running container would never actually connect to the broker.
- **Fix:** Added `depends_on: rabbitmq: condition: service_healthy` and the `RABBITMQ_HOST`/`PORT`/`USER`/`PASSWORD` env vars, matching `users-permissions-service`'s existing pattern.
- **Files modified:** `docker-compose.yml`
- **Verification:** Restarted `custom-field-service`, confirmed healthy startup; `OutboxPublisherTest` passes.
- **Committed in:** `450aa8d` (Task 3 commit)

**4. [Rule 1 - Bug] Missing `JwtAuthenticationConverter` meant every `@PreAuthorize` check would silently deny real callers**
- **Found during:** Post-Task-3 review of a parallel plan's commit (04-06's settings-service independently found and fixed the identical root cause in commit `e5e6e68`)
- **Issue:** Spring Security's OAuth2 resource-server default `jwt()` customizer only extracts authorities from a flat `scope`/`scp` claim — it has zero awareness of Keycloak's nested `realm_access.roles` claim. Every `@PreAuthorize("hasRole('role_customfield_admin')")` in `CustomFieldController`/`FieldTemplateController` would see **zero authorities** on any real Keycloak-issued token, denying genuine admins exactly as hard as non-admins. This bug was invisible to every integration test in this plan because they all use `jwt().authorities(() -> "ROLE_...")`, which sets Spring Security authorities directly and bypasses JWT-to-authority conversion entirely.
- **Fix:** Added an explicit `JwtAuthenticationConverter` bean (`realmRoleJwtAuthenticationConverter`) that reads `realm_access.roles` and maps each to a `ROLE_`-prefixed `SimpleGrantedAuthority`, wired via `.jwt(jwt -> jwt.jwtAuthenticationConverter(...))`. Added a new regression-guard test (`realKeycloakShapedToken_withRealmAccessRolesClaim_grantsAccess`) that autowires the real converter bean, runs a realistic nested `realm_access.roles` claim through it, and feeds the extracted authorities into the `jwt()` test post-processor — proving the fix works, not merely that it compiles.
- **Files modified:** `src/main/java/com/bookinghub/customfield/security/SecurityConfig.java`, `src/test/java/com/bookinghub/customfield/controller/CustomFieldControllerIntegrationTest.java`
- **Verification:** New regression scenario passes; all other 13 scenarios across the 5 required test classes still pass (14/14 total)
- **Committed in:** `4185596`

---

**Total deviations:** 4 auto-fixed (2 Rule 1 bugs, 2 Rule 3 blocking issues)
**Impact on plan:** All four were necessary for correctness — #1 and #4 are genuine production-correctness bugs (one pre-existing in test infrastructure, one a real authorization bypass that would have affected every deployed caller), #2 and #3 are infrastructure gaps blocking the outbox publisher's required real-broker verification. No scope creep; all fixes stayed within custom-field-service's and shared infra's existing boundaries.

## Known Stubs

None found. Stub scan (`TODO|FIXME|placeholder|not.?implemented|coming soon`) across all created/modified files returned only one incidental hit — a javadoc comment referencing TechArch's historical placeholder guess for context, not an actual code stub.

## Issues Encountered

- `hasRole()`'s implicit `ROLE_` prefix requirement tripped up initial integration-test authoring (`jwt().authorities(() -> "role_customfield_admin")` without the prefix returned 403 for every scenario) — resolved by using `ROLE_role_customfield_admin` in test authorities, consistent with the sibling `locations-resources-service` `ProbeTest`'s established pattern.
- JSONB payload round-trip reformats whitespace (Postgres adds a space after `:`), so `OutboxPublisherTest`'s exact-string payload assertion needed to become a parsed-JSON-equivalence comparison instead.
- The raw AMQP client deserializes string message headers as `LongStringHelper$ByteArrayLongString`, not `java.lang.String` — fixed via `.toString()` instead of a direct cast in the outbox test.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- F5 (Custom Fields) is now functionally complete: admin can define Custom Field definitions and Field Templates, associate templates with a specific booking context or globally, and Phase 5's booking-service has a concrete, working applicability-query endpoint (`GET /custom-fields?context_id={locationId}`) to call before rendering/validating a booking's `custom_field_values[]`.
- The `JwtAuthenticationConverter` fix is a load-bearing correctness fix for this service going into Phase 5 — without it, no real admin action against this service would have worked in a deployed environment, which would have surfaced as an extremely confusing "everyone is forbidden" bug during Phase 5 integration testing had it gone unnoticed.
- `infra/rabbitmq/definitions.json`'s missing guest-user fix benefits every other service in this phase (and already-built services in prior phases) whose outbox publishers depend on the same broker — this was silently broken platform-wide before this fix.
- No blockers for Phase 5.

---
*Phase: 04-reference-data-extensibility-configuration*
*Completed: 2026-10-09*

## Self-Check: PASSED

- All `key-files.created` verified present on disk via `[ -f ]` checks
- `git log --oneline --all --grep="04-04"` returns 4 commits (084a831, 13e550b, 450aa8d, 4185596) — all confirmed present
- Build check: `mvn -q -DskipTests package` → exit 0 (confirmed twice: after Task 3, and again after the post-task bugfix)
- Full required test suite (`CustomFieldControllerIntegrationTest`, `FieldTemplateControllerIntegrationTest`, `OutboxPublisherTest`, `Tier1Tier2ConsistencyTest`, `Tier2FailClosedTest`) → 14/14 passed, 0 failures, 0 errors
- `## Known Stubs` section present above; no blocking stubs found
