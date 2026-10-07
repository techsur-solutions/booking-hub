# Custom Fields Audit — Customfields (Controller), Customfield/Customfieldjoin/Customfieldvalue (Models)

Source repository: `https://github.com/neokoenig/RoomBooking` (branch `master`). All citations below are `path` references into that repo as fetched via `raw.githubusercontent.com`.

## Customfields controller

**File:** `controllers/Customfields.cfc` (160 lines)

### Permission gates (controller `init()`)

```
filters(through="checkPermissionAndRedirect", permission="accessCustomfields");
filters(through="denyInDemoMode", except="index");
useslayout(template=false, only="fieldpicker");
verifies(only="delete,deletetemplate", params="key", paramsTypes="string", route="home", error="Sorry, that field can't be found");
```
(`controllers/Customfields.cfc` lines 7-17)

- Every action requires permission `accessCustomfields` (a dedicated permission row — see `install/new-installation.sql` line 152: `INSERT INTO permissions VALUES ('accessCustomFields', '1','0','0','0', 'Allow configuration of custom fields and templates')` — only role 1, i.e. the first/admin role column, is granted `1` by default; all other roles are `0`).
- The parent `Controller.cfc` additionally gates **everything** application-wide behind `permission="accessapplication"` (`controllers/Controller.cfc` line 9) — `accessCustomfields` is layered on top of that base gate, not a replacement for it.
- `denyInDemoMode` blocks all actions **except** `index` when the install is running in demo mode (config not in this file; a global demo-mode flag enforced elsewhere in the base Wheels/Controller stack).
- `delete` and `deletetemplate` are guarded by `verifies()`: if `params.key` isn't present (and a string), the request is redirected to the `home` route with the error "Sorry, that field can't be found" **before the action body runs**. No equivalent `verifies()` guard exists for `edit`, `update`, `edittemplate`, or `updatetemplate` — those actions trust `params.key` (and `params.type` for templates) unguarded, relying on `model(...).findOne(where=...)` returning an empty/non-object result if the key is bad (no explicit null-check follow-up in `edit`/`update` either — see Action-by-action table).

### Routes

`config/routes.cfm` defines **no explicit routes for the Customfields controller** — only `users`, `sessions`, `eventdata`, and the default `home` (→ `bookings#index`) are custom-routed. Customfields relies entirely on the Wheels framework's default RESTful/convention routing (`/customfields/:action` or `/customfields/:action/:key`), consistent with every action name observed in the controller.

### Action-by-action

| Action | Route (convention) | Request shape | Response shape | Notes |
|---|---|---|---|---|
| `index()` | `GET /customfields` | none | Loads `customfields` (all `Customfield` rows, `order="parentmodel ASC,sortorder ASC"`) and `customtemplates` (all `Template` rows, `order="parentmodel ASC"`) into the view | Pure read, no mutation |
| `add()` | `GET /customfields/add` | none | `customfield = model("customfield").new()` (blank object for the form) | — |
| `create()` | `POST /customfields/create` | `params.customfield` (struct) | Redirect to `index` on success; re-render `add` with error on failure | Guarded by `if(structKeyExists(params,"customfield"))` — a POST with no `customfield` key silently does nothing (falls through, blank response) |
| `edit(key)` | `GET /customfields/edit/:key` | `params.key` | Loads one `Customfield` row by `id` | **No existence check** — if `key` matches no row, `findOne` returns an empty/non-object and the view will error or render blank; no `verifies()` guard on this action (see Permission gates) |
| `update(key)` | `POST /customfields/update/:key` | `params.key`, `params.customfield` | Redirect to `index` on success; re-render `edit` with error on failure | Same "no existence check" gap as `edit` |
| `delete(key)` | `GET/POST /customfields/delete/:key` | `params.key` | Redirect to `index` with success/error flash | Guarded by `verifies()` (see above). Deletion behavior/cascade: see Open Questions — retention policy |
| `addtemplate()` | `GET /customfields/addtemplate` | none | `template = model("template").new()`, `template.template=''` | — |
| `createtemplate()` | `POST /customfields/createtemplate` | `params.template` (struct, incl. `parentmodel`+`type`) | Redirect to `index` on success; re-render `addtemplate` with error | — |
| `edittemplate(key,type)` | `GET /customfields/edittemplate/:key?type=X` | `params.key` (→ `parentmodel`), `params.type` | Loads one `Template` row by composite `parentmodel`+`type` | No `verifies()` guard; `findOne` can return empty |
| `updatetemplate(key,type)` | `POST /customfields/updatetemplate/:key?type=X` | `params.key`, `params.type`, `params.template` | Redirect to `index` on success; re-render `edittemplate` with error | — |
| `deletetemplate(key,type)` | `GET/POST /customfields/deletetemplate/:key?type=X` | `params.key`, `params.type` | Redirect to `index` with success/error flash | Guarded by `verifies()` |
| `fieldpicker()` | AJAX `GET /customfields/fieldpicker?key=X` (rendered with `useslayout(template=false)` — bare partial, no site chrome) | `params.key` (a `modeltype`, e.g. `event` or `location`) | Renders the `fieldpicker.cfm` view: `systemfields=model(params.key).new()` + `customfields=getBlankCustomFields(params.key)` | This is the **field-picker/context-selection UI** — see Views reviewed below for full behavior |

### Field-type / options / context logic found in the controller and its callers

The `Customfields` controller itself contains **no field-type switch/enum logic** and **no explicit context-join logic** — those live in:
1. The base `Controller.cfc` (inherited by every controller, including `Customfields`), which defines the actual read/write path for custom field values:
   - `getCustomFields(objectname, key)` (`controllers/Controller.cfc` lines 18-52) — raw SQL `LEFT JOIN` across `customfields` → `customfieldjoins` (filtered on `customfieldchildid = key`) → `customfieldvalues`, scoped `WHERE customfields.parentmodel = objectname`. This is the exact mechanism that resolves "which custom fields + their stored values apply to record `key` of model `objectname`" — see join/context model verdict below.
   - `getBlankCustomFields(objectname)` (lines 57-78) — same query without the value join, used by `fieldpicker()` to list all field **definitions** for a given parentmodel type (no specific record context — used for the picker UI, not for filling in values).
   - `updateCustomFields(objectname, key, customfields)` (lines 83-98) — for each submitted `field → value` pair: looks up an existing `Customfieldjoin` row keyed on `(customfieldsid=field, customfieldchildid=key)`; if found, updates the linked `Customfieldvalue.value`; if not found, creates a new `Customfieldvalue` then a new `Customfieldjoin` row linking `customfieldsid`, `customfieldchildid=key`, and the new value's id. **No validation of `value` against the field's declared `type`, `required` flag, or `options` is performed anywhere in this path** — it accepts and stores whatever string is submitted.
2. The `views/shortcodes/field.cfm` shortcode renderer (invoked via `processShortCodes()` wherever `[field id=N]` appears in a stored Template), which contains the actual field-type `cfswitch` — see Views reviewed.

### `parentmodel` / context enum (confirms the join/context model)

`events/onapplicationstart.cfm` (lines 8-16) sets, at app boot:
```
application.rbs = { ..., modeltypes="event,location", templatetypes="form,output" };
```
This list literally populates the `customfield.parentmodel` `<select>` options in `views/customfields/_form.cfm` (`options=application.rbs.modeltypes`, line 14) and the `key` `<select>` in `views/customfields/index.cfm`'s "Create Template" form (line 17). **`parentmodel` is therefore constrained (by the admin UI, not by a DB constraint) to exactly two values: `event` and `location`** — see Open Questions for the precise shape this gives the join/context model.

### Views reviewed

All views live under `views/customfields/` (8 files) plus `views/common/form/_customfields.cfm` and `views/shortcodes/{field,output}.cfm` (discovered via the live repo tree and pulled in because they are the actual render/execution point for field-type and value logic described above).

- **`fieldpicker.cfm`** (36 lines) — **direct evidence for the context-selection UI**. Renders two button-group lists inside a bare modal fragment (no layout, per `useslayout(template=false, only="fieldpicker")`):
  - "Custom Fields" — one button pair (`field` / `output`) per row in `customfields` (the query result of `getBlankCustomFields(params.key)`), labeled `#name# (#type#)`.
  - "System Fields" — one button pair per entry in `systemfields.systemfields` (an array of structs with `name`/`type`/`description`, defined on each model itself — see Model note below), labeled `#name# (#type#)`.
  - Clicking a button just toggles a CSS class (`fielddata-selected`) client-side; the actual shortcode insertion (`[field id=...]` / `[output id=...]`) happens in the calling `_templateform.cfm`'s `insert_customfield()` JS handler (below), **confirming this is purely an authoring-time picker for building a Template** — it is not where a booking's custom-field values get filled in (that happens via `views/common/form/_customfields.cfm` + the `[field]` shortcode at booking-create/edit time). This resolves the open question of what "context" means here: the context is the **model type** (`event` or `location`) passed as `params.key`, not a specific Location/Event instance.
  - "System fields" come from `model(params.key).new()`'s `systemfields` property — a model-declared array of built-in fields (name/type/description), separate entirely from the `Customfield` table; this confirms custom fields and system fields are two parallel but distinct field-definition sources, both surfaced through the same picker and the same `[field]`/`[output]` shortcode mechanism.
- **`_form.cfm`** (94 lines) — the Customfield create/edit form. **This is the authoritative, user-facing confirmation of the supported `type` enum**: `#select(objectname="customfield", property="type", label="Type", options="textfield,select,textarea,radio,checkbox")#` (line 10) — exactly five values: `textfield, select, textarea, radio, checkbox`. Also renders: `name`, `parentmodel` (via `application.rbs.modeltypes`), `description`, `options` (hidden field populated by an inline Ace JSON editor — raw JSON textarea, no client-side schema validation beyond "valid JSON" from the editor itself), `class` (optional extra CSS class), `sortorder` (numeric), and `required` (checkbox). The in-page help text documents the **expected `options` JSON shape per type** (array of single-key objects, e.g. `[{"key1":"value1"}]`) for `select`/`radio`/`checkbox`, but this is human-readable guidance only — **no programmatic validation of the JSON shape exists in this view or anywhere else audited** (confirms the Open Question verdict below).
- **`_templateform.cfm`** (62 lines) — the Template editor (`gridmanager` drag-and-drop grid builder). The `insert_customfield()` JS callback AJAX-loads `fieldpicker.cfm` into a modal, and on "Insert Field" builds the literal shortcode string `[field id=<id>]` or `[output id=<id>]` (the `data-type` from the clicked button) and inserts it into the grid HTML via `gm.addEditableAreaClick(...)`. **No client-side `field_type` re-validation happens here** — it trusts whatever `type` the picker displayed.
- **`add.cfm` / `edit.cfm`** — thin wrappers around `_form.cfm` (create vs. update form actions); no additional logic.
- **`addtemplate.cfm` / `edittemplate.cfm`** — thin wrappers around `_templateform.cfm`, pre-setting `template.parentmodel`/`template.type` from `params.key`/`params.type`.
- **`index.cfm`** (126 lines) — lists all `Customfield` rows (columns: ID, Model, Name, Type, Description, Actions) and all `Template` rows (columns: Model, Type, Actions), with Edit/Delete links for each and a "Create Template" form (`key` = model type via `application.rbs.modeltypes`, `type` = `application.rbs.templatetypes` i.e. `form`/`output`). Inline help text states plainly: *"Remember, changes affect all instances of the model, and you will need to restart the application to see the effect"* — confirming templates/field definitions are **global per model type**, not per-instance, and template changes require an app restart to take effect (likely due to `application.rbs.templates` being cached at `onApplicationStart` — see `events/onapplicationstart.cfm` line 35-37, which only populates the template cache once at boot).
- **`views/common/form/_customfields.cfm`** (15 lines) — the actual injection point used by booking/location forms to render their custom fields: loops the `customfields` query and builds `[field id=N]` for every row, then calls `processShortCodes()` to expand them via the `field` shortcode. This is the consumer-side counterpart to the admin-side `fieldpicker.cfm` — it always renders **every** custom field defined for the current parentmodel (no per-instance subset selection at render time; subsetting is only a Template-authoring concept via the picker).
- **`views/shortcodes/field.cfm`** (185 lines) — the actual field-type `cfswitch` (fetched because it is the only place a `field_type`-driven render decision is made, and directly answers whether `options[]`/`required` are enforced). For `fieldtype EQ "custom"`: switches on `attr.type` with cases `textfield`, `select`, `textarea`, `radio`, `checkbox`, plus a `cfdefaultcase` that renders `"Incorrect Field Type Specified"` for any other value — **this is strong secondary confirmation of the exact 5-value type enum** (matches `_form.cfm`'s `<select>` options exactly, no discrepancy found). `required` only ever reaches this shortcode as a boolean that's merged into `fieldValues.required` (passed through to the Wheels form-helper tag, e.g. `textFieldTag(required=true)`), which at most adds an HTML5 `required` attribute / Wheels-level client validation — **there is no server-side re-check of `required` when a value is actually saved** (see `Controller.updateCustomFields` above, which saves unconditionally).
- **`views/shortcodes/output.cfm`** (54 lines) — the read-only `[output id=N]` counterpart; looks up the stored `Customfieldvalue.value` for a custom field, or a system-field value off `variables[modeltype][id]` for system fields, and renders it with light formatting (date auto-format via `isDate()`, otherwise `autolink()`). No type-specific formatting for `select`/`radio`/`checkbox` beyond "treat the stored string as text, auto-link it if applicable" — i.e. output rendering does **not** re-interpret `options[]` to show a human label, it shows the raw stored value verbatim.

## Customfield model

**File:** `models/Customfield.cfc`

```
component extends="Model" hint=""
{
	public void function init() {
		hasMany(name="customfieldjoins");
		property(name="sortorder", defaultValue=0);
	}
}
```

This is the **entire** file — 10 lines of actual code. Confirmed exactly as present, no more, no less:
- One association: `hasMany(name="customfieldjoins")` — **no `dependent=` option is passed**, which in CFWheels ORM defaults to no automatic cascade behavior on the parent's deletion (see Open Questions — retention policy).
- One declared property default: `sortorder` defaults to `0`.
- **No validations of any kind are declared on this model** — no `validatesPresenceOf`, no `validatesInclusionOf` for `type` or `parentmodel`, no `validatesFormatOf` for `options`. Every piece of "validation" a user experiences (type enum, required-checkbox, parentmodel enum) is **UI-only**, enforced solely by the `<select>` option lists in `_form.cfm` and `index.cfm` — nothing stops a row with an arbitrary `type` or `parentmodel` string from existing if inserted directly (e.g. via SQL, a future migration script, or a bug elsewhere) — the `output.cfm`/`field.cfm` shortcode renderers would simply hit their `cfdefaultcase` ("Incorrect Field Type Specified") for such a row.
- Table schema (`install/new-installation.sql` lines 32-44) confirms the full column set and matches every field referenced above: `id` (PK, autoincrement), `name` (`varchar(255)`, `NOT NULL`), `parentmodel` (`varchar(255)`, `NOT NULL`), `type` (`varchar(50)`, `NOT NULL`), `options` (`longtext`, nullable), `class` (`varchar(255)`, nullable), `description` (`varchar(255)`, nullable), `sortorder` (`smallint(5)`, `NOT NULL DEFAULT 0`), `required` (`tinyint(1)`, `NOT NULL DEFAULT 0`). **No DB-level `CHECK`/`ENUM` constraint on `type` or `parentmodel`** — confirms the "UI-only enum" finding above at the schema level, not just the ORM level.

## Customfieldjoin model

**File:** `models/Customfieldjoin.cfc`

```
component extends="Model" hint=""
{
	public void function init() {
		belongsTo(name="customfield", joinType="left");
		belongsTo(name="customfieldvalue", joinType="left");
	}
}
```

This is the **entire** file — the join is a thin, association-only class exactly as the plan predicted from file size. **Confirmed: this is the per-record-instance join, not a Customfield↔Location or Customfield↔Event model-level association.**

Table schema (`install/new-installation.sql` lines 21-27):
```sql
CREATE TABLE `customfieldjoins` (
  `customfieldsid` int(11) NOT NULL,
  `customfieldchildid` int(11) NOT NULL,
  `customfieldvalueid` int(11) NOT NULL,
  PRIMARY KEY (`customfieldsid`,`customfieldchildid`,`customfieldvalueid`)
);
```
Three columns, composite PK, **no foreign-key constraints declared** (consistent with the install script's blanket `SET FOREIGN_KEY_CHECKS=0;` at the top and no FK clauses anywhere in the file — this schema relies entirely on ORM-level association integrity, not DB-enforced referential integrity).

Cross-referencing `Controller.updateCustomFields()` (base `Controller.cfc` lines 83-98), the three columns resolve to:
- `customfieldsid` → the `Customfield` definition's `id` (which field)
- `customfieldchildid` → the **id of the specific model-instance record** the value belongs to (an `Event.id` or a `Location.id`, depending on the Customfield's `parentmodel`) — **this is the per-instance context link**, distinct from the `parentmodel`-type-level scoping on `Customfield` itself
- `customfieldvalueid` → the `Customfieldvalue.id` holding the actual stored value

So the full shape is two-tier: **`Customfield.parentmodel`** scopes a field definition to a model *type* (`event` or `location`, applies to every instance of that type identically), and **`Customfieldjoin.customfieldchildid`** scopes one *value* to one specific instance of that type. There is no third tier scoping a field to, e.g., one specific Location only — a custom field defined with `parentmodel="location"` applies to **every** Location.

## Customfieldvalue model

**File:** `models/Customfieldvalue.cfc`

```
component extends="Model" hint=""
{
	public void function init() {
		hasMany(name="customfieldjoins");
	}
}
```

Entire file — again association-only, no validations, no declared properties beyond the implicit table columns. Table schema (`install/new-installation.sql` lines 49-54):
```sql
CREATE TABLE `customfieldvalues` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `value` longtext,
  PRIMARY KEY (`id`)
);
```
Confirmed: **this is the per-booking(-instance) value store** exactly as the plan's context expected — one row per stored value, linked to its owning record-instance (Event or Location row) only indirectly through a `Customfieldjoin` row (there is no direct `eventid`/`locationid` column on `Customfieldvalue` itself; the join table is the only path from a value back to its owning instance). `value` is untyped `longtext` — whatever string representation the field's `type` produces (plain text, a JSON-ish key string for radio/checkbox/select, or a date string) is stored identically; the column itself carries no type information.

## Open Questions (custom-fields)

Cross-referenced against PRD Open Question #6 and FRD F5 §Process steps 1, 2, 5. All five required items below have an explicit, cited verdict — none are left ambiguous.

1. **Exact supported `field_type` set** — **RESOLVED.** Exactly five values: `textfield`, `select`, `textarea`, `radio`, `checkbox`. Confirmed twice, independently: the admin form's `<select>` options (`views/customfields/_form.cfm` line 10) and the render-time `cfswitch` cases in `views/shortcodes/field.cfm` (lines 109-139 for the "custom" branch) — both lists match exactly, with the shortcode's `cfdefaultcase` rendering "Incorrect Field Type Specified" for anything else. No `numeric` or `date` type exists for custom fields (system fields separately support `datepicker`/`colourpicker`, but those are declared per-model, not part of the `Customfield` table's type enum).

2. **Whether `options[]` is required/enforced for choice-based types** — **RESOLVED — not enforced.** `options` is a free-form `longtext` column with **no DB constraint, no model validation** (`models/Customfield.cfc` has zero `validatesX` calls), and **no server-side JSON-shape check** anywhere in the controller or shortcode renderers audited. The admin UI (`_form.cfm`) provides an Ace JSON editor and inline documentation of the expected shape (array of single-key `{"key":"label"}` objects) as a courtesy, but nothing stops saving `select`/`radio`/`checkbox` with empty, malformed, or non-JSON `options` — the failure mode in that case is a runtime error/blank render in `views/shortcodes/field.cfm` (e.g. `arraylen(tempArray)` on a non-array) rather than a save-time validation rejection.

3. **The exact join/context model (per-Location vs. "all bookings")** — **RESOLVED — neither, it's a third shape: per-model-type, not per-instance, not global-across-types.** `Customfield.parentmodel` is constrained (UI-only, via `application.rbs.modeltypes = "event,location"` set in `events/onapplicationstart.cfm` line 14) to exactly `event` or `location`. A field tagged `parentmodel="location"` applies identically to **every** Location record (not one specific Location) — there is no per-instance definition scoping. The actual per-instance link is at the **value** layer: `Customfieldjoin.customfieldchildid` ties one stored `Customfieldvalue` to one specific Event-or-Location row id (`controllers/Controller.cfc` `getCustomFields`/`updateCustomFields`, lines 18-98). So: field *definitions* are global-per-type; field *values* are per-instance. The FRD's "unconfirmed" framing (a specific Location vs. all bookings) maps onto this as: it is **always** "all bookings/locations of that type" — a narrower per-Location-instance definition scope does not exist in the legacy system at all.

4. **Whether required/optional + type-specific validation is enforced** — **RESOLVED — not enforced server-side, UI-only at best.** `Customfield.required` (a `tinyint(1)` flag) only ever reaches `views/shortcodes/field.cfm` (lines 98-104), where it conditionally adds `required=true` to the struct passed into a Wheels form-helper tag (e.g. `textFieldTag(required=true)`) — this is, at most, an HTML5 `required` attribute on the rendered `<input>`, trivially bypassable by submitting the form field directly or via a non-browser client. The actual save path, `Controller.updateCustomFields()` (`controllers/Controller.cfc` lines 83-98), unconditionally creates/updates a `Customfieldvalue.value` for whatever was submitted — it never checks `customfield.required`, never checks `customfield.type` against the submitted value's shape, and never rejects a missing/empty value. This fully confirms — with citation — the FRD's interim placeholder ("all custom fields are optional, free-text-validated only") is **accurate for "optional"** but overstates the validation: there is **no** validation at all, free-text or otherwise, server-side.

5. **Retention policy: does deleting a Customfield definition purge or retain historical Customfieldvalue rows** — **RESOLVED — retains (orphans), does not purge.** `Customfields.delete()` (`controllers/Customfields.cfc` lines 79-87) calls only `customfield.delete()` on the `Customfield` row. `Customfield.cfc`'s `hasMany(name="customfieldjoins")` declaration passes **no `dependent=` argument** (CFWheels syntax for cascade behavior, e.g. `dependent="deleteAll"`), so the framework default (no automatic cascade) applies. Neither `Customfieldjoin` nor `Customfieldvalue` rows are touched by this delete — they become orphaned (a `Customfieldjoin.customfieldsid` pointing at a now-nonexistent `Customfield.id`). This is further corroborated by the schema itself: the `customfieldjoins`/`customfieldvalues` tables declare **no foreign-key constraints** (`install/new-installation.sql`, consistent with the file's blanket `SET FOREIGN_KEY_CHECKS=0`), so even a direct SQL delete would not cascade. Historical `Customfieldvalue.value` data therefore survives a definition's deletion indefinitely, unreachable through the normal `getCustomFields()` query (which inner-depends on `customfields.parentmodel = objectname`, i.e. joins from the *Customfield* side) but still physically present in the `customfieldvalues` table.
