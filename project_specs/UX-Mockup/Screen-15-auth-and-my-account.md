### Screen: Login, My Account & Password Reset

**Purpose:** Authentication entry point, self-service profile/password editing, and the forgot-password recovery flow — all backed by Keycloak rather than a locally stored credential.
**User Stories:** US-6.2, US-6.3, US-6.4

#### Layout

```
── Login (/login) ──
┌──────────────────────────────────────┐
│          Booking-Hub                  │
│  Email/Username [______________]      │
│  Password        [______________]      │
│  ☐ Remember me                        │
│             [Log In]                  │
│  Forgot password?                     │
└──────────────────────────────────────┘

── My Account (/account) ──
┌──────────────────────────────────────────────────┐
│  My Account                                         │
│  Display Name     [Maya Torres_______]              │
│                                   [Save Profile]     │
│  ── Change Password ──                               │
│  Current Password  [______________]                  │
│  New Password       [______________]                  │
│                                   [Change Password]   │
└──────────────────────────────────────────────────┘

── Password Reset Request (/password-reset/request) ──
┌──────────────────────────────────────┐
│  Reset your password                  │
│  Email/Username [______________]      │
│             [Send reset link]         │
│  "If an account exists, a reset link  │
│   has been sent." (always shown)      │
└──────────────────────────────────────┘

── Password Reset Complete (/password-reset/complete?token=...) ──
┌──────────────────────────────────────┐
│  Choose a new password                │
│  New Password     [______________]     │
│  Confirm Password  [______________]     │
│             [Set New Password]        │
└──────────────────────────────────────┘
```

#### Information Hierarchy

| Priority | Content | Placement |
|----------|---------|-----------|
| Primary | Credential fields, primary action button | Center of each screen, single-column form |
| Secondary | "Remember me" / "Forgot password?" | Below credential fields on Login |
| Tertiary | Generic reset-acknowledgment copy | Below the Send button, always identical regardless of account existence |

#### States

| State | Appearance | User Feedback |
|-------|------------|---------------|
| Login: invalid credentials | Form-level red banner | "Invalid username or password" (US-6.4, 401) |
| Login success | Redirect to Calendar View | — |
| Protected route without valid token | Auto-redirect to Login | "Please log in to continue" (US-6.4) |
| My Account: wrong current password | Inline error on Current Password field | "Current password is incorrect" (US-6.2, 401) |
| My Account: new password fails policy | Inline error on New Password field | "New password does not meet complexity requirements" (US-6.2, 400) |
| My Account: save success | Toast | "Profile updated" / "Password changed" |
| Reset request: any input | Always shows the same generic success message, regardless of whether the account exists | "If an account exists, a reset link has been sent." (US-6.3 — deliberately uninformative to prevent account enumeration) |
| Reset complete: expired/invalid/used token | Form-level error, no password fields accepted | "This password reset link is invalid or has expired" (US-6.3, 400) |
| Reset complete: success | Redirect to Login with a success banner | "Password updated — please log in" |

#### Interactive Elements

| Element | Type | Behavior |
|---------|------|----------|
| Remember me | Checkbox | Extends issued session/token lifetime (US-6.4) |
| Log In | Primary button | Authenticates against Keycloak; on success returns access/refresh token + roles |
| Forgot password? | Link | Navigates to Password Reset Request |
| Save Profile | Button | Updates display name; no admin involvement required (US-6.2) |
| Change Password | Button | Delegates to Keycloak credential-update mechanism |
| Send reset link | Button | Publishes `password.reset.requested` event; always shows generic acknowledgment (US-6.3) |
| Set New Password | Button | Validates token + policy, then updates credential via Keycloak |

---
