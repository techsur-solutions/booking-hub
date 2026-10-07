---
phase: 01-legacy-functional-audit
plan: 03
subsystem: audit
tags: [custom-fields, cfwheels, coldfusion, legacy-audit, roombooking]

# Dependency graph
requires: []
provides:
  - "Cited audit of legacy Customfields controller (all 11 actions, permission gates, routes)"
  - "Cited audit of Customfield/Customfieldjoin/Customfieldvalue models (field set, validations, associations)"
  - "Confirmed field_type enum: textfield, select, textarea, radio, checkbox (5 values, cross-verified controller+view)"
  - "Confirmed join/context model: two-tier — parentmodel scopes definition to type (event|location), Customfieldjoin.customfieldchildid scopes value to instance"
  - "Confirmed required/type validation is NOT enforced server-side (HTML5 attribute only)"
  - "Confirmed retention policy: deleting a Customfield orphans Customfieldjoin/Customfieldvalue rows, no cascade/purge"
affects: ["Phase 4 (F5 Custom Fields build)", "Phase 4 (F10 Settings, shares application.rbs config pattern)"]

# Tech tracking
tech-stack:
  added: []
  patterns: ["Legacy audit citation format: inline code-reference citations to raw.githubusercontent.com source paths"]

key-files:
  created:
    - ".planning/phases/01-legacy-functional-audit/findings/03-custom-fields.md"
  modified: []

key-decisions:
  - "FRD's interim placeholder ('all custom fields are optional, free-text-validated only') confirmed accurate for optional, but corrected: there is NO validation at all server-side (not even free-text-only validation) — any string is accepted unconditionally by Controller.updateCustomFields()"
  - "Join/context model is NOT per-Location (as FRD's open framing suggested) — it is per-model-TYPE (event or location, literally only these two), with per-instance value scoping handled separately via Customfieldjoin.customfieldchildid. No narrower per-Location-instance field definition exists in the legacy system."
  - "Template changes (views/customfields/_templateform.cfm content) require an application restart to take effect, because application.rbs.templates is populated once at onApplicationStart and never invalidated — a behavioral quirk worth preserving or explicitly deciding to drop in the rebuild"

patterns-established:
  - "Citation format: every finding cites exact file path + line numbers from the fetched source, enabling line-level traceability for Phase 4 implementers"

# Metrics
duration: 4min
completed: 2026-10-06
---

# Phase 1 Plan 03: Custom Fields Audit Summary

**Confirmed the legacy custom-fields system has zero server-side validation (type, required, or options-shape) and a two-tier field-definition/value-instance join model scoped to exactly two model types (event, location) — directly overriding FRD's vaguer "optional, free-text-validated" placeholder and resolving the unconfirmed join/context model via Controller.cfc's getCustomFields/updateCustomFields SQL.**

## Performance

- **Duration:** 4 min
- **Started:** 2026-10-06T15:56:22Z
- **Completed:** 2026-10-06T16:00:33Z
- **Tasks:** 2 completed
- **Files modified:** 1 (findings doc, built across 2 atomic commits)

## Accomplishments
- Audited all 11 actions of `controllers/Customfields.cfc` (index, add/create/edit/update/delete, addtemplate/createtemplate/edittemplate/updatetemplate/deletetemplate, fieldpicker) with permission gates, routes (Wheels convention routing — no explicit `routes.cfm` entries), and request/response shapes
- Traced the actual field-type/options/context logic to the inherited base `controllers/Controller.cfc` (`getCustomFields`, `getBlankCustomFields`, `updateCustomFields`) since the Customfields controller itself delegates all of this
- Reviewed all 8 `views/customfields/*.cfm` files plus 3 supporting views (`views/common/form/_customfields.cfm`, `views/shortcodes/field.cfm`, `views/shortcodes/output.cfm`) discovered via the live repo tree — these shortcode views turned out to be the actual field-type `cfswitch` and the actual value-save consumer, providing the strongest evidence for the open questions
- Audited `models/Customfield.cfc`, `models/Customfieldjoin.cfc`, `models/Customfieldvalue.cfc` — confirmed all three are genuinely thin (10-12 lines each), zero validations declared anywhere
- Pulled `install/new-installation.sql` to confirm the DB schema independently of the ORM models — confirmed no DB-level CHECK/ENUM/FK constraints exist, corroborating the "UI-only enum, no server validation" findings at the schema level
- Resolved all 5 FRD-flagged custom-field open questions with explicit RESOLVED verdicts and citations (field_type enum, options[] enforcement, join/context model, required/type validation enforcement, retention policy)

## Task Commits

Each task was committed atomically:

1. **Task 1: Audit Customfields controller** - `c58c425` (docs)
2. **Task 2: Audit Customfield/Customfieldjoin/Customfieldvalue models; finalize Open Questions** - `ad0fc69` (docs)

_Note: documentation-only plan — no application code, no TDD cycle applicable._

## Files Created/Modified
- `.planning/phases/01-legacy-functional-audit/findings/03-custom-fields.md` - Cited audit findings for Customfields controller, 3 Customfield-family models, and 5 resolved FRD open questions (169 lines, 5 `##` sections)

## Decisions Made
- FRD's "optional, free-text-validated" interim placeholder is corrected to "optional, NOT validated at all" (server-side) — this changes the baseline Phase 4 should build against: a faithful behavioral port requires explicitly choosing to EITHER replicate the zero-validation behavior OR document this as an intentional improvement over the legacy system (a Rule-4-style decision belongs in Phase 4's planning, not this audit)
- The join/context model's real shape (type-level definitions + instance-level values, exactly 2 types) is narrower and simpler than FRD's open framing suggested ("a specific Location, or all bookings") — Phase 4 should design around "per-type, not per-instance" field definitions unless a product decision explicitly wants to expand this

## Deviations from Plan

None — plan executed exactly as written. Both tasks' verify commands passed on first attempt; no bugs, missing dependencies, or blocking issues encountered. The task's suggested approach (fetch controller first, then models) surfaced additional directly-relevant files (base `Controller.cfc`, `onapplicationstart.cfm`, `install/new-installation.sql`, `views/shortcodes/{field,output}.cfm`) that were not explicitly listed in the plan's file list but were necessary to give every must-have truth and Open Question a cited, non-speculative verdict — this is squarely within the plan's own instruction to "document it where it actually is" when controller logic (not model logic) turned out to hold the real behavior.

## Known Stubs

None found. This is a documentation-only plan producing a markdown findings file — no application code was created or modified, so no stub/placeholder/TODO scan applies beyond the markdown content itself (scanned, no false positives beyond a benign substring match on "implemented" inside analytical prose).

## Issues Encountered

None.

## User Setup Required

None - no external service configuration required. This plan only performed read-only `curl` fetches against public `raw.githubusercontent.com` / `api.github.com` URLs (no auth required) and wrote local markdown.

## Next Phase Readiness

- `.planning/phases/01-legacy-functional-audit/findings/03-custom-fields.md` is complete and ready to be consumed by the Wave 2 consolidation plan and by Phase 4's F5 (Custom Fields) planning work.
- All 5 FRD-flagged custom-field open questions (field_type enum, options enforcement, join/context model, required/type validation enforcement, retention policy) are RESOLVED with citations — none remain open for this subsystem.
- No blockers for proceeding to the next Wave 1 audit plan (01-04).

## Self-Check: PASSED

- `FOUND: .planning/phases/01-legacy-functional-audit/findings/03-custom-fields.md` (169 lines, verified with `test -f` and `wc -l`)
- `FOUND: c58c425` (Task 1 commit, verified via `git log --oneline`)
- `FOUND: ad0fc69` (Task 2 commit, verified via `git log --oneline`)
- Plan-level verification: `grep -c "^## "` → 5 sections (matches contract's required 5-9 range); `grep -cE "RESOLVED|OPEN"` → 5 (matches required 5-9 range for the 5 FRD-flagged ambiguities)
- No build/test/lint applicable — documentation-only plan, no application code touched
- `## Known Stubs` section present above, no blocking stubs found

---
*Phase: 01-legacy-functional-audit*
*Completed: 2026-10-06*
