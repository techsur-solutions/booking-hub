### Flow 4: Resolving a Disputed Approval Decision

**Trigger:** A requester disputes that they never received a decision on a booking David recalls denying.
**User Story:** US-11.1, US-11.2
**Journey Reference:** JRN-02.2

```
[Approval Queue or Calendar View]
          │
          ▼
[Search/locate the disputed booking — List View search by
 requester name + date range]
          │
          ▼
[Booking Detail Modal] ──▶ [Click "View history" link]
          │
          ▼
[Audit Log Viewer /admin/audit-log — pre-filtered to this booking_id]
┌──────────────────────────────────────────────────────────┐
│ Timestamp           Actor        Action        Before→After │
│ 2026-09-28 14:02    David.O      booking.denied pending→denied│
│                                   denial_reason: "Room double-│
│                                   booked for exec review"      │
└──────────────────────────────────────────────────────────┘
          │
          ▼
[David shares/screenshots the immutable audit entry with
 the requester — dispute resolved]
```

**Steps:**
1. David locates the disputed booking using the List View's search (by requester, date range, status) rather than scanning manually (US-1.5 list filtering capability; JRN-02.2 "Locate Booking" risk-of-abandonment stage).
2. He opens the Booking Detail Modal and clicks "View history," which deep-links into the Audit Log Viewer pre-filtered to this exact `booking_id` (US-11.2) — David never has to construct the filter manually.
3. The audit trail shows actor, timestamp, action_type, and before/after values for every lifecycle transition on this booking (create → pending → denied), satisfying US-11.2's requirement that entries include actor/timestamp/action_type/before-after.
4. Because audit log entries are immutable once written (US-11.2, no edit/delete API exists), the displayed record is presented with a visible "Immutable record" indicator reinforcing its authority as a dispute-resolution tool.
5. David shares the entry (via screenshot or a "Copy summary" button) with the requester, closing the dispute with an authoritative, timestamped fact rather than his memory.

**Exit point:** Dispute resolved entirely within the system — zero reliance on external email archaeology (JRN-02.2 success outcome).

---
