---
phase: 01-legacy-functional-audit
plan: 02
subsystem: audit
tags: [locations, resources, cfwheels, coldfusion, legacy-audit, roombooking, soft-delete, reference-data]

# Dependency graph
requires: []
provides:
  - "Cited audit of legacy Locations/Resources controllers (every action, permission gate, route, request/response shape)"
  - "Cited audit of Location/Resource models (full field set, validations, associations)"
  - "Confirmed Location field set: name, class, colour, description, building, layouts (6 fields, not the FRD-assumed subset)"
  - "Confirmed Resource field set: name, type, description, isunique, restrictlocations (5 fields, not just 'name')"
  - "Confirmed name uniqueness is enforced nowhere (model or DB) for Location or Resource"
  - "Confirmed Location/Resource deletes are TRUE HARD DELETES (no deletedAt column on either table), contradicting FRD's soft-delete assumption"
  - "Confirmed deletion-in-use policy is ORPHAN-ALLOW: no booking-reference guard, no cascade, no DB foreign key constraints anywhere in schema"
affects: ["Phase 4 (F4 Locations & Resources Management build)", "Phase 4 (F10 Settings, shares application.rbs config pattern)", "Bookings controller audit (checkavailability permission gate cross-reference)"]

# Tech tracking
tech-stack:
  added: []
  patterns: ["Legacy audit citation format: inline code-reference citations to raw.githubusercontent.com source paths", "Wheels soft-delete detection: per-model, gated on presence of a deletedAt-named DB column, not a framework-wide default"]

key-files:
  created:
    - ".planning/phases/01-legacy-functional-audit/findings/02-reference-data.md"
  modified: []

key-decisions:
  - "FRD's layout metadata assumption corrected: the real field is 'layouts' (plural), a free-text comma-separated string (e.g. 'boardroom,lecture'), not a structured/enum type — and it is programmatically disconnected from Event.layoutstyle, which is driven by a separate global settings list"
  - "FRD's Resource field-set assumption ('just name') corrected: Resource actually has 5 fields (name, type, description, isunique, restrictlocations), none of which are declared in the model itself — they exist only in the DB schema and the view form, since models/Resource.cfc is a 12-line file with zero field/validation declarations"
  - "FRD's deletion-in-use interim assumption (soft-delete, references remain intact) is INCORRECT for Locations/Resources: both are hard-deleted (no deletedAt column on either table, confirmed via Wheels' own soft-delete gating logic in wheels/model/initialization.cfm) with no booking-reference guard and no DB foreign keys anywhere in the schema — true orphan-allow-via-hard-delete, a materially riskier legacy behavior than assumed. Flagged for explicit product/architecture decision in F4 planning: preserve orphan-allow for legacy equivalence, or introduce a blocking/cascade guard as a deliberate improvement."
  - "Logged a 5th (non-FRD-flagged) open question: Resources' checkavailability Ajax endpoint is gated behind the admin-only accessresources permission with no except= carve-out, which would make it unreachable for ordinary booking users if it's meant to support live availability checks in the general booking UI — needs cross-reference with the Bookings controller audit to confirm whether this is a legacy bug or an unused feature path"

patterns-established:
  - "When a legacy Wheels model declares zero fields/validations itself (as with Resource.cfc), source the real field set from the SQL install schema cross-checked against the actual rendered view form — the model file alone understates the entity"
  - "Wheels soft- vs hard-delete must be verified per-table against the SQL schema's deletedAt/deletedat column presence, never assumed as a framework-wide default — confirmed by reading wheels/model/initialization.cfm's conditional soft-deletion setup and wheels/events/onapplicationstart.cfm's softDeleteProperty default"

# Metrics
duration: 18min
completed: 2026-10-06
---

# Phase 1 Plan 2: Reference Data Audit (Locations, Resources) Summary

**Confirmed Location has 6 fields (not FRD's assumed subset) and Resource has 5 (not just `name`), and proved via Wheels' own soft-delete gating code that both entities are hard-deleted with zero booking-reference guard or DB foreign keys — the opposite of FRD's soft-delete assumption.**

## Performance

- **Duration:** ~18 min
- **Started:** 2026-10-06T15:56:00Z (approx, first fetch)
- **Completed:** 2026-10-06T16:04:12Z
- **Tasks:** 2
- **Files modified:** 1 (created)

## Accomplishments
- Fetched and cited `controllers/Locations.cfc`, `controllers/Resources.cfc`, `models/Location.cfc`, `models/Resource.cfc`, `config/routes.cfm`, all 10 view templates under `views/locations/` and `views/resources/`, plus supporting framework evidence (`install/new-installation.sql` schema, `wheels/model/initialization.cfm`, `wheels/model/associations.cfm`, `wheels/events/onapplicationstart.cfm`) to independently prove the hard-delete/orphan-allow finding at three converging levels (controller, model/association, framework/schema).
- Documented every action in both controllers with permission gate, route, request shape, and response/redirect behavior, including a flagged dead/vestigial `view` filter reference in Resources.cfc (no `view` action actually exists) and an admin-only permission gate on the `checkavailability` Ajax endpoint that looks like a legacy bug.
- Resolved all 4 FRD-flagged reference-data open questions with full code-cited verdicts, and logged one additional (5th) open question not originally flagged by FRD.

## Task Commits

Each task was committed atomically:

1. **Task 1: Audit Locations and Resources controllers** - `ac84939` (feat)
2. **Task 2: Audit Location and Resource models; finalize reference-data Open Questions** - `304f150` (docs)

_Note: This is a documentation-only plan (markdown findings, no application code) — commit types reflect that the controller/view audit (Task 1) produced the primary cited findings (`feat`), while Task 2 appended model/validation documentation and closed out the Open Questions section (`docs`)._

## Files Created/Modified
- `.planning/phases/01-legacy-functional-audit/findings/02-reference-data.md` - Cited audit of Locations/Resources controllers, Location/Resource models, and 5 resolved/open verdicts on reference-data ambiguities (168 lines)

## Decisions Made
See `key-decisions` in frontmatter above — four FRD-assumption corrections plus one newly-logged open question (Resources' `checkavailability` permission gate).

## Deviations from Plan

None - plan executed exactly as written. Both tasks' verify commands passed on first attempt; no auto-fixes, no blocking issues, no architectural decisions required. The plan is a documentation-only audit with no application code, build, or test runner to invoke — the task-level `<verify>` grep checks (confirmed passing above) constitute this plan's full verification surface.

## Issues Encountered
None.

## User Setup Required
None - no external service configuration required. This plan only reads public GitHub-hosted source files and writes a local markdown findings document.

## Next Phase Readiness
- Phase 4 (F4 Locations & Resources Management) now has a confirmed legacy baseline for Location/Resource CRUD, field sets, and the deletion-in-use policy, replacing FRD's interim assumptions.
- The deletion-in-use policy finding (orphan-allow-via-hard-delete) is a product/architecture decision point that Phase 4 planning must explicitly resolve (preserve legacy behavior vs. introduce a guard) rather than silently inherit either way.
- The newly-logged open question about `checkavailability`'s admin-only permission gate should be cross-referenced against the Bookings controller audit (already completed per `01-01-SUMMARY.md`/commit `bae21cb`) to confirm whether it's a legacy bug affecting booking-conflict UX for non-admin users.
- Ready for remaining Wave 1 plans / Wave 2 consolidation to proceed.

## Self-Check: PASSED

- FOUND: `.planning/phases/01-legacy-functional-audit/findings/02-reference-data.md` (168 lines, verified via `test -f`)
- FOUND: commit `ac84939` (`git log --oneline --all | grep ac84939`)
- FOUND: commit `304f150` (`git log --oneline --all | grep 304f150`)
- Build check: N/A — documentation-only plan, no build/test runner applies. Task-level `<verify>` grep commands (TASK1_OK, TASK2_OK) and the plan-level `integration_contracts.provides[0].verify` contract check (CONTRACT_OK) all ran and passed, as shown above.
- `## Known Stubs`: None found. Two `grep` hits for the word "placeholder" were inspected and are false positives — they refer to the legacy HTML form field's `placeholder` attribute (UI hint text), not an incomplete implementation in this audit document.

---
*Phase: 01-legacy-functional-audit*
*Completed: 2026-10-06*
