# Booking-Hub

## What This Is

Booking-Hub is a modernized rewrite of the open-source **OxAlto RoomBooking System** (https://github.com/neokoenig/RoomBooking) — a web-based room/resource booking calendar currently built on ColdFusion (cfWheels), Bootstrap 3, jQuery, and FullCalendar.js. The legacy app lets organizations manage bookable locations/resources, submit and approve bookings, enforce conflict rules, notify stakeholders by email, expose read-only feeds (RSS/iCal/JSON/digital-signage display), support custom fields per booking, and control access via a role/permission system.

Booking-Hub re-platforms this functionality — with **zero feature loss** — onto a modern microservice stack: React + TypeScript frontend, Java 21 + Spring Boot 3 backend services, one PostgreSQL database per service, RabbitMQ for inter-service events, Keycloak for authentication/authorization, Spring Cloud Gateway as the API gateway, and Docker/Kubernetes for deployment.

## Core Value

Every existing feature, business rule, validation, and workflow in the legacy RoomBooking system must exist and behave equivalently in the new system — verified by tests, not assumed. Where legacy behavior is ambiguous or undocumented, it is raised as an explicit open question rather than guessed at and silently reinterpreted.

## Requirements

### Validated

(None yet — ship to validate)

### Active

- [ ] **Legacy functional audit** — Analyze the RoomBooking repository end-to-end (every controller, model, view, route, event/scheduled handler, email template, and permission rule) and produce a documented inventory of existing functionality BEFORE any new code is written. Known legacy surface area to audit:
  - Controllers: `Api` (RSS2/iCal/JSON feeds, digital signage display board), `Bookings` (core event CRUD, list/day/calendar views, approve/deny, conflict check), `Customfields` (custom field + field-template management), `Eventdata` (AJAX event detail/modal endpoints), `Locations` (bookable rooms/spaces), `Logfiles` (audit log viewing/filtering), `PasswordResets`, `Permissions` (role/permission matrix), `Resources` (bookable equipment/resources attached to events), `Sessions` (login/logout/"remember me"/access-denied), `Settings` (system-wide configuration), `Users` (accounts, roles, self-service "my account")
  - Models: `Event` (booking: status workflow pending/approved, date validation, auto-approve setting, system fields), `Eventresource` (join between events and resources), `Location`, `Resource`, `Customfield`/`Customfieldjoin`/`Customfieldvalue`, `User` (auth, password hashing/salt, validation rules), `Settings`
  - Cross-cutting: booking conflict/overlap detection, approval workflow driven by a global "approveBooking" setting, email notifications (booking created/approved/denied, password reset), RSS2/iCal/JSON public feeds with optional per-location filtering, custom fields attachable to bookings via a field-picker, full permission matrix gating nearly every controller action, activity/change logging (`Logfiles`)
  - Output: a functional spec (or FRD-equivalent) covering every workflow, validation rule, and permission rule — gaps/unclear behavior go into an explicit Open Questions list, never silently resolved
- [ ] **Booking management service** — create/edit/delete/clone bookings, date/time validation (end not before start, default 1-hour duration), recurring/repeat bookings, multi-resource bookings, status workflow (pending → approved/denied) gated by a configurable auto-approve setting
- [ ] **Conflict detection** — prevent/flag overlapping bookings per location/resource, surfaced in both calendar and list views
- [ ] **Approval workflow** — approve/deny actions restricted by permission, notifies relevant parties
- [ ] **Locations & resources management** — CRUD for bookable locations (name, CSS class/colour for calendar display, building, layouts) and bookable resources attached to events
- [ ] **Custom fields** — admin-defined custom fields (with templates) attachable to bookings, rendered dynamically on booking forms
- [ ] **User & role management** — accounts, authentication, self-service profile/password management, role assignment
- [ ] **Permission system** — granular permission matrix (e.g., accessCalendar, allowRoomBooking, viewRoomBooking, allowApproveBooking, accessPermissions, allowAPI) gating controller actions, re-implemented via Keycloak roles/scopes
- [ ] **Notifications** — email notifications for booking lifecycle events (created, approved, denied) and password reset, re-implemented as an event-driven notification service consuming RabbitMQ events
- [ ] **Public feeds** — RSS2, iCal, JSON/API, and a digital-signage "display board" view of approved upcoming bookings, optionally filtered by location
- [ ] **Settings** — system-wide configuration (approval requirement, calendar slot size/min/max time, etc.)
- [ ] **Activity/audit logging** — equivalent of legacy `Logfiles` — who changed what, when
- [ ] **Microservice architecture** — React+TypeScript frontend; Java 21 + Spring Boot 3 backend split into bounded-context services; one PostgreSQL schema/database per service; RabbitMQ for cross-service events (e.g., booking-created → notification service); Keycloak for auth; Spring Cloud Gateway as the single entry point; Docker images and Kubernetes manifests for deployment
- [ ] **Regression verification** — automated tests mapping every legacy feature/business rule to a corresponding test in the new system (traceability from audit → requirements → tests)

### Out of Scope

- Preserving the ColdFusion/cfWheels codebase or runtime — this is a full re-platform, not a lift-and-shift
- Preserving exact legacy UI (Bootstrap 3/jQuery/FullCalendar.js look-and-feel) — React/TypeScript UI should cover the same user-facing capabilities, not pixel-match the old screens, unless the user says otherwise
- New features beyond what the legacy system does — scope is feature parity first; enhancements are explicitly future work

## Context

- **Legacy system**: OxAlto RoomBooking System, open source (Apache 2.0), by Tom King (neokoenig). ColdFusion on Wheels (cfWheels MVC framework) + Bootstrap 3 + jQuery + FullCalendar.js + MomentJS. ~307 files; `controllers/`, `models/`, `views/`, `events/` (CF application lifecycle hooks: onApplicationStart/End, onSessionStart/End, onRequestStart/End, onError, onMaintenance, onMissingTemplate), `config/routes.cfm` (custom + default REST-ish routes), per-environment settings (development/production/testing/design/maintenance).
- **Known legacy domain model** (from initial scan, to be confirmed/completed during the audit phase): `Event` (booking) belongsTo `Location`, hasMany `Eventresource` (join to `Resource`); `Customfield` hasMany `Customfieldjoin`/`Customfieldvalue`; `User` with salted/hashed password and validation rules; global `Settings` singleton controlling approval workflow and calendar display parameters.
- **This is a from-scratch rebuild** in this repository — no existing code/dependencies here yet. The legacy app is a reference/specification source only, accessed via its public GitHub repository, not a codebase to branch from.
- Demo of the legacy app: roombooking.oxalto.co.uk (per legacy README); legacy docs at roombooking.readme.io.

## Constraints

- **Tech stack (backend)**: Java 21, Spring Boot 3, Spring Cloud Gateway (API gateway) — mandated by user
- **Tech stack (frontend)**: React + TypeScript — mandated by user
- **Tech stack (data)**: PostgreSQL, one database per microservice (no shared schema across services) — mandated by user
- **Tech stack (messaging)**: RabbitMQ for inter-service events — mandated by user
- **Tech stack (auth)**: Keycloak for authentication/authorization — mandated by user
- **Deployment**: Docker + Kubernetes — mandated by user
- **Functional parity**: No functionality may be lost versus the legacy system; every feature/rule/workflow must be verified with tests — hard requirement from user
- **Ambiguity handling**: Anything unclear in legacy behavior must be listed as an open question, never guessed — hard requirement from user

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Full microservice re-platform (not incremental strangler-fig on the CF codebase) | User mandated a modern stack (Java/Spring Boot, React/TS, Postgres-per-service, RabbitMQ, Keycloak, Gateway, Docker/K8s) with no mention of preserving the CF runtime | — Pending |
| Legacy functional audit as an explicit first-class deliverable before any build work | Strict no-functionality-lost requirement means the spec must be derived from the real legacy behavior, not assumptions | — Pending |
| Database per service (not a shared Postgres instance/schema) | Explicit user requirement, aligns with microservice bounded-context isolation | — Pending |

---
*Last updated: 2026-10-06 after initialization*
