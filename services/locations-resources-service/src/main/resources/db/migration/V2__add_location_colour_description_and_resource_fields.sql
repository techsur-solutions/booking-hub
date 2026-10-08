-- F0 Open Question #12 (open-questions.md): TechArch's V1 DDL for `locations` omitted
-- `colour` and `description` entirely — both confirmed legacy fields per F0 audit
-- (findings/02-reference-data.md, models/Location.cfc registerSystemFields()).
-- `colour` is a distinct hex-colour field (UI widget: colourpicker), NOT the same
-- field as `css_class` — the FRD's own Inputs section incorrectly conflated the two,
-- which F0 corrected. TechArch was written pre-F0-confirmation; this completes it.
ALTER TABLE locations ADD COLUMN colour VARCHAR(7) NULL;
ALTER TABLE locations ADD COLUMN description VARCHAR(500) NULL;

-- F0 Open Question #11 (open-questions.md): TechArch's V1 DDL for `resources` only
-- declared `name`. F0 confirmed (findings/02-reference-data.md,
-- views/resources/_form.cfm) the full legacy field set also includes `type`
-- (optional grouping/category), `description` (optional), `isunique` (boolean,
-- governs per-resource double-booking restriction semantics — consumed by Phase 5's
-- conflict-detection logic), and `restrictlocations` (optional list of Location ids
-- this Resource may be booked at).
ALTER TABLE resources ADD COLUMN type VARCHAR(255) NULL;
ALTER TABLE resources ADD COLUMN description VARCHAR(500) NULL;
ALTER TABLE resources ADD COLUMN is_unique BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE resources ADD COLUMN restrict_locations JSONB NULL;

-- Transactional outbox (same pattern as Phase 3 plan 03-01 — each service owns its
-- own copy, no shared library across services per microservice isolation).
CREATE TABLE outbox (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  VARCHAR(64) NOT NULL,              -- 'location' | 'resource'
    aggregate_id    UUID NOT NULL,
    exchange        VARCHAR(100) NOT NULL,              -- 'location.events' | 'resource.events' (already declared, Phase 2 plan 02-09)
    routing_key     VARCHAR(100) NOT NULL,              -- 'location.created' | 'location.updated' | 'location.deleted' | 'resource.created' | 'resource.updated' | 'resource.deleted'
    idempotency_key UUID NOT NULL DEFAULT gen_random_uuid(),
    payload         JSONB NOT NULL,
    status          VARCHAR(16) NOT NULL DEFAULT 'pending',
    attempt_count   INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at    TIMESTAMPTZ NULL
);
CREATE INDEX idx_outbox_pending ON outbox (status, created_at) WHERE status = 'pending';
CREATE UNIQUE INDEX uq_outbox_idempotency_key ON outbox (idempotency_key);
