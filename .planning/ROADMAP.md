# Roadmap: Booking-Hub v2.0 UI Implementation

## Overview

Milestone v2.0 implements the complete React + TypeScript frontend UI connecting to v1.0's backend APIs. This milestone transforms the Phase 2 SPA shell (routing + Keycloak PKCE wiring + placeholder routes) into a fully functional user-facing application with complete feature parity to the legacy OxAlto RoomBooking UI. The implementation is divided into two major phases: Core User Interface (Phase 9) covering all end-user booking workflows and public surfaces, and Admin Interface & Polish (Phase 10) covering all administrative screens, settings, and production readiness.

Phase numbering continues from v1.0 (ended at Phase 8), so v2.0 runs Phases 9–10.

## Phases

**Phase Numbering:**
- Phases 9-10: v2.0 UI Implementation  
- Continues from v1.0 (Phases 1-8: backend APIs)

- [ ] **Phase 9: Core User Interface** - App shell, navigation, API client, booking calendar/day/list views, booking create/edit/detail, approval queue, display board, authentication flows, shared UI patterns
- [ ] **Phase 10: Admin Interface & Polish** - Admin CRUD screens (locations, resources, custom fields, users, roles, settings), audit log viewer, responsive design, accessibility, E2E tests, documentation

## Phase Details

### Phase 9: Core User Interface
**Goal**: Build the complete end-user booking experience with calendar/day/list views, booking creation/editing with conflict detection, approval workflow, public display board, and authentication flows, so users can discover, create, manage, and approve bookings with full feature parity to the legacy system.

**Status**: In progress

**Depends on**: v1.0 Phase 2 (SPA shell with routing + Keycloak exists)

**Requirements**: UI-NAV-01, UI-NAV-02, UI-PATTERN-01, UI-PATTERN-02, UI-PATTERN-03, UI-CAL-01, UI-CAL-02, UI-CAL-03, UI-CAL-04, UI-BOOK-01, UI-BOOK-02, UI-BOOK-03, UI-BOOK-04, UI-APPR-01, UI-APPR-02, UI-PUB-01, UI-PUB-02, UI-AUTH-01, UI-AUTH-02, UI-AUTH-03

**Success Criteria** (what must be TRUE):
  1. **Foundation & Navigation**: App shell with sidebar navigation, permission-gated visibility (no disabled elements, structurally absent instead), reusable StateConfirmationBanner and ConflictBadge components, API client layer with JWT handling and error boundaries.
  
  2. **Booking Views**: Calendar View (FullCalendar month/week grid), Day View (hourly slots), and List View (filterable table) all render bookings with identical location colour-coding and conflict badge rendering, with view state preserved in URL params across navigation.
  
  3. **Booking CRUD**: Create Form supports all fields including dynamic custom fields, real-time conflict detection, and recurring patterns. Edit Form requires explicit scope selection for recurring series. Detail Modal shows complete booking info with permission-gated actions. Conflict UI hard-blocks non-approvers and soft-warns approvers.
  
  4. **Approval Workflow**: Approval Queue (visible only to allowApproveBooking users) lists pending bookings with inline conflict badges. Approve/Deny are one-way actions with explicit confirmation. Conflicted bookings require approval confirmation dialog.
  
  5. **Public Surfaces**: Display Board renders full-screen approved-only bookings with visible guarantee badge and auto-refresh. Feed Subscription UI lists RSS2/iCal/JSON URLs with copy buttons.
  
  6. **Authentication**: Login redirects to Keycloak PKCE flow with "Remember me". My Account allows self-editing (name/view roles/change password). Password Reset flows work end-to-end with proper token validation.

**Plans**: 18 plans

Plans:
- [ ] 09-01-PLAN.md — App shell layout: sidebar navigation with permission-gated sections, header with user menu, logout, responsive structure
- [ ] 09-02-PLAN.md — API client layer: axios with JWT interceptor, React Query/SWR setup, error boundaries, loading states
- [ ] 09-03-PLAN.md — Shared UI patterns: StateConfirmationBanner, ConflictBadge, PermissionGate utilities
- [ ] 09-04-PLAN.md — Design system: color palette, typography, spacing, reusable button/input/form components
- [ ] 09-05-PLAN.md — Calendar View: FullCalendar integration, location colour-coding, conflict badges, month/week switcher
- [ ] 09-06-PLAN.md — Day View: hourly slot layout, same data source as Calendar, slot/event click handlers
- [ ] 09-07-PLAN.md — List View: filterable table, conflict rendering, export to CSV/JSON, row click to detail modal
- [ ] 09-08-PLAN.md — View state persistence: URL query params for view/date/filters, bookmarkable URLs
- [ ] 09-09-PLAN.md — Booking Create Form: all fields, dynamic custom fields, recurring patterns, real-time conflict detection
- [ ] 09-10-PLAN.md — Booking Edit Form: recurring series scope dialog, re-validation, post-save summary
- [ ] 09-11-PLAN.md — Booking Detail Modal: full field display, status badge, Edit/Delete/Clone actions, audit link
- [ ] 09-12-PLAN.md — Conflict detection UI: inline banner, hard-block vs soft-warn, specific booking IDs
- [ ] 09-13-PLAN.md — Approval Queue: table with inline conflicts, Approve/Deny actions, optimistic UI updates
- [ ] 09-14-PLAN.md — Display Board & Feed Subscription: full-screen kiosk mode, approved-only guarantee, RSS2/iCal/JSON URLs
- [ ] 09-15-PLAN.md — Authentication flows: Login (Keycloak redirect), My Account (self-edit), Password Reset (request/complete)
- [ ] 09-16-PLAN.md — [GAP] Add Create Booking button to BookingsView for navigation (UAT #6 - major)
- [ ] 09-17-PLAN.md — [GAP] Fix location dropdown data loading in BookingCreate (UAT #6 - blocker)
- [ ] 09-18-PLAN.md — [GAP] Add test user seed data and enhance error handling for My Account (UAT #14 - blocker)

### Phase 10: Admin Interface & Polish
**Goal**: Implement all administrative screens for managing locations, resources, custom fields, users, roles, and settings, plus audit log viewer, responsive design for all screen sizes, accessibility compliance, E2E tests for critical flows, and user documentation, so the application is production-ready with full admin capabilities and verified quality.

**Status**: Not started

**Depends on**: Phase 9 (API client, permission-gated UI, app shell, shared components)

**Requirements**: UI-ADMIN-01, UI-ADMIN-02, UI-ADMIN-03, UI-ADMIN-04, UI-ADMIN-05, UI-ADMIN-06, UI-ADMIN-07, UI-RESP-01, UI-RESP-02, UI-RESP-03, UI-A11Y-01

**Success Criteria** (what must be TRUE):
  1. **Reference Data Admin**: Locations Admin (table + single-page CRUD form with colour picker), Resources Admin (table + single-page form with is_unique/restrict_locations), Custom Field Builder (tabbed UI for definitions/templates with field preview). All use soft-delete with context-aware confirmation dialogs.
  
  2. **User & Permission Admin**: Users Admin (table + Create/Edit forms with role assignment, admin vs self restrictions). Role & Permission Matrix (read-only grid of 17 flags × roles with Keycloak link). Settings Admin (single-page form with approval/calendar/display sections, immediate propagation indicator).
  
  3. **Audit Log**: Audit Log Viewer (filterable table by entity type/actor/date range, export to CSV, immutable record indicator, deep-link from Booking Detail Modal).
  
  4. **Responsive Design**: Desktop (>1024px) shows always-visible sidebar and full calendar grid. Tablet (768–1024px) uses collapsible sidebar and 2-3 week calendar. Mobile (<768px) uses bottom nav, defaults to Day View, stacked forms.
  
  5. **Accessibility & Testing**: WCAG AA compliance (ARIA labels, keyboard nav, focus indicators, color contrast). E2E tests (Playwright/Cypress) cover login, booking CRUD, conflict handling, approval, recurring scope selection. User documentation for end-users and admins.

**Plans**: 14 plans

Plans:
- [ ] 10-01-PLAN.md — Locations Admin: table view, Create/Edit single-page form, colour picker, soft-delete confirmation
- [ ] 10-02-PLAN.md — Resources Admin: table view, Create/Edit form with is_unique checkbox and restrict_locations multi-select
- [ ] 10-03-PLAN.md — Custom Field Builder: tabbed UI (definitions/templates), options validation, field preview rendering
- [ ] 10-04-PLAN.md — Users Admin: table view, Create form (name/email/password/role), Edit with admin vs self restrictions
- [ ] 10-05-PLAN.md — Role & Permission Matrix: read-only grid (17 flags × roles), deny-by-default highlights, Keycloak link
- [ ] 10-06-PLAN.md — Settings Admin: single-page form (approval/calendar/display sections), propagation indicator
- [ ] 10-07-PLAN.md — Audit Log Viewer: filterable table, entity type/actor/date filters, CSV export, immutable indicator
- [ ] 10-08-PLAN.md — Desktop responsive verification: sidebar always visible, full calendar grid, table columns fit
- [ ] 10-09-PLAN.md — Tablet responsive: collapsible sidebar (hamburger menu), 2-3 week calendar, horizontal scroll tables
- [ ] 10-10-PLAN.md — Mobile responsive: bottom nav, Day View default, stacked forms, full-width inputs
- [ ] 10-11-PLAN.md — Accessibility audit: ARIA labels, keyboard nav, focus indicators, WCAG AA contrast, screen reader testing
- [ ] 10-12-PLAN.md — E2E test suite: Playwright/Cypress setup, critical flow coverage, CI integration
- [ ] 10-13-PLAN.md — User documentation: end-user guide (booking/approval), admin guide (setup/config), inline tooltips
- [ ] 10-14-PLAN.md — Final integration & UAT: full-stack smoke test, cross-browser testing, performance optimization

## Progress

**Execution Order:**
Phases execute in numeric order: 9 → 10

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 9. Core User Interface | 0/15 | In progress | — |
| 10. Admin Interface & Polish | 0/14 | Not started | — |

---
*Roadmap created: 2026-10-13*
*Granularity: consolidated (2 phases, 29 plans)*