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
