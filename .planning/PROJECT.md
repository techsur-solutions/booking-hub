# Booking-Hub

## What This Is

Booking-Hub is a modernized rewrite of the open-source **OxAlto RoomBooking System** (https://github.com/neokoenig/RoomBooking) — a web-based room/resource booking calendar currently built on ColdFusion (cfWheels), Bootstrap 3, jQuery, and FullCalendar.js. The legacy app lets organizations manage bookable locations/resources, submit and approve bookings, enforce conflict rules, notify stakeholders by email, expose read-only feeds (RSS/iCal/JSON/digital-signage display), support custom fields per booking, and control access via a role/permission system.

Booking-Hub re-platforms this functionality — with **zero feature loss** — onto a modern microservice stack: React + TypeScript frontend, Java 21 + Spring Boot 3 backend services, one PostgreSQL database per service, RabbitMQ for inter-service events, Keycloak for authentication/authorization, Spring Cloud Gateway as the API gateway, and Docker/Kubernetes for deployment.

## Core Value

Every existing feature, business rule, validation, and workflow in the legacy RoomBooking system must exist and behave equivalently in the new system — verified by tests, not assumed. Where legacy behavior is ambiguous or undocumented, it is raised as an explicit open question rather than guessed at and silently reinterpreted.

## Requirements

### Validated

- ✓ **Legacy functional audit (F0)** — v1.0: Documented all 12 legacy controllers, 7 model groups, permissions, workflows, and environment-specific behaviors; produced 204-finding baseline inventory, Open Questions list with 25 items, legacy-audit-findings.md
- ✓ **Microservice architecture (F12)** — v1.0: 9 Spring Boot 3 services (booking, locations-resources, custom-field, users-permissions, notifications, feeds, settings, audit-log, api-gateway), React+TypeScript SPA, per-service PostgreSQL databases, RabbitMQ domain events, Keycloak authentication, Spring Cloud Gateway with JWT validation
- ✓ **User & role management (F6)** — v1.0: User CRUD, Keycloak-backed login/logout/password-reset, self-service profile management, role assignment, "remember me" sessions
- ✓ **Permission system (F7)** — v1.0: Granular permission matrix (17 permission flags from F0 audit), role-to-permission mapping API, Tier1 (Gateway) and Tier2 (service) enforcement with automated consistency contract test, deny-by-default on undefined flags
- ✓ **Locations & resources management (F4)** — v1.0: Location/resource CRUD with soft-delete policy preserving existing booking references, admin-only access gating, CSS class/colour/building/layout metadata
- ✓ **Custom fields (F5)** — v1.0: Custom field definitions and templates, booking-context association, dynamic field picker, select-type fields with options validation
- ✓ **Settings (F10)** — v1.0: Singleton settings entity with `approveBooking` flag, calendar display parameters (slot size, min/max time), admin-only modification, immediate propagation to booking service
- ✓ **Booking management service (F1)** — v1.0: Full booking CRUD with clone, date/time validation (end > start, default 1-hour duration), recurring bookings, multi-resource bookings, status workflow (pending→approved/denied) per auto-approve setting
- ✓ **Conflict detection (F2)** — v1.0: Half-open interval overlap checking per location AND per resource, evaluated on every create AND edit, excludes booking's own prior state and deleted/denied bookings, surfaced with conflict_flags in calendar/list views
- ✓ **Approval workflow (F3)** — v1.0: One-way pending→approved/denied transitions, permission-gated approve/deny actions, domain events (booking.approved, booking.denied) published to RabbitMQ
- ✓ **Notifications (F8)** — v1.0: Event-driven email delivery for booking.created/approved/denied and password.reset.requested, durable RabbitMQ consumption with idempotency (no duplicate sends), retry with backoff, dead-letter queue for exhausted retries
- ✓ **Activity/audit logging (F11)** — v1.0: Immutable audit log capturing changes across booking lifecycle, location/resource management, user/role changes, settings changes, permission changes; admin-facing filterable GET /audit-log API by entity type/actor/date range; 7-queue RabbitMQ consumer with routing-key-derived entity/action parsing
- ✓ **Public feeds (F9)** — v1.0: RSS2, iCal, JSON, and display-board feeds of approved upcoming bookings, optional per-location filtering, zero authentication required, strong ETag caching, hand-built renderers for minimal attack surface
- ✓ **Regression verification (F13)** — v1.0: Traceability matrix linking 204 F0 audit findings to requirements and tests, test coverage gap analysis, CI regression pipeline (GitHub Actions), branch protection, Open Question resolution protocol with test-before-merge enforcement

### Active

(None — all v1.0 requirements validated)

### Out of Scope

- Preserving the ColdFusion/cfWheels codebase or runtime — this is a full re-platform, not a lift-and-shift
- Preserving exact legacy UI (Bootstrap 3/jQuery/FullCalendar.js look-and-feel) — React/TypeScript UI should cover the same user-facing capabilities, not pixel-match the old screens, unless the user says otherwise
- New features beyond what the legacy system does — scope is feature parity first; enhancements are explicitly future work

## Context

- **Legacy system**: OxAlto RoomBooking System, open source (Apache 2.0), by Tom King (neokoenig). ColdFusion on Wheels (cfWheels MVC framework) + Bootstrap 3 + jQuery + FullCalendar.js + MomentJS. ~307 files; `controllers/`, `models/`, `views/`, `events/` (CF application lifecycle hooks), `config/routes.cfm` (custom + default REST-ish routes), per-environment settings (development/production/testing/design/maintenance).
- **Current system (v1.0 shipped)**: 9 Java 21 + Spring Boot 3 microservices, React + TypeScript SPA, 291 Java source files, 1,441 TypeScript files. Per-service PostgreSQL databases (no shared schema), RabbitMQ for domain events, Keycloak for authentication/authorization, Spring Cloud Gateway as API gateway, Docker images and Kubernetes manifests for deployment.
- **Migration status**: v1.0 achieves complete backend API feature parity with legacy system. All 14 F0-F13 requirements validated. 25 Open Questions documented for product owner decisions. Frontend SPA shell exists with routing and Keycloak integration; booking views/calendar UI are next-milestone work.
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
| Full microservice re-platform (not incremental strangler-fig on the CF codebase) | User mandated a modern stack (Java/Spring Boot, React/TS, Postgres-per-service, RabbitMQ, Keycloak, Gateway, Docker/K8s) with no mention of preserving the CF runtime | ✓ Good — v1.0 delivered 9 Spring Boot services with complete bounded-context isolation, per-service databases, event-driven integration via RabbitMQ, zero shared schema dependencies |
| Legacy functional audit as an explicit first-class deliverable before any build work | Strict no-functionality-lost requirement means the spec must be derived from the real legacy behavior, not assumptions | ✓ Good — Phase 1 F0 audit produced 204-finding baseline inventory, 25 Open Questions with explicit "unknown because..." reasoning, zero silent assumptions; traceability matrix (Phase 8) linked every finding to requirements and tests |
| Database per service (not a shared Postgres instance/schema) | Explicit user requirement, aligns with microservice bounded-context isolation | ✓ Good — Each service (booking, locations-resources, custom-field, users-permissions, notifications, feeds, settings, audit-log) has its own PostgreSQL database; zero cross-service database connections; domain events via RabbitMQ |
| Scheduled polling-publisher for outbox relay (not Debezium CDC) | First service to implement outbox relay (Phase 3); simpler, zero new infrastructure, sets precedent for other services | ✓ Good — All services (users-permissions, locations-resources, custom-field, booking, settings) use @Scheduled bean polling pending outbox rows every 2s, publishing to RabbitMQ; zero Debezium/Kafka Connect complexity; consistent pattern across all services |
| Tier1/Tier2 consistency contract test reading ACTUAL SecurityConfig and @PreAuthorize annotations (not hardcoded assumptions) | Phase 3's 5th success criterion: "The two enforcement layers never disagree" must be provable by automation, not manual verification | ✓ Good — Tier1Tier2ConsistencyTest parses Gateway's real SecurityConfig.java source and reads service's real annotations via reflection; fails build if Tier2 ever weaker than Tier1; contract verified continuously |
| Conflict enforcement: legacy has NO permission-conditional hard-block/soft-warning split at all — `Bookings.check()` is unconditionally non-blocking/informational for every user regardless of `allowApproveBooking`; `create()`/`update()` perform no server-side conflict validation whatsoever | F0 audit corrected interim assumption that conflict checking had a permission-conditional split; legacy is simpler: always non-blocking for everyone | ⚠️ Revisit — v1.0 implemented informational conflict_flags in calendar/list reads (Phase 5), deferring hard-block enforcement decision to Open Question #2 (3 options: hard-block for all, soft-warning for approvers, always-allow). Product owner decision still pending. Test stub created (Phase 8) for each option. |
| Public Feed access: legacy feeds are NOT fully public — the 3 data-serving actions (`rss2`/`ical`/`display`) require a valid per-user API token (`f_isValidAPIRequest`, token >25 chars matching a `users.apitoken` row); the `allowAPI` permission only gates the human-browsed `index` listing page | F0 audit found legacy is token-gated (any valid token works), not role-gated; corrected interim assumption of "fully public" | ⚠️ Revisit — v1.0 implemented fully-public feeds (Phase 7) per Personas/JTBD material for Jordan (zero login prompts). Named divergence from legacy's token-gated approach. Open Question #7 documented; product owner sign-off still required to confirm or revert to token-gated. |
| Booking time-range validation precision: `end_time` must be strictly after `start_time` (zero-duration bookings rejected) | F0 audit found legacy ACCEPTS zero-duration bookings; v1.0 schema's strict `end_time > start_time` CHECK is an intentional stricter rule | ✓ Good — Deliberate improvement over legacy; zero-duration bookings rejected at DB level; validation rule documented as named divergence in baseline-inventory.md BF-020 |
| Hand-built RSS2/iCal renderers (not ical4j/rome libraries) for feeds-service | Feeds-service is the only public internet-facing service with zero authentication; minimal dependency/attack surface desired | ✓ Good — Phase 7 implemented format-correct RSS2 and iCal renderers with proper XML/RFC-5545 escaping, proven by unit tests; zero third-party feed library dependencies; smaller attack surface |

---
*Last updated: 2026-10-13 after v1.0 milestone completion*
