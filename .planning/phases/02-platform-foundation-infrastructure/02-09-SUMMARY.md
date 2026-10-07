---
phase: 02-platform-foundation-infrastructure
plan: 09
subsystem: infra
tags: [keycloak, rabbitmq, realm-export, messaging-topology, domain-events, dead-letter-queues]

# Dependency graph
requires:
  - phase: 01-legacy-functional-audit
    provides: Confirmed bounded-context decomposition and permission flag inventory
provides:
  - Importable Keycloak realm with 11 clients (1 public PKCE frontend, 9 bearer-only services, 1 service-account admin), 11 realm roles, and 1 superuser seed account
  - Importable RabbitMQ topology with 7 topic exchanges, 11 consumer queues, 11 DLX+DLQ pairs, and 28+ routing-key bindings
affects: [02-12, 03, 04, 05, 06, 07]

# Tech tracking
tech-stack:
  added: [keycloak-realm-export, rabbitmq-definitions, management-plugin]
  patterns: [infrastructure-as-code, dead-letter-queues, transactional-outbox-ready, pkce-authentication]

key-files:
  created:
    - infra/keycloak/realm-export.json
    - infra/keycloak/README.md
    - infra/rabbitmq/definitions.json
    - infra/rabbitmq/rabbitmq.conf
    - infra/rabbitmq/README.md
  modified: []

key-decisions:
  - "Keycloak realm export uses placeholder client secrets (CHANGE_ME_*) and seed password (ChangeMe123!) — documented as local-dev-only, requiring rotation before any non-local deployment"
  - "RabbitMQ loopback_users.guest=false relaxation added to enable host-side management-API verification via guest:guest — documented as local-dev-only security exception"
  - "userperm-admin-client service account granted only manage-users/view-users realm-management roles, NOT full admin — scopes Admin API blast radius to account provisioning/credential-reset per TechArch §5.1"
  - "Every consumer queue declares x-dead-letter-exchange pointing to a matching DLX/DLQ pair — implements TechArch §7.2 'never a silent drop' requirement at topology level before any consumer code exists"

patterns-established:
  - "Infrastructure as code: Keycloak realm and RabbitMQ topology as version-controlled JSON, imported at container boot via --import-realm and management.load_definitions"
  - "Idempotent imports: Both Keycloak --import-realm and RabbitMQ definitions import are no-ops on restart when entities already exist with matching config"
  - "Dead-letter infrastructure: One DLX (fanout) + DLQ (durable) pair per consumer queue, satisfying retry-exhaustion requirement before Phase 6 consumer implementation"

# Metrics
duration: 3 min
completed: 2026-10-07
---

# Phase 02 Plan 09: Keycloak Realm Export + RabbitMQ Topology Summary

**Declarative Keycloak realm (11 clients, 11 roles, 1 superuser seed) and RabbitMQ topology (7 exchanges, 11 queues, 11 DLQ pairs) as version-controlled infrastructure-as-code, importable at container boot**

## Performance

- **Duration:** 3 min
- **Started:** 2026-10-07T03:36:20Z
- **Completed:** 2026-10-07T03:40:09Z
- **Tasks:** 2
- **Files modified:** 5

## Accomplishments

- Keycloak realm `bookinghub` with complete authentication/authorization substrate: 1 public PKCE frontend client, 9 confidential bearer-only service clients, 1 service-account Admin API client, 11 realm roles mapping to legacy permission flags, and 1 superuser seed account holding all roles for end-to-end testing
- RabbitMQ topology covering every domain event from TechArch §7.2: 7 topic exchanges (one per bounded context), 11 durable consumer queues with routing-key bindings, and 11 dead-letter exchange/queue pairs ensuring no message is ever silently dropped
- Infrastructure-as-code pattern established: both realm and topology are version-controlled JSON files, imported idempotently at container boot via standard Keycloak/RabbitMQ mechanisms

## Task Commits

Each task was committed atomically:

1. **Task 1: Keycloak realm export (clients, roles, seed user)** - `444fa08` (feat)
2. **Task 2: RabbitMQ topology definitions (exchanges, queues, DLQs)** - `4782953` (feat)

**Plan metadata:** (next commit) (docs: complete plan)

## Files Created/Modified

### Created

- **`infra/keycloak/realm-export.json`** - Complete Keycloak realm export in admin-console format: realm `bookinghub`, 11 clients (bookinghub-frontend as public PKCE, 9 bearer-only services, userperm-admin-client service account), 11 realm roles per TechArch §5.2, 1 seed user `admin@bookinghub.local` with all roles
- **`infra/keycloak/README.md`** - Documents import mechanism (--import-realm + volume mount), idempotency behavior, security warnings for placeholder secrets/passwords (local-dev-only), and Admin API client role scoping
- **`infra/rabbitmq/definitions.json`** - RabbitMQ management-plugin definitions JSON: 7 topic exchanges (booking.events, location.events, resource.events, customfield.events, user.events, permission.events, settings.events), 11 consumer queues (notifications.booking.q, audit.booking.q, feeds.booking.q, audit.location.q, feeds.location.q, audit.resource.q, audit.customfield.q, audit.user.q, notifications.passwordreset.q, audit.permission.q, audit.settings.q) with routing-key bindings, 11 DLX+DLQ pairs
- **`infra/rabbitmq/rabbitmq.conf`** - RabbitMQ config enabling management plugin definitions import (management.load_definitions) and loopback_users.guest=false relaxation for local-dev host-side management-API access
- **`infra/rabbitmq/README.md`** - Documents full exchange/queue/binding table, import mechanism (idempotent via management.load_definitions), local-dev security warning for loopback relaxation, routing-key design rationale, and verification curl commands

## Decisions Made

1. **Keycloak client secrets as placeholders** - All 10 confidential client secrets set to `CHANGE_ME_<client>_secret` pattern, explicitly documented in `infra/keycloak/README.md` as requiring regeneration (e.g., `openssl rand -base64 32`) and Kubernetes Secret storage before any non-local deployment — commits real infrastructure-as-code without committing real production secrets
2. **Seed user password as local-dev fixture** - Seed user `admin@bookinghub.local` password is `ChangeMe123!` (non-temporary), documented in README as local-dev-only and requiring deletion/rotation before any shared environment — satisfies platform's "no login screen nobody can log into" rule for preview/testing from this phase onward
3. **Admin API client role scoping** - `userperm-admin-client` service account granted ONLY `manage-users` and `view-users` realm-management client roles (not full `admin` role) — scopes Admin API blast radius to account provisioning/credential-reset per TechArch §5.1's "the one place in the architecture a service calls Keycloak as an administrative actor"
4. **RabbitMQ loopback relaxation as local-dev exception** - `loopback_users.guest = false` added to `rabbitmq.conf` so host-side `curl -u guest:guest localhost:15672/api/...` works through Docker port-forwarding (arrives from bridge network, not 127.0.0.1) — required for plan 02-12's RabbitMQ verification, documented in README as same-category exception as Keycloak seed password and requiring replacement with non-guest credential before non-local deployment
5. **DLQ topology upfront** - Every consumer queue declared with `x-dead-letter-exchange` and a matching DLX+DLQ pair at the topology level (before any Phase 6 consumer code exists) — implements TechArch §7.2's "never a silent drop" requirement structurally, so retry-exhaustion handling is correct-by-construction when consumers are later added

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

Ready for plan 02-10 (Spring Cloud Gateway) and 02-11 (React SPA) — both will consume this realm's clients/roles for JWT validation and PKCE login respectively. Plan 02-12 (docker-compose integration) will mount these files into Keycloak and RabbitMQ containers and verify import succeeded.

---
*Phase: 02-platform-foundation-infrastructure*  
*Completed: 2026-10-07*

## Self-Check: PASSED

✅ All created files exist on disk  
✅ Both commits (444fa08, 4782953) present in git history  
✅ Both JSON files parse successfully  
✅ Verification commands from plan Tasks 1 and 2 pass
