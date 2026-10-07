CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Denormalized projection, populated by consuming booking.created/updated/approved/denied/deleted
-- and location.updated/deleted events, per TechArch §1.4 read-pattern decision.
CREATE TABLE feed_bookings (
    booking_id      UUID PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    location_id     UUID NOT NULL,
    location_name   VARCHAR(255) NOT NULL,
    start_time      TIMESTAMPTZ NOT NULL,
    end_time        TIMESTAMPTZ NOT NULL,
    status          VARCHAR(16) NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_feed_bookings_upcoming ON feed_bookings (status, start_time);
CREATE INDEX idx_feed_bookings_location ON feed_bookings (location_id, start_time);

CREATE OR REPLACE FUNCTION set_updated_at() RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_feed_bookings_updated_at BEFORE UPDATE ON feed_bookings
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
