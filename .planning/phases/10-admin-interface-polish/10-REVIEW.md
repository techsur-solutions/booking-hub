---
phase: 10
status: issues_found
blockers: 1
warnings: 0
files_reviewed: 11
files_reviewed_list:
  - frontend/src/api/settingsApi.ts
  - frontend/src/api/users.ts
  - frontend/src/components/admin/CustomFieldDefinitionsTab.tsx
  - frontend/src/components/admin/CustomFieldTemplatesTab.tsx
  - frontend/src/components/admin/LocationTable.tsx
  - frontend/src/components/admin/ResourceTable.tsx
  - frontend/src/components/admin/SettingsForm.tsx
  - frontend/src/components/admin/UserTable.tsx
  - frontend/src/components/common/HelpTooltip.tsx
  - frontend/src/components/common/SoftDeleteDialog.tsx
  - frontend/src/pages/admin/CustomFieldBuilder.tsx
  # Backend files read for integration seam verification:
  # services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/controller/UserController.java
  # services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/service/UserService.java
  # services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/keycloak/KeycloakAdminService.java
  # services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/dto/UserResponse.java
reviewed_at: 2026-10-15T13:41:38Z
iteration: 2
---

# Phase 10 Code Review — Iteration 2 (Re-review after fixes)

## Previous Findings Resolution Summary

| Finding | Fix commit | Verdict |
|---|---|---|
| B1: `CustomFieldTemplatesTab` wrong locationsApi import | 32cf5e4 | ✅ Fixed |
| B2: `handleToggleActive` no-op / missing backend endpoint | a223cc6 | ❌ Still broken — see new B1 below |
| B3: ARIA tabs missing aria-controls / aria-labelledby / keyboard nav | ab444f9 | ✅ Fixed |
| B4: `HelpTooltip` inline `outline:none` suppresses focus indicator | 47f2a99 | ✅ Fixed |
| W1: `htmlFor` targeting a `<button>` | fdbd109 | ✅ Fixed |
| W2: Duplicate `Settings` interface in settingsApi.ts | a2288b7 | ✅ Fixed |
| W3: SoftDeleteDialog confirm button not disabled during delete | 2b22bd4 | ✅ Fixed |
| W4: Mutation errors swallowed in custom field tabs | 44cafb1 | ✅ Fixed |
| W5: No UI error on toggle failure | a223cc6 | ✅ Fixed (surface code is correct) |

---

## BLOCKERs

### B1: `usersApi.setEnabled()` calls a backend endpoint that does not exist — Deactivate/Activate will always fail with 404

- **File:** `frontend/src/api/users.ts:55-57` (frontend), `services/users-permissions-service/src/main/java/com/bookinghub/userspermissions/controller/UserController.java` (backend — missing endpoint)
- **Category:** integration (bug)
- **Evidence:**

  The B2 fix added this to `users.ts`:
  ```ts
  /** PATCH /users/:id/enabled — admin-only enable/disable (Keycloak enabled flag) */
  setEnabled: (id: string, enabled: boolean) =>
    apiClient.patch<UserWithRoles>(`/users/${id}/enabled`, { enabled }),
  ```
  And `handleToggleActive` now calls:
  ```ts
  await usersApi.setEnabled(user.id, !user.enabled);
  ```

  **The backend `UserController` has no such endpoint.** Reading the full `UserController.java`, its complete mapping set is:
  - `GET /users` — `listUsers()`
  - `POST /users` — `createUser()`
  - `GET /users/{id}` — `getUser()`
  - `PUT /users/{id}` — `updateUser()`
  - `PUT /users/{id}/roles` — `assignRoles()`
  - `GET /users/me` — `getOwnProfile()`
  - `PUT /users/me` — `updateOwnProfile()`

  There is **no `@PatchMapping("/{id}/enabled")`**. Confirmed by `grep -rn "PatchMapping|@Patch|PATCH" services/users-permissions-service/src/main/java/` — zero results.

  `UserService.java` also has no `setEnabled`/`enableUser`/`disableUser` method. `KeycloakAdminService.java` has no enable/disable operation (only `createUser`, `resetPassword`, `updateUserRealmRoles`, `logoutUser`, `findUserIdByEmail`).

  Furthermore, the backend `UserResponse` DTO is:
  ```java
  public record UserResponse(
      String id, String email, String displayName, List<String> roles,
      Instant createdAt, Instant updatedAt
  ) {}
  ```
  There is **no `enabled` field**. The frontend `UserWithRoles.enabled: boolean` receives `undefined` from `GET /users` at runtime. `handleToggleActive` therefore calls `usersApi.setEnabled(user.id, !undefined)` → `setEnabled(user.id, true)` — always attempting to **enable** regardless of current status.

  **Effect:** Every click of Deactivate/Activate will send `PATCH /users/:id/enabled` to a non-existent route, receive a 404, and render the `toggleError` banner: `"Failed to deactivate <name>. Please try again."` This is worse than the original no-op — the button now always shows a visible error. The Deactivate/Activate feature remains completely non-functional.

- **Fix direction:** The fix requires a backend change — **not** just a frontend change. Add `@PatchMapping("/{id}/enabled")` to `UserController.java`, a `setEnabled(UUID id, boolean enabled)` method to `UserService.java`, and a `setUserEnabled(String keycloakUserId, boolean enabled)` method to `KeycloakAdminService.java` (using `UserRepresentation.setEnabled()` + `keycloak.realm(realm).users().get(id).update(rep)`). Also add `enabled` to `UserResponse`. Until then, the button should either remain hidden or display a "not yet supported" notice rather than a permanent error.

---

## WARNINGs

*No new warnings introduced by the fixes. All W1–W5 from iteration 1 are resolved.*

---

## Fix Correctness Detail

### B1 Fix (32cf5e4) — ✅ Correct
`CustomFieldTemplatesTab` now imports `locationsApi` from `../../api/locationsApi` (line 6) and calls `.getAll()` (line 36). The `deleted_at` filter was already present in the JSX dropdown (line 196: `locations?.filter(l => !l.deleted_at)`). Import path verified against `locationsApi.ts` which exports `locationsApi.getAll()`.

### B3 Fix (ab444f9) — ✅ Correct
`CustomFieldBuilder` now has:
- `id={`tab-${tab.id}`}` on each button → generates `tab-definitions` and `tab-templates`
- `aria-controls={PANEL_ID}` on each button (both tabs legitimately control the shared panel — this is valid per WAI-ARIA 1.2 §3.5)
- `tabIndex={activeTab === tab.id ? 0 : -1}` for roving tabindex
- `const PANEL_ID = 'custom-field-tabpanel'` with `id={PANEL_ID}` on the div
- `aria-labelledby={`tab-${TABS[activeTabIndex].id}`}` — `activeTabIndex` is recomputed each render from `activeTab` state, so always tracks the active tab
- `tabIndex={0}` on the panel div (correct per WAI-ARIA pattern)
- ArrowLeft, ArrowRight, Home, End keys all handled; `e.preventDefault()` called before focus move

### B4 Fix (47f2a99) — ✅ Correct
Both `outline: 'none'` and `boxShadow: 'none'` removed from the inline `style` object. The remaining button style has `background: 'transparent'`, `border: '1.5px solid currentColor'`, etc. — none of which suppress focus. `a11y.css` line 87–91 has `button:focus-visible { outline: 2px solid #0066CC; outline-offset: 2px; }` which will now apply cleanly.

### W1 Fix (fdbd109) — ✅ Correct
`SettingsForm.tsx` now has `<span id="approve-booking-label">` (not `<label htmlFor>`), and the switch button has `aria-labelledby="approve-booking-label"` with no redundant `aria-label`. Verified in diff and in-file at lines 403–418.

### W2 Fix (a2288b7) — ✅ Correct
`settingsApi.ts` now has `import type { Settings } from './types'` and `export type { Settings }` — no local `interface Settings`. TypeScript confirms zero errors (`tsc --noEmit` exits 0).

### W3 Fix (2b22bd4) — ✅ Correct
`SoftDeleteDialog` accepts `loading?: boolean` (default `false`). Confirm button has `disabled={loading}` and `aria-disabled={loading}`. Text is `{loading ? 'Deleting…' : 'Delete'}`. Both `LocationTable` and `ResourceTable` pass `loading={deleteLoading}` and `onConfirm={handleDeleteConfirm}` directly (no more `() => {}`).

### W4 Fix (44cafb1) — ✅ Correct
`CustomFieldDefinitionsTab`: `onError` on `deleteMutation` sets `deleteError` state, rendered as `<div role="alert">`. `deleteError` is cleared on success (`setDeleteError(null)` in `onSuccess`).  
`CustomFieldTemplatesTab`: `onError` on `createMutation` and `updateMutation` both set `formError`. `onError` on `deleteMutation` sets `deleteError`. Both are displayed, cleared appropriately.

### W5 Fix (a223cc6) — ✅ Code is correct, but only partially effective
`toggleError` state renders inline `role="alert"` div. `setToggleError(null)` called at start of each toggle attempt. Error message is descriptive and references user name. The surface code is correctly implemented. However, since B1 (this iteration) means `setEnabled()` always 404s, users will always see the error. The W5 surface is correct; the failure mode is the B2/B1 backend gap.

---

## Cross-file seams checked (iteration 2)

| Seam | Status |
|---|---|
| `CustomFieldTemplatesTab` → `locationsApi.getAll()` from `../../api/locationsApi` | ✅ Fixed (was B1 iter-1) |
| `usersApi.setEnabled()` → `PATCH /users/:id/enabled` ↔ `UserController.java` | **BLOCKER B1** — endpoint absent in backend |
| `UserWithRoles.enabled` ↔ `UserResponse` backend DTO | **BLOCKER B1** — `enabled` field absent from backend DTO; runtime value is `undefined` |
| `CustomFieldBuilder` ARIA tabs: `aria-controls`, `aria-labelledby`, `tabIndex`, keyboard | ✅ Fixed (was B3 iter-1) |
| `HelpTooltip` button: `outline`/`boxShadow` inline removed, `:focus-visible` unblocked | ✅ Fixed (was B4 iter-1) |
| `SettingsForm` `approve-booking-label` → `aria-labelledby` on switch button | ✅ Fixed (was W1 iter-1) |
| `settingsApi.ts` `Settings` type → single source from `types.ts` | ✅ Fixed (was W2 iter-1) |
| `SoftDeleteDialog` `loading` prop → `disabled` on confirm → `LocationTable`, `ResourceTable` | ✅ Fixed (was W3 iter-1) |
| `CustomFieldDefinitionsTab` / `CustomFieldTemplatesTab` mutation `onError` handlers | ✅ Fixed (was W4 iter-1) |
| `UserTable` `toggleError` → `role="alert"` div | ✅ Fixed (was W5 iter-1) |
| TypeScript compilation (`tsc --noEmit`) | ✅ Clean (exit 0) — TypeScript cannot detect the missing backend endpoint |
