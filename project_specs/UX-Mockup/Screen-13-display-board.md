### Screen: Public Display Board (Digital Signage)

**Purpose:** A public, unauthenticated, auto-refreshing screen-friendly view of upcoming approved bookings, designed for lobby/corridor kiosk hardware — never exposes pending/denied bookings.
**User Stories:** US-9.1, US-9.4

#### Layout

```
┌──────────────────────────────────────────────────────────────────┐
│                      📍 Building A — Room Schedule                │
│                                                       🕐 2:14 PM    │
├──────────────────────────────────────────────────────────────────┤
│  NOW                                                                │
│  ┌────────────────────┐  ┌────────────────────┐                   │
│  │ Conf Room 2B        │  │ Studio 1            │                  │
│  │ Client Call          │  │ Team Sync            │                  │
│  │ 2:00 – 3:00 PM ✓    │  │ 2:30 – 4:00 PM  ✓   │                  │
│  └────────────────────┘  └────────────────────┘                   │
│                                                                      │
│  UP NEXT                                                            │
│  Boardroom A — All-Hands — 3:00–4:00 PM  ✓                          │
├──────────────────────────────────────────────────────────────────┤
│  [Building A ▾]                 Subscribe to this calendar →        │
└──────────────────────────────────────────────────────────────────┘
```

#### Information Hierarchy

| Priority | Content | Placement |
|----------|---------|-----------|
| Primary | Currently active bookings ("NOW" cards) | Top, largest type size (kiosk legibility) |
| Primary | "✓" approved micro-label on every entry | Inline on every card, reinforces US-9.4 guarantee |
| Secondary | "UP NEXT" upcoming bookings | Below current |
| Tertiary | Location filter, Subscribe link | Footer, small type (not kiosk-critical, but present for the rare interactive touch-screen deployment) |

#### States

| State | Appearance | User Feedback |
|-------|------------|---------------|
| Default | Cards populated per location filter | N/A |
| No current bookings | "NOW" section shows calm empty state | "No meetings in progress" |
| No upcoming bookings | "UP NEXT" section shows empty state | "Nothing else scheduled today" |
| Auto-refresh | Silent background refresh, no flicker/flash | Small corner indicator pulses briefly on data refresh (sub-1-minute latency, US-9.4) |
| Data source unavailable | Full-screen calm fallback, not a raw error stack trace | "Schedule temporarily unavailable — please check with reception" (503 handled gracefully, kiosk-appropriate) |
| Filtered to unknown location | Empty board, no error (consistent with feed behavior) | "No bookings for this location" |

#### Interactive Elements

| Element | Type | Behavior |
|---------|------|----------|
| Location filter | Dropdown (optional, touch-enabled deployments only) | Re-queries board scoped to selected location (US-9.1) |
| Subscribe to this calendar | Link | Navigates to Feed Subscription page |
| (No login control anywhere on this screen) | N/A | By design — zero authentication friction (US-9.3, US-9.4) |

**Hard guarantee (not just styling):** this screen's data query is structurally restricted server-side to `status=approved` — there is no client-side toggle, filter, or hidden admin mode that could ever surface a pending or denied booking here, satisfying US-9.4's "never, regardless of caller access level."

---
