### Screen: Settings Admin

**Purpose:** Single-screen system-wide configuration: the `approveBooking` toggle and calendar display parameters, plus a Display & Feeds panel for operational verification.
**User Stories:** US-10.1, US-10.2, US-10.3

#### Layout

```
┌──────────────────────────────────────────────────────────────────┐
│  Settings                                                           │
├──────────────────────────────────────────────────────────────────┤
│  Approval Workflow                                                  │
│  Require approval for new bookings   [●━━ ON]                       │
│  ⓘ Applies only to bookings created after you save this change      │
│                                                                       │
│  Calendar Display                                                    │
│  Slot size (minutes)      [30___]                                   │
│  Earliest time shown      [07:00 AM]                                 │
│  Latest time shown        [08:00 PM]                                 │
│                                                                       │
│  Display & Feeds                                                     │
│  [Preview Display Board]      [Copy feed links →]                    │
│                                                                       │
│  Last updated: Oct 6, 2026 14:32:07 by Priya Patel                   │
│                                [Save Settings]                       │
└──────────────────────────────────────────────────────────────────┘
```

#### Information Hierarchy

| Priority | Content | Placement |
|----------|---------|-----------|
| Primary | `approveBooking` toggle with non-retroactivity note | Top of form |
| Primary | Calendar slot size / min-max time | Middle section |
| Secondary | Display & Feeds operational links | Lower section |
| Tertiary | Last-updated audit stamp | Footer, always visible |

#### States

| State | Appearance | User Feedback |
|-------|------------|---------------|
| Default | Current singleton values loaded | N/A |
| Validation error: min time after max time | Inline error on both time fields | "Calendar minimum time must be before maximum time" (US-10.2) |
| Validation error: slot size ≤ 0 | Inline error | "Calendar slot size must be a positive number of minutes" (US-10.2) |
| Save success | Persistent banner with exact timestamp | "Settings updated at 14:32:07 — applies to bookings created after this time only" (US-10.3) |
| Settings Service unavailable | Form-level banner, Save disabled | "Settings currently unavailable — try again shortly" (503, US-10.3) |
| Forbidden (no admin permission) | Screen/nav item absent; read-only variant not exposed to non-admins in this screen | N/A |

#### Interactive Elements

| Element | Type | Behavior |
|---------|------|----------|
| Approval toggle | Switch | Stages `approveBooking` change |
| Slot size / min / max time | Number / time inputs | Client + server validated before save |
| Save Settings | Primary button | Persists singleton record; one record system-wide, never creates a new one (US-10.3) |
| Preview Display Board | Button | Opens `/display-board` in a new tab for operational verification |
| Copy feed links | Button | Navigates to Feed Subscription page |
| Last updated stamp | Read-only text | Always visible so "did this take effect?" never requires a separate audit-log trip |

---
