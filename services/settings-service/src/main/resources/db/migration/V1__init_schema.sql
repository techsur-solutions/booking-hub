CREATE TABLE settings (
    id                   INTEGER PRIMARY KEY DEFAULT 1, -- singleton row enforced by CHECK below
    approve_booking      BOOLEAN NOT NULL DEFAULT true,
    calendar_slot_size   INTEGER NOT NULL DEFAULT 30,    -- minutes
    calendar_min_time    TIME NOT NULL DEFAULT '08:00',
    calendar_max_time    TIME NOT NULL DEFAULT '18:00',
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by           UUID NULL,
    CONSTRAINT chk_settings_singleton CHECK (id = 1),
    CONSTRAINT chk_settings_calendar_range CHECK (calendar_min_time < calendar_max_time),
    CONSTRAINT chk_settings_slot_size CHECK (calendar_slot_size > 0)
);
INSERT INTO settings (id) VALUES (1) ON CONFLICT (id) DO NOTHING;
