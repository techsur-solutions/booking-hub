## Interaction Patterns

### Pattern: Explicit State-Confirmation Banner

**When to use:** After any mutating action where the user's historical pain point is "did this actually take effect?" — booking save, approve/deny, settings change, location/resource save, permission mapping update.
**Behavior:** A persistent (not auto-dismissing-in-3-seconds) banner or toast states the exact outcome in plain language, including the resulting status where relevant (e.g., "Pending approval" vs. "Approved") and, for Settings, an explicit effective timestamp. Contrasts with a bare "Success" toast, which the cross-journey analysis identified as insufficient to rebuild trust.
**Examples:** Booking Create/Edit Form save confirmation (US-1.1, US-3.4), Settings Admin save confirmation (US-10.3), Locations Admin save confirmation (US-4.1).

---

### Pattern: Boundary/Scope Confirmation Dialog

**When to use:** Any action where the system is about to apply a change across an ambiguous or consequential scope — recurring booking edit/delete, permission mapping changes, destructive deletes.
**Behavior:** A modal blocks the action until the user makes an explicit, named choice (never a silent default). The dialog states, in plain language, the exact blast radius of each option before the user commits ("This occurrence only" vs. "Entire series"). Confirmation after the fact (a post-save summary restating what was/was not changed) reinforces the decision.
**Examples:** Recurring booking edit/delete scope (US-1.2, US-1.6), Location/Resource delete-in-use confirmation (US-4.3).

---

### Pattern: Single Conflict Badge Component

**When to use:** Anywhere a booking with `conflict_flags[]` is rendered — Calendar, Day, List, Approval Queue, Booking Detail Modal.
**Behavior:** One shared component renders the ⚠ icon, red-tinted background/outline, and hover tooltip (conflicting booking id + conflict type: location or resource). It is never re-implemented per screen, guaranteeing visual and logical consistency across every entry point (US-2.2's "identical regardless of entry point" requirement, lifted directly into the component architecture).
**Examples:** All booking-list/grid screens; Approval Queue rows; Booking Detail Modal.

---

### Pattern: Deny-by-Default Visibility (Permission-Gated UI)

**When to use:** Any nav item, button, or screen gated by a permission flag — especially one marked unconfirmed per F0/F7.
**Behavior:** A user lacking the required permission simply does not see the element at all — it is omitted from the render tree, not shown-and-disabled with an ambiguous tooltip. For admin-facing permission-configuration screens themselves (Role & Permission Matrix), an *unconfirmed* flag is visually distinct (⚠ Pending chip) from a *confirmed-and-denied* flag (✗), so nobody mistakes "we don't know yet" for "definitely no access."
**Examples:** Sidebar "Approvals" item (US-3.1), Admin section (US-4.1/US-7.2), Role & Permission Matrix "Pending" cells (US-7.1).

---

### Pattern: Dynamic Form Field Rendering by Context

**When to use:** The Booking Create/Edit Form, where custom fields must appear only when applicable to the selected location's context.
**Behavior:** Changing the Location field re-queries the Custom Field Service and re-renders the applicable fields section without a full form reload or loss of already-entered standard fields (title, time, resources).
**Examples:** Booking Create/Edit Form custom fields section (US-5.2).

---

### Pattern: Immutable Record Indicator

**When to use:** Audit Log Viewer and any screen that surfaces audit-sourced history (Booking Detail Modal "View history").
**Behavior:** A small lock icon and tooltip ("Audit entries cannot be edited or deleted") reinforces that this is a system-of-record, not just another editable table — directly supporting the dispute-resolution use case (JRN-02.2).
**Examples:** Audit Log Viewer rows (US-11.2).

---

### Pattern: Approved-Only Hard Guarantee on Public Surfaces

**When to use:** Display Board and all Feed Subscription formats.
**Behavior:** The underlying data query is structurally restricted to `status=approved` server-side; the UI additionally reinforces this with a visible "✓ Approved" micro-label so the guarantee is legible to the end user, not just enforced invisibly in the API.
**Examples:** Display Board booking cards (US-9.1, US-9.4), Feed Subscription footnote (US-9.4).

---
