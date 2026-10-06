# User Story Map
## Booking-Hub

| Field | Value |
|-------|-------|
| **Product Name** | Booking-Hub |
| **Date** | 2026-10-06 |
| **Related Personas** | PERSONAS-BookingHub.md |
| **Related Journeys** | JOURNEYS-BookingHub.md |
| **Related JTBD** | JTBD-BookingHub.md |
| **Related User Stories** | UserStories-BookingHub.md |
| **Related PRD** | PRD-BookingHub.md |

---

## Overview

This story map organizes Booking-Hub's 49 user stories (US-0.1 through US-13.2) along two axes: the horizontal axis of journey stages drawn from the 7 journeys in JOURNEYS-BookingHub.md, and the vertical axis of release sequencing (R1–R3) derived from PRD priority (P0/P1).

**NaC (Natural Acceptance Criteria)** bridge JTBD outcomes to testable criteria: each NaC is derived by taking a JTBD functional outcome, applying it to the specific journey stage context, and producing a criterion that is already testable against the story's acceptance criteria in UserStories-BookingHub.md. No NaC in this document is invented — each one traces to a JTBD ID and a journey stage.

Seven lanes below (one per persona per primary journey) place every journey-mapped story at its stage. 12 of the 49 stories do not correspond to a named journey stage (login/account management, clone, deletion-in-use, and platform substrate work) — these are flagged as **orphan stories** in the Coverage Analysis rather than force-fit with invented NaC.

---

## Story Map Matrix

### PER-01: Maya Torres — JRN-01.1: Quick Conflict-Free Room Booking with Resources

| Activity | Persona | Epic | Stories | NaC | Release |
|---|---|---|---|---|---|
| Discover: scan the calendar for a free slot before a client call | PER-01 | Epic 1 (F1) | US-1.5 | JTBD-01.1: Every returned booking includes conflict_flags so Maya can tell at a glance which rooms are truly free, not just tentatively held | R1 |
| Select: pick a colour-coded room on the calendar grid | PER-01 | Epic 4 (F4) | US-4.4 | JTBD-01.1: Each location renders with a consistent colour/CSS class across calendar and list views so Maya visually confirms the right room before booking | R2 |
| Select/Submit: create the booking with room, time, and resources in one form | PER-01 | Epic 1 (F1) | US-1.1 | JTBD-01.1: Booking saves and returns its assigned status plus any conflict flags at submission, in a single step | R1 |
| Configure: attach a conference phone and fill the required catering headcount field | PER-01 | Epic 5 (F5) | US-5.2 | JTBD-01.4: Required custom fields render dynamically based on the selected location's context, with submission blocked until completed | R2 |
| Submit: watch for conflict feedback before navigating away | PER-01 | Epic 2 (F2) | US-2.1 | JTBD-01.1: A detected conflict is surfaced immediately with the conflicting booking's id and whether it is a location or resource conflict | R1 |
| Submit: trust the conflict signal is the same one David will later see | PER-01 | Epic 2 (F2) | US-2.2 | JTBD-01.1: Conflict evaluation logic is identical regardless of entry point (UI calendar, UI list, or API), so conflicts never appear inconsistently between views | R1 |
| Confirm: see current booking status in the detail view | PER-01 | Epic 1 (F1) | US-1.7 | JTBD-01.3: Detail view reflects the booking's current status (pending/approved/denied) accurately, visible immediately after submission | R1 |
| Confirm: know immediately whether the booking auto-approved or needs review | PER-01 | Epic 3 (F3) | US-3.4 | JTBD-01.3: Booking status is set per the current approveBooking setting at creation time, with no ambiguity about which state applies | R1 |
| Confirm: receive a submission notification as evidence the booking was recorded | PER-01 | Epic 8 (F8) | US-8.1 | JTBD-01.3: A booking.created event reliably triggers a confirmation email, with no duplicate send on event redelivery | R2 |

### PER-01: Maya Torres — JRN-01.2: Recurring Series Edit and Approval Wait

| Activity | Persona | Epic | Stories | NaC | Release |
|---|---|---|---|---|---|
| Locate: find this week's occurrence of the recurring sync | PER-01 | Epic 1 (F1) | US-1.6 | JTBD-01.2: A recurrence definition expands into individual occurrences sharing a series_id, each independently identifiable and editable | R1 |
| Edit: open the occurrence and change its start time | PER-01 | Epic 1 (F1) | US-1.2 | JTBD-01.2: Editing a booking that belongs to a series requires an explicit scope selection (this occurrence or whole series); omitting scope is rejected | R1 |
| Edit: cancel a single occurrence without touching the series | PER-01 | Epic 1 (F1) | US-1.3 | JTBD-01.2: Deleting a booking in a series requires the same explicit scope selection as editing, so other occurrences are never silently altered | R1 |
| Confirm Scope: select "this occurrence only" and see it applied | PER-01 | Epic 1 (F1) | US-1.2 | JTBD-01.2: A successful single-occurrence edit leaves every other occurrence in the series unmodified, confirmed before the change is final | R1 |
| Submit: save the time change, which now requires approval | PER-01 | Epic 2 (F2) | US-2.3 | JTBD-01.1: Editing a booking's time triggers a fresh conflict evaluation (not a cached result), with multi-resource conflicts unioned across all attached resources | R1 |
| Submit: see the booking's status immediately after the edit | PER-01 | Epic 3 (F3) | US-3.4 | JTBD-01.3: Status is set per the current approveBooking setting at the moment of edit submission, visible without delay | R1 |
| Wait: check status periodically while continuing other work | PER-01 | Epic 1 (F1) | US-1.7 | JTBD-01.3: Booking detail view reflects the current status accurately on demand, so Maya never has to guess whether a decision was made | R1 |
| Resolve: receive a notification the moment David decides | PER-01 | Epic 8 (F8) | US-8.2 | JTBD-01.3: A booking.approved/denied event triggers an email within minutes, retried per a backoff policy on transient failure | R2 |

### PER-02: David Okafor — JRN-02.1: Morning Pending-Queue Review

| Activity | Persona | Epic | Stories | NaC | Release |
|---|---|---|---|---|---|
| Open Queue: navigate to the pending-booking queue | PER-02 | Epic 3 (F3) | US-3.1 | JTBD-02.1: Pending queue lists all status=pending bookings and is restricted to users holding allowApproveBooking, loading quickly | R1 |
| Scan: scan the list for conflict-flagged bookings | PER-02 | Epic 2 (F2) | US-2.2 | JTBD-02.1: Every queued booking displays conflict_flags inline, using the same underlying conflict data as calendar/list views — no manual cross-checking | R1 |
| Scan: factor in submitted custom field values (e.g., catering cost) | PER-02 | Epic 5 (F5) | US-5.3 | JTBD-02.1: Custom field values are included in the booking representation returned in queue/detail views, visible without a separate lookup | R2 |
| Investigate: open a flagged booking to see exactly what it conflicts with | PER-02 | Epic 2 (F2) | US-2.1 | JTBD-02.1: A detected conflict is surfaced with the conflicting booking's id and whether it is a location or resource conflict, not just a generic flag | R1 |
| Decide: approve a non-conflicting booking in one action | PER-02 | Epic 3 (F3) | US-3.2 | JTBD-02.2: A successful approval sets status=approved, records approved_by/approved_at, and publishes a booking.approved event in a single action | R1 |
| Decide: deny a conflicting booking with an optional reason | PER-02 | Epic 3 (F3) | US-3.3 | JTBD-02.2: A successful denial sets status=denied with an optional denial_reason and publishes a booking.denied event consumed by Notifications | R1 |
| Decide: trust the requester is reliably notified without manual follow-up | PER-02 | Epic 8 (F8) | US-8.3 | JTBD-02.2: 100% of approved/denied events result in a delivered or retried-to-success notification, with no silent drops | R2 |
| Verify Boundary: confirm a booking already shows auto-approved | PER-02 | Epic 3 (F3) | US-3.4 | JTBD-02.3: Changing the approveBooking flag never retroactively changes the status of already-created bookings, so the queue reflects exactly the current setting | R1 |
| Verify Boundary: cross-check against the current approveBooking setting state | PER-02 | Epic 10 (F10) | US-10.1 | JTBD-02.3: Only one settings record exists system-wide and the Booking Service reads its latest saved value at creation time, removing ambiguity in the queue | R3 |

### PER-02: David Okafor — JRN-02.2: Resolving a Disputed Approval Decision

| Activity | Persona | Epic | Stories | NaC | Release |
|---|---|---|---|---|---|
| Locate Booking: find the specific booking in question | PER-02 | Epic 1 (F1) | US-1.7 | JTBD-02.4: Event-detail endpoint returns the full field set, including current status, so David can locate and confirm the exact record in dispute | R1 |
| Query Audit Trail: open the audit log entry for that booking | PER-02 | Epic 11 (F11) | US-11.2 | JTBD-02.4: Audit log entries for a booking include actor, timestamp, action_type, and before/after values, and are immutable once written | R3 |
| Resolve: share the audit record to close the dispute | PER-02 | Epic 11 (F11) | US-11.1 | JTBD-02.4: Filtered audit log queries return a paginated list of matching entries, giving David an authoritative, shareable record | R3 |

### PER-03: Priya Patel — JRN-03.1: Onboarding a New Bookable Room

| Activity | Persona | Epic | Stories | NaC | Release |
|---|---|---|---|---|---|
| Navigate: open the admin area and go to Locations management | PER-03 | Epic 4 (F4) | US-4.1 | JTBD-03.1: A single form captures name, calendar colour, and building metadata so the location is ready in one pass | R2 |
| Create: fill in location metadata and attach a resource | PER-03 | Epic 4 (F4) | US-4.2 | JTBD-03.1: Resources are created independently of locations and referenced by id only, so equipment can be attached without duplicated setup | R2 |
| Save: submit the form and watch for confirmation | PER-03 | Epic 4 (F4) | US-4.1 | JTBD-03.1: A new or edited location is immediately available to the Booking Service for selection and conflict scoping, with clear save confirmation | R2 |
| Verify: confirm the new room is immediately selectable in the booking flow | PER-03 | Epic 1 (F1) | US-1.1 | JTBD-03.1: location_id must reference an existing, non-deleted location — confirming the new room is live and bookable within 5 minutes | R1 |
| Log: confirm the creation action appears in the audit log | PER-03 | Epic 11 (F11) | US-11.3 | JTBD-03.4: Location/resource changes produce a corresponding audit log entry with actor and timestamp, with no dropped events | R3 |

### PER-03: Priya Patel — JRN-03.2: Verifying Permission Migration and Settings Propagation

| Activity | Persona | Epic | Stories | NaC | Release |
|---|---|---|---|---|---|
| Review Mapping: check a legacy flag against its Keycloak role/scope | PER-03 | Epic 7 (F7) | US-7.1 | JTBD-03.2: Every referenced permission flag appears in the mapping table before its gated action is implemented; unconfirmed scopes are deny-by-default | R1 |
| Review Mapping: view/edit which permission flags each role carries | PER-03 | Epic 7 (F7) | US-7.2 | JTBD-03.2: Only users holding accessPermissions may modify role-to-permission mappings, with every change recorded in the audit log | R1 |
| Run Boundary Test: review the permission-boundary test result for the mapping | PER-03 | Epic 13 (F13) | US-13.1 | JTBD-03.2: Every FRD validation/permission rule has at least one linked, concrete automated test before its feature is release-ready | R1 |
| Toggle Setting: change the approveBooking flag | PER-03 | Epic 10 (F10) | US-10.1 | JTBD-03.3: Only admin-permission users can modify approveBooking, and the change is recorded in the audit log with actor and timestamp | R3 |
| Toggle Setting: adjust calendar slot size and visible time range | PER-03 | Epic 10 (F10) | US-10.2 | JTBD-03.3: calendar_slot_size must be a positive integer and calendar_min_time must precede calendar_max_time, rejecting invalid values before propagation | R3 |
| Confirm Propagation: check Booking Service behavior right after the toggle | PER-03 | Epic 10 (F10) | US-10.3 | JTBD-03.3: The Booking Service reads the current approveBooking value at booking-creation time, reflecting the latest saved setting with zero manual checking | R3 |
| Review Audit Entries: check the audit log for both actions | PER-03 | Epic 11 (F11) | US-11.1 | JTBD-03.4: Audit log is filterable by entity type, actor, and time range, returning paginated matching entries for both the test run and the settings change | R3 |
| Review Audit Entries: confirm no state-changing action is missing | PER-03 | Epic 11 (F11) | US-11.3 | JTBD-03.4: 100% of state-changing actions exercised in the regression suite produce a corresponding audit log entry, with dropped events retried via DLQ | R3 |
| Sign Off: review the F0 inventory for completeness | PER-03 | Epic 0 (F0) | US-0.1 | JTBD-03.5: Audit findings document includes a dedicated section for every legacy controller and model, with every finding citing its source | R1 |
| Sign Off: confirm every ambiguity was resolved, not assumed | PER-03 | Epic 0 (F0) | US-0.2 | JTBD-03.5: All seed Open Questions have either a cited resolution or remain explicitly logged as open; none are closed by assumption | R1 |
| Sign Off: review the traceability matrix before giving release sign-off | PER-03 | Epic 13 (F13) | US-13.1 | JTBD-03.5: Every F0 audit finding has at least one linked FRD requirement row, with no orphaned findings | R1 |
| Sign Off: trust CI blocks any parity regression before release | PER-03 | Epic 13 (F13) | US-13.2 | JTBD-03.5: A CI-enforced regression suite runs on every change and must pass before merge/release, blocking any previously-passing test from silently failing | R1 |

### PER-04: Jordan Lee — JRN-04.1: Directing a Visitor via the Display Board and Setting Up a Personal Feed

| Activity | Persona | Epic | Stories | NaC | Release |
|---|---|---|---|---|---|
| Glance: look at the lobby display board while a visitor waits | PER-04 | Epic 9 (F9) | US-9.1 | JTBD-04.1: Display board shows only bookings with status=approved and auto-refreshes client-side, keeping Jordan's view current | R3 |
| Filter: confirm the board is scoped to this specific location | PER-04 | Epic 9 (F9) | US-9.1 | JTBD-04.3: Display board can be filtered to a specific location when multiple buildings are shown, with no login required to apply the filter | R3 |
| Verify Approval: check the booking shown is marked approved, not pending | PER-04 | Epic 9 (F9) | US-9.4 | JTBD-04.1: Pending and denied bookings are never exposed via any feed format or the display board; refresh latency stays under 1 minute from approval | R3 |
| Set Up Feed: subscribe once to the iCal feed for this location | PER-04 | Epic 9 (F9) | US-9.2 | JTBD-04.2: The iCal feed produces one VEVENT per approved upcoming booking, with per-location filtering and no manual resubscription ever required | R3 |
| Set Up Feed: trust access works the same across RSS2/JSON/iCal/display board | PER-04 | Epic 9 (F9) | US-9.3 | JTBD-04.3: Feed access control is evaluated identically regardless of requested format, so a caller denied one format is denied all, with no login prompt | R3 |

---

## NaC Derivation Table

| JTBD ID | Outcome | Journey Stage | NaC | Story |
|---------|---------|---------------|-----|-------|
| JTBD-01.1 | Conflict visible at submission | JRN-01.1:Discover | Conflict_flags returned on every date-range view so free slots are distinguishable from tentative holds | US-1.5 |
| JTBD-01.1 | Conflict visible at submission | JRN-01.1:Select | Consistent location colour-coding across calendar/list views | US-4.4 |
| JTBD-01.1 | Conflict visible at submission | JRN-01.1:Submit | Booking returns status + conflict flags immediately on save | US-1.1 |
| JTBD-01.1 | Conflict visible at submission | JRN-01.1:Submit | Conflict surfaced with conflicting booking id + conflict type | US-2.1 |
| JTBD-01.1 | Conflict visible at submission | JRN-01.1:Submit | Identical conflict logic across UI calendar, UI list, API | US-2.2 |
| JTBD-01.1 | Conflict visible at submission | JRN-01.2:Submit | Conflict re-evaluated fresh on every edit, multi-resource union | US-2.3 |
| JTBD-01.1 | Conflict visible at submission | JRN-03.1:Verify | location_id must reference a live, non-deleted location | US-1.1 |
| JTBD-01.2 | Safe recurring-series edits | JRN-01.2:Locate | Recurrence expands into occurrences sharing a series_id | US-1.6 |
| JTBD-01.2 | Safe recurring-series edits | JRN-01.2:Edit | Scope selection required on edit; omission rejected | US-1.2 |
| JTBD-01.2 | Safe recurring-series edits | JRN-01.2:Edit | Scope selection required on delete, matching edit rule | US-1.3 |
| JTBD-01.2 | Safe recurring-series edits | JRN-01.2:Confirm Scope | Single-occurrence edit leaves all other occurrences unmodified | US-1.2 |
| JTBD-01.3 | Reliable status notification | JRN-01.1:Confirm | Detail view reflects current status accurately | US-1.7 |
| JTBD-01.3 | Reliable status notification | JRN-01.1:Confirm | Status set per current approveBooking setting at creation | US-3.4 |
| JTBD-01.3 | Reliable status notification | JRN-01.1:Confirm | booking.created event reliably triggers confirmation email, no dupes | US-8.1 |
| JTBD-01.3 | Reliable status notification | JRN-01.2:Submit | Status set per current setting at edit-submission time | US-3.4 |
| JTBD-01.3 | Reliable status notification | JRN-01.2:Wait | Detail view reflects status accurately on demand | US-1.7 |
| JTBD-01.3 | Reliable status notification | JRN-01.2:Resolve | Decision event triggers email within minutes, retried on failure | US-8.2 |
| JTBD-01.4 | Self-service custom fields | JRN-01.1:Configure | Required fields render dynamically; submission blocked until complete | US-5.2 |
| JTBD-02.1 | Fast, conflict-flagged queue | JRN-02.1:Open Queue | Queue lists all pending, restricted to allowApproveBooking holders | US-3.1 |
| JTBD-02.1 | Fast, conflict-flagged queue | JRN-02.1:Scan | Conflict_flags inline, same underlying data as calendar/list | US-2.2 |
| JTBD-02.1 | Fast, conflict-flagged queue | JRN-02.1:Scan | Custom field values included in queue/detail representation | US-5.3 |
| JTBD-02.1 | Fast, conflict-flagged queue | JRN-02.1:Investigate | Conflict surfaced with conflicting id + location/resource type | US-2.1 |
| JTBD-02.2 | Trustworthy decision notification | JRN-02.1:Decide | Approval sets status/approved_by/approved_at, publishes event | US-3.2 |
| JTBD-02.2 | Trustworthy decision notification | JRN-02.1:Decide | Denial sets status/denied_by/denial_reason, publishes event | US-3.3 |
| JTBD-02.2 | Trustworthy decision notification | JRN-02.1:Decide | 100% of decision events delivered or retried-to-success, no silent drops | US-8.3 |
| JTBD-02.3 | Clear auto-approve boundary | JRN-02.1:Verify Boundary | Setting changes never retroactively change existing bookings | US-3.4 |
| JTBD-02.3 | Clear auto-approve boundary | JRN-02.1:Verify Boundary | Single settings record read at creation time, no ambiguity | US-10.1 |
| JTBD-02.4 | Defensible audit trail | JRN-02.2:Locate Booking | Full field set + current status returned for exact-record lookup | US-1.7 |
| JTBD-02.4 | Defensible audit trail | JRN-02.2:Query Audit Trail | Actor/timestamp/action_type/before-after returned, immutable | US-11.2 |
| JTBD-02.4 | Defensible audit trail | JRN-02.2:Resolve | Filtered queries return paginated authoritative entries | US-11.1 |
| JTBD-03.1 | Fast reference data setup | JRN-03.1:Navigate | Single form captures name, colour, building metadata | US-4.1 |
| JTBD-03.1 | Fast reference data setup | JRN-03.1:Create | Resources created independently, referenced by id only | US-4.2 |
| JTBD-03.1 | Fast reference data setup | JRN-03.1:Save | New/edited location immediately available for booking/conflict scope | US-4.1 |
| JTBD-03.2 | Confident permission migration | JRN-03.2:Review Mapping | Every referenced flag in mapping table; unconfirmed = deny-by-default | US-7.1 |
| JTBD-03.2 | Confident permission migration | JRN-03.2:Review Mapping | Only accessPermissions holders edit mappings; changes audited | US-7.2 |
| JTBD-03.2 | Confident permission migration | JRN-03.2:Run Boundary Test | Every validation/permission rule has a linked passing test | US-13.1 |
| JTBD-03.3 | Trustworthy settings propagation | JRN-03.2:Toggle Setting | Admin-only modification, recorded in audit log | US-10.1 |
| JTBD-03.3 | Trustworthy settings propagation | JRN-03.2:Toggle Setting | Slot size/time-range validated before propagation | US-10.2 |
| JTBD-03.3 | Trustworthy settings propagation | JRN-03.2:Confirm Propagation | Booking Service reads latest saved value at creation time | US-10.3 |
| JTBD-03.4 | Complete audit visibility | JRN-03.1:Log | Location/resource changes produce an audit entry, actor + timestamp | US-11.3 |
| JTBD-03.4 | Complete audit visibility | JRN-03.2:Review Audit Entries | Log filterable by entity type, actor, time range | US-11.1 |
| JTBD-03.4 | Complete audit visibility | JRN-03.2:Review Audit Entries | 100% of regression-suite state changes produce an audit entry | US-11.3 |
| JTBD-03.5 | Verified zero functional regression | JRN-03.2:Sign Off | Every legacy controller/model has an audit section with cited source | US-0.1 |
| JTBD-03.5 | Verified zero functional regression | JRN-03.2:Sign Off | Every Open Question cited-resolved or explicitly logged open | US-0.2 |
| JTBD-03.5 | Verified zero functional regression | JRN-03.2:Sign Off | Every audit finding linked to a requirement row, zero orphans | US-13.1 |
| JTBD-03.5 | Verified zero functional regression | JRN-03.2:Sign Off | CI blocks merge/release on any regressed previously-passing test | US-13.2 |
| JTBD-04.1 | Accurate, approved-only display | JRN-04.1:Glance | Board shows only status=approved, auto-refreshes client-side | US-9.1 |
| JTBD-04.1 | Accurate, approved-only display | JRN-04.1:Verify Approval | Pending/denied never exposed; refresh latency under 1 minute | US-9.4 |
| JTBD-04.2 | Self-syncing feed subscription | JRN-04.1:Set Up Feed | One VEVENT per approved booking, per-location filter, no resub | US-9.2 |
| JTBD-04.3 | Frictionless public access | JRN-04.1:Filter | Per-location filtering applies with no login required | US-9.1 |
| JTBD-04.3 | Frictionless public access | JRN-04.1:Set Up Feed | Access control identical across RSS2/iCal/JSON/display board | US-9.3 |

---

## Release Planning

### Release R1: Core Booking, Approval & Identity Foundation

**Theme:** Deliver the end-to-end create → conflict-check → approve/deny loop (F1, F2, F3) on top of a verified identity/permission substrate (F6, F7, F12) and the audit-first discipline that makes "zero functional loss" provable (F0, F13).

**Stories:** US-0.1, US-0.2, US-1.1, US-1.2, US-1.3, US-1.4, US-1.5, US-1.6, US-1.7, US-2.1, US-2.2, US-2.3, US-3.1, US-3.2, US-3.3, US-3.4, US-6.1, US-6.2, US-6.3, US-6.4, US-6.5, US-7.1, US-7.2, US-7.3, US-12.1, US-12.2, US-13.1, US-13.2

**Personas Served:** PER-01 (full JRN-01.1/JRN-01.2 minus notifications), PER-02 (full JRN-02.1 minus settings-boundary reference and JRN-02.2 minus audit query), PER-03 (permission migration + sign-off portion of JRN-03.2; "Verify" step of JRN-03.1)

**JTBD Addressed:** JTBD-01.1, JTBD-01.2, JTBD-01.3 (status visibility, not yet notification delivery), JTBD-02.1, JTBD-02.2 (decision recorded, not yet guaranteed delivery), JTBD-02.3 (partial — setting reference ships R3), JTBD-02.4 (booking locate only — audit query ships R3), JTBD-03.1 (partial — location must already exist), JTBD-03.2, JTBD-03.5

**Acceptance Gate:**
- [ ] All NaC for included stories pass
- [ ] Maya can create, edit-with-scope, and cancel-with-scope a booking end-to-end with live conflict feedback
- [ ] David can clear a pending queue with inline conflict flags and one-action approve/deny
- [ ] Priya can verify 100% of permission mappings via passing boundary tests and sign off using the F0→F13 traceability matrix
- [ ] Jordan is not yet served — flagged, not a gate failure, per release sequencing

---

### Release R2: Booking Completeness, Reference Data & Reliable Notifications

**Theme:** Complete the booking happy path with resources, custom fields, and location metadata (F4, F5), and replace "did that actually send?" uncertainty with durable, retried notifications (F8).

**Stories:** US-4.1, US-4.2, US-4.3, US-4.4, US-5.1, US-5.2, US-5.3, US-8.1, US-8.2, US-8.3, US-8.4

**Personas Served:** PER-01 (JRN-01.1 fully complete: resource attach, custom fields, colour-coding, submission notification; JRN-01.2 resolve-via-notification), PER-02 (custom-field-informed decisions, guaranteed decision delivery), PER-03 (full JRN-03.1 room-onboarding journey)

**JTBD Addressed:** JTBD-01.1 (colour-coded select stage), JTBD-01.3 (notification delivery), JTBD-01.4, JTBD-02.1 (custom-field visibility), JTBD-02.2 (guaranteed delivery), JTBD-03.1 (full onboarding journey)

**Acceptance Gate:**
- [ ] All NaC for included stories pass
- [ ] Maya completes a room+resource+custom-field booking end-to-end in under 2 minutes with zero follow-up emails
- [ ] 100% of approve/deny decisions result in a delivered (or retried-to-success) notification in staging load tests
- [ ] Priya onboards a new location/resource and it is immediately bookable, in under 5 minutes
- [ ] Release extends journey depth without breaking R1 flows

---

### Release R3: Visibility, Configuration & Compliance

**Theme:** Open the system to public, login-free visibility (F9), give Priya confirmed control over system-wide settings (F10), and close the audit loop for dispute resolution and compliance review (F11).

**Stories:** US-9.1, US-9.2, US-9.3, US-9.4, US-10.1, US-10.2, US-10.3, US-11.1, US-11.2, US-11.3

**Personas Served:** PER-04 (full JRN-04.1 — first release in which Jordan is served), PER-02 (full JRN-02.2 dispute resolution), PER-03 (settings + audit portions of JRN-03.2)

**JTBD Addressed:** JTBD-02.3 (settings-state reference), JTBD-02.4 (audit query/resolve), JTBD-03.3, JTBD-03.4, JTBD-04.1, JTBD-04.2, JTBD-04.3

**Acceptance Gate:**
- [ ] All NaC for included stories pass
- [ ] Jordan can complete JRN-04.1 end-to-end: glance, filter, verify-approved, and subscribe to iCal — with zero login prompts
- [ ] David resolves a disputed decision using only the per-booking audit trail, no external records
- [ ] A settings change (approveBooking, calendar params) is confirmed to propagate to the Booking Service with zero manual verification
- [ ] 100% of state-changing actions across all prior-release features now appear in the audit log

---

## Coverage Analysis

### Persona Coverage

| Persona | R1 | R2 | R3 |
|---------|----|----|-----|
| PER-01 (Maya) | US-1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 2.1, 2.2, 2.3, 3.4, 6.2, 6.3, 6.4 | US-4.4, 5.2, 8.1, 8.2, 8.4 | — |
| PER-02 (David) | US-1.7, 2.1, 2.2, 3.1, 3.2, 3.3, 3.4 | US-5.3, 8.3 | US-10.1, 11.1, 11.2 |
| PER-03 (Priya) | US-0.1, 0.2, 1.1, 6.1, 6.5, 7.1, 7.2, 7.3, 12.1, 12.2, 13.1, 13.2 | US-4.1, 4.2, 4.3, 5.1 | US-10.1, 10.2, 10.3, 11.1, 11.3 |
| PER-04 (Jordan) | — | — | US-9.1, 9.2, 9.3, 9.4 |

### JTBD Coverage

| JTBD ID | Release(s) | NaC Count | Notes |
|---------|-----------|-----------|-------|
| JTBD-01.1 | R1, R2 | 7 | R1 covers creation/conflict core; R2 adds colour-coded select stage |
| JTBD-01.2 | R1 | 4 | Fully addressed by R1 — recurring-edit scope is a creation-path concern |
| JTBD-01.3 | R1, R2 | 6 | R1 ships status visibility; R2 adds guaranteed notification delivery |
| JTBD-01.4 | R2 | 1 | Depends on F5, which ships in R2 |
| JTBD-02.1 | R1, R2 | 4 | R1 covers queue + conflict; R2 adds custom-field-informed decisions |
| JTBD-02.2 | R1, R2 | 3 | R1 records the decision; R2 guarantees delivery |
| JTBD-02.3 | R1, R3 | 2 | R1 covers non-retroactivity; R3 adds the settings-state cross-check |
| JTBD-02.4 | R1, R3 | 3 | R1 covers locate-booking only; audit query/resolve ship in R3 |
| JTBD-03.1 | R1, R2 | 4 | R1's "Verify" step assumes the location exists; full onboarding ships R2 |
| JTBD-03.2 | R1 | 3 | Fully addressed by R1 |
| JTBD-03.3 | R3 | 3 | Fully deferred to R3 — Settings epic (F10) ships there |
| JTBD-03.4 | R3 | 3 | Fully deferred to R3 — Audit epic (F11) ships there |
| JTBD-03.5 | R1 | 4 | Fully addressed by R1 |
| JTBD-04.1 | R3 | 2 | Fully deferred to R3 — Public Feeds epic (F9) ships there |
| JTBD-04.2 | R3 | 1 | Fully deferred to R3 |
| JTBD-04.3 | R3 | 2 | Fully deferred to R3 |

### Gap Analysis

- **No journey depicts a login/account-management stage.** F6 is P0, but none of the 7 journeys include an explicit "Sign In" or "Manage My Account" stage — US-6.1 (create account), US-6.2 (edit profile/password), US-6.3 (password reset), US-6.4 (log in/out), and US-6.5 (assign role) are **orphan stories**. Recommend adding an explicit pre-stage to JRN-01.1/JRN-02.1/JRN-03.1 in a future journey revision so identity flows have NaC coverage.
- **Jordan (PER-04) has zero story coverage in R1 and R2.** His entire journey (JRN-04.1) ships only in R3 — the public-feed persona is unserved for two full releases. Flag for roadmap discussion: a minimal read-only display board could plausibly move earlier if lobby signage is a launch-blocking integration.
- **Orphan stories (12 of 49) — not mapped to any journey stage:**
  - US-1.4 (Clone an Existing Booking) — no journey depicts cloning as a task
  - US-4.3 (Safely Remove a Location or Resource) — no journey depicts decommissioning a room
  - US-5.1 (Define Custom Field Templates) — Priya's only journey (JRN-03.1) covers rooms, not field templates
  - US-6.1, US-6.2, US-6.3, US-6.4, US-6.5 (identity/account management) — see login gap above
  - US-7.3 (gateway/service enforcement consistency) — cross-cutting, no single stage represents it
  - US-8.4 (password reset email) — no journey depicts the reset flow
  - US-12.1, US-12.2 (platform/gateway substrate) — expected orphans per PERSONAS-BookingHub.md: "F0, F12, and F13 are foundational engineering deliverables with no direct user-facing surface"
- **JTBD-02.4 (defensible audit trail) is only fully realized in R3.** David's JRN-02.2 journey cannot be completed end-to-end until the Audit epic (F11) ships, even though the dispute-trigger (approve/deny) ships in R1 — a 2-release gap between cause and resolution capability.
- **JTBD-03.1 (fast reference-data onboarding) has a forward dependency in R1.** The "Verify" stage of JRN-03.1 (confirming a new room is bookable) is placed in R1 against US-1.1, but the "Navigate/Create/Save" stages that actually create the room ship in R2 (F4) — sequencing note for sprint planning, not a functional gap.
- **No journey stage without any story coverage was found** among the 7 mapped journeys — every stage in every journey has at least one mapped story.

---

## NaC-to-Acceptance Criteria Mapping

| NaC | Story | AC from UserStories-BookingHub.md | Aligned? |
|-----|-------|-------------------------------------|----------|
| JTBD-01.1: Booking returns status + conflict flags immediately on save | US-1.1 | "On successful creation, the booking is returned with its assigned status (pending or approved) and any conflict flags" | Yes |
| JTBD-01.2: Scope selection required on edit; omission rejected | US-1.2 | "Editing a booking that belongs to a recurring series requires a scope selection (this occurrence or whole series); omitting scope is rejected with a clear error" | Yes |
| JTBD-01.3: Decision event triggers email within minutes, retried on failure | US-8.2 | "A booking.approved or booking.denied event triggers an email to the booking owner" + "Notification delivery is retried per a backoff policy on transient send failure" | Yes |
| JTBD-01.4: Required fields render dynamically; submission blocked until complete | US-5.2 | "The booking form dynamically renders only the custom fields applicable to the selected location's context" | Yes |
| JTBD-02.1: Conflict_flags inline, same underlying data as calendar/list | US-3.1 | "Each queued booking displays its conflict_flags inline" | Yes |
| JTBD-02.2: 100% of decision events delivered or retried-to-success, no silent drops | US-8.3 | "100% of booking.approved/booking.denied events result in a delivered or retried-to-success notification with no silent drops" | Yes |
| JTBD-02.3: Setting changes never retroactively change existing bookings | US-3.4 | "Changing the approveBooking flag never retroactively changes the status of already-created bookings" | Yes |
| JTBD-02.4: Actor/timestamp/action_type/before-after returned, immutable | US-11.2 | "Audit log entries for a booking include actor, timestamp, action_type, and before/after values where available" + "Audit log entries are immutable once written" | Yes |
| JTBD-03.1: New/edited location immediately available for booking/conflict scope | US-4.1 | "A new or edited location is immediately available to the Booking Service for selection and conflict scoping" | Yes |
| JTBD-03.2: Every referenced flag in mapping table; unconfirmed = deny-by-default | US-7.1 | "Every permission flag referenced by any other feature... appears in the mapping table before its gated action is implemented" + "A permission flag whose gating scope is unconfirmed is treated as restrictive (deny by default)" | Yes |
| JTBD-03.3: Booking Service reads latest saved value at creation time | US-10.3 | "The Booking Service reads the current approveBooking value at booking-creation time, reflecting the latest saved setting" | Yes |
| JTBD-03.4: 100% of regression-suite state changes produce an audit entry | US-11.3 | "100% of state-changing actions exercised in the regression suite produce a corresponding audit log entry" | Yes |
| JTBD-03.5: Every audit finding linked to a requirement row, zero orphans | US-13.1 | "Every F0 audit finding has at least one linked FRD requirement row with no orphaned findings" | Yes |
| JTBD-04.1: Board shows only status=approved, auto-refreshes client-side | US-9.1 | "Display board shows only bookings with status=approved; pending and denied bookings never appear" + "Display board auto-refreshes client-side to stay current" | Yes |
| JTBD-04.2: One VEVENT per approved booking, per-location filter, no resub | US-9.2 | "The iCal feed produces a standard .ics document with one VEVENT per approved upcoming booking" + "Per-location filtering is supported on the iCal feed the same way as other formats" | Yes |
| JTBD-04.3: Access control identical across RSS2/iCal/JSON/display board | US-9.3 | "Feed access control is evaluated identically regardless of requested format (RSS2, iCal, JSON, display board)" | Yes |

---

*Document generated by Pivota Spec Framework*
*Last updated: 2026-10-06*
