# Branch Protection Setup

**Purpose:** Enforce F13.4 requirement that regression suite must pass before any release.

## GitHub Branch Protection Rules

Navigate to repository **Settings → Branches → Branch protection rules → Add rule**

### Rule for `main` branch:

**Branch name pattern:** `main`

**Protect matching branches:**

- ✅ **Require a pull request before merging**
  - Require approvals: 1 (recommended for production)
  - Dismiss stale pull request approvals when new commits are pushed: ✅

- ✅ **Require status checks to pass before merging**
  - Require branches to be up to date before merging: ✅
  - **Status checks that are required:**
    - `Regression Suite Summary` (the `regression-summary` job from workflow)
    - `Backend Integration Tests` (optional — summary covers it)
    - `Frontend E2E Tests` (optional — summary covers it)

  **Note:** Status check names appear in the list AFTER the workflow runs at least once. Push a commit to trigger the workflow, then return here to select the checks.

- ✅ **Require conversation resolution before merging**
  - All PR review comments must be resolved before merge

- ✅ **Do not allow bypassing the above settings**
  - Admins cannot bypass (ensures even urgent fixes are tested)

**Result:** Any PR that fails the regression suite is blocked from merging until fixed.

## Local Pre-Push Verification

Developers should run the local regression suite before pushing:

```bash
bash scripts/ci/run-full-regression.sh
```

This runs the SAME tests CI will run, catching failures early.

**Pre-push hook (optional):**

Add to `.git/hooks/pre-push`:

```bash
#!/bin/bash
echo "Running regression suite before push..."
if ! bash scripts/ci/run-full-regression.sh; then
  echo ""
  echo "❌ Push aborted: regression suite failed"
  echo "Fix test failures before pushing."
  exit 1
fi
```

Make executable: `chmod +x .git/hooks/pre-push`

**Note:** This is optional — CI is the enforcement point, not local hooks.

## Monitoring Test Failures

**When CI fails:**

1. Check **Actions** tab for workflow run details
2. Click failed job (Backend/Frontend) to see logs
3. Download test report artifacts (JUnit XML / Playwright HTML)
4. Fix failing tests locally
5. Re-push — CI re-runs automatically

**When a previously passing test now fails (regression detected):**

1. This is the EXACT scenario F13.4 exists to catch
2. DO NOT merge the PR
3. Either:
   - Fix the code to restore passing behavior (if unintended regression)
   - Update the test if behavior intentionally changed (rare — requires product owner approval)

## Updating Traceability Matrix on Test Changes

When a test is added/modified:

1. Update `.planning/traceability-matrix.csv` with test_ref and status
2. Re-run coverage analysis:
   ```bash
   bash scripts/traceability/analyze-coverage.sh
   ```
3. Commit matrix + coverage report updates in same PR as test changes

## CI Workflow Maintenance

**Updating the workflow:**

File: `.github/workflows/regression-suite.yml`

Common changes:
- Add new test suites (e.g., integration tests for Phase 6 RabbitMQ consumers)
- Adjust timeouts (if tests grow slower)
- Add caching (Maven local repo, npm cache — already configured)

**Do NOT:**
- Remove `fail-on-error: true` (defeats F13.4 enforcement)
- Add `continue-on-error: true` to test jobs
- Skip tests in CI while enabling locally

## Troubleshooting

**"Status check not found" when setting up branch protection:**
- Workflow must run successfully at least once before status check appears
- Push a commit to main, wait for workflow, then configure protection

**Tests pass locally but fail in CI:**
- Check CI Postgres version matches docker-compose (postgres:16)
- Check environment variables (SPRING_PROFILES_ACTIVE=test, VITE_E2E_MODE=true)
- Download test artifacts from failed run for detailed logs

**CI is slow:**
- Backend tests: Maven dependency caching configured (uses `actions/setup-java` cache)
- Frontend tests: npm cache configured, Playwright browsers cached
- Typical run time: 5-10 minutes for full suite

**Timeout mitigations (F13.4 threat T-08-07 — CI hang DoS):**
- Both `backend-tests` and `frontend-tests` jobs have `timeout-minutes: 30`
- GitHub Actions also enforces a 6-hour global job timeout as a backstop

## First-Time Setup Checklist

After merging this PR:

1. **Trigger first workflow run:**
   ```bash
   git push origin main
   ```
   Wait for the workflow to complete in the **Actions** tab.

2. **Configure branch protection:**
   - Settings → Branches → Add rule → Branch name pattern: `main`
   - Enable all settings listed above
   - In "Status checks", search for and select `Regression Suite Summary`

3. **Verify enforcement:**
   - Create a failing test (e.g., `assertThat(true).isFalse()`)
   - Push to a feature branch, open a PR
   - CI runs, fails
   - GitHub blocks "Merge" button: "Required status check has not succeeded"
   - Revert the failing test, push, CI passes, merge is unblocked

---
*Branch protection is the enforcement mechanism for "no functionality lost" — do not disable it.*
