---
phase: 9
status: issues_found
blockers: 3
warnings: 2
files_reviewed: 11
files_reviewed_list:
  - frontend/src/pages/BookingsView.tsx
  - frontend/src/api/client.ts
  - frontend/src/hooks/useLocations.ts
  - frontend/src/components/booking/BookingForm.tsx
  - frontend/src/pages/MyAccount.tsx
  - frontend/src/api/types.ts
  - frontend/src/api/users.ts
  - services/users-permissions-service/src/main/resources/db/migration/V3__seed_test_user.sql
  - docker-compose.yml
  - frontend/e2e/booking-create.spec.ts
  - frontend/e2e/auth-flows.spec.ts
reviewed_at: 2026-10-14T18:30:00Z
iteration: 1
---

# Phase 9 Code Review

## BLOCKERs

### B1: Schema mismatch - V3 migration inserts non-existent columns
- **File:** services/users-permissions-service/src/main/resources/db/migration/V3__seed_test_user.sql:13-22, 25-34
- **Category:** bug
- **Evidence:** V3 migration attempts to INSERT into columns `first_name` and `last_name`, but the V1 schema defines only `display_name` column (V1__init_schema.sql line 4). V2 migration does not add these columns. Flyway will reject this migration at runtime with error: `ERROR: column "first_name" of relation "users" does not exist`.
- **Fix direction:** Either (1) add ALTER TABLE migration before V3 to split display_name into first_name/last_name columns, OR (2) change V3 to INSERT into display_name with concatenated values like 'Test User' and 'Booking Approver'.

### B2: Frontend/backend type mismatch - User interface incompatible with API contract
- **File:** frontend/src/api/types.ts:19-24, services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/dto/UserResponse.java:11-18
- **Category:** integration
- **Evidence:** Frontend User interface declares `first_name: string; last_name: string` but backend UserResponse returns `displayName` (single field). MyAccount.tsx line 68 reads `user.first_name` and `user.last_name` which will be undefined at runtime. Test at auth-flows.spec.ts:299-304 mocks the wrong shape (`first_name`, `last_name`) that does not match actual backend response.
- **Fix direction:** Align frontend User type with backend: change to `display_name: string`, update MyAccount.tsx lines 56-58, 68, 85 to use display_name, update usersApi.updateMe to send `display_name` not `first_name`/`last_name`, fix E2E mock at auth-flows.spec.ts:299-304.

### B3: HTTP method mismatch - frontend calls PATCH, backend provides PUT
- **File:** frontend/src/api/users.ts:12-13, services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/controller/UserController.java:103
- **Category:** integration
- **Evidence:** frontend/src/api/users.ts line 12 calls `apiClient.patch<User>('/users/me', data)` but UserController line 103 defines `@PutMapping("/me")`. Axios will send PATCH request, backend only handles PUT. Spring will respond with 405 Method Not Allowed. The E2E test at auth-flows.spec.ts:292-318 does not exercise the update path, so this defect is untested.
- **Fix direction:** Change frontend/src/api/users.ts line 12 to `apiClient.put<User>('/users/me', data)` OR add `@PatchMapping("/me")` handler to UserController (PUT is more semantically correct for full profile replacement).

## WARNINGs

### W1: Fragile 404 error detection relies on string matching
- **File:** frontend/src/pages/MyAccount.tsx:126, 134
- **Evidence:** Lines 126 and 134 check `error.message.includes('404')` to detect user-not-found errors. Axios error messages are implementation details (typically "Request failed with status code 404"). This check may fail if axios changes message format or if the API returns different error messages. Should check `error.response?.status === 404` instead.
- **Fix direction:** Replace `error.message.includes('404')` with `(error as any).response?.status === 404` to directly inspect HTTP status code.

### W2: Keycloak admin console URL mismatch in docker-compose.yml
- **File:** docker-compose.yml:59
- **Category:** bug
- **Evidence:** Comment at line 59 states "Keycloak admin console: http://localhost:9090/admin (admin/admin)" but keycloak service ports mapping at line 75 publishes port 8180, not 9090. The correct URL is http://localhost:8180/admin. This will confuse developers following setup instructions.
- **Fix direction:** Change comment line 59 to "http://localhost:8180/admin" to match actual port mapping.

## Cross-file seams checked

- **Plan 09-16 - BookingsView → BookingCreate navigation:**
  - BookingsView.tsx:47 builds URL `/bookings/create?start_time=${currentDate}T09:00` → OK, routes/index.tsx:71 defines route
  - BookingCreate.tsx:27 reads `start_time` param → OK, matches query param name
  - E2E test booking-create.spec.ts:645-649 verifies param preservation → OK

- **Plan 09-17 - JWT token refresh interceptor:**
  - client.ts:16 calls `keycloak.updateToken(30)` → OK, Keycloak JS API documented method
  - MyAccount.tsx:29 comment confirms centralized pattern → OK, no duplicate updateToken calls found
  - useLocations.ts, useResources.ts, useCustomFields.ts all use apiClient → OK, inherit token refresh
  - E2E tests mock keycloak.updateToken → OK, client.test.ts:10 verifies behavior

- **Plan 09-17 - BookingForm location dropdown:**
  - BookingForm.tsx:91 calls `useLocations()` → OK, hook returns { data, isLoading, isError }
  - Lines 174-201 conditionally render loading/error/empty/populated states → OK
  - E2E test booking-create.spec.ts:662-699 verifies dropdown population → OK

- **Plan 09-18 - My Account user record seed:**
  - **BLOCKED by B1, B2, B3** - integration broken at multiple layers
  - docker-compose.yml:48-59 documents test user UUIDs → matches V3 migration UUIDs (OK on documentation side)
  - E2E test auth-flows.spec.ts:292-318 mocks V3 seed data shape → **WRONG shape** (first_name/last_name not display_name)

- **Type safety:**
  - frontend User type vs backend UserResponse → **BLOCKER B2**
  - V3 seed columns vs V1 schema → **BLOCKER B1**
  - PATCH vs PUT → **BLOCKER B3**
