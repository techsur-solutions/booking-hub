---
phase: 9
status: issues_found
blockers: 3
warnings: 5
files_reviewed: 100
files_reviewed_list:
  - frontend/src/api/bookings.ts
  - frontend/src/api/client.ts
  - frontend/src/api/customFields.ts
  - frontend/src/api/feeds.ts
  - frontend/src/api/locations.ts
  - frontend/src/api/resources.ts
  - frontend/src/api/settings.ts
  - frontend/src/api/types.ts
  - frontend/src/api/users.ts
  - frontend/src/auth/AuthProvider.tsx
  - frontend/src/auth/keycloak.ts (unmodified but read for context)
  - frontend/src/auth/ProtectedRoute.tsx (unmodified but read for context)
  - frontend/src/App.tsx
  - frontend/src/main.tsx
  - frontend/src/routes/index.tsx
  - frontend/src/components/AppShell.tsx
  - frontend/src/components/Badge.tsx
  - frontend/src/components/Button.tsx
  - frontend/src/components/Card.tsx
  - frontend/src/components/ConflictBadge.tsx
  - frontend/src/components/ErrorBoundary.tsx
  - frontend/src/components/Form.tsx
  - frontend/src/components/Header.tsx
  - frontend/src/components/Input.tsx
  - frontend/src/components/LoadingSpinner.tsx
  - frontend/src/components/Modal.tsx
  - frontend/src/components/PermissionGate.tsx
  - frontend/src/components/Select.tsx
  - frontend/src/components/Sidebar.tsx
  - frontend/src/components/StateConfirmationBanner.tsx
  - frontend/src/components/Textarea.tsx
  - frontend/src/components/Toast.tsx
  - frontend/src/components/UserMenu.tsx
  - frontend/src/components/approval/ApprovalTable.tsx
  - frontend/src/components/approval/ApproveButton.tsx
  - frontend/src/components/approval/DenyButton.tsx
  - frontend/src/components/booking/BookingDetailModal.tsx
  - frontend/src/components/booking/BookingForm.tsx
  - frontend/src/components/booking/ConflictBanner.tsx
  - frontend/src/components/booking/ConflictWarning.tsx
  - frontend/src/components/booking/CustomFieldRenderer.tsx
  - frontend/src/components/booking/RecurringPattern.tsx
  - frontend/src/components/booking/RecurringScopeDialog.tsx
  - frontend/src/components/booking/StatusBadge.tsx
  - frontend/src/components/calendar/BookingEvent.tsx
  - frontend/src/components/calendar/DayViewEvent.tsx
  - frontend/src/components/calendar/FullCalendarWrapper.tsx
  - frontend/src/components/calendar/HourlySlotGrid.tsx
  - frontend/src/components/display/ApprovedOnlyBadge.tsx
  - frontend/src/components/list/BookingTable.tsx
  - frontend/src/components/list/TableFilters.tsx
  - frontend/src/hooks/useBookings.ts
  - frontend/src/hooks/useCustomFields.ts
  - frontend/src/hooks/useLocations.ts
  - frontend/src/hooks/useResources.ts
  - frontend/src/hooks/useSettings.ts
  - frontend/src/hooks/useViewState.ts
  - frontend/src/pages/ApprovalQueue.tsx
  - frontend/src/pages/ApprovalsQueue.tsx
  - frontend/src/pages/BookingCreate.tsx
  - frontend/src/pages/BookingEdit.tsx
  - frontend/src/pages/BookingsView.tsx
  - frontend/src/pages/CalendarView.tsx
  - frontend/src/pages/DayView.tsx
  - frontend/src/pages/DisplayBoard.tsx
  - frontend/src/pages/FeedSubscription.tsx
  - frontend/src/pages/ListView.tsx
  - frontend/src/pages/Login.tsx
  - frontend/src/pages/MyAccount.tsx
  - frontend/src/pages/PasswordReset.tsx
  - frontend/src/pages/PasswordResetComplete.tsx
  - frontend/src/utils/exportBookings.ts
  - frontend/src/utils/permissions.ts
  - frontend/src/utils/rememberMe.ts
  - frontend/src/index.css
  - frontend/src/styles/components.css
  - frontend/src/styles/layout.css
  - frontend/src/styles/tokens.css
  - frontend/src/test-setup.ts
  - frontend/tsconfig.app.json
  - frontend/vite.config.ts
  - frontend/package.json
  - frontend/e2e/approval-queue.spec.ts
  - frontend/e2e/auth-flows.spec.ts
  - frontend/e2e/auth-redirect.spec.ts
  - frontend/e2e/booking-create.spec.ts
  - frontend/e2e/booking-detail.spec.ts
  - frontend/e2e/booking-edit.spec.ts
  - frontend/e2e/calendar-view.spec.ts
  - frontend/e2e/conflict-detection-ui.spec.ts
  - frontend/e2e/day-view.spec.ts
  - frontend/e2e/display-board.spec.ts
  - frontend/e2e/feed-subscription.spec.ts
  - frontend/e2e/list-view.spec.ts
  - frontend/e2e/navigation.spec.ts
  - frontend/e2e/view-state-persistence.spec.ts
  - frontend/src/api/client.test.ts
  - frontend/src/components/ConflictBadge.test.tsx
  - frontend/src/components/PermissionGate.test.tsx
  - frontend/src/components/StateConfirmationBanner.test.tsx
  - frontend/src/utils/exportBookings.test.ts
  - frontend/src/pages/ListView.test.tsx
reviewed_at: 2026-10-13T00:00:00Z
iteration: 1
---

# Phase 9 Code Review

## BLOCKERs

### B1: Open redirect via unvalidated `?redirect=` URL parameter in Login page
- **File:** `frontend/src/pages/Login.tsx:19,35`
- **Category:** security
- **Evidence:**
  ```ts
  const redirectTo = params.get('redirect') ?? '/bookings?view=month';
  // …
  window.location.replace(redirectTo);
  ```
  `redirectTo` is taken verbatim from the `?redirect=` query parameter. An attacker can craft the link `/login?redirect=https://evil.com`; any already-authenticated user who visits it is immediately redirected to the attacker-controlled site via `window.location.replace`. This is a classic open-redirect. The `keycloak.login()` call on line 49 is **not** affected because it prepends `window.location.origin`, producing an invalid URL that Keycloak would reject, but the `window.location.replace(redirectTo)` on line 35 (the auto-redirect for authenticated users) is fully exploitable.
- **Fix direction:** Validate that `redirectTo` is a same-origin path before using it: reject any value that starts with `//`, `http://`, or `https://`, or that contains `:`. Fall back to the default path if the value is invalid.

---

### B2: `rememberMe_pending` sessionStorage flag set but never consumed — "Remember Me" silently broken for first-time users
- **File:** `frontend/src/pages/Login.tsx:43-47`
- **Category:** bug
- **Evidence:**
  ```ts
  } else if (rememberMe) {
    // No stored email yet — we'll save it after Keycloak returns the token
    // For now, mark the intent so the callback knows to save
    sessionStorage.setItem('rememberMe_pending', 'true');
  }
  ```
  A search across the entire frontend source (`grep -rn "rememberMe_pending"`) finds exactly one write (this line) and zero reads. After Keycloak redirects back and `AuthProvider` initialises successfully, nothing reads `'rememberMe_pending'` from sessionStorage, so the email is never written to the cookie. For any user who checks "Remember Me" before their first Keycloak login, the feature does nothing. On subsequent visits the `storedEmail` remains `null` and the reminder UI is never shown.
- **Fix direction:** After successful Keycloak initialisation in `AuthProvider` (or in a post-auth hook), check `sessionStorage.getItem('rememberMe_pending')`; if truthy, call `saveRememberMe(keycloak.tokenParsed?.email)` and then remove the flag.

---

### B3: Approvals route (`/approvals`) has no route-level permission enforcement — any authenticated user can access it
- **File:** `frontend/src/routes/index.tsx:96-104`, `frontend/src/pages/ApprovalQueue.tsx:25-32`
- **Category:** security
- **Evidence:**
  ```tsx
  // routes/index.tsx — only authentication is checked, NOT the approver role:
  {
    path: '/approvals',
    element: (
      <ProtectedRoute>       // ← checks only: keycloak.authenticated
        <AppShell>
          <ApprovalQueue />  // ← role check is inside, runs after render
        </AppShell>
      </ProtectedRoute>
    ),
  },
  ```
  ```ts
  // ApprovalQueue.tsx — gate is a useEffect, so it fires AFTER the component mounts:
  const allowed = hasApprovePermission();
  useEffect(() => {
    if (!allowed) {
      showToast('Access denied', { variant: 'danger' });
      navigate('/bookings?view=month', { replace: true });
    }
  }, [allowed, navigate, showToast]);
  ```
  Between the initial render and the `useEffect` fire, `ApprovalQueue` fetches all pending bookings via `GET /bookings?status=pending`. A non-approver authenticated user navigating to `/approvals` directly will trigger that API call and momentarily see the table render before being redirected. If the backend does not enforce the approver role on that endpoint, pending booking data is exposed. Even if the backend is hardened, the `ApprovalQueue` itself renders `null` only after checking `allowed`, but the data fetch is unconditionally initiated on mount when `allowed` is truthy at first render (and `hasApprovePermission()` reads `keycloak.realmAccess?.roles` which might not have loaded yet in some race conditions). The root issue is UI-only access control with no route-wrapper permission gate.
- **Fix direction:** Create a `PermissionProtectedRoute` (or extend `ProtectedRoute`) that accepts a role predicate and calls `keycloak.login()` / redirects before rendering children, eliminating the render-then-redirect race. Alternatively, wrap `<ApprovalQueue />` in `<PermissionGate requireRole="role_booking_approver" fallback={<Navigate to="/bookings" replace />}>` at the route level.

---

## WARNINGs

### W1: DayView and ListView use lightweight inline `BookingDetailModal` instead of the full `UI-BOOK-03` component
- **File:** `frontend/src/pages/DayView.tsx:29-82`, `frontend/src/pages/ListView.tsx:16-72`
- **Evidence:** Both `DayView.tsx` and `ListView.tsx` define a *local* `function BookingDetailModal({ booking, onClose })` that accepts a `Booking` object and shows only Date, Time, Status, Description, and conflict count. The full component in `frontend/src/components/booking/BookingDetailModal.tsx` (used by `CalendarView`) provides Edit, Delete, Clone, conflict drill-down, audit trail link, and status badges. Users clicking a booking row in the List View or an event in Day View get a stripped-down view with no action buttons. The `CreateFormModal` in `DayView` is explicitly marked as a placeholder, but the `BookingDetailModal` is not. The CalendarView integration shows the full modal is available and working.
- **Fix direction:** Replace the local inline `BookingDetailModal` in both pages with the imported full component from `../../components/booking/BookingDetailModal` (passing `bookingId={booking.id}` instead of the booking object); remove the local interface definitions.

### W2: Orphaned stub file `ApprovalsQueue.tsx` shadows the real `ApprovalQueue.tsx`
- **File:** `frontend/src/pages/ApprovalsQueue.tsx`
- **Evidence:**
  ```tsx
  // ApprovalsQueue.tsx (whole file):
  export function ApprovalsQueue() {
    return <div><h1>Approval Queue</h1><p>Pending booking approvals — built in Phase 9</p></div>;
  }
  ```
  `routes/index.tsx` correctly imports from `ApprovalQueue` (singular), so the stub is not actively rendered. However, the stub's exported name `ApprovalsQueue` (with trailing 's') is close enough to cause a misimport. Any developer who accidentally imports `ApprovalsQueue` instead of `ApprovalQueue` will silently get the placeholder. The file should be removed.
- **Fix direction:** Delete `frontend/src/pages/ApprovalsQueue.tsx`. It is not imported anywhere and creates confusion.

### W3: `role_booking_manager` used in `BookingTable` does not exist in any backend role definition
- **File:** `frontend/src/components/list/BookingTable.tsx:269`
- **Evidence:**
  ```tsx
  <PermissionGate requireAnyRole={['role_booking_manager', 'role_booking_approver']}>
  ```
  `role_booking_manager` appears nowhere else in the codebase—not in `permissions.ts`, not in `types.ts`, not in any backend source file found. The `Permission` type in `api/types.ts` defines `allow_approve_booking`, `allow_manage_locations`, etc., with no `role_booking_manager` counterpart. As currently written, only users with `role_booking_approver` will see the Edit/Delete action buttons in the table (the phantom role never matches). This is likely a typo or leftover from an earlier naming draft.
- **Fix direction:** Remove `'role_booking_manager'` from the `requireAnyRole` array, or replace it with the correct role name if one is defined in the backend Keycloak realm configuration.

### W4: `StateConfirmationBanner` variant type mismatch — callers pass `'warning'` but type only allows `'success' | 'warning' | 'error'`
- **File:** `frontend/src/pages/BookingCreate.tsx:59,95,129`
- **Evidence:**
  ```ts
  // In BookingCreate.tsx state definition:
  variant: 'success' | 'warning';  // local state type

  // Passed to StateConfirmationBanner:
  variant={confirmationBanner.variant}   // can be 'warning'

  // StateConfirmationBanner prop type:
  variant?: 'success' | 'warning' | 'error';  // 'warning' IS accepted
  ```
  `'warning'` is technically valid for `StateConfirmationBanner`. However, the banner renders `variant === 'warning'` with the `⚠` icon and `confirmation-banner--warning` CSS class. There is no `confirmation-banner--warning` CSS class defined anywhere in the component stylesheets (only `--success` and `--error` variants are typically defined). If the CSS rule is absent the banner will render without styling, appearing visually identical to the unstyled default state—misleading for a "submitted for approval" condition. This warrants a CSS check.
- **Fix direction:** Verify that `confirmation-banner--warning` has a CSS rule defined in `styles/components.css`. If not, add it (amber/yellow background), or map the pending-submission state to a different visual treatment.

### W5: `Modal` uses a hard-coded `id="modal-title"` — multiple open modals sharing the same ID
- **File:** `frontend/src/components/Modal.tsx:56`
- **Evidence:**
  ```ts
  const titleId = title ? 'modal-title' : undefined;
  ```
  When `BookingDetailModal` renders while `showDeleteConfirm=true`, two `<Modal>` instances are simultaneously in the DOM: the main detail modal (title=undefined → no id) and the delete-confirm modal (title="Confirm Delete" → id="modal-title"). While this specific combination avoids collision because the main modal's title is `undefined` for loaded bookings, the `ApproveButton` renders TWO `<Modal>` elements simultaneously (simple-confirm AND conflict-confirm, both with non-null titles) whenever both `showSimpleConfirm` and `showConflictConfirm` could ever be true—though in practice only one fires at a time. More broadly, if any two titled modals are ever simultaneously in the DOM the duplicate `id` attribute violates the HTML spec and breaks `aria-labelledby` pointing.
- **Fix direction:** Replace the static string with a `useId()` hook or a unique identifier per `Modal` instance (e.g. `id={title ? \`modal-title-${React.useId()}\` : undefined}`) so each modal gets a unique label ID.

---

## Cross-file seams checked

| Seam | Result |
|------|--------|
| `routes/index.tsx` imports `ApprovalQueue` from `pages/ApprovalQueue` (not stub `ApprovalsQueue`) | OK — correct import |
| `routes/index.tsx` imports `LocationsAdmin`, `ResourcesAdmin`, `CustomFieldsAdmin`, `UsersAdmin`, `RolesAdmin`, `SettingsAdmin`, `AuditLogViewer` from `pages/admin/` | OK — all files exist as stubs |
| `AuthProvider` → `keycloak.ts` singleton — same instance used by `client.ts` interceptor | OK |
| `feedsApi.getDisplayBoard()` uses `apiClient` (JWT interceptor) for a public endpoint | OK — `client.ts` only injects token if `keycloak.token` is non-null; unauthenticated calls go through without Authorization header |
| `/feeds` route wraps `AppShell` (→ `Sidebar` → `useAuth()`) without `ProtectedRoute` — would crash if `AuthProvider` not in tree | OK — `AuthProvider` is an ancestor of `RouteRenderer` in `App.tsx` |
| `BookingForm.onSubmit` signature `(values: BookingFormValues) => Promise<void>` matches callers `BookingCreate.handleSubmit` and `BookingEdit.handleSubmit` | OK |
| `patchBooking(id, data, scope?: RecurringScope)` in `bookings.ts` matches caller in `BookingEdit` | OK |
| `BookingDetailModal` from `components/booking/` takes `bookingId: string | null`; `CalendarView`, `ApprovalTable`, `ConflictBanner` all pass `string | null` | OK |
| `DayView` local `BookingDetailModal` takes `booking: Booking | null` — this is a *different* component, not the canonical one | WARNING W1 |
| `ListView` local `BookingDetailModal` takes `booking: Booking | null` — same issue | WARNING W1 |
| `StateConfirmationBanner` variant `'success' | 'warning' | 'error'` vs callers passing `'success'` and `'warning'` | OK (types match; CSS coverage is W4) |
| `Login.tsx` → `window.location.replace(redirectTo)` — open redirect | BLOCKER B1 |
| `rememberMe_pending` set in `Login.tsx` but never read in `AuthProvider` or anywhere else | BLOCKER B2 |
| `PermissionGate` / `hasRole` reads from `keycloak.realmAccess.roles` (same singleton as `Sidebar`) | OK |
| `BookingTable` `requireAnyRole=['role_booking_manager', 'role_booking_approver']` — `role_booking_manager` undefined in codebase | WARNING W3 |
| `useBookingById` query key `['bookings', id]` — can collide with list query key `['bookings', {}]` when id happens to be an empty string (guarded by `enabled: !!id`) | OK — `enabled: false` prevents execution |
| `ApprovalsQueue.tsx` stub exists alongside canonical `ApprovalQueue.tsx` | WARNING W2 |
| `Modal` hard-coded `id="modal-title"` — concurrent titled modals | WARNING W5 |
