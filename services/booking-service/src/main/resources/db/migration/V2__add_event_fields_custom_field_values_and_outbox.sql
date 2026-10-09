-- V2: F0 schema completion + custom_field_values DB-ownership fix + transactional outbox
-- Additive migration on top of Phase 2's V1 (plan 02-01, already executed). Do NOT touch V1.

-- ─── F0 schema completion ───────────────────────────────────────────────────
-- (findings/01-booking-core.md, Event model registerSystemFields())
-- TechArch's V1 DDL (Phase 2 plan 02-01, already executed) covers only title/location_id/
-- start_time/end_time/status + workflow columns + series_id. These six additional confirmed
-- legacy Event fields were omitted entirely from TechArch's placeholder schema.
ALTER TABLE bookings ADD COLUMN all_day BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE bookings ADD COLUMN description TEXT NULL;
ALTER TABLE bookings ADD COLUMN layout_style VARCHAR(255) NULL;
ALTER TABLE bookings ADD COLUMN contact_name VARCHAR(255) NULL;
ALTER TABLE bookings ADD COLUMN contact_email VARCHAR(320) NULL;
ALTER TABLE bookings ADD COLUMN contact_no VARCHAR(50) NULL;

-- Named decision: `emailcontact` is explicitly NOT added as a column here. F0 confirmed it is a
-- transient, request-only virtual flag read exactly once by Bookings.create()'s email-trigger
-- check and never referenced in update() or anywhere else. It is carried as a field on
-- BookingCreateRequest (plan 05-03) and in the booking.created event payload only — never
-- persisted. Do not add an email_contact column.

-- ─── custom_field_values DB-ownership fix ────────────────────────────────────
-- Named decision (closes the custom_field_values DB-ownership contradiction): TechArch §2.4/
-- §1.5 states "booking-service persists custom_field_values rows itself" while TechArch §3.3
-- declares the custom_field_values table owned by custom-field-service's OWN database
-- (customfld_db), with a live FK to custom_fields(id) WITHIN that database. Phase 2's already-
-- executed database-per-service decision (Success Criterion 2: "no shared schema and no cross-
-- service database connection string") makes the literal TechArch instruction architecturally
-- impossible — booking-service has no connection string into customfld_db and must not be given
-- one. Resolution: booking-service owns a LOCAL table in its own booking_db, referencing
-- custom_field_id by UUID only (a cross-service reference — the identical pattern already
-- established for location_id/resource_id in this same table), validated not via a DB-level FK
-- but via a synchronous call to custom-field-service's applicability endpoint (plan 05-02). This
-- preserves TechArch's actual intent (booking-service owns and writes this data) while fixing the
-- self-contradictory DDL placement.
--
-- KNOWN, ACCEPTED CONSEQUENCE (explicitly acknowledged, not silently introduced): Phase 2 plan
-- 02-03 already executed custom-field-service's V1__init_schema.sql, which contains a REAL,
-- deployed, FK-constrained `custom_field_values` table (customfld_db, custom_field_id REFERENCES
-- custom_fields(id)) built directly from TechArch's self-contradictory §3.3 text. This decision
-- renders that Phase-2-executed table permanently UNUSED — booking-service will never write to
-- it, and nothing else has a reason to. This is accepted as known, harmless debt (an empty,
-- never-written table costs nothing at runtime) rather than fixed here, since dropping it would
-- mean editing an already-executed Phase 2 migration file, which is a materially bigger change
-- than this plan's own additive corrections. Recommended follow-up: a future Phase-2-scoped
-- cleanup migration (owned by custom-field-service, not this plan) should DROP TABLE
-- custom_field_values, cross-referencing this plan (05-01) as the reason it became dead weight.
CREATE TABLE booking_custom_field_values (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id      UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    custom_field_id UUID NOT NULL,                    -- cross-service reference to custom-field-service; no FK (different database)
    value           TEXT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_booking_custom_field UNIQUE (booking_id, custom_field_id)
);
CREATE INDEX idx_booking_custom_field_values_booking ON booking_custom_field_values (booking_id);

-- ─── Transactional outbox ─────────────────────────────────────────────────────
-- Same pattern as every other service in this project: Phase 3 plan 03-01,
-- Phase 4 plans 04-01/04-03/04-05. Written in Task 1 here (not a later plan)
-- specifically so the scheduled relay bean exists and is independently testable
-- BEFORE any business-logic plan writes to it — plans 05-03 and 05-04 run in the
-- SAME wave in parallel and both need to publish outbox rows; whichever created
-- OutboxPublisher.java first would create a file-ownership race. Centralizing it
-- here, proven against a directly-inserted row (not a controller-created one),
-- avoids that race entirely.
CREATE TABLE outbox (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  VARCHAR(64) NOT NULL,              -- 'booking'
    aggregate_id    UUID NOT NULL,
    exchange        VARCHAR(100) NOT NULL,              -- 'booking.events' (already declared, Phase 2 plan 02-09)
    routing_key     VARCHAR(100) NOT NULL,              -- 'booking.created' | 'booking.updated' | 'booking.deleted' | 'booking.approved' | 'booking.denied'
    idempotency_key UUID NOT NULL DEFAULT gen_random_uuid(),
    payload         JSONB NOT NULL,
    status          VARCHAR(16) NOT NULL DEFAULT 'pending',
    attempt_count   INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at    TIMESTAMPTZ NULL
);
CREATE INDEX idx_outbox_pending ON outbox (status, created_at) WHERE status = 'pending';
CREATE UNIQUE INDEX uq_outbox_idempotency_key ON outbox (idempotency_key);
