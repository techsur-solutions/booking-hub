### Flow 7: Directing a Visitor via the Display Board and Setting Up a Personal Feed

**Trigger:** A visitor at the front desk needs to be directed to the correct room; Jordan later wants a personal, login-free iCal subscription.
**User Story:** US-9.1, US-9.2, US-9.3, US-9.4
**Journey Reference:** JRN-04.1

```
[Lobby kiosk hardware, bookmarked] ──▶ [Display Board /display-board]
            │
            ▼
  [Location filter dropdown: "Building A ▾"]
            │
            ▼
  ┌─────────────────────────────────────────┐
  │  NOW SHOWING                             │
  │  Conf Room 2B   2:00–3:00 PM   ✓ Approved│
  │  Studio 1       2:30–4:00 PM   ✓ Approved│
  └─────────────────────────────────────────┘
  (auto-refreshes client-side every <60s; only status=approved
   bookings ever rendered — US-9.1, US-9.4)
            │
            ▼
  [Jordan confirms room, directs visitor with confidence]
            │
            ▼
  [Later: clicks footer link "Subscribe to this calendar"]
            │
            ▼
  [Feed Subscription /feeds]
  ┌─────────────────────────────────────────┐
  │ Location: [Building A ▾]                 │
  │                                           │
  │ 📅 iCal:  https://.../feeds/ical?location=A  [Copy]│
  │ 📰 RSS2:  https://.../feeds/rss2?location=A  [Copy]│
  │ 🔗 JSON:  https://.../feeds/json?location=A  [Copy]│
  └─────────────────────────────────────────┘
            │
            ▼
  [Jordan copies the iCal link, pastes into personal calendar
   app's "Add by URL" — no login prompt at any step, US-9.3]
```

**Steps:**
1. The Display Board is bookmarked on lobby kiosk hardware and filtered to "Building A" so Jordan only sees relevant rooms (US-9.1, US-9.3 filtering parity across formats).
2. The board shows only bookings where `status=approved`; pending/denied bookings are structurally never queried for this view, not merely hidden by a client-side filter (US-9.4) — this is reinforced with a small "✓ Approved" micro-label on each row so Jordan never has to wonder.
3. The board auto-refreshes client-side (polling or SSE) with a sub-1-minute latency guarantee from the moment a booking is approved (US-9.4, JRN-04.1 success measure).
4. Jordan confidently directs the visitor, then later clicks a footer link to the Feed Subscription page to set up a personal iCal feed.
5. The Feed Subscription page presents all four formats (display board link, iCal, RSS2, JSON) side by side, each respecting the same `location_id` filter and the same access-control evaluation (US-9.3: "a caller denied access to one format is equally denied access to all other formats").
6. Jordan copies the iCal URL and adds it to a personal calendar app — no login prompt appears at any point in this entire flow, satisfying US-9.3's zero-login-friction requirement and JTBD-04.3's success measure.

**Exit point:** Visitor directed correctly; Jordan has a perpetually self-syncing personal feed requiring no further trips to the lobby board.

---
