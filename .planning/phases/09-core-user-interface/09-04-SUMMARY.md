---
phase: 09-core-user-interface
plan: 04
subsystem: ui
tags: [css-tokens, design-system, react-components, accessibility, typescript]

# Dependency graph
requires:
  - phase: 02-platform-foundation-infrastructure
    provides: React + TypeScript SPA (F12.1)
provides:
  - CSS design tokens (colors, spacing, typography, shadows, focus-ring)
  - Button component (primary/secondary/danger/ghost variants, loading state)
  - Input, Select, Textarea form field components with validation error states
  - Form wrapper with accessible error summary banner
  - Card layout container with header/body/footer slots
  - Badge with status variants + BadgeLocation for location colour-coding (UI-CAL-04)
  - Modal with React portal, fixed header/footer, scrollable body, Escape + click-outside close
  - Toast notification system (ToastProvider + useToast hook + standalone Toast)
affects:
  - UI-CAL-01/02/03 (calendar views using Card, Badge, Button)
  - UI-BOOK-01 (booking forms using Input, Select, Textarea, Form)
  - UI-APPR-01 (approvals queue using Button, Badge)
  - UI-ADMIN-01-07 (admin screens using all components)

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "CSS custom properties (design tokens) as single source of truth for all colours, spacing, typography"
    - "form-field pattern: label + input/select/textarea + error/hint slots, consistent across Input/Select/Textarea"
    - "ARIA-first form validation: aria-invalid, aria-describedby, role=alert on error messages"
    - "React portal for body-level rendering (Modal, Toast container)"
    - "BadgeLocation with --location-color CSS custom property for UI-CAL-04 location colour-coding"

key-files:
  created:
    - frontend/src/styles/tokens.css
    - frontend/src/styles/layout.css
    - frontend/src/styles/components.css
    - frontend/src/components/Button.tsx
    - frontend/src/components/Input.tsx
    - frontend/src/components/Select.tsx
    - frontend/src/components/Textarea.tsx
    - frontend/src/components/Form.tsx
    - frontend/src/components/Card.tsx
    - frontend/src/components/Badge.tsx
    - frontend/src/components/Modal.tsx
    - frontend/src/components/Toast.tsx
  modified:
    - frontend/src/index.css

key-decisions:
  - "All component styles use CSS custom properties from tokens.css; no raw colour/spacing values in component files"
  - "BadgeLocation introduces --location-color CSS custom property for UI-CAL-04 location colour-coding"
  - "Modal uses React portal to document.body for correct stacking context; Escape key + click-outside-to-close baked in"
  - "Toast uses ToastProvider + useToast hook pattern (not a singleton global); standalone Toast also exported for direct use"
  - "verbatimModuleSyntax compliance: ReactNode imported as type-only; pre-existing vite.config.ts tsc error is out-of-scope"

patterns-established:
  - "form-field pattern: label + control (input/select/textarea) + error (role=alert) + hint, consistent props (label, error, hint, required)"
  - "Focus accessibility: all interactive elements use --focus-ring CSS token via :focus-visible"
  - "Card: title/children/actions props for simple usage; CardHeader/CardBody/CardFooter sub-components for richer composition"
  - "Badge: variant prop for status colours; BadgeLocation for data-driven location colours via CSS custom property"

# Metrics
duration: 5min
completed: 2026-10-13
---

# Phase 9 Plan 04: Design System and Reusable UI Primitives Summary

**CSS design token system with 13 reusable components (Button, Input, Select, Textarea, Form, Card, Badge, BadgeLocation, Modal, Toast, and sub-components) — all styled via tokens.css custom properties with accessible ARIA attributes and keyboard navigation support**

## Performance

- **Duration:** 5 min
- **Started:** 2026-10-13T17:27:08Z
- **Completed:** 2026-10-13T17:32:08Z
- **Tasks:** 3
- **Files modified:** 13

## Accomplishments

- CSS design token system in `tokens.css`: colours (primary/success/warning/danger), spacing (8px grid), typography, shadows, z-index scale, focus rings
- Responsive layout utilities in `layout.css`: app shell layout, page header, grid system, flex utilities, breakpoints for UI-RESP-01/02/03
- 5 form components (Button, Input, Select, Textarea, Form): consistent label/error/hint pattern, ARIA validation attributes, loading/disabled states
- 4 layout primitives (Card, Badge, Modal, Toast): React portal rendering, location colour-coding for UI-CAL-04, auto-dismiss toast with provider + hook

## Task Commits

Each task was committed atomically:

1. **Task 1: CSS design tokens and base styles** - `14dd757` (feat)
2. **Task 2: Form components (Button, Input, Select, Textarea, Form wrapper)** - `726d3f1` (feat)
3. **Task 3: Layout primitives (Card, Badge, Modal, Toast)** - `6a767da` (feat)

## Files Created/Modified

- `frontend/src/styles/tokens.css` — CSS custom properties for design tokens (colours, spacing, typography, layout, focus, z-index)
- `frontend/src/styles/layout.css` — responsive grid/flex utilities, app shell layout, page header, breakpoints
- `frontend/src/styles/components.css` — component styles: Button, form fields, Card, Badge, Modal, Toast
- `frontend/src/index.css` — imports design system (@import tokens, layout, components), reset/normalize, base typography
- `frontend/src/components/Button.tsx` — primary/secondary/danger/ghost variants, small/medium/large sizes, loading spinner
- `frontend/src/components/Input.tsx` — label, error state (aria-invalid + aria-describedby), hint text, required marker
- `frontend/src/components/Select.tsx` — label, error, placeholder option, custom CSS chevron
- `frontend/src/components/Textarea.tsx` — label, error, hint, vertical-only resize
- `frontend/src/components/Form.tsx` — error summary banner with role=alert for screen-reader accessibility
- `frontend/src/components/Card.tsx` — header/body/footer slots; CardHeader/CardBody/CardFooter sub-components
- `frontend/src/components/Badge.tsx` — default/success/warning/danger/primary variants; BadgeLocation for UI-CAL-04
- `frontend/src/components/Modal.tsx` — React portal, fixed header + scrollable body + fixed footer, Escape key + click-outside close, ARIA dialog
- `frontend/src/components/Toast.tsx` — ToastProvider + useToast hook; standalone Toast; auto-dismiss with cleanup

## Decisions Made

- All component styles use CSS custom properties from `tokens.css` — no raw values in component files
- `BadgeLocation` introduces `--location-color` CSS custom property set via inline style from `locations.colour` field, implementing UI-CAL-04
- Modal uses React portal to `document.body` for correct stacking context above all app content
- Toast uses `ToastProvider + useToast` pattern (not a singleton global) with portal-mounted container; standalone `Toast` also exported
- Focus accessibility baked into all interactive elements via CSS `--focus-ring` token on `:focus-visible`

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Cleared stale tsc build cache before final build**
- **Found during:** Overall build verification (post Task 3)
- **Issue:** `tsc -b` used a stale `.tsbuildinfo` file that reported errors no longer present in the source (the write tool had already produced correct `import type` syntax)
- **Fix:** Deleted `node_modules/.tmp/tsconfig.app.tsbuildinfo` to force a clean build; build passed immediately with no changes to component source
- **Files modified:** None (cache file only, not committed)
- **Verification:** `npm run build` → `✓ built in 71ms`, `tsc --noEmit` → no errors

---

**Total deviations:** 1 auto-fixed (1 bug/environment)
**Impact on plan:** Cosmetic — stale cache, not a code defect. No scope creep.

## Known Stubs

None found — all components implement complete logic with no hardcoded/static data or placeholder implementations.

## Issues Encountered

- Pre-existing `vite.config.ts` TypeScript error (`test` property not in `UserConfigExport` type without `@vitest/plugin`) — pre-dates this plan, out of scope per deviation rules scope boundary. Out-of-scope finding logged here for completeness; not fixed.

## User Setup Required

None — no external service configuration required.

## Next Phase Readiness

- Design system is complete and immediately usable by all subsequent plans in Phase 9
- All components export TypeScript interfaces for type-safe usage
- `BadgeLocation` is ready for UI-CAL-04 location colour rendering
- Form components (Input, Select, Textarea, Form) are ready for UI-BOOK-01 booking forms
- Modal is ready for all modal dialogs across admin and booking screens
- Toast system requires `<ToastProvider>` to be added to the app root (`App.tsx`) — downstream plans can do this when they first use `useToast()`

---
*Phase: 09-core-user-interface*
*Completed: 2026-10-13*

## Self-Check: PASSED

- [x] `frontend/src/styles/tokens.css` — FOUND
- [x] `frontend/src/styles/layout.css` — FOUND
- [x] `frontend/src/styles/components.css` — FOUND
- [x] `frontend/src/components/Button.tsx` — FOUND
- [x] `frontend/src/components/Input.tsx` — FOUND
- [x] `frontend/src/components/Select.tsx` — FOUND
- [x] `frontend/src/components/Textarea.tsx` — FOUND
- [x] `frontend/src/components/Form.tsx` — FOUND
- [x] `frontend/src/components/Card.tsx` — FOUND
- [x] `frontend/src/components/Badge.tsx` — FOUND
- [x] `frontend/src/components/Modal.tsx` — FOUND
- [x] `frontend/src/components/Toast.tsx` — FOUND
- [x] Commits 14dd757, 726d3f1, 6a767da — FOUND in git log
- [x] Build check: `npm run build` → exit 0 (✓ built in 71ms)
- [x] Known Stubs section: None found
