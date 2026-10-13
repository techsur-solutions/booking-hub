---
phase: 8
status: issues_found
blockers: 2
warnings: 5
files_reviewed: 14
files_reviewed_list:
  - .github/workflows/regression-suite.yml
  - .planning/traceability-matrix.csv
  - .planning/traceability-matrix-schema.md
  - .planning/test-stubs/open-questions/OQ-02-conflict-enforcement.md
  - .planning/test-stubs/open-questions/OQ-03-multi-resource-conflict.md
  - .planning/test-stubs/open-questions/OQ-07-feed-access-gating.md
  - .planning/test-stubs/open-questions/OQ-other-stubs.md
  - .planning/test-stubs/open-questions/README.md
  - docs/ci/branch-protection-setup.md
  - docs/testing/open-question-resolution-protocol.md
  - docs/testing/test-gap-report.md
  - docs/testing/test-writing-guide.md
  - frontend/package.json
  - scripts/ci/run-full-regression.sh
  - scripts/traceability/analyze-coverage.sh
  - scripts/traceability/query-matrix.sh
  - scripts/traceability/seed-matrix.sh
reviewed_at: 2026-10-13T04:37:47Z
iteration: 1
---

# Phase 8 Code Review

## BLOCKERs

### B1: CI workflow sets `VITE_E2E_MODE=true` at job level, which causes `auth-redirect.spec.ts` to always fail in CI

- **File:** `.github/workflows/regression-suite.yml`:97-99 (env block in `frontend-tests` job)
- **Category:** bug
- **Evidence:**
  The `frontend-tests` job sets `VITE_E2E_MODE: 'true'` as a step-level env var for the "Run E2E tests" step (lines 97-99). When `npx playwright test` runs, it spawns the vite dev server via playwright's `webServer` config. Child processes inherit all environment variables from the parent process. Therefore vite receives `VITE_E2E_MODE=true`, which causes `AuthProvider.tsx:32` to immediately set `authenticated=true` and skip Keycloak initialization entirely.

  `auth-redirect.spec.ts` explicitly tests that navigating to `/calendar` triggers a Keycloak PKCE redirect (`expect(keycloakRequests.length).toBeGreaterThan(0)`). Because `VITE_E2E_MODE=true` suppresses the redirect for **all** tests, this assertion fails unconditionally in CI.

  `playwright.config.ts` attempts to handle this with a `PW_TEST_TYPE !== 'auth'` guard in `webServer.env`, but `PW_TEST_TYPE` is never set by the CI workflow, and the guard only controls what `webServer.env` additionally sets — it does **not** unset the already-inherited `VITE_E2E_MODE` from the parent environment.

  **Concrete failing scenario:** Push to `feature/**` → CI runs `frontend-tests` → `auth-redirect.spec.ts::should redirect to Keycloak for protected routes with PKCE` → assertion `keycloakRequests.length > 0` fails → `frontend-tests` job exits non-zero → `regression-summary` blocks merge. The CI enforcement mechanism is broken for all PRs from day one.

- **Fix direction:** Remove `VITE_E2E_MODE` from the CI step env. Instead, set it only in `playwright.config.ts`'s `webServer.env` block (where the `PW_TEST_TYPE` guard can selectively apply it), or use a Playwright project-level `env` or `grep`/`testMatch` filter to exclude `auth-redirect.spec.ts` from the VITE_E2E_MODE-enabled run.

---

### B2: `query-matrix.sh` `by-feature` and `coverage-summary` commands, and `analyze-coverage.sh`, all use an unanchored substring grep that produces false-positive feature matches — the generated `test-gap-report.md` already contains the wrong data

- **File:** `scripts/traceability/query-matrix.sh`:53, 69-72; `scripts/traceability/analyze-coverage.sh`:46,51; `docs/testing/test-gap-report.md` (the already-committed output)
- **Category:** bug
- **Evidence:**
  `by-feature F1` runs `grep ",$feature"` which matches `,F1`, `,F10`, `,F11`, `,F12`, `,F13`, `,F1.x` — any value containing the substring `,F1`. Verified empirically:
  ```
  $ tail -n+2 .planning/traceability-matrix.csv | grep -c ',F1'
  25
  ```
  Those 25 hits include BF-082–BF-087 (`F10.x`), BF-096–BF-108 (`F11.x`), and BF-111 (`F12`) — none of which belong to F1. The same substring match is used in `analyze-coverage.sh` line 46 (`FEATURE_TOTAL`) and line 51 (`FEATURE_TESTED`), meaning **coverage percentages are computed over the wrong finding sets**.

  The already-committed `docs/testing/test-gap-report.md` reflects this wrong data: the F1 section reports 25 findings with gaps including BF-082, BF-083, BF-086, BF-087, BF-096–BF-102, BF-105, BF-107, BF-108, BF-111 — which are actually F10, F11, and F12 findings. They also appear (correctly) in their own feature sections, so counts are inflated and gaps are duplicated.

  This matters because F13.4 requires the gap report to accurately gate release readiness. Inflated "not tested" counts for F1 could create false urgency; or if a future version auto-gates on gap counts, it will gate incorrectly.

- **Fix direction:** Replace `grep ",$feature"` with a word-boundary-aware pattern, e.g., `grep -E ",(\"?)$feature([,.\"]|$)"` or use `awk` to split on commas and compare field 2 exactly. The `coverage-summary` loop in `query-matrix.sh` and both grep lines (46, 51) in `analyze-coverage.sh` need the same fix. The committed `test-gap-report.md` must be regenerated after the script is corrected.

---

## WARNINGs

### W1: `analyze-coverage.sh` feature name lookup always produces "Unknown" — wrong grep pattern against `REQUIREMENTS.md`

- **File:** `scripts/traceability/analyze-coverage.sh`:55
- **Evidence:**
  ```bash
  FEATURE_NAME=$(grep -A 1 "^\*\*$feature\*\*:" .planning/REQUIREMENTS.md | tail -1 | sed ...)
  ```
  The grep anchors to `^**F1**:` (start of line). But `REQUIREMENTS.md` has lines formatted as `- [ ] **F1**: User can create…` — they start with `- [ ] ` not `**`. The pattern never matches. The `|| echo "Unknown"` fallback fires for every feature, confirmed by `test-gap-report.md` showing `### F1: Unknown`, `### F2: Unknown`, etc. for all 13 features. The report is navigable but every feature heading is uninformative.

- **Fix direction:** Change the grep pattern to `grep -E "^\s*-\s*\[\s*\]\s*\*\*$feature\*\*:"` or use a simpler `grep "\*\*$feature\*\*:"` (without line-start anchor) to match the actual REQUIREMENTS.md format.

---

### W2: OQ-03 stub file maps to wrong traceability finding IDs (`BF-029`, `BF-030` instead of `BF-113`)

- **File:** `.planning/test-stubs/open-questions/OQ-03-multi-resource-conflict.md`:8
- **Evidence:**
  OQ-03 is "multi-resource conflict scope" — which is explicitly the subject of BF-113 (`F2.1, "Multi-resource conflict aggregation semantics unconfirmed", Open Question`). But the stub's "Traceability findings" field lists `BF-029, BF-030`. Checking the matrix:
  ```
  BF-029,F4.1,,,not_started,   ← "Confirmed Location field set: name, building…"
  BF-030,F4.2,,,not_started,   ← "Confirmed Resource field set: name, description…"
  ```
  These are confirmed, non-conflicting reference-data schema findings — completely unrelated to conflict detection. When a developer resolves OQ-03 and follows Step 6 of the protocol ("Update traceability matrix for BF-029, BF-030"), they will mark the wrong rows as tested and leave BF-113 (the actual conflict-related finding) unlinked to any OQ-03 test.

- **Fix direction:** Change the "Traceability findings" line in OQ-03 stub to reference `BF-113` (and possibly `BF-112` which is the related hard-block vs. non-blocking question).

---

### W3: Stub README `open-questions/README.md` has OQ numbering inconsistent with `open-question-resolution-protocol.md` — three stub entries use wrong OQ IDs

- **File:** `.planning/test-stubs/open-questions/README.md`:14-16
- **Evidence:**
  Comparing the two authoritative lists:

  | Item | Resolution Protocol | Stub README |
  |------|-------------------|-------------|
  | Item 14 (deletion policy) | OQ-14 | *(missing — covered in OQ-other-stubs under OQ-14 label, but not in README table)* |
  | Item 15 (checkavailability) | OQ-15 | **OQ-15 = "Email enumeration" (wrong!)** |
  | Item 19 (password-reset re-check) | OQ-19 | OQ-16 (off by one) |
  | Item 20 (email enumeration) | OQ-20 | OQ-15 (wrong) |
  | Item 22 (admin-cannot-assume-admin) | OQ-22 | OQ-12 (wrong) |
  | Item 7 (feed gating, RESOLVED) | not in list | OQ-07 (stub exists for a RESOLVED question) |

  A developer following the README to resolve "OQ-15 (checkavailability)" will implement a password-reset stub instead, and vice-versa.

- **Fix direction:** Reconcile the README table against `open-question-resolution-protocol.md` as the authoritative source. Remove OQ-07 from the stub list (it is already RESOLVED per `open-questions.md`). Renumber OQ-12, OQ-15, OQ-16 to match OQ-22, OQ-20, OQ-19.

---

### W4: `run-full-regression.sh` frontend output-parsing logic will false-report passing tests as failures when the playwright `list` reporter summary line has leading whitespace

- **File:** `scripts/ci/run-full-regression.sh`:64-74
- **Evidence:**
  ```bash
  if npm run test:e2e 2>&1 | tee /tmp/pw-output.txt | grep -qE '^[0-9]+ (passed|failed)'; then
  ```
  The playwright `list` reporter emits the summary line as `  2 passed (3.1s)` (two leading spaces). The regex `^[0-9]+` anchors at line-start — it will not match a line beginning with spaces. When grep returns exit 1 (no match), the `if` condition is false, and the script falls into the `else` branch: `echo "❌ Frontend tests FAILED (non-zero exit)"` and increments `FAILED`. This occurs even when playwright exited 0 and all tests passed.

  Note: this bug is confined to the local `run-full-regression.sh` script. The CI workflow (`.github/workflows/regression-suite.yml` line 99) runs `npx playwright test` directly and relies on exit code only — CI is not affected.

- **Fix direction:** Replace the output-parsing approach with a simple exit-code check: `if npm run test:e2e; then echo "✅ passed"; else echo "❌ FAILED"; FAILED=$((FAILED+1)); fi`. The tee/grep output-parsing is unnecessary complexity.

---

### W5: `query-matrix.sh` `finding` command is vulnerable to basic regex metacharacter expansion in user-supplied `FINDING` argument

- **File:** `scripts/traceability/query-matrix.sh`:82
- **Evidence:**
  ```bash
  tail -n +2 "$MATRIX" | { grep "^$FINDING," || true; }
  ```
  `$FINDING` is raw user input passed directly into a grep BRE pattern. A value like `BF-.28` (dot is regex metachar) would match `BF-028`, `BF-128`, `BF-028` etc. While this is a developer tool with no network exposure and the input is constrained by convention, it is still a documentation-level defect: passing `BF-028|BF-001` would attempt a `|`-extended expression (which basic grep ignores, but an `-E` grep would not), and any metachar like `.`, `*`, or `[` produces unintended behaviour without an error message. For a traceability lookup tool, wrong matches are silent data quality errors.

- **Fix direction:** Either use `grep -F "^$FINDING,"` (fixed-string, no regex interpretation) or validate that `$FINDING` matches `^BF-[0-9]{3}$` before using it in grep.

---

## Cross-file seams checked

- `frontend/package.json` `test:e2e` script → `playwright.config.ts` `testDir`/`reporter` config: **OK** — both use `--workers=1 --reporter=list` and target `./e2e`.
- `regression-suite.yml` `frontend-tests` job → `frontend/package.json` scripts: **BLOCKER B1** — `VITE_E2E_MODE=true` at job level breaks `auth-redirect.spec.ts`.
- `regression-suite.yml` `backend-tests` → `services/*/pom.xml` existence: **OK** — all 9 service directories have `pom.xml`; CI `for service_dir in */` loop will not encounter a missing-pom failure.
- `run-full-regression.sh` `cd services` loop → `services/*/pom.xml` existence: **OK** — all 9 services have pom.xml; the local script's skip-if-no-pom guard is present as a defensive measure.
- `analyze-coverage.sh` output → `docs/testing/test-gap-report.md` content: **BLOCKER B2** — committed report already reflects the false-positive feature grep; report is wrong.
- `seed-matrix.sh` output format → `traceability-matrix-schema.md` column spec: **OK** — printf `'%s,%s,,,%s,%s\n'` produces 6 fields (5 commas) matching `finding_id,requirement_ref,implementation_ref,test_ref,status,notes`.
- `query-matrix.sh` `gaps` command — double `{ grep -v … || true; }` pipeline: **OK** — tested empirically; correctly excludes both `tested` and `verified` rows, leaving `not_started` and `implemented`.
- `OQ-03-multi-resource-conflict.md` traceability IDs → `traceability-matrix.csv`: **WARNING W2** — BF-029/030 are wrong; BF-113 is the correct conflict-scope finding.
- `open-question-resolution-protocol.md` OQ table → stub `README.md` OQ table: **WARNING W3** — three OQ IDs are mis-numbered; OQ-07 stub exists for an already-RESOLVED question.
- `regression-suite.yml` `regression-summary` job `needs: [backend-tests, frontend-tests]` with `if: always()` → check logic: **OK** — the `if [ "…result" != "success" ] || [ "…result" != "success" ]` check correctly fails the summary when either job is non-success, even when jobs are skipped/cancelled.
- `analyze-coverage.sh` feature name lookup → `REQUIREMENTS.md` format: **WARNING W1** — grep pattern `^\*\*$feature\*\*:` never matches; all feature names are "Unknown".
