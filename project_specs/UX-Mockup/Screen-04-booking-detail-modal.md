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
