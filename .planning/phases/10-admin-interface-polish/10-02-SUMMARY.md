---
phase: 10-admin-interface-polish
plan: 02
subsystem: ui
tags: [react, typescript, playwright, admin, resources, soft-delete]

# Dependency graph
requires:
  - phase: 10-admin-interface-polish/10-01
    provides: SoftDeleteDialog component with contextMessage prop
  - phase: 09-core-user-interface
    provides: StateConfirmationBanner, AppShell, routing
provides:
  - Resources Admin table with Name/Type/Description/Is Unique/Restrict Locations columns
  - ResourceForm with is_unique checkbox and restrict_locations multi-select
  - resourcesApi.ts with getAll/getById/create/update/delete methods
  - ResourceCreate and ResourceEdit pages
  - Playwright tests for Resources Admin CRUD flow
affects:
  - 10-03-onwards (ResourceCreate/ResourceEdit pages referenced in routes)

# Tech tracking
tech-stack:
  added: []
  patterns:
    - Admin table + form pattern (parallel to LocationsAdmin/LocationTable/LocationForm)
    - restrict_locations multi-select via checkbox list
    - is_unique checkbox with inline tooltip hint

key-files:
  created:
    - frontend/src/api/resourcesApi.ts
    - frontend/src/components/admin/ResourceTable.tsx
    - frontend/src/components/admin/ResourceForm.tsx
    - frontend/src/pages/admin/ResourceCreate.tsx
    - frontend/src/pages/admin/ResourceEdit.tsx
    - frontend/e2e/admin/resources-admin.spec.ts
  modified:
    - frontend/src/pages/admin/ResourcesAdmin.tsx
    - frontend/src/routes/index.tsx

key-decisions:
  - "restrict_locations implemented as checkbox list (not react-select) — consistent with project pattern of native HTML inputs, no new dependencies"
  - "ResourceForm handles both create and edit via optional initialData prop — same single-page form shared by ResourceCreate and ResourceEdit"
  - "ResourceTable receives locations[] as prop from parent (ResourcesAdmin fetches) — avoids duplicate API calls in table component"

patterns-established:
  - "Admin CRUD pattern: Admin page (fetches data) → Table component (renders + delete) → Form component (create/edit) — established by LocationsAdmin, now applied to Resources"
  - "restrict_locations multi-select: checkbox list fetching via locationsApi.getAll(), filtering deleted_at === null, storing UUIDs"

# Metrics
duration: 5min
completed: 2026-10-15
---

# Phase 10 Plan 02: Resources Admin Summary

**Resources Admin screen with table, is_unique checkbox for conflict enforcement, restrict_locations multi-select for location-scoped availability, soft-delete confirmation dialog, and 4 passing Playwright tests.**

## Performance

- **Duration:** 5 min
- **Started:** 2026-10-15T02:15:20Z
- **Completed:** 2026-10-15T02:20:36Z
- **Tasks:** 3
- **Files modified:** 8

## Accomplishments

- Resources Admin table displays Name, Type, Description, Is Unique (checkmark/dash icon), Restrict Locations (location names or "All locations"), and Actions columns
- ResourceForm with all 5 fields from Phase 4 schema: name, type, description, is_unique (checkbox with tooltip), restrict_locations (checkbox multi-select fetching locations)
- ResourceCreate and ResourceEdit pages using the shared form component
- resourcesApi.ts with correct method signatures (getAll/getById/create/update/delete) matching Phase 4 endpoint contracts
- Soft-delete shows "Existing bookings will keep this resource reference" via SoftDeleteDialog
- StateConfirmationBanner appears after successful save
- 4/4 Playwright tests pass covering create, edit, soft-delete, and is_unique behavior

## Task Commits

Each task was committed atomically:

1. **Task 1: Resources Admin table view with soft-delete** - `4760f56` (feat)
2. **Task 2: Resource Create/Edit form with is_unique and restrict_locations** - `c3a4bd3` (feat)
3. **Task 3: Playwright tests for Resources Admin CRUD flow** - `7231b9a` (test)

## Files Created/Modified

- `frontend/src/api/resourcesApi.ts` - API client with getAll/getById/create/update/delete methods
- `frontend/src/components/admin/ResourceTable.tsx` - Table with 6 columns and SoftDeleteDialog integration
- `frontend/src/components/admin/ResourceForm.tsx` - Single-page form with is_unique checkbox and restrict_locations multi-select
- `frontend/src/pages/admin/ResourcesAdmin.tsx` - Main admin page shell fetching resources and locations
- `frontend/src/pages/admin/ResourceCreate.tsx` - Create route rendering empty ResourceForm
- `frontend/src/pages/admin/ResourceEdit.tsx` - Edit route fetching resource and rendering pre-filled ResourceForm
- `frontend/src/routes/index.tsx` - Added /admin/resources/new and /admin/resources/:id/edit routes
- `frontend/e2e/admin/resources-admin.spec.ts` - 4 Playwright tests for CRUD flow

## Decisions Made

- `restrict_locations` implemented as a checkbox list instead of react-select. Consistent with project's pattern of native HTML inputs without additional dependencies. Stores UUIDs, pre-selects in edit mode.
- `ResourceTable` receives `locations[]` as a prop from `ResourcesAdmin` parent (which fetches once) rather than fetching inside the table. Avoids duplicate API calls when navigating.
- `ResourceForm` handles both create and edit modes via optional `initialData` prop — same component, single-page form pattern (not wizard).

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None.

## User Setup Required

None - no external service configuration required.

## Known Stubs

None found — all handler logic is implemented with real API calls via resourcesApi.

## Next Phase Readiness

- Resources Admin fully functional with table, form, soft-delete, and tests
- ResourceCreate and ResourceEdit routes registered at /admin/resources/new and /admin/resources/:id/edit
- resourcesApi.ts exported and available for use by other components (e.g., BookingForm resources dropdown)
- 4 Playwright tests pass; admin e2e/admin/ directory established as pattern for subsequent admin tests

## Self-Check: PASSED

- `frontend/src/api/resourcesApi.ts` ✓ exists
- `frontend/src/components/admin/ResourceTable.tsx` ✓ exists
- `frontend/src/components/admin/ResourceForm.tsx` ✓ exists
- `frontend/src/pages/admin/ResourceCreate.tsx` ✓ exists
- `frontend/src/pages/admin/ResourceEdit.tsx` ✓ exists
- `frontend/e2e/admin/resources-admin.spec.ts` ✓ exists
- Commits `4760f56`, `c3a4bd3`, `7231b9a` ✓ present in git log
- Build check: `npm run build` → exit 0 ✓
- Known Stubs: None (no blocking stubs)

---
*Phase: 10-admin-interface-polish*
*Completed: 2026-10-15*
