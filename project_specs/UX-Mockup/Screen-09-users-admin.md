### Screen: Users Admin

**Purpose:** Admin-facing account creation and role assignment, backed by Keycloak provisioning rather than a locally stored password.
**User Stories:** US-6.1, US-6.5

#### Layout

```
┌──────────────────────────────────────────────────────────────────┐
│  Users                                             [+ New User]    │
├──────────────────────────────────────────────────────────────────┤
│  Name            Email                 Role            Actions     │
│  ─────────────────────────────────────────────────────────────    │
│  Maya Torres     maya@co.com            Booker          [Edit Role]│
│  David Okafor    david@co.com           Approver         [Edit Role]│
│  Priya Patel     priya@co.com           Admin            [Edit Role]│
│  Jordan Lee       (public feed — no account)             —          │
└──────────────────────────────────────────────────────────────────┘

── New User form ──
┌──────────────────────────────────────┐
│  Email / Username * [____________]    │
│  Display Name *      [____________]   │
│  Initial Role *       [Booker ▾]      │
│                                        │
│              [Cancel]   [Create]      │
└──────────────────────────────────────┘

── Edit Role drawer ──
┌──────────────────────────────────────┐
│  Maya Torres — Current role: Booker   │
│  New role:    [Approver ▾]            │
│  ⓘ Takes effect on next issued token  │
│              [Cancel]   [Save]        │
└──────────────────────────────────────┘
```

#### Information Hierarchy

| Priority | Content | Placement |
|----------|---------|-----------|
| Primary | Name, Email, Role | Main table columns |
| Secondary | Edit Role action | Rightmost column |
| Tertiary | Account-creation metadata (created date) | Expandable row detail, not shown by default |

#### States

| State | Appearance | User Feedback |
|-------|------------|---------------|
| Default | List populated | N/A |
| Validation error: duplicate email | Inline error, form stays open | "An account with this email/username already exists" (US-6.1, 409) |
| Create success | Drawer closes; banner | "User created in Keycloak — role assigned: Booker" |
| Role change saved | Drawer closes; banner with explicit timing note | "Role updated — takes effect on next login/token refresh" (US-6.5, sets correct expectation rather than implying instant effect) |
| Forbidden | Screen/nav item absent | N/A |

#### Interactive Elements

| Element | Type | Behavior |
|---------|------|----------|
| + New User | Button | Opens creation form; provisions account in Keycloak realm, not a local password hash (US-6.1) |
| Initial Role dropdown | Select | Required; feeds directly into Permission System (F7) |
| Edit Role | Row action | Opens Edit Role drawer |
| Row click | Click target | Expands to show account metadata |

---
