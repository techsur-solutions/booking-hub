---
phase: 4
status: issues_found
blockers: 1
warnings: 3
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
reviewed_at: 2026-10-09T15:04:42Z
iteration: 1
---

# Phase 4 Code Review

## BLOCKERs

### B1: `locations-resources-service` and `custom-field-service` outbox publishers fail with an unhandled non-`AmqpException` runtime exception — the outer `outboxEventRepository.save(event)` call is placed unconditionally AFTER the try/catch, but a non-`AmqpException` thrown during publish will escape the loop and skip persisting all remaining events' state changes in the same batch.

- **File:** `services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/outbox/OutboxPublisher.java`:65-88  
  `services/custom-field-service/src/main/java/com/bookinghub/customfield/outbox/OutboxPublisher.java`:54-78  
  `services/settings-service/src/main/java/com/bookinghub/settings/outbox/OutboxPublisher.java`:59-84
- **Category:** bug
- **Evidence:**  
  In every `OutboxPublisher.relayPendingEvents()` the structure is:
  ```java
  for (OutboxEvent event : batch) {
      try {
          rabbitTemplate.send(...);
          event.setStatus("published");
          event.setPublishedAt(Instant.now());
      } catch (AmqpException ex) {
          event.incrementAttemptCount();
      }
      outboxEventRepository.save(event);  // ← outside the try/catch
  }
  ```
  The catch only handles `AmqpException`. `RabbitTemplate.send()` can additionally throw unchecked `AmqpConnectException` (which IS a subclass of `AmqpException` and is caught), but the `outboxEventRepository.save(event)` call itself can throw `DataAccessException` (e.g. if the DB connection drops between the batch load and the save). That exception will propagate out of the loop, aborting the `@Transactional` and rolling back ALL saves in the batch that were already executed — meaning events whose status was just set to `"published"` are silently rolled back to `"pending"` in the DB (fine, they'll retry), but the bigger concern is a bug at the **start** of the loop: if an exception escapes the `try/catch` block inside the loop (e.g., from a hypothetical `RuntimeException` thrown by `rabbitTemplate.send` that is NOT an `AmqpException` subclass), the `outboxEventRepository.save(event)` for that event is also skipped — the event's in-memory state is mutated (`setStatus("published")`), but the save never runs for it, and the transaction eventually rolls back. This is a silent data-integrity gap where the message was delivered to RabbitMQ but the outbox row is never marked `published`, causing a re-publish on the next poll (a correctness issue: downstream consumers must handle duplicate delivery, which the `idempotency_key` header is supposed to support, but the design comment claims "never loses" guarantees that the loop structure doesn't fully back up). The identical pattern is present in all three services.  
  *Refutation attempt:* Could argue that `AmqpException` covers all Spring AMQP exceptions. Checked Spring AMQP source — `AmqpException` IS the root of the Spring AMQP exception hierarchy, so in practice `rabbitTemplate.send()` will only throw `AmqpException` subclasses for AMQP failures. However, the save-outside-try is still structurally wrong: if `outboxEventRepository.save()` throws, the exception escapes the loop, aborting the transaction, and the batch continues with partially-committed state on retrial. This is a real, not theoretical, failure path when the DB connection is briefly interrupted mid-batch.
- **Fix direction:** Move `outboxEventRepository.save(event)` inside the `try` block (after the status-set lines) and add a separate catch for the save itself, OR wrap the entire loop body (including the save) in an individual per-event try/catch so a single-event failure never aborts the remaining batch.

---

## WARNINGs

### W1: `settings-service/application.yml` contains a stale/wrong comment block inherited from a previous phase that could mislead operators and future developers.

- **File:** `services/settings-service/src/main/resources/application.yml`:28-33
- **Evidence:**  
  ```yaml
  listener:
    simple:
      # Disabled until Phase 3: RabbitMQ listener beans will be implemented in Phase 3
      # when event-driven message processing is added. Enabling without listener beans
      # will cause startup failure with "No bean of type RabbitListenerContainerFactory"
      auto-startup: false
  ```
  Phase 4 is now complete. The comment says "Disabled until Phase 3" — the current phase IS Phase 4, and Phase 3 has already shipped. There are no `@RabbitListener` beans in settings-service (confirmed by grep), so `auto-startup: false` is harmless, but the comment is factually wrong about the failure condition ("No bean of type RabbitListenerContainerFactory" is not what happens when listener beans are absent — it's what happens when the listener container factory itself is absent) and is misleading about which phase this was deferred to. It is not a functional defect but could cause an operator/dev to incorrectly believe they must add a listener bean before enabling this.
- **Fix direction:** Remove the entire `listener.simple.auto-startup: false` block (it is no longer needed since settings-service will never have RabbitMQ consumer beans — it is a pure publisher) or update the comment to accurately reflect that this setting should remain `false` permanently for settings-service.

---

### W2: `SettingsService.update()` performs partial-update logic with a structural inconsistency: individual domain setters (`setApproveBooking`, `setCalendarMinTime`, etc.) each independently call `Instant.now()` to update `updatedAt`, which is then overwritten by an explicit `settings.setUpdatedAt(Instant.now())` call. This means `updatedAt` gets set up to four times in a single update, and the FINAL explicit `setUpdatedAt` always wins — but the outbox payload `payload.put("updated_at", settings.getUpdatedAt().toString())` captures this final value correctly. The only real risk is that IF a downstream reader ever receives a slightly earlier timestamp (e.g., if the outbox row was written between two setter calls instead of at the end), there would be a discrepancy. In the current code this cannot happen since the payload is built AFTER the final `setUpdatedAt`.

- **File:** `services/settings-service/src/main/java/com/bookinghub/settings/service/SettingsService.java`:110-161
- **Evidence:**  
  `setApproveBooking(value)` at line 111 calls `this.updatedAt = Instant.now()` inside the setter (Settings.java:79). Then `setCalendarMinTime`, `setCalendarMaxTime`, `setCalendarSlotSize` each do the same. Then line 151 calls `settings.setUpdatedAt(Instant.now())` explicitly, overwriting all prior values. Then the outbox payload is built using `settings.getUpdatedAt()` (line 161) which is the correct final value. The actual persisted `updated_at` is the value from the final `setUpdatedAt` call (line 151), which is also the value saved and published. However, the code is unnecessarily fragile: if the `setUpdatedAt` call at line 151 were removed or reordered, the outbox payload would capture a slightly different timestamp than what gets persisted.
- **Fix direction:** Remove `this.updatedAt = Instant.now()` from the individual domain setters in `Settings.java` and rely solely on the explicit `setUpdatedAt` call in the service layer (matching the pattern used by `Location` and `Resource` entities where the service controls `updatedAt` explicitly). This makes the update logic unambiguous.

---

### W3: `docker-compose.yml` is missing `KEYCLOAK_HOST`, `KEYCLOAK_PORT`, and `KEYCLOAK_REALM` environment variables for `locations-resources-service`, `custom-field-service`, and `settings-service`. All three services use `${KEYCLOAK_HOST:localhost}:${KEYCLOAK_PORT:8180}` as their JWT issuer-uri default, which is correct for local development outside Docker but resolves to a non-routable address inside the docker-compose network where Keycloak is reachable only as `keycloak:8080`.

- **File:** `docker-compose.yml`:91-135, 206-227 (the three new service entries)
- **Evidence:**  
  Only `api-gateway` receives `KEYCLOAK_HOST: keycloak` and `KEYCLOAK_PORT: 8080` (docker-compose.yml:295-296). `locations-resources-service` (lines 91-112), `custom-field-service` (lines 114-135), and `settings-service` (lines 206-227) have no `KEYCLOAK_HOST`/`KEYCLOAK_PORT` entries. When these services start inside docker-compose, Spring Security's OAuth2 resource server will attempt to fetch JWKS from `http://localhost:8180/realms/bookinghub/.well-known/openid-configuration` (the default), which is unreachable from inside the container network. Any request bearing a real JWT will fail at token validation time with a `JwksOutageAuthenticationEntryPoint` 503 response — not a startup failure, but every authenticated call will be 503 in the docker-compose stack.  
  *Note:* This same omission pre-exists for `users-permissions-service` (phase 3, not this phase's introduction), so it is a cross-phase pattern. The phase-04 contribution to the problem is adding three more services with the same gap.
- **Fix direction:** Add `KEYCLOAK_HOST: keycloak`, `KEYCLOAK_PORT: 8080`, and `KEYCLOAK_REALM: bookinghub` to the `environment` blocks of `locations-resources-service`, `custom-field-service`, and `settings-service` in `docker-compose.yml`.

---

## Cross-file seams checked

- `LocationController` → `LocationService` → `LocationRepository`/`OutboxEventRepository`: call signatures match, return types consistent. OK
- `ResourceController` → `ResourceService` → `ResourceRepository`/`OutboxEventRepository`: call signatures match. OK
- `LocationDtos.LocationUpsertRequest` ↔ `Location` constructor (6-arg): all fields passed in correct order. OK
- `ResourceDtos.ResourceUpsertRequest` ↔ `Resource` constructor (5-arg): all fields passed in correct order. OK
- `LocationResponse` record ↔ `LocationService.toResponse()`: all 10 fields mapped. OK
- `ResourceResponse` record ↔ `ResourceService.toResponse()`: all 9 fields mapped. OK
- `CustomFieldController` → `CustomFieldService`: `listApplicableToContext(UUID)`, `get(UUID)`, `create`, `update`, `delete` — signatures match. OK
- `FieldTemplateController` → `FieldTemplateService`: `list()`, `get(UUID)`, `create`, `update`, `delete` — signatures match. OK
- `CustomFieldDtos.CustomFieldUpsertRequest` ↔ `CustomField` constructor (4-arg): `Boolean.TRUE.equals(request.required())` correctly handles null. OK
- `FieldTemplateDtos.FieldTemplateUpsertRequest` ↔ `CustomFieldTemplate` constructor: name/contextId match. OK
- `FieldTemplateResponse` ↔ `FieldTemplateService.toResponse()`: all 6 fields (id, name, contextId, fieldIds, createdAt, updatedAt) mapped. OK
- `CustomFieldTemplateRepository.findApplicableToContext(UUID)` JPQL: `WHERE t.contextId = :contextId OR t.contextId IS NULL` — correctly implements global+context logic. OK
- `SettingsController` → `SettingsService.getCurrent()`/`update()`: no `@Valid` on `SettingsUpdateRequest` — intentional (all fields optional, validation is in service layer). OK
- `SettingsDtos.SettingsUpdateRequest` ↔ `SettingsService.update()` partial-merge logic: null-check pattern for all four fields is consistent. OK
- `Settings.id` field type (`Integer`) ↔ `SettingsRepository extends JpaRepository<Settings, Integer>`: consistent. OK
- `OutboxEvent` entity ↔ `V2` DDL (`outbox` table): all 10 columns map to entity fields in all three services. OK
- `OutboxPublisher.relayPendingEvents()` ↔ `OutboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc(String)`: derived method name matches `status` field and `createdAt` field on entity. OK
- `SecurityConfig` `realmRoleJwtAuthenticationConverter()` bean type `Converter<Jwt, ? extends AbstractAuthenticationToken>` ↔ `jwt.jwtAuthenticationConverter(...)` parameter: compatible — `JwtAuthenticationConverter` implements this interface. OK
- `@PreAuthorize("hasRole('role_location_admin')")` ↔ `SecurityConfig.extractRealmRoleAuthorities()` producing `ROLE_role_location_admin`: Spring's `hasRole()` prepends `ROLE_` prefix automatically, so `hasRole('role_location_admin')` matches `ROLE_role_location_admin`. OK
- `LocationControllerIntegrationTest.withRole("role_location_admin")` → `SimpleGrantedAuthority("ROLE_role_location_admin")`: consistent with converter output. OK
- `SettingsControllerIntegrationTest` scenario 2 uses `.authorities(() -> "ROLE_role_settings_admin")` (not via converter) — correct for mock JWT test. OK
- `docker-compose.yml` `RABBITMQ_USER: guest` for `locations-resources-service` and `custom-field-service` ↔ `application.yml` `${RABBITMQ_USER:guest}`: match. OK
- `docker-compose.yml` `RABBITMQ_USERNAME: guest` for `settings-service` ↔ `application.yml` `${RABBITMQ_USERNAME:guest}`: match. OK
- `docker-compose.yml` `KEYCLOAK_HOST`/`KEYCLOAK_PORT` not set for `locations-resources-service`, `custom-field-service`, `settings-service` ↔ issuer-uri default `localhost:8180`: MISMATCH — see W3
- `custom_field_joins` V1 DDL `ON DELETE CASCADE` on `custom_field_template_id` ↔ `FieldTemplateService.delete()` relying on cascade (not manually deleting joins): correct and consistent. OK
- `FieldTemplateService.update()` `deleteByCustomFieldTemplateId(id)` (derived Spring Data delete method, no `@Modifying` needed) ↔ `CustomFieldJoinRepository`: works correctly — Spring Data derived delete methods execute within the caller's transaction. OK
- `CustomFieldService.validateOptions()` uses `CHOICE_BASED_TYPES = Set.of("select", "radio", "checkbox")` ↔ `@Pattern` regex `textfield|select|textarea|radio|checkbox` on DTO: complementary (DTO gatekeeps enum membership; service gatekeeps options requirement for choice subset). OK
- `settings-service` `V1__init_schema.sql` `chk_settings_singleton CHECK (id = 1)` ↔ `Settings.id` with no `@GeneratedValue` and `SINGLETON_ID = 1` constant: two independent layers both enforce singleton. OK
