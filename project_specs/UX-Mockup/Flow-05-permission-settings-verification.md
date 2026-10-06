### Flow 6: Verifying Permission Migration and Settings Propagation

**Trigger:** Ahead of a release milestone, Priya must confirm a Keycloak permission mapping, toggle and verify `approveBooking` propagation, and review the audit trail before signing off.
**User Story:** US-7.1, US-7.2, US-10.1, US-10.2, US-10.3, US-11.3, US-13.1
**Journey Reference:** JRN-03.2

```
[Sidebar: Admin > Roles & Permissions]
            │
            ▼
[Role & Permission Matrix /admin/roles]
  ┌───────────────────────────────────────────────┐
  │ Flag                  Keycloak Role   Confirmed│
  │ allowApproveBooking   ROLE_APPROVER   ✓ Yes    │
  │ accessPermissions     ROLE_ADMIN      ✓ Yes    │
  │ allowAPI              (unconfirmed)   ⚠ Pending │
  └───────────────────────────────────────────────┘
            │
            ▼
[Reviews mapping row; unconfirmed flags visibly flagged,
 never silently treated as unrestricted — US-7.1]
            │
            ▼
[Sidebar: Admin > Settings]
            │
            ▼
[Settings Admin /admin/settings]
            │
            ▼
[Toggle "Require approval for new bookings" ON]
            │
            ▼
[Click "Save"] ──▶ [Confirmation banner: "Settings updated at
                    14:32:07 — applies to bookings created
                    after this time only"]
            │
            ▼
[Switch to Calendar View, create a throwaway test booking]
            │
            ▼
[New booking immediately shows "Pending approval" —
 propagation confirmed with zero manual multi-screen check]
            │
            ▼
[Sidebar: Admin > Audit Log, filter by actor=Priya, entity=settings]
            │
            ▼
[Audit Log Viewer shows settings.updated entry AND the
 permission mapping review — both captured, US-11.3]
            │
            ▼
[Sign-off: review F13 traceability matrix summary (external
 tooling link) — 100% coverage indicator]
```

**Steps:**
1. Priya opens the Role & Permission Matrix and reviews each legacy permission flag against its Keycloak role mapping (US-7.1, US-7.2); any flag marked unconfirmed is visibly flagged with a warning chip, never silently rendered as if it carries no restriction — this directly implements the FRD's "deny by default" rule in the UI itself.
2. She opens Settings Admin and toggles `approveBooking` on (US-10.1). The Save action requires admin permission server-side (enforced, not just hidden); a non-admin never sees this toggle at all.
3. On save, a confirmation banner states the exact effective timestamp and explicitly scopes the change: "applies to bookings created after this time only" — directly resolving JRN-03.2's "will this actually apply everywhere right away?" anxiety, and satisfying US-10.3's non-retroactivity rule.
4. Priya creates a disposable test booking from the Calendar View and immediately observes it enters `pending` status — confirming propagation without needing to check any other screen (US-10.3, US-1.1).
5. She filters the Audit Log Viewer by her own actor ID and the `settings` entity type, confirming both the settings change and the permission-mapping review produced audit entries (US-11.3).
6. She reviews a linked traceability/regression summary (an external F13 tooling view, referenced but not owned by this frontend) before signing off on the release — a single screen showing 100% coverage replaces the legacy manual, stressful sign-off process (JRN-03.2 Sign Off delight opportunity).

**Exit point:** Confident release sign-off with zero manual multi-screen verification.

---
