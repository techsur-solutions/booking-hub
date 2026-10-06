---
phase: 01-legacy-functional-audit
plan: 05
subsystem: audit
tags: [coldfusion, wheels-framework, settings, api-feeds, audit-logging, cf-lifecycle, environments]

# Dependency graph
requires: []
provides:
  - "Cited audit of Settings controller/model (33 settings fields, allowSettings kill-switch, per-row editable flag)"
  - "Cited audit of Api controller — corrected feed-gating verdict (token-gated, not role-gated)"
  - "Cited audit of Logfiles controller — confirmed no before/after value capture"
  - "Full route inventory cross-referencing all 12 controllers against config/routes.cfm"
  - "All 9 CF application lifecycle event handlers documented with citation"
  - "Per-environment settings behavior: 5 dedicated subsections (dev/prod/testing/design/maintenance)"
  - "Resolution of PRD Open Questions #7, #8, #10"
affects: ["F8-Notifications", "F9-Public-Feeds", "F10-Settings", "F11-Activity-Audit-Logging", "Phase-2-environment-substrate"]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Legacy settings are an in-memory cache (application.rbs.setting) rebuilt only at onApplicationStart — UI updates require a reload, confirmed by the controller's own success message"
    - "Legacy API feeds use token-possession auth (query-string token matched against users.apitoken), not role/permission-based auth"
    - "Legacy audit logging is a flat free-text message trail (message + data columns only) with no structured before/after diff, populated by a mix of deliberate addlogline() calls and an incidental global logFlash after-filter"

key-files:
  created:
    - ".planning/phases/01-legacy-functional-audit/findings/05-platform-settings.md"
  modified: []

key-decisions:
  - "PRD Open Question #7 corrected: legacy Api.cfc feeds (rss2/ical/display) are gated by per-user API token possession, not by the allowAPI permission flag (which only gates the index listing page and is granted to every role by default, making it a no-op). Phase 7 (Public Feeds) must design around token-based access, not role-based."
  - "PRD Open Question #8 resolved: logfiles table has no before/after value columns at all — only free-text message/data fields. Write-side coverage is a confirmed subset (Sessions login attempts, PasswordResets completion, Cookie remember-me helpers) plus incidental logging via a global logFlash after-filter on flash error/success messages. Booking approve/deny, user CRUD, and permission changes have no deliberate audit-log call."
  - "PRD Open Question #10 resolved: all 5 per-environment settings files ship empty (zero config-value differences). Exactly one functional code-path difference exists in the whole codebase: production serves a minified JS bundle, every other environment serves unminified. Maintenance mode is the one major functional outlier — a full, unconditional, no-bypass lockout page for all users including admins, triggered by Wheels' framework-level environment dispatch to the fully-static onmaintenance.cfm handler."
  - "Settings model has a data-driven global kill-switch (allowSettings setting) layered on top of the accessSettings permission gate — if an admin sets allowSettings=0 via the UI, the entire Settings controller becomes inaccessible to everyone, recoverable only via direct DB edit."

patterns-established:
  - "Citation discipline for ambiguous/unlocatable write-sites: when a log type (e.g. 'ajax') or a config-implied feature (e.g. errorEmailAddress send-logic) has no confirmed call site within the plan's file set, state the gap explicitly as an Open Question rather than assuming absence or presence."

# Metrics
duration: 25min
completed: 2026-10-06
---

# Phase 01 Plan 05: Platform Settings/Api/Logfiles Audit Summary

**Corrected two PRD assumptions with direct code citation: legacy public feeds are token-gated (not role-gated, not fully public), and the audit log captures zero before/after values — it's a flat free-text message trail with deliberately-logged login/password-reset events plus incidental logging of any flash message via a global after-filter.**

## Performance

- **Duration:** 25 min
- **Tasks:** 2
- **Files created:** 1 (`findings/05-platform-settings.md`, 318 lines)

## Accomplishments

- Documented all 33 legacy Settings fields (category/fieldtype/editable) sourced directly from `install/new-installation.sql`, confirming `approveBooking`, `calendarSlotMinutes`/`calendarMintime`/`calendarMaxtime`, and `calendarFirstday` per the plan's expected fields
- Found and documented a two-layer gate on Settings (permission `accessSettings` + data-driven `allowSettings` kill-switch) plus a third per-row `editable` flag enforced independently at the model/schema level
- Corrected the PRD's assumed Api.cfc format list (RSS2/iCal/JSON/display) — only 3 actions exist (`rss2`/`ical`/`display`); no dedicated JSON action was found
- Delivered an explicit per-feed-action gating verdict for Api.cfc: `index` is permission-gated (`allowAPI`, granted to all roles by default); the 3 actual data-serving actions are token-gated instead, with no re-check of the token owner's permissions at request time
- Confirmed Logfiles schema (`message`, `data` free-text only) has no before/after value capture anywhere, and traced every actual `addlogline()`/`addLogLine()` call site across all 12 controllers plus the global `logFlash` after-filter mechanism
- Built a full route inventory cross-referencing all 12 controllers against the 9 explicit `addRoute()` declarations plus Wheels' implicit convention routing
- Documented all 9 CF lifecycle event handlers, with `onApplicationStart` (full settings/permission cache rebuild), `onRequestStart` (global form/url param trimming), and `onMaintenance` (confirmed full unconditional lockout) as the functionally significant ones
- Delivered the dedicated Per-Environment Settings Behavior section (5 subsections) required by Roadmap Success Criterion #3, confirming all 5 environment files ship empty and only one functional (non-config) difference exists codebase-wide

## Task Commits

Both tasks were committed atomically (interleaved with other concurrently-executing plans' commits, confirmed via `git show <hash>` byte-identical content match against what was written):

1. **Task 1: Audit Settings, Api, Logfiles controllers and Settings model** - `aa7015a` (feat)
2. **Task 2: Audit routes.cfm, CF lifecycle events, and per-environment settings; finalize platform Open Questions** - landed in `9dc31b4` (docs, a concurrently-running sibling plan's commit that included this file's changes alongside its own work — content verified identical to what this plan wrote, both tasks' verification commands pass against the final file on disk)

_Note: this plan ran with `parallelization: true` in config.json, so commits from sibling plans interleaved with this plan's own. Content was verified byte-for-byte via `git show <hash>:<path>` against the working tree after each task, and both tasks' `<verify>` blocks pass against the committed state._

## Files Created/Modified

- `.planning/phases/01-legacy-functional-audit/findings/05-platform-settings.md` - Full cited audit: Settings/Api/Logfiles controllers, Settings model, route inventory, CF lifecycle events, per-environment behavior, Open Questions resolution

## Decisions Made

See `key-decisions` in frontmatter above — all four are corrections to PRD/PROJECT.md interim assumptions, backed by direct code citation rather than inference.

## Deviations from Plan

None - plan executed exactly as written. Both tasks' `<verify>` blocks pass against the final committed file, and the plan's `integration_contracts.provides[0].verify` contract (`grep` for the 7 required `## ` sections plus the 5 `### ` environment subsections) also passes.

## Issues Encountered

**Parallel-execution commit interleaving:** `config.json` has `parallelization: true`, and other plans (01-01, 01-02, 01-03) were committing concurrently during this plan's execution. Task 2's changes to `findings/05-platform-settings.md` ended up bundled into a sibling plan's commit (`9dc31b4`, titled "docs(01-01): complete booking-core audit plan") rather than landing in a commit with this plan's own name in the message. This was verified NOT to be data loss: `git show 9dc31b4:.planning/phases/01-legacy-functional-audit/findings/05-platform-settings.md` is byte-identical to the working-tree file, both task `<verify>` commands pass, and the file's full 7-section/5-subsection structure is intact. No content was lost or overwritten; only the commit message attribution for Task 2's specific diff is shared with a sibling plan's commit rather than being plan-05-exclusive.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Findings are ready for the Wave 2 consolidation plan to fold the Api.cfc token-gating verdict and the Logfiles no-before/after-capture verdict into the master Open Questions resolution
- Phase 7 (Public Feeds) has a confirmed, corrected security model to design against (token possession, not role check)
- Phase 6 (Notifications & Audit Logging) has a confirmed baseline: audit logging will need to be rebuilt from near-scratch as a deliberate, comprehensive mechanism — the legacy system's coverage is sparse and largely incidental
- Phase 4 (Settings) has the full 33-field inventory plus the `allowSettings` kill-switch and in-memory-cache-requires-reload behavior to replicate or deliberately improve upon
- Phase 2 (environment/config substrate) can treat per-environment differences as minimal in legacy — no parity requirement exists for anything beyond the production-minification and maintenance-lockout behaviors
- No blockers

## Known Stubs

None found. The two `grep` hits for "stub"/"placeholder" in `findings/05-platform-settings.md` are descriptive citations of legacy code's own characteristics (an empty `onApplicationEnd` handler in the legacy app, and a documented placeholder config value `UA-` for unconfigured Google Analytics) — not incomplete work in this plan's own deliverable. This plan produces a markdown findings document only (per its own `<context>`); no application code, stub handlers, or hardcoded responses were authored.

## Self-Check: PASSED

- `findings/05-platform-settings.md` exists on disk, 318 lines, all 7 required `## ` sections present (`Settings controller and model`, `Api controller (public feeds)`, `Logfiles controller`, `Route Inventory (config/routes.cfm)`, `CF Application Lifecycle Events`, `Per-Environment Settings Behavior`, `Open Questions (platform-settings)`), matching the plan's `integration_contracts.provides[0].exports` list exactly
- Per-Environment section has exactly 5 `### ` subsections (development/production/testing/design/maintenance) — confirmed via `grep -cE` returning `5`
- Task 1 commit `aa7015a` found in git history (confirmed via `git log --oneline --all | grep`)
- Task 2 content confirmed landed byte-identical in commit `9dc31b4` (verified via `diff <(git show 9dc31b4:<path>) <(cat <path>)` → IDENTICAL) — see "Issues Encountered" above for the parallel-execution commit-interleaving explanation
- Both tasks' own `<verify>` bash blocks executed against the final file and returned `TASK1_OK` / `TASK2_OK`
- Plan frontmatter's `integration_contracts.provides[0].verify` command executed and returned `CONTRACT_OK`
- No build/typecheck/lint applicable — this is a docs-only audit plan per its own `<context>` ("produces a markdown findings document only — no application code")
- `01-05-SUMMARY.md` created at `.planning/phases/01-legacy-functional-audit/01-05-SUMMARY.md`
- `.planning/STATE.md` updated: plan counter advanced, 3 decisions appended, session continuity recorded

---
*Phase: 01-legacy-functional-audit*
*Completed: 2026-10-06*
