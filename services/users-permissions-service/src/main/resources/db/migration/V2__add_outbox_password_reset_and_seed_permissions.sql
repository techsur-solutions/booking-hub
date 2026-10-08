-- F0 Open Question #21: users.email uniqueness is treated as case-insensitive (the safer,
-- more common modern default) — this index is the enforcement mechanism, additive to the
-- plain-UNIQUE V1 column (which remains, harmlessly stricter-than-needed on its own).
CREATE UNIQUE INDEX uq_users_email_lower ON users (lower(email));

-- Transactional outbox (TechArch Y3 "Publisher guarantee" / outbox pattern): every domain
-- event this service publishes (user.created, user.updated, role.assigned,
-- password.reset.requested, permission.updated) is written here in the SAME transaction as
-- the business change, then relayed to the already-declared RabbitMQ exchanges by a
-- scheduled polling publisher (Phase 3 plan 03-05) — never a direct in-request publish.
CREATE TABLE outbox (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  VARCHAR(64) NOT NULL,              -- 'user' | 'permission'
    aggregate_id    UUID NOT NULL,
    exchange        VARCHAR(100) NOT NULL,              -- 'user.events' | 'permission.events' (already declared, Phase 2 plan 02-09)
    routing_key     VARCHAR(100) NOT NULL,              -- 'user.created' | 'user.updated' | 'role.assigned' | 'password.reset.requested' | 'permission.updated'
    idempotency_key UUID NOT NULL DEFAULT gen_random_uuid(),
    payload         JSONB NOT NULL,
    status          VARCHAR(16) NOT NULL DEFAULT 'pending', -- 'pending' | 'published' | 'failed'
    attempt_count   INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at    TIMESTAMPTZ NULL
);
CREATE INDEX idx_outbox_pending ON outbox (status, created_at) WHERE status = 'pending';
CREATE UNIQUE INDEX uq_outbox_idempotency_key ON outbox (idempotency_key);

-- F0 Open Question #19: legacy only re-checks the 2-hour reset-token expiry when the EDIT
-- form loads, not when the new password is SUBMITTED. The new system closes this gap by
-- storing the token here (hashed, never plaintext) and re-validating expires_at/used_at at
-- the single point this API exposes submission (POST /auth/password-reset/complete) — there
-- is no separate earlier check to go stale. 2-hour window is preserved from legacy (F0
-- confirmed exact figure); no reason found to diverge.
CREATE TABLE password_reset_tokens (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash      VARCHAR(255) NOT NULL UNIQUE,        -- SHA-256 hex of the raw token; raw token exists only in the emailed link/event payload, never stored in plaintext
    expires_at      TIMESTAMPTZ NOT NULL,
    used_at         TIMESTAMPTZ NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_password_reset_tokens_user ON password_reset_tokens (user_id);
CREATE INDEX idx_password_reset_tokens_expiry ON password_reset_tokens (expires_at) WHERE used_at IS NULL;

-- Full 17-row permission seed (15 actively enforced + 2 dead/reserved), derived from
-- F0 audit findings/04-identity-access.md Permission Flag Inventory. Two rows
-- (accessresources, allowiCal, allowRSS) are deliberately confirmed = false:
-- per FRD F7 §Validation, any Tier-2 code path checking a permission row with
-- confirmed = false MUST deny by default, never treat it as "no restriction".
INSERT INTO permissions (legacy_flag, keycloak_role, gated_actions, confirmed) VALUES
  ('accessApplication', 'n/a_baseline_authenticated', '{"description":"Baseline authenticated-access gate (legacy super.init() filter on every controller except Sessions.cfc/Api.cfc); new-system equivalent is simply holding any valid Keycloak access token, not a distinct realm role."}', true),
  ('accessCalendar', 'role_calendar_viewer', '{"description":"Calendar/location/resource read access."}', true),
  ('allowRoomBooking', 'role_booking_creator', '{"description":"Create/edit/delete/clone own bookings."}', true),
  ('viewRoomBooking', 'role_booking_viewer', '{"description":"List/view bookings (data-complete read views)."}', true),
  ('allowApproveBooking', 'role_booking_approver', '{"description":"Approve/deny pending bookings."}', true),
  ('bypassApproveBooking', 'role_booking_approver', '{"description":"Auto-approve bypass at booking creation when the global approveBooking setting is on; folded into role_booking_approver (Phase 3 explicit decision) since legacy gates it with the same effective privilege as allowApproveBooking."}', true),
  ('accessCustomfields', 'role_customfield_admin', '{"description":"Custom field/template CRUD."}', true),
  ('accessLocations', 'role_location_admin', '{"description":"Location CRUD (all actions except public list/view)."}', true),
  ('accesslogfiles', 'role_audit_viewer', '{"description":"Audit/activity log viewing (Logfiles controller + admin user-table Activity link)."}', true),
  ('accessPermissions', 'role_permissions_admin', '{"description":"View/edit role-to-permission mappings."}', true),
  ('accessresources', 'role_location_admin', '{"description":"Resource CRUD; no dedicated TechArch role exists for this flag. Provisionally folded into role_location_admin (Phase 3 planning decision) but left confirmed=false and therefore deny-by-default until a product owner explicitly confirms the mapping.","ambiguity":"no 1:1 TechArch role mapping found for this legacy flag"}', false),
  ('accessSettings', 'role_settings_admin', '{"description":"PUT /settings (system-wide configuration)."}', true),
  ('accessUsers', 'role_user_admin', '{"description":"Admin user-management actions: account creation, role assignment, listing."}', true),
  ('updateOwnAccount', 'n/a_self_access_tier2_check', '{"description":"Self-service myaccount/updateaccount/updatepassword actions. Enforced as a Tier-2 code-level ownership check (caller id == target user id), not a distinct Keycloak role."}', true),
  ('allowAPI', 'role_feed_api', '{"description":"Gates only the legacy feed-listing index page per F0 Open Question #7 resolution; the actual feed-serving actions (rss2/ical/json/display) are token-gated by a per-user apitoken, not role-gated, and granted to all roles by default. Seeded here for inventory completeness; primarily a Phase 7 concern."}', true),
  ('allowiCal', 'n/a_reserved_unmapped', '{"description":"Legacy dead flag — no code reference found anywhere; the legacy seed data''s own notes column reads Reserved for future use. Carried forward as reserved-but-unmapped (Phase 3 explicit decision) rather than silently dropped."}', false),
  ('allowRSS', 'n/a_reserved_unmapped', '{"description":"Legacy dead flag — no code reference found anywhere; the legacy seed data''s own notes column reads Reserved for future use. Carried forward as reserved-but-unmapped (Phase 3 explicit decision) rather than silently dropped."}', false)
ON CONFLICT (legacy_flag) DO NOTHING;
