
## 6. Technology Stack

All entries below marked **(mandated)** are hard constraints from `PROJECT.md`/PRD Section 4 and are not open for substitution. Entries marked **(TechArch choice)** are implementation-level decisions made within those constraints.

| Layer | Technology | Version | Purpose |
|---|---|---|---|
| Frontend framework | React | 18.x | Single-page application UI |
| Frontend language | TypeScript | 5.x | Type-safe frontend development **(mandated)** |
| Frontend build tooling | Vite | 5.x | Dev server + production bundling (TechArch choice) |
| Frontend calendar rendering | FullCalendar (React wrapper) | 6.x | Calendar/day view rendering — functional equivalent of legacy FullCalendar.js, not a pixel-match requirement |
| Frontend OIDC client | `keycloak-js` (or equivalent PKCE-capable OIDC client) | latest stable | Authorization Code + PKCE login flow against Keycloak |
| API Gateway | Spring Cloud Gateway (WebFlux) | Spring Cloud 2023.x / Spring Boot 3.3.x | Single ingress, JWT validation, coarse-grained routing/RBAC **(mandated)** |
| Backend language/runtime | Java | 21 (LTS) | Backend service implementation language **(mandated)** |
| Backend framework | Spring Boot | 3.3.x | Each bounded-context microservice **(mandated)** |
| Backend web layer (business services) | Spring Web MVC (`spring-boot-starter-web`) | bundled w/ Spring Boot 3.3.x | Synchronous REST controllers (all services except Gateway) |
| Backend data access | Spring Data JPA + Hibernate | bundled w/ Spring Boot 3.3.x | Repository layer over each service's PostgreSQL database |
| Database migrations | Flyway | 10.x | Versioned DDL migrations per service (one Flyway history per database) |
| Database | PostgreSQL | 16.x | One database per microservice, no shared schema **(mandated)** |
| DB extensions | `pgcrypto` | bundled w/ PG 16 | `gen_random_uuid()` for UUID primary keys |
| Messaging broker | RabbitMQ | 3.13.x | Inter-service domain events, durable queues, DLQs **(mandated)** |
| Messaging client | Spring AMQP (`spring-boot-starter-amqp`) | bundled w/ Spring Boot 3.3.x | Publisher (outbox relay) and consumer bindings in every event-participating service |
| Outbox relay | Debezium (Postgres CDC) **or** a scheduled polling-publisher bean | Debezium 2.x (if adopted) | Transactional outbox → RabbitMQ relay, per §1.5 decision (TechArch choice between CDC-based or poll-based outbox, finalized during implementation) |
| Identity & Access Management | Keycloak | 25.x | Authentication, session/token management, realm roles/scopes **(mandated)** |
| Keycloak protocol | OpenID Connect (OIDC) / OAuth2, JWT (RS256) | OIDC 1.0 | Frontend login (PKCE), service-to-service bearer validation |
| API documentation | springdoc-openapi (OpenAPI 3) | 2.x | Per-service OpenAPI spec, aggregated at the Gateway for a unified API catalog |
| Containerization | Docker | 24.x+ | One image per service, per the frontend, and reference Dockerfiles for local Keycloak/RabbitMQ/Postgres **(mandated)** |
| Orchestration | Kubernetes | 1.29+ | Deployment/Service/ConfigMap/Secret manifests per service, independent scaling **(mandated)** |
| Package manifests | Helm charts (one per service, or a shared chart with per-service values) | Helm 3.x | Templated Kubernetes manifests (TechArch choice) |
| Email delivery | SMTP relay or transactional email API (provider TBD — e.g., SendGrid/SES/Postmark) | — | notifications-service outbound email (per `Y3-integrations.md`, "exact provider TBD in TechArch" — left as an infrastructure/ops decision, not a functional one) |
| Observability — metrics | Micrometer + Prometheus | bundled w/ Spring Boot 3.3.x / Prometheus 2.x | Per-service metrics export |
| Observability — tracing | Micrometer Tracing (Brave or OpenTelemetry bridge) | bundled w/ Spring Boot 3.3.x | Distributed trace correlation across Gateway → service → RabbitMQ consumer hops |
| Observability — logging | Structured JSON logging (Logback + `logstash-logback-encoder`) shipped to a log aggregator (e.g., ELK/Loki) | — | Centralized log search, correlated via trace ID |
| CI/CD | GitHub Actions (or equivalent) running: build → test → ArchUnit architecture-rule checks → Docker build → Helm/K8s deploy | — | Enforces F12's architectural invariants (no cross-service DB access, no non-Gateway ingress) as automated CI gates, not just documentation |
| Testing — unit/integration | JUnit 5, Mockito, Testcontainers (Postgres + RabbitMQ) | JUnit 5.10.x, Testcontainers 1.19.x | Backend test pyramid per F13's traceability requirement |
| Testing — frontend | Vitest + React Testing Library | latest stable | Frontend component/unit tests |
| Testing — end-to-end | Playwright | latest stable | Full booking-creation-to-notification flow tests (F13 high-risk coverage) |
| Testing — architecture rules | ArchUnit | 1.3.x | CI-enforced checks: no cross-service DB dependency, no non-Gateway `Ingress`, permission-flag-to-mapping-table consistency |

### 6.1 Per-Service Database Naming Convention

| Service | Database Name |
|---|---|
| booking-service | `booking_db` |
| locations-resources-service | `locres_db` |
| custom-field-service | `customfld_db` |
| users-permissions-service | `userperm_db` |
| notifications-service | `notif_db` |
| feeds-service | `feeds_db` |
| settings-service | `settings_db` |
| audit-log-service | `audit_db` |
| (optional) F13 traceability tooling | `traceability_db` |

Each database runs as its own PostgreSQL instance (or, at minimum, its own logical database within a shared PostgreSQL cluster with per-database credentials and no cross-database `postgres_fdw`/`dblink` configured) — the deployment topology diagram in §1.3 treats "one database per service" as a hard boundary enforced at the infrastructure level, not merely a naming convention.
