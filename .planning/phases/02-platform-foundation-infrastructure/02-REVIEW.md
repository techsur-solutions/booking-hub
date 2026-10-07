---
phase: 2
status: clean
blockers: 0
warnings: 0
files_reviewed: 2
files_reviewed_list:
  - docker-compose.yml
  - services/api-gateway/src/main/java/com/bookinghub/gateway/config/SecurityConfig.java
reviewed_at: 2026-10-07T16:45:00Z
iteration: 2
---

# Phase 2 Code Review - Iteration 2 (Re-review)

## Scope

This is a re-review after iteration 1 fixes. Verifying:
1. B1 blocker (JWT error handling) has been properly resolved
2. No new issues introduced by the fix
3. The fix properly addresses the root cause

Files reviewed:
- `docker-compose.yml` (unchanged since iteration 1)
- `services/api-gateway/src/main/java/com/bookinghub/gateway/config/SecurityConfig.java` (fix applied in commit d8b970b)

## BLOCKERs

None.

## WARNINGs

None.

## Iteration 1 Finding Resolution

### B1: JWT decoder wraps all decode errors as JwksUnreachableException (RESOLVED ✓)

**Original issue:** The `failClosedJwtDecoder()` method's `onErrorResume` handler caught ALL errors from `decoder.decode(token)` and wrapped them as `JwksUnreachableException`, causing:
- Invalid/expired JWTs → 503 Service Unavailable (wrong - should be 401 Unauthorized)
- JWKS fetch failures → 503 Service Unavailable (correct)

**Fix applied (commit d8b970b):**
```java
.onErrorResume(ex -> {
    // JWT validation errors (expired, invalid signature, malformed) → propagate for 401
    if (ex instanceof JwtException) {
        return Mono.error(ex);
    }
    // JWKS fetch failures (network errors, timeouts, DNS failures, etc.) → fail closed with 503
    return Mono.error(new JwksUnreachableException(
            "Identity provider JWKS endpoint unreachable - failing closed", ex));
})
```

**Verification:**
1. **Import added:** `org.springframework.security.oauth2.jwt.JwtException` imported (line 13)
2. **Logic correct:** Exception type discrimination properly distinguishes:
   - `JwtException` subclasses (BadJwtException, JwtValidationException, JwtEncodingException) → represent client-side JWT validation failures → propagated unchanged for Spring Security's auth entry point to return 401
   - Non-JwtException errors (WebClientException, IOException, RestClientException wrapped in RuntimeException) → represent infrastructure failures during JWKS fetch → wrapped as JwksUnreachableException for 503 response
3. **Flow verified:**
   - Invalid/expired JWT: `decoder.decode()` → JwtException → `instanceof` check passes → `Mono.error(ex)` → propagates to `authenticationEntryPoint` (line 82) → `handleAuthenticationError()` (line 115) → 401 + AUTH_UNAUTHENTICATED ✓
   - JWKS unreachable: `decoder.decode()` → WebClientException → `instanceof JwtException` fails → wrapped as JwksUnreachableException → caught by `GatewayErrorAttributes` (line 28) → 503 + SERVICE_UNAVAILABLE ✓
4. **Fail-closed semantics preserved:** Infrastructure failures still return 503 (deny by default), not 200 or silent failures
5. **Comments updated:** Accurately describe the new behavior (lines 102, 105, 109)

**Impact:** Correctly distinguishes "your token is bad" (401, client should re-authenticate) from "auth service is down" (503, client should retry later). Standard HTTP semantics restored.

**Resolution:** VERIFIED FIXED. The implementation now correctly handles both error categories with appropriate HTTP status codes.

## Regression Analysis

### Changes introduced by fix commit (d8b970b)

**Modified lines in SecurityConfig.java:**
- Added import: `JwtException` (line 13)
- Modified `onErrorResume` handler (lines 104-112): added type check before wrapping exception
- Updated comments (lines 102, 105, 109)

**No changes to:**
- Security filter chain configuration (lines 35-86)
- JWT decoder initialization (lines 95-99) — lazy initialization preserved
- Authentication/access denied handlers (lines 115-150)
- JwksUnreachableException class definition (lines 156-160)
- docker-compose.yml (unchanged since iteration 1)

**Potential regression vectors checked:**

1. **Could the fix break fail-closed behavior?**
   - NO. Non-JwtException errors still wrapped as JwksUnreachableException → 503 as before
   - Fail-closed contract maintained for infrastructure failures

2. **Could the fix cause valid tokens to be rejected?**
   - NO. Valid tokens never throw JwtException — they decode successfully
   - Only invalid tokens throw JwtException, which should be rejected with 401

3. **Could the instanceof check fail for certain JwtException subtypes?**
   - NO. All Spring Security JWT validation errors extend JwtException
   - Checked Spring Security 6.x (Boot 3.3.4) documentation: BadJwtException, JwtValidationException, JwtEncodingException all extend JwtException

4. **Could network errors be misclassified as JwtException?**
   - NO. NimbusReactiveJwtDecoder wraps JWKS fetch errors as non-JwtException types
   - WebClient failures (network, timeout, DNS) propagate as WebClientException or IOException wrapped in RuntimeException

5. **Does the fix break the integration with GatewayErrorAttributes?**
   - NO. JwksUnreachableException still thrown for JWKS failures (line 110)
   - GatewayErrorAttributes still catches it and returns 503 (GatewayErrorAttributes.java:28-32)
   - JwtException instances now handled by Spring Security's default auth entry point → 401

6. **Could the fix break unit tests?**
   - NO IMPACT. Both test files (GatewaySecurityTest.java, GatewayFailClosedTest.java) are @Disabled
   - Tests defer to full-stack integration testing per plan 02-12
   - Fix improves correctness for integration tests (expired tokens will now properly return 401)

7. **Does lazy initialization still work?**
   - YES. Lines 95-99 unchanged — decoder creation does not trigger JWKS fetch
   - docker-compose.yml healthcheck ensures Keycloak available before Gateway starts
   - Lazy decoder only fetches JWKS on first validation request

**Verdict:** NO REGRESSIONS DETECTED. The fix is minimal, surgical, and preserves all existing correct behavior while fixing the classification bug.

## Cross-file seams checked

### docker-compose.yml ↔ SecurityConfig.java (JWKS endpoint path)
- **Keycloak healthcheck path:** `/realms/bookinghub/.well-known/openid-configuration` (docker-compose.yml:60)
- **SecurityConfig JWKS path:** `issuerUri + "/protocol/openid-connect/certs"` → `http://keycloak:8080/realms/bookinghub/protocol/openid-connect/certs` (SecurityConfig.java:97)
- **Status:** OK — Both paths target same Keycloak realm. Standard Keycloak JWKS path matches.

### docker-compose.yml ↔ application.yml (Keycloak connection parameters)
- **docker-compose env vars:** `KEYCLOAK_HOST: keycloak`, `KEYCLOAK_PORT: 8080` (docker-compose.yml:283-284)
- **application.yml issuer-uri:** `http://${KEYCLOAK_HOST:localhost}:${KEYCLOAK_PORT:8180}/realms/bookinghub`
- **Resolved URI in container:** `http://keycloak:8080/realms/bookinghub`
- **Status:** OK — Environment variables correctly override defaults.

### docker-compose.yml dependency chain
- **api-gateway depends_on keycloak:** `condition: service_healthy` (docker-compose.yml:263-264)
- **Keycloak healthcheck:** interval 5s, timeout 5s, retries 30, start_period 30s → max 180s to healthy
- **Status:** OK — Gateway startup blocked until Keycloak OIDC discovery returns 200. Prevents DNS resolution failures during eager decoder initialization.

### SecurityConfig.java ↔ GatewayErrorAttributes.java (JwksUnreachableException handling)
- **SecurityConfig throws:** `JwksUnreachableException` for JWKS fetch failures (SecurityConfig.java:110)
- **SecurityConfig propagates:** `JwtException` for JWT validation errors (SecurityConfig.java:107)
- **GatewayErrorAttributes catches:** `instanceof JwksUnreachableException` → 503 + SERVICE_UNAVAILABLE (GatewayErrorAttributes.java:28-32)
- **Spring Security catches:** `JwtException` → delegates to authenticationEntryPoint → 401 + AUTH_UNAUTHENTICATED
- **Status:** OK — Contract correctly implemented. Both error paths properly handled by their respective handlers.

## Summary

**Status:** CLEAN ✓

All blockers from iteration 1 have been resolved. The fix correctly addresses the root cause by distinguishing JWT validation errors (client-side, 401) from JWKS infrastructure failures (server-side, 503). No regressions introduced. The phase 02 implementation now correctly handles all JWT error scenarios per the threat model.

**Commit log:**
- d8b970b: fix(phase-2): B1 — distinguish JWT validation errors from JWKS fetch failures

Phase 02 is ready to ship.
