CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE outbox (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  VARCHAR(64) NOT NULL,              -- always 'settings' (singleton — no other aggregate type exists in this service)
    aggregate_id    UUID NOT NULL,                      -- a fixed sentinel UUID representing the singleton (not the integer PK — outbox rows need a UUID-shaped aggregate_id for cross-service consistency with every other service's outbox table)
    exchange        VARCHAR(100) NOT NULL,              -- 'settings.events' (already declared, Phase 2 plan 02-09)
    routing_key     VARCHAR(100) NOT NULL,              -- always 'settings.updated' (only one routing key exists for this aggregate)
    idempotency_key UUID NOT NULL DEFAULT gen_random_uuid(),
    payload         JSONB NOT NULL,
    status          VARCHAR(16) NOT NULL DEFAULT 'pending',
    attempt_count   INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at    TIMESTAMPTZ NULL
);
CREATE INDEX idx_outbox_pending ON outbox (status, created_at) WHERE status = 'pending';
CREATE UNIQUE INDEX uq_outbox_idempotency_key ON outbox (idempotency_key);
