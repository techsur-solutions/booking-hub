---
phase: 4
status: clean
blockers: 0
warnings: 0
files_reviewed: 53
files_reviewed_list:
  - docker-compose.yml
  - infra/rabbitmq/definitions.json
  - services/locations-resources-service/docker-compose.yml
  - services/locations-resources-service/pom.xml
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/controller/LocationController.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/controller/ResourceController.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/domain/Location.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/domain/Resource.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/domain/OutboxEvent.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/dto/LocationDtos.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/dto/ResourceDtos.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/ApiError.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/ApiException.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/GlobalExceptionHandler.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/LocationNameRequiredException.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/LocationNotFoundException.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/LocationResourceForbiddenException.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/ResourceNameRequiredException.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/ResourceNotFoundException.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/outbox/OutboxPublisher.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/repository/LocationRepository.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/repository/OutboxEventRepository.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/repository/ResourceRepository.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/security/CurrentUserProvider.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/security/JwksOutageAuthenticationEntryPoint.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/security/LocationResourceAccessDeniedHandler.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/security/SecurityConfig.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/service/LocationService.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/service/ResourceService.java
  - services/locations-resources-service/src/main/resources/application.yml
  - services/locations-resources-service/src/main/resources/db/migration/V2__add_location_colour_description_and_resource_fields.sql
  - services/locations-resources-service/src/test/java/com/bookinghub/locationsresources/controller/LocationControllerIntegrationTest.java
  - services/locations-resources-service/src/test/java/com/bookinghub/locationsresources/security/Tier1Tier2ConsistencyTest.java
  - services/custom-field-service/pom.xml
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/controller/CustomFieldController.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/controller/FieldTemplateController.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/domain/CustomField.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/domain/CustomFieldJoin.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/domain/CustomFieldTemplate.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/domain/OutboxEvent.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/dto/CustomFieldDtos.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/dto/FieldTemplateDtos.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/error/*.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/outbox/OutboxPublisher.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/repository/CustomFieldJoinRepository.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/repository/CustomFieldRepository.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/repository/CustomFieldTemplateRepository.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/security/CurrentUserProvider.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/security/CustomFieldAccessDeniedHandler.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/security/JwksOutageAuthenticationEntryPoint.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/security/SecurityConfig.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/service/CustomFieldService.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/service/FieldTemplateService.java
  - services/custom-field-service/src/main/resources/application.yml
  - services/custom-field-service/src/main/resources/db/migration/V2__add_outbox_and_required_flag.sql
  - services/custom-field-service/src/test/java/com/bookinghub/customfield/controller/CustomFieldControllerIntegrationTest.java
  - services/custom-field-service/src/test/java/com/bookinghub/customfield/controller/FieldTemplateControllerIntegrationTest.java
  - services/custom-field-service/src/test/java/com/bookinghub/customfield/security/Tier1Tier2ConsistencyTest.java
  - services/settings-service/pom.xml
  - services/settings-service/src/main/java/com/bookinghub/settings/controller/SettingsController.java
  - services/settings-service/src/main/java/com/bookinghub/settings/domain/OutboxEvent.java
  - services/settings-service/src/main/java/com/bookinghub/settings/domain/Settings.java
  - services/settings-service/src/main/java/com/bookinghub/settings/dto/SettingsDtos.java
  - services/settings-service/src/main/java/com/bookinghub/settings/error/*.java
  - services/settings-service/src/main/java/com/bookinghub/settings/outbox/OutboxPublisher.java
  - services/settings-service/src/main/java/com/bookinghub/settings/repository/OutboxEventRepository.java
  - services/settings-service/src/main/java/com/bookinghub/settings/repository/SettingsRepository.java
  - services/settings-service/src/main/java/com/bookinghub/settings/security/CurrentUserProvider.java
  - services/settings-service/src/main/java/com/bookinghub/settings/security/JwksOutageAuthenticationEntryPoint.java
  - services/settings-service/src/main/java/com/bookinghub/settings/security/SecurityConfig.java
  - services/settings-service/src/main/java/com/bookinghub/settings/security/SettingsAccessDeniedHandler.java
  - services/settings-service/src/main/java/com/bookinghub/settings/service/SettingsService.java
  - services/settings-service/src/main/resources/application.yml
  - services/settings-service/src/main/resources/db/migration/V2__add_outbox.sql
  - services/settings-service/src/test/java/com/bookinghub/settings/controller/SettingsControllerIntegrationTest.java
  - services/settings-service/src/test/java/com/bookinghub/settings/security/Tier1Tier2ConsistencyTest.java
reviewed_at: 2026-10-09T00:00:00Z
iteration: 1
---

# Phase 4 Code Review

## BLOCKERs

None found.

## WARNINGs

None found.

## Cross-file seams checked

- Gateway `SecurityConfig.java` `/locations/**,/resources/**` rules (GET=authenticated, POST/PUT/DELETE=SCOPE_role_location_admin) ↔ `LocationController`/`ResourceController` `@PreAuthorize` (role_calendar_viewer for GET — stricter, role_location_admin for writes — exact match) — OK, proven by `Tier1Tier2ConsistencyTest` which parses the Gateway's actual source file.
- Gateway `/custom-fields/**,/field-templates/**` single uniform `SCOPE_role_customfield_admin` rule (no HttpMethod narrowing) ↔ `CustomFieldController`/`FieldTemplateController` uniform `@PreAuthorize("hasRole('role_customfield_admin')")` on every method including GET — OK, proven by `Tier1Tier2ConsistencyTest`'s regex assertion that the Gateway line has no `HttpMethod` restriction.
- Gateway `/settings/**` GET=authenticated, PUT=SCOPE_role_settings_admin ↔ `SettingsController.getSettings()` (no `@PreAuthorize`, class-level authenticated baseline) / `updateSettings()` (`@PreAuthorize("hasRole('role_settings_admin')")`) — OK, proven by `Tier1Tier2ConsistencyTest`.
- `JwtAuthenticationConverter` realm_access.roles → ROLE_-prefixed authorities, identical implementation across all three services' `SecurityConfig` and matching `CurrentUserProvider.getCurrentRoles()`'s own realm_access.roles parsing — OK, same source of truth in each service.
- `LocationService`/`ResourceService` write-path DTOs (`LocationUpsertRequest`/`ResourceUpsertRequest`) ↔ `LocationController`/`ResourceController` `@RequestBody` consumption ↔ entity constructors — field order and types all align; `@JsonProperty` snake_case mapping consistent between request/response DTOs.
- `CustomFieldUpsertRequest.fieldType` `@Pattern` enum (textfield|select|textarea|radio|checkbox) ↔ `CustomFieldService.VALID_FIELD_TYPES`/`CHOICE_BASED_TYPES` constants — identical 5-value set and 3-value choice-subset, defense-in-depth duplication is intentional and consistent.
- `FieldTemplateService.update()`'s REPLACE-not-append join semantics (`deleteByCustomFieldTemplateId` then `createJoins`) ↔ `FieldTemplateControllerIntegrationTest`'s 2-field→1-field replace assertion — OK, test proves the behavior matches the javadoc claim.
- `CustomFieldTemplateRepository.findApplicableToContext` (context_id = :contextId OR context_id IS NULL) ↔ `CustomFieldService.listApplicableToContext`'s join-then-collect logic ↔ `CustomFieldControllerIntegrationTest`'s global+scoped-template scenario — OK, matches expected global/scoped resolution.
- Outbox table schema (`aggregate_type`, `exchange`, `routing_key`, `idempotency_key`, `payload`, `status`, `attempt_count`) identical across all three V2 migrations ↔ each service's `OutboxEvent` entity field mapping ↔ each `OutboxPublisher.relayPendingEvents()` consuming the same shape — OK, verified byte-for-byte consistent pattern (established Phase 3 precedent).
- `docker-compose.yml`/`services/locations-resources-service/docker-compose.yml` RabbitMQ env vars (`RABBITMQ_USER`) ↔ `locations-resources-service`/`custom-field-service` `application.yml` (`${RABBITMQ_USER:guest}`) — OK, consistent naming. `settings-service`'s pre-existing `RABBITMQ_USERNAME` convention (not touched this phase) is self-consistent between its own `application.yml` and `docker-compose.yml` entry — no integration break, just a naming inconsistency predating this phase, out of scope.
- `Settings` entity's DB-level CHECK constraints (`chk_settings_calendar_range: calendar_min_time < calendar_max_time`, `chk_settings_slot_size: calendar_slot_size > 0`, both from Phase 2's V1 migration) ↔ `SettingsService.update()`'s service-layer pre-merge validation (`resultingMinTime.isBefore(resultingMaxTime)`, `resultingSlotSize <= 0`) — OK, service validation is at least as strict as the DB constraint, checked against post-merge values so a partial update is validated correctly.
- `ApiError` record shape (`error_code`, `message`, `timestamp`, `path`) identical across all three services ↔ each `GlobalExceptionHandler`/`AccessDeniedHandler`/`JwksOutageAuthenticationEntryPoint` constructing it the same way — OK.
- Soft-delete-always-succeeds policy: `LocationService`/`ResourceService`'s `getById` (plain `findById`, 404 only if id never existed) vs `update`/`delete` (`findByIdAndDeletedAtIsNull`, 404 if missing-or-deleted) — internally consistent in both services, matching their own javadoc's named decision, and exercised end-to-end by `LocationControllerIntegrationTest`'s delete-then-read scenario.

No BLOCKERs or WARNINGs identified. Implementation is clean, well cross-verified by contract tests that read the Gateway's actual source rather than hardcoded assumptions, and the three services follow consistent, mutually-reinforcing patterns (same Tier-2 JWT converter bugfix applied identically where needed, same outbox-publisher shape, same error-contract shape).
