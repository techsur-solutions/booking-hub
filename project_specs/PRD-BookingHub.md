# Product Requirements Document: Booking-Hub

**Project Acronym:** BookingHub
**Document Type:** PRD (Product Requirements Document)
**Status:** Draft — Pending Validation
**Last Updated:** 2026-10-06

---

## 1. Executive Summary

Booking-Hub is a full re-platform of the open-source **OxAlto RoomBooking System** (ColdFusion/cfWheels, Bootstrap 3, jQuery, FullCalendar.js) onto a modern microservice architecture: React + TypeScript frontend, Java 21 + Spring Boot 3 backend services, PostgreSQL per service, RabbitMQ for inter-service events, Keycloak for auth, and Spring Cloud Gateway as the single entry point, deployed via Docker/Kubernetes. The product lets organizations manage bookable locations and resources, submit and approve bookings with conflict detection, notify stakeholders of booking lifecycle events, and expose public read-only feeds (RSS2/iCal/JSON/digital-signage) — with **zero functional regression** versus the legacy system, verified by an explicit audit-to-test traceability process rather than assumption.

## 2. Problem Statement

The legacy RoomBooking system has served its purpose but is built on an aging, increasingly hard-to-hire-for stack (ColdFusion on Wheels, Bootstrap 3, jQuery) that limits maintainability, scalability, and integration with modern identity and infrastructure tooling. Organizations relying on it face several pain points:

- The ColdFusion/cfWheels runtime is a shrinking skill pool, making long-term maintenance and hiring difficult.
- The monolithic MVC structure (controllers/models/views in a single codebase) does not scale independently — booking volume, feed traffic, and admin operations all compete for the same application and database resources.
- Authentication/authorization is hand-rolled (salted/hashed passwords, custom permission matrix) rather than delegated to a standards-based identity provider, making it harder to integrate with enterprise SSO or extend access models.
- Email notification logic is tightly coupled to the request/response cycle of the monolith, with no durable event log or retry semantics if a notification fails.
- There is no current, authoritative specification of what the legacy system actually does — behavior is only knowable by reading ~307 files of ColdFusion code, which is a barrier to any safe modernization effort.

The core risk this project must manage is **silent feature loss during rewrite**: without a rigorous audit-first process and systematic test traceability, a rewrite of this scope will almost certainly drop edge-case business rules (e.g., exact conflict-detection semantics, auto-approve interactions, permission edge cases) that users depend on without realizing they depend on them.

## 3. Product Vision

**Vision statement:** Booking-Hub is the dependable, modern successor to RoomBooking — every capability users relied on in the legacy system works the same way (or is explicitly flagged as an open question before being reinterpreted), running on infrastructure that is horizontally scalable, independently deployable, and built on standards-based auth and messaging.

**Strategic goals:**
- Achieve complete, test-verified functional parity with the legacy RoomBooking system before any new capability is considered.
- Replace the hand-rolled auth/permission system with Keycloak-backed roles and scopes without changing the effective access-control behavior users experience today.
- Decompose the monolith into bounded-context microservices aligned to the legacy domain model (bookings, locations/resources, custom fields, users/permissions, notifications, feeds, settings, audit log) so each can scale and deploy independently.
- Establish a durable, event-driven notification backbone (RabbitMQ) so booking lifecycle events are never silently lost, replacing synchronous email-in-request-cycle behavior.
- Produce a living audit-to-requirements-to-test traceability matrix so "no functionality lost" is a continuously verifiable claim, not a one-time assertion.
- Treat ambiguous or undocumented legacy behavior as a first-class open question, resolved through explicit decision rather than silent reinterpretation.

## 4. Technical Architecture

| Layer | Technology | Purpose |
|---|---|---|
| Frontend | React + TypeScript | Single-page application covering calendar/list booking views, admin screens, public feed landing pages |
| API Gateway | Spring Cloud Gateway | Single entry point for all client traffic; routes to backend services, enforces auth at the edge |
| Backend services | Java 21 + Spring Boot 3 | Bounded-context microservices (see Feature Requirements for service boundaries) |
| Identity & Access | Keycloak | Authentication, session/token management, role/permission mapping (replaces legacy hashed-password auth + permission matrix) |
| Data persistence | PostgreSQL (one database per service) | No shared schema across services; each bounded context owns its data |
| Messaging | RabbitMQ | Asynchronous inter-service events (e.g., `booking.created` → notification service); decouples booking lifecycle from notification delivery |
| Deployment | Docker + Kubernetes | Containerized services, independently deployable and scalable |
| Observability / Audit | Dedicated audit log service (PostgreSQL-backed), consuming domain events | Equivalent of legacy `Logfiles` — who changed what, when |

**Indicative service decomposition** (bounded contexts derived from the legacy domain model — subject to confirmation during the Legacy Functional Audit, F0):
- Booking Service (Event, Eventresource)
- Location & Resource Service (Location, Resource)
- Custom Field Service (Customfield, Customfieldjoin, Customfieldvalue)
- User/Identity-adjacent Service (profile data not owned by Keycloak) + Keycloak realm (User, roles)
- Notification Service (consumes RabbitMQ events, sends email)
- Public Feed Service (RSS2/iCal/JSON/display board, read-optimized views)
- Settings Service (system-wide configuration singleton)
- Audit Log Service (Logfiles equivalent)

## 5. Feature Requirements

### F0: Legacy Functional Audit
**Description:** A comprehensive, documented inventory of every piece of existing functionality in the RoomBooking repository, produced before any new code is written. This is the specification source of truth for all downstream FRD, TechArch, and UserStories work, and the baseline against which "no functionality lost" is measured.

**Capabilities:**
- End-to-end review of every controller (`Api`, `Bookings`, `Customfields`, `Eventdata`, `Locations`, `Logfiles`, `PasswordResets`, `Permissions`, `Resources`, `Sessions`, `Settings`, `Users`), every model (`Event`, `Eventresource`, `Location`, `Resource`, `Customfield`/`Customfieldjoin`/`Customfieldvalue`, `User`, `Settings`), every view, route (`config/routes.cfm`), and CF application lifecycle event handler (`onApplicationStart/End`, `onSessionStart/End`, `onRequestStart/End`, `onError`, `onMaintenance`, `onMissingTemplate`)
- Documents every workflow, validation rule, and permission rule in a structured functional spec (FRD-equivalent)
- Produces an explicit **Open Questions** list for any legacy behavior that is ambiguous, undocumented, or cannot be confirmed from the code/docs/demo site — these are never silently resolved or guessed at
- Captures per-environment settings behavior (development/production/testing/design/maintenance) as part of the audited surface area
- Establishes the baseline inventory used to build the audit → requirements → test traceability matrix (see F13)

**Legacy Reference:** All controllers/models/views/events listed in PROJECT.md "Active Requirements"; routes.cfm; roombooking.readme.io docs; roombooking.oxalto.co.uk demo.

**Related Features:** Feeds into every other feature below; direct input to F13 (Regression Verification).

**Priority:** P0 (Critical — blocking; no build work proceeds without this deliverable)

---

### F1: Booking Management Service
**Description:** The core service for creating, editing, deleting, and cloning bookings (the legacy `Event` model), including date/time validation, recurring/repeat bookings, multi-resource bookings, and the pending/approved/denied status workflow.

**Capabilities:**
- Create/edit/delete/clone bookings with full field set equivalent to legacy `Event` (title, location, times, resources, custom field values, system fields)
- Date/time validation: end time must not precede start time; default booking duration of 1 hour when end time is unspecified
- Recurring/repeat booking support (series creation, and edit/delete scoping — single occurrence vs. whole series — to be confirmed against legacy behavior in F0 and flagged if ambiguous)
- Multi-resource bookings: a single booking can be associated with multiple bookable resources via an `Eventresource`-equivalent join
- Status workflow: new bookings enter `pending` or `approved` depending on the global auto-approve setting (see F10); transitions to `approved`/`denied` are gated by the approval workflow (F3)
- Calendar, day, and list views of bookings equivalent to legacy `Bookings` controller views
- AJAX-style event detail/modal retrieval equivalent to legacy `Eventdata` controller

**Legacy Reference:** `Bookings` controller; `Event`, `Eventresource` models; `Eventdata` controller.

**Related Features:** F2 (Conflict Detection), F3 (Approval Workflow), F4 (Locations & Resources), F5 (Custom Fields), F8 (Notifications), F11 (Audit Logging).

**Priority:** P0 (Critical — core product functionality)

---

### F2: Conflict Detection
**Description:** Detects and prevents (or flags) overlapping bookings for the same location or resource, surfaced consistently in both calendar and list views so users and approvers can see conflicts before and after booking creation.

**Capabilities:**
- Overlap-checking logic evaluated per location and per resource at booking creation/edit time
- Conflict results surfaced inline in calendar view (visual indication) and list view (flagged rows)
- Behavior when a conflict is detected (hard block vs. warn-and-allow, and whether this differs by permission level) to be confirmed during the Legacy Functional Audit (F0) and flagged as an open question if not clearly documented in legacy code
- Conflict detection re-evaluated on every booking edit, not only at creation

**Legacy Reference:** `Bookings` controller conflict-check logic; `Event`/`Eventresource` models.

**Related Features:** F1 (Booking Management), F3 (Approval Workflow).

**Priority:** P0 (Critical — prevents double-booking, a core value proposition of the product)

---

### F3: Approval Workflow
**Description:** The approve/deny workflow for pending bookings, restricted to users with the appropriate permission, with notification of relevant parties on each decision.

**Capabilities:**
- Approve and deny actions on pending bookings, gated by a permission equivalent to legacy `allowApproveBooking`
- Workflow driven by the global `approveBooking` setting (F10): when disabled, bookings auto-approve on creation; when enabled, bookings require explicit approval
- Approval/denial triggers a domain event (`booking.approved` / `booking.denied`) consumed by the Notification Service (F8)
- Approval/denial actions are recorded in the audit log (F11)

**Legacy Reference:** `Bookings` controller approve/deny actions; `Event` model status field; `Settings` model `approveBooking` flag.

**Related Features:** F1 (Booking Management), F7 (Permission System), F8 (Notifications), F10 (Settings), F11 (Audit Logging).

**Priority:** P0 (Critical — core workflow)

---

### F4: Locations & Resources Management
**Description:** CRUD management of bookable locations (rooms/spaces) and bookable resources (equipment) that can be attached to bookings.

**Capabilities:**
- Location CRUD: name, CSS class/colour used for calendar display, building grouping, and layout metadata
- Resource CRUD: bookable equipment/resources that can be attached to bookings independently of location
- Locations and resources are referenced by the Booking Service (F1) for conflict detection scoping and calendar rendering
- Admin-only access gated by permission (F7)

**Legacy Reference:** `Locations` controller; `Resources` controller; `Location`, `Resource` models.

**Related Features:** F1 (Booking Management), F2 (Conflict Detection), F7 (Permission System), F9 (Public Feeds — per-location filtering).

**Priority:** P1 (High — required for booking creation to function, but is reference/admin data rather than the core transactional flow)

---

### F5: Custom Fields
**Description:** Admin-defined custom fields, organized via templates, that can be attached to bookings and rendered dynamically on booking forms via a field-picker.

**Capabilities:**
- Admin management of custom field definitions and field templates (equivalent to legacy `Customfield` + field-template management)
- Attachment of custom fields to bookings via a join model (equivalent to `Customfieldjoin`) and storage of per-booking values (equivalent to `Customfieldvalue`)
- Dynamic rendering of applicable custom fields on the booking creation/edit form based on field-picker configuration
- Custom field values displayed alongside standard booking fields in calendar/list/detail views and, where applicable, public feeds

**Legacy Reference:** `Customfields` controller; `Customfield`, `Customfieldjoin`, `Customfieldvalue` models.

**Related Features:** F1 (Booking Management), F9 (Public Feeds).

**Priority:** P1 (High — significant legacy feature, not required for the minimal booking happy path)

---

### F6: User & Role Management
**Description:** User account management, authentication, self-service profile/password management, and role assignment — re-platformed onto Keycloak-backed identity while preserving equivalent user-facing capability.

**Capabilities:**
- Account creation/management (admin-facing) equivalent to legacy `Users` controller
- Self-service "my account" profile editing and password change, equivalent to legacy self-service account screens
- Password reset flow (request → emailed token/link → reset) equivalent to legacy `PasswordResets` controller, re-implemented using Keycloak's credential-reset mechanisms rather than legacy salted/hashed password storage
- Login/logout and "remember me" session behavior equivalent to legacy `Sessions` controller, re-implemented via Keycloak session/token management
- Role assignment per user, feeding into the Permission System (F7)
- Access-denied handling equivalent to legacy `Sessions` access-denied behavior

**Legacy Reference:** `Users` controller; `Sessions` controller; `PasswordResets` controller; `User` model (auth, password hashing/salt, validation rules).

**Related Features:** F7 (Permission System), F8 (Notifications — password reset emails), F11 (Audit Logging).

**Priority:** P0 (Critical — required for any authenticated access to the system)

---

### F7: Permission System
**Description:** The granular permission matrix that gates nearly every controller action in the legacy system, re-implemented as Keycloak roles/scopes enforced at the API Gateway and/or per-service, while preserving the same effective access-control behavior.

**Capabilities:**
- Permission matrix equivalent to legacy permissions including (non-exhaustive, to be completed by F0): `accessCalendar`, `allowRoomBooking`, `viewRoomBooking`, `allowApproveBooking`, `accessPermissions`, `allowAPI`
- Role-to-permission mapping administrable equivalent to legacy `Permissions` controller
- Enforcement at Spring Cloud Gateway (coarse-grained route access) and within individual services (fine-grained action checks), both driven by Keycloak-issued tokens/roles/scopes
- Full mapping of every legacy permission to its Keycloak equivalent is a required F0 audit output; any permission whose exact gating behavior is unclear is raised as an open question rather than approximated

**Legacy Reference:** `Permissions` controller; permission checks embedded throughout `Bookings`, `Locations`, `Resources`, `Users`, `Settings`, `Api` controllers.

**Related Features:** F3 (Approval Workflow), F6 (User & Role Management), every other feature that gates actions by permission.

**Priority:** P0 (Critical — cross-cutting; required for correct access control across the entire system)

---

### F8: Notifications
**Description:** Event-driven notification service that replaces legacy synchronous email sending with RabbitMQ-driven, durable event consumption, covering the same set of notification triggers as the legacy system.

**Capabilities:**
- Consumes domain events published by other services: `booking.created`, `booking.approved`, `booking.denied`, `password.reset.requested`
- Sends email notifications equivalent to legacy behavior for each event type, to the equivalent set of recipients (booking owner, approvers, other stakeholders — exact recipient rules to be confirmed in F0)
- Durable, retryable delivery via RabbitMQ (improves on legacy in-request email sending, without changing user-facing notification content/triggers unless legacy behavior is itself ambiguous, in which case it is flagged per F0)
- Notification templates equivalent in content/intent to legacy email templates

**Legacy Reference:** Email-sending logic embedded in `Bookings` (created/approved/denied) and `PasswordResets` controllers; legacy email templates.

**Related Features:** F1 (Booking Management), F3 (Approval Workflow), F6 (User & Role Management — password reset).

**Priority:** P1 (High — important for user awareness, but not blocking for core booking CRUD to function)

---

### F9: Public Feeds
**Description:** Read-only public feed endpoints exposing approved upcoming bookings in multiple formats, equivalent to the legacy `Api` controller, including a digital-signage "display board" view.

**Capabilities:**
- RSS2 feed of approved upcoming bookings
- iCal feed of approved upcoming bookings (subscribable calendar format)
- JSON/API feed of approved upcoming bookings (equivalent to legacy `allowAPI`-gated JSON endpoint)
- Digital-signage "display board" view: a visual, auto-refreshing screen-friendly view of upcoming approved bookings suitable for lobby/corridor displays
- Optional per-location filtering on all feed formats
- Feed access control equivalent to legacy `allowAPI` permission where the legacy feed is not fully public

**Legacy Reference:** `Api` controller (RSS2/iCal/JSON/display board).

**Related Features:** F1 (Booking Management — source data), F4 (Locations & Resources — per-location filtering), F7 (Permission System — `allowAPI`).

**Priority:** P1 (High — valuable integration/visibility surface, not required for internal booking workflow)

---

### F10: Settings
**Description:** System-wide configuration equivalent to the legacy `Settings` singleton model, controlling approval workflow behavior and calendar display parameters.

**Capabilities:**
- `approveBooking` flag: whether new bookings require explicit approval or auto-approve
- Calendar display configuration: slot size, minimum time, maximum time, and other calendar-rendering parameters equivalent to legacy settings
- Admin-only access to modify settings, gated by permission (F7)
- Settings changes take effect across all dependent services (e.g., Booking Service reads `approveBooking` for status-on-create logic) — exact propagation mechanism (shared config service vs. event-driven settings-changed broadcast) to be determined in TechArch, informed by F0 findings on how legacy settings are read/cached

**Legacy Reference:** `Settings` controller; `Settings` model.

**Related Features:** F1 (Booking Management — default status), F3 (Approval Workflow), F7 (Permission System).

**Priority:** P1 (High — required for correct approval-workflow and calendar behavior, but is configuration rather than core transactional flow)

---

### F11: Activity/Audit Logging
**Description:** System-wide audit log capturing who changed what and when, equivalent to the legacy `Logfiles` controller, with viewing and filtering capability for administrators.

**Capabilities:**
- Captures change events across booking lifecycle (create/edit/delete/approve/deny), location/resource management, user/role changes, settings changes, and permission changes
- Admin-facing log viewing with filtering equivalent to legacy `Logfiles` controller capability
- Implemented as a dedicated Audit Log Service consuming domain events from other services (event-sourced audit trail) rather than direct per-service writes to a shared log table, consistent with the database-per-service constraint
- Exact set of fields captured per legacy log entry (actor, timestamp, entity type/id, before/after values, action type) to be confirmed in F0

**Legacy Reference:** `Logfiles` controller.

**Related Features:** All features that mutate state (F1, F3, F4, F5, F6, F7, F10).

**Priority:** P1 (High — important for accountability/compliance, not blocking for core booking happy path)

---

### F12: Microservice Architecture & Platform
**Description:** The foundational platform work: React+TypeScript frontend, Java 21 + Spring Boot 3 backend decomposed into bounded-context services, one PostgreSQL database per service, RabbitMQ for cross-service events, Keycloak for auth, Spring Cloud Gateway as the single entry point, and Docker/Kubernetes for deployment. This is the architectural substrate every other feature is built on.

**Capabilities:**
- React + TypeScript single-page frontend covering all user-facing screens (calendar/list booking views, admin CRUD screens, public feed pages, display board)
- Java 21 + Spring Boot 3 services split along the bounded contexts identified in Technical Architecture (Section 4) and confirmed/refined during F0
- One PostgreSQL database/schema per service — no cross-service shared schema or direct cross-database joins
- RabbitMQ topology for domain events (`booking.created`, `booking.approved`, `booking.denied`, `password.reset.requested`, and audit-relevant change events)
- Keycloak realm/client configuration mapping legacy roles/permissions to Keycloak roles and scopes
- Spring Cloud Gateway configured as the single ingress point, handling routing and coarse-grained auth enforcement
- Docker images per service and Kubernetes manifests (deployments, services, config/secrets) for all components

**Legacy Reference:** N/A — this is new infrastructure replacing the monolithic ColdFusion/cfWheels runtime; informed by the bounded contexts discovered in F0.

**Related Features:** Underpins F1–F11.

**Priority:** P0 (Critical — nothing else can be built or deployed without this foundation)

---

### F13: Regression Verification & Test Traceability
**Description:** Automated tests mapping every legacy feature and business rule (as documented by F0) to a corresponding test in the new system, with a maintained traceability matrix from audit finding → requirement → implementation → test, so the "no functionality lost" claim is continuously and objectively verifiable.

**Capabilities:**
- Traceability matrix linking each F0 audit finding to the requirement(s) it generated (in this PRD and downstream FRD) and the test(s) that verify it
- Automated test coverage for every validation rule, workflow transition, and permission check identified in F0 (unit, integration, and end-to-end as appropriate)
- Explicit test coverage for conflict-detection edge cases, approval/auto-approve interactions, recurring-booking edit/delete scoping, and permission-matrix boundaries — the areas most likely to harbor subtle legacy behavior
- CI-enforced regression suite that must pass before any release, preventing future regressions against the parity baseline
- Open Questions (F0) that are later resolved get an explicit decision recorded and a corresponding test added, closing the loop

**Legacy Reference:** Derived from F0's full audit output.

**Related Features:** Depends on F0; verifies F1–F12.

**Priority:** P0 (Critical — this is the mechanism that makes the project's core "no functionality lost" guarantee verifiable rather than aspirational)

## 6. Non-Functional Requirements

- **Functional parity:** Zero loss of legacy functionality; every legacy feature, business rule, and workflow must exist and behave equivalently in the new system, verified by automated tests (F13), not asserted by inspection alone.
- **Ambiguity handling:** Any legacy behavior that cannot be confidently determined from code, documentation, or the public demo site must be recorded as an explicit open question (see Section 10) and resolved via an explicit decision before implementation — never silently reinterpreted.
- **Service isolation:** Each microservice owns its own PostgreSQL database/schema; no service may directly query another service's database or share a schema.
- **Event-driven decoupling:** Cross-service side effects (notifications, audit logging) must be triggered via RabbitMQ domain events, not direct synchronous service-to-service calls, so that a notification or audit failure cannot block the originating transaction (e.g., booking creation).
- **Standards-based identity:** All authentication and authorization must flow through Keycloak; no service may implement its own password storage or ad-hoc session mechanism.
- **Single ingress:** All external client traffic must route through Spring Cloud Gateway; backend services are not directly externally addressable.
- **Deployability:** Every service must be independently buildable as a Docker image and independently deployable/scalable via Kubernetes manifests, without requiring a coordinated deploy of all services.
- **Auditability:** Every state-changing action across every service must be traceable in the Audit Log Service (F11), including actor, timestamp, and the nature of the change.
- **Conflict-detection correctness:** Overlap checks must be evaluated consistently regardless of entry point (UI calendar, UI list, API) and must account for both location-level and resource-level conflicts.

## 7. Success Metrics

- **100% of F0-documented legacy features** have a corresponding implemented requirement and at least one passing automated test before general release (traceability matrix coverage = 100%).
- **Zero open P0/P1 functional-parity gaps** at release — every item in the Open Questions list (Section 10) is either resolved with an explicit decision or formally accepted as out-of-scope with sign-off.
- **100% of legacy permission rules** identified in F0 mapped to an equivalent Keycloak role/scope enforcement, verified by permission-boundary tests (part of F13 coverage).
- **Conflict detection false-negative rate of 0%** in the regression suite — no booking combination that should conflict per legacy rules is allowed to double-book in the new system.
- **Notification delivery reliability:** 100% of `booking.created`/`approved`/`denied` and `password.reset.requested` events result in a delivered (or retried-to-success) notification in staging load tests, with no silent drops.
- **Independent deployability:** Each of the services identified in Section 4 can be deployed to Kubernetes independently of the others without requiring a full-system redeploy, demonstrated in a staging environment.
- **Audit completeness:** 100% of state-changing actions exercised in the regression suite produce a corresponding audit log entry.

## 8. Risks & Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Legacy behavior is ambiguous or underdocumented in areas like recurring-booking edit scoping, conflict-detection edge cases, or exact notification recipient rules | Silent feature loss or incorrect reinterpretation | Mandatory Open Questions process (F0, Section 10): any ambiguity is logged and explicitly decided before implementation, never guessed |
| Decomposing a monolith into bounded-context services may surface hidden coupling (e.g., shared transactional assumptions between bookings and conflict checks) not visible in the legacy single-database design | Data consistency issues, eventual-consistency bugs across service boundaries | Use F0 audit to explicitly map legacy cross-model dependencies before finalizing service boundaries in TechArch; favor synchronous calls or sagas where strict consistency is required, events where eventual consistency is acceptable |
| Re-implementing the permission matrix via Keycloak roles/scopes may not map 1:1 to legacy fine-grained permission checks | Users gain or lose access unintentionally versus legacy behavior | Explicit per-permission mapping table produced in F0/F7, with permission-boundary tests (F13) covering every legacy permission flag |
| RabbitMQ-based notification delivery introduces new failure modes (message loss, duplicate delivery, ordering) not present in legacy synchronous email sending | Missed or duplicate notifications undermine user trust | Use durable queues with acknowledgement and dead-letter handling; add idempotency keys to notification events; include notification delivery in success metrics and regression tests |
| Scope of "zero functionality lost" is large (12+ first-class legacy capability areas) and could slip if the audit (F0) is rushed or incomplete | Incomplete audit cascades into incomplete requirements, incomplete tests, and real feature loss discovered post-release | Treat F0 as a hard-blocking P0 deliverable with its own sign-off gate before any service implementation begins |
| Database-per-service constraint may make some legacy reporting/query patterns (e.g., joining bookings with custom field values and location data in one query) harder to replicate | Feature or performance regression in list/detail views that aggregate across domains | Identify cross-domain read patterns during F0/TechArch and design read models (e.g., API composition at the gateway/BFF layer, or service-level denormalized read views) accordingly |

## 9. Feature Index

| ID | Feature | Priority | Category |
|---|---|---|---|
| F0 | Legacy Functional Audit | P0 | Foundation / Discovery |
| F1 | Booking Management Service | P0 | Core Booking |
| F2 | Conflict Detection | P0 | Core Booking |
| F3 | Approval Workflow | P0 | Core Booking |
| F4 | Locations & Resources Management | P1 | Reference Data / Admin |
| F5 | Custom Fields | P1 | Booking Extensibility |
| F6 | User & Role Management | P0 | Identity |
| F7 | Permission System | P0 | Identity / Access Control |
| F8 | Notifications | P1 | Messaging |
| F9 | Public Feeds | P1 | Integration / Visibility |
| F10 | Settings | P1 | Configuration |
| F11 | Activity/Audit Logging | P1 | Compliance / Observability |
| F12 | Microservice Architecture & Platform | P0 | Foundation / Infrastructure |
| F13 | Regression Verification & Test Traceability | P0 | Quality / Verification |

**Priority summary:** 8 × P0 (Critical), 6 × P1 (High), 0 × P2/P3. No feature in this release is lower than P1 — this reflects the strict feature-parity mandate: every legacy capability area is treated as required, not optional, for this release.

## 10. Open Questions (Legacy Behavior Requiring Clarification)

Per the project's hard constraint that ambiguous legacy behavior must be flagged rather than guessed, the following are known candidate areas of ambiguity identified from the initial scan in PROJECT.md. **This list is a starting point, not exhaustive — F0 (Legacy Functional Audit) is responsible for producing the authoritative, complete Open Questions list** by reading the actual legacy source:

1. **Recurring booking edit/delete scoping:** When a user edits or deletes one occurrence of a recurring booking series, does the legacy system apply the change to that occurrence only, the whole series, or prompt the user to choose? Exact legacy UX/logic unconfirmed.
2. **Conflict detection enforcement:** Is a detected conflict a hard block (booking cannot be saved) or a soft warning (booking can be saved anyway, e.g., by an admin/approver)? Does this differ by role/permission?
3. **Multi-resource conflict scope:** When a booking has multiple resources attached, is conflict checked per-resource independently, or does any single resource conflict block the whole booking?
4. **Auto-approve interaction with notifications:** When `approveBooking` is disabled (auto-approve), does the system still send a "booking created" notification, an "approved" notification, both, or neither?
5. **Notification recipient rules:** For each lifecycle event (created/approved/denied), exactly who receives the email — booking owner only, owner + all approvers, owner + location-specific approvers, or configurable per-location?
6. **Custom field validation rules:** Do custom fields support required/optional flags, type-specific validation (e.g., numeric, date), or are they free-text only?
7. **Feed access control:** Are the RSS2/iCal/JSON feeds and display board fully public by default, or always gated behind the `allowAPI` permission / a token? Does per-location filtering apply uniformly across all feed formats?
8. **Audit log field completeness:** Does the legacy `Logfiles` capture before/after values for edits, or only the fact that an edit occurred? Is every controller action logged, or only a subset?
9. **Permission matrix completeness:** The permission flags named in PROJECT.md (`accessCalendar`, `allowRoomBooking`, `viewRoomBooking`, `allowApproveBooking`, `accessPermissions`, `allowAPI`) are described as non-exhaustive — the full set and their exact gating scope per controller action is unconfirmed.
10. **Per-environment settings behavior:** What functional differences (if any) exist between development/production/testing/design/maintenance environment settings beyond configuration values — e.g., does "maintenance" mode change user-facing behavior (read-only mode, banner, access lockout)?

**Resolution process:** Each open question must be answered via the F0 audit (by direct code/doc/demo evidence) or, if truly undeterminable, escalated for an explicit product decision and recorded as a Key Decision before the corresponding feature (F1–F13) is implemented. No open question may be closed by assumption.

---
*This PRD is the foundation for the FRD, Technical Architecture, and User Stories documents. All feature IDs (F0–F13) are stable references for cross-document traceability.*
