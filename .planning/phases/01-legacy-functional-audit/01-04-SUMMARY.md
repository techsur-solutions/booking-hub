---
phase: 01-legacy-functional-audit
plan: 04
subsystem: identity-access
tags: [audit, identity, permissions, sha-512, password-hashing, rbac, coldfusion-wheels]

# Dependency graph
requires: []
provides:
  - "Cited audit of Users, Sessions, PasswordResets, Permissions controllers and the User model"
  - "Complete 12-controller permission-flag inventory (17 flags total, 15 active, 2 unused)"
  - "Resolution of 4 FRD-flagged identity/access open questions (initial-credential, remember-me duration, password complexity, permission-matrix completeness)"
  - "5 newly-discovered open decision points for Phase 3 (permission-cache-reload, reset-token re-check gap, email-enumeration inconsistency, email-collation, admin-assume-admin enforcement)"
affects: ["03-identity-access-control (Phase 3)", "06-user-role-management (F6)", "07-permission-system (F7)"]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Legacy permission check: filters(through='checkPermissionAndRedirect', permission='flagName') per-action/per-controller, backed by an in-memory struct populated once at onApplicationStart (requires app reload for changes to take effect)"
    - "Legacy password hashing: SHA-512(plaintext + raw-UUID-salt), with the salt itself stored CFMX_COMPAT-encrypted under a shared per-installation authKey"

key-files:
  created:
    - ".planning/phases/01-legacy-functional-audit/findings/04-identity-access.md"
  modified: []

key-decisions:
  - "Initial-credential mechanism confirmed: admin sets the password directly at account-creation time (typed twice, no auto-generation, no email invite link)"
  - "Remember-me cookie confirmed as a 360-day login-prefill convenience (email only), not a session-duration extension; underlying session-timeout itself depends on an unconfirmed ColdFusion/Lucee engine default not present in this codebase"
  - "Password complexity baseline confirmed: >=6 chars, >=1 digit, >=1 lowercase letter, no uppercase/symbol requirement — new system's policy must be no weaker"
  - "PRD Open Question #9 resolved as NOT exhaustive: 17 total permission flags exist, 15 are actively enforced (9 more beyond the 6 named in PRD), 2 are defined-but-dead (allowiCal, allowRSS)"

patterns-established:
  - "Findings docs cite exact controller:line and regex/constant values verbatim rather than paraphrasing, so Phase 3 can treat this document as a drop-in legacy baseline without re-reading source"

# Metrics
duration: 35 min
completed: 2026-10-06
---

# Phase 1 Plan 04: Identity & Access Audit Summary

**Complete cited audit of Users/Sessions/PasswordResets/Permissions controllers + User model, plus a full 12-controller permission-flag scan resolving PRD Open Question #9 as 17 total flags (15 active, 2 unused) rather than the 6 originally named.**

## Performance

- **Duration:** 35 min
- **Started:** 2026-10-06T15:58:00Z
- **Completed:** 2026-10-06T16:05:17Z
- **Tasks:** 2
- **Files modified:** 1 (findings doc, built incrementally across 2 commits)

## Accomplishments
- Fetched and cited all identity-access controllers (`Users.cfc`, `Sessions.cfc`, `PasswordResets.cfc`, `Permissions.cfc`), the `User.cfc` model, the shared `Controller.cfc` base, `events/functions.cfm` (global auth helpers), and all 18 relevant view templates under `views/users/`, `views/sessions/`, `views/passwordresets/`, `views/permissions/`
- Resolved all 4 FRD-flagged identity/access open questions with exact code citations (initial-credential mechanism, remember-me duration, password complexity, permission-matrix completeness)
- Scanned all 12 legacy controllers for permission-gate patterns and cross-referenced against the full 17-row `permissions` table seed data, producing a complete flag-by-flag inventory with per-action gating scope
- Documented 5 explicit unguarded/partially-unguarded action findings (Sessions, PasswordResets, Api feed actions, Bookings calendar views, Locations list/view) per F7.4's deny-by-default requirement
- Surfaced 5 additional open decision points not explicitly flagged in the FRD but material to Phase 3 design (permission-cache-reload requirement, reset-token expiry re-check gap, login vs. reset-request email-enumeration inconsistency, unconfirmed email-collation, UI-only admin-assume-admin restriction)

## Task Commits

Each task was committed atomically:

1. **Task 1: Audit Users, Sessions, PasswordResets, Permissions controllers and User model** - `c65b412` (docs)
2. **Task 2: Build cross-controller permission-flag inventory; finalize identity-access Open Questions** - `8229ed2` (docs)

**Plan metadata:** (this commit) `docs(01-04): complete identity-access audit plan`

## Files Created/Modified
- `.planning/phases/01-legacy-functional-audit/findings/04-identity-access.md` - Cited audit findings: 5 controller/model sections + permission flag inventory table (17 rows) + Open Questions section (9 items)

## Decisions Made
See `key-decisions` in frontmatter above. All four FRD-flagged open questions were resolved directly from source code citations — no assumptions were required.

## Deviations from Plan

None - plan executed exactly as written. Both tasks' verification scripts passed without requiring any auto-fixes. The plan's own content scope (fetch Controller.cfc as the base-filter source) was followed as specified; the additional fetches of `events/functions.cfm`, `events/onapplicationstart.cfm`, `config/routes.cfm`, and `install/functions.cfm`/`install/new-installation.sql` were made because the plan's context explicitly asked this plan to resolve exact mechanism details (remember-me duration, initial-credential mechanism, permission-matrix storage shape) that only exist in those supporting files — reading them was necessary groundwork for citing the controllers accurately, not scope creep beyond the plan's stated objective.

## Issues Encountered

None. All required source files were publicly fetchable via `raw.githubusercontent.com`; no auth gates, no missing files, no ambiguous code paths requiring judgment calls beyond what's captured in the Open Questions section.

## User Setup Required

None - no external service configuration required. This plan produces a markdown findings document only.

## Next Phase Readiness

- Findings document provides a complete, citation-backed legacy baseline for Phase 3 (Identity & Access Control), F6 (User & Role Management), and F7 (Permission System)
- The 17-flag permission inventory (vs. the 6 originally named in PRD) is ready to seed Phase 3's deny-by-default Keycloak role/permission mapping
- 9 Open Questions are logged for Phase 3 to explicitly resolve or consciously carry forward — none are blocking for continuing Phase 1's remaining audit plans (01-05, 01-06), since those cover independent controller groups
- No blockers for the Wave 2 consolidation plan that will aggregate this plan's findings with 01-01 through 01-03/01-05/01-06

---
*Phase: 01-legacy-functional-audit*
*Completed: 2026-10-06*

## Self-Check: PASSED

- `.planning/phases/01-legacy-functional-audit/findings/04-identity-access.md` exists: FOUND (190 lines, exceeds 100-line minimum)
- Commit `c65b412` (Task 1): FOUND in `git log`
- Commit `8229ed2` (Task 2): FOUND in `git log`
- All 7 required `## ` sections present (verified via grep, see Task Commits verification above)
- Contract verify command (`grep -q '^## Permission Flag Inventory' ... && echo CONTRACT_OK`): PASSED
- No build/test applicable — this is a documentation-only plan producing a markdown findings artifact; no application code was created or modified
- Known Stubs: None found (grep for TODO/FIXME/placeholder/not-implemented returned no matches)
