---
phase: 9
status: clean
blockers: 0
warnings: 0
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
  - frontend/src/routes/index.tsx
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
reviewed_at: 2026-10-13T09:00:00Z
iteration: 2
---

# Phase 9 Code Review — Iteration 2

## Re-review: Verification of Iteration 1 Fixes

All 3 BLOCKERs and all 5 WARNINGs from iteration 1 have been fixed and verified. No regressions introduced by the fixes.

### B1 — Open redirect via unvalidated `?redirect=` (FIXED ✓)
- `Login.tsx:20-27`: `rawRedirect` is now validated by `/^\/[^/]/` regex (blocks `//`, `https://`, etc.) plus a `!raw.includes(':')` guard (blocks scheme separators).
- Confirmed via `URLSearchParams` decoding test: `%2F%2Fevil.com` decodes to `//evil.com` which correctly fails the regex. No bypass path found.

### B2 — `rememberMe_pending` set but never consumed (FIXED ✓)
- `AuthProvider.tsx:107-112`: After successful Keycloak init, `sessionStorage.getItem('rememberMe_pending') === 'true'` is checked; if set, the flag is removed and `saveRememberMe(keycloak.tokenParsed?.email)` is called. The `if (email)` guard prevents writing an undefined email to the cookie.

### B3 — `/approvals` route has no route-level permission enforcement (FIXED ✓)
- `routes/index.tsx:98-111`: The `/approvals` route now wraps `<AppShell><ApprovalQueue /></AppShell>` in `<PermissionGate requireRole="role_booking_approver" fallback={<Navigate to="/bookings?view=month" replace />}>`, which sits inside `<ProtectedRoute>`. `ProtectedRoute` returns `null` until `authenticated=true` (i.e., after Keycloak's `loading=false`), so `PermissionGate` only evaluates after `keycloak.realmAccess` is populated — no loading-state race.
- The redundant `useEffect` redirect inside `ApprovalQueue` remains but is now harmless (the component will never mount for non-approvers).

### W1 — Inline `BookingDetailModal` in DayView/ListView (FIXED ✓)
- `DayView.tsx`: Local inline `BookingDetailModal` removed; now imports and uses `BookingDetailModal` from `../../components/booking/BookingDetailModal`. State changed from `Booking | null` to `string | null` (bookingId); `handleEventClick(bookingId: string)` correctly feeds the canonical component.
- `ListView.tsx`: Same fix applied — uses imported `BookingDetailModal` with `bookingId={detailBookingId}`.

### W2 — Orphaned stub `ApprovalsQueue.tsx` (FIXED ✓)
- `frontend/src/pages/ApprovalsQueue.tsx` deleted (confirmed: `ls` returns "No such file or directory").

### W3 — Phantom `role_booking_manager` in `BookingTable` (FIXED ✓)
- `BookingTable.tsx:269`: `requireAnyRole` now contains only `['role_booking_approver']`.

### W4 — Missing `confirmation-banner--warning` CSS class (FIXED ✓)
- `styles/components.css:826-896`: Complete `.confirmation-banner` ruleset added, including `.confirmation-banner--success`, `.confirmation-banner--warning`, `.confirmation-banner--error`, and all element sub-classes (`__icon`, `__content`, `__timestamp`, `__dismiss`).

### W5 — Duplicate `id="modal-title"` with concurrent modals (FIXED ✓)
- `Modal.tsx:56`: `useId()` (React 18) is now called unconditionally at the top of the component (before the `if (!isOpen) return null` guard) and produces `titleId = \`modal-title-${instanceId}\`` — unique per Modal instance.

---

## BLOCKERs

_None._

## WARNINGs

_None._

---

## Cross-file seams checked (iteration 2)

| Seam | Result |
|------|--------|
| `routes/index.tsx` `/approvals` → `ProtectedRoute` → `PermissionGate(role_booking_approver)` → `AppShell` → `ApprovalQueue` | OK — B3 fix applied correctly; no loading race |
| `DayView` now uses canonical `BookingDetailModal` from `components/booking/` with `bookingId: string \| null` | OK — W1 fixed |
| `ListView` now uses canonical `BookingDetailModal` from `components/booking/` with `bookingId: string \| null` | OK — W1 fixed |
| `frontend/src/pages/ApprovalsQueue.tsx` (stub) | OK — deleted (W2 fixed) |
| `BookingTable.tsx:269` `requireAnyRole` | OK — only `['role_booking_approver']` remains (W3 fixed) |
| `styles/components.css` `.confirmation-banner--warning` | OK — class present at line 844 (W4 fixed) |
| `Modal.tsx` `useId()` called before `if (!isOpen) return null` | OK — unique IDs per instance (W5 fixed) |
| `AuthProvider.tsx` consumes `rememberMe_pending` after Keycloak init | OK — reads, removes, saves email via `saveRememberMe` (B2 fixed) |
| `Login.tsx` redirect validation | OK — regex + colon guard, percent-encoding bypass confirmed blocked (B1 fixed) |
| `UserMenu` → `useAuth()` called from `/feeds` route (public, no `ProtectedRoute`) | OK — `AuthProvider` is always an ancestor in `App.tsx`; `keycloak.tokenParsed` optional-chained gracefully shows "User" for unauthenticated state |
| `PermissionGate` `requireRole` vs `requireAnyRole` semantics in fix for B3 | OK — `requireRole` single string dispatches to `hasRole()`, `requireAnyRole` array dispatches to `hasAnyRole()`; correct variant used |
| `BookingDetailModal` state-resets on re-open (conflict/delete/scope states) | OK — all state is per-mount; `bookingId` prop change triggers re-render, stale state from prior open is isolated |
| `ConflictWarning` "Save Anyway" unblock flow | OK — `onBlockChange(false)` correctly unblocks parent form; subsequent field changes re-run the debounced check effect and can re-block if conflicts persist |
