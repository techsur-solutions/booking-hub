### Screen: Feed Subscription (iCal / RSS2 / JSON links)

**Purpose:** A public (or `allowAPI`-scoped, pending F0 confirmation) landing page where a user obtains subscribable feed URLs for their location of interest — no account required for the happy path.
**User Stories:** US-9.2, US-9.3, US-9.4

#### Layout

```
┌──────────────────────────────────────────────────────────────────┐
│  Subscribe to Booking-Hub Feeds                                     │
├──────────────────────────────────────────────────────────────────┤
│  Filter by location:  [Building A ▾]   (optional — leave blank for │
│                                          all locations)              │
│                                                                       │
│  📅  iCal (subscribe in your calendar app)                          │
│      https://bookinghub.example.com/feeds/ical?location=A  [Copy]   │
│                                                                       │
│  📰  RSS2                                                            │
│      https://bookinghub.example.com/feeds/rss2?location=A  [Copy]   │
│                                                                       │
│  🔗  JSON (for integrations)                                         │
│      https://bookinghub.example.com/feeds/json?location=A  [Copy]   │
│                                                                       │
│  📺  View live Display Board →                                       │
├──────────────────────────────────────────────────────────────────┤
│  ⓘ These feeds show only approved, upcoming bookings and refresh     │
│    within 1 minute of any approval.                                  │
└──────────────────────────────────────────────────────────────────┘
```

#### Information Hierarchy

| Priority | Content | Placement |
|----------|---------|-----------|
| Primary | Location filter, per-format URL + Copy button | Core content, one row per format |
| Secondary | "View live Display Board" cross-link | Below the format list |
| Tertiary | Trust/content footnote (approved-only, refresh latency) | Footer, reinforces US-9.4 guarantee |

#### States

| State | Appearance | User Feedback |
|-------|------------|---------------|
| Default | All four format rows rendered with location-scoped URLs | N/A |
| No location selected | URLs omit the `location` query param (all-locations feed) | Placeholder note: "Showing all locations" |
| Copy action | Button briefly shows checkmark | "Copied!" (2s micro-confirmation) |
| Access denied (if `allowAPI` gating confirmed by F0) | Entire format list replaced with a message, not a broken link | "API access is required to view feed links — contact your administrator" (US-9.3: denial applies identically across all formats, never partially) |
| Unknown/invalid location filter | Page still renders successfully (empty feed is a valid, non-error outcome per US-9.2) | No error shown — URL generated as normal; consuming app will simply show an empty feed |

#### Interactive Elements

| Element | Type | Behavior |
|---------|------|----------|
| Location dropdown | Select (optional) | Regenerates all four URLs with the selected `location_id` param |
| Copy (per format) | Button | Copies the exact URL to clipboard |
| View live Display Board | Link | Opens `/display-board` filtered to the same location |

---
