---
phase: 09-core-user-interface
plan: "02"
subsystem: ui
tags: [react, typescript, axios, react-query, vitest, jwt, keycloak]

requires:
  - phase: 02-platform-foundation-infrastructure
    provides: React SPA with Keycloak auth, Spring Cloud Gateway as API entry point

provides:
  - Axios apiClient with JWT bearer token request interceptor and 401 redirect
  - TypeScript interfaces for all backend DTOs (Booking, Location, Resource, Settings, User, Permission, FeedBooking)
  - Type-safe API method modules for all services (bookings, locations, resources, customFields, users, settings, feeds)
  - React Query hooks for bookings, locations, and settings with proper query key patterns
  - ErrorBoundary component wrapping App in main.tsx
  - LoadingSpinner reusable component
  - Unit tests for JWT interceptor and 401 handling (4 tests, all passing)

affects:
  - 09-core-user-interface
  - All plans consuming API layer (09-03 through 09-15)

tech-stack:
  added: [axios@^1.6.0, "@tanstack/react-query@^5.0.0", vitest, axios-mock-adapter]
  patterns:
    - "apiClient singleton with JWT interceptor — all API calls route through it"
    - "React Query hooks per entity — useBookings/useLocations/useSettings with proper queryKey arrays"
    - "import type — all type-only imports use type keyword for verbatimModuleSyntax compat"

key-files:
  created:
    - frontend/src/api/client.ts
    - frontend/src/api/types.ts
    - frontend/src/api/bookings.ts
    - frontend/src/api/locations.ts
    - frontend/src/api/resources.ts
    - frontend/src/api/customFields.ts
    - frontend/src/api/users.ts
    - frontend/src/api/settings.ts
    - frontend/src/api/feeds.ts
    - frontend/src/hooks/useBookings.ts
    - frontend/src/hooks/useLocations.ts
    - frontend/src/hooks/useSettings.ts
    - frontend/src/components/ErrorBoundary.tsx
    - frontend/src/components/LoadingSpinner.tsx
    - frontend/src/api/client.test.ts
  modified:
    - frontend/src/main.tsx
    - frontend/package.json
    - frontend/vite.config.ts

key-decisions:
  - "Named export keycloak (not default) from keycloak.ts — client.ts imports { keycloak } for token access"
  - "vite.config.ts defineConfig from vitest/config (not vite) — required for test property type safety"
  - "import type used throughout API layer — required by verbatimModuleSyntax in tsconfig"
  - "useSettings staleTime=5min — settings change infrequently, cache aggressively"
  - "feedsApi exposes iCal/RSS as URL builders (not API calls) — feeds are direct URL consumption, not JSON"

patterns-established:
  - "API module pattern: one module per backend service, methods map to FRD Y1-api endpoints"
  - "React Query pattern: useX() for list/singleton queries, useCreateX/useUpdateX/useDeleteX for mutations invalidating queryKey"
  - "Test pattern: vi.mock('../auth/keycloak') + MockAdapter for isolated client tests"

duration: 6min
completed: "2026-10-13"
---

# Phase 9 Plan 02: API Client Layer Summary

**Axios apiClient with JWT bearer/401 interceptors, type-safe API modules for all 7 backend services, React Query hooks for bookings/locations/settings, ErrorBoundary, and 4-passing vitest unit tests**

## Performance

- **Duration:** 6 min
- **Started:** 2026-10-13T17:27:11Z
- **Completed:** 2026-10-13T17:32:37Z
- **Tasks:** 3
- **Files modified:** 18

## Accomplishments

- Axios `apiClient` singleton with JWT bearer token injection on every request and automatic redirect to `keycloak.login()` on 401
- 7 type-safe API modules (bookings, locations, resources, customFields, users, settings, feeds) wrapping `apiClient` with endpoint methods matching FRD Y1-api
- React Query hooks for common entities — `useBookings`, `useLocationById`, `useCreateBooking`, etc., with proper query key invalidation
- `ErrorBoundary` wrapping `<App />` in `main.tsx` catching network/API failures with user-friendly UI
- 4 unit tests passing: JWT token injection, no-token path, 401→keycloak.login(), non-401 no login call

## Task Commits

Each task was committed atomically:

1. **Task 1: Axios client with JWT interceptor** - `87599f3` (feat)
2. **Task 2: API modules + React Query hooks** - `d034902` (feat)
3. **Task 3: ErrorBoundary, LoadingSpinner, unit tests** - `f1174ed` (feat)

**Plan metadata:** to be committed in docs commit

## Files Created/Modified

- `frontend/src/api/client.ts` — Axios instance with JWT request interceptor + 401 response interceptor
- `frontend/src/api/types.ts` — TypeScript interfaces: Booking, Location, Resource, Settings, User, Permission, FeedBooking, ConflictFlag, CustomField, CustomFieldValue
- `frontend/src/api/bookings.ts` — getBookings/getById/create/update/delete/checkConflicts/approve/deny/clone
- `frontend/src/api/locations.ts` — CRUD methods for locations
- `frontend/src/api/resources.ts` — CRUD methods for resources
- `frontend/src/api/customFields.ts` — CRUD + field values methods
- `frontend/src/api/users.ts` — CRUD + permissions + me
- `frontend/src/api/settings.ts` — getSettings/updateSettings (singleton)
- `frontend/src/api/feeds.ts` — JSON/iCal/RSS feed access
- `frontend/src/hooks/useBookings.ts` — useBookings, useBookingById, useCreateBooking, useUpdateBooking, useDeleteBooking, useApproveBooking, useDenyBooking, useCheckConflicts
- `frontend/src/hooks/useLocations.ts` — useLocations, useLocationById, useCreateLocation, useUpdateLocation, useDeleteLocation
- `frontend/src/hooks/useSettings.ts` — useSettings (5min stale), useUpdateSettings
- `frontend/src/components/ErrorBoundary.tsx` — React class error boundary with optional fallback prop
- `frontend/src/components/LoadingSpinner.tsx` — Accessible spinner with role=status, aria-live
- `frontend/src/api/client.test.ts` — 4 vitest unit tests for client interceptors
- `frontend/src/main.tsx` — Wrapped `<App />` in `<ErrorBoundary>`
- `frontend/package.json` — Added axios, @tanstack/react-query, vitest, axios-mock-adapter
- `frontend/vite.config.ts` — Changed defineConfig import to vitest/config

## Decisions Made

- Named export `keycloak` (not default export) from `keycloak.ts` — imported as `{ keycloak }` in client.ts
- `vite.config.ts` requires `defineConfig` from `vitest/config` (not `vite`) when `test` property is used
- `import type` required throughout for `verbatimModuleSyntax` TypeScript config
- `useSettings` staleTime = 5 minutes — settings change infrequently, cache aggressively to reduce gateway load
- `feedsApi` exposes iCal/RSS as URL builders (not API calls) — feeds are direct URL links for browsers/calendar apps

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] TypeScript verbatimModuleSyntax required import type for all type-only imports**
- **Found during:** Task 3 (build verification)
- **Issue:** `verbatimModuleSyntax` in tsconfig requires `import type` for interfaces; all api modules and hooks used plain `import { Interface }`
- **Fix:** Changed to `import type { ... }` in all 7 api modules and 3 hooks
- **Files modified:** All api/*.ts and hooks/*.ts files
- **Verification:** `npm run build` passes with no TS errors
- **Committed in:** `f1174ed` (Task 3 commit)

**2. [Rule 3 - Blocking] vite.config.ts used wrong defineConfig for test configuration**
- **Found during:** Task 3 (build verification)
- **Issue:** `vite.config.ts` imported `defineConfig` from `vite` but the `test` property (vitest config) requires `defineConfig` from `vitest/config` — stale tsbuildinfo cache had masked this
- **Fix:** Changed `from 'vite'` to `from 'vitest/config'` in vite.config.ts
- **Files modified:** frontend/vite.config.ts
- **Verification:** `npm run build` passes cleanly; `npm test` passes 4/4 tests
- **Committed in:** `f1174ed` (Task 3 commit)

---

**Total deviations:** 2 auto-fixed (1 type annotation, 1 blocking build config)
**Impact on plan:** Both fixes necessary for TypeScript correctness and build success. No scope creep.

## Known Stubs

None found.

## Issues Encountered

None — plan executed successfully with 2 auto-fixed deviations.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- API client layer complete — all subsequent plans can `import { apiClient }` or use the React Query hooks directly
- `useBookings`, `useLocations`, `useSettings` hooks ready for calendar views (09-03 plans)
- `useCreateBooking`, `useApproveBooking`, `useDenyBooking` ready for booking forms and approval queue
- `ErrorBoundary` catches API failures globally; `LoadingSpinner` available for all loading states

---
*Phase: 09-core-user-interface*
*Completed: 2026-10-13*

## Self-Check: PASSED

- `frontend/src/api/client.ts` — FOUND
- `frontend/src/api/types.ts` — FOUND
- `frontend/src/components/ErrorBoundary.tsx` — FOUND
- `frontend/src/components/LoadingSpinner.tsx` — FOUND
- `frontend/src/api/client.test.ts` — FOUND
- Commits `87599f3`, `d034902`, `f1174ed` — FOUND
- Build check: `npm run build` → exit 0 (✓ 42 modules transformed, dist/ generated)
- Unit tests: 4/4 passing
