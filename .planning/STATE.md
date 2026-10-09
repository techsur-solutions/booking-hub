---
pivota_spec_state_version: 1.0
milestone: v1.0
milestone_name: milestone
current_plan: 6
status: unknown
stopped_at: Completed 04-05-PLAN.md
last_updated: "2026-10-09T00:03:32.663Z"
progress:
  total_phases: 8
  completed_phases: 3
  total_plans: 41
  completed_plans: 26
  percent: 13
---

# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-10-06)

**Core value:** Every existing feature, business rule, validation, and workflow in the legacy RoomBooking system must exist and behave equivalently in the new system — verified by tests, not assumed.
**Current focus:** Phase 01 — legacy-functional-audit

## Current Position

Phase: 01 (legacy-functional-audit) — COMPLETE
Plan: 6 of 6 (complete) — phase done, ready for verification
Current Plan: 6
Total Plans in Phase: 6

Progress: [█░░░░░░░░░] 13%

## Performance Metrics

**Velocity:**

- Total plans completed: 0
- Average duration: N/A
- Total execution time: 0 hours

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| Phase 01 P03 | 4min | 2 tasks | 1 files |

**Recent Trend:**

- Last 5 plans: 01-03 (4min)
- Trend: N/A (first recorded plan)

*Updated after each plan completion*
| Phase 01-legacy-functional-audit P01 | 25 min | 2 tasks | 1 files |
| Phase 01-legacy-functional-audit P02 | 18min | 2 tasks | 1 files |
| Phase 01-legacy-functional-audit P04 | 35 min | 2 tasks | 1 files |
| Phase 01-legacy-functional-audit P05 | 25 min | 2 tasks | 1 files |
| Phase 01-legacy-functional-audit P06 | 33 min | 3 tasks | 5 files |
| Phase 02-platform-foundation-infrastructure P01 | 4 min | 3 tasks | 11 files |
| Phase 02-platform-foundation-infrastructure P09 | 3 min | 2 tasks | 5 files |
| Phase 02-platform-foundation-infrastructure P07 | 4 min | 3 tasks | 11 files |
| Phase 02-platform-foundation-infrastructure P03 | 5 min | 3 tasks | 12 files |
| Phase 02-platform-foundation-infrastructure P06 | 5 min | 3 tasks | 11 files |
| Phase 02-platform-foundation-infrastructure P04 | 5 min | 3 tasks | 11 files |
| Phase 02-platform-foundation-infrastructure P11 | 5min | 3 tasks | 29 files |
| Phase 02-platform-foundation-infrastructure P08 | 6 min | 3 tasks | 11 files |
| Phase 02-platform-foundation-infrastructure P05 | 6 min | 3 tasks | 11 files |
| Phase 02-platform-foundation-infrastructure P02 | 7 min | 3 tasks | 13 files |
| Phase 02-platform-foundation-infrastructure P10 | 6 min | 3 tasks | 12 files |
| Phase 02-platform-foundation-infrastructure P12 | 17min | 2 tasks | 5 files |
| Phase 02-platform-foundation-infrastructure P13 | 5min | 3 tasks | 2 files |
| Phase 03-identity-access-control P02 | 11 min | 3 tasks | 12 files |
| Phase 03-identity-access-control P03 | 8min | 3 tasks | 11 files |
| Phase 03-identity-access-control P04 | 8min | 2 tasks | 16 files |
| Phase 03-identity-access-control P05 | 10min | 3 tasks | 13 files |
| Phase 04-reference-data-extensibility-configuration P03 | 55min | 3 tasks | 26 files |
| Phase 04-reference-data-extensibility-configuration P05 | 35min | 2 tasks | 20 files |

## Accumulated Context

### Decisions

Decisions are logged in PROJECT.md Key Decisions table.
Recent decisions affecting current work:

- Pre-roadmap: Full microservice re-platform (not strangler-fig) — Pending confirmation via F0
- Pre-roadmap: Legacy functional audit (F0) is an explicit first-class deliverable before any build work
- Pre-roadmap: Database-per-service (no shared Postgres schema)
- Pre-roadmap: Conflict enforcement interim policy — hard block without `allowApproveBooking`, soft warning with it (pending final F0 confirmation)
- Pre-roadmap: Public feed access interim policy — fully public, no auth (pending final F0 confirmation)
- Roadmap: F10 (Settings) placed in Phase 4 alongside F4/F5 rather than later with F9, because F3's auto-approve logic has a hard functional dependency on reading the `approveBooking` flag at booking-creation time — Phase 5 (Core Booking) cannot be built without it existing first.
- [Phase 01-03]: FRD 'optional, free-text-validated' custom-fields placeholder corrected: legacy has NO server-side validation at all (not even free-text), confirmed via Controller.updateCustomFields()
- [Phase 01-03]: Custom-fields join/context model confirmed as two-tier: parentmodel scopes field definitions to exactly 2 model types (event, location), Customfieldjoin.customfieldchildid scopes values to specific instances — no per-Location-instance definition scoping exists
- [Phase 01-legacy-functional-audit]: PRD Open Question #2 interim decision (hard block without allowApproveBooking, soft warning with it) is corrected, not confirmed: legacy Bookings.check() is unconditionally non-blocking/informational for every user regardless of permission; create()/update() perform no server-side conflict validation at all — Direct code citation from controllers/Bookings.cfc: check(), create(), update() — see findings/01-booking-core.md
- [Phase 01-legacy-functional-audit]: No recurring-booking series concept exists past creation time: Bookings.create()'s repeat-loop creates independent sibling rows with no linking id; update()/delete() always target a single row — resolves PRD Open Question #1 — Direct code citation from controllers/Bookings.cfc: create(), update(), delete(); models/Event.cfc: registerSystemFields()
- [Phase 01-legacy-functional-audit]: FRD deletion-in-use interim assumption corrected: Locations/Resources are TRUE HARD DELETES (no deletedAt column on either table) with no booking-reference guard and no DB foreign keys anywhere in schema — orphan-allow-via-hard-delete, not soft-delete-intact-references as FRD assumed
- [Phase 01-legacy-functional-audit]: FRD Resource field-set and Location layout-metadata assumptions corrected: Resource has 5 fields (name, type, description, isunique, restrictlocations) sourced from DB schema since the model declares none itself; Location's 'layout' field is actually named 'layouts' (plural), free-text, and programmatically disconnected from Event.layoutstyle
- [Phase 01-legacy-functional-audit]: [Phase 01-04]: Initial-credential mechanism confirmed: admin sets the password directly at account-creation time (typed twice, no auto-generation, no email invite link) — views/users/formparts/_userpw.cfm + Users.create()
- [Phase 01-legacy-functional-audit]: [Phase 01-04]: PRD Open Question #9 resolved as NOT exhaustive: 17 total permission flags exist (6 PRD-named + 11 more), 15 actively enforced, 2 defined-but-dead (allowiCal, allowRSS) — full cross-controller inventory in findings/04-identity-access.md
- [Phase 01-legacy-functional-audit]: [Phase 01-04]: Remember-me cookie confirmed as a 360-day email-prefill convenience (not a session-duration extension); underlying session-timeout depends on an unconfirmed ColdFusion/Lucee engine default absent from this codebase's config
- [Phase 01-legacy-functional-audit]: [Phase 01-04]: Password complexity baseline confirmed from models/User.cfc regex: >=6 chars, >=1 digit, >=1 lowercase letter, no uppercase/symbol requirement — new system's policy must be no weaker
- [Phase 01-legacy-functional-audit]: PRD Open Question #7 corrected: legacy Api.cfc feeds are token-gated (per-user apitoken), not role/allowAPI-gated — allowAPI only gates the feed-listing index page and is granted to all roles by default
- [Phase 01-legacy-functional-audit]: PRD Open Question #8 resolved: logfiles table has no before/after value columns at all (message+data free text only); write-side coverage is a confirmed subset (Sessions/PasswordResets/Cookie helpers) plus incidental logging via a global logFlash after-filter on any flash message
- [Phase 01-legacy-functional-audit]: PRD Open Question #10 resolved: all 5 per-environment settings files ship empty; only one functional (non-config) difference exists codebase-wide — production serves minified JS, all else unminified. Maintenance mode is a full unconditional lockout for all users including admins, no bypass mechanism found
- [Phase 02-07]: settings-service singleton table uses CHECK (id = 1) constraint with ON CONFLICT DO NOTHING seed for idempotency; no updated_at trigger per TechArch §3.3 (application-managed column)
- [Phase 02-03]: Port 8083 for custom-field-service (following sequence: booking-service 8081, locations-resources-service 8082)
- [Phase 02-03]: ClusterIP-only Service for custom-field-service (no external Ingress) - internal service accessed via Gateway
- [Phase 02-platform-foundation-infrastructure]: users-permissions-service combines F6 and F7 into one deployable with two internal modules (user and permission) - tables have no FK between them to allow future split without schema migration
- [Phase 02-platform-foundation-infrastructure]: users.id is UUID PRIMARY KEY with NO DEFAULT - must be explicitly set to Keycloak sub claim at insert time, per TechArch §3.3
- [Phase 02-11]: Used react-router-dom 6.x useRoutes hook-based routing instead of older <Routes>/<Route> component tree for cleaner, type-safe route declarations
- [Phase 02-platform-foundation-infrastructure]: RabbitMQ dependency included in plan 02-05 (Phase 6 adds consumer logic only) — Keeps dependency set stable; Phase 6 only adds @RabbitListener code without modifying pom.xml
- [Phase 02-platform-foundation-infrastructure]: spring.rabbitmq.listener.simple.auto-startup=false prevents boot failure when RabbitMQ unreachable — Consistent with fail-closed-but-not-fail-crashed posture (TechArch Threat T-02-05-04)
- [Phase 02-platform-foundation-infrastructure]: Lazy JWT decoder initialization (NimbusReactiveJwtDecoder.withJwkSetUri()) instead of eager (ReactiveJwtDecoders.fromIssuerLocation()) to defer JWKS fetch until first request, allowing Gateway to start independently of Keycloak
- [Phase 03-03]: F0 Open Question #17 resolved: session timeouts set to 30 min idle / 10 hours max (fresh decision, not legacy parity)
- [Phase 03-03]: F0 Open Question #19 closed: password reset token expiry/used status re-validated at submit time
- [Phase 03-03]: F0 Open Question #20 closed: password-reset-request returns identical 202 response regardless of email existence
- [Phase 03-04]: UserSelfUpdateRequest type-level guard: NO password/role fields (compile-time, not runtime strip)
- [Phase 03-04]: createUser generates random initial password with temporary=true (improvement over legacy's permanent password)
- [Phase 03-05]: F0 Open Question #18 resolved: no in-memory permission cache (changes take effect on next JWT issue, improvement over legacy's restart-required behavior)
- [Phase 03-05]: TechArch §5 choice: scheduled polling-publisher bean (not Debezium CDC) for outbox relay - simpler, zero new infra, sets precedent
- [Phase 04-03]: F0-driven schema completion: custom_fields.required column (TechArch's V1 DDL omitted it entirely) added additively in V2 - storage/retrieval only, enforcement deferred to Phase 5's booking-service
- [Phase 04-03]: custom-field-service Tier-2 role requirement is an EXACT match (not merely not-weaker) to Gateway's Tier-1 for the entire /custom-fields/**+/field-templates/** route group - no read/write split, unlike locations-resources-service
- [Phase 04-03]: CUSTOM_FIELD_VALUE_INVALID error code deliberately NOT implemented in custom-field-service - validating custom_field_values[] is Phase 5 booking-service scope
- [Phase 04-05]: Settings singleton entity (no @GeneratedValue) enforces F10.3 at the code layer alongside the existing DB CHECK constraint
- [Phase 04-05]: SETTINGS_UNAVAILABLE (503) deliberately not implemented in settings-service - it is the calling service's (Phase 5 booking-service) error when this service is unreachable
- [Phase 04-05]: pgcrypto extension added in V2 migration (absent from V1) to support outbox table's gen_random_uuid() defaults

### Pending Todos

None yet.

### Blockers/Concerns

- Phase 1 (F0 audit) is a hard-blocking gate: no F1–F13 implementation work should begin until the audit document, Open Questions list, and F13 traceability seed handoff are complete and signed off.
- F0 audit complete: of the 10 PRD Section 10 seed Open Questions plus 15 additional items Wave 1 discovered (25 total, see `.planning/phases/01-legacy-functional-audit/open-questions.md`), 12 are RESOLVED with direct code citation and 13 remain OPEN pending an explicit product/architecture decision (not a legacy-behavior ambiguity — the legacy facts are confirmed, but the forward-looking design choice is not this planning agent's to make). `open-questions.md` is the authoritative list going forward; the original roadmap-era "14 FRD-level flags" framing is superseded by this audit's actual, cited outcome.
- F0 sign-off gate: `legacy-audit-findings.md` (12 controller + 7 model-group sections), `open-questions.md` (10 seed questions resolved-or-logged, plus 15 additional), and `baseline-inventory.md` (F13 seed, 113 rows) are complete and handed off. Any OPEN items remain tracked in open-questions.md and must be resolved via explicit product decision before the corresponding F1–F13 feature is finalized — they do not block the START of downstream phase planning, but do block FINALIZING the specific behavior they concern, per PRD Section 7's release-time success metric.

## Session Continuity

Last session: 2026-10-09T00:03:32.655Z
Stopped at: Completed 04-05-PLAN.md
Resume file: None
