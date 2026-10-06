# Legacy Audit Findings — RoomBooking (F0)

**Source repository:** https://github.com/neokoenig/RoomBooking (branch `master`).
**Audit date:** 2026-10-06.

This document consolidates the five Wave 1 findings files produced during Phase 1
(legacy-functional-audit) into the single canonical F0 deliverable named by
FRD-BookingHub.md's F0 "Outputs" section: one dedicated, cited section per legacy
controller (12 total) and per legacy model group (7 total), plus the
cross-cutting Route Inventory, CF Application Lifecycle Events, and
Per-Environment Settings Behavior sections. No findings below are re-derived —
every citation is preserved verbatim from its source file:

- [findings/01-booking-core.md](./findings/01-booking-core.md) — Bookings, Eventdata controllers; Event, Eventresource models
- [findings/02-reference-data.md](./findings/02-reference-data.md) — Locations, Resources controllers; Location, Resource models
- [findings/03-custom-fields.md](./findings/03-custom-fields.md) — Customfields controller; Customfield, Customfieldjoin, Customfieldvalue models
- [findings/04-identity-access.md](./findings/04-identity-access.md) — Users, Sessions, PasswordResets, Permissions controllers; User model; cross-controller Permission Flag Inventory
- [findings/05-platform-settings.md](./findings/05-platform-settings.md) — Settings, Api, Logfiles controllers; Settings model; Route Inventory; CF Application Lifecycle Events; Per-Environment Settings Behavior

Each source findings file also carries its own "Open Questions" subsection; those
are consolidated separately into [open-questions.md](./open-questions.md), the
canonical Open Questions list for this audit. A one-row-per-finding seed for the
F13 traceability matrix is consolidated into
[baseline-inventory.md](./baseline-inventory.md).

---


---

## Api

**Controller:** `controllers/Api.cfc`
(https://raw.githubusercontent.com/neokoenig/RoomBooking/master/controllers/Api.cfc)

**Confirmed feed actions and formats — 4 total, matching the PRD's assumed list (RSS2/iCal/JSON/display board), with one important correction: there is no distinct "JSON" action.** The controller declares `provides("json,xml,html")` as supported response *formats* framework-wide, but only **3 explicit public actions** exist: `display` (HTML display board), `rss2` (XML), `ical` (plain-text .ics). No controller action named `json` or returning a dedicated JSON payload was found — "JSON" in the PRD's assumed format list appears to be either aspirational/unimplemented, or refers to the generic Wheels `provides("json")` content-negotiation fallback applying to `rss2`/`display` if a client requests `Accept: application/json` (untested from code alone; no JSON-specific view template exists in `views/api/`). **This corrects the PRD's assumed 4-distinct-format list to 3 implemented actions + 1 content-negotiation possibility.**

**Explicit per-feed-action permission/auth verdict — this directly confirms, then corrects, PRD Open Question #7 / the PROJECT.md "fully public, no allowAPI gate" interim decision:**

```cfm
public void function init() {
    // Permissions (no super.init())
    filters(through="f_isValidAPIRequest", except="index");
    filters(through="checkPermissionAndRedirect", permission="allowAPI", only="index");
    ...
}

private void function f_isValidAPIRequest() {
    var r=false;
    if(structKeyExists(params, "token") AND len(params.token) GT 25){
        if(model("user").exists(where="apitoken='#params.token#'")){
            r=true;
        }
    }
    if(!r){
        redirectTo(route="denied", error="No API Authentication Token Present");
    }
}
```

| Action | Gate applied | Verdict |
|---|---|---|
| `index` (the feed-URL-listing page, human-browsed) | `checkPermissionAndRedirect(permission="allowAPI")` | **Permission-gated.** Per the seeded permission matrix, `allowAPI` is granted to ALL roles including `guest` (`'1','1','1','1'` — "Reserved for future use"), so in the shipped default data this gate is effectively a no-op; but the mechanism itself IS a real permission check, contrary to a "no gate at all" reading. |
| `rss2`, `ical`, `display` (the actual feed-serving/data-returning actions) | `f_isValidAPIRequest` (via `except="index"`, i.e. applied to every OTHER action) | **NOT gated by `allowAPI` or any permission check at all.** Instead gated by a **per-user API token** (`params.token`, must be >25 chars and match a `users.apitoken` value via `model("user").exists(...)`). This is authentication (proves who you are / that you possess a valid token), not authorization-by-role. Critically: **the token check does not re-verify the token-holder's `allowAPI` permission at request time** — merely having ANY valid token (regardless of the token owner's current role/permissions) is sufficient. |

**Verdict on PRD Open Question #7:** The PROJECT.md interim decision "fully public, no allowAPI gate" is **PARTIALLY CORRECT, PARTIALLY WRONG**. It is correct that the three data-serving feed actions (`rss2`/`ical`/`display`) are NOT gated by the `allowAPI` permission flag. It is **incorrect** that they are "fully public" with **no gate whatsoever** — they require a valid per-user API token passed as a URL parameter (`?token=...`), confirmed in `views/api/index.cfm` where every feed link is built with `token=#session.currentuser.apitoken#` appended. A request with no token, or an invalid/revoked token, is redirected to `/denied`. So: **not role-gated, but not anonymous-public either — token-gated.** Any user with ANY generated API token (any role) can reach any feed/location-filtered data; the gate is "possession of a valid token," not "is in a permitted role." This is a meaningfully different security model than either of the PRD's two assumed extremes and should be corrected in Phase 7 planning: feeds require token provisioning (an admin/privileged action, per `views/api/index.cfm`'s own warning text: *"Administrators, or those with user creation privileges can create these for you on request"*), but do NOT check `allowAPI` on the data-serving endpoints themselves.

**Per-location filtering parameter handling** — confirmed uniform across all 3 actions: each accepts an optional `params.location` (numeric location ID); if present AND numeric, the underlying `model("location").findAll(where="...AND id = #params.location#")` query is scoped to that location only; if absent, the query returns events across ALL locations. `display` additionally accepts `params.today` (boolean int, 0/1) to switch from "upcoming" to "today only" date-range filtering, and `params.maxrows` (default 5 for `display`, 25 for `rss2`/`ical`). All three actions share the identical `status = 'approved'` filter — **unapproved/pending/denied bookings are never exposed via any feed**, regardless of token validity. This is an important confirmed data-exposure boundary for Phase 7.

### Views reviewed (Api)
- `views/api/index.cfm` — the authenticated feed-URL directory; explicitly checks `!structkeyexists(session, "currentuser") OR !len(session.currentuser.apitoken)` and shows an error/no-links UI if the logged-in user lacks their own token — **confirms no server-side auth check beyond what's in the controller is embedded in this view; the view's own gate is UX-only (hiding links from users without tokens), not a second enforcement layer**, since the underlying feed URLs work for ANY valid token regardless of which account is currently browsing `index`.
- `views/api/display.cfm` — renders the display-board table; reads `isToday`/`isSingleLocation` booleans passed from the controller; no auth logic present (confirms the view layer adds no additional gate on top of `f_isValidAPIRequest`).
- `views/api/displayboard.cfm` — the layout wrapper used only for `display` (`usesLayout(template="displayboard", only="display")`), includes the shared `/common/layout/head` and `/common/layout/footer` partials — no auth logic.
- `views/api/ical.cfm` — raw `.ics` content-type response, single line (`<cfcontent type="text/calendar">...<cfoutput>#data#</cfoutput><cfabort>`) — no auth logic; data is pre-filtered by the controller before reaching this view.
- `views/api/rss2.xml.cfm` — raw RSS/XML content-type response; no auth logic; confirms every item's `<link>`/`<guid>` points back at `controller=bookings, action=view` — i.e. the RSS feed links out to the main (permission-gated) booking-detail view, not a public detail page, meaning an anonymous recipient of a shared RSS link who clicks through would hit the normal `accessCalendar`/`viewRoomBooking` permission gate on `Bookings.cfc::view()`.


---

## Bookings

**Permission filter stack** (controllers/Bookings.cfc: init):
- `checkPermissionAndRedirect(permission="accesscalendar")` — applies to **every** action in this
  controller, no exceptions. This is the baseline "can see the calendar at all" gate.
- `checkPermissionAndRedirect(permission="allowRoomBooking", except="index,list,day,building,location,check")`
  — applies to every action **except** `index, list, day, building, location, check`. In practice
  this means: `view, add, approve, deny, clone, edit, create, update, delete` all require
  `allowRoomBooking`; the read-oriented/calendar-browsing actions do not.
- `checkPermissionAndRedirect(permission="viewRoomBooking", only="list,view")` — `list` and `view`
  additionally require `viewRoomBooking`. Net effect: `list`/`view` need `accesscalendar` +
  `viewRoomBooking` but **not** `allowRoomBooking`; `add/edit/create/update/delete/clone/approve/deny`
  need `accesscalendar` + `allowRoomBooking`; `index/day/building/location` need only
  `accesscalendar`.
- `checkPermissionAndRedirect(permission="allowApproveBooking", only="approve,deny")` — `approve`
  and `deny` additionally require `allowApproveBooking`.
- `verifies(only="approve,deny,view,clone,edit,delete", params="key", paramsTypes="integer", route="home", error="Sorry, that event can't be found")`
  — these five actions require an integer `params.key`; a missing/non-integer key redirects to the
  `home` route with the quoted flash error, before the action body ever runs.
- `filters(through="_getLocations", only="index,building,location,add,edit,clone,create,update,list,day")`
  and `filters(through="_getResources", only="index,building,location,add,edit,clone,create,update,list,view")`
  preload `locations`/`resources` struct-scoped vars consumed by the views.
- `filters(through="_setModelType")` (all actions) sets `request.modeltype="event"`, used by the
  Custom Fields subsystem (`getCustomFields`/`updateCustomFields` in Controller.cfc).
- `usesLayout(template=false, only="check")` — the `check` action renders with **no page layout**;
  it is a pure AJAX HTML-fragment endpoint (confirms it is not a full page/view).

**Actions** (controllers/Bookings.cfc):

- **`index`** — no explicit function body exists for this action in `Bookings.cfc`; CFWheels
  auto-renders `views/bookings/index.cfm` with whatever the filters populated (`locations`,
  `resources`). Reachable via the app's default/home route (`config/routes.cfm: addRoute(name="home", pattern="", controller="bookings", action="index")`). Gate: `accesscalendar` only.
- **`building()`** (controllers/Bookings.cfc: building) — `renderPage(action="index")`, i.e. reuses
  the index view/calendar but scoped by a building key (passed via `params.key`, a building name
  via `toTagSafe()` — see `views/bookings/_locations.cfm`). Gate: `accesscalendar` only.
- **`location()`** (controllers/Bookings.cfc: location) — same pattern as `building()`, scoped to a
  single `locationid`. Gate: `accesscalendar` only.
- **`list()`** (controllers/Bookings.cfc: list) — agenda-style table view. Defaults
  `params.datefrom`/`params.dateto` to "now" / "+1 month", reads `params.location`/`params.q`,
  builds a where-clause via the private helper `_agendaListWC()` (date range, `status=`, a
  `FIND_IN_SET(locationid, params.location)` multi-select location filter, and a `title LIKE … OR
  description LIKE …` keyword filter over a stripped/sanitized `params.q`). Gate: `accesscalendar`
  + `viewRoomBooking` (not `allowRoomBooking` — confirms list is viewable read-only by users who
  can only view, not book). **Views reviewed:**
  `views/bookings/list.cfm` renders the agenda table and independently re-checks
  `checkPermission("allowRoomBooking")` around the per-row View/Edit action-button group — so a
  `viewRoomBooking`-only user sees the list rows but **no** action buttons on them (a view-layer
  permission check layered on top of, and distinct from, the controller-level filter).
  `views/bookings/list/_filter.cfm` is the GET-submitted filter form (date range, multi-select
  locations, status dropdown) that populates the `params.*` the controller's `_agendaListWC()`
  reads.
- **`day()`** (controllers/Bookings.cfc: day) — explicitly `@hint`-marked "deprecated in 1.2".
  Builds an MRBS-style grid using a private `_dayListWC()` where-clause helper and an in-memory
  Query-of-Queries cursor to assign table rowspans per timeslot. Gate: `accesscalendar` only. No
  further functional detail is load-bearing for current-system parity given the deprecation marker.
  **Views reviewed:** `views/bookings/day.cfm` and `views/bookings/day/_header.cfm` (prev/next-day
  navigation + a date-picker jump field) confirm the deprecated grid-view UI; no conflict-detection
  or approval logic appears in either.
- **`add()`** (controllers/Bookings.cfc: add) — builds a new, unsaved `event` (with a nested new
  `eventresource`) for the create form. Pre-fills `event.start` from a `params.d` (`YYYY-M-D`) query
  param if present (used by calendar "click an empty slot" UX), and pre-fills `event.locationid`
  from `params.key` if numeric. Gate: `accesscalendar` + `allowRoomBooking`. **Views reviewed:**
  `views/bookings/add.cfm` wraps the shared `_form.cfm` partial; its submit-button label varies in
  three ways driven by `application.rbs.setting.approveBooking AND checkPermission("bypassApproveBooking")`
  ("Create And Auto-approve Booking"), `application.rbs.setting.approveBooking` alone ("Request
  Booking"), or neither ("Create Booking") — this view-layer conditional independently corroborates
  the bypass-approval controller logic documented under `create()` below.
- **`approve()`** (controllers/Bookings.cfc: approve) — loads the event by `params.key`, sets
  `event.status="approved"`, saves, calls `notifyContact(event)` unconditionally (no `emailContact`
  flag check — see Open Question #5), redirects back with a success flash message. **No conflict
  check of any kind runs here** — approving a pending booking never re-validates it against other
  bookings. Gate: `accesscalendar` + `allowRoomBooking` + `allowApproveBooking`.
- **`deny()`** (controllers/Bookings.cfc: deny) — sets `event.status="denied"`, saves, calls
  `notifyContact(event)` unconditionally, and additionally **deletes** the event if
  `params.delete` is truthy ("Deny & Delete"), with a distinct flash message for each branch. Gate:
  same as `approve`.
- **`clone()`** (controllers/Bookings.cfc: clone) — loads the existing event (with `eventresources`
  include) **by the original event's id**, loads its custom fields, then
  `renderPage(action="add")` — i.e. clone does not create a new row itself; it renders the `add`
  form pre-populated with the original event's field values. Because `views/bookings/_form.cfm`
  emits `hiddenFieldTag(name="tempkey", value=event.key())`, and `event` here is still the
  **original** (to-be-cloned) event object, the clone form's hidden `tempkey` — which the
  client-side `concurrencyCheck()` JS sends as the `id` param to exclude "myself" from the clash
  check — carries the **original event's id**, not "no id" (0/new). Net effect: cloning an event and
  picking an overlapping time will **not** flag a clash against the very event being cloned (the
  check query excludes `id != params.id`), even though the clone is actually creating a brand-new,
  independent row via `create()`. This is a directly-observable quirk of the cited code, not a
  guess. Gate: `accesscalendar` + `allowRoomBooking`.
- **`edit()`** (controllers/Bookings.cfc: edit) — loads event + `eventresources` by `params.key`,
  loads custom fields keyed by `params.key`. Gate: `accesscalendar` + `allowRoomBooking`. **Views
  reviewed:** `views/bookings/edit.cfm` wraps `_form.cfm` via `startFormTag(action="update",
  key=event.id)`; unlike `add.cfm` it shows **no** bypass-approval/status-related submit-button
  variation — consistent with `update()` never touching approval status (see below).
- **`create()`** (controllers/Bookings.cfc: create) — the POST target for `add`. Builds
  `event=model("event").new(params.event)` and saves it. This is the single most
  business-rule-dense action in the controller; findings below are grouped by concern:
  - **Approval-status assignment (two-stage):** stage 1 is the `Event` model's own
    `beforeCreate("checkApproval")` callback (see Event model section) which sets
    `status="pending"` if `application.rbs.setting.approveBooking` is on, else `"approved"`. Stage 2
    is an **additional, controller-level override** that runs only after `event.save()` succeeds:
    `if(application.rbs.setting.approveBooking AND checkPermission("bypassApproveBooking")){ event.status="approved"; event.save(); }`
    — i.e. a **second, distinct permission flag** (`bypassApproveBooking`, not the `allowApproveBooking`
    that gates the `approve`/`deny` actions) can force-flip a would-be-pending booking straight to
    `approved` at creation time. This is the actual mechanic behind PRD's "auto-approve" concept and
    is the key citation for Open Question #4.
  - **Bulk/recurring creation:** if `params.repeat != "none"` and `params.repeatno` is numeric and
    `>= 1`, the action loops `i=1..params.repeatno`, creating a **new, fully independent**
    `model("event").new(params.event)` each iteration, with `start`/`end` shifted by `i*7` days
    (`repeat="week"`) or `i` months (`repeat="month"`) via `dateAdd()`. Each child event is saved
    directly (`nevent.save()`) — **the bypass-approval override above is never re-applied to these
    child events** (it only runs once, on the original `event` object, before the loop). So: with
    approval-on and a bypass-capable user, the **first** occurrence of a recurring series is
    auto-approved, but every subsequent occurrence created by the loop is left `"pending"` by the
    model callback alone. This is directly readable from the code, not inferred — see Open Question
    #1. The code comment also explicitly notes: "repeated events can't/don't save customfield
    metadata" (child events never receive the parent's custom-field values).
  - **No stored series/recurrence identifier:** neither the loop nor `Event.cfc`'s field metadata
    (see Event model section) creates or stores any linking id between the parent and its repeat
    children — they are indistinguishable sibling rows once created.
  - **Conflict enforcement: NONE at save time.** There is no call to any conflict-check function
    inside `create()` — the only conflict-detection code path in this entire controller is the
    separate, non-blocking AJAX `check()` action (below). A booking **will** save successfully even
    if it directly clashes with another approved booking in the same location. This is the
    controlling citation for Open Question #2.
  - **Email trigger:** `if(structKeyExists(params.event, "emailContact") AND params.event.emailContact){ notifyContact(event); }`
    — fires only for the **parent** event, gated by a user-controlled checkbox
    (`views/bookings/tabs/_contact.cfm`, shown only on `add`/`create`), never for loop-created
    repeat children.
  - On failure: `renderPage(action="add", error="There were problems creating that event")`.
  Gate: `accesscalendar` + `allowRoomBooking`.
- **`notifyContact(required struct event)`** (controllers/Bookings.cfc: notifyContact) — not a
  routable action (it requires a struct argument), effectively an internal helper despite being
  `public`. Sends email only if `isValid("email", event.contactemail)` **and**
  `!application.rbs.setting.isDemoMode` (silent no-op otherwise). Uses template
  `/email/bookingNotify`, subject interpolates `#event.status#`, optional BCC to
  `application.rbs.setting.bccAllEmailTo` if `application.rbs.setting.bccAllEmail` is set. Call
  sites: `approve()`, `deny()` (both unconditional), `create()` (conditional on the `emailContact`
  checkbox). This is the complete set of email-trigger call sites relevant to Open Question #5 (email
  **content** itself is explicitly out of this plan's scope, per the plan's framing — F8's job).
- **`update()`** (controllers/Bookings.cfc: update) — loads the existing event + `eventresources`
  by `params.key`, calls `event.update(params.event)` then `event.save()` (re-running the
  `checkDates` validation, but **not** `checkApproval`, since that callback is registered via
  `beforeCreate` only — see Event model). **Approval status is therefore never reset or re-derived
  on edit** unless `params.event.status` is explicitly included by the caller. Updates custom fields
  via `updateCustomFields` if `params.customfields` present. No repeat/series-aware branching exists
  — editing always targets the single row identified by `params.key`. This is the direct citation
  for Open Question #1's "edit scope" half: there is no "edit this occurrence vs. edit the whole
  series" choice anywhere in the code because no series concept exists past creation time. Gate:
  `accesscalendar` + `allowRoomBooking`.
- **`delete()`** (controllers/Bookings.cfc: delete) — deletes the single event row identified by
  `params.key` (cascading to its `eventresources` rows via `nestedproperties(..., allowDelete=true)`
  on the `Event` model). No "delete series" action/option exists anywhere in the controller — same
  conclusion as `update()` for Open Question #1. Gate: `accesscalendar` + `allowRoomBooking`.
- **`check()`** (controllers/Bookings.cfc: check) — the sole conflict-detection endpoint, rendered
  with `usesLayout(template=false)` as a pure AJAX HTML fragment. Requires `params.start`,
  `params.end` (received but **not actually used** in the SQL — see below), `params.location`,
  `params.id`. Query (editing case): `status != 'denied' AND id != #params.id# AND start <= '#params.start#' AND end >= '#params.start#' AND locationid = #params.location#`;
  (new-booking case, when `len(params.id)` is 0): same minus the `id !=` clause. Two findings, both
  directly readable from the cited code, not guesses:
  1. **The check is permission-independent and non-blocking for every user.** There is no branch on
     `allowApproveBooking` (or any other permission) anywhere in this query or in the surrounding
     action — it behaves identically regardless of who is booking. This directly contradicts the
     premise of PRD Open Question #2's interim decision (see Open Questions section below).
  2. **The overlap test is incomplete/asymmetric.** Despite receiving `params.end`, the SQL clause
     never references it — it only tests whether an **existing** event's `start`/`end` span
     contains the **new** booking's `start` instant (`existing.start <= new.start AND existing.end
     >= new.start`). A genuine overlap where the **new** booking starts **before** an existing one
     but the two still overlap later (e.g. existing 14:00–15:00, new 13:30–14:30) is **not**
     detected, because `existing.start (14:00) <= new.start (13:30)` is false. This is a confirmed,
     citable gap in the legacy overlap logic, not an open question — flagged here for F2's benefit
     (the new system's conflict detection should implement a complete overlap test, which would not
     be "parity" with this specific query but *is* consistent with the evident intent of the
     feature). `status != 'denied'` confirms denied bookings are excluded from conflict
     consideration (pending **and** approved bookings both count). Gate: `accesscalendar` only
     (excepted from `allowRoomBooking`).
  **Views reviewed:** `views/bookings/check.cfm` renders a Bootstrap `alert-danger` block listing
  each clashing event's title and formatted date range when `eCheck.recordcount` is nonzero — and
  nothing else. There is no disabled-submit-button, no required-acknowledgment checkbox, no
  JS-side `preventDefault()` — confirmed by reading `views/bookings/_form.cfm`'s
  `concurrencyCheck()` function, which only ever does an async `$.ajax` GET and swaps the result
  into a `<div>`; the form's submit button is never touched by this code path. This is the
  conclusive view-layer evidence that the conflict check is advisory-only.
  **Additional views reviewed (shared `_form.cfm` partial, used by both `add` and `edit`):**
  - `views/bookings/_form.cfm` also conditionally includes `tabs/resources` (only if
    `application.rbs.setting.allowResources`) and `tabs/repeat` (**only** when
    `params.action EQ "add" OR params.action EQ "create"` — confirming bulk/recurring creation is an
    **add-time-only** feature, unavailable from the edit form, independently corroborating the
    `update()` controller finding above). It also wires a **separate**, resource-level AJAX
    uniqueness check (`controller="resources", action="checkavailability"`) for resources flagged
    `data-unique` — this is a distinct mechanism from the event/location-level `check()` endpoint
    documented above; `Resources.cfc`/the `Resource` model were not in this plan's declared file
    list, so the actual enforcement logic behind that endpoint is **not** confirmed here (see Open
    Question #3).
  - `views/bookings/tabs/_repeat.cfm` — the bulk-create UI: radio buttons for
    none/week/month and a `repeatno` dropdown hard-coded to the options "1" through "15". The
    controller's `create()` loop only validates `isnumeric(params.repeatno) AND params.repeatno GTE
    1` — there is **no server-side upper bound** matching the UI's 15-item dropdown; a direct POST
    with a larger `repeatno` would not be rejected by anything in `Bookings.cfc`. Confirmed gap, not
    a guess.
  - `views/bookings/tabs/_contact.cfm` — contact fields plus the `emailContact` checkbox, shown
    only on `add`/`create` (matching the `create()` email-trigger gate above); when
    `application.rbs.setting.isDemoMode` is on, a "No emails will be sent in demo mode" help text is
    shown, directly corroborating `notifyContact()`'s demo-mode guard.
  - `views/bookings/tabs/_resources.cfm` — renders a `hasManyCheckbox` bound to
    `event.eventresources`, one row per bookable resource, with optional `data-restrict` (a
    location-restriction list) and `data-unique` (one-at-a-time) attributes sourced from fields on
    the `resources` query (`restrictlocations`, `isunique`) that belong to the out-of-scope
    `Resource` model — cited here only because they appear directly in this reviewed view file, and
    flagged as corroborating context for Open Question #3.
  - `views/bookings/view.cfm` — a thin wrapper that includes `/eventdata/details` — confirming
    `Bookings.view()` and `Eventdata.getevent()` (below) render through the **same** shared detail
    partial.
  - `views/bookings/index.cfm` — the FullCalendar container; wires `data-eventsurl` to the named
    route `getEvents` (→ `Eventdata.getevents`), `data-eventurl` to `Eventdata.getevent` (used for
    the remote-modal popup), and `data-addurl` to `Bookings.add`. This is the authoritative
    cross-reference confirming the controller/route wiring declared in `config/routes.cfm`:
    `addRoute(name="getEvents", pattern="/eventdata/getevents/[type]/[key]", action="getevents", controller="eventdata")`
    and `addRoute(name="getEvent", pattern="/eventdata/getevent/[key]", action="getevent", controller="eventdata")`.
  - `views/bookings/_locations.cfm` — the building/location quick-filter bar on `index`; purely a
    navigation aid, no business logic.
  - `views/bookings/_eventmodal.cfm` — an empty Bootstrap modal shell (`<div id="eventmodal">`) that
    JS injects AJAX content into (the `getevent` remote-modal target).


---

## Customfields

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


---

## Eventdata

**Permission/filter stack** (controllers/Eventdata.cfc: init): `checkPermissionAndRedirect(permission="accesscalendar")`
only (no `allowRoomBooking`/`viewRoomBooking` filter at the controller level — see the permission
gap noted under `getevent()` below). `filters(through="_getResources", only="getevent")`.
`verifies(only="getevent", params="key", paramsTypes="integer", route="home", error="Sorry, that event can't be found")`.
`provides("html,json")` (format negotiation). `usesLayout(template="modal", only="getevent")` — the
`getevent` action renders wrapped in the custom `modal.cfm` layout. `filters(through="_setModelType")`
(all actions).

**Actions** (controllers/Eventdata.cfc):

- **`getevents()`** (controllers/Eventdata.cfc: getevents) — the calendar's primary AJAX data feed,
  reachable at the named route `getEvents` (`config/routes.cfm`: `/eventdata/getevents/[type]/[key]`).
  Requires `params.start`/`params.end`; branches on `params.type` (`"building"`, `"location"`, or
  default/all) to build a `model("event").findAll(select="id, title, locationid, class, start, end, allday, status", where="start >= :sd AND end <= :ed [AND locations.building = :key | AND locationid = :key]", include="location", order="start ASC")`
  query, then reshapes the result through the private `prepeventdata()` helper into a
  FullCalendar-compatible array (`id, title, start, end, allDay, className` where `className`
  concatenates the location's CSS class and the event's status) via the private `_f_d()` date
  formatter, and renders it with `renderWith(events)` (JSON given the `provides("html,json")`
  declaration and the `format=json` param seen in `index.cfm`'s `data-eventsurl`). **Permission
  note:** this action carries **no** `allowRoomBooking`/`viewRoomBooking` gate beyond the baseline
  `accesscalendar` — any user who can see the calendar at all can read event data (title, times,
  status, location) for every location via this feed, not just locations/bookings they have
  `viewRoomBooking`/`allowRoomBooking` for. This is a direct citation of the permission filter list,
  not a guess; documented here for completeness per the plan's requirement to note "absence of any
  gate" explicitly.
- **`getevent()`** (controllers/Eventdata.cfc: getevent) — the AJAX single-event detail action used
  by modal popups, reachable at named route `getEvent` (`/eventdata/getevent/[key]`). Body:
  `event=model("location").findAll(where="events.id = #params.key#", include="events(eventresources)")`
  — functionally identical query shape to `Bookings.view()`. Response is rendered through the
  `modal.cfm` layout (Bootstrap modal header/body/footer chrome wrapping `includeContent()`) and the
  `getevent.cfm` view, which simply `includePartial("details")` — i.e. **the actual response body is
  the shared `/eventdata/details` partial**, observed to be consumed as an **HTML** fragment in
  practice (the calendar's `data-eventurl` wiring in `views/bookings/index.cfm` carries no
  `format=json` param, unlike `getevents`'s `data-eventsurl`), despite `provides("html,json")` being
  declared at the controller level. Gate: `accesscalendar` only at the controller level — see the
  next bullet for where real restriction actually happens.
  **Views reviewed:**
  - `views/eventdata/getevent.cfm` — thin wrapper, `includePartial("details")`.
  - `views/eventdata/modal.cfm` — the custom layout template (`usesLayout(template="modal")`)
    providing Bootstrap modal chrome around `includeContent()`.
  - `views/eventdata/_details.cfm` — the **shared** detail-rendering partial (used by both
    `Bookings.view()` and `Eventdata.getevent()`). This is where the actual content-level permission
    gating lives, since the controller itself does not gate `getevent` beyond `accesscalendar`:
    - The entire approval-status banner block (denied/pending alerts) and the `approve`/`deny`
      action buttons are wrapped in `<cfif application.rbs.setting.approveBooking>`, with the
      `approve`/`deny`/"`deny & delete`" buttons additionally gated by
      `checkPermission("allowApproveBooking")` — this is the actual UI entry point for the
      `approve()`/`deny()` controller actions documented above.
    - The edit/clone/delete button group is gated by `checkPermission("allowRoomBooking")`.
    - The main detail-content block (`[output id="..."]` shortcode template, or the custom
      `application.rbs.templates.event.output` override) is gated by
      `checkPermission("viewRoomBooking")` — with a literal `<cfelse>You're not allowed to view the
      booking details</cfelse>` fallback. **This is the only place `viewRoomBooking` is actually
      enforced for the `getevent` AJAX path** — confirmed from the code, not inferred, since the
      controller-level filter list for `Eventdata.cfc` plainly lacks a `viewRoomBooking` filter.
    - A resources-requested block is shown if `application.rbs.setting.allowResources AND
      len(event.resourceid)`, looping the event's resources against the preloaded `resources` query.


---

## Locations

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

## Logfiles

**Controller:** `controllers/Logfiles.cfc`
(https://raw.githubusercontent.com/neokoenig/RoomBooking/master/controllers/Logfiles.cfc)

- Gate: `super.init()` (global `accessApplication` + `logFlash` after-filter) plus `filters(through="checkPermissionAndRedirect", permission="accesslogfiles")` — per the seeded permission matrix, `accessLogfiles` is granted to `admin` only (`'1','0','0','0'`); editor/user/guest are all denied. This is the most restrictive gate found across the three controllers in this plan.
- Single action: `index()` — a log **viewer only**; filterable by `params.type` (one of 5 fixed type strings, see below) and `params.userid`, with a `params.rows` cap (default 250). Uses `includeSoftDeletes=true` on the query, meaning soft-deleted log rows ARE still shown in this viewer (logs are never truly hidden from the admin view once soft-deleted).
- `_getLogFileTypes()` returns a **hard-coded, fixed list**: `"login,success,error,ajax,cookie"` — these are the only 5 type values the filter dropdown offers, though (see below) the actual `type=` values written by calling code don't perfectly match this list verbatim (e.g. `Login` with capital L, `Cookie` with capital C are what's actually written — ColdFusion's case-insensitive string comparisons mean the filter still works, but the canonical list in code and the actual written values differ in casing only).

**Explicit before/after-value-capture verdict — directly confirms/corrects PRD Open Question #8:**

Schema (`install/new-installation.sql`):
```sql
CREATE TABLE `logfiles` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `message` varchar(500) NOT NULL,
  `data` text,
  `ipaddress` varchar(16) NOT NULL,
  `type` varchar(16) NOT NULL,
  `userid` int(11) NOT NULL DEFAULT '0',
  `createdat`/`updatedat`/`deletedat` datetime,
  PRIMARY KEY (`id`)
)
```

**Verdict: NO before/after value pairs are captured anywhere in this system.** The schema has exactly one `message` field (a free-text string, max 500 chars) and one `data` field (free-text `TEXT`, unbounded) — there is no `old_value`/`new_value`, no `field_changed`, no structured diff of any kind. The log is a flat, human-readable message trail, not a structured audit/change-history table. Confirmed from the write-side helper in `events/functions.cfm`:

```cfm
public void function addlogline() {
    if(isLoggedIn()){
        arguments.userid=session.currentuser.id;
    }
    arguments.ipaddress=getIPAddress();
    l=model("logfile").create(arguments);
}
```

`addlogline()` accepts arbitrary named arguments and passes them straight through to `model("logfile").create()` — so whatever caller passes as `message=`/`type=`/(occasionally mistyped `success=` instead of `message=`, see `PasswordResets.cfc::update()` which calls `addLogLine(type="login", success="Password reset successfully.")` — this is very likely a **bug** in the legacy code: `success` is not a column on `logfiles`, so this value is silently dropped by the ORM's `create()`, meaning that particular log line is written with an EMPTY `message` field) becomes the stored message text. There is no evidence anywhere of a before/after value capture mechanism — confirms PRD Open Question #8 with a definitive **NO**: the audit log records that a flagged event occurred (login success/failure, password reset, cookie-remember-me usage) and a free-text description of it, but never captures field-level before/after state for any entity (bookings, settings, users, permissions, etc.).

**Write-side cross-controller search — confirms only a SUBSET of controller actions actually write to the log, not a global "every action is logged" policy** (same caveat style as plan 04's permission scan): a direct grep for `addlogline`/`addLogLine` calls was run across all 12 controller files. Confirmed call sites:
- `controllers/Sessions.cfc::attemptlogin()` — 4 call sites: successful login (`type="Login"`), wrong password (`type="Login"`), user-not-found (`type="Login"`), missing email/password (`type="Login"`)
- `controllers/PasswordResets.cfc::update()` — 1 call site (`type="login"` — note lowercase, a different literal string than Sessions' `"Login"`, another casing inconsistency), and the bug noted above (uses `success=` instead of `message=`)
- `events/functions.cfm::setCookieRememberUsername()` / `setCookieForgetUsername()` — both call `addlogline(message=..., type="Cookie")` directly (not from a controller action, but from a shared auth helper invoked from `Sessions.cfc`)
- A global `after`-filter also exists on the base `Controller.cfc`: `filters(through="logFlash", type="after")`, which runs on **every single controller action in the entire app** (since `Controller` is the base every other controller `extends`), and calls `addLogLine` for any flash `error` or `success` message present after the action completes (`type="error"` or `type="success"` respectively) — **this is the actual mechanism that makes "logging" appear broad-based**: it is not that individual controllers deliberately call `addlogline`, but that ANY action setting a flash error/success message gets auto-logged as a side effect. This explains the `error`/`success` types seen in `_getLogFileTypes()`'s fixed list alongside the more deliberate `Login`/`Cookie`/`ajax` types.
- **No direct write-call was found for a dedicated "ajax" type** anywhere in the 12 controllers or `events/functions.cfm` — `ajax` appears in the hard-coded filter-type list but its actual write-site was not located within this plan's confirmed file set. Logged as an Open Question below (same "write-side isn't fully visible from this file alone" caveat the plan anticipates).
- **No explicit logging call was found in `Bookings.cfc`, `Users.cfc`, `Locations.cfc`, `Resources.cfc`, `Customfields.cfc`, `Eventdata.cfc`, or `Permissions.cfc`** — booking create/approve/deny/edit/delete, user create/edit/delete, and all settings/permission/location/resource changes are **NOT explicitly logged** beyond whatever incidental flash-message side effect the global `logFlash` after-filter captures (and only if that specific action happens to set a flash message). This means the audit trail for the most business-critical actions (who approved/denied a booking, who changed a permission, who edited a user's role) is **not reliably captured as a structured, intentional audit entry** — it exists only incidentally, if at all, via the flash-message side channel.

### Views reviewed (Logfiles)
- `views/logfiles/_filter.cfm` — confirms the exact filterable field set matches the controller 1:1: `type` (select, options = the controller's 5-type hard-coded list), `userid` (select of all users by email), `rows` (select of fixed row-count options 50/100/250/500/1000/5000) — no additional filter fields exist beyond what the controller supports.
- `views/logfiles/index.cfm` — confirms the exact displayed field set: `ID`, `Message`, `Type` (rendered as a clickable filter-link + colored label), `CreatedAt` (formatted date+time), `IPaddress` (rendered as an external geolocation lookup link `infosniper.net`, UNLESS `isDemoMode` is on, in which case it shows the literal string `"DemoIP"` instead — another confirmed demo-mode behavioral difference), and `User` (resolved via a live sub-query against the `users` table by `userid`, falling back to the literal text `"Anon"` if no matching user row is found — confirming anonymous/unauthenticated log entries, e.g. failed logins with a bad email, are supported and expected). **No before/after value column is rendered** — consistent with the controller/schema finding above; the view has nothing to show because the data was never captured.


---

## PasswordResets

**File:** `controllers/PasswordResets.cfc`

**Filters (init()):** `super.init()` (so the base `accessapplication` filter *does* apply here — but the permissions seed data grants `accessApplication=1` to the `guest` role as well, so anonymous visitors are not blocked by this baseline check). `redirectIfLoggedIn` (an already-authenticated user cannot access the reset flow). `denyInDemoMode` (the **entire controller** is disabled in demo mode, not just specific actions).

**Actions:**
- `create()` (controllers/PasswordResets.cfc:18-33) — looks up user by submitted email. If found: calls `user.createPasswordResetToken()` (see User model section) and sends an email via the `/email/passwordReset` template; flashes a generic success message; redirects to login. **If not found: flashes "Hmm... we couldn't find an account for that address"** — this path **does** reveal account existence by email, an inconsistency with the login flow's deliberately generic error (see Open Questions / observations).
- `edit()` (controllers/PasswordResets.cfc:38-48) — looks up the user by `passwordResetToken`. **If `DateDiff("h", user.passwordResetAt, Now()) > 2`, redirects with "Password reset has expired. [PR2]"** — this is the exact, confirmed **2-hour token expiry window**. Otherwise calls `user.passwordToBlank()` to clear the in-memory password/confirmation fields before rendering the reset form.
- `update()` (controllers/PasswordResets.cfc:53-64) — looks up the user by token again, but **does not re-check the 2-hour expiry at this final submit step** (expiry is only enforced when the edit form is first loaded, not when the new password is actually submitted — logged as an Open Question / legacy timing gap below). Generates a new salt (`createSalt()`) and computes `hashPassword(user.password, user.salt)` using the *existing* (already-hashed) password value — this specific line is effectively superseded by the subsequent `user.update(params.user)` call, because the User model's `beforeSave("securePassword")` hook re-salts/re-hashes from `params.user`'s plaintext `password`+`passwordConfirmation` if both are present (see User model section) — flagged as redundant/dead code rather than a functional defect, since the model hook is what actually produces the final stored hash. On success, calls `_createUserInScope(user)` directly — **a successful password reset auto-logs the user in**, with no separate login step required. On failure: redirects home with error `[PR1]`.

**Views reviewed** (`views/passwordresets/*.cfm`):
- `new.cfm` / `_create.cfm` — single email-address field, posts to `create`.
- `edit.cfm` / `_change.cfm` — two password fields (New Password / Confirm Password), posts to `update` with `key=params.key`. Note: unlike `formparts/_userpw.cfm` (admin create-user form), these `passwordField()` calls are **not** marked `required="true"` at the view-attribute level (server-side `validatesPresenceOf` on the model still enforces this at save time regardless).


---

## Permissions

**File:** `controllers/Permissions.cfc`

**Filters (init()):** `super.init()`. `checkPermissionAndRedirect(permission="accessPermissions")` — the **entire controller** is gated by this single flag, no per-action exceptions. `denyInDemoMode`, only `edit,update`. `verifies(... params="key", paramsTypes="string", route="home")` on `edit,update`.

**Actions:**
- `index()` — lists all permission rows, ordered by `id`.
- `edit()` — loads a permission row by `id` (the row's `id` **is** the permission flag's name string, e.g. `'accessCalendar'` — confirmed by the `permissions` table schema, whose primary key column `id` is `varchar(255)`). If not found, redirects back with an error message that also mentions "isn't editable" — but no distinct `Editable` flag was found on the Permission model/table (unlike `Settings`, which does have one — see Settings controller in the cross-controller scan); this message text appears to be boilerplate copied from the analogous Settings controller rather than reflecting an actual editability check specific to permissions.
- `update()` — updates the permission row with submitted role-checkbox values and saves. **On success, the flash message explicitly reads: "please note you will need to reload the application for this to take effect."**

**Role-to-permission mapping mechanism (exact, cited):**
- **Storage shape:** a single `permissions` table — **one row per permission flag** (PK = the flag name, e.g. `accessCalendar`), with **one integer (0/1) column per role** (`admin`, `editor`, `user`, `guest`) plus a `notes` text column. This is a wide/pivoted matrix table, **not** a normalized permission↔role join table.
- **Roles are not a separate enum/table.** `application.rbs.roles` is derived at application startup by taking the `permissions` table's own **column list** and removing the `ID` and `NOTES` columns (events/onapplicationstart.cfm:23-26: `rolelist=permissions.columnlist; rolelist=listDeleteAt(rolelist, ListFind(rolelist,"ID")); ... application.rbs.roles=rolelist;`). **Adding a new role therefore requires an `ALTER TABLE` on `permissions` to add a new column** — roles are schema-defined, not data-defined.
- **UI shape:** `views/permissions/index.cfm` renders one row per flag × one column per role (looped dynamically over `application.rbs.roles`) with tick/cross icons — a visual matrix, confirming the FRD's "matrix" description. Editing, however, happens **per-flag**: `views/permissions/edit.cfm` → `_form.cfm` shows one row of role checkboxes **for a single permission at a time** — there is no single mass-edit-the-whole-matrix screen.
- **Runtime enforcement + the "reload required" caching behavior:** at `onApplicationStart` (events/onapplicationstart.cfm:21-33), the entire `permissions` table is read once into the in-memory struct `application.rbs.permission[flagName][roleName] = 0|1`. `checkPermission(flag)` (events/functions.cfm:151-161) reads **only** this in-memory struct — it never re-queries the database per-request. **This confirms the Permissions controller's own flash message is accurate: editing a permission via the UI updates the database row immediately, but the change has no runtime effect until the application is reloaded/restarted**, which re-runs `onApplicationStart`. This is a legacy-specific caching quirk that Phase 3 must consciously decide whether to replicate (logged as an open decision point below, not assumed).

**Views reviewed** (`views/permissions/*.cfm`): `index.cfm` (matrix table, described above), `edit.cfm` (wraps `_form`), `_form.cfm` (one role-checkbox row per flag, `#checkbox(objectName="permission", property=i, label=i)#` looped over `application.rbs.roles`).


### Permission Flag Inventory (cross-controller scan)

**Method:** fetched and scanned all 12 legacy controllers (`Api.cfc`, `Bookings.cfc`, `Customfields.cfc`, `Eventdata.cfc`, `Locations.cfc`, `Logfiles.cfc`, `PasswordResets.cfc`, `Permissions.cfc`, `Resources.cfc`, `Sessions.cfc`, `Settings.cfc`, `Users.cfc`) for every `filters(through="checkPermissionAndRedirect", permission="...")` declaration and every inline `checkPermission("...")` call (including inside view templates). Cross-referenced against the full seed list in `install/new-installation.sql` (`INSERT INTO permissions ...`), which defines **17** named flags total — the authoritative ground-truth list of what flags *exist*, independent of whether code currently references them.

| Flag | Gates | Citation |
|------|-------|----------|
| accessApplication | **Base/global filter — applies to every controller that calls `super.init()`** (all except `Sessions.cfc` and `Api.cfc`, which explicitly skip `super.init()`) | controllers/Controller.cfc:9 |
| accessCalendar | `Bookings.cfc` (whole controller, no except); `Eventdata.cfc` (whole controller: `getevents`, `getevent`); `Locations.cfc` (whole controller **except** `list,view`) | controllers/Bookings.cfc:11; controllers/Eventdata.cfc:11; controllers/Locations.cfc:14 |
| allowRoomBooking | `Bookings.cfc`, all actions **except** `index,list,day,building,location,check` (i.e. required for create/edit/update/delete/approve/deny/etc., not required for the various calendar *viewing* actions) | controllers/Bookings.cfc:12 |
| viewRoomBooking | `Bookings.cfc`, **only** `list,view` | controllers/Bookings.cfc:13 |
| allowApproveBooking | `Bookings.cfc`, **only** `approve,deny` | controllers/Bookings.cfc:14 |
| bypassApproveBooking | `Bookings.cfc` — **inline** `checkPermission()` call (not a filter) inside `create()`, only consulted when `application.rbs.setting.approveBooking` is also true; if both are true the newly-created event is auto-marked `status="approved"` | controllers/Bookings.cfc:233 |
| accessCustomfields | `Customfields.cfc` (whole controller, no except — includes the `fieldpicker` ajax action) | controllers/Customfields.cfc:10 |
| accessLocations | `Locations.cfc`, all actions **except** `list,view` | controllers/Locations.cfc:13 |
| accesslogfiles | `Logfiles.cfc` (whole controller); **also** an inline `checkPermission("accessLogfiles")` call inside a view template gating the "Activity" link on the admin user table | controllers/Logfiles.cfc:11; views/users/_usertable.cfm:39 |
| accessPermissions | `Permissions.cfc` (whole controller, no except) | controllers/Permissions.cfc:10 |
| accessresources | `Resources.cfc` (whole controller, no except — includes the `checkavailability` ajax action) | controllers/Resources.cfc:12 |
| accessSettings | `Settings.cfc` (whole controller, no except) | controllers/Settings.cfc:10 |
| accessUsers | `Users.cfc`, all actions **except** `myaccount,updateaccount,updatepassword` | controllers/Users.cfc:11 |
| updateOwnAccount | `Users.cfc`, **only** `myaccount,updateaccount,updatepassword` — the self-service counterpart to `accessUsers` | controllers/Users.cfc:12 |
| allowAPI | `Api.cfc`, **only** `index` | controllers/Api.cfc:11 |
| allowiCal | **No code reference found anywhere** (no filter, no inline check, no view reference) — DB seed notes column reads "Reserved for future use" | install/new-installation.sql:161 (seed row only; confirmed unused by grep across all 12 controllers + all reviewed views) |
| allowRSS | **No code reference found anywhere** — same as above, DB seed notes column reads "Reserved for future use" | install/new-installation.sql:163 (seed row only; confirmed unused) |

**Unguarded / partially-unguarded actions found during the scan (explicit absence-of-gate findings, per F7.4):**
- `Sessions.cfc` — **entirely ungated** by any permission flag (does not call `super.init()`, declares no `checkPermissionAndRedirect` filter at all). This is **by design**: login/logout/forgetme must work for anonymous visitors. Not a gap.
- `PasswordResets.cfc` — gated only by the baseline `accessApplication` (via `super.init()`), which the seed data grants to the `guest` role (`guest=1` for `accessApplication`) — so anonymous users are not blocked. No reset-specific permission flag exists; access control here is purely token-based (the emailed reset token), not role-based. Not a gap, but worth noting no role-flag protects this flow — it is intentionally public-by-design.
- `Api.cfc` — **split enforcement**: the `index` action requires the `allowAPI` permission flag but is **not** covered by the `f_isValidAPIRequest` token filter (`except="index"`); conversely, `display`, `rss2`, and `ical` actions **are** covered by `f_isValidAPIRequest` (require a valid per-user `apitoken` query-string value) but have **no permission-flag check at all** — meaning any holder of *any* valid user's API token can hit `rss2`/`ical`/`display` regardless of that user's role/permissions. This directly supports the PRD's interim "fully public feeds" decision — confirmed from source as token-gated-only, not role-gated, for the feed-rendering actions. (See also `findings/05-platform-settings.md` for the dedicated `Api.cfc`/`Settings.cfc` audit.)
- `Bookings.cfc` — the calendar-viewing actions (`index,list,day,building,location,check`) require only `accessCalendar`, not `allowRoomBooking`/`viewRoomBooking` — i.e. a logged-in user with baseline calendar access can see the various calendar views without either of the more specific booking-view/booking-create flags; `viewRoomBooking` only additionally restricts `list` and `view` (the most data-complete views), not the ajax/basic calendar renders.
- `Locations.cfc` — `list` and `view` actions require **only** the base `accessApplication` (via `super.init()`), explicitly excepted from both `accessLocations` and `accessCalendar` — these are the public-facing, read-only location views.

**Verdict on PRD Open Question #9 (permission matrix completeness):** **RESOLVED — NOT EXHAUSTIVE, as PRD/PROJECT.md already suspected.** The legacy system defines **17** permission flags (per the `permissions` table seed data), of which **15 are actively enforced** somewhere in the controller/view code (the 6 PRD-named flags **plus 9 more**: `accessApplication`, `accessCustomFields`, `accessLocations`, `accessLogfiles`, `accessResources`, `accessSettings`, `accessUsers`, `updateOwnAccount`, `bypassApproveBooking`), and **2 are defined but entirely unreferenced/dead** (`allowiCal`, `allowRSS` — both explicitly labelled "Reserved for future use" in their own seed data). Phase 3's deny-by-default permission baseline must account for all 15 active flags, not just the 6 originally named; the 2 reserved-but-unused flags should be explicitly decided (carry forward as reserved, or drop) rather than silently ported.


---

## Resources

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

## Sessions

**File:** `controllers/Sessions.cfc`

**Filters (init()):** explicitly **does not** call `super.init()` (code comment: "doesn't go via super.init()", controllers/Sessions.cfc:8) — meaning Sessions is exempt from `Controller.cfc`'s global `accessapplication` permission filter and its `logFlash` after-filter. Only filter: `redirectIfLoggedIn`, only `new,attemptlogin` (bounces an already-logged-in user away from the login form).

**Actions:**
- `attemptlogin()` (controllers/Sessions.cfc:16-47) — looks up the user by email (`findOneByEmail`). If found: decrypts the stored salt (`decrypt(user.salt, getAuthKey(), 'CFMX_COMPAT')`), computes `hash(submittedPassword & decryptedSalt, 'SHA-512')`, and compares to `user.password`. On match: if `params.rememberme` was submitted, calls `setCookieRememberUsername(params.email)` (see below for the exact duration); logs the event via `addlogline()`; calls `_createUserInScope(user)`. On any failure path (wrong password, user not found, missing email/password), the response is a **generic** "We could not sign you in. Please try that again." — no user-enumeration distinction is made at login (contrast with `PasswordResets.create()`, which *does* distinguish — see below).
- `logout()` (controllers/Sessions.cfc:52-55) — deletes `session.currentUser`, redirects home.
- `forgetme()` (controllers/Sessions.cfc:60-63) — clears the remember-me cookie, redirects to login.

**"Remember me" mechanism — exact citation (events/functions.cfm:315-319):**
```
<cffunction name="setCookieRememberUsername" ...>
  <cfargument name="username">
  <cfcookie name="RBS_UN" expires="360" value="#arguments.username#" httpOnly="true">
  ...
</cffunction>
```
**This resolves the FRD remember-me-duration open question.** The cookie's `expires` is **360 days**. Critically, the cookie stores **only the submitted email address** — it is a login-form **username-prefill convenience**, not a persistent-session or stay-logged-in token. There is no corresponding extension of the underlying CF/Lucee web-session lifetime tied to this checkbox. No session-timeout override (e.g. `this.sessionTimeout` / `cfapplication sessionTimeout`) was found anywhere in `Application.cfc`, `config/app.cfm`, `config/environment.cfm`, `config/settings.cfm`, or any of the per-environment `config/*/settings.cfm` files — the actual session-expiry duration is therefore an **engine/server-level default outside this codebase** (see Open Questions).

**Access-denied redirect/response behavior:** `checkPermissionAndRedirect()` (the mechanism used by every permission filter across all 12 controllers) redirects to `route="denied"` → `controllers/Sessions.cfc`'s `denied` action → `views/sessions/denied.cfm`, whose **entire file content** is a single HTML comment (`<!--- Access Denied--->`) — confirmed from source: the access-denied response renders an effectively blank page with no user-facing message, button, or navigation aid.

**Views reviewed** (`views/sessions/*.cfm`):
- `new.cfm` — wraps the `_signin` partial in a panel.
- `_signin.cfm` — **in demo mode, prints the demo admin credentials in plaintext directly in the page** (`admin@domain.com` / `roombooking100`, views/sessions/_signin.cfm:7-11) — a notable demo-mode-only finding. Login form: if a saved-email cookie is present, shows a "Welcome back {email} (Not You?)" message with a hidden `email` field and **no** remember-me checkbox (it's implicitly already remembered); if no cookie is present, shows the email field **and** the "Remember my email" checkbox. This view confirms the checkbox's sole purpose is deciding whether to *set* the `RBS_UN` cookie — not an independent "stay logged in longer" toggle.
- `denied.cfm` — see above; single comment line, no content.


---

## Settings

**Controller:** `controllers/Settings.cfc`
(https://raw.githubusercontent.com/neokoenig/RoomBooking/master/controllers/Settings.cfc)

- Permission gate: `filters(through="checkPermissionAndRedirect", permission="accessSettings")` in `init()` — every action in this controller requires the `accessSettings` permission flag. Per the legacy permission matrix (`install/new-installation.sql`, `INSERT INTO permissions ... ('accessSettings', '1', '1', '0', '0', ...)`), this is granted to `admin` and `editor` roles only; `user`/`guest` are denied.
- A **second**, independent gate exists on top of the permission check: `filters(through="_checkSettingsAdmin")`, whose body is:
  ```cfm
  private void function _checkSettingsAdmin() {
      if(!application.rbs.setting.allowSettings){
          redirectTo(route="home", error="Facility to edit settings has been disabled");
      }
  }
  ```
  This reads a **data-driven kill-switch**: the `allowSettings` row in the `settings` table (seeded `'1'` / boolean / category `General` in `install/new-installation.sql`). If an admin sets this setting to `0` via the settings UI itself, the *entire* Settings controller becomes inaccessible to everyone — including other admins — until it is flipped back directly in the database (the controller has no other way in once `allowSettings=0`, since the gate fires before every action). This is a real global kill-switch, not merely a display toggle.
- `edit`/`update` actions additionally carry `filters(through="denyInDemoMode", only="edit,update")` — demo-mode installs (`isDemoMode` setting) cannot modify settings at all, read-only.
- `update()` further re-checks per-record editability at the model layer (see below) independent of the permission/demo gates: `if(!isObject(setting) OR !setting.Editable OR application.rbs.setting.isDemoMode)` — so even a user who passes both the `accessSettings` permission and the `allowSettings` kill-switch cannot update a setting whose own `editable` column is `0`.
- Singleton/record-identity mechanism: there is **no single "Settings" singleton row**. Instead `models/Settings.cfc` wraps a **rowset** of individually keyed setting records (table `settings`, primary key `id` = the setting's string name, e.g. `allowSettings`, `approveBooking`, `calendarSlotMinutes`). Each `edit`/`update` action operates on exactly one named row at a time: `model("setting").findOne(where="id = '#params.key#'")`. "Singleton" in the traditional one-row sense does not apply here — the closest analogue is `application.rbs.setting`, an in-memory struct built once at application-start (`events/onapplicationstart.cfm`) by iterating every row of the `settings` table into `application.rbs.setting['#setting.id#']`. All runtime reads throughout the app (`application.rbs.setting.sitetitle`, `.allowSettings`, `.isDemoMode`, etc.) hit this in-memory struct, NOT the database directly — meaning **a setting update via the UI does not take effect until the application is reloaded** (confirmed directly by the controller's own success message: `"Setting successfully updated - please note you will need to reload the application for this to take effect"`).

### Views reviewed (Settings)
- `views/settings/index.cfm` — renders all settings grouped into Bootstrap tabs by `category` (so the 6 categories above — General/Email/Calendar/Locations — become UI tabs); each row shows ID, current value (boolean rendered as tick/cross via `tickorcross()`, else raw `code` text), and an Edit button **gated per-row on `editable`** (`<cfif editable>`) — confirms the schema-level `editable` column directly drives UI affordance, matching the controller's own `!setting.Editable` check.
- `views/settings/edit.cfm` — thin wrapper rendering `_form` inside a panel with an update button.
- `views/settings/_form.cfm` — renders a single setting's edit widget based on `fieldtype`: `integer,string` → text field; `boolean` → a `0/1` select. **No `fieldtype` branch exists for any other type** — if a future setting were seeded with any fieldtype other than `integer`/`string`/`boolean`, the form would render nothing for the value (silent gap, logged as an Open Question below).

### Email notification templates (content review, cross-ref Open Question #5)

Views: `views/email/bookingNotify.cfm`, `views/email/passwordreset.cfm`, wrapped by `views/common/email/_header.cfm` and `views/common/email/_footer.cfm`.

- **`bookingNotify.cfm`** (https://raw.githubusercontent.com/neokoenig/RoomBooking/master/views/email/bookingNotify.cfm) — recipient is **not resolved in this template**; the template receives a passed-in `event` struct and personalizes greeting via `#event.contactName#` — i.e. the booking's own contact name/email, not necessarily the logged-in user. Body text branches on `#event.status#` (`approved` / `pending` / `denied`) with three distinct hard-coded message strings — this is direct evidence that **a single template handles all three booking-status notification variants** (confirm vs. pending vs. denied all route through the same `bookingNotify` template with conditional copy, not three separate templates). Placeholders rendered: `event.contactName`, `event.title`, `event.start`/`event.end` (via `_formatDate()`), `eventlocation.name`/`.description`/`.building`, `event.description`, and a "Contact Details" block built from `event.contactname`/`event.contactemail`/`event.contactno` (falls back to "None provided" if all three are empty). **No explicit admin/CC/BCC address is referenced inside this template** — any BCC behavior is handled upstream at send-time (see `bccAllEmail`/`bccAllEmailTo` settings above; the template itself has no knowledge of them). The actual recipient address (`to=`) and `sendEmail()` call invoking this template were **not found inside this template file itself** — the call site is in a model/controller not covered by this plan's confirmed file list (flagged as Open Question below, same caveat pattern as the write-side logging gap).
- **`passwordreset.cfm`** — recipient is explicitly `user.firstname` in greeting; the reset link is built with `linkto(controller="passwordResets", action="edit", key=user.passwordResetToken)`. The actual `sendEmail()` call IS visible for this one (in `controllers/PasswordResets.cfc::create()`): `to=user.email`, `from="#application.rbs.setting.sitetitle# <#application.rbs.setting.siteemailaddress#>"`, confirming the site's configured `siteEmailAddress` setting is the From address for at least this email type.
- **`common/email/_header.cfm`** / **`common/email/_footer.cfm`** — these wrap every outgoing email with shared branding boilerplate (a full inline-CSS HTML email shell). The header injects `An automated email from #application.rbs.setting.sitetitle#` as a preheader line; the footer injects `This is an automated email sent on behalf of #application.rbs.setting.sitetitle#`. Both pull the site title from the same `application.rbs.setting` struct documented above — confirming **every transactional email (booking notify + password reset) is branded via the same site-title setting**, with no per-email-type override.

### Shared layout/form helpers (cross-controller)

- **`views/common/layout/_navbar.cfm`** — this is the authoritative, UI-rendered permission-gated nav. Confirmed gates, each matching the permission matrix seeded in `install/new-installation.sql`:
  - `accessCalendar` gates the entire "Events" dropdown (Calendar/List/Locations links)
  - `allowAPI` gates the "Data Feeds" nav link specifically (`<cfif checkpermission("allowAPI")>`) — **this is a UI-level mention of `allowAPI` as a permission check**, directly relevant to the Api controller gating verdict below (the nav hides the link, but as shown in `## Api controller`, the controller itself does NOT enforce `allowAPI` on the feed-serving actions — only on `index`, the page that lists feed URLs).
  - `allowRoomBooking` gates "Book a Room"
  - `isloggedin() AND (allowSettings OR allowLocations)` gates the entire "Settings" dropdown container
  - Inside that dropdown: `updateOwnAccount` → "My Account"; `accessUsers` → "Users"; `allowLocations AND accessLocations` → "Locations"; `allowResources AND accessResources` → "Resources"; `allowSettings` wraps a further nested set: `accessSettings` → "Configuration", `accessPermissions` → "Permissions", `accessCustomFields` → "Custom Fields", `accessLogfiles` → "Logs".
  - This nested-gating pattern (data-setting AND permission-flag, e.g. `allowSettings AND accessSettings`) directly cross-references and confirms plan 04's Permission Flag Inventory — nav visibility is never permission-only; several sections require **both** a setting toggle AND a permission flag to be true simultaneously.
  - Also renders `isDemoMode` as a "(Demo Mode)" suffix on the site title/brand — confirms demo mode is user-visible in the UI chrome, not just a silent backend gate.
- **`views/common/layout/_head.cfm`** — also surfaces `isDemoMode` in the `<title>` tag; adds a `<meta http-equiv="refresh" content="30">` specifically when `request.bodyClass EQ "displayBoard"` (i.e., the Api controller's `display` action) — confirms the public display-board view auto-refreshes every 30 seconds, a functional behavior relevant to Phase 7 (Public Feeds).
- **`views/common/layout/_footer.cfm`** — conditionally loads minified vs. unminified JS bundle based on `application.wheels.environment EQ "production"` — **this is the one directly-observed functional (not just config-value) difference tied to environment**, confirmed here as evidence for the Per-Environment Settings Behavior section below. Also conditionally injects Google Analytics tracking script only if `googleanalytics` setting is not the placeholder value `UA-`.
- **`views/common/layout/_main.cfm`** — generic content wrapper; footer shows `application.rbs.versionNumber` (hard-coded app version, confirmed `"1.2"` in `events/onapplicationstart.cfm`) and `application.rbs.setting.version` (the separate, user-visible/DB-stored `version` setting row) — these are two **different** version numbers, one code-level and one data-level; do not conflate them.
- **`views/common/form/_customfields.cfm`** — generic custom-field-rendering partial, confirmed as a thin wrapper that loops the custom-fields query and calls `processShortCodes()` against a `[field id=#id#]` shortcode template — cross-references plan 03's field_type findings; this partial itself does not contain per-field-type rendering logic (that lives in the shortcode callback `field_callback` in `events/functions.cfm`, which `include`s a separate `/shortcodes/field.cfm` template not in this plan's confirmed file list — flagged as Open Question below).


---

## Users

**File:** `controllers/Users.cfc`

**Filters (init()):**
- `_checkLoggedIn` — all actions must be authenticated (controllers/Users.cfc:10).
- `checkPermissionAndRedirect(permission="accessUsers")`, **except** `myaccount,updateaccount,updatepassword` (controllers/Users.cfc:11) — admin user-management actions require the `accessUsers` flag.
- `checkPermissionAndRedirect(permission="updateOwnAccount")`, **only** `myaccount,updateaccount,updatepassword` (controllers/Users.cfc:12) — self-service actions are gated by a *separate* flag from admin management.
- `denyInDemoMode`, only `create,update,updateaccount,updatepassword,assumeuser,generateAPIKey` (controllers/Users.cfc:13) — mutating actions are disabled when `application.rbs.setting.isdemomode` is true.
- `verifies(... params="key", paramsTypes="integer", route="home")` on `edit,update,delete,assumeUser,recover,generateAPIKey` (controllers/Users.cfc:16) — numeric key required, else redirected home with a "can't be found" error.
- `getCurrentUser` filter loads the acting user for `myaccount,updateaccount,updatepassword` (controllers/Users.cfc:20).
- `_getRoles` filter loads `application.rbs.roles` for `index,add,edit,delete,update,create` (controllers/Users.cfc:21) — populates the role dropdown on admin forms.

**Actions:**
- `updateaccount()` (controllers/Users.cfc:28-43) — self-service profile update. Explicitly `structDelete`s `password`/`passwordConfirmation` from `params` before calling `user.update()`, and strips `role` from `params.user` — this is the server-side mechanism preventing a non-admin self-service edit from touching their own password or escalating their own role. On success: flash + redirect to `myaccount`; on failure: re-render `myaccount`.
- `updatepassword()` (controllers/Users.cfc:48-64) — dedicated self-service password-change action. Requires `params.password EQ params.passwordConfirmation` (a *controller-level* equality check, independent of the model's `validatesConfirmationOf`). Hashes the new password using the user's **existing** salt (`hashPassword(params.password, decryptSalt(user.salt))`) — notably does **not** rotate the salt on a self-service password change (contrast with `PasswordResets.update()` below, which does rotate the salt).
- `assumeUser()` (controllers/Users.cfc:69-77) — admin "login as" / impersonation. Blocked entirely in demo mode. No additional permission flag beyond the class-level `accessUsers` gate (impersonation is not separately permissioned). Directly calls `_createUserInScope(user)` — logs the admin into the target account's session with **no password re-entry**.
- `index()` (controllers/Users.cfc:81-84) — admin account listing, paginated 25/page, excludes soft-deleted users (`includeSoftDeletes=false`).
- `add()` / `create()` (controllers/Users.cfc:88-110) — **admin creates the account directly**, submitting `params.user` which (per the `add.cfm` view, see below) includes a plaintext initial password + confirmation typed by the admin at creation time. **This resolves the FRD initial-credential open question: the legacy mechanism is admin-set-password-at-creation, not an auto-generated password or an email invite/activation link.**
- `update()` (controllers/Users.cfc:114-130) — admin edit of an existing user's full record; blocked in demo mode.
- `delete()` (controllers/Users.cfc:134-148) — **soft** delete (sets `deletedAt` via the Wheels `delete()` convention); blocked in demo mode.
- `recover()` (controllers/Users.cfc:152-163) — un-deletes by blanking `deletedAt`.
- `generateAPIKey()` (controllers/Users.cfc:169-178) — generates/replaces `user.apitoken` (consumed by `Api.cfc`'s token-based access, see Permission Flag Inventory). Falls under the class-level `accessUsers` gate (not in that filter's `except` list) and under `denyInDemoMode`.

**Views reviewed** (`views/users/*.cfm`, `views/users/formparts/*.cfm`):
- `_usertable.cfm` — admin listing table (name/email/role/API-token-present/created date) with a per-row action dropdown: Edit; **Assume** (shown only when `role NEQ "admin" AND userIsInRole("admin")` — i.e. the UI hides "assume" for admin-on-admin targets, but this is a **client-side/view-only** restriction; the `assumeUser()` controller action itself performs no such check, so it is not server-side enforced); Generate/Regenerate API Key; an "Activity" link gated inline by `checkPermission("accessLogfiles")` (views/users/_usertable.cfm:39) — the only in-view (non-filter) permission check found in the identity-access view set; Disable (soft-delete, with a client confirm dialog) or Recover.
- `add.cfm` / `edit.cfm` — assemble `formparts/main` + `formparts/userrole` (+ `formparts/userpw`, **add only**).
- `formparts/_userrole.cfm` — role `<select>` sourced from the `roles` variable (`application.rbs.roles`), cross-referencing the Permissions controller's role-derivation mechanism (see Permissions controller section) — confirms role assignment on the user form is a plain select from the same role set the permission matrix uses.
- `formparts/_userpw.cfm` — panel titled **"Initial Password"**, two required password fields (password + confirmation). No client-side complexity hint/pattern is rendered here (the model's regex validation is server-side only; the admin sees no complexity guidance in this form).
- `formparts/_main.cfm` — firstname/lastname/email/tel/address/country fields; no password-related fields.
- `myaccount.cfm` — self-service page: profile form (posts to `updateaccount` route) plus a separate "Change Password" panel (posts to `updatepassword` route); the password panel is replaced with "Disabled in Demo Mode" text when `application.rbs.setting.isDemoMode` is true.
- `index.cfm` — thin wrapper rendering `_usertable` + pagination links.


---

## Event

**Source:** `models/Event.cfc` (extends `Model`). No explicit `property()` declarations exist in
this file — CFWheels models are reflection-based over the underlying DB table by default, so the
**complete column list** cannot be fully confirmed from this file alone (see Open Questions
limitation note below); the `registerSystemFields()` callback below is nonetheless the authoritative
**inferred** field list, since it is the actual metadata source the form-builder/shortcode template
system (`_form.cfm`, `_details.cfm`) renders from.

**Associations** (models/Event.cfc: init):
- `belongsTo("location")` — confirmed.
- `hasMany("eventresources")` — confirmed.
- `nestedproperties(associations="eventresources", allowDelete=true)` — nested
  create/update/delete of `eventresources` rows is permitted directly through `event.new(...)` /
  `event.update(...)` calls (consistent with `tabs/_resources.cfm`'s `hasManyCheckbox` usage);
  `allowDelete=true` means unchecking a resource on an update actually **deletes** the
  corresponding `eventresource` row rather than merely unlinking it.

**Validation** (models/Event.cfc: init, checkDates):
- `validate("checkDates")` (a custom validation function, not a conventional `validatesX()` call).
  Body: defaults `this.start=now()` if not already a valid date; defaults `this.end =
  dateAdd("h", 1, this.start)` if not already valid (1-hour default duration); then
  `if((DateCompare("#this.end#", "#this.start#")) EQ -1){ addError(property="end", message="End Date can not be before Start Date.") }`.
  **Correction to PROJECT.md's existing "CONFIRMED" claim:** PROJECT.md states "`end_time` must be
  strictly after `start_time`" with zero-duration bookings rejected. The actual code only checks
  `DateCompare(...) EQ -1` (strictly **before**) — it does **not** reject `DateCompare(...) EQ 0`
  (end exactly equal to start). A zero-duration booking (`end == start`) therefore **passes** this
  validation in the legacy system; PROJECT.md's "zero-duration rejected" sub-claim is **not**
  supported by the cited code and should be corrected in the consolidation pass. The "end must not
  be strictly before start" half of the claim is confirmed as written.

**Callbacks** (models/Event.cfc: init):
- `afterFind("formatDates")` — runs after every load from the DB; reformats `this.start`/`this.end`
  from raw DB datetimes into display strings (`"DD MMM YYYY" & ' ' & "HH:mm"`) **in place**. Any
  further date arithmetic against a freshly-loaded event's `start`/`end` is therefore operating on a
  formatted string, not a date object, until re-parsed — this is why `checkDates()` re-defends with
  `isDate()` checks even on objects that have already been through a find.
- `afterInitialization("registerSystemFields")` — runs on **every** instantiation (`new()` and
  find-hydration alike). Populates `this.systemfields`, an in-memory (non-persisted) array
  describing each built-in field for the dynamic `[field id='...']`/`[output id='...']` shortcode
  template system used by `_form.cfm` and `_details.cfm`. Full field list, with `required` flag as
  declared:
  - `title` (textfield, **required**)
  - `start` (datepicker, **required**)
  - `status` (select: `pending`/`denied`/`approved`, **not required** at the systemfields level —
    actual assignment is fully automated via `checkApproval`, see below)
  - `end` (datepicker, **required**)
  - `allday` (checkbox, not required)
  - `description` (textarea, not required)
  - `locationid` (select, **required**)
  - `layoutstyle` (select, not required; options sourced from `application.rbs.setting.roomlayouttypes`
    or per-location overrides)
  - `contactname` (textfield, not required)
  - `contactemail` (textfield/email, not required)
  - `contactno` (textfield, not required)
  - `emailcontact` (checkbox, not required — a **transient/virtual** flag only ever read by
    `Bookings.create()`'s email-trigger check; it is not referenced anywhere else in the model or
    in `update()`, confirming it is not a persisted column consumed post-creation).
  Note: contact fields (`contactname`/`contactemail`/`contactno`) are **not required** at this
  level — there is no model-level mandatory-contact-info constraint.
- `beforeCreate("checkApproval")` — **create-only** (not `beforeSave`/`beforeUpdate`), so it never
  re-fires on `update()`. Sets `this.status="pending"` if `application.rbs.setting.approveBooking`
  is on, else `this.status="approved"`. This is the sole model-level source of truth for initial
  approval status; the controller's additional `bypassApproveBooking` override (documented under
  `Bookings.create()`) runs strictly after this callback and after the first `event.save()`
  succeeds.


---

## Eventresource

**Source:** `models/Eventresource.cfc` (extends `Model`). Entire file body:

**Associations:** `belongsTo("event")`, `belongsTo("resource")` — this is the complete structural
definition. These two `belongsTo` declarations confirm `Eventresource` is the Event↔Resource
many-to-many **join/junction** model, consistent with `Event.cfc`'s `hasMany("eventresources")` and
with the composite-key pattern seen in `views/bookings/tabs/_resources.cfm`'s
`hasManyCheckbox(objectname="event", association="eventresources", keys="#event.key()#,#id#")`
(event id + resource id pair).

**Validations/callbacks/additional fields:** none declared in this file. As with `Event.cfc`, the
exact DB column list (presumably `id`, `eventid`, `resourceid` at minimum) cannot be fully confirmed
from the `.cfc` source alone — see Open Questions limitation note.


---

## Location

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

## Resource

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

## Customfield / Customfieldjoin / Customfieldvalue

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

### Customfieldjoin model

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

### Customfieldvalue model

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


---

## User

**File:** `models/User.cfc`

**Password hashing/salt mechanism (exact, cited):**
- Hash function: **SHA-512**, salted.
- Salt generation (models/User.cfc:37-46, `securePassword()`): a fresh salt is a **UUID** (`createUUID()`). The **raw UUID** is what gets concatenated with the plaintext password before hashing: `hash(this.password & p.salt.uuid, 'SHA-512')`. The salt as **stored** in `users.salt`, however, is the **encrypted** form of that same UUID — `encrypt(p.salt.uuid, getAuthKey(), 'CFMX_COMPAT')` — using ColdFusion's `CFMX_COMPAT` algorithm and a per-installation secret key.
- The secret key (events/functions.cfm:109-118, `getAuthKey()`): read from `config/auth.cfm`; if that file doesn't exist, a new UUID is generated and **written** to it on first access. This key is shared across **all** users in the installation. The code's own comment states the explicit intentional purpose: "you can invalidate all the site passwords in one go by just changing it" — i.e. rotating `config/auth.cfm` is a deliberate kill-switch that breaks every existing user's ability to have their password re-verified (since every login re-derives the raw salt by decrypting with the *current* key).
- Verifying a login therefore requires: decrypt the stored (encrypted) salt back to the raw UUID using the current `authKey`, then re-hash `password & rawUUID` and compare to the stored hash (see Sessions controller, `attemptlogin()`).
- **Password complexity validation (exact regex + message, cited verbatim, models/User.cfc:11-13):**
  ```
  validatesFormatOf(property="password",
      regEx="^.*(?=.{6,})(?=.*\d)(?=.*[a-z]).*$",
      message="Your password must be at least 6 characters long and contain a mixture of numbers and letters.");
  ```
  **This resolves the FRD password-complexity open question.** Requirements: minimum **6 characters**, at least **one digit**, at least **one lowercase letter**. The regex does **not** require an uppercase letter and does **not** require any special/symbol character. **Any Keycloak password policy configured in the new system must be no weaker than this** (≥6 chars, ≥1 digit, ≥1 lowercase letter) to avoid a functional regression, though the new policy may reasonably be made *stricter*.
- **All other validations (models/User.cfc:10-17):** `validatesFormatOf(email, type="email")`; `validatesPresenceOf("firstname,lastname,email")`; `validatesConfirmationOf(properties="password")` (password must equal passwordConfirmation, message: "Your passwords must match!"); `validatesPresenceOf(properties="password", message="You must enter a password")` — this is an **unconditional** presence rule; non-password-touching updates (e.g. `Users.updateaccount()`) only succeed because the controller explicitly `structDelete`s the `password`/`passwordConfirmation` keys from `params` *before* calling `update()`, rather than any conditional-validation logic in the model itself; `validatesUniquenessOf("email")` — case-sensitivity of this check is inherited from the database column's collation, which was **not** explicitly declared in the `users` table's `CREATE TABLE` statement reviewed (`install/new-installation.sql`) beyond the table-level `DEFAULT CHARSET=utf8` — the effective collation (and therefore whether `Joe@x.com` and `joe@x.com` collide) could not be confirmed from source alone (logged as an Open Question).
- **`beforeSave("sanitize,securePassword")` (models/User.cfc:9):**
  - `sanitize()` — runs `htmlEditFormat()` over firstname/lastname/address1/address2/state/postcode/country/tel (basic stored-XSS mitigation at save time).
  - `securePassword()` — **only** re-salts/re-hashes if **both** `this.password` **and** `this.passwordConfirmation` struct keys exist on the object at save time (`StructKeyExists` guard, models/User.cfc:39). This is the exact mechanism by which a save that never touched password fields leaves the existing password/salt untouched.
- **Role association:** `role` is a plain string column directly on `users` (not a join table), `DEFAULT 'user'` per schema. **No database-level referential integrity** ties `users.role` to the `permissions` table's role-columns — a mistyped/unknown role value falls through `checkPermission()`'s `structKeyExists` guard (events/functions.cfm:153) to **no permission granted**, i.e. the legacy system already deny-by-defaults on an unrecognized role, which is consistent with the new system's F7.4 requirement and worth deliberately preserving.
- `generateToken()` (models/User.cfc:79-81) — lower-cased UUID with dashes stripped. Used for **both** `passwordResetToken` (active, see PasswordResets controller) and `emailConfirmationToken` (models/User.cfc:63-65, `setEmailConfirmationToken()` — the method's own doc comment reads **"(not actually used)"**, and no controller action anywhere in the codebase calls it — confirmed dead/unused code, not a functioning email-confirmation feature).


---

## Settings (model)

**Model:** `models/Settings.cfc`
(https://raw.githubusercontent.com/neokoenig/RoomBooking/master/models/Settings.cfc)

The model file itself is a near-empty Wheels ORM stub (`component extends="Model"` with just a constructor) — it declares no explicit associations, validations, or callbacks in code. All per-row metadata (`fieldtype`, `editable`, `category`, `notes`) lives in the **database schema**, confirmed from `install/new-installation.sql`:

```sql
CREATE TABLE `settings` (
  `id` varchar(255) NOT NULL,
  `value` varchar(500) NOT NULL,
  `notes` varchar(1000) DEFAULT NULL,
  `fieldtype` varchar(55) NOT NULL DEFAULT 'integer',
  `editable` int(1) NOT NULL DEFAULT '1',
  `category` varchar(255) NOT NULL DEFAULT 'General',
  PRIMARY KEY (`id`)
)
```

**Full configurable field inventory** (every row from `install/new-installation.sql`'s `INSERT INTO settings`, 33 rows total, grouped by `category`):

| Category | id | default value | fieldtype | editable |
|---|---|---|---|---|
| Locations | allowLocations | 1 | boolean | 1 |
| General | allowResources | 1 | boolean | 1 |
| General | allowSettings | 1 | boolean | 1 |
| General | **approveBooking** | 0 | boolean | 1 |
| Email | bccAllEmail | 0 | boolean | 1 |
| Email | bccAllEmailTo | admin@domain.com | string | 1 |
| Calendar | calendarAllDaySlot | 1 | boolean | 1 |
| Calendar | calendarAllDayText | all-day | string | 1 |
| Calendar | calendarAxisFormat | h(:mm)a | string | 1 |
| Calendar | calendarColumnFormatDay | dddd d/M | string | 1 |
| Calendar | calendarColumnFormatMonth | ddd | string | 1 |
| Calendar | calendarColumnFormatWeek | ddd d/M | string | 1 |
| Calendar | calendarDefaultView | month | string | 1 |
| Calendar | **calendarFirstday** | 1 | boolean | 1 |
| Calendar | calendarHeadercenter | title | string | 1 |
| Calendar | calendarHeaderleft | prev,next today | string | 1 |
| Calendar | calendarHeaderright | month,agendaWeek,agendaDay | string | 1 |
| Calendar | calendarHiddenDays | [] | string | 1 |
| Calendar | **calendarMaxtime** | 23:00:00 | string | 1 |
| Calendar | **calendarMintime** | 07:00:00 | string | 1 |
| Calendar | calendarSlotEventOverlap | 0 | boolean | 1 |
| Calendar | **calendarSlotMinutes** | 00:15:00 | string | 1 |
| Calendar | calendarTimeformat | H:mm | string | 1 |
| Calendar | calendarWeekends | 1 | boolean | 1 |
| Calendar | calendarWeekNumbers | 0 | boolean | 1 |
| General | defaultDateFormat | DD MMM YYYY | string | 1 |
| General | defaultTimeFormat | HH:MM | string | 1 |
| General | googleanalytics | UA- | string | 1 |
| General | isDemoMode | 0 | boolean | **0 (not editable via UI)** |
| General | resourceTypes | Computers,Audio Visual,Furniture | string | 1 |
| Locations | roomlayouttypes | Standard,Boardroom,Lecture | string | 1 |
| Locations | showlocationcolours | 1 | boolean | 1 |
| Locations | showlocationfilter | 1 | boolean | 1 |
| General | sitedescription | Room Booking System | string | 1 |
| Email | siteEmailAddress | bookings@domain.com | string | 1 |
| General | sitelogo | / | string | 1 |
| General | sitetitle | Room Booking System | string | 1 |
| General | version | 1.2 | string | 1 |

This **directly confirms** PROJECT.md/FRD's assumed `approveBooking` flag (boolean, auto-approve toggle for new bookings — "Whether to force new bookings to be approved by a user with the allowApproveBooking permission") and the three calendar slot-size/min-time/max-time fields (`calendarSlotMinutes`, `calendarMintime`, `calendarMaxtime`), plus `calendarFirstday` (week-start day). Note `isDemoMode` is present in the schema but **not editable** through the Settings UI (`editable=0`) — it can only be flipped directly in the database, consistent with it being a deployment-level flag rather than a tenant-configurable one.

---

## Route Inventory (config/routes.cfm)

**Source:** `config/routes.cfm`
(https://raw.githubusercontent.com/neokoenig/RoomBooking/master/config/routes.cfm)

The file declares exactly **9 explicit custom routes**, plus 1 default route. Every other controller/action is reached via **Wheels' implicit RESTful/CRUD routing convention** (`/controller/action`, `/controller/action/key`), which is not itself declared in this file but is the framework default for any action not explicitly overridden. Cross-referencing against all 12 controllers' actual public functions (enumerated above and in the Settings/Api/Logfiles section) confirms every controller has at least one reachable action:

| Controller | Explicit named route(s) | Pattern | Reachable via implicit convention? |
|---|---|---|---|
| `bookings` | `home` (default route) | `""` → `bookings#index` | Yes — `view`/`building`/`location`/`list`/`day`/`add`/`approve`/`deny`/`clone`/`edit`/`create`/`update`/`delete`/`check` all reachable at `/bookings/{action}[/key]` |
| `sessions` | `logout`, `attemptlogin`, `login`, `forgetme`, `denied` | `/logout`, `/login/a/`, `/login`, `/forgetme`, `/denied` | `login` maps to `sessions#new` (note the route NAME `login` maps to the conventional `new` action, a Rails-style "new session" idiom) — all 5 Sessions actions are covered by explicit routes; no implicit fallback needed |
| `users` | `updateaccount`, `myaccount`, `updatepassword`, `mypassword` | `/my/account/u`, `/my/account`, `/my/password/u`, `/my/password` | Yes — `index`/`add`/`edit`/`create`/`update`/`delete`/`recover`/`generateAPIKey`/`assumeUser` reachable at `/users/{action}[/key]`; the 4 explicit "my account/password" routes give human-friendly URLs to the self-service subset of actions that would otherwise collide with the admin CRUD routes |
| `eventdata` | `getEvents`, `getEvent` | `/eventdata/getevents/[type]/[key]`, `/eventdata/getevent/[key]` | These 2 are Eventdata's **only** 2 public actions (plus the private `_setModelType` filter) — fully covered by explicit routes; the pattern-based `[type]`/`[key]` segments are the only parameterized (non-query-string) route patterns in the whole file |
| `locations` | none | — | Yes — `list`/`view`/`add`/`create`/`edit`/`update`/`delete` reachable at `/locations/{action}[/key]`, confirmed in `views/common/layout/_navbar.cfm`'s `linkTo(controller="locations", action="list", ...)` calls |
| `logfiles` | none | — | Yes — single action `index` reachable at `/logfiles` (or `/logfiles/index`); confirmed via navbar's `linkTo(controller="logfiles", text=...)` (no action= specified, defaults to `index`) |
| `permissions` | none | — | Yes — `index`/`edit`/`update` reachable at `/permissions/{action}[/key]`; confirmed via navbar `linkTo(controller="permissions", ...)` |
| `resources` | none | — | Yes — `add`/`create`/`edit`/`update`/`delete`/`checkavailability` reachable at `/resources/{action}[/key]`; confirmed via navbar `linkTo(controller="resources", ...)` |
| `settings` | none | — | Yes — `edit`/`update` reachable at `/settings/{action}/{key}`; confirmed via navbar `linkTo(controller="settings", ...)` and `views/settings/_form.cfm`'s `startFormTag(action="update", key=setting.id)` |
| `customfields` | none | — | Yes — `index`/`add`/`create`/`edit`/`update`/`delete`/`addtemplate`/`createtemplate`/`edittemplate`/`updatetemplate`/`deletetemplate`/`fieldpicker` reachable at `/customfields/{action}[/key]`; confirmed via navbar `linkTo(controller="Customfields", ...)` |
| `passwordresets` | none (the 4 explicit `/my/password*` routes above belong to `users`, NOT `passwordresets`) | — | Yes — `create`/`edit`/`update` reachable at `/passwordresets/{action}[/key]`; confirmed via `views/email/passwordreset.cfm`'s `linkto(controller="passwordResets", action="edit", key=...)` |
| `api` | none (the `allowAPI` permission-gated `index` action is reached via implicit convention, not an explicit route) | — | Yes — `index`/`rss2`/`ical`/`display` reachable at `/api/{action}` plus query-string params (`?token=`, `?location=`, `?today=`, `?maxrows=`); confirmed via every `linkTo(controller="api", action=..., params=...)` call in `views/api/index.cfm` |

**All 12 controllers confirmed reachable** — 2 (`Bookings` via the default route, `Eventdata` via its 2 dedicated AJAX-data routes) rely partly/fully on explicit named routes; the remaining 10 rely entirely on Wheels' implicit convention routing, with no explicit `addRoute()` declarations for them anywhere in `config/routes.cfm`. This is a lean, mostly-convention-driven routing setup — the 9 explicit routes exist only to provide human-friendly/REST-breaking URLs (login/logout, my-account self-service, AJAX data endpoints with path-segment params) where the default `/controller/action` shape would be awkward or where a conventional action name needed to be overridden (`login` → `new`).

**Route-level constraints not visible from controllers alone:** the `getEvents`/`getEvent` routes are the only ones using **path-segment** parameter patterns (`[type]`, `[key]`) rather than query-string or REST-style `/controller/action/key`; this is a deliberate departure for what are confirmed (from `controllers/Eventdata.cfc`'s own doc comments, not reproduced in full here) to be AJAX/calendar-data-fetching endpoints called frequently by the front-end calendar widget, where a terser URL shape matters.


---

## CF Application Lifecycle Events

**Source:** all 10 files under `events/`
(https://raw.githubusercontent.com/neokoenig/RoomBooking/master/events/)

### onApplicationStart
`events/onapplicationstart.cfm` — **the single most functionally significant lifecycle handler in the entire application.** On every application (re)start:
1. Wipes any existing `application.rbs` struct and rebuilds it from scratch as `{versionNumber="1.2", setting={}, permission={}, templates={}, modeltypes="event,location", templatetypes="form,output"}`.
2. Loads **every row** of the `settings` table into `application.rbs.setting['#id#'] = value` — this is the in-memory cache that the Settings controller's own success message warns about: **no settings change takes effect until this handler re-runs**, i.e. until the application is reloaded (via the `reloadPassword`-gated `?reload=production` URL mechanism confirmed in `config/settings.cfm`, or a server restart).
3. Loads the full `permissions` table, dynamically derives the role list (`rolelist`) by taking the permissions table's column list and stripping `ID`/`NOTES`, then builds `application.rbs.permission["#permission.id#"]["#role#"] = permission["#role#"]` for every permission × every role — this is the exact in-memory structure `checkPermission()` (in `events/functions.cfm`) reads at request time. **The set of valid roles is itself schema-derived** (whatever columns exist on the `permissions` table, confirmed as `admin`/`editor`/`user`/`guest` from the install SQL), not a hard-coded list — adding a new role column to the table would, per this code, automatically become a selectable role without any code change (though nothing in the UI reviewed in this plan exposes a way to add new role COLUMNS at runtime).
4. Loads the `templates` table (custom-field rendering templates) into `application.rbs.templates[parentmodel][type]`.
5. Registers two global shortcode callbacks (`field`, `output`) used by `processShortCodes()` — the mechanism `views/common/form/_customfields.cfm` depends on.

### onApplicationEnd
`events/onapplicationend.cfm` — **empty stub** (only a comment placeholder). No cleanup logic of any kind runs on application end.

### onSessionStart
`events/onsessionstart.cfm` — **empty stub**. No session-initialization logic beyond ColdFusion's own default session-scope creation.

### onSessionEnd
`events/onsessionend.cfm` — **empty stub**. No session-teardown logic; session expiry is purely framework-default (timeout-based), with no app-level cleanup (e.g., no explicit "mark user as logged out" bookkeeping on timeout).

### onRequestStart
`events/onrequeststart.cfm` — **runs on every single request**, and does real work (confirmed non-trivial, unlike most of the other handlers):
1. Initializes `request.cookie` struct and `request.cookie.username=""` as a default.
2. Checks for the `RBS_UN` ("remember me" username) cookie; if absent, sets a blank one (360-day expiry, `httponly`); if present, copies its value into `request.cookie.username` for the login form to pre-fill.
3. **Trims all `url` scope values**, and **trims all simple (non-complex) `form` scope values** — a blanket whitespace-trim applied to every incoming request parameter, framework-wide. This is a genuine functional behavior (not configuration) that affects every form submission and query-string param in the app — e.g., a booking title submitted with leading/trailing whitespace is silently trimmed before any controller logic sees it.
4. Sets `request.showNavBar="true"` as a per-request default (later consulted by `views/common/layout/_navbar.cfm`'s `<cfif request.showNavBar>` wrapper — meaning any action could in principle suppress the navbar by setting this to false before rendering, though no controller in the reviewed set does so).

### onRequestEnd
`events/onrequestend.cfm` — **empty stub**. No post-request cleanup logic.

### onError
`events/onerror.cfm` — static HTML-only handler (no `<cfscript>`/`<cfset>` logic at all): renders a generic "Error! Sorry, that caused an unexpected error. Please try again later." message. **No error logging, no email-on-error, no stack trace capture is wired into this handler** — contrary to what the base `config/settings.cfm`'s `excludeFromErrorEmail` setting (`"password,hashedpassword,passwordsalt,ssn"`) might imply (that setting exists to scrub sensitive fields FROM an error email, but no code in this handler — or anywhere else reviewed in this plan — actually SENDS an error email). This is a confirmed gap: the `excludeFromErrorEmail` setting is dead configuration with no corresponding send-logic found in the audited files (logged as an Open Question below).

### onMaintenance
`events/onmaintenance.cfm` — static HTML-only handler: renders "Maintenance! Sorry, maintenance work is being performed. Please try again later." **This is the direct, and only, evidence of maintenance-mode's user-facing behavior.** See the dedicated `### maintenance` subsection below for the full functional verdict — this handler by itself does not contain any user-role bypass logic, IP-allowlist check, or banner-only (non-blocking) mode; it is a full-page replacement with no visible mechanism in this file alone to let an admin through. (`config/maintenance/settings.cfm`'s own inline comment mentions an `ipExceptions` setting as an EXAMPLE value, not a confirmed shipped one — see below.)

### onMissingTemplate
`events/onmissingtemplate.cfm` — static HTML-only handler: renders "File Not Found! Sorry, the page you requested could not be found. Please verify the address." — a generic 404 page, no functional logic.

### functions.cfm (shared helper library, included by all of the above indirectly via framework wiring)
`events/functions.cfm` — not itself a lifecycle event, but the shared function library invoked throughout filters and other event handlers. Confirmed to contain: the entire permission-check engine (`checkPermission`, `checkPermissionAndRedirect`, `_permissionsSetup`, `_returnUserRole`), session/login helpers (`isLoggedIn`, `userIsInRole`, `_createUserInScope`, `signOut`, `redirectIfLoggedIn`, `getCurrentUser`), password salt/hash helpers (`getAuthKey`, `createSalt`, `decryptSalt`, `hashPassword` — all SHA-512-based, with the salt itself symmetrically encrypted using a per-installation auth key stored in `config/auth.cfm`, auto-generated as a UUID on first access if that file doesn't exist), the `denyInDemoMode` filter function referenced throughout multiple controllers, the two custom-field shortcode callbacks, `getIPAddress()` (reads the `x-forwarded-for` header, defaulting to `127.0.0.1` if absent — relevant to the Logfiles IP-capture finding above), and the entire logging subsystem (`addlogline`, `logFlash`) documented in the Logfiles section above.


---

## Per-Environment Settings Behavior

**Sources:** `config/app.cfm`, `config/environment.cfm`, `config/settings.cfm` (base), and all 5 `config/{env}/settings.cfm` files.

`config/app.cfm` is an **empty template stub** (only a comment showing the `this.name=` example) — no app-level `this.*` scope settings are actually configured. `config/environment.cfm` sets the single active environment via `<cfset set(environment="production")>` — confirming **production is the default/shipped environment** or whatever value is present at deploy time; the comment above it documents the 5 valid values as `"design"`, `"development"`, `"testing"`, `"maintenance"`, `"production"`.

The base `config/settings.cfm` sets framework-wide (environment-agnostic unless overridden) configuration: `dataSourceName="roombooking"`, `URLRewriting="off"`, `allowedEnvironmentSwitchThroughURL=true` (confirms environments CAN be switched live via a `?reload=production`-style URL, gated by `reloadPassword="roombooking"` — **the shipped default reload password is a plaintext, trivially-guessable string identical to the datasource name**, a real security concern worth flagging for the new system even though it's legacy-only), plus a large block of Bootstrap-3 form-helper styling defaults (`labelClass`, `prependToLabel`, etc.) that affect rendering CSS classes only, not business logic.

**Critical finding: all 5 per-environment settings files (`config/{development,production,testing,design,maintenance}/settings.cfm`) are, as shipped, 100% EMPTY** — each contains only its own templated comment block (e.g., *"This file is used to configure specific settings for the 'development' environment... Example: `<cfset set(dataSourceName="devDB")>`"*) with no actual `<cfset set(...)>` calls present in any of the 5 files. This means, **as shipped in this repository, there are ZERO configuration-VALUE differences between any of the 5 environments** — every environment runs with exactly the same `dataSourceName`, `URLRewriting`, `reloadPassword`, etc., inherited unmodified from the single base `config/settings.cfm`. Each environment subsection below documents (a) what the file's own template comment suggests an operator COULD configure, and (b) the separate, more important question of whether FUNCTIONAL (code-path) behavior differs by environment regardless of configuration values — which, per the findings above, is driven entirely by `application.wheels.environment` checks in application code, not by anything in these per-environment settings files.

### development
- **Value differences from base `config/settings.cfm`:** None (file is empty as shipped).
- **Functional difference verdict:** **NONE FOUND.** No application code reviewed in this plan (controllers, views, lifecycle events) contains a conditional branch keyed specifically on `environment EQ "development"`. The only environment-keyed functional branch found anywhere (`views/common/layout/_footer.cfm`'s JS-bundle selection) checks for `"production"` specifically, with every other environment (including development) falling into the same "unminified" else-branch as testing/design/maintenance.

### production
- **Value differences from base `config/settings.cfm`:** None (file is empty as shipped); the file's own example comment suggests an operator would typically set `errorEmailAddress` here, but no such setting is actually configured, and — as confirmed in the `onError` handler finding above — no code path was found anywhere in this plan's reviewed files that actually sends an error email even if `errorEmailAddress` were set. This setting (and its implied feature) appears to be a **dead/unimplemented example** in this codebase.
- **Functional difference verdict:** **YES — the only confirmed functional (not config-value) difference tied to environment in the entire audited codebase.** `views/common/layout/_footer.cfm`: `<cfif application.wheels.environment EQ "production">#javascriptIncludeTag("rbs.min")#<cfelse>#javascriptIncludeTag("rbs")#</cfif>` — production serves the minified JS bundle (`rbs.min.js`); every other environment serves the unminified development bundle (`rbs.js`). This is a genuine code-path difference (different file served, with potential debuggability/performance implications), not merely a config value.

### testing
- **Value differences from base `config/settings.cfm`:** None (file is empty as shipped); the template's own example suggests `cacheQueries=false` as a typical testing override, but this is NOT actually set.
- **Functional difference verdict:** **NONE FOUND.** No code path in the reviewed controllers/views/events checks for `environment EQ "testing"` specifically. In particular — directly addressing the PRD's suggested hypothesis — **no evidence was found that the testing environment disables email sending**; `sendEmail()` calls in `PasswordResets.cfc` and (implied) booking notification flows contain no environment conditional guarding them. As shipped, a "testing" environment would send real emails identically to production, aside from the JS-bundle difference shared with every non-production environment.

### design
- **Value differences from base `config/settings.cfm`:** None (file is empty as shipped); the template's own example suggests a `dataSourceName="devDB"` override (identical example text to the development file), implying design mode was originally intended to point at a separate/throwaway datasource for visual-design work — but this is NOT actually configured.
- **Functional difference verdict:** **NONE FOUND.** No code path checks for `environment EQ "design"` specifically. Contrary to what the name might suggest, "design" mode does **not** alter rendering behavior, enable a style-guide view, or change any template-selection logic anywhere in the reviewed code — it behaves identically to development/testing (non-minified JS bundle, otherwise no differences).

### maintenance
- **Value differences from base `config/settings.cfm`:** None (file is empty as shipped); the template's own example comment suggests `ipExceptions="an.ip.num.ber"` as a typical maintenance-mode override — **this is the one per-environment example that, if actually implemented in framework code, would represent a genuine IP-allowlist bypass mechanism for maintenance mode** — but as shipped, this setting is NOT configured, and more importantly, **no code anywhere in this plan's reviewed files (controllers, events, functions.cfm) reads or checks an `ipExceptions` setting at all.** It is purely an unconfigured, unimplemented-in-this-codebase example in the template comment.
- **Functional difference verdict — THIS IS THE MOST FUNCTIONALLY SIGNIFICANT ENVIRONMENT, confirmed by direct citation of `events/onmaintenance.cfm`:** When `application.wheels.environment EQ "maintenance"`, the Wheels framework itself (not application code reviewed in this plan, but standard Wheels framework behavior triggered by this exact environment value) invokes `events/onmaintenance.cfm` **in place of normal request processing for every request** — rendering the static "Maintenance! Sorry, maintenance work is being performed. Please try again later." page. Based on the content of `onmaintenance.cfm` alone (a bare static HTML fragment with zero conditional logic, zero role-check, zero IP-check), **this is confirmed as a full, unconditional lockout for ALL users including admins** — there is no visible bypass mechanism in the audited files. This directly resolves PRD Open Question #10's maintenance-mode sub-question: maintenance mode is **read-only/fully-locked-out by default** (not a banner-only or admin-bypass mode), though the unconfigured `ipExceptions` template-comment hint suggests the ORIGINAL framework author may have intended (or the operator was expected to separately implement) an IP-allowlist bypass that is **not present in this specific application's codebase** as audited. **New-system designers should treat "maintenance mode = full outage page, no exceptions" as the confirmed legacy behavior**, and treat any IP-allowlist/admin-bypass capability as a new feature decision, not a parity requirement.

