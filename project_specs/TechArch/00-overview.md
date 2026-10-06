# Technical Architecture: Booking-Hub

**Project Acronym:** BookingHub
**Document Type:** TechArch (Technical Architecture Document)
**Status:** Draft — derived from PRD-BookingHub.md and FRD-BookingHub.md
**Last Updated:** 2026-10-06

---

## 0. How This Document Relates to the FRD

This TechArch operationalizes the bounded-context decomposition, schema (`Y0-schema.md`), API catalog (`Y1-api.md`), error catalog (`Y2-errors.md`), and integration points (`Y3-integrations.md`) already specified in `FRD-BookingHub.md`. Where the FRD already pins down a concrete decision (e.g., exact DDL, exact endpoint shapes), this document reuses it verbatim and adds the structural/runtime detail (service topology, deployment, security enforcement, messaging topology) the FRD explicitly deferred to TechArch. Every `[OPEN QUESTION — deferred to F0]` flag in the FRD is preserved here, not silently resolved — the interim/default behavior documented in the FRD is what this architecture implements until F0 closes the question.

All tech stack choices below are **hard constraints, not recommendations** — mandated in `PROJECT.md` and PRD Section 4: React + TypeScript frontend; Java 21 + Spring Boot 3 backend services; PostgreSQL, one database per microservice; RabbitMQ for inter-service events; Keycloak for authentication/authorization; Spring Cloud Gateway as the single API gateway entry point; Docker + Kubernetes for deployment.

---

## 1. Architectural Overview

### 1.1 Pattern

Booking-Hub is a **bounded-context microservice architecture** with:

- A single public ingress (Spring Cloud Gateway) — no backend service is externally addressable.
- **Database-per-service** — nine independently owned PostgreSQL databases, zero shared schemas, zero cross-service database joins. Cross-service data needs are resolved either by synchronous REST calls between services (when strict consistency is required) or by consuming RabbitMQ domain events into a service-local, denormalized read model (when eventual consistency is acceptable) — see §1.4.
- **Event-driven decoupling** — side effects that must never block the originating transaction (notifications, audit logging) are triggered exclusively via durable RabbitMQ domain events, published using the **transactional outbox pattern** so a RabbitMQ outage cannot roll back a booking creation.
- **Standards-based identity** — Keycloak is the sole source of truth for credentials, sessions, and role claims. No service stores a password hash. Every protected request carries a Keycloak-issued JWT validated against the realm's published JWKS.
- **Independent deployability** — every service has its own Docker image, its own Kubernetes Deployment/Service/ConfigMap/Secret set, and can be built, deployed, and scaled without coordinating with any other service's release.

### 1.2 Bounded-Context Service Decomposition

Nine deployable units, aligned 1:1 with the FRD's per-service schema ownership (`Y0-schema.md`) and the PRD's indicative decomposition (confirmed, not altered, by this TechArch):

| # | Service | Legacy Equivalent | Owns (DB tables) | FRD Features |
|---|---|---|---|---|
| 1 | **booking-service** | `Bookings`, `Eventdata` controllers; `Event`, `Eventresource` models | `bookings`, `booking_resources` | F1, F2, F3 |
| 2 | **locations-resources-service** | `Locations`, `Resources` controllers; `Location`, `Resource` models | `locations`, `resources` | F4 |
| 3 | **custom-field-service** | `Customfields` controller; `Customfield`/`Customfieldjoin`/`Customfieldvalue` models | `custom_fields`, `custom_field_templates`, `custom_field_joins`, `custom_field_values` | F5 |
| 4 | **users-permissions-service** | `Users`, `Sessions`, `PasswordResets`, `Permissions` controllers; `User` model | `users`, `permissions` | F6, F7 |
| 5 | **notifications-service** | Email logic embedded in `Bookings`/`PasswordResets` | `notification_deliveries` | F8 |
| 6 | **feeds-service** | `Api` controller | `feed_bookings` (optional projection) | F9 |
| 7 | **settings-service** | `Settings` controller/model | `settings` | F10 |
| 8 | **audit-log-service** | `Logfiles` controller | `audit_log_entries` | F11 |
| 9 | **(Keycloak — not a Booking-Hub-authored service)** | hand-rolled auth | realm DB (Keycloak-managed, opaque to Booking-Hub) | F6, F7 (identity half) |

`users-permissions-service` combines F6 (user/role management) and F7 (permission mapping metadata) into one deployable because both own small, tightly related reference tables (`users`, `permissions`) with no independent scaling profile — see §2 Component Architecture for the internal module boundary kept between them (they remain separately testable and could be split later without a schema change, since neither table references the other via FK).

### 1.3 Deployment Topology (ASCII)

```
                                   ┌─────────────────────────┐
                                   │   React + TypeScript    │
                                   │   SPA (static, CDN/     │
                                   │   nginx pod)            │
                                   └────────────┬────────────┘
                                                │ HTTPS (OIDC/PKCE for
                                                │ browser-facing login)
                                                ▼
                                   ┌─────────────────────────┐
                                   │  Spring Cloud Gateway   │◄──────┐
                                   │  (single ingress,       │       │ JWKS fetch /
                                   │  JWT validation,        │       │ token introspection
                                   │  coarse-grained RBAC)   │       │
                                   └────────────┬────────────┘       │
                                                │ internal cluster   │
                                                │ traffic only       │
              ┌───────────────┬────────────────┼──────────────┬─────┴────────┬───────────────┬────────────────┐
              ▼               ▼                ▼              ▼              ▼               ▼                ▼
      ┌───────────────┐┌─────────────┐┌────────────────┐┌───────────┐┌──────────────┐┌─────────────┐┌─────────────────┐
      │ booking-      ││ locations-  ││ custom-field-  ││ users-     ││ notifications││ feeds-      ││ settings-       │
      │ service       ││ resources-  ││ service        ││ permissions││ -service     ││ service     ││ service         │
      │               ││ service     ││                ││ -service   ││              ││             ││                 │
      └───┬───────┬───┘└──────┬──────┘└───────┬────────┘└─────┬──────┘└──────┬───────┘└──────┬──────┘└────────┬────────┘
          │       │           │               │               │              │               │                │
          ▼       │           ▼               ▼               ▼              │               │                ▼
    ┌──────────┐  │     ┌──────────┐    ┌──────────┐    ┌──────────┐         │               │          ┌──────────┐
    │ booking_ │  │     │ locres_  │    │ customfld│    │ userperm_│         │               │          │ settings_│
    │ db (PG)  │  │     │ db (PG)  │    │ _db (PG) │    │ db (PG)  │         │               │          │ db (PG)  │
    └──────────┘  │     └──────────┘    └──────────┘    └──────────┘         │               │          └──────────┘
                  │                                                           ▼               ▼
                  │                                                     ┌──────────┐    ┌──────────┐
                  │                                                     │ notif_db │    │ feeds_db │
                  │                                                     │ (PG)     │    │ (PG,     │
                  │                                                     └──────────┘    │ optional)│
                  │                                                                      └──────────┘
                  └──────────────────────┐
                                          ▼
                                 ┌─────────────────┐        ┌───────────────────┐
                                 │  audit-log-      │        │     RabbitMQ       │
                                 │  service         │◄───────┤  (durable exchanges│
                                 │  (event consumer │        │  + per-consumer    │
                                 │  only)           │        │  queues + DLQs)    │
                                 └────────┬─────────┘        └─────────▲──────────┘
                                          ▼                            │ publishes
                                   ┌──────────────┐                    │ booking.*, location.*,
                                   │ audit_db (PG)│                    │ resource.*, user.*, role.*,
                                   └──────────────┘                    │ settings.updated,
                                                                        │ permission.updated,
                                                                        │ password.reset.requested
                                                                        │
                                                            (all 8 business services
                                                             publish; notifications-service
                                                             and audit-log-service consume)

                                   ┌─────────────────────────┐
                                   │        Keycloak          │  (separate realm DB,
                                   │  (OIDC provider, realm   │   not a Booking-Hub
                                   │  roles/scopes, Admin API)│   service database)
                                   └─────────────────────────┘
                                   Consulted by: Gateway (JWT validation),
                                   users-permissions-service (Admin API calls
                                   for provisioning/credential-reset),
                                   every service (JWKS-based token verification)
```

Key read: every arrow into Spring Cloud Gateway is the **only** externally reachable path into the cluster. Every PostgreSQL instance pictured is a logically (and in production, physically) separate database with its own connection pool, credentials, and Kubernetes Secret — no service's connection string references another service's database. RabbitMQ and Keycloak are shared infrastructure, not owned by any single bounded context, consumed by multiple services through well-defined contracts (AMQP topology; OIDC/Admin API respectively).

### 1.4 Cross-Domain Read Pattern Decisions (PRD Risk Mitigation)

The PRD explicitly flags that database-per-service makes cross-domain reads (e.g., Booking + Custom Field + Location in one list view) harder to replicate from the legacy single-database system. This TechArch resolves that risk per read pattern as follows:

| Read Pattern | Services Involved | Strategy Chosen | Rationale |
|---|---|---|---|
| Booking calendar/list/detail view showing location name + colour alongside booking fields | booking-service, locations-resources-service | **Synchronous API composition at the BFF/Gateway layer** (or client-side parallel fetch): booking-service returns `location_id` only; frontend/BFF joins against `GET /locations` cache | Location data changes infrequently; always-fresh location names outweigh the complexity of a projection |
| Booking detail view showing custom field values | booking-service, custom-field-service | **Synchronous API composition**: booking-service persists `custom_field_values` rows itself (it owns the join to its own `bookings.id`), custom-field-service is only consulted for field *definitions* (label, type) at render time, cached client-side | Avoids a sync call per booking read; booking-service already stores submitted values at write time (F1 §Process step 7) |
| Public feed (RSS2/iCal/JSON/display board) over approved upcoming bookings, filterable by location, needs to be fast and public-read-heavy | booking-service, locations-resources-service, feeds-service | **Event-driven denormalized projection** (`feed_bookings` table in feeds-service, per `Y0-schema.md` §Feed) populated by consuming `booking.created`/`booking.updated`/`booking.approved`/`booking.denied`/`booking.deleted` and `location.updated`/`location.deleted` events | Public feeds must not place synchronous load on the transactional booking-service, and must survive booking-service being temporarily unavailable |
| Audit log entries referencing entity details beyond id/type | all services → audit-log-service | **Event-sourced, denormalized by design**: the audit log stores whatever `before_values`/`after_values` JSONB the publishing service included in its domain event payload; it never calls back to the source service to enrich a record | Keeps audit-log-service a pure event consumer with zero outbound dependencies, per F11's "never performs a direct read of another service's database" rule |

This table is the TechArch-level resolution the FRD (PRD Risk table, F12 §Process step 8) deferred here.

### 1.5 Architectural Decision Log

| Decision | Options Considered | Choice | Rationale |
|---|---|---|---|
| Settings propagation (FRD F10 §Process step 4, deferred to TechArch) | (a) synchronous read of Settings Service API per Booking creation; (b) `settings.changed` broadcast cached locally | **(a) Synchronous read, with a short-TTL local cache (5s) in booking-service** | `approveBooking` is read exactly once per booking creation (F3 §Validation: "read at creation time only, non-retroactive") — a stale cache for a few seconds has no correctness impact, and avoids the complexity of a broadcast-and-invalidate scheme for a single boolean+three small fields |
| Notification/audit decoupling mechanism | synchronous call vs. async event | **Async RabbitMQ events exclusively**, per NFR "Event-driven decoupling" | Hard requirement in PRD/PROJECT.md: a notification or audit failure must never block the originating transaction |
| Outbox vs. direct publish-on-commit | direct `channel.publish()` inside the request thread vs. transactional outbox table + relay | **Transactional outbox pattern** in every event-publishing service | Guarantees the domain event is published if-and-only-if the local DB transaction committed, per Y3 "Publisher guarantee" — a mid-transaction RabbitMQ outage cannot silently drop an event nor roll back a successful booking |
| users-permissions-service as one deployable vs. two | single service vs. split `users-service` + `permissions-service` | **Single deployable, two internal modules** | Both own small reference tables with no independent scaling need today; FRD's schema ownership keeps `users` and `permissions` as separate tables with no FK between them, so a future split requires no schema migration — see §2.4 |
| Gateway framework | Spring Cloud Gateway (mandated) | **Spring Cloud Gateway**, reactive (WebFlux-based) | Hard constraint from PROJECT.md/PRD |
| Conflict-detection enforcement default | hard block vs. soft warning (FRD F2 §Process step 6, `[OPEN QUESTION — deferred to F0]`) | **Hard block (HTTP 409) for all roles**, interim, until F0 confirms otherwise | Matches FRD's stated conservative interim default; implemented as a single code path in booking-service per F2 §Process step 8 |
