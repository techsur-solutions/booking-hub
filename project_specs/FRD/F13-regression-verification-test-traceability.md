## F13: Regression Verification & Test Traceability

**Description:** F13 is the mechanism that makes the project's "no functionality lost" guarantee objectively verifiable: a maintained traceability matrix linking every F0 audit finding to the FRD requirement(s) it produced (this document), the implementation that satisfies it, and the automated test that proves it, enforced as a CI-blocking regression suite.

**Terminology:**
- **Traceability matrix:** A structured record with one row per F0 audit finding, columns for `requirement_ref` (FRD section/bullet), `implementation_ref` (code location/PR), and `test_ref` (test identifier), plus status (`not started` / `implemented` / `tested` / `verified`).
- **Parity baseline:** The complete set of F0 audit findings, against which "zero functionality lost" is measured.
- **CI-enforced regression suite:** The automated test suite that must pass before any release, gating merges/deploys.

**Sub-features:**
- Traceability matrix construction (seeded from F0)
- Automated test coverage for every validation rule, workflow transition, and permission check
- Targeted coverage for high-risk areas (conflict detection, approval/auto-approve interactions, recurring-booking scoping, permission boundaries)
- CI-enforced regression suite
- Open Question closure loop (resolution → test addition)

**Process:**
1. F13 ingests F0's baseline inventory (audit findings + Open Questions list) as the seed rows of the traceability matrix, one row per audit finding.
2. For every bullet in every feature chunk's Validation and Error States sections (F1–F12) in this FRD, a corresponding row is added or linked to the matrix with a `requirement_ref` pointing to that exact bullet/row (this FRD's per-rule phrasing was written specifically to make this 1:1 mapping possible — see `00-header.md` Conventions, "Testability requirement").
3. As each requirement is implemented, the `implementation_ref` column is populated (e.g., commit/PR link or code module reference).
4. As each requirement is covered by an automated test (unit, integration, or end-to-end as appropriate to the rule — e.g., a single validation bullet typically maps to a unit test, a cross-service workflow like F3's approve transition typically maps to an integration test, a full booking-creation-to-notification path typically maps to an end-to-end test), the `test_ref` column is populated and status moves to `tested`.
5. High-risk areas receive explicit, non-negotiable test coverage regardless of general matrix progress: every conflict-detection edge case (F2, including the interim hard-block default and its reversal if F0 changes it), every approval/auto-approve interaction (F3, including the interim notification-event default), every recurring-booking edit/delete scoping rule (F1, once F0 resolves PRD Open Question #1), and every permission-matrix boundary (F7, once F0 completes the permission mapping table).
6. The CI-enforced regression suite runs on every change; a change that would regress a previously `tested` requirement (i.e., makes its linked test fail) blocks the merge/release.
7. When an Open Question (F0 §Process step 9) is resolved — either via evidence or an explicit Key Decision — the corresponding FRD chunk is updated to remove its `[OPEN QUESTION]` flag (per `00-header.md` Conventions), a new/updated requirement row is added to the matrix, and a corresponding test is added before the resolution is considered closed, per PRD Section 10's resolution process ("No open question may be closed by assumption").
8. Before general release, the matrix is reviewed against PRD Section 7's success metrics: 100% of F0-documented legacy features have an implemented requirement and at least one passing test; zero open P0/P1 functional-parity gaps remain unresolved or unaccepted; 100% of legacy permission rules are mapped and boundary-tested; conflict-detection false-negative rate is 0% in the regression suite; 100% of notification-triggering events result in delivered-or-retried-to-success notifications in staging load tests; every service demonstrates independent deployability in staging; 100% of state-changing actions in the regression suite produce a corresponding audit log entry.

**Inputs:**
- F0 baseline inventory (audit findings + Open Questions list).
- This FRD's per-feature Validation/Error States bullets (the requirement source for matrix rows).
- Implementation commits/PRs (for `implementation_ref`).
- Test suite results (for `test_ref` and pass/fail status).

**Outputs:**
- The traceability matrix itself (structured data: spreadsheet, database, or test-management tool export), queryable by audit finding, requirement, implementation, or test.
- CI pipeline gate result (pass/fail) per change.
- Release readiness report against PRD Section 7 success metrics.

**Validation:**
- Every F0 audit finding has at least one linked FRD requirement row (no orphaned findings).
- Every FRD Validation/Error States bullet in F1–F12 has at least one linked test before that feature is considered release-ready.
- No requirement row may be marked `tested`/`verified` without a concrete `test_ref` that actually exercises the stated rule (not a placeholder).
- A merge/release is blocked if any previously `tested` requirement's linked test now fails (regression).
- Every Open Question closure (F0 §Process step 9/10) produces both a matrix update and a new test before the question is marked resolved.

**Error States:**
| Scenario | Impact | Required Action |
|---|---|---|
| An F0 audit finding has no corresponding FRD requirement row | Potential silent feature loss | Block release; add the missing FRD requirement and matrix row |
| A requirement is marked `tested` without a real linked test | False confidence in parity | Reject in matrix review; require a genuine test before status change |
| CI regression suite fails on a previously passing test | Functional regression introduced | Block merge/deploy until fixed or the behavior change is an explicit, approved Key Decision with matrix/test update |
| An Open Question is marked resolved without a corresponding new test | Violates PRD's "no assumption closure" rule | Reject the resolution; require test addition before closure |

**API Surface (this feature):** No client-facing runtime API; F13's traceability matrix and CI gate are development-process tooling, not a deployed service. If a traceability-matrix viewer is built as an internal tool, its endpoints would be listed in `Y1-api.md` §Traceability (optional, non-blocking).

**Schema Surface (this feature):** If the traceability matrix is implemented as a persisted store rather than an external tool (e.g., spreadsheet/test-management SaaS), it would own a `traceability_entries` table — see `Y0-schema.md` §Traceability (optional). Not required to be a Booking-Hub-hosted service; may be satisfied by existing test-management tooling.
