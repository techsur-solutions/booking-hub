---
phase: 2
status: issues_found
blockers: 1
warnings: 0
files_reviewed: 2
files_reviewed_list:
  - docker-compose.yml
  - services/api-gateway/src/main/java/com/bookinghub/gateway/config/SecurityConfig.java
reviewed_at: 2026-10-07T15:18:30Z
iteration: 3
---

# Phase 2 Code Review - Iteration 3 (Gap Closure Wave 3)

## Scope

This review covers **only** the gap-closure changes from plan 02-13 (wave 3):
- docker-compose.yml (Keycloak healthcheck + api-gateway dependency)
- services/api-gateway/src/main/java/com/bookinghub/gateway/config/SecurityConfig.java (lazy JWT decoder)

Plans 02-01 through 02-12 (waves 1-2) were reviewed in iterations 1-2 and are out of scope.

## BLOCKERs

### B1: JWT decoder wraps all decode errors as JwksUnreachableException, causing invalid JWTs to return 503 instead of 401

- **File:** services/api-gateway/src/main/java/com/bookinghub/gateway/config/SecurityConfig.java:101-107
- **Category:** bug
- **Evidence:** The `failClosedJwtDecoder()` method's `onErrorResume` handler (lines 101-107) catches **any** error emitted by `decoder.decode(token)` and wraps it as `JwksUnreachableException`. This includes:
  - JWT validation errors (expired token, invalid signature, malformed JWT) → should return 401 Unauthorized
  - JWKS fetch failures (network errors, DNS failures) → should return 503 Service Unavailable
  
  The current implementation wraps both categories as `JwksUnreachableException`, which is explicitly handled by `GatewayErrorAttributes.java:28-32` to return 503 with error_code `SERVICE_UNAVAILABLE`. This means a request with an expired or invalid JWT receives "Identity provider unavailable" (503) instead of "Authentication required" (401), violating the principle that 503 indicates server-side unavailability, not client-side bad credentials.

  The plan (02-13-PLAN.md:92-93) states: "At request time: if Keycloak's JWKS endpoint is unreachable, returns 503" — not "if JWT validation fails". The comment at line 103 says "Any failure reaching JWKS endpoint" but the code catches all decode failures including token validation.

  **Concrete failing scenario:**
  1. Keycloak is healthy and reachable
  2. Client sends request with expired JWT: `Authorization: Bearer eyJhbGc...` (expired exp claim)
  3. `NimbusReactiveJwtDecoder.decode()` emits `JwtValidationException` (expired token)
  4. `onErrorResume` catches it and wraps as `JwksUnreachableException`
  5. `GatewayErrorAttributes` returns 503 with message "Identity provider unavailable"
  6. **Expected:** 401 Unauthorized with AUTH_UNAUTHENTICATED
  7. **Actual:** 503 Service Unavailable with SERVICE_UNAVAILABLE

  **Impact:** Clients cannot distinguish between "my token is invalid" and "the auth service is down". This breaks standard HTTP semantics where 401 indicates client needs to re-authenticate and 503 indicates retry later. Debugging authentication issues becomes significantly harder.

  **Note on scope:** This bug exists in the **unchanged error handling logic** within the modified `failClosedJwtDecoder()` method. The gap-closure changes (02-13) modified the decoder initialization (lines 95-98) but preserved the pre-existing error handling (lines 101-107). The error handling was also present in the original implementation from wave 1 (plan 02-10). 
  
  However, this is classified as a BLOCKER because:
  1. The modified method (`failClosedJwtDecoder`) is the core deliverable of the gap-closure plan
  2. The bug affects the runtime correctness of JWT validation, which is the method's primary purpose
  3. The file was explicitly modified in this wave, making the bug subject to review even if the specific lines weren't changed
  4. The plan's threat model (T-02-13-01) explicitly states "unreachable JWKS triggers 503", implying other errors should NOT trigger 503

- **Fix direction:** Modify `onErrorResume` at line 102 to selectively catch only network/IO-related exceptions (e.g., exceptions indicating JWKS fetch failure), not JWT validation exceptions. One approach: check exception type or message to distinguish JWKS fetch failures from token validation failures. For example, catch only exceptions that are NOT `JwtException` subclasses, or inspect the cause chain for `IOException` or connection-related exceptions. Allow `JwtValidationException`, `BadJwtException`, and other JWT validation errors to propagate unchanged so Spring Security's authentication entry point handles them as 401.

## WARNINGs

None.

## Cross-file seams checked

### docker-compose.yml ↔ SecurityConfig.java (JWKS endpoint path)
- **Keycloak healthcheck path:** `/realms/bookinghub/.well-known/openid-configuration` (docker-compose.yml:60)
- **SecurityConfig JWKS path:** `issuerUri + "/protocol/openid-connect/certs"` → `http://keycloak:8080/realms/bookinghub/protocol/openid-connect/certs` (SecurityConfig.java:96)
- **Status:** OK — Both paths target the same Keycloak realm (`bookinghub`). Healthcheck tests OIDC discovery endpoint, SecurityConfig directly constructs JWKS URI. Standard Keycloak JWKS path is `/realms/{realm}/protocol/openid-connect/certs`, which matches the constructed URI.

### docker-compose.yml ↔ application.yml (Keycloak connection parameters)
- **docker-compose env vars:** `KEYCLOAK_HOST: keycloak`, `KEYCLOAK_PORT: 8080` (docker-compose.yml:283-284)
- **application.yml issuer-uri:** `http://${KEYCLOAK_HOST:localhost}:${KEYCLOAK_PORT:8180}/realms/bookinghub` (application.yml:68)
- **Resolved URI in container:** `http://keycloak:8080/realms/bookinghub`
- **Status:** OK — Environment variables correctly override the localhost defaults, pointing Gateway to the Keycloak container on internal port 8080 (not host port 8180).

### docker-compose.yml dependency chain
- **api-gateway depends_on keycloak:** `condition: service_healthy` (docker-compose.yml:263-264)
- **Keycloak healthcheck:** interval 5s, timeout 5s, retries 30, start_period 30s → max 180 seconds to healthy
- **Status:** OK — Gateway startup is blocked until Keycloak's OIDC discovery endpoint returns 200. This prevents the DNS resolution failure (`UnknownHostException: keycloak`) that gap-closure plan 02-13 was meant to fix. Combined with lazy decoder initialization (SecurityConfig.java:95-98), ensures Gateway can boot even if Keycloak temporarily becomes unavailable after initial startup.

### SecurityConfig.java ↔ GatewayErrorAttributes.java (JwksUnreachableException handling)
- **SecurityConfig throws:** `JwksUnreachableException` (SecurityConfig.java:105)
- **GatewayErrorAttributes catches:** `if (error instanceof SecurityConfig.JwksUnreachableException)` (GatewayErrorAttributes.java:28)
- **Status:** Contract fulfilled — GatewayErrorAttributes explicitly handles the custom exception and returns 503. However, the contract is **overused** due to B1: the exception is thrown for all JWT decode errors, not just JWKS unreachability.

## Re-Review: Verification of Previous Findings

### From Iteration 2 (Waves 1-2)

All previous blockers (B1-B3) and warnings (W1-W7) from iteration 2 were related to wave 1-2 plans and are not re-verified in this gap-closure review. Iteration 2 status was `issues_found` with 0 blockers, 1 warning (W1: rate limiter non-volatile field). That warning remains unaddressed but is out of scope for this gap-closure review.

## Gap Closure Specific Verification

### Did the gap-closure changes fix the reported UAT issue?

**UAT Test 5 failure (02-UAT.md):** Gateway container crash-looped with `java.net.UnknownHostException: keycloak` during JWT decoder initialization.

**Root cause:** Eager JWT decoder initialization (`ReactiveJwtDecoders.fromIssuerLocation(issuerUri)`) attempted to fetch OIDC discovery and JWKS at bean creation time, before Keycloak was resolvable.

**Gap-closure fixes:**
1. **docker-compose.yml:** Added Keycloak healthcheck + changed api-gateway dependency to `service_healthy` → Ensures Keycloak is reachable before Gateway starts ✓
2. **SecurityConfig.java:** Replaced eager decoder with lazy initialization (`NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build()`) → Defers JWKS fetch until first request ✓

**Verification:** The gap-closure changes correctly implement the plan's objectives. Gateway startup no longer depends on Keycloak being immediately reachable during bean creation. The healthcheck ensures Keycloak is available before Gateway starts, and lazy initialization prevents crash if Keycloak becomes temporarily unavailable after startup.

**However:** B1 remains unresolved and affects runtime behavior after successful startup. This bug was pre-existing (present in plan 02-10's original implementation) but is now exposed as the Gateway can actually start and process requests.

## Verdict

**Status:** `issues_found`  
**Blockers:** 1 (B1: JWT validation errors incorrectly return 503 instead of 401)  
**Warnings:** 0  

The gap-closure changes **successfully fix the startup issue** (UAT Test 5) but **expose a pre-existing runtime bug** in JWT error handling that must be fixed before phase 2 ships. The bug does not prevent Gateway startup (the gap-closure's goal) but breaks correct JWT validation behavior for production traffic.
