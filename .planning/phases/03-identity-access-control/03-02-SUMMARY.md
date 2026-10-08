---
phase: 03-identity-access-control
plan: 02
subsystem: auth
tags: [jwt, keycloak, security, oauth2, wiremock, tier-2-validation]

# Dependency graph
requires:
  - phase: 03-01
    provides: ApiError shared error contract
provides:
  - Tier-2 JWT resource-server validation (defense-in-depth, fail-closed on JWKS outage)
  - KeycloakAdminService: createUser, resetPassword, updateUserRealmRoles (REPLACE semantics), logoutUser (ALL sessions), findUserIdByEmail
  - KeycloakTokenClient: directGrantLogin (with remember-me client routing), verifyCurrentPassword
  - CurrentUserProvider: getCurrentUserId/getCurrentRoles/hasRole/isSelf for all controllers
  - F6.6 structured ApiError access-denied responses (401/403/503) replacing legacy's blank denied page
affects: [03-03, 03-04, 03-05]

# Tech tracking
tech-stack:
  added: [spring-boot-starter-security, spring-boot-starter-oauth2-resource-server, keycloak-admin-client:25.0.6, wiremock-standalone:3.9.2, spring-security-test]
  patterns: [Tier-2 independent JWT validation, WireMock-backed integration tests for Keycloak, fail-closed 503 on IdP outage]

key-files:
  created:
    - services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/security/SecurityConfig.java
    - services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/security/JwksOutageAuthenticationEntryPoint.java
    - services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/security/ApiAccessDeniedHandler.java
    - services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/security/CurrentUserProvider.java
    - services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/keycloak/KeycloakAdminClientConfig.java
    - services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/keycloak/KeycloakAdminService.java
    - services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/keycloak/KeycloakTokenClient.java
    - services/users-permissions-service/src/test/java/com/bookinghub/userspermissions/keycloak/KeycloakAdminServiceTest.java
    - services/users-permissions-service/src/test/java/com/bookinghub/userspermissions/keycloak/KeycloakTokenClientTest.java
    - services/users-permissions-service/src/test/java/com/bookinghub/userspermissions/security/Tier2FailClosedTest.java
  modified:
    - services/users-permissions-service/pom.xml
    - services/users-permissions-service/src/main/resources/application.yml

key-decisions:
  - "Keycloak Admin Client version pinned to 25.0.6 (matching server image 25.0) to prevent client-server version drift"
  - "updateUserRealmRoles uses REPLACE semantics (remove-all + add-new) per FRD's array-shaped PUT contract, not additive — deliberate improvement over legacy's single-role column"
  - "logoutUser revokes ALL sessions (no targeted revocation) because POST /auth/logout API contract carries no session identifier — coarser than legacy but safer default"
  - "F6.6 decision: all access-denied outcomes (401/403/503) produce structured ApiError JSON, not Spring Security's default bodies — the named translation of legacy's blank denied.cfm page into API-first responses"
  - "WireMock standalone used for Keycloak integration tests (zero live Keycloak dependency) — proven pattern for testing external service integrations"

patterns-established:
  - "Tier-2 defense-in-depth: every backend service independently re-validates JWTs, never trusting Gateway's decision alone (TechArch §5.2)"
  - "Fail-closed consistency: Tier 2 mirrors Gateway's fail-closed behavior (JWKS-unreachable → 503 SERVICE_UNAVAILABLE), never silently passing through or defaulting to permit"
  - "WireMock-backed external service testing: integration tests stub Keycloak Admin REST API and token endpoints, enabling full test coverage without live dependencies"

# Metrics
duration: 11min
completed: 2026-10-08
---

# Phase 03 Plan 02: Tier-2 JWT Security + Keycloak Integration Summary

**Tier-2 JWT validation with Gateway-matching fail-closed behavior, Keycloak Admin/token integration (zero live dependency in tests), and F6.6 structured ApiError access-denied responses**

## Performance

- **Duration:** 11 min
- **Started:** 2026-10-08T14:56:52Z
- **Completed:** 2026-10-08T15:08:19Z
- **Tasks:** 3
- **Files modified:** 12

## Accomplishments

- users-permissions-service independently re-validates every JWT (Tier-2 defense-in-depth), failing closed with 503 on JWKS outage exactly like the Gateway
- All access-denied outcomes (401 unauthenticated, 403 insufficient-role, 503 JWKS-unreachable) produce the shared ApiError JSON shape, never Spring Security's default error bodies (F6.6 named decision)
- Keycloak Admin API + direct-grant token clients fully tested against WireMock (16 test scenarios, zero live Keycloak dependency)
- CurrentUserProvider extracts user-id/roles from JWT for all later controllers to use (no hand-parsing)

## Task Commits

Each task was committed atomically:

1. **Task 1: Tier-2 JWT security config with fail-closed JWKS-outage handling** - `8257b6c` (feat)
2. **Task 2: Keycloak Admin API client + direct-grant token client** - `7f95151` (feat)
3. **Task 3: WireMock-backed Keycloak-client tests + Tier-2 fail-closed/access-denied tests** - `8496d18` (test)

## Files Created/Modified

Created:
- `SecurityConfig.java` - Tier-2 JWT resource-server config: public routes (only /auth/login, /auth/password-reset/**, /actuator/health/**), fail-closed 503 on JWKS outage, 403 on insufficient role
- `JwksOutageAuthenticationEntryPoint.java` - Distinguishes JWKS network failure (503) from invalid/missing token (401), both as ApiError JSON
- `ApiAccessDeniedHandler.java` - Authenticated-but-insufficient-role → 403 AUTH_FORBIDDEN ApiError (F6.6 translation of legacy's blank denied page)
- `CurrentUserProvider.java` - getCurrentUserId/getCurrentRoles/hasRole/isSelf helpers for all controllers
- `KeycloakAdminClientConfig.java` - Service-account client_credentials grant for Admin API
- `KeycloakAdminService.java` - 5 operations: createUser (with initial role), resetPassword, updateUserRealmRoles (REPLACE semantics), logoutUser (ALL sessions), findUserIdByEmail
- `KeycloakTokenClient.java` - directGrantLogin (routes to standard or remember-me client based on flag), verifyCurrentPassword (boolean, not thrown exception)
- Test files: KeycloakAdminServiceTest (7 scenarios), KeycloakTokenClientTest (6 scenarios), Tier2FailClosedTest (3 security scenarios)

Modified:
- `pom.xml` - Added spring-security, oauth2-resource-server, keycloak-admin-client:25.0.6, wiremock-standalone, spring-security-test
- `application.yml` - Added JWT issuer-uri, Keycloak server/realm/client configs

## Decisions Made

- **Keycloak version pinning:** Admin client pinned to 25.0.6 (matching server image 25.0 from Phase 2 plan 02-12) to prevent client-server API incompatibilities — if server is ever upgraded to 26.x, this dependency must be bumped together
- **updateUserRealmRoles REPLACE semantics:** Removes ALL current realm roles, then adds exactly `newRoles` — matches FRD's array-shaped `PUT /users/{id}/roles {roles[]}` contract, deliberate improvement over legacy's single-role `users.role` column
- **logoutUser revokes ALL sessions:** No targeted session scope because `POST /auth/logout` contract carries no session/refresh-token identifier — residual risk (legitimate session on another device gets force-logged-out) accepted as safer default
- **F6.6 structured access-denied responses:** Every 401/403/503 produces ApiError JSON with exact FRD error codes (AUTH_UNAUTHENTICATED/AUTH_FORBIDDEN/SERVICE_UNAVAILABLE), not Spring Security's default HTML/text bodies — the named translation of legacy's blank `denied.cfm` page into API-first responses
- **WireMock for Keycloak tests:** Keycloak Admin Client + token endpoint integration fully tested against WireMock stand-ins (16 scenarios total) with zero live Keycloak dependency — proven pattern enabling CI/offline dev

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None - all tasks completed as specified, all tests passed.

## Known Stubs

None found.

## User Setup Required

None - no external service configuration required (Keycloak clients already declared as placeholders in Phase 2 plan 02-04; actual creation happens in plan 03-03).

## Next Phase Readiness

Ready for plan 03-03 (Keycloak realm setup + direct-grant client creation). The Tier-2 security foundation is complete:
- JWT validation layer operational
- Fail-closed behavior mirrors Gateway exactly
- Keycloak integration clients ready to be used once realm is provisioned
- CurrentUserProvider available for all later controllers (03-04 UserController, 03-05 PermissionController)

---
*Phase: 03-identity-access-control*
*Completed: 2026-10-08*
