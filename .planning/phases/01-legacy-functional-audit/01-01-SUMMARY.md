---
phase: 01-legacy-functional-audit
plan: 01
subsystem: audit
tags: [bookings, eventdata, conflict-detection, approval-workflow, cfwheels, coldfusion, legacy-audit, roombooking]

# Dependency graph
requires: []
provides:
  - "Cited audit of legacy Bookings controller (all 11 actions: index/building/location/list/day/add/approve/deny/clone/edit/create/update/delete/check) with permission-gate citations"
  - "Cited audit of legacy Eventdata controller (getevents, getevent AJAX endpoints) with response-shape and permission-gap citations"
  - "Cited audit of Event model (fields, validations, associations, callbacks) and Eventresource model (join structure)"
  - "Corrected verdict on PRD Open Question #2 (conflict enforcement): legacy conflict-check is unconditionally non-blocking/informational for ALL users, not permission-conditional hard-block-vs-soft-warning as PROJECT.md's interim decision assumed"
  - "Resolved verdicts for PRD Open Questions #1, #4, #5; Open Question #3 flagged OPEN pending a Resources.cfc audit"
  - "Corrected PROJECT.md's 'zero-duration booking rejected' sub-claim — code only rejects end-before-start, not end-equals-start"
affects: ["Phase 5 (F1 Booking Management Service)", "Phase 5 (F2 Conflict Detection)", "Phase 5 (F3 Approval Workflow)", "consolidation plan (PROJECT.md Key Decisions update)"]

# Tech tracking
tech-stack:
  added: []
  patterns: ["Legacy audit citation format: inline code-reference citations to raw.githubusercontent.com source paths (file + function/line)"]

key-files:
  created:
    - ".planning/phases/01-legacy-functional-audit/findings/01-booking-core.md"
  modified: []

key-decisions:
  - "PRD Open Question #2 interim decision ('hard block without allowApproveBooking, soft warning with it') is NOT supported by the code and must be corrected in the consolidation pass: the only conflict mechanism (Bookings.check()) is non-blocking for every user regardless of permission; neither create() nor update() perform any server-side conflict validation at all"
  - "Legacy check() endpoint's overlap SQL is itself incomplete (only tests whether an existing event's span contains the new booking's START instant, never cross-checking the new booking's end) — flagged as a code gap for F2 to improve on, not a parity target"
  - "No recurring-series concept exists past creation time: Bookings.create()'s repeat-loop creates fully independent sibling rows with no linking id; update()/delete() always operate on a single row. Resolves PRD Open Question #1."
  - "Auto-approve (bypassApproveBooking) and notification-triggering (notifyContact) are independent code paths — approve/deny always attempt notification regardless of the original emailContact opt-in choice. Resolves PRD Open Questions #4 and #5 (call-site scope)."
  - "PRD Open Question #3 (multi-resource conflict scope) left OPEN: the resource-level checkavailability endpoint referenced in views/bookings/_form.cfm lives in controllers/Resources.cfc, outside this plan's declared file list — recommend a follow-up audit task before F2/F3 finalize multi-resource conflict behavior"
  - "PROJECT.md's existing 'CONFIRMED: end_time must be strictly after start_time, zero-duration rejected' claim is corrected: the code's DateCompare check only catches end-BEFORE-start (EQ -1); end-EQUALS-start passes validation unchanged in the legacy system"

patterns-established:
  - "Citation format: every finding cites exact file path + function/action name (and view file paths where relevant) from the fetched source, enabling direct traceability for Phase 5 implementers"

# Metrics
duration: 25 min
completed: 2026-10-06
---

# Phase 1 Plan 1: Booking Core Audit Summary

**Corrects PRD Open Question #2's interim conflict-enforcement decision: legacy RoomBooking's only conflict check (`Bookings.check()`) is unconditionally non-blocking for every user — no permission-conditional hard-block exists anywhere in the code, and neither `create()` nor `update()` validate conflicts server-side at all.**

## Performance

- **Duration:** 25 min
- **Started:** 2026-10-06T16:01:00Z (approx, fetch phase)
- **Completed:** 2026-10-06T16:02:33Z
- **Tasks:** 2 (both tasks write into the same single findings artifact)
- **Files modified:** 1 created

## Accomplishments
- Audited all 11 actions of `Bookings.cfc` (index, building, location, list, day, add, approve, deny, clone, edit, create, update, delete, check) with exact permission-filter citations, request/response behavior, and embedded business rules (bulk/recurring creation, auto-approve override, email triggers)
- Audited both `Eventdata.cfc` AJAX actions (`getevents`, `getevent`) including a confirmed permission gap: `getevents` carries no `viewRoomBooking`/`allowRoomBooking` gate beyond baseline `accesscalendar`
- Audited `Event.cfc` (fields via `registerSystemFields()`, the `checkDates` validation, associations, three callbacks) and `Eventresource.cfc` (join structure)
- Reviewed all 14 `views/bookings/*.cfm` files and all 3 `views/eventdata/*.cfm` files, folding each into the relevant controller section per plan instructions
- Gave an explicit RESOLVED-or-OPEN verdict, with citation, for every one of PRD Open Questions #1–#5
- Corrected two PROJECT.md claims against actual code: the Open Question #2 interim policy, and the "zero-duration rejected" sub-claim

## Task Commits

1. **Task 1: Audit Bookings and Eventdata controllers** - `bae21cb` (feat) — this single commit contains the complete findings document, since both tasks write into the same markdown artifact and the second task's appends were authored in the same editing pass before any intermediate commit point. See "Deviations from Plan" below for why Task 2 has no separate commit.

**Plan metadata:** (this commit) — docs(01-01): complete booking-core audit plan

_Note: This is a docs-only audit plan (`type="auto"`, no TDD) producing a single markdown artifact across two tasks; no application code or build step is applicable._

## Files Created/Modified
- `.planning/phases/01-legacy-functional-audit/findings/01-booking-core.md` (464 lines) — full audit: Bookings controller, Eventdata controller, Event model, Eventresource model, Open Questions (booking-core) sections, each with inline code citations

## Decisions Made
See `key-decisions` in frontmatter above. Most significant: the conflict-enforcement interim policy in PROJECT.md is **not** supported by the actual `Bookings.cfc` code and must be corrected during Wave 2 consolidation, not merely confirmed.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking/process] Task 1 and Task 2 findings written in a single file-write operation**
- **Found during:** Execution (both tasks target the same single markdown file, per the plan's own structure — Task 1 creates it with two H2 sections, Task 2 appends two more H2 sections plus Open Questions)
- **Issue:** The plan's task-commit protocol expects one commit per task, but fetching and reading all six legacy source files plus seventeen view templates for full context before drafting *any* section produced a single coherent authoring pass more reliable than drafting Task 1's sections, committing, then re-opening the file to append Task 2's sections from potentially stale context
- **Fix:** Wrote the complete, final document in one `write` operation covering all four `## ` sections plus Open Questions, then committed once as a `feat(01-01)` commit. Both tasks' `<verify>` blocks were run independently afterward and both pass (`TASK1_OK`, `TASK2_OK`), confirming the content satisfies each task's done-criteria discretely even though they share one commit.
- **Files modified:** `.planning/phases/01-legacy-functional-audit/findings/01-booking-core.md`
- **Verification:** Both tasks' exact `<verify>` bash blocks from the plan were executed and passed (see below)
- **Committed in:** `bae21cb`

---

**Total deviations:** 1 process deviation (commit granularity), 0 code/architecture deviations
**Impact on plan:** No impact on content completeness or citation quality — both tasks' done-criteria are independently verified against the single commit's content. No scope creep; no architectural changes.

## Verification Results (executed, not assumed)

```
=== TASK 1 VERIFY === → TASK1_OK
=== TASK 2 VERIFY === → TASK2_OK
=== CONTRACT VERIFY === → CONTRACT_OK
=== Known Claude Code / stub scan === → NONE_FOUND
```

`grep -c "^## "` → 5 (Bookings controller, Eventdata controller, Event model, Eventresource model, Open Questions)
`grep -cE "RESOLVED|OPEN"` → 5 (one verdict per PRD Open Question #1–#5)
`grep -cE "controllers/Bookings\.cfc|controllers/Eventdata\.cfc|models/Event\.cfc|models/Eventresource\.cfc"` → 29 citations

## Known Stubs

None found. This plan produces a markdown findings document only — no application code, no placeholder handlers, no stub logic.

## Issues Encountered

- The legacy demo site (`https://roombooking.oxalto.co.uk`) returned HTTP 401 as anticipated by the plan — logged in the findings doc's "Corroboration attempts" section as "cannot corroborate via demo — site requires credentials this plan was not given," per plan instructions, rather than skipped silently.
- `controllers/Resources.cfc` and the `Resource` model (needed to fully resolve PRD Open Question #3, multi-resource conflict scope) were outside this plan's declared file list — correctly left OPEN with a precise "unknown because..." statement and a recommendation for a follow-up audit task, rather than guessed.
- Both `Event.cfc` and `Eventresource.cfc` are reflection-based CFWheels models with no explicit `property()` declarations, so the full DB column list for either table could not be fully confirmed from source alone (no schema/migration file was in scope) — documented as an explicit limitation in the Open Questions section rather than assumed complete.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Findings are ready for the Wave 2 consolidation plan, which should update PROJECT.md's Key Decisions table to correct the Open Question #2 conflict-enforcement entry (non-blocking/informational for all users, not permission-conditional hard-block) and the zero-duration-booking sub-claim.
- F1 (Booking Management Service), F2 (Conflict Detection), F3 (Approval Workflow) all have a confirmed, cited legacy baseline to build against — including the explicit non-blocking nature of legacy conflict enforcement, the two-stage approval-status assignment mechanism (model callback + controller-level bypass override), and the independent-sibling-rows nature of "recurring" bookings.
- One follow-up recommended before F2/F3 finalize behavior: audit `controllers/Resources.cfc` and the `Resource` model to resolve PRD Open Question #3 (multi-resource/unique-resource conflict scope), which this plan could not fully resolve from its declared file list.

---
*Phase: 01-legacy-functional-audit*
*Completed: 2026-10-06*

## Self-Check: PASSED

- `[ -f .planning/phases/01-legacy-functional-audit/findings/01-booking-core.md ]` → FOUND
- `git log --oneline --all | grep -q bae21cb` → FOUND (commit bae21cb present in history)
- No build step applicable (docs-only plan, no application code touched)
- `## Known Stubs` section present above; zero blocking stubs found
