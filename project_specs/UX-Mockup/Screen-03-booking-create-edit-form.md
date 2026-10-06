### Screen: Booking Create / Edit Form

**Purpose:** Single form for creating a new booking or editing an existing one, including recurrence definition, multi-resource attachment, dynamic custom fields, and conflict feedback before save.
**User Stories:** US-1.1, US-1.2, US-1.6, US-2.1, US-2.3, US-4.4, US-5.1, US-5.2

#### Layout

```
┌──────────────────────────────────────────────────────────────────┐
│  New Booking                                            [✕ Close] │
├──────────────────────────────────────────────────────────────────┤
│  Title *            [__________________________________]          │
│  Location *         [Room 2B ▾]            ■ (colour swatch)       │
│  Start              [Oct 6, 2026  10:00 AM]                        │
│  End                [Oct 6, 2026  11:00 AM]  (defaults +1h if blank)│
│  Resources           [+ Add Resource]                               │
│                       • Conference Phone [✕]                        │
│  ── Custom Fields (scoped to Room 2B) ──────────────────────────    │
│  Catering Headcount  [____]                                         │
│  ── Recurrence ──────────────────────────────────────────────       │
│  ○ One-time   ● Repeats weekly on [Mon ▾] until [Nov 24, 2026]      │
│                (must resolve to a finite set of occurrences)        │
│                                                                      │
│  ⚠ Conflict: overlaps Booking #482 (resource: Conference Phone)     │
│     [View conflicting booking]                                      │
│                                                                      │
│                                [Cancel]          [Save Booking]      │
└──────────────────────────────────────────────────────────────────┘

── On Save of a booking belonging to a series ──
┌──────────────────────────────────────┐
│  Apply this change to:                │
│   ◉ This occurrence only              │
│   ○ Entire series                     │
│         [Cancel]        [Confirm]     │
└──────────────────────────────────────┘
```

#### Information Hierarchy

| Priority | Content | Placement |
|----------|---------|-----------|
| Primary | Title, Location, Start/End time | Top of form, required-field asterisk |
| Primary | Conflict banner (if present) | Directly above Save button, impossible to miss before submitting |
| Secondary | Resources picker, Custom Fields (dynamically rendered per location context) | Middle of form |
| Secondary | Recurrence controls | Below custom fields, collapsed to "One-time" by default |
| Tertiary | Scope Confirmation Dialog (edit of series booking only) | Modal overlay, blocks Save until resolved |

#### States

| State | Appearance | User Feedback |
|-------|------------|---------------|
| Default (create) | Empty form, End time placeholder shows "+1h from start" | N/A |
| Default (edit) | Pre-filled with existing values | N/A |
| Validation error: empty title | Red outline + inline text under field | "Booking title is required" (US-1.1) |
| Validation error: end before start | Red outline on End field | "End time must not be before start time" (US-1.1) |
| Validation error: location not found | Form-level banner | "Specified location does not exist" |
| Conflict (hard block, interim default) | Red banner above Save; Save remains clickable for a corrected resubmission | "This booking conflicts with an existing booking for the selected location or resource" (US-2.1) |
| Scope required (series edit, scope omitted) | Scope dialog cannot be dismissed without a selection | "Scope (this occurrence or whole series) is required for recurring bookings" (US-1.2) |
| Saving | Save button shows spinner, disabled | "Saving…" |
| Success | Form closes; toast confirms | "Booking saved — Pending approval" / "— Approved" |
| Custom field value invalid (`field_id` unknown) | Inline error on that field | "This field is no longer valid for this location" |

#### Interactive Elements

| Element | Type | Behavior |
|---------|------|----------|
| Location dropdown | Select | Re-queries applicable custom fields for the newly selected location (US-5.2) |
| + Add Resource | Button → multi-select list | Adds resource chips; each triggers availability re-check |
| Recurrence toggle | Radio (One-time / Repeats) | Expands recurrence sub-form; rejects open-ended series (no "repeats forever" option exists in the UI — US-1.6) |
| Save Booking | Primary button | Validates client-side, submits, triggers conflict check server-side |
| Scope dialog radio buttons | Radio (required) | Must select before "Confirm" enables (US-1.2) |
| View conflicting booking | Link | Opens the conflicting Booking Detail Modal in a secondary panel without losing current form state |

---
