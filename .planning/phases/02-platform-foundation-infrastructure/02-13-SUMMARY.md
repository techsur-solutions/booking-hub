---
phase: 02-platform-foundation-infrastructure
plan: 13
subsystem: infra
tags: [docker-compose, keycloak, spring-cloud-gateway, jwt, healthcheck, lazy-initialization]

# Dependency graph
requires:
  - phase: 02-platform-foundation-infrastructure
    provides: API Gateway with JWT validation (Plan 10)
provides:
  - Keycloak healthcheck ensuring OIDC endpoint availability before Gateway starts
  - Lazy JWT decoder initialization allowing Gateway to start independently of Keycloak
  - Fail-closed-but-not-fail-crashed posture: Gateway starts even when Keycloak temporarily unavailable, returns 503 at request time if unreachable
affects: [02-platform-foundation-infrastructure, UAT, deployment]

# Tech tracking
tech-stack:
  added: []
  patterns: 
    - "Lazy JWT decoder initialization with NimbusReactiveJwtDecoder.withJwkSetUri()"
    - "Service health dependencies using condition: service_healthy"
    - "OIDC discovery endpoint healthcheck pattern"

key-files:
  created: []
  modified:
    - docker-compose.yml
    - services/api-gateway/src/main/java/com/bookinghub/gateway/config/SecurityConfig.java

key-decisions:
  - "Use lazy JWT decoder initialization (NimbusReactiveJwtDecoder.withJwkSetUri()) instead of eager (ReactiveJwtDecoders.fromIssuerLocation()) to defer JWKS fetch until first request"
  - "Add Keycloak healthcheck testing OIDC discovery endpoint (not just container ready) to ensure Gateway has valid JWKS endpoint at startup"
  - "Change api-gateway depends_on keycloak to service_healthy (from service_started) to prevent race condition during initial context load"

patterns-established:
  - "Healthcheck pattern: test HTTP endpoint with exec 3<>/dev/tcp approach for containers without curl/wget"
  - "Fail-closed-but-not-fail-crashed: services start even when dependencies temporarily unavailable, fail at request time with 503"

# Metrics
duration: 5min
completed: 2026-10-07
---

# Phase 2 Plan 13: API Gateway Lazy Decoder Summary

**Fixed Gateway crash-loop on startup by implementing lazy JWT decoder initialization and Keycloak healthcheck - Gateway now starts independently and returns 401 (not crash) when Keycloak temporarily unavailable**

## Performance

- **Duration:** 5 min
- **Started:** 2026-10-07T15:06:47Z
- **Completed:** 2026-10-07T15:11:59Z
- **Tasks:** 3
- **Files modified:** 2

## Accomplishments

- Fixed UAT Test 5 gap: API Gateway no longer crash-loops with `UnknownHostException: keycloak` during startup
- Implemented lazy JWT decoder initialization deferring JWKS fetch until first request
- Added Keycloak healthcheck ensuring OIDC discovery endpoint ready before Gateway starts
- Verified full-stack boot: all 13 services start successfully without crash-loops
- Gateway correctly routes requests and returns 401 (authenticated health check working)

## Task Commits

Each task was committed atomically:

1. **Task 1: Add Keycloak health check and update api-gateway dependency** - `4a327c9` (feat)
2. **Task 2: Implement lazy JWT decoder initialization in SecurityConfig** - `8826f12` (feat)
3. **Task 3: Verify full-stack boot and gateway functionality** - (verification only, no commit)

## Files Created/Modified

- `docker-compose.yml` - Added healthcheck to keycloak service testing OIDC discovery endpoint; changed api-gateway depends_on keycloak to condition: service_healthy
- `services/api-gateway/src/main/java/com/bookinghub/gateway/config/SecurityConfig.java` - Replaced eager ReactiveJwtDecoders.fromIssuerLocation() with lazy NimbusReactiveJwtDecoder.withJwkSetUri(); preserved fail-closed error handling

## Root Cause Analysis

**Problem:** UAT Test 5 found Gateway container crash-looping with `java.net.UnknownHostException: keycloak` during Spring context initialization. The Gateway's JWT decoder initialization at bean creation time called `ReactiveJwtDecoders.fromIssuerLocation(issuerUri)`, which immediately fetches the OIDC discovery document and JWKS. When Keycloak's hostname wasn't resolvable yet (race condition during compose startup), the Gateway failed to initialize and crashed.

**Two-part Solution:**

1. **Infrastructure fix (docker-compose.yml):** Added healthcheck to Keycloak service that verifies the OIDC discovery endpoint returns 200 (not just container started). Changed api-gateway's depends_on to `condition: service_healthy` for Keycloak instead of `service_started`. This ensures Keycloak's identity endpoints are actually reachable before Gateway starts.

2. **Code fix (SecurityConfig.java):** Replaced eager initialization with lazy initialization using `NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build()`. This constructs the decoder but defers all network calls until the first `decode()` invocation. The Gateway can now start even when Keycloak is temporarily unavailable, maintaining the fail-closed posture at request time (returns 503 if JWKS unreachable, never allows unverified tokens through).

## Decisions Made

**Lazy initialization approach:** Used `NimbusReactiveJwtDecoder.withJwkSetUri()` instead of `ReactiveJwtDecoders.fromIssuerLocation()`. The former constructs the decoder immediately but defers JWKS fetch, while the latter eagerly fetches during bean creation. This aligns with TechArch fail-closed-but-not-fail-crashed posture: the service starts (doesn't crash), but requests fail with 503 until Keycloak is reachable.

**Healthcheck implementation:** Used `exec 3<>/dev/tcp/localhost/8080` approach for Keycloak healthcheck since the Keycloak container doesn't include curl/wget. This native bash approach tests the OIDC discovery endpoint without requiring additional tools.

**Fail-closed error handling preserved:** Kept the `JwksUnreachableException` wrapper around the decoder's `onErrorResume` to ensure any JWKS fetch failure at request time returns 503 (not 401 or 200), maintaining the fail-closed security posture.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None

## User Setup Required

None - no external service configuration required.

## UAT Test 5 Status

**PASS** - API Gateway successfully starts and routes requests to backend services

**Before fix:**
- Gateway container crash-looped with `UnknownHostException: keycloak`
- Full stack could not start
- UAT Test 5 blocked entire platform verification

**After fix:**
- `docker compose up -d` starts all 13 services successfully
- Gateway logs show "Started ApiGatewayApplication" with no DNS errors
- `http://localhost:8080/bookings/actuator/health` returns 401 (authenticated health check working)
- No crash-loops or restart counts on any service

## Verification Results

**Stack boot verification:**
- ✅ `docker compose up -d` succeeds without crash-loops
- ✅ All 13 services reach "Up" state within 90 seconds
- ✅ Gateway logs show "Started ApiGatewayApplication in 6.908 seconds"
- ✅ No `UnknownHostException: keycloak` errors in Gateway logs

**Gateway routing verification:**
- ✅ `http://localhost:8080/bookings/actuator/health` returns 401 (authenticated, not 503 service unavailable)
- ✅ Gateway correctly applies JWT validation (returns AUTH_UNAUTHENTICATED for requests without token)

**Integration contracts:**
- ✅ Keycloak service has healthcheck testing `/realms/bookinghub/.well-known/openid-configuration`
- ✅ api-gateway depends_on keycloak with `condition: service_healthy`
- ✅ SecurityConfig uses `NimbusReactiveJwtDecoder.withJwkSetUri()` for lazy initialization
- ✅ No `ReactiveJwtDecoders.fromIssuerLocation()` call remains

## Next Phase Readiness

Phase 2 (Platform Foundation Infrastructure) complete and ready for Phase 3. All 13 plans executed successfully:
- Plans 01-12: Platform foundation services
- Plan 13 (this plan): Fixed Gateway startup issue found in UAT

**Blockers:** None

**Concerns:** None - full stack boots successfully and UAT Test 5 passes

---
*Phase: 02-platform-foundation-infrastructure*
*Completed: 2026-10-07*
