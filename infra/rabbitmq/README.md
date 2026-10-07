# RabbitMQ Topology Configuration

## Overview

This directory contains the declarative RabbitMQ messaging topology for BookingHub, loaded automatically at broker startup via the management plugin's definitions import.

## Files

- **`definitions.json`**: RabbitMQ management-plugin definitions in the standard import format, declaring:
  - **7 topic exchanges** (one per bounded-context publishing domain events)
  - **11 consumer queues** (notifications, audit log, feeds projections)
  - **11 dead-letter exchange/queue pairs** (one DLX+DLQ per consumer queue)
  - **28+ bindings** (consumer queues to topic exchanges by routing key)
  
- **`rabbitmq.conf`**: RabbitMQ broker configuration enabling definitions import and local-dev loopback relaxation

## Topology Summary

### Topic Exchanges (Publishers)

| Exchange | Routing Keys | Publisher Service |
|----------|--------------|-------------------|
| `booking.events` | `booking.created`, `booking.updated`, `booking.deleted`, `booking.approved`, `booking.denied` | booking-service |
| `location.events` | `location.created`, `location.updated`, `location.deleted` | locations-resources-service |
| `resource.events` | `resource.created`, `resource.updated`, `resource.deleted` | locations-resources-service |
| `customfield.events` | `customfield.created`, `customfield.updated`, `customfield.deleted`, `template.created`, `template.updated`, `template.deleted` | custom-field-service |
| `user.events` | `user.created`, `user.updated`, `role.assigned`, `password.reset.requested` | users-permissions-service |
| `permission.events` | `permission.updated` | users-permissions-service |
| `settings.events` | `settings.updated` | settings-service |

All exchanges are **durable topic exchanges** (type `topic`, `durable: true`).

### Consumer Queues

| Queue | Bound To | Routing Keys | Consumer Service |
|-------|----------|--------------|------------------|
| `notifications.booking.q` | `booking.events` | `booking.created`, `booking.approved`, `booking.denied` | notifications-service |
| `audit.booking.q` | `booking.events` | `booking.*` (all 5: created/updated/deleted/approved/denied) | audit-log-service |
| `feeds.booking.q` | `booking.events` | `booking.*` (all 5) | feeds-service |
| `audit.location.q` | `location.events` | `location.*` (all 3) | audit-log-service |
| `feeds.location.q` | `location.events` | `location.updated`, `location.deleted` | feeds-service |
| `audit.resource.q` | `resource.events` | `resource.*` (all 3) | audit-log-service |
| `audit.customfield.q` | `customfield.events` | `customfield.*`, `template.*` (all 6) | audit-log-service |
| `audit.user.q` | `user.events` | `user.created`, `user.updated`, `role.assigned` | audit-log-service |
| `notifications.passwordreset.q` | `user.events` | `password.reset.requested` | notifications-service |
| `audit.permission.q` | `permission.events` | `permission.updated` | audit-log-service |
| `audit.settings.q` | `settings.events` | `settings.updated` | audit-log-service |

All consumer queues are **durable** (`durable: true`) and declare `x-dead-letter-exchange` pointing to their matching `.dlx` exchange.

### Dead-Letter Infrastructure

For **each of the 11 consumer queues**, the topology declares:

1. A **dead-letter exchange** (fanout): `<queue-name>.dlx`
2. A **dead-letter queue** (durable): `<queue-name>.dlq`
3. A **binding** from the DLX to the DLQ

When a message exhausts retry attempts (3 attempts with 5s/30s/2min backoff, enforced in application code from Phase 6 onward), it is routed to the dead-letter queue rather than being silently dropped.

**Example DLQ set for `notifications.booking.q`:**
- `notifications.booking.q` has `x-dead-letter-exchange: notifications.booking.q.dlx`
- `notifications.booking.q.dlx` (fanout exchange) → bound to → `notifications.booking.q.dlq` (durable queue)

This pattern is repeated for all 11 consumer queues, satisfying TechArch §7.2's explicit "never a silent drop" requirement.

## Import Mechanism

The topology is loaded at RabbitMQ broker boot via the **management plugin's definitions import** (configured in `rabbitmq.conf`):

```conf
management.load_definitions = /etc/rabbitmq/definitions.json
```

Plan 02-12's `docker-compose.yml` mounts this file into the RabbitMQ container:

```yaml
services:
  rabbitmq:
    image: rabbitmq:3.13-management
    volumes:
      - ./infra/rabbitmq/rabbitmq.conf:/etc/rabbitmq/rabbitmq.conf:ro
      - ./infra/rabbitmq/definitions.json:/etc/rabbitmq/definitions.json:ro
```

### Idempotency

The definitions import is **declarative and idempotent**. Re-declaring an existing exchange/queue/binding with identical arguments on broker restart is a no-op. This makes the import safe across container restarts.

## Local Dev Configuration — Security Warning

⚠️ **The `rabbitmq.conf` contains a LOCAL-DEV-ONLY relaxation:**

```conf
loopback_users.guest = false
```

### What This Does

RabbitMQ's default `loopback_users` restriction only permits the built-in `guest` account to authenticate from connections RabbitMQ itself considers **loopback** (`127.0.0.1` from the broker's point of view).

A **host-side `curl` reaching the management API** through Docker's port-forwarded `15672:15672` mapping arrives from the **Docker bridge network**, NOT from `127.0.0.1` as RabbitMQ sees it. Without this relaxation, `guest:guest` would be **rejected** on every host-side management-API call.

### Why It's Required

Plan 02-12 Task 2's RabbitMQ topology verification runs `curl -u guest:guest http://localhost:15672/api/...` from the host to confirm the definitions loaded correctly. Without `loopback_users.guest = false`, this verification would fail with `401 Unauthorized`.

### Security Impact

This is the same category of local-dev exception as:
- Keycloak seed user `admin@bookinghub.local` with password `ChangeMe123!`
- Keycloak's `KEYCLOAK_ADMIN_PASSWORD=admin`
- Any `POSTGRES_PASSWORD` env var in docker-compose

**Before deploying to any shared/staging/production environment:**

1. Remove or comment out `loopback_users.guest = false`
2. Create a dedicated non-guest administrator credential with appropriate permissions
3. Update all services and scripts to use the new credential, NOT `guest:guest`

## Routing Key Design

Per TechArch §7.2, routing keys follow a `<entity>.<action>` pattern:

- **Wildcard binding (`booking.*`)**: Consumer receives ALL events for that entity (audit log uses this — it wants every booking lifecycle event)
- **Specific binding (`booking.created`, `booking.approved`)**: Consumer receives only those specific events (notifications-service uses this — it only cares about created/approved/denied, NOT updated/deleted)

This allows future routing-key additions without breaking existing consumers (add `booking.cancelled` and only consumers bound to `booking.*` or explicitly to `booking.cancelled` will see it).

## Verification

After loading the definitions, verify the topology:

```bash
# List all exchanges (should include booking.events, location.events, etc.)
curl -s -u guest:guest http://localhost:15672/api/exchanges/%2F | jq '.[].name' | grep events

# List all queues (should include notifications.booking.q, audit.booking.q, *.dlq, etc.)
curl -s -u guest:guest http://localhost:15672/api/queues/%2F | jq '.[].name' | sort

# Check bindings for a specific queue
curl -s -u guest:guest http://localhost:15672/api/queues/%2F/notifications.booking.q/bindings | jq '.[] | {source, routing_key}'
```

Full verification is performed in plan 02-12's integration task.

## Integration with Application Code

### Publisher Side (Phase 3+ services)

Services publish events using the **transactional outbox pattern**:

1. Service writes domain event to local `outbox` table **in the same transaction** as business state change
2. Outbox relay process (Debezium CDC or polling publisher) reads committed rows and publishes to the topic exchange
3. Message is published with `routing_key` matching the event type (e.g., `booking.created`)
4. Message is marked `persistent: true` and carries an `idempotency_key` UUID

### Consumer Side (Phase 6+ services)

Services consume events with:

1. **At-least-once delivery** — messages are acknowledged only after processing succeeds
2. **Idempotency** — redelivered `idempotency_key` is a safe no-op (checked against DB or in-memory dedup)
3. **Retry with backoff** — transient failures (DB timeout, SMTP 5xx) are retried 3 times (5s/30s/2min)
4. **Dead-letter routing** — exhausted retries route to the DLQ, surfaced via monitoring alert

No silent drops, per F8/F11's explicit requirement.

---

**Last updated:** 2026-10-07  
**Plan:** 02-09 (Keycloak realm export + RabbitMQ topology definitions)  
**Phase:** 02-platform-foundation-infrastructure
