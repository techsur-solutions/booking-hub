---
phase: 9
status: issues_found
blockers: 2
warnings: 0
files_reviewed: 8
files_reviewed_list:
  - frontend/src/api/types.ts
  - frontend/src/api/users.ts
  - frontend/src/pages/MyAccount.tsx
  - frontend/e2e/auth-flows.spec.ts
  - services/users-permissions-service/src/main/resources/db/migration/V3__seed_test_user.sql
  - services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/dto/UserResponse.java
  - services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/dto/UserSelfUpdateRequest.java
  - docker-compose.yml
reviewed_at: 2026-10-14T20:45:00Z
iteration: 3
previous_blockers_fixed: 4
previous_warnings_fixed: 2
---

# Phase 9 Code Review — Iteration 3 (FINAL)

## Previous Iterations Summary

**Iteration 1** found 3 blockers + 2 warnings:
- B1: V3 migration schema mismatch (display_name column) → FIXED (3a76411)
- B2: Frontend/backend type mismatch (first_name/last_name vs display_name) → FIXED (d2fc069)
- B3: HTTP method mismatch (PATCH vs PUT) → FIXED (d2fc069)
- W1: Fragile 404 error detection using string matching → FIXED (23d892e)
- W2: Keycloak port documentation mismatch → FIXED (73fde83)

**Iteration 2** found 1 blocker:
- B4: E2E test mocks incompatible with fixed User type → FIXED (e509fc3)
  - All 5 test mocks updated to use display_name instead of first_name/last_name
  - HTTP method check changed from PATCH to PUT
  - Test assertions updated to verify display_name field

**Iteration 3** (this review) verifies all fixes and checks for regressions.

---

## Fix Verification

### All Previous Findings — VERIFIED FIXED ✓

**B1, B2, B3, B4, W1, W2** — All source code changes correctly applied:
- ✓ V3 migration uses `display_name` column (V3__seed_test_user.sql:13,24)
- ✓ Frontend User type uses `display_name: string` (types.ts:68)
- ✓ MyAccount component reads `user?.display_name` (MyAccount.tsx:81)
- ✓ updateMe uses PUT method (users.ts:36, UserController.java:132)
- ✓ All 6 E2E test mocks use `display_name` (auth-flows.spec.ts lines 141, 177, 189, 229, 258, 295)
- ✓ E2E mock checks `method() === 'PUT'` (auth-flows.spec.ts:181)
- ✓ Test assertions verify `display_name` field (auth-flows.spec.ts:218)
- ✓ Error handling uses `?.response?.status === 404` (MyAccount.tsx:122, 130)
- ✓ Keycloak port 8180 documented correctly (docker-compose.yml:59, V3 migration:5)

TypeScript compilation passes without errors. No `first_name` or `last_name` references remain in frontend code.

---

## BLOCKERs

### B5: Missing @JsonProperty annotation on UserResponse.displayName — frontend/backend integration will fail
- **File:** services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/dto/UserResponse.java:14
- **Category:** integration
- **Evidence:**
  
  **Backend serialization:**
  ```java
  public record UserResponse(
          String id,
          String email,
          String displayName,  // ← NO @JsonProperty annotation
          List<String> roles,
          Instant createdAt,
          Instant updatedAt
  ) {}
  ```
  
  Spring Boot's default Jackson configuration serializes Java field names as-is → backend returns JSON:
  ```json
  { "id": "...", "email": "...", "displayName": "John Doe", ... }
  ```
  
  **Frontend expects snake_case:**
  ```typescript
  export interface User {
    id: string;
    email: string;
    display_name: string;  // ← expects display_name, not displayName
    deleted_at: string | null;
  }
  ```
  
  **Result:** MyAccount.tsx:81 reads `user?.display_name` → `undefined` → displays fallback "—" instead of actual name.
  
  **Evidence from other services:** Booking service DTOs use `@JsonProperty` for snake_case (BookingReadDtos.java:33,68-70, BookingDtos.java:88-90):
  ```java
  @JsonProperty("location_id") UUID locationId,
  @JsonProperty("start_time") Instant startTime,
  ```
  
  This is the project-wide naming convention for API contracts (snake_case JSON ↔ camelCase Java).
  
  **Why tests didn't catch this:**
  - All E2E tests use mocked responses with frontend-expected format (`display_name`)
  - Backend gate was skipped (09-GATE.md:99) — no integration tests ran
  - Frontend unit tests don't call real backend
  
  **Runtime failure scenario:**
  1. User opens /account page
  2. Frontend calls GET /users/me
  3. Backend returns `{ "displayName": "John Doe" }`
  4. Frontend TypeScript interface expects `display_name`
  5. `user.display_name` is `undefined` at runtime (TypeScript can't prevent this — response is untyped JSON)
  6. MyAccount.tsx:81 displays "—" instead of "John Doe"
  7. User tries to edit name → same issue
  
- **Fix direction:**
  
  Add `@JsonProperty("display_name")` annotation to UserResponse.displayName field. Also add for createdAt/updatedAt if they're exposed in API (check if frontend uses them):
  ```java
  import com.fasterxml.jackson.annotation.JsonProperty;
  
  public record UserResponse(
          String id,
          String email,
          @JsonProperty("display_name") String displayName,
          List<String> roles,
          @JsonProperty("created_at") Instant createdAt,
          @JsonProperty("updated_at") Instant updatedAt
  ) {}
  ```
  
  Verify: check frontend types.ts User interface fields and ensure all backend fields have matching @JsonProperty annotations.

### B6: Missing @JsonProperty annotation on UserSelfUpdateRequest.displayName — frontend PUT requests will fail
- **File:** services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/dto/UserSelfUpdateRequest.java:10
- **Category:** integration
- **Evidence:**
  
  **Backend request DTO:**
  ```java
  public record UserSelfUpdateRequest(String displayName) {}
  ```
  
  Spring Boot's default Jackson deserialization expects camelCase field names → backend expects:
  ```json
  { "displayName": "Updated Name" }
  ```
  
  **Frontend sends snake_case:**
  ```typescript
  // MyAccount.tsx:54
  usersApi.updateMe({ display_name: name.trim() });
  
  // users.ts:36
  apiClient.put<User>('/users/me', data)
  ```
  
  Frontend sends:
  ```json
  { "display_name": "Updated Name" }
  ```
  
  **Result:** Backend receives request body where `displayName` field is `null` → UserService will either:
  1. Silently ignore the null field (no update occurs, but returns 200) → user sees success banner but name doesn't change
  2. Throw validation error if field has @NotNull constraint → user sees "Failed to save name" error
  
  **Runtime failure scenario:**
  1. User edits name on /account page
  2. Frontend sends PUT /users/me with `{ "display_name": "New Name" }`
  3. Backend UserSelfUpdateRequest deserializes → `displayName` field is `null` (JSON field `display_name` is ignored)
  4. UserService.updateOwnProfile receives request with null displayName → no update occurs OR validation error
  5. User sees error banner or success banner with no actual change
  6. E2E test auth-flows.spec.ts:218 would FAIL if it called real backend (currently mocked, so passes)
  
- **Fix direction:**
  
  Add `@JsonProperty("display_name")` annotation to UserSelfUpdateRequest.displayName field:
  ```java
  import com.fasterxml.jackson.annotation.JsonProperty;
  
  public record UserSelfUpdateRequest(
      @JsonProperty("display_name") String displayName
  ) {}
  ```
  
  Verify: send test PUT request with `{ "display_name": "Test" }` and confirm backend correctly receives the value.

---

## WARNINGs

(none)

---

## Cross-file seams checked

All seams from iteration 2 remain valid. Additional integration checks:

- **Frontend User type ↔ Backend UserResponse:**
  - types.ts:68: `display_name: string` → UserResponse.java:14: `String displayName` → **BLOCKER B5** (missing @JsonProperty)
  - types.ts:66: `id: string` → UserResponse.java:12: `String id` → OK
  - types.ts:67: `email: string` → UserResponse.java:13: `String email` → OK
  - types.ts:69: `deleted_at: string | null` → UserResponse.java:17: not exposed (only createdAt/updatedAt) → OK (backend filters this field)

- **Frontend PUT /users/me request ↔ Backend UserSelfUpdateRequest:**
  - MyAccount.tsx:54: sends `{ display_name: name.trim() }` → UserSelfUpdateRequest.java:10: expects `displayName` → **BLOCKER B6** (missing @JsonProperty)
  - users.ts:36: uses PUT method → UserController.java:132: `@PutMapping("/me")` → OK

- **Database schema ↔ Java entity:**
  - V1 schema: `display_name VARCHAR(255)` → User.java:26: `@Column(name = "display_name")` → OK
  - V3 seed: `display_name` values → matches schema → OK

- **Java entity ↔ DTO mapping:**
  - User.java:27: `private String displayName` with getter/setter → UserResponse.java:14: `String displayName` → OK (field name matches)
  - But DTO → JSON serialization is broken (B5)

- **E2E test mocks ↔ actual API contract:**
  - All 6 test mocks use snake_case `display_name` → matches frontend expectation → OK
  - But mocks don't match actual backend response (backend will use camelCase without @JsonProperty) → B5
  - Test would pass with mocks but fail with real backend

---

## Regression Analysis

**No fix-introduced regressions detected** in source code. All iteration 1 and iteration 2 fixes are correctly applied and functioning as intended.

**New findings (B5, B6)** are NOT regressions — they are pre-existing integration defects that were hidden by:
1. Mock-based E2E tests (auth-flows.spec.ts) that use frontend-expected format
2. No integration tests calling real backend
3. Backend gate skipped (09-GATE.md:99)

The phase 9 fixes correctly aligned:
- ✓ Frontend internal consistency (User type, API calls, component usage)
- ✓ Backend internal consistency (User entity, DTOs, controller method)
- ✓ Database schema (V1 column, V3 seed data)
- ✓ E2E test mocks (all use display_name, PUT method)

But missed:
- ✗ Frontend ↔ Backend JSON contract (B5: response deserialization, B6: request deserialization)

---

## Review Scope

Per iteration 3 instructions (final review after all fixes):
1. ✓ Reviewed all 6 files from iteration 2's `files_reviewed_list`
2. ✓ Added 2 backend DTO files to review scope (UserResponse.java, UserSelfUpdateRequest.java) — these were not changed by phase 9 but are part of the integration contract
3. ✓ Verified each previous finding (B1-B4, W1-W2) is actually fixed by reading the code
4. ✓ Checked for regressions introduced by fixes → none found
5. ✓ Traced cross-file integration between frontend and backend → found B5, B6

**Integration gap root cause:**
- Phase 9 plans focused on frontend implementation and E2E test alignment
- Backend DTOs (UserResponse, UserSelfUpdateRequest) were implemented in Phase 3 (before naming convention was established)
- Booking service DTOs (Phase 2-4) use @JsonProperty for snake_case — established pattern
- Users service DTOs never updated to match this pattern
- Gap was not caught because backend gate was skipped and E2E tests are mocked

---

## Unresolved Blockers

**B5 and B6 remain open.** These are integration defects that will cause runtime failures when frontend calls real backend. They MUST be fixed before My Account page can work in production or even local integration testing.

**Impact:**
- My Account page will display "—" instead of user's name (B5)
- Name editing will fail silently or with error (B6)
- E2E tests pass with mocks but would fail with real backend

**Recommended fix order:**
1. Fix B5 and B6 by adding @JsonProperty annotations to both DTOs
2. Add integration test that calls real backend (not mocked) to verify JSON contract
3. Run E2E tests against real backend stack (docker compose up) to catch future contract mismatches

This is iteration 3 (FINAL) — per instructions, unresolved blockers are recorded as-is.
