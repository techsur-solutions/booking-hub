## F5: Custom Fields

**Description:** The Custom Field Service lets admins define custom fields (equivalent to legacy `Customfield`), organize them into templates/field-pickers, attach them to Bookings via a join entity (legacy `Customfieldjoin`), and store per-booking values (legacy `Customfieldvalue`). The Booking creation/edit form renders applicable fields dynamically based on field-picker configuration.

**Terminology:**
- **Custom Field:** An admin-defined field definition (label, type, options) that can be attached to Bookings.
- **Field Template / Field-Picker:** A named grouping/configuration of custom fields that determines which fields render on a given Booking form context.
- **CustomFieldJoin:** The association between a Custom Field and a context (e.g., a Location, or globally) determining where it is offered.
- **CustomFieldValue:** The actual value a user entered for a given Custom Field on a specific Booking.

**Sub-features:**
- Admin management of custom field definitions
- Admin management of field templates
- Attachment of custom fields to bookings via field-picker configuration
- Dynamic rendering of applicable fields on booking create/edit form
- Display of custom field values in calendar/list/detail views and public feeds

**Process:**
1. Admin user (holding the admin permission, F7) creates a Custom Field definition with `label`, `field_type` `[OPEN QUESTION — deferred to F0: exact supported types — free-text, numeric, date, dropdown/select — are unconfirmed; PRD Open Question #6]`, and, for choice-based types, an `options[]` list.
2. Admin user creates or edits a Field Template, associating one or more Custom Fields with it via a CustomFieldJoin, and associates the Template with a context (e.g., a specific Location, or "all bookings") `[OPEN QUESTION — deferred to F0: exact join context/scope model unconfirmed]`.
3. When a user opens the Booking creation/edit form for a given Location, the Booking Service (F1) queries the Custom Field Service for the applicable Field Template/fields for that context and renders them dynamically alongside the standard fields.
4. User enters values for each rendered Custom Field; on Booking submission, these are sent as `custom_field_values[]` (F1 §Inputs) and persisted as CustomFieldValue rows linked to the Booking.
5. Service validates each submitted value against its Custom Field's `field_type` and any required/optional flag `[OPEN QUESTION — deferred to F0: whether legacy enforces required/optional and type-specific validation, or treats all custom fields as free-text/optional — PRD Open Question #6]`; interim default: all custom fields are optional, free-text-validated only (no type coercion enforced), pending F0 confirmation.
6. Custom field values are included in the Booking representation returned by calendar/list/detail views (F1) and, where applicable, in Public Feed payloads (F9) `[OPEN QUESTION — deferred to F0: which custom fields, if any, are exposed in public feeds vs. internal-only — unconfirmed]`.
7. Admin edits/deletes a Custom Field definition or Field Template; existing CustomFieldValue rows for past Bookings are retained even if the definition is later deleted (historical values are not purged) `[OPEN QUESTION — deferred to F0: confirm retention policy]`.

**Inputs:**
- `label` (string, required): Custom Field display label.
- `field_type` (enum, required): e.g., `text`, `number`, `date`, `select` — exact enum pending F0 confirmation.
- `options` (array of string, required for `select` type): Choice list.
- `template_id` (UUID/long, optional): Field Template this field belongs to.
- `context_id` (UUID/long, optional): Location or other context the Field Template applies to.
- `custom_field_values` (array of {field_id, value}, submitted with Booking — see F1 §Inputs).

**Outputs:**
- Custom Field representation: `id`, `label`, `field_type`, `options[]`, `created_at`, `updated_at`.
- Field Template representation: `id`, `name`, `field_ids[]` (via CustomFieldJoin), `context_id`.
- CustomFieldValue representation (embedded in Booking payload): `{field_id, label, field_type, value}`.

**Validation:**
- `label` is required and non-empty for a Custom Field.
- `options[]` is required and non-empty when `field_type = select` (or equivalent choice type).
- A submitted `custom_field_values[]` entry must reference a `field_id` that is applicable (via Field Template/CustomFieldJoin) to the Booking's Location context; values for inapplicable fields are rejected `[OPEN QUESTION — deferred to F0: whether legacy enforces this strictly or accepts any submitted value]`.
- Required/optional enforcement and type-specific validation (numeric/date format) are `[OPEN QUESTION — deferred to F0 — PRD Open Question #6]`; interim: not enforced beyond field existence.
- Only users holding the admin permission (F7) may create/edit/delete Custom Field definitions and Field Templates.

**Error States:**
| Scenario | HTTP Status | Error Code | Message |
|---|---|---|---|
| Missing/empty `label` on Custom Field creation | 400 | CUSTOM_FIELD_LABEL_REQUIRED | "Custom field label is required" |
| `field_type = select` with empty `options[]` | 400 | CUSTOM_FIELD_OPTIONS_REQUIRED | "Options are required for selection-type fields" |
| Caller lacks admin permission | 403 | CUSTOM_FIELD_FORBIDDEN | "You do not have permission to manage custom fields" |
| Submitted `custom_field_values[]` references unknown `field_id` | 400 | CUSTOM_FIELD_VALUE_INVALID | "One or more submitted custom field values are invalid" |
| Target Custom Field or Template `id` not found | 404 | CUSTOM_FIELD_NOT_FOUND | "Custom field or template not found" |

**API Surface (this feature):** see `Y1-api.md` §CustomFields (`GET/POST /custom-fields`, `GET/PUT/DELETE /custom-fields/{id}`, `GET/POST /field-templates`, `GET/PUT/DELETE /field-templates/{id}`).

**Schema Surface (this feature):** owns tables `custom_fields`, `custom_field_templates`, `custom_field_joins`, `custom_field_values` — see `Y0-schema.md` §CustomField. `custom_field_values.booking_id` references the Booking Service's `bookings.id` by ID only (cross-service reference, no DB-level FK).
