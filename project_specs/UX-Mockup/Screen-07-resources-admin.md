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
