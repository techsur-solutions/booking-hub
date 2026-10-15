-- V3: Seed UAT locations and resources
-- Provides test data for booking views, calendar, and conflict detection
-- All inserts are idempotent using ON CONFLICT DO NOTHING

-- Locations with descriptive names for different testing scenarios
INSERT INTO locations (id, name, css_class, building, layout, created_at, updated_at, deleted_at)
VALUES 
    ('10000000-0000-0000-0000-000000000001'::uuid,
     'Conference Room A - Building 1',
     'location-blue',
     'Building 1',
     '{"capacity": 20, "equipment": ["projector", "whiteboard"]}',
     NOW(),
     NOW(),
     NULL),
    ('10000000-0000-0000-0000-000000000002'::uuid,
     'Conference Room B - Building 1',
     'location-green',
     'Building 1',
     '{"capacity": 10, "equipment": ["tv", "phone"]}',
     NOW(),
     NOW(),
     NULL),
    ('10000000-0000-0000-0000-000000000003'::uuid,
     'Training Room - Building 2',
     'location-orange',
     'Building 2',
     '{"capacity": 30, "equipment": ["projector", "sound-system"]}',
     NOW(),
     NOW(),
     NULL),
    ('10000000-0000-0000-0000-000000000004'::uuid,
     'Executive Boardroom - Building 1',
     'location-purple',
     'Building 1',
     '{"capacity": 12, "equipment": ["video-conferencing", "whiteboard"]}',
     NOW(),
     NOW(),
     NULL),
    ('10000000-0000-0000-0000-000000000005'::uuid,
     'Team Space Alpha - Building 2',
     'location-teal',
     'Building 2',
     '{"capacity": 8, "equipment": ["whiteboard"]}',
     NOW(),
     NOW(),
     NULL)
ON CONFLICT (id) DO NOTHING;

-- Resources that can be attached to bookings
INSERT INTO resources (id, name, created_at, updated_at, deleted_at)
VALUES 
    ('20000000-0000-0000-0000-000000000001'::uuid,
     'Laptop Cart',
     NOW(),
     NOW(),
     NULL),
    ('20000000-0000-0000-0000-000000000002'::uuid,
     'Projector (Portable)',
     NOW(),
     NOW(),
     NULL),
    ('20000000-0000-0000-0000-000000000003'::uuid,
     'Catering Setup',
     NOW(),
     NOW(),
     NULL),
    ('20000000-0000-0000-0000-000000000004'::uuid,
     'Video Recording Equipment',
     NOW(),
     NOW(),
     NULL),
    ('20000000-0000-0000-0000-000000000005'::uuid,
     'Flip Chart Stand',
     NOW(),
     NOW(),
     NULL)
ON CONFLICT (id) DO NOTHING;
