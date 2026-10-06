# Reference Data Audit — Locations, Resources (Controllers)

Source: `neokoenig/RoomBooking` (master branch). All citations reference file paths relative to repo root; line numbers are to the fetched blob content at audit time (2026-10-06).

## Locations controller

**File:** `controllers/Locations.cfc` (126 lines)

### Permission gates (init(), lines 7–22)

- `super.init()` first applies the global `Controller.cfc` filter: `checkPermissionAndRedirect(permission="accessapplication")` on every action (deny-all by default — see `controllers/Controller.cfc` lines 7–12).
- `filters(through="f_checkLocationsAdmin")` (line 12) — runs on **every** action. Checks `application.rbs.setting.allowLocations`; if falsy, redirects to `home` with "Facility to edit Locations has been disabled" (lines 114–118). This is a feature-flag gate, not a role gate.
- `filters(through="checkPermissionAndRedirect", permission="accessLocations", except="list,view")` (line 13) — requires the `accessLocations` permission for every action **except** `list` and `view`. Per `install/new-installation.sql` line 153, `accessLocations` is granted to `admin` and `editor` roles only (`'1','1','0','0'` for admin/editor/user/guest) — `list`/`view` are reachable by any authenticated role (and per `accessapplication`/`accessCalendar` grants, effectively all roles including guest, since those permissions are `1,1,1,1` in the seed data).
- `filters(through="checkPermissionAndRedirect", permission="accessCalendar", except="list,view")` (line 14) — additional check, same except-list; `accessCalendar` is also granted to all four roles in seed data (line 151), so in practice this filter is redundant with the all-roles grant but is still code-enforced.
- `filters(through="_setModelType")` (line 15) — sets `request.modeltype="location"` for every action (used by the Custom Fields / shortcode system).
- `filters(through="_getLocations", only="index,list")` (line 17) — loads `locations` list from `controllers/Controller.cfc`'s `_getLocations()` (lines 106–108): `model("location").findAll(order="building,name")`.
- `verifies(only="view,edit,update,delete", params="key", paramsTypes="integer", route="home", error="Sorry, that event can't be found")` (line 20) — requires an integer `params.key` for those four actions, redirecting to `home` otherwise. Note the copy-pasted error text ("that event can't be found") — a legacy wording artifact, not Location-specific text; purely cosmetic.

**No `list` action body exists** (line 28: `public void function list() {}` — empty). This is a Wheels convention: the view template `views/locations/list.cfm` renders directly off whatever `locations` variable is in scope from the `_getLocations` filter (confirmed: `list.cfm` references `locations.recordcount` with no local re-fetch). `list` has no permission requirement beyond the global `accessapplication`/`accessCalendar` grants (both all-roles).

### Actions

| Action | Route binding | Permission (beyond global) | Request params | Response / redirect |
|---|---|---|---|---|
| `view` (line 34) | Wheels REST convention — no explicit custom route in `config/routes.cfm`; resolves via default resource routing to `Locations#view` with `key` | Public per `list,view` exception | `params.key` (int, via `verifies`) | Sets `location` + `customfields` vars; renders `views/locations/view.cfm`. No redirect — direct render. |
| `add` (line 42) | default resource route | `accessLocations` required | none | `location=model("location").new()`; renders `views/locations/add.cfm` |
| `create` (line 50) | default resource route, POST | `accessLocations` required | `params.location` (struct) | On `location.save()` success → `redirectTo(action="index", success="location successfully created")`. On failure → `renderPage(action="add", error="There were problems creating that location")` (re-renders add form with validation errors via `errorMessagesFor`). |
| `edit` (line 65) | default resource route | `accessLocations` required | `params.key` (int) | `location=model("location").findOne(where="id = #params.key#")`; also re-sets `request.modeltype="location"` (redundant with filter) and fetches `customfields`; renders `views/locations/edit.cfm` |
| `update` (line 75) | default resource route, PUT/PATCH | `accessLocations` required | `params.key`, `params.location` (struct), optional `params.customfields` | On save success → also calls `updateCustomFields()` if custom fields were submitted, then `redirectTo(action="index", success="Location successfully updated")`. On failure → `renderPage(action="edit", error=...)`. |
| `delete` (line 94) | default resource route, DELETE | `accessLocations` required | `params.key` (int) | **Guard: only a minimum-count check** — `checkLocation=model("location").findAll(); if(checkLocation.recordcount GT 1)` (line 96): delete is only attempted if more than one Location exists system-wide. **There is NO guard checking whether the Location is referenced by any existing Booking/Event** (no query against `events` or any join table before delete — confirmed by reading the full function body, lines 94–109). On success → `redirectTo(action="index", success="Location successfully deleted")`. On failure (e.g. the "only one left" case) → `redirectTo(action="index", error="At least one Location is required.")` or "There were problems deleting that Location" if `.delete()` itself returns false. |

**Delete-when-referenced-by-Booking: HARD DELETE, UNGUARDED.** See "Open Questions" section below for the full mechanism proof (Wheels soft-delete only activates per-model when a `deletedat`/`deletedAt` column exists on that model's table, and `locations` has no such column per `install/new-installation.sql` — confirmed absent at lines 95–104). The controller's only safety check is "don't delete the last remaining Location" — it does **not** check `events.locationid` for any row referencing the Location being deleted. A delete on a Location that has existing Bookings will succeed and hard-remove the row, leaving `events.locationid` pointing at a non-existent location id (an orphaned foreign-key-shaped value — there is no DB foreign key constraint declared in `install/new-installation.sql`, confirmed: no `FOREIGN KEY`/`REFERENCES`/`CONSTRAINT` clause anywhere in the file).

### Views reviewed (`views/locations/*.cfm`)

- **`_form.cfm`** (38 lines) — **This is the direct evidence source for the Location display-metadata field set.** The form renders via a shortcode template system: if an admin-configured custom template exists at `application.rbs.templates.location.form` it's used instead; otherwise the **default template** (lines 10–34) lays out exactly these fields via `[field id="..."]` shortcodes: `name`, `building`, `description`, `class`, `colour`, `layouts` — followed by `#includePartial(partial="/common/form/customfields")#` for admin-defined custom fields. This exactly matches the model's `systemfields` array (see Location model section below) — the view does **not** expose any field beyond what the model declares; `layouts` (plural) is the real field name, not `layout` (singular) as FRD assumed.
- **`add.cfm`** (11 lines) — thin wrapper: `panel(title="New Location")` → `startFormTag(action="create")` → `includePartial("form")` → `submitTag(value="Create New Location")`. No fields of its own.
- **`edit.cfm`** (11 lines) — same wrapper pattern, `startFormTag(action="update", key=location.id)`.
- **`index.cfm`** (45 lines) — admin table view: columns ID, Building, Name, Description, Actions (View/Edit/Delete). The Delete link uses `confirm='Are you Sure?'` (line 31) — a client-side JS `confirm()` only, **no server-side re-confirmation step**; this is the sole "are you sure" UX before the hard-delete described above fires.
- **`list.cfm`** (35 lines) — public-facing list (reachable via the `list`/`view` permission exception): columns Name, Description, Actions (More Information/view only) — no Edit/Delete exposed here, consistent with `list` being outside the `accessLocations` gate.
- **`view.cfm`** (35 lines) — detail view: renders `name`, `description`, `class`, `colour` via `[output]` shortcodes (note: `building` and `layouts` are **not** shown in the default view template, only in the form — an asymmetry worth flagging for parity work) plus any custom fields in a second column. Supports an admin-overridable template at `application.rbs.templates.location.output`, same shortcode-template mechanism as the form.

---

## Resources controller

**File:** `controllers/Resources.cfc` (127 lines)

### Permission gates (init(), lines 7–26)

- Global `accessapplication` gate applies (inherited, as above).
- `filters(through="checkPermissionAndRedirect", permission="accessresources")` (line 12) — applies to **every** action, with **no except list** (unlike Locations, which exempts `list`/`view`). Per seed data (`install/new-installation.sql` line 156), `accessResources` is granted **only to `admin`** (`'1','0','0','0'`) — Resources management is admin-only with no public list/view carve-out.
- `filters(through="_checkResourcesAdmin")` (line 13) — feature-flag gate on `application.rbs.setting.allowResources`; redirects to `home` with "Facility to add/edit resources has been disabled" if off (lines 91–95).
- `filters(through="_getresources", only="index")` (line 17) — note the lowercase `_getresources`; this does **not** match the base `Controller.cfc`'s `_getResources()` (capital R, line 120) exactly in CFML's case-insensitive function-name resolution it still resolves to the same function (CFML component methods are case-insensitive), so this is NOT a bug — it calls `model("resource").findAll(order="type,name")`.
- `filters(through="_getLocations")` (line 18) — loads all Locations (no `only=`, so on every action) for the "restrict to locations" multi-select in the form.
- `verifies(only="view,edit,update,delete", params="key", paramsTypes="integer", route="home", ...)` (line 21) — same pattern as Locations, despite there being no explicit `view` action defined in this controller (see below — likely dead code inherited from a copy-paste, since there's no `public void function view()` in Resources.cfc).
- `provides("html,json")` (line 25) — Resources controller supports JSON response format in addition to HTML (Locations controller does not declare this).

### Actions

| Action | Permission | Request params | Response / redirect |
|---|---|---|---|
| `add` (line 32) | `accessresources` (admin-only) | none | `resource=model("resource").new()`; renders `views/resources/add.cfm` |
| `create` (line 39) | `accessresources` | `params.resource` (struct) | Success → `redirectTo(action="index", success="resource successfully created")`. Failure → `renderPage(action="add", error="There were problems creating that resource")` |
| `edit` (line 54) | `accessresources` | `params.key` (int) | `resource=model("resource").findOne(where="id = #params.key#")`; renders `views/resources/edit.cfm`. **No custom-fields loading** (unlike Locations' `edit`) — Resources has no Custom Fields integration at all; `request.modeltype` is never set to `"resource"` anywhere in this controller. |
| `update` (line 61) | `accessresources` | `params.key`, `params.resource` (struct) | Success → `redirectTo(action="index", success="resource successfully updated")`. Failure → `renderPage(action="edit", error=...)` |
| `delete` (line 77) | `accessresources` | `params.key` (int) | **No guard at all** — not even the "at least one must remain" check Locations has. Unconditionally: `resource = model("resource").findOne(...); resource.delete()`. Success → `redirectTo(action="index", success="resource successfully deleted")`. Failure → error redirect. |

**Delete-when-referenced-by-Booking: HARD DELETE, UNGUARDED (more permissive than Locations).** Same underlying mechanism as Locations (no `deletedat` column on `resources` table — confirmed absent, `install/new-installation.sql` lines 173–181) — but Resources' `delete()` doesn't even have the minimum-count safety check Locations has. A Resource referenced by `eventresources` rows (the join table linking Events↔Resources, confirmed schema at lines 60–64: `eventid`,`resourceid` composite PK, no FK constraint) can be deleted at any time regardless of existing bookings, leaving orphaned `eventresources.resourceid` values.

**No `view` action exists in Resources.cfc** despite the controller's `verifies()` filter listing `view` (line 21) and the base routing conventions implying one should exist. This means visiting a `view` route for a Resource would hit Wheels' default "undefined action" behavior (not exercised further in this audit — flagged as a dead/vestigial filter reference, functionally harmless since nothing links to a Resources `view` route anywhere in the views reviewed).

### Ajax/Remote action: `checkavailability` (lines 100–126)

Not a CRUD action but directly relevant to Resource-booking interaction: given `params.id` (resource id), `params.eventid` (event being edited, to exclude from the conflict check), `params.start`/`params.end`, it queries `model("event").findAll(where="start <= ... AND end >= ... AND id != #eventid# AND resourceid = #id#", include="eventresources")` plus a second query for all-day events using date-only boundaries, and returns `0` (unavailable) or `1` (available) as plain text. This is the resource-conflict check used by the booking UI — confirms Resources participate in the same start/end overlap conflict logic as Locations' event scheduling, scoped per-resource via the `eventresources` join. No permission filter exempts this action, so it's gated by the controller-wide `accessresources` permission (admin-only) — meaning only admin-role users could even successfully call this endpoint through the UI as built, which is suspicious for an availability-check endpoint presumably meant for all booking users; flagged as an open question below.

### Views reviewed (`views/resources/*.cfm`)

- **`_form.cfm`** (37 lines) — **Direct evidence for the Resource field set.** Explicit (non-shortcode, plain Wheels helper) form fields: `name` (required, line 7), `description` (line 10), `type` (`select`, labeled "Grouping", options from `application.rbs.setting.resourceTypes` — a configurable list, line 13), `isunique` (checkbox, labeled "Unique Item", line 21 — with inline help text explaining unique vs generic resource semantics), `restrictlocations` (multi-select of Locations, line 29 — "Only allow the resource to be booked in these locations"). This confirms Resource's real field set is **`name`, `description`, `type`, `isunique`, `restrictlocations`** — not just `name` as the FRD assumed.
- **`add.cfm`** (11 lines) — thin wrapper, same pattern as Locations.
- **`edit.cfm`** (11 lines) — thin wrapper, same pattern as Locations.
- **`index.cfm`** (48 lines) — admin table: ID, Name+Description, Type (`<code>` styled), Restricted? (tick/cross via `tickorcross(len(restrictlocations))`), Unique? (tick/cross via `tickorcross(isunique)`), Actions (Edit/Delete only — **no View link**, consistent with no `view` action existing). Delete link uses the same client-side `confirm='Are you Sure?'` pattern, no server-side re-confirm.

---

## Location model

**File:** `models/Location.cfc` (82 lines)

### Associations (init(), lines 7–11)

- `hasMany("events")` (line 9) — default foreign key inferred as `locationid` (Wheels convention: singularized model name + "id"); confirmed against `events.locationid` column in schema. **No `dependent` option specified** — per `wheels/model/associations.cfm` `$deleteDependents()` (lines 95-131), `dependent` defaults to unset/false-equivalent in practice (the association's `dependent` key is only read if explicitly set; absent-dependent means `$deleteDependents()`'s condition `associations[loc.key].dependent != false` — an *unset* struct key compared with `!=false` in CFML evaluates true only if it was explicitly assigned a non-false value; since `dependent` was never passed to `hasMany("events")`, Wheels' own `$args()` helper does not inject a default for it here, so the key is effectively absent and the dependent-cascade path never triggers for Location→Events). This independently corroborates the controller-level finding: **no cascade-delete of Events ever fires when a Location is deleted** — the Events rows are simply orphaned.
- `afterInitialization("registerSystemFields")` (line 10) — populates `this.systemfields` (not a Wheels association/validation primitive; this is the app's own Custom-Fields-adjacent metadata system used to render admin-configurable forms).

### Field set (via `registerSystemFields()`, lines 16–80, cross-checked against `install/new-installation.sql` lines 95–104 `CREATE TABLE locations`)

| Field | DB column (schema) | Nullable (DB) | Type (system field) | Required (system field flag) | Description |
|---|---|---|---|---|---|
| `name` | `varchar(255) NOT NULL` | No | `textfield` | `1` (required) | "The Main room name, i.e Seminar Room 1" |
| `class` | `varchar(55) NOT NULL` | No | `textfield` | `1` (required) | "Classname used to assign a colour, should be unique to this location" (label: "CSS Class") — **note: this is a descriptive claim in a UI help-text string, not an enforced uniqueness rule** — see Open Questions. |
| `colour` | `varchar(7) DEFAULT NULL` | Yes | `colourpicker` | `1` (required per systemfields array, **but DB column is nullable** — a required-in-UI/optional-in-DB mismatch) | "The colour assigned to this location" (label: "HEX Colour") |
| `description` | `varchar(500) DEFAULT NULL` | Yes | `textfield` | `0` (optional) | "Might be a floor or other description" |
| `building` | `varchar(255) DEFAULT NULL` | Yes | `textfield` | `0` (optional) | "Parent Building (optional)" |
| `layouts` | `varchar(500) DEFAULT NULL` | Yes | `textfield` | `0` (optional) | "List of possible layouts (optional)", placeholder `"i.e boardroom,lecture"` — **confirms FRD's assumed `layout` field is actually named `layouts` (plural) and is a free-text comma-separated string list, not a structured/typed field** (see Open Questions). |

`id` (PK, auto-increment) exists per schema but is not in `systemfields` (expected — PKs aren't user-editable form fields).

### Validations

- **No explicit `validate()`/`validatesPresenceOf()`/`validatesUniquenessOf()`/etc. calls exist anywhere in `Location.cfc`** — the file contains only the constructor, the association, and `registerSystemFields()`.
- Wheels' **automatic validations** (`wheels/model/initialization.cfm` lines 127–164, active by default — confirmed `application.$wheels.automaticValidations = true` in `wheels/events/onapplicationstart.cfm` line 205, with no override found in `config/settings.cfm` or any environment settings file) generate, per non-nullable/non-defaulted DB column: a `validatesPresenceOf` (for `name` and `class`, the two `NOT NULL` columns with no DB default) and a `validatesLengthOf(maximum=<column size>)` for string-typed columns. **Automatic validations never include uniqueness** — confirmed by reading `wheels/model/initialization.cfm` in full: the auto-validation block only ever calls `validatesPresenceOf`, `validatesLengthOf`, `validatesNumericalityOf`, and `validatesFormatOf` (for datetime columns); there is no automatic uniqueness check in the Wheels framework at all, and the Location model adds none manually.
- **`name` uniqueness is NOT enforced** — neither in the model (no `validatesUniquenessOf` call) nor at the database level (`install/new-installation.sql` lines 95–104: no `UNIQUE` constraint/index on `locations.name` or any column — the only unique-ish structure in the whole file is the PK). The `class` field's help text ("should be unique to this location") is advisory UI copy only, not a validated rule.

### Soft-delete / hard-delete mechanism (model-level confirmation)

Wheels enables per-model soft-deletion **only if the model's underlying table has a column matching `application.wheels.softDeleteProperty`** (set to `"deletedAt"` globally in `wheels/events/onapplicationstart.cfm` line 197) — confirmed in `wheels/model/initialization.cfm` lines 188–197: `if (Len(application.wheels.softDeleteProperty) && StructKeyExists(variables.wheels.class.properties, application.wheels.softDeleteProperty)) { variables.wheels.class.softDeletion = true; } else { variables.wheels.class.softDeletion = false; }`. The `locations` table (schema lines 95–104) has **no `deletedat`/`deletedAt` column** — unlike `events` and `logfiles`, which both explicitly declare one (schema lines 86, 129). Therefore **`Location.delete()` always performs a true SQL `DELETE`, never a soft-delete flag-set**, regardless of the FRD's interim assumption.

---

## Resource model

**File:** `models/Resource.cfc` (12 lines — the entire file)

### Associations (init(), lines 7–10)

- `hasMany("eventresources")` (line 9) — the only content in the model besides the constructor. No `dependent` option — same reasoning as Location above: **no cascade-delete of `eventresources` rows fires when a Resource is deleted** (the join-table rows are orphaned, pointing at a now-nonexistent `resourceid`).

### Field set

**The model file itself declares zero fields, zero validations, and zero `systemfields` metadata** — unlike Location, Resource has no `registerSystemFields()` call and no `this.systemfields` array at all. The full field set is therefore sourced entirely from the DB schema (`install/new-installation.sql` lines 173–181, `CREATE TABLE resources`) and corroborated by the form fields actually rendered in `views/resources/_form.cfm` (see Resources controller → Views reviewed, above):

| Field | DB column | Nullable | Form field type | Notes |
|---|---|---|---|---|
| `name` | `varchar(255) NOT NULL` | No | `textField`, required | Only FRD-assumed field; confirmed present. |
| `type` | `varchar(255) DEFAULT NULL` | Yes | `select`, options = `application.rbs.setting.resourceTypes` (admin-configurable list) | FRD did not mention this — a grouping/category field, optional. |
| `description` | `varchar(500) DEFAULT NULL` | Yes | `textField` | FRD did not mention this — optional free text. |
| `isunique` | `tinyint(1) NOT NULL DEFAULT '0'` | No (has DB default, so no presence validation triggers) | `checkBox` | FRD did not mention this — a boolean flag controlling booking-conflict semantics: per the form's help text, a "unique" resource can't be double-booked for overlapping time, a "generic" resource has no such restriction. This directly affects the conflict-checking logic (`checkavailability` action above operates per-resource-id regardless of this flag, so the actual uniqueness-of-booking enforcement must live in the booking/event conflict-check code outside this plan's scope — flagged for cross-reference with the Bookings controller audit). |
| `restrictlocations` | `varchar(255) DEFAULT NULL` | Yes | multi-select of Locations | FRD did not mention this — stores a delimited list of Location ids/names this Resource may be booked at; optional (unrestricted if blank). |

Because the model declares no validations of its own and all DB columns beyond `name` are nullable/defaulted, Wheels' automatic validations generate only a `validatesPresenceOf` + `validatesLengthOf(maximum=255)` for `name`. **No uniqueness validation exists for Resource `name` either** (same reasoning as Location — automatic validations never include uniqueness, and no manual `validatesUniquenessOf` call exists in the 12-line file).

### Soft-delete / hard-delete mechanism

Identical mechanism to Location: `resources` table (schema lines 173–181) has **no `deletedat`/`deletedAt` column** → `variables.wheels.class.softDeletion = false` for this model → `Resource.delete()` always performs a true SQL `DELETE`.

---

## Open Questions (reference-data)

**1. Exact legacy `Resource` field set beyond `name` — RESOLVED.**
Confirmed via `models/Resource.cfc` (no model-level field declarations) cross-referenced against `install/new-installation.sql` (`CREATE TABLE resources`, lines 173–181) and `views/resources/_form.cfm` (lines 1–37, the actual rendered form fields). Full field set: `name` (required), `type` (optional grouping/category, admin-configurable option list), `description` (optional), `isunique` (boolean, default false, governs per-resource double-booking restriction semantics), `restrictlocations` (optional location-restriction list). See "Resource model" section above for the complete table.

**2. Exact shape/purpose of the legacy `layout` metadata field on Location — RESOLVED.**
Confirmed via `models/Location.cfc` `registerSystemFields()` (lines 69–78) cross-referenced against `install/new-installation.sql` (`locations.layouts varchar(500) DEFAULT NULL`, line 102) and `views/locations/_form.cfm` (line 30, `[field id="layouts"]`). The real field name is **`layouts`** (plural, not singular `layout` as FRD assumed), it is a free-text field (not an enum/structured type), optional, intended to hold a human-typed comma-separated list of layout style names (placeholder text: `"i.e boardroom,lecture"`; seed data confirms this exact shape — e.g. `install/new-installation.sql` line 109: `'Lecture,Debate'`). It is purely descriptive/informational text stored on the Location — there is no code anywhere in `Locations.cfc`/`Location.cfc` that parses, validates, or structurally interprets this string; it exists only to be displayed and to inform a human filling in the Event's separate `layoutstyle` field (confirmed in `models/Event.cfc` lines 137–146 — `Event.layoutstyle` is an entirely independent field driven by a **global** `application.rbs.setting.roomlayouttypes` options list, not by the specific Location's `layouts` text). These two "layout" concepts are not programmatically linked.

**3. Whether `name` uniqueness is enforced on Location — RESOLVED.**
Confirmed **NOT enforced**, neither at the model level (no `validatesUniquenessOf` call in `models/Location.cfc`, and Wheels' automatic-validation generator — `wheels/model/initialization.cfm` lines 127–164 — never synthesizes a uniqueness check, only presence/length/numericality/date-format) nor at the database level (`install/new-installation.sql` lines 95–104: no `UNIQUE` constraint or index on `locations.name`, the only key defined is the auto-increment `id` PRIMARY KEY). The same conclusion applies identically to `Resource.name` (`models/Resource.cfc` has zero validations of its own, and `resources` table schema likewise has no unique constraint beyond its PK). The FRD's "interim: not enforced beyond non-empty" assumption is **confirmed correct** for both entities — "non-empty" being the automatic `validatesPresenceOf` on the `NOT NULL` `name` column.

**4. Whether deleting a Location/Resource referenced by existing bookings is blocked, cascades, or allows orphaned references — RESOLVED, and contradicts the FRD's interim assumption.**
FRD's interim assumption was: "soft-delete only, `deleted_at` set, existing Booking references remain intact." **This is incorrect for both entities.** Confirmed via three independent, converging pieces of code evidence:
   - (a) **Controller level:** `Locations.cfc.delete()` (lines 94–109) contains only a global "at least one Location must remain" count check — no query against `events` for existing bookings referencing the Location being deleted. `Resources.cfc.delete()` (lines 77–85) has **no guard at all**.
   - (b) **Model/association level:** Neither `Location.hasMany("events")` (line 9) nor `Resource.hasMany("eventresources")` (line 9) specifies a `dependent` option, so Wheels' `$deleteDependents()` (`wheels/model/associations.cfm` lines 95–131) never cascades — the related `events`/`eventresources` rows are left in place, now pointing at a deleted parent id (an **orphan-allow**, not a cascade).
   - (c) **Framework/schema level — the deciding factor on soft- vs hard-delete:** Wheels only soft-deletes a model whose table has a `deletedAt`-equivalent column (`wheels/model/initialization.cfm` lines 188–197, keyed off `application.wheels.softDeleteProperty = "deletedAt"` set in `wheels/events/onapplicationstart.cfm` line 197). `install/new-installation.sql` shows `events` (line 86) and `logfiles` (line 129) **do** have a `deletedat` column, but `locations` (lines 95–104) and `resources` (lines 173–181) **do not**. Therefore Location and Resource deletes are **true hard SQL `DELETE`s**, not soft-deletes — the opposite of the FRD's interim assumption.

   **Net verdict: ORPHAN-ALLOW VIA HARD DELETE.** Deleting a Location or Resource that has existing Bookings/Events referencing it succeeds unconditionally (modulo Locations' unrelated "last-one-standing" count check) and permanently removes the row. The referencing `events.locationid` / `eventresources.resourceid` values become dangling references to a no-longer-existent row — there is no database-level `FOREIGN KEY` constraint anywhere in the schema (confirmed: zero `FOREIGN KEY`/`REFERENCES`/`CONSTRAINT` clauses in all 276 lines of `install/new-installation.sql`) to even prevent this at the DB layer. This is a materially different (and riskier) legacy behavior than FRD assumed, and the new system's F4 design must explicitly decide whether to preserve this orphan-allow behavior (equivalence with legacy) or introduce a blocking/cascade guard as a deliberate improvement — this decision should be escalated to the F4 planning phase as a product/architecture choice, not silently resolved either way.

**5. Why `checkavailability` (Resources' Ajax conflict-check endpoint) is gated behind the admin-only `accessresources` permission — OPEN, logged as a design-smell rather than a functional ambiguity.**
`controllers/Resources.cfc` applies `checkPermissionAndRedirect(permission="accessresources")` (admin/editor-excluded, admin-only per seed data) to **every** action including `checkavailability`, with no `except=` carve-out (contrast with Locations' explicit `except="list,view"` pattern). If this Ajax endpoint is meant to support the general booking UI's live availability check for any logged-in user creating a booking, this permission gate would make it unreachable for non-admin users, which would be a functional bug in the legacy system itself (not an audit-shape ambiguity) — or, alternatively, resource-level availability-checking in the booking UI is itself an admin-only/unused feature in practice. **Unknown because:** this plan's scope is the Locations/Resources controllers and models in isolation; confirming whether the Bookings/Event-creation UI actually calls this endpoint for non-admin users (and if so, whether it fails silently, is blocked by this filter in production, or routes through a different unguarded endpoint) requires cross-referencing the Bookings controller and its JS/view layer, which is out of scope for this plan. Flagged for the Bookings controller audit plan to confirm or refute.
