## User Flows

### Flow 1: Quick Conflict-Free Room Booking with Resources

**Trigger:** Maya has an open window and needs to book a room + resource for a call today.
**User Story:** US-1.1, US-1.5, US-2.1, US-2.2, US-4.4, US-5.2
**Journey Reference:** JRN-01.1

```
[Calendar View /bookings?view=month]
    │  (sees colour-coded locations, US-4.4)
    ▼
[Click open slot on desired room]
    │
    ▼
[Booking Create Form opens, pre-filled: location + start/end from slot]
    │
    ├─▶ [Attach Resource: "Conference Phone"] ──▶ inline resource-availability check
    │
    ├─▶ [Dynamic Custom Field renders: "Catering Headcount"] (US-5.2, scoped to location context)
    │
    ▼
[Click "Save"]
    │
    ├── Conflict detected (location OR resource) ──▶ [Inline conflict banner: "Conflicts with
    │                                                  Booking #482 (Resource: Conference Phone)"]
    │                                                  │
    │                                                  └─▶ user adjusts time/resource, resubmits
    │
    └── No conflict ──▶ [Save succeeds]
                              │
                              ▼
                   [Toast: "Booking saved — Pending approval" or "Booking saved — Approved"]
                   (status badge immediately visible, per US-3.4)
                              │
                              ▼
                   [Booking Detail Modal auto-opens, confirms full field set] (US-1.7)
                              │
                              ▼
                   [User closes modal, returns to Calendar View — new event visible immediately]
```

**Steps:**
1. Maya opens the Calendar View (default landing page) and visually scans colour-coded locations for an open slot (US-4.4, US-1.5).
2. She clicks an empty slot on the desired room's calendar row; the Booking Create Form opens pre-populated with that location, start time, and a default end time of start + 1 hour (US-1.1).
3. She opens the resource picker and attaches "Conference Phone"; the form renders the "Catering Headcount" custom field because it is scoped to this location's context (US-5.2).
4. She fills in the title and catering headcount, then clicks Save.
5. The system runs conflict detection synchronously across both location and resource scope (US-2.1). If a conflict exists, an inline banner names the specific conflicting booking and conflict type (location vs. resource) — this is a **hard block** in the interim default (FRD F2 §Process step 6); the Save button stays enabled for a corrected resubmission, it does not silently fail.
6. On success, a toast confirms the save and explicitly states the resulting status (`Pending approval` vs. `Approved`) per the current `approveBooking` setting (US-3.4) — this directly resolves the "did that actually go through?" anxiety flagged in JRN-01.1's Submit stage.
7. The Booking Detail Modal opens automatically showing the full persisted record (US-1.7), then the user returns to the Calendar View where the new event is already rendered with the correct location colour and no refresh needed.

**Exit point:** Calendar View, with new booking visible and conflict-free (or flagged, if a soft-scenario slips through after an F0 policy change).

---
