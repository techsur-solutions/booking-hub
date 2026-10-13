# Traceability Matrix Schema

Schema documentation for `.planning/traceability-matrix.csv` per FRD F13 requirements.

## Overview

The traceability matrix links every legacy audit finding (F0 baseline) to FRD requirements, implementation locations, and test references. It serves as the objective verification artifact for "no functionality lost" — each finding is traceable from legacy behavior through to a passing test.

**Seed input:** `.planning/phases/01-legacy-functional-audit/baseline-inventory.md` (113 findings, BF-001 through BF-113)

---

## Column Definitions

| Column | Type | Required | Description |
|--------|------|----------|-------------|
| `finding_id` | String | Yes | Stable ID from baseline-inventory.md (format `BF-NNN`, e.g. `BF-001`) |
| `requirement_ref` | String | Yes | FRD requirement ID(s) this finding maps to (e.g. `F7`, `F2.3`, `F1.5, F3.2`) |
| `implementation_ref` | String | No | Code location or PR/commit link — populated during implementation phases, empty initially |
| `test_ref` | String | No | Test class/method reference — populated when test is written, empty initially |
| `status` | Enum | Yes | Current state: one of `not_started`, `implemented`, `tested`, `verified` |
| `notes` | String | No | Optional free-text for Open Question references, edge cases, or deferred decisions |

### Column Details

#### `finding_id`
- Format: `BF-NNN` (BF = Baseline Finding, NNN = zero-padded 3-digit number)
- Range: `BF-001` through `BF-113` (initial seed)
- Stable identifier — never changes or reused even if finding is superseded

#### `requirement_ref`
- FRD requirement ID from the project's REQUIREMENTS.md / FRD document
- Examples: `F7`, `F2.1`, `F1.5, F3.2` (comma-separated when finding spans multiple features)
- Populated from baseline-inventory.md's "Maps-To-Feature" column at seed time

#### `implementation_ref`
- Empty at seed time — populated during Phases 2–7 as implementation work lands
- Format: `service-name/path/to/File.java:MethodName` or PR/commit reference
- Example: `booking-service/src/main/java/...BookingService.java:createBooking`

#### `test_ref`
- Empty at seed time — populated when tests are written for the finding
- Format: `TestClassName::testMethodName` or `e2e/spec-file.spec.ts::test description`
- Examples:
  - `BookingServiceIntegrationTest::testCreateBookingConflictEnforcement`
  - `e2e/booking.spec.ts::creates booking with approval flow`

#### `status`
See Status Values section below.

#### `notes`
- Optional free-text field
- Used for:
  - Open Question references: `"Open Question - see open-questions.md"`
  - Deferred decisions referencing specific issue numbers
  - Edge cases that affect implementation but aren't blocking

---

## Status Values

| Status | Meaning | When Set |
|--------|---------|----------|
| `not_started` | No implementation or test exists yet | Default at seed time for all findings |
| `implemented` | Code exists, `implementation_ref` populated, no test yet | When implementation PR lands |
| `tested` | Test exists and passes, `test_ref` populated | When test is written and passes locally |
| `verified` | Test has passed at least once in CI (production-ready) | When CI pipeline confirms test passes |

### Status Transitions

```
not_started → implemented → tested → verified
```

- `not_started` → `implemented`: Code implementing the finding's behavior has landed; `implementation_ref` must be populated before this transition
- `implemented` → `tested`: Test for the finding is written and passes locally; `test_ref` must be populated before this transition  
- `tested` → `verified`: CI pipeline has confirmed the test passes; marks finding production-ready

**Immutability rules:**
1. Status only moves **forward** — never regress from `verified` to `tested`, or `tested` to `implemented`
2. Every finding with `status=implemented` MUST have a non-empty `implementation_ref`
3. Every finding with `status=tested` or `status=verified` MUST have both `implementation_ref` AND `test_ref`

---

## CSV Format

**Header row:**
```
finding_id,requirement_ref,implementation_ref,test_ref,status,notes
```

**Example rows:**
```csv
BF-001,F7,,,not_started,
BF-028,F2.1,,,not_started,Open Question - see open-questions.md
BF-009,"F1, F3",,,not_started,
```

**Escaping rules:**
- Values containing commas MUST be quoted with double-quotes
- Double-quotes within values MUST be escaped as `""`
- Empty fields are represented as consecutive commas (no space)

---

## Query Examples

```bash
# All findings with no test (not_started or implemented)
grep -v ',tested,' .planning/traceability-matrix.csv | grep -v ',verified,' | grep -v '^finding_id'

# Coverage by feature area F7
grep ',F7' .planning/traceability-matrix.csv | cut -d, -f2,5 | sort | uniq -c

# Show specific findings by ID
grep '^BF-028\|^BF-038\|^BF-058' .planning/traceability-matrix.csv

# Summary of status across all findings
cut -d, -f5 .planning/traceability-matrix.csv | sort | uniq -c

# Open Question findings
grep 'Open Question' .planning/traceability-matrix.csv | cut -d, -f1,6

# Use query helper script for more complex queries
bash scripts/traceability/query-matrix.sh gaps
bash scripts/traceability/query-matrix.sh coverage-summary
bash scripts/traceability/query-matrix.sh open-questions
bash scripts/traceability/query-matrix.sh by-feature F7
bash scripts/traceability/query-matrix.sh finding BF-028
```

---

## Maintenance Rules

1. **Never delete rows** — findings are a historical record; if a finding is superseded or merged, update `notes` to reference the successor row instead
2. **Status only moves forward** — never regress status (e.g., from `verified` to `tested`); if a test breaks, it's a CI failure to fix, not a status regression
3. **Open Question resolution requires a test** — every F0 Open Question resolution (items in `open-questions.md` with status OPEN) MUST add a test before the corresponding finding can be marked `tested`
4. **New findings append** — discoveries post-audit get `BF-NNN+1` IDs and append to the bottom of the matrix; never insert rows in the middle
5. **Atomic updates** — when promoting a finding's status, update `implementation_ref`/`test_ref` and `status` in the same commit
6. **CSV escaping** — fields with commas (e.g., `requirement_ref` spanning multiple FRD features) MUST be quoted

---

## Integration with Open Questions

Findings with `Status=Open` in baseline-inventory.md are seeded with `notes="Open Question - see open-questions.md"`. The 13 Open findings are:

| Finding ID | Open Question | Feature |
|------------|---------------|---------|
| BF-028 | checkavailability() permission gate suspicious for booking-UI endpoint | F2.1 |
| BF-038 | Orphan-allow verdict for Location/Resource hard deletes | F4 |
| BF-058 | Assume link server-side check gap | F6.1 |
| BF-062 | Session timeout depends on ColdFusion/Lucee engine default | F6.4 |
| BF-065 | Password reset reveals account existence | F6.3 |
| BF-067 | Password reset token expiry not re-checked at final submit | F6.3 |
| BF-071 | Permission changes have no runtime effect until app reload | F7.3 |
| BF-076 | Email uniqueness case-sensitivity depends on undeclared DB collation | F6.1 |
| BF-089 | Actual bookingNotify.cfm send() call site not found | F8.1 |
| BF-102 | "ajax" log type has no confirmed write-call site | F11.4 |
| BF-107 | onerror.cfm has no actual error logging | F11 |
| BF-112 | Forward decision: hard-block vs non-blocking conflict enforcement | F2 |
| BF-113 | Multi-resource conflict aggregation semantics unconfirmed | F2.1 |

See `.planning/phases/01-legacy-functional-audit/open-questions.md` for full resolution details and forward decisions required.
