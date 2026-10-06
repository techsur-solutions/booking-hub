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
