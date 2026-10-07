CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE bookings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    series_id       UUID NULL,                       -- shared across occurrences of a recurring series
    title           VARCHAR(255) NOT NULL,
    location_id     UUID NOT NULL,                   -- cross-service reference to locations-resources-service
    start_time      TIMESTAMPTZ NOT NULL,
    end_time        TIMESTAMPTZ NOT NULL,
    status          VARCHAR(16) NOT NULL DEFAULT 'pending', -- 'pending' | 'approved' | 'denied'
    owner_id        UUID NOT NULL,                   -- cross-service reference to Keycloak subject / users-permissions-service
    approved_by     UUID NULL,
    approved_at     TIMESTAMPTZ NULL,
    denied_by       UUID NULL,
    denied_at       TIMESTAMPTZ NULL,
    denial_reason   TEXT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ NULL,
    CONSTRAINT chk_booking_time_range CHECK (end_time > start_time),
    CONSTRAINT chk_booking_status CHECK (status IN ('pending','approved','denied'))
);
CREATE INDEX idx_bookings_location_time ON bookings (location_id, start_time, end_time) WHERE deleted_at IS NULL;
CREATE INDEX idx_bookings_series ON bookings (series_id) WHERE series_id IS NOT NULL;
CREATE INDEX idx_bookings_status ON bookings (status) WHERE deleted_at IS NULL;

CREATE TABLE booking_resources (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id      UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    resource_id     UUID NOT NULL,                   -- cross-service reference to locations-resources-service
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_booking_resource UNIQUE (booking_id, resource_id)
);
CREATE INDEX idx_booking_resources_resource_time ON booking_resources (resource_id, booking_id);

CREATE OR REPLACE FUNCTION set_updated_at() RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_bookings_updated_at BEFORE UPDATE ON bookings
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
