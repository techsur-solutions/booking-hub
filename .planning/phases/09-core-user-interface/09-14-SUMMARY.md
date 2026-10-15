---
phase: 09-core-user-interface
plan: "14"
subsystem: ui
tags: [react, typescript, playwright, kiosk, display-board, feeds, rss, ical, clipboard]

# Dependency graph
requires:
  - phase: 09-02
    provides: feedsApi with getDisplayBoard method, API client layer

provides:
  - DisplayBoard full-screen kiosk page at /display-board (approved bookings, 60s auto-refresh)
  - ApprovedOnlyBadge component (fixed-position green watermark)
  - FeedSubscription page at /feeds (RSS2/iCal/JSON URLs with copy buttons)
  - 18 Playwright E2E tests (8 Display Board, 10 Feed Subscription)

affects:
  - 09-core-user-interface

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "page.route() with regex pattern to avoid intercepting Vite JS module requests in Playwright"
    - "navigator.clipboard.writeText with execCommand fallback for cross-browser clipboard"
    - "setInterval auto-refresh with useEffect cleanup (clearInterval) for kiosk displays"

key-files:
  created:
    - frontend/src/pages/DisplayBoard.tsx
    - frontend/src/pages/FeedSubscription.tsx
    - frontend/src/components/display/ApprovedOnlyBadge.tsx
    - frontend/e2e/display-board.spec.ts
    - frontend/e2e/feed-subscription.spec.ts
    - frontend/src/pages/PasswordReset.tsx
    - frontend/src/pages/PasswordResetComplete.tsx
  modified:
    - frontend/src/api/feeds.ts
    - frontend/src/routes/index.tsx
    - frontend/src/pages/BookingsView.tsx
    - frontend/src/pages/ListView.test.tsx
    - frontend/src/utils/exportBookings.test.ts
    - frontend/e2e/auth-redirect.spec.ts

key-decisions:
  - "Playwright route mock uses regex /\\/locations(\\?.*)?$/ not glob '**/locations**' to avoid intercepting Vite JS module paths"
  - "FeedsLanding replaced by FeedSubscription at /feeds route — FeedsLanding was a placeholder stub"
  - "feedsApi.getRss2FeedUrl/getICalFeedUrl/getJsonFeedUrl use URL constructor for correct query param encoding"
  - "ApprovedOnlyBadge uses pointerEvents: none so it doesn't block underlying booking card interaction"

patterns-established:
  - "API mock pattern in Playwright: use specific regex not broad glob to avoid MIME-type interference with Vite"
  - "Clipboard mock in Playwright: inject via addInitScript() defining navigator.clipboard with captured array"

# Metrics
duration: 24min
completed: "2026-10-13"
---

# Phase 9 Plan 14: Display Board & Feed Subscription UI Summary

**Full-screen kiosk Display Board with ApprovedOnlyBadge watermark and 60s auto-refresh, plus Feed Subscription page listing RSS2/iCal/JSON URLs with navigator.clipboard copy buttons and location filter, backed by 18 passing Playwright E2E tests**

## Performance

- **Duration:** 24 min
- **Started:** 2026-10-13T17:42:27Z
- **Completed:** 2026-10-13T18:07:10Z
- **Tasks:** 3
- **Files modified:** 12

## Accomplishments

- `DisplayBoard.tsx`: Full-screen kiosk view (no AppShell) fetching approved bookings from `/feeds/display-board`, grouped by date, auto-refreshing every 60 seconds, with `?location_id=X` URL filter and footer link to `/feeds`
- `ApprovedOnlyBadge.tsx`: Fixed-position top-right green semi-transparent badge with "✓ Approved Only" text, non-dismissible, pointer-events: none
- `FeedSubscription.tsx`: Three feed URL sections (RSS2/iCal/JSON) with copy-to-clipboard buttons (navigator.clipboard.writeText + execCommand fallback), location filter dropdown parameterising all three URLs, "No authentication required" green banner
- 18 Playwright E2E tests: 8 for Display Board (layout, badge, bookings, empty, location filter, footer link) + 10 for Feed Subscription (URL sections, banner, copy clipboard, location filter, URL update, preview button)

## Task Commits

Each task was committed atomically:

1. **Task 1: Display Board full-screen kiosk mode** - `49aad74` (feat)
2. **Task 2: Feed Subscription UI with copy buttons** - `86b9e8d` (feat)
3. **Task 3: Playwright E2E tests** - `8c3bddf` (test)

## Files Created/Modified

- `frontend/src/pages/DisplayBoard.tsx` — Full-screen kiosk with auto-refresh, date grouping, location colour dots
- `frontend/src/pages/FeedSubscription.tsx` — Feed URL listing with copy/preview buttons and location filter
- `frontend/src/components/display/ApprovedOnlyBadge.tsx` — Fixed watermark component
- `frontend/e2e/display-board.spec.ts` — 8 Playwright tests for Display Board
- `frontend/e2e/feed-subscription.spec.ts` — 10 Playwright tests for Feed Subscription
- `frontend/src/api/feeds.ts` — Added getDisplayBoard(), getRss2FeedUrl(), getICalFeedUrl(), getJsonFeedUrl()
- `frontend/src/routes/index.tsx` — Replaced FeedsLanding with FeedSubscription at /feeds
- `frontend/src/pages/PasswordReset.tsx` — Created stub (was missing, blocked TypeScript build)
- `frontend/src/pages/PasswordResetComplete.tsx` — Created stub (was missing, blocked TypeScript build)
- `frontend/src/pages/BookingsView.tsx` — Fixed duplicate function body
- `frontend/src/pages/ListView.test.tsx` — Fixed UseQueryResult type casts
- `frontend/src/utils/exportBookings.test.ts` — Fixed Node.js global → globalThis
- `frontend/e2e/auth-redirect.spec.ts` — Updated expected text for feeds page

## Decisions Made

- Playwright `page.route()` uses regex `//\/locations(\?.*)?$/` not glob `**/locations**` — the glob pattern intercepted Vite's JavaScript module request for `/src/api/locations.ts`, causing a MIME-type mismatch that prevented the React app from mounting
- `FeedsLanding` replaced by `FeedSubscription` at `/feeds` — the landing was a placeholder stub; this plan implements the real UI
- `feedsApi.getRss2FeedUrl` etc. use `URL` constructor for correct encoding of query params with special characters
- `ApprovedOnlyBadge` has `pointerEvents: none` to ensure it doesn't block interaction with underlying booking cards

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Fixed BookingsView.tsx duplicate function body**
- **Found during:** Task 1 (TypeScript build)
- **Issue:** `BookingsView.tsx` had a second dangling `return` block (lines 57-92) that was a duplicate of the first, causing TS2128 error
- **Fix:** Removed the orphaned second block
- **Files modified:** `frontend/src/pages/BookingsView.tsx`
- **Verification:** `npm run build` → exit 0
- **Committed in:** `49aad74` (Task 1 commit)

**2. [Rule 3 - Blocking] Created missing PasswordReset.tsx and PasswordResetComplete.tsx**
- **Found during:** Task 1 (TypeScript build)
- **Issue:** `routes/index.tsx` imported `PasswordReset` and `PasswordResetComplete` which didn't exist, blocking the TypeScript build
- **Fix:** Created stub pages (Keycloak manages actual password reset)
- **Files modified:** created both files
- **Verification:** `npm run build` → exit 0
- **Committed in:** `49aad74` (Task 1 commit)

**3. [Rule 1 - Bug] Fixed ListView.test.tsx UseQueryResult type cast**
- **Found during:** Task 2 (TypeScript build)
- **Issue:** `as ReturnType<typeof useBookings>` cast failed with strict tanstack-query types; needed `as unknown as ReturnType<...>`
- **Fix:** Added `unknown` intermediate cast on all 14 occurrences
- **Files modified:** `frontend/src/pages/ListView.test.tsx`
- **Verification:** `npm run build` → exit 0
- **Committed in:** `86b9e8d` (Task 2 commit)

**4. [Rule 1 - Bug] Fixed exportBookings.test.ts Node global → globalThis**
- **Found during:** Task 2 (TypeScript build)
- **Issue:** `global.URL.createObjectURL` used Node.js `global` which doesn't exist in browser jsdom context
- **Fix:** Replaced `global.` with `globalThis.` throughout the file
- **Files modified:** `frontend/src/utils/exportBookings.test.ts`
- **Verification:** TypeScript build passes; `npm test` 74/74 passing
- **Committed in:** `86b9e8d` (Task 2 commit)

**5. [Rule 1 - Bug] Fixed Playwright route mock pattern causing MIME-type mismatch**
- **Found during:** Task 3 (Playwright test debugging)
- **Issue:** `page.route('**/locations**', ...)` intercepted Vite's JavaScript module request `/src/api/locations.ts`, returning JSON instead of JS, preventing React from mounting
- **Fix:** Changed to regex `//\/locations(\?.*)?$/` matching only the API endpoint
- **Files modified:** `frontend/e2e/feed-subscription.spec.ts`
- **Verification:** All 10 Feed Subscription tests pass; 18/18 total E2E tests pass
- **Committed in:** `8c3bddf` (Task 3 commit)

---

**Total deviations:** 5 auto-fixed (2 missing critical/blocking, 3 bugs)
**Impact on plan:** All fixes essential for build and test correctness. No scope creep. Pre-existing issues in files that were touched by this plan's work.

## Known Stubs

- `frontend/src/pages/PasswordReset.tsx` — **Cosmetic**: Full UI built (styled form with email/password fields) but actual password reset is Keycloak-managed; this page is a placeholder for future Keycloak redirect handling
- `frontend/src/pages/PasswordResetComplete.tsx` — **Cosmetic**: Same — Keycloak handles the actual reset flow

Neither stub blocks this plan's objectives (Display Board and Feed Subscription).

## Issues Encountered

- Playwright glob route pattern `**/locations**` intercepted Vite's JS module requests containing "locations" in the path (e.g. `/@fs/.../locations.ts`), preventing React from mounting. Debugged by adding a console-logging Playwright test to detect the MIME-type error. Fixed by switching to a specific regex.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Display Board and Feed Subscription pages are complete and E2E-tested
- The `/feeds` route now renders the real Feed Subscription UI (FeedsLanding stub replaced)
- All public routes (no auth required) are functional
- `feedsApi` updated with correct URL builders for all three feed formats

---
*Phase: 09-core-user-interface*
*Completed: 2026-10-13*

## Self-Check: PASSED

- `frontend/src/pages/DisplayBoard.tsx` — FOUND
- `frontend/src/pages/FeedSubscription.tsx` — FOUND
- `frontend/src/components/display/ApprovedOnlyBadge.tsx` — FOUND
- `frontend/e2e/display-board.spec.ts` — FOUND
- `frontend/e2e/feed-subscription.spec.ts` — FOUND
- Commits `49aad74`, `86b9e8d`, `8c3bddf` — FOUND
- Build check: `npm run build` → exit 0 (✓ 164 modules transformed, dist/ generated)
- Unit tests: 74/74 passing (vitest run)
- E2E tests: 18/18 passing (display-board.spec.ts 8/8, feed-subscription.spec.ts 10/10)
