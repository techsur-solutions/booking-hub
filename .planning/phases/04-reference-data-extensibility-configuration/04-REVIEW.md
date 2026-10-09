---
phase: 4
status: issues_found
blockers: 0
warnings: 1
files_reviewed: 87
files_reviewed_list:
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/service/LocationService.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/service/ResourceService.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/controller/LocationController.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/controller/ResourceController.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/domain/Location.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/domain/Resource.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/domain/OutboxEvent.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/dto/LocationDtos.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/dto/ResourceDtos.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/security/SecurityConfig.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/security/CurrentUserProvider.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/security/JwksOutageAuthenticationEntryPoint.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/security/LocationResourceAccessDeniedHandler.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/GlobalExceptionHandler.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/ApiException.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/ApiError.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/LocationNotFoundException.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/LocationNameRequiredException.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/LocationResourceForbiddenException.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/ResourceNotFoundException.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/ResourceNameRequiredException.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/repository/LocationRepository.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/repository/ResourceRepository.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/repository/OutboxEventRepository.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/outbox/OutboxPublisher.java
  - services/locations-resources-service/src/main/resources/application.yml
  - services/locations-resources-service/src/main/resources/db/migration/V1__init_schema.sql
  - services/locations-resources-service/src/main/resources/db/migration/V2__add_location_colour_description_and_resource_fields.sql
  - services/locations-resources-service/src/test/java/com/bookinghub/locationsresources/controller/LocationControllerIntegrationTest.java
  - services/locations-resources-service/src/test/java/com/bookinghub/locationsresources/controller/ResourceControllerIntegrationTest.java
  - services/locations-resources-service/src/test/java/com/bookinghub/locationsresources/outbox/OutboxPublisherTest.java
  - services/locations-resources-service/src/test/java/com/bookinghub/locationsresources/security/Tier1Tier2ConsistencyTest.java
  - services/locations-resources-service/src/test/java/com/bookinghub/locationsresources/security/Tier2FailClosedTest.java
  - services/locations-resources-service/src/test/java/com/bookinghub/locationsresources/repository/SchemaCompletionTest.java
  - services/locations-resources-service/src/test/resources/application-test.properties
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/service/CustomFieldService.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/service/FieldTemplateService.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/controller/CustomFieldController.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/controller/FieldTemplateController.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/domain/CustomField.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/domain/CustomFieldTemplate.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/domain/CustomFieldJoin.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/domain/OutboxEvent.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/dto/CustomFieldDtos.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/dto/FieldTemplateDtos.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/security/SecurityConfig.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/security/CurrentUserProvider.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/security/JwksOutageAuthenticationEntryPoint.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/security/CustomFieldAccessDeniedHandler.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/error/GlobalExceptionHandler.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/error/ApiException.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/error/ApiError.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/error/CustomFieldNotFoundException.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/error/CustomFieldForbiddenException.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/error/CustomFieldLabelRequiredException.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/error/CustomFieldOptionsRequiredException.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/repository/CustomFieldRepository.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/repository/CustomFieldTemplateRepository.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/repository/CustomFieldJoinRepository.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/repository/OutboxEventRepository.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/outbox/OutboxPublisher.java
  - services/custom-field-service/src/main/resources/application.yml
  - services/custom-field-service/src/main/resources/db/migration/V1__init_schema.sql
  - services/custom-field-service/src/main/resources/db/migration/V2__add_outbox_and_required_flag.sql
  - services/custom-field-service/src/test/java/com/bookinghub/customfield/controller/CustomFieldControllerIntegrationTest.java
  - services/custom-field-service/src/test/java/com/bookinghub/customfield/controller/FieldTemplateControllerIntegrationTest.java
  - services/custom-field-service/src/test/java/com/bookinghub/customfield/outbox/OutboxPublisherTest.java
  - services/custom-field-service/src/test/java/com/bookinghub/customfield/security/Tier1Tier2ConsistencyTest.java
  - services/custom-field-service/src/test/java/com/bookinghub/customfield/security/Tier2FailClosedTest.java
  - services/custom-field-service/src/test/java/com/bookinghub/customfield/repository/DomainRepositoryTest.java
  - services/custom-field-service/src/test/resources/application-test.properties
  - services/settings-service/src/main/java/com/bookinghub/settings/service/SettingsService.java
  - services/settings-service/src/main/java/com/bookinghub/settings/controller/SettingsController.java
  - services/settings-service/src/main/java/com/bookinghub/settings/domain/Settings.java
  - services/settings-service/src/main/java/com/bookinghub/settings/domain/OutboxEvent.java
  - services/settings-service/src/main/java/com/bookinghub/settings/dto/SettingsDtos.java
  - services/settings-service/src/main/java/com/bookinghub/settings/security/SecurityConfig.java
  - services/settings-service/src/main/java/com/bookinghub/settings/security/CurrentUserProvider.java
  - services/settings-service/src/main/java/com/bookinghub/settings/security/JwksOutageAuthenticationEntryPoint.java
  - services/settings-service/src/main/java/com/bookinghub/settings/security/SettingsAccessDeniedHandler.java
  - services/settings-service/src/main/java/com/bookinghub/settings/error/GlobalExceptionHandler.java
  - services/settings-service/src/main/java/com/bookinghub/settings/error/ApiException.java
  - services/settings-service/src/main/java/com/bookinghub/settings/error/SettingsForbiddenException.java
  - services/settings-service/src/main/java/com/bookinghub/settings/error/SettingsInvalidCalendarRangeException.java
  - services/settings-service/src/main/java/com/bookinghub/settings/error/SettingsInvalidSlotSizeException.java
  - services/settings-service/src/main/java/com/bookinghub/settings/repository/SettingsRepository.java
  - services/settings-service/src/main/java/com/bookinghub/settings/repository/OutboxEventRepository.java
  - services/settings-service/src/main/java/com/bookinghub/settings/outbox/OutboxPublisher.java
  - services/settings-service/src/main/resources/application.yml
  - services/settings-service/src/main/resources/db/migration/V1__init_schema.sql
  - services/settings-service/src/main/resources/db/migration/V2__add_outbox.sql
  - services/settings-service/src/test/java/com/bookinghub/settings/controller/SettingsControllerIntegrationTest.java
  - services/settings-service/src/test/java/com/bookinghub/settings/outbox/OutboxPublisherTest.java
  - services/settings-service/src/test/java/com/bookinghub/settings/repository/SettingsSingletonTest.java
  - services/settings-service/src/test/java/com/bookinghub/settings/security/Tier1Tier2ConsistencyTest.java
  - services/settings-service/src/test/java/com/bookinghub/settings/security/Tier2FailClosedTest.java
  - services/settings-service/src/test/resources/application-test.properties
  - docker-compose.yml
reviewed_at: 2026-10-09T16:00:00Z
iteration: 2
---

# Phase 4 Code Review

## BLOCKERs

_None. B1 from iteration 1 is resolved._

---

## WARNINGs

### W1 (iteration 1) — RESOLVED ✓
**`settings-service/application.yml` stale Phase 3 comment.**
- **Fix (427d94e):** The three-line stale "Disabled until Phase 3" comment was replaced with an accurate comment: `settings-service is a pure outbox publisher — it has no @RabbitListener beans and never will. Keep auto-startup: false permanently to suppress the idle SimpleMessageListenerContainer Spring Boot would otherwise start.`
- **Verification:** Read `services/settings-service/src/main/resources/application.yml` lines 28–33 — comment is accurate and no regression introduced.
- **Status:** RESOLVED.

---

### W2 (iteration 1) — CARRY-FORWARD (not fixed; was not in scope for iteration 2)
**`SettingsService.update()` / `Settings.java` setters each redundantly update `updatedAt`, which is then overwritten by an explicit `settings.setUpdatedAt(Instant.now())` call at line 151.**
- **File:** `services/settings-service/src/main/java/com/bookinghub/settings/service/SettingsService.java`:110–161
- **Evidence:** Individual domain setters (`setApproveBooking`, `setCalendarMinTime`, etc.) each call `this.updatedAt = Instant.now()` internally (Settings.java), and then the service explicitly calls `settings.setUpdatedAt(Instant.now())` after all field updates, overwriting the prior setter-side values. The final explicit `setUpdatedAt` always wins and is the value captured in the outbox payload and persisted — so there is no observable correctness defect in the current code. The concern is latent fragility: if the explicit `setUpdatedAt` call were accidentally removed or reordered, the outbox payload and persisted value could reflect a slightly earlier timestamp than intended. The fix from iteration 1 was not attempted (correctly — it is a WARNING, not a BLOCKER).
- **Fix direction:** Remove `this.updatedAt = Instant.now()` from the individual domain setters in `Settings.java` and rely solely on the explicit `setUpdatedAt` call in the service layer, matching the pattern used by `Location` and `Resource` entities.

---

### W3 (iteration 1) — RESOLVED ✓
**`docker-compose.yml` missing `KEYCLOAK_HOST`/`KEYCLOAK_PORT`/`KEYCLOAK_REALM` for `locations-resources-service`, `custom-field-service`, and `settings-service`.**
- **Fix (e88609b):** Added `KEYCLOAK_HOST: keycloak`, `KEYCLOAK_PORT: 8080`, `KEYCLOAK_REALM: bookinghub` to the `environment` block of all three services. Also added `keycloak: condition: service_healthy` to each service's `depends_on` block.
- **Verification:**
  - `docker-compose.yml` lines 98–112: `locations-resources-service` now has all three vars and the keycloak health dependency. ✓
  - `docker-compose.yml` lines 126–140: `custom-field-service` likewise. ✓
  - `docker-compose.yml` lines 223–237: `settings-service` likewise. ✓
  - `keycloak` service (lines 48–64) has a `healthcheck` defined (TCP probe against port 8080 for `openid-configuration`), so `service_healthy` is a valid dependency condition. ✓
  - No regression: `RABBITMQ_USER`/`RABBITMQ_USERNAME` naming convention for each service is unchanged and still consistent with each service's `application.yml`. ✓
- **Status:** RESOLVED.

---

## B1 (iteration 1) — RESOLVED ✓
**All three OutboxPublishers: `outboxEventRepository.save(event)` placed after the try/catch, skipped on non-AmqpException.**

- **Fix (c826c0d):** `outboxEventRepository.save(event)` moved inside both branches of the try/catch in all three services:
  - `locations-resources-service/outbox/OutboxPublisher.java`: `save()` at line 82 (success path, inside try) and line 88 (AmqpException path, inside catch). ✓
  - `custom-field-service/outbox/OutboxPublisher.java`: `save()` at line 71 (success path) and line 76 (catch). ✓
  - `settings-service/outbox/OutboxPublisher.java`: `save()` at line 76 (success path) and line 82 (catch). ✓
- **Regression check:** Both success and failure paths each call `save()` exactly once. A non-AmqpException from `rabbitTemplate.send()` now propagates out of the loop without calling `save()`, leaving the DB row untouched at `pending` — correct outbox-pattern behaviour (the `@Transactional` on the scheduler method rolls back cleanly). No duplicate saves, no skipped saves, no in-memory/DB state divergence. ✓
- **Status:** RESOLVED. No regression introduced.

---

## Cross-file seams checked (iteration 2 re-verification of affected seams)

- `OutboxPublisher` (all three services) success path: `rabbitTemplate.send()` → `setStatus("published")` / `setPublishedAt()` → `outboxEventRepository.save(event)` — all inside `try`. ✓
- `OutboxPublisher` (all three services) failure path: `catch (AmqpException)` → `incrementAttemptCount()` → `outboxEventRepository.save(event)` — all inside `catch`. ✓
- `docker-compose.yml` `KEYCLOAK_HOST: keycloak` / `KEYCLOAK_PORT: 8080` for `locations-resources-service`, `custom-field-service`, `settings-service` ↔ `application.yml` `${KEYCLOAK_HOST:localhost}:${KEYCLOAK_PORT:8180}` default: MISMATCH now resolved — env vars supplied override defaults inside docker-compose. ✓
- `keycloak` healthcheck defined (`exec 3<>/dev/tcp/localhost/8080`) ↔ `service_healthy` used in `depends_on`: valid — healthcheck exists and probes the correct internal port 8080. ✓
- `settings-service/application.yml` `listener.simple.auto-startup: false` comment: accurate, no functional change introduced. ✓
- All other seams verified in iteration 1 (controller→service→repository chains, DTO↔entity constructors, security config, DDL↔entity field mapping) remain unchanged by the fix commits. ✓
