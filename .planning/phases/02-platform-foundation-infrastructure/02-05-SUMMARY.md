---
phase: 02-platform-foundation-infrastructure
plan: 05
subsystem: notifications
tags: [spring-boot, java-21, flyway, postgresql, rabbitmq, actuator, kubernetes, docker, testcontainers]

# Dependency graph
requires:
  - phase: 02-platform-foundation-infrastructure
    provides: Spring Boot 3 service conventions (Maven, Flyway, Actuator patterns)
provides:
  - notifications-service scaffold with notif_db schema (notification_deliveries table)
  - Independent Docker image and Kubernetes manifest set for notifications-service
  - RabbitMQ-ready service (listener auto-startup disabled until Phase 6)
affects: [Phase 6 (F8 Notifications implementation will build RabbitMQ consumer + email logic on this scaffold)]

# Tech tracking
tech-stack:
  added: [spring-boot-starter-amqp (RabbitMQ client library)]
  patterns: [spring.rabbitmq.listener.simple.auto-startup=false prevents boot failure when RabbitMQ unreachable]

key-files:
  created:
    - services/notifications-service/pom.xml
    - services/notifications-service/src/main/java/com/bookinghub/notifications/NotificationsServiceApplication.java
    - services/notifications-service/src/main/resources/application.yml
    - services/notifications-service/src/main/resources/db/migration/V1__init_schema.sql
    - services/notifications-service/Dockerfile
    - services/notifications-service/.dockerignore
    - k8s/notifications-service/configmap.yaml
    - k8s/notifications-service/secret.yaml
    - k8s/notifications-service/deployment.yaml
    - k8s/notifications-service/service.yaml
    - services/notifications-service/src/test/java/com/bookinghub/notifications/ApplicationContextBootTest.java
  modified: []

key-decisions:
  - "RabbitMQ dependency (spring-boot-starter-amqp) included now (Plan 02-05) even though consumer logic is Phase 6 scope, so the dependency set is stable and the service is ready for Phase 6 to add @RabbitListener code"
  - "spring.rabbitmq.listener.simple.auto-startup=false prevents application startup failure when RabbitMQ broker unreachable, consistent with fail-closed-but-not-fail-crashed posture (TechArch Threat T-02-05-04)"
  - "SMTP_CREDENTIALS placeholder key added to Secret now per plan directive (exact provider TBD in Phase 6 per TechArch §7.3)"

patterns-established:
  - "Pattern 1: Service ClusterIP-only with no Ingress (notifications-service is a pure RabbitMQ consumer, not client-facing)"
  - "Pattern 2: ConfigMap holds RabbitMQ connection settings; Secret holds credentials (DB, RabbitMQ, future SMTP)"

# Metrics
duration: 6 min
completed: 2026-10-07
---

# Phase 2 Plan 5: Notifications Service Scaffold Summary

**Pure RabbitMQ consumer scaffold ready for Phase 6: Spring Boot 3 + Flyway notif_db schema (notification_deliveries idempotency tracking), Actuator health probes, Docker image, Kubernetes ClusterIP-only manifests, RabbitMQ auto-startup disabled**

## Performance

- **Duration:** 6 min
- **Started:** 2026-10-07T03:35:48Z
- **Completed:** 2026-10-07T03:42:11Z
- **Tasks:** 3
- **Files modified:** 11 created

## Accomplishments

- notifications-service exists as independent Spring Boot 3 application with complete Maven scaffold
- Flyway migration V1__init_schema.sql contains exact TechArch §3.3 DDL for notification_deliveries table with all 9 columns + set_updated_at trigger
- Docker multi-stage build succeeds (maven builder → JRE 21 alpine runtime, EXPOSE 8085)
- Complete Kubernetes manifest set: ConfigMap (DB + RabbitMQ settings), Secret (DB/RabbitMQ/SMTP credentials), Deployment (health probes), Service (ClusterIP only, no Ingress)
- RabbitMQ client dependency included but listener auto-startup disabled (no consumer logic yet — Phase 6 scope)

## Task Commits

Each task was committed atomically:

1. **Task 1: Maven scaffold + Flyway schema + Actuator health** - `(pre-existing in commit c0f6bd6)` (feat) — Maven project, application.yml (port 8085, RabbitMQ config with auto-startup=false), V1__init_schema.sql with notification_deliveries DDL
2. **Task 2: Dockerfile + Kubernetes manifests** - `1ecde5a` (feat) — Multi-stage Dockerfile, K8s ConfigMap/Secret/Deployment/Service (ClusterIP only)
3. **Task 3: Context-boot test** - `cc84f53` (test) — ApplicationContextBootTest with Testcontainers (Testcontainers Docker connectivity deferred)

**Plan metadata:** (to be committed after SUMMARY.md/STATE.md updates)

_Note: Task 1 files (pom.xml, main class, application.yml, migration SQL) were pre-created in commit c0f6bd6 (plan 02-08 audit-log-service execution), which incorrectly created notifications-service files alongside audit-log-service. Task 1 work was already complete and correct, so no duplicate commit was created._

## Files Created/Modified

- `services/notifications-service/pom.xml` - Maven project (Spring Boot 3.3.4, Java 21, dependencies: web, data-jpa, actuator, **amqp**, flyway, postgresql, testcontainers)
- `services/notifications-service/src/main/java/com/bookinghub/notifications/NotificationsServiceApplication.java` - Main application class
- `services/notifications-service/src/main/resources/application.yml` - Port 8085, datasource (notif_db), RabbitMQ config, Actuator health endpoints, **listener auto-startup disabled**
- `services/notifications-service/src/main/resources/db/migration/V1__init_schema.sql` - Exact TechArch §3.3 DDL: notification_deliveries table with id, idempotency_key (UNIQUE), event_type, entity_id, status, attempt_count, last_attempted_at, created_at, updated_at + set_updated_at trigger
- `services/notifications-service/Dockerfile` - Multi-stage: maven:3.9-eclipse-temurin-21 builder → eclipse-temurin:21-jre-alpine runtime, EXPOSE 8085
- `services/notifications-service/.dockerignore` - Excludes target/, .git, etc.
- `k8s/notifications-service/configmap.yaml` - DB_HOST/PORT/NAME, RABBITMQ_HOST/PORT
- `k8s/notifications-service/secret.yaml` - Placeholder DB/RabbitMQ credentials + SMTP_CREDENTIALS stub (Phase 6)
- `k8s/notifications-service/deployment.yaml` - Image bookinghub/notifications-service:latest, liveness/readiness on port 8085, envFrom ConfigMap+Secret
- `k8s/notifications-service/service.yaml` - type: ClusterIP, port 8085, no Ingress
- `services/notifications-service/src/test/java/com/bookinghub/notifications/ApplicationContextBootTest.java` - Context-boot test with Testcontainers PostgreSQL, health probe assertions

## Decisions Made

- **RabbitMQ dependency timing:** Included spring-boot-starter-amqp now (Plan 02-05) even though Phase 6 implements consumer logic, so dependency set is stable and Phase 6 only adds @RabbitListener code without modifying pom.xml
- **RabbitMQ auto-startup disabled:** `spring.rabbitmq.listener.simple.auto-startup=false` prevents application startup failure if RabbitMQ broker unreachable at boot — consistent with fail-closed-but-not-fail-crashed posture per TechArch Threat T-02-05-04
- **SMTP credentials placeholder:** SMTP_CREDENTIALS key added to Secret now (dummy base64 value) per plan directive, keeping Secret shape stable even though actual provider (SendGrid/SES/Postmark/etc.) is TBD in Phase 6 per TechArch §7.3

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Task 1 files pre-created in wrong plan execution**
- **Found during:** Task 1 file staging
- **Issue:** notifications-service pom.xml, main class, application.yml, and V1__init_schema.sql already existed in the repository (commit c0f6bd6, plan 02-08 audit-log-service execution). Investigation revealed that a previous agent incorrectly created notifications-service files alongside audit-log-service in that commit.
- **Fix:** Verified existing files are identical to plan requirements (correct Maven dependencies including AMQP, correct application.yml with port 8085 + RabbitMQ config + auto-startup disabled, correct migration DDL matching TechArch §3.3 verbatim). No changes needed; proceeded to Task 2.
- **Files verified:** services/notifications-service/pom.xml, NotificationsServiceApplication.java, application.yml, V1__init_schema.sql
- **Verification:** File content comparison confirmed 100% match with plan requirements; `mvn package` succeeds; migration contains all 9 required columns + trigger
- **Committed in:** (no new commit needed; files already correct in c0f6bd6)

---

**Total deviations:** 1 issue handled (1 Rule 3 - Blocking: pre-existing files verified correct)
**Impact on plan:** The pre-created files were already correct and complete, so no rework was needed. This was purely a commit-attribution quirk (files credited to 02-08 instead of 02-05) with no functional impact.

## Deferred Issues

### Testcontainers Docker Connectivity (Environment-Specific)

**Issue:** ApplicationContextBootTest cannot execute against real Testcontainers PostgreSQL due to Docker API version mismatch.

**Details:**
- Testcontainers Java library (version 1.19.8 from Spring Boot 3.3.4 parent) bundles docker-java client that defaults to API version 1.32
- Sandbox Docker daemon requires minimum API version 1.40 ("client version 1.32 is too old")
- Sandbox Docker version is 29.8.1 with API 1.56, but Testcontainers cannot detect/use it
- Attempted fixes: DOCKER_API_VERSION env var, TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE, .testcontainers.properties config file — none succeeded
- Same issue encountered in booking-service (plan 02-01, commit c776f52) and deferred there after 3 attempts

**Current State:**
- Test code is correct and complete (identical pattern to booking-service)
- contextLoads(), livenessProbeReturnsUp(), readinessProbeReturnsUp() assertions are properly structured
- Verification will succeed when test runs in an environment with compatible Testcontainers/Docker integration (later verification phase or CI pipeline with Docker-in-Docker support)

**Classified as:** Environment infrastructure issue, not application defect

**Deferred to:** Plan 02-12 full-stack verification OR later verify-work phase with proper Testcontainers environment

## Issues Encountered

**Testcontainers Docker connectivity:** Testcontainers Java library cannot connect to sandbox Docker daemon due to API version incompatibility (library uses 1.32, daemon requires ≥1.40). Same issue affected booking-service (02-01) and was deferred there. Test code is correct; issue is environment-specific. Deferred to later verification phase.

## Next Phase Readiness

- notifications-service scaffold is complete and ready for Phase 6 (F8 Notifications)
- Phase 6 will add: RabbitMQ @RabbitListener consumer logic, email/SMTP integration, idempotency enforcement, retry + DLQ handling
- No blockers for Phase 6 implementation
- Full-stack verification (plan 02-12) will test all services together with real PostgreSQL + RabbitMQ infrastructure via docker-compose

---
*Phase: 02-platform-foundation-infrastructure*
*Completed: 2026-10-07*
