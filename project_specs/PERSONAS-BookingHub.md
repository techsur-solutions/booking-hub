# Personas
## Booking-Hub

| Field | Value |
|-------|-------|
| **Product Name** | Booking-Hub |
| **Date** | 2026-10-06 |
| **Related PRD** | PRD-BookingHub.md |

---

## Persona Summary

| ID | Name | Role | Primary Goal |
|----|------|------|-------------|
| PER-01 | Maya Torres | Marketing Coordinator (End User / Booker) | Book a conflict-free room or resource in under 2 minutes and know its status immediately |
| PER-02 | David Okafor | Facilities/Department Manager (Approver) | Review and decide on pending bookings quickly, with full conflict visibility and reliable notification delivery |
| PER-03 | Priya Patel | IT/Facilities System Administrator | Keep locations, resources, custom fields, users, permissions, and settings accurate and correctly enforced |
| PER-04 | Jordan Lee | Reception/Facilities Viewer (Public Feed Consumer) | See an always-current, trustworthy view of upcoming approved bookings without needing to log in |

---

## PER-01: Maya Torres

**Role & Context:**
Maya is a marketing coordinator at a mid-size organization who books meeting rooms and equipment 3-5 times per week for internal syncs, client calls, and recurring team stand-ups. She is not an administrator and has no special permissions beyond standard booking access (`allowRoomBooking`/`viewRoomBooking`). She typically books from her desktop browser between other tasks, often while already on a call or rushing between meetings, so speed and clarity matter more to her than configurability. She has no visibility into backend permission or settings logic — she only experiences booking as "does this work or not."

**Goals:**
- Find and book an available room or resource without stepping through a conflict she can't see until after submission (F1, F2)
- Set up a recurring weekly team sync once and trust that edits to a single occurrence don't silently break the whole series (F1)
- Attach required equipment (projector, conference phone) to a single booking without a separate request (F4)
- Know immediately whether her booking is pending or auto-approved, and get notified the moment it's approved or denied (F3, F8)
- Fill in any required custom fields (e.g., catering headcount) directly on the booking form instead of emailing facilities separately (F5)

**Pain Points:**
- Under the legacy system, slow monolithic page loads make it unclear whether a booking actually saved before she navigates away
- Conflict feedback is inconsistent between calendar and list views, so she sometimes only discovers a double-booking when she shows up to a room
- Email notifications are sent synchronously in the legacy request cycle with no retry — she has experienced bookings being approved with no confirmation email ever arriving
- No clear indication of whether editing one occurrence of a recurring series affects the whole series until after she saves

**Technical Expertise:** Intermediate — comfortable with modern web apps and calendar tools (Outlook/Google Calendar), has no patience for multi-step or ambiguous workflows

**Top Tasks:**
1. Create a new booking for a room with attached resources (daily, critical)
2. Check the status of a pending booking or view today's/this week's calendar (daily, critical)
3. Edit or cancel a single occurrence of a recurring booking (weekly, high)
4. Fill in required custom fields on a booking form (occasional, medium)
5. Respond to an approval/denial notification by rebooking if denied (weekly, medium)

**Success Criteria:**
- Can complete a room + resource booking end-to-end in under 2 minutes
- Zero instances of showing up to a double-booked room
- Receives a status notification (approved/denied) within minutes of the decision, 100% of the time

---

## PER-02: David Okafor

**Role & Context:**
David is a facilities/department manager with the `allowApproveBooking` permission for the locations his team oversees. He reviews a queue of pending bookings most mornings and throughout the day as new requests arrive, deciding whether to approve or deny based on conflict visibility, booking purpose, and sometimes custom field values (e.g., catering cost thresholds). He is not a system administrator — he cannot change locations, resources, or settings — his role is purely the approve/deny decision gate that the legacy `approveBooking` workflow routes through when auto-approve is disabled. He coordinates informally with Priya (PER-03) when a permission or settings issue blocks his ability to approve, and his decisions directly affect Maya's (PER-01) ability to use a room.

**Goals:**
- See every pending booking in a single queue with conflict status clearly flagged before deciding (F2, F3)
- Approve or deny a booking in one action, with the system correctly gating this to only users who hold `allowApproveBooking` (F3, F7)
- Trust that his approval or denial reliably triggers a notification to the requester without manual follow-up (F8)
- Understand exactly how auto-approve interacts with his review queue so bookings don't silently bypass his review when they shouldn't (F3, F10)
- Check the audit trail on a booking when a dispute arises about who approved/denied what and when (F11)

**Pain Points:**
- In the legacy system, the pending queue is slow to load and doesn't clearly surface which bookings have location/resource conflicts, forcing manual cross-checking
- Notification delivery is unreliable (synchronous, no retry), so requesters sometimes don't know a decision was made, leading to duplicate requests or confusion
- The legacy conflict-block-vs-warn behavior for approvers/admins is undocumented — he is unsure whether he can override a flagged conflict or whether the system silently prevents it
- No visibility into why a booking auto-approved versus needing his review, when the global setting changes

**Technical Expertise:** Intermediate — manages this task as one of several administrative responsibilities, expects straightforward web UI, does not use command-line or API tools directly

**Top Tasks:**
1. Review the pending booking queue and approve/deny with full conflict visibility (daily, critical)
2. Investigate a flagged conflict before deciding whether to approve anyway (as-needed, high)
3. Check audit history for a disputed booking decision (as-needed, medium)
4. Communicate denial reasons back to the requester when the system doesn't auto-explain (weekly, medium)
5. Confirm whether a new booking auto-approved or is awaiting his review (daily, low)

**Success Criteria:**
- 100% of his approve/deny decisions result in a delivered notification to the requester
- Average time-to-decision under 5 minutes per booking once in his queue
- Zero approvals granted to users who do not hold his effective permission level (verified by permission-boundary tests)

---

## PER-03: Priya Patel

**Role & Context:**
Priya is an IT/facilities system administrator responsible for the reference data and configuration that the rest of the organization depends on to book successfully: locations, resources, custom field templates, user accounts and role assignments, system-wide settings, and the permission matrix now re-implemented through Keycloak. She has full administrative access (`accessPermissions`, `allowAPI`, and equivalent admin scopes) and works in the admin screens several times a week — onboarding new rooms/equipment, adjusting the approval-required setting for specific periods, and auditing permission changes after a security review. She is the primary point of contact when David (PER-02) can't approve something or Maya (PER-01) can't see a resource she expects to see, and she is accountable for ensuring the legacy permission matrix was mapped correctly to Keycloak roles with no unintended access changes.

**Goals:**
- Create and maintain locations and resources (name, calendar colour, building grouping) so they appear correctly in booking and conflict-detection scope (F4)
- Define and maintain custom field templates so the right fields render dynamically on booking forms for the right booking types (F5)
- Manage user accounts and role assignments, confident that Keycloak-backed roles preserve the exact same effective access as the legacy permission matrix (F6, F7)
- Toggle the `approveBooking` setting and calendar display parameters (slot size, min/max time) and trust the change propagates correctly to the Booking Service (F10)
- Review the audit log to confirm every state-changing action (bookings, permissions, settings, resources) is captured with actor and timestamp (F11)
- Confirm, via the F0 audit output and F13 regression results, that no legacy permission flag or admin behavior was lost in the re-platform (F0, F13)

**Pain Points:**
- The legacy hand-rolled permission system is opaque — there is no single place to see "who can do what," making audits slow and error-prone
- No standards-based identity integration means she cannot extend access to enterprise SSO or easily revoke access across systems at once
- Settings changes in the legacy system have unclear propagation — she has no confidence a calendar slot-size change actually took effect everywhere until she manually checks multiple screens
- Legacy admin screens (Bootstrap 3/jQuery) are slow and clunky for repetitive tasks like onboarding multiple rooms at once

**Technical Expertise:** Expert — comfortable with Keycloak realms/roles, databases, and API-level verification; the primary technical power-user of the admin surface

**Top Tasks:**
1. Create/edit locations and resources, including calendar colour and building metadata (weekly, high)
2. Manage custom field definitions and templates (monthly, high)
3. Manage user accounts, role assignments, and verify permission-boundary behavior (weekly, critical)
4. Adjust system settings (`approveBooking` flag, calendar display parameters) (as-needed, high)
5. Review the audit log for compliance or to investigate an access-related issue (monthly, medium)

**Success Criteria:**
- 100% of legacy permission rules mapped to an equivalent Keycloak role/scope, verified by permission-boundary tests
- Can onboard a new location or resource in under 5 minutes
- Settings changes (e.g., `approveBooking` toggle) take effect across all dependent services with zero manual verification needed
- 100% of state-changing actions she performs appear in the audit log

---

## PER-04: Jordan Lee

**Role & Context:**
Jordan works reception/front-of-house at a location with multiple bookable meeting rooms. Jordan does not log into Booking-Hub and has no account — their entire interaction with the system is through the public, read-only surfaces: the lobby digital-signage display board showing upcoming approved bookings, and occasionally the iCal feed subscribed to their own calendar app to keep track of which rooms are busy during their shift. Jordan's job depends on this information being current and accurate, since visitors and employees ask at the front desk which room a meeting is in, and an out-of-date display sends people to the wrong place.

**Goals:**
- Glance at the display board and immediately see which rooms are booked now and coming up next, filtered to the lobby's location (F9, F4)
- Trust that what's shown on the board reflects only approved bookings, not pending/unconfirmed ones (F9, F1)
- Subscribe once to an iCal feed and have it stay in sync without manual refreshing (F9)
- Not need a login or special permission just to see public booking visibility, consistent with how the legacy `allowAPI`-gated feed was intended to work for public/semi-public use (F9, F7)

**Pain Points:**
- Under the legacy system, it's unclear whether the display board or feed reliably shows only current, approved data or sometimes stale/cached information
- No visibility into whether feed access rules (`allowAPI`) are meant to be fully public or token-gated, leading to inconsistent access experiences across formats (RSS/iCal/JSON/display board)
- The legacy feed/display surface is a lower priority within the monolith, so it occasionally lags behind real booking activity during high load

**Technical Expertise:** Novice — not a system user in the administrative sense; interacts only with a passive screen or a calendar subscription set up once by someone else

**Top Tasks:**
1. Read the auto-refreshing lobby display board to direct visitors to the correct room (continuous during shift, critical)
2. Filter the display/feed view to their specific location when multiple buildings are shown (occasional, medium)
3. Subscribe to the iCal feed once so bookings for their location sync to a personal/shared calendar (one-time setup, medium)
4. Confirm a booking is actually approved (not just pending) before directing a visitor to a room (daily, high)

**Success Criteria:**
- Display board refresh latency under 1 minute from the moment a booking is approved
- Feed/display data matches the authoritative approved-booking state 100% of the time
- Zero visitor misdirection incidents attributable to stale or incorrect feed/display data

---

## Persona Relationships

| Persona | Interacts With | Nature of Interaction |
|---------|---------------|----------------------|
| PER-01 (Maya) | PER-02 (David) | Maya's bookings enter David's approval queue when auto-approve is disabled; David's decision determines whether Maya's booking is confirmed |
| PER-01 (Maya) | PER-03 (Priya) | Maya depends on the locations, resources, and custom field templates Priya maintains being accurate and available when she books |
| PER-02 (David) | PER-03 (Priya) | David escalates to Priya when permission or settings issues block his ability to approve, or when a location/resource needs correction |
| PER-03 (Priya) | PER-04 (Jordan) | Priya configures per-location feed filtering and `allowAPI` access rules that determine what Jordan sees on the display board/feed |
| PER-01 (Maya) | PER-04 (Jordan) | Maya's approved bookings are the source data Jordan relies on to direct visitors correctly at reception |

---

## Feature-Persona Matrix

| Feature | PER-01 (Maya) | PER-02 (David) | PER-03 (Priya) | PER-04 (Jordan) |
|---------|--------|--------|--------|--------|
| F0: Legacy Functional Audit | — | — | Secondary | — |
| F1: Booking Management Service | Primary | Secondary | — | Secondary |
| F2: Conflict Detection | Primary | Primary | — | — |
| F3: Approval Workflow | Secondary | Primary | — | — |
| F4: Locations & Resources Management | Secondary | — | Primary | Secondary |
| F5: Custom Fields | Secondary | Secondary | Primary | — |
| F6: User & Role Management | Secondary | Secondary | Primary | — |
| F7: Permission System | — | Secondary | Primary | Secondary |
| F8: Notifications | Primary | Primary | Secondary | — |
| F9: Public Feeds | — | — | Secondary | Primary |
| F10: Settings | — | Secondary | Primary | — |
| F11: Activity/Audit Logging | — | Secondary | Primary | — |
| F12: Microservice Architecture & Platform | — | — | Secondary | — |
| F13: Regression Verification & Test Traceability | — | — | Secondary | — |

*Note: F0, F12, and F13 are foundational engineering deliverables with no direct user-facing surface. Priya (PER-03) is marked Secondary for these because, as system administrator, she is the closest persona to consuming their outputs (audit findings, platform operation, regression sign-off) — not because end users interact with them directly.*

---

*Document generated by Pivota Spec Framework*
*Last updated: 2026-10-06*
