# Requirements Traceability Matrix: Booking-Hub

**Project Acronym:** BookingHub
**Document Type:** RTM (Requirements Traceability Matrix)
**Status:** Draft — Derived from PRD-BookingHub.md, FRD-BookingHub.md, TechArch-BookingHub.md, UserStories-BookingHub.md
**Generated:** 2026-10-06
**Last Updated:** 2026-10-06

---

## 1. Overview

This Requirements Traceability Matrix (RTM) provides bidirectional traceability between Booking-Hub's product requirements (PRD), functional requirements (FRD), technical architecture (TechArch), and user stories, so that the project's hard constraint of **zero functional regression** against the legacy OxAlto RoomBooking System can be continuously and objectively verified rather than assumed. Booking-Hub's own FRD (feature F13, "Regression Verification & Test Traceability") already mandates this artifact as a first-class deliverable — this document is the realization of that mandate at the current spec-complete stage of the project, ahead of F0 (Legacy Functional Audit) sign-off and ahead of any implementation work.

Because Booking-Hub is a re-platform governed by an explicit audit-first process, every row in this matrix ultimately traces back to one of the 14 PRD features (F0–F13), each of which carries the **same feature ID** through the FRD (per the FRD's own convention: "Feature IDs (F0–F13) are stable references matching the PRD feature index"), through its owning TechArch service/component, and through its corresponding User Story epic. Traceability therefore flows: **PRD Feature (F#) → FRD Requirement (same F#, broken into Validation rules and Error States) → TechArch Component (owning microservice/schema/API) → User Story (US-#.#)**, with the FRD's per-rule phrasing (one testable assertion per Validation bullet or Error States row) serving as the seed for the eventual automated test suite (F13). Fourteen `[OPEN QUESTION — deferred to F0]` flags remain unresolved in the FRD as of this writing; every such flag is tracked explicitly in this matrix rather than silently treated as resolved, consistent with the project's ambiguity-handling constraint recorded in `PROJECT.md`.

Three traceability levels are maintained in this document: (1) **feature-level** traceability (Section 3), linking each of the 14 PRD/FRD features to its TechArch-owning service and its User Story epic; (2) **rule-level** traceability (Section 4), enumerating the FRD's individual Validation rules and Error State codes per feature as the atomic, independently testable units F13 will convert into automated tests; and (3) **test-coverage** traceability (Section 5), which — because this is a pre-implementation, spec-complete snapshot — reports the *planned* test surface (count of testable FRD assertions per feature, mapped against user story acceptance criteria) rather than executed test results, using the same `not_started | implemented | tested | verified` status vocabulary the FRD's own `traceability_entries` schema (`Y0-schema.md` §Traceability) defines for this exact purpose.

## 2. Requirements Summary

**By priority (per PRD Section 9 Feature Index and PRD Section 1):**
- **P0 — Critical (8 features):** F0 Legacy Functional Audit, F1 Booking Management Service, F2 Conflict Detection, F3 Approval Workflow, F6 User & Role Management, F7 Permission System, F12 Microservice Architecture & Platform, F13 Regression Verification & Test Traceability.
- **P1 — High (6 features):** F4 Locations & Resources Management, F5 Custom Fields, F8 Notifications, F9 Public Feeds, F10 Settings, F11 Activity/Audit Logging.
- **P2/P3:** None — the strict feature-parity mandate treats every legacy capability area as required for this release (PRD Section 9).

**By category (per PRD Section 9):**
- **Foundation / Discovery:** F0 Legacy Functional Audit.
- **Core Booking:** F1 Booking Management Service, F2 Conflict Detection, F3 Approval Workflow.
- **Reference Data / Admin:** F4 Locations & Resources Management.
- **Booking Extensibility:** F5 Custom Fields.
- **Identity:** F6 User & Role Management.
- **Identity / Access Control:** F7 Permission System.
- **Messaging:** F8 Notifications.
- **Integration / Visibility:** F9 Public Feeds.
- **Configuration:** F10 Settings.
- **Compliance / Observability:** F11 Activity/Audit Logging.
- **Foundation / Infrastructure:** F12 Microservice Architecture & Platform.
- **Quality / Verification:** F13 Regression Verification & Test Traceability.

**By FRD testable-assertion volume** (Validation bullets + Error States rows per feature chunk — the atomic units F13 maps 1:1 to automated tests):
- F1 (Booking Management Service) carries the largest rule surface: 15 testable assertions (8 Validation, 7 Error States).
- F6 (User & Role Management) carries 11 testable assertions (4 Validation, 7 Error States).
- F5 (Custom Fields) carries 10 testable assertions (5 Validation, 5 Error States).
- F2, F3, F4, F7 each carry 8 testable assertions.
- F8, F10, F12, F13 each carry 9 testable assertions.
- F0, F9, F11 carry 8, 6, and 7 testable assertions respectively.
- **Total across all 14 features: 125 independently testable FRD assertions** forming the F13 regression-suite seed set.

**By open-question exposure** (FRD `[OPEN QUESTION — deferred to F0]` flags, per feature):
- F1: 3 open flags (recurrence pattern shape, series edit/delete scope default, recurrence finiteness constraint).
- F2: 2 open flags (multi-resource conflict scope communication; denied-booking exclusion rationale) plus the interim hard-block/soft-warning Key Decision pending final F0 confirmation.
- F3: 2 open flags (auto-approve/notification interaction; denial-reason requirement).
- F4: 3 open flags (Resource field set beyond name; deletion-in-use policy; `layout` metadata shape; name-uniqueness constraint).
- F5: 4 open flags (field-type enum; join/context scope model; required/optional + type validation; public-feed exposure of custom fields; retention policy).
- F6: 2 open flags (initial-credential mechanism; remember-me duration; password-complexity parity).
- F7: 1 open flag (immediate-vs-next-session role-change semantics).
- F8: 1 open flag (notification recipient rules — PRD Open Question #5).
- F9: 1 open flag (interim fully-public Key Decision pending final F0 confirmation — PRD Open Question #7).
- F10: 2 open flags (settings-propagation mechanism — resolved in TechArch §1.5; per-environment functional differences — PRD Open Question #10).
- F11: 1 open flag (before/after value completeness and action-coverage scope — PRD Open Question #8).
- F0, F12, F13 carry no FRD-level open flags themselves (F0 is the process that resolves all others; F12/F13 are new infrastructure with no legacy equivalent).

## 3. Traceability Matrix — Feature Level

Each row links a PRD Feature to its FRD requirement chunk (same ID), its owning TechArch component(s), and its User Story epic. "FRD Rules" is the count of Validation + Error States assertions (Section 4 gives the itemized breakdown for every P0 feature and the error-code-bearing P1 features).

| PRD Feature | FRD Requirement | TechArch Component | FRD Rules | User Story Epic |
|---|---|---|---|---|
| F0: Legacy Functional Audit | FRD F0 (process/documentation feature — no runtime API/schema) | N/A — documentation deliverable; feeds F13 traceability seed | 8 | Epic 0 (US-0.1, US-0.2) |
| F1: Booking Management Service | FRD F1 | **booking-service** (`booking_db`; owns `bookings`, `booking_resources`) — TechArch §2.2 | 15 | Epic 1 (US-1.1–US-1.7) |
| F2: Conflict Detection | FRD F2 | **booking-service** `ConflictDetectionService` module (single code path) — TechArch §2.2, §1.5 | 8 | Epic 2 (US-2.1–US-2.3) |
| F3: Approval Workflow | FRD F3 | **booking-service** (status workflow columns on `bookings`) — TechArch §2.2 | 8 | Epic 3 (US-3.1–US-3.4) |
| F4: Locations & Resources Management | FRD F4 | **locations-resources-service** (`locres_db`; owns `locations`, `resources`) — TechArch §2.3 | 8 | Epic 4 (US-4.1–US-4.4) |
| F5: Custom Fields | FRD F5 | **custom-field-service** (`customfld_db`; owns `custom_fields`, `custom_field_templates`, `custom_field_joins`, `custom_field_values`) — TechArch §2.4 | 10 | Epic 5 (US-5.1–US-5.3) |
| F6: User & Role Management | FRD F6 | **users-permissions-service** `user` module + Keycloak realm — TechArch §2.5, §2.11 | 11 | Epic 6 (US-6.1–US-6.5) |
| F7: Permission System | FRD F7 | **users-permissions-service** `permission` module + Spring Cloud Gateway RBAC — TechArch §2.1, §2.5 | 8 | Epic 7 (US-7.1–US-7.3) |
| F8: Notifications | FRD F8 | **notifications-service** (`notif_db`; owns `notification_deliveries`) — TechArch §2.6 | 9 | Epic 8 (US-8.1–US-8.4) |
| F9: Public Feeds | FRD F9 | **feeds-service** (`feeds_db`; owns `feed_bookings` projection) — TechArch §2.7 | 6 | Epic 9 (US-9.1–US-9.4) |
| F10: Settings | FRD F10 | **settings-service** (`settings_db`; owns `settings` singleton) — TechArch §2.8 | 9 | Epic 10 (US-10.1–US-10.3) |
| F11: Activity/Audit Logging | FRD F11 | **audit-log-service** (`audit_db`; owns `audit_log_entries`, INSERT-only) — TechArch §2.9 | 7 | Epic 11 (US-11.1–US-11.3) |
| F12: Microservice Architecture & Platform | FRD F12 | **All 9 deployable units** + Spring Cloud Gateway + React/TypeScript SPA + RabbitMQ topology + Keycloak realm — TechArch §1–§2 (whole document) | 9 | Epic 12 (US-12.1–US-12.2) |
| F13: Regression Verification & Test Traceability | FRD F13 | **traceability_db** (optional; `traceability_entries` table) / CI pipeline — TechArch §3.3 | 9 | Epic 13 (US-13.1–US-13.2) |

**Totals:** 14 PRD features ↔ 14 FRD requirement chunks ↔ 9 TechArch deployable services (+ Gateway, Frontend, Keycloak) ↔ 13 User Story epics (F0–F13 map to Epics 0–13) ↔ 49 user stories ↔ 125 FRD testable assertions.

## 4. Requirements Detail — Rule-Level Traceability

This section itemizes each feature's FRD Validation rules and Error States (the atomic units F13 converts 1:1 into automated tests), cross-referenced to the User Stories whose acceptance criteria exercise them.

### F0: Legacy Functional Audit → Epic 0 (US-0.1, US-0.2)
- Every controller named in PRD §5 F0 has an audit-findings section before sign-off → US-0.1
- Every model named in PRD §5 F0 has an audit-findings section before sign-off → US-0.1
- Every PRD §10 seed Open Question (1–10) has a cited resolution or remains logged "open because..." → US-0.2
- Every audit finding has a reviewer-traceable citation → US-0.1
- F0 is not marked complete until the above conditions hold (P0 blocking gate) → US-0.1, US-0.2
- Error: missing controller/model section blocks sign-off → US-0.1
- Error: Open Question resolved by assumption is rejected → US-0.2
- Error: F13 seeding skipped blocks F13 kickoff → US-0.2

### F1: Booking Management Service → Epic 1 (US-1.1–US-1.7)
- `title` non-empty (`BOOKING_TITLE_REQUIRED`, 400) → US-1.1
- `end_time` strictly after `start_time`, zero-duration rejected (`BOOKING_INVALID_TIME_RANGE`, 400) → US-1.1
- `end_time` omitted defaults to `start_time + 1h` → US-1.1
- `location_id` must reference existing, non-deleted Location (`BOOKING_LOCATION_NOT_FOUND`, 404) → US-1.1
- Each `resource_ids` entry must reference existing, non-deleted Resource (`BOOKING_RESOURCE_NOT_FOUND`, 404) → US-1.1
- `recurrence`, if present, must be finite/determinate **[OPEN QUESTION — deferred to F0]** → US-1.6
- Series edit/delete requires `scope` value (`BOOKING_SCOPE_REQUIRED`, 400) **[OPEN QUESTION — deferred to F0, PRD OQ#1]** → US-1.2, US-1.3
- Conflict Detection (F2) invoked synchronously at create/edit, not post-hoc → US-1.1, US-1.2
- Clone of non-existent source rejected (`BOOKING_SOURCE_NOT_FOUND`, 404) → US-1.4
- Create/edit blocked by conflict for non-approver (`BOOKING_CONFLICT`, 409) → US-1.1, US-2.1

### F2: Conflict Detection → Epic 2 (US-2.1–US-2.3)
- Booking never compared against itself (`exclude_booking_id` honored) → US-2.1
- Deleted bookings excluded from conflict comparison → US-2.1
- Denied bookings excluded **[OPEN QUESTION — deferred to F0]** → US-2.1
- Half-open interval semantics (touching boundary = no conflict) → US-2.1
- Conflict re-evaluated on every edit (not cached) → US-2.3
- Error: hard block for non-`allowApproveBooking` callers (`BOOKING_CONFLICT`, 409) — interim Key Decision, pending F0 → US-1.1, US-2.1
- Soft warning (save proceeds, flagged) for `allowApproveBooking` holders → US-2.3, US-3.2
- Error: invalid/missing input to conflict check (`CONFLICT_CHECK_INVALID_INPUT`, 400) → US-2.1

### F3: Approval Workflow → Epic 3 (US-3.1–US-3.4)
- Approve/deny only permitted when status is exactly `pending` → US-3.2, US-3.3
- Caller must hold `allowApproveBooking`, verified server-side → US-3.1, US-3.2
- `denial_reason` optional, no required format **[OPEN QUESTION — deferred to F0]** → US-3.3
- `approveBooking` flag read at creation time only, non-retroactive → US-3.4
- Error: caller lacks permission (`APPROVAL_FORBIDDEN`, 403) → US-3.2
- Error: target not in `pending` state (`APPROVAL_INVALID_STATE`, 409) → US-3.2, US-3.3
- Error: target booking not found (`APPROVAL_BOOKING_NOT_FOUND`, 404) → US-3.2
- Error: Settings Service unavailable at creation time (`APPROVAL_SETTINGS_UNAVAILABLE`, 503) → US-3.4, US-10.3

### F4: Locations & Resources Management → Epic 4 (US-4.1–US-4.4)
- `name` required/non-empty for Location and Resource → US-4.1, US-4.2
- `name` uniqueness among non-deleted Locations **[OPEN QUESTION — deferred to F0]** → US-4.1
- Only admin permission holders may create/edit/delete → US-4.1, US-4.2
- Deletion-in-use follows interim soft-delete policy **[OPEN QUESTION — deferred to F0]** → US-4.3
- Error: missing/empty name (`LOCATION_NAME_REQUIRED` / `RESOURCE_NAME_REQUIRED`, 400) → US-4.1, US-4.2
- Error: caller lacks admin permission (`LOCATION_RESOURCE_FORBIDDEN`, 403) → US-4.1, US-4.2
- Error: target not found (`LOCATION_NOT_FOUND` / `RESOURCE_NOT_FOUND`, 404) → US-4.3
- Error: delete blocked for in-use entity, pending policy confirmation (`LOCATION_IN_USE` / `RESOURCE_IN_USE`, 409 interim) → US-4.3

### F5: Custom Fields → Epic 5 (US-5.1–US-5.3)
- `label` required/non-empty → US-5.1
- `options[]` required/non-empty when `field_type = select` → US-5.1
- Submitted `custom_field_values[]` must reference applicable `field_id` **[OPEN QUESTION — deferred to F0]** → US-5.2
- Required/optional + type-specific validation **[OPEN QUESTION — deferred to F0, PRD OQ#6]** → US-5.2
- Only admin permission holders manage definitions/templates → US-5.1
- Error: missing/empty label (`CUSTOM_FIELD_LABEL_REQUIRED`, 400) → US-5.1
- Error: select type with empty options (`CUSTOM_FIELD_OPTIONS_REQUIRED`, 400) → US-5.1
- Error: caller lacks admin permission (`CUSTOM_FIELD_FORBIDDEN`, 403) → US-5.1
- Error: unknown `field_id` in submitted value (`CUSTOM_FIELD_VALUE_INVALID`, 400) → US-5.2
- Error: target field/template not found (`CUSTOM_FIELD_NOT_FOUND`, 404) → US-5.1, US-5.3

### F6: User & Role Management → Epic 6 (US-6.1–US-6.5)
- `email`/`username` required and unique among active accounts → US-6.1
- `new_password` must meet Keycloak policy, no weaker than legacy **[OPEN QUESTION — deferred to F0]** → US-6.2
- `reset_token` must be valid (unexpired, unused) at completion → US-6.3
- Only admin may create accounts/assign roles; self-edit always permitted → US-6.1, US-6.2, US-6.5
- Error: invalid login credentials (`AUTH_INVALID_CREDENTIALS`, 401) → US-6.4
- Error: unauthenticated access to protected resource (`AUTH_UNAUTHENTICATED`, 401) → US-6.4
- Error: insufficient role on valid token (`AUTH_FORBIDDEN`, 403) → US-6.4
- Error: duplicate account on creation (`USER_ALREADY_EXISTS`, 409) → US-6.1
- Error: expired/invalid/used reset token (`PASSWORD_RESET_TOKEN_INVALID`, 400) → US-6.3
- Error: incorrect current password on change (`PASSWORD_CHANGE_INVALID_CURRENT`, 401) → US-6.2
- Error: new password fails policy (`PASSWORD_POLICY_VIOLATION`, 400) → US-6.2

### F7: Permission System → Epic 7 (US-7.1–US-7.3)
- Every permission flag referenced elsewhere appears in the mapping table before its gated action ships → US-7.1
- Unconfirmed flag treated as restrictive (deny-by-default), never "no restriction" → US-7.1
- Only `accessPermissions` holders modify role-permission mappings → US-7.2
- Gateway-level and service-level enforcement must agree (no service grants what Gateway blocks) → US-7.3
- Error: Gateway-protected route without role/scope (`GATEWAY_FORBIDDEN`, 401/403) → US-7.3
- Error: service-level fine-grained check fails despite Gateway pass (`ACTION_FORBIDDEN`, 403) → US-7.3
- Error: mapping modification without `accessPermissions` (`PERMISSIONS_FORBIDDEN`, 403) → US-7.2
- Error: reference to unconfirmed/undefined flag (`PERMISSION_FLAG_UNDEFINED`, 400) → US-7.2

### F8: Notifications → Epic 8 (US-8.1–US-8.4)
- Every consumed event must include non-empty `idempotency_key` → US-8.1, US-8.3
- Event already `sent` for a given key is never re-sent → US-8.1
- Recipient resolution must not silently fail open or closed **[OPEN QUESTION — deferred to F0, PRD OQ#5]** → US-8.3
- Template rendering failures treated as processing failures, not swallowed → US-8.2
- Impact: SMTP transient failure → retried per backoff policy → US-8.2
- Impact: retry exhaustion → routed to DLQ, never silently dropped → US-8.2, US-8.3
- Impact: missing idempotency key → routed to DLQ immediately → US-8.3
- Impact: unresolvable recipient → routed to DLQ, does not block originating transaction → US-8.3
- Impact: duplicate delivery → suppressed via idempotency key, no customer-visible error → US-8.1

### F9: Public Feeds → Epic 9 (US-9.1–US-9.4)
- Only `status = approved` Bookings ever appear in any feed format → US-9.1, US-9.4
- Unknown `location_id` filter returns empty feed, not an error → US-9.2
- Feed access evaluated identically regardless of format — interim Key Decision (fully public), pending F0 (PRD OQ#7) → US-9.3
- Error: unsupported format requested (`FEED_FORMAT_UNSUPPORTED`, 400) → US-9.2
- Non-error: unknown `location_id` → 200 empty feed → US-9.2
- Error: upstream read model unavailable (`FEED_SOURCE_UNAVAILABLE`, 503) → US-9.4

### F10: Settings → Epic 10 (US-10.1–US-10.3)
- Only one Settings record exists system-wide (singleton) → US-10.3
- `calendar_min_time` must precede `calendar_max_time` → US-10.2
- `calendar_slot_size` must be a positive integer → US-10.2
- Only admin may modify Settings; read access may be broader **[confirm in F0]** → US-10.1
- Per-environment functional differences never implemented by assumption **[OPEN QUESTION — deferred to F0, PRD OQ#10]** → US-10.1
- Error: caller lacks admin permission (`SETTINGS_FORBIDDEN`, 403) → US-10.1
- Error: invalid calendar range (`SETTINGS_INVALID_CALENDAR_RANGE`, 400) → US-10.2
- Error: non-positive slot size (`SETTINGS_INVALID_SLOT_SIZE`, 400) → US-10.2
- Error: Settings Service unavailable to dependent reader (`SETTINGS_UNAVAILABLE`, 503) → US-10.3

### F11: Activity/Audit Logging → Epic 11 (US-11.1–US-11.3)
- 100% of regression-suite state changes produce a corresponding audit entry → US-11.3
- Audit entries immutable once written (no API may mutate/delete) → US-11.2
- Only admin (`Logfiles`-equivalent) permission holders may query the log → US-11.1
- Missed/dropped event surfaces via DLQ, never silently gaps the trail → US-11.3
- Error: caller lacks permission to view log (`AUDIT_LOG_FORBIDDEN`, 403) → US-11.1
- Error: invalid date range filter, `date_from` after `date_to` (`AUDIT_LOG_INVALID_DATE_RANGE`, 400) → US-11.1
- Impact: event consumption failure after retry exhaustion → routed to DLQ, surfaced via observability → US-11.3

### F12: Microservice Architecture & Platform → Epic 12 (US-12.1–US-12.2)
- No service directly connects to another service's PostgreSQL database → US-12.1
- No backend service reachable except through Spring Cloud Gateway → US-12.2
- Every domain event referenced in F1–F11 has a corresponding RabbitMQ exchange/queue/routing key → US-12.2
- Every F7 permission flag has a corresponding Keycloak role/scope in the realm config → US-12.2
- Each service independently buildable/deployable/scalable without coordinated redeploy → US-12.1
- Impact: direct cross-service DB connection string → rejected in code review/ArchUnit rule → US-12.1
- Impact: backend service exposed via its own external Ingress → rejected in deployment review → US-12.2
- Impact: domain event published with no consumer binding → blocks deployment of publisher → US-12.2
- Impact: hidden coordinated-deploy dependency → must be identified/removed before release → US-12.1

### F13: Regression Verification & Test Traceability → Epic 13 (US-13.1–US-13.2)
- Every F0 audit finding has ≥1 linked FRD requirement row (no orphans) → US-13.1
- Every FRD Validation/Error States bullet in F1–F12 has ≥1 linked test before release-ready → US-13.1
- No requirement marked `tested`/`verified` without a concrete, real `test_ref` → US-13.1
- Merge/release blocked if a previously `tested` requirement's linked test now fails → US-13.2
- Every Open Question closure produces both a matrix update and a new test → US-13.2
- Impact: F0 finding with no FRD requirement row → blocks release, add missing row → US-13.1
- Impact: requirement marked `tested` without a real test → rejected in matrix review → US-13.1
- Impact: CI regression failure on previously passing test → blocks merge/deploy → US-13.2
- Impact: Open Question resolved without a new test → rejected, test required before closure → US-13.2

## 5. Test Case Coverage

Because Booking-Hub is at the spec-complete, pre-implementation stage (F0 Legacy Functional Audit has not yet been signed off, per `PROJECT.md`'s "Active" requirement status), no automated tests have been written yet. This table reports the **planned test surface** — the count of independently testable FRD assertions (the seed set F13 will convert into unit/integration/E2E tests) against each feature's user story acceptance criteria — with status tracked using the same vocabulary as the FRD's `traceability_entries.status` column (`not_started | implemented | tested | verified`).

| Feature | Validation Rules | Error States | Total Testable Assertions | User Stories | Acceptance Criteria (approx.) | Status |
|---|---|---|---|---|---|---|
| F0: Legacy Functional Audit | 5 | 3 | 8 | 2 | 8 | not_started |
| F1: Booking Management Service | 8 | 7 | 15 | 7 | 32 | not_started |
| F2: Conflict Detection | 5 | 3 | 8 | 3 | 13 | not_started |
| F3: Approval Workflow | 4 | 4 | 8 | 4 | 16 | not_started |
| F4: Locations & Resources Management | 4 | 4 | 8 | 4 | 15 | not_started |
| F5: Custom Fields | 5 | 5 | 10 | 3 | 10 | not_started |
| F6: User & Role Management | 4 | 7 | 11 | 5 | 19 | not_started |
| F7: Permission System | 4 | 4 | 8 | 3 | 9 | not_started |
| F8: Notifications | 4 | 5 | 9 | 4 | 13 | not_started |
| F9: Public Feeds | 3 | 3 | 6 | 4 | 14 | not_started |
| F10: Settings | 5 | 4 | 9 | 3 | 9 | not_started |
| F11: Activity/Audit Logging | 4 | 3 | 7 | 3 | 8 | not_started |
| F12: Microservice Architecture & Platform | 5 | 4 | 9 | 2 | 6 | not_started |
| F13: Regression Verification & Test Traceability | 5 | 4 | 9 | 2 | 6 | not_started |
| **Total** | **65** | **60** | **125** | **49** | **178** | **0% tested** |

**Coverage gating note (per F13 §Validation):** No feature may be marked `tested`/`verified` until (a) F0 sign-off resolves or explicitly accepts every open question listed in Section 2 above, and (b) each row's `test_ref` in the `traceability_entries` table (`Y0-schema.md` §Traceability) points to a real, executing test — a placeholder reference is explicitly rejected. The 14 features above are 100% specified (every Validation/Error States bullet is phrased as a single independently verifiable assertion per the FRD's "Testability requirement" convention) but 0% implemented/tested as of this document's generation date, which is itself the expected, correct state for a project still gated on F0.

## 6. Change Management

| Version | Date | Change Description | Changed By | Affected Sections |
|---|---|---|---|---|
| 0.1 | 2026-10-06 | Initial RTM generated from PRD-BookingHub.md, FRD-BookingHub.md, TechArch-BookingHub.md, UserStories-BookingHub.md following spec validation pass that recorded three interim Key Decisions (conflict-enforcement policy, public-feed access policy, booking time-range precision) in `PROJECT.md` | Pivota Spec RTM Generator | All sections |

**Open items carried forward to next revision:**
- F0 Legacy Functional Audit has not yet produced its findings document or confirmed/closed the 10 PRD Section 10 seed Open Questions; this RTM will require a full re-traceability pass once F0 signs off (new/changed FRD rules, removed `[OPEN QUESTION]` flags, and the first `implementation_ref`/`test_ref` entries).
- The three interim Key Decisions recorded in `PROJECT.md` (conflict enforcement hard-block/soft-warning split; public feed fully-public access; strict `end_time > start_time` precision) are marked "Interim — pending final F0 confirmation" in two of three cases and must be re-verified against this matrix once F0 closes them.
- F13's own `traceability_entries` persisted schema (optional, `traceability_db`) is not yet provisioned; this document is the interim artifact until that store (or an external test-management tool) is stood up.

## 7. Approval

| Role | Name | Signature | Date | Decision |
|---|---|---|---|---|
| Product Owner | _________________________ | _________________________ | _________________________ | ☐ Approved ☐ Approved with comments ☐ Rejected |
| Engineering Lead / TechArch Owner | _________________________ | _________________________ | _________________________ | ☐ Approved ☐ Approved with comments ☐ Rejected |
| QA / Test Lead (F13 owner) | _________________________ | _________________________ | _________________________ | ☐ Approved ☐ Approved with comments ☐ Rejected |
| F0 Legacy Audit Owner | _________________________ | _________________________ | _________________________ | ☐ Approved ☐ Approved with comments ☐ Rejected |

**Sign-off condition (per PRD Section 10 and FRD F0 §Validation):** This RTM is accepted as the baseline traceability artifact for the current spec-complete milestone. Full approval of feature-level test coverage (Section 5 reaching `tested`/`verified` status) is explicitly deferred until F0 (Legacy Functional Audit) sign-off resolves or formally accepts every Open Question enumerated in Section 2, per the project's non-negotiable ambiguity-handling constraint — no open question may be closed by assumption, and no requirement row may be marked tested without a genuine, executing test reference.

---
*Document generated by Pivota Spec RTM Generator*
*Traces: PRD-BookingHub.md ↔ FRD-BookingHub.md ↔ TechArch-BookingHub.md ↔ UserStories-BookingHub.md*
*Last updated: 2026-10-06*
