### Screen: Custom Field Builder

**Purpose:** Admin screen for defining custom field templates (label, type, options) and attaching them to a context (a specific location or "all bookings"), which the Booking Form renders dynamically.
**User Stories:** US-5.1, US-5.3

#### Layout

```
┌──────────────────────────────────────────────────────────────────┐
│  Custom Fields                                    [+ New Field]    │
├──────────────────────────────────────────────────────────────────┤
│  Label                 Type      Applies to        Actions         │
│  ─────────────────────────────────────────────────────────────    │
│  Catering Headcount    Number    Room 2B            [Edit][Delete] │
│  Setup Notes           Text      All bookings        [Edit][Delete] │
│  Room Layout           Select    Studio 1, Boardroom A [Edit][Delete]│
├──────────────────────────────────────────────────────────────────┤
│  Field Templates                                   [+ New Template]│
│  ─────────────────────────────────────────────────────────────    │
│  "Executive Meeting Pack" → Catering Headcount, Setup Notes         │
└──────────────────────────────────────────────────────────────────┘

── New/Edit Custom Field form ──
┌──────────────────────────────────────────────┐
│  Label *          [____________]              │
│  Field Type *     [Text ▾]                    │
│      (if Select)  Options: [Round][Theatre][+Add]│
│  Applies to        ◉ Specific location: [Room 2B ▾]│
│                     ○ All bookings              │
│                                                  │
│                      [Cancel]   [Save]          │
└──────────────────────────────────────────────┘
```

#### Information Hierarchy

| Priority | Content | Placement |
|----------|---------|-----------|
| Primary | Label, Field Type | Leftmost columns |
| Primary | Options editor (only shown when Type = Select) | Conditionally rendered in form |
| Secondary | "Applies to" context scoping | Middle column / form |
| Tertiary | Field Templates grouping section | Below the main field list |

#### States

| State | Appearance | User Feedback |
|-------|------------|---------------|
| Default | List populated, grouped by field then templates | N/A |
| Empty | Empty state | "No custom fields yet — define fields to appear on booking forms" |
| Validation error: empty label | Inline error | "Custom field label is required" (US-5.1) |
| Validation error: Select type with no options | Inline error under Options editor | "Options are required for selection-type fields" (US-5.1) |
| Save success | Drawer closes; banner | "Custom field saved — now appears on applicable booking forms" |
| Delete field with historical values | Confirmation notes retention | "Past bookings that used this field will keep their recorded values; this field will no longer appear on new/edit forms." (US-5.3 — deleting a definition never purges historical values) |
| Forbidden | Screen/nav item absent | N/A |

#### Interactive Elements

| Element | Type | Behavior |
|---------|------|----------|
| + New Field | Button | Opens field creation form |
| Field Type dropdown | Select (Text/Number/Date/Select) | Toggles Options editor visibility |
| Applies-to radio | Radio (Specific location / All bookings) | Determines the CustomFieldJoin context used by the Booking Form's dynamic renderer (US-5.1, US-5.2) |
| + New Template | Button | Opens template form grouping multiple fields together |
| Edit / Delete | Row actions | Standard drawer edit / confirm-delete |

---
