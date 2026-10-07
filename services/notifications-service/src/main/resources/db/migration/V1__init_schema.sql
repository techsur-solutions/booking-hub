CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE notification_deliveries (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key     VARCHAR(255) NOT NULL UNIQUE,
    event_type          VARCHAR(64) NOT NULL,            -- 'booking.created' | 'booking.approved' | 'booking.denied' | 'password.reset.requested'
    entity_id           UUID NOT NULL,
    status              VARCHAR(16) NOT NULL DEFAULT 'pending', -- 'pending' | 'sent' | 'retrying' | 'dead_lettered'
    attempt_count       INTEGER NOT NULL DEFAULT 0,
    last_attempted_at   TIMESTAMPTZ NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_notification_status ON notification_deliveries (status);

CREATE OR REPLACE FUNCTION set_updated_at() RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_notification_deliveries_updated_at BEFORE UPDATE ON notification_deliveries
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
