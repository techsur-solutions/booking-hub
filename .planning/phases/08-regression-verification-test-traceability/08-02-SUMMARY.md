---
phase: 08-regression-verification-test-traceability
plan: 02
subsystem: testing
tags: [traceability, test-coverage, gap-analysis, parity-verification, f13]

# Dependency graph
requires:
  - phase: 01-legacy-functional-audit
    provides: baseline-inventory.md with 113 BF-xxx findings as seed input
  - phase: 08-regression-verification-test-traceability
    provides: traceability-matrix.csv (plan 08-01 seeded; used by coverage script)
provides:
  - Automated coverage analysis script (analyze-coverage.sh) comparing test files vs matrix
  - Gap report (test-gap-report.md) showing 0% baseline coverage across 113 findings
  - Test-writing guide (test-writing-guide.md) with backend+frontend templates and edge-case scenarios
affects:
  - 08-03-CI-enforcement (gap report informs which tests must exist before CI gates)
  - 08-04-open-question-closure (test stubs list from gap report)
  - phases/05-core-booking-approval-workflow (high-priority F2/F3 test scenarios documented)
  - phases/03-identity-access-control (F7 permission matrix test scenarios)

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Coverage analysis via bash script scanning test files vs CSV matrix"
    - "Test gap report as regenerable markdown artifact (run script after adding tests)"
    - "SpringBootTest + MockMvc + WireMock as canonical backend integration test pattern"
    - "Playwright + VITE_E2E_MODE bypass as canonical frontend E2E pattern"

key-files:
  created:
    - scripts/traceability/analyze-coverage.sh
    - docs/testing/test-gap-report.md
    - docs/testing/test-writing-guide.md
  modified: []

key-decisions:
  - "Gap report regenerable (not static): run script after each test addition to track progress"
  - "Traceability matrix CSV seeded with 113 findings at not_started status (pre-implementation baseline)"
  - "Open Questions (13) explicitly listed in gap report for test stubs per F13.5 requirement"
  - "Test-writing guide references existing Phase 3 patterns (MockMvc+WireMock) to maintain consistency"

patterns-established:
  - "Coverage script: grep -c for count queries (avoid wc -l || echo 0 pipefail double-zero issue)"
  - "Test guide: GIVEN/WHEN/THEN structure with explicit finding IDs (BF-NNN) per test"
  - "Per-feature gap listing in report: one section per FRD feature with finding IDs as gap identifiers"

# Metrics
duration: 7min
completed: 2026-10-13
---

# Phase 08 Plan 02: Coverage Analysis and Test-Writing Guide Summary

**Coverage analysis tooling (analyze-coverage.sh) and gap report establishing 0% baseline coverage across 113 F0 audit findings, plus a 380-line test-writing guide with MockMvc+Playwright patterns for parity verification**

## Performance

- **Duration:** 7 min
- **Started:** 2026-10-13T04:12:56Z
- **Completed:** 2026-10-13T04:20:05Z
- **Tasks:** 2 completed
- **Files modified:** 3 created

## Accomplishments
- Coverage analysis script (`scripts/traceability/analyze-coverage.sh`) scans test files, reads traceability matrix, and generates a regenerable gap report
- Initial gap report shows 64 backend test files and 2 E2E test files exist, but 0/113 findings are formally linked to tests in the matrix (expected pre-phases-4-7 baseline)
- High-priority gaps explicitly called out per F13.3: F2 conflict detection (5 findings), F3 approval workflow (2 findings), F1.3 recurring (3 findings), F7 permissions (7 findings)
- 13 Open Questions listed for test stubs per F13.5
- Test-writing guide (380 lines) documents backend and frontend test templates, F0 finding-to-test mapping examples, and complete edge-case test scenarios

## Task Commits

Each task was committed atomically:

1. **Task 1: Create coverage analysis script** - `09b959c` (feat)
2. **Task 2: Create test-writing guide with patterns** - `21ac471` (docs)

**Plan metadata:** `[pending]` (docs: complete plan)

## Files Created/Modified
- `scripts/traceability/analyze-coverage.sh` - Bash script scanning test files vs traceability matrix, generating per-feature gap breakdown
- `docs/testing/test-gap-report.md` - Generated gap report: 113 findings at 0% coverage, high-priority F2/F3/F1.3/F7 gaps, 13 open-question stubs
- `docs/testing/test-writing-guide.md` - 380-line guide: SpringBootTest+MockMvc+WireMock backend template, Playwright frontend template, BF-001 permission gate example, all F13.3 edge-case scenarios

## Decisions Made
- Gap report is regenerable (not static): developers run `bash scripts/traceability/analyze-coverage.sh` after adding tests to track progress toward 100% coverage
- Coverage script uses `grep -c` instead of `grep | wc -l` to avoid bash pipefail causing double-zero output when grep matches nothing
- Test-writing guide references existing Phase 3 test infrastructure (MockMvc+WireMock) to maintain consistency with established patterns

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Traceability matrix CSV not yet on filesystem**
- **Found during:** Task 1 (Create coverage analysis script)
- **Issue:** `analyze-coverage.sh` requires `.planning/traceability-matrix.csv` to run; plan 08-01 creates it but no SUMMARY.md exists — however `git show HEAD` confirmed the CSV was already committed (114 lines, 113 data rows)
- **Fix:** Verified CSV already existed in git HEAD (seeded by prior work); created `docs/testing/` directory as needed
- **Files modified:** None (CSV already present)
- **Verification:** `bash scripts/traceability/analyze-coverage.sh` ran successfully
- **Committed in:** 09b959c (Task 1 commit)

**2. [Rule 1 - Bug] Script produced "00" for zero counts due to pipefail + `|| echo "0"` interaction**
- **Found during:** Task 1 verification
- **Issue:** With `set -o pipefail`, when `grep` finds no matches it exits 1, causing the pipeline to fail; `wc -l` already wrote "0" to stdout, then `|| echo "0"` appended another "0" → TESTED variable = "00"
- **Fix:** Replaced `grep ... | wc -l | tr -d '[:space:]' || echo "0"` with `grep -c ...` which outputs a count without triggering pipefail issues; fallback via `|| VARIABLE=0` assignment pattern
- **Files modified:** scripts/traceability/analyze-coverage.sh
- **Verification:** Re-ran script; "Tested findings: 0" (not "00"); per-feature counts show "0/N" correctly
- **Committed in:** 09b959c (Task 1 commit, fixed before commit)

---

**Total deviations:** 2 (1 non-issue after verification, 1 auto-fixed bug)
**Impact on plan:** Both addressed before task commit. CSV prerequisite was already present; bug fix corrected script output format with no functional change to analysis logic.

## Issues Encountered
None — plan executed successfully. Script bug discovered during verification run and fixed before commit.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Gap report (`docs/testing/test-gap-report.md`) is ready for plan 08-03 (CI enforcement) to reference as the list of required tests
- Test-writing guide is ready for development teams to use during phases 4-7 test implementation
- Coverage analysis script can be re-run at any time: `bash scripts/traceability/analyze-coverage.sh`
- 13 open-question stubs identified in gap report for plan 08-04 (open question closure)

## Self-Check: PASSED

- [x] `scripts/traceability/analyze-coverage.sh` exists: `[ -f scripts/traceability/analyze-coverage.sh ]` ✓
- [x] `docs/testing/test-gap-report.md` exists: `[ -f docs/testing/test-gap-report.md ]` ✓
- [x] `docs/testing/test-writing-guide.md` exists: `[ -f docs/testing/test-writing-guide.md ]` ✓
- [x] Task commits exist: `09b959c` (feat), `21ac471` (docs) ✓
- [x] No build step applicable (shell script + markdown artifacts, no compilation)
- [x] No blocking stubs found (script is fully implemented, report is generated, guide is complete)

---
*Phase: 08-regression-verification-test-traceability*
*Completed: 2026-10-13*
