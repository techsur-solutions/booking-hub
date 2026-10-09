---
phase: 04
status: issues_found
blockers: 1
warnings: 2
files_reviewed: 47
files_reviewed_list:
  - services/locations-resources-service/src/main/resources/db/migration/V2__add_location_colour_description_and_resource_fields.sql
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/domain/Location.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/domain/Resource.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/domain/OutboxEvent.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/repository/LocationRepository.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/repository/ResourceRepository.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/repository/OutboxEventRepository.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/ApiError.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/ApiException.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/GlobalExceptionHandler.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/LocationNameRequiredException.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/ResourceNameRequiredException.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/LocationResourceForbiddenException.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/LocationNotFoundException.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/error/ResourceNotFoundException.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/security/SecurityConfig.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/security/JwksOutageAuthenticationEntryPoint.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/security/LocationResourceAccessDeniedHandler.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/security/CurrentUserProvider.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/dto/LocationDtos.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/dto/ResourceDtos.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/service/LocationService.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/service/ResourceService.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/controller/LocationController.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/controller/ResourceController.java
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/outbox/OutboxPublisher.java
  - services/custom-field-service/src/main/resources/db/migration/V2__add_outbox_and_required_flag.sql
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/domain/CustomField.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/domain/CustomFieldTemplate.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/domain/CustomFieldJoin.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/domain/OutboxEvent.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/repository/CustomFieldRepository.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/repository/CustomFieldTemplateRepository.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/repository/CustomFieldJoinRepository.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/repository/OutboxEventRepository.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/security/SecurityConfig.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/security/CurrentUserProvider.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/dto/CustomFieldDtos.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/dto/FieldTemplateDtos.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/service/CustomFieldService.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/service/FieldTemplateService.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/controller/CustomFieldController.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/controller/FieldTemplateController.java
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/outbox/OutboxPublisher.java
  - services/settings-service/src/main/java/com/bookinghub/settings/domain/Settings.java
  - services/settings-service/src/main/java/com/bookinghub/settings/security/SecurityConfig.java
  - services/settings-service/src/main/java/com/bookinghub/settings/security/CurrentUserProvider.java
  - services/settings-service/src/main/java/com/bookinghub/settings/controller/SettingsController.java
  - services/settings-service/src/main/java/com/bookinghub/settings/service/SettingsService.java
reviewed_at: 2026-10-09T15:37:09Z
iteration: 1
---

# Phase 04 Code Review

## BLOCKERs

### B1: CustomFieldService.update() mutates updatedAt via entity setters but doesn't persist the change
- **File:** services/custom-field-service/src/main/java/com/bookinghub/customfield/service/CustomFieldService.java:156-178
- **Category:** bug
- **Evidence:** The `update()` method calls `field.setLabel()`, `field.setFieldType()`, `field.setOptions()`, and `field.setRequired()` at lines 164-169. Each of these setters automatically calls `this.updatedAt = Instant.now()` internally (see CustomField.java lines 79-97). However, immediately after these setter calls at line 170, there is NO explicit call to `field.setUpdatedAt()` or `customFieldRepository.save(field)` **before** the outbox event is written. The save only happens implicitly through the `@Transactional` boundary at method exit. The pattern is inconsistent with LocationService/ResourceService which explicitly call `.save()` after mutations (LocationService.java:124, ResourceService.java:116). While the transaction boundary will eventually persist the entity, the outbox event payload is built and saved at lines 172-176 BEFORE the implicit save, potentially creating a race where the outbox sees the new updatedAt timestamp but the database row hasn't been flushed yet. More critically, the code is fragile: if someone later adds non-transactional code after the outbox write, the update could be lost entirely.
- **Fix direction:** Add explicit `customFieldRepository.save(field);` immediately after line 169, before the `writeOutboxEvent` call at line 172, matching the LocationService/ResourceService pattern and ensuring the entity state is definitely persisted before the outbox event references it.

## WARNINGs

### W1: SettingsService.update() validates post-merge calendarSlotSize but allows zero via request merge path
- **File:** services/settings-service/src/main/java/com/bookinghub/settings/service/SettingsService.java:130-137
- **Evidence:** Lines 130-132 compute `resultingSlotSize` as `request.calendarSlotSize() != null ? request.calendarSlotSize() : settings.getCalendarSlotSize()`. Line 134 validates `if (resultingSlotSize <= 0)` and throws. However, if `request.calendarSlotSize()` is explicitly `0` (not null), the condition `!= null` is true, so `resultingSlotSize = 0`, which then correctly fails the `<= 0` check. But if the **persisted** `settings.getCalendarSlotSize()` somehow contains `0` (violating the DB CHECK constraint which should enforce `> 0`), and the request passes `null` for this field, then `resultingSlotSize = 0` from the DB, which would fail validation. This is actually **correct** behavior — the validation catches a DB constraint violation that shouldn't exist. The warning is that the code doesn't trust the DB constraint and re-validates on every update, which is technically defense-in-depth but may mask a deeper data integrity issue. The real risk is if the DB CHECK constraint is missing or wrong — checked settings-service V1 migration (Phase 2), confirmed `CHECK (calendar_slot_size > 0)` exists, so this is purely defensive redundancy, not a bug.
- **Fix direction:** This is actually correct defense-in-depth. No fix required, but document in a comment at line 134 that this validation is redundant with the DB CHECK constraint and exists to catch constraint violations early with a clearer error message.

### W2: CustomFieldService.listApplicableToContext() performs N+1 queries when contextId is provided
- **File:** services/custom-field-service/src/main/java/com/bookinghub/customfield/service/CustomFieldService.java:88-109
- **Category:** bug
- **Evidence:** When `contextId != null`, lines 95-96 call `customFieldTemplateRepository.findApplicableToContext(contextId)` which returns a list of templates. Lines 99-102 then iterate over each template and call `customFieldJoinRepository.findByCustomFieldTemplateId(template.getId())` — one query per template. For a booking context with 10 templates, this issues 11 queries (1 for templates + 10 for joins). Lines 104-108 then call `customFieldRepository.findByIdAndDeletedAtIsNull(fieldId)` once per distinct fieldId — potentially many more queries. For a typical scenario (10 templates, 5 fields each = 50 joins, 20 distinct fields), this is 1 + 10 + 20 = 31 queries. The applicability query is called by the booking form on every load (Phase 5 will call `GET /custom-fields?context_id={locationId}` per TechArch §2.4), so this N+1 pattern will create significant database load under production traffic. The repository layer has no `@EntityGraph` or JOIN FETCH to batch-load the joins.
- **Fix direction:** Refactor `CustomFieldTemplateRepository.findApplicableToContext` to return a DTO or use a native query that JOINs `custom_field_templates` → `custom_field_joins` → `custom_fields` in a single query, avoiding the N+1 pattern. Alternatively, add `@Query` with explicit JOIN FETCH or batch the findByIdAndDeletedAtIsNull calls. This is not a BLOCKER because the feature is functionally correct, but it will cause performance degradation under load — flagged as WARNING rather than BLOCKER because Phase 5's booking-service hasn't been built yet and the impact is latency, not correctness.

## Cross-file seams checked

**locations-resources-service:**
- LocationController.create() → LocationService.create() → LocationRepository.save() + OutboxEventRepository.save(): OK (both called, @Transactional boundary covers both)
- LocationController.delete() → LocationService.delete() → sets deletedAt, no SQL DELETE: OK (soft-delete policy correctly implemented)
- LocationController.getById() → LocationService.getById() → `findById()` (not `findByIdAndDeletedAtIsNull`): OK (Success Criterion 2 requirement, allows reading soft-deleted rows)
- ResourceController follows identical patterns to LocationController: OK
- OutboxPublisher.relayPendingEvents() → RabbitTemplate.send() with idempotency_key header: OK (event relay mechanism correct)
- SecurityConfig.realmRoleJwtAuthenticationConverter() extracts roles matching CurrentUserProvider.getCurrentRoles() logic: OK (both read `realm_access.roles` identically)

**custom-field-service:**
- CustomFieldController.list(?context_id) → CustomFieldService.listApplicableToContext(): OK (query param correctly optional, null passed when absent)
- CustomFieldService.create() validates label + options for choice-based types: OK (LABEL_REQUIRED/OPTIONS_REQUIRED thrown correctly per FRD F5 error table)
- CustomFieldService.listApplicableToContext() → findApplicableToContext (OR-is-null query) → joins → fields: OK functionally, performance warning W2 above
- FieldTemplateService.update() → deleteByCustomFieldTemplateId() then creates new joins: OK (REPLACE semantics correctly implemented)
- FieldTemplateService.delete() relies on ON DELETE CASCADE for joins: OK (V1 schema confirmed FK with ON DELETE CASCADE exists)
- CustomFieldDtos.CustomFieldUpsertRequest.fieldType @Pattern regex: "textfield|select|textarea|radio|checkbox": OK (matches F0-confirmed 5-value enum, corrects TechArch's pre-F0 guess)
- SecurityConfig.realmRoleJwtAuthenticationConverter() extracts roles: OK (same pattern as locations-resources-service)

**settings-service:**
- SettingsController.getSettings() → SettingsService.getCurrent() → findById(1): OK (singleton read, no cache, immediate propagation)
- SettingsController.updateSettings() → SettingsService.update() → findById(1), mutate, save(), outbox: OK (upsert-against-fixed-id discipline, never creates new row)
- SettingsService.update() validates post-merge calendarMinTime < calendarMaxTime and slotSize > 0: OK (defense-in-depth validation, warning W1 above is cosmetic)
- SettingsService.update() uses SETTINGS_AGGREGATE_ID constant UUID for outbox aggregate_id: OK (singleton aggregate representation, never randomly generated)
- Settings entity has NO @GeneratedValue on id field: OK (singleton enforcement at code level, prevents accidental second-row creation)
- SecurityConfig.realmRoleJwtAuthenticationConverter() extracts roles: OK (same pattern as other services)

**Gateway integration (Tier-1 ↔ Tier-2 consistency, per summaries' Tier1Tier2ConsistencyTest claims):**
- Gateway route table (Phase 2 plan 02-10, not re-checked here — out of review scope per bounded-review rule) vs. service-level @PreAuthorize annotations:
  - locations-resources-service: GET requires `role_calendar_viewer` (stricter than Gateway's authenticated), writes require `role_location_admin` (matches Gateway): OK per plan 04-01's explicit "stricter for reads" decision
  - custom-field-service: ALL methods require `role_customfield_admin` (exact match to Gateway's uniform admin-gating): OK per plan 04-03's "no read/write split" decision
  - settings-service: GET has no additional @PreAuthorize (authenticated baseline only, matching Gateway), PUT requires `role_settings_admin` (matches Gateway): OK per plan 04-05/04-06's "read/write split matches Gateway exactly" decision

All cross-file seams checked. No integration mismatches found beyond the findings above.
