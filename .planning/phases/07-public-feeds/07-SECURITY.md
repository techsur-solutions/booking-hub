# SECURITY AUDIT REPORT
## Phase 7: Public Feeds

**Audit Mode:** VERIFY (threat models declared in PLAN files)  
**Audit Date:** 2026-10-13  
**Auditor:** pivota_spec Security Audit (claude-sonnet-4-5)  
**Scope:** feeds-service implementation (Phase 7 Plans 07-01, 07-02, 07-03)

---

## Executive Summary

**Status:** ⚠️ **OPEN THREATS**  
**Critical/High Findings:** 1  
**Medium Findings:** 0  
**Low/Info Findings:** 0  

### Threat Overview
Phase 7 implements a deliberately public feeds-service with NO authentication layer (F9.6 requirement). The audit verified all 10 declared mitigations across 3 threat model registers. **One HIGH-severity finding confirmed**: unbounded query result sets enable DoS via resource exhaustion.

---

## Audit Scope

### Plans Audited
- **07-01-PLAN.md**: Foundation (idempotency, error handling, NO security)
- **07-02-PLAN.md**: Projection consumer (event processing, denormalization)
- **07-03-PLAN.md**: Public endpoints (RSS2, iCal, JSON, display-board)

### Implementation Surface
- **4 public HTTP endpoints**: `/feeds/{rss2,ical,json,display-board}` (GET only)
- **2 RabbitMQ consumers**: `feeds.booking.q`, `feeds.location.q`
- **3 hand-built renderers**: RSS2 XML, iCalendar, HTML display board
- **Projection database**: `feed_bookings`, `processed_events`, `location_names`

---

## Threat Model Verification

### Plan 07-01: Foundation (4 threats)

| Threat ID | Category | Mitigation | Status | Evidence |
|-----------|----------|------------|--------|----------|
| **T-07-01-01** | Elevation of privilege | No mutating endpoints, GET-only | ✅ VERIFIED | FeedController.java:63-100 — exactly 4 `@GetMapping`, zero `@PostMapping/@PutMapping/@DeleteMapping`; pom.xml:25-148 — NO spring-boot-starter-security or oauth2-resource-server |
| **T-07-01-02** | Tampering | V2 migration additive-only | ✅ VERIFIED | V2__add_idempotency_key.sql:10-12 — `CREATE TABLE processed_events` only, no ALTER/DROP of V1 objects |
| **T-07-01-03** | Denial of service | Bounded retry + DLQ routing | ✅ VERIFIED | RabbitConfig.java:79-90 — maxAttempts=4 (1+3 retries), RejectAndDontRequeueRecoverer, AmqpRejectAndDontRequeueException non-retryable; MissingIdempotencyKeyException.java:21-25 extends AmqpRejectAndDontRequeueException |
| **T-07-01-04** | Information disclosure | Generic 500 error, no stack trace leak | ✅ VERIFIED | GlobalExceptionHandler.java:59-73 — `@ExceptionHandler(Exception.class)` returns generic "INTERNAL_ERROR" ApiError, logs actual exception server-side only |

### Plan 07-02: Projection Consumer (4 threats)

| Threat ID | Category | Mitigation | Status | Evidence |
|-----------|----------|------------|--------|----------|
| **T-07-02-01** | Tampering/DoS | Malformed body log-and-skip | ✅ VERIFIED | FeedProjectionService.java:127-151 — defensive null-checks on `id`/`title`/`location_id`/`start_time`/`end_time`, log-warn-and-return on missing fields (never throws for bad data); UncheckedIOException for unparseable JSON (FeedEventConsumer.java:93-98) triggers DLQ via RejectAndDontRequeueRecoverer |
| **T-07-02-02** | Information disclosure | denied/deleted rows physically removed | ✅ VERIFIED | FeedProjectionService.java:133-135 — `case "denied", "deleted" -> feedBookingRepository.deleteById(bookingId)` physically removes row; FeedQueryService.java:63,74-75 filters `status='approved'` at query level (defense-in-depth) |
| **T-07-02-03** | Spoofing | location_name from events only | ✅ VERIFIED | FeedProjectionService.java:163-176 — location_name sourced from `location.created`/`location.updated` events into `location_names` cache; no cross-service datasource/JdbcTemplate found in feeds-service |
| **T-07-02-04** | Repudiation/DoS | Idempotent event processing | ✅ VERIFIED | FeedProjectionService.java:96-104 — lookup-first `existsByIdempotencyKey` + atomic `@Transactional` (projection + processed_events insert); FeedEventConsumer.java:82-88 throws MissingIdempotencyKeyException for missing key → DLQ routing |

### Plan 07-03: Public Endpoints (4 threats)

| Threat ID | Category | Mitigation | Status | Evidence |
|-----------|----------|------------|--------|----------|
| **T-07-03-01** | Elevation of privilege | GET-only endpoints | ✅ VERIFIED | FeedController.java:63-100 — 4 `@GetMapping` annotations, zero mutating methods; no `@PreAuthorize/@Secured` annotations anywhere in controller (verified via grep) |
| **T-07-03-02** | Information disclosure | Single shared query filtering approved+upcoming | ✅ VERIFIED | FeedQueryService.java:59-78 — all 4 endpoints call same method; line 63 filters `status='approved'`, line 60 filters `start_time >= now()`; FeedController.java:65,72,84,94 — all delegate to `feedQueryService.getApprovedUpcoming` |
| **T-07-03-03** | Tampering (output injection) | Format-correct escaping in renderers | ✅ VERIFIED | Rss2Renderer.java:62-79 — `xmlEscape` applied to all title/location text (& < > " '); ICalRenderer.java:59-76 — `icalEscape` RFC-5545 compliant (\\ \; \, \\n); DisplayBoardRenderer.java:89-106 — `htmlEscape` applied (& < > " ') |
| **T-07-03-04** | Denial of service | Malformed UUID → clean 400 | ✅ VERIFIED | FeedQueryService.java:66-71 — `UUID.fromString` wrapped in try-catch, throws FeedLocationFilterInvalidException (400) on IllegalArgumentException; GlobalExceptionHandler.java:39-51 converts ApiException to 400 JSON response |

---

## Confirmed Findings

### 🔴 HIGH: SEC-07-001 — Unbounded Query Result Sets Enable DoS

**Severity:** HIGH  
**Category:** Denial of Service (STRIDE: D)  
**CVSS:** 7.5 (High) — AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:N/A:H  

**Location:**
- `services/feeds-service/src/main/java/com/bookinghub/feeds/repository/FeedBookingRepository.java:27-31`
- `services/feeds-service/src/main/java/com/bookinghub/feeds/feed/FeedQueryService.java:63,74-75`

**Description:**

The public feed endpoints query `feed_bookings` with NO pagination or result-set limits. The repository methods return unbounded `List<FeedBooking>`:

```java
// FeedBookingRepository.java:27-28
List<FeedBooking> findByStatusAndStartTimeGreaterThanEqualOrderByStartTimeAsc(
    String status, Instant now);
```

A malicious or legitimate caller can exhaust server resources by requesting feeds when:
1. A large number of approved bookings exist (e.g., 10,000+ future events for a conference center)
2. All 4 feed formats (RSS2, iCal, JSON, display-board) render the ENTIRE unbounded result set into memory as a single string
3. Multiple concurrent requests amplify memory/CPU consumption

**Attack Path:**
1. Attacker makes concurrent GET requests to `/feeds/rss2`, `/feeds/json`, `/feeds/display-board`
2. Each request loads ALL approved upcoming bookings into memory
3. Renderers build large strings (RSS2Renderer.java:34-56 uses `StringBuilder.append` in a loop over ALL items)
4. Server OOMs or becomes unresponsive, denying service to legitimate users

**Evidence of Exploitation:**

The threat model T-07-01-03 addresses "bounded retry" for RabbitMQ but does NOT address unbounded query results. The implementation has:
- ✅ Bounded retry (4 attempts)
- ✅ Cache headers (Cache-Control: max-age=60)
- ❌ **No query pagination or LIMIT clause**
- ❌ **No size-based throttling**

Repository queries are derived method names (Spring Data), which do NOT support `LIMIT` without a `Pageable` parameter. The indexes (`idx_feed_bookings_upcoming`, `idx_feed_bookings_location`) optimize query speed but do NOT limit result size.

**Recommended Fix:**

1. **Add pagination to repository queries** (breaking change to FeedQueryService contract):
   ```java
   // Option A: Hard limit (quick fix)
   @Query("SELECT f FROM FeedBooking f WHERE f.status = :status AND f.startTime >= :now 
           ORDER BY f.startTime ASC LIMIT 1000")
   List<FeedBooking> findByStatusAndStartTimeGreaterThanEqualOrderByStartTimeAsc(
       @Param("status") String status, @Param("now") Instant now);
   
   // Option B: Pageable support (flexible)
   Page<FeedBooking> findByStatusAndStartTimeGreaterThanEqualOrderByStartTimeAsc(
       String status, Instant now, Pageable pageable);
   ```

2. **Update FeedQueryService to apply default limit** (e.g., 500 results max for feeds)

3. **Document limit in feed metadata** (RSS channel description, iCal COMMENT, JSON response header)

4. **Add rate limiting at gateway** for `/feeds/**` routes (e.g., 10 req/min per IP) — mitigates amplification

**Risk if Unfixed:**

An attacker can cause server OOM or degrade performance for ALL users (feeds-service is single-instance). Even legitimate use by a conference center with 5000+ future bookings triggers the issue.

**Mitigation Status:** ❌ OPEN (requires implementation change)

---

## Verified Safe: Key Security Strengths

### ✅ Intentionally Public Design (by requirement)
- **No security dependencies**: pom.xml deliberately omits spring-boot-starter-security and oauth2-resource-server (F9.6 requirement)
- **Gateway permitAll**: Gateway SecurityConfig line permits `/feeds/**` (verified in api-gateway/config/SecurityConfig.java)
- **No auth annotations**: Zero `@PreAuthorize/@Secured` on any controller (grep verified)

### ✅ Output Injection Prevention
- **Format-specific escaping**: XML entities (RSS2), RFC-5545 escaping (iCal), HTML entities (display-board) applied to ALL upstream-controlled text (booking titles, location names)
- **Escaping coverage**: 100% — every title/location occurrence passes through the appropriate escape function
- **Unit tested**: FeedRendererTest verifies escaping with `&`, `<`, `;`, `,` inputs

### ✅ Input Validation
- **UUID validation**: FeedQueryService catches `IllegalArgumentException` from `UUID.fromString` and returns clean 400 FEED_LOCATION_FILTER_INVALID (never 500)
- **Unknown location → empty feed**: Well-formed but unknown location_id returns HTTP 200 with empty result set (correct behavior per Success Criterion 2)

### ✅ Error Message Sanitization
- **Generic 500 fallback**: GlobalExceptionHandler catches all unhandled exceptions and returns `{"error_code":"INTERNAL_ERROR", "message":"An unexpected error occurred."}` — no stack traces, SQL errors, or internal paths leak to public internet
- **Server-side logging**: Actual exceptions logged at ERROR level for diagnosis

### ✅ Projection Integrity
- **Physical deletion of denied/deleted bookings**: Cannot leak through ANY renderer (defense-in-depth beyond query filter)
- **Idempotent event processing**: Duplicate events are safe no-ops; missing idempotency keys dead-letter to DLQ (never silently dropped)

---

## STRIDE Analysis Summary

| Category | Threats | Verified | Open |
|----------|---------|----------|------|
| **S**poofing | 1 (T-07-02-03) | 1 | 0 |
| **T**ampering | 3 (T-07-01-02, T-07-02-01, T-07-03-03) | 3 | 0 |
| **R**epudiation | 1 (T-07-02-04) | 1 | 0 |
| **I**nformation disclosure | 2 (T-07-01-04, T-07-02-02) | 2 | 0 |
| **D**enial of service | 2 (T-07-01-03, T-07-03-04) | 2 | **1 (SEC-07-001)** |
| **E**levation of privilege | 2 (T-07-01-01, T-07-03-01) | 2 | 0 |
| **Total** | **11** | **11** | **1** |

---

## Audit Methodology

### Adversarial Verification
For each declared mitigation:
1. **Grep for presence** of the claimed control (e.g., `xmlEscape`, `@GetMapping`, `existsByIdempotencyKey`)
2. **Verify coverage** across ALL relevant entry points (e.g., all 4 endpoints call same query method)
3. **Attempt refutation**: Is input user-controlled? Is there an upstream guard? Does auth gate it?
4. **Confirm finding** only if it survives adversarial refutation

### New Threat Discovery (Retroactive Surface Scan)
- Scanned for: command injection (none - no shell exec), SQL injection (Spring Data derived queries - safe), path traversal (no file ops), IDOR (no workspace/user scoping needed - public feed), secret leakage (no credentials in code/logs/URLs)
- **Found**: Unbounded query DoS (SEC-07-001) — not in original threat models

### Files Reviewed
- 16 Java implementation files (controllers, services, renderers, consumers, config, error handlers)
- 3 SQL migration files
- 1 pom.xml
- 1 gateway SecurityConfig.java (cross-service verification)

---

## Recommendations

### Immediate (Phase 7 Completion Blocker)
1. **Fix SEC-07-001**: Add hard limit (1000 results) to feed queries via `@Query` with `LIMIT` clause OR convert to `Pageable` with default size

### Post-Phase 7 Hardening
1. **Add rate limiting** at gateway for `/feeds/**` (e.g., 10 req/min per IP) — mitigates DoS amplification
2. **Add monitoring** for feed_bookings table size and query execution time (alert if >5s or >1000 rows returned)
3. **Document feed size limits** in public API docs and feed metadata (RSS channel description)

### Low Priority
1. Consider adding `Last-Modified` header alongside ETag (HTTP conditional requests optimization)
2. Consider GZIP compression at gateway for feeds (RSS/iCal/JSON all compress well)

---

## Sign-Off

**Threats Open:** 1 (HIGH)  
**Recommended Action:** Fix SEC-07-001 before Phase 7 completion OR document as accepted risk with operational mitigations (monitoring, rate limiting at gateway)

**Audit Trail:**
- Threat models: 11 declared mitigations → 11 verified present in code
- New findings: 1 HIGH (unbounded queries)
- False positives: 0 (no auth on public service is BY DESIGN per F9.6, not a vulnerability)

---

**Auditor:** pivota_spec Security Audit  
**Date:** 2026-10-13  
**Framework:** STRIDE threat modeling + FORCE adversarial stance  
**Scope:** Phase 7 (feeds-service) — plans 07-01, 07-02, 07-03
