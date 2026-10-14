-- V3: Seed test user for development/UAT
-- 
-- This user's UUID MUST match the 'sub' claim in Keycloak test user's JWT token.
-- To find the correct UUID:
-- 1. Log in to Keycloak admin console (http://localhost:9090)
-- 2. Navigate to Users -> View all users
-- 3. Click on your test user (e.g., testuser@example.com)
-- 4. Copy the user ID (UUID) from the URL or Details tab
-- 
-- For now, using a placeholder UUID that should be replaced with actual Keycloak user ID.
-- If Keycloak test user doesn't exist yet, create one with this exact UUID.

INSERT INTO users (id, email, first_name, last_name, deleted_at, created_at, updated_at)
VALUES 
    ('00000000-0000-0000-0000-000000000001'::uuid, 
     'testuser@example.com', 
     'Test', 
     'User', 
     NULL, 
     NOW(), 
     NOW())
ON CONFLICT (id) DO NOTHING;

-- Add a second test user for approval workflow testing
INSERT INTO users (id, email, first_name, last_name, deleted_at, created_at, updated_at)
VALUES 
    ('00000000-0000-0000-0000-000000000002'::uuid,
     'approver@example.com',
     'Booking',
     'Approver',
     NULL,
     NOW(),
     NOW())
ON CONFLICT (id) DO NOTHING;
