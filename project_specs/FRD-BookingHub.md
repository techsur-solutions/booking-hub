# Functional Requirements Document: Booking-Hub

**Project Acronym:** BookingHub
**Document Type:** FRD (Functional Requirements Document)
**Status:** Draft — Derived from PRD-BookingHub.md v1 (2026-10-06)
**Generated:** 2026-10-06

---

## Scope

This FRD specifies the exact functional behavior for every feature (F0–F13) identified in `PRD-BookingHub.md`, grounded in the known legacy domain model documented in `.planning/PROJECT.md` (the OxAlto RoomBooking ColdFusion/cfWheels application). Per the project's hard constraint on zero functional regression, every rule specified here either (a) restates a confirmed legacy behavior with a legacy reference, or (b) is explicitly marked **`[OPEN QUESTION — deferred to F0]`** when the legacy behavior is not yet confirmed from the initial scan. No rule in this document invents unconfirmed legacy behavior; where PRD Section 10 lists an open question relevant to a feature, that question is restated verbatim in the feature's Validation/Process/Error sections rather than silently resolved. Every rule in this document is written to be independently testable, feeding directly into F13's audit → requirement → test traceability matrix.

## Conventions

- **Feature IDs** (`F0`–`F13`) are stable references matching the PRD feature index; chunk filenames use zero-padded `F00`–`F13` for sort order only — the functional ID remains `F0`–`F13` in prose and cross-references.
- **`[OPEN QUESTION — deferred to F0]`** marks any rule whose exact legacy behavior is unconfirmed. These are not implementation gaps — they are explicit flags that F0 (Legacy Functional Audit) must resolve via code/doc/demo evidence before the corresponding implementation is finalized, per PRD Section 10's resolution process. Implementers must not guess at these; the interim/default behavior stated (where given) is a safe placeholder only, subject to F0 confirmation.
- **Service ownership:** Each feature chunk names the owning bounded-context service(s) per PRD Section 4's indicative decomposition (Booking, Location & Resource, Custom Field, User/Identity-adjacent + Keycloak, Notification, Public Feed, Settings, Audit Log services). Service boundaries are indicative pending F0/TechArch confirmation.
- **Cross-references:** `see F03 §Process step 2` style references point to another feature chunk's section. `see Y0 §Booking` points to the consolidated schema chunk's domain subsection. `see Y1 §Auth` points to the consolidated API chunk's domain subsection.
- **HTTP/error conventions:** All error tables use HTTP status, a stable machine-readable error code, and a human-readable message. The full cross-feature error catalog is in `Y2-errors.md`; per-feature chunks list only errors specific to that feature's primary flows.
- **Testability requirement:** Every bullet under Validation and every row under Error States in every feature chunk must be phrased as a single, independently verifiable assertion (one behavior per bullet/row) so it maps 1:1 to a test case in F13's traceability matrix.

## Shared Terminology (cross-feature)

- **Event (legacy) / Booking (new):** The legacy `Event` model is renamed `Booking` in all new-system specs; legacy field/table names are noted in parentheses where relevant for audit traceability.
- **Eventresource (legacy) / BookingResource (new):** The join entity associating a Booking with one or more Resources.
- **Bounded-context service:** A Spring Boot 3 microservice owning exactly one PostgreSQL database/schema, per PRD Section 4 and the `Service isolation` non-functional requirement.
- **Domain event:** A RabbitMQ message published by one service and consumed by one or more other services, replacing legacy synchronous side effects (e.g., `booking.created`, `booking.approved`, `booking.denied`, `password.reset.requested`).
- **Auto-approve:** The behavior driven by the global `approveBooking` Settings flag (F10) where a new booking's initial status is `approved` instead of `pending` when the flag is disabled. See F1 §Process, F3 §Process.
- **Permission flag:** A named legacy access-control gate (e.g., `accessCalendar`, `allowRoomBooking`, `viewRoomBooking`, `allowApproveBooking`, `accessPermissions`, `allowAPI`) re-implemented as a Keycloak role/scope per F7. The named set is non-exhaustive per PRD; F0 must confirm the complete set.
- **Actor:** The authenticated principal (Keycloak subject) performing a state-changing action, recorded in every audit log entry (F11).
- **Traceability matrix:** The F13 artifact linking each F0 audit finding → FRD requirement (this document) → implementation → automated test.

## Master Table of Contents

| Chunk | Contents |
|---|---|
| `00-header.md` | This file — conventions, shared terminology |
| `F00-legacy-functional-audit.md` | F0: Legacy Functional Audit |
| `F01-booking-management-service.md` | F1: Booking Management Service |
| `F02-conflict-detection.md` | F2: Conflict Detection |
| `F03-approval-workflow.md` | F3: Approval Workflow |
| `F04-locations-resources-management.md` | F4: Locations & Resources Management |
| `F05-custom-fields.md` | F5: Custom Fields |
| `F06-user-role-management.md` | F6: User & Role Management |
| `F07-permission-system.md` | F7: Permission System |
| `F08-notifications.md` | F8: Notifications |
| `F09-public-feeds.md` | F9: Public Feeds |
| `F10-settings.md` | F10: Settings |
| `F11-activity-audit-logging.md` | F11: Activity/Audit Logging |
| `F12-microservice-architecture-platform.md` | F12: Microservice Architecture & Platform |
| `F13-regression-verification-test-traceability.md` | F13: Regression Verification & Test Traceability |
| `Y0-schema.md` | Consolidated database DDL, all services |
| `Y1-api.md` | Consolidated REST API endpoint catalog |
| `Y2-errors.md` | Cross-feature error catalog |
| `Y3-integrations.md` | External integration points (Keycloak, RabbitMQ, SMTP) |

---
## F0: Legacy Functional Audit

**Description:** F0 is a documentation-producing feature, not a running service: it is the end-to-end review of the OxAlto RoomBooking ColdFusion/cfWheels codebase (~307 files) that produces the authoritative functional specification all other FRD chunks in this document are grounded in. Every subsequent feature chunk (F1–F13) either cites a confirmed F0 finding or carries an explicit `[OPEN QUESTION — deferred to F0]` flag pending that finding. F0 has no runtime API or schema of its own; its "output" is the Open Questions resolution log and the audit findings document that feeds F13's traceability matrix.

**Terminology:**
- **Audit finding:** A single documented fact about legacy behavior (a validation rule, workflow transition, permission gate, etc.), sourced from code, legacy docs, or the public demo site, with a citation (file/function/line or doc section).
- **Open Question:** A specific legacy behavior that cannot be confidently determined from available sources; logged per PRD Section 10's resolution process, never silently resolved.
- **Key Decision:** The explicit, recorded resolution of an Open Question that could not be determined from legacy evidence, requiring product sign-off before the dependent feature is implemented.

**Sub-features:**
- Controller-by-controller review: `Api`, `Bookings`, `Customfields`, `Eventdata`, `Locations`, `Logfiles`, `PasswordResets`, `Permissions`, `Resources`, `Sessions`, `Settings`, `Users`
- Model-by-model review: `Event`, `Eventresource`, `Location`, `Resource`, `Customfield`, `Customfieldjoin`, `Customfieldvalue`, `User`, `Settings`
- Route inventory (`config/routes.cfm`) mapped to controller actions
- CF application lifecycle event handler review: `onApplicationStart/End`, `onSessionStart/End`, `onRequestStart/End`, `onError`, `onMaintenance`, `onMissingTemplate`
- Per-environment settings behavior review (development/production/testing/design/maintenance)
- Open Questions list production and maintenance
- Baseline inventory handoff to F13 for traceability matrix construction

**Process:**
1. Auditor clones/reads the public OxAlto RoomBooking GitHub repository (reference only — this repository is not branched from or executed).
2. For each controller in scope, auditor documents every action, its permission gate(s), its request/response shape, and any embedded business rule (validation, side effect, email trigger).
3. For each model in scope, auditor documents every field, validation rule, association, and callback/lifecycle hook.
4. Auditor cross-references `config/routes.cfm` to confirm every controller action is reachable and to capture any route-level behavior (e.g., default actions, route constraints) not visible from the controller alone.
5. Auditor reviews CF application lifecycle event handlers for any global behavior affecting every request (e.g., session timeout, maintenance-mode interception).
6. Auditor consults `roombooking.readme.io` docs and the `roombooking.oxalto.co.uk` demo site to corroborate or disambiguate code-derived findings.
7. For any behavior that remains ambiguous after steps 2–6, auditor adds it to the Open Questions list with a precise description of what is unknown and why (per PRD Section 10 format).
8. Auditor compiles all findings into a structured functional spec document (the authoritative source this FRD's `[OPEN QUESTION]` flags will be resolved against) and hands off the baseline inventory to F13.
9. Product stakeholders review the Open Questions list; each question is either resolved via an explicit Key Decision (recorded in `PROJECT.md` Key Decisions table) or formally accepted as out-of-scope with sign-off, per PRD Section 7 success metric ("Zero open P0/P1 functional-parity gaps at release").
10. As each Open Question is resolved, the corresponding FRD chunk(s) in this document are updated to remove the `[OPEN QUESTION]` flag and restate the confirmed rule, and F13 adds a corresponding test.

**Inputs:**
- `legacy_repository_url` (string, required): Public GitHub URL of OxAlto RoomBooking.
- `legacy_docs_url` (string, required): `roombooking.readme.io`.
- `legacy_demo_url` (string, required): `roombooking.oxalto.co.uk`.
- `controller_list` (array of string, required): The 12 controllers named in PRD Section 5 F0.
- `model_list` (array of string, required): The 7 model groups named in PRD Section 5 F0.

**Outputs:**
- `legacy-audit-findings.md` (or equivalent structured document): one section per controller/model with cited findings.
- `open-questions.md`: numbered list matching PRD Section 10 format (at minimum the 10 seed questions, expanded as needed).
- Baseline inventory record consumed by F13 to seed the traceability matrix (one row per audit finding).

**Validation:**
- Every controller named in PRD Section 5 F0 has at least one corresponding audit-findings section before F0 is marked complete.
- Every model named in PRD Section 5 F0 has at least one corresponding audit-findings section before F0 is marked complete.
- Every PRD Section 10 seed Open Question (1–10) has either a cited resolution (with evidence reference) or remains logged as open with a clear "unknown because..." statement — none may be silently dropped.
- Every audit finding has a citation (source file/section/URL) traceable by a reviewer without re-doing the research.
- F0 is not marked complete (and no F1–F13 implementation work proceeds, per PRD priority "P0 — blocking") until the above three conditions hold.

**Error States:**
| Scenario | Impact | Required Action |
|---|---|---|
| A controller/model in scope has no corresponding audit section | F0 incomplete; downstream FRD chunks for related features remain speculative | Block sign-off; auditor must complete the missing section before F0 close-out |
| An Open Question is resolved by assumption rather than evidence or explicit Key Decision | Silent feature loss risk (violates PRD Section 6 "Ambiguity handling") | Reject the resolution; require either code/doc/demo citation or a recorded Key Decision with product sign-off |
| F0 findings document is produced but F13 traceability matrix seeding is skipped | "No functionality lost" becomes unverifiable (PRD Section 7 metric fails) | Block F13 kickoff; require baseline inventory handoff before any service implementation begins |

**API Surface (this feature):** None — F0 produces documentation artifacts, not a running service. No entry in `Y1-api.md`.

**Schema Surface (this feature):** None — F0 has no persisted runtime data. The baseline inventory it produces is consumed (not stored) by F13's traceability tooling, which may persist its own tracking schema (see `Y0-schema.md` if F13 requires a dedicated store; not required for F0 itself).
## F1: Booking Management Service

**Description:** The Booking Service is the core transactional service of Booking-Hub, owning the `Booking` entity (legacy `Event`) and its resource associations (legacy `Eventresource`). It handles create/edit/delete/clone operations, date/time validation, recurring-series management, multi-resource attachment, and the pending/approved/denied status workflow, and serves the calendar/day/list view read models and the AJAX-style event-detail retrieval equivalent to legacy `Eventdata`.

**Terminology:**
- **Booking (legacy `Event`):** A single reservation of a Location for a time range, optionally attached to one or more Resources and custom field values.
- **BookingResource (legacy `Eventresource`):** Join entity linking a Booking to a Resource.
- **Series:** A set of Bookings generated from a single recurring-booking definition, sharing a `series_id`.
- **Clone:** A new Booking created by copying an existing Booking's field values (excluding identity/status fields) as a starting point for a new submission.
- **Status workflow:** The lifecycle `pending → approved` or `pending → denied`, or direct `approved` on auto-approve; see F3 for transition rules.

**Sub-features:**
- Create booking (single or recurring series)
- Edit booking (single occurrence or whole series)
- Delete booking (single occurrence or whole series)
- Clone booking
- Multi-resource attachment per booking
- Calendar view, day view, list view (read models)
- AJAX event-detail/modal retrieval (legacy `Eventdata` equivalent)
- Default-duration and date/time validation

**Process:**
1. Client submits a booking creation request with title, location, start/end time, optional resource list, optional custom field values, and optional recurrence definition.
2. Service validates the request (see Validation).
3. If `end_time` is omitted, service defaults it to `start_time + 1 hour` (legacy default duration rule).
4. Service invokes Conflict Detection (F2) for the proposed location and each proposed resource over the proposed time range.
5. If recurrence is specified, service expands the recurrence definition into individual Booking occurrences sharing a `series_id`, applying steps 2–4 to each occurrence `[OPEN QUESTION — deferred to F0: exact recurrence pattern options (daily/weekly/monthly, end-date vs. occurrence-count) are not yet confirmed from legacy code; interim assumption is weekly-by-day-of-week with an end date, pending F0 confirmation]`.
6. Service determines initial status per F3 §Process (reads the `approveBooking` Settings flag via F10): `pending` if approval is required, `approved` if auto-approve is enabled.
7. Service persists the Booking (and BookingResource rows for each attached resource, and CustomFieldValue rows if applicable — see F5) and publishes a `booking.created` domain event (consumed by F8 Notifications and F11 Audit Logging).
8. Service returns the created Booking (or series) representation to the client, including any conflict warnings surfaced per F2.
9. On edit: client submits updated fields for an existing Booking (or series). If the Booking belongs to a series, client must specify scope (`this_occurrence` or `whole_series`) `[OPEN QUESTION — deferred to F0: whether legacy prompts for scope choice, defaults to one, or does not support series-wide edits at all — PRD Open Question #1]`. Service re-validates and re-runs Conflict Detection (F2) for the edited time/location/resources, then publishes a `booking.updated` domain event and records the change in the Audit Log (F11).
10. On delete: client requests deletion with the same scope parameter as edit (`this_occurrence` or `whole_series`) subject to the same `[OPEN QUESTION]` flag as step 9. Service soft- or hard-deletes per F0 confirmation (interim: soft-delete via a `deleted_at` timestamp to preserve audit trail) and publishes a `booking.deleted` domain event.
11. On clone: client requests a clone of an existing Booking by ID. Service copies all fields except `id`, `status` (reset to the default per step 6), `created_at`, `series_id` (clone is never part of the original series), and timestamps, and returns a new unsaved/draft representation (or directly persists per F0 confirmation of legacy clone behavior) for the client to adjust before submission.
12. Calendar/day/list views: service exposes read endpoints returning Bookings within a requested date range, filterable by location and/or resource, with conflict flags attached per F2.
13. Event-detail retrieval: service exposes a single-booking detail endpoint returning the full field set (including custom field values) for modal/detail-pane rendering, equivalent to legacy `Eventdata`.

**Inputs:**
- `title` (string, required): Booking title/subject.
- `location_id` (UUID/long, required): References a Location (F4).
- `start_time` (ISO-8601 datetime, required): Booking start.
- `end_time` (ISO-8601 datetime, optional): Booking end; defaults to `start_time + 1h` if omitted.
- `resource_ids` (array of UUID/long, optional): Zero or more Resources to attach (F4).
- `custom_field_values` (array of {field_id, value}, optional): Per F5.
- `recurrence` (object, optional): Recurrence definition (pattern, interval, end condition) — exact shape `[OPEN QUESTION — deferred to F0]`.
- `scope` (enum: `this_occurrence` | `whole_series`, required for edit/delete of a series Booking): Edit/delete scope.
- `source_booking_id` (UUID/long, required for clone): The Booking to clone from.

**Outputs:**
- Booking representation: `id`, `series_id` (nullable), `title`, `location`, `start_time`, `end_time`, `resources[]`, `custom_field_values[]`, `status`, `owner`, `created_at`, `updated_at`, `deleted_at` (nullable), `conflict_flags[]` (from F2).
- Calendar/day/list view responses: arrays of Booking representations scoped to the requested date range and filters.
- Event-detail response: full single-Booking representation equivalent to legacy `Eventdata` payload.

**Validation:**
- `title` must be non-empty.
- `location_id` must reference an existing, non-deleted Location (F4).
- `end_time` must not precede `start_time` (legacy rule, confirmed in PRD Section 5 F1).
- If `end_time` is omitted, it is set to `start_time + 1 hour` (legacy default duration rule, confirmed).
- Each entry in `resource_ids` must reference an existing, non-deleted Resource (F4).
- `recurrence`, if present, must define a determinate, finite set of occurrences (no open-ended series) `[OPEN QUESTION — deferred to F0: exact constraint unconfirmed]`.
- Edit/delete of a Booking that belongs to a series must include a valid `scope` value; omission is rejected `[OPEN QUESTION — deferred to F0: whether legacy actually requires explicit scope or has a silent default — PRD Open Question #1]`.
- Conflict Detection (F2) is invoked synchronously as part of create and edit validation, not as a post-hoc check (see F2 §Process).

**Error States:**
| Scenario | HTTP Status | Error Code | Message |
|---|---|---|---|
| Missing or empty `title` | 400 | BOOKING_TITLE_REQUIRED | "Booking title is required" |
| `end_time` precedes `start_time` | 400 | BOOKING_INVALID_TIME_RANGE | "End time must not be before start time" |
| `location_id` references a non-existent or deleted Location | 404 | BOOKING_LOCATION_NOT_FOUND | "Specified location does not exist" |
| One or more `resource_ids` reference a non-existent or deleted Resource | 404 | BOOKING_RESOURCE_NOT_FOUND | "One or more specified resources do not exist" |
| Edit/delete of series Booking without `scope` parameter | 400 | BOOKING_SCOPE_REQUIRED | "Scope (this_occurrence or whole_series) is required for recurring bookings" |
| Clone requested for non-existent `source_booking_id` | 404 | BOOKING_SOURCE_NOT_FOUND | "Booking to clone does not exist" |
| Create/edit blocked by hard-block conflict (see F2) | 409 | BOOKING_CONFLICT | "This booking conflicts with an existing booking for the selected location or resource" |

**API Surface (this feature):** see `Y1-api.md` §Booking for full request/response schemas (`POST /bookings`, `PUT /bookings/{id}`, `DELETE /bookings/{id}`, `POST /bookings/{id}/clone`, `GET /bookings`, `GET /bookings/{id}`).

**Schema Surface (this feature):** owns tables `bookings`, `booking_resources` — see `Y0-schema.md` §Booking. References `locations`/`resources` (F4, cross-service reference by ID only — no direct DB join, per service-isolation NFR) and `custom_field_values` (F5).
## F2: Conflict Detection

**Description:** Conflict Detection evaluates whether a proposed or edited Booking overlaps in time with another existing, non-deleted Booking for the same Location or the same Resource, and surfaces the result consistently across calendar view, list view, and the API, so double-booking is caught before (and re-checked after) a Booking is saved.

**Terminology:**
- **Overlap:** Two time ranges `[start_a, end_a)` and `[start_b, end_b)` overlap if `start_a < end_b AND start_b < end_a` (half-open interval comparison; adjacent bookings that touch exactly at the boundary, e.g. one ends at 10:00 and another starts at 10:00, do not overlap).
- **Location-level conflict:** An overlap between two Bookings that reference the same `location_id`.
- **Resource-level conflict:** An overlap between two Bookings where at least one attached `resource_id` is shared between them.
- **Hard block:** A conflict that prevents the Booking from being saved at all.
- **Soft warning:** A conflict that is surfaced to the user but does not prevent saving.

**Sub-features:**
- Per-location overlap check
- Per-resource overlap check
- Conflict surfacing in calendar view (visual indicator)
- Conflict surfacing in list view (flagged row)
- Re-evaluation on every edit, not only at creation

**Process:**
1. On Booking create or edit (F1 §Process steps 4 and 9), the Booking Service invokes Conflict Detection with the proposed `location_id`, `resource_ids[]`, `start_time`, `end_time`, and (for edits) the Booking's own `id` so the Booking is excluded from comparison against itself.
2. Conflict Detection queries all non-deleted Bookings sharing the same `location_id` whose time range overlaps the proposed range (per the Overlap definition above), excluding the Booking being edited.
3. Conflict Detection separately queries all non-deleted Bookings sharing at least one `resource_id` in common with the proposed `resource_ids[]`, whose time range overlaps the proposed range, excluding the Booking being edited.
4. If multiple resources are attached, each resource is checked independently; the conflict result for the whole Booking includes the union of all per-resource conflicts found `[OPEN QUESTION — deferred to F0: whether legacy evaluates multi-resource conflicts per-resource independently (interim assumption) or treats any single resource conflict as blocking the whole booking identically — PRD Open Question #3; the distinction matters only for how the result is communicated, not whether a conflict is detected]`.
5. Conflict Detection returns a structured result: a boolean `has_conflict`, and a list of `conflicts[]` each citing the conflicting Booking's `id`, the conflict type (`location` or `resource`), and the specific `location_id`/`resource_id` involved.
6. The calling Booking Service (F1) applies the enforcement policy: `[OPEN QUESTION — deferred to F0: whether a detected conflict is a hard block (save rejected, HTTP 409) or a soft warning (save allowed, conflict surfaced for information) and whether this differs by permission/role — PRD Open Question #2]`. Interim default pending F0 confirmation: **hard block for all roles** (the conservative choice that cannot silently allow a double-booking regression); this default must be revisited and explicitly confirmed/corrected once F0 resolves the question, with a corresponding F13 test added either way.
7. For calendar and list view read endpoints (F1 §Process step 12), Conflict Detection is invoked in bulk for all Bookings in the requested range so each returned Booking representation includes its `conflict_flags[]`, ensuring conflicts are visible even for already-saved Bookings (e.g., if a conflict was allowed under a soft-warning policy, or created via direct API access).
8. Conflict Detection logic is identical regardless of entry point (UI calendar, UI list, or direct API call) per the cross-cutting NFR "Conflict-detection correctness" — there is exactly one conflict-evaluation code path in the Booking Service, invoked by all three entry points.

**Inputs:**
- `location_id` (UUID/long, required): Proposed/existing Booking's location.
- `resource_ids` (array of UUID/long, optional): Proposed/existing Booking's attached resources.
- `start_time` (ISO-8601 datetime, required).
- `end_time` (ISO-8601 datetime, required).
- `exclude_booking_id` (UUID/long, optional): The Booking's own ID, supplied on edit to exclude self-comparison.

**Outputs:**
- `has_conflict` (boolean).
- `conflicts[]`: array of `{conflicting_booking_id, conflict_type: "location"|"resource", location_id | resource_id}`.

**Validation:**
- A Booking is never compared against itself (`exclude_booking_id` is always honored when present).
- Deleted Bookings (`deleted_at IS NOT NULL`) are never included in conflict comparison.
- Denied Bookings `[OPEN QUESTION — deferred to F0: does legacy exclude denied bookings from conflict checks, since they never occupy the slot? Interim assumption: denied bookings are excluded from conflict comparison]` are excluded from conflict comparison.
- Overlap is evaluated using half-open interval semantics (a booking ending at time T does not conflict with one starting at time T).
- Conflict re-evaluation occurs on every edit (not cached from creation time), per PRD Section 5 F2 capability "re-evaluated on every booking edit, not only at creation."

**Error States:**
| Scenario | HTTP Status | Error Code | Message |
|---|---|---|---|
| Hard-block conflict detected on create/edit (pending F0 confirmation of enforcement policy, interim default) | 409 | BOOKING_CONFLICT | "This booking conflicts with an existing booking for the selected location or resource" |
| Conflict check requested with invalid/missing `location_id` and empty `resource_ids` | 400 | CONFLICT_CHECK_INVALID_INPUT | "At least a location or one resource must be specified for conflict checking" |

**API Surface (this feature):** Conflict Detection is primarily an internal Booking Service function invoked during `POST /bookings` and `PUT /bookings/{id}` (see F1); it is also exposed as a standalone check endpoint — see `Y1-api.md` §Booking, `POST /bookings/check-conflicts`.

**Schema Surface (this feature):** No dedicated table; queries the `bookings` and `booking_resources` tables owned by the Booking Service — see `Y0-schema.md` §Booking. Conflict queries are indexed on `(location_id, start_time, end_time)` and `(resource_id, start_time, end_time)` for performance.
## F3: Approval Workflow

**Description:** The Approval Workflow governs how a Booking moves from `pending` to `approved` or `denied`, driven by the global `approveBooking` Settings flag (F10) and gated by the `allowApproveBooking` permission (F7). Approvals/denials trigger notifications (F8) and are recorded in the audit log (F11).

**Terminology:**
- **`approveBooking` flag:** Global Settings boolean (F10) determining whether new Bookings require explicit approval (`true`) or auto-approve on creation (`false`).
- **`allowApproveBooking` permission:** The Keycloak-mapped role/scope required to call approve/deny actions (F7).
- **Approver:** Any authenticated user holding `allowApproveBooking`.

**Sub-features:**
- Approve action on a pending Booking
- Deny action on a pending Booking
- Auto-approve on creation when `approveBooking` is disabled
- Domain event emission on approve/deny
- Audit logging of approve/deny actions

**Process:**
1. On Booking creation (F1 §Process step 6), the Booking Service reads the current value of the `approveBooking` flag from the Settings Service (F10).
2. If `approveBooking` is `true`, the new Booking's `status` is set to `pending`.
3. If `approveBooking` is `false`, the new Booking's `status` is set to `approved` directly (auto-approve); no separate approve action occurs.
4. For Bookings with `status = pending`, an authenticated user holding `allowApproveBooking` may call the approve action, specifying the `booking_id`.
5. Service verifies the caller holds `allowApproveBooking` (F7); if not, request is rejected (403).
6. Service verifies the target Booking's current `status` is `pending`; approving/denying a Booking that is not pending is rejected (409) — approve/deny are one-way transitions, not re-toggleable.
7. On approve: service sets `status = approved`, records `approved_by` (actor) and `approved_at` (timestamp), publishes a `booking.approved` domain event, and writes an audit log entry (F11).
8. On deny: service sets `status = denied`, records `denied_by` (actor), `denied_at` (timestamp), and optionally a `denial_reason` (free text), publishes a `booking.denied` domain event, and writes an audit log entry (F11).
9. The Notification Service (F8) consumes `booking.approved`/`booking.denied` events and sends notifications per F8 §Process; recipient rules are `[OPEN QUESTION — deferred to F0 — PRD Open Question #5]`.
10. For auto-approved Bookings (step 3), whether a `booking.approved` event is also published in addition to (or instead of) `booking.created` is `[OPEN QUESTION — deferred to F0: PRD Open Question #4 — auto-approve/notification interaction]`; interim default: only `booking.created` is published for auto-approved bookings (no separate `booking.approved` event), to avoid inventing a duplicate-notification behavior not confirmed in legacy.

**Inputs:**
- `booking_id` (UUID/long, required): The pending Booking to approve/deny.
- `denial_reason` (string, optional): Free-text reason supplied on deny.

**Outputs:**
- Updated Booking representation reflecting new `status`, `approved_by`/`approved_at` or `denied_by`/`denied_at`/`denial_reason`.
- Domain event payload: `{event_type, booking_id, actor, timestamp}` published to RabbitMQ.

**Validation:**
- Approve/deny is only permitted on a Booking whose current `status` is exactly `pending`.
- The caller must hold `allowApproveBooking` (F7) — verified server-side on every approve/deny call, not just hidden in the UI.
- `denial_reason`, if supplied, has no required format (free text) `[OPEN QUESTION — deferred to F0: whether legacy requires a reason on deny]`; interim: optional.
- The global `approveBooking` flag value is read at Booking-creation time only; changing the flag does not retroactively alter the status of already-created Bookings.

**Error States:**
| Scenario | HTTP Status | Error Code | Message |
|---|---|---|---|
| Caller lacks `allowApproveBooking` | 403 | APPROVAL_FORBIDDEN | "You do not have permission to approve or deny bookings" |
| Target Booking is not in `pending` status | 409 | APPROVAL_INVALID_STATE | "Only pending bookings can be approved or denied" |
| Target `booking_id` does not exist | 404 | APPROVAL_BOOKING_NOT_FOUND | "Booking not found" |
| Settings Service unavailable when reading `approveBooking` at creation time | 503 | APPROVAL_SETTINGS_UNAVAILABLE | "Unable to determine approval requirement; try again shortly" |

**API Surface (this feature):** see `Y1-api.md` §Booking (`POST /bookings/{id}/approve`, `POST /bookings/{id}/deny`).

**Schema Surface (this feature):** extends the `bookings` table (owned by Booking Service) with `status`, `approved_by`, `approved_at`, `denied_by`, `denied_at`, `denial_reason` columns — see `Y0-schema.md` §Booking. Reads (does not own) the `settings` table via the Settings Service API (F10) — no direct cross-service DB access, per service-isolation NFR.
## F4: Locations & Resources Management

**Description:** The Location & Resource Service owns CRUD management of bookable Locations (rooms/spaces, legacy `Location`) and bookable Resources (equipment, legacy `Resource`), the two reference entities that Bookings attach to. Both are admin-managed and consumed by the Booking Service (F1) for conflict-detection scoping (F2) and calendar rendering.

**Terminology:**
- **Location:** A bookable room/space with display metadata (name, CSS class/colour, building grouping, layout).
- **Resource:** Bookable equipment attachable to a Booking independently of Location (e.g., a projector, a specific chair count).
- **Building:** A grouping attribute on Location used for organizing locations in admin UI and potentially filtering.

**Sub-features:**
- Location CRUD (create/read/update/delete)
- Resource CRUD (create/read/update/delete)
- Location display metadata (CSS class/colour, building, layout)
- Admin-only access gating via permission (F7)

**Process:**
1. Admin user (holding the appropriate permission, F7) submits a Location creation request with `name`, `css_class`/`colour`, `building`, and optional `layout` metadata.
2. Service validates the request (see Validation) and persists the Location, returning its representation including a generated `id`.
3. Admin user submits a Resource creation request with `name` and any resource-specific metadata `[OPEN QUESTION — deferred to F0: full legacy Resource field set beyond name is unconfirmed]`.
4. Service validates and persists the Resource, returning its representation including a generated `id`.
5. Admin user may edit an existing Location or Resource; service re-validates and persists updates, recording the change in the Audit Log (F11).
6. Admin user may delete a Location or Resource. Service checks for existing non-deleted Bookings referencing the entity; if any exist, deletion behavior is `[OPEN QUESTION — deferred to F0: does legacy block deletion of a Location/Resource in use, cascade-delete dependent bookings, or allow orphaned references? Interim assumption: soft-delete only (deleted_at set), existing Booking references remain intact and display using the Location/Resource's last-known values]`.
7. The Booking Service (F1) and Conflict Detection (F2) reference Locations/Resources by `id` only, via a read API exposed by this service (no direct cross-service DB access, per service-isolation NFR).
8. Calendar rendering (F1 §Process step 12) uses a Location's `css_class`/`colour` metadata to render the Location consistently across calendar views.

**Inputs:**
- `name` (string, required): Location or Resource display name.
- `css_class` (string, optional): CSS class or colour token used for calendar rendering (Location only).
- `building` (string, optional): Building grouping (Location only).
- `layout` (object/string, optional): Layout metadata (Location only) `[OPEN QUESTION — deferred to F0: exact shape/purpose of legacy "layout" metadata unconfirmed]`.

**Outputs:**
- Location representation: `id`, `name`, `css_class`, `building`, `layout`, `created_at`, `updated_at`, `deleted_at` (nullable).
- Resource representation: `id`, `name`, `created_at`, `updated_at`, `deleted_at` (nullable).
- List endpoints return all non-deleted Locations/Resources (paginated or full list per admin UI needs).

**Validation:**
- `name` is required and non-empty for both Location and Resource.
- `name` must be unique among non-deleted Locations `[OPEN QUESTION — deferred to F0: uniqueness constraint unconfirmed]`; interim: not enforced beyond non-empty.
- Only users holding the admin permission equivalent to legacy Location/Resource management access (F7) may create/edit/delete.
- Deletion of a Location/Resource referenced by non-deleted Bookings follows the interim soft-delete policy in Process step 6, pending F0 confirmation.

**Error States:**
| Scenario | HTTP Status | Error Code | Message |
|---|---|---|---|
| Missing/empty `name` | 400 | LOCATION_NAME_REQUIRED / RESOURCE_NAME_REQUIRED | "Name is required" |
| Caller lacks admin permission | 403 | LOCATION_RESOURCE_FORBIDDEN | "You do not have permission to manage locations/resources" |
| Target Location/Resource `id` not found | 404 | LOCATION_NOT_FOUND / RESOURCE_NOT_FOUND | "Location/Resource not found" |
| Delete requested for entity referenced by active bookings (pending F0 policy confirmation) | 409 (interim, if block policy confirmed) | LOCATION_IN_USE / RESOURCE_IN_USE | "Cannot delete: referenced by existing bookings" |

**API Surface (this feature):** see `Y1-api.md` §Locations and §Resources (`GET/POST /locations`, `GET/PUT/DELETE /locations/{id}`, `GET/POST /resources`, `GET/PUT/DELETE /resources/{id}`).

**Schema Surface (this feature):** owns tables `locations`, `resources` — see `Y0-schema.md` §LocationResource. Referenced by the Booking Service's `bookings.location_id` and `booking_resources.resource_id` via ID reference only (no FK across service database boundaries).
## F5: Custom Fields

**Description:** The Custom Field Service lets admins define custom fields (equivalent to legacy `Customfield`), organize them into templates/field-pickers, attach them to Bookings via a join entity (legacy `Customfieldjoin`), and store per-booking values (legacy `Customfieldvalue`). The Booking creation/edit form renders applicable fields dynamically based on field-picker configuration.

**Terminology:**
- **Custom Field:** An admin-defined field definition (label, type, options) that can be attached to Bookings.
- **Field Template / Field-Picker:** A named grouping/configuration of custom fields that determines which fields render on a given Booking form context.
- **CustomFieldJoin:** The association between a Custom Field and a context (e.g., a Location, or globally) determining where it is offered.
- **CustomFieldValue:** The actual value a user entered for a given Custom Field on a specific Booking.

**Sub-features:**
- Admin management of custom field definitions
- Admin management of field templates
- Attachment of custom fields to bookings via field-picker configuration
- Dynamic rendering of applicable fields on booking create/edit form
- Display of custom field values in calendar/list/detail views and public feeds

**Process:**
1. Admin user (holding the admin permission, F7) creates a Custom Field definition with `label`, `field_type` `[OPEN QUESTION — deferred to F0: exact supported types — free-text, numeric, date, dropdown/select — are unconfirmed; PRD Open Question #6]`, and, for choice-based types, an `options[]` list.
2. Admin user creates or edits a Field Template, associating one or more Custom Fields with it via a CustomFieldJoin, and associates the Template with a context (e.g., a specific Location, or "all bookings") `[OPEN QUESTION — deferred to F0: exact join context/scope model unconfirmed]`.
3. When a user opens the Booking creation/edit form for a given Location, the Booking Service (F1) queries the Custom Field Service for the applicable Field Template/fields for that context and renders them dynamically alongside the standard fields.
4. User enters values for each rendered Custom Field; on Booking submission, these are sent as `custom_field_values[]` (F1 §Inputs) and persisted as CustomFieldValue rows linked to the Booking.
5. Service validates each submitted value against its Custom Field's `field_type` and any required/optional flag `[OPEN QUESTION — deferred to F0: whether legacy enforces required/optional and type-specific validation, or treats all custom fields as free-text/optional — PRD Open Question #6]`; interim default: all custom fields are optional, free-text-validated only (no type coercion enforced), pending F0 confirmation.
6. Custom field values are included in the Booking representation returned by calendar/list/detail views (F1) and, where applicable, in Public Feed payloads (F9) `[OPEN QUESTION — deferred to F0: which custom fields, if any, are exposed in public feeds vs. internal-only — unconfirmed]`.
7. Admin edits/deletes a Custom Field definition or Field Template; existing CustomFieldValue rows for past Bookings are retained even if the definition is later deleted (historical values are not purged) `[OPEN QUESTION — deferred to F0: confirm retention policy]`.

**Inputs:**
- `label` (string, required): Custom Field display label.
- `field_type` (enum, required): e.g., `text`, `number`, `date`, `select` — exact enum pending F0 confirmation.
- `options` (array of string, required for `select` type): Choice list.
- `template_id` (UUID/long, optional): Field Template this field belongs to.
- `context_id` (UUID/long, optional): Location or other context the Field Template applies to.
- `custom_field_values` (array of {field_id, value}, submitted with Booking — see F1 §Inputs).

**Outputs:**
- Custom Field representation: `id`, `label`, `field_type`, `options[]`, `created_at`, `updated_at`.
- Field Template representation: `id`, `name`, `field_ids[]` (via CustomFieldJoin), `context_id`.
- CustomFieldValue representation (embedded in Booking payload): `{field_id, label, field_type, value}`.

**Validation:**
- `label` is required and non-empty for a Custom Field.
- `options[]` is required and non-empty when `field_type = select` (or equivalent choice type).
- A submitted `custom_field_values[]` entry must reference a `field_id` that is applicable (via Field Template/CustomFieldJoin) to the Booking's Location context; values for inapplicable fields are rejected `[OPEN QUESTION — deferred to F0: whether legacy enforces this strictly or accepts any submitted value]`.
- Required/optional enforcement and type-specific validation (numeric/date format) are `[OPEN QUESTION — deferred to F0 — PRD Open Question #6]`; interim: not enforced beyond field existence.
- Only users holding the admin permission (F7) may create/edit/delete Custom Field definitions and Field Templates.

**Error States:**
| Scenario | HTTP Status | Error Code | Message |
|---|---|---|---|
| Missing/empty `label` on Custom Field creation | 400 | CUSTOM_FIELD_LABEL_REQUIRED | "Custom field label is required" |
| `field_type = select` with empty `options[]` | 400 | CUSTOM_FIELD_OPTIONS_REQUIRED | "Options are required for selection-type fields" |
| Caller lacks admin permission | 403 | CUSTOM_FIELD_FORBIDDEN | "You do not have permission to manage custom fields" |
| Submitted `custom_field_values[]` references unknown `field_id` | 400 | CUSTOM_FIELD_VALUE_INVALID | "One or more submitted custom field values are invalid" |
| Target Custom Field or Template `id` not found | 404 | CUSTOM_FIELD_NOT_FOUND | "Custom field or template not found" |

**API Surface (this feature):** see `Y1-api.md` §CustomFields (`GET/POST /custom-fields`, `GET/PUT/DELETE /custom-fields/{id}`, `GET/POST /field-templates`, `GET/PUT/DELETE /field-templates/{id}`).

**Schema Surface (this feature):** owns tables `custom_fields`, `custom_field_templates`, `custom_field_joins`, `custom_field_values` — see `Y0-schema.md` §CustomField. `custom_field_values.booking_id` references the Booking Service's `bookings.id` by ID only (cross-service reference, no DB-level FK).
## F6: User & Role Management

**Description:** User & Role Management covers account creation/administration, self-service profile/password management, password reset, and login/logout/session behavior — equivalent to legacy `Users`, `Sessions`, and `PasswordResets` controllers — re-platformed onto Keycloak so that authentication, credential storage, and session/token management are standards-based rather than hand-rolled, while preserving equivalent user-facing capability.

**Terminology:**
- **Keycloak realm:** The identity domain hosting Booking-Hub user accounts, credentials, and roles, replacing legacy `User.password`/salt storage.
- **Profile-adjacent data:** Non-credential user data (display name, contact email for notifications, etc.) that may be owned by a lightweight User/Identity-adjacent Service rather than Keycloak itself, if Keycloak's user-attribute model is insufficient `[to be confirmed in TechArch]`.
- **Access token / Refresh token:** Keycloak-issued JWT tokens used by the client and validated at the Gateway and per-service.
- **Remember me:** Extended session/token lifetime behavior equivalent to legacy "remember me" checkbox.

**Sub-features:**
- Admin-facing account creation/management
- Self-service "my account" profile editing
- Self-service password change
- Password reset flow (request → emailed token/link → reset)
- Login/logout and "remember me" session behavior
- Access-denied handling
- Role assignment per user (feeding F7)

**Process:**
1. Admin user (holding the appropriate permission, F7) creates a new user account by submitting `email`/`username` and initial role assignment; service provisions the account in the Keycloak realm (via Keycloak Admin API) rather than storing a password hash locally.
2. New user receives an invitation/initial-credential mechanism equivalent to legacy account creation `[OPEN QUESTION — deferred to F0: does legacy auto-generate a password, send an invite link, or require admin-set initial password? Unconfirmed]`.
3. User logs in by submitting credentials; the client authenticates against Keycloak directly (or via a backend-for-frontend flow) and receives an access token and refresh token.
4. If the user selects "remember me", the issued refresh token (or session) has an extended lifetime equivalent to the legacy "remember me" duration `[OPEN QUESTION — deferred to F0: exact legacy remember-me duration unconfirmed]`.
5. User logs out; client discards tokens and/or the service revokes the Keycloak session/refresh token.
6. User accesses "my account" and edits profile-adjacent fields (e.g., display name); service persists changes either to Keycloak user attributes or a dedicated profile-adjacent store, publishing a profile-updated event if other services need to react.
7. User requests a password change (while authenticated) by submitting current and new password; request is delegated to Keycloak's credential-update mechanism, replacing legacy salted/hashed password validation and storage.
8. User requests a password reset (unauthenticated) by submitting their email/username; service triggers Keycloak's reset-credential flow (or a custom flow backed by Keycloak), publishing a `password.reset.requested` domain event consumed by the Notification Service (F8) to send the reset link/token email.
9. User follows the reset link/token and submits a new password; Keycloak validates the token and updates the credential.
10. Any request to a protected resource by an unauthenticated or insufficiently-privileged user triggers access-denied handling equivalent to legacy `Sessions` access-denied behavior (redirect to login, or 401/403 response for API calls).
11. Admin assigns/changes a user's role(s); this updates the user's Keycloak role mappings, which F7's Permission System reads to gate subsequent actions.

**Inputs:**
- `email` / `username` (string, required): Account identifier.
- `initial_role` (string, required for admin-created accounts): Role to assign at creation.
- `current_password` (string, required for authenticated password change).
- `new_password` (string, required for password change/reset).
- `remember_me` (boolean, optional, default `false`): Extended session flag at login.
- `reset_token` (string, required for completing a password reset): Token from the emailed reset link.

**Outputs:**
- User representation: `id`, `email`/`username`, `display_name`, `roles[]`, `created_at`, `updated_at`.
- Login response: access token, refresh token, token expiry, assigned roles/scopes.
- Password reset request response: generic success acknowledgment (does not reveal whether the email/username exists, to avoid user enumeration — standard security practice; `[confirm legacy parity in F0]`).

**Validation:**
- `email`/`username` is required and must be unique among active accounts.
- `new_password` must meet Keycloak's configured password policy (replacing legacy hand-rolled complexity rules) — the policy's effective strictness should be configured to be no weaker than the legacy complexity rules `[OPEN QUESTION — deferred to F0: legacy password complexity rules unconfirmed]`.
- `reset_token` must be valid (unexpired, unused) at the time of reset completion; expired/used tokens are rejected.
- Only users holding the admin permission (F7) may create accounts or assign roles to other users; a user may always edit their own profile and change their own password.

**Error States:**
| Scenario | HTTP Status | Error Code | Message |
|---|---|---|---|
| Login with invalid credentials | 401 | AUTH_INVALID_CREDENTIALS | "Invalid username or password" |
| Access to protected resource without valid token | 401 | AUTH_UNAUTHENTICATED | "Authentication required" |
| Access to protected resource with valid token but insufficient role | 403 | AUTH_FORBIDDEN | "You do not have permission to perform this action" |
| Account creation with duplicate `email`/`username` | 409 | USER_ALREADY_EXISTS | "An account with this email/username already exists" |
| Password reset completion with expired/invalid/used token | 400 | PASSWORD_RESET_TOKEN_INVALID | "This password reset link is invalid or has expired" |
| Password change with incorrect `current_password` | 401 | PASSWORD_CHANGE_INVALID_CURRENT | "Current password is incorrect" |
| New password fails Keycloak policy | 400 | PASSWORD_POLICY_VIOLATION | "New password does not meet complexity requirements" |

**API Surface (this feature):** see `Y1-api.md` §Auth and §Users (`POST /auth/login`, `POST /auth/logout`, `POST /auth/password-reset/request`, `POST /auth/password-reset/complete`, `GET/POST /users`, `GET/PUT /users/{id}`, `PUT /users/{id}/roles`, `GET/PUT /users/me`).

**Schema Surface (this feature):** credentials and core identity are owned by Keycloak's own realm database (not a Booking-Hub-owned schema). Any profile-adjacent data not modeled in Keycloak is owned by a dedicated `users` table in a User/Identity-adjacent Service — see `Y0-schema.md` §User. No service stores password hashes directly, per the "Standards-based identity" NFR.
## F7: Permission System

**Description:** The Permission System re-implements the legacy fine-grained permission matrix as Keycloak roles/scopes, enforced both coarsely at the Spring Cloud Gateway (route-level access) and finely within individual services (action-level checks), preserving the same effective access-control behavior users experience in the legacy system while replacing hand-rolled permission storage.

**Terminology:**
- **Permission flag:** A named legacy access gate (e.g., `accessCalendar`, `allowRoomBooking`, `viewRoomBooking`, `allowApproveBooking`, `accessPermissions`, `allowAPI`). This set is explicitly non-exhaustive per PRD Section 5 F7 and PRD Open Question #9.
- **Keycloak role/scope:** The new-system equivalent of a permission flag, issued as a claim in the user's access token.
- **Coarse-grained enforcement:** Route-level allow/deny at the Gateway based on token role/scope presence, before a request reaches a backend service.
- **Fine-grained enforcement:** Action-level checks within a service (e.g., "can this user approve this specific booking") that may require more context than the Gateway has.
- **Permission mapping table:** The explicit 1:1 (or documented N:1) mapping from every legacy permission flag to its Keycloak role/scope equivalent, a required F0/F7 output.

**Sub-features:**
- Permission matrix definition (legacy flags → Keycloak roles/scopes)
- Role-to-permission mapping administration
- Gateway-level coarse-grained enforcement
- Service-level fine-grained enforcement
- Full legacy-to-Keycloak permission mapping table (F0 audit output)

**Process:**
1. F0 audit produces the complete, confirmed list of legacy permission flags and the exact controller action(s) each one gates (PRD Open Question #9) — this FRD lists the known non-exhaustive seed set (`accessCalendar`, `allowRoomBooking`, `viewRoomBooking`, `allowApproveBooking`, `accessPermissions`, `allowAPI`) pending that confirmation.
2. Each confirmed legacy permission flag is mapped 1:1 to a Keycloak client role or scope, recorded in the permission mapping table; any flag whose gating scope cannot be confirmed is flagged `[OPEN QUESTION — deferred to F0]` and not implemented until resolved, rather than approximated.
3. Admin user (holding `accessPermissions` or its Keycloak equivalent) manages role-to-permission mappings via an admin screen equivalent to legacy `Permissions` controller — assigning which roles carry which permission flags.
4. Spring Cloud Gateway is configured with route-to-role/scope rules so that requests lacking the required role/scope for a route are rejected at the edge (401/403) before reaching a backend service.
5. Each backend service additionally performs fine-grained checks for actions where Gateway-level routing alone is insufficient (e.g., `allowApproveBooking` gates the specific approve/deny action within the Booking Service, not just a route prefix; a user might have access to `/bookings` generally but not to the approve sub-action).
6. Every feature chunk in this FRD (F1, F3, F4, F5, F6, F9, F10, F11) that gates an action by permission references this feature for the enforcement mechanism and cites the specific legacy permission flag involved.
7. Changes to a user's role (F6 §Process step 11) take effect on the user's next issued token (immediate for new logins; existing tokens remain valid until natural expiry or explicit revocation, per standard JWT semantics) `[confirm whether legacy had equivalent immediate-vs-next-session semantics in F0]`.

**Inputs:**
- `role_name` (string, required): Role being configured.
- `permission_flags` (array of string, required): Set of permission flags assigned to the role.

**Outputs:**
- Permission mapping table representation: `{legacy_flag, keycloak_role_or_scope, gated_actions[], confirmed: boolean}`.
- Role representation: `{role_name, permission_flags[]}`.

**Validation:**
- Every permission flag referenced by any other feature chunk in this FRD must appear in the permission mapping table before that feature's permission-gated action is implemented.
- A permission flag whose gating scope is marked unconfirmed (`confirmed: false`) must not be silently treated as "no restriction" — the safe interim default is to treat it as restrictive (deny by default) until F0 confirms otherwise, per PRD's ambiguity-handling NFR.
- Only users holding `accessPermissions` (or its confirmed Keycloak equivalent) may modify role-to-permission mappings.
- Gateway-level and service-level enforcement must agree: a service must never grant an action that the Gateway would have blocked for the same role, and a service's fine-grained check must never be weaker than what the Gateway's coarse-grained check implies.

**Error States:**
| Scenario | HTTP Status | Error Code | Message |
|---|---|---|---|
| Request to a Gateway-protected route without required role/scope | 401 or 403 | GATEWAY_FORBIDDEN | "Access denied for this resource" |
| Service-level fine-grained check fails despite passing Gateway routing | 403 | ACTION_FORBIDDEN | "You do not have permission to perform this action" |
| Attempt to modify role-permission mapping without `accessPermissions` | 403 | PERMISSIONS_FORBIDDEN | "You do not have permission to manage permissions" |
| Reference to an unconfirmed/undefined permission flag in a mapping update | 400 | PERMISSION_FLAG_UNDEFINED | "Unknown or unconfirmed permission flag" |

**API Surface (this feature):** see `Y1-api.md` §Permissions (`GET/PUT /roles/{role}/permissions`, `GET /permissions`). Enforcement itself is cross-cutting (Gateway filter + per-service interceptor/annotation), not a single feature-owned endpoint set.

**Schema Surface (this feature):** role definitions and role-to-permission mappings are primarily modeled as Keycloak realm roles/client scopes (owned by Keycloak, not a Booking-Hub service database). If a supplementary mapping-metadata table is needed (e.g., to store the legacy-flag-to-Keycloak-role mapping table itself for admin UI display), it is owned by a dedicated `permissions` table — see `Y0-schema.md` §Permission.
## F8: Notifications

**Description:** The Notification Service consumes RabbitMQ domain events (`booking.created`, `booking.approved`, `booking.denied`, `password.reset.requested`) and sends email notifications equivalent to legacy synchronous email-sending, but with durable, retryable delivery so a notification failure can never block or silently drop the originating transaction.

**Terminology:**
- **Domain event:** A RabbitMQ message published by an upstream service (Booking Service for booking events, User/Identity Service for password-reset events) describing a state change.
- **Notification template:** The content/structure of an email for a given event type, equivalent in content/intent to legacy email templates.
- **Dead-letter queue (DLQ):** A queue receiving messages that fail delivery/processing after retry exhaustion, for manual inspection rather than silent loss.
- **Idempotency key:** A unique identifier on each event preventing duplicate notification delivery on redelivery/retry.

**Sub-features:**
- Consumption of `booking.created` events
- Consumption of `booking.approved` events
- Consumption of `booking.denied` events
- Consumption of `password.reset.requested` events
- Durable, retryable delivery via RabbitMQ
- Notification templates per event type

**Process:**
1. Upstream service (Booking Service for booking lifecycle events, F1/F3; User/Identity Service for password reset, F6) publishes a domain event to RabbitMQ with an idempotency key, event type, relevant entity ID(s), and actor context.
2. Notification Service consumes the event from its durable queue.
3. Service determines the recipient set for the event type: `[OPEN QUESTION — deferred to F0: exact recipient rules per event type — booking owner only, owner + all approvers, owner + location-specific approvers, or configurable per-location — PRD Open Question #5]`; interim default pending F0 confirmation: booking owner only for `booking.created`/`booking.approved`/`booking.denied`, and the requesting user only for `password.reset.requested`.
4. Service renders the appropriate notification template for the event type, substituting entity-specific data (booking title, time, location; or reset link/token).
5. Service sends the email via the configured SMTP/email provider integration (see `Y3-integrations.md`).
6. On send success, service acknowledges the message (removing it from the queue).
7. On send failure (transient, e.g., provider timeout), service retries delivery according to a backoff policy up to a configured maximum retry count.
8. On retry exhaustion, the message is routed to the dead-letter queue for manual inspection rather than being silently dropped, satisfying the "Notification delivery reliability" success metric (PRD Section 7: 100% of events result in delivered or retried-to-success notification, with no silent drops).
9. For auto-approved bookings (F3 §Process step 10), whether `booking.created` alone is published or `booking.approved` is also published is governed by F3's interim rule, and the Notification Service's behavior follows whatever event(s) it actually receives — it does not infer auto-approve status independently.
10. Redelivery of an already-processed event (e.g., after a consumer crash before acknowledgment) is detected via the idempotency key, and the service does not send a duplicate email for an event it has already successfully processed.

**Inputs:**
- Domain event payload: `{event_type, idempotency_key, entity_id, actor, timestamp, event_specific_data}` (e.g., for `booking.created`: booking title/time/location/owner; for `password.reset.requested`: user email, reset token/link).

**Outputs:**
- Sent email (subject, body, recipient(s)) per notification template.
- Delivery status record: `{event_id, status: sent|retrying|dead_lettered, attempt_count, last_attempted_at}` for observability.

**Validation:**
- Every consumed event must include a non-empty `idempotency_key`; events missing one are routed to the DLQ immediately rather than risking duplicate sends.
- An event already marked `sent` for a given `idempotency_key` is never re-sent, even if redelivered by RabbitMQ.
- Recipient resolution must not silently fail open (send to nobody) or silently fail closed (swallow the event) — an unresolvable recipient is treated as a processing failure and follows the retry/DLQ path (step 7–8).
- Notification template rendering failures (e.g., missing substitution data) are treated as processing failures, not swallowed.

**Error States:**
| Scenario | Impact | Required Action |
|---|---|---|
| SMTP/email provider transient failure | Delivery delayed | Retry per backoff policy (step 7) |
| Retry count exhausted | Delivery not confirmed | Route to DLQ (step 8); surfaced for manual/alerted inspection, never silently dropped |
| Event missing idempotency key | Cannot safely dedupe | Route to DLQ immediately; log for investigation |
| Recipient address cannot be resolved (e.g., deleted user) | Cannot deliver | Route to DLQ; does not block originating transaction (booking creation already succeeded) |
| Duplicate event delivery (RabbitMQ at-least-once redelivery) | Risk of duplicate email | Detected and suppressed via idempotency key (no customer-visible error) |

**API Surface (this feature):** No direct client-facing REST API for sending notifications (event-driven only). An internal admin/observability endpoint for inspecting delivery status/DLQ contents is listed in `Y1-api.md` §Notifications (`GET /notifications/delivery-status`, `GET /notifications/dead-letter`).

**Schema Surface (this feature):** owns table `notification_deliveries` (tracking idempotency keys, status, attempt counts) — see `Y0-schema.md` §Notification. Does not own `bookings` or `users` data; consumes event payloads only, per service-isolation NFR.
## F9: Public Feeds

**Description:** The Public Feed Service exposes read-only, read-optimized views of approved upcoming Bookings in multiple formats — RSS2, iCal, JSON, and a digital-signage "display board" view — equivalent to legacy `Api` controller, with optional per-location filtering and access control equivalent to legacy `allowAPI`.

**Terminology:**
- **Approved upcoming booking:** A Booking with `status = approved` and `start_time` (or `end_time`) in the future relative to the feed request time.
- **Display board:** An auto-refreshing, screen-friendly HTML/visual view intended for lobby/corridor digital signage, not a machine-readable feed format.
- **`allowAPI` permission:** The legacy permission flag gating non-public feed access, re-implemented via Keycloak (F7).

**Sub-features:**
- RSS2 feed
- iCal (subscribable calendar) feed
- JSON/API feed
- Digital-signage display board view
- Per-location filtering on all feed formats
- Feed access control via `allowAPI`

**Process:**
1. Client requests a feed in a given format (RSS2, iCal, JSON, or display board), optionally with a `location_id` filter query parameter.
2. Service determines access control for the request: `[OPEN QUESTION — deferred to F0: are feeds fully public by default, or always gated behind allowAPI/a token? — PRD Open Question #7]`; interim default pending F0 confirmation: feeds require a valid `allowAPI`-scoped token or a feed-specific access token, consistent with the conservative default-deny posture used elsewhere in this FRD for unconfirmed access rules.
3. If access is granted, service queries the Booking Service's read model (or a denormalized feed-optimized view, per PRD Risk mitigation on cross-domain read patterns) for all Bookings matching `status = approved` and `start_time >= now` (or configured lookback/lookahead window), filtered by `location_id` if supplied.
4. Service renders the result in the requested format:
   - **RSS2:** standard RSS 2.0 XML with one `<item>` per Booking (title, location, time, link).
   - **iCal:** standard iCalendar (`.ics`) format with one `VEVENT` per Booking, subscribable by calendar clients.
   - **JSON:** structured JSON array of Booking summaries.
   - **Display board:** server-rendered HTML view styled for signage display, auto-refreshing client-side (e.g., via meta-refresh or polling) to stay current.
5. Per-location filtering applies uniformly across all four formats `[OPEN QUESTION — deferred to F0: confirm uniform applicability — PRD Open Question #7]`.
6. Custom field values are included in feed output only if confirmed exposable per F5 §Process step 6 (pending F0 confirmation); otherwise feeds expose only standard Booking fields (title, location, time).
7. Feed responses are cacheable (e.g., with appropriate `Cache-Control`/`ETag` headers) given their read-heavy, publicly-consumed nature, without compromising the access-control check in step 2.

**Inputs:**
- `format` (enum, required, typically via route/path or `Accept` header): `rss2` | `ical` | `json` | `display-board`.
- `location_id` (UUID/long, optional): Filter to a single Location.
- `access_token` (string, required if feeds are gated per step 2 interim default): Credential proving `allowAPI` access.

**Outputs:**
- RSS2 XML document.
- iCal `.ics` document.
- JSON array: `[{booking_id, title, location, start_time, end_time}, ...]`.
- Display board HTML page.

**Validation:**
- Only Bookings with `status = approved` ever appear in any feed format — `pending` and `denied` Bookings are never exposed, regardless of access level (no role sees unapproved bookings via the public feed surface; internal views use F1's endpoints instead).
- `location_id`, if supplied, must reference an existing Location; an unknown `location_id` returns an empty feed (not an error) to avoid leaking location-existence information differently across feed vs. internal endpoints `[confirm this parity choice against legacy behavior in F0]`.
- Feed access control (step 2) is evaluated identically regardless of format — a caller denied JSON access is equally denied RSS2/iCal/display-board access for the same scope, per the single-conflict-logic-path principle applied analogously here.

**Error States:**
| Scenario | HTTP Status | Error Code | Message |
|---|---|---|---|
| Feed requested without required `allowAPI` access (pending F0 confirmation of default-public vs. gated) | 401 or 403 | FEED_FORBIDDEN | "Access to this feed requires API access" |
| Unsupported `format` requested | 400 | FEED_FORMAT_UNSUPPORTED | "Requested feed format is not supported" |
| `location_id` filter references a non-existent Location | 200 (empty feed, per Validation) | — | — |
| Upstream Booking read model unavailable | 503 | FEED_SOURCE_UNAVAILABLE | "Unable to retrieve booking feed at this time" |

**API Surface (this feature):** see `Y1-api.md` §Feeds (`GET /feeds/rss2`, `GET /feeds/ical`, `GET /feeds/json`, `GET /feeds/display-board`).

**Schema Surface (this feature):** no independently owned transactional table; reads a denormalized/read-optimized view sourced from the Booking Service's `bookings` data (via API composition or an event-driven read-model projection, per PRD's cross-domain read-pattern mitigation) — see `Y0-schema.md` §Feed for the projected read-model shape, if materialized.
## F10: Settings

**Description:** The Settings Service owns system-wide configuration equivalent to the legacy `Settings` singleton — principally the `approveBooking` flag governing the approval workflow (F3) and calendar display parameters consumed by the Booking Service's calendar view (F1).

**Terminology:**
- **Settings singleton:** A single, system-wide configuration record (not per-user, not per-location) equivalent to the legacy `Settings` model.
- **`approveBooking` flag:** Boolean controlling whether new bookings require explicit approval (F3).
- **Calendar display parameters:** Slot size, minimum time, maximum time, and other FullCalendar.js-equivalent rendering configuration.

**Sub-features:**
- `approveBooking` flag read/write
- Calendar display configuration (slot size, min time, max time, etc.)
- Admin-only access to modify settings
- Propagation of settings changes to dependent services

**Process:**
1. Admin user (holding the admin permission, F7) reads the current Settings singleton via a `GET /settings` call.
2. Admin user updates one or more Settings fields (e.g., toggles `approveBooking`, adjusts calendar slot size) via a `PUT /settings` call.
3. Service validates the update (see Validation) and persists the new Settings singleton state, recording the change in the Audit Log (F11).
4. Dependent services (notably the Booking Service, F1/F3, which reads `approveBooking` at Booking-creation time) obtain the updated value either by direct synchronous read of the Settings Service API, or via a `settings.changed` broadcast event — exact propagation mechanism `[to be determined in TechArch, informed by F0 findings on how legacy settings are read/cached, per PRD Section 5 F10]`.
5. Changes to `approveBooking` affect only Bookings created after the change; already-created Bookings retain the status they were assigned at creation time (F3 §Validation).
6. Changes to calendar display parameters affect how the Booking Service's calendar view (F1 §Process step 12) renders on next client fetch; no retroactive effect on stored Booking data.
7. Per-environment settings behavior (development/production/testing/design/maintenance) is audited by F0; any functional difference beyond configuration values (e.g., maintenance-mode read-only lockout) is `[OPEN QUESTION — deferred to F0 — PRD Open Question #10]` and not implemented until confirmed.

**Inputs:**
- `approveBooking` (boolean, optional on update): Approval requirement toggle.
- `calendar_slot_size` (integer, minutes, optional): Calendar slot granularity.
- `calendar_min_time` (time, optional): Earliest time shown on calendar.
- `calendar_max_time` (time, optional): Latest time shown on calendar.

**Outputs:**
- Settings representation: `{approveBooking, calendar_slot_size, calendar_min_time, calendar_max_time, updated_at, updated_by}`.

**Validation:**
- Only one Settings record exists system-wide (singleton); update operations modify the existing record rather than creating new ones.
- `calendar_min_time` must precede `calendar_max_time`.
- `calendar_slot_size` must be a positive integer.
- Only users holding the admin permission (F7) may modify Settings; read access may be broader (e.g., any authenticated user needs to read calendar display parameters to render the calendar) `[confirm legacy read-access scope in F0]`.
- A maintenance-mode or other per-environment functional difference is never implemented based on assumption; if F0 confirms such behavior exists, it is added as an explicit new rule here with its own validation/error states, not inferred from configuration naming alone.

**Error States:**
| Scenario | HTTP Status | Error Code | Message |
|---|---|---|---|
| Caller lacks admin permission for Settings update | 403 | SETTINGS_FORBIDDEN | "You do not have permission to modify settings" |
| `calendar_min_time` not before `calendar_max_time` | 400 | SETTINGS_INVALID_CALENDAR_RANGE | "Calendar minimum time must be before maximum time" |
| `calendar_slot_size` non-positive | 400 | SETTINGS_INVALID_SLOT_SIZE | "Calendar slot size must be a positive number of minutes" |
| Settings Service unavailable when a dependent service reads `approveBooking` | 503 | SETTINGS_UNAVAILABLE | "Settings currently unavailable" |

**API Surface (this feature):** see `Y1-api.md` §Settings (`GET /settings`, `PUT /settings`).

**Schema Surface (this feature):** owns table `settings` (single-row singleton table) — see `Y0-schema.md` §Settings.
## F11: Activity/Audit Logging

**Description:** The Audit Log Service provides a system-wide, searchable record of state-changing actions across every other service — equivalent to legacy `Logfiles` — implemented as an event-sourced consumer of domain events (not direct per-service writes to a shared log table), consistent with the database-per-service constraint.

**Terminology:**
- **Audit log entry:** A single recorded fact: who (actor) did what (action type) to what (entity type/id) when (timestamp), optionally with before/after values.
- **Event-sourced audit trail:** The Audit Log Service builds its log exclusively by consuming domain events published by other services, never by direct database access to those services.

**Sub-features:**
- Capture of booking lifecycle events (create/edit/delete/approve/deny)
- Capture of location/resource management changes
- Capture of user/role changes
- Capture of settings changes
- Capture of permission/mapping changes
- Admin-facing log viewing with filtering

**Process:**
1. Every state-changing action in every other feature (F1, F3, F4, F5, F6, F7, F10) publishes a domain event describing the change (e.g., `booking.created`, `booking.updated`, `booking.deleted`, `booking.approved`, `booking.denied`, `location.created`, `location.updated`, `location.deleted`, `resource.created/updated/deleted`, `user.created/updated`, `role.assigned`, `settings.updated`, `permission.updated`).
2. The Audit Log Service consumes each event and writes a corresponding audit log entry: `{actor, timestamp, entity_type, entity_id, action_type, before_values, after_values}`.
3. Whether every domain event includes before/after values, or only the fact that a change occurred, is `[OPEN QUESTION — deferred to F0: does legacy Logfiles capture before/after values for edits, or only the fact of the edit? Is every controller action logged, or only a subset? — PRD Open Question #8]`; interim default pending F0 confirmation: capture both before and after values wherever the triggering event payload includes them (the more complete, conservative choice — omitting data is easier to retrofit than recovering data never captured).
4. Admin user (holding appropriate permission) views the audit log via a filterable list endpoint equivalent to legacy `Logfiles` controller, filterable by `entity_type`, `entity_id`, `actor`, and date range.
5. Audit log entries are immutable once written — no feature may edit or delete a prior audit log entry, including the Audit Log Service's own admin UI.
6. If the Audit Log Service fails to consume/process an event (e.g., transient failure), the event is retried via the same durable-queue/DLQ mechanism used by Notifications (F8), ensuring the "Auditability" NFR ("every state-changing action... traceable") is not silently violated by a missed event.

**Inputs:**
- Domain event payloads from all other services (see step 1 event-type list).
- Admin filter query: `entity_type` (string, optional), `entity_id` (UUID/long, optional), `actor` (string, optional), `date_from`/`date_to` (date, optional).

**Outputs:**
- Audit log entry representation: `{id, actor, timestamp, entity_type, entity_id, action_type, before_values (nullable), after_values (nullable)}`.
- Filtered list response: paginated array of audit log entries matching the admin's filter query.

**Validation:**
- Every state-changing action exercised in the F13 regression suite must produce a corresponding audit log entry (PRD Section 7 success metric: "100% of state-changing actions... produce a corresponding audit log entry").
- An audit log entry, once written, is never mutated or deleted by any API surface.
- Only users holding the admin permission equivalent to legacy `Logfiles` viewing access (F7) may query the audit log.
- A missed/dropped domain event (consumer failure) must surface via the DLQ mechanism (step 6) rather than silently producing an incomplete audit trail with no trace of the gap.

**Error States:**
| Scenario | HTTP Status | Error Code | Message |
|---|---|---|---|
| Caller lacks permission to view audit log | 403 | AUDIT_LOG_FORBIDDEN | "You do not have permission to view the audit log" |
| Filter query with invalid date range (`date_from` after `date_to`) | 400 | AUDIT_LOG_INVALID_DATE_RANGE | "date_from must be before date_to" |
| Event consumption failure after retry exhaustion | N/A (async) | — | Routed to DLQ; surfaced via observability/alerting, not a client-facing error |

**API Surface (this feature):** see `Y1-api.md` §AuditLog (`GET /audit-log` with query filters).

**Schema Surface (this feature):** owns table `audit_log_entries` — see `Y0-schema.md` §AuditLog. Receives event payloads only from other services; never performs a direct read of another service's database, per service-isolation NFR.
## F12: Microservice Architecture & Platform

**Description:** F12 is the foundational infrastructure substrate every other feature runs on: the React+TypeScript frontend, the bounded-context Spring Boot 3 service decomposition, the one-database-per-service PostgreSQL topology, the RabbitMQ event backbone, Keycloak identity integration, Spring Cloud Gateway as single ingress, and Docker/Kubernetes deployment. Unlike F0–F11, F12 has no legacy functional equivalent — it is net-new infrastructure, informed by the bounded contexts F0 discovers.

**Terminology:**
- **Bounded-context service:** A Spring Boot 3 microservice with its own PostgreSQL database, deployable independently of all others.
- **Single ingress:** The architectural rule that all external client traffic enters exclusively through Spring Cloud Gateway; no backend service is directly externally addressable.
- **Domain event topology:** The full set of RabbitMQ exchanges/queues/routing keys covering every cross-service event referenced across F1–F11.

**Sub-features:**
- React + TypeScript single-page frontend
- Java 21 + Spring Boot 3 bounded-context services
- PostgreSQL per-service databases (no shared schema)
- RabbitMQ domain event topology
- Keycloak realm/client configuration
- Spring Cloud Gateway routing and coarse-grained auth
- Docker images and Kubernetes manifests per service

**Process:**
1. TechArch (informed by F0's confirmed bounded contexts) finalizes the service decomposition: indicatively Booking Service (F1, F2, F3), Location & Resource Service (F4), Custom Field Service (F5), User/Identity-adjacent Service + Keycloak (F6, F7), Notification Service (F8), Public Feed Service (F9), Settings Service (F10), Audit Log Service (F11).
2. Each service is scaffolded as an independent Spring Boot 3 application with its own Maven/Gradle build, its own PostgreSQL database/schema (no service queries another's schema directly), and its own Docker image.
3. RabbitMQ exchanges/queues/routing keys are defined for every domain event referenced in F1–F11 (`booking.created`, `booking.updated`, `booking.deleted`, `booking.approved`, `booking.denied`, `password.reset.requested`, `location.*`, `resource.*`, `user.*`, `role.*`, `settings.updated`, `permission.updated`), each with durable queues, retry/backoff policy, and dead-letter routing per F8/F11's requirements.
4. Keycloak realm is configured with clients for the frontend (public client, PKCE) and each backend service (confidential/bearer-only clients as appropriate), and roles/scopes are defined per F7's permission mapping table.
5. Spring Cloud Gateway is configured with routes to every backend service, coarse-grained role/scope-based access rules per F7, and is the sole externally reachable ingress — backend services are deployed with no external-facing Kubernetes Service/Ingress of their own.
6. React + TypeScript frontend is built covering every user-facing screen required by F1–F10 (calendar/list/day booking views, booking create/edit/detail modal, admin CRUD screens for locations/resources/custom fields/users/roles/settings, audit log viewer, public feed landing pages, display board), communicating exclusively through the Gateway.
7. Each service's Docker image and Kubernetes manifests (Deployment, Service, ConfigMap/Secret) are authored so the service can be deployed/scaled independently of all others, per the "Deployability" NFR, and demonstrated via an independent-deploy test in staging (PRD Section 7 success metric).
8. Cross-domain read patterns identified in PRD Risks (e.g., Booking + Custom Field + Location data needed in one list/detail view) are addressed via an explicit read-model strategy — API composition at the Gateway/BFF layer or service-level denormalized read views — decided in TechArch and documented per affected feature (F1, F5, F9).

**Inputs:**
- Confirmed bounded-context list (from F0/TechArch).
- Full domain event inventory (derived from F1–F11 feature chunks in this document).
- Keycloak role/scope definitions (from F7's permission mapping table).

**Outputs:**
- One deployable Spring Boot 3 service per bounded context, each with its own Docker image.
- One PostgreSQL database per service (see `Y0-schema.md` for per-service schema ownership).
- RabbitMQ topology definition (exchanges/queues/bindings) — see `Y3-integrations.md`.
- Keycloak realm export/configuration.
- Spring Cloud Gateway route configuration.
- Kubernetes manifests per service.
- React + TypeScript frontend application.

**Validation:**
- No service's codebase or runtime directly connects to another service's PostgreSQL database (verifiable via network policy / connection-string audit).
- No backend service is reachable from outside the cluster except through Spring Cloud Gateway (verifiable via Kubernetes Service/Ingress configuration audit).
- Every domain event referenced in any F1–F11 feature chunk has a corresponding RabbitMQ exchange/queue/routing key defined in the topology.
- Every permission flag in F7's mapping table has a corresponding Keycloak role/scope defined in the realm configuration.
- Each service can be built, deployed, and scaled via its own Kubernetes manifests without requiring changes to or redeployment of any other service's manifests (demonstrated in staging, per PRD Section 7).

**Error States:**
| Scenario | Impact | Required Action |
|---|---|---|
| A service's codebase includes a direct connection string/driver reference to another service's database | Violates service-isolation NFR; silent coupling | Reject in code review / architecture test (e.g., ArchUnit rule); refactor to API or event-based access |
| A backend service is exposed via its own externally-reachable Kubernetes Service/Ingress | Violates single-ingress NFR | Reject in deployment review; route exclusively through Gateway |
| A domain event is published without a corresponding consumer queue/binding | Event silently lost | Block deployment of the publishing service until the topology is updated |
| A service cannot be deployed independently (hidden coordinated-deploy dependency) | Violates "Deployability" NFR | Identify and remove the hidden dependency (e.g., shared migration ordering, implicit startup-order assumption) before release |

**API Surface (this feature):** N/A — F12 is infrastructure; the APIs it hosts are specified per-feature (F1–F11) and consolidated in `Y1-api.md`. The Gateway's route table is documented in `Y1-api.md` §Gateway Routing.

**Schema Surface (this feature):** N/A directly — F12 defines the per-service database topology that every other feature's schema (see `Y0-schema.md`) is deployed under; F12 owns no business data of its own.
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
## Y0: Database Schema (Consolidated)

This chunk consolidates the DDL for every service's PostgreSQL database. **Each subsection is an independent database/schema owned exclusively by the named service** — no foreign keys cross service boundaries; cross-service references are by ID only, resolved via API calls (per the "Service isolation" NFR). All tables use `UUID` primary keys (`gen_random_uuid()` default, `pgcrypto`/`uuid-ossp` extension) unless noted; all tables include `created_at`/`updated_at` timestamps with trigger-maintained `updated_at`.

### §Booking (owned by Booking Service) — supports F1, F2, F3

```sql
CREATE TABLE bookings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    series_id       UUID NULL,                       -- shared across occurrences of a recurring series
    title           VARCHAR(255) NOT NULL,
    location_id     UUID NOT NULL,                   -- cross-service reference to Location & Resource Service
    start_time      TIMESTAMPTZ NOT NULL,
    end_time        TIMESTAMPTZ NOT NULL,
    status          VARCHAR(16) NOT NULL DEFAULT 'pending', -- 'pending' | 'approved' | 'denied'
    owner_id        UUID NOT NULL,                   -- cross-service reference to Keycloak subject / User service
    approved_by     UUID NULL,
    approved_at     TIMESTAMPTZ NULL,
    denied_by       UUID NULL,
    denied_at       TIMESTAMPTZ NULL,
    denial_reason   TEXT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ NULL,
    CONSTRAINT chk_booking_time_range CHECK (end_time > start_time),
    CONSTRAINT chk_booking_status CHECK (status IN ('pending','approved','denied'))
);
CREATE INDEX idx_bookings_location_time ON bookings (location_id, start_time, end_time) WHERE deleted_at IS NULL;
CREATE INDEX idx_bookings_series ON bookings (series_id) WHERE series_id IS NOT NULL;
CREATE INDEX idx_bookings_status ON bookings (status) WHERE deleted_at IS NULL;

CREATE TABLE booking_resources (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id      UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    resource_id     UUID NOT NULL,                   -- cross-service reference to Location & Resource Service
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_booking_resource UNIQUE (booking_id, resource_id)
);
CREATE INDEX idx_booking_resources_resource_time ON booking_resources (resource_id, booking_id);
```

### §LocationResource (owned by Location & Resource Service) — supports F4

```sql
CREATE TABLE locations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    css_class       VARCHAR(100) NULL,
    building        VARCHAR(255) NULL,
    layout          JSONB NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ NULL
);
CREATE INDEX idx_locations_active ON locations (id) WHERE deleted_at IS NULL;

CREATE TABLE resources (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ NULL
);
CREATE INDEX idx_resources_active ON resources (id) WHERE deleted_at IS NULL;
```

### §CustomField (owned by Custom Field Service) — supports F5

```sql
CREATE TABLE custom_fields (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    label           VARCHAR(255) NOT NULL,
    field_type      VARCHAR(32) NOT NULL,             -- 'text' | 'number' | 'date' | 'select' (pending F0 confirmation)
    options         JSONB NULL,                        -- required when field_type = 'select'
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ NULL
);

CREATE TABLE custom_field_templates (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    context_id      UUID NULL,                         -- e.g., Location id this template applies to; NULL = global
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE custom_field_joins (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    custom_field_template_id UUID NOT NULL REFERENCES custom_field_templates(id) ON DELETE CASCADE,
    custom_field_id          UUID NOT NULL REFERENCES custom_fields(id) ON DELETE CASCADE,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_template_field UNIQUE (custom_field_template_id, custom_field_id)
);

CREATE TABLE custom_field_values (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id      UUID NOT NULL,                     -- cross-service reference to Booking Service
    custom_field_id UUID NOT NULL REFERENCES custom_fields(id),
    value           TEXT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_custom_field_values_booking ON custom_field_values (booking_id);
```

### §User (owned by User/Identity-adjacent Service; credentials owned by Keycloak realm, not this schema) — supports F6

```sql
CREATE TABLE users (
    id              UUID PRIMARY KEY,                  -- matches Keycloak subject (sub claim), not independently generated
    email           VARCHAR(320) NOT NULL UNIQUE,
    display_name    VARCHAR(255) NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ NULL
);
-- No password/hash/salt columns: credential storage is delegated entirely to Keycloak, per "Standards-based identity" NFR.
```

### §Permission (owned by Permission System, if supplementary mapping-metadata store is needed beyond Keycloak roles) — supports F7

```sql
CREATE TABLE permissions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    legacy_flag         VARCHAR(100) NOT NULL UNIQUE,   -- e.g., 'accessCalendar', 'allowRoomBooking'
    keycloak_role       VARCHAR(100) NOT NULL,
    gated_actions       JSONB NULL,
    confirmed           BOOLEAN NOT NULL DEFAULT false, -- true only once F0 confirms exact gating scope
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

### §Notification (owned by Notification Service) — supports F8

```sql
CREATE TABLE notification_deliveries (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key     VARCHAR(255) NOT NULL UNIQUE,
    event_type          VARCHAR(64) NOT NULL,            -- 'booking.created' | 'booking.approved' | 'booking.denied' | 'password.reset.requested'
    entity_id           UUID NOT NULL,
    status              VARCHAR(16) NOT NULL DEFAULT 'pending', -- 'pending' | 'sent' | 'retrying' | 'dead_lettered'
    attempt_count       INTEGER NOT NULL DEFAULT 0,
    last_attempted_at   TIMESTAMPTZ NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_notification_status ON notification_deliveries (status);
```

### §Feed (owned by Public Feed Service, optional materialized read-model) — supports F9

```sql
-- Optional denormalized projection, populated by consuming booking.created/approved/denied/deleted events,
-- used only if API composition at request time proves insufficient per TechArch decision.
CREATE TABLE feed_bookings (
    booking_id      UUID PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    location_id     UUID NOT NULL,
    location_name   VARCHAR(255) NOT NULL,
    start_time      TIMESTAMPTZ NOT NULL,
    end_time        TIMESTAMPTZ NOT NULL,
    status          VARCHAR(16) NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_feed_bookings_upcoming ON feed_bookings (status, start_time);
CREATE INDEX idx_feed_bookings_location ON feed_bookings (location_id, start_time);
```

### §Settings (owned by Settings Service) — supports F10

```sql
CREATE TABLE settings (
    id                   INTEGER PRIMARY KEY DEFAULT 1, -- singleton row enforced by CHECK below
    approve_booking      BOOLEAN NOT NULL DEFAULT true,
    calendar_slot_size   INTEGER NOT NULL DEFAULT 30,    -- minutes
    calendar_min_time    TIME NOT NULL DEFAULT '08:00',
    calendar_max_time    TIME NOT NULL DEFAULT '18:00',
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by           UUID NULL,
    CONSTRAINT chk_settings_singleton CHECK (id = 1),
    CONSTRAINT chk_settings_calendar_range CHECK (calendar_min_time < calendar_max_time),
    CONSTRAINT chk_settings_slot_size CHECK (calendar_slot_size > 0)
);
INSERT INTO settings (id) VALUES (1) ON CONFLICT (id) DO NOTHING;
```

### §AuditLog (owned by Audit Log Service) — supports F11

```sql
CREATE TABLE audit_log_entries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id        UUID NOT NULL,
    occurred_at     TIMESTAMPTZ NOT NULL,
    entity_type     VARCHAR(64) NOT NULL,              -- 'booking' | 'location' | 'resource' | 'user' | 'role' | 'settings' | 'permission' | ...
    entity_id       UUID NOT NULL,
    action_type     VARCHAR(64) NOT NULL,              -- 'created' | 'updated' | 'deleted' | 'approved' | 'denied' | ...
    before_values   JSONB NULL,
    after_values    JSONB NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_entity ON audit_log_entries (entity_type, entity_id);
CREATE INDEX idx_audit_actor ON audit_log_entries (actor_id);
CREATE INDEX idx_audit_occurred_at ON audit_log_entries (occurred_at);
-- Immutability: no UPDATE/DELETE grants on this table for any application role; writes are INSERT-only.
```

### §Traceability (optional, owned by F13 tooling if not satisfied by external test-management SaaS)

```sql
CREATE TABLE traceability_entries (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    audit_finding_ref   VARCHAR(255) NOT NULL,          -- reference into F0 findings document
    requirement_ref     VARCHAR(255) NOT NULL,          -- e.g., 'FRD F2 §Validation bullet 3'
    implementation_ref  VARCHAR(255) NULL,
    test_ref            VARCHAR(255) NULL,
    status              VARCHAR(16) NOT NULL DEFAULT 'not_started', -- 'not_started'|'implemented'|'tested'|'verified'
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
```
## Y1: API Endpoints (Consolidated)

All endpoints are reached exclusively through Spring Cloud Gateway (single ingress, per F12). Paths below are logical API paths as exposed at the Gateway; the Gateway routes each prefix to its owning backend service. All request/response bodies are JSON unless noted (feed formats are the exception, per F9). All endpoints except `GET /feeds/*` and `POST /auth/login` require a valid Keycloak-issued bearer token; permission requirements reference F7 flags pending F0 confirmation of exact names/scopes.

### §Booking (Booking Service — F1, F2, F3)

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `POST /bookings` | `{title, location_id, start_time, end_time?, resource_ids?, custom_field_values?, recurrence?}` | `201` Booking repr (or series array) | `allowRoomBooking` |
| `GET /bookings` | query: `from`, `to`, `location_id?`, `resource_id?` | `200` array of Booking repr w/ `conflict_flags[]` | `viewRoomBooking` |
| `GET /bookings/{id}` | — | `200` full Booking repr (Eventdata-equivalent) | `viewRoomBooking` |
| `PUT /bookings/{id}` | `{title?, location_id?, start_time?, end_time?, resource_ids?, custom_field_values?, scope?}` | `200` updated Booking repr | `allowRoomBooking` (owner) or admin |
| `DELETE /bookings/{id}` | query/body: `scope?` | `204` | `allowRoomBooking` (owner) or admin |
| `POST /bookings/{id}/clone` | — | `201` new draft Booking repr | `allowRoomBooking` |
| `POST /bookings/{id}/approve` | — | `200` updated Booking repr | `allowApproveBooking` |
| `POST /bookings/{id}/deny` | `{denial_reason?}` | `200` updated Booking repr | `allowApproveBooking` |
| `POST /bookings/check-conflicts` | `{location_id?, resource_ids?, start_time, end_time, exclude_booking_id?}` | `200 {has_conflict, conflicts[]}` | `allowRoomBooking` |

### §Locations (Location & Resource Service — F4)

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /locations` | — | `200` array of Location repr | `accessCalendar` (read) |
| `POST /locations` | `{name, css_class?, building?, layout?}` | `201` Location repr | admin (location mgmt) |
| `GET /locations/{id}` | — | `200` Location repr | `accessCalendar` (read) |
| `PUT /locations/{id}` | `{name?, css_class?, building?, layout?}` | `200` Location repr | admin (location mgmt) |
| `DELETE /locations/{id}` | — | `204` | admin (location mgmt) |

### §Resources (Location & Resource Service — F4)

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /resources` | — | `200` array of Resource repr | `accessCalendar` (read) |
| `POST /resources` | `{name}` | `201` Resource repr | admin (resource mgmt) |
| `GET /resources/{id}` | — | `200` Resource repr | `accessCalendar` (read) |
| `PUT /resources/{id}` | `{name?}` | `200` Resource repr | admin (resource mgmt) |
| `DELETE /resources/{id}` | — | `204` | admin (resource mgmt) |

### §CustomFields (Custom Field Service — F5)

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /custom-fields` | — | `200` array of Custom Field repr | admin (custom field mgmt) |
| `POST /custom-fields` | `{label, field_type, options?}` | `201` Custom Field repr | admin (custom field mgmt) |
| `GET /custom-fields/{id}` | — | `200` Custom Field repr | admin (custom field mgmt) |
| `PUT /custom-fields/{id}` | `{label?, field_type?, options?}` | `200` Custom Field repr | admin (custom field mgmt) |
| `DELETE /custom-fields/{id}` | — | `204` | admin (custom field mgmt) |
| `GET /field-templates` | — | `200` array of Field Template repr | admin (custom field mgmt) |
| `POST /field-templates` | `{name, context_id?, field_ids?}` | `201` Field Template repr | admin (custom field mgmt) |
| `GET /field-templates/{id}` | — | `200` Field Template repr | admin (custom field mgmt) |
| `PUT /field-templates/{id}` | `{name?, context_id?, field_ids?}` | `200` Field Template repr | admin (custom field mgmt) |
| `DELETE /field-templates/{id}` | — | `204` | admin (custom field mgmt) |

### §Auth (User/Identity-adjacent Service + Keycloak — F6)

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `POST /auth/login` | `{username, password, remember_me?}` | `200 {access_token, refresh_token, expires_in, roles[]}` | public |
| `POST /auth/logout` | — | `204` | authenticated |
| `POST /auth/password-reset/request` | `{email}` | `202` (generic ack) | public |
| `POST /auth/password-reset/complete` | `{reset_token, new_password}` | `200` | public (token-bearing) |
| `POST /auth/password-change` | `{current_password, new_password}` | `200` | authenticated (self) |

### §Users (User/Identity-adjacent Service — F6)

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /users` | — | `200` array of User repr | admin (user mgmt) |
| `POST /users` | `{email, initial_role}` | `201` User repr | admin (user mgmt) |
| `GET /users/{id}` | — | `200` User repr | admin (user mgmt) or self |
| `PUT /users/{id}` | `{display_name?}` | `200` User repr | admin (user mgmt) or self |
| `PUT /users/{id}/roles` | `{roles[]}` | `200` User repr | admin (user mgmt) |
| `GET /users/me` | — | `200` User repr | authenticated (self) |
| `PUT /users/me` | `{display_name?}` | `200` User repr | authenticated (self) |

### §Permissions (Permission System — F7)

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /permissions` | — | `200` array of permission mapping entries | `accessPermissions` |
| `GET /roles/{role}/permissions` | — | `200 {role_name, permission_flags[]}` | `accessPermissions` |
| `PUT /roles/{role}/permissions` | `{permission_flags[]}` | `200` updated role-permission repr | `accessPermissions` |

### §Notifications (Notification Service — F8)

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /notifications/delivery-status` | query: `event_type?`, `status?` | `200` array of delivery status repr | admin (ops/observability) |
| `GET /notifications/dead-letter` | — | `200` array of dead-lettered entries | admin (ops/observability) |

### §Feeds (Public Feed Service — F9)

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /feeds/rss2` | query: `location_id?` | `200` RSS2 XML | `allowAPI` (pending F0 default-public confirmation) |
| `GET /feeds/ical` | query: `location_id?` | `200` iCal `.ics` | `allowAPI` (pending F0 default-public confirmation) |
| `GET /feeds/json` | query: `location_id?` | `200` JSON array | `allowAPI` (pending F0 default-public confirmation) |
| `GET /feeds/display-board` | query: `location_id?` | `200` HTML | `allowAPI` (pending F0 default-public confirmation) |

### §Settings (Settings Service — F10)

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /settings` | — | `200` Settings repr | authenticated (read scope TBD per F0) |
| `PUT /settings` | `{approve_booking?, calendar_slot_size?, calendar_min_time?, calendar_max_time?}` | `200` Settings repr | admin (settings mgmt) |

### §AuditLog (Audit Log Service — F11)

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /audit-log` | query: `entity_type?`, `entity_id?`, `actor?`, `date_from?`, `date_to?` | `200` paginated array of audit log entries | admin (log viewing, legacy `Logfiles` equivalent) |

### §Gateway Routing (F12)

| Route Prefix | Target Service | Coarse-grained Role/Scope Required |
|---|---|---|
| `/bookings/**` | Booking Service | any authenticated role carrying `viewRoomBooking` or `allowRoomBooking` |
| `/locations/**`, `/resources/**` | Location & Resource Service | authenticated (read); admin role for write methods |
| `/custom-fields/**`, `/field-templates/**` | Custom Field Service | admin role |
| `/auth/**` | User/Identity-adjacent Service + Keycloak | public (login/reset) or authenticated (logout/change) |
| `/users/**` | User/Identity-adjacent Service | admin role or self |
| `/permissions/**`, `/roles/**` | Permission System | `accessPermissions` |
| `/notifications/**` | Notification Service | admin/ops role |
| `/feeds/**` | Public Feed Service | `allowAPI` or public, per F9 pending confirmation |
| `/settings/**` | Settings Service | authenticated (read); admin (write) |
| `/audit-log/**` | Audit Log Service | admin role |
## Y2: Error Catalog (Cross-Feature)

This catalog consolidates error scenarios that span multiple features or are not specific to a single feature's primary flow. Feature-specific errors are documented in each `F{n}-*.md` chunk's Error States table; this catalog covers (a) cross-cutting platform-level errors common to every endpoint, and (b) errors that span two or more features. All errors return a JSON body of shape `{error_code, message, timestamp, path}` unless otherwise noted.

### Platform-level errors (apply to every endpoint, per F12/F7)

| Scenario | HTTP Status | Error Code | Message | Retry Guidance |
|---|---|---|---|---|
| Request without a bearer token to a protected route | 401 | AUTH_UNAUTHENTICATED | "Authentication required" | Do not retry without obtaining a valid token |
| Bearer token expired | 401 | AUTH_TOKEN_EXPIRED | "Session has expired; please log in again" | Refresh token or re-authenticate, then retry |
| Bearer token valid but insufficient role/scope for the route (Gateway-level) | 403 | GATEWAY_FORBIDDEN | "Access denied for this resource" | Do not retry; requires role change |
| Bearer token valid, Gateway allows, but service-level fine-grained check fails | 403 | ACTION_FORBIDDEN | "You do not have permission to perform this action" | Do not retry; requires role/ownership change |
| Malformed JSON request body | 400 | REQUEST_MALFORMED | "Request body is malformed or not valid JSON" | Fix request body and retry |
| Request to unknown route | 404 | ROUTE_NOT_FOUND | "The requested resource does not exist" | Do not retry |
| Backend service unavailable (Gateway cannot reach target) | 503 | SERVICE_UNAVAILABLE | "Service temporarily unavailable" | Retry with exponential backoff |
| Rate limit exceeded (if configured at Gateway) | 429 | RATE_LIMIT_EXCEEDED | "Too many requests; please slow down" | Retry after the indicated `Retry-After` interval |
| Unhandled server-side exception | 500 | INTERNAL_ERROR | "An unexpected error occurred" | Retry once; escalate if persistent |

### Cross-feature errors

| Scenario | HTTP Status | Error Code | Message | Spanning Features | Retry Guidance |
|---|---|---|---|---|---|
| Booking create/edit references a Location that exists but is soft-deleted | 404 | BOOKING_LOCATION_NOT_FOUND | "Specified location does not exist" | F1, F4 | Do not retry with same location_id |
| Booking create/edit references a Resource that exists but is soft-deleted | 404 | BOOKING_RESOURCE_NOT_FOUND | "One or more specified resources do not exist" | F1, F4 | Do not retry with same resource_ids |
| Booking create/edit submits a custom field value for a field not applicable to the booking's location context | 400 | CUSTOM_FIELD_VALUE_INVALID | "One or more submitted custom field values are invalid" | F1, F5 | Fix field selection and retry |
| Settings Service unreachable when Booking Service needs `approveBooking` at creation time | 503 | APPROVAL_SETTINGS_UNAVAILABLE | "Unable to determine approval requirement; try again shortly" | F1, F3, F10 | Retry with exponential backoff |
| Approve/deny action attempted by a user whose role mapping has not yet propagated after a recent role change | 403 | ACTION_FORBIDDEN | "You do not have permission to perform this action" | F3, F6, F7 | Re-authenticate to obtain a fresh token reflecting the new role, then retry |
| Domain event consumed by Notification Service or Audit Log Service references an entity (e.g., booking_id) that no longer exists due to a race with a subsequent delete | 200 (processed as best-effort; logged as a data-quality warning, not a client-facing error) | — | — | F1, F8, F11 | N/A (async; investigate via DLQ/observability if pattern recurs) |
| Public feed request filtered by `location_id` that does not exist | 200 (empty feed body, per F9 §Validation) | — | — | F9, F4 | N/A — not an error condition by design |
| Conflict check invoked with neither `location_id` nor `resource_ids` supplied | 400 | CONFLICT_CHECK_INVALID_INPUT | "At least a location or one resource must be specified for conflict checking" | F1, F2 | Fix request and retry |

### Notification delivery failure modes (F8, cross-referenced by F1/F3/F6 as event publishers)

| Scenario | Impact | Retry Guidance |
|---|---|---|
| SMTP/email provider transient failure | Delivery delayed, not lost | Automatic retry per backoff policy; no client action needed (async) |
| Retry count exhausted | Delivery unconfirmed | Routed to DLQ; requires manual/alerted operational follow-up, not a client retry |
| Duplicate event redelivery (RabbitMQ at-least-once semantics) | Risk of duplicate email | Suppressed via idempotency key; no client-visible error |

### Audit log consumption failure modes (F11, cross-referenced by all mutating features)

| Scenario | Impact | Retry Guidance |
|---|---|---|
| Audit Log Service fails to consume a domain event | Audit trail gap risk | Automatic retry via durable queue; DLQ + alerting on exhaustion, per F11 §Process step 6 |
## Y3: Integration Points (External Systems)

This chunk documents every external system Booking-Hub depends on, the contract each feature relies on, and failure-handling expectations. All integrations are owned by F12 (platform) at the infrastructure level, but consumed functionally by the features noted.

### Keycloak (Identity & Access Management)

- **Consumed by:** F6 (User & Role Management), F7 (Permission System), F12 (Gateway auth enforcement).
- **Contract:** Keycloak realm exposes OIDC endpoints (`/auth/realms/{realm}/protocol/openid-connect/token`, `/userinfo`, etc.) and an Admin REST API for account/role provisioning. Booking-Hub services validate bearer JWTs against the realm's published JWKS; no service independently verifies passwords.
- **Roles/scopes:** Keycloak client roles map 1:1 (or documented N:1) to legacy permission flags per F7's permission mapping table (e.g., `accessCalendar`, `allowRoomBooking`, `viewRoomBooking`, `allowApproveBooking`, `accessPermissions`, `allowAPI`).
- **Failure handling:** If Keycloak is unreachable, the Gateway and all services must fail closed (reject requests with `503`), never fail open (never grant access when the identity provider cannot be consulted) — this is a hard requirement given the "Standards-based identity" NFR.
- **Token refresh:** Access tokens are short-lived; refresh tokens (extended lifetime if `remember_me` was set per F6) are used to obtain new access tokens without re-prompting credentials.

### RabbitMQ (Messaging)

- **Consumed by:** F1/F3 (publish `booking.created`, `booking.updated`, `booking.deleted`, `booking.approved`, `booking.denied`), F4 (publish `location.*`, `resource.*`), F5 (publish custom-field change events), F6 (publish `user.*`, `role.assigned`, `password.reset.requested`), F7 (publish `permission.updated`), F10 (publish `settings.updated`), F8 (consume booking/password-reset events), F11 (consume all change events).
- **Contract:** Each event is published to a durable exchange with a stable routing key per event type (see F12 §Process step 3 for the full event inventory). Each consuming service binds its own durable queue to the relevant routing keys — publishers never address consumer queues directly (decoupled, per "Event-driven decoupling" NFR).
- **Delivery semantics:** At-least-once delivery; consumers (F8, F11) must be idempotent (idempotency key on each event payload) to tolerate redelivery without duplicate side effects.
- **Failure handling:** Failed message processing is retried with backoff; retry exhaustion routes to a dead-letter queue per event type, surfaced via observability/alerting rather than silently dropped (per F8 §Process steps 7–8, F11 §Process step 6).
- **Publisher guarantee:** A publishing service's own transaction (e.g., Booking Service persisting a new Booking) must not be blocked or rolled back by a RabbitMQ outage — events are published after the local transaction commits (outbox pattern or equivalent), per "Event-driven decoupling" NFR ensuring a notification/audit failure cannot block the originating transaction.

### SMTP / Email Provider

- **Consumed by:** F8 (Notifications) exclusively.
- **Contract:** Notification Service sends rendered email templates via a configured SMTP relay or transactional email API (exact provider TBD in TechArch).
- **Failure handling:** Transient provider failures trigger the retry/DLQ path defined in F8 §Process steps 7–8; the provider integration itself does not implement business logic (recipient resolution, template selection happen in the Notification Service before the provider call).
- **Content parity:** Notification templates must be equivalent in content/intent to legacy email templates (F8 §Description); exact legacy template content is an F0 audit input, not invented here.

### Public Demo / Documentation (reference-only, not a runtime dependency)

- **Consumed by:** F0 (Legacy Functional Audit) only, as a corroboration source.
- **`roombooking.oxalto.co.uk`:** Legacy demo site, used to observe actual UI/UX behavior where code alone is ambiguous.
- **`roombooking.readme.io`:** Legacy documentation site, used to corroborate code-derived findings.
- **Note:** Neither is a runtime integration of Booking-Hub itself; both are inputs to the F0 audit process only and have no bearing on F1–F13 runtime behavior beyond the findings they produce.

### Kubernetes / Docker (Deployment Platform)

- **Consumed by:** F12 exclusively, as the deployment substrate for every other feature's owning service.
- **Contract:** Each service ships a Docker image and a set of Kubernetes manifests (Deployment, Service, ConfigMap, Secret) enabling independent build/deploy/scale, per the "Deployability" NFR.
- **Failure handling:** Standard Kubernetes liveness/readiness probes per service; a single service's pod failures must not cascade to other services (enforced by the service-isolation and single-ingress architectural rules in F12).
