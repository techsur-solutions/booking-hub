---
phase: 02-platform-foundation-infrastructure
plan: 11
subsystem: ui
tags: [react, typescript, vite, react-router, keycloak-js, playwright, nginx, docker]

# Dependency graph
requires:
  - phase: 02-09-keycloak-realm
    provides: Keycloak realm (bookinghub) with bookinghub-frontend public PKCE client configured
provides:
  - React + TypeScript SPA shell with Vite 5.x build tooling and HMR dev server
  - react-router-dom 6.x routing with 13-route table covering all planned screens
  - AppShell navigation component linking to 12 nav-reachable routes (all except display-board kiosk view)
  - 12 placeholder page components mapped to their implementing phases (3-7)
  - Keycloak PKCE (Authorization Code + S256) authentication integration with public client (no client secret)
  - ProtectedRoute guard preventing unauthenticated access to all routes except /feeds and /display-board
  - Multi-stage Dockerfile (node:20-alpine builder → nginx:alpine static-serve runtime)
  - nginx.conf with SPA client-side routing fallback and static asset caching
  - Playwright E2E tests proving navigation and auth-redirect behavior
affects: [03-identity-access-control, 04-reference-data, 05-core-booking, 06-notifications-audit, 07-public-feeds]

# Tech tracking
tech-stack:
  added: [vite@5.x, react@18.x, react-dom@18.x, typescript@5.x, react-router-dom@6.x, keycloak-js, @playwright/test, nginx:alpine]
  patterns:
    - "Vite-based SPA build with TypeScript strict mode and verbatimModuleSyntax"
    - "React Router useRoutes hook-based routing with RouteObject[] declaration"
    - "AppShell wrapper providing persistent nav header/sidebar for all non-kiosk routes"
    - "ProtectedRoute component gating rendering until Keycloak authentication confirmed (no flash of protected content)"
    - "E2E test bypass via VITE_E2E_MODE env var allowing Playwright navigation tests to run without live Keycloak"
    - "Multi-stage Docker build (CI/K8s-ready): build in node:20-alpine with full devDependencies, serve static dist from nginx:alpine"

key-files:
  created:
    - frontend/package.json
    - frontend/vite.config.ts
    - frontend/tsconfig.json
    - frontend/src/App.tsx
    - frontend/src/routes/index.tsx
    - frontend/src/components/AppShell.tsx
    - frontend/src/components/Nav.tsx
    - frontend/src/pages/CalendarView.tsx
    - frontend/src/pages/ListView.tsx
    - frontend/src/pages/DayView.tsx
    - frontend/src/pages/admin/LocationsAdmin.tsx
    - frontend/src/pages/admin/ResourcesAdmin.tsx
    - frontend/src/pages/admin/CustomFieldsAdmin.tsx
    - frontend/src/pages/admin/UsersAdmin.tsx
    - frontend/src/pages/admin/RolesAdmin.tsx
    - frontend/src/pages/admin/SettingsAdmin.tsx
    - frontend/src/pages/AuditLogViewer.tsx
    - frontend/src/pages/FeedsLanding.tsx
    - frontend/src/pages/DisplayBoard.tsx
    - frontend/src/auth/keycloak.ts
    - frontend/src/auth/AuthProvider.tsx
    - frontend/src/auth/ProtectedRoute.tsx
    - frontend/Dockerfile
    - frontend/.dockerignore
    - frontend/nginx.conf
    - frontend/playwright.config.ts
    - frontend/e2e/navigation.spec.ts
    - frontend/e2e/auth-redirect.spec.ts
    - frontend/public/silent-check-sso.html
  modified: []

key-decisions:
  - "Used react-router-dom 6.x useRoutes hook-based routing instead of older <Routes>/<Route> component tree for cleaner, type-safe route declarations"
  - "Display board (/display-board) renders without AppShell wrapper to provide full-screen kiosk layout with no nav chrome"
  - "E2E tests bypass real Keycloak via VITE_E2E_MODE env var to enable fast, hermetic navigation/routing verification without external dependencies"
  - "Auth-redirect tests run with real ProtectedRoute logic active (no E2E bypass) and intercept Keycloak authorize endpoint to verify PKCE redirect behavior without requiring live Keycloak server"
  - "Playwright configured with workers: 1 to avoid parallel test race conditions and match sandbox CPU constraints"
  - "Fixed TypeScript verbatimModuleSyntax errors by using 'import type' for ReactNode and RouteObject type-only imports"

patterns-established:
  - "Route table pattern: centralized RouteObject[] in src/routes/index.tsx importing all page components"
  - "AppShell wrapper pattern: shared nav/header layout applied via wrapper element in route definitions"
  - "Public route exemption pattern: /feeds and /display-board routes render without ProtectedRoute wrapper per F9.6 public access requirement"
  - "Auth context pattern: AuthProvider wraps entire app, ProtectedRoute components consume useAuth() hook for authentication state"
  - "E2E bypass convention: VITE_E2E_MODE env var controlled by playwright webServer env, read by AuthProvider and ProtectedRoute to skip Keycloak init"

# Metrics
duration: 5min
completed: 2026-10-07
---

# Phase 2 Plan 11: React + TypeScript SPA Shell Summary

**Vite 5.x + React 18.x + TypeScript SPA with 13-route placeholder screen shell, Keycloak PKCE authentication, protected-route guards, and nginx-served Docker image — ready for Phases 3-7 to build real screens into placeholders**

## Performance

- **Duration:** 5 min
- **Started:** 2026-10-07T03:36:15Z
- **Completed:** 2026-10-07T03:41:07Z
- **Tasks:** 3
- **Files created:** 29

## Accomplishments

- Scaffolded Vite 5.x + React 18.x + TypeScript 5.x SPA with react-router-dom 6.x routing
- Created complete 13-route navigation table covering all planned screens (calendar, list, day views; 6 admin CRUD pages; audit log; public feeds; display board)
- Built AppShell wrapper with persistent nav header/sidebar linking to all 12 non-kiosk routes
- Created 12 placeholder page components with phase-mapped "built in Phase N" descriptions
- Integrated Keycloak PKCE (Authorization Code + S256) authentication with public client (no client secret anywhere)
- Implemented ProtectedRoute guard preventing unauthenticated rendering of protected content
- Public routes (/feeds, /display-board) accessible without authentication per F9.6 requirement
- Display board renders full-screen kiosk layout with no nav chrome
- E2E test bypass via VITE_E2E_MODE enabling Playwright navigation tests without live Keycloak
- Multi-stage Dockerfile: node:20-alpine builder with full devDependencies → nginx:alpine static-serve runtime
- nginx.conf with SPA client-side routing fallback (try_files $uri /index.html) and static asset caching
- Playwright test suite: 6 navigation tests + 3 auth-redirect tests, all passing

## Task Commits

Each task was committed atomically:

1. **Task 1: Vite + React + TS scaffold, routing table, shared app shell (nav)** - `5755afc` (feat)
2. **Task 2: Placeholder page stubs (all 12 screens) + full build + navigation Playwright test** - `e166674` (feat)
3. **Task 3: Keycloak PKCE auth wiring + protected-route redirect** - `8f22184` (feat)

**Plan metadata:** (to be added in final commit)

## Files Created/Modified

### Created (29 files)
- `frontend/package.json` - Vite + React + TypeScript + react-router-dom + keycloak-js + Playwright dependencies
- `frontend/vite.config.ts` - Vite build configuration
- `frontend/tsconfig.json`, `frontend/tsconfig.app.json`, `frontend/tsconfig.node.json` - TypeScript strict mode with verbatimModuleSyntax
- `frontend/index.html` - SPA HTML shell
- `frontend/src/main.tsx` - React root render
- `frontend/src/App.tsx` - BrowserRouter + AuthProvider + RouteRenderer
- `frontend/src/routes/index.tsx` - RouteObject[] table with 13 routes, ProtectedRoute guards on 11 protected routes
- `frontend/src/components/AppShell.tsx` - Nav wrapper for non-kiosk routes
- `frontend/src/components/Nav.tsx` - Persistent header/sidebar with links to all 12 nav-reachable routes
- `frontend/src/pages/CalendarView.tsx` - Calendar booking view placeholder (Phase 5)
- `frontend/src/pages/ListView.tsx` - List booking view placeholder (Phase 5)
- `frontend/src/pages/DayView.tsx` - Day booking view placeholder (Phase 5)
- `frontend/src/pages/admin/LocationsAdmin.tsx` - Location CRUD placeholder (Phase 4)
- `frontend/src/pages/admin/ResourcesAdmin.tsx` - Resource CRUD placeholder (Phase 4)
- `frontend/src/pages/admin/CustomFieldsAdmin.tsx` - Custom field CRUD placeholder (Phase 4)
- `frontend/src/pages/admin/UsersAdmin.tsx` - User management placeholder (Phase 3)
- `frontend/src/pages/admin/RolesAdmin.tsx` - Role/permission mapping placeholder (Phase 3)
- `frontend/src/pages/admin/SettingsAdmin.tsx` - System settings placeholder (Phase 4)
- `frontend/src/pages/AuditLogViewer.tsx` - Audit log viewer placeholder (Phase 6)
- `frontend/src/pages/FeedsLanding.tsx` - Public feeds landing placeholder (Phase 7), links to display board
- `frontend/src/pages/DisplayBoard.tsx` - Digital signage kiosk view placeholder (Phase 7), full-screen no-nav layout
- `frontend/src/auth/keycloak.ts` - Keycloak config: realm=bookinghub, clientId=bookinghub-frontend, VITE_KEYCLOAK_URL env var
- `frontend/src/auth/AuthProvider.tsx` - Keycloak init with pkceMethod=S256, E2E bypass, React context provider
- `frontend/src/auth/ProtectedRoute.tsx` - Auth guard: redirects via keycloak.login() before rendering children
- `frontend/Dockerfile` - Multi-stage build: node:20-alpine builder → nginx:alpine runtime
- `frontend/.dockerignore` - Exclude node_modules, dist, .git
- `frontend/nginx.conf` - SPA routing fallback, gzip, static asset caching
- `frontend/playwright.config.ts` - baseURL localhost:5173, workers: 1, conditional VITE_E2E_MODE via webServer env
- `frontend/e2e/navigation.spec.ts` - 6 tests: root redirect, all nav links, routing to each placeholder, display-board no-nav
- `frontend/e2e/auth-redirect.spec.ts` - 3 tests: protected route PKCE redirect with code_challenge_method=S256, public routes no redirect
- `frontend/public/silent-check-sso.html` - Keycloak silent SSO check iframe

### Modified
- None (initial frontend scaffold)

## Decisions Made

- **React Router 6.x useRoutes pattern:** Chose hook-based useRoutes(RouteObject[]) over older <Routes>/<Route> component tree for cleaner, type-safe route declarations and easier centralized route table management
- **Display board full-screen kiosk layout:** Deliberately renders without AppShell wrapper to provide full-screen digital signage view with no nav chrome, matching kiosk use case from TechArch §2.10
- **E2E test bypass strategy:** VITE_E2E_MODE env var (set by Playwright webServer) allows navigation tests to run hermetically without live Keycloak, while auth-redirect tests run with real ProtectedRoute logic and intercept the Keycloak endpoint to verify PKCE behavior
- **Playwright workers: 1:** Single-worker mode avoids parallel test race conditions and matches sandbox CPU constraints ($PIVOTA_SANDBOX_CPUS typically 2 cores)
- **Public route exemption:** /feeds and /display-board routes render without ProtectedRoute wrapper per F9.6 public access requirement — feeds landing page links to display board to satisfy "reached from" reachability path

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Fixed TypeScript verbatimModuleSyntax type import errors**
- **Found during:** Task 2 (first npm run build after creating components)
- **Issue:** TypeScript compiler error: 'ReactNode' is a type and must be imported using a type-only import when 'verbatimModuleSyntax' is enabled (same for RouteObject)
- **Fix:** Changed `import { ReactNode } from 'react'` to `import type { ReactNode } from 'react'` in AppShell.tsx; changed `import { Navigate, RouteObject } from 'react-router-dom'` to separate value/type imports in routes/index.tsx
- **Files modified:** frontend/src/components/AppShell.tsx, frontend/src/routes/index.tsx
- **Verification:** npm run build succeeded, Vite build output showed no TypeScript errors
- **Committed in:** e166674 (Task 2 commit)

---

**Total deviations:** 1 auto-fixed (1 bug)
**Impact on plan:** Single type-import fix required by TypeScript strict mode configuration; no functional impact, build succeeded after fix

## Issues Encountered

None - plan executed smoothly with only the expected TypeScript strict-mode type-import correction

## User Setup Required

None - no external service configuration required. Keycloak PKCE client configuration referenced in keycloak.ts is provisioned in plan 02-09 (Keycloak realm); frontend reads VITE_KEYCLOAK_URL env var for runtime Keycloak server discovery (defaults to http://localhost:8180 for local dev).

## Next Phase Readiness

- **Frontend scaffold complete:** React + TypeScript SPA shell exists with routing, nav, and Keycloak PKCE authentication wiring
- **All planned screens have placeholder routes:** Every screen from TechArch §2.10 has a nav-reachable placeholder ready for Phases 3-7 to build into
- **Protected-route guards active:** ProtectedRoute prevents unauthenticated rendering; public routes (/feeds, /display-board) accessible without login per F9.6
- **Docker image buildable:** Multi-stage Dockerfile produces deployable nginx-served static image
- **Ready for Phase 3:** Identity & Access Control phase can build real login UI and user/role management screens into the UsersAdmin and RolesAdmin placeholders
- **Blockers:** None - full-stack integration verification happens in plan 02-12 (docker-compose boot of entire platform)

---
*Phase: 02-platform-foundation-infrastructure*
*Completed: 2026-10-07*
