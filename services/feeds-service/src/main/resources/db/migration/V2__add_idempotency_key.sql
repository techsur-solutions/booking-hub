-- Y3-integrations.md mandates idempotent consumption for feeds-service's projection updater, but V1's DDL
-- (TechArch §3.3) gave the projection no idempotency tracking. A single booking's lifecycle produces MULTIPLE
-- distinct events (created -> approved -> updated -> deleted) that each legitimately mutate the SAME projection
-- row by booking_id, so an upsert keyed on booking_id CANNOT distinguish a legitimate later lifecycle event from
-- a redelivery of an already-processed one. A dedup table keyed on the event's own idempotency_key can. This
-- mirrors notifications-service's / audit-log-service's lookup-first precedent (Phase 6), keyed the same way.
--
-- Additive-only migration: it creates exactly one new table and touches no V1 object (the projection table,
-- its two indexes, and its set_updated_at trigger are all left untouched).
CREATE TABLE processed_events (
    idempotency_key  UUID PRIMARY KEY,
    processed_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
-- PRIMARY KEY gives the unique constraint for free; a redelivered key will violate it on insert,
-- which is the final race-safe backstop beyond plan 07-02's lookup-first existsByIdempotencyKey check.
