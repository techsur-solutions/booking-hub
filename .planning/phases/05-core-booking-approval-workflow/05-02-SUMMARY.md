---
phase: 05-core-booking-approval-workflow
plan: 02
subsystem: api
tags: [spring-boot, jpa, postgresql, rest-client, wiremock, conflict-detection, token-relay]

# Dependency graph
requires:
  - phase: 05-core-booking-approval-workflow
    plan: "05-01"
    provides: "BookingRepository, BookingResourceRepository, CurrentUserProvider.getRawBearerToken(), ConflictCheckInvalidInputException, ApprovalSettingsUnavailableException"
  - phase: 04-reference-data-extensibility-configuration
    provides: "LocationsResourcesClient contract (GET /locations/{id} returns 200 for soft-deleted), CustomFieldClient carve-out (role_calendar_viewer on GET /custom-fields?context_id), SettingsClient contract (GET /settings, no role required)"

provides:
  - "ConflictDetectionService: the single shared conflict-evaluation engine — location-level + is_unique-aware resource-level overlap using TechArch §3.3's exact half-open-interval queries"
  - "LocationsResourcesClient: token-relay GET /locations/{id} and GET /resources/{id}, 200-for-soft-deleted, 404->Optional.empty()"
  - "SettingsClient: token-relay GET /settings with 5-second local cache per TechArch §1.5, throws ApprovalSettingsUnavailableException on failure"
  - "CustomFieldClient: token-relay GET /custom-fields?context_id, graceful degradation (empty list) only for genuine outage/misprovisioning after 04-04 carve-out fix"
  - "POST /bookings/check-conflicts: purely informational endpoint, always 200, role_booking_creator gated"
  - "13 ApiException subclasses extracted to individual public files for cross-package access (plans 05-03/05-04)"
affects:
  - "05-03: calls ConflictDetectionService.checkConflicts() for conflict enforcement on create/edit"
  - "05-04: calls ConflictDetectionService for bulk read-view conflict flagging; reads SettingsClient.getApproveBookingFlag() for auto-approve"

# Tech tracking
tech-stack:
  added:
    - "org.springframework.web.client.RestClient (Spring 6.1, via spring-boot-starter-web — no new dependency)"
  patterns:
    - "is_unique-aware conflict detection: caller responsibility (ConflictController/BookingWriteService) pre-filters uniqueResourceIds before calling ConflictDetectionService"
    - "Token relay: CurrentUserProvider.getRawBearerToken() forwarded as Authorization: Bearer header on all outbound calls"
    - "5-second volatile cache: SettingsClient holds (SettingsResponse value, long fetchedAtEpochMillis) pair"
    - "Narrowed graceful degradation: CustomFieldClient returns empty list only for 5xx/403 (genuine edge case, not universal path)"
    - "Spring Security Test jwt().authorities(new SimpleGrantedAuthority('ROLE_...')) for integration tests"
    - "Exception subclasses extracted to individual public files for cross-package throwability"

key-files:
  created:
    - "services/booking-service/src/main/java/com/bookinghub/booking/conflict/ConflictDetectionService.java"
    - "services/booking-service/src/main/java/com/bookinghub/booking/conflict/dto/ConflictDtos.java"
    - "services/booking-service/src/main/java/com/bookinghub/booking/conflict/ConflictController.java"
    - "services/booking-service/src/main/java/com/bookinghub/booking/client/LocationsResourcesClient.java"
    - "services/booking-service/src/main/java/com/bookinghub/booking/client/SettingsClient.java"
    - "services/booking-service/src/main/java/com/bookinghub/booking/client/CustomFieldClient.java"
    - "services/booking-service/src/main/java/com/bookinghub/booking/client/ClientConfig.java"
    - "services/booking-service/src/main/java/com/bookinghub/booking/client/dto/ClientDtos.java"
    - "services/booking-service/src/main/java/com/bookinghub/booking/error/[13 exception files extracted from ApiException.java]"
    - "services/booking-service/src/test/java/com/bookinghub/booking/conflict/ConflictDetectionServiceTest.java"
    - "services/booking-service/src/test/java/com/bookinghub/booking/client/LocationsResourcesClientTest.java"
    - "services/booking-service/src/test/java/com/bookinghub/booking/client/SettingsClientTest.java"
    - "services/booking-service/src/test/java/com/bookinghub/booking/client/CustomFieldClientTest.java"
    - "services/booking-service/src/test/java/com/bookinghub/booking/conflict/ConflictControllerIntegrationTest.java"
  modified:
    - "services/booking-service/src/main/java/com/bookinghub/booking/repository/BookingRepository.java — added findConflictingBookingIdsByLocation"
    - "services/booking-service/src/main/java/com/bookinghub/booking/repository/BookingResourceRepository.java — added findConflictingBookingIdsByResource"
    - "services/booking-service/src/main/java/com/bookinghub/booking/error/ApiException.java — refactored to base class only"
    - "services/booking-service/src/main/resources/application.yml — added downstream service base URLs"

key-decisions:
  - "ConflictDetectionService has ZERO cross-service HTTP calls (pure local-database query) — is_unique filtering is the caller's pre-processing responsibility"
  - "13 ApiException subclasses extracted to individual public files for cross-package access by client/, conflict/ packages"
  - "Test status fixed: schema constraint chk_booking_status allows only 'pending'/'approved'/'denied' (not 'confirmed') — tests use 'approved'"
  - "Spring Security Test jwt() helper requires explicit .authorities(new SimpleGrantedAuthority('ROLE_...')) — not realm_access JWT claim (bypasses custom converter)"
  - "Exception subclass refactoring applied pre-emptively for all 13 exceptions (plans 05-03/05-04 will need them all from different packages)"

# Metrics
duration: 18min
completed: 2026-10-09
---

# Phase 05 Plan 02: Conflict Detection Engine + Outbound HTTP Clients Summary

**Single shared ConflictDetectionService with TechArch's verbatim half-open-interval SQL, three token-relay RestClient outbound clients, and a purely informational POST /bookings/check-conflicts endpoint — proven by 16 tests against real Postgres and WireMock.**

## Performance

- **Duration:** ~18 min
- **Started:** 2026-10-09T18:33:43Z
- **Completed:** 2026-10-09T18:51:27Z
- **Tasks:** 3 completed
- **Files modified:** ~27 files (new + modified)

## Accomplishments

- **ConflictDetectionService**: The single shared conflict-evaluation engine — location-level overlap (`findConflictingBookingIdsByLocation`) and resource-level overlap (`findConflictingBookingIdsByResource`) using TechArch §3.3's exact half-open-interval predicates verbatim. Zero cross-service HTTP. 6 scenarios proven against real Postgres (overlap, adjacent-no-overlap, denied/deleted exclusion, self-exclusion on edit, resource conflict).
- **Three outbound HTTP clients**: `LocationsResourcesClient` (token relay, 200-for-soft-deleted, 404→Optional.empty()), `SettingsClient` (5-second local cache per TechArch §1.5, ApprovalSettingsUnavailableException on failure), `CustomFieldClient` (real success path after 04-04 carve-out, graceful degradation only for genuine outage/misprovisioning).
- **POST /bookings/check-conflicts**: Purely informational — always returns 200 ConflictCheckResult, never 409. role_booking_creator gated. 5 integration scenarios proven (no conflict, with conflict, 400 invalid input, 403 no-role, 403 approver-only).
- **Exception refactoring**: All 13 ApiException subclasses extracted from single-file multi-class pattern to individual public class files, enabling cross-package throwability by plans 05-03/05-04's service/controller packages.

## Task Commits

Each task was committed atomically:

1. **Task 1: ConflictDetectionService** - `cb79e5f` (feat)
2. **Task 2: Outbound HTTP clients + exception refactoring** - `a235b65` (feat)
3. **Task 3: POST /bookings/check-conflicts endpoint** - `8e5fc33` (feat)

## Files Created/Modified

- `src/main/java/com/bookinghub/booking/conflict/ConflictDetectionService.java` — Single conflict engine
- `src/main/java/com/bookinghub/booking/conflict/dto/ConflictDtos.java` — ConflictFlag, ConflictCheckResult, ConflictCheckRequest
- `src/main/java/com/bookinghub/booking/conflict/ConflictController.java` — POST /bookings/check-conflicts
- `src/main/java/com/bookinghub/booking/client/LocationsResourcesClient.java` — GET /locations/{id}, /resources/{id}
- `src/main/java/com/bookinghub/booking/client/SettingsClient.java` — GET /settings with 5s cache
- `src/main/java/com/bookinghub/booking/client/CustomFieldClient.java` — GET /custom-fields?context_id
- `src/main/java/com/bookinghub/booking/client/ClientConfig.java` — RestClient beans
- `src/main/java/com/bookinghub/booking/client/dto/ClientDtos.java` — LocationResponse, ResourceResponse, CustomFieldDefinition, SettingsResponse
- `src/main/java/com/bookinghub/booking/error/ApiException.java` — Refactored to base class only
- `src/main/java/com/bookinghub/booking/error/[13 files]` — Individual public exception class files
- `src/main/java/com/bookinghub/booking/repository/BookingRepository.java` — Added conflict query
- `src/main/java/com/bookinghub/booking/repository/BookingResourceRepository.java` — Added conflict query
- `src/main/resources/application.yml` — Downstream service URLs

## Decisions Made

- **ConflictDetectionService ZERO HTTP**: is_unique filtering is the caller's responsibility, not the service's. This keeps it maximally simple and independently testable.
- **Exception files refactoring**: Java requires each public class in its own file. Extracted all 13 to individual `.java` files, enabling cross-package use by plans 05-03/05-04.
- **Test status values**: Database `chk_booking_status` constraint allows only `'pending'`, `'approved'`, `'denied'` — tests use `'approved'` (not the invalid `'confirmed'` in initial draft).
- **jwt() authorities**: Spring Security Test's `jwt()` helper bypasses the custom `JwtAuthenticationConverter` — must use `.authorities(new SimpleGrantedAuthority("ROLE_..."))` explicitly.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Test used invalid booking status 'confirmed'**
- **Found during:** Task 1 (ConflictDetectionServiceTest first run)
- **Issue:** Schema's `chk_booking_status` constraint only allows 'pending', 'approved', 'denied' — 'confirmed' is not a valid status, causing DataIntegrityViolation on all 5 affected test scenarios
- **Fix:** Changed all test saveBooking() calls to use 'approved' status
- **Files modified:** ConflictDetectionServiceTest.java
- **Verification:** All 6 test scenarios pass
- **Committed in:** cb79e5f (Task 1 commit)

**2. [Rule 1 - Bug] ApiException subclasses were package-private — cross-package throwability impossible**
- **Found during:** Task 2 (compile attempt after adding clients)
- **Issue:** Java requires public top-level classes to be in their own file; all 13 exception subclasses were package-private inner classes in one file — making them unreachable from client/ and conflict/ packages
- **Fix:** Extracted all 13 subclasses to individual public class files in the `error` package; refactored `ApiException.java` to contain only the abstract base class
- **Files modified:** ApiException.java (base only) + 13 new exception .java files
- **Verification:** `mvn compile test-compile` succeeds; SettingsClient and ConflictController compile successfully importing their respective exception types
- **Committed in:** a235b65 (Task 2 commit)

**3. [Rule 1 - Bug] jwt() test helper bypasses custom JwtAuthenticationConverter**
- **Found during:** Task 3 (ConflictControllerIntegrationTest — all scenarios returned 403)
- **Issue:** Spring Security Test's `jwt()` post-processor creates a `JwtAuthenticationToken` with default scope-based authorities, bypassing the custom `realmRoleJwtAuthenticationConverter`. Using `jwt().jwt(j -> j.claim("realm_access", ...))` adds JWT claims but doesn't invoke the converter — the test still gets no `ROLE_` authorities
- **Fix:** Changed all jwt() calls to use `jwt().authorities(new SimpleGrantedAuthority("ROLE_role_booking_creator"))` — sets ROLE_-prefixed authorities directly in the mock security context
- **Files modified:** ConflictControllerIntegrationTest.java
- **Verification:** All 5 integration scenarios pass
- **Committed in:** 8e5fc33 (Task 3 commit)

---

**Total deviations:** 3 auto-fixed (all Rule 1 - Bug)
**Impact on plan:** All fixes necessary for correctness. No scope creep. Tests now accurately represent the real production behavior.

## Known Stubs

None found — all client methods, service logic, and controller behavior are fully implemented with real production logic. The CustomFieldClient graceful degradation is documented as an explicit named decision (not a stub), correctly handling the narrowed edge cases after plan 04-04's carve-out fix.

## Issues Encountered

None beyond the auto-fixed bugs documented above.

## User Setup Required

None - no external service configuration required. All downstream service URLs use docker-compose service name defaults.

## Next Phase Readiness

- `ConflictDetectionService.checkConflicts()` is ready for plans 05-03 (create/edit enforcement) and 05-04 (approval/read bulk flagging) to consume — zero modifications to this plan's files required
- `LocationsResourcesClient`, `SettingsClient`, `CustomFieldClient` all tested and ready for plans 05-03/05-04's service layer
- All 13 exception types are now public and importable from any package in the service
- `POST /bookings/check-conflicts` is working and role-gated

## Self-Check: PASSED

- All 8 key files verified present on disk ✓
- All 3 task commits exist (cb79e5f, a235b65, 8e5fc33) ✓
- Plan-level build: `mvn -q -DskipTests package` → exit 0 ✓
- No blocking stubs found ✓

---
*Phase: 05-core-booking-approval-workflow*
*Completed: 2026-10-09*
