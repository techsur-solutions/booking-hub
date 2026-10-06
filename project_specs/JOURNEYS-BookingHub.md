# User Journeys
## Booking-Hub

| Field | Value |
|-------|-------|
| **Product Name** | Booking-Hub |
| **Date** | 2026-10-06 |
| **Related Personas** | PERSONAS-BookingHub.md |
| **Related JTBD** | JTBD-BookingHub.md |
| **Related PRD** | PRD-BookingHub.md |

---

## Journey Index

| ID | Persona | Scenario | Key JTBD | Stages |
|----|---------|----------|----------|--------|
| JRN-01.1 | PER-01 | Maya books a room with attached equipment between meetings and needs to trust it's conflict-free | JTBD-01.1, JTBD-01.4 | 5 |
| JRN-01.2 | PER-01 | Maya edits one occurrence of her recurring weekly sync, then waits on an approval decision | JTBD-01.2, JTBD-01.3 | 6 |
| JRN-02.1 | PER-02 | David clears his morning pending-booking queue with conflict visibility | JTBD-02.1, JTBD-02.2, JTBD-02.3 | 5 |
| JRN-02.2 | PER-02 | David investigates a disputed approval decision using the audit trail | JTBD-02.4 | 4 |
| JRN-03.1 | PER-03 | Priya onboards a new meeting room so it's immediately bookable | JTBD-03.1 | 5 |
| JRN-03.2 | PER-03 | Priya verifies a Keycloak permission migration and confirms a settings change propagated | JTBD-03.2, JTBD-03.3, JTBD-03.4, JTBD-03.5 | 6 |
| JRN-04.1 | PER-04 | Jordan directs a visitor using the lobby display board and sets up a personal iCal feed | JTBD-04.1, JTBD-04.2, JTBD-04.3 | 5 |

---

## PER-01: Maya Torres

### JRN-01.1: Quick Conflict-Free Room Booking with Resources

**Persona:** PER-01 (Maya Torres)
**Scenario:** Maya has five minutes between calls and needs to book a room for a client call later today, attaching a conference phone and filling in a required catering headcount field, without later discovering someone else also has the room.
**Related Jobs:** JTBD-01.1, JTBD-01.4

#### Journey Stages

| Stage | Action | Touchpoint | Thinking | Feeling | Pain Point | Opportunity |
|-------|--------|------------|----------|---------|------------|-------------|
| Discover | Opens the booking calendar to find an open slot for the client call | Calendar view (F1) | "Which rooms are free at 2pm?" | Rushed | Hard to tell at a glance which rooms are truly free vs. tentatively held | Color-code availability directly on the calendar grid |
| Select | Clicks an open slot on the room she wants and opens the new-booking form | Booking creation form (F1, F4) | "This looks open — let me grab it before someone else does" | Mildly anxious | Not sure yet if a resource (phone) is also free at this time | Show resource availability inline as soon as a slot is selected |
| Configure | Attaches a conference phone resource and fills in the required catering headcount custom field | Booking form — resource picker (F4) + custom field (F5) | "I need the phone too, and I think catering needs a number" | Focused | In the legacy system this meant a separate email to facilities | Dynamic field rendering means no separate request needed |
| Submit | Clicks Save and watches for conflict feedback before navigating away | Booking form submission (F1, F2) | "Did that actually go through? Is this room really mine now?" | Tense, waiting | Legacy system gave inconsistent conflict feedback between calendar/list views | Instant, consistent conflict check with visible save confirmation |
| Confirm | Sees a clear confirmation with booking status and returns to her prior task | Booking confirmation / detail view (F1, F3) | "Good, it's confirmed — on to the next thing" | Relieved | None — confirmation is immediate and unambiguous | Toast/banner confirmation that persists briefly even after navigating away |

#### Key Moments
- **Decision Point:** Select stage — if the room's true availability is unclear, Maya may pick a slot that turns out double-booked, eroding trust in the system before submission even happens.
- **Risk of Abandonment:** Submit stage — if conflict/save feedback is slow or ambiguous, Maya navigates away unsure whether the booking saved, as happened routinely in the legacy system.
- **Delight Opportunity:** Confirm stage — an instant, unambiguous "you're booked" state removes the anxious double-checking Maya currently does out of habit.

#### Success Outcome
Maya completes the room + resource booking end-to-end in under 2 minutes with zero double-booking incidents (JTBD-01.1 success measure), and submits zero follow-up emails to facilities for the catering detail (JTBD-01.4 success measure).

#### Feature Touchpoints

| Stage | Features |
|-------|----------|
| Discover | F1 |
| Select | F1, F4 |
| Configure | F4, F5 |
| Submit | F1, F2 |
| Confirm | F1, F3 |

---

### JRN-01.2: Recurring Series Edit and Approval Wait

**Persona:** PER-01 (Maya Torres)
**Scenario:** Maya's weekly team sync is a recurring booking. This week only, the meeting needs to move 30 minutes later because of a conflicting client call, but every other week should stay untouched. The room in question requires approval, so Maya also has to wait on a decision and be ready to rebook elsewhere if denied.
**Related Jobs:** JTBD-01.2, JTBD-01.3

#### Journey Stages

| Stage | Action | Touchpoint | Thinking | Feeling | Pain Point | Opportunity |
|-------|--------|------------|----------|---------|------------|-------------|
| Locate | Finds this week's occurrence of the recurring sync in the calendar | Calendar view (F1) | "There it is — just this one needs to move" | Neutral | Recurring entries look identical to single bookings until opened | Visual badge distinguishing recurring occurrences |
| Edit | Opens the occurrence and changes the start time, triggering an edit-scope prompt | Booking edit form (F1) | "Is this going to change every other week too?" | Anxious | In the legacy system this was unclear until after saving, sometimes breaking the whole series | Explicit scope choice ("this occurrence only" vs. "entire series") shown before save |
| Confirm Scope | Selects "this occurrence only" and sees the change applied | Scope confirmation dialog (F1) | "Good, it told me exactly what would happen" | Reassured | None — the explicit prompt removes the guesswork | Post-save summary confirming other occurrences are untouched |
| Submit | Saves the single-occurrence edit, which requires approval since it changed the time | Booking form submission (F1, F2, F3) | "Now it's up to David to approve this new time" | Slightly tense | Doesn't know how long approval will take | Immediate status badge: "Pending approval" visible right after save |
| Wait | Checks booking status periodically while continuing other work | Booking list / status badge (F3, F8) | "Has David looked at this yet?" | Mildly anxious, distracted | In the legacy system, notification emails sometimes never arrived, leaving her unsure | Reliable, retried notification the moment a decision is made |
| Resolve | Receives a notification that the booking was approved (or denied) and acts accordingly | Email/in-app notification (F8) | "Approved — good, it's locked in" / "Denied — I need to rebook now" | Relieved, or determined if denied | None if notification arrives promptly; otherwise missed-meeting risk | One-click "find another slot" action embedded in a denial notification |

#### Key Moments
- **Decision Point:** Edit stage — the scope prompt is the single most consequential moment; getting this wrong historically meant Maya avoided editing recurring bookings altogether, deleting and recreating whole series out of caution.
- **Risk of Abandonment:** Wait stage — if no notification arrives, Maya either shows up assuming approval or manually re-checks repeatedly, both of which damage trust.
- **Delight Opportunity:** Resolve stage — a denial notification that offers an immediate rebooking path turns a negative moment into a fast recovery.

#### Success Outcome
Zero recurring-series edits produce unintended changes to other occurrences (JTBD-01.2 success measure), and Maya receives a delivered status notification within minutes of David's decision, 100% of the time (JTBD-01.3 success measure).

#### Feature Touchpoints

| Stage | Features |
|-------|----------|
| Locate | F1 |
| Edit | F1 |
| Confirm Scope | F1 |
| Submit | F1, F2, F3 |
| Wait | F3, F8 |
| Resolve | F8 |

---

## PER-02: David Okafor

### JRN-02.1: Morning Pending-Queue Review

**Persona:** PER-02 (David Okafor)
**Scenario:** David starts his day by clearing the pending-booking queue for the locations he manages, needing to quickly see which requests have conflicts and which were already auto-approved under current settings, so he can decide confidently without manual cross-checking.
**Related Jobs:** JTBD-02.1, JTBD-02.2, JTBD-02.3

#### Journey Stages

| Stage | Action | Touchpoint | Thinking | Feeling | Pain Point | Opportunity |
|-------|--------|------------|----------|---------|------------|-------------|
| Open Queue | Logs in and navigates to the pending-booking queue | Approval queue (F3) | "How many are waiting on me today?" | Neutral, slightly braced | Legacy queue was slow to load, delaying his whole morning routine | Fast-loading queue scoped only to locations he can act on |
| Scan | Scans the list for bookings flagged with conflicts | Queue list with conflict flags (F2, F3) | "Which of these actually have a problem?" | Focused | Legacy system didn't clearly surface conflicts inline, forcing manual cross-checking | Inline conflict badge per row, no extra click needed |
| Investigate | Opens a flagged booking to see exactly what it conflicts with | Booking detail / conflict view (F2) | "Is this a real conflict or just an overlapping hold?" | Analytical, mildly uncertain | Undocumented legacy behavior on whether he can override a flagged conflict | Clear indication of whether this conflict is a hard block or an override-able warning |
| Decide | Approves or denies each booking in a single action per row | Approval action buttons (F3, F7) | "This one's fine, approve. This one conflicts — deny." | Confident | None once conflict status is clear | Bulk-approve for non-conflicting, non-ambiguous bookings |
| Verify Boundary | Notices a booking already shows "auto-approved" and confirms it matches the current settings state | Booking status / settings reference (F3, F10) | "Good — that one didn't need me because auto-approve is on for that period" | Reassured | Legacy system gave no visibility into why something bypassed review | Status history showing "auto-approved per [setting] at [time]" |

#### Key Moments
- **Decision Point:** Investigate stage — David's decision to approve-anyway-despite-conflict or deny is the highest-stakes moment in his day; ambiguity here directly causes either double-bookings or unnecessary denials.
- **Risk of Abandonment:** Open Queue stage — a slow or cluttered queue causes David to delay reviews, backing up Maya's and others' pending requests.
- **Delight Opportunity:** Decide stage — one-action approve/deny with conflict context already visible lets David clear his queue in minutes instead of the legacy cross-checking routine.

#### Success Outcome
David's average time-to-decision is under 5 minutes per booking once it enters his queue (JTBD-02.1 success measure), and he has zero instances of a booking auto-approving when he expected it to require his review (JTBD-02.3 success measure).

#### Feature Touchpoints

| Stage | Features |
|-------|----------|
| Open Queue | F3 |
| Scan | F2, F3 |
| Investigate | F2 |
| Decide | F3, F7 |
| Verify Boundary | F3, F10 |

---

### JRN-02.2: Resolving a Disputed Approval Decision

**Persona:** PER-02 (David Okafor)
**Scenario:** A requester insists David never responded to their booking request, but David recalls denying it days ago. He needs to pull an authoritative record of exactly what happened, rather than relying on memory or digging through old emails.
**Related Jobs:** JTBD-02.4

#### Journey Stages

| Stage | Action | Touchpoint | Thinking | Feeling | Pain Point | Opportunity |
|-------|--------|------------|----------|---------|------------|-------------|
| Receive Dispute | Gets a message from a requester claiming no decision was ever communicated | Email/chat (outside system) | "I know I denied this — let me prove it" | Defensive, slightly annoyed | In the legacy system this meant searching old email threads with no guarantee of finding proof | A system-of-record that settles disputes without needing email archaeology |
| Locate Booking | Finds the specific booking in question | Booking list/search (F1) | "Which one was this again?" | Focused | Hard to search historical bookings precisely in legacy screens | Search by requester, date range, and status |
| Query Audit Trail | Opens the audit log entry for that specific booking | Audit trail view, booking-scoped (F11) | "Show me exactly who did what, and when" | Confident | Legacy logging gave only partial or system-wide (not per-booking) information | Per-booking audit view showing actor, action, and timestamp together |
| Resolve | Shares the audit record (actor, action, timestamp) with the requester to close the dispute | Audit trail export/view (F11) | "Here's the record — this settles it" | Relieved, vindicated | None — the record is authoritative and timestamped | One-click shareable audit summary for the specific booking |

#### Key Moments
- **Decision Point:** Query Audit Trail stage — if the audit entry is incomplete or missing the decision action, David has no authoritative way to resolve the dispute and must fall back to his word against the requester's.
- **Risk of Abandonment:** Locate Booking stage — if search is clunky, David may give up and resort to the old practice of digging through emails.
- **Delight Opportunity:** Resolve stage — a clean, shareable audit record turns a potentially tense interpersonal dispute into a quick, neutral fact-check.

#### Success Outcome
100% of disputed booking decisions are resolved using the audit trail with no reliance on external email records (JTBD-02.4 success measure).

#### Feature Touchpoints

| Stage | Features |
|-------|----------|
| Receive Dispute | — |
| Locate Booking | F1 |
| Query Audit Trail | F11 |
| Resolve | F11 |

---

## PER-03: Priya Patel

### JRN-03.1: Onboarding a New Bookable Room

**Persona:** PER-03 (Priya Patel)
**Scenario:** Facilities has just finished renovating a new meeting room, and Priya needs to make it immediately bookable — with the correct calendar colour and building grouping — without juggling multiple slow legacy admin screens or leaving it half-configured.
**Related Jobs:** JTBD-03.1

#### Journey Stages

| Stage | Action | Touchpoint | Thinking | Feeling | Pain Point | Opportunity |
|-------|--------|------------|----------|---------|------------|-------------|
| Navigate | Opens the admin area and goes to Locations management | Admin console — Locations (F4) | "Let me get this room set up before people start asking for it" | Purposeful | Legacy admin screens (Bootstrap 3/jQuery) were slow and clunky for this | Fast-loading, modern admin form |
| Create | Fills in a single form: name, calendar colour, building grouping | Location creation form (F4) | "Can I do this all in one pass, or will I have to come back?" | Cautiously optimistic | Legacy required juggling multiple screens to fully configure one room | Single form captures all required metadata at once |
| Save | Submits the form and watches for confirmation | Location form submission (F4) | "Did that save correctly?" | Slightly tense | Previously unclear if a change actually took effect everywhere | Immediate success confirmation with a direct link to verify |
| Verify | Checks that the new room is immediately selectable in the booking flow | Booking creation form — location dropdown (F1, F4) | "Good — it's already there for anyone booking a room" | Satisfied | In legacy systems, new reference data sometimes lagged before appearing elsewhere | Real-time availability in booking/conflict scope with no propagation delay |
| Log | Confirms the creation action appears in the audit log | Audit log view (F11) | "This is recorded in case anyone asks who added this room" | Confident | None — this is expected and present | Audit entry auto-linked from the location record itself |

#### Key Moments
- **Decision Point:** Create stage — if the form doesn't capture everything needed in one pass, Priya reverts to the legacy habit of juggling multiple screens, undermining the "single form" value proposition.
- **Risk of Abandonment:** Save stage — unclear save confirmation erodes Priya's confidence that the room is truly ready, prompting redundant manual checks.
- **Delight Opportunity:** Verify stage — seeing the new room instantly available in the booking flow (no propagation lag) is the moment Priya's trust in the new system solidifies.

#### Success Outcome
Priya onboards a new location or resource in under 5 minutes, immediately visible and selectable in the booking flow and conflict scope (JTBD-03.1 success measure).

#### Feature Touchpoints

| Stage | Features |
|-------|----------|
| Navigate | F4 |
| Create | F4 |
| Save | F4 |
| Verify | F1, F4 |
| Log | F11 |

---

### JRN-03.2: Verifying Permission Migration and Settings Propagation

**Persona:** PER-03 (Priya Patel)
**Scenario:** Ahead of a release milestone, Priya needs to confirm that a legacy permission flag now maps correctly to its Keycloak-backed role, that a settings change (toggling `approveBooking`) propagates everywhere without manual checking, and that the audit log captures both actions — all so she can sign off on the re-platform with confidence rather than hope.
**Related Jobs:** JTBD-03.2, JTBD-03.3, JTBD-03.4, JTBD-03.5

#### Journey Stages

| Stage | Action | Touchpoint | Thinking | Feeling | Pain Point | Opportunity |
|-------|--------|------------|----------|---------|------------|-------------|
| Review Mapping | Opens the permission mapping table to check a legacy flag against its Keycloak role/scope | Admin — Permissions / role mapping (F6, F7) | "Does this Keycloak role grant exactly the same access as the old flag?" | Methodical | Legacy permission system was opaque — no single place to see "who can do what" | A documented, side-by-side legacy-flag-to-Keycloak-role mapping table |
| Run Boundary Test | Triggers or reviews the permission-boundary test result for that mapping | Permission-boundary test suite output (F13) | "I need proof, not just a code review" | Focused | Previously relied on manual impersonation testing with no systematic verification | Automated, re-runnable boundary tests tied directly to each permission |
| Toggle Setting | Changes the `approveBooking` flag in Settings | Settings screen (F10) | "Will this actually apply everywhere right away?" | Slightly anxious | Legacy settings changes had unclear propagation, requiring manual multi-screen checks | Single settings screen reflecting confirmed, current effective state |
| Confirm Propagation | Checks the Booking Service behavior immediately after the toggle to confirm the new setting is already in effect | Booking creation flow (F1, F10) | "Good — new bookings are already behaving per the new setting" | Relieved | None — this is the expected, trusted outcome | Visible timestamp/confirmation of when the setting took effect |
| Review Audit Entries | Checks the audit log for both the permission test run and the settings change | Audit log, filtered by actor/entity (F11) | "Are both of these state changes captured, with me as the actor?" | Thorough | Legacy logs were incomplete, leaving audit gaps | Fully filterable audit log by entity type, actor, and time range |
| Sign Off | Reviews the traceability matrix linking F0 audit findings to requirements and passing tests before giving release sign-off | Traceability matrix / regression report (F0, F13) | "Is every legacy behavior accounted for, with nothing silently dropped?" | Confident, accountable | Previously relied on developer assurance with no independent verification | A living traceability view showing 100% coverage at a glance |

#### Key Moments
- **Decision Point:** Run Boundary Test stage — a failing or missing test here means Priya cannot confidently confirm the permission mapping, blocking her sign-off.
- **Risk of Abandonment:** Toggle Setting stage — if propagation isn't instantly confirmable, Priya falls back to manually checking multiple screens, exactly the behavior the new system is meant to eliminate.
- **Delight Opportunity:** Sign Off stage — a single traceability view showing 100% coverage replaces a stressful, uncertain manual sign-off process with a confident, evidence-based one.

#### Success Outcome
100% of legacy permission rules are mapped to an equivalent Keycloak role/scope and verified by passing permission-boundary tests (JTBD-03.2); the settings change propagates with zero manual verification (JTBD-03.3); 100% of state-changing actions appear in the audit log (JTBD-03.4); and 100% of F0-documented legacy features have an implemented requirement and at least one passing test before Priya signs off (JTBD-03.5).

#### Feature Touchpoints

| Stage | Features |
|-------|----------|
| Review Mapping | F6, F7 |
| Run Boundary Test | F13 |
| Toggle Setting | F10 |
| Confirm Propagation | F1, F10 |
| Review Audit Entries | F11 |
| Sign Off | F0, F13 |

---

## PER-04: Jordan Lee

### JRN-04.1: Directing a Visitor via the Display Board and Setting Up a Personal Feed

**Persona:** PER-04 (Jordan Lee)
**Scenario:** A visitor arrives at the front desk asking which room their meeting is in. Jordan glances at the lobby display board to confirm the approved booking and room, then later sets up a personal iCal subscription so they can check room status from their own calendar app during future shifts without re-checking the board each time.
**Related Jobs:** JTBD-04.1, JTBD-04.2, JTBD-04.3

#### Journey Stages

| Stage | Action | Touchpoint | Thinking | Feeling | Pain Point | Opportunity |
|-------|--------|------------|----------|---------|------------|-------------|
| Glance | Looks at the lobby display board while the visitor waits | Digital signage display board (F9) | "Which room is this meeting actually in?" | Slightly pressured (visitor waiting) | Legacy board sometimes lagged behind real booking activity during high load | Sub-1-minute refresh guarantee from approval to display |
| Filter | Confirms the board is scoped to this specific location/building | Display board location filter (F9, F4) | "Is this showing the right building's rooms?" | Attentive | Unclear in the legacy system whether filtering applied consistently across formats | Consistent per-location filtering guaranteed across all feed formats |
| Verify Approval | Checks that the booking shown is marked approved, not just pending | Display board booking entry (F9, F1) | "I don't want to send them to a room that isn't actually confirmed" | Cautious | No clear way in the legacy system to confirm pending vs. approved on the board | Display board shows only approved bookings, by design, never pending ones |
| Direct Visitor | Tells the visitor the correct room with confidence | (in-person, no system touchpoint) | "I'm confident this is right" | Confident, helpful | None at this point if data was reliable | — |
| Set Up Feed | Later, subscribes once to the iCal feed for this location in a personal calendar app | iCal feed subscription (F9) | "I want to just glance at my own calendar instead of walking to the board every time" | Relieved, independent | Previously required requesting a login/account from IT just to view read-only info | Login-free subscription that self-syncs with no manual refresh |

#### Key Moments
- **Decision Point:** Verify Approval stage — if Jordan cannot clearly distinguish approved from pending bookings, a visitor may be misdirected to a room that isn't actually confirmed.
- **Risk of Abandonment:** Glance stage — if the board visibly lags during busy periods, Jordan loses trust in it and resorts to calling facilities to confirm, adding delay and friction to every visitor interaction.
- **Delight Opportunity:** Set Up Feed stage — a one-time, login-free iCal subscription that stays perpetually in sync removes Jordan's need to walk to the board at all for routine shift awareness.

#### Success Outcome
Display board refresh latency stays under 1 minute from the moment a booking is approved, with zero visitor misdirection incidents attributable to stale data (JTBD-04.1); the iCal feed matches the authoritative approved-booking state 100% of the time with no manual refresh (JTBD-04.2); and Jordan encounters zero login prompts or access-denied errors across all public feed/display formats (JTBD-04.3).

#### Feature Touchpoints

| Stage | Features |
|-------|----------|
| Glance | F9 |
| Filter | F9, F4 |
| Verify Approval | F9, F1 |
| Direct Visitor | — |
| Set Up Feed | F9 |

---

## Cross-Journey Patterns

- **Common Pain Points:**
  - *Unreliable or delayed confirmation:* Maya (save confirmation), David (notification delivery), Priya (settings propagation), and Jordan (display refresh latency) all independently distrust the legacy system's ability to confirm that an action "actually took effect" — this is the single most recurring friction pattern across all four personas.
  - *Opaque scope/boundary behavior:* Maya (recurring-edit scope), David (conflict override boundaries, auto-approve boundary), and Priya (permission mapping boundaries) all hit moments where the legacy system's behavior at a decision boundary was undocumented or ambiguous, forcing guesswork or escalation.
  - *Manual cross-checking as a workaround:* Maya (cross-referencing calendars), David (cross-checking conflicts manually), and Priya (manually verifying settings across multiple screens) each describe falling back to manual verification specifically because the legacy system didn't confirm things automatically.

- **Shared Opportunities:**
  - A single, consistent "state confirmation" pattern (visible save/approval/propagation confirmation with a timestamp) would resolve the top cross-journey pain point for Maya, David, Priya, and Jordan simultaneously, since it stems from one underlying trust gap: "did this actually take effect?"
  - Explicit boundary/scope prompts (recurring edit scope, conflict override behavior, auto-approve boundary, permission mapping) share a common UI pattern: surface the ambiguous decision *before* the action completes, not after.
  - Reliable, retried event-driven notifications (F8) and consistent per-location filtering (F9, F4) both serve multiple personas (Maya/David for notifications; Jordan/Priya for location scoping) and are worth validating once, centrally, rather than per-journey.

- **Convergence Points:**
  - Maya's booking submission (JRN-01.1/JRN-01.2) is the direct input to David's queue review (JRN-02.1) — a delay or ambiguity in one directly produces friction in the other.
  - Priya's location/resource onboarding (JRN-03.1) is a precondition for both Maya's booking flow (JRN-01.1) and Jordan's display board filtering (JRN-04.1) — any gap here cascades to two other personas.
  - The audit log (F11) is a shared trust mechanism across David's dispute resolution (JRN-02.2) and Priya's compliance sign-off (JRN-03.2), making audit completeness a cross-cutting success factor, not a single-persona concern.

---

## Journey-to-JTBD Traceability

| Journey Stage | JTBD ID | Expected Outcome |
|--------------|---------|-------------------|
| JRN-01.1:Submit | JTBD-01.1 | Room + resource booking completes in under 2 minutes with zero double-booking incidents |
| JRN-01.1:Configure | JTBD-01.4 | Zero post-booking emails needed to communicate custom-field details |
| JRN-01.2:Confirm Scope | JTBD-01.2 | Recurring-series edits never unintentionally alter other occurrences |
| JRN-01.2:Resolve | JTBD-01.3 | 100% of status decisions deliver a notification within minutes |
| JRN-02.1:Scan | JTBD-02.1 | Average time-to-decision under 5 minutes per queued booking |
| JRN-02.1:Decide | JTBD-02.2 | 100% of approve/deny decisions deliver a notification to the requester |
| JRN-02.1:Verify Boundary | JTBD-02.3 | Zero unexpected auto-approvals relative to current settings state |
| JRN-02.2:Query Audit Trail | JTBD-02.4 | 100% of disputed decisions resolved via audit trail, no external records needed |
| JRN-03.1:Verify | JTBD-03.1 | New location/resource onboarded and bookable in under 5 minutes |
| JRN-03.2:Run Boundary Test | JTBD-03.2 | 100% of legacy permission rules mapped and verified via Keycloak scopes |
| JRN-03.2:Confirm Propagation | JTBD-03.3 | Settings changes propagate to all dependent services with zero manual verification |
| JRN-03.2:Review Audit Entries | JTBD-03.4 | 100% of state-changing actions produce an audit log entry |
| JRN-03.2:Sign Off | JTBD-03.5 | 100% of F0-documented features have an implemented requirement and passing test |
| JRN-04.1:Verify Approval | JTBD-04.1 | Display board refresh latency under 1 minute; zero misdirection incidents |
| JRN-04.1:Set Up Feed | JTBD-04.2 | Feed data matches approved-booking state 100% of the time, no manual refresh |
| JRN-04.1:Filter | JTBD-04.3 | Zero login prompts/access-denied errors on public feed/display surfaces |

---

*Document generated by Pivota Spec Framework*
*Last updated: 2026-10-06*
