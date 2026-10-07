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

-- permission module (no FK to users — intentionally decoupled, see TechArch §2.5)
CREATE TABLE permissions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    legacy_flag         VARCHAR(100) NOT NULL UNIQUE,   -- e.g., 'accessCalendar', 'allowRoomBooking'
    keycloak_role       VARCHAR(100) NOT NULL,
    gated_actions       JSONB NULL,
    confirmed           BOOLEAN NOT NULL DEFAULT false, -- true only once F0 confirms exact gating scope
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE OR REPLACE FUNCTION set_updated_at() RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at BEFORE UPDATE ON users
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_permissions_updated_at BEFORE UPDATE ON permissions
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
