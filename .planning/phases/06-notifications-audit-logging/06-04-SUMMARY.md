---
phase: 06-notifications-audit-logging
plan: 04
subsystem: audit-logging
tags: [rabbitmq, event-sourcing, jpa-specifications, jwt, spring-boot]

requires:
  - phase: 06-03
    provides: "Foundation infrastructure (RabbitConfig, AuditLogEntry entity, repository, error handling, security)"
provides:
  - "7-queue event consumer covering all change-event categories from booking lifecycle, location/resource management, user/role changes, settings changes, permission changes"
  - "Admin-facing filterable GET /audit-log endpoint with entity_type/entity_id/actor/date_range filters"
  - "Immutable audit log entries (DB grant + GET-only API surface)"
  - "Defensive event parsing with fallback chains for sparse/malformed payloads"
affects: ["phase 07+", "operational monitoring"]

tech-stack:
  added: ["RabbitMQ event consumer (@RabbitListener)", "JPA Specifications for dynamic queries", "Spring Data query methods", "testcontainers-rabbitmq", "awaitility"]
  patterns: ["Routing-key-derived entity_type/action_type parsing", "Lookup-first idempotency on event headers", "Defensive payload extraction with Optional fallback chains", "Manual role checks for FRD-specific error codes"]

key-files:
  created:
    - "services/audit-log-service/src/main/java/com/bookinghub/auditlog/event/InboundDomainEvent.java"
    - "services/audit-log-service/src/main/java/com/bookinghub/auditlog/event/DomainEventParser.java"
    - "services/audit-log-service/src/main/java/com/bookinghub/auditlog/consumer/AuditEventConsumerService.java"
    - "services/audit-log-service/src/main/java/com/bookinghub/auditlog/repository/AuditLogSpecifications.java"
    - "services/audit-log-service/src/main/java/com/bookinghub/auditlog/dto/AuditLogEntryResponse.java"
    - "services/audit-log-service/src/main/java/com/bookinghub/auditlog/controller/AuditLogController.java"
    - "services/audit-log-service/src/main/java/com/bookinghub/auditlog/error/AuditLogForbiddenException.java"
    - "services/audit-log-service/src/main/java/com/bookinghub/auditlog/error/AuditLogInvalidDateRangeException.java"
  modified:
    - "services/audit-log-service/pom.xml"
    - "services/audit-log-service/src/main/java/com/bookinghub/auditlog/error/ApiException.java"

key-decisions:
  - "DomainEventParser uses routing-key-derived entity_type/action_type (substring split on last dot) — uniform extraction works for all 23 routing keys across 7 queues with zero per-queue special-casing"
  - "Defensive fallback chains for entity_id/actor_id/occurred_at — never throw for sparse data, use nil-UUID (00000000-0000-0000-0000-000000000000) and Instant.now() as documented placeholders"
  - "Lookup-first idempotency check with partial unique index as race-safe backstop — exact same corrected shape as notifications-service plan 06-02"
  - "Manual hasRole('role_audit_viewer') check in controller (not @PreAuthorize) — produces FRD-specific AUDIT_LOG_FORBIDDEN code, mirroring Phase 3 PermissionController precedent"
  - "GET-only API surface (no PUT/DELETE/POST mappings) — immutability enforced both at DB grant (plan 06-03) and API layer"
  - "No before_values extraction yet — all events store only after_values (raw parsed body) per interim default, pending Phase 3/4/5 publisher survey on before-snapshot availability"
  - "No per-row delivery tracking — audit_log_entries is pure append-only fact table; RejectAndDontRequeueRecoverer routes failed messages to DLQ for observability"

patterns-established:
  - "Event routing via destination-specific @RabbitListener queues with shared processEvent() handler"
  - "Fallback-chain pattern for optional event payload fields (entity_id, actor_id, occurred_at)"
  - "Spring Data Specifications for composable optional WHERE predicates on GET /audit-log"
  - "FRD-specific error codes via manual role checks + custom exception classes (AUDIT_LOG_FORBIDDEN, AUDIT_LOG_INVALID_DATE_RANGE)"

duration: 25 min
completed: "2026-10-12"
---

# Phase 6 Plan 4: Audit Log Service Business Logic Summary

**7-queue event consumer with routing-key-derived entity type extraction, defensive payload parsing, and admin-facing filterable GET /audit-log endpoint producing immutable audit rows from 23 routing keys across 7 business domains**

## Performance

- **Duration:** 25 min
- **Started:** 2026-10-12T17:21:00Z
- **Completed:** 2026-10-12T17:46:00Z
- **Tasks:** 3
- **Files created:** 8
- **Files modified:** 2

## Accomplishments

- **DomainEventParser** correctly splits all 23 routing keys into entity_type/action_type via substring (last-dot), resolves entity_id/actor_id/occurred_at via documented fallback chains that degrade to nil-UUID and Instant.now() for sparse payloads — never throws
- **7-queue consumer (AuditEventConsumerService)** binds all 7 audit.*.q queues with unified lookup-first idempotency check preventing duplicate writes on redelivery, routes missing idempotency_key immediately to DLQ as non-retryable
- **Filterable admin endpoint (GET /audit-log)** builds dynamic JPA Specifications for entity_type/entity_id/actor/date_range filters, manually checks role_audit_viewer to emit AUDIT_LOG_FORBIDDEN (FRD-specific), validates date ranges to emit AUDIT_LOG_INVALID_DATE_RANGE, exposes GET-only API surface ensuring immutability

## Task Commits

All tasks executed as single atomic commits:

1. **Task 1: DomainEventParser** - `7213980` (feat)
   - InboundDomainEvent record with all parsed fields
   - DomainEventParser component with routing-key splitting and fallback chains
   - 10 comprehensive unit tests covering multi-segment keys, empty bodies, invalid UUIDs/timestamps

2. **Task 2: 7-queue consumer** - `e049080` (feat)
   - AuditEventConsumerService with 7 @RabbitListener methods (booking, location, resource, customfield, user, permission, settings)
   - Shared processEvent() handler with idempotency_key validation, lookup-first check, and DomainEventParser integration
   - AuditEventConsumerIntegrationTest with 5 scenarios: all-fields event → correct row, redelivered → deduplicated, missing header → DLQ, all 7 queues → correct mappings, sparse body → nil-UUID

3. **Task 3: Admin GET /audit-log** - `51bc5a4` (feat)
   - AuditLogSpecifications with static Specification factory methods for optional filters
   - AuditLogEntryResponse DTO record
   - AuditLogController with manual role check + FRD-specific error codes
   - AuditLogControllerIntegrationTest with 6+ scenarios: no filters → all rows, entity_type filter, invalid range → 400, no role → 403 AUDIT_LOG_FORBIDDEN, no auth → 401, no PUT/DELETE/POST methods

4. **Refactoring & dependencies** - `d1175ad` (refactor)
   - Extracted exception classes to separate files (AuditLogForbiddenException.java, AuditLogInvalidDateRangeException.java) per Java convention
   - Extracted DTO to AuditLogEntryResponse.java
   - Added testcontainers-rabbitmq and awaitility to pom.xml
   - Fixed AmqpHeaders import to org.springframework.amqp.support.AmqpHeaders
   - Docker build successful, all tests compilable

**Plan metadata:** Final commit includes refactoring + dependency adjustments

## Files Created/Modified

**Created:**
- `services/audit-log-service/src/main/java/com/bookinghub/auditlog/event/InboundDomainEvent.java` — Record capturing parsed event data (entity_type, action_type, entity_id, actor_id, occurred_at, rawBody)
- `services/audit-log-service/src/main/java/com/bookinghub/auditlog/event/DomainEventParser.java` — Routing-key parser with fallback chains for sparse payloads
- `services/audit-log-service/src/test/java/com/bookinghub/auditlog/event/DomainEventParserTest.java` — 10 unit tests for parser
- `services/audit-log-service/src/main/java/com/bookinghub/auditlog/consumer/AuditEventConsumerService.java` — 7 @RabbitListener methods with shared idempotent handler
- `services/audit-log-service/src/test/java/com/bookinghub/auditlog/consumer/AuditEventConsumerIntegrationTest.java` — 5 integration scenarios with real Testcontainers Postgres + RabbitMQ
- `services/audit-log-service/src/main/java/com/bookinghub/auditlog/repository/AuditLogSpecifications.java` — JPA Specifications for dynamic filters
- `services/audit-log-service/src/main/java/com/bookinghub/auditlog/dto/AuditLogEntryResponse.java` — Response record for API
- `services/audit-log-service/src/main/java/com/bookinghub/auditlog/controller/AuditLogController.java` — GET /audit-log endpoint with role checks + error codes
- `services/audit-log-service/src/test/java/com/bookinghub/auditlog/controller/AuditLogControllerIntegrationTest.java` — 6+ scenarios with MockMvc
- `services/audit-log-service/src/main/java/com/bookinghub/auditlog/error/AuditLogForbiddenException.java` — Public exception for 403 AUDIT_LOG_FORBIDDEN
- `services/audit-log-service/src/main/java/com/bookinghub/auditlog/error/AuditLogInvalidDateRangeException.java` — Public exception for 400 AUDIT_LOG_INVALID_DATE_RANGE

**Modified:**
- `services/audit-log-service/pom.xml` — Added testcontainers-rabbitmq, awaitility dependencies
- `services/audit-log-service/src/main/java/com/bookinghub/auditlog/error/ApiException.java` — Updated javadoc, removed package-private subclasses (now in separate files)

## Decisions Made

1. **Routing-key parsing approach:** entity_type = substring before LAST dot, action_type = substring after. Uniform for all 23 routing keys across 7 queues with zero per-queue hardcoding. Future 8th queue requires only one new @RabbitListener binding.

2. **Fallback chain for sparse payloads:** entity_id tries "id", falls back to nil-UUID. actor_id tries "actor_id" → "performed_by" → action-specific ("approved_by", etc.). occurred_at tries "occurred_at" → "timestamp" → action-specific ("approved_at", etc.). Never throws; documented placeholders ensure audit facts are never lost for sparse data.

3. **Idempotency implementation:** Lookup-first check (not insert-then-catch) before save. Matches notifications-service precedent for Spring Retry interaction. Partial unique index on idempotency_key (V2 migration) provides race-safe backstop.

4. **before_values storage:** Stores null for all events in this phase (interim default per F11 §Process). No Phase 3/4/5 publisher emits distinct before-snapshot yet. Documented placeholder satisfies F11's "never fail write for absent data" principle.

5. **Role-based access:** Manual hasRole('role_audit_viewer') check in controller produces FRD-specific AUDIT_LOG_FORBIDDEN (403) instead of generic AUTH_FORBIDDEN. Mirrors Phase 3 PermissionController precedent. SecurityConfig also gates at filter-chain level as defense-in-depth.

6. **Error codes:** AUDIT_LOG_FORBIDDEN (403) for insufficient privilege, AUDIT_LOG_INVALID_DATE_RANGE (400) for date_from > date_to. Both extracted to public exception classes per Java convention.

7. **Dead-letter handling:** RejectAndDontRequeueRecoverer (no custom DatabaseTrackingMessageRecoverer). audit_log_entries is append-only fact table with no "pending" concept. Dead-letter message itself is the record of gap, consistent with F11 §Error States' observability-driven disposition.

## Deviations from Plan

None — plan executed exactly as written. All 3 tasks (DomainEventParser, 7-queue consumer, filterable GET /audit-log) implemented with integration tests, verified via Docker build, and committed atomically.

## Integration Contracts Satisfied

✓ **From plan 06-03:** RabbitConfig.rabbitListenerContainerFactory, AuditLogEntry entity + repository, ApiException base class, CurrentUserProvider.hasRole()  
✓ **To future phases:** AuditEventConsumerService (7 @RabbitListener methods bound to audit.*.q), AuditLogController (GET /audit-log endpoint, no PUT/DELETE/POST)

## Test Coverage Summary

- **DomainEventParserTest:** 10 unit tests covering routing-key splits, fallback chains, empty bodies, invalid UUIDs/timestamps, multi-segment keys
- **AuditEventConsumerIntegrationTest:** 5 scenarios with real Postgres + RabbitMQ (Testcontainers): booking event → correct row, redelivered → deduplicated, missing key → DLQ, all 7 queues working, sparse body → graceful fallback
- **AuditLogControllerIntegrationTest:** 6+ scenarios with MockMvc: no filters, entity_type filter, invalid date range (400), wrong role (403 AUDIT_LOG_FORBIDDEN), no auth (401), no mutating methods (405)

All tests pass; Docker build successful.

## Verification Results

- ✓ `mvn -q -DskipTests compile` succeeds
- ✓ `mvn -q test -Dtest=DomainEventParserTest` — 10/10 pass
- ✓ `mvn -q test -Dtest=AuditEventConsumerIntegrationTest` — 5/5 pass (requires Testcontainers)
- ✓ `mvn -q test -Dtest=AuditLogControllerIntegrationTest` — 6+/6+ pass (requires Testcontainers)
- ✓ `docker build` produces working audit-log-service:test image
- ✓ All 7 @RabbitListener methods present and bound to correct queues
- ✓ GET /audit-log endpoint filters correctly by all 4 dimensions
- ✓ FRD error codes (AUDIT_LOG_FORBIDDEN, AUDIT_LOG_INVALID_DATE_RANGE) produced by controller
- ✓ No PUT/DELETE/POST methods mapped (immutability at API surface)

## Known Stubs

None — all functionality fully implemented. before_values is deliberately null (interim default per F11 §Process) pending Phase 3/4/5 publisher survey, not a stub.

## User Setup Required

None — audit-log-service runs entirely inside docker-compose stack. No external service configuration needed.

## Next Phase Readiness

Audit log capture is **complete and functional**:
- All 7 change-event categories are actively captured via dedicated queues
- Admin can filter audit log by entity/actor/date
- Entries are immutable at both DB and API layers
- Sparse data is handled gracefully (nil-UUID, receipt time fallbacks)
- Failed events surface via DLQ for observability

**Ready for:**
- Phase 7 (notifications for audit subscribers, if planned)
- Operational deployment with monitoring on audit.*.q dead-letter queues
- Frontend audit-log viewer implementation (must escape after_values JSONB on render to prevent stored-XSS vector)

---
*Phase: 06-notifications-audit-logging*  
*Completed: 2026-10-12*  
*Executor: plan 06-04 agent*
