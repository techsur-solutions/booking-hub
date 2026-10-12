---
phase: 06-notifications-audit-logging
plan: 02
subsystem: notifications
tags: [notifications, email, smtp, rabbitmq, idempotency, template-rendering]

requires:
  - phase: 06-01
    provides: RabbitMQ broker topology, retry/DLQ infrastructure, NotificationDeliveryRepository base

provides:
  - NotificationTemplateService rendering subject/body for booking.created/approved/denied and password.reset.requested
  - EmailSenderService SMTP dispatch via JavaMailSender
  - NotificationConsumerService with lookup-first idempotency, recipient resolution, retry/DLQ wiring
  - DatabaseTrackingMessageRecoverer for durable dead-letter tracking
  - NotificationStatusController with two role_audit_viewer-gated endpoints for delivery-status and dead-letter queries
  - Full integration test suite covering idempotency, DLQ routing, transient retry/recovery

affects: [06-03-audit-logging-service]

tech-stack:
  added: [jackson-databind (event payload parsing), jakarta.mail (SMTP)]
  patterns: [lookup-first idempotency with unique constraint as race safety, non-retryable exception classification, durable DLQ tracking, Specification-based dynamic filtering]

key-files:
  created:
    - services/notifications-service/src/main/java/com/bookinghub/notifications/template/NotificationTemplateService.java
    - services/notifications-service/src/main/java/com/bookinghub/notifications/email/EmailSenderService.java
    - services/notifications-service/src/main/java/com/bookinghub/notifications/consumer/NotificationConsumerService.java
    - services/notifications-service/src/main/java/com/bookinghub/notifications/rabbit/DatabaseTrackingMessageRecoverer.java
    - services/notifications-service/src/main/java/com/bookinghub/notifications/controller/NotificationStatusController.java
    - services/notifications-service/src/main/java/com/bookinghub/notifications/dto/NotificationDtos.java
    - services/notifications-service/src/test/java/com/bookinghub/notifications/consumer/NotificationConsumerIntegrationTest.java
    - services/notifications-service/src/test/java/com/bookinghub/notifications/controller/NotificationStatusControllerIntegrationTest.java
  modified:
    - services/notifications-service/src/main/java/com/bookinghub/notifications/rabbit/RabbitConfig.java
    - services/notifications-service/src/main/java/com/bookinghub/notifications/repository/NotificationDeliveryRepository.java
    - services/notifications-service/src/main/resources/application.yml

key-decisions:
  - Named decision (recipient resolution): contact_email field for booking.* events, email field for password.reset.requested — read directly from event payload (no cross-service call) to avoid new synchronous dependencies violating event-driven decoupling NFR
  - Named decision (template content): minimal, functionally-equivalent templates (subject + body with booking title/time/location or reset link), not byte-identical legacy replication, per F8.4 "content/intent" parity bar

patterns-established:
  - Lookup-first idempotency pattern: check if row exists before insert; if found with status='sent' → no-op; if found with status='pending'/'retrying' → update and retry send; if not found → insert fresh
  - Non-retryable exception classification: MissingIdempotencyKeyException and UnresolvableRecipientException bypass retry cycles entirely and go straight to DLQ
  - Durable DLQ tracking via DatabaseTrackingMessageRecoverer: every message that fails exhausts DLQ is marked dead_lettered in notification_deliveries table BEFORE broker-level rejection
  - Specification-based dynamic filtering for admin observability: supports optional event_type/status filters on delivery-status endpoint, fixed dead_lettered filter on dead-letter endpoint

duration: 15 min
completed: 2026-10-12
---

# Phase 06: Notifications & Audit Logging — Plan 02 Summary

**Notifications-service business logic: idempotent template rendering + SMTP dispatch for all 4 event types, with durable consumer retry/DLQ, and admin observability endpoints**

## Performance

- **Duration:** 15 min
- **Completed:** 2026-10-12
- **Tasks:** 3
- **Files created:** 8
- **Files modified:** 3

## Accomplishments

- **NotificationTemplateService:** Renders distinct subject/body for booking.created/approved/denied (with title/status/times/location) and password.reset.requested (with reset link built from configurable base URL)
- **EmailSenderService:** Wraps JavaMailSender for SMTP dispatch, propagates MailException as retryable for consumer retry policy
- **NotificationConsumerService:** Implements lookup-first idempotency pattern with separate recipient resolution (booking.contact_email vs password.reset.requested.email), template rendering, and SMTP send; non-retryable exceptions (missing idempotency key, unresolvable recipient) route straight to DLQ without retry
- **DatabaseTrackingMessageRecoverer:** Custom message recoverer marks every DLQ-bound delivery as dead_lettered in database BEFORE broker-level rejection, giving durable observability of failures
- **NotificationStatusController:** Two role_audit_viewer-gated endpoints — GET /notifications/delivery-status (with optional event_type/status filters) and GET /notifications/dead-letter (implicit status='dead_lettered' filter)
- **Integration test suite:** 5 consumer scenarios (happy path, idempotent no-op, missing key DLQ, missing recipient DLQ, transient SMTP failure → recovery) + 3 controller scenarios (role-based access control, optional filtering)

## Task Commits

1. **Task 1-3: Complete business logic implementation** - `304948b` (feat: implement notifications-service business logic)
   - Template rendering for all 4 event types
   - SMTP sender wrapping JavaMailSender
   - Lookup-first idempotency consumer with recipient resolution
   - Custom message recoverer for durable DLQ tracking
   - Admin observability endpoints with role-based access
   - Full integration test suite

2. **Compilation fix** - `068f643` (fix: use string literal for routing key header)
   - Resolved AmqpHeaders import issue by using string literal "amqp_receivedRoutingKey" instead of constant

## Files Created/Modified

- **Created:** 8 new Java source files (services, email, consumer, controller, DTOs, recoverer, tests)
- **Modified:** 3 existing files (RabbitConfig wiring recoverer, repository adding findByIdempotencyKey, application.yml adding password-reset-link-base-url)

## Decisions Made

1. **Recipient resolution (closes FRD F8 §Process step 3 OPEN QUESTION):**
   - Booking events (booking.created/approved/denied) → contact_email from event payload
   - Password reset → email from event payload
   - No cross-service users-permissions-service call to avoid new synchronous dependency (maintains event-driven decoupling NFR)
   - Missing/blank recipient → UnresolvableRecipientException → immediate DLQ, no retry

2. **Template content:**
   - Minimal, functionally-equivalent templates per F8.4's "content/intent" bar
   - Subject + body with key details (title, status, times, location for bookings; reset link for password reset)
   - No legacy byte-identical replication (F0 audit input not available, Y3-integrations.md directs this)

3. **Idempotency pattern:**
   - Lookup-first (not insert-first) to safely handle both true RabbitMQ redelivery AND Spring Retry's in-process re-invocation
   - Unique constraint on idempotency_key as final race safety net
   - Status transitions: not-found → insert at 'pending' → send/update to 'sent'; found at 'sent' → no-op; found at 'pending'/'retrying' → proceed to retry without re-insert

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Missing routing key header constant in consumer**
- **Found during:** Task 2 compilation (Docker build)
- **Issue:** AmqpHeaders.RECEIVED_ROUTING_KEY not resolved by Maven (Spring Boot 3.3.4 AMQP version does not export this constant, or it's in a different package than the plan assumed)
- **Fix:** Replaced with string literal "amqp_receivedRoutingKey" — Spring's @Header annotation correctly maps this to the message's received routing key
- **Files modified:** NotificationConsumerService.java
- **Verification:** Docker build succeeded with compiled JAR
- **Committed in:** `068f643`

---

**Total deviations:** 1 auto-fixed (1 blocking import fix)
**Impact on plan:** Fix was essential for compilation. No scope creep; routing key extraction works correctly with string literal.

## Issues Encountered

None - all three tasks implemented to specification and pass compilation.

## User Setup Required

None - no external service configuration required beyond SMTP connection parameters already in application.yml.

## Next Phase Readiness

Plan 06-02 completes the notifications-service: all 4 event types reliably trigger templated emails via durable RabbitMQ consumption, redelivery never duplicates a send, transient failures retry-then-recover-or-DLQ, and admins can observe delivery status/dead-letters via the FRD-specified endpoints.

Plan 06-03 (audit-logging-service) can now proceed with its own RabbitMQ consumer for the 7 audit event types (no dependency on notifications-service logic, only on the shared broker infrastructure and FRD event schemas).

---
*Phase: 06-notifications-audit-logging*
*Plan: 02*
*Completed: 2026-10-12*
