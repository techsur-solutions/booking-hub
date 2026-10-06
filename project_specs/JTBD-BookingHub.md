# Jobs to Be Done
## Booking-Hub

| Field | Value |
|-------|-------|
| **Product Name** | Booking-Hub |
| **Date** | 2026-10-06 |
| **Related Personas** | PERSONAS-BookingHub.md |
| **Related PRD** | PRD-BookingHub.md |

---

## JTBD Summary

*Note: **Job Urgency** (P0/P1/P2) reflects how pressing a job is for the persona experiencing it — it is a distinct scale from the release-sequencing **Priority** used in UserStories-BookingHub.md and the Story Map. A P2 job (e.g., JTBD-02.4, JTBD-04.3) is still fully in scope for this release; it is simply less urgent to the persona than a P0 job, not deprioritized out of the release.*

| ID | Persona | Job Statement | Job Urgency |
|----|---------|--------------|----------|
| JTBD-01.1 | PER-01 | When I need to book a room between other tasks, I want conflict status visible at submission, so I can trust the booking is mine without a later double-booking surprise. | P0 |
| JTBD-01.2 | PER-01 | When I edit or cancel one occurrence of a recurring series, I want explicit control over scope, so I can avoid unintentionally changing other weeks. | P0 |
| JTBD-01.3 | PER-01 | When I submit a booking requiring approval, I want immediate status visibility and reliable decision notifications, so I can rebook quickly if denied. | P0 |
| JTBD-01.4 | PER-01 | When my booking type needs extra details, I want to fill custom fields on the form itself, so I can avoid a separate email to facilities. | P1 |
| JTBD-02.1 | PER-02 | When I review my pending queue, I want every booking's conflict status flagged inline, so I can decide quickly without manual cross-checking. | P0 |
| JTBD-02.2 | PER-02 | When I approve or deny a booking, I want the requester reliably notified without manual follow-up, so I can avoid duplicate requests. | P0 |
| JTBD-02.3 | PER-02 | When auto-approve settings or new bookings appear, I want clarity on what bypassed my review, so I can trust my queue is complete. | P1 |
| JTBD-02.4 | PER-02 | When a dispute arises over a decision, I want to check the audit trail, so I can resolve it with an authoritative record. | P2 |
| JTBD-03.1 | PER-03 | When I onboard a new room or resource, I want to set it up in one pass, so I can make it bookable immediately without follow-up fixes. | P1 |
| JTBD-03.2 | PER-03 | When I map a legacy permission to Keycloak, I want verified equivalence, so I can be confident no access changed unintentionally. | P0 |
| JTBD-03.3 | PER-03 | When I toggle a system-wide setting, I want immediate, confirmed propagation, so I can trust the change without manually checking every screen. | P1 |
| JTBD-03.4 | PER-03 | When I perform compliance review, I want every state-changing action logged, so I can answer "who did what, when" without gaps. | P1 |
| JTBD-03.5 | PER-03 | When the re-platform nears release, I want audit-to-test traceability confirming zero functional loss, so I can sign off with confidence. | P0 |
| JTBD-04.1 | PER-04 | When someone asks which room a meeting is in, I want an always-current, approved-only display, so I can direct people correctly. | P1 |
| JTBD-04.2 | PER-04 | When I want to track room status passively, I want a self-syncing calendar subscription, so I can glance at my own calendar app instead. | P1 |
| JTBD-04.3 | PER-04 | When I check booking visibility at the front desk, I want access without a login, so I can do my job without IT involvement. | P2 |

---

## PER-01: Maya Torres — Jobs

### JTBD-01.1: Conflict-Free Quick Booking

**Job Statement:**
When I need to book a room for an internal sync or client call between other tasks, I want to create a booking with attached resources and see conflict status immediately, so I can confirm the space is mine without later discovering a double-booking.

**Current Alternatives:**
- Manually cross-references multiple calendars/spreadsheets before booking
- Books anyway and only discovers a conflict upon arriving at the room

**Hiring Criteria:**
- Conflict check runs at submission and on every edit, consistent between calendar and list views
- Resource attachment happens in the same form as the booking, with no separate request
- Visible save confirmation appears before she navigates away

**Success Measure:** Maya completes a room+resource booking end-to-end in under 2 minutes with zero double-booking incidents.

**Related Features:** F1, F2, F4
**Job Urgency:** P0

---

### JTBD-01.2: Trustworthy Recurring Series Management

**Job Statement:**
When I set up a recurring weekly team sync and later need to edit or cancel a single occurrence, I want clear control over whether my change applies to just that occurrence or the whole series, so I can make adjustments without unintentionally breaking other weeks' bookings.

**Current Alternatives:**
- Avoids editing recurring bookings at all, deleting and recreating the whole series out of caution
- Asks Priya to manually repair bookings after an edit unexpectedly affects the entire series

**Hiring Criteria:**
- System explicitly asks for and displays edit scope (single occurrence vs. whole series) before applying a change
- Confirmation that an edit to one occurrence leaves all other occurrences untouched

**Success Measure:** Zero recurring-series edits produce unintended changes to other occurrences, across 100% of recurring-booking edit actions.

**Related Features:** F1
**Job Urgency:** P0

---

### JTBD-01.3: Reliable Status Awareness

**Job Statement:**
When I submit a booking that requires approval, I want to know immediately whether it's pending or auto-approved and be notified the moment a decision is made, so I can rebook elsewhere right away if it's denied instead of finding out too late.

**Current Alternatives:**
- Repeatedly checks the booking list manually for a status change
- Waits on email notifications that sometimes never arrive, leading to missed meetings

**Hiring Criteria:**
- Booking status (pending/approved/denied) is visible at a glance immediately after submission
- Notification is delivered reliably within minutes of a decision, with retry on failure

**Success Measure:** 100% of status decisions result in a delivered notification to Maya within minutes of the decision.

**Related Features:** F3, F8
**Job Urgency:** P0

---

### JTBD-01.4: Custom Field Completion Without Separate Request

**Job Statement:**
When my booking type requires additional details such as catering headcount, I want to fill in those fields directly on the booking form, so I can avoid a separate email to facilities and reduce back-and-forth.

**Current Alternatives:**
- Emails facilities separately after submitting the booking
- Forgets to communicate required details, causing delays or errors on the day of the event

**Hiring Criteria:**
- Required custom fields render dynamically on the form based on the booking type
- Form validates required fields before allowing submission

**Success Measure:** Zero post-booking emails required to communicate custom-field details for bookings with applicable field templates.

**Related Features:** F5
**Job Urgency:** P1

---

## PER-02: David Okafor — Jobs

### JTBD-02.1: Fast, Conflict-Aware Queue Review

**Job Statement:**
When I review my pending booking queue each morning, I want to see every pending booking with conflict status clearly flagged, so I can approve or deny each one quickly without manually cross-checking for conflicts.

**Current Alternatives:**
- Manually cross-references calendar/list views to spot conflicts before deciding
- Delays decisions until there's time to manually verify, slowing down requesters

**Hiring Criteria:**
- Pending queue loads quickly and surfaces conflict status inline per booking
- Approve/deny is achievable in a single action per booking
- Queue only shows bookings David holds permission to act on

**Success Measure:** Average time-to-decision under 5 minutes per booking once it enters his queue.

**Related Features:** F2, F3
**Job Urgency:** P0

---

### JTBD-02.2: Trustworthy Decision Notification

**Job Statement:**
When I approve or deny a booking, I want the requester to be reliably notified of my decision without manual follow-up, so I can avoid duplicate requests and confused stakeholders.

**Current Alternatives:**
- Manually emails or messages the requester after deciding, as a backup to the system notification
- Fields follow-up questions from requesters unsure whether a decision was ever made

**Hiring Criteria:**
- Every approve/deny action triggers a durable, retried-on-failure notification event
- No manual step is required to notify the requester

**Success Measure:** 100% of David's approve/deny decisions result in a delivered notification to the requester.

**Related Features:** F3, F8
**Job Urgency:** P0

---

### JTBD-02.3: Clear Auto-Approve Boundary

**Job Statement:**
When the auto-approve setting changes or a new booking appears, I want to understand immediately whether it auto-approved or is awaiting my review, so I can trust my queue reflects exactly what needs my decision and nothing silently bypasses review.

**Current Alternatives:**
- Assumes all bookings need review and double-checks status manually
- Escalates to Priya whenever queue behavior seems inconsistent with expectations

**Hiring Criteria:**
- Booking status/history indicates whether it was auto-approved or routed through explicit approval
- Queue composition updates immediately and visibly after a settings change

**Success Measure:** Zero instances where a booking auto-approves when David expected it to require his review, verified against current settings state.

**Related Features:** F3, F10
**Job Urgency:** P1

---

### JTBD-02.4: Defensible Audit Trail on Dispute

**Job Statement:**
When a dispute arises about who approved or denied a booking and when, I want to check the audit trail for that booking, so I can resolve the disagreement with an authoritative record rather than relying on memory or email threads.

**Current Alternatives:**
- Searches old emails to reconstruct who decided what and when
- Relies on his own recollection, which is often disputed

**Hiring Criteria:**
- Audit trail shows actor, timestamp, and action for every approve/deny decision on a booking
- Audit entries are queryable per booking, not only system-wide

**Success Measure:** 100% of disputed booking decisions resolved using the audit trail with no reliance on external email records.

**Related Features:** F11
**Job Urgency:** P2

---

## PER-03: Priya Patel — Jobs

### JTBD-03.1: Fast, Accurate Reference Data Setup

**Job Statement:**
When I onboard a new room or piece of equipment, I want to create or edit the location/resource with its calendar colour and building grouping in one pass, so I can make it immediately available for booking and conflict detection without follow-up fixes.

**Current Alternatives:**
- Juggles multiple slow legacy admin screens to set up a single room or resource
- Onboards items in bulk reluctantly due to clunky Bootstrap 3/jQuery screens

**Hiring Criteria:**
- Single form captures name, colour, and building metadata for a location/resource
- New location/resource is immediately visible and selectable in the booking flow and conflict scope

**Success Measure:** Priya can onboard a new location or resource in under 5 minutes.

**Related Features:** F4
**Job Urgency:** P1

---

### JTBD-03.2: Confident Permission Migration Verification

**Job Statement:**
When I map a legacy permission rule to its Keycloak-backed equivalent, I want to verify that the new role/scope grants exactly the same effective access as before, so I can be confident no user gains or loses access unintentionally during the re-platform.

**Current Alternatives:**
- Manually tests each permission combination by impersonating different roles
- Relies on code review alone, with no systematic verification

**Hiring Criteria:**
- Every legacy permission flag has a documented, testable Keycloak role/scope mapping
- Permission-boundary tests exist and pass for each mapped permission

**Success Measure:** 100% of legacy permission rules mapped to an equivalent Keycloak role/scope, verified by passing permission-boundary tests.

**Related Features:** F6, F7, F13
**Job Urgency:** P0

---

### JTBD-03.3: Trustworthy Settings Propagation

**Job Statement:**
When I toggle a system-wide setting like the approval requirement or calendar slot size, I want the change to take effect across all dependent services immediately, so I can trust the system behaves correctly without manually checking every screen.

**Current Alternatives:**
- Manually opens multiple legacy screens after a settings change to confirm it took effect
- Waits and hopes the change propagated correctly, discovering failures only when users complain

**Hiring Criteria:**
- Settings change is confirmed to propagate to all dependent services without manual verification
- A single settings screen reflects current effective state, not a cached or stale one

**Success Measure:** Settings changes take effect across all dependent services with zero manual verification steps needed, confirmed in 100% of settings-change test cases.

**Related Features:** F10
**Job Urgency:** P1

---

### JTBD-03.4: Complete Audit Visibility

**Job Statement:**
When I perform compliance review or investigate an access-related issue, I want every state-changing action across bookings, permissions, settings, and resources captured in the audit log, so I can answer "who did what, when" without gaps.

**Current Alternatives:**
- Pieces together partial information from scattered legacy logs and manual notes
- Cannot fully answer audit questions due to incomplete legacy logging

**Hiring Criteria:**
- Every state-changing action (create/edit/delete/approve/deny across all entities) produces an audit log entry with actor and timestamp
- Audit log is filterable by entity type, actor, and time range

**Success Measure:** 100% of state-changing actions appear in the audit log, verified across all entity types.

**Related Features:** F11
**Job Urgency:** P1

---

### JTBD-03.5: Verified Zero Functional Regression

**Job Statement:**
When the re-platform nears release, I want to confirm via the audit findings and regression test results that no legacy permission flag or admin behavior was lost, so I can sign off on the migration with confidence rather than hope.

**Current Alternatives:**
- Manually spot-checks a sample of legacy behaviors against the new system
- Relies on developer assurance without independent verification

**Hiring Criteria:**
- Traceability matrix links every F0 audit finding to a requirement and a passing test
- Open Questions list shows 100% resolution (explicit decision or accepted out-of-scope) before sign-off

**Success Measure:** 100% of F0-documented legacy features have a corresponding implemented requirement and at least one passing automated test before Priya signs off on release.

**Related Features:** F0, F13
**Job Urgency:** P0

---

## PER-04: Jordan Lee — Jobs

### JTBD-04.1: Trustworthy At-a-Glance Room Status

**Job Statement:**
When a visitor or employee asks which room a meeting is in, I want to glance at the lobby display board and see only approved, up-to-date bookings for my location, so I can direct people to the correct room with confidence.

**Current Alternatives:**
- Calls or messages facilities to confirm a booking before directing a visitor
- Guesses based on a stale or cached display, occasionally misdirecting visitors

**Hiring Criteria:**
- Display board shows only approved bookings, never pending/unconfirmed ones
- Board refreshes automatically and reflects new approvals within a short, predictable window
- Board is filterable/scoped to Jordan's specific location

**Success Measure:** Display board refresh latency under 1 minute from the moment a booking is approved, with zero visitor misdirection incidents attributable to stale data.

**Related Features:** F9, F1, F4
**Job Urgency:** P1

---

### JTBD-04.2: Set-and-Forget Calendar Sync

**Job Statement:**
When I want to track which rooms at my location are busy during my shift without constantly checking a separate screen, I want to subscribe once to an iCal feed that stays in sync automatically, so I can glance at my own calendar app instead of a shared display.

**Current Alternatives:**
- Repeatedly reopens the display board or asks colleagues for status updates
- Maintains a manually updated personal note of known bookings, which quickly goes stale

**Hiring Criteria:**
- iCal feed subscription requires no login or special permission to set up
- Feed updates automatically without requiring Jordan to manually refresh or resubscribe

**Success Measure:** Feed data matches the authoritative approved-booking state 100% of the time, with no manual refresh required after initial subscription.

**Related Features:** F9
**Job Urgency:** P1

---

### JTBD-04.3: Frictionless Public Access

**Job Statement:**
When I need to check booking visibility as part of my front-desk role, I want to access the public feed or display board without needing a login or special account, so I can do my job without IT involvement or access requests.

**Current Alternatives:**
- Requests a login/account from IT just to view read-only booking information
- Relies on someone else with an account to check and relay booking status

**Hiring Criteria:**
- Public feed/display surfaces are accessible without authentication, consistent with the intended `allowAPI` public/semi-public access
- Access behavior is consistent across RSS/iCal/JSON/display-board formats

**Success Measure:** Zero login prompts or access-denied errors encountered when accessing public feed/display surfaces across all formats.

**Related Features:** F9, F7
**Job Urgency:** P2

---

## Outcome-to-Feature Traceability

| JTBD ID | Feature | Expected Outcome |
|---------|---------|-----------------|
| JTBD-01.1 | F1, F2, F4 | Room + resource booking completes in under 2 minutes with zero double-booking incidents |
| JTBD-01.2 | F1 | Recurring-series edits never unintentionally alter other occurrences |
| JTBD-01.3 | F3, F8 | 100% of status decisions deliver a notification within minutes |
| JTBD-01.4 | F5 | Zero post-booking emails needed to communicate custom-field details |
| JTBD-02.1 | F2, F3 | Average time-to-decision under 5 minutes per queued booking |
| JTBD-02.2 | F3, F8 | 100% of approve/deny decisions deliver a notification to the requester |
| JTBD-02.3 | F3, F10 | Zero unexpected auto-approvals relative to current settings state |
| JTBD-02.4 | F11 | 100% of disputed decisions resolved via audit trail, no external records needed |
| JTBD-03.1 | F4 | New location/resource onboarded and bookable in under 5 minutes |
| JTBD-03.2 | F6, F7, F13 | 100% of legacy permission rules mapped and verified via Keycloak scopes |
| JTBD-03.3 | F10 | Settings changes propagate to all dependent services with zero manual verification |
| JTBD-03.4 | F11 | 100% of state-changing actions produce an audit log entry |
| JTBD-03.5 | F0, F13 | 100% of F0-documented features have an implemented requirement and passing test |
| JTBD-04.1 | F9, F1, F4 | Display board refresh latency under 1 minute; zero misdirection incidents |
| JTBD-04.2 | F9 | Feed data matches approved-booking state 100% of the time, no manual refresh |
| JTBD-04.3 | F9, F7 | Zero login prompts/access-denied errors on public feed/display surfaces |

---

## NaC Preview

| JTBD ID | Outcome | Candidate NaC |
|---------|---------|--------------|
| JTBD-01.1 | Fast, conflict-free booking | Given an available room and resource, when Maya submits a booking, then it saves in under 2 minutes and no conflicting booking is later accepted for the same slot |
| JTBD-01.2 | Safe recurring-series edits | Given a recurring booking series, when Maya edits a single occurrence, then only that occurrence changes and all other occurrences remain unmodified |
| JTBD-01.3 | Reliable status notification | Given a pending booking, when an approver makes a decision, then Maya receives a notification within minutes, 100% of the time |
| JTBD-01.4 | Self-service custom fields | Given a booking type with required custom fields, when Maya submits the form without them, then submission is blocked until the fields are completed |
| JTBD-02.1 | Fast, conflict-flagged queue review | Given a pending queue with conflicting and non-conflicting bookings, when David opens the queue, then every conflicting booking is visibly flagged without manual cross-checking |
| JTBD-02.2 | Reliable decision notification | Given David approves or denies a booking, when the action is confirmed, then the requester receives a notification, with retry on initial delivery failure |
| JTBD-02.3 | Transparent auto-approve boundary | Given the `approveBooking` setting is toggled, when a new booking is created, then its status (auto-approved vs. pending) matches the current setting state with no ambiguity in the queue |
| JTBD-02.4 | Authoritative audit trail | Given a disputed approval decision, when David queries the audit log for that booking, then actor, timestamp, and action are returned |
| JTBD-03.1 | Fast reference data onboarding | Given a new room, when Priya submits the location form with colour and building metadata, then it is immediately selectable in the booking flow in under 5 minutes |
| JTBD-03.2 | Verified permission parity | Given a legacy permission flag, when its Keycloak-equivalent role/scope is tested, then the permission-boundary test passes for every mapped permission |
| JTBD-03.3 | Confirmed settings propagation | Given a settings change (e.g., `approveBooking` toggle), when it is saved, then all dependent services reflect the new value without manual verification |
| JTBD-03.4 | Complete audit coverage | Given any state-changing action across bookings, permissions, settings, or resources, when it occurs, then a corresponding audit log entry with actor and timestamp exists |
| JTBD-03.5 | Zero functional regression sign-off | Given the F0 audit inventory, when the regression suite runs, then every documented legacy feature has at least one passing test and the traceability matrix shows 100% coverage |
| JTBD-04.1 | Accurate, approved-only display | Given a booking is approved, when the display board next refreshes, then it reflects the approval within 1 minute and shows no pending/unconfirmed bookings |
| JTBD-04.2 | Self-syncing feed subscription | Given Jordan subscribes once to the iCal feed, when a booking changes, then the feed reflects the authoritative state without any manual resubscription or refresh |
| JTBD-04.3 | Frictionless public access | Given Jordan has no account, when they access the public feed or display board, then no login prompt or access-denied error occurs across all supported formats |

---

*Document generated by Pivota Spec Framework*
*Last updated: 2026-10-06*
