## F12: Microservice Architecture & Platform

**Description:** F12 is the foundational infrastructure substrate every other feature runs on: the React+TypeScript frontend, the bounded-context Spring Boot 3 service decomposition, the one-database-per-service PostgreSQL topology, the RabbitMQ event backbone, Keycloak identity integration, Spring Cloud Gateway as single ingress, and Docker/Kubernetes deployment. Unlike F0–F11, F12 has no legacy functional equivalent — it is net-new infrastructure, informed by the bounded contexts F0 discovers.

**Terminology:**
- **Bounded-context service:** A Spring Boot 3 microservice with its own PostgreSQL database, deployable independently of all others.
- **Single ingress:** The architectural rule that all external client traffic enters exclusively through Spring Cloud Gateway; no backend service is directly externally addressable.
- **Domain event topology:** The full set of RabbitMQ exchanges/queues/routing keys covering every cross-service event referenced across F1–F11.

**Sub-features:**
- React + TypeScript single-page frontend
- Java 21 + Spring Boot 3 bounded-context services
- PostgreSQL per-service databases (no shared schema)
- RabbitMQ domain event topology
- Keycloak realm/client configuration
- Spring Cloud Gateway routing and coarse-grained auth
- Docker images and Kubernetes manifests per service

**Process:**
1. TechArch (informed by F0's confirmed bounded contexts) finalizes the service decomposition: indicatively Booking Service (F1, F2, F3), Location & Resource Service (F4), Custom Field Service (F5), User/Identity-adjacent Service + Keycloak (F6, F7), Notification Service (F8), Public Feed Service (F9), Settings Service (F10), Audit Log Service (F11).
2. Each service is scaffolded as an independent Spring Boot 3 application with its own Maven/Gradle build, its own PostgreSQL database/schema (no service queries another's schema directly), and its own Docker image.
3. RabbitMQ exchanges/queues/routing keys are defined for every domain event referenced in F1–F11 (`booking.created`, `booking.updated`, `booking.deleted`, `booking.approved`, `booking.denied`, `password.reset.requested`, `location.*`, `resource.*`, `user.*`, `role.*`, `settings.updated`, `permission.updated`), each with durable queues, retry/backoff policy, and dead-letter routing per F8/F11's requirements.
4. Keycloak realm is configured with clients for the frontend (public client, PKCE) and each backend service (confidential/bearer-only clients as appropriate), and roles/scopes are defined per F7's permission mapping table.
5. Spring Cloud Gateway is configured with routes to every backend service, coarse-grained role/scope-based access rules per F7, and is the sole externally reachable ingress — backend services are deployed with no external-facing Kubernetes Service/Ingress of their own.
6. React + TypeScript frontend is built covering every user-facing screen required by F1–F10 (calendar/list/day booking views, booking create/edit/detail modal, admin CRUD screens for locations/resources/custom fields/users/roles/settings, audit log viewer, public feed landing pages, display board), communicating exclusively through the Gateway.
7. Each service's Docker image and Kubernetes manifests (Deployment, Service, ConfigMap/Secret) are authored so the service can be deployed/scaled independently of all others, per the "Deployability" NFR, and demonstrated via an independent-deploy test in staging (PRD Section 7 success metric).
8. Cross-domain read patterns identified in PRD Risks (e.g., Booking + Custom Field + Location data needed in one list/detail view) are addressed via an explicit read-model strategy — API composition at the Gateway/BFF layer or service-level denormalized read views — decided in TechArch and documented per affected feature (F1, F5, F9).

**Inputs:**
- Confirmed bounded-context list (from F0/TechArch).
- Full domain event inventory (derived from F1–F11 feature chunks in this document).
- Keycloak role/scope definitions (from F7's permission mapping table).

**Outputs:**
- One deployable Spring Boot 3 service per bounded context, each with its own Docker image.
- One PostgreSQL database per service (see `Y0-schema.md` for per-service schema ownership).
- RabbitMQ topology definition (exchanges/queues/bindings) — see `Y3-integrations.md`.
- Keycloak realm export/configuration.
- Spring Cloud Gateway route configuration.
- Kubernetes manifests per service.
- React + TypeScript frontend application.

**Validation:**
- No service's codebase or runtime directly connects to another service's PostgreSQL database (verifiable via network policy / connection-string audit).
- No backend service is reachable from outside the cluster except through Spring Cloud Gateway (verifiable via Kubernetes Service/Ingress configuration audit).
- Every domain event referenced in any F1–F11 feature chunk has a corresponding RabbitMQ exchange/queue/routing key defined in the topology.
- Every permission flag in F7's mapping table has a corresponding Keycloak role/scope defined in the realm configuration.
- Each service can be built, deployed, and scaled via its own Kubernetes manifests without requiring changes to or redeployment of any other service's manifests (demonstrated in staging, per PRD Section 7).

**Error States:**
| Scenario | Impact | Required Action |
|---|---|---|
| A service's codebase includes a direct connection string/driver reference to another service's database | Violates service-isolation NFR; silent coupling | Reject in code review / architecture test (e.g., ArchUnit rule); refactor to API or event-based access |
| A backend service is exposed via its own externally-reachable Kubernetes Service/Ingress | Violates single-ingress NFR | Reject in deployment review; route exclusively through Gateway |
| A domain event is published without a corresponding consumer queue/binding | Event silently lost | Block deployment of the publishing service until the topology is updated |
| A service cannot be deployed independently (hidden coordinated-deploy dependency) | Violates "Deployability" NFR | Identify and remove the hidden dependency (e.g., shared migration ordering, implicit startup-order assumption) before release |

**API Surface (this feature):** N/A — F12 is infrastructure; the APIs it hosts are specified per-feature (F1–F11) and consolidated in `Y1-api.md`. The Gateway's route table is documented in `Y1-api.md` §Gateway Routing.

**Schema Surface (this feature):** N/A directly — F12 defines the per-service database topology that every other feature's schema (see `Y0-schema.md`) is deployed under; F12 owns no business data of its own.
