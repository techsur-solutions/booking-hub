---
phase: 09-core-user-interface
verified: 2026-10-14T14:42:31Z
status: gaps_found
score: 5/6 must-haves verified
re_verification:
  previous_status: passed
  previous_score: 6/6
  previous_verified: 2026-10-13T19:11:25Z
  gaps_closed: []
  gaps_remaining:
    - "Truth #6: Authentication - My Account self-editing fails due to frontend/backend JSON contract mismatch"
  regressions: []
  reason: "Code review iteration 3 (FINAL) discovered 2 integration blockers (B5, B6) not caught by initial verification due to mocked E2E tests and skipped backend gate. These are pre-existing defects in Phase 3 DTOs, now surfaced by Phase 9 frontend integration."
gaps:
  - truth: "Authentication: My Account allows self-editing"
    status: failed
    reason: "Frontend/backend JSON contract mismatch - missing @JsonProperty annotations on UserResponse and UserSelfUpdateRequest"
    artifacts:
      - path: "services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/dto/UserResponse.java"
        issue: "B5: Missing @JsonProperty(\"display_name\") on line 14 - backend returns { displayName: \"...\" } but frontend expects { display_name: \"...\" }. MyAccount.tsx:81 reads user?.display_name → undefined → displays fallback \"—\" instead of actual name."
      - path: "services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/dto/UserSelfUpdateRequest.java"
        issue: "B6: Missing @JsonProperty(\"display_name\") on line 10 - frontend sends { display_name: \"...\" } but backend expects { displayName: \"...\" }. Field deserializes as null → name update fails silently or throws validation error."
    missing:
      - "Add @JsonProperty(\"display_name\") annotation to UserResponse.displayName (line 14)"
      - "Add @JsonProperty(\"created_at\") and @JsonProperty(\"updated_at\") annotations to UserResponse if frontend uses these fields"
      - "Add @JsonProperty(\"display_name\") annotation to UserSelfUpdateRequest.displayName (line 10)"
      - "Add import com.fasterxml.jackson.annotation.JsonProperty to both DTO files"
      - "Add integration test that calls real backend (not mocked) to verify JSON contract"
      - "Run E2E tests against real backend stack (docker compose) to catch future contract mismatches"
human_verification:
  - test: "My Account page displays user's name (integration test)"
    expected: "After fixing @JsonProperty, GET /users/me returns { \"display_name\": \"John Doe\" } and MyAccount page displays the name (not \"—\")"
    why_human: "Requires running real backend + frontend integration (not mocked)"
  - test: "My Account name editing works (integration test)"
    expected: "After fixing @JsonProperty, PUT /users/me with { \"display_name\": \"New Name\" } successfully updates the user's name in database"
    why_human: "Requires running real backend + database + frontend integration"
  - test: "DayView slot-click booking creation"
    expected: "Click empty time slot → CreateFormModal opens pre-filled with location and time"
    why_human: "The CreateFormModal intentionally provides simplified form (not full BookingForm) — needs human judgment on feature parity"
  - test: "Display Board full-screen kiosk appearance"
    expected: "Full-screen dark background (#0f172a), approved-only bookings, ApprovedOnlyBadge visible, auto-refreshes every 60s"
    why_human: "CSS minHeight: 100vh and styling can only be confirmed visually in browser"
  - test: "Password Reset end-to-end token validation"
    expected: "Request reset → check email → follow link → complete reset → password changed → redirect to login"
    why_human: "Requires real email delivery and backend token validation"
---

# Phase 9: Core User Interface Verification Report

**Phase Goal:** Build the complete end-user booking experience with calendar/day/list views, booking creation/editing with conflict detection, approval workflow, public display board, and authentication flows, so users can discover, create, manage, and approve bookings with full feature parity to the legacy system.

**Verified:** 2026-10-14T14:42:31Z  
**Status:** gaps_found  
**Re-verification:** Yes — after code review iteration 3 (FINAL) discovered 2 integration blockers

---

## Re-verification Context

**Previous verification:** 2026-10-13T19:11:25Z  
**Previous status:** passed (6/6 must-haves verified)  
**Current status:** gaps_found (5/6 must-haves verified)

**What changed:**
- Code review iteration 3 (09-REVIEW.md) completed after initial verification
- Review discovered **2 BLOCKER integration defects** (B5, B6) in backend DTOs
- These are **pre-existing defects** from Phase 3 (UserResponse, UserSelfUpdateRequest created before project-wide snake_case JSON convention was established)
- Initial verification missed these because:
  1. E2E tests use mocked responses with frontend-expected format (display_name)
  2. Backend gate was skipped (09-GATE.md:99 - no integration tests ran)
  3. Frontend unit tests don't call real backend

**Per workflow rules:** `review_blockers_open: 2` → status CANNOT be `passed`

**No regressions:** All previously verified features (Truths 1-5) remain intact. Gates still green for build/tests (74/74 pass, 208 modules, 5 waves).

---

## Gate Evidence (Step 7c)

Read `.planning/phases/09-core-user-interface/09-GATE.md` and `09-REVIEW.md`.

| Gate Check | Result | Notes |
|---|---|---|
| `gate_status` | **passed** | All 5 waves: build pass, tests pass (74/74) |
| `boot_smoke` | **skipped** | Backend gate skipped — missed integration contract |
| `review_blockers_open` | **2** | 🛑 B5 (UserResponse), B6 (UserSelfUpdateRequest) |
| Build (all 5 waves) | pass | 208 modules transformed |
| Tests (74/74) | pass | 6 test files, vitest |
| Code Review (iteration 3) | **2 blockers, 0 warnings** | B5, B6 are integration defects in Phase 3 DTOs |

**BLOCKER B5** (from 09-REVIEW.md:64-136):
- **File:** UserResponse.java:14
- **Issue:** Missing `@JsonProperty("display_name")` annotation
- **Impact:** Backend returns `{ "displayName": "John Doe" }` (camelCase), frontend expects `{ "display_name": "..." }` (snake_case)
- **Result:** MyAccount.tsx:81 reads `user?.display_name` → `undefined` → displays "—" instead of user's name
- **Evidence:** Project-wide convention uses @JsonProperty for snake_case (CustomFieldDtos.java:34,48,51-53)

**BLOCKER B6** (from 09-REVIEW.md:138-190):
- **File:** UserSelfUpdateRequest.java:10
- **Issue:** Missing `@JsonProperty("display_name")` annotation
- **Impact:** Frontend sends `{ "display_name": "Updated Name" }`, backend expects `{ "displayName": "..." }`
- **Result:** Backend deserializes `displayName` field as `null` → name update fails silently or throws validation error
- **Evidence:** MyAccount.tsx:54 sends `{ display_name: name.trim() }`, users.ts:36 uses PUT method

**Why tests didn't catch this:**
- All 6 E2E test mocks (auth-flows.spec.ts) use snake_case `display_name` → match frontend expectation
- But mocks don't match actual backend response (backend will use camelCase without @JsonProperty)
- Tests pass with mocks but would fail with real backend

---

## Goal Achievement

### Observable Truths (from ROADMAP.md Success Criteria)

| # | Truth | Status | Evidence |
|---|---|---|---|
| 1 | **Foundation & Navigation**: App shell with sidebar navigation, permission-gated visibility (structurally absent, not disabled), StateConfirmationBanner, ConflictBadge, API client with JWT handling and error boundaries | ✓ VERIFIED | **No regression.** Sidebar.tsx reads realmAccess.roles; UserMenu.tsx calls keycloak.logout(); StateConfirmationBanner.tsx exists (74 lines); ConflictBadge.tsx exists; client.ts injects Bearer token via Axios interceptor; ErrorBoundary.tsx is class component with getDerivedStateFromError |
| 2 | **Booking Views**: CalendarView (FullCalendar month/week), DayView (hourly slots), ListView (filterable table) all render bookings with identical location colour-coding and conflict badges; view state preserved in URL params | ✓ VERIFIED | **No regression.** FullCalendarWrapper.tsx sets backgroundColor: location?.colour; HourlySlotGrid.tsx builds locationColourMap; BookingTable.tsx renders location-dot with location.colour; ConflictBadge present in all 3 views; useViewState.ts reads/writes all view params via useSearchParams() |
| 3 | **Booking CRUD**: Create Form with custom fields, conflict detection, recurring patterns; Edit Form with explicit scope selection; Detail Modal with permission-gated actions; Conflict UI hard-blocks non-approvers, soft-warns approvers | ✓ VERIFIED | **No regression.** BookingForm.tsx imports CustomFieldRenderer + ConflictWarning; RecurringPattern.tsx handles recurring input; BookingEdit.tsx uses RecurringScopeDialog; BookingDetailModal.tsx uses PermissionGate; ConflictBanner.tsx has hard-block (data-testid="conflict-banner-hard-block") and soft-warn (data-testid="conflict-banner-soft-warn") |
| 4 | **Approval Workflow**: Approval Queue (visible only to role_booking_approver) lists pending bookings with inline conflict badges; Approve/Deny are one-way with explicit confirmation; conflicted bookings require approval confirmation dialog | ✓ VERIFIED | **No regression.** /approvals route wraps PermissionGate requireRole="role_booking_approver"; ApprovalTable.tsx renders ConflictBadge; ApproveButton.tsx has showConflictConfirm dialog (data-testid="approve-conflict-dialog"); DenyButton.tsx has reason textarea + bookingsApi.denyBooking(id, reason) |
| 5 | **Public Surfaces**: Display Board renders full-screen approved-only bookings with visible guarantee badge and auto-refresh; Feed Subscription UI lists RSS2/iCal/JSON URLs with copy buttons | ✓ VERIFIED | **No regression.** DisplayBoard.tsx uses minHeight: '100vh', calls feedsApi.getDisplayBoard() (approved-only endpoint), ApprovedOnlyBadge imported and rendered, setInterval(fetchBookings, AUTO_REFRESH_MS) every 60s; FeedSubscription.tsx renders feed URLs with copy-to-clipboard |
| 6 | **Authentication**: Login redirects to Keycloak PKCE flow with "Remember me"; My Account allows self-editing; Password Reset flows work end-to-end | ✗ FAILED | **Login + Password Reset:** ✓ Still verified (Login.tsx calls keycloak.login with PKCE; open-redirect fixed; rememberMe_pending consumed in AuthProvider.tsx; PasswordReset.tsx + PasswordResetComplete.tsx exist). **My Account:** ✗ FAILED due to frontend/backend JSON contract mismatch. **B5:** UserResponse.java:14 missing @JsonProperty("display_name") → backend returns camelCase, frontend expects snake_case → MyAccount.tsx:81 displays "—" instead of user's name. **B6:** UserSelfUpdateRequest.java:10 missing @JsonProperty("display_name") → frontend sends snake_case, backend expects camelCase → name update fails. Evidence: grep confirms no @JsonProperty annotations in either DTO; project-wide pattern (CustomFieldDtos.java:34,48,51-53) uses @JsonProperty for snake_case. |

**Score:** 5/6 truths verified (Truth 6 blocked by B5, B6)

---

## Required Artifacts

### Key Artifacts Status (Focused Re-verification)

**Previously verified artifacts (Truths 1-5):** Quick regression check confirms all key files still exist:
- ✓ Sidebar.tsx, StateConfirmationBanner.tsx, ConflictBadge.tsx, client.ts (4 files)
- ✓ CalendarView.tsx, DayView.tsx, ListView.tsx (3 files)
- ✓ BookingForm.tsx, ConflictBanner.tsx, BookingDetailModal.tsx (3 files)
- ✓ ApprovalQueue.tsx, ApproveButton.tsx (2 files)
- ✓ DisplayBoard.tsx, FeedSubscription.tsx (2 files)

**Full artifact table from previous verification remains valid** — see 09-VERIFICATION.md (previous) lines 53-86 for complete list.

### Failed Artifacts (Truth 6 - My Account integration)

| Artifact | Expected | Status | Details |
|---|---|---|---|
| `services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/dto/UserResponse.java` | Backend DTO with snake_case JSON serialization | ✗ FAILED | **B5:** Line 14: `String displayName` lacks `@JsonProperty("display_name")` annotation. Spring Boot Jackson default serializes as camelCase → backend returns `{ "displayName": "..." }` but frontend types.ts:68 expects `{ "display_name": "..." }`. Confirmed: grep shows no @JsonProperty import or annotation. Project-wide pattern (CustomFieldDtos.java:34,48,51-53) uses @JsonProperty for all snake_case fields. |
| `services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/dto/UserSelfUpdateRequest.java` | Backend request DTO with snake_case JSON deserialization | ✗ FAILED | **B6:** Line 10: `String displayName` lacks `@JsonProperty("display_name")` annotation. Spring Boot Jackson default deserializes from camelCase → backend expects `{ "displayName": "..." }` but frontend MyAccount.tsx:54 sends `{ "display_name": "..." }` via users.ts:36. Confirmed: grep shows no @JsonProperty import or annotation. Field will deserialize as null → name update fails silently or validation error. |
| `frontend/src/api/types.ts` | Frontend User interface matching backend JSON | ⚠️ PARTIAL | Line 68: `display_name: string` is correct snake_case format, consistent with project-wide convention. Issue is backend DTOs don't match this format. Frontend is correct; backend is wrong. |
| `frontend/src/pages/MyAccount.tsx` | My Account page with self-editing | ⚠️ PARTIAL | Lines 54,64,81,256: Correctly uses `display_name` throughout (reads user?.display_name, sends { display_name: name.trim() }). Component implementation is correct; will work once backend DTOs are fixed. |

---

## Key Link Verification

### Previously Verified Links (Truths 1-5)

All key links from previous verification remain WIRED. No regressions detected. See 09-VERIFICATION.md (previous) lines 89-106 for complete table.

### Failed Links (Truth 6 - My Account integration)

| From | To | Via | Status | Details |
|---|---|---|---|---|
| `frontend User type (types.ts:68)` | `backend UserResponse (UserResponse.java:14)` | JSON serialization | ✗ NOT_WIRED | **B5:** Frontend expects `display_name` (snake_case), backend returns `displayName` (camelCase). Missing @JsonProperty annotation breaks contract. Confirmed: frontend reads user?.display_name → undefined at runtime → displays "—". |
| `frontend MyAccount PUT request (MyAccount.tsx:54, users.ts:36)` | `backend UserSelfUpdateRequest (UserSelfUpdateRequest.java:10)` | JSON deserialization | ✗ NOT_WIRED | **B6:** Frontend sends `{ display_name: "..." }` (snake_case), backend expects `{ displayName: "..." }` (camelCase). Missing @JsonProperty annotation breaks contract. Confirmed: backend deserializes displayName field as null → update fails. |

---

## Requirements Coverage

All requirements from previous verification (UI-NAV-01/02, UI-PATTERN-01/02/03, UI-CAL-01/02/03/04, UI-BOOK-01/02/03/04, UI-APPR-01/02, UI-PUB-01/02) remain SATISFIED except:

| Requirement | Status | Blocking Issue |
|---|---|---|
| UI-AUTH-03: My Account self-editing | ✗ BLOCKED | **B5 + B6:** Frontend/backend JSON contract mismatch prevents name display and editing. Frontend correctly sends/expects snake_case; backend DTOs missing @JsonProperty annotations. |

---

## Anti-Patterns Found

No new anti-patterns found. The DayView CreateFormModal placeholder comment (ℹ️ Info severity) from previous verification remains cosmetic and non-blocking.

The B5/B6 issues are **integration defects** (missing annotations), not code anti-patterns.

---

## Behavioral Spot-Checks

| Check | Command | Output | Result |
|---|---|---|---|
| Build succeeds | Wave 5 (09-GATE.md:93) | `208 modules transformed. ✓ built in 165ms` | ✓ PASS |
| All 74 unit tests pass | Wave 5 (09-GATE.md:95) | `Test Files 6 passed (6) / Tests 74 passed (74)` | ✓ PASS |
| Frontend DTOs use snake_case | `grep display_name frontend/src/api/types.ts` | Line 68: `display_name: string` | ✓ PASS |
| Frontend sends snake_case | `grep display_name frontend/src/api/users.ts frontend/src/pages/MyAccount.tsx` | updateMe sends `{ display_name: name.trim() }` (line 54); reads `user?.display_name` (lines 64, 81) | ✓ PASS |
| Backend DTOs missing @JsonProperty | `grep JsonProperty services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/dto/UserResponse.java UserSelfUpdateRequest.java` | (no output - no annotations found) | ✗ FAIL |
| Project-wide pattern uses @JsonProperty | `grep "@JsonProperty" services/custom-field-service/src/main/java/com/bookinghub/customfield/dto/CustomFieldDtos.java` | Lines 34,48,51-53: `@JsonProperty("field_type")`, `@JsonProperty("created_at")`, etc. | ✓ PASS (confirms pattern) |

**Integration spot-check (would fail if run with real backend):**
- E2E test auth-flows.spec.ts uses mocked `/users/me` response with `display_name` (snake_case) → test passes
- Real backend GET /users/me returns `displayName` (camelCase) → frontend reads undefined → test would fail
- Real frontend PUT /users/me sends `display_name` (snake_case) → backend receives null → test would fail

---

## Human Verification Required

### Integration Tests (new - required after fix)

#### 1. My Account page displays user's name (integration test)

**Test:** Start docker compose stack → login as test user → navigate to /account page → verify name displays correctly (not "—")  
**Expected:** After fixing @JsonProperty annotations, GET /users/me returns `{ "display_name": "John Doe" }` and MyAccount page displays "John Doe" in the header (MyAccount.tsx:256)  
**Why human:** Requires running real backend + frontend integration (not mocked). Current E2E tests use mocked responses that hide the contract mismatch.

#### 2. My Account name editing works (integration test)

**Test:** On /account page → click Edit button → change name → click Save → verify success banner → refresh page → verify name persists  
**Expected:** After fixing @JsonProperty annotations, PUT /users/me with `{ "display_name": "New Name" }` successfully updates user's display_name column in database, returns updated user object, and MyAccount page shows the new name  
**Why human:** Requires running real backend + database + frontend integration. Current E2E tests mock the PUT endpoint and don't verify actual database persistence.

### Previously Identified Human Tests (still valid)

#### 3. DayView slot-click booking creation

**Test:** Click an empty time slot in DayView — confirm a modal opens pre-filled with that location and time.  
**Expected:** CreateFormModal opens with correct date/time; the "Full booking form" placeholder note is visible but the slot-click UX works end-to-end.  
**Why human:** The CreateFormModal intentionally provides a simplified form (not the full BookingForm) — needs human judgment on whether this meets "feature parity" requirements for slot-click creation specifically.

#### 4. Display Board full-screen kiosk appearance

**Test:** Navigate to /display-board on a real browser session.  
**Expected:** Full-screen dark background (#0f172a), approved-only bookings visible, ApprovedOnlyBadge visible, auto-refreshes every 60s.  
**Why human:** CSS minHeight: 100vh and styling can only be confirmed visually in a browser.

#### 5. Password Reset end-to-end token validation

**Test:** Request password reset → check email → follow link → complete reset.  
**Expected:** Token validated, password changed, redirect to login.  
**Why human:** Requires real email delivery and backend token validation; can't be verified statically.

---

## Gaps Summary

**Status changed from `passed` to `gaps_found`** due to 2 integration blockers discovered in code review iteration 3.

**Root cause:** Phase 3 (users-permissions-service) implemented UserResponse and UserSelfUpdateRequest DTOs before project-wide snake_case JSON convention was established. Other services (custom-field-service, booking-service) use `@JsonProperty` annotations for all snake_case fields. Users service DTOs were never updated to match this pattern.

**Impact:**
- **B5 (UserResponse):** My Account page displays "—" instead of user's display name (GET /users/me returns wrong field name)
- **B6 (UserSelfUpdateRequest):** Name editing fails silently or with validation error (PUT /users/me receives null field)
- E2E tests pass with mocks but would fail with real backend
- Phase 9 frontend implementation is correct; backend DTOs are the issue

**Why initial verification missed this:**
1. All E2E tests (auth-flows.spec.ts) use mocked responses with frontend-expected format (`display_name`)
2. Backend gate was skipped (09-GATE.md:99 - boot_smoke: skipped, backend build/test not run)
3. Frontend unit tests don't call real backend
4. Code review iteration 3 was the first time the DTO files were inspected

**Fixes required:**
1. Add `@JsonProperty("display_name")` to UserResponse.displayName (line 14)
2. Add `@JsonProperty("created_at")` and `@JsonProperty("updated_at")` to UserResponse if frontend uses these (lines 16-17)
3. Add `@JsonProperty("display_name")` to UserSelfUpdateRequest.displayName (line 10)
4. Add `import com.fasterxml.jackson.annotation.JsonProperty;` to both files
5. Add integration test that calls real backend to verify JSON contract
6. Run E2E tests against real backend stack (docker compose up) to catch future contract mismatches

**No regressions:** All other truths (1-5) remain verified. Gates still green for build/tests (74/74 pass). This is a targeted integration fix in 2 backend DTO files.

---

_Verified: 2026-10-14T14:42:31Z_  
_Verifier: Claude (pivota_spec-verifier)_
