# Roadmap: Booking-Hub

## Overview

Booking-Hub re-platforms the legacy OxAlto RoomBooking ColdFusion application onto a microservice stack with zero functional regression. The journey starts with a hard-blocking audit of the legacy system (Phase 1) and the foundational platform substrate (Phase 2) that every other service builds on. Identity/permissions (Phase 3) and reference/configuration data (Phase 4) land before the core transactional booking workflow (Phase 5), since bookings are permission-gated and reference locations, resources, custom fields, and the approval-requirement setting. Event-driven consumers — notifications and audit logging (Phase 6) — trail the domain events F1/F3 publish. Public feeds (Phase 7) are a standalone, low-dependency read surface. The roadmap closes with a dedicated regression-verification phase (Phase 8) that converts the F0 audit into a CI-enforced, continuously verifiable parity guarantee rather than a one-time assertion.

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

- [x] **Phase 1: Legacy Functional Audit** - Document every legacy controller/model/route/workflow and produce the authoritative Open Questions list before any build work begins (completed 2026-10-07)
- [x] **Phase 2: Platform Foundation & Infrastructure** - Stand up the nine bounded-context service scaffolds, per-service Postgres, RabbitMQ topology, Keycloak realm, Gateway, and the frontend SPA shell (completed 2026-10-07)
- [ ] **Phase 3: Identity & Access Control** - Users authenticate via Keycloak and every legacy permission flag has a confirmed, enforced Keycloak-role mapping
- [ ] **Phase 4: Reference Data, Extensibility & Configuration** - Admins manage locations, resources, custom fields, and system-wide settings that booking depends on
- [ ] **Phase 5: Core Booking & Approval Workflow** - Users create/edit/delete/clone bookings with full conflict detection and an approval workflow
- [ ] **Phase 6: Notifications & Audit Logging** - Booking/account lifecycle events reliably trigger emails and produce an immutable, queryable audit trail
- [ ] **Phase 7: Public Feeds** - Approved upcoming bookings are exposed via RSS2/iCal/JSON/display-board feeds with zero login required
- [ ] **Phase 8: Regression Verification & Test Traceability** - A CI-enforced regression suite and traceability matrix make "no functionality lost" continuously verifiable

## Phase Details

### Phase 1: Legacy Functional Audit
**Goal**: A documented, end-to-end functional audit of the legacy RoomBooking repository exists before any service implementation begins, so every subsequent phase builds against confirmed behavior rather than assumption.
**Status**: Complete (2026-10-07)
**Depends on**: Nothing (first phase)
**Requirements**: F0 (F0.1, F0.2, F0.3, F0.4, F0.5)
**Success Criteria** (what must be TRUE):
  1. A legacy-audit-findings document exists with a dedicated, cited section for every one of the 12 legacy controllers and every one of the 7 legacy model groups.
  2. Every legacy behavior that is ambiguous, undocumented, or unconfirmable appears in an Open Questions list with a clear "unknown because..." statement — none are silently resolved or guessed at.
  3. Per-environment settings behavior (development/production/testing/design/maintenance) has its own audited section.
  4. The audit's baseline inventory (one row per finding) is formally handed off as the seed input to the F13 traceability matrix, and F0 sign-off is recorded as a gate — no F1–F13 implementation work proceeds without it.
**Plans**: 6 plans

Plans:
- [ ] 01-01-PLAN.md — Audit booking core: Bookings/Eventdata controllers, Event/Eventresource models; confirm/correct conflict-enforcement policy (PRD OQ#2)
- [ ] 01-02-PLAN.md — Audit reference data: Locations/Resources controllers, Location/Resource models
- [ ] 01-03-PLAN.md — Audit custom fields: Customfields controller, Customfield/Customfieldjoin/Customfieldvalue models
- [ ] 01-04-PLAN.md — Audit identity & access: Users/Sessions/PasswordResets/Permissions controllers, User model, full cross-controller permission-flag inventory
- [ ] 01-05-PLAN.md — Audit platform: Settings/Api/Logfiles controllers, routes.cfm, CF lifecycle events, per-environment settings (dev/prod/test/design/maintenance)
- [ ] 01-06-PLAN.md — Consolidate into legacy-audit-findings.md, open-questions.md, baseline-inventory.md; record F0 sign-off gate

### Phase 2: Platform Foundation & Infrastructure
**Goal**: The microservice platform substrate exists so that every other feature can be built, deployed, and scaled independently on top of it.
**Status**: Complete (2026-10-07)
**Depends on**: Phase 1 (bounded-context decomposition must be confirmed by the audit)
**Requirements**: F12 (F12.1, F12.2, F12.3, F12.4, F12.5, F12.6, F12.7)
**Success Criteria** (what must be TRUE):
  1. Nine bounded-context services (per the F0-confirmed decomposition) are scaffolded as independent Spring Boot 3 applications, each with its own Docker image and Kubernetes manifest set.
  2. Each service has exactly one dedicated PostgreSQL database with no shared schema and no cross-service database connection string.
  3. A RabbitMQ topology (exchanges/queues/routing keys) exists for every domain event named across F1–F11, ready for services to publish/consume as they are built.
  4. A Keycloak realm is provisioned with roles/clients, and Spring Cloud Gateway is the sole externally-reachable ingress, validating every request's JWT against the realm — no backend service is directly externally addressable and no service stores its own passwords.
  5. A React + TypeScript SPA shell exists with routing, Keycloak PKCE login wiring, and placeholder routes for every planned screen (calendar/list views, admin CRUD, feeds, display board), ready for feature-specific screens to be built into it in later phases.
**Plans**: 12 plans

Plans:
- [ ] 02-01-PLAN.md — Scaffold booking-service (Maven, Flyway DDL, Docker, K8s, context-boot test)
- [ ] 02-02-PLAN.md — Scaffold locations-resources-service
- [ ] 02-03-PLAN.md — Scaffold custom-field-service
- [ ] 02-04-PLAN.md — Scaffold users-permissions-service (no credential storage)
- [ ] 02-05-PLAN.md — Scaffold notifications-service
- [ ] 02-06-PLAN.md — Scaffold feeds-service
- [ ] 02-07-PLAN.md — Scaffold settings-service (singleton + seed row)
- [ ] 02-08-PLAN.md — Scaffold audit-log-service (immutability REVOKE)
- [ ] 02-09-PLAN.md — Keycloak realm export + RabbitMQ topology definitions
- [ ] 02-10-PLAN.md — Spring Cloud Gateway (routing, JWT validation, fail-closed, rate limiting)
- [ ] 02-11-PLAN.md — React+TypeScript SPA shell (routing, nav, Keycloak PKCE wiring)
- [ ] 02-12-PLAN.md — docker-compose integration: full-stack boot + network/DB isolation audit

### Phase 3: Identity & Access Control
**Goal**: Users can authenticate and self-manage their accounts, and every protected action is gated by a confirmed permission, so access control behaves equivalently to the legacy system without anyone silently gaining or losing access during the re-platform.
**Depends on**: Phase 2 (Keycloak realm, Gateway, users-permissions-service scaffold)
**Requirements**: F6 (F6.1–F6.6), F7 (F7.1–F7.4)
**Success Criteria** (what must be TRUE):
  1. Admin can create a user account (provisioned in Keycloak, no local password storage) and assign an initial role.
  2. User can log in/out, use "remember me" extended session behavior, edit their own profile, and change their own password via Keycloak-backed flows, with access-denied handled equivalently to legacy `Sessions` behavior.
  3. User can request and complete a password reset via an emailed token/link; expired, invalid, or already-used tokens are rejected.
  4. Every legacy permission flag (`accessCalendar`, `allowRoomBooking`, `viewRoomBooking`, `allowApproveBooking`, `accessPermissions`, `allowAPI`, plus any F0-confirmed additions) has a documented, administrable Keycloak-role mapping; any flag whose gating scope is unconfirmed is treated as deny-by-default, never approximated.
  5. A request to a Gateway-protected route without the required role is rejected at the edge, and a service-level fine-grained check independently rejects actions the Gateway would have allowed through by mistake — the two enforcement layers never disagree.
**Plans**: 5 plans

Plans:
- [ ] 03-01-PLAN.md — Domain/migration layer: V2 migration (outbox, password-reset tokens, case-insensitive email, 17-row permission seed), JPA entities/repositories, shared ApiError contract
- [ ] 03-02-PLAN.md — Keycloak integration layer: Tier-2 JWT validation with fail-closed JWKS-outage handling, Keycloak Admin API client, direct-grant token client, CurrentUserProvider
- [ ] 03-03-PLAN.md — Auth flows: login/logout/password-reset/password-change against Keycloak, additive realm config (session timeouts, password policy, direct-grant clients)
- [ ] 03-04-PLAN.md — User management: account CRUD, admin-vs-self authorization, role assignment, outbox event emission
- [ ] 03-05-PLAN.md — Permission system CRUD, outbox-to-RabbitMQ relay, Tier1/Tier2 consistency contract test

### Phase 4: Reference Data, Extensibility & Configuration
**Goal**: Admins can manage the reference data and configuration that booking depends on — locations, resources, custom fields, and system-wide settings — so the Core Booking phase has real data and rules to build against.
**Depends on**: Phase 3 (admin-only actions are permission-gated)
**Requirements**: F4 (F4.1–F4.4), F5 (F5.1–F5.4), F10 (F10.1–F10.4)
**Success Criteria** (what must be TRUE):
  1. Admin can create/edit/delete bookable Locations (name, CSS class/colour, building, layout) and Resources, both gated by admin permission.
  2. Deleting a Location/Resource referenced by existing bookings follows the soft-delete policy — the entity is marked deleted but existing booking references remain intact and display using last-known values.
  3. Admin can define custom field definitions and field templates (select-type fields require a non-empty `options[]`) and associate a template with a booking context.
  4. Custom field values attach to bookings via a join model distinct from field definitions, ready for the booking form to render applicable fields dynamically once Phase 5 builds it.
  5. Admin can toggle the `approveBooking` flag and configure calendar display parameters (slot size, min/max time); settings exist as a single enforced singleton, are admin-only to modify, and are readable by the Booking Service for creation-time status logic.
**Plans**: 6 plans

Plans:
- [ ] 04-01-PLAN.md — locations-resources-service: F0 schema-completion migration, entities/repos, ApiError contract, Tier-2 security
- [ ] 04-02-PLAN.md — locations-resources-service: Location/Resource CRUD controllers (soft-delete policy), outbox publisher
- [ ] 04-03-PLAN.md — custom-field-service: outbox + required-flag migration, entities/repos, ApiError contract, Tier-2 security
- [ ] 04-04-PLAN.md — custom-field-service: CustomField/FieldTemplate CRUD controllers, applicability-query endpoint, outbox publisher
- [ ] 04-05-PLAN.md — settings-service: outbox migration, singleton entity, ApiError contract, Tier-2 security
- [ ] 04-06-PLAN.md — settings-service: GET/PUT controller (validation + immediate propagation), outbox publisher

### Phase 5: Core Booking & Approval Workflow
**Goal**: Users can create, edit, delete, and clone bookings with full conflict detection and an approval workflow, so no two conflicting bookings for the same room or resource ever go unnoticed regardless of how they were entered, and every approval/denial decision is final and reaches the right outcome.
**Depends on**: Phase 4 (locations, resources, custom fields, approveBooking setting must exist)
**Requirements**: F1 (F1.1–F1.7), F2 (F2.1–F2.5), F3 (F3.1–F3.4)
**Success Criteria** (what must be TRUE):
  1. User can create/edit/delete/clone a booking with the full legacy field set, correct date/time validation (zero-duration rejected; end time defaults to start + 1 hour when omitted), and multiple attached resources.
  2. Calendar, day, and list views return bookings within a date range with `conflict_flags` attached, and an AJAX-style detail endpoint returns the full field set for a single booking.
  3. Overlap-checking is evaluated per location AND per resource, on every create AND edit (not just at creation), using half-open interval semantics, and excludes the booking's own prior state plus deleted/denied bookings from comparison.
  4. A detected conflict is a hard block for callers without `allowApproveBooking` and a soft, informational warning for callers who hold it — enforced by exactly one conflict-evaluation code path regardless of UI calendar, UI list, or direct API entry point.
  5. New bookings receive `pending` or `approved` status per the global auto-approve setting; a user holding `allowApproveBooking` can approve or deny a pending booking, each decision is a one-way transition (no re-toggling), and each publishes a domain event.
**Plans**: TBD

### Phase 6: Notifications & Audit Logging
**Goal**: Every booking lifecycle event and password reset reliably triggers an email notification, and every state-changing action across the system is captured in an audit trail, so no event is silently lost and every change is accountable after the fact.
**Depends on**: Phase 5 (booking.created/approved/denied events must exist to consume)
**Requirements**: F8 (F8.1–F8.4), F11 (F11.1–F11.4)
**Success Criteria** (what must be TRUE):
  1. `booking.created`, `booking.approved`, `booking.denied`, and `password.reset.requested` events each trigger an equivalent email notification to the confirmed recipient(s), delivered via durable RabbitMQ consumption rather than synchronous in-request sending.
  2. A redelivered event with an already-`sent` idempotency key never results in a duplicate email; a notification that fails transiently is retried with backoff, and retry exhaustion routes to a dead-letter queue rather than silently dropping the message.
  3. Change events are captured across booking lifecycle, location/resource management, user/role changes, settings changes, and permission changes — not just booking actions.
  4. Admins can view and filter the audit log by entity type, actor, and date range; audit log entries are immutable once written (no API can edit or delete a prior entry).
  5. A missed or failed domain event consumption is retried via the durable queue/DLQ mechanism rather than silently leaving a gap in the audit trail.
**Plans**: TBD

### Phase 7: Public Feeds
**Goal**: Approved upcoming bookings are exposed via public, read-only feeds, so external viewers like reception staff and visitors can see room availability at a glance without ever logging in or being shown an unconfirmed booking.
**Depends on**: Phase 5 (approved bookings must exist to project), Phase 4 (location filtering)
**Requirements**: F9 (F9.1–F9.6)
**Success Criteria** (what must be TRUE):
  1. RSS2, iCal, JSON feeds and a digital-signage display-board view are all available, each showing only `status=approved` upcoming bookings — pending and denied bookings never appear in any format.
  2. All feed formats support optional per-location filtering, applied consistently; an unknown `location_id` filter returns an empty feed rather than an error.
  3. Every feed format is accessible with zero login, account, or token required, and access rules are identical across RSS2/iCal/JSON/display board — no format is gated while another isn't.
**Plans**: TBD

### Phase 8: Regression Verification & Test Traceability
**Goal**: Automated tests verify every legacy feature and business rule identified by F0, with a maintained traceability matrix, so "no functionality lost" is continuously and objectively verifiable — and a CI-enforced regression suite blocks any release that would silently regress parity.
**Depends on**: Phases 1–7 (every feature and the F0 findings must exist to be traced and tested)
**Requirements**: F13 (F13.1–F13.5)
**Success Criteria** (what must be TRUE):
  1. A traceability matrix links every F0 audit finding to the requirement(s) it generated and the test(s) that verify it — no orphaned findings.
  2. Automated test coverage exists for every validation rule, workflow transition, and permission check identified in F0, including explicit edge-case coverage for conflict-detection, approval/auto-approve interactions, recurring-booking edit/delete scoping, and permission-matrix boundaries.
  3. A CI-enforced regression suite runs on every change and blocks merge/release if a previously passing parity test now fails.
  4. Every F0 Open Question that gets resolved has its decision recorded and a new corresponding test added — a resolution without a new test is rejected.
**Plans**: TBD

## Progress

**Execution Order:**
Phases execute in numeric order: 1 → 2 → 3 → 4 → 5 → 6 → 7 → 8

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Legacy Functional Audit | 0/TBD | Complete | 2026-10-07 |
| 2. Platform Foundation & Infrastructure | 12/12 | Complete | 2026-10-07 |
| 3. Identity & Access Control | 0/5 | Planned | - |
| 4. Reference Data, Extensibility & Configuration | 0/6 | Planned | - |
| 5. Core Booking & Approval Workflow | 0/TBD | Not started | - |
| 6. Notifications & Audit Logging | 0/TBD | Not started | - |
| 7. Public Feeds | 0/TBD | Not started | - |
| 8. Regression Verification & Test Traceability | 0/TBD | Not started | - |

---
*Roadmap created: 2026-10-06*
*Granularity: standard (8 phases)*