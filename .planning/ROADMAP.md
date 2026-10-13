# Roadmap: Booking-Hub v2.0 UI Implementation

## Overview

Milestone v2.0 implements the complete React + TypeScript frontend UI connecting to v1.0's backend APIs. This milestone transforms the Phase 2 SPA shell (routing + Keycloak PKCE wiring + placeholder routes) into a fully functional user-facing application with complete feature parity to the legacy OxAlto RoomBooking UI. The journey builds UI components layer by layer: foundational layout/patterns first (Phase 9), then core booking views (Phase 10), booking creation/editing (Phase 11), admin screens for reference data (Phase 12), user/permission/settings admin (Phase 13), approval workflow UI (Phase 14), public surfaces (Phase 15), and polish/testing/documentation (Phase 16).

Phase numbering continues from v1.0 (ended at Phase 8), so v2.0 runs Phases 9–16.

## Phases

**Phase Numbering:**
- Phases 9-16: v2.0 UI Implementation
- Continues from v1.0 (Phases 1-8: backend APIs)

- [ ] **Phase 9: UI Foundation & Shared Patterns** - App shell layout, navigation, reusable UI components, API client layer, permission-gated UI patterns
- [ ] **Phase 10: Core Booking Views** - Calendar/Day/List views with FullCalendar integration, conflict badge component, view state persistence
- [ ] **Phase 11: Booking Creation & Editing** - Create/edit forms, recurring series scope handling, booking detail modal, conflict detection UI
- [ ] **Phase 12: Admin Screens: Reference Data** - Locations/Resources/Custom Fields admin CRUD interfaces
- [ ] **Phase 13: Admin Screens: Users, Permissions & Settings** - Users admin, role/permission matrix viewer, system settings configuration
- [ ] **Phase 14: Approval Workflow UI** - Approval queue, approve/deny actions, auto-approve status indicators
- [ ] **Phase 15: Public Surfaces & Authentication** - Display Board, Feed Subscription UI, Login/My Account/Password Reset flows
- [ ] **Phase 16: Polish, Testing & Documentation** - Responsive layout refinement, accessibility audit, E2E tests, user documentation

## Phase Details

### Phase 9: UI Foundation & Shared Patterns
**Goal**: Establish the foundational UI architecture, navigation structure, reusable component library, and API client layer that every subsequent phase builds on, ensuring consistent patterns for permission-gated visibility, state confirmation, and conflict rendering across all screens.
**Status**: Not started
**Depends on**: v1.0 Phase 2 (SPA shell with routing + Keycloak exists)
**Requirements**: UI-NAV-01, UI-NAV-02, UI-PATTERN-01, UI-PATTERN-02, UI-PATTERN-03
**Success Criteria** (what must be TRUE):
  1. App shell with sidebar navigation, header with user menu, and logout functionality is implemented and renders correctly with placeholder content for each route from the Navigation Map.
  2. Permission-gated navigation works: users without allowApproveBooking see no "Approvals" link, users without any admin permission see no "Admin" section — elements are structurally absent, not disabled.
  3. A reusable StateConfirmationBanner component exists and can be triggered from any screen to show timestamped action confirmations that auto-dismiss after 5 seconds.
  4. A reusable ConflictBadge component exists that accepts conflict_flags[] and renders conflict icons with clickable detail, ready for use in Calendar/Day/List/Approval Queue views (Phase 10/11/14).
  5. An API client layer (hooks or service classes) wraps all v1.0 backend endpoints with proper error handling, JWT token inclusion from Keycloak context, and loading/error state management.
**Plans**: 5 plans

Plans:
- [ ] 09-01-PLAN.md — App shell layout: sidebar navigation (conditional sections based on permissions), header with user avatar menu, logout action, responsive shell structure
- [ ] 09-02-PLAN.md — API client layer: axios instance with JWT interceptor, React Query or SWR setup for data fetching, error boundary components, loading states
- [ ] 09-03-PLAN.md — Shared UI patterns: StateConfirmationBanner component, ConflictBadge component, permission-gated wrapper utilities (PermissionGate HOC/hook)
- [ ] 09-04-PLAN.md — Design system: color palette (location colour handling), typography, spacing, button/input/form components matching UX mockup design principles
- [ ] 09-05-PLAN.md — Integration: wire navigation to routes, test permission-gated visibility with mock user contexts (viewer-only, approver, admin)

### Phase 10: Core Booking Views
**Goal**: Users can view bookings in Calendar, Day, and List formats with identical conflict visibility, location colour-coding, and filter state persistence across view switches, so every user can discover bookings in their preferred mode without inconsistent data or missing conflict warnings.
**Status**: Not started
**Depends on**: Phase 9 (API client, ConflictBadge component, app shell)
**Requirements**: UI-CAL-01, UI-CAL-02, UI-CAL-03, UI-CAL-04
**Success Criteria** (what must be TRUE):
  1. Calendar View renders bookings in month/week grid using FullCalendar (or equivalent React scheduling library), with location colour-coding applied from locations.colour backend data, and conflict badges overlaid on events with conflicts.
  2. Day View renders a single-day hourly schedule using the same underlying booking data source as Calendar View, with identical conflict badge rendering and colour-coding.
  3. List View renders bookings in a filterable table with columns (Title, Location, Resources, Start/End, Status, Conflict Flags), and clicking a row opens a placeholder Booking Detail Modal (full implementation in Phase 11).
  4. Switching between Calendar/Day/List views preserves selected date/range and filter state via URL query params, and users can bookmark a specific view+date+filters URL.
  5. Clicking an empty slot in Calendar/Day opens a placeholder Booking Create Form (full implementation in Phase 11), and clicking an existing booking opens a placeholder Booking Detail Modal.
**Plans**: 4 plans

Plans:
- [ ] 10-01-PLAN.md — Calendar View: FullCalendar React integration, fetch bookings with conflict_flags[], apply location colour-coding, render ConflictBadge on events, view switcher (Month/Week)
- [ ] 10-02-PLAN.md — Day View: hourly slot layout, same data source as Calendar, same conflict rendering, slot click → create form placeholder, event click → detail modal placeholder
- [ ] 10-03-PLAN.md — List View: table with filters (date range, location, status, search query), export to CSV/JSON, row click → detail modal placeholder, same ConflictBadge rendering
- [ ] 10-04-PLAN.md — View state persistence: URL query params for view/date/filters, restore state on load, bookmarkable URLs, smooth view switching with preserved context

### Phase 11: Booking Creation & Editing
**Goal**: Users can create, edit, clone, and view detailed bookings with full validation, conflict detection UI, recurring series scope handling, and explicit state confirmation, so every booking action is unambiguous and every conflict is actionable before the save completes.
**Status**: Not started
**Depends on**: Phase 10 (Calendar/Day/List views, placeholder modals)
**Requirements**: UI-BOOK-01, UI-BOOK-02, UI-BOOK-03, UI-BOOK-04
**Success Criteria** (what must be TRUE):
  1. Booking Create Form supports all fields (title, location, resources, start/end date-time, recurring pattern), dynamically renders custom fields based on selected location context, runs real-time conflict detection on field changes, and shows inline conflict warnings distinguishing location vs. resource conflicts.
  2. Booking Edit Form detects recurring series bookings and requires explicit scope selection ("This occurrence only" vs. "Entire series") before allowing save, with no silent default, and shows a post-save summary banner confirming the scope of changes.
  3. Booking Detail Modal displays all booking fields, status badge (Pending/Approved/Denied with timestamp), conflict information (IDs, types), and permission-gated Edit/Delete/Clone actions, plus a "View history" link (routes to Audit Log filtered to this booking, Phase 13).
  4. Conflict detection UI hard-blocks non-approvers from saving conflicted bookings (save button disabled until conflict resolved) and soft-warns approvers (save enabled with confirmation dialog: "Approve despite conflict?").
  5. Successful save triggers StateConfirmationBanner showing action taken, status result (Pending/Approved per settings), and timestamp.
**Plans**: 4 plans

Plans:
- [ ] 11-01-PLAN.md — Booking Create Form: all fields, location/resource selectors, date-time pickers, recurring pattern options, custom field dynamic rendering based on context, real-time conflict detection API calls
- [ ] 11-02-PLAN.md — Booking Edit Form: detect recurring series (show "Recurring" badge), scope confirmation dialog (required before save), post-save summary banner, re-validation on every edit
- [ ] 11-03-PLAN.md — Booking Detail Modal: full field display, status badge, conflict section with IDs/types, Edit/Delete/Clone actions (permission-gated, owner-restricted), "View history" link (Phase 13 audit log integration)
- [ ] 11-04-PLAN.md — Conflict detection UI: inline conflict banner with specific conflicting booking IDs, distinguish location vs resource conflicts, hard-block for non-approvers, soft-warn for approvers (confirmation dialog)

### Phase 12: Admin Screens: Reference Data
**Goal**: Admins can manage locations, resources, and custom fields through single-page CRUD forms with inline validation and soft-delete confirmations, so onboarding a new bookable room or resource is a sub-5-minute single-form task rather than a multi-screen process.
**Status**: Not started
**Depends on**: Phase 9 (API client, permission-gated UI, app shell with admin nav)
**Requirements**: UI-ADMIN-01, UI-ADMIN-02, UI-ADMIN-03
**Success Criteria** (what must be TRUE):
  1. Locations Admin shows a table of all locations (name, colour, building, layout) with Create/Edit/Delete actions, and the Create/Edit form is a single page (not a wizard) with inline validation (name required, colour hex format).
  2. Resources Admin shows a table of all resources (name, type, description, is_unique, restrict_locations) with Create/Edit/Delete actions, and the form is a single page with inline validation.
  3. Custom Field Builder has two tabs (Field Definitions, Field Templates), and the Field Definition form validates options[] is required for select/radio/checkbox types and shows a preview of how the field renders in a booking form.
  4. Delete actions show confirmation dialogs with context-specific messaging (e.g., "Existing bookings will keep reference to this location" for soft-delete policy).
  5. All CRUD actions trigger StateConfirmationBanner showing success/failure with timestamp.
**Plans**: 3 plans

Plans:
- [ ] 12-01-PLAN.md — Locations Admin: table view with colour swatches, Create/Edit single-page form (name, colour picker, building, layout), soft-delete confirmation dialog, inline validation
- [ ] 12-02-PLAN.md — Resources Admin: table view, Create/Edit single-page form (name, type, description, is_unique checkbox, restrict_locations multi-select), soft-delete confirmation
- [ ] 12-03-PLAN.md — Custom Field Builder: tabbed UI (Field Definitions, Field Templates), definition form with field_type selector and options[] validation, template form with context_id selector, preview rendering

### Phase 13: Admin Screens: Users, Permissions & Settings
**Goal**: Admins can manage user accounts, view the permission matrix, and configure system-wide settings through single-page forms with immediate propagation indicators, so every settings change and permission assignment is verifiable at the moment it is made.
**Status**: Not started
**Depends on**: Phase 9 (API client, permission-gated UI, app shell with admin nav)
**Requirements**: UI-ADMIN-04, UI-ADMIN-05, UI-ADMIN-06, UI-ADMIN-07
**Success Criteria** (what must be TRUE):
  1. Users Admin shows a table of all users (name, email, roles, active status) with Create/Edit actions, and admins can assign roles while self-editing is restricted to name/email only (no self-role-change or self-password-change from here).
  2. Role & Permission Matrix displays a read-only grid of 17 permission flags (rows) × roles (columns) with current Keycloak mappings, and includes a "View in Keycloak Admin" link for actual editing.
  3. Settings Admin shows a single-page form (Approval Workflow, Calendar Display, Display & Feeds sections), and saving triggers StateConfirmationBanner with timestamp and an immediate propagation indicator (e.g., "approveBooking change affects new bookings immediately").
  4. Audit Log Viewer shows a filterable table (timestamp, actor, entity type/id, action, before/after) with filters (entity type, actor, date range), export to CSV, and is read-only (no edit/delete actions, immutable record indicator shown).
  5. All admin screens are absent from navigation for users without the corresponding admin permission (deny-by-default pattern from Phase 9).
**Plans**: 4 plans

Plans:
- [ ] 13-01-PLAN.md — Users Admin: table view, Create form (name, email, temporary password, role assignment), Edit form (admin vs self restrictions), Keycloak role display
- [ ] 13-02-PLAN.md — Role & Permission Matrix: read-only grid rendering 17 flags × roles, highlight deny-by-default permissions, "View in Keycloak Admin" external link
- [ ] 13-03-PLAN.md — Settings Admin: single-page form with sections (Approval, Calendar, Display), approveBooking toggle with propagation indicator, slot size/min/max time fields, "Preview Display Board" and "Copy feed links" buttons
- [ ] 13-04-PLAN.md — Audit Log Viewer: filterable table with entity type/actor/date range filters, export to CSV, immutable record indicator, deep-link integration from Booking Detail Modal (Phase 11)

### Phase 14: Approval Workflow UI
**Goal**: Users with allowApproveBooking can approve or deny pending bookings with one-way explicit confirmations and conflicted-booking override capability, so the approval queue is a zero-ambiguity decision surface and every approval/denial is recorded and confirmed.
**Status**: Not started
**Depends on**: Phase 9 (ConflictBadge, permission-gated UI), Phase 10 (booking data fetching), Phase 11 (Booking Detail Modal)
**Requirements**: UI-APPR-01, UI-APPR-02
**Success Criteria** (what must be TRUE):
  1. Approval Queue page is visible only to users with allowApproveBooking (absent from sidebar for others), and lists all status=pending bookings with inline ConflictBadge rendering (no separate click needed to see conflicts).
  2. Approve/Deny actions are one-way (no undo), trigger StateConfirmationBanner on success, and optimistically remove the row from the queue immediately.
  3. Conflicted bookings show Approve button enabled for approvers, but clicking it triggers a confirmation dialog ("Approve despite conflict with Booking #482?") before the action completes.
  4. Deny action opens an inline optional reason field before confirming the denial.
  5. Auto-approved bookings (created when approveBooking setting was off) show an "Auto-approved" chip with a tooltip linking to Settings Admin for verification.
**Plans**: 2 plans

Plans:
- [ ] 14-01-PLAN.md — Approval Queue: table view with inline ConflictBadge, Approve/Deny row actions, permission-gated visibility (sidebar + route), optimistic UI removal on action
- [ ] 14-02-PLAN.md — Approval actions: one-way transition enforcement, conflict confirmation dialog for approvers, deny reason field, auto-approved chip with Settings link, StateConfirmationBanner integration

### Phase 15: Public Surfaces & Authentication
**Goal**: Users can authenticate via Keycloak, manage their own account/password, and external viewers can access the Display Board and feed subscription URLs with zero login required, so the public surfaces have a hard-enforced approved-only guarantee visible in the UI.
**Status**: Not started
**Depends on**: Phase 9 (API client, app shell), Phase 10 (booking data rendering patterns)
**Requirements**: UI-PUB-01, UI-PUB-02, UI-AUTH-01, UI-AUTH-02, UI-AUTH-03
**Success Criteria** (what must be TRUE):
  1. Display Board renders in full-screen kiosk mode showing only status=approved bookings for today + next 7 days, auto-refreshes every 60 seconds, supports optional location filter via URL param, and shows a visible "Approved-Only" guarantee badge/watermark.
  2. Feed Subscription UI lists RSS2, iCal, JSON feed URLs with copy-to-clipboard buttons, optional location filter that updates URLs, usage instructions for each format, and a visible "No authentication required" guarantee.
  3. Login page redirects to Keycloak PKCE flow, supports "Remember me" (360-day email pre-fill cookie), and post-login redirects to intended route or default Calendar View.
  4. My Account page lets users edit their own name, view their roles (read-only), and trigger a "Change Password" action that redirects to Keycloak password change flow.
  5. Password Reset Request/Complete flows work end-to-end with identical responses for valid/invalid emails (security), expired/used token handling, and success redirect to Login with confirmation.
**Plans**: 3 plans

Plans:
- [ ] 15-01-PLAN.md — Display Board: full-screen kiosk layout, approved-only filter, today + 7 days timeline, 60-second auto-refresh, location filter URL param, "Approved-Only" watermark, footer link to Feed Subscription
- [ ] 15-02-PLAN.md — Feed Subscription UI: list RSS2/iCal/JSON URLs, copy-to-clipboard, location filter selector (updates URLs), usage instructions, preview buttons, "No auth required" guarantee
- [ ] 15-03-PLAN.md — Authentication flows: Login (Keycloak redirect, "Remember me", post-login redirect), My Account (edit name, view roles, Change Password link), Password Reset Request/Complete (token validation, success/error states)

### Phase 16: Polish, Testing & Documentation
**Goal**: The v2.0 UI is responsive across desktop/tablet/mobile, accessible to keyboard/screen-reader users, covered by E2E tests for critical flows, and documented for end users, so the application is production-ready with verified cross-device compatibility and testable functional parity.
**Status**: Not started
**Depends on**: Phases 9-15 (all UI features implemented)
**Requirements**: UI-RESP-01, UI-RESP-02, UI-RESP-03, UI-A11Y-01
**Success Criteria** (what must be TRUE):
  1. Desktop layout (>1024px) renders with always-visible sidebar, full calendar grid showing 4-5 weeks, and all admin table columns visible without horizontal scroll.
  2. Tablet layout (768px–1024px) renders with collapsible sidebar (hamburger menu), calendar showing 2-3 weeks, and admin tables scrolling horizontally if needed.
  3. Mobile layout (<768px) renders with bottom nav bar, Calendar defaulting to Day View, List View as primary booking discovery, and forms stacked vertically with full-width inputs.
  4. Accessibility audit passes: ARIA labels on all interactive elements, keyboard navigation works for all forms/actions, focus indicators visible, color contrast meets WCAG AA, and screen reader tested for login, booking creation, and approval flows.
  5. E2E tests (Playwright or Cypress) cover critical flows: login, create booking (conflict-free), create booking (conflicted, non-approver blocked), approve booking, deny booking, edit recurring booking with scope selection, and view audit log.
**Plans**: 4 plans

Plans:
- [ ] 16-01-PLAN.md — Responsive refinement: desktop layout verification, tablet collapsible sidebar + grid adjustments, mobile bottom nav + Day View default + stacked forms
- [ ] 16-02-PLAN.md — Accessibility audit: ARIA labels, keyboard nav testing, focus indicators, WCAG AA contrast check, screen reader testing (NVDA/VoiceOver)
- [ ] 16-03-PLAN.md — E2E test suite: Playwright/Cypress setup, critical flow coverage (login, booking CRUD, approval, conflict handling, recurring scope), CI integration
- [ ] 16-04-PLAN.md — User documentation: end-user guide (how to book, approve, manage locations/resources/settings), admin guide (permission setup, custom fields, audit log usage), inline help tooltips

## Progress

**Execution Order:**
Phases execute in numeric order: 9 → 10 → 11 → 12 → 13 → 14 → 15 → 16

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 9. UI Foundation & Shared Patterns | 0/5 | Not started | — |
| 10. Core Booking Views | 0/4 | Not started | — |
| 11. Booking Creation & Editing | 0/4 | Not started | — |
| 12. Admin Screens: Reference Data | 0/3 | Not started | — |
| 13. Admin Screens: Users, Permissions & Settings | 0/4 | Not started | — |
| 14. Approval Workflow UI | 0/2 | Not started | — |
| 15. Public Surfaces & Authentication | 0/3 | Not started | — |
| 16. Polish, Testing & Documentation | 0/4 | Not started | — |

---
*Roadmap created: 2026-10-13*
*Granularity: standard (8 phases, 29 plans)*
