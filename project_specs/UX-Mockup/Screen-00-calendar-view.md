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
