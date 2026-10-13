-- feed_bookings.location_name is denormalized and NOT NULL (V1 DDL), but booking.* events carry
-- location_id only, not the location's name (booking-service does not own location names —
-- confirmed from Phase 5's buildCreatePayload which emits id/title/location_id/start_time/end_time/status).
-- feeds-service may never query locations-resources-service's DB (database-per-service invariant).
-- This cache is fed by location.updated events (payload carries id + name, confirmed from Phase 4's
-- LocationService writeOutboxEvent) and consulted when projecting a booking, so
-- feed_bookings.location_name can always be satisfied with the last-known real name (or a graceful
-- "Location <uuid>" placeholder until the first location.updated for that id arrives).
--
-- Additive only: V1 created feed_bookings, V2 added processed_events; neither is touched here.
CREATE TABLE location_names (
    location_id    UUID PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
