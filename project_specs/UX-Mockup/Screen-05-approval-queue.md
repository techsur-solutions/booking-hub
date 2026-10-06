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
| Conflict hard-blocked (interim default) | Approve button replaced with disabled state | Tooltip: "Resolve the conflict before this booking can be approved" |
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
