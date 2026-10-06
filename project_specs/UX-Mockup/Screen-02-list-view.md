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
