-- V3: Seed UAT bookings
-- Provides comprehensive test data for all booking UI scenarios:
-- - Multiple status types (pending, approved, denied)
-- - Conflicting time slots (for conflict detection testing)
-- - Different locations and date ranges (calendar, day, list view testing)
-- - Recurring series (for edit/delete recurring booking tests)
-- - Bookings owned by different users (for approval queue and edit authorization)
-- All inserts are idempotent using ON CONFLICT DO NOTHING

-- Approved bookings for display board and calendar views
-- Today's bookings across different time slots
INSERT INTO bookings (id, series_id, title, location_id, start_time, end_time, status, owner_id, approved_by, approved_at, created_at, updated_at, deleted_at)
VALUES 
    ('40000000-0000-0000-0000-000000000001'::uuid,
     NULL,
     'Morning Team Standup',
     '10000000-0000-0000-0000-000000000001'::uuid,
     (CURRENT_DATE + INTERVAL '9 hours')::timestamptz,
     (CURRENT_DATE + INTERVAL '10 hours')::timestamptz,
     'approved',
     '00000000-0000-0000-0000-000000000001'::uuid,
     '00000000-0000-0000-0000-000000000002'::uuid,
     NOW(),
     NOW(),
     NOW(),
     NULL),
    ('40000000-0000-0000-0000-000000000002'::uuid,
     NULL,
     'Client Presentation',
     '10000000-0000-0000-0000-000000000001'::uuid,
     (CURRENT_DATE + INTERVAL '14 hours')::timestamptz,
     (CURRENT_DATE + INTERVAL '16 hours')::timestamptz,
     'approved',
     '00000000-0000-0000-0000-000000000001'::uuid,
     '00000000-0000-0000-0000-000000000002'::uuid,
     NOW(),
     NOW(),
     NOW(),
     NULL),
    ('40000000-0000-0000-0000-000000000003'::uuid,
     NULL,
     'Training Session - New Hires',
     '10000000-0000-0000-0000-000000000003'::uuid,
     (CURRENT_DATE + INTERVAL '10 hours')::timestamptz,
     (CURRENT_DATE + INTERVAL '12 hours')::timestamptz,
     'approved',
     '00000000-0000-0000-0000-000000000004'::uuid,
     '00000000-0000-0000-0000-000000000002'::uuid,
     NOW(),
     NOW(),
     NOW(),
     NULL)
ON CONFLICT (id) DO NOTHING;

-- Tomorrow's bookings for multi-day calendar view
INSERT INTO bookings (id, series_id, title, location_id, start_time, end_time, status, owner_id, approved_by, approved_at, created_at, updated_at, deleted_at)
VALUES 
    ('40000000-0000-0000-0000-000000000004'::uuid,
     NULL,
     'Executive Strategy Meeting',
     '10000000-0000-0000-0000-000000000004'::uuid,
     (CURRENT_DATE + INTERVAL '1 day' + INTERVAL '9 hours')::timestamptz,
     (CURRENT_DATE + INTERVAL '1 day' + INTERVAL '11 hours')::timestamptz,
     'approved',
     '00000000-0000-0000-0000-000000000003'::uuid,
     '00000000-0000-0000-0000-000000000002'::uuid,
     NOW(),
     NOW(),
     NOW(),
     NULL),
    ('40000000-0000-0000-0000-000000000005'::uuid,
     NULL,
     'Department Workshop',
     '10000000-0000-0000-0000-000000000002'::uuid,
     (CURRENT_DATE + INTERVAL '1 day' + INTERVAL '13 hours')::timestamptz,
     (CURRENT_DATE + INTERVAL '1 day' + INTERVAL '17 hours')::timestamptz,
     'approved',
     '00000000-0000-0000-0000-000000000001'::uuid,
     '00000000-0000-0000-0000-000000000002'::uuid,
     NOW(),
     NOW(),
     NOW(),
     NULL)
ON CONFLICT (id) DO NOTHING;

-- Next week bookings for calendar spanning multiple days
INSERT INTO bookings (id, series_id, title, location_id, start_time, end_time, status, owner_id, approved_by, approved_at, created_at, updated_at, deleted_at)
VALUES 
    ('40000000-0000-0000-0000-000000000006'::uuid,
     NULL,
     'Product Launch Planning',
     '10000000-0000-0000-0000-000000000004'::uuid,
     (CURRENT_DATE + INTERVAL '7 days' + INTERVAL '14 hours')::timestamptz,
     (CURRENT_DATE + INTERVAL '7 days' + INTERVAL '16 hours')::timestamptz,
     'approved',
     '00000000-0000-0000-0000-000000000003'::uuid,
     '00000000-0000-0000-0000-000000000002'::uuid,
     NOW(),
     NOW(),
     NOW(),
     NULL)
ON CONFLICT (id) DO NOTHING;

-- Pending bookings for approval queue testing
-- Some with conflicts, some without
INSERT INTO bookings (id, series_id, title, location_id, start_time, end_time, status, owner_id, created_at, updated_at, deleted_at)
VALUES 
    ('40000000-0000-0000-0000-000000000007'::uuid,
     NULL,
     'PENDING-APPROVAL Team Meeting',
     '10000000-0000-0000-0000-000000000002'::uuid,
     (CURRENT_DATE + INTERVAL '2 days' + INTERVAL '10 hours')::timestamptz,
     (CURRENT_DATE + INTERVAL '2 days' + INTERVAL '11 hours')::timestamptz,
     'pending',
     '00000000-0000-0000-0000-000000000001'::uuid,
     NOW(),
     NOW(),
     NULL),
    ('40000000-0000-0000-0000-000000000008'::uuid,
     NULL,
     'PENDING-WITH-CONFLICT Interview Room',
     '10000000-0000-0000-0000-000000000001'::uuid,
     (CURRENT_DATE + INTERVAL '14 hours' + INTERVAL '30 minutes')::timestamptz,
     (CURRENT_DATE + INTERVAL '15 hours' + INTERVAL '30 minutes')::timestamptz,
     'pending',
     '00000000-0000-0000-0000-000000000004'::uuid,
     NOW(),
     NOW(),
     NULL),
    ('40000000-0000-0000-0000-000000000009'::uuid,
     NULL,
     'PENDING-APPROVAL Quarterly Review',
     '10000000-0000-0000-0000-000000000005'::uuid,
     (CURRENT_DATE + INTERVAL '3 days' + INTERVAL '13 hours')::timestamptz,
     (CURRENT_DATE + INTERVAL '3 days' + INTERVAL '15 hours')::timestamptz,
     'pending',
     '00000000-0000-0000-0000-000000000004'::uuid,
     NOW(),
     NOW(),
     NULL)
ON CONFLICT (id) DO NOTHING;

-- Denied booking for list view status filtering
INSERT INTO bookings (id, series_id, title, location_id, start_time, end_time, status, owner_id, denied_by, denied_at, denial_reason, created_at, updated_at, deleted_at)
VALUES 
    ('40000000-0000-0000-0000-000000000010'::uuid,
     NULL,
     'DENIED Social Event',
     '10000000-0000-0000-0000-000000000004'::uuid,
     (CURRENT_DATE + INTERVAL '5 days' + INTERVAL '18 hours')::timestamptz,
     (CURRENT_DATE + INTERVAL '5 days' + INTERVAL '22 hours')::timestamptz,
     'denied',
     '00000000-0000-0000-0000-000000000001'::uuid,
     '00000000-0000-0000-0000-000000000002'::uuid,
     NOW(),
     'Executive boardroom reserved for board meetings only',
     NOW(),
     NOW(),
     NULL)
ON CONFLICT (id) DO NOTHING;

-- Recurring booking series for edit/delete recurring tests
-- Series: Weekly team meeting every Monday for 4 weeks
INSERT INTO bookings (id, series_id, title, location_id, start_time, end_time, status, owner_id, approved_by, approved_at, created_at, updated_at, deleted_at)
VALUES 
    ('40000000-0000-0000-0000-000000000011'::uuid,
     '41000000-0000-0000-0000-000000000001'::uuid,
     'RECURRING Weekly Team Sync - Week 1',
     '10000000-0000-0000-0000-000000000005'::uuid,
     (CURRENT_DATE + INTERVAL '1 week' - EXTRACT(DOW FROM CURRENT_DATE)::int * INTERVAL '1 day' + INTERVAL '1 day' + INTERVAL '10 hours')::timestamptz,
     (CURRENT_DATE + INTERVAL '1 week' - EXTRACT(DOW FROM CURRENT_DATE)::int * INTERVAL '1 day' + INTERVAL '1 day' + INTERVAL '11 hours')::timestamptz,
     'approved',
     '00000000-0000-0000-0000-000000000001'::uuid,
     '00000000-0000-0000-0000-000000000002'::uuid,
     NOW(),
     NOW(),
     NOW(),
     NULL),
    ('40000000-0000-0000-0000-000000000012'::uuid,
     '41000000-0000-0000-0000-000000000001'::uuid,
     'RECURRING Weekly Team Sync - Week 2',
     '10000000-0000-0000-0000-000000000005'::uuid,
     (CURRENT_DATE + INTERVAL '2 weeks' - EXTRACT(DOW FROM CURRENT_DATE)::int * INTERVAL '1 day' + INTERVAL '1 day' + INTERVAL '10 hours')::timestamptz,
     (CURRENT_DATE + INTERVAL '2 weeks' - EXTRACT(DOW FROM CURRENT_DATE)::int * INTERVAL '1 day' + INTERVAL '1 day' + INTERVAL '11 hours')::timestamptz,
     'approved',
     '00000000-0000-0000-0000-000000000001'::uuid,
     '00000000-0000-0000-0000-000000000002'::uuid,
     NOW(),
     NOW(),
     NOW(),
     NULL),
    ('40000000-0000-0000-0000-000000000013'::uuid,
     '41000000-0000-0000-0000-000000000001'::uuid,
     'RECURRING Weekly Team Sync - Week 3',
     '10000000-0000-0000-0000-000000000005'::uuid,
     (CURRENT_DATE + INTERVAL '3 weeks' - EXTRACT(DOW FROM CURRENT_DATE)::int * INTERVAL '1 day' + INTERVAL '1 day' + INTERVAL '10 hours')::timestamptz,
     (CURRENT_DATE + INTERVAL '3 weeks' - EXTRACT(DOW FROM CURRENT_DATE)::int * INTERVAL '1 day' + INTERVAL '1 day' + INTERVAL '11 hours')::timestamptz,
     'approved',
     '00000000-0000-0000-0000-000000000001'::uuid,
     '00000000-0000-0000-0000-000000000002'::uuid,
     NOW(),
     NOW(),
     NOW(),
     NULL)
ON CONFLICT (id) DO NOTHING;

-- Booking to clone (for clone booking test)
INSERT INTO bookings (id, series_id, title, location_id, start_time, end_time, status, owner_id, approved_by, approved_at, created_at, updated_at, deleted_at)
VALUES 
    ('40000000-0000-0000-0000-000000000014'::uuid,
     NULL,
     'CLONABLE Project Kickoff Meeting',
     '10000000-0000-0000-0000-000000000002'::uuid,
     (CURRENT_DATE - INTERVAL '1 day' + INTERVAL '14 hours')::timestamptz,
     (CURRENT_DATE - INTERVAL '1 day' + INTERVAL '15 hours')::timestamptz,
     'approved',
     '00000000-0000-0000-0000-000000000001'::uuid,
     '00000000-0000-0000-0000-000000000002'::uuid,
     NOW(),
     NOW(),
     NOW(),
     NULL)
ON CONFLICT (id) DO NOTHING;

-- Booking with resources attached (for resource display testing)
INSERT INTO bookings (id, series_id, title, location_id, start_time, end_time, status, owner_id, approved_by, approved_at, created_at, updated_at, deleted_at)
VALUES 
    ('40000000-0000-0000-0000-000000000015'::uuid,
     NULL,
     'All-Hands Meeting with Resources',
     '10000000-0000-0000-0000-000000000003'::uuid,
     (CURRENT_DATE + INTERVAL '4 days' + INTERVAL '15 hours')::timestamptz,
     (CURRENT_DATE + INTERVAL '4 days' + INTERVAL '17 hours')::timestamptz,
     'approved',
     '00000000-0000-0000-0000-000000000003'::uuid,
     '00000000-0000-0000-0000-000000000002'::uuid,
     NOW(),
     NOW(),
     NOW(),
     NULL)
ON CONFLICT (id) DO NOTHING;

-- Associate resources with the last booking
INSERT INTO booking_resources (id, booking_id, resource_id, created_at)
VALUES 
    ('42000000-0000-0000-0000-000000000001'::uuid,
     '40000000-0000-0000-0000-000000000015'::uuid,
     '20000000-0000-0000-0000-000000000002'::uuid,
     NOW()),
    ('42000000-0000-0000-0000-000000000002'::uuid,
     '40000000-0000-0000-0000-000000000015'::uuid,
     '20000000-0000-0000-0000-000000000003'::uuid,
     NOW()),
    ('42000000-0000-0000-0000-000000000003'::uuid,
     '40000000-0000-0000-0000-000000000015'::uuid,
     '20000000-0000-0000-0000-000000000005'::uuid,
     NOW())
ON CONFLICT (booking_id, resource_id) DO NOTHING;
