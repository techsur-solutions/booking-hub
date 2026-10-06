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
