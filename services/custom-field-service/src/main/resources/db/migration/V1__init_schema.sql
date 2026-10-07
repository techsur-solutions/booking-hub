CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE custom_fields (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    label           VARCHAR(255) NOT NULL,
    field_type      VARCHAR(32) NOT NULL,             -- 'text' | 'number' | 'date' | 'select' (pending F0 confirmation)
    options         JSONB NULL,                        -- required when field_type = 'select'
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ NULL
);

CREATE TABLE custom_field_templates (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    context_id      UUID NULL,                         -- e.g., Location id this template applies to; NULL = global
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE custom_field_joins (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    custom_field_template_id UUID NOT NULL REFERENCES custom_field_templates(id) ON DELETE CASCADE,
    custom_field_id          UUID NOT NULL REFERENCES custom_fields(id) ON DELETE CASCADE,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_template_field UNIQUE (custom_field_template_id, custom_field_id)
);

CREATE TABLE custom_field_values (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id      UUID NOT NULL,                     -- cross-service reference to booking-service
    custom_field_id UUID NOT NULL REFERENCES custom_fields(id),
    value           TEXT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_custom_field_values_booking ON custom_field_values (booking_id);

CREATE OR REPLACE FUNCTION set_updated_at() RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_custom_fields_updated_at BEFORE UPDATE ON custom_fields
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_custom_field_templates_updated_at BEFORE UPDATE ON custom_field_templates
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_custom_field_values_updated_at BEFORE UPDATE ON custom_field_values
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
