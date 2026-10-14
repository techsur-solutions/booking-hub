-- V4: Seed additional UAT users with deterministic UUIDs matching Keycloak
-- These users support comprehensive UI testing scenarios:
-- - Admin user with all roles (role_booking_approver, admin-level permissions)
-- - Regular user without approver role (for permission-gated sidebar testing)
-- 
-- IMPORTANT: These UUIDs MUST be created in Keycloak with matching IDs.
-- See infra/keycloak/realm-export.json for the corresponding Keycloak user definitions.

-- Admin user with full permissions (including role_booking_approver)
INSERT INTO users (id, email, display_name, deleted_at, created_at, updated_at)
VALUES 
    ('00000000-0000-0000-0000-000000000003'::uuid, 
     'uat.admin@example.test', 
     'UAT Admin User', 
     NULL, 
     NOW(), 
     NOW())
ON CONFLICT (id) DO NOTHING;

-- Regular user without role_booking_approver (for permission testing)
INSERT INTO users (id, email, display_name, deleted_at, created_at, updated_at)
VALUES 
    ('00000000-0000-0000-0000-000000000004'::uuid,
     'uat.user@example.test',
     'UAT Regular User',
     NULL,
     NOW(),
     NOW())
ON CONFLICT (id) DO NOTHING;
