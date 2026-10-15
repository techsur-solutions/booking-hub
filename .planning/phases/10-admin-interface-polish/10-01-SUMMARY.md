---
phase: 10-admin-interface-polish
plan: 01
subsystem: ui
tags: [react, typescript, playwright, admin, colour-picker, soft-delete, crud]

# Dependency graph
requires:
  - phase: 09-core-user-interface
    provides: App shell navigation (UI-NAV-01), StateConfirmationBanner (UI-PATTERN-01)
  - phase: 04-reference-data-extensibility-configuration
    provides: Backend GET/POST/PUT/DELETE /locations API (soft-delete, colour/building/layouts fields)
provides:
  - Locations Admin table (Name/Colour swatch/Building/Layout/Actions columns) at /admin/locations
  - ColourPicker reusable component (hex input + visual swatch + native picker fallback)
  - SoftDeleteDialog reusable component with contextMessage prop for booking-reference warnings
  - LocationForm single-page 6-field form (name/class/colour/description/building/layouts)
  - locationsApi.ts with getAll/getById/create/update/delete methods
  - 11 Playwright E2E tests covering full CRUD + validation
affects: [other admin plans that may reuse SoftDeleteDialog or ColourPicker]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Visual colour swatch: inline-block span with backgroundColor style (ensures Playwright visibility without external CSS)"
    - "Soft-delete dialog: open/onConfirm/onCancel/entityName/contextMessage prop pattern"
    - "ColourPicker dual-input: hex text + native browser picker + live swatch preview"
    - "Admin page pattern: useQuery for data fetch, filter deleted_at==null client-side, Link to /new and /:id/edit"

key-files:
  created:
    - frontend/src/api/locationsApi.ts
    - frontend/src/components/admin/LocationTable.tsx
    - frontend/src/components/admin/LocationForm.tsx
    - frontend/src/components/common/ColourPicker.tsx
    - frontend/src/components/common/SoftDeleteDialog.tsx
    - frontend/e2e/admin/locations-admin.spec.ts
  modified:
    - frontend/src/pages/admin/LocationsAdmin.tsx
    - frontend/src/pages/admin/LocationCreate.tsx
    - frontend/src/pages/admin/LocationEdit.tsx
    - frontend/src/routes/index.tsx

key-decisions:
  - "Client-side soft-delete filter: getAll() returns all rows including deleted; LocationsAdmin filters deleted_at !== null client-side per Phase 4 Plan 02 decision"
  - "Inline styles on colour swatches: ensures Playwright toBeVisible() works without external CSS loaded in test environment"
  - "locationsApi method names follow plan's exact API contract (getAll/getById/create/update/delete) — separate from pre-existing locations.ts (getLocations/createLocation etc.)"
  - "Pre-existing stub files restored: CustomFieldForm.tsx was already in git HEAD; restored rather than replacing"

patterns-established:
  - "ColourPicker: hex input + visual swatch + native browser picker, validates ^#[0-9A-Fa-f]{3,6}$ on blur"
  - "SoftDeleteDialog: reusable across all admin entities with contextMessage explaining data retention"
  - "Admin table: Name/ColourSwatch/Building/Layout/Actions column layout for location-like entities"

# Metrics
duration: 7min
completed: 2026-10-15
---

# Phase 10 Plan 01: Locations Admin Interface Summary

**Locations Admin with visual colour-swatch table, single-page 6-field CRUD form with ColourPicker (hex input + swatch + native picker), SoftDeleteDialog showing booking-retention context message, and 11 Playwright tests covering create/edit/soft-delete/validation**

## Performance

- **Duration:** 7 min
- **Started:** 2026-10-15T02:14:23Z
- **Completed:** 2026-10-15T02:21:42Z
- **Tasks:** 3
- **Files modified:** 10

## Accomplishments

- Locations Admin table at `/admin/locations` with Name, Colour (visual swatch), Building, Layout, Actions columns — no raw hex text in table
- Single-page Create/Edit form (not wizard) with all 6 fields visible: name, class, colour, description, building, layouts
- ColourPicker component: dual hex text input + visual swatch preview + native `<input type="color">` fallback, validates hex format on blur with error message
- SoftDeleteDialog reusable component with context-aware message: "Existing bookings will keep this location reference"
- StateConfirmationBanner shown after successful save, then auto-navigates back to `/admin/locations`
- 11 Playwright E2E tests, all passing: create (3 tests), edit (2 tests), soft-delete (3 tests), colour validation (3 tests)

## Task Commits

Each task was committed atomically:

1. **Task 1: Locations Admin table view with soft-delete** — `0e5571c` (feat)
2. **Task 2: Location Create/Edit form with colour picker** — `21d5414` (feat)
3. **Task 3: Playwright tests for Locations Admin CRUD flow** — `5ed3ed5` (feat)

**Plan metadata:** _(docs commit — to follow)_

## Files Created/Modified

### Created (6 files)
- `frontend/src/api/locationsApi.ts` — API client with getAll/getById/create/update/delete (soft-delete) methods
- `frontend/src/components/admin/LocationTable.tsx` — Data table with colour swatch column, Edit/Delete action buttons, SoftDeleteDialog integration
- `frontend/src/components/admin/LocationForm.tsx` — 6-field single-page form with inline validation (name required, hex format)
- `frontend/src/components/common/ColourPicker.tsx` — Hex input + visual swatch + native picker, validates `^#[0-9A-Fa-f]{3,6}$`
- `frontend/src/components/common/SoftDeleteDialog.tsx` — Reusable confirm dialog with contextMessage prop
- `frontend/e2e/admin/locations-admin.spec.ts` — 11 Playwright tests with mocked API, role injection

### Modified (4 files)
- `frontend/src/pages/admin/LocationsAdmin.tsx` — Replaced stub with full implementation using useQuery + LocationTable
- `frontend/src/pages/admin/LocationCreate.tsx` — Replaced stub with full LocationForm + locationsApi.create
- `frontend/src/pages/admin/LocationEdit.tsx` — Replaced stub with useQuery load + LocationForm + locationsApi.update
- `frontend/src/routes/index.tsx` — Added `/admin/locations/new` and `/admin/locations/:id/edit` routes + imports

## Decisions Made

- **locationsApi.ts separate from locations.ts:** Plan requires specific method names (getAll/create/update/delete); pre-existing `locations.ts` uses different names (getLocations/createLocation etc.). Created `locationsApi.ts` alongside it per plan's explicit API client spec.
- **Inline swatch styles:** Playwright `toBeVisible()` requires elements have non-zero dimensions. Since the test environment has no external CSS, swatches use inline `display:inline-block; width:2rem; height:2rem` to pass Playwright visibility checks.
- **Client-side deleted_at filter:** `locationsApi.getAll()` calls `GET /locations` which returns all rows including soft-deleted (Phase 4 Plan 02 decision). Admin table filters `deleted_at === null` client-side to show only active locations.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Swatch elements required inline dimensions for Playwright visibility**
- **Found during:** Task 3 (first Playwright test run)
- **Issue:** `<span>` swatches with only className had zero dimensions; Playwright's `toBeVisible()` reports "hidden" for zero-dimension elements even with backgroundColor set
- **Fix:** Added `display: inline-block; width: 2rem; height: 2rem; border; border-radius` inline styles to ColourPicker swatch and LocationTable colour swatch
- **Files modified:** `frontend/src/components/common/ColourPicker.tsx`, `frontend/src/components/admin/LocationTable.tsx`
- **Verification:** All 11 Playwright tests pass
- **Committed in:** `5ed3ed5`

**2. [Rule 3 - Blocking] Test route conflict: mockLocationById + mockLocationUpdate same URL pattern**
- **Found during:** Task 3 (second test run iteration)
- **Issue:** `mockLocationById` and `mockLocationUpdate` both registered separate route handlers for the same URL pattern (`/locations/loc-a`); only the first handler ran
- **Fix:** Merged GET and PUT handling into a single route handler in the "Edit form saves" test, using `route.request().method()` discrimination
- **Files modified:** `frontend/e2e/admin/locations-admin.spec.ts`
- **Verification:** Edit save test passes
- **Committed in:** `5ed3ed5`

---

**Total deviations:** 2 auto-fixed (2 blocking)
**Impact on plan:** Both fixes necessary for correct test execution. No scope creep.

## Known Stubs

None found — all components have complete implementations.

## Issues Encountered

- First Playwright run showed 8 passed, 3 failing. Second run after fixes showed 11/11 passing. The initial failures were due to missing inline swatch dimensions and route handler conflict (both auto-fixed under deviation rules).

## User Setup Required

None — no external service configuration required.

## Next Phase Readiness

- **ColourPicker** ready for reuse in Resources Admin and any other entity with colour metadata
- **SoftDeleteDialog** ready for reuse in Resources Admin, Users Admin, or any soft-delete admin pattern
- **locationsApi.ts** ready for use by any component needing Location CRUD
- **Admin page pattern** established: useQuery → filter deleted_at → LocationTable → SoftDeleteDialog

## Self-Check

- [x] `frontend/src/api/locationsApi.ts` — EXISTS
- [x] `frontend/src/components/admin/LocationTable.tsx` — EXISTS
- [x] `frontend/src/components/admin/LocationForm.tsx` — EXISTS
- [x] `frontend/src/components/common/ColourPicker.tsx` — EXISTS
- [x] `frontend/src/components/common/SoftDeleteDialog.tsx` — EXISTS
- [x] `frontend/src/pages/admin/LocationsAdmin.tsx` — EXISTS (modified from stub)
- [x] `frontend/src/pages/admin/LocationCreate.tsx` — EXISTS (modified from stub)
- [x] `frontend/src/pages/admin/LocationEdit.tsx` — EXISTS (modified from stub)
- [x] `frontend/e2e/admin/locations-admin.spec.ts` — EXISTS with 11 tests
- [x] Build: `npm run build` → exit 0 ✓
- [x] Playwright: 11/11 tests pass ✓
- [x] Integration contracts: ColourPicker onChange CONTRACT_OK, SoftDeleteDialog contextMessage CONTRACT_OK ✓
- [x] No blocking stubs found

## Self-Check: PASSED

---
*Phase: 10-admin-interface-polish*
*Completed: 2026-10-15*
