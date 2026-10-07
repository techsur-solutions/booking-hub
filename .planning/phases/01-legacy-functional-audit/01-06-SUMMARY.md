---
phase: 01-legacy-functional-audit
plan: 06
subsystem: audit
tags: [legacy-audit, coldfusion, cfwheels, roombooking, traceability, open-questions]

# Dependency graph
requires:
  - phase: 01-legacy-functional-audit (01-01 through 01-05)
    provides: Five Wave 1 findings files covering all 12 controllers, 7 model groups, permission flag inventory, and per-environment settings behavior
provides:
  - "legacy-audit-findings.md: consolidated F0 deliverable, 12 controller + 7 model-group sections + Route Inventory + CF Lifecycle Events + Per-Environment Settings Behavior, all citations preserved verbatim from Wave 1"
  - "open-questions.md: canonical 25-item Open Questions list (10 PRD seed questions + 15 additional), each with explicit RESOLVED-with-citation or OPEN-with-unknown-because verdict"
  - "baseline-inventory.md: 111-row F13 traceability seed (BF-001 through BF-111), one row per distinct Wave 1 finding with stable ID, citation, and feature mapping"
  - "PROJECT.md Key Decisions: conflict-enforcement and public-feed-access rows updated from Interim-Pending to Corrected-with-citation"
  - "STATE.md: F0 sign-off gate recorded, Blockers/Concerns reflects actual audit outcome (12 resolved / 13 open of 25 items)"
affects: [02-platform-foundation, 03-identity-access, 04-reference-data, 05-core-booking, 13-regression-verification]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "F0 audit consolidation: merge-not-re-derive — every citation from source findings files is preserved verbatim through the consolidation pass"
    - "Open Questions verdict discipline: every item gets RESOLVED-with-citation or OPEN-with-unknown-because, never silently dropped or guessed"
    - "Key Decision correction discipline: PROJECT.md rows only move from Pending to Confirmed/Corrected on direct code evidence; genuinely undecidable items stay Pending with an open-questions.md cross-reference, never a fabricated resolution"

key-files:
  created:
    - .planning/phases/01-legacy-functional-audit/legacy-audit-findings.md
    - .planning/phases/01-legacy-functional-audit/open-questions.md
    - .planning/phases/01-legacy-functional-audit/baseline-inventory.md
  modified:
    - .planning/PROJECT.md
    - .planning/STATE.md

key-decisions:
  - "Conflict enforcement (PRD Open Question #2): legacy has NO permission-conditional hard-block/soft-warning split — Bookings.check() is unconditionally non-blocking for every user; create()/update() perform no server-side conflict validation at all. PROJECT.md's prior interim decision (hard block without allowApproveBooking, soft warning with it) is CORRECTED, not confirmed. Whether the new system should introduce a hard-block is a forward design decision, left OPEN in open-questions.md #2."
  - "Public feed access (PRD Open Question #7): legacy feeds are NOT fully public — rss2/ical/display require a valid per-user API token; allowAPI only gates the index listing page and is a no-op by default (granted to all roles). PROJECT.md's prior interim decision (fully public, no auth) is CORRECTED to token-gated, not role-gated."
  - "13 of 25 total open questions remain genuinely OPEN pending an explicit product/architecture decision this planning agent is not authorized to make — these are confirmed legacy facts with an undecided forward-looking design choice, not unconfirmed legacy ambiguities."

patterns-established:
  - "F0 sign-off gate: three canonical documents (legacy-audit-findings.md, open-questions.md, baseline-inventory.md) are the formal handoff artifacts; F13's traceability matrix consumes baseline-inventory.md directly as its seed input."

# Metrics
duration: 33min
completed: 2026-10-06
---

# Phase 01 Plan 06: Consolidate Legacy Audit Findings & Record F0 Sign-Off Summary

**Consolidated five Wave 1 findings files into the three FRD-named F0 deliverables (12 controller + 7 model-group sections, a 25-item Open Questions list, and a 111-row F13 traceability seed), then corrected two PROJECT.md interim Key Decisions with direct code citations and recorded the F0 sign-off gate in STATE.md — formally unblocking Phase 2+ planning.**

## Performance

- **Duration:** 33 min
- **Started:** 2026-10-06T16:06:58Z
- **Completed:** 2026-10-06T16:38:51Z
- **Tasks:** 3
- **Files modified:** 5 (3 created, 2 modified)

## Accomplishments

- Assembled `legacy-audit-findings.md` (1,204 lines): exactly one H2 section per legacy controller (12 total) and per legacy model group (7 total), in PRD Section 5 F0's canonical order, plus Route Inventory, CF Application Lifecycle Events, and Per-Environment Settings Behavior (all 5 environment subsections intact) — every citation preserved verbatim from the five Wave 1 source files, nothing re-derived.
- Built `open-questions.md` (580 lines, 25 items): all 10 PRD Section 10 seed questions in order, each with an explicit RESOLVED-with-citation or OPEN-with-"unknown because..." verdict, plus 15 additional questions Wave 1 discovered during the audit. Final tally: 12 RESOLVED, 13 OPEN.
- Built `baseline-inventory.md` (134 lines, 111 rows): one row per distinct Wave 1 finding (BF-001 through BF-111) with stable ID, source file, citation, description, status (Confirmed/Open), and Maps-To-Feature (F1–F13) — the direct seed input for F13's traceability matrix.
- Corrected two PROJECT.md Key Decisions rows (conflict enforcement, public feed access) from "Interim — Pending" to "Corrected" with direct code citations, since Wave 1 found the legacy code's actual behavior differs from both prior interim assumptions.
- Recorded the F0 sign-off gate in STATE.md's Blockers/Concerns section, replacing the stale roadmap-era "14 FRD-level flags" framing with the audit's real, cited outcome.

## Task Commits

Each task was committed atomically:

1. **Task 1: Assemble legacy-audit-findings.md** - `cf13d01` (feat)
2. **Task 2: Build open-questions.md and baseline-inventory.md** - `8c0193b` (feat)
3. **Task 3: Apply confirmed Key Decisions to PROJECT.md and record F0 sign-off gate in STATE.md** - `6262cc4` (feat)

**Plan metadata:** (this commit, following SUMMARY.md creation)

## Files Created/Modified

- `.planning/phases/01-legacy-functional-audit/legacy-audit-findings.md` — consolidated F0 deliverable (1,204 lines): 12 controller sections (Api, Bookings, Customfields, Eventdata, Locations, Logfiles, PasswordResets, Permissions, Resources, Sessions, Settings, Users), 7 model-group sections (Event, Eventresource, Location, Resource, Customfield/Customfieldjoin/Customfieldvalue, User, Settings (model)), plus Route Inventory, CF Application Lifecycle Events, and Per-Environment Settings Behavior.
- `.planning/phases/01-legacy-functional-audit/open-questions.md` — canonical 25-item Open Questions list (580 lines), items 1–10 mapping exactly to PRD Section 10's seed questions, items 11–25 are additional discoveries.
- `.planning/phases/01-legacy-functional-audit/baseline-inventory.md` — 111-row F13 traceability seed table (134 lines), `ID | Source File | Citation | Description | Status | Maps-To-Feature`.
- `.planning/PROJECT.md` — Key Decisions table: conflict-enforcement and public-feed-access rows updated with citations and corrected outcomes; "Last updated" footer bumped.
- `.planning/STATE.md` — Blockers/Concerns section rewritten to reflect the real 12-resolved/13-open audit outcome and the F0 sign-off gate; Current Position/Progress/Performance Metrics untouched (framework-maintained).

## Decisions Made

- **Conflict enforcement interim policy corrected, not confirmed:** The prior interim decision assumed a permission-conditional hard-block/soft-warning split tied to `allowApproveBooking`. Wave 1's code audit found this split does not exist anywhere in the legacy code — `Bookings.check()` is unconditionally non-blocking for every user, and `create()`/`update()` perform no server-side conflict validation at all. PROJECT.md's Key Decisions row now states this corrected fact with citation (`controllers/Bookings.cfc: check(), create(), update()`). The forward-looking question of whether the *new* system should introduce a hard-block (a design choice, not legacy parity) remains explicitly OPEN in `open-questions.md` #2 — not silently resolved.
- **Public feed access interim policy corrected, not confirmed:** The prior interim decision assumed feeds were fully public with no gate. Wave 1 found a third model: not role/permission-gated (the `allowAPI` flag only gates the human-browsed `index` page and is a no-op by default), but token-gated (`f_isValidAPIRequest` requires a valid per-user API token on the 3 actual data-serving actions). PROJECT.md's Key Decisions row now states this corrected fact with citation.
- **13 of 25 open questions deliberately left OPEN:** Every item that required a product/architecture decision this planning agent is not authorized to make (e.g., whether deleting a referenced Location should cascade/block vs. legacy's orphan-allow, whether the new system should hard-block conflicts for any user, whether permission-change caching should require a reload) was left explicitly OPEN with an "unknown because..." or "escalated to [phase]" statement — never guessed at, per the plan's core constraint.

## Deviations from Plan

None - plan executed exactly as written. All three tasks completed per their `<action>`/`<verify>`/`<done>` specifications; no bugs, missing dependencies, or architectural surprises were encountered during the consolidation-only (no re-derivation) work this plan specifies.

## Issues Encountered

None.

## User Setup Required

None - no external service configuration required. This plan produces only documentation artifacts (no running service, per FRD-BookingHub.md's F0 description: "F0 has no runtime API or schema of its own").

## Known Stubs

None found. A grep for `TODO|FIXME|placeholder|not.?implemented|coming soon` across all three created files returned only legitimate uses of the words "placeholder" and "stub" that describe *legacy ColdFusion code behavior* being audited (e.g., "`layouts` field has placeholder text `i.e boardroom,lecture`", "`onapplicationend.cfm` — empty stub"), not incomplete work in this plan's own deliverables.

## Next Phase Readiness

- **F0 sign-off gate is formally recorded and complete.** All three canonical F0 output documents exist, are cross-linked, and meet every Roadmap Phase 1 Success Criterion:
  1. `legacy-audit-findings.md` has a dedicated, cited section for all 12 controllers and all 7 model groups. ✓
  2. `open-questions.md` logs every ambiguous legacy behavior with a clear "unknown because..." statement (13 OPEN items); none are silently resolved. ✓
  3. Per-environment settings behavior has its own dedicated audited section (5 environment subsections intact). ✓
  4. `baseline-inventory.md` (111 rows, one per finding) is produced as the F13 seed. ✓
- **No blockers to starting Phase 2 planning.** The 13 remaining OPEN items are forward-looking product/architecture decisions (not legacy-behavior ambiguities — the legacy facts themselves are fully confirmed and cited). Per this plan's own framing and STATE.md's updated Blockers/Concerns note, these OPEN items do not block the *start* of downstream phase planning — they block *finalizing* the specific behavior they concern (e.g., F2's conflict-enforcement policy, F4's Location/Resource deletion-guard policy) before that feature ships, per PRD Section 7's release-time "zero open P0/P1 functional-parity gaps" metric.
- **Phase 1 (legacy-functional-audit) is complete** — all 6 plans (01-01 through 01-06) have matching SUMMARY.md files. Ready for `/pivota_spec-plan-phase 2` (Platform Foundation & Infrastructure) and `/pivota_spec-verify-work 1`.

---
*Phase: 01-legacy-functional-audit*
*Completed: 2026-10-06*

## Self-Check: PASSED

- All 3 created files found on disk: `legacy-audit-findings.md`, `open-questions.md`, `baseline-inventory.md`.
- All 3 task commits found in git history: `cf13d01`, `8c0193b`, `6262cc4`.
- All 3 tasks' own `<verify>` scripts re-run and passed: `TASK1_OK`, `TASK2_OK`, `TASK3_OK`.
- No build/test step applies — this plan produces only documentation artifacts (F0 has no runtime API or schema of its own, per FRD-BookingHub.md's own description).
- Known Stubs scan: none found (see above).
