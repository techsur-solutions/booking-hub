# UX Mockup

**Project:** Booking-Hub
**Generated:** 2026-10-06
**Based on:** UserStories-BookingHub.md, JOURNEYS-BookingHub.md, PRD-BookingHub.md, FRD-BookingHub.md, PROJECT.md

---

## Overview

Booking-Hub's frontend is a React + TypeScript single-page application replacing a Bootstrap 3/jQuery/FullCalendar.js legacy UI, with **feature parity as the explicit design constraint** — this is not a visual refresh brief, it is a trust-rebuilding brief. The cross-journey pattern analysis in `JOURNEYS-BookingHub.md` identified one dominant friction theme across all four personas: *"did this action actually take effect?"* (save confirmation, approval delivery, settings propagation, display refresh). Every screen in this document is designed around resolving that trust gap explicitly, rather than assuming a generic "good UX" will solve it.

**Design principles:**

1. **Explicit state confirmation over implicit success.** Every mutating action (save, approve, deny, settings change, delete) surfaces a visible, timestamped confirmation — never a silent 200 OK. This directly answers the #1 cross-journey pain point (JRN-01.1 Submit/Confirm stages, JRN-03.2 Confirm Propagation stage).
2. **Boundary/scope decisions surfaced before, not after.** Recurring-edit scope (US-1.2, US-1.6), conflict hard-block-vs-soft-warning (role-dependent, US-2.1/US-2.3), and auto-approve boundary (US-3.4) are all surfaced as explicit choices or status badges *before* an action completes, per the "Shared Opportunities" pattern in JOURNEYS-BookingHub.md.
3. **Conflict visibility is identical everywhere.** Calendar, day, list, and approval-queue views render the same `conflict_flags[]` using the same visual language (US-2.2) — there is exactly one conflict-evaluation code path and exactly one conflict-badge component.
4. **Approved-only means approved-only, visibly.** The display board and all public feed formats show a hard visual/data guarantee that only `status=approved` bookings ever appear (US-9.1, US-9.4) — this is reinforced in the UI copy, not just the API contract.
5. **Admin screens are single-pass, not multi-screen scavenger hunts.** Per JRN-03.1, Priya's location-onboarding success measure is "under 5 minutes, one form" — admin CRUD screens favor single-page forms with inline validation over multi-step wizards, replacing the legacy "juggle multiple screens" pain point.
6. **Deny-by-default permission visibility.** Any screen/action gated by an unconfirmed or restrictive permission flag is simply absent from the UI for a user who lacks it (not shown-but-disabled with a confusing tooltip), consistent with FRD F7's "deny by default" rule.

**Frontend stack context:** React + TypeScript SPA, communicating exclusively through Spring Cloud Gateway (no direct service calls). Calendar/Day/List views are three rendering modes of one FullCalendar-equivalent component (e.g., FullCalendar React wrapper or an equivalent scheduling grid library) sharing one data-fetching hook so conflict flags and location colour-coding (US-4.4) are guaranteed consistent across modes, satisfying US-1.5's acceptance criterion that "views render consistently regardless of whether accessed via calendar or list entry point."

---

## Navigation Map

<!-- The single source of truth for how every screen is REACHED. The planner derives
     nav-wiring tasks from this table; verify-work flags any built route missing from
     the running app's link graph as an orphan gap. -->

| Screen | Route | Reached from | Nav element |
|--------|-------|--------------|-------------|
| Login | `/login` | App entry (unauthenticated) | Redirect target for any protected route accessed without a valid token |
| Calendar View | `/bookings?view=month` | App shell | Sidebar: "Calendar" (default landing page post-login) |
| Day View | `/bookings?view=day` | Calendar View | View-switcher tab: "Day" (within Bookings Workspace header) |
| List View | `/bookings?view=list` | Calendar View | View-switcher tab: "List" (within Bookings Workspace header) |
| Booking Create Form | `/bookings/new` | Calendar / Day / List View | Header button: "+ New Booking"; or click an empty calendar slot |
| Booking Detail Modal | `/bookings/:id` | Calendar / Day / List View / Approval Queue | Click any booking event/row |
| Booking Edit Form | `/bookings/:id/edit` | Booking Detail Modal | Button: "Edit" (owner or admin only) |
| Approval Queue | `/approvals` | App shell | Sidebar: "Approvals" (visible only to users holding `allowApproveBooking`) |
| Locations Admin | `/admin/locations` | App shell | Sidebar: Admin section > "Locations" |
| Resources Admin | `/admin/resources` | App shell | Sidebar: Admin section > "Resources" |
| Custom Field Builder | `/admin/custom-fields` | App shell | Sidebar: Admin section > "Custom Fields" |
| Users Admin | `/admin/users` | App shell | Sidebar: Admin section > "Users" |
| Role & Permission Matrix | `/admin/roles` | App shell / Users Admin | Sidebar: Admin section > "Roles & Permissions"; or Users Admin row action "View role" |
| Settings Admin | `/admin/settings` | App shell | Sidebar: Admin section > "Settings" |
| Audit Log Viewer | `/admin/audit-log` | App shell / Booking Detail Modal | Sidebar: Admin section > "Audit Log"; or Booking Detail Modal link "View history" |
| My Account | `/account` | App shell | Header: user avatar menu > "My Account" |
| Password Reset Request | `/password-reset/request` | Login Screen | Link: "Forgot password?" |
| Password Reset Complete | `/password-reset/complete?token=...` | Password Reset Request (via emailed link) | Emailed reset link (token embedded in URL) |
| Display Board | `/display-board` | Settings Admin | Button: "Preview Display Board" (Display & Feeds panel) |
| Feed Subscription | `/feeds` | Display Board / App shell / Settings Admin | Display Board footer link: "Subscribe to this calendar"; Sidebar: "Feeds"; Settings Admin "Copy feed links" |

**Invariant check:** every screen above has at least one inbound path traceable to the app shell (sidebar/header) or a reachable parent screen. The Display Board and Feed Subscription pages are public/unauthenticated but are still reachable from an authenticated admin surface (Settings Admin) for setup/verification purposes, in addition to being directly bookmarked on kiosk hardware for day-to-day use (Jordan's JRN-04.1 "Glance" stage) — the kiosk bookmark is an operational deployment detail, not the only discovery path, so the invariant holds.

---
## User Flows

### Flow 1: Quick Conflict-Free Room Booking with Resources

**Trigger:** Maya has an open window and needs to book a room + resource for a call today.
**User Story:** US-1.1, US-1.5, US-2.1, US-2.2, US-4.4, US-5.2
**Journey Reference:** JRN-01.1

```
[Calendar View /bookings?view=month]
    │  (sees colour-coded locations, US-4.4)
    ▼
[Click open slot on desired room]
    │
    ▼
[Booking Create Form opens, pre-filled: location + start/end from slot]
    │
    ├─▶ [Attach Resource: "Conference Phone"] ──▶ inline resource-availability check
    │
    ├─▶ [Dynamic Custom Field renders: "Catering Headcount"] (US-5.2, scoped to location context)
    │
    ▼
[Click "Save"]
    │
    ├── Conflict detected (location OR resource) ──▶ [Inline conflict banner: "Conflicts with
    │                                                  Booking #482 (Resource: Conference Phone)"]
    │                                                  │
    │                                                  └─▶ user adjusts time/resource, resubmits
    │
    └── No conflict ──▶ [Save succeeds]
                              │
                              ▼
                   [Toast: "Booking saved — Pending approval" or "Booking saved — Approved"]
                   (status badge immediately visible, per US-3.4)
                              │
                              ▼
                   [Booking Detail Modal auto-opens, confirms full field set] (US-1.7)
                              │
                              ▼
                   [User closes modal, returns to Calendar View — new event visible immediately]
```

**Steps:**
1. Maya opens the Calendar View (default landing page) and visually scans colour-coded locations for an open slot (US-4.4, US-1.5).
2. She clicks an empty slot on the desired room's calendar row; the Booking Create Form opens pre-populated with that location, start time, and a default end time of start + 1 hour (US-1.1).
3. She opens the resource picker and attaches "Conference Phone"; the form renders the "Catering Headcount" custom field because it is scoped to this location's context (US-5.2).
4. She fills in the title and catering headcount, then clicks Save.
5. The system runs conflict detection synchronously across both location and resource scope (US-2.1). If a conflict exists, an inline banner names the specific conflicting booking and conflict type (location vs. resource) — for Maya, who does not hold `allowApproveBooking`, this is a **hard block** (interim Key Decision, FRD F2 §Process step 6); the Save button stays enabled for a corrected resubmission, it does not silently fail. (A user holding `allowApproveBooking` would instead see the same banner as a dismissible warning and could save anyway — see the Approval Queue flow below.)
6. On success, a toast confirms the save and explicitly states the resulting status (`Pending approval` vs. `Approved`) per the current `approveBooking` setting (US-3.4) — this directly resolves the "did that actually go through?" anxiety flagged in JRN-01.1's Submit stage.
7. The Booking Detail Modal opens automatically showing the full persisted record (US-1.7), then the user returns to the Calendar View where the new event is already rendered with the correct location colour and no refresh needed.

**Exit point:** Calendar View, with new booking visible and conflict-free (Maya's bookings are always conflict-free on save, since a conflict hard-blocks her submission).

---
### Flow 2: Recurring Series Edit (Single Occurrence) and Approval Wait

**Trigger:** Maya needs to move this week's occurrence of a recurring sync by 30 minutes, without touching other weeks.
**User Story:** US-1.2, US-1.6, US-2.3, US-3.1, US-3.2, US-3.3, US-8.2
**Journey Reference:** JRN-01.2

```
[Calendar View] ──▶ [Click this week's occurrence (badge: "Recurring")]
                              │
                              ▼
                  [Booking Detail Modal] ──▶ [Click "Edit"]
                              │
                              ▼
                  [Booking Edit Form — change start time]
                              │
                              ▼
                  [Click "Save"] ──▶ triggers Scope Confirmation Dialog
                              │        (US-1.2: omitting scope is a hard rejection, not a silent default)
                              │
              ┌───────────────┴────────────────┐
              ▼                                 ▼
   [Select "This occurrence only"]   [Select "Entire series"]
              │                                 │
              ▼                                 ▼
   [Re-validation + fresh conflict    [All future occurrences
    check runs for this occurrence     re-validated + re-checked]
    only — US-2.3]
              │
              ▼
   [Post-save summary banner: "Only this occurrence was
    changed — other weeks are untouched"]
              │
              ▼
   [Status badge: "Pending approval" — because this edit
    changed the time, requiring re-approval]
              │
              ▼
   [Maya continues other work; checks booking list periodically]
              │
              ▼
   [Notification arrives: "Approved" or "Denied"] (US-8.2)
              │
       ┌──────┴──────┐
       ▼             ▼
  [Approved:     [Denied: notification includes
   relieved,      "Find another slot" button ──▶
   locked in]     Booking Create Form pre-filled
                  with same details, new time]
```

**Steps:**
1. Maya locates this week's occurrence in the Calendar View; a small "Recurring" badge visually distinguishes it from one-off bookings (addresses JRN-01.2's "recurring entries look identical" pain point).
2. She opens the Booking Detail Modal, clicks Edit, and changes the start time.
3. On Save, because the booking belongs to a series, the system requires an explicit scope choice before the save completes (US-1.2) — the Scope Confirmation Dialog blocks submission until "This occurrence only" or "Entire series" is chosen; there is no silent default.
4. Selecting "This occurrence only" re-runs full validation (title/time/location/resources) and a fresh conflict check scoped to only this occurrence (US-1.2, US-2.3) — conflict results are never inherited from the original creation check.
5. A post-save summary banner explicitly confirms the blast radius: "Only this occurrence was changed — other weeks are untouched," directly resolving JRN-01.2's highest-stakes decision point.
6. Because the edit changed the booking's time, the booking re-enters `pending` status (per the approval setting) and a "Pending approval" badge is shown immediately (US-3.1 queue visibility begins here).
7. Maya can check status from the List View's status column at any time, but does not need to: a reliable, retried notification (US-8.2) arrives the moment David decides, with zero manual polling required.
8. If denied, the notification includes a one-click "Find another slot" action that reopens the Booking Create Form pre-filled with the same title/location/resources and only the time cleared for re-entry — turning a negative moment into fast recovery (JRN-01.2 Delight Opportunity).

**Exit point:** Either a reaffirmed, approved recurring occurrence, or a fast rebooking path if denied.

---
### Flow 3: Morning Pending-Queue Review

**Trigger:** David starts his day and needs to clear the pending-booking queue for locations he manages.
**User Story:** US-3.1, US-3.2, US-3.3, US-2.2, US-7.1
**Journey Reference:** JRN-02.1

```
[Login] ──▶ [App shell lands on Calendar View]
                      │
                      ▼
          [Sidebar: "Approvals" — only visible because
           David holds allowApproveBooking, US-3.1]
                      │
                      ▼
          [Approval Queue /approvals]
          ┌─────────────────────────────────────────┐
          │ Row: Booking #482  [⚠ Conflict] [Approve][Deny]│
          │ Row: Booking #483  [—]          [Approve][Deny]│
          │ Row: Booking #484  [⚠ Conflict] [Approve][Deny]│
          └─────────────────────────────────────────┘
                      │
          ┌───────────┴────────────┐
          ▼                        ▼
  [Row with no conflict]   [Row flagged ⚠ Conflict]
          │                        │
          ▼                        ▼
  [Click Approve]          [Click conflict badge ──▶
          │                 Booking Detail Modal shows
          │                 conflicting booking id +
          │                 conflict type (location/resource)]
          │                        │
          │                ┌───────┴────────┐
          │                ▼                 ▼
          │         [Approve anyway    [Deny, with optional
          │          — allowed; David   denial_reason]
          │          holds allowApprove
          │          Booking, so the
          │          conflict is a soft
          │          warning for him]
          ▼                ▼                 ▼
  [Row removed from queue; toast "Approved"]
          │
          ▼
  [Booking already marked "Auto-approved" row —
   David confirms it matches current approveBooking
   setting via Settings reference link] (US-3.4, F3 step 3)
```

**Steps:**
1. David logs in and the sidebar shows an "Approvals" item — this item is structurally absent (not disabled) for any user lacking `allowApproveBooking`, per the deny-by-default UI principle (US-3.1).
2. The Approval Queue lists every `status=pending` booking, each row showing its `conflict_flags[]` inline using the identical conflict-badge component used in Calendar/List views (US-2.2) — no separate click needed to discover a conflict exists.
3. For a non-conflicted row, David clicks Approve directly from the row — a single action, no modal required (US-3.2).
4. For a conflicted row, clicking the conflict badge opens the Booking Detail Modal pre-scrolled to the conflict section, showing the specific conflicting booking's id and whether it's a location or resource conflict (US-2.1).
5. Because David holds `allowApproveBooking`, the conflict is a **soft warning** for him (interim Key Decision, FRD F2 §Process step 6): the Approve button remains enabled on a conflicted row, with a confirmation step ("Approve despite conflict with Booking #482?") so the override is deliberate, not accidental. Denying remains available at all times regardless of conflict state.
6. Denying a row opens a lightweight inline reason field (optional free text, US-3.3) before confirming.
7. Approved/denied rows disappear from the queue immediately (optimistic UI) and a toast confirms the one-way transition (US-3.2/US-3.3: "Approve/deny are one-way transitions").
8. Rows for bookings created while auto-approve was active show an "Auto-approved" status chip with a hover tooltip: "Auto-approved per Settings at [timestamp]" — a direct link to Settings Admin lets David instantly confirm this matches the current configuration (US-3.4, JRN-02.1 Verify Boundary stage), with zero manual cross-checking.

**Exit point:** Empty (or reduced) queue; David moves to other work with full confidence every remaining row requires his attention for a documented reason.

---
### Flow 4: Resolving a Disputed Approval Decision

**Trigger:** A requester disputes that they never received a decision on a booking David recalls denying.
**User Story:** US-11.1, US-11.2
**Journey Reference:** JRN-02.2

```
[Approval Queue or Calendar View]
          │
          ▼
[Search/locate the disputed booking — List View search by
 requester name + date range]
          │
          ▼
[Booking Detail Modal] ──▶ [Click "View history" link]
          │
          ▼
[Audit Log Viewer /admin/audit-log — pre-filtered to this booking_id]
┌──────────────────────────────────────────────────────────┐
│ Timestamp           Actor        Action        Before→After │
│ 2026-09-28 14:02    David.O      booking.denied pending→denied│
│                                   denial_reason: "Room double-│
│                                   booked for exec review"      │
└──────────────────────────────────────────────────────────┘
          │
          ▼
[David shares/screenshots the immutable audit entry with
 the requester — dispute resolved]
```

**Steps:**
1. David locates the disputed booking using the List View's search (by requester, date range, status) rather than scanning manually (US-1.5 list filtering capability; JRN-02.2 "Locate Booking" risk-of-abandonment stage).
2. He opens the Booking Detail Modal and clicks "View history," which deep-links into the Audit Log Viewer pre-filtered to this exact `booking_id` (US-11.2) — David never has to construct the filter manually.
3. The audit trail shows actor, timestamp, action_type, and before/after values for every lifecycle transition on this booking (create → pending → denied), satisfying US-11.2's requirement that entries include actor/timestamp/action_type/before-after.
4. Because audit log entries are immutable once written (US-11.2, no edit/delete API exists), the displayed record is presented with a visible "Immutable record" indicator reinforcing its authority as a dispute-resolution tool.
5. David shares the entry (via screenshot or a "Copy summary" button) with the requester, closing the dispute with an authoritative, timestamped fact rather than his memory.

**Exit point:** Dispute resolved entirely within the system — zero reliance on external email archaeology (JRN-02.2 success outcome).

---
### Flow 5: Onboarding a New Bookable Room

**Trigger:** A newly renovated room needs to become bookable with correct colour and building grouping.
**User Story:** US-4.1, US-4.3, US-4.4
**Journey Reference:** JRN-03.1

```
[App shell] ──▶ [Sidebar: Admin > Locations]
                        │
                        ▼
          [Locations Admin /admin/locations]
                        │
                        ▼
          [Click "+ New Location"]
                        │
                        ▼
          [Single-page Location Form:
           name, colour swatch picker, building dropdown,
           optional layout metadata]
                        │
                        ▼
          [Click "Save"]
                        │
             ┌──────────┴──────────┐
             ▼                     ▼
      [Missing name:         [Valid: Save succeeds]
       inline error,                 │
       no navigation away]           ▼
                           [Success banner: "Location saved —
                            available for booking now" with a
                            direct "Verify in booking form" link]
                                     │
                                     ▼
                           [Click verify link ──▶ Booking Create
                            Form opens, new location already
                            present in the location dropdown,
                            rendered with its configured colour]
                                     │
                                     ▼
                           [Return to Audit Log Viewer — a
                            location.created entry is already
                            present, actor = Priya, timestamped]
```

**Steps:**
1. Priya navigates to Locations Admin via the sidebar (US-4.1) — a single modern list+form screen replacing the legacy multi-screen juggling pain point.
2. She clicks "+ New Location," which opens a single-page form capturing name, calendar colour (swatch picker, not a raw CSS class text field, for discoverability), building grouping, and optional layout metadata — all in one pass (JRN-03.1 Create stage).
3. Submitting with an empty name produces an inline, non-blocking-navigation error (US-4.1: "rejects a missing or empty name with a clear error") — she does not lose her other entered field values.
4. On success, a persistent success banner appears (not a toast that vanishes in 3 seconds) with a direct "Verify in booking form" link — this directly answers JRN-03.1's "did that save correctly?" anxiety.
5. Clicking the verify link opens the Booking Create Form; the new location is already present and selectable in the location dropdown with zero propagation delay (US-4.1: "immediately available to the Booking Service"), rendered using its configured colour (US-4.4).
6. Priya checks the Audit Log Viewer and confirms a `location.created` entry already exists with her as actor and a timestamp (US-4.3, F11) — closing the loop with zero manual cross-service verification.

**Exit point:** New location fully bookable, colour-coded, and audit-logged in under 5 minutes (JRN-03.1 success measure).

---
### Flow 6: Verifying Permission Migration and Settings Propagation

**Trigger:** Ahead of a release milestone, Priya must confirm a Keycloak permission mapping, toggle and verify `approveBooking` propagation, and review the audit trail before signing off.
**User Story:** US-7.1, US-7.2, US-10.1, US-10.2, US-10.3, US-11.3, US-13.1
**Journey Reference:** JRN-03.2

```
[Sidebar: Admin > Roles & Permissions]
            │
            ▼
[Role & Permission Matrix /admin/roles]
  ┌───────────────────────────────────────────────┐
  │ Flag                  Keycloak Role   Confirmed│
  │ allowApproveBooking   ROLE_APPROVER   ✓ Yes    │
  │ accessPermissions     ROLE_ADMIN      ✓ Yes    │
  │ allowAPI              (unconfirmed)   ⚠ Pending │
  └───────────────────────────────────────────────┘
            │
            ▼
[Reviews mapping row; unconfirmed flags visibly flagged,
 never silently treated as unrestricted — US-7.1]
            │
            ▼
[Sidebar: Admin > Settings]
            │
            ▼
[Settings Admin /admin/settings]
            │
            ▼
[Toggle "Require approval for new bookings" ON]
            │
            ▼
[Click "Save"] ──▶ [Confirmation banner: "Settings updated at
                    14:32:07 — applies to bookings created
                    after this time only"]
            │
            ▼
[Switch to Calendar View, create a throwaway test booking]
            │
            ▼
[New booking immediately shows "Pending approval" —
 propagation confirmed with zero manual multi-screen check]
            │
            ▼
[Sidebar: Admin > Audit Log, filter by actor=Priya, entity=settings]
            │
            ▼
[Audit Log Viewer shows settings.updated entry AND the
 permission mapping review — both captured, US-11.3]
            │
            ▼
[Sign-off: review F13 traceability matrix summary (external
 tooling link) — 100% coverage indicator]
```

**Steps:**
1. Priya opens the Role & Permission Matrix and reviews each legacy permission flag against its Keycloak role mapping (US-7.1, US-7.2); any flag marked unconfirmed is visibly flagged with a warning chip, never silently rendered as if it carries no restriction — this directly implements the FRD's "deny by default" rule in the UI itself.
2. She opens Settings Admin and toggles `approveBooking` on (US-10.1). The Save action requires admin permission server-side (enforced, not just hidden); a non-admin never sees this toggle at all.
3. On save, a confirmation banner states the exact effective timestamp and explicitly scopes the change: "applies to bookings created after this time only" — directly resolving JRN-03.2's "will this actually apply everywhere right away?" anxiety, and satisfying US-10.3's non-retroactivity rule.
4. Priya creates a disposable test booking from the Calendar View and immediately observes it enters `pending` status — confirming propagation without needing to check any other screen (US-10.3, US-1.1).
5. She filters the Audit Log Viewer by her own actor ID and the `settings` entity type, confirming both the settings change and the permission-mapping review produced audit entries (US-11.3).
6. She reviews a linked traceability/regression summary (an external F13 tooling view, referenced but not owned by this frontend) before signing off on the release — a single screen showing 100% coverage replaces the legacy manual, stressful sign-off process (JRN-03.2 Sign Off delight opportunity).

**Exit point:** Confident release sign-off with zero manual multi-screen verification.

---
### Flow 7: Directing a Visitor via the Display Board and Setting Up a Personal Feed

**Trigger:** A visitor at the front desk needs to be directed to the correct room; Jordan later wants a personal, login-free iCal subscription.
**User Story:** US-9.1, US-9.2, US-9.3, US-9.4
**Journey Reference:** JRN-04.1

```
[Lobby kiosk hardware, bookmarked] ──▶ [Display Board /display-board]
            │
            ▼
  [Location filter dropdown: "Building A ▾"]
            │
            ▼
  ┌─────────────────────────────────────────┐
  │  NOW SHOWING                             │
  │  Conf Room 2B   2:00–3:00 PM   ✓ Approved│
  │  Studio 1       2:30–4:00 PM   ✓ Approved│
  └─────────────────────────────────────────┘
  (auto-refreshes client-side every <60s; only status=approved
   bookings ever rendered — US-9.1, US-9.4)
            │
            ▼
  [Jordan confirms room, directs visitor with confidence]
            │
            ▼
  [Later: clicks footer link "Subscribe to this calendar"]
            │
            ▼
  [Feed Subscription /feeds]
  ┌─────────────────────────────────────────┐
  │ Location: [Building A ▾]                 │
  │                                           │
  │ 📅 iCal:  https://.../feeds/ical?location=A  [Copy]│
  │ 📰 RSS2:  https://.../feeds/rss2?location=A  [Copy]│
  │ 🔗 JSON:  https://.../feeds/json?location=A  [Copy]│
  └─────────────────────────────────────────┘
            │
            ▼
  [Jordan copies the iCal link, pastes into personal calendar
   app's "Add by URL" — no login prompt at any step, US-9.3]
```

**Steps:**
1. The Display Board is bookmarked on lobby kiosk hardware and filtered to "Building A" so Jordan only sees relevant rooms (US-9.1, US-9.3 filtering parity across formats).
2. The board shows only bookings where `status=approved`; pending/denied bookings are structurally never queried for this view, not merely hidden by a client-side filter (US-9.4) — this is reinforced with a small "✓ Approved" micro-label on each row so Jordan never has to wonder.
3. The board auto-refreshes client-side (polling or SSE) with a sub-1-minute latency guarantee from the moment a booking is approved (US-9.4, JRN-04.1 success measure).
4. Jordan confidently directs the visitor, then later clicks a footer link to the Feed Subscription page to set up a personal iCal feed.
5. The Feed Subscription page presents all four formats (display board link, iCal, RSS2, JSON) side by side, each respecting the same `location_id` filter and the same access-control evaluation (US-9.3: "a caller denied access to one format is equally denied access to all other formats").
6. Jordan copies the iCal URL and adds it to a personal calendar app — no login prompt appears at any point in this entire flow, satisfying US-9.3's zero-login-friction requirement and JTBD-04.3's success measure.

**Exit point:** Visitor directed correctly; Jordan has a perpetually self-syncing personal feed requiring no further trips to the lobby board.

---
## Screen Designs

### Screen: Calendar View (Month/Week grid)

**Purpose:** Default landing page; a FullCalendar-equivalent grid showing all non-deleted bookings within a visible date range, colour-coded by location, with conflict flags surfaced inline.
**User Stories:** US-1.5, US-2.2, US-4.4, US-1.7

#### Layout

```
┌──────────────────────────────────────────────────────────────────┐
│ [Logo] Booking-Hub      [Calendar|Day|List]      [+ New Booking]  │
│                                                   [🔔][Avatar ▾]   │
├───────────────┬──────────────────────────────────────────────────┤
│ SIDEBAR        │  ◀  October 2026  ▶          [Location ▾][Resource ▾]│
│ ▸ Calendar     ├──────────────────────────────────────────────────┤
│ ▸ Approvals(3) │  Mon   Tue   Wed   Thu   Fri   Sat   Sun          │
│ ▸ Feeds        │ ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐                    │
│ ▸ Admin ▾      │ │▇▇▇│ │   │ │▇▇▇│ │▇⚠▇│ │   │   ...               │
│   Locations    │ │Rm2B│ │   │ │Stu1│ │Rm2B│ │   │                 │
│   Resources    │ └───┘ └───┘ └───┘ └───┘ └───┘                    │
│   CustomFields │                                                   │
│   Users        │  Legend: ▇ = location colour  ⚠ = conflict flag   │
│   Roles        │                                                   │
│   Settings     │                                                   │
│   Audit Log    │                                                   │
└───────────────┴──────────────────────────────────────────────────┘
```

#### Information Hierarchy

| Priority | Content | Placement |
|----------|---------|-----------|
| Primary | Booking blocks colour-coded by location, conflict badge (⚠) overlay | Calendar grid cells |
| Primary | View switcher (Calendar / Day / List) | Top header, always visible |
| Secondary | Location/Resource filter dropdowns | Toolbar row above grid |
| Secondary | "+ New Booking" action | Header, persistent |
| Tertiary | Approvals badge count (visible only if `allowApproveBooking`) | Sidebar |

#### States

| State | Appearance | User Feedback |
|-------|------------|---------------|
| Default | Grid populated with colour-coded blocks | N/A |
| Loading | Skeleton grid cells (shimmer) | "Loading bookings…" |
| Empty (no bookings in range) | Grid renders with no blocks | Subtle placeholder text: "No bookings this week" |
| Conflict present | Block renders with red ⚠ corner badge + dashed red outline | Tooltip on hover: "Conflicts with Booking #482 (resource)" |
| Filtered (location/resource applied) | Non-matching blocks hidden, filter chip shown in toolbar | Chip: "Location: Studio 1 ✕" |
| Error (fetch failure) | Grid area replaced with inline error panel | "Unable to load bookings. [Retry]" |

#### Interactive Elements

| Element | Type | Behavior |
|---------|------|----------|
| Calendar cell (empty slot) | Click target | Opens Booking Create Form pre-filled with location + time |
| Booking block | Click target | Opens Booking Detail Modal (US-1.7) |
| View switcher tabs | Tab group | Switches render mode without full page reload; same underlying data hook (US-1.5 consistency requirement) |
| Location/Resource filter | Dropdown (multi-select) | Re-queries date range scoped to selection (US-1.5) |
| "+ New Booking" | Primary button | Opens blank Booking Create Form |
| Conflict badge (⚠) | Icon + tooltip | Hover shows conflicting booking id and type; click opens Booking Detail Modal scrolled to conflict section |

---
### Screen: Day View

**Purpose:** Hour-by-hour single-day schedule across all (or filtered) locations — the FullCalendar "resourceTimeGrid"-equivalent view, useful for comparing room availability side by side within one day.
**User Stories:** US-1.5, US-2.2, US-4.4, US-10.2

#### Layout

```
┌──────────────────────────────────────────────────────────────────┐
│ [Calendar|Day|List]   ◀ Tue, Oct 6 2026 ▶   [Location ▾][Resource ▾]│
├───────────┬───────────────┬───────────────┬───────────────────────┤
│  Time     │  Room 2B      │  Studio 1     │  Boardroom A          │
├───────────┼───────────────┼───────────────┼───────────────────────┤
│ 9:00 AM   │               │  ▇▇▇▇▇▇▇▇▇▇  │                       │
│ 9:30 AM   │               │  Team Sync    │                       │
│ 10:00 AM  │  ▇▇▇⚠▇▇▇▇▇▇  │               │                       │
│ 10:30 AM  │  Client Call  │               │                       │
│ 11:00 AM  │               │               │  ▇▇▇▇▇▇▇▇▇▇          │
├───────────┴───────────────┴───────────────┴───────────────────────┤
│ Visible range governed by Settings calendar_min_time/max_time (US-10.2)│
└──────────────────────────────────────────────────────────────────┘
```

#### Information Hierarchy

| Priority | Content | Placement |
|----------|---------|-----------|
| Primary | Per-location column with time-aligned booking blocks | Main grid |
| Primary | Conflict badge on overlapping blocks | Inline on block |
| Secondary | Date stepper (◀ Tue, Oct 6 2026 ▶) | Toolbar |
| Tertiary | Admin-configured slot size/visible range (reflects Settings) | Governs grid granularity, not separately displayed |

#### States

| State | Appearance | User Feedback |
|-------|------------|---------------|
| Default | Columns populated per visible locations | N/A |
| Loading | Column skeletons | "Loading day schedule…" |
| Empty location (no bookings) | Blank column, grid lines only | No special message — blank space communicates openness |
| Conflict present | Dashed red outline + ⚠ badge, identical styling to Calendar View | Tooltip: conflicting booking id + type |
| Settings misconfigured (`calendar_min_time` > `calendar_max_time`) | N/A — update rejected at Settings Admin before reaching this screen | — |

#### Interactive Elements

| Element | Type | Behavior |
|---------|------|----------|
| Date stepper | Prev/Next buttons | Navigates one day at a time |
| Time-slot cell (empty) | Click target | Opens Booking Create Form pre-filled with that location + time |
| Booking block | Click target | Opens Booking Detail Modal |
| Column header (location name) | Click target | Deep-links to that location's filtered List View |

**Consistency note:** the conflict badge, booking block colour, and click-to-detail behavior are pixel-identical to Calendar View and List View — enforced by sharing one `<BookingBlock>` component across all three render modes (US-1.5, US-2.2).

---
### Screen: List View

**Purpose:** Tabular, searchable/filterable listing of bookings — the fastest path for finding a specific booking by requester, date range, status, or conflict state (supports JRN-02.2 dispute investigation and JRN-01.2 status checking).
**User Stories:** US-1.5, US-2.2, US-11.2

#### Layout

```
┌──────────────────────────────────────────────────────────────────┐
│ [Calendar|Day|List]  [Search requester/title...] [Status ▾][Date range ▾]│
├──────────────────────────────────────────────────────────────────┤
│ ⚠  Title          Location    Start          Status      Owner    │
│ ──────────────────────────────────────────────────────────────── │
│ ⚠  Client Call    Room 2B     10:00–11:00 AM  Pending     Maya T. │
│    Team Sync      Studio 1    9:00–9:30 AM    Approved    Maya T. │
│    Budget Review  Boardroom A 2:00–3:00 PM    Denied      Sam R.  │
│ ⚠  All-Hands      Studio 1    4:00–5:00 PM    Approved    Priya P.│
├──────────────────────────────────────────────────────────────────┤
│                                          [◀ Prev] Page 2 of 5 [Next ▶]│
└──────────────────────────────────────────────────────────────────┘
```

#### Information Hierarchy

| Priority | Content | Placement |
|----------|---------|-----------|
| Primary | Conflict flag column (⚠), Title, Status | Leftmost columns |
| Secondary | Location, Start/End time, Owner | Middle columns |
| Tertiary | Pagination, row count | Footer |

#### States

| State | Appearance | User Feedback |
|-------|------------|---------------|
| Default | Rows populated, sorted by start time ascending | N/A |
| Loading | Row skeletons (5–10 shimmer rows) | "Loading bookings…" |
| Empty (no matches) | Empty-state illustration + message | "No bookings match your filters. [Clear filters]" |
| Conflict present | Row has ⚠ icon + subtle red-tinted left border | Tooltip identical to Calendar/Day view conflict tooltip |
| Filtered | Active filter chips shown above table | Chips: "Status: Pending ✕", "Date: This week ✕" |
| Search in progress | Debounced spinner in search box | — |

#### Interactive Elements

| Element | Type | Behavior |
|---------|------|----------|
| Search box | Text input (debounced) | Filters by title/requester substring |
| Status filter | Dropdown (multi-select: Pending/Approved/Denied) | Narrows rows client-side via re-fetch |
| Date range filter | Date picker | Scopes query range; rejects `date_from > date_to` with inline error (consistent with US-11.1's audit-log filter rule) |
| Row click | Click target | Opens Booking Detail Modal |
| Column header click | Sort toggle | Re-sorts by that column ascending/descending |
| Conflict icon (⚠) | Click target | Opens Booking Detail Modal scrolled to conflict section |

---
### Screen: Booking Create / Edit Form

**Purpose:** Single form for creating a new booking or editing an existing one, including recurrence definition, multi-resource attachment, dynamic custom fields, and conflict feedback before save.
**User Stories:** US-1.1, US-1.2, US-1.6, US-2.1, US-2.3, US-4.4, US-5.1, US-5.2

#### Layout

```
┌──────────────────────────────────────────────────────────────────┐
│  New Booking                                            [✕ Close] │
├──────────────────────────────────────────────────────────────────┤
│  Title *            [__________________________________]          │
│  Location *         [Room 2B ▾]            ■ (colour swatch)       │
│  Start              [Oct 6, 2026  10:00 AM]                        │
│  End                [Oct 6, 2026  11:00 AM]  (defaults +1h if blank)│
│  Resources           [+ Add Resource]                               │
│                       • Conference Phone [✕]                        │
│  ── Custom Fields (scoped to Room 2B) ──────────────────────────    │
│  Catering Headcount  [____]                                         │
│  ── Recurrence ──────────────────────────────────────────────       │
│  ○ One-time   ● Repeats weekly on [Mon ▾] until [Nov 24, 2026]      │
│                (must resolve to a finite set of occurrences)        │
│                                                                      │
│  ⚠ Conflict: overlaps Booking #482 (resource: Conference Phone)     │
│     [View conflicting booking]                                      │
│                                                                      │
│                                [Cancel]          [Save Booking]      │
└──────────────────────────────────────────────────────────────────┘

── On Save of a booking belonging to a series ──
┌──────────────────────────────────────┐
│  Apply this change to:                │
│   ◉ This occurrence only              │
│   ○ Entire series                     │
│         [Cancel]        [Confirm]     │
└──────────────────────────────────────┘
```

#### Information Hierarchy

| Priority | Content | Placement |
|----------|---------|-----------|
| Primary | Title, Location, Start/End time | Top of form, required-field asterisk |
| Primary | Conflict banner (if present) | Directly above Save button, impossible to miss before submitting |
| Secondary | Resources picker, Custom Fields (dynamically rendered per location context) | Middle of form |
| Secondary | Recurrence controls | Below custom fields, collapsed to "One-time" by default |
| Tertiary | Scope Confirmation Dialog (edit of series booking only) | Modal overlay, blocks Save until resolved |

#### States

| State | Appearance | User Feedback |
|-------|------------|---------------|
| Default (create) | Empty form, End time placeholder shows "+1h from start" | N/A |
| Default (edit) | Pre-filled with existing values | N/A |
| Validation error: empty title | Red outline + inline text under field | "Booking title is required" (US-1.1) |
| Validation error: end before or equal to start | Red outline on End field | "End time must be after start time" (US-1.1) |
| Validation error: location not found | Form-level banner | "Specified location does not exist" |
| Conflict, caller without `allowApproveBooking` (hard block, interim default) | Red banner above Save; Save remains clickable for a corrected resubmission | "This booking conflicts with an existing booking for the selected location or resource" (US-2.1) |
| Conflict, caller holding `allowApproveBooking` (soft warning, interim default) | Amber banner above Save; Save proceeds on confirmation | "This booking conflicts with an existing booking — save anyway?" (US-2.3) |
| Scope required (series edit, scope omitted) | Scope dialog cannot be dismissed without a selection | "Scope (this occurrence or whole series) is required for recurring bookings" (US-1.2) |
| Saving | Save button shows spinner, disabled | "Saving…" |
| Success | Form closes; toast confirms | "Booking saved — Pending approval" / "— Approved" |
| Custom field value invalid (`field_id` unknown) | Inline error on that field | "This field is no longer valid for this location" |

#### Interactive Elements

| Element | Type | Behavior |
|---------|------|----------|
| Location dropdown | Select | Re-queries applicable custom fields for the newly selected location (US-5.2) |
| + Add Resource | Button → multi-select list | Adds resource chips; each triggers availability re-check |
| Recurrence toggle | Radio (One-time / Repeats) | Expands recurrence sub-form; rejects open-ended series (no "repeats forever" option exists in the UI — US-1.6) |
| Save Booking | Primary button | Validates client-side, submits, triggers conflict check server-side |
| Scope dialog radio buttons | Radio (required) | Must select before "Confirm" enables (US-1.2) |
| View conflicting booking | Link | Opens the conflicting Booking Detail Modal in a secondary panel without losing current form state |

---
### Screen: Booking Detail Modal

**Purpose:** Read-focused modal/detail pane showing the full field set for a single booking (legacy `Eventdata` equivalent), including status, custom field values, and conflict context, with entry points to Edit, Clone, Delete, and Audit History.
**User Stories:** US-1.7, US-1.3, US-1.4, US-2.1, US-5.3, US-11.2

#### Layout

```
┌──────────────────────────────────────────────────────────────────┐
│  Client Call                                    [Pending ▾] [✕]   │
├──────────────────────────────────────────────────────────────────┤
│  Location:   Room 2B  ■                                           │
│  When:       Oct 6, 2026, 10:00–11:00 AM                           │
│  Owner:      Maya Torres                                          │
│  Resources:  Conference Phone                                      │
│  Catering Headcount: 8                                             │
│                                                                      │
│  ⚠ Conflict with Booking #482 (resource: Conference Phone)         │
│     [View Booking #482]                                            │
│                                                                      │
│  Recurring: Weekly on Mon, until Nov 24, 2026  [This occurrence]    │
│                                                                      │
│  ──────────────────────────────────────────────────────────────    │
│  [Edit]   [Clone]   [Delete]              [View history →]         │
└──────────────────────────────────────────────────────────────────┘
```

#### Information Hierarchy

| Priority | Content | Placement |
|----------|---------|-----------|
| Primary | Title, Status badge, Location, Time | Header + top block |
| Primary | Conflict warning (if present) | Immediately below core fields, high visual weight |
| Secondary | Resources, Custom field values | Middle block (US-5.3) |
| Secondary | Recurrence summary + which occurrence is being viewed | Below custom fields |
| Tertiary | Edit/Clone/Delete/View history actions | Footer action bar |

#### States

| State | Appearance | User Feedback |
|-------|------------|---------------|
| Pending | Amber "Pending" status badge | — |
| Approved | Green "Approved" status badge | — |
| Denied | Red "Denied" status badge + denial reason shown if present | "Denied: Room double-booked for exec review" |
| Conflict present | Red warning panel with link to conflicting booking | Per US-2.1 — cites conflicting booking id + type |
| Not found | Modal replaced with error state | "Booking not found" (US-1.7) |
| Loading | Skeleton rows | "Loading booking details…" |
| Delete confirmation (non-series) | Inline confirm: "Delete this booking?" [Cancel] [Delete] | — |
| Delete confirmation (series) | Scope dialog (same component as Edit) | Requires explicit scope before deleting (US-1.3) |
| Clone in progress | Clone button shows spinner | Opens Booking Create Form pre-filled, status reset, new (no) series_id (US-1.4) |

#### Interactive Elements

| Element | Type | Behavior |
|---------|------|----------|
| Status badge | Read-only (approvers see no inline approve/deny here — that lives in Approval Queue for queue-centric workflow, but a direct Approve/Deny action is also available here if accessed via a pending booking and caller holds `allowApproveBooking`) | Visual-only for non-approvers |
| Edit | Button | Opens Booking Edit Form; if series, edit triggers Scope Confirmation Dialog on save |
| Clone | Button | Creates draft copy excluding id/status/created_at/series_id (US-1.4); opens prefilled Create Form |
| Delete | Button | Soft-deletes; if series, requires scope selection (US-1.3) |
| View history | Link | Deep-links to Audit Log Viewer filtered to this `booking_id` (US-11.2) |
| View Booking #482 | Link | Opens the conflicting booking's own Detail Modal |

---
### Screen: Approval Queue / Dashboard

**Purpose:** The restricted-access queue of every `status=pending` booking, scoped to users holding `allowApproveBooking`, with inline conflict visibility and one-action approve/deny.
**User Stories:** US-3.1, US-3.2, US-3.3, US-2.2, US-7.1, US-7.3

#### Layout

```
┌──────────────────────────────────────────────────────────────────┐
│  Approvals (3 pending)                                             │
├──────────────────────────────────────────────────────────────────┤
│ ⚠  Client Call     Room 2B     Oct 6, 10:00–11:00 AM   Maya T.      │
│                                           [Approve] [Deny ▾]        │
│ ── Catering Headcount: 8 ────────────────────────────────────────  │
│                                                                      │
│    Team Offsite    Studio 1    Oct 7, 9:00 AM–5:00 PM  Sam R.       │
│                                           [Approve] [Deny ▾]        │
│                                                                      │
│ ⚠  All-Hands        Boardroom A Oct 8, 2:00–3:00 PM    Priya P.     │
│                                           [Approve] [Deny ▾]        │
├──────────────────────────────────────────────────────────────────┤
│  Auto-approved today: 12  (see Settings → approveBooking)           │
└──────────────────────────────────────────────────────────────────┘
```

#### Information Hierarchy

| Priority | Content | Placement |
|----------|---------|-----------|
| Primary | Conflict badge (⚠), Title, Location, Time, Requester | Row, left-to-right scan order |
| Primary | Approve / Deny actions | Row, right-aligned, always visible (no overflow menu for Approve) |
| Secondary | Custom field values (e.g., catering headcount) | Expandable row detail |
| Tertiary | "Auto-approved today" counter linking to Settings | Footer summary bar |

#### States

| State | Appearance | User Feedback |
|-------|------------|---------------|
| Default (queue has items) | Rows sorted oldest-first | Badge count in sidebar nav matches row count |
| Empty queue | Empty-state illustration | "You're all caught up — no pending bookings" |
| Conflict present | ⚠ icon + red-tinted row background | Tooltip: conflicting booking id + type (identical to Calendar/List, US-2.2) |
| Conflict present, Approve clicked (interim Key Decision: soft warning for `allowApproveBooking` holders) | Confirmation dialog before proceeding | "Approve despite conflict with Booking #482? This will not resolve the conflict automatically." [Cancel] [Approve anyway] |
| Forbidden (no `allowApproveBooking`) | Screen/nav item does not render at all | N/A — route guard redirects to Calendar View (US-3.1, US-7.3) |
| Approving | Row shows spinner over Approve button | — |
| Approve success | Row fades out and is removed from list | Toast: "Booking approved" |
| Deny success | Row fades out | Toast: "Booking denied" |
| Stale state (booking already decided elsewhere) | Row shows "Already decided" banner on attempted action | 409 surfaced: "Only pending bookings can be approved or denied" (US-3.2/3.3) |

#### Interactive Elements

| Element | Type | Behavior |
|---------|------|----------|
| Approve | Primary button | Sets status=approved, records approved_by/approved_at, publishes `booking.approved` (US-3.2) |
| Deny ▾ | Split button | Opens inline optional reason field, then confirms deny (US-3.3) |
| Row expand | Click target (chevron) | Reveals custom field values inline (US-5.3) |
| Conflict badge | Click target | Opens Booking Detail Modal scrolled to conflict |
| "Auto-approved today" counter | Link | Navigates to Settings Admin, `approveBooking` section |
| Row (non-action area) | Click target | Opens full Booking Detail Modal |

---
### Screen: Locations Admin

**Purpose:** Admin-only CRUD screen for bookable locations (name, colour, building, layout metadata), feeding the Booking Service's location dropdown and conflict scoping.
**User Stories:** US-4.1, US-4.3, US-4.4

#### Layout

```
┌──────────────────────────────────────────────────────────────────┐
│  Locations                                        [+ New Location]│
├──────────────────────────────────────────────────────────────────┤
│  Name          Colour   Building        Bookings   Actions         │
│  ───────────────────────────────────────────────────────────────  │
│  Room 2B        ■        Building A      14         [Edit][Delete] │
│  Studio 1       ■        Building A      8          [Edit][Delete] │
│  Boardroom A    ■        Building B      3          [Edit][Delete] │
└──────────────────────────────────────────────────────────────────┘

── New/Edit Location form (inline drawer, single pass) ──
┌──────────────────────────────────────┐
│  Name *        [____________]         │
│  Colour        [■ swatch picker]      │
│  Building      [Building A ▾]         │
│  Layout (opt.) [____________]         │
│                                        │
│              [Cancel]   [Save]        │
└──────────────────────────────────────┘
```

#### Information Hierarchy

| Priority | Content | Placement |
|----------|---------|-----------|
| Primary | Name, Colour swatch | Leftmost columns — matches how colour renders on the Calendar |
| Secondary | Building grouping, active booking count | Middle columns |
| Tertiary | Edit/Delete actions | Rightmost column |

#### States

| State | Appearance | User Feedback |
|-------|------------|---------------|
| Default | List populated | N/A |
| Empty (no locations yet) | Empty state | "No locations yet — add your first bookable room" |
| Validation error: empty name | Inline error under Name field, drawer stays open | "Name is required" (US-4.1) |
| Save success | Drawer closes; persistent success banner | "Location saved — available for booking now [Verify in booking form]" |
| Delete requested, location in use | Confirmation dialog explains interim policy | "This location is referenced by 14 existing bookings. It will be hidden from new bookings but existing bookings will keep showing 'Room 2B' as a historical record." (US-4.3 soft-delete policy) |
| Delete success | Row removed from active list (may appear in a separate "Archived" filter) | Toast: "Location deleted" |
| Forbidden (no admin permission) | Screen/nav item absent | N/A |
| Delete non-existent id (race condition) | Inline error | "Location not found" |

#### Interactive Elements

| Element | Type | Behavior |
|---------|------|----------|
| + New Location | Button | Opens inline drawer form |
| Colour swatch picker | Visual picker (not raw hex/class input) | Sets `css_class`/colour used consistently across Calendar/Day/List (US-4.4) |
| Edit | Row action | Opens drawer pre-filled |
| Delete | Row action | Opens confirmation dialog describing soft-delete impact |
| Booking count | Link | Deep-links to List View filtered to this location |

---
### Screen: Resources Admin

**Purpose:** Admin-only CRUD screen for bookable equipment/resources, independent of locations, referenced by the Booking Service by ID only.
**User Stories:** US-4.2, US-4.3

#### Layout

```
┌──────────────────────────────────────────────────────────────────┐
│  Resources                                         [+ New Resource]│
├──────────────────────────────────────────────────────────────────┤
│  Name                  In Bookings    Actions                      │
│  ─────────────────────────────────────────────────────────────    │
│  Conference Phone      6              [Edit][Delete]               │
│  Projector A            2              [Edit][Delete]               │
│  Whiteboard Cart        0              [Edit][Delete]               │
└──────────────────────────────────────────────────────────────────┘

── New/Edit Resource form (inline drawer) ──
┌──────────────────────────────────────┐
│  Name *        [____________]         │
│                                        │
│              [Cancel]   [Save]        │
└──────────────────────────────────────┘
```

#### Information Hierarchy

| Priority | Content | Placement |
|----------|---------|-----------|
| Primary | Name | Leftmost column |
| Secondary | In-use booking count | Middle column (signals safe-to-delete at a glance) |
| Tertiary | Edit/Delete actions | Rightmost column |

#### States

| State | Appearance | User Feedback |
|-------|------------|---------------|
| Default | List populated | N/A |
| Empty | Empty state | "No resources yet — add equipment that can be attached to bookings" |
| Validation error: empty name | Inline error, drawer stays open | "Name is required" (US-4.2) |
| Save success | Drawer closes; banner | "Resource saved — available to attach now" |
| Delete, resource in use (0 bookings) | No confirmation friction — direct delete | Toast: "Resource deleted" |
| Delete, resource in use (>0 bookings) | Confirmation dialog, same soft-delete language as Locations | "Referenced by 6 existing bookings — they will keep showing 'Conference Phone' as a historical record." (US-4.3) |
| Forbidden | Screen/nav item absent | N/A |

#### Interactive Elements

| Element | Type | Behavior |
|---------|------|----------|
| + New Resource | Button | Opens inline drawer form |
| Edit | Row action | Opens drawer pre-filled |
| Delete | Row action | Confirms if in use, otherwise deletes immediately |
| "In Bookings" count | Link | Deep-links to List View filtered to this resource |

---
### Screen: Custom Field Builder

**Purpose:** Admin screen for defining custom field templates (label, type, options) and attaching them to a context (a specific location or "all bookings"), which the Booking Form renders dynamically.
**User Stories:** US-5.1, US-5.3

#### Layout

```
┌──────────────────────────────────────────────────────────────────┐
│  Custom Fields                                    [+ New Field]    │
├──────────────────────────────────────────────────────────────────┤
│  Label                 Type      Applies to        Actions         │
│  ─────────────────────────────────────────────────────────────    │
│  Catering Headcount    Number    Room 2B            [Edit][Delete] │
│  Setup Notes           Text      All bookings        [Edit][Delete] │
│  Room Layout           Select    Studio 1, Boardroom A [Edit][Delete]│
├──────────────────────────────────────────────────────────────────┤
│  Field Templates                                   [+ New Template]│
│  ─────────────────────────────────────────────────────────────    │
│  "Executive Meeting Pack" → Catering Headcount, Setup Notes         │
└──────────────────────────────────────────────────────────────────┘

── New/Edit Custom Field form ──
┌──────────────────────────────────────────────┐
│  Label *          [____________]              │
│  Field Type *     [Text ▾]                    │
│      (if Select)  Options: [Round][Theatre][+Add]│
│  Applies to        ◉ Specific location: [Room 2B ▾]│
│                     ○ All bookings              │
│                                                  │
│                      [Cancel]   [Save]          │
└──────────────────────────────────────────────┘
```

#### Information Hierarchy

| Priority | Content | Placement |
|----------|---------|-----------|
| Primary | Label, Field Type | Leftmost columns |
| Primary | Options editor (only shown when Type = Select) | Conditionally rendered in form |
| Secondary | "Applies to" context scoping | Middle column / form |
| Tertiary | Field Templates grouping section | Below the main field list |

#### States

| State | Appearance | User Feedback |
|-------|------------|---------------|
| Default | List populated, grouped by field then templates | N/A |
| Empty | Empty state | "No custom fields yet — define fields to appear on booking forms" |
| Validation error: empty label | Inline error | "Custom field label is required" (US-5.1) |
| Validation error: Select type with no options | Inline error under Options editor | "Options are required for selection-type fields" (US-5.1) |
| Save success | Drawer closes; banner | "Custom field saved — now appears on applicable booking forms" |
| Delete field with historical values | Confirmation notes retention | "Past bookings that used this field will keep their recorded values; this field will no longer appear on new/edit forms." (US-5.3 — deleting a definition never purges historical values) |
| Forbidden | Screen/nav item absent | N/A |

#### Interactive Elements

| Element | Type | Behavior |
|---------|------|----------|
| + New Field | Button | Opens field creation form |
| Field Type dropdown | Select (Text/Number/Date/Select) | Toggles Options editor visibility |
| Applies-to radio | Radio (Specific location / All bookings) | Determines the CustomFieldJoin context used by the Booking Form's dynamic renderer (US-5.1, US-5.2) |
| + New Template | Button | Opens template form grouping multiple fields together |
| Edit / Delete | Row actions | Standard drawer edit / confirm-delete |

---
### Screen: Users Admin

**Purpose:** Admin-facing account creation and role assignment, backed by Keycloak provisioning rather than a locally stored password.
**User Stories:** US-6.1, US-6.5

#### Layout

```
┌──────────────────────────────────────────────────────────────────┐
│  Users                                             [+ New User]    │
├──────────────────────────────────────────────────────────────────┤
│  Name            Email                 Role            Actions     │
│  ─────────────────────────────────────────────────────────────    │
│  Maya Torres     maya@co.com            Booker          [Edit Role]│
│  David Okafor    david@co.com           Approver         [Edit Role]│
│  Priya Patel     priya@co.com           Admin            [Edit Role]│
│  Jordan Lee       (public feed — no account)             —          │
└──────────────────────────────────────────────────────────────────┘

── New User form ──
┌──────────────────────────────────────┐
│  Email / Username * [____________]    │
│  Display Name *      [____________]   │
│  Initial Role *       [Booker ▾]      │
│                                        │
│              [Cancel]   [Create]      │
└──────────────────────────────────────┘

── Edit Role drawer ──
┌──────────────────────────────────────┐
│  Maya Torres — Current role: Booker   │
│  New role:    [Approver ▾]            │
│  ⓘ Takes effect on next issued token  │
│              [Cancel]   [Save]        │
└──────────────────────────────────────┘
```

#### Information Hierarchy

| Priority | Content | Placement |
|----------|---------|-----------|
| Primary | Name, Email, Role | Main table columns |
| Secondary | Edit Role action | Rightmost column |
| Tertiary | Account-creation metadata (created date) | Expandable row detail, not shown by default |

#### States

| State | Appearance | User Feedback |
|-------|------------|---------------|
| Default | List populated | N/A |
| Validation error: duplicate email | Inline error, form stays open | "An account with this email/username already exists" (US-6.1, 409) |
| Create success | Drawer closes; banner | "User created in Keycloak — role assigned: Booker" |
| Role change saved | Drawer closes; banner with explicit timing note | "Role updated — takes effect on next login/token refresh" (US-6.5, sets correct expectation rather than implying instant effect) |
| Forbidden | Screen/nav item absent | N/A |

#### Interactive Elements

| Element | Type | Behavior |
|---------|------|----------|
| + New User | Button | Opens creation form; provisions account in Keycloak realm, not a local password hash (US-6.1) |
| Initial Role dropdown | Select | Required; feeds directly into Permission System (F7) |
| Edit Role | Row action | Opens Edit Role drawer |
| Row click | Click target | Expands to show account metadata |

---
### Screen: Role & Permission Matrix

**Purpose:** Single admin screen showing "who can do what" — every permission flag's mapping to a Keycloak role/scope, and the ability to edit which flags each role carries.
**User Stories:** US-7.1, US-7.2, US-7.3

#### Layout

```
┌──────────────────────────────────────────────────────────────────┐
│  Roles & Permissions                                                │
├──────────────────────────────────────────────────────────────────┤
│  Permission Flag        Booker  Approver  Admin   Keycloak Role     │
│  ─────────────────────────────────────────────────────────────    │
│  accessCalendar           ✓        ✓        ✓     ROLE_USER         │
│  allowRoomBooking         ✓        ✓        ✓     ROLE_BOOKER       │
│  viewRoomBooking          ✓        ✓        ✓     ROLE_VIEWER       │
│  allowApproveBooking      ✗        ✓        ✓     ROLE_APPROVER     │
│  accessPermissions        ✗        ✗        ✓     ROLE_ADMIN        │
│  allowAPI               ⚠Pending  ⚠Pending ⚠Pending  (unconfirmed)  │
│                                                                      │
│  ⚠ Flags marked "Pending" are treated as restrictive (deny-by-      │
│    default) until confirmed — see F0 Open Questions.                │
└──────────────────────────────────────────────────────────────────┘
```

#### Information Hierarchy

| Priority | Content | Placement |
|----------|---------|-----------|
| Primary | Permission flag name, per-role checkbox grid | Core matrix |
| Primary | "Pending confirmation" warning chip | Inline per unconfirmed flag, impossible to mistake for "unrestricted" |
| Secondary | Mapped Keycloak role/scope | Rightmost column |
| Tertiary | Footnote explaining deny-by-default policy | Below matrix |

#### States

| State | Appearance | User Feedback |
|-------|------------|---------------|
| Default | Matrix populated, checkboxes reflect current mapping | N/A |
| Editing a cell | Checkbox toggled, "Unsaved changes" bar appears | "You have unsaved changes [Save][Discard]" |
| Save success | Bar dismisses; banner | "Permissions updated" + recorded in audit log (US-7.2) |
| Attempt to reference unconfirmed flag in an update | Inline rejection | "Unknown or unconfirmed permission flag" (US-7.2, 400) |
| Forbidden (no `accessPermissions`) | Screen/nav item absent | N/A (US-7.2) |
| Flag unconfirmed | Cells render with a distinct "⚠ Pending" treatment, never shown as blank/unrestricted | Tooltip: "Gating scope not yet confirmed by legacy audit — treated as restricted" (US-7.1) |

#### Interactive Elements

| Element | Type | Behavior |
|---------|------|----------|
| Checkbox cell | Toggle | Stages a role↔permission mapping change |
| Save | Button | Commits staged changes; requires `accessPermissions` server-side, not just UI-hidden (US-7.2, US-7.3) |
| Discard | Button | Reverts staged checkbox changes |
| Keycloak Role column | Read-only text | Shown for transparency; not directly editable here (edited via Keycloak admin console, out of this app's scope) |

---
### Screen: Settings Admin

**Purpose:** Single-screen system-wide configuration: the `approveBooking` toggle and calendar display parameters, plus a Display & Feeds panel for operational verification.
**User Stories:** US-10.1, US-10.2, US-10.3

#### Layout

```
┌──────────────────────────────────────────────────────────────────┐
│  Settings                                                           │
├──────────────────────────────────────────────────────────────────┤
│  Approval Workflow                                                  │
│  Require approval for new bookings   [●━━ ON]                       │
│  ⓘ Applies only to bookings created after you save this change      │
│                                                                       │
│  Calendar Display                                                    │
│  Slot size (minutes)      [30___]                                   │
│  Earliest time shown      [07:00 AM]                                 │
│  Latest time shown        [08:00 PM]                                 │
│                                                                       │
│  Display & Feeds                                                     │
│  [Preview Display Board]      [Copy feed links →]                    │
│                                                                       │
│  Last updated: Oct 6, 2026 14:32:07 by Priya Patel                   │
│                                [Save Settings]                       │
└──────────────────────────────────────────────────────────────────┘
```

#### Information Hierarchy

| Priority | Content | Placement |
|----------|---------|-----------|
| Primary | `approveBooking` toggle with non-retroactivity note | Top of form |
| Primary | Calendar slot size / min-max time | Middle section |
| Secondary | Display & Feeds operational links | Lower section |
| Tertiary | Last-updated audit stamp | Footer, always visible |

#### States

| State | Appearance | User Feedback |
|-------|------------|---------------|
| Default | Current singleton values loaded | N/A |
| Validation error: min time after max time | Inline error on both time fields | "Calendar minimum time must be before maximum time" (US-10.2) |
| Validation error: slot size ≤ 0 | Inline error | "Calendar slot size must be a positive number of minutes" (US-10.2) |
| Save success | Persistent banner with exact timestamp | "Settings updated at 14:32:07 — applies to bookings created after this time only" (US-10.3) |
| Settings Service unavailable | Form-level banner, Save disabled | "Settings currently unavailable — try again shortly" (503, US-10.3) |
| Forbidden (no admin permission) | Screen/nav item absent; read-only variant not exposed to non-admins in this screen | N/A |

#### Interactive Elements

| Element | Type | Behavior |
|---------|------|----------|
| Approval toggle | Switch | Stages `approveBooking` change |
| Slot size / min / max time | Number / time inputs | Client + server validated before save |
| Save Settings | Primary button | Persists singleton record; one record system-wide, never creates a new one (US-10.3) |
| Preview Display Board | Button | Opens `/display-board` in a new tab for operational verification |
| Copy feed links | Button | Navigates to Feed Subscription page |
| Last updated stamp | Read-only text | Always visible so "did this take effect?" never requires a separate audit-log trip |

---
### Screen: Audit Log Viewer

**Purpose:** Admin-only, filterable, paginated, immutable record of every state-changing action system-wide — the legacy `Logfiles` equivalent, and the shared trust mechanism for dispute resolution (David) and compliance sign-off (Priya).
**User Stories:** US-11.1, US-11.2, US-11.3

#### Layout

```
┌──────────────────────────────────────────────────────────────────┐
│  Audit Log                                                          │
├──────────────────────────────────────────────────────────────────┤
│  Entity Type [Booking ▾]  Actor [____]  Date [Oct 1 ▾]–[Oct 6 ▾]    │
├──────────────────────────────────────────────────────────────────┤
│  Timestamp          Actor       Entity          Action    Details   │
│  ─────────────────────────────────────────────────────────────    │
│  Oct 6 14:32:07     Priya P.    Settings         updated   approveBooking: false→true│
│  Oct 6 10:05:22     David O.    Booking #482     denied    reason: "Room double-booked"│
│  Oct 6 09:58:11     Maya T.     Booking #482     updated   start_time: 10:00→10:30│
│  Oct 5 16:02:00     Priya P.    Location #12     created   name: "Room 2B"│
├──────────────────────────────────────────────────────────────────┤
│                                          [◀ Prev] Page 1 of 9 [Next ▶]│
└──────────────────────────────────────────────────────────────────┘
```

#### Information Hierarchy

| Priority | Content | Placement |
|----------|---------|-----------|
| Primary | Timestamp, Actor, Action type | Leftmost columns — the three facts that settle a dispute |
| Primary | Entity type + id | Enables deep-linking from any record's "View history" |
| Secondary | Before→after detail string | Rightmost column, expandable for full diff |
| Tertiary | Filter controls | Toolbar above table |

#### States

| State | Appearance | User Feedback |
|-------|------------|---------------|
| Default | Rows populated, newest first | N/A |
| Pre-filtered (deep-linked from a booking) | `entity_id` filter chip pre-applied | Chip: "Booking #482 ✕" |
| Validation error: `date_from` after `date_to` | Inline error on date filter | "date_from must be before date_to" (US-11.1) |
| Empty (no matches) | Empty state | "No audit entries match your filters" |
| Loading | Row skeletons | "Loading audit log…" |
| Forbidden (no admin/Logfiles-equivalent permission) | Screen/nav item absent | N/A (US-11.1) |
| Row expand | Reveals full before/after JSON diff | "before_values / after_values" shown as a two-column diff |
| Immutable record indicator | Small lock icon next to every row | Tooltip: "Audit entries cannot be edited or deleted" (US-11.2) |

#### Interactive Elements

| Element | Type | Behavior |
|---------|------|----------|
| Entity Type filter | Dropdown | Scopes query (Booking, Location, Resource, User, Settings, Permission) |
| Actor filter | Text input (autocomplete) | Filters by actor name/id |
| Date range filter | Date pickers | Rejects invalid range client + server side |
| Row expand chevron | Click target | Shows full before/after diff |
| "Copy summary" (per row) | Button | Copies a shareable plain-text summary for dispute resolution (JRN-02.2) |

---
### Screen: Public Display Board (Digital Signage)

**Purpose:** A public, unauthenticated, auto-refreshing screen-friendly view of upcoming approved bookings, designed for lobby/corridor kiosk hardware — never exposes pending/denied bookings.
**User Stories:** US-9.1, US-9.4

#### Layout

```
┌──────────────────────────────────────────────────────────────────┐
│                      📍 Building A — Room Schedule                │
│                                                       🕐 2:14 PM    │
├──────────────────────────────────────────────────────────────────┤
│  NOW                                                                │
│  ┌────────────────────┐  ┌────────────────────┐                   │
│  │ Conf Room 2B        │  │ Studio 1            │                  │
│  │ Client Call          │  │ Team Sync            │                  │
│  │ 2:00 – 3:00 PM ✓    │  │ 2:30 – 4:00 PM  ✓   │                  │
│  └────────────────────┘  └────────────────────┘                   │
│                                                                      │
│  UP NEXT                                                            │
│  Boardroom A — All-Hands — 3:00–4:00 PM  ✓                          │
├──────────────────────────────────────────────────────────────────┤
│  [Building A ▾]                 Subscribe to this calendar →        │
└──────────────────────────────────────────────────────────────────┘
```

#### Information Hierarchy

| Priority | Content | Placement |
|----------|---------|-----------|
| Primary | Currently active bookings ("NOW" cards) | Top, largest type size (kiosk legibility) |
| Primary | "✓" approved micro-label on every entry | Inline on every card, reinforces US-9.4 guarantee |
| Secondary | "UP NEXT" upcoming bookings | Below current |
| Tertiary | Location filter, Subscribe link | Footer, small type (not kiosk-critical, but present for the rare interactive touch-screen deployment) |

#### States

| State | Appearance | User Feedback |
|-------|------------|---------------|
| Default | Cards populated per location filter | N/A |
| No current bookings | "NOW" section shows calm empty state | "No meetings in progress" |
| No upcoming bookings | "UP NEXT" section shows empty state | "Nothing else scheduled today" |
| Auto-refresh | Silent background refresh, no flicker/flash | Small corner indicator pulses briefly on data refresh (sub-1-minute latency, US-9.4) |
| Data source unavailable | Full-screen calm fallback, not a raw error stack trace | "Schedule temporarily unavailable — please check with reception" (503 handled gracefully, kiosk-appropriate) |
| Filtered to unknown location | Empty board, no error (consistent with feed behavior) | "No bookings for this location" |

#### Interactive Elements

| Element | Type | Behavior |
|---------|------|----------|
| Location filter | Dropdown (optional, touch-enabled deployments only) | Re-queries board scoped to selected location (US-9.1) |
| Subscribe to this calendar | Link | Navigates to Feed Subscription page |
| (No login control anywhere on this screen) | N/A | By design — zero authentication friction (US-9.3, US-9.4) |

**Hard guarantee (not just styling):** this screen's data query is structurally restricted server-side to `status=approved` — there is no client-side toggle, filter, or hidden admin mode that could ever surface a pending or denied booking here, satisfying US-9.4's "never, regardless of caller access level."

---
### Screen: Feed Subscription (iCal / RSS2 / JSON links)

**Purpose:** A fully public landing page (interim Key Decision, pending final F0 confirmation) where a user obtains subscribable feed URLs for their location of interest — no account required at all, for any path.
**User Stories:** US-9.2, US-9.3, US-9.4

#### Layout

```
┌──────────────────────────────────────────────────────────────────┐
│  Subscribe to Booking-Hub Feeds                                     │
├──────────────────────────────────────────────────────────────────┤
│  Filter by location:  [Building A ▾]   (optional — leave blank for │
│                                          all locations)              │
│                                                                       │
│  📅  iCal (subscribe in your calendar app)                          │
│      https://bookinghub.example.com/feeds/ical?location=A  [Copy]   │
│                                                                       │
│  📰  RSS2                                                            │
│      https://bookinghub.example.com/feeds/rss2?location=A  [Copy]   │
│                                                                       │
│  🔗  JSON (for integrations)                                         │
│      https://bookinghub.example.com/feeds/json?location=A  [Copy]   │
│                                                                       │
│  📺  View live Display Board →                                       │
├──────────────────────────────────────────────────────────────────┤
│  ⓘ These feeds show only approved, upcoming bookings and refresh     │
│    within 1 minute of any approval.                                  │
└──────────────────────────────────────────────────────────────────┘
```

#### Information Hierarchy

| Priority | Content | Placement |
|----------|---------|-----------|
| Primary | Location filter, per-format URL + Copy button | Core content, one row per format |
| Secondary | "View live Display Board" cross-link | Below the format list |
| Tertiary | Trust/content footnote (approved-only, refresh latency) | Footer, reinforces US-9.4 guarantee |

#### States

| State | Appearance | User Feedback |
|-------|------------|---------------|
| Default | All four format rows rendered with location-scoped URLs | N/A |
| No location selected | URLs omit the `location` query param (all-locations feed) | Placeholder note: "Showing all locations" |
| Copy action | Button briefly shows checkmark | "Copied!" (2s micro-confirmation) |
| Unknown/invalid location filter | Page still renders successfully (empty feed is a valid, non-error outcome per US-9.2) | No error shown — URL generated as normal; consuming app will simply show an empty feed |

#### Interactive Elements

| Element | Type | Behavior |
|---------|------|----------|
| Location dropdown | Select (optional) | Regenerates all four URLs with the selected `location_id` param |
| Copy (per format) | Button | Copies the exact URL to clipboard |
| View live Display Board | Link | Opens `/display-board` filtered to the same location |

---
### Screen: Login, My Account & Password Reset

**Purpose:** Authentication entry point, self-service profile/password editing, and the forgot-password recovery flow — all backed by Keycloak rather than a locally stored credential.
**User Stories:** US-6.2, US-6.3, US-6.4

#### Layout

```
── Login (/login) ──
┌──────────────────────────────────────┐
│          Booking-Hub                  │
│  Email/Username [______________]      │
│  Password        [______________]      │
│  ☐ Remember me                        │
│             [Log In]                  │
│  Forgot password?                     │
└──────────────────────────────────────┘

── My Account (/account) ──
┌──────────────────────────────────────────────────┐
│  My Account                                         │
│  Display Name     [Maya Torres_______]              │
│                                   [Save Profile]     │
│  ── Change Password ──                               │
│  Current Password  [______________]                  │
│  New Password       [______________]                  │
│                                   [Change Password]   │
└──────────────────────────────────────────────────┘

── Password Reset Request (/password-reset/request) ──
┌──────────────────────────────────────┐
│  Reset your password                  │
│  Email/Username [______________]      │
│             [Send reset link]         │
│  "If an account exists, a reset link  │
│   has been sent." (always shown)      │
└──────────────────────────────────────┘

── Password Reset Complete (/password-reset/complete?token=...) ──
┌──────────────────────────────────────┐
│  Choose a new password                │
│  New Password     [______________]     │
│  Confirm Password  [______________]     │
│             [Set New Password]        │
└──────────────────────────────────────┘
```

#### Information Hierarchy

| Priority | Content | Placement |
|----------|---------|-----------|
| Primary | Credential fields, primary action button | Center of each screen, single-column form |
| Secondary | "Remember me" / "Forgot password?" | Below credential fields on Login |
| Tertiary | Generic reset-acknowledgment copy | Below the Send button, always identical regardless of account existence |

#### States

| State | Appearance | User Feedback |
|-------|------------|---------------|
| Login: invalid credentials | Form-level red banner | "Invalid username or password" (US-6.4, 401) |
| Login success | Redirect to Calendar View | — |
| Protected route without valid token | Auto-redirect to Login | "Please log in to continue" (US-6.4) |
| My Account: wrong current password | Inline error on Current Password field | "Current password is incorrect" (US-6.2, 401) |
| My Account: new password fails policy | Inline error on New Password field | "New password does not meet complexity requirements" (US-6.2, 400) |
| My Account: save success | Toast | "Profile updated" / "Password changed" |
| Reset request: any input | Always shows the same generic success message, regardless of whether the account exists | "If an account exists, a reset link has been sent." (US-6.3 — deliberately uninformative to prevent account enumeration) |
| Reset complete: expired/invalid/used token | Form-level error, no password fields accepted | "This password reset link is invalid or has expired" (US-6.3, 400) |
| Reset complete: success | Redirect to Login with a success banner | "Password updated — please log in" |

#### Interactive Elements

| Element | Type | Behavior |
|---------|------|----------|
| Remember me | Checkbox | Extends issued session/token lifetime (US-6.4) |
| Log In | Primary button | Authenticates against Keycloak; on success returns access/refresh token + roles |
| Forgot password? | Link | Navigates to Password Reset Request |
| Save Profile | Button | Updates display name; no admin involvement required (US-6.2) |
| Change Password | Button | Delegates to Keycloak credential-update mechanism |
| Send reset link | Button | Publishes `password.reset.requested` event; always shows generic acknowledgment (US-6.3) |
| Set New Password | Button | Validates token + policy, then updates credential via Keycloak |

---
## Interaction Patterns

### Pattern: Explicit State-Confirmation Banner

**When to use:** After any mutating action where the user's historical pain point is "did this actually take effect?" — booking save, approve/deny, settings change, location/resource save, permission mapping update.
**Behavior:** A persistent (not auto-dismissing-in-3-seconds) banner or toast states the exact outcome in plain language, including the resulting status where relevant (e.g., "Pending approval" vs. "Approved") and, for Settings, an explicit effective timestamp. Contrasts with a bare "Success" toast, which the cross-journey analysis identified as insufficient to rebuild trust.
**Examples:** Booking Create/Edit Form save confirmation (US-1.1, US-3.4), Settings Admin save confirmation (US-10.3), Locations Admin save confirmation (US-4.1).

---

### Pattern: Boundary/Scope Confirmation Dialog

**When to use:** Any action where the system is about to apply a change across an ambiguous or consequential scope — recurring booking edit/delete, permission mapping changes, destructive deletes.
**Behavior:** A modal blocks the action until the user makes an explicit, named choice (never a silent default). The dialog states, in plain language, the exact blast radius of each option before the user commits ("This occurrence only" vs. "Entire series"). Confirmation after the fact (a post-save summary restating what was/was not changed) reinforces the decision.
**Examples:** Recurring booking edit/delete scope (US-1.2, US-1.6), Location/Resource delete-in-use confirmation (US-4.3).

---

### Pattern: Single Conflict Badge Component

**When to use:** Anywhere a booking with `conflict_flags[]` is rendered — Calendar, Day, List, Approval Queue, Booking Detail Modal.
**Behavior:** One shared component renders the ⚠ icon, red-tinted background/outline, and hover tooltip (conflicting booking id + conflict type: location or resource). It is never re-implemented per screen, guaranteeing visual and logical consistency across every entry point (US-2.2's "identical regardless of entry point" requirement, lifted directly into the component architecture).
**Examples:** All booking-list/grid screens; Approval Queue rows; Booking Detail Modal.

---

### Pattern: Deny-by-Default Visibility (Permission-Gated UI)

**When to use:** Any nav item, button, or screen gated by a permission flag — especially one marked unconfirmed per F0/F7.
**Behavior:** A user lacking the required permission simply does not see the element at all — it is omitted from the render tree, not shown-and-disabled with an ambiguous tooltip. For admin-facing permission-configuration screens themselves (Role & Permission Matrix), an *unconfirmed* flag is visually distinct (⚠ Pending chip) from a *confirmed-and-denied* flag (✗), so nobody mistakes "we don't know yet" for "definitely no access."
**Examples:** Sidebar "Approvals" item (US-3.1), Admin section (US-4.1/US-7.2), Role & Permission Matrix "Pending" cells (US-7.1).

---

### Pattern: Dynamic Form Field Rendering by Context

**When to use:** The Booking Create/Edit Form, where custom fields must appear only when applicable to the selected location's context.
**Behavior:** Changing the Location field re-queries the Custom Field Service and re-renders the applicable fields section without a full form reload or loss of already-entered standard fields (title, time, resources).
**Examples:** Booking Create/Edit Form custom fields section (US-5.2).

---

### Pattern: Immutable Record Indicator

**When to use:** Audit Log Viewer and any screen that surfaces audit-sourced history (Booking Detail Modal "View history").
**Behavior:** A small lock icon and tooltip ("Audit entries cannot be edited or deleted") reinforces that this is a system-of-record, not just another editable table — directly supporting the dispute-resolution use case (JRN-02.2).
**Examples:** Audit Log Viewer rows (US-11.2).

---

### Pattern: Approved-Only Hard Guarantee on Public Surfaces

**When to use:** Display Board and all Feed Subscription formats.
**Behavior:** The underlying data query is structurally restricted to `status=approved` server-side; the UI additionally reinforces this with a visible "✓ Approved" micro-label so the guarantee is legible to the end user, not just enforced invisibly in the API.
**Examples:** Display Board booking cards (US-9.1, US-9.4), Feed Subscription footnote (US-9.4).

---
## Responsive Considerations

### Desktop (>1024px)

- Full sidebar + header app shell persists across all authenticated screens; Calendar/Day views render multi-column grids (multiple locations side by side in Day View).
- Admin screens (Locations, Resources, Custom Fields, Users, Roles, Settings, Audit Log) use inline drawers/side panels for create/edit forms rather than full-page navigation, preserving list context (supports the "single-pass, under 5 minutes" onboarding goal, JRN-03.1).
- Display Board is designed primarily for desktop-class kiosk hardware/large-format displays; "NOW"/"UP NEXT" cards render at a 2–3 column grid.

### Tablet (768px–1024px)

- Sidebar collapses to an icon-only rail with flyout labels on hover/tap; Admin section becomes a single expandable group.
- Day View's per-location columns become horizontally scrollable with sticky time-axis on the left, rather than compressing illegibly.
- Booking Create/Edit Form and admin drawers become full-screen overlays instead of side panels, preserving touch-target sizing.
- Approval Queue rows stack the Approve/Deny buttons below the booking summary rather than right-aligned inline, to avoid accidental mis-taps.

### Mobile (<768px)

- Sidebar becomes a bottom nav bar or hamburger menu; "Calendar / Day / List" view switcher becomes a dropdown rather than a tab row.
- Calendar (month grid) view is de-emphasized in favor of List View as the practical default on small screens — month grids with colour blocks are not legible at this size; the view switcher still allows access to Calendar/Day for users who want it.
- Booking Detail Modal and Create/Edit Form become full-screen single-column layouts; the Scope Confirmation Dialog and conflict banner remain fully blocking (no bottom-sheet dismiss-by-swipe for a decision that requires an explicit choice, since accidental dismissal must never silently default to a scope).
- Audit Log Viewer's table collapses to a card-per-entry layout (Timestamp/Actor/Action stacked vertically) rather than a horizontally scrolling table.
- Display Board is not optimized for mobile as a primary target (it is a kiosk/signage surface) but remains functional and legible if viewed on a phone for spot-checking.
- Feed Subscription page's Copy buttons use the native share sheet on mobile where available, in addition to clipboard copy.

---
## Accessibility Notes

- **Colour is never the sole signal.** Location colour-coding (US-4.4) and conflict badges (US-2.2) always pair colour with a non-colour indicator — a ⚠ glyph for conflicts, and a text label or pattern-fill fallback for location identity — so colour-blind users and the Display Board's printed/greyscale fallback remain legible.
- **Contrast:** All status badges (Pending/Approved/Denied), conflict banners, and the "Pending" permission-matrix chip meet WCAG 2.1 AA contrast ratios (4.5:1 for text, 3:1 for large text/icons) against both light and dark theme backgrounds.
- **Keyboard navigation:** Every interactive element documented in this mockup (calendar cells, booking blocks, Approve/Deny buttons, drawer forms, the Scope Confirmation Dialog, the Role & Permission Matrix checkbox grid) is reachable and operable via Tab/Shift+Tab/Enter/Space, with a visible focus ring at all times — no mouse-only affordances (e.g., hover-only conflict tooltips also expose their content via a focusable element, not solely `:hover`).
- **Modal focus trapping:** The Booking Create/Edit Form, Booking Detail Modal, and Scope Confirmation Dialog trap focus within themselves while open and return focus to the triggering element on close, consistent with WAI-ARIA Authoring Practices for dialogs. The Scope Confirmation Dialog specifically cannot be dismissed via Escape/outside-click without an explicit choice, since silent dismissal must never be interpreted as a default scope selection (US-1.2).
- **Screen reader considerations:**
  - Conflict badges announce their full tooltip content via `aria-label` (e.g., "Conflict warning: overlaps Booking 482, resource conflict"), not just an icon with no accessible name.
  - Status badges (Pending/Approved/Denied) use `aria-live="polite"` regions when they change as a result of an in-page action (e.g., Approval Queue row fading out after Approve), so the outcome is announced without requiring the user to re-scan the page.
  - The Settings Admin "Last updated" stamp and the Booking save confirmation banner are both exposed as `aria-live="polite"` regions, directly supporting the "did this actually take effect?" trust requirement for screen reader users as much as sighted users.
  - Table-based screens (List View, Audit Log Viewer, Role & Permission Matrix) use proper `<table>` semantics with `<th scope="col">`/`<th scope="row">` so assistive tech can announce column/row context per cell, not just raw text.
- **ARIA labels needed:**
  - Calendar/Day view booking blocks: `role="button"` with `aria-label` summarizing title, time, and conflict state in one string.
  - Icon-only admin row actions (Edit/Delete pencil/trash icons): explicit `aria-label="Edit Room 2B"` / `aria-label="Delete Room 2B"`, never icon-only with no text alternative.
  - Display Board auto-refresh indicator: `aria-live="off"` by design (a kiosk display is not read by a screen reader in situ), but the underlying feed data (JSON/RSS2/iCal) remains the accessible equivalent for any assistive consumption path (US-9.2, US-9.3).
  - Copy-to-clipboard buttons on the Feed Subscription page: `aria-label` states the exact format being copied (e.g., "Copy iCal feed URL"), and the post-copy "Copied!" confirmation is announced via `aria-live="polite"`.
- **Form error association:** every inline validation error (empty title, invalid time range, missing scope, password policy violation, etc.) is programmatically associated with its field via `aria-describedby`, not conveyed by colour/position alone.

---

*Document generated by Pivota Spec Framework*
*Last updated: 2026-10-06*
