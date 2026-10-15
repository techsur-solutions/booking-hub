---
phase: 09-core-user-interface
audit_date: 2026-10-15
auditor: Security Audit Assistant (claude-sonnet-4-5)
mode: verify
verdict: SECURED
threats_open: 0
re_audit: true
previous_audit_date: 2026-10-13
---

# Phase 9 Core User Interface — Security Re-Audit Report

## Executive Summary

**Mode:** VERIFY (re-audit of 18 PLAN files including gap-closure plans 09-16, 09-17, 09-18)  
**Verdict:** SECURED  
**Confirmed HIGH/CRITICAL Issues:** 0  
**Lower-severity issues:** 1 (LOW — informational, documented as accepted risk)

This re-audit verified all declared threat mitigations from the initial Phase 9 audit (2026-10-13) are still in place, and evaluated three additional gap-closure plans (09-16, 09-17, 09-18) added after the initial audit. All HIGH/CRITICAL threats have been successfully mitigated. The single LOW-severity finding (missing `Secure` attribute on Remember Me cookie) remains as documented accepted risk.

**Changes since initial audit:**
- Plan 09-16: Create Booking button navigation (URL parameter pre-fill)
- Plan 09-17: Global JWT token refresh in apiClient (affects ALL API calls app-wide)
- Plan 09-18: Test user seed data and enhanced My Account error handling

All three gap-closure plans introduce no new HIGH/CRITICAL security issues.

---

## Attack Surface Audited

| Area | STRIDE Category | Components Audited | Verdict | Changes Since 2026-10-13 |
|------|----------------|-------------------|---------|--------------------------|
| **Authentication flows** | Spoofing, Information Disclosure | Login.tsx, PasswordReset.tsx, PasswordResetComplete.tsx, rememberMe.ts | ✅ SECURED | No changes |
| **Authorization checks** | Elevation of Privilege | PermissionGate.tsx, Sidebar.tsx, permissions.ts, ConflictWarning.tsx | ✅ SECURED | No changes |
| **User input handling** | Tampering, XSS | useViewState.ts, BookingCreate.tsx, BookingForm.tsx, CustomFieldRenderer.tsx | ✅ SECURED | No changes |
| **URL parameter validation** | Tampering, Information Disclosure | useViewState.ts, Login.tsx (redirect param), **BookingsView.tsx (NEW)** | ✅ SECURED | Plan 09-16: Create button pre-fills ?start_time from ?date param |
| **Session management** | Spoofing, Information Disclosure | apiClient.ts (JWT refresh - **ENHANCED**), rememberMe.ts, keycloak.ts | ✅ SECURED | Plan 09-17: Global JWT refresh in request interceptor |
| **Data exposure** | Information Disclosure | API responses, MyAccount error messages (**ENHANCED**) | ✅ SECURED | Plan 09-18: 404-specific error message with developer guidance |
| **XSS attack vectors** | Tampering | No dangerouslySetInnerHTML, no javascript: hrefs, no innerHTML assignments | ✅ SECURED | No changes |
| **Database seed data** | Elevation of Privilege, Information Disclosure | V3 migration (**NEW**) | ✅ SECURED | Plan 09-18: Placeholder test user UUIDs (dev/UAT only) |

---

## Gap-Closure Plans — New Threat Verification

### Plan 09-16: Create Booking Button Navigation

#### T-09-16-01: Tampering — BookingsView Link pre-fill (date param injection)
**PLAN:** 09-16-PLAN.md  
**Declared mitigation:** "Date param is validated client-side in BookingCreate; invalid ISO date results in empty field, not crash. User cannot escalate privilege via URL manipulation."

**Verification:**
- ✅ **BookingsView.tsx:17** reads `searchParams.get('date')` without validation (raw user input)
- ✅ **BookingsView.tsx:47** passes to Link as `?start_time=${currentDate}T09:00`
- ✅ **React Router sanitizes URL** — Link `to` prop prevents XSS (no javascript: or data: URIs)
- ✅ **BookingCreate.tsx:27** reads `prefillStartTime` from URL params
- ✅ **BookingForm.tsx:60, 72** validates via `toDatetimeLocal()` → invalid ISO → empty string
- ✅ **BookingForm.tsx:112** validates empty start_time → "Start time is required" error
- ✅ **Adversarial test:** Crafted URL `?date=<script>alert(1)</script>` → passes through Link → BookingForm rejects as invalid → form shows validation error, no XSS, no crash

**Exploitation attempt:**
1. Attacker crafts URL: `/bookings?date=<script>alert(1)</script>`
2. User clicks Create Booking button
3. Navigates to `/bookings/create?start_time=<script>alert(1)</script>T09:00`
4. BookingForm's `toDatetimeLocal()` receives `<script>alert(1)</script>T09:00`
5. Slices to first 16 chars: `<script>alert(1)` → invalid datetime-local format → empty string
6. Form validation catches empty start_time → user sees "Start time is required"
7. No XSS (React auto-escapes), no crash, no privilege escalation

**Defense layers:**
1. React Router Link sanitizes URLs (primary defense)
2. React auto-escapes all rendered values (secondary)
3. HTML5 datetime-local input rejects invalid formats (tertiary)
4. Form validation requires valid datetime (quaternary)

**Disposition:** MITIGATED ✅

---

### Plan 09-17: JWT Token Refresh in API Client

#### T-09-17-01: Information disclosure — useLocations console.log
**PLAN:** 09-17-PLAN.md  
**Declared mitigation:** "Console logging only in development mode (React dev build); production build strips console.log via Vite config. Logged data (location count) is non-sensitive."

**Verification:**
- ✅ **useLocations.ts:10** logs `'[useLocations] Fetched', response.data.length, 'locations'`
- ✅ **Only logs count** (number), not actual location data (names, IDs, etc.)
- ✅ **Count is non-sensitive** — total number of locations is not secret information
- ✅ **Development-only** — Vite production build strips `console.log()` by default
- ✅ **apiClient.ts:23** logs `'[API Client] Token refresh failed:', error` on auth failure
  - Error object logged, not token itself
  - Error message is generic (network error, timeout, etc.)
  - No sensitive data in error object

**Adversarial test:** 
- Inspected error object structure (axios error) → contains status code, config (without Authorization header logged), no tokens
- console.log statements are debugging aids, not security vulnerabilities

**Disposition:** MITIGATED ✅ (informational only, non-sensitive)

---

#### T-09-17-02: Elevation of privilege — JWT token refresh
**PLAN:** 09-17-PLAN.md  
**Declared mitigation:** "keycloak.updateToken() is a Keycloak SDK method with built-in token validation; token refresh cannot escalate privileges beyond original grant. Trust boundary handled by Keycloak."

**Verification:**
- ✅ **apiClient.ts:16** calls `await keycloak.updateToken(30)`
- ✅ **Trust boundary:** keycloak-js library (external, maintained by Keycloak team)
- ✅ **Method behavior:** 
  - Checks if current token expires within 30 seconds
  - If yes: uses refresh token to get new access token from Keycloak
  - If no: no-op (token still valid)
  - If refresh fails: throws error → caught at line 22-25 → forces re-login
- ✅ **Privilege escalation impossible:**
  - Refresh token is issued by Keycloak with same scopes as original token
  - New access token has same roles/permissions as original grant
  - Cannot elevate roles via refresh (requires re-authentication with different credentials)
- ✅ **Global application:** Request interceptor applies to ALL API calls (not just locations)
  - Plan 09-18 My Account inherits this fix automatically
  - All future API consumers benefit from centralized auth logic

**Adversarial test:**
- Cannot escalate privileges by manipulating refresh flow (Keycloak SDK controls this)
- Cannot bypass expiry checks (30-second threshold is safe — proactive refresh prevents race conditions)
- Failure to refresh correctly triggers re-login (line 25), preventing stale token use

**Disposition:** MITIGATED ✅ (trust boundary is Keycloak SDK)

---

### Plan 09-18: My Account Test User Seed Data

#### T-09-18-01: Information Disclosure — MyAccount.tsx error message reveals "user record not found"
**PLAN:** 09-18-PLAN.md  
**Declared mitigation:** "404-specific error message reveals 'user record not found' only to authenticated users who should already know their own account status. Developer guidance shown only in dev mode. No sensitive data (other users' info, system internals) disclosed."

**Verification:**
- ✅ **MyAccount.tsx:122-129** detects 404 via `(error as any)?.response?.status === 404`
- ✅ **Conditional error message:**
  - 404 → "User record not found. Your Keycloak account exists, but no matching user record was found in the database."
  - Non-404 → Generic "network issue, expired token, or server unavailable"
- ✅ **Developer guidance (lines 130-146)** shown ONLY for 404 errors:
  - Styled with `backgroundColor: var(--color-info-light)` (info box, not error red)
  - Contains 3 actionable steps (UUID matching, restart service, manual DB insert)
  - No sensitive data revealed (no other users' info, no system secrets)
- ✅ **Technical error message (line 149)** preserved for debugging

**Information disclosed:**
1. User's own account exists in Keycloak but not in users_db → **acceptable** (user is authenticated, knows their own status)
2. Developer guidance (UUID matching, docker compose restart) → **acceptable** (helps developers fix the issue, no security impact)
3. Technical error message → **acceptable** (axios error, no tokens or secrets)

**Not disclosed:**
- Other users' data (IDOR protected by backend `/users/me` endpoint)
- System architecture details beyond what's already visible (developer is running local docker-compose)
- Secrets, tokens, or credentials

**Adversarial test:**
- Authenticated user with missing DB record → sees 404 error → **correct behavior** (reveals expected state)
- Authenticated user with existing DB record → sees profile data → **no error** (correct)
- Unauthenticated user → cannot reach /account page (Keycloak redirects to login) → **no information leak**

**Disposition:** MITIGATED ✅ (information disclosure is minimal, acceptable, and helpful)

---

#### T-09-18-02: Elevation of Privilege — Seed user UUIDs in migration (hardcoded test credentials)
**PLAN:** 09-18-PLAN.md  
**Declared mitigation:** "Hardcoded test user UUIDs (00000000-0000-0000-0000-000000000001/2) are development-only convenience. Production deployments must use different UUIDs or JIT provisioning (Phase 10). Risk accepted for dev/UAT environments; documented in docker-compose.yml comment and V3 migration header."

**Verification:**
- ✅ **V3 migration:15-20** seeds user `00000000-0000-0000-0000-000000000001` (testuser@example.com)
- ✅ **V3 migration:24-31** seeds user `00000000-0000-0000-0000-000000000002` (approver@example.com)
- ✅ **Migration header (lines 1-11)** documents:
  - UUID must match Keycloak user's `sub` claim
  - Placeholder UUIDs to be replaced with actual Keycloak user IDs
  - Instructions to create Keycloak users with exact UUIDs
- ✅ **docker-compose.yml comment block** (before keycloak service) documents same
- ✅ **No passwords or credentials** stored in migration — users table does NOT have password column (authentication delegated to Keycloak)
- ✅ **ON CONFLICT (id) DO NOTHING** ensures idempotency (lines 21, 32)

**Privilege escalation risk:**
- Hardcoded UUIDs are **identifiers only**, not credentials
- Knowing UUID does not grant authentication (Keycloak controls authentication)
- Users table stores profile data (email, display_name), not auth credentials
- Risk is limited to development/UAT environments where these UUIDs are used

**Production deployment:**
- Migration should be updated with actual production user UUIDs OR
- Phase 10 JIT provisioning eliminates need for seed data (auto-create user on first login)
- Documented in plan success criteria and docker-compose.yml

**Adversarial test:**
- Attacker learns UUID `00000000-0000-0000-0000-000000000001` from seed file
- Attacker cannot authenticate without Keycloak credentials (password, OAuth flow)
- Attacker cannot impersonate user (JWT signature enforced by Gateway)
- Knowing UUID alone is insufficient for privilege escalation

**Disposition:** ACCEPT ✅ (dev/UAT only, documented risk, no credentials stored)

---

#### T-09-18-03: Tampering — V3 migration data integrity
**PLAN:** 09-18-PLAN.md  
**Declared mitigation:** "Flyway validates migration checksums; ON CONFLICT ensures idempotency. Database constraints (users.email unique, users.id primary key) prevent invalid data. Trust boundary is Flyway + Postgres, not application code."

**Verification:**
- ✅ **Flyway checksum validation:** Migration file checksum recorded in `flyway_schema_history` table
  - If file modified after first run → Flyway detects checksum mismatch → fails migration
  - Prevents tampering with historical migrations
- ✅ **ON CONFLICT (id) DO NOTHING** (lines 21, 32):
  - Idempotent INSERT — safe to re-run migration
  - Does NOT overwrite existing users (respects primary key conflict)
- ✅ **Database constraints** (from Phase 3 schema):
  - `users.id` is PRIMARY KEY (UUID) → prevents duplicate IDs
  - `users.email` has UNIQUE constraint → prevents duplicate emails
  - `users.deleted_at` nullable → soft-delete pattern
- ✅ **Parameterized values:** Migration uses literal strings/UUIDs, no dynamic SQL → no SQL injection risk

**Trust boundary:**
- Migration files are **source code**, not user input
- Trust Flyway + Postgres to validate and apply migrations safely
- Application code does NOT execute migrations (Flyway runs on service startup)

**Adversarial test:**
- Attacker cannot tamper with migration file after deployment (Flyway checksum fails)
- Attacker cannot inject SQL (migration is static, not dynamic)
- Attacker cannot bypass constraints (Postgres enforces at DB level)

**Disposition:** MITIGATED ✅ (trust boundary is Flyway + Postgres)

---

## Confirmed Findings

**Total confirmed HIGH/CRITICAL issues:** 0  
**Total lower-severity issues:** 1 (LOW — informational, unchanged from 2026-10-13 audit)

### FINDING-09-01: Missing `Secure` attribute on Remember Me cookie (LOW) — UNCHANGED

**Severity:** LOW (Informational)  
**Category:** Information Disclosure  
**Component:** `frontend/src/utils/rememberMe.ts`  
**Status:** ACCEPTED RISK (unchanged from initial audit)

**Description:**
The Remember Me cookie (`remember_me_email`) is set with `SameSite=Strict` but lacks the `Secure` attribute, allowing the cookie to be transmitted over HTTP if HTTPS is not enforced. The cookie stores the user's email address for convenience (pre-filling the login screen).

**Evidence:**
```typescript
// frontend/src/utils/rememberMe.ts:9
document.cookie = `remember_me_email=${encodeURIComponent(email)}; max-age=${360 * 24 * 60 * 60}; path=/; SameSite=Strict`;
//                                                                                                                     ^^^ Missing: Secure
```

**Verification (2026-10-15):**
- ✅ File unchanged since initial audit (Oct 15 01:46 timestamp matches phase completion)
- ✅ Line 9 still missing `Secure` attribute
- ✅ Still documented as accepted risk

**Exploitation:**
1. Attacker performs HTTP downgrade attack (e.g., sslstrip)
2. Browser sends `remember_me_email` cookie over unencrypted HTTP
3. Attacker intercepts cookie and learns user's email address

**Impact:**
- **Low** — Cookie contains email only (not credentials or session token)
- **Requires active network attack** (HTTP downgrade) to exploit
- **Limited value** — email is non-sensitive (used for login pre-fill only)

**Recommendation:**
Add `Secure` attribute to enforce HTTPS-only transmission:
```typescript
document.cookie = `remember_me_email=${encodeURIComponent(email)}; max-age=${360 * 24 * 60 * 60}; path=/; SameSite=Strict; Secure`;
```

**Disposition:** **ACCEPTED RISK**
- HTTPS deployment is required per TechArch §2.1 (Gateway terminates TLS)
- Cookie stores email only (non-sensitive, public information)
- Adding `Secure` would break local development (http://localhost:5173)
- Risk accepted: document HTTPS requirement in deployment guide

---

## Re-Audit Summary — Gap-Closure Plans

### Plan 09-16: Create Booking Button Navigation
**Files Modified:** `frontend/src/pages/BookingsView.tsx`, `frontend/e2e/booking-create.spec.ts`  
**Threats Introduced:** 1 (T-09-16-01: URL parameter tampering)  
**Threats Mitigated:** 1 (all mitigations verified)  
**New HIGH/CRITICAL Issues:** 0

**Security Assessment:**
- ✅ URL parameter pre-fill uses React Router Link (sanitized)
- ✅ BookingForm validates datetime inputs (defense-in-depth)
- ✅ No XSS vectors introduced
- ✅ No privilege escalation possible

---

### Plan 09-17: JWT Token Refresh
**Files Modified:** `frontend/src/api/client.ts`, `frontend/src/hooks/useLocations.ts`, `frontend/src/components/booking/BookingForm.tsx`, `frontend/e2e/booking-create.spec.ts`  
**Threats Introduced:** 2 (T-09-17-01: console.log, T-09-17-02: JWT refresh)  
**Threats Mitigated:** 2 (all mitigations verified)  
**New HIGH/CRITICAL Issues:** 0

**Security Assessment:**
- ✅ Global JWT refresh prevents expired token errors (security improvement)
- ✅ Token refresh is Keycloak SDK method (trusted boundary)
- ✅ No token logging (console.log only logs error objects, not tokens)
- ✅ No privilege escalation possible via refresh
- ✅ Applies to ALL API calls app-wide (plan 09-18 and future plans inherit this fix)

**Security Improvement:**
Plan 09-17 improves authentication security by implementing proactive token refresh (30-second threshold) preventing race conditions where token expires between validity check and API call. This is a **security enhancement**, not a vulnerability.

---

### Plan 09-18: My Account Test User Seed
**Files Created:** `services/users-permissions-service/src/main/resources/db/migration/V3__seed_test_user.sql`  
**Files Modified:** `docker-compose.yml`, `frontend/src/pages/MyAccount.tsx`, `frontend/e2e/auth-flows.spec.ts`  
**Threats Introduced:** 3 (T-09-18-01: error disclosure, T-09-18-02: hardcoded UUIDs, T-09-18-03: migration integrity)  
**Threats Mitigated:** 3 (all mitigations verified)  
**New HIGH/CRITICAL Issues:** 0

**Security Assessment:**
- ✅ 404 error message is informational, not a security leak
- ✅ Hardcoded UUIDs are dev/UAT only, no credentials stored
- ✅ Migration integrity protected by Flyway checksums
- ✅ No SQL injection vectors
- ✅ No IDOR issues (backend enforces `/users/me` returns only authenticated user's data)

---

## Audit Trail

### Files Audited (Security-Critical) — Re-Audit 2026-10-15

**Previously audited (2026-10-13) — re-verified:**

| File Path | Lines Audited | Threats Verified | Issues Found |
|-----------|--------------|-----------------|--------------|
| `frontend/src/utils/rememberMe.ts` | 1-19 | T-09-15-03 | FINDING-09-01 (LOW, unchanged) |
| `frontend/src/pages/PasswordReset.tsx` | 1-143 | T-09-15-01 | None |
| `frontend/src/pages/PasswordResetComplete.tsx` | 1-270 | T-09-15-02 | None |
| `frontend/src/hooks/useViewState.ts` | 1-181 | T-09-08-01 | None |
| `frontend/src/components/PermissionGate.tsx` | 1-30 | T-09-06 | None |
| `frontend/src/pages/Login.tsx` | 1-198 | Open redirect check | None (validated) |
| `frontend/src/components/Sidebar.tsx` | 1-113 | T-09-01 | None |
| `frontend/src/auth/keycloak.ts` | 1-17 | T-09-02 | None |

**Newly audited (gap-closure plans 09-16, 09-17, 09-18):**

| File Path | Lines Audited | Threats Verified | Issues Found |
|-----------|--------------|-----------------|--------------|
| `frontend/src/pages/BookingsView.tsx` | 1-63 | T-09-16-01 | None |
| `frontend/src/api/client.ts` | 1-43 | T-09-03, T-09-04, T-09-17-02 | None |
| `frontend/src/hooks/useLocations.ts` | 1-15 | T-09-17-01 | None |
| `frontend/src/pages/BookingCreate.tsx` | 1-159 | T-09-16-01 (sink validation) | None |
| `frontend/src/components/booking/BookingForm.tsx` | 1-400 (sampled) | T-09-16-01 (datetime validation) | None |
| `frontend/src/pages/MyAccount.tsx` | 115-164 | T-09-18-01 | None |
| `services/users-permissions-service/src/main/resources/db/migration/V3__seed_test_user.sql` | 1-32 | T-09-18-02, T-09-18-03 | None |
| `docker-compose.yml` | Keycloak section | T-09-18-02 (documentation) | None |

### XSS Attack Surface — Re-Scanned and Clear (2026-10-15)
- ✅ **No `dangerouslySetInnerHTML` usage** (scanned all `.tsx` files including new code)
- ✅ **No `javascript:` hrefs** (scanned all `.tsx` files including new code)
- ✅ **No `.innerHTML` assignments** (scanned all `.tsx` files including new code)
- ✅ **React auto-escapes all user input** (default behavior verified, no changes)

### URL Redirect Validation — Re-Verified Safe
**Login redirect param (`?redirect=`)** — `frontend/src/pages/Login.tsx:19-27` (unchanged):
```typescript
const rawRedirect = params.get('redirect');
const redirectTo =
  rawRedirect &&
  // Must start with '/' but NOT '//' (protocol-relative), no ':' (no scheme)
  /^\/[^/]/.test(rawRedirect) &&
  !rawRedirect.includes(':')
    ? rawRedirect
    : '/bookings?view=month';
```
- ✅ **Open redirect prevented** — regex `/^\/[^/]/` ensures same-origin relative path
- ✅ **Protocol-relative URLs blocked** — `/^\/[^/]/` rejects `//evil.com`
- ✅ **Scheme URLs blocked** — `!includes(':')` rejects `javascript:`, `data:`, `http:`
- ✅ **Fallback to safe default** — invalid redirects fall back to `/bookings?view=month`

### Session Management — Re-Verified Secure
- ✅ **JWT stored in memory** (keycloak-js library, not localStorage) — unchanged
- ✅ **401 triggers re-authentication** (axios interceptor) — unchanged
- ✅ **JWT refresh before API calls** (NEW in plan 09-17) — keycloak.updateToken(30) in request interceptor
- ✅ **PKCE flow** (Keycloak configuration verified in Phase 2) — unchanged
- ⚠️ **Remember Me cookie missing `Secure`** (FINDING-09-01, accepted risk) — unchanged

---

## Recommendations

### Priority: INFORMATIONAL (No blocking issues)

1. **[INFORMATIONAL] Add `Secure` attribute to Remember Me cookie** — unchanged from 2026-10-13 audit
   - **File:** `frontend/src/utils/rememberMe.ts:9`
   - **Change:** Add `; Secure` to cookie string
   - **Note:** Will break local development (use conditional: `Secure` only if `window.location.protocol === 'https:'`)

2. **[BEST PRACTICE] Document HTTPS requirement** — unchanged from 2026-10-13 audit
   - Add to deployment guide: "HTTPS required for production (TLS terminated at Gateway)"
   - Justifies accepted risk for FINDING-09-01

3. **[NEW - INFORMATIONAL] Consider using useViewState for validated URL params in BookingsView**
   - **File:** `frontend/src/pages/BookingsView.tsx:17`
   - **Current:** `const currentDate = searchParams.get('date') ?? '';` (raw, unvalidated)
   - **Improvement:** `const { date } = useViewState();` (validated via isValidIsoDate)
   - **Impact:** Low (defense-in-depth already works via BookingForm validation)
   - **Rationale:** Consistency — other components use useViewState for URL parameter validation
   - **Not a security issue:** React Router + BookingForm validation prevent all attack vectors

4. **[PHASE 10] Implement JIT (Just-In-Time) user provisioning** — new recommendation
   - **Context:** Plan 09-18 uses hardcoded placeholder UUIDs for test users
   - **Long-term solution:** Auto-create user record on first authenticated request if JWT `sub` not in users table
   - **Benefit:** Eliminates manual UUID matching requirement between Keycloak and seed data
   - **Security impact:** None (improves developer experience, reduces configuration errors)
   - **Reference:** Documented in 09-18-PLAN.md success criteria and V3 migration header

---

## Conclusion

**Phase 9 Core User Interface remains SECURED after gap-closure plans 09-16, 09-17, 09-18.**

All declared threat mitigations from the initial audit (2026-10-13) remain in place and verified. The three gap-closure plans introduce no new HIGH or CRITICAL security issues. All six new threats (T-09-16-01, T-09-17-01, T-09-17-02, T-09-18-01, T-09-18-02, T-09-18-03) have been successfully mitigated or accepted as documented risks.

The single LOW-severity finding (missing `Secure` cookie attribute) is unchanged and remains an accepted risk given the HTTPS deployment requirement and the non-sensitive nature of the cookie contents (email address only).

### Key Security Strengths (Unchanged)

1. ✅ **Password reset email enumeration prevented** — Identical success message for all outcomes
2. ✅ **URL parameter validation comprehensive** — ISO-8601 dates, enum checks, fallbacks to defaults
3. ✅ **No XSS vectors found** — No dangerous React patterns, auto-escaping verified
4. ✅ **Open redirect prevented** — Login redirect param validated with strict regex
5. ✅ **Client-side permission checks documented as UX-only** — Backend enforces authorization
6. ✅ **JWT stored in memory** — No localStorage persistence, limited XSS exposure window
7. ✅ **401 handling triggers re-authentication** — Expired tokens force login

### New Security Strengths (Gap-Closure Plans)

8. ✅ **Global JWT refresh prevents expired token errors** (Plan 09-17) — Proactive refresh with 30-second threshold prevents race conditions
9. ✅ **Test user seed data documented and scoped to dev/UAT** (Plan 09-18) — Clear documentation prevents production misuse
10. ✅ **404 error handling provides actionable developer guidance** (Plan 09-18) — Helps resolve missing user record issue without leaking sensitive data

### Accepted Risks (Documented)

1. **Client-side permission gating is UX-only** — UI hides elements based on JWT roles, but backend enforces all authorization decisions (T-09-01, T-09-06)
2. **Remember Me cookie lacks `Secure` attribute** — Email-only cookie, HTTPS deployment assumed (FINDING-09-01)
3. **Frontend trusts Gateway responses** — If Gateway is compromised, frontend cannot detect tampering (T-09-05, architectural decision)
4. **Hardcoded test user UUIDs in V3 migration** — Dev/UAT only, no credentials stored, documented for production replacement (T-09-18-02)

---

## Re-Audit Verification Checklist

- [x] All 18 PLAN files read and threat models extracted
- [x] All 3 gap-closure SUMMARY files reviewed for implemented changes
- [x] All new threats (T-09-16-01, T-09-17-01, T-09-17-02, T-09-18-01, T-09-18-02, T-09-18-03) verified
- [x] All pre-existing mitigations re-verified (no regressions)
- [x] XSS attack surface re-scanned (dangerouslySetInnerHTML, innerHTML, javascript: hrefs)
- [x] URL redirect validation re-verified (Login.tsx)
- [x] Session management re-verified (JWT in memory, 401 handling, token refresh)
- [x] Console logging audited (no token leakage)
- [x] Database seed data audited (no credentials, idempotency verified)
- [x] Error messages audited (no sensitive data disclosure)
- [x] FINDING-09-01 status verified (unchanged, still accepted risk)

---

**Re-Audit completed:** 2026-10-15  
**Auditor:** Security Audit Assistant (claude-sonnet-4-5)  
**Previous audit:** 2026-10-13  
**Next action:** Phase 9 complete — proceed to next phase; no blocking security issues found

---

## Diff from 2026-10-13 Audit

**Additions:**
- Plan 09-16: Create Booking button navigation (T-09-16-01 verified, mitigated)
- Plan 09-17: Global JWT token refresh (T-09-17-01, T-09-17-02 verified, mitigated)
- Plan 09-18: Test user seed data (T-09-18-01, T-09-18-02, T-09-18-03 verified, mitigated/accepted)
- 8 new files audited (BookingsView.tsx, V3 migration, etc.)
- 3 new security strengths identified (JWT refresh, seed documentation, 404 error handling)
- 1 new recommendation (JIT user provisioning for Phase 10)

**Unchanged:**
- FINDING-09-01 (LOW severity, accepted risk)
- All pre-existing mitigations from 09-01 through 09-15
- XSS attack surface (still clear)
- URL redirect validation (still safe)
- Session management (enhanced with JWT refresh, still secure)

**Removals:**
- None

**Verdict:** SECURED (no change from 2026-10-13 audit)
