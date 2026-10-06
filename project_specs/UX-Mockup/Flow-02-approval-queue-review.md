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
          │          — interim hard     denial_reason]
          │          block means this
          │          path is disabled
          │          pending F0]
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
5. Per the interim hard-block default (FRD F2 §Process step 6), "Approve anyway despite conflict" is not offered as a UI action until F0 confirms a soft-warning policy exists — the Approve button for a hard-blocked conflicted row is replaced with a "Resolve conflict first" state, preventing an approver from creating a double-booking via the approval path itself.
6. Denying a row opens a lightweight inline reason field (optional free text, US-3.3) before confirming.
7. Approved/denied rows disappear from the queue immediately (optimistic UI) and a toast confirms the one-way transition (US-3.2/US-3.3: "Approve/deny are one-way transitions").
8. Rows for bookings created while auto-approve was active show an "Auto-approved" status chip with a hover tooltip: "Auto-approved per Settings at [timestamp]" — a direct link to Settings Admin lets David instantly confirm this matches the current configuration (US-3.4, JRN-02.1 Verify Boundary stage), with zero manual cross-checking.

**Exit point:** Empty (or reduced) queue; David moves to other work with full confidence every remaining row requires his attention for a documented reason.

---
