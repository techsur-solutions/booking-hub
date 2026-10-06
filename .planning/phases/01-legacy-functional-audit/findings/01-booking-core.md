# Booking Core Audit — Bookings, Eventdata (Controllers)

**Source:** https://github.com/neokoenig/RoomBooking (branch `master`), fetched directly via
`raw.githubusercontent.com` at audit time. All citations below reference the file paths as they
exist in that repository. Base controller `controllers/Controller.cfc` (extends CFWheels' own
`Wheels` controller) was also reviewed because it supplies filters inherited by every controller
in the app, including Bookings and Eventdata.

**Base permission gate (applies to every controller, including Bookings/Eventdata):**
`filters(through="checkPermissionAndRedirect", permission="accessapplication")` — deny-everything-
by-default login gate — plus `filters(through="logFlash", type="after")` for flash-message cleanup
(controllers/Controller.cfc: init).

---

## Bookings controller

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

## Eventdata controller

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

## Event model

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

## Eventresource model

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

## Open Questions (booking-core)

1. **PRD #1 — Recurring booking edit/delete scoping.**
   **RESOLVED.** Legacy RoomBooking has **no concept of a booking series after creation**.
   `Bookings.create()`'s repeat-loop (`params.repeat`/`params.repeatno`) creates fully independent
   sibling `Event` rows with no stored linking identifier (`Event.cfc`'s `registerSystemFields()`
   field list has no `seriesid`/`recurrenceid`/`groupid`-equivalent field). `update()` and
   `delete()` both operate unconditionally on the single row identified by `params.key`, with no
   series-aware branching anywhere in `Bookings.cfc`. Conclusion: there is no "edit/delete this
   occurrence vs. the whole series" choice in the legacy system because there is no other mode to
   select — editing/deleting is always single-occurrence, and "recurring" is purely a bulk-create-
   time convenience. Cite: `controllers/Bookings.cfc: create()` (repeat loop), `update()`,
   `delete()`; `models/Event.cfc: registerSystemFields()`.

2. **PRD #2 — Conflict hard-block vs. soft-warning (PROJECT.md interim decision).**
   **OPEN — PROJECT.md's interim decision is corrected by the code, not confirmed.** The premise (a
   permission-conditional split: hard block without `allowApproveBooking`, soft warning with it)
   does not exist anywhere in the cited code. There is exactly **one** conflict-detection mechanism
   — `Bookings.check()`, an AJAX endpoint rendering `views/bookings/check.cfm` — and it is
   **unconditionally non-blocking** (an informational alert only) for every user regardless of
   `allowApproveBooking` or any other permission. Neither `create()` nor `update()` perform **any**
   server-side conflict validation; a clashing booking saves successfully in all cases. Recommend the
   consolidation pass correct PROJECT.md's Key Decisions entry to reflect: "Legacy conflict
   enforcement is always non-blocking/informational (AJAX-only, `Bookings.cfc: check()`); no hard
   block exists at any permission level in the legacy system. Any hard-block behavior in the new
   system (F2) is new design, not legacy parity." Separately, the `check()` query's overlap test
   itself is incomplete (only tests whether an existing booking's span contains the **new**
   booking's **start** instant, never cross-checking the new booking's end) — a confirmed code gap,
   cited above under `Bookings.cfc: check()`, relevant to how F2 should actually detect overlaps
   (completely, unlike the legacy query) even though the non-blocking **enforcement** behavior is
   what should carry forward as "parity" if that remains the intended default.

3. **PRD #3 — Multi-resource conflict scope.**
   **OPEN — unknown because the resource-level enforcement code is outside this plan's declared file
   set.** The event/location-level `check()` endpoint audited above does not consider resources at
   all. A **separate** AJAX mechanism exists for resource-level availability — referenced in
   `views/bookings/_form.cfm` as a call to `controller="resources", action="checkavailability"`,
   triggered only for resources flagged `isunique` (a field read from the `resources` query, i.e.
   the `Resource` model) — but `controllers/Resources.cfc` and the `Resource` model were not in this
   plan's fetch list (`Bookings`, `Eventdata`, `Event`, `Eventresource` only), so the actual
   enforcement logic/query behind that endpoint is unconfirmed here. Recommend a follow-up audit
   task against `controllers/Resources.cfc` (and the `Resource` model) before F2/F3 finalize
   multi-resource conflict behavior.

4. **PRD #4 — Auto-approve/notification interaction.**
   **RESOLVED.** `Bookings.create()`'s auto-approve mechanism (the `bypassApproveBooking`-gated
   status override) and its notification trigger (`notifyContact()`) are **independent** code paths:
   auto-approve only flips `event.status` to `"approved"` after the initial save; `notifyContact()`
   fires separately, gated solely by the user-controlled `params.event.emailContact` checkbox
   (`views/bookings/tabs/_contact.cfm`, shown only on `add`/`create`) — not by the resulting approval
   status. A bypass-auto-approved booking and a still-pending booking therefore receive identical
   notification *triggering* behavior; only the `#event.status#` token interpolated into the email
   subject differs (content itself is F8's concern, out of this plan's scope). Cite:
   `controllers/Bookings.cfc: create(), notifyContact()`; `views/bookings/tabs/_contact.cfm`.

5. **PRD #5 — Notification recipient rules (call sites only).**
   **RESOLVED (call-site scope, per this plan's framing).** Three call sites for `notifyContact()`:
   (a) `create()` — conditional on the `emailContact` checkbox; (b) `approve()` — unconditional; (c)
   `deny()` — unconditional (independent of the original `emailContact` choice — i.e. approve/deny
   always attempt to notify even if the creator never opted in at booking time). Recipient is always
   the event's own `contactname`/`contactemail` fields (never the acting/current user), with
   optional BCC to `application.rbs.setting.bccAllEmailTo` when
   `application.rbs.setting.bccAllEmail` is enabled. The call silently no-ops if `contactemail` fails
   `isValid("email", ...)` or if `application.rbs.setting.isDemoMode` is true. Cite:
   `controllers/Bookings.cfc: notifyContact(), approve(), deny(), create()`.

**Additional limitation noted (not one of PRD #1–#5, but load-bearing for F1):** both `Event.cfc`
and `Eventresource.cfc` are reflection-based CFWheels models with no explicit `property()`
declarations — the **complete** DB column list for either table (beyond what `registerSystemFields()`
documents as the Event model's built-in form fields, and the two `belongsTo` associations on
`Eventresource`) is unknown because no schema/migration file was in this plan's declared fetch list.
Recommend confirming the live DB schema (or a migration/DDL file, if one exists in the repo) before
finalizing the new system's `Event`/`Eventresource`-equivalent table definitions.

**Corroboration attempts (best-effort, per plan instructions):**
- Legacy docs (`https://roombooking.readme.io`) — reachable (HTTP 200) at audit time; not fetched in
  depth beyond the reachability check, as the plan marks this corroboration as best-effort and
  non-blocking, and the primary source code citations above are already fully sufficient to answer
  every required finding.
- Legacy demo (`https://roombooking.oxalto.co.uk`) — returned **HTTP 401** at audit time, as
  anticipated in the plan. Cannot corroborate via demo — site requires credentials this plan was not
  given. Logged here rather than silently skipped, per plan instructions.
