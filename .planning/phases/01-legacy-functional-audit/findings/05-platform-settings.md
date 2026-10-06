# Platform Audit — Settings, Api, Logfiles (Controllers), Settings (Model), Routes, Lifecycle Events, Per-Environment Config

## Settings controller and model

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

## Api controller (public feeds)

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

## Logfiles controller

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

## Open Questions (platform-settings)

**PRD Open Question #7 (feed access gating) — RESOLVED, with correction.** `Api.cfc`'s 3 data-serving actions (`rss2`/`ical`/`display`) are gated by **per-user API token possession** (`f_isValidAPIRequest`, token >25 chars matching a `users.apitoken` row), NOT by the `allowAPI` permission flag (that flag only gates the human-browsed `index` listing page, and — per the seeded default data — is granted to every role anyway, making it a non-restrictive no-op in practice). The PROJECT.md interim decision "fully public, no allowAPI gate" is corrected to: **"not role/permission-gated, but IS token-gated — any valid API token (regardless of the token-holder's current permissions) grants access to all approved-status events, optionally filtered by location."** See full citation and the `views/api/index.cfm` token-provisioning UX in the `## Api controller` section above.

**PRD Open Question #8 (audit log field completeness) — RESOLVED, definitively NO before/after capture.** `logfiles` schema has only `message` (varchar 500) and `data` (text) free-text fields — no structured before/after value pair exists anywhere in the schema or the `addlogline()`/`model("logfile").create()` write path. Additionally, **write-side coverage is a confirmed SUBSET, not "every controller action"**: deliberate `addlogline` calls exist only in `Sessions.cfc` (login attempts) and `PasswordResets.cfc` (reset completion, with a confirmed likely bug — `success=` param instead of `message=`, silently dropped), plus a shared `Cookie`-type call from `functions.cfm`'s remember-me helpers. The global `logFlash` after-filter (on the base `Controller.cfc`, inherited by all 12 controllers) opportunistically logs ANY flash `error`/`success` message as a side effect — this is the ONLY mechanism giving business actions like booking approve/deny or user edits even incidental log coverage, and only when that specific action happens to set a flash message. **Booking approve/deny, user CRUD, permission changes, location/resource CRUD have no deliberate/dedicated audit-log call** — see full citation list in `## Logfiles controller` above. An unresolved sub-question remains OPEN: the write-site for the `"ajax"` log type (present in the controller's hard-coded type-filter list) was not located within this plan's confirmed file set — it may live in a model file or a controller filter not covered by this plan's file list, and should be confirmed by whichever phase implements the new audit-logging feature (F11) before assuming `ajax`-type logging is fully understood.

**PRD Open Question #10 (per-environment functional differences) — RESOLVED. Overall verdict: functional differences between environments are minimal and almost entirely confined to ONE behavior.** As shipped, all 5 per-environment settings files are empty (zero config-VALUE differences). Exactly ONE functional code-path difference was found anywhere in the audited codebase, and it applies only to `production` vs. everything-else (not a unique behavior per environment): production serves a minified JS bundle, every other environment (development/testing/design/maintenance) serves the unminified bundle. The ONE major exception/outlier is **maintenance mode**, which — via Wheels' own framework-level environment dispatch combined with the fully-static `events/onmaintenance.cfm` handler — produces a confirmed full, unconditional, no-bypass lockout page for every request, for every user including admins. Development/testing/design are functionally **indistinguishable from one another** in this codebase (all three share the "non-production, non-maintenance" code path with no further differentiation) — contrary to any assumption that "testing disables email" or "design changes rendering," neither hypothesis is supported by the audited code. New-system implementers should treat environment-specific behavior as effectively binary in the legacy system: **(production vs. not) for the JS-bundle optimization, and (maintenance vs. not) for the full-lockout page** — any finer-grained per-environment behavior in the new system (e.g., a testing environment that suppresses outbound email) would be a **new capability**, not a parity requirement, and should be scoped as such if desired.

**Additional open item surfaced during this audit (not a pre-existing PRD question, logged here for completeness):** `config/settings.cfm`'s `excludeFromErrorEmail` setting (`"password,hashedpassword,passwordsalt,ssn"`) and `config/production/settings.cfm`'s template-comment-suggested `errorEmailAddress` setting both imply an error-notification-email feature that **has no corresponding send-logic anywhere in the files reviewed by this plan** (`onerror.cfm` is static HTML only). Before the new system's error-handling/observability design (likely Phase 2 or later) assumes "legacy sent error emails to admins," this should be explicitly confirmed as either (a) genuinely unimplemented dead configuration in this codebase, or (b) implemented in a file outside this plan's scope (e.g., a plugin or a model-layer error handler not covered here) — flagged rather than assumed, per this plan's own citation discipline.

