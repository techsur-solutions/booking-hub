# Requirements: Booking-Hub

**Defined:** 2026-10-06
**Core Value:** Every existing feature, business rule, validation, and workflow in the legacy RoomBooking system must exist and behave equivalently in the new system — verified by tests, not assumed.

**Source:** Derived from `project_specs/PRD-BookingHub.md` (F0–F13), `project_specs/FRD-BookingHub.md`, and `project_specs/UserStories-BookingHub.md`. Original feature IDs (F0–F13) are preserved; sub-requirements use `F{n}.{m}` to keep each item specific and testable per F13's audit→requirement→test traceability mandate. All 14 features are rated P0/P1 in the PRD — this release has no P2/P3 scope, reflecting the hard feature-parity mandate (full legacy replacement, not a product with optional differentiators).

## v1 Requirements

### Foundation & Discovery

- [ ] **F0**: A documented, end-to-end functional audit of the legacy RoomBooking repository exists before any service implementation begins
  - [ ] **F0.1**: Every controller, model, view, route, and CF application-lifecycle event handler in the legacy repo is reviewed and its behavior documented
  - [ ] **F0.2**: Every workflow, validation rule, and permission rule is captured in a structured functional spec (FRD-equivalent)
  - [ ] **F0.3**: Every legacy behavior that is ambiguous, undocumented, or unconfirmable is recorded as an explicit Open Question — never silently resolved or guessed
  - [ ] **F0.4**: Per-environment settings behavior (development/production/testing/design/maintenance) is included in the audited surface area
  - [ ] **F0.5**: The audit output is the direct input to the F13 traceability matrix (audit finding → requirement → test)

- [ ] **F12**: The microservice platform substrate exists and every other feature is built on it
  - [ ] **F12.1**: A React + TypeScript single-page frontend covers all user-facing screens (booking views, admin CRUD, public feeds, display board)
  - [ ] **F12.2**: Backend is implemented as Java 21 + Spring Boot 3 services split along bounded contexts confirmed by F0
  - [ ] **F12.3**: Each service owns exactly one PostgreSQL database/schema; no service directly queries another service's database or shares a schema
  - [ ] **F12.4**: Cross-service domain events (`booking.created`, `booking.approved`, `booking.denied`, `password.reset.requested`, audit-relevant change events) flow through RabbitMQ
  - [ ] **F12.5**: All authentication/authorization flows through Keycloak; no service implements its own password storage or ad-hoc session mechanism
  - [ ] **F12.6**: All external client traffic routes through Spring Cloud Gateway; backend services are not directly externally addressable
  - [ ] **F12.7**: Every service is independently buildable as a Docker image and independently deployable/scalable via Kubernetes manifests

### Core Booking

- [ ] **F1**: User can create, edit, delete, and clone bookings with the full legacy `Event` field set (title, location, times, resources, custom field values, system fields)
  - [ ] **F1.1**: End time must be strictly after start time; a zero-duration booking (end == start) is rejected
  - [ ] **F1.2**: If end time is unspecified, it defaults to 1 hour after start time
  - [ ] **F1.3**: Recurring/repeat bookings can be created as a series; edit/delete scoping (single occurrence vs. whole series) matches confirmed legacy behavior (flagged in F0 Open Question #1 pending confirmation)
  - [ ] **F1.4**: A single booking can be associated with multiple bookable resources
  - [ ] **F1.5**: New bookings enter `pending` or `approved` status depending on the global auto-approve setting (F10)
  - [ ] **F1.6**: Calendar, day, and list views of bookings are available, equivalent to legacy `Bookings` controller views
  - [ ] **F1.7**: AJAX-style event detail/modal retrieval is available, equivalent to legacy `Eventdata` controller

- [ ] **F2**: Overlapping bookings for the same location or resource are detected and surfaced
  - [ ] **F2.1**: Overlap-checking is evaluated per location and per resource at booking creation and edit time
  - [ ] **F2.2**: Conflicts are surfaced inline in calendar view (visual indication) and list view (flagged rows)
  - [ ] **F2.3**: A detected conflict is a hard block (booking cannot be saved) for users without `allowApproveBooking`
  - [ ] **F2.4**: A user holding `allowApproveBooking` may save a conflicting booking anyway, with the conflict surfaced as an informational flag
  - [ ] **F2.5**: Conflict detection is re-evaluated on every booking edit, not only at creation

- [ ] **F3**: Pending bookings can be approved or denied by permitted users, with relevant parties notified
  - [ ] **F3.1**: Approve/deny actions are gated by the `allowApproveBooking`-equivalent permission
  - [ ] **F3.2**: When the global `approveBooking` setting is disabled, bookings auto-approve on creation; when enabled, bookings require explicit approval
  - [ ] **F3.3**: Approval/denial publishes a `booking.approved`/`booking.denied` domain event consumed by the Notification Service
  - [ ] **F3.4**: Approval/denial actions are recorded in the audit log

### Reference Data & Extensibility

- [ ] **F4**: Admins can manage bookable locations and resources
  - [ ] **F4.1**: Location CRUD supports name, CSS class/colour for calendar display, building grouping, and layout metadata
  - [ ] **F4.2**: Resource CRUD supports bookable equipment/resources attachable to bookings independently of location
  - [ ] **F4.3**: Locations and resources are available to the Booking Service for conflict-detection scoping and calendar rendering
  - [ ] **F4.4**: Location/resource management is admin-only, gated by permission

- [ ] **F5**: Admins can define custom fields that attach to bookings
  - [ ] **F5.1**: Admin can manage custom field definitions and field templates
  - [ ] **F5.2**: Custom fields attach to bookings via a join model, with per-booking values stored separately from field definitions
  - [ ] **F5.3**: Applicable custom fields render dynamically on the booking creation/edit form via a field-picker
  - [ ] **F5.4**: Custom field values display alongside standard booking fields in calendar/list/detail views and, where applicable, public feeds

### Identity & Access Control

- [ ] **F6**: Users can authenticate and self-manage their account
  - [ ] **F6.1**: Admin can create/manage user accounts
  - [ ] **F6.2**: User can self-service edit their profile and change their password ("my account")
  - [ ] **F6.3**: User can request a password reset (request → emailed token/link → reset), implemented via Keycloak credential-reset mechanisms
  - [ ] **F6.4**: User can log in/out, with "remember me" session behavior, implemented via Keycloak session/token management
  - [ ] **F6.5**: Each user has one or more roles, feeding into the permission system (F7)
  - [ ] **F6.6**: Access-denied is handled equivalently to legacy `Sessions` access-denied behavior

- [ ] **F7**: A granular permission matrix gates every protected action
  - [ ] **F7.1**: The full permission set (including at minimum `accessCalendar`, `allowRoomBooking`, `viewRoomBooking`, `allowApproveBooking`, `accessPermissions`, `allowAPI`) is mapped and administrable
  - [ ] **F7.2**: Role-to-permission mapping is administrable, equivalent to legacy `Permissions` controller
  - [ ] **F7.3**: Permissions are enforced at Spring Cloud Gateway (coarse-grained route access) and within individual services (fine-grained action checks), both driven by Keycloak-issued tokens/roles/scopes
  - [ ] **F7.4**: Every legacy permission has a confirmed Keycloak-equivalent mapping (F0 output); any unclear mapping is an Open Question, not an approximation

### Messaging & Integration

- [ ] **F8**: Lifecycle events trigger email notifications via durable, event-driven delivery
  - [ ] **F8.1**: `booking.created`, `booking.approved`, `booking.denied`, and `password.reset.requested` domain events each trigger an equivalent email notification
  - [ ] **F8.2**: Notifications are delivered durably and retryably via RabbitMQ consumption, not synchronous in-request sending
  - [ ] **F8.3**: Notification recipients match confirmed legacy recipient rules (F0 Open Question #5 pending confirmation)
  - [ ] **F8.4**: Notification template content/intent is equivalent to legacy email templates

- [ ] **F9**: Approved upcoming bookings are exposed via public, read-only feeds
  - [ ] **F9.1**: RSS2 feed of approved upcoming bookings is available
  - [ ] **F9.2**: iCal (subscribable calendar) feed of approved upcoming bookings is available
  - [ ] **F9.3**: JSON/API feed of approved upcoming bookings is available
  - [ ] **F9.4**: A digital-signage "display board" auto-refreshing view of upcoming approved bookings is available
  - [ ] **F9.5**: All feed formats support optional per-location filtering
  - [ ] **F9.6**: All feed formats are fully public — no authentication or token required

### Configuration & Compliance

- [ ] **F10**: System-wide settings control approval workflow and calendar display behavior
  - [ ] **F10.1**: An `approveBooking` flag controls whether new bookings require explicit approval or auto-approve
  - [ ] **F10.2**: Calendar display configuration (slot size, minimum time, maximum time) is administrable
  - [ ] **F10.3**: Settings modification is admin-only, gated by permission
  - [ ] **F10.4**: Settings changes propagate to all dependent services (e.g., Booking Service reads `approveBooking` for status-on-create logic)

- [ ] **F11**: Every state-changing action is captured in an admin-viewable audit log
  - [ ] **F11.1**: Change events are captured across booking lifecycle (create/edit/delete/approve/deny), location/resource management, user/role changes, settings changes, and permission changes
  - [ ] **F11.2**: Admins can view and filter the audit log, equivalent to legacy `Logfiles` capability
  - [ ] **F11.3**: Audit log is implemented as a dedicated service consuming domain events (event-sourced), not direct per-service writes to a shared log table
  - [ ] **F11.4**: Captured fields (actor, timestamp, entity type/id, before/after values, action type) match confirmed legacy log completeness (F0 Open Question #8 pending confirmation)

### Quality & Verification

- [ ] **F13**: Automated tests verify every legacy feature/business rule identified by F0, with a maintained traceability matrix
  - [ ] **F13.1**: A traceability matrix links each F0 audit finding to the requirement(s) it generated and the test(s) that verify it
  - [ ] **F13.2**: Automated test coverage exists for every validation rule, workflow transition, and permission check identified in F0
  - [ ] **F13.3**: Explicit test coverage exists for conflict-detection edge cases, approval/auto-approve interactions, recurring-booking edit/delete scoping, and permission-matrix boundaries
  - [ ] **F13.4**: A CI-enforced regression suite must pass before any release
  - [ ] **F13.5**: Every F0 Open Question that gets resolved has its decision recorded and a corresponding test added

## v2 Requirements

(None — this release has no deferred scope. All 14 legacy capability areas are required for functional parity; there is no "nice to have" tier in a strict no-functionality-lost migration.)

## Out of Scope

| Feature | Reason |
|---------|--------|
| Preserving the ColdFusion/cfWheels runtime or codebase | Full re-platform mandated by user, not a lift-and-shift |
| Pixel-matching legacy UI (Bootstrap 3/jQuery/FullCalendar.js look-and-feel) | React/TypeScript UI covers the same capabilities; visual parity is not required unless the user says otherwise |
| New product features beyond legacy scope | Scope is feature parity first; enhancements are explicit future work, out of this release |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| F0 | Phase 1 — Legacy Functional Audit | Pending |
| F12 | Phase 2 — Platform Foundation & Infrastructure | Pending |
| F6 | Phase 3 — Identity & Access Control | Pending |
| F7 | Phase 3 — Identity & Access Control | Pending |
| F4 | Phase 4 — Reference Data, Extensibility & Configuration | Pending |
| F5 | Phase 4 — Reference Data, Extensibility & Configuration | Pending |
| F10 | Phase 4 — Reference Data, Extensibility & Configuration | Pending |
| F1 | Phase 5 — Core Booking & Approval Workflow | Pending |
| F2 | Phase 5 — Core Booking & Approval Workflow | Pending |
| F3 | Phase 5 — Core Booking & Approval Workflow | Pending |
| F8 | Phase 6 — Notifications & Audit Logging | Pending |
| F11 | Phase 6 — Notifications & Audit Logging | Pending |
| F9 | Phase 7 — Public Feeds | Pending |
| F13 | Phase 8 — Regression Verification & Test Traceability | Pending |

**Coverage:**
- v1 requirements: 14 top-level (F0–F13), 54 testable sub-requirements total
- Mapped to phases: 14/14 ✓
- Unmapped: 0 ✓

**Phase note:** F10 (Settings) is mapped to Phase 4 rather than alongside F9 in Phase 7, because F3's auto-approve logic has a hard functional dependency on the `approveBooking` flag at booking-creation time (TechArch §1.5) — Phase 5 (Core Booking) requires it to exist first. F9 (Public Feeds) has no such upstream dependency beyond approved-booking data and location filtering, so it remains a standalone later phase as a pure read-only projection.

---
*Requirements defined: 2026-10-06*
*Last updated: 2026-10-06 after roadmap creation — 14/14 requirements mapped to 8 phases*
