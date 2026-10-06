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
