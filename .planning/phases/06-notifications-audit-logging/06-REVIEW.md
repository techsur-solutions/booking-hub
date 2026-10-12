---
phase: 6
status: issues_found
blockers: 1
warnings: 0
files_reviewed: 24
files_reviewed_list:
  - services/notifications-service/src/main/java/com/bookinghub/notifications/consumer/NotificationConsumerService.java
  - services/notifications-service/src/main/java/com/bookinghub/notifications/controller/NotificationStatusController.java
  - services/notifications-service/src/main/java/com/bookinghub/notifications/dto/NotificationDtos.java
  - services/notifications-service/src/main/java/com/bookinghub/notifications/email/EmailSenderService.java
  - services/notifications-service/src/main/java/com/bookinghub/notifications/rabbit/DatabaseTrackingMessageRecoverer.java
  - services/notifications-service/src/main/java/com/bookinghub/notifications/rabbit/RabbitConfig.java
  - services/notifications-service/src/main/java/com/bookinghub/notifications/template/NotificationTemplateService.java
  - services/notifications-service/src/main/java/com/bookinghub/notifications/repository/NotificationDeliveryRepository.java
  - services/notifications-service/src/main/resources/application.yml
  - services/audit-log-service/src/main/java/com/bookinghub/auditlog/consumer/AuditEventConsumerService.java
  - services/audit-log-service/src/main/java/com/bookinghub/auditlog/controller/AuditLogController.java
  - services/audit-log-service/src/main/java/com/bookinghub/auditlog/dto/AuditLogDtos.java
  - services/audit-log-service/src/main/java/com/bookinghub/auditlog/dto/AuditLogEntryResponse.java
  - services/audit-log-service/src/main/java/com/bookinghub/auditlog/error/ApiException.java
  - services/audit-log-service/src/main/java/com/bookinghub/auditlog/error/AuditLogForbiddenException.java
  - services/audit-log-service/src/main/java/com/bookinghub/auditlog/error/AuditLogInvalidDateRangeException.java
  - services/audit-log-service/src/main/java/com/bookinghub/auditlog/event/DomainEventParser.java
  - services/audit-log-service/src/main/java/com/bookinghub/auditlog/event/InboundDomainEvent.java
  - services/audit-log-service/src/main/java/com/bookinghub/auditlog/repository/AuditLogSpecifications.java
  - services/audit-log-service/src/main/java/com/bookinghub/auditlog/domain/AuditLogEntry.java
  - services/audit-log-service/src/main/java/com/bookinghub/auditlog/repository/AuditLogEntryRepository.java
  - services/audit-log-service/pom.xml
  - services/notifications-service/src/test/java/com/bookinghub/notifications/consumer/NotificationConsumerIntegrationTest.java
  - services/audit-log-service/src/test/java/com/bookinghub/auditlog/consumer/AuditEventConsumerIntegrationTest.java
reviewed_at: 2026-10-12T17:30:00Z
iteration: 1
---

# Phase 6 Code Review

## BLOCKERs

### B1: Audit-log-service uses non-existent AmqpHeaders.RECEIVED_ROUTING_KEY constant

- **File:** services/audit-log-service/src/main/java/com/bookinghub/auditlog/consumer/AuditEventConsumerService.java:63 (and 7 more @RabbitListener methods)
- **Category:** bug / integration
- **Evidence:** 
  - All 7 @RabbitListener methods in AuditEventConsumerService use `@Header(AmqpHeaders.RECEIVED_ROUTING_KEY)` to inject the routing key
  - This constant does NOT exist in Spring Boot 3.3.4's AMQP library (org.springframework.amqp.support.AmqpHeaders)
  - Proof: Plan 06-02 (notifications-service) encountered the exact same issue and fixed it in commit 068f643 by replacing `@Header(AmqpHeaders.RECEIVED_ROUTING_KEY)` with `@Header("amqp_receivedRoutingKey")` (string literal)
  - Plan 06-04 was committed AFTER that fix (commits 7213980, e049080, 51bc5a4) but still uses the broken constant
  - The test file (AuditEventConsumerIntegrationTest) correctly sets the header manually with `props.setHeader("amqp_receivedRoutingKey", routingKey)` on line 294, confirming the intended header name, but the consumer won't resolve it
  - This will cause compilation failure: `error: cannot find symbol — variable RECEIVED_ROUTING_KEY`

- **Fix direction:** Replace all occurrences of `@Header(AmqpHeaders.RECEIVED_ROUTING_KEY)` with `@Header("amqp_receivedRoutingKey")` across all 7 listener methods in AuditEventConsumerService.java (lines 63, 74, 84, 96, 108, 119, 130). Remove the now-unused `import org.springframework.amqp.support.AmqpHeaders;` import.

- **Resolution:** fixed (4c7d89c) — Replaced all 7 @Header annotations with string literal "amqp_receivedRoutingKey" to match Spring AMQP's actual header name. Removed unused AmqpHeaders import. Verified: test file confirms header name at AuditEventConsumerIntegrationTest:294,306.

## WARNINGs

None found.

## Cross-file seams checked

### Notifications-service
- ✅ NotificationConsumerService correctly imports and uses all injected services (EmailSenderService, NotificationTemplateService, NotificationDeliveryRepository, ObjectMapper)
- ✅ EmailSenderService.send() throws MailException (retryable per design)
- ✅ NotificationTemplateService.render() throws TemplateRenderingException (retryable per design)
- ✅ NotificationTemplateService correctly extracts fields from JsonNode with fallback chain (requireText, textOrDefault) 
- ✅ NotificationDeliveryRepository.findByIdempotencyKey() signature matches consumer usage (returns Optional<NotificationDelivery>)
- ✅ DatabaseTrackingMessageRecoverer.recover() gracefully handles missing idempotency_key header and missing delivery row (uses .ifPresent())
- ✅ NotificationStatusController.getDeliveryStatus() and getDeadLettered() correctly use Specification-based filtering via JpaSpecificationExecutor
- ✅ NotificationDtos.DeliveryStatusResponse maps all entity fields correctly via fromEntity()
- ✅ RabbitConfig registers MissingIdempotencyKeyException and UnresolvableRecipientException as non-retryable (SimpleRetryPolicy exemption map)
- ✅ application.yml configures SMTP (mail.host, mail.port) and password-reset-link-base-url
- ✅ Both @RabbitListener methods use correct string literal `@Header("amqp_receivedRoutingKey")` for routing key injection

### Audit-log-service
- ❌ AuditEventConsumerService uses AmqpHeaders.RECEIVED_ROUTING_KEY constant which does NOT exist → **BLOCKER B1**
- ✅ DomainEventParser correctly extracts entity_type and action_type via lastIndexOf('.') split of routing key
- ✅ DomainEventParser implements defensive fallback chains for entity_id (nil-UUID), actor_id (multiple field attempts), and occurred_at (multiple timestamp attempts + Instant.now())
- ✅ AuditEventConsumerService correctly implements lookup-first idempotency: findByIdempotencyKey() before insert, skips if duplicate (MissingIdempotencyKeyException for validation)
- ✅ AuditLogController.list() validates date range (dateFrom must be before dateTo on line 82) and constructs inclusive date bounds correctly (line 87-88)
- ✅ AuditLogController manually checks hasRole("role_audit_viewer") to throw FRD-specific AUDIT_LOG_FORBIDDEN instead of generic AUTH_FORBIDDEN (line 77)
- ✅ AuditLogSpecifications correctly chains null-safe Specification builders for optional filters
- ✅ AuditLogEntryRepository.findByIdempotencyKey() signature matches consumer usage (returns Optional<AuditLogEntry> with UUID type)
- ✅ ApiException base class correctly exposes httpStatus and errorCode for GlobalExceptionHandler polymorphic catching
- ✅ AuditLogEntryResponse record maps all entity fields correctly
- ✅ AuditLogEntry domain model correctly marks created_at as insertable=false/updatable=false (DB DEFAULT now()), and idempotency_key as nullable UUID
- ✅ pom.xml includes spring-retry and spring-aspects for RabbitMQ retry interceptor

---

**Summary:** Phase 6 implementations are sound in logic, error handling, idempotency patterns, and dynamic filtering. However, audit-log-service has a critical compilation blocker: the use of a non-existent Spring AMQP constant that notifications-service already identified and fixed in plan 06-02. The fix is trivial (replace constant with string literal), but it must be applied before audit-log-service can compile or run.
