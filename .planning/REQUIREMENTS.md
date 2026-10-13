# Requirements: Booking-Hub v2.0 UI Implementation

**Defined:** 2026-10-13
**Milestone:** v2.0
**Core Value:** Implement complete frontend UI with feature parity to legacy RoomBooking system, connecting to v1.0 backend APIs, following UX-Mockup-BookingHub.md design principles.

**Source:** Derived from `project_specs/UX-Mockup-BookingHub.md`, building on v1.0's complete backend API implementation. All UI requirements implement existing v1.0 backend endpoints; no new backend features are in scope for this milestone.

## Design Principles (from UX Mockup)

1. **Explicit state confirmation over implicit success** — Every mutating action surfaces visible, timestamped confirmation
2. **Boundary/scope decisions surfaced before, not after** — Recurring-edit scope, conflict warnings, auto-approve status shown before action completes
3. **Conflict visibility is identical everywhere** — Calendar, day, list, and approval-queue views use same conflict-badge component
4. **Approved-only means approved-only, visibly** — Display board and feeds show hard visual guarantee of approved-only content
5. **Single-pass admin forms** — Admin CRUD screens favor single-page forms over multi-step wizards
6. **Deny-by-default permission visibility** — UI elements absent (not disabled) for users lacking required permissions

## v2.0 Requirements

### Core Booking Views

- [ ] **UI-CAL-01**: Calendar View displays bookings in month/week grid with FullCalendar integration
  - Calendar grid shows bookings with location colour-coding (from locations.colour)
  - Clicking empty slot opens Booking Create Form pre-filled with location, start/end time
  - Clicking existing booking opens Booking Detail Modal
  - View switcher tabs: Month/Week modes
  - Conflict badges visible on booking events
  
- [ ] **UI-CAL-02**: Day View displays single-day schedule with hourly slots
  - Time-slot based vertical layout (configurable slot size from settings)
  - Same location colour-coding as Calendar View
  - Same conflict badge rendering as Calendar View
  - Clicking slot opens Booking Create Form
  
- [ ] **UI-CAL-03**: List View displays bookings in filterable table format
  - Columns: Title, Location, Resources, Start/End Time, Status, Conflict Flags, Actions
  - Filter by: date range, location, status, search query
  - Same conflict badge rendering as Calendar/Day views
  - Click row opens Booking Detail Modal
  - Export functionality (CSV/JSON)
  
- [ ] **UI-CAL-04**: View state persists across navigation
  - Selected date/range preserved when switching between Calendar/Day/List
  - Filter state preserved in URL query params
  - User can bookmark specific view+date+filters

### Booking Creation & Editing

- [ ] **UI-BOOK-01**: Booking Create Form supports full booking lifecycle
  - Pre-filled from calendar slot click (location, start/end time)
  - Default end time = start + 1 hour (when not pre-filled)
  - Title, location selector, start/end date-time pickers
  - Multi-resource selector with availability hints
  - Dynamic custom fields based on location context
  - Recurring booking options (daily/weekly pattern)
  - Real-time conflict detection with inline warnings
  - Save shows explicit status confirmation (Pending/Approved based on settings)
  
- [ ] **UI-BOOK-02**: Booking Edit Form handles recurring series scope
  - Recurring series shows "Recurring" badge
  - Edit triggers Scope Confirmation Dialog ("This occurrence only" vs "Entire series")
  - Scope selection is required (no silent default)
  - Post-save summary banner confirms scope ("Only this occurrence changed")
  - Re-validation runs on every edit (not inherited from original)
  
- [ ] **UI-BOOK-03**: Booking Detail Modal shows complete booking information
  - All booking fields (title, location, resources, times, custom fields)
  - Status badge (Pending/Approved/Denied with timestamp)
  - Conflict information (conflicting booking IDs, conflict type)
  - Audit trail link ("View history" → Audit Log filtered to this booking)
  - Edit/Delete actions (permission-gated, owner-restricted)
  - Clone button (creates draft copy)
  
- [ ] **UI-BOOK-04**: Conflict detection UI provides actionable information
  - Inline conflict banner during save attempt
  - Lists specific conflicting booking IDs
  - Distinguishes location vs resource conflicts
  - For non-approvers: hard block (save disabled until resolved)
  - For approvers (allowApproveBooking): soft warning (can save with confirmation)
  - Conflict badge component reused across all views

### Approval Workflow

- [ ] **UI-APPR-01**: Approval Queue shows pending bookings for approval
  - Visible only to users with allowApproveBooking permission
  - Lists all status=pending bookings
  - Columns: Booking details, Conflict flags, Approve/Deny actions
  - Conflict badges inline (no separate click to see conflicts)
  - Approve button enabled even for conflicts (for approvers)
  - Deny action includes optional reason field
  
- [ ] **UI-APPR-02**: Approval actions show explicit one-way confirmation
  - Approve/Deny are one-way transitions (cannot be undone)
  - Toast confirmation on action ("Approved"/"Denied")
  - Row removed from queue immediately (optimistic UI)
  - Auto-approved bookings show "Auto-approved" chip with settings link
  - Conflicted approvals require confirmation dialog ("Approve despite conflict?")

### Admin Screens: Reference Data

- [ ] **UI-ADMIN-01**: Locations Admin supports full location CRUD
  - Table view: Name, Colour, Building, Layout, Actions
  - Create/Edit form: single-page (not wizard)
  - Fields: name, CSS class/colour picker, building grouping, layout metadata
  - Soft-delete confirmation ("Existing bookings will keep reference")
  - Inline validation (name required, colour hex format)
  
- [ ] **UI-ADMIN-02**: Resources Admin supports full resource CRUD
  - Table view: Name, Type, Description, Is Unique, Restrict Locations, Actions
  - Create/Edit form: single-page
  - Fields: name, type, description, is_unique checkbox, restrict_locations multi-select
  - Soft-delete confirmation
  - Inline validation (name required)
  
- [ ] **UI-ADMIN-03**: Custom Field Builder supports field definition and templates
  - Two tabs: Field Definitions, Field Templates
  - Field Definition form: name, field_type (select/textarea/textfield/radio/checkbox), options[] (for choice types)
  - Field Template form: context_id (null=global, or specific location), field associations
  - Options validation (required for select/radio/checkbox types)
  - Preview of how field renders in booking form

### Admin Screens: Users & Permissions

- [ ] **UI-ADMIN-04**: Users Admin supports user account management
  - Table view: Name, Email, Roles, Active status, Actions
  - Create form: name, email, password (temporary), role assignment
  - Edit form (self): name, email only (no password/role changes)
  - Edit form (admin): all fields including role assignment
  - Password change (self): separate action via My Account
  - Admin can view user's Keycloak roles
  
- [ ] **UI-ADMIN-05**: Role & Permission Matrix shows permission assignments
  - Matrix grid: Roles (columns) × Permissions (rows)
  - 17 permission flags from F0 audit (accessCalendar, allowRoomBooking, etc.)
  - Read-only view of current Keycloak role mappings
  - Link to "View in Keycloak Admin" for actual editing
  - Highlights deny-by-default permissions
  
- [ ] **UI-ADMIN-06**: Settings Admin controls system-wide configuration
  - Single-page form (not tabbed unless >10 settings)
  - Sections: Approval Workflow, Calendar Display, Display & Feeds
  - Approval: approveBooking toggle with immediate propagation indicator
  - Calendar: slot size, min/max time
  - Display & Feeds: "Preview Display Board" button, "Copy feed links" button
  - Save shows timestamped confirmation ("Settings updated at [timestamp]")
  - No in-memory cache (every read fetches from backend)

### Admin Screens: Audit & Logging

- [ ] **UI-ADMIN-07**: Audit Log Viewer provides filterable change history
  - Table view: Timestamp, Actor, Entity Type, Entity ID, Action, Before/After
  - Filters: entity type dropdown, actor search, date range picker
  - Read-only (no edit/delete actions)
  - Export to CSV for external analysis
  - Deep-link from Booking Detail Modal ("View history for this booking")
  - Immutable record indicator (UI shows audit entries cannot be changed)

### Public Surfaces

- [ ] **UI-PUB-01**: Display Board shows approved bookings for digital signage
  - Full-screen kiosk mode (no nav bar)
  - Auto-refresh every 60 seconds
  - Shows only status=approved bookings
  - Today + next 7 days in scrolling timeline
  - Optional location filter via URL param (?location_id=X)
  - "Approved-Only" badge/watermark visible
  - Footer link to Feed Subscription page
  
- [ ] **UI-PUB-02**: Feed Subscription UI provides feed URLs for external calendars
  - Lists RSS2, iCal, JSON feed URLs
  - Copy-to-clipboard buttons for each format
  - Optional location filter selection (updates URLs)
  - Usage instructions for each format (how to subscribe in Outlook/Google Calendar/etc.)
  - Preview button for each feed (opens in new tab)
  - "No authentication required" guarantee shown

### Authentication & Account

- [ ] **UI-AUTH-01**: Login page integrates with Keycloak PKCE flow
  - Redirects to Keycloak login
  - "Remember me" checkbox (360-day cookie for email pre-fill)
  - "Forgot password?" link to Password Reset Request
  - Post-login redirect to intended route (or default Calendar View)
  
- [ ] **UI-AUTH-02**: My Account page supports self-service profile management
  - Display: name, email (read-only for self), roles (read-only)
  - Edit: name only (email change requires admin)
  - Change Password: separate action → Keycloak password change flow
  - Link to Audit Log filtered to "actions by me"
  
- [ ] **UI-AUTH-03**: Password Reset flow supports emailed token reset
  - Request page: email field, submit sends reset link
  - Identical response for valid/invalid email (security)
  - Complete page: token from URL, new password fields
  - Expired/used token shows clear error ("Token expired, request new one")
  - Success redirects to Login with confirmation

### Navigation & Layout

- [ ] **UI-NAV-01**: App shell provides consistent navigation structure
  - Sidebar: Calendar, Approvals (conditional), Feeds, Admin section (conditional), My Account
  - Header: user avatar menu, logout
  - Admin section visible only if user has any admin permission
  - Approvals visible only if user has allowApproveBooking
  - Active route highlighted in sidebar
  
- [ ] **UI-NAV-02**: Navigation matches UX Mockup Navigation Map exactly
  - All 18 routes from Navigation Map table implemented
  - Every screen reachable from app shell or parent screen
  - Breadcrumbs for nested admin screens
  - Browser back/forward works correctly (URL state drives UI state)

### Cross-Cutting UI Patterns

- [ ] **UI-PATTERN-01**: Explicit State-Confirmation Banner pattern
  - Every mutating action (save/approve/deny/settings change) shows banner
  - Banner includes: action taken, timestamp, affected entity
  - Auto-dismiss after 5 seconds, or manual dismiss
  - Persistent banner for critical state changes (approval status)
  
- [ ] **UI-PATTERN-02**: Conflict Badge Component is reused everywhere
  - Single component instance used in: Calendar View, Day View, List View, Approval Queue, Booking Detail
  - Badge shows: conflict icon, conflicting booking ID(s), conflict type (location/resource)
  - Click opens Booking Detail Modal pre-scrolled to conflict section
  
- [ ] **UI-PATTERN-03**: Permission-gated UI elements use deny-by-default
  - Elements absent (not disabled) if user lacks permission
  - No "You don't have permission" tooltips on disabled buttons
  - Entire screens absent from nav if user has no access
  - Clear separation: structural absence vs. business-rule disablement

### Responsive & Accessibility

- [ ] **UI-RESP-01**: Desktop layout (>1024px) uses full sidebar + content area
  - Sidebar always visible
  - Calendar grid shows 4-5 weeks simultaneously
  - Admin tables show all columns
  
- [ ] **UI-RESP-02**: Tablet layout (768px–1024px) uses collapsible sidebar
  - Sidebar toggleable via hamburger menu
  - Calendar grid shows 2-3 weeks
  - Admin tables scroll horizontally if needed
  
- [ ] **UI-RESP-03**: Mobile layout (<768px) uses bottom nav + stacked content
  - Sidebar becomes bottom nav bar (4-5 primary items)
  - Calendar switches to Day View by default
  - List View becomes primary booking discovery mode
  - Forms stack vertically with full-width inputs
  
- [ ] **UI-A11Y-01**: Accessibility requirements met
  - ARIA labels on all interactive elements
  - Keyboard navigation works for all forms/actions
  - Focus indicators visible
  - Color contrast meets WCAG AA
  - Screen reader tested for critical flows (login, booking creation, approval)

## v3.0+ Requirements (Future)

- Real-time updates via WebSocket (currently: manual refresh)
- Drag-and-drop booking rescheduling in Calendar View
- Bulk approval/denial in Approval Queue
- Advanced conflict resolution UI (suggest alternative slots)
- Mobile-specific native app
- Offline mode with sync

## Out of Scope (v2.0)

| Feature | Reason |
|---------|--------|
| Pixel-matching legacy UI (Bootstrap 3 look-and-feel) | React/TypeScript UI covers same capabilities; visual refresh is acceptable per PROJECT.md |
| New features beyond legacy scope | v2.0 is UI implementation of v1.0 backend; new features are v3.0+ |
| Backend API changes | v1.0 backend is complete; v2.0 is frontend-only |
| Real-time WebSocket updates | Deferred to v3.0; v2.0 uses polling/manual refresh |
| Mobile native app | Deferred to v3.0+; v2.0 responsive web app is sufficient |

## Traceability

Requirements will be mapped to phases during roadmap creation.

| Requirement | Phase | Status |
|-------------|-------|--------|
| (To be populated by roadmapper) | | |

---
*Generated: 2026-10-13*
