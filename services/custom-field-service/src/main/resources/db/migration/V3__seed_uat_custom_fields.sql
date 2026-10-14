-- V3: Seed UAT custom fields and templates
-- Provides custom field definitions for location-specific booking forms
-- All inserts are idempotent using ON CONFLICT DO NOTHING

-- Custom field definitions
INSERT INTO custom_fields (id, label, field_type, options, created_at, updated_at, deleted_at)
VALUES 
    ('30000000-0000-0000-0000-000000000001'::uuid,
     'Meeting Purpose',
     'select',
     '{"options": ["Team Meeting", "Client Presentation", "Training Session", "Workshop", "Interview"]}'::jsonb,
     NOW(),
     NOW(),
     NULL),
    ('30000000-0000-0000-0000-000000000002'::uuid,
     'Expected Attendees',
     'number',
     NULL,
     NOW(),
     NOW(),
     NULL),
    ('30000000-0000-0000-0000-000000000003'::uuid,
     'Setup Notes',
     'text',
     NULL,
     NOW(),
     NOW(),
     NULL),
    ('30000000-0000-0000-0000-000000000004'::uuid,
     'Catering Required',
     'select',
     '{"options": ["None", "Coffee/Tea", "Lunch", "Snacks"]}'::jsonb,
     NOW(),
     NOW(),
     NULL),
    ('30000000-0000-0000-0000-000000000005'::uuid,
     'Department Code',
     'text',
     NULL,
     NOW(),
     NOW(),
     NULL)
ON CONFLICT (id) DO NOTHING;

-- Template for Conference Room A (location 10000000-0000-0000-0000-000000000001)
INSERT INTO custom_field_templates (id, name, context_id, created_at, updated_at)
VALUES 
    ('31000000-0000-0000-0000-000000000001'::uuid,
     'Conference Room A Template',
     '10000000-0000-0000-0000-000000000001'::uuid,
     NOW(),
     NOW())
ON CONFLICT (id) DO NOTHING;

-- Associate custom fields with Conference Room A template
INSERT INTO custom_field_joins (id, custom_field_template_id, custom_field_id, created_at)
VALUES 
    ('32000000-0000-0000-0000-000000000001'::uuid,
     '31000000-0000-0000-0000-000000000001'::uuid,
     '30000000-0000-0000-0000-000000000001'::uuid,
     NOW()),
    ('32000000-0000-0000-0000-000000000002'::uuid,
     '31000000-0000-0000-0000-000000000001'::uuid,
     '30000000-0000-0000-0000-000000000002'::uuid,
     NOW()),
    ('32000000-0000-0000-0000-000000000003'::uuid,
     '31000000-0000-0000-0000-000000000001'::uuid,
     '30000000-0000-0000-0000-000000000003'::uuid,
     NOW())
ON CONFLICT (custom_field_template_id, custom_field_id) DO NOTHING;

-- Template for Executive Boardroom (location 10000000-0000-0000-0000-000000000004)
INSERT INTO custom_field_templates (id, name, context_id, created_at, updated_at)
VALUES 
    ('31000000-0000-0000-0000-000000000002'::uuid,
     'Executive Boardroom Template',
     '10000000-0000-0000-0000-000000000004'::uuid,
     NOW(),
     NOW())
ON CONFLICT (id) DO NOTHING;

-- Associate custom fields with Executive Boardroom template
INSERT INTO custom_field_joins (id, custom_field_template_id, custom_field_id, created_at)
VALUES 
    ('32000000-0000-0000-0000-000000000004'::uuid,
     '31000000-0000-0000-0000-000000000002'::uuid,
     '30000000-0000-0000-0000-000000000001'::uuid,
     NOW()),
    ('32000000-0000-0000-0000-000000000005'::uuid,
     '31000000-0000-0000-0000-000000000002'::uuid,
     '30000000-0000-0000-0000-000000000004'::uuid,
     NOW()),
    ('32000000-0000-0000-0000-000000000006'::uuid,
     '31000000-0000-0000-0000-000000000002'::uuid,
     '30000000-0000-0000-0000-000000000005'::uuid,
     NOW())
ON CONFLICT (custom_field_template_id, custom_field_id) DO NOTHING;
