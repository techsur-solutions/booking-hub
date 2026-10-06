# UX Mockup

**Project:** Booking-Hub
**Generated:** 2026-10-06
**Based on:** UserStories-BookingHub.md, JOURNEYS-BookingHub.md, PRD-BookingHub.md, FRD-BookingHub.md, PROJECT.md

---

## Overview

Booking-Hub's frontend is a React + TypeScript single-page application replacing a Bootstrap 3/jQuery/FullCalendar.js legacy UI, with **feature parity as the explicit design constraint** — this is not a visual refresh brief, it is a trust-rebuilding brief. The cross-journey pattern analysis in `JOURNEYS-BookingHub.md` identified one dominant friction theme across all four personas: *"did this action actually take effect?"* (save confirmation, approval delivery, settings propagation, display refresh). Every screen in this document is designed around resolving that trust gap explicitly, rather than assuming a generic "good UX" will solve it.

**Design principles:**

1. **Explicit state confirmation over implicit success.** Every mutating action (save, approve, deny, settings change, delete) surfaces a visible, timestamped confirmation — never a silent 200 OK. This directly answers the #1 cross-journey pain point (JRN-01.1 Submit/Confirm stages, JRN-03.2 Confirm Propagation stage).
2. **Boundary/scope decisions surfaced before, not after.** Recurring-edit scope (US-1.2, US-1.6), conflict hard-block vs. warning (US-2.1), and auto-approve boundary (US-3.4) are all surfaced as explicit choices or status badges *before* an action completes, per the "Shared Opportunities" pattern in JOURNEYS-BookingHub.md.
3. **Conflict visibility is identical everywhere.** Calendar, day, list, and approval-queue views render the same `conflict_flags[]` using the same visual language (US-2.2) — there is exactly one conflict-evaluation code path and exactly one conflict-badge component.
4. **Approved-only means approved-only, visibly.** The display board and all public feed formats show a hard visual/data guarantee that only `status=approved` bookings ever appear (US-9.1, US-9.4) — this is reinforced in the UI copy, not just the API contract.
5. **Admin screens are single-pass, not multi-screen scavenger hunts.** Per JRN-03.1, Priya's location-onboarding success measure is "under 5 minutes, one form" — admin CRUD screens favor single-page forms with inline validation over multi-step wizards, replacing the legacy "juggle multiple screens" pain point.
6. **Deny-by-default permission visibility.** Any screen/action gated by an unconfirmed or restrictive permission flag is simply absent from the UI for a user who lacks it (not shown-but-disabled with a confusing tooltip), consistent with FRD F7's "deny by default" rule.

**Frontend stack context:** React + TypeScript SPA, communicating exclusively through Spring Cloud Gateway (no direct service calls). Calendar/Day/List views are three rendering modes of one FullCalendar-equivalent component (e.g., FullCalendar React wrapper or an equivalent scheduling grid library) sharing one data-fetching hook so conflict flags and location colour-coding (US-4.4) are guaranteed consistent across modes, satisfying US-1.5's acceptance criterion that "views render consistently regardless of whether accessed via calendar or list entry point."

---

## Navigation Map

<!-- The single source of truth for how every screen is REACHED. The planner derives
     nav-wiring tasks from this table; verify-work flags any built route missing from
     the running app's link graph as an orphan gap. -->

| Screen | Route | Reached from | Nav element |
|--------|-------|--------------|-------------|
| Login | `/login` | App entry (unauthenticated) | Redirect target for any protected route accessed without a valid token |
| Calendar View | `/bookings?view=month` | App shell | Sidebar: "Calendar" (default landing page post-login) |
| Day View | `/bookings?view=day` | Calendar View | View-switcher tab: "Day" (within Bookings Workspace header) |
| List View | `/bookings?view=list` | Calendar View | View-switcher tab: "List" (within Bookings Workspace header) |
| Booking Create Form | `/bookings/new` | Calendar / Day / List View | Header button: "+ New Booking"; or click an empty calendar slot |
| Booking Detail Modal | `/bookings/:id` | Calendar / Day / List View / Approval Queue | Click any booking event/row |
| Booking Edit Form | `/bookings/:id/edit` | Booking Detail Modal | Button: "Edit" (owner or admin only) |
| Approval Queue | `/approvals` | App shell | Sidebar: "Approvals" (visible only to users holding `allowApproveBooking`) |
| Locations Admin | `/admin/locations` | App shell | Sidebar: Admin section > "Locations" |
| Resources Admin | `/admin/resources` | App shell | Sidebar: Admin section > "Resources" |
| Custom Field Builder | `/admin/custom-fields` | App shell | Sidebar: Admin section > "Custom Fields" |
| Users Admin | `/admin/users` | App shell | Sidebar: Admin section > "Users" |
| Role & Permission Matrix | `/admin/roles` | App shell / Users Admin | Sidebar: Admin section > "Roles & Permissions"; or Users Admin row action "View role" |
| Settings Admin | `/admin/settings` | App shell | Sidebar: Admin section > "Settings" |
| Audit Log Viewer | `/admin/audit-log` | App shell / Booking Detail Modal | Sidebar: Admin section > "Audit Log"; or Booking Detail Modal link "View history" |
| My Account | `/account` | App shell | Header: user avatar menu > "My Account" |
| Password Reset Request | `/password-reset/request` | Login Screen | Link: "Forgot password?" |
| Password Reset Complete | `/password-reset/complete?token=...` | Password Reset Request (via emailed link) | Emailed reset link (token embedded in URL) |
| Display Board | `/display-board` | Settings Admin | Button: "Preview Display Board" (Display & Feeds panel) |
| Feed Subscription | `/feeds` | Display Board / App shell / Settings Admin | Display Board footer link: "Subscribe to this calendar"; Sidebar: "Feeds"; Settings Admin "Copy feed links" |

**Invariant check:** every screen above has at least one inbound path traceable to the app shell (sidebar/header) or a reachable parent screen. The Display Board and Feed Subscription pages are public/unauthenticated but are still reachable from an authenticated admin surface (Settings Admin) for setup/verification purposes, in addition to being directly bookmarked on kiosk hardware for day-to-day use (Jordan's JRN-04.1 "Glance" stage) — the kiosk bookmark is an operational deployment detail, not the only discovery path, so the invariant holds.

---
