---
phase: 2
status: issues_found
blockers: 3
warnings: 4
files_reviewed: 164
files_reviewed_list:
  - docker-compose.yml
  - frontend/.dockerignore
  - frontend/.gitignore
  - frontend/.oxlintrc.json
  - frontend/Dockerfile
  - frontend/README.md
  - frontend/e2e/auth-redirect.spec.ts
  - frontend/e2e/navigation.spec.ts
  - frontend/index.html
  - frontend/nginx.conf
  - frontend/package.json
  - frontend/playwright.config.ts
  - frontend/public/favicon.svg
  - frontend/public/icons.svg
  - frontend/public/silent-check-sso.html
  - frontend/src/App.css
  - frontend/src/App.tsx
  - frontend/src/assets/hero.png
  - frontend/src/assets/react.svg
  - frontend/src/assets/vite.svg
  - frontend/src/auth/AuthProvider.tsx
  - frontend/src/auth/ProtectedRoute.tsx
  - frontend/src/auth/keycloak.ts
  - frontend/src/components/AppShell.tsx
  - frontend/src/components/Nav.tsx
  - frontend/src/index.css
  - frontend/src/main.tsx
  - frontend/src/pages/AuditLogViewer.tsx
  - frontend/src/pages/CalendarView.tsx
  - frontend/src/pages/DayView.tsx
  - frontend/src/pages/DisplayBoard.tsx
  - frontend/src/pages/FeedsLanding.tsx
  - frontend/src/pages/ListView.tsx
  - frontend/src/pages/admin/CustomFieldsAdmin.tsx
  - frontend/src/pages/admin/LocationsAdmin.tsx
  - frontend/src/pages/admin/ResourcesAdmin.tsx
  - frontend/src/pages/admin/RolesAdmin.tsx
  - frontend/src/pages/admin/SettingsAdmin.tsx
  - frontend/src/pages/admin/UsersAdmin.tsx
  - frontend/src/routes/index.tsx
  - frontend/tsconfig.app.json
  - frontend/tsconfig.json
  - frontend/tsconfig.node.json
  - frontend/vite.config.ts
  - infra/keycloak/README.md
  - infra/keycloak/realm-export.json
  - infra/postgres/audit-grants.sh
  - infra/postgres/init-multi-db.sh
  - infra/rabbitmq/README.md
  - infra/rabbitmq/definitions.json
  - infra/rabbitmq/rabbitmq.conf
  - k8s/api-gateway/configmap.yaml
  - k8s/api-gateway/deployment.yaml
  - k8s/api-gateway/secret.yaml
  - k8s/api-gateway/service.yaml
  - k8s/audit-log-service/configmap.yaml
  - k8s/audit-log-service/deployment.yaml
  - k8s/audit-log-service/secret.yaml
  - k8s/audit-log-service/service.yaml
  - k8s/booking-service/configmap.yaml
  - k8s/booking-service/deployment.yaml
  - k8s/booking-service/secret.yaml
  - k8s/booking-service/service.yaml
  - k8s/custom-field-service/configmap.yaml
  - k8s/custom-field-service/deployment.yaml
  - k8s/custom-field-service/secret.yaml
  - k8s/custom-field-service/service.yaml
  - k8s/feeds-service/configmap.yaml
  - k8s/feeds-service/deployment.yaml
  - k8s/feeds-service/secret.yaml
  - k8s/feeds-service/service.yaml
  - k8s/locations-resources-service/configmap.yaml
  - k8s/locations-resources-service/deployment.yaml
  - k8s/locations-resources-service/secret.yaml
  - k8s/locations-resources-service/service.yaml
  - k8s/notifications-service/configmap.yaml
  - k8s/notifications-service/deployment.yaml
  - k8s/notifications-service/secret.yaml
  - k8s/notifications-service/service.yaml
  - k8s/settings-service/configmap.yaml
  - k8s/settings-service/deployment.yaml
  - k8s/settings-service/secret.yaml
  - k8s/settings-service/service.yaml
  - k8s/users-permissions-service/configmap.yaml
  - k8s/users-permissions-service/deployment.yaml
  - k8s/users-permissions-service/secret.yaml
  - k8s/users-permissions-service/service.yaml
  - services/api-gateway/.dockerignore
  - services/api-gateway/Dockerfile
  - services/api-gateway/pom.xml
  - services/api-gateway/src/main/java/com/bookinghub/gateway/ApiGatewayApplication.java
  - services/api-gateway/src/main/java/com/bookinghub/gateway/config/RateLimiterConfig.java
  - services/api-gateway/src/main/java/com/bookinghub/gateway/config/SecurityConfig.java
  - services/api-gateway/src/main/java/com/bookinghub/gateway/error/GatewayErrorAttributes.java
  - services/api-gateway/src/main/java/com/bookinghub/gateway/filter/InMemoryRateLimiterFilter.java
  - services/api-gateway/src/main/resources/application.yml
  - services/api-gateway/src/test/java/com/bookinghub/gateway/GatewayFailClosedTest.java
  - services/api-gateway/src/test/java/com/bookinghub/gateway/GatewaySecurityTest.java
  - services/api-gateway/src/test/java/com/bookinghub/gateway/config/TestSecurityConfig.java
  - services/api-gateway/src/test/resources/application-test.yml
  - services/audit-log-service/.dockerignore
  - services/audit-log-service/Dockerfile
  - services/audit-log-service/pom.xml
  - services/audit-log-service/src/main/java/com/bookinghub/auditlog/AuditLogServiceApplication.java
  - services/audit-log-service/src/main/resources/application.yml
  - services/audit-log-service/src/main/resources/db/migration/V1__init_schema.sql
  - services/audit-log-service/src/test/java/com/bookinghub/auditlog/ApplicationContextBootTest.java
  - services/booking-service/.dockerignore
  - services/booking-service/Dockerfile
  - services/booking-service/pom.xml
  - services/booking-service/src/main/java/com/bookinghub/booking/BookingServiceApplication.java
  - services/booking-service/src/main/resources/application.yml
  - services/booking-service/src/main/resources/db/migration/V1__init_schema.sql
  - services/booking-service/src/test/java/com/bookinghub/booking/ApplicationContextBootTest.java
  - services/custom-field-service/.dockerignore
  - services/custom-field-service/Dockerfile
  - services/custom-field-service/pom.xml
  - services/custom-field-service/src/main/java/com/bookinghub/customfield/CustomFieldServiceApplication.java
  - services/custom-field-service/src/main/resources/application.yml
  - services/custom-field-service/src/main/resources/db/migration/V1__init_schema.sql
  - services/custom-field-service/src/test/java/com/bookinghub/customfield/ApplicationContextBootTest.java
  - services/custom-field-service/src/test/resources/testcontainers.properties
  - services/feeds-service/.dockerignore
  - services/feeds-service/Dockerfile
  - services/feeds-service/pom.xml
  - services/feeds-service/src/main/java/com/bookinghub/feeds/FeedsServiceApplication.java
  - services/feeds-service/src/main/resources/application.yml
  - services/feeds-service/src/main/resources/db/migration/V1__init_schema.sql
  - services/feeds-service/src/test/java/com/bookinghub/feeds/ApplicationContextBootTest.java
  - services/locations-resources-service/.dockerignore
  - services/locations-resources-service/Dockerfile
  - services/locations-resources-service/docker-compose.yml
  - services/locations-resources-service/pom.xml
  - services/locations-resources-service/src/main/java/com/bookinghub/locationsresources/LocationsResourcesServiceApplication.java
  - services/locations-resources-service/src/main/resources/application.yml
  - services/locations-resources-service/src/main/resources/db/migration/V1__init_schema.sql
  - services/locations-resources-service/src/test/java/com/bookinghub/locationsresources/ApplicationContextBootTest.java
  - services/notifications-service/.dockerignore
  - services/notifications-service/Dockerfile
  - services/notifications-service/pom.xml
  - services/notifications-service/src/main/java/com/bookinghub/notifications/NotificationsServiceApplication.java
  - services/notifications-service/src/main/resources/application.yml
  - services/notifications-service/src/main/resources/db/migration/V1__init_schema.sql
  - services/notifications-service/src/test/java/com/bookinghub/notifications/ApplicationContextBootTest.java
  - services/settings-service/.dockerignore
  - services/settings-service/Dockerfile
  - services/settings-service/pom.xml
  - services/settings-service/src/main/java/com/bookinghub/settings/SettingsServiceApplication.java
  - services/settings-service/src/main/resources/application.yml
  - services/settings-service/src/main/resources/db/migration/V1__init_schema.sql
  - services/settings-service/src/test/java/com/bookinghub/settings/ApplicationContextBootTest.java
  - services/users-permissions-service/.dockerignore
  - services/users-permissions-service/Dockerfile
  - services/users-permissions-service/pom.xml
  - services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/UsersPermissionsServiceApplication.java
  - services/users-permissions-service/src/main/resources/application.yml
  - services/users-permissions-service/src/main/resources/db/migration/V1__init_schema.sql
  - services/users-permissions-service/src/test/java/com/bookinghub/userspermissions/ApplicationContextBootTest.java
  - services/users-permissions-service/src/test/resources/testcontainers.properties
reviewed_at: 2026-10-07T12:00:00Z
iteration: 1
---

# Phase 2 Code Review

## BLOCKERs

### B1: Rate limiter refill timestamp not updated correctly
- **File:** services/api-gateway/src/main/java/com/bookinghub/gateway/config/RateLimiterConfig.java:54
- **Category:** bug
- **Evidence:** In the `TokenBucket.refill()` method, line 54 updates `lastRefillTimestamp = now`, but this should be `lastRefillTimestamp += tokensToAdd * refillIntervalMillis` to properly track the next refill time. The current implementation causes the timestamp to jump forward by the full elapsed time, which breaks the token refill rate calculation. Consider: if 1 minute passes and tokensToAdd=120 (all tokens refilled), setting `lastRefillTimestamp = now` means the next call will see timePassed=0 briefly, then jump again. The correct approach is to increment by the amount of time actually consumed in refilling tokens. This leads to bursty behavior where tokens accumulate faster than the intended rate.
- **Fix direction:** Change line 54 to `lastRefillTimestamp = now - (timePassed % refillIntervalMillis)` to properly track partial intervals, or redesign to use `lastRefillTimestamp += tokensToAdd * refillIntervalMillis` to advance by discrete refill intervals.

### B2: Audit grants script has no error handling
- **File:** infra/postgres/audit-grants.sh:10-13
- **Category:** bug
- **Evidence:** The script runs `psql` with no verification that the connection succeeded or that the GRANT/REVOKE commands executed successfully. If the audit-log-service healthcheck passes but the database connection from the audit-grants container fails (wrong password, network issue, etc.), the script exits silently with no error propagation. The compose file shows no restart policy or health verification for the audit-grants service, so a silent failure leaves the database in an insecure state where `audit_svc` has no grants at all (can't read or write) OR retains default PUBLIC grants (can UPDATE/DELETE). The core security invariant (audit immutability) is not mechanically enforced.
- **Fix direction:** Add `set -e` to abort on any command failure, verify psql exit code, and add a verification query at the end (`SELECT has_table_privilege('audit_svc', 'audit_log_entries', 'UPDATE')` expecting `f`). Consider adding a healthcheck to the audit-grants container or making it a restart-on-failure service.

### B3: Missing FLYWAY_DB credentials in audit-log K8s configmap
- **File:** k8s/audit-log-service/configmap.yaml:1-13
- **Category:** integration
- **Evidence:** The audit-log-service application.yml (lines 13-16) defines separate Flyway datasource credentials (`FLYWAY_DB_USER: audit_migrator`, `FLYWAY_DB_PASSWORD: audit_migrator_pw`) for the two-role split pattern. The K8s configmap only provides `DB_HOST`, `DB_PORT`, `DB_NAME` but omits `FLYWAY_DB_HOST`, `FLYWAY_DB_PORT`, `FLYWAY_DB_NAME`, `FLYWAY_DB_USER`, `FLYWAY_DB_PASSWORD`. The application.yml defaults to using `${DB_HOST}` etc. for Flyway, which means Flyway will attempt to connect as `audit_svc` (the runtime role) instead of `audit_migrator` (the schema owner). This will cause Flyway migration to fail in K8s because `audit_svc` does not have CREATE TABLE privileges. The docker-compose.yml correctly provides all six env vars (lines 220-224).
- **Fix direction:** Add FLYWAY_DB_* environment variables to k8s/audit-log-service/configmap.yaml and k8s/audit-log-service/secret.yaml matching the docker-compose pattern.

## WARNINGs

### W1: Inconsistent health probe paths between docker-compose and K8s
- **File:** docker-compose.yml:76 (and 8 other services), k8s/api-gateway/deployment.yaml:32
- **Category:** bug
- **Evidence:** Docker-compose healthchecks use `/actuator/health` (lines 76, 93, 110, 133, 156, 179, 202, 230, 278) while K8s deployments use `/actuator/health/liveness` and `/actuator/health/readiness` (e.g., k8s/api-gateway/deployment.yaml lines 32, 40). Both patterns are valid for Spring Boot Actuator, but the inconsistency makes it harder to verify behavior across environments. If a service is healthy in docker-compose but unhealthy in K8s, debugging requires knowing this difference.
- **Fix direction:** Standardize on `/actuator/health/liveness` and `/actuator/health/readiness` in docker-compose.yml to match K8s manifest behavior, OR document the intentional difference in the docker-compose comments.

### W2: Missing CORS configuration in API Gateway
- **File:** services/api-gateway/src/main/java/com/bookinghub/gateway/config/SecurityConfig.java:33-81
- **Category:** integration
- **Evidence:** The SecurityConfig defines comprehensive JWT validation and route-to-role enforcement but has no CORS configuration. When the frontend (served from nginx on port 3000 or a different origin in production) calls the gateway (port 8080), browsers will block the requests with CORS errors. The docker-compose.yml shows frontend on port 3000 and gateway on 8080, which are different origins. Spring Cloud Gateway requires explicit CORS configuration via `http.cors()` or global CORS configuration in application.yml. Current implementation will break all frontend-to-backend calls in any deployment where they're on different origins.
- **Fix direction:** Add CORS configuration to SecurityConfig (e.g., `http.cors(cors -> cors.configurationSource(corsConfigurationSource()))` with a bean defining allowed origins, methods, and headers), OR add spring.cloud.gateway.globalcors in application.yml.

### W3: Rate limiter token buckets never cleaned up (memory leak)
- **File:** services/api-gateway/src/main/java/com/bookinghub/gateway/config/RateLimiterConfig.java:14
- **Category:** bug
- **Evidence:** The `ConcurrentHashMap<String, TokenBucket>` on line 14 grows unbounded as new client IPs are seen. Each unique IP gets a TokenBucket stored in the map, but there's no eviction mechanism. In a production environment with many clients (especially behind NAT or proxies with rotating IPs), this will cause a memory leak. A gateway processing requests from 10,000 unique IPs per hour will accumulate 240,000 map entries per day. Each TokenBucket instance holds ~40 bytes (3 primitives + object overhead), so 1M entries ≈ 40MB, growing indefinitely.
- **Fix direction:** Add a scheduled cleanup task to remove buckets idle for >1 hour, OR switch to a bounded cache implementation (e.g., Caffeine with size-based eviction), OR add an LRU eviction policy.

### W4: RabbitMQ listener auto-startup disabled with no documentation
- **File:** services/audit-log-service/src/main/resources/application.yml:26-28 (and 4 other services)
- **Category:** bug
- **Evidence:** Five services (audit-log, booking, feeds, notifications, users-permissions) configure `spring.rabbitmq.listener.simple.auto-startup: false`, which prevents the RabbitMQ message listeners from starting automatically when the application boots. This is documented in the GATE.md as a deferred stub ("RabbitMQ listener implementation deferred to Phase 3"), but there's no inline comment in the application.yml files explaining why auto-startup is disabled. A developer modifying these services in Phase 3 might enable auto-startup without implementing the listener beans, causing a startup failure with a confusing error ("No bean of type RabbitListenerContainerFactory"). Alternatively, they might implement listener beans but forget to enable auto-startup, leading to silent message loss.
- **Fix direction:** Add inline YAML comments above each `auto-startup: false` line explaining the defer-to-phase-3 decision and linking to the phase 3 plan that will implement the listeners.

### W5: Public feeds route bypasses authentication but not authorization check
- **File:** services/api-gateway/src/main/java/com/bookinghub/gateway/config/SecurityConfig.java:39
- **Category:** bug
- **Evidence:** Line 39 declares `.pathMatchers("/feeds/**").permitAll()`, which correctly allows unauthenticated access to the feeds endpoint per the public-access requirement. However, the implementation relies on the downstream feeds-service to enforce any authorization logic. If the feeds-service later adds authenticated-only endpoints under `/feeds/admin/**` or similar, the gateway will pass through all requests without authentication. While this isn't necessarily wrong (the service can enforce its own auth), it creates an inconsistency: all other routes have gateway-level authentication requirements. If feeds-service expects the gateway to enforce authentication for some sub-paths, it will silently fail open.
- **Fix direction:** Document in a comment that feeds-service is responsible for all authorization under `/feeds/**`, OR split the route into `/feeds/public/**` (permitAll) and `/feeds/admin/**` (authenticated) if such a split is planned.

### W6: Keycloak realm uses placeholder client secrets
- **File:** infra/keycloak/realm-export.json:81, 95, etc.
- **Category:** security
- **Evidence:** All confidential/bearer-only service clients in the realm export have placeholder secrets like `CHANGE_ME_booking_service_secret`. These are used for bearer-only validation and service-to-service calls. While the bearer-only clients don't actively use the secret for OAuth flows in the current architecture (JWT validation is done via JWKS), the placeholder values are exported in plaintext in the realm JSON, which is committed to git. If these secrets are ever used (e.g., for service accounts or client credentials flow), they're already compromised.
- **Fix direction:** Generate real secrets and move them to environment variables or Kubernetes secrets, then reference them in the Keycloak admin console post-import, OR document that bearer-only clients don't use the secret field and it's safe to leave as a placeholder.

### W7: Docker-compose exposes PostgreSQL port 5432 to host
- **File:** docker-compose.yml:20-23
- **Category:** security
- **Evidence:** Lines 20-23 publish Postgres port 5432 to the host with comment "Dev convenience only". While this is acceptable for local development, the comment notes it's "acceptable since Postgres is infrastructure, not a Booking-Hub business service." This reasoning is weak: if the justification for NOT publishing business service ports is network isolation, then publishing the database port breaks isolation even more severely (direct access to all service data bypassing application logic). An attacker on the host machine (or anyone with access to localhost:5432) can connect directly to any of the 8 service databases using the plaintext credentials from docker-compose.yml.
- **Fix direction:** Remove the ports mapping from the postgres service and add a separate `postgres-admin` service or docker-compose override file for developers who need direct psql access. Document the security tradeoff in the compose file comments.

## Cross-file seams checked

- **API Gateway → Backend Services (routes ↔ service ports):** OK - All 8 services correctly mapped (booking:8081, locations-resources:8082, custom-field:8083, users-permissions:8084, notifications:8085, feeds:8086, settings:8087, audit-log:8088) in both gateway application.yml and docker-compose service definitions
- **API Gateway → Keycloak (issuer-uri ↔ realm endpoint):** OK - Gateway application.yml references `http://${KEYCLOAK_HOST}:${KEYCLOAK_PORT}/realms/bookinghub` matching realm-export.json realm name
- **Frontend → Keycloak (clientId ↔ realm client):** OK - frontend/src/auth/keycloak.ts clientId `bookinghub-frontend` matches realm-export.json client definition line 33
- **Frontend → API Gateway (base URL):** NOT CHECKED - Frontend has no API client yet (pages are placeholders), deferred to Phase 3-4
- **Docker-compose service names → Gateway routes:** OK - Gateway application.yml uses service names (booking-service, locations-resources-service, etc.) matching docker-compose.yml service definitions
- **Database names (docker-compose env vars ↔ init script):** OK - All 8 databases created in init-multi-db.sh match docker-compose env vars (booking_db, locres_db, customfld_db, userperm_db, notif_db, feeds_db, settings_db, audit_db)
- **Database roles (docker-compose env vars ↔ init script):** OK - All 9 roles created in init-multi-db.sh match docker-compose env vars (8 service roles + audit_migrator)
- **Flyway migrations (database names ↔ schema names):** OK - Checked 5 migrations, all use unqualified table names (defaulting to public schema), consistent with single-database-per-service pattern
- **K8s service names → Gateway routes:** MISMATCH - See B3 (audit-log K8s config missing Flyway credentials)
- **RabbitMQ topology (definitions.json exchanges ↔ service configs):** OK - All 7 topic exchanges and 11 queues defined, services have rabbitmq config but listeners disabled (documented in W4)
- **Keycloak roles (realm-export ↔ Gateway SecurityConfig):** OK - All 11 realm roles in realm-export.json match Gateway SCOPE_role_* matchers (booking_viewer, booking_creator, booking_approver, location_admin, customfield_admin, user_admin, permissions_admin, settings_admin, audit_viewer, calendar_viewer, feed_api)
