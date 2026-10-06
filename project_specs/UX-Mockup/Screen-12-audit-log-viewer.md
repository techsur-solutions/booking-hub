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
