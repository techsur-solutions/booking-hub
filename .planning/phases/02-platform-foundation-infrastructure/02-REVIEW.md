---
phase: 2
status: issues_found
blockers: 0
warnings: 1
files_reviewed: 17
files_reviewed_list:
  - infra/keycloak/README.md
  - infra/keycloak/realm-export.json
  - infra/postgres/audit-grants.sh
  - k8s/audit-log-service/configmap.yaml
  - k8s/audit-log-service/secret.yaml
  - services/api-gateway/src/main/java/com/bookinghub/gateway/config/RateLimiterConfig.java
  - services/api-gateway/src/main/java/com/bookinghub/gateway/config/SecurityConfig.java
  - services/api-gateway/src/main/resources/application.yml
  - services/api-gateway/src/test/java/com/bookinghub/gateway/GatewayFailClosedTest.java
  - services/api-gateway/src/test/java/com/bookinghub/gateway/GatewaySecurityTest.java
  - services/api-gateway/src/test/java/com/bookinghub/gateway/config/TestSecurityConfig.java
  - services/audit-log-service/src/main/resources/application.yml
  - services/booking-service/src/test/java/com/bookinghub/booking/ApplicationContextBootTest.java
  - services/feeds-service/src/main/resources/application.yml
  - services/feeds-service/src/test/java/com/bookinghub/feeds/ApplicationContextBootTest.java
  - services/notifications-service/src/main/resources/application.yml
  - services/settings-service/src/main/resources/application.yml
reviewed_at: 2026-10-07T12:30:00Z
iteration: 2
---

# Phase 2 Code Review - Iteration 2

## BLOCKERs

None. All previous blockers have been verified as fixed:
- **B1 (Rate limiter refill):** Correctly fixed at RateLimiterConfig.java:78 with `lastRefillTimestamp = now - (timePassed % refillIntervalMillis)`
- **B2 (Audit grants error handling):** Correctly fixed with `set -e` and verification query at audit-grants.sh:16-23
- **B3 (Missing Flyway credentials in K8s):** Correctly fixed in k8s/audit-log-service/configmap.yaml:12-14 and secret.yaml:14-15

## WARNINGs

### W1: Rate limiter cleanup has non-volatile field read (benign race condition)
- **File:** services/api-gateway/src/main/java/com/bookinghub/gateway/config/RateLimiterConfig.java:47
- **Category:** bug
- **Evidence:** The `lastAccessTime` field at line 47 is not declared `volatile`, but is written inside a synchronized method (line 59 in `tryConsume()`) and read outside synchronization (line 68 in `getLastAccessTime()`, called by cleanup thread at line 34). Without volatile or synchronization on read, the cleanup thread may see a stale value of `lastAccessTime` due to lack of happens-before relationship. This can cause two edge-case behaviors: (1) An active bucket might be removed prematurely if cleanup sees a stale old timestamp, causing the bucket to be recreated on next request (performance degradation but not correctness failure), or (2) An idle bucket might be retained longer if cleanup sees a stale recent timestamp (memory leak mitigation delayed). The impact is minor because cleanup runs every 10 minutes with a 1-hour idle threshold, so visibility lag is unlikely to span the full threshold window. The system remains functionally correct, but there's a potential for transient inefficiency.
- **Fix direction:** Declare `lastAccessTime` as `volatile` at line 47 to ensure visibility across threads, OR make `getLastAccessTime()` synchronized to establish happens-before relationship.

## Re-Review: Verification of Previous Findings

### Previously Reported BLOCKERs (All Fixed)

**B1 - Rate limiter refill timestamp:** FIXED ✓  
Verified at RateLimiterConfig.java:78. The refill logic now correctly advances `lastRefillTimestamp` by consumed intervals using `now - (timePassed % refillIntervalMillis)`, which properly tracks partial intervals and maintains consistent token refill rate. Tested logic with example: 120 tokens/min (500ms interval), after 1200ms adds 2 tokens and sets timestamp to 1000ms, next call at 1600ms correctly sees 600ms elapsed. Mathematical correctness confirmed.

**B2 - Audit grants script error handling:** FIXED ✓  
Verified at audit-grants.sh:2 (`set -e` present) and lines 16-23 (verification query). The script now fails fast on any psql error and verifies the immutability constraint by checking `has_table_privilege('audit_svc', 'audit_log_entries', 'UPDATE')` returns `f`. Exit code 1 on verification failure ensures docker-compose will not silently proceed with insecure grants. The original review noted `set -e` was already present (added in initial implementation), fix correctly added the missing verification query.

**B3 - Missing Flyway credentials in K8s:** FIXED ✓  
Verified at k8s/audit-log-service/configmap.yaml:12-14 (FLYWAY_DB_HOST, FLYWAY_DB_PORT, FLYWAY_DB_NAME) and k8s/audit-log-service/secret.yaml:14-15 (FLYWAY_DB_USER, FLYWAY_DB_PASSWORD base64 encoded). These match the application.yml structure at lines 14-16 which references `${FLYWAY_DB_HOST:${DB_HOST:localhost}}` pattern. K8s deployment will now correctly use `audit_migrator` role for schema migrations and `audit_svc` role for runtime queries, maintaining the two-role security split.

### Previously Reported WARNINGs (All Addressed)

**W1 - Healthcheck path inconsistency:** FIXED ✓  
Verified docker-compose.yml lines 80, 97, 114, 137, 160, 183, 206, 234, 282 all use `/actuator/health/readiness` matching K8s deployment manifests. Consistency across environments achieved.

**W2 - Missing CORS configuration:** FIXED ✓  
Verified at api-gateway/src/main/resources/application.yml:9-25. Global CORS configuration added with `allowedOrigins: ["http://localhost:3000", "http://${FRONTEND_HOST:localhost}:${FRONTEND_PORT:3000}"]`, `allowCredentials: true`, and appropriate methods/headers. Environment variables default to localhost:3000 which matches docker-compose frontend port mapping (3000:80). The frontend container runs nginx on internal port 80 but is exposed to host on 3000, so browser Origin header will be `http://localhost:3000`, correctly matching CORS config. No CORS conflicts in SecurityConfig (line 35 only disables CSRF, no cors() call present).

**W3 - Rate limiter memory leak:** FIXED (with new W1 caveat) ✓  
Verified at RateLimiterConfig.java:30-36. Scheduled cleanup task added with `@Scheduled(fixedRate = 600_000)` (10 minutes) removing buckets idle for >1 hour. Uses `ConcurrentHashMap.entrySet().removeIf()` which is atomic and safe for concurrent modification. `@EnableScheduling` present at line 11. Cleanup logic is correct but introduced the non-volatile field issue reported as new W1.

**W4 - RabbitMQ auto-startup documentation:** FIXED ✓  
Verified inline comments added to 4 services: audit-log-service/application.yml:28-30, feeds-service/application.yml:33-35, notifications-service/application.yml:25-27, settings-service/application.yml:25-27. Comments document deferral to Phase 3 and explain that enabling without listener beans will cause startup failure. Original review stated 5 services, but only 4 services actually have `spring.rabbitmq` config (booking-service and users-permissions-service do not have rabbitmq config yet, though docker-compose provides env vars for forward compatibility). Fix correctly identified and documented all 4 services with rabbitmq configuration.

**W5 - Feeds public route authorization responsibility:** FIXED ✓  
Verified at SecurityConfig.java:39-40. Inline comment added explaining feeds-service is responsible for all authorization under `/feeds/**` and noting potential for admin sub-paths in future. Clarifies gateway-level authentication is intentionally bypassed.

**W6 - Keycloak placeholder secrets:** ADDRESSED ✓  
Verified at infra/keycloak/README.md:38-64. Documentation now clearly distinguishes bearer-only clients (9 services) which don't actively use secrets in the current JWT validation architecture, from service-account client (`userperm-admin-client`) which requires a real secret for client credentials flow. README provides concrete guidance for production secret generation and Kubernetes secret management. Placeholders remain in realm-export.json per documented local-dev-only status.

**W7 - PostgreSQL port exposure:** ADDRESSED (documentation enhanced) ✓  
Verified at docker-compose.yml:20-26. Port mapping retained but comments enhanced to document security tradeoff (direct database access bypassing application logic) and provide alternatives: `docker compose exec postgres psql` or separate docker-compose.override.yml for dev-only exposure. Original WARNING noted weak justification ("infrastructure not business service"); fix strengthens documentation acknowledging the tradeoff without claiming it's acceptable, and provides secure alternatives.

## Cross-file seams checked (fixer-touched files only)

- **Rate limiter cleanup → TokenBucket access:** OK - `ConcurrentHashMap.removeIf()` is atomic, `computeIfAbsent()` is atomic, only issue is non-volatile field read (reported as W1)
- **Audit grants script → audit-log Flyway migration:** OK - script waits for audit-log-service healthy (Flyway completed), applies grants to `audit_log_entries` table created by V1__init_schema.sql
- **K8s audit-log Flyway env vars → application.yml:** OK - configmap provides FLYWAY_DB_HOST/PORT/NAME with fallback to DB_* vars, secret provides FLYWAY_DB_USER/PASSWORD, matching application.yml:14-16 structure
- **CORS allowedOrigins → docker-compose frontend port:** OK - defaults to localhost:3000, docker-compose exposes frontend on 3000:80, browser Origin header matches
- **CORS allowedOrigins → K8s frontend:** DEFERRED - K8s configmap does not provide FRONTEND_HOST/PORT env vars, will default to localhost:3000 which is incorrect for K8s cluster-internal frontend service. However, K8s frontend service does not exist yet (no k8s/frontend/), so this mismatch is expected and will be addressed when K8s frontend deployment is implemented in a future phase. Not a blocker for phase 2 which targets docker-compose as primary deployment.
- **RabbitMQ auto-startup comments → docker-compose env vars:** OK - 6 services receive RABBITMQ_* env vars in docker-compose (booking, users-permissions, notifications, feeds, settings, audit-log), but only 4 have `spring.rabbitmq` config with auto-startup disabled (notifications, feeds, settings, audit-log). The 2 services without rabbitmq config (booking, users-permissions) receive env vars for forward compatibility with Phase 3 implementation.

## New Issues Introduced by Fixes

None. The rate limiter cleanup fix introduced a minor non-volatile field visibility issue (W1) which is a refinement of the memory leak fix, not a regression. All other fixes are correct without introducing new defects.
