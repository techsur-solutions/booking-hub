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
