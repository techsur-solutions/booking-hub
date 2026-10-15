-- V4: Seed resource conflict scenario for UAT Test 11
-- Provides two bookings that share the same resource (Portable Projector) at overlapping times
-- to demonstrate resource conflict detection in addition to location conflicts
-- All inserts are idempotent using ON CONFLICT DO NOTHING

-- Booking 1: Morning workshop using portable projector
INSERT INTO bookings (id, series_id, title, location_id, start_time, end_time, status, owner_id, approved_by, approved_at, created_at, updated_at, deleted_at)
VALUES 
    ('40000000-0000-0000-0000-000000000016'::uuid,
     NULL,
     'RESOURCE-CONFLICT Marketing Workshop',
     '10000000-0000-0000-0000-000000000002'::uuid,  -- Conference Room B
     (CURRENT_DATE + INTERVAL '6 days' + INTERVAL '10 hours')::timestamptz,
     (CURRENT_DATE + INTERVAL '6 days' + INTERVAL '12 hours')::timestamptz,
     'approved',
     '00000000-0000-0000-0000-000000000001'::uuid,  -- testuser
     '00000000-0000-0000-0000-000000000002'::uuid,  -- approver
     NOW(),
     NOW(),
     NOW(),
     NULL)
ON CONFLICT (id) DO NOTHING;

-- Booking 2: Overlapping training session also using portable projector (CONFLICT!)
INSERT INTO bookings (id, series_id, title, location_id, start_time, end_time, status, owner_id, approved_by, approved_at, created_at, updated_at, deleted_at)
VALUES 
    ('40000000-0000-0000-0000-000000000017'::uuid,
     NULL,
     'RESOURCE-CONFLICT Sales Training',
     '10000000-0000-0000-0000-000000000005'::uuid,  -- Team Space Alpha (different location!)
     (CURRENT_DATE + INTERVAL '6 days' + INTERVAL '11 hours')::timestamptz,
     (CURRENT_DATE + INTERVAL '6 days' + INTERVAL '13 hours')::timestamptz,
     'approved',
     '00000000-0000-0000-0000-000000000004'::uuid,  -- uat.user
     '00000000-0000-0000-0000-000000000002'::uuid,  -- approver
     NOW(),
     NOW(),
     NOW(),
     NULL)
ON CONFLICT (id) DO NOTHING;

-- Attach Portable Projector (resource 20000000-0000-0000-0000-000000000002) to BOTH bookings
-- This creates a resource conflict even though the locations are different
INSERT INTO booking_resources (id, booking_id, resource_id, created_at)
VALUES 
    ('42000000-0000-0000-0000-000000000004'::uuid,
     '40000000-0000-0000-0000-000000000016'::uuid,
     '20000000-0000-0000-0000-000000000002'::uuid,  -- Projector (Portable)
     NOW()),
    ('42000000-0000-0000-0000-000000000005'::uuid,
     '40000000-0000-0000-0000-000000000017'::uuid,
     '20000000-0000-0000-0000-000000000002'::uuid,  -- Same Projector (Portable) - CONFLICT!
     NOW())
ON CONFLICT (booking_id, resource_id) DO NOTHING;
