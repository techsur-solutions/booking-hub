# Milestones

## v2.0 UI Implementation (In Progress: Started 2026-10-13)

**Phases planned:** 2 phases (9-10), 29 plans

**Timeline:** Started 2026-10-13

**Tech stack:** React + TypeScript SPA, connecting to v1.0 backend APIs (Java 21 + Spring Boot 3 microservices)

**Scope:** Complete frontend UI implementation with feature parity to legacy OxAlto RoomBooking system

**Target features:**
- Calendar/Day/List booking views with FullCalendar integration and conflict detection UI
- Booking Create/Edit forms with recurring series scope handling
- Approval Queue for pending bookings with one-way approve/deny actions
- Admin screens: Locations, Resources, Custom Fields, Users, Roles, Settings, Audit Log
- Display Board (public digital signage view) and Feed Subscription UI
- Complete responsive layout (desktop/tablet/mobile) with accessibility compliance
- Authentication flows (Login, My Account, Password Reset) with Keycloak integration

**Design principles:**
- Explicit state confirmation over implicit success
- Boundary/scope decisions surfaced before action completes
- Identical conflict visibility across all views
- Deny-by-default permission visibility
- Single-pass admin forms (not multi-step wizards)

**Requirements:** 42 UI requirements organized across 10 functional areas (Navigation, Booking Views, Creation/Editing, Admin screens, Approval, Public surfaces, Auth, Responsive, Accessibility, Patterns)

**Phase structure:**
- Phase 9: Core User Interface (15 plans) - App shell, navigation, API client, booking views, CRUD forms, approval queue, display board, authentication
- Phase 10: Admin Interface & Polish (14 plans) - Admin screens, responsive design, accessibility, E2E tests, documentation

---

## v1.0 Complete Backend API Implementation (Shipped: 2026-10-13)

**Phases completed:** 8 phases, 47 plans

**Timeline:** 2026-10-07 → 2026-10-13 (6 days)

**Tech stack:** Java 21 + Spring Boot 3 microservices (9 services), React + TypeScript SPA, PostgreSQL (per-service databases), RabbitMQ, Keycloak, Spring Cloud Gateway, Docker + Kubernetes

**Scale:** 291 Java files, 1,441 TypeScript files

**Key accomplishments:**

- **Complete legacy functional audit** — Documented all 12 legacy controllers, 7 model groups, permissions, workflows, and environment-specific behaviors; identified 25 Open Questions with explicit "unknown because..." reasoning
- **Microservice platform foundation** — 9 bounded-context Spring Boot services, per-service PostgreSQL databases, RabbitMQ topology for domain events, Keycloak realm with JWT validation, Spring Cloud Gateway as single entry point, React SPA shell
- **Identity and access control** — User authentication via Keycloak, role-based permission matrix with deny-by-default enforcement, password reset flows, Tier1/Tier2 consistency contract test ensuring Gateway and service-level authorization never disagree
- **Reference data and configuration** — Location/resource CRUD with soft-delete policy, custom field definitions and templates, system settings singleton controlling approval workflow and calendar display
- **Core booking and approval workflow** — Full booking CRUD with conflict detection per location AND resource, half-open interval overlap checking, approval workflow with one-way pending→approved/denied transitions, recurring bookings, multi-resource support
- **Notifications and audit logging** — Event-driven email notifications for booking lifecycle and password reset via durable RabbitMQ consumers with idempotency and retry/DLQ handling; immutable audit log capturing changes across all services with admin-facing filterable query API
- **Public feeds** — RSS2, iCal, JSON, and display-board feeds of approved upcoming bookings with optional per-location filtering, zero authentication required, strong ETag caching
- **Regression verification and test traceability** — Traceability matrix linking 204 F0 audit findings to requirements and tests, test coverage gap analysis, CI regression pipeline with GitHub Actions, Open Question resolution protocol with test-before-merge enforcement

**Validated requirements:** F0, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13 (all 14 requirements with 54 sub-requirements)

---
