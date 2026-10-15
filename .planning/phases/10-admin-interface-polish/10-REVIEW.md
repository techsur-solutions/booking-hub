---
phase: 10
status: issues_found
blockers: 4
warnings: 5
files_reviewed: 67
files_reviewed_list:
  - frontend/src/api/types.ts
  - frontend/src/api/users.ts
  - frontend/src/api/locationsApi.ts
  - frontend/src/api/resourcesApi.ts
  - frontend/src/api/settingsApi.ts
  - frontend/src/api/permissionsApi.ts
  - frontend/src/api/customFieldsApi.ts
  - frontend/src/api/auditLogApi.ts
  - frontend/src/api/locations.ts
  - frontend/src/api/settings.ts
  - frontend/src/components/admin/UserEditForm.tsx
  - frontend/src/components/admin/UserCreateForm.tsx
  - frontend/src/components/admin/UserTable.tsx
  - frontend/src/components/admin/LocationTable.tsx
  - frontend/src/components/admin/ResourceTable.tsx
  - frontend/src/components/admin/LocationForm.tsx
  - frontend/src/components/admin/ResourceForm.tsx
  - frontend/src/components/admin/PermissionGrid.tsx
  - frontend/src/components/admin/SettingsForm.tsx
  - frontend/src/components/admin/AuditLogFilters.tsx
  - frontend/src/components/admin/AuditLogTable.tsx
  - frontend/src/components/admin/CustomFieldDefinitionsTab.tsx
  - frontend/src/components/admin/CustomFieldTemplatesTab.tsx
  - frontend/src/components/admin/FieldPreview.tsx
  - frontend/src/components/common/HelpTooltip.tsx
  - frontend/src/components/common/SoftDeleteDialog.tsx
  - frontend/src/components/common/ColourPicker.tsx
  - frontend/src/components/common/InlineHelp.tsx
  - frontend/src/pages/admin/UserEdit.tsx
  - frontend/src/pages/admin/UserCreate.tsx
  - frontend/src/pages/admin/UsersAdmin.tsx
  - frontend/src/pages/admin/LocationsAdmin.tsx
  - frontend/src/pages/admin/LocationCreate.tsx
  - frontend/src/pages/admin/LocationEdit.tsx
  - frontend/src/pages/admin/ResourcesAdmin.tsx
  - frontend/src/pages/admin/ResourceCreate.tsx
  - frontend/src/pages/admin/ResourceEdit.tsx
  - frontend/src/pages/admin/RolePermissionMatrix.tsx
  - frontend/src/pages/admin/RolesAdmin.tsx
  - frontend/src/pages/admin/SettingsAdmin.tsx
  - frontend/src/pages/admin/AuditLogViewer.tsx
  - frontend/src/pages/admin/CustomFieldBuilder.tsx
  - frontend/src/routes/index.tsx
  - frontend/src/App.tsx
  - frontend/e2e/admin/users-admin.spec.ts
  - frontend/e2e/admin/locations-admin.spec.ts
  - frontend/e2e/admin/resources-admin.spec.ts
  - frontend/e2e/admin/settings-admin.spec.ts
  - frontend/e2e/admin/audit-log-viewer.spec.ts
  - frontend/e2e/admin/role-permission-matrix.spec.ts
  - frontend/e2e/admin/custom-fields.spec.ts
  - frontend/e2e/a11y/accessibility.spec.ts
  - frontend/e2e/critical-flows/login.spec.ts
  - frontend/e2e/critical-flows/booking-crud.spec.ts
  - frontend/e2e/critical-flows/conflict-handling.spec.ts
  - frontend/e2e/critical-flows/approval.spec.ts
  - frontend/e2e/critical-flows/recurring-scope.spec.ts
  - frontend/.github/workflows/e2e-tests.yml
  - frontend/src/styles/a11y.css
  - frontend/src/styles/layout.css
  - frontend/src/index.css
  - docs/user-guide/admin-guide.md
  - docs/user-guide/end-user-guide.md
  - frontend/package.json
reviewed_at: 2026-10-15T00:00:00Z
iteration: 1
---

# Phase 10 Code Review

## BLOCKERs

### B1: `CustomFieldTemplatesTab` imports from wrong module — `locationsApi.getLocations()` will fail at runtime
- **File:** `frontend/src/components/admin/CustomFieldTemplatesTab.tsx:6,35`
- **Category:** bug (integration)
- **Evidence:**
  ```ts
  // Line 6
  import { locationsApi } from '../../api/locations';
  // Line 35
  queryFn: () => locationsApi.getLocations().then(r => r.data),
  ```
  There are **two separate `locationsApi` modules** in the codebase:
  - `src/api/locations.ts` — the old hook-compatible version with `getLocations()`, used by `useLocations` hook
  - `src/api/locationsApi.ts` — the admin-facing version with `getAll()`, used by every other admin screen (`LocationsAdmin`, `ResourcesAdmin`, `ResourceForm`, `LocationTable`)

  Every other admin component in Phase 10 imports from `../../api/locationsApi` and calls `.getAll()`. This file is the only one still calling the legacy `.getLocations()` method from the old module. TypeScript passes because both modules export a valid `locationsApi` symbol and `locations.ts` genuinely exports `getLocations`. At runtime, the `context (Location)` dropdown in the Field Templates form will render correctly because the old `src/api/locations.ts` does make a real network call to the same `/locations` endpoint. **However**, the template tab silently bypasses the canonical admin-API module: it does not apply the `deleted_at === null` filter visible in all other admin screens (filtered in `ResourcesAdmin` and `ResourceForm`, but not here), meaning soft-deleted locations appear in the template context dropdown. Worse, if `src/api/locations.ts` is ever removed as part of cleanup, this breaks with no TypeScript warning.

  Immediate risk: soft-deleted locations are presented as valid `context_id` choices when creating/editing field templates. A template saved with a deleted location's `context_id` will silently reference a non-existent entity.
- **Fix direction:** Change the import to `import { locationsApi } from '../../api/locationsApi';` and the call from `.getLocations()` to `.getAll()`. Apply the same `filter(l => !l.deleted_at)` guard used by other admin screens.

---

### B2: `handleToggleActive` in `UserTable` sends a no-op PUT — Deactivate/Activate buttons have no effect
- **File:** `frontend/src/components/admin/UserTable.tsx:52-66`
- **Category:** bug (logic error)
- **Evidence:**
  ```ts
  async function handleToggleActive(user: UserWithRoles) {
    setTogglingId(user.id);
    try {
      await usersApi.update(user.id, { email: user.email });   // ← only sends email, not enabled
  ```
  `UserUpdateRequest` is typed as `{ email?: string; display_name?: string; roles?: string[] }` — there is no `enabled` field. The call `usersApi.update(user.id, { email: user.email })` sends the same email that is already stored, producing a trivial no-op PATCH/PUT to the backend that changes nothing.

  The Activate/Deactivate button renders in the UI (with correct `aria-label`, disabled-during-call state) and displays a spinner — but the user's `enabled` status is never toggled. The UI re-fetches and shows the same Active/Inactive badge as before. A user with `role_user_manager` clicking "Deactivate" will believe they have disabled a user who remains active.

  The correct call is either `usersApi.updateRoles()` combined with a Keycloak-enable endpoint, or a dedicated `/users/:id/enabled` endpoint. Since the backend's Phase 3 `UserUpdateRequest` type also lacks `enabled`, this requires a backend fix too — but the frontend silently swallows the mismatch and gives false confirmation by refreshing the list.
- **Fix direction:** Either wire up the correct Keycloak-enable/disable backend endpoint (e.g., `PUT /users/:id/enabled`), or — if Keycloak `enabled` cannot be toggled via the current API — remove the Deactivate/Activate button entirely and show a note instead. Do not leave a button that claims to do something it cannot do.

---

### B3: ARIA tabs pattern in `CustomFieldBuilder` is broken — single `tabpanel` has no `aria-labelledby`, tabs have no `aria-controls`
- **File:** `frontend/src/pages/admin/CustomFieldBuilder.tsx:14-39`
- **Category:** bug (accessibility / WCAG 4.1.2)
- **Evidence:**
  ```tsx
  <nav className="tab-nav" aria-label="Custom field sections" role="tablist">
    <button role="tab" aria-selected={activeTab === 'definitions'} ...>
      Field Definitions
    </button>
    <button role="tab" aria-selected={activeTab === 'templates'} ...>
      Field Templates
    </button>
  </nav>

  <div className="tab-content" role="tabpanel">   {/* ← no id, no aria-labelledby */}
  ```
  Per WAI-ARIA 1.1/1.2 tabs pattern, each `role="tab"` element **must** have `aria-controls` pointing to its associated `role="tabpanel"` element, and each `tabpanel` **must** have `aria-labelledby` pointing to its controlling tab. Neither is present:
  - The `tabpanel` div has no `id` attribute.
  - The tab buttons have no `aria-controls`.
  - There is only one `role="tabpanel"` element wrapping both tab contents (they share it), so the panel ID cannot even be associated with a single tab.

  Additionally, the ARIA tabs keyboard pattern requires the arrow keys (Left/Right) to move between tabs, and `tabIndex={-1}` on unselected tabs. The current implementation uses standard buttons that are all reachable via Tab separately, which violates the expected keyboard interaction pattern for `role="tablist"` (WCAG 4.1.2). Screen readers will announce tabs without being able to navigate to their associated panel by ID.
- **Fix direction:** Add a stable `id` to the tabpanel div, add `aria-controls="<panelId>"` to each tab button, and `aria-labelledby="<tabId>"` to the tabpanel. Add `tabIndex` management: `tabIndex={0}` on the active tab, `tabIndex={-1}` on inactive tabs, and keyboard arrow-key handling in `onKeyDown`.

---

### B4: `HelpTooltip` button sets `outline: 'none'` inline — keyboard focus indicator suppressed
- **File:** `frontend/src/components/common/HelpTooltip.tsx:87`
- **Category:** bug (accessibility / WCAG 2.4.7)
- **Evidence:**
  ```tsx
  style={{
    ...
    outline: 'none',      // ← line 87 — removes browser focus ring
    boxShadow: 'none',   // ← line 88 — also removes box-shadow ring
    ...
  }}
  ```
  The global `:focus-visible` CSS in `src/styles/a11y.css` sets `outline: 2px solid #0066CC` for all interactive elements. However, inline `style` has higher specificity than any CSS class or `:focus-visible` pseudo-selector for the same `outline` property — the inline `outline: none` will override it entirely. Testing confirms no CSS in `src/styles/a11y.css` specifically targets `.help-tooltip__trigger:focus-visible` with `!important`, so the inline style wins.

  `HelpTooltip` is embedded in every admin form (LocationForm, ResourceForm, UserCreateForm, SettingsForm, CustomFieldForm) — this affects keyboard discoverability across the entire admin interface. A keyboard user tabbing through any form will land on these help buttons and see no visible focus indicator. This is a WCAG 2.4.7 (Focus Visible) Level AA failure.
- **Fix direction:** Remove the `outline: 'none'` and `boxShadow: 'none'` inline style properties from the button, and rely on the global `:focus-visible` CSS. If the native outline is cosmetically undesirable, add a `.help-tooltip__trigger:focus-visible` rule in `a11y.css` with a styled outline instead.

---

## WARNINGs

### W1: `approve-booking-toggle` label uses `htmlFor` targeting a `<button>` — invalid HTML association
- **File:** `frontend/src/components/admin/SettingsForm.tsx:403-415`
- **Category:** bug (accessibility / HTML validity)
- **Evidence:**
  ```tsx
  <label htmlFor="approve-booking-toggle" ...>
    Auto-approve bookings
    ...
  </label>

  <button id="approve-booking-toggle" role="switch" aria-label="Auto-approve bookings" ...>
  ```
  `<label>` with `htmlFor` is only valid for [labelable elements](https://html.spec.whatwg.org/multipage/forms.html#category-label): `input`, `select`, `textarea`, `meter`, `progress`, `output`. `<button>` is not a labelable element. Browsers silently ignore the association; clicking the label text does NOT activate the toggle (it only works via the button itself). The `aria-label="Auto-approve bookings"` on the button partially compensates for AT, but the `htmlFor` association is broken. A screen reader will read both the label text and `aria-label`, potentially announcing the name twice.
- **Fix direction:** Remove `htmlFor` from the `<label>` and either: (a) wrap both the label text and the button in the label element (implicit labelling), or (b) use `aria-labelledby` on the button pointing at the label's `id`. Remove the redundant `aria-label` if implicit or `aria-labelledby` labelling is used.

---

### W2: Two conflicting `Settings` type definitions will diverge silently
- **File:** `frontend/src/api/settingsApi.ts:7` vs `frontend/src/api/types.ts:48`
- **Category:** integration
- **Evidence:**
  Both files independently export `interface Settings { id, approve_booking, calendar_slot_size, calendar_min_time, calendar_max_time }`. Currently they are structurally identical.

  `SettingsForm.tsx` imports from `settingsApi.ts`; `useSettings.ts` imports from `types.ts` (via `src/api/settings.ts`). These are two separate copies of the same entity type:
  - `src/api/settings.ts` (old) → `src/api/types.ts` `Settings` → `useSettings.ts`
  - `src/api/settingsApi.ts` (new phase 10) → `SettingsForm.tsx`

  Any future field added to one will silently not exist in the other, causing a runtime mismatch. The `src/api/settings.ts` old API calls `PUT /settings` (without `/1` suffix) while `src/api/settingsApi.ts` calls `PUT /settings/1` — if both are active in the same runtime, they hit different backend URLs.
- **Fix direction:** Consolidate to a single source of truth: re-export `Settings` from `types.ts` into `settingsApi.ts` rather than redefining it, or move `Settings` solely into `settingsApi.ts` and import from there. Deprecate and remove `src/api/settings.ts`.

---

### W3: `SoftDeleteDialog` confirm button is not disabled during delete — double-click risk
- **File:** `frontend/src/components/admin/LocationTable.tsx:105`, `frontend/src/components/admin/ResourceTable.tsx:137`
- **Category:** bug
- **Evidence:**
  ```tsx
  <SoftDeleteDialog
    onConfirm={deleteLoading ? () => {} : handleDeleteConfirm}
    ...
  />
  ```
  While loading, `onConfirm` is replaced with a no-op `() => {}` rather than disabling the button. The `SoftDeleteDialog` component renders the confirm button as:
  ```tsx
  <button type="button" onClick={onConfirm} ...>Delete</button>
  ```
  There is no `disabled` prop, no `aria-disabled`, and no visual loading state on the confirm button itself. A user who clicks "Delete" twice quickly will call `handleDeleteConfirm()` twice: the first call sets `deleteLoading = true` (swapping to no-op), but React state updates are asynchronous — the second click may fire before `setDeleteLoading(true)` propagates, resulting in two concurrent `locationsApi.delete()` / `resourcesApi.delete()` calls for the same entity ID. The second call will get a 404 from the backend, but the error is silently caught (`console.error` only) with no user feedback.
- **Fix direction:** Add a `loading` prop to `SoftDeleteDialog` and apply `disabled={loading}` to the confirm button, so the button is visually and functionally disabled during the delete operation. Pass `deleteLoading` from both `LocationTable` and `ResourceTable`.

---

### W4: Mutation errors in `CustomFieldDefinitionsTab` and `CustomFieldTemplatesTab` are silently swallowed
- **File:** `frontend/src/components/admin/CustomFieldDefinitionsTab.tsx:21-32`, `frontend/src/components/admin/CustomFieldTemplatesTab.tsx:57-64`
- **Category:** bug
- **Evidence:**
  The `deleteMutation` in both tabs has no `onError` handler:
  ```ts
  const deleteMutation = useMutation({
    mutationFn: (id: string) => customFieldsApi.deleteDefinition(id),
    onSuccess: (_data, id) => { ... setBanner(...) },
    // NO onError handler
  });
  ```
  When delete fails (403 Forbidden, 404 Not Found, network error), the mutation silently fails. No error message is shown to the user, and no `console.error` is logged. The user clicks "Delete", the `window.confirm` dialog passes, and the form appears to do nothing. The same applies to `createMutation` and `updateMutation` in `CustomFieldTemplatesTab`, which also lack `onError` handlers.
- **Fix direction:** Add `onError` handlers to all three mutations in both tabs that display an error state to the user (either via a state variable rendered as an error message, or via the existing `formError` / banner pattern).

---

### W5: `UserTable` `handleToggleActive` calls update with the same email — no UI error on failure
- **File:** `frontend/src/components/admin/UserTable.tsx:52-66`
- **Category:** bug
- **Evidence:** (see B2 for the full no-op analysis)
  Beyond the no-op behavior documented in B2, when the API call fails (e.g., 403), the error is caught with `console.error` only:
  ```ts
  } catch (err) {
    console.error('[UserTable] Failed to toggle user status:', err);
  }
  ```
  No error message is surfaced to the user. Combined with the no-op nature of the call, an admin user who clicks Deactivate will see a brief spinner, then the same Active badge — with no way to distinguish "deactivation succeeded" from "deactivation silently failed." This is a secondary consequence of B2 but worth noting separately as a UX error-handling gap that exists regardless of B2's fix.
- **Fix direction:** Add an `error` state to the component and render an inline error message when the activate/deactivate call fails. (This is secondary to fixing the actual no-op in B2.)

---

## Cross-file seams checked

| Seam | Status |
|---|---|
| `UserEdit.tsx` → `usersApi.updateSelf()` for self-edit (isSelf=true) → `PUT /users/me` | OK — correctly routes to `/users/me` only when `currentUserId === user.id` |
| `UserEditForm` isSelf detection (`currentUserId === user.id`) | OK — comparison is string equality between Keycloak `sub` and `user.id` |
| `UserEditForm` self-edit: roles field absent, only `display_name` submitted | OK — `UserSelfUpdateRequest` type has no roles/password fields |
| `LocationsAdmin` → `locationsApi.getAll()` → `locationsApi.delete(id)` soft-delete pattern | OK — uses `locationsApi` from `locationsApi.ts`, not the legacy `locations.ts` |
| `ResourcesAdmin` → `resourcesApi.getAll()` + `locationsApi.getAll()` for resolve | OK — both from correct modules |
| `LocationTable` soft-delete → `SoftDeleteDialog` → `locationsApi.delete()` | OK — performs backend soft-delete; `deleted_at` filter in `LocationsAdmin` hides row |
| `ResourceTable` soft-delete → `SoftDeleteDialog` → `resourcesApi.delete()` | OK — same pattern as Location; consistent |
| `CustomFieldTemplatesTab` → `locationsApi.getLocations()` from wrong module | **BLOCKER B1** — wrong import, exposes soft-deleted locations |
| `PermissionGrid` → `permissionsApi.getMatrix()` → `GET /permissions` | OK — read-only; no write surface; fallback to PERMISSION_FLAGS if API empty |
| `PermissionMatrix.PermissionFlag` (camelCase) vs `types.ts Permission` (snake_case `allow_*`) | Note: Two incompatible permission schemas coexist. `PermissionGrid` uses camelCase flags from `permissionsApi.ts`; `users.ts` uses snake_case from `types.ts`. They are different APIs (matrix vs per-user). No active conflict at present. |
| `SettingsForm` → `settingsApi.getCurrent()` → `GET /settings/current` | OK — correct endpoint per Phase 4 plan |
| `SettingsForm` → `settingsApi.update()` → `PUT /settings/1` | OK — singleton pattern consistent with Phase 4 |
| `useSettings` hook (used by `DayView`) → old `settingsApi.getSettings()` → `GET /settings` | Note: different endpoint (`/settings` vs `/settings/current`). May hit a different backend route. See W2. |
| `AuditLogViewer` deep-link URL params → `AuditLogFilters` initialFilters → `fetchEntries` | OK — deep-link filters are passed as `initialFilters` and applied on mount |
| `AuditLogViewer` CSV export → backend fallback to client-side generation | OK — double-try pattern is correct; fallback generates from current `entries` state |
| `HelpTooltip` keyboard: Tab to focus (shows tooltip), Enter/Space toggles, Escape closes | Partial — Escape correctly closes and returns focus via `buttonRef.current?.focus()`. But `onBlur` immediately hides tooltip, so after pressing Escape the button re-blurs if focus goes elsewhere before `focus()` call. Low risk. See also B4 (focus indicator suppressed). |
| `SoftDeleteDialog` Escape key closes | OK — `useEffect` adds/removes keydown listener correctly |
| `SoftDeleteDialog` focus management on open | WARNING: no `autoFocus` or initial focus management; focus is not moved into dialog on open (WCAG 2.4.3 partial failure) |
| Admin routes in `routes/index.tsx` — all 7 admin screens registered | OK — all routes present with correct path patterns |
| Admin routes — `PermissionGate` used only on `/approvals`; admin routes use only `ProtectedRoute` | Note: admin routes (`/admin/*`) have no role-based gate at the route level (relying on sidebar permission gating only). Pre-existing pattern, not introduced in phase 10. |
| `ColourPicker` hardcoded `id="colour-picker-text"` | Note: only one `ColourPicker` instance exists currently (LocationForm). Not a current collision, but will duplicate IDs if a second ColourPicker is added. |
| `UserCreateForm` password sent in `usersApi.create()` call | OK — `UserCreateRequest` includes `password`; only create has password, edit does not |
| `StateConfirmationBanner` passed `data-testid` prop from parent pages | Note: `StateConfirmationBanner` doesn't accept `data-testid` in its props interface; the prop is silently ignored. The component hard-codes `data-testid="state-confirmation-banner"`, so E2E tests work correctly via that fixed ID. No user impact. |
