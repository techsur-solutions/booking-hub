-- F0-driven schema completion: TechArch's V1 DDL for custom_fields omitted the
-- `required` column entirely, even though F0 (findings/03-custom-fields.md) confirmed
-- it exists in legacy as a real tinyint(1) flag. Point 11 of this phase's scope: this
-- service's job is to correctly STORE and RETURN this flag so Phase 5's booking-service
-- can enforce it at submission time — enforcement itself is explicitly out of scope here,
-- matching F0's confirmed legacy behavior that `required` was never checked server-side
-- either (only an HTML5 attribute on the rendered input).
ALTER TABLE custom_fields ADD COLUMN required BOOLEAN NOT NULL DEFAULT false;

-- Transactional outbox (same pattern as every other service in this phase/Phase 3 —
-- each service owns its own copy, no shared library, per microservice isolation).
CREATE TABLE outbox (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  VARCHAR(64) NOT NULL,              -- 'customfield' | 'template'
    aggregate_id    UUID NOT NULL,
    exchange        VARCHAR(100) NOT NULL,              -- 'customfield.events' (already declared, Phase 2 plan 02-09)
    routing_key     VARCHAR(100) NOT NULL,              -- 'customfield.created' | 'customfield.updated' | 'customfield.deleted' | 'template.created' | 'template.updated' | 'template.deleted'
    idempotency_key UUID NOT NULL DEFAULT gen_random_uuid(),
    payload         JSONB NOT NULL,
    status          VARCHAR(16) NOT NULL DEFAULT 'pending',
    attempt_count   INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at    TIMESTAMPTZ NULL
);
CREATE INDEX idx_outbox_pending ON outbox (status, created_at) WHERE status = 'pending';
CREATE UNIQUE INDEX uq_outbox_idempotency_key ON outbox (idempotency_key);
