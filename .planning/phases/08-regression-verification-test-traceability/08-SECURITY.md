# Security Report — Phase 8: Regression Verification & Test Traceability

**Mode:** verify
**Audited:** 2026-10-13
**Verdict:** SECURED
**Confirmed HIGH/CRITICAL:** 0

## Summary

Phase 8 implements test traceability infrastructure (F13) via bash scripts, GitHub Actions workflows, and documentation. Four PLAN files declared 6 threats across STRIDE categories (Tampering, Denial of Service, Information Disclosure, Repudiation, Elevation of Privilege). All declared mitigations have been verified in the implemented code. No command injection, path traversal, or secret leakage vulnerabilities were found. All bash scripts use `set -euo pipefail` with bounded regex patterns and safe path handling. CI workflow includes timeout mitigations and uses ephemeral test credentials only. No user-controlled input flows to shell execution sinks. Phase is safe to ship.

## Attack surface audited

| Area | STRIDE | Verdict | Evidence (file:line) |
|------|--------|---------|----------------------|
| seed-matrix.sh (baseline parsing) | T | SAFE | scripts/traceability/seed-matrix.sh:12,27 — `set -euo pipefail`, bounded regex `^\| BF-[0-9]{3}`, read-only input |
| query-matrix.sh (user args) | T | SAFE | scripts/traceability/query-matrix.sh:6,51,79-82 — user args only in grep patterns (quoted), validated BF-NNN format |
| analyze-coverage.sh (file scan) | T | SAFE | scripts/traceability/analyze-coverage.sh:2,34-35 — `find` with literal paths, no user input |
| run-full-regression.sh (CI runner) | T | SAFE | scripts/ci/run-full-regression.sh:4,6-7,38 — anchored to PROJECT_ROOT, service_dir validated via pom.xml check |
| CI workflow (GitHub Actions) | D | SAFE | .github/workflows/regression-suite.yml:17,74 — `timeout-minutes: 30` mitigates T-08-07 hang DoS |
| CI workflow (secrets) | I | SAFE | .github/workflows/regression-suite.yml:22-25,48-53 — ephemeral test DB creds only (POSTGRES_PASSWORD=test_pass) |
| Traceability matrix CSV | T | SAFE (accepted) | T-08-01 accepted — matrix edits version-controlled, false test_ref caught by CI |
| Branch protection bypass | E | SAFE (accepted) | T-08-06 accepted — admin bypass is GitHub platform trust assumption |
| Test stub templates | T | SAFE (accepted) | T-08-09 accepted — stubs are templates, not production code |
| Coverage report disclosure | I | SAFE (accepted) | T-08-03 accepted — report in planning dir, not runtime artifact |
| Developer ignores gap report | R | SAFE (accepted) | T-08-04 accepted — coverage analysis scans actual files, not self-reports |
| Open Question resolution without test | R | SAFE (mitigated) | T-08-08 mitigated — code review checklist + auditable git history |

## Confirmed findings

> None. All audited surface areas are SAFE.

## Resolved findings

> No prior audit of this phase.

## Accepted risks

| ID | Risk | Why accepted | Owner |
|----|------|--------------|-------|
| T-08-01 | Tampering: Developer manually edits traceability matrix to claim false test coverage | Matrix edits version-controlled; false test_ref claims caught when CI runs non-existent test; residual risk (trivial passing test) mitigated by code review (standard gate). | Development Team (code review process) |
| T-08-03 | Information Disclosure: test-gap-report.md exposes internal architecture | Report is planning artifact (committed), not runtime. Project is not security-by-obscurity; test structure mirrors documented FRD. | Product Owner |
| T-08-04 | Repudiation: Developer claims test exists but gap report doesn't detect it | Coverage script scans actual file paths (`find services -name "*Test.java"`), not self-reported claims. If test exists but isn't detected, script's pattern needs updating (logged as improvement, not security gap). | Phase 8 Maintainer |
| T-08-06 | Elevation of Privilege: Admin bypasses branch protection to merge failing PR | GitHub "Do not allow bypassing" blocks admins, but repo owner can toggle setting. This is GitHub platform trust assumption. Documented in branch-protection-setup.md as requirement. | Repository Owner |
| T-08-09 | Tampering: Test stub templates contain incorrect logic | Stubs are templates with decision placeholders, not production code. Decision-specific logic explicitly marked "fill in after resolution." Mitigated by code review of actual test implementations. | Code Reviewer |

## Audit trail

### Diff scoped via
- SUMMARY.md file lists from 08-01-SUMMARY.md through 08-04-SUMMARY.md
- Files modified across 4 plans:
  - Plan 08-01: `.planning/traceability-matrix.csv`, `.planning/traceability-matrix-schema.md`, `scripts/traceability/seed-matrix.sh`, `scripts/traceability/query-matrix.sh`
  - Plan 08-02: `scripts/traceability/analyze-coverage.sh`, `docs/testing/test-gap-report.md`, `docs/testing/test-writing-guide.md`
  - Plan 08-03: `.github/workflows/regression-suite.yml`, `scripts/ci/run-full-regression.sh`, `docs/ci/branch-protection-setup.md`, `frontend/package.json`
  - Plan 08-04: `docs/testing/open-question-resolution-protocol.md`, `.planning/test-stubs/open-questions/` (5 files), `.planning/STATE.md`

### Register
Loaded from PLAN.md `<threat_model>` blocks in 4 files:
- **08-01-PLAN.md** declared 2 threats: T-08-01 (Tampering: matrix edits), T-08-02 (DoS: seed-matrix.sh infinite loop)
- **08-02-PLAN.md** declared 2 threats: T-08-03 (Info Disclosure: gap report), T-08-04 (Repudiation: test detection)
- **08-03-PLAN.md** declared 3 threats: T-08-05 (Tampering: disable fail-on-error), T-08-06 (EoP: admin bypass), T-08-07 (DoS: CI hang)
- **08-04-PLAN.md** declared 2 threats: T-08-08 (Repudiation: OQ resolution w/o test), T-08-09 (Tampering: stub logic)

Total: 9 declared threats (6 mitigated, 3 accepted per plans)

### Refutation
15 candidates examined (9 declared threats + 6 additional attack surface areas from retroactive scan), 0 confirmed as HIGH/CRITICAL open findings, 15 verified as SAFE (6 mitigated, 9 accepted/safe-by-design).

---

## Verification Details

### T-08-01: Traceability matrix tampering (Tampering) — ACCEPTED ✓
- **Plan disposition:** accept
- **Declared mitigation:** Matrix edits version-controlled; false test_ref caught by CI; residual risk (trivial test) mitigated by code review
- **Verification:** 
  - Matrix is `.planning/traceability-matrix.csv` (committed to git) ✓
  - CI workflow `.github/workflows/regression-suite.yml` runs tests via `mvn clean test` and `npx playwright test` — non-existent test_ref will fail ✓
  - Code review is standard gate (not matrix-specific) ✓
- **Verdict:** Mitigation verified; residual risk accepted per plan

### T-08-02: seed-matrix.sh infinite loop on malformed input (DoS) — MITIGATED ✓
- **Plan disposition:** mitigate
- **Declared mitigation:** Script uses `set -euo pipefail` and bounded regex `^| BF-[0-9]{3}` — malformed input causes early exit; input is committed artifact, not untrusted
- **Verification:**
  - `scripts/traceability/seed-matrix.sh:12` — `set -euo pipefail` present ✓
  - Line 27 — `grep -E '^\| BF-[0-9]{3} \|'` bounded regex ✓
  - Line 34 — validation skips non-BF-NNN IDs: `if ! echo "$id" | grep -qE '^BF-[0-9]{3}$'; then continue; fi` ✓
  - Input file is `.planning/phases/01-legacy-functional-audit/baseline-inventory.md` (Phase 1 committed artifact) ✓
- **Verdict:** Mitigation fully implemented

### T-08-03: test-gap-report.md exposes internal architecture (Info Disclosure) — ACCEPTED ✓
- **Plan disposition:** accept
- **Declared mitigation:** Report is planning artifact (committed), not runtime; project is not security-by-obscurity
- **Verification:**
  - `docs/testing/test-gap-report.md` is output of `analyze-coverage.sh` (documentation, not deployed) ✓
  - Content shows feature names, finding IDs, test counts — all derived from public FRD/REQUIREMENTS.md ✓
  - No secrets, credentials, or proprietary algorithms disclosed ✓
- **Verdict:** Risk accepted per plan; disclosure is intentional documentation

### T-08-04: Developer claims test exists but gap report doesn't detect (Repudiation) — MITIGATED ✓
- **Plan disposition:** mitigate
- **Declared mitigation:** Coverage analysis scans actual file paths (`find services -name "*Test.java"`), not self-reports
- **Verification:**
  - `scripts/traceability/analyze-coverage.sh:34-35` — `find services -name "*Test.java"` and `find frontend/e2e -name "*.spec.ts"` ✓
  - Script reads traceability matrix status but cross-checks with filesystem ✓
  - If test file exists but isn't detected, script logic (not attacker) is at fault ✓
- **Verdict:** Mitigation implemented; false-negative is tool bug, not security exploit

### T-08-05: Developer disables fail-on-error in CI workflow (Tampering) — MITIGATED ✓
- **Plan disposition:** mitigate
- **Declared mitigation:** Branch protection "Do not allow bypassing" prevents merge without CI pass; code review catches `continue-on-error: true` additions
- **Verification:**
  - `.github/workflows/regression-suite.yml:69` — `fail-on-error: true` present in test-reporter action ✓
  - No `continue-on-error: true` found in backend-tests or frontend-tests jobs ✓
  - Summary job (lines 122-128) exits 1 if either job fails ✓
  - Tampering would require PR to modify workflow file — caught by code review + branch protection ✓
- **Verdict:** Mitigation implemented; branch protection setup documented in `docs/ci/branch-protection-setup.md`

### T-08-06: Admin bypasses branch protection (Elevation of Privilege) — ACCEPTED ✓
- **Plan disposition:** accept
- **Declared mitigation:** GitHub "Do not allow bypassing" blocks admins, but repo owner can toggle; platform trust assumption documented
- **Verification:**
  - `docs/ci/branch-protection-setup.md:396` — "Do not allow bypassing the above settings: Admins cannot bypass" documented ✓
  - Line 529 note acknowledges repo owner can toggle setting ✓
  - This is GitHub platform governance (outside Phase 8 scope) ✓
- **Verdict:** Risk accepted per plan; documented as requirement

### T-08-07: Malicious test causes CI to hang (DoS) — MITIGATED ✓
- **Plan disposition:** mitigate
- **Declared mitigation:** Add explicit `timeout-minutes: 30` to backend-tests and frontend-tests jobs
- **Verification:**
  - `.github/workflows/regression-suite.yml:17` — `timeout-minutes: 30` on backend-tests job ✓
  - Line 74 — `timeout-minutes: 30` on frontend-tests job ✓
  - Default GitHub Actions timeout (360 min) overridden with tighter bound ✓
- **Verdict:** Mitigation implemented per plan (08-03-SUMMARY.md documents this as auto-fix for missing critical mitigation)

### T-08-08: Open Question marked RESOLVED without test (Repudiation) — MITIGATED ✓
- **Plan disposition:** mitigate
- **Declared mitigation:** Code review checklist requires test_ref + passing CI; matrix update auditable in git history
- **Verification:**
  - `docs/testing/open-question-resolution-protocol.md:256-267` — 6-item code review checklist includes:
    - [ ] Test exists and covers decision ✓
    - [ ] Test passes in CI ✓
    - [ ] Traceability matrix updated with test reference ✓
    - Reject if decision documented but test missing ✓
  - Workflow enforces test must exist before resolution (Step 3 before Step 4) ✓
  - Git history of `.planning/traceability-matrix.csv` provides audit trail ✓
- **Verdict:** Mitigation implemented via process enforcement

### T-08-09: Test stub templates contain incorrect logic (Tampering) — ACCEPTED ✓
- **Plan disposition:** accept
- **Declared mitigation:** Stubs are templates (placeholders), not production code; decision-specific logic marked "fill in"; code review mitigates
- **Verification:**
  - `.planning/test-stubs/open-questions/OQ-02-conflict-enforcement.md:48` — "DECISION-SPECIFIC LOGIC: Choose ONE test based on resolution" clearly marked ✓
  - Stubs provide multiple option skeletons (Option A, B, C) — not executable production logic ✓
  - Code review of actual test implementations (not stubs) is the gate ✓
- **Verdict:** Risk accepted per plan; stubs are templates by design

---

## Additional Attack Surface (Retroactive Scan)

### Command Injection in query-matrix.sh user arguments
- **Surface:** User-supplied `FEATURE` and `FINDING` arguments in `by-feature`, `by-status`, `finding` commands
- **Analysis:**
  - Line 51: `FEATURE="$1"` — user input assigned to variable
  - Line 53: `grep -E "(,|\")$FEATURE([^0-9]|$)"` — feature used in regex pattern (NOT command substitution)
  - Line 79-82: `FINDING` validated with `grep -qE '^BF-[0-9]{3}$'` before use
  - Line 86: `grep -F "$FINDING,"` — literal match (not regex, no injection)
- **Refutation:** Arguments are used in grep patterns (safe) or validated against bounded regex. No eval, no unquoted expansion in command position.
- **Verdict:** SAFE

### Path Traversal in run-full-regression.sh service iteration
- **Surface:** `for service_dir in */` loop at line 34 iterates directories
- **Analysis:**
  - Line 6-7: `PROJECT_ROOT` derived from `BASH_SOURCE[0]` and anchored via `cd "${PROJECT_ROOT}"`
  - Line 33: `cd services` is literal path (not user input)
  - Line 34: `service_dir` from glob `*/` within `services/` directory
  - Line 38: `if [ ! -f "${service_dir}pom.xml" ]` validates service directory has pom.xml before processing
  - Line 44: `(cd "$service_dir" && mvn test -q)` — subshell with quoted path
- **Refutation:** No user input controls service directory names; validated via pom.xml check; path is quoted and relative to known PROJECT_ROOT.
- **Verdict:** SAFE

### Secret Leakage in CI workflow environment variables
- **Surface:** GitHub Actions workflow exposes DB credentials in env vars (lines 22-25, 48-53)
- **Analysis:**
  - Postgres service credentials: `POSTGRES_PASSWORD: test_pass` (line 25)
  - Backend test env: `DB_PASSWORD: test_pass` (line 53)
  - These are **ephemeral test credentials** for CI-internal postgres:16 container
  - Not production secrets; container destroyed after job completes
  - No real data or external DB connection
- **Refutation:** "test_pass" is a literal test credential for throwaway CI database, not a secret. Standard practice for CI test harnesses.
- **Verdict:** SAFE (test credentials only)

### Information Disclosure via test stub decision options
- **Surface:** Test stubs in `.planning/test-stubs/open-questions/` expose multiple implementation options
- **Analysis:**
  - `OQ-02-conflict-enforcement.md` shows 3 options: hard-block, soft-warning, always-allow
  - `OQ-07-feed-access-gating.md` notes Phase 7 implemented "fully public" feeds
  - Stubs are planning artifacts (not deployed), documenting **forward decisions** not yet made
- **Refutation:** Stubs document known unknowns (Open Questions). Exposing decision space is intentional for product owner review. No proprietary algorithms or trade secrets disclosed.
- **Verdict:** SAFE (accepted per T-08-03 info disclosure reasoning)

### Denial of Service via analyze-coverage.sh find operations
- **Surface:** `find services -name "*Test.java"` and `find frontend/e2e -name "*.spec.ts"` at lines 34-35
- **Analysis:**
  - `find` with literal paths (`services`, `frontend/e2e`) — no user input
  - `2>/dev/null` suppresses errors (safe: missing dir returns 0 count, not hang)
  - Worst case: large repo causes slow find, but script is local developer tool (not API endpoint)
- **Refutation:** No attacker control over find paths; unbounded result size would slow script, not crash/hang; not a remote attack surface.
- **Verdict:** SAFE

### CSV Injection in traceability-matrix.csv notes field
- **Surface:** `seed-matrix.sh` line 42 sets `notes="Open Question - see open-questions.md"` — user could edit baseline-inventory.md
- **Analysis:**
  - Input is committed `.planning/phases/01-legacy-functional-audit/baseline-inventory.md` (Phase 1 artifact)
  - Not runtime user input; requires git commit access
  - CSV output is planning artifact (not imported to spreadsheet by untrusted users)
  - Line 47-49: requirement_ref with commas is quoted (CSV-safe)
- **Refutation:** Input file is version-controlled (attacker needs commit access, at which point they can tamper with any code). CSV is not consumed by application logic (only by bash scripts and human review).
- **Verdict:** SAFE (commit-access threat model assumed; CSV not in untrusted spreadsheet tool)

---

## Threat Model Completeness Check

All 9 threats from PLAN threat registers audited:
- ✅ T-08-01 (Tampering: matrix edits) — ACCEPTED, mitigation verified
- ✅ T-08-02 (DoS: seed-matrix loop) — MITIGATED, `set -euo pipefail` + bounded regex verified
- ✅ T-08-03 (Info Disclosure: gap report) — ACCEPTED, documentation artifact verified
- ✅ T-08-04 (Repudiation: test detection) — MITIGATED, `find` filesystem scan verified
- ✅ T-08-05 (Tampering: CI disable) — MITIGATED, `fail-on-error: true` + branch protection verified
- ✅ T-08-06 (EoP: admin bypass) — ACCEPTED, GitHub platform trust documented
- ✅ T-08-07 (DoS: CI hang) — MITIGATED, `timeout-minutes: 30` verified
- ✅ T-08-08 (Repudiation: OQ w/o test) — MITIGATED, code review checklist verified
- ✅ T-08-09 (Tampering: stub logic) — ACCEPTED, template nature verified

Additional 6 retroactive surface areas scanned and verified SAFE.

**Verdict: SECURED — Phase 8 is safe to ship. All declared mitigations implemented, all accepted risks documented, no open HIGH/CRITICAL findings.**
