# Functional Requirements Document: Booking-Hub

**Project Acronym:** BookingHub
**Document Type:** FRD (Functional Requirements Document)
**Status:** Draft — Derived from PRD-BookingHub.md v1 (2026-10-06)
**Generated:** 2026-10-06

---

## Scope

This FRD specifies the exact functional behavior for every feature (F0–F13) identified in `PRD-BookingHub.md`, grounded in the known legacy domain model documented in `.planning/PROJECT.md` (the OxAlto RoomBooking ColdFusion/cfWheels application). Per the project's hard constraint on zero functional regression, every rule specified here either (a) restates a confirmed legacy behavior with a legacy reference, or (b) is explicitly marked **`[OPEN QUESTION — deferred to F0]`** when the legacy behavior is not yet confirmed from the initial scan. No rule in this document invents unconfirmed legacy behavior; where PRD Section 10 lists an open question relevant to a feature, that question is restated verbatim in the feature's Validation/Process/Error sections rather than silently resolved. Every rule in this document is written to be independently testable, feeding directly into F13's audit → requirement → test traceability matrix.

## Conventions

- **Feature IDs** (`F0`–`F13`) are stable references matching the PRD feature index; chunk filenames use zero-padded `F00`–`F13` for sort order only — the functional ID remains `F0`–`F13` in prose and cross-references.
- **`[OPEN QUESTION — deferred to F0]`** marks any rule whose exact legacy behavior is unconfirmed. These are not implementation gaps — they are explicit flags that F0 (Legacy Functional Audit) must resolve via code/doc/demo evidence before the corresponding implementation is finalized, per PRD Section 10's resolution process. Implementers must not guess at these; the interim/default behavior stated (where given) is a safe placeholder only, subject to F0 confirmation.
- **Service ownership:** Each feature chunk names the owning bounded-context service(s) per PRD Section 4's indicative decomposition (Booking, Location & Resource, Custom Field, User/Identity-adjacent + Keycloak, Notification, Public Feed, Settings, Audit Log services). Service boundaries are indicative pending F0/TechArch confirmation.
- **Cross-references:** `see F03 §Process step 2` style references point to another feature chunk's section. `see Y0 §Booking` points to the consolidated schema chunk's domain subsection. `see Y1 §Auth` points to the consolidated API chunk's domain subsection.
- **HTTP/error conventions:** All error tables use HTTP status, a stable machine-readable error code, and a human-readable message. The full cross-feature error catalog is in `Y2-errors.md`; per-feature chunks list only errors specific to that feature's primary flows.
- **Testability requirement:** Every bullet under Validation and every row under Error States in every feature chunk must be phrased as a single, independently verifiable assertion (one behavior per bullet/row) so it maps 1:1 to a test case in F13's traceability matrix.

## Shared Terminology (cross-feature)

- **Event (legacy) / Booking (new):** The legacy `Event` model is renamed `Booking` in all new-system specs; legacy field/table names are noted in parentheses where relevant for audit traceability.
- **Eventresource (legacy) / BookingResource (new):** The join entity associating a Booking with one or more Resources.
- **Bounded-context service:** A Spring Boot 3 microservice owning exactly one PostgreSQL database/schema, per PRD Section 4 and the `Service isolation` non-functional requirement.
- **Domain event:** A RabbitMQ message published by one service and consumed by one or more other services, replacing legacy synchronous side effects (e.g., `booking.created`, `booking.approved`, `booking.denied`, `password.reset.requested`).
- **Auto-approve:** The behavior driven by the global `approveBooking` Settings flag (F10) where a new booking's initial status is `approved` instead of `pending` when the flag is disabled. See F1 §Process, F3 §Process.
- **Permission flag:** A named legacy access-control gate (e.g., `accessCalendar`, `allowRoomBooking`, `viewRoomBooking`, `allowApproveBooking`, `accessPermissions`, `allowAPI`) re-implemented as a Keycloak role/scope per F7. The named set is non-exhaustive per PRD; F0 must confirm the complete set.
- **Actor:** The authenticated principal (Keycloak subject) performing a state-changing action, recorded in every audit log entry (F11).
- **Traceability matrix:** The F13 artifact linking each F0 audit finding → FRD requirement (this document) → implementation → automated test.

## Master Table of Contents

| Chunk | Contents |
|---|---|
| `00-header.md` | This file — conventions, shared terminology |
| `F00-legacy-functional-audit.md` | F0: Legacy Functional Audit |
| `F01-booking-management-service.md` | F1: Booking Management Service |
| `F02-conflict-detection.md` | F2: Conflict Detection |
| `F03-approval-workflow.md` | F3: Approval Workflow |
| `F04-locations-resources-management.md` | F4: Locations & Resources Management |
| `F05-custom-fields.md` | F5: Custom Fields |
| `F06-user-role-management.md` | F6: User & Role Management |
| `F07-permission-system.md` | F7: Permission System |
| `F08-notifications.md` | F8: Notifications |
| `F09-public-feeds.md` | F9: Public Feeds |
| `F10-settings.md` | F10: Settings |
| `F11-activity-audit-logging.md` | F11: Activity/Audit Logging |
| `F12-microservice-architecture-platform.md` | F12: Microservice Architecture & Platform |
| `F13-regression-verification-test-traceability.md` | F13: Regression Verification & Test Traceability |
| `Y0-schema.md` | Consolidated database DDL, all services |
| `Y1-api.md` | Consolidated REST API endpoint catalog |
| `Y2-errors.md` | Cross-feature error catalog |
| `Y3-integrations.md` | External integration points (Keycloak, RabbitMQ, SMTP) |

---
