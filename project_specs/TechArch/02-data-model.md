
## 3. Data Model

### 3.1 Service-Level Entity-Relationship Overview (ASCII)

Each box is an independently owned PostgreSQL database. Solid lines within a box are real foreign keys. Dashed lines crossing box boundaries are **ID-only references**, resolved at runtime via REST call or event payload — never a database-level foreign key, per the service-isolation NFR.

```
┌─────────────────────────────┐        ┌──────────────────────────────┐
│   booking_db                │        │   locres_db                  │
│  ┌─────────────┐            │        │  ┌───────────┐  ┌───────────┐│
│  │ bookings    │            │ ─ ─ ─ ▶│  │ locations │  │ resources ││
│  │ id (PK)     │  location_id (ref)  │  └───────────┘  └───────────┘│
│  │ series_id   │            │        └──────────────────────────────┘
│  │ status      │            │
│  │ owner_id ───┼─ ─ ─ ─ ─ ─ ─┼ ─ ─ ─ ▶ users_db.users.id
│  └──────┬──────┘            │
│         │ 1:N (real FK)     │        ┌──────────────────────────────┐
│         ▼                   │        │   customfld_db               │
│  ┌─────────────┐            │        │  ┌─────────────┐             │
│  │ booking_    │ resource_id│ ─ ─ ─ ▶│  │ custom_      │             │
│  │ resources   │  (ref)     │        │  │ fields       │             │
│  └─────────────┘            │        │  └──────┬──────┘             │
└──────────────┬───────────────┘        │         │ 1:N (real FK)      │
               │ booking_id (ref)        │         ▼                   │
               └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─▶│  ┌─────────────────┐        │
                                         │  │ custom_field_    │        │
                                         │  │ joins            │        │
                                         │  └─────────────────┘        │
                                         │  ┌─────────────────┐        │
                                         │  │ custom_field_    │        │
                                         │  │ values           │        │
                                         │  │  booking_id(ref) │        │
                                         │  └─────────────────┘        │
                                         └──────────────────────────────┘

┌──────────────────────────────┐  ┌──────────────────────┐  ┌──────────────────────┐
│   userperm_db                 │  │   notif_db            │  │   settings_db         │
│  ┌───────────┐ ┌────────────┐ │  │  ┌──────────────────┐│  │  ┌──────────────────┐ │
│  │ users     │ │ permissions│ │  │  │ notification_     ││  │  │ settings         │ │
│  │ id = KC   │ │ (no FK to  │ │  │  │ deliveries        ││  │  │ (singleton row)  │ │
│  │ subject   │ │  users)    │ │  │  └──────────────────┘│  │  └──────────────────┘ │
│  └───────────┘ └────────────┘ │  └──────────────────────┘  └──────────────────────┘
└──────────────────────────────┘

┌──────────────────────────────┐  ┌──────────────────────────────┐
│   feeds_db (optional          │  │   audit_db                    │
│   projection)                 │  │  ┌──────────────────────────┐│
│  ┌──────────────────────────┐│  │  │ audit_log_entries         ││
│  │ feed_bookings             ││  │  │  entity_id (ref, any svc) ││
│  │  booking_id (ref)         ││  │  │  INSERT-only, no UPDATE/  ││
│  │  location_id (ref)        ││  │  │  DELETE grants             ││
│  └──────────────────────────┘│  │  └──────────────────────────┘│
└──────────────────────────────┘  └──────────────────────────────┘

Keycloak realm DB (opaque, Keycloak-managed — not pictured in detail):
  stores credentials, sessions, refresh tokens, realm/client roles.
  users_db.users.id == Keycloak `sub` claim (see §User DDL note).
```

### 3.2 Conventions

- All tables use `UUID` primary keys (`gen_random_uuid()` default via the `pgcrypto` or `uuid-ossp` extension), except `settings` (deliberate `INTEGER` singleton) and Keycloak-aligned `users.id` (set explicitly to the Keycloak `sub` claim, not independently generated).
- Every table carries `created_at`/`updated_at` (`TIMESTAMPTZ`, UTC); `updated_at` is trigger-maintained (`BEFORE UPDATE` trigger invoking a shared `set_updated_at()` function per service).
- Soft-delete (`deleted_at TIMESTAMPTZ NULL`) is used wherever the FRD's interim deletion-policy default applies (bookings, locations, resources) so audit history and existing references remain intact.
- No table in one service's database may be referenced by a `FOREIGN KEY` from another service's database — every cross-service reference in the diagram above is a plain `UUID` column with no referential-integrity constraint at the database level; integrity is enforced at the application layer (existence-check API calls at write time) and reconciled via domain events.

### 3.3 Per-Service DDL

The following DDL is authoritative and matches `FRD-BookingHub.md` §Y0 verbatim (TechArch does not alter any FRD-specified column, type, or constraint — it adds deployment/ownership framing only).

#### booking-service — database `booking_db`

```sql
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
```

**Conflict-detection query shape** (not a table — the indexed query pattern `ConflictDetectionService` runs on every create/edit/bulk-read, per FRD F2 §Schema Surface):

```sql
-- Location-level overlap (excludes self on edit; excludes deleted/denied per F2 §Validation interim default)
SELECT id FROM bookings
WHERE location_id = :location_id
  AND deleted_at IS NULL
  AND status <> 'denied'
  AND id <> COALESCE(:exclude_booking_id, '00000000-0000-0000-0000-000000000000'::uuid)
  AND start_time < :proposed_end_time
  AND :proposed_start_time < end_time;

-- Resource-level overlap, per attached resource
SELECT br.booking_id FROM booking_resources br
JOIN bookings b ON b.id = br.booking_id
WHERE br.resource_id = ANY(:resource_ids)
  AND b.deleted_at IS NULL
  AND b.status <> 'denied'
  AND b.id <> COALESCE(:exclude_booking_id, '00000000-0000-0000-0000-000000000000'::uuid)
  AND b.start_time < :proposed_end_time
  AND :proposed_start_time < b.end_time;
```

#### locations-resources-service — database `locres_db`

```sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;

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

#### custom-field-service — database `customfld_db`

```sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;

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
    booking_id      UUID NOT NULL,                     -- cross-service reference to booking-service
    custom_field_id UUID NOT NULL REFERENCES custom_fields(id),
    value           TEXT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_custom_field_values_booking ON custom_field_values (booking_id);
```

#### users-permissions-service — database `userperm_db`

```sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- user module
CREATE TABLE users (
    id              UUID PRIMARY KEY,                  -- matches Keycloak subject (sub claim), not independently generated
    email           VARCHAR(320) NOT NULL UNIQUE,
    display_name    VARCHAR(255) NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ NULL
);
-- No password/hash/salt columns: credential storage is delegated entirely to Keycloak, per "Standards-based identity" NFR.

-- permission module (no FK to users — intentionally decoupled, see §2.5)
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

#### notifications-service — database `notif_db`

```sql
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
```

#### feeds-service — database `feeds_db` (optional materialized projection)

```sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Denormalized projection, populated by consuming booking.created/updated/approved/denied/deleted
-- and location.updated/deleted events, per §1.4 read-pattern decision.
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

#### settings-service — database `settings_db`

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

#### audit-log-service — database `audit_db`

```sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;

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
-- Immutability enforced at the Postgres role level: the application's runtime role
-- is granted INSERT, SELECT only on audit_log_entries — no UPDATE/DELETE grant exists.
REVOKE UPDATE, DELETE ON audit_log_entries FROM PUBLIC;
```

#### F13 traceability tooling — database `traceability_db` (optional; only if not satisfied by external test-management SaaS)

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

### 3.4 `updated_at` Trigger (applied per-database)

```sql
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Example attachment (repeated per table with an updated_at column, per service):
CREATE TRIGGER trg_bookings_updated_at BEFORE UPDATE ON bookings
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
```
