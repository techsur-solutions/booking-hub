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
