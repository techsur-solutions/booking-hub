### Screen: Role & Permission Matrix

**Purpose:** Single admin screen showing "who can do what" — every permission flag's mapping to a Keycloak role/scope, and the ability to edit which flags each role carries.
**User Stories:** US-7.1, US-7.2, US-7.3

#### Layout

```
┌──────────────────────────────────────────────────────────────────┐
│  Roles & Permissions                                                │
├──────────────────────────────────────────────────────────────────┤
│  Permission Flag        Booker  Approver  Admin   Keycloak Role     │
│  ─────────────────────────────────────────────────────────────    │
│  accessCalendar           ✓        ✓        ✓     ROLE_USER         │
│  allowRoomBooking         ✓        ✓        ✓     ROLE_BOOKER       │
│  viewRoomBooking          ✓        ✓        ✓     ROLE_VIEWER       │
│  allowApproveBooking      ✗        ✓        ✓     ROLE_APPROVER     │
│  accessPermissions        ✗        ✗        ✓     ROLE_ADMIN        │
│  allowAPI               ⚠Pending  ⚠Pending ⚠Pending  (unconfirmed)  │
│                                                                      │
│  ⚠ Flags marked "Pending" are treated as restrictive (deny-by-      │
│    default) until confirmed — see F0 Open Questions.                │
└──────────────────────────────────────────────────────────────────┘
```

#### Information Hierarchy

| Priority | Content | Placement |
|----------|---------|-----------|
| Primary | Permission flag name, per-role checkbox grid | Core matrix |
| Primary | "Pending confirmation" warning chip | Inline per unconfirmed flag, impossible to mistake for "unrestricted" |
| Secondary | Mapped Keycloak role/scope | Rightmost column |
| Tertiary | Footnote explaining deny-by-default policy | Below matrix |

#### States

| State | Appearance | User Feedback |
|-------|------------|---------------|
| Default | Matrix populated, checkboxes reflect current mapping | N/A |
| Editing a cell | Checkbox toggled, "Unsaved changes" bar appears | "You have unsaved changes [Save][Discard]" |
| Save success | Bar dismisses; banner | "Permissions updated" + recorded in audit log (US-7.2) |
| Attempt to reference unconfirmed flag in an update | Inline rejection | "Unknown or unconfirmed permission flag" (US-7.2, 400) |
| Forbidden (no `accessPermissions`) | Screen/nav item absent | N/A (US-7.2) |
| Flag unconfirmed | Cells render with a distinct "⚠ Pending" treatment, never shown as blank/unrestricted | Tooltip: "Gating scope not yet confirmed by legacy audit — treated as restricted" (US-7.1) |

#### Interactive Elements

| Element | Type | Behavior |
|---------|------|----------|
| Checkbox cell | Toggle | Stages a role↔permission mapping change |
| Save | Button | Commits staged changes; requires `accessPermissions` server-side, not just UI-hidden (US-7.2, US-7.3) |
| Discard | Button | Reverts staged checkbox changes |
| Keycloak Role column | Read-only text | Shown for transparency; not directly editable here (edited via Keycloak admin console, out of this app's scope) |

---
