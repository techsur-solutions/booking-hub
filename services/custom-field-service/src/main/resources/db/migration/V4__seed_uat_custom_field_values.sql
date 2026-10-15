-- V4: Seed UAT custom field values
-- Associates custom field values with seeded bookings from booking-service V3
-- Demonstrates populated custom fields in booking detail views
-- All inserts are idempotent using ON CONFLICT DO NOTHING

-- Custom field values for booking 40000000-0000-0000-0000-000000000001 (Morning Team Standup)
INSERT INTO custom_field_values (id, booking_id, custom_field_id, value, created_at, updated_at)
VALUES 
    ('50000000-0000-0000-0000-000000000001'::uuid,
     '40000000-0000-0000-0000-000000000001'::uuid,
     '30000000-0000-0000-0000-000000000001'::uuid,
     'Team Meeting',
     NOW(),
     NOW()),
    ('50000000-0000-0000-0000-000000000002'::uuid,
     '40000000-0000-0000-0000-000000000001'::uuid,
     '30000000-0000-0000-0000-000000000002'::uuid,
     '15',
     NOW(),
     NOW()),
    ('50000000-0000-0000-0000-000000000003'::uuid,
     '40000000-0000-0000-0000-000000000001'::uuid,
     '30000000-0000-0000-0000-000000000003'::uuid,
     'Please arrange chairs in circle. Need whiteboard markers.',
     NOW(),
     NOW())
ON CONFLICT (id) DO NOTHING;

-- Custom field values for booking 40000000-0000-0000-0000-000000000002 (Client Presentation)
INSERT INTO custom_field_values (id, booking_id, custom_field_id, value, created_at, updated_at)
VALUES 
    ('50000000-0000-0000-0000-000000000004'::uuid,
     '40000000-0000-0000-0000-000000000002'::uuid,
     '30000000-0000-0000-0000-000000000001'::uuid,
     'Client Presentation',
     NOW(),
     NOW()),
    ('50000000-0000-0000-0000-000000000005'::uuid,
     '40000000-0000-0000-0000-000000000002'::uuid,
     '30000000-0000-0000-0000-000000000002'::uuid,
     '8',
     NOW(),
     NOW()),
    ('50000000-0000-0000-0000-000000000006'::uuid,
     '40000000-0000-0000-0000-000000000002'::uuid,
     '30000000-0000-0000-0000-000000000003'::uuid,
     'VIP client - ensure room is clean and projector tested beforehand.',
     NOW(),
     NOW())
ON CONFLICT (id) DO NOTHING;

-- Custom field values for booking 40000000-0000-0000-0000-000000000004 (Executive Strategy Meeting)
INSERT INTO custom_field_values (id, booking_id, custom_field_id, value, created_at, updated_at)
VALUES 
    ('50000000-0000-0000-0000-000000000007'::uuid,
     '40000000-0000-0000-0000-000000000004'::uuid,
     '30000000-0000-0000-0000-000000000001'::uuid,
     'Workshop',
     NOW(),
     NOW()),
    ('50000000-0000-0000-0000-000000000008'::uuid,
     '40000000-0000-0000-0000-000000000004'::uuid,
     '30000000-0000-0000-0000-000000000004'::uuid,
     'Coffee/Tea',
     NOW(),
     NOW()),
    ('50000000-0000-0000-0000-000000000009'::uuid,
     '40000000-0000-0000-0000-000000000004'::uuid,
     '30000000-0000-0000-0000-000000000005'::uuid,
     'EXEC-001',
     NOW(),
     NOW())
ON CONFLICT (id) DO NOTHING;

-- Custom field values for booking 40000000-0000-0000-0000-000000000015 (All-Hands Meeting)
INSERT INTO custom_field_values (id, booking_id, custom_field_id, value, created_at, updated_at)
VALUES 
    ('50000000-0000-0000-0000-000000000010'::uuid,
     '40000000-0000-0000-0000-000000000015'::uuid,
     '30000000-0000-0000-0000-000000000001'::uuid,
     'Training Session',
     NOW(),
     NOW()),
    ('50000000-0000-0000-0000-000000000011'::uuid,
     '40000000-0000-0000-0000-000000000015'::uuid,
     '30000000-0000-0000-0000-000000000002'::uuid,
     '50',
     NOW(),
     NOW()),
    ('50000000-0000-0000-0000-000000000012'::uuid,
     '40000000-0000-0000-0000-000000000015'::uuid,
     '30000000-0000-0000-0000-000000000003'::uuid,
     'Company-wide training. Setup theater-style seating. Test sound system.',
     NOW(),
     NOW())
ON CONFLICT (id) DO NOTHING;
