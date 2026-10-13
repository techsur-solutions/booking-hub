---
phase: 08-regression-verification-test-traceability
plan: 03
subsystem: testing
tags: [github-actions, ci, playwright, maven, branch-protection, regression]

# Dependency graph
requires: []
provides:
  - GitHub Actions workflow running full backend (Maven) + frontend (Playwright) regression suite on push/PR
  - Local pre-push regression runner script for developer use
  - Branch protection setup documentation with exact GitHub settings
affects: []

# Tech tracking
tech-stack:
  added: [github-actions, dorny/test-reporter@v1, actions/setup-java@v4, actions/setup-node@v4]
  patterns:
    - CI jobs run in parallel (backend-tests + frontend-tests) with a consolidating summary job for branch protection
    - 30-minute per-job timeout mitigates T-08-07 (CI hang DoS) from threat model
    - VITE_E2E_MODE bypass enables frontend E2E tests without live backend in CI

key-files:
  created:
    - .github/workflows/regression-suite.yml
    - scripts/ci/run-full-regression.sh
    - docs/ci/branch-protection-setup.md
  modified:
    - frontend/package.json

key-decisions:
  - "Added timeout-minutes: 30 to backend-tests and frontend-tests jobs per T-08-07 threat model mitigation"
  - "regression-summary job is the single branch protection status check (covers both backend and frontend)"
  - "test:e2e added to frontend/package.json so local runner and CI share one canonical command"

patterns-established:
  - "CI summary job pattern: parallel test jobs feed a consolidating job whose name is the branch protection status check"

# Metrics
duration: 2min
completed: 2026-10-13
---

# Phase 8 Plan 3: Regression Suite CI Enforcement Summary

**GitHub Actions workflow (3 jobs: backend Maven + frontend Playwright + summary gate) with local pre-push runner and branch protection documentation implementing F13.4 CI-enforced regression requirement**

## Performance

- **Duration:** 2 min
- **Started:** 2026-10-13T04:12:47Z
- **Completed:** 2026-10-13T04:15:10Z
- **Tasks:** 3
- **Files modified:** 4

## Accomplishments

- CI workflow with parallel backend (Maven/JUnit against postgres:16) and frontend (Playwright) jobs, consolidated by a `regression-summary` status check suitable for branch protection
- Local `scripts/ci/run-full-regression.sh` iterates all Maven services and runs frontend Playwright tests, exits 0/1 so it works as a git pre-push hook
- `docs/ci/branch-protection-setup.md` (153 lines) documents exact GitHub settings, first-time checklist, monitoring, traceability matrix update procedure, and troubleshooting

## Task Commits

Each task was committed atomically:

1. **Task 1: Create GitHub Actions regression suite workflow** - `242ecf9` (feat)
2. **Task 2: Create local regression suite runner** - `9f65d1a` (feat)
3. **Task 3: Document branch protection setup** - `d2ff651` (docs)

**Plan metadata:** `(forthcoming)` (docs: complete plan)

## Files Created/Modified

- `.github/workflows/regression-suite.yml` - 3-job GitHub Actions workflow: backend-tests (mvn clean test + postgres:16), frontend-tests (Playwright), regression-summary (consolidation gate)
- `scripts/ci/run-full-regression.sh` - Local pre-push regression runner; auto-discovers Maven services, runs frontend via npm test:e2e or npx fallback
- `docs/ci/branch-protection-setup.md` - Branch protection configuration guide with first-time setup checklist, monitoring, traceability procedures, and troubleshooting
- `frontend/package.json` - Added `test:e2e` script (`playwright test --workers=1 --reporter=list`)

## Decisions Made

- **Timeout per job:** Added `timeout-minutes: 30` to `backend-tests` and `frontend-tests` per the plan's threat model (T-08-07: malicious test causes CI hang). The plan's STRIDE register listed this as a "mitigate" disposition.
- **Single status check for branch protection:** `regression-summary` is the canonical check name (covers both backend and frontend). Individual job names (`Backend Integration Tests`, `Frontend E2E Tests`) are documented as optional additions.
- **Canonical E2E command:** `test:e2e` added to `frontend/package.json` so the local runner and CI share one command, keeping them in sync automatically.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 2 - Missing Critical] Added timeout-minutes: 30 to CI jobs per threat model**
- **Found during:** Task 1 (Create GitHub Actions regression suite workflow)
- **Issue:** The plan's STRIDE threat register (T-08-07) listed "Malicious test causes CI to hang indefinitely" as a **mitigate** disposition with the note "Add explicit `timeout-minutes: 30`" — but the provided workflow YAML template omitted those fields
- **Fix:** Added `timeout-minutes: 30` to both `backend-tests` and `frontend-tests` jobs, implementing the mitigation the plan's own threat model required
- **Files modified:** `.github/workflows/regression-suite.yml`
- **Verification:** grep confirms `timeout-minutes: 30` present in both jobs
- **Committed in:** 242ecf9 (Task 1 commit)

**2. [Rule 1 - Bug] Improved local runner robustness: project-root resolution and path safety**
- **Found during:** Task 2 (Create local regression suite runner)
- **Issue:** The plan's script template used bare `cd services` / `cd ..` without anchoring to the project root, meaning the script would fail or produce wrong results if called from a subdirectory; `$service_dir/pom.xml` had a path quoting inconsistency
- **Fix:** Added `PROJECT_ROOT` derivation from `BASH_SOURCE[0]` and used `cd "${PROJECT_ROOT}"` as anchor throughout; fixed path quoting in pom.xml check
- **Files modified:** `scripts/ci/run-full-regression.sh`
- **Verification:** `bash -n` syntax check passes; script structure verified correct
- **Committed in:** 9f65d1a (Task 2 commit)

---

**Total deviations:** 2 auto-fixed (1 missing critical, 1 bug)
**Impact on plan:** Both fixes improve correctness and security without scope creep. The timeout fix implements the plan's own threat model. The path fix prevents silent wrong-directory failures.

## Known Stubs

None — all three deliverables contain real, functional implementations with no placeholder logic.

## Issues Encountered

None — all tasks executed cleanly.

## User Setup Required

None - no external service configuration required. Branch protection configuration is documented in `docs/ci/branch-protection-setup.md` and requires a one-time admin action after the first workflow run triggers.

## Next Phase Readiness

- CI regression enforcement infrastructure is complete (F13.4)
- Admin must configure GitHub branch protection rules per `docs/ci/branch-protection-setup.md` after first workflow run
- Phase 8 has remaining plans (08-04+) — this plan provides the CI gate that those plans' tests will run under

---
*Phase: 08-regression-verification-test-traceability*
*Completed: 2026-10-13*

## Self-Check: PASSED

- ✅ `.github/workflows/regression-suite.yml` exists on disk
- ✅ `scripts/ci/run-full-regression.sh` exists and passes `bash -n` syntax check
- ✅ `docs/ci/branch-protection-setup.md` exists (153 lines, >30 line minimum met)
- ✅ Commits exist: 242ecf9, 9f65d1a, d2ff651
- ✅ No stubs or blocking placeholders found
- ✅ No build step required (pure configuration/documentation plan — no compiled artifacts)
