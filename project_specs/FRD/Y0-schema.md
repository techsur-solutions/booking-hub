## Y0: Database Schema (Consolidated)

This chunk consolidates the DDL for every service's PostgreSQL database. **Each subsection is an independent database/schema owned exclusively by the named service** — no foreign keys cross service boundaries; cross-service references are by ID only, resolved via API calls (per the "Service isolation" NFR). All tables use `UUID` primary keys (`gen_random_uuid()` default, `pgcrypto`/`uuid-ossp` extension) unless noted; all tables include `created_at`/`updated_at` timestamps with trigger-maintained `updated_at`.

### §Booking (owned by Booking Service) — supports F1, F2, F3

```sql
CREATE TABLE bookings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    series_id       UUID NULL,                       -- shared across occurrences of a recurring series
    title           VARCHAR(255) NOT NULL,
    location_id     UUID NOT NULL,                   -- cross-service reference to Location & Resource Service
    start_time      TIMESTAMPTZ NOT NULL,
    end_time        TIMESTAMPTZ NOT NULL,
    status          VARCHAR(16) NOT NULL DEFAULT 'pending', -- 'pending' | 'approved' | 'denied'
    owner_id        UUID NOT NULL,                   -- cross-service reference to Keycloak subject / User service
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
    resource_id     UUID NOT NULL,                   -- cross-service reference to Location & Resource Service
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_booking_resource UNIQUE (booking_id, resource_id)
);
CREATE INDEX idx_booking_resources_resource_time ON booking_resources (resource_id, booking_id);
```

### §LocationResource (owned by Location & Resource Service) — supports F4

```sql
CREATE TABLE locations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    css_class       VARCHAR(100) NULL,
    building        VARCHAR(255) NULL,
    layout          JSONB NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ NULL
);
CREATE INDEX idx_locations_active ON locations (id) WHERE deleted_at IS NULL;

CREATE TABLE resources (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ NULL
);
CREATE INDEX idx_resources_active ON resources (id) WHERE deleted_at IS NULL;
```

### §CustomField (owned by Custom Field Service) — supports F5

```sql
CREATE TABLE custom_fields (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    label           VARCHAR(255) NOT NULL,
    field_type      VARCHAR(32) NOT NULL,             -- 'text' | 'number' | 'date' | 'select' (pending F0 confirmation)
    options         JSONB NULL,                        -- required when field_type = 'select'
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ NULL
);

CREATE TABLE custom_field_templates (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    context_id      UUID NULL,                         -- e.g., Location id this template applies to; NULL = global
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE custom_field_joins (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    custom_field_template_id UUID NOT NULL REFERENCES custom_field_templates(id) ON DELETE CASCADE,
    custom_field_id          UUID NOT NULL REFERENCES custom_fields(id) ON DELETE CASCADE,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_template_field UNIQUE (custom_field_template_id, custom_field_id)
);

CREATE TABLE custom_field_values (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id      UUID NOT NULL,                     -- cross-service reference to Booking Service
    custom_field_id UUID NOT NULL REFERENCES custom_fields(id),
    value           TEXT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_custom_field_values_booking ON custom_field_values (booking_id);
```

### §User (owned by User/Identity-adjacent Service; credentials owned by Keycloak realm, not this schema) — supports F6

```sql
CREATE TABLE users (
    id              UUID PRIMARY KEY,                  -- matches Keycloak subject (sub claim), not independently generated
    email           VARCHAR(320) NOT NULL UNIQUE,
    display_name    VARCHAR(255) NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ NULL
);
-- No password/hash/salt columns: credential storage is delegated entirely to Keycloak, per "Standards-based identity" NFR.
```

### §Permission (owned by Permission System, if supplementary mapping-metadata store is needed beyond Keycloak roles) — supports F7

```sql
CREATE TABLE permissions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    legacy_flag         VARCHAR(100) NOT NULL UNIQUE,   -- e.g., 'accessCalendar', 'allowRoomBooking'
    keycloak_role       VARCHAR(100) NOT NULL,
    gated_actions       JSONB NULL,
    confirmed           BOOLEAN NOT NULL DEFAULT false, -- true only once F0 confirms exact gating scope
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

### §Notification (owned by Notification Service) — supports F8

```sql
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
```

### §Feed (owned by Public Feed Service, optional materialized read-model) — supports F9

```sql
-- Optional denormalized projection, populated by consuming booking.created/approved/denied/deleted events,
-- used only if API composition at request time proves insufficient per TechArch decision.
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
```

### §Settings (owned by Settings Service) — supports F10

```sql
CREATE TABLE settings (
    id                   INTEGER PRIMARY KEY DEFAULT 1, -- singleton row enforced by CHECK below
    approve_booking      BOOLEAN NOT NULL DEFAULT true,
    calendar_slot_size   INTEGER NOT NULL DEFAULT 30,    -- minutes
    calendar_min_time    TIME NOT NULL DEFAULT '08:00',
    calendar_max_time    TIME NOT NULL DEFAULT '18:00',
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by           UUID NULL,
    CONSTRAINT chk_settings_singleton CHECK (id = 1),
    CONSTRAINT chk_settings_calendar_range CHECK (calendar_min_time < calendar_max_time),
    CONSTRAINT chk_settings_slot_size CHECK (calendar_slot_size > 0)
);
INSERT INTO settings (id) VALUES (1) ON CONFLICT (id) DO NOTHING;
```

### §AuditLog (owned by Audit Log Service) — supports F11

```sql
CREATE TABLE audit_log_entries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id        UUID NOT NULL,
    occurred_at     TIMESTAMPTZ NOT NULL,
    entity_type     VARCHAR(64) NOT NULL,              -- 'booking' | 'location' | 'resource' | 'user' | 'role' | 'settings' | 'permission' | ...
    entity_id       UUID NOT NULL,
    action_type     VARCHAR(64) NOT NULL,              -- 'created' | 'updated' | 'deleted' | 'approved' | 'denied' | ...
    before_values   JSONB NULL,
    after_values    JSONB NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_entity ON audit_log_entries (entity_type, entity_id);
CREATE INDEX idx_audit_actor ON audit_log_entries (actor_id);
CREATE INDEX idx_audit_occurred_at ON audit_log_entries (occurred_at);
-- Immutability: no UPDATE/DELETE grants on this table for any application role; writes are INSERT-only.
```

### §Traceability (optional, owned by F13 tooling if not satisfied by external test-management SaaS)

```sql
CREATE TABLE traceability_entries (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    audit_finding_ref   VARCHAR(255) NOT NULL,          -- reference into F0 findings document
    requirement_ref     VARCHAR(255) NOT NULL,          -- e.g., 'FRD F2 §Validation bullet 3'
    implementation_ref  VARCHAR(255) NULL,
    test_ref            VARCHAR(255) NULL,
    status              VARCHAR(16) NOT NULL DEFAULT 'not_started', -- 'not_started'|'implemented'|'tested'|'verified'
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
```
