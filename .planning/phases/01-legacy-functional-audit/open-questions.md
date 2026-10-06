# Open Questions — Legacy Functional Audit (F0)

Source: consolidated from findings/01 through findings/05 (Wave 1), per
`legacy-audit-findings.md`'s preamble. Each item is **RESOLVED** (closed with
cited legacy-code evidence) or **OPEN** (requires a product decision — see
`PROJECT.md` Key Decisions for any that have since been formally decided).
Items 1–10 correspond exactly, in order, to PRD-BookingHub.md Section 10's
seed Open Questions. Items 11+ are additional questions Wave 1 discovered
during the audit that were not part of the original 10.

---

## 1. Recurring booking edit/delete scoping

**Status:** RESOLVED

**Finding:** Legacy RoomBooking has no concept of a booking series after
creation. `Bookings.create()`'s repeat-loop (`params.repeat`/`params.repeatno`)
creates fully independent sibling `Event` rows with no stored linking
identifier — `Event.cfc`'s `registerSystemFields()` field list has no
`seriesid`/`recurrenceid`/`groupid`-equivalent field. `update()` and
`delete()` both operate unconditionally on the single row identified by
`params.key`, with no series-aware branching anywhere in `Bookings.cfc`.
There is no "edit/delete this occurrence vs. the whole series" choice in the
legacy system because there is no other mode to select — editing/deleting is
always single-occurrence, and "recurring" is purely a bulk-create-time
convenience.

**Citation:** `controllers/Bookings.cfc: create()` (repeat loop), `update()`,
`delete()`; `models/Event.cfc: registerSystemFields()`. See
findings/01-booking-core.md, Open Questions item 1.

---

## 2. Conflict detection enforcement hard-block-vs-soft-warning

**Status:** OPEN — PROJECT.md's interim decision is corrected by the code, not confirmed

**Finding:** The premise of the interim decision (a permission-conditional
split — hard block without `allowApproveBooking`, soft warning with it) does
not exist anywhere in the cited code. There is exactly one
conflict-detection mechanism — `Bookings.check()`, an AJAX endpoint rendering
`views/bookings/check.cfm` — and it is unconditionally non-blocking (an
informational alert only) for every user regardless of `allowApproveBooking`
or any other permission. Neither `create()` nor `update()` perform any
server-side conflict validation; a clashing booking saves successfully in all
cases. Separately, the `check()` query's own overlap test is incomplete
(only tests whether an existing booking's span contains the new booking's
start instant, never cross-checking the new booking's end) — a confirmed
code gap, not an open question, relevant to how F2 should actually detect
overlaps completely (not "parity" with this specific query, but consistent
with the evident intent of the feature).

**Unknown because:** Whether the new system should hard-block for all users
(a genuinely new design decision, not legacy parity) or replicate the
legacy's always-non-blocking behavior is a product decision this planning
agent is not authorized to make. See PROJECT.md Key Decisions row
"Conflict enforcement interim policy."

**Citation:** `controllers/Bookings.cfc: check(), create(), update()`. See
findings/01-booking-core.md, Open Questions item 2.

---

## 3. Multi-resource conflict scope

**Status:** OPEN — unknown because the resource-level enforcement code is outside Wave 1's declared file set

**Finding:** The event/location-level `check()` endpoint does not consider
resources at all. A separate AJAX mechanism exists for resource-level
availability (`controllers/Resources.cfc::checkavailability`), triggered only
for resources flagged `isunique`, but the actual enforcement
logic/query behind whether a multi-resource booking is blocked by any single
resource conflict, or whether resources are checked fully independently with
results merged, was only partially confirmed — `Resources.cfc::checkavailability`
itself was reviewed by findings/02-reference-data.md and queries per-resource-id
independently, returning a single 0/1 availability flag per call. Whether the
booking UI aggregates multiple per-resource calls into a single
blocking/non-blocking verdict for the whole booking, or evaluates them with
any cross-resource logic, was not confirmed from the Bookings controller's
own file content in isolation.

**Unknown because:** This question sits at the intersection of
`Bookings.cfc` (not shown to call `checkavailability` directly in its own
controller body) and `Resources.cfc`/the client-side JS in
`views/bookings/_form.cfm` (which does wire a per-resource, `data-unique`-gated
AJAX call). The exact aggregation/blocking semantics for a booking with
multiple attached resources, when one resource conflicts and others do not,
remains unconfirmed from source alone.

**Citation:** `controllers/Resources.cfc: checkavailability()`;
`views/bookings/_form.cfm` (resource-level AJAX wiring);
`views/bookings/tabs/_resources.cfm`. See findings/01-booking-core.md, Open
Questions item 3, and findings/02-reference-data.md, Api/Resources sections.

---

## 4. Auto-approve interaction with notifications

**Status:** RESOLVED

**Finding:** `Bookings.create()`'s auto-approve mechanism (the
`bypassApproveBooking`-gated status override) and its notification trigger
(`notifyContact()`) are independent code paths: auto-approve only flips
`event.status` to `"approved"` after the initial save; `notifyContact()`
fires separately, gated solely by the user-controlled
`params.event.emailContact` checkbox — not by the resulting approval status.
A bypass-auto-approved booking and a still-pending booking therefore receive
identical notification *triggering* behavior; only the `#event.status#` token
interpolated into the email subject differs.

**Citation:** `controllers/Bookings.cfc: create(), notifyContact()`;
`views/bookings/tabs/_contact.cfm`. See findings/01-booking-core.md, Open
Questions item 4.

---

## 5. Notification recipient rules

**Status:** RESOLVED (call-site scope, per Wave 1's framing)

**Finding:** Three call sites for `notifyContact()`: (a) `create()` —
conditional on the `emailContact` checkbox; (b) `approve()` — unconditional;
(c) `deny()` — unconditional (independent of the original `emailContact`
choice). Recipient is always the event's own `contactname`/`contactemail`
fields (never the acting/current user), with optional BCC to
`application.rbs.setting.bccAllEmailTo` when `application.rbs.setting.bccAllEmail`
is enabled. The call silently no-ops if `contactemail` fails
`isValid("email", ...)` or if `application.rbs.setting.isDemoMode` is true.
Cross-referencing findings/05-platform-settings.md: `bookingNotify.cfm`'s
actual `to=`/`sendEmail()` call site was not located within Wave 1's file
set, but the recipient-resolution logic (contact fields on the event, not
the acting user) is independently confirmed by the template's own rendered
placeholders.

**Citation:** `controllers/Bookings.cfc: notifyContact(), approve(), deny(),
create()`. See findings/01-booking-core.md, Open Questions item 5, and
findings/05-platform-settings.md's Email notification templates subsection.

---

## 6. Custom field validation rules

**Status:** RESOLVED

**Finding:** Exactly five field types exist: `textfield`, `select`,
`textarea`, `radio`, `checkbox` — confirmed twice independently (admin
form's `<select>` options and the render-time `cfswitch` in
`views/shortcodes/field.cfm`). The `options[]` JSON shape for choice-based
types is **not enforced**: no DB constraint, no model validation, no
server-side JSON-shape check anywhere audited — only client-side UI
guidance. The join/context model is two-tier: `Customfield.parentmodel`
scopes a field *definition* to a model type (`event` or `location`,
UI-only enum, applies to every instance of that type identically); a
`Customfieldjoin.customfieldchildid` row scopes one stored *value* to one
specific instance. `Customfield.required` is **not enforced server-side** —
it only ever reaches an HTML5 `required` attribute on the rendered input;
the actual save path (`Controller.updateCustomFields()`) unconditionally
creates/updates a value for whatever was submitted, with no check of
`required`, `type`, or value shape. Deleting a `Customfield` definition
orphans (does not purge) its historical `Customfieldjoin`/`Customfieldvalue`
rows — no cascade delete is configured and no DB foreign keys exist to
enforce one.

**Citation:** `models/Customfield.cfc`; `controllers/Controller.cfc:
getCustomFields(), getBlankCustomFields(), updateCustomFields()`;
`views/customfields/_form.cfm`; `views/shortcodes/field.cfm`. See
findings/03-custom-fields.md, Open Questions items 1–5 (all five sub-items
feed this single PRD seed question).

---

## 7. Feed access control / allowAPI gating

**Status:** RESOLVED, with correction

**Finding:** `Api.cfc`'s 3 data-serving actions (`rss2`/`ical`/`display`) are
gated by per-user API token possession (`f_isValidAPIRequest`, token >25
chars matching a `users.apitoken` row), NOT by the `allowAPI` permission
flag — that flag only gates the human-browsed `index` listing page, and (per
the seeded default data) is granted to every role anyway, making it a
non-restrictive no-op in practice. The PROJECT.md interim decision "fully
public, no allowAPI gate" is corrected to: not role/permission-gated, but IS
token-gated — any valid API token (regardless of the token-holder's current
permissions) grants access to all `status='approved'` events, optionally
filtered by location. Per-location filtering applies uniformly across all 3
data-serving actions (`rss2`, `ical`, `display` all accept the same
`params.location` parameter).

**Citation:** `controllers/Api.cfc: init(), f_isValidAPIRequest()`;
`views/api/index.cfm` (token-provisioning UX). See
findings/05-platform-settings.md, Open Questions, PRD Open Question #7
subsection.

---

## 8. Audit log field completeness

**Status:** RESOLVED, definitively NO before/after capture

**Finding:** `logfiles` schema has only `message` (varchar 500) and `data`
(text) free-text fields — no structured before/after value pair exists
anywhere in the schema or the `addlogline()`/`model("logfile").create()`
write path. Write-side coverage is a confirmed SUBSET, not "every controller
action": deliberate `addlogline` calls exist only in `Sessions.cfc` (login
attempts) and `PasswordResets.cfc` (reset completion, with a confirmed
likely bug — `success=` param instead of `message=`, silently dropped),
plus a shared `Cookie`-type call from `functions.cfm`'s remember-me helpers.
The global `logFlash` after-filter on the base `Controller.cfc` (inherited
by all 12 controllers) opportunistically logs any flash error/success
message as a side effect — this is the ONLY mechanism giving business
actions like booking approve/deny or user edits even incidental log
coverage, and only when that specific action happens to set a flash
message. Booking approve/deny, user CRUD, permission changes, and
location/resource CRUD have no deliberate/dedicated audit-log call.

**Citation:** `install/new-installation.sql: CREATE TABLE logfiles`;
`events/functions.cfm: addlogline()`; `controllers/Controller.cfc:
logFlash`. See findings/05-platform-settings.md, Open Questions, PRD Open
Question #8 subsection.

---

## 9. Permission matrix completeness

**Status:** RESOLVED — NOT exhaustive, as PRD/PROJECT.md already suspected

**Finding:** The legacy system defines 17 permission flags (per the
`permissions` table seed data), of which 15 are actively enforced somewhere
in the controller/view code (the 6 PRD-named flags plus 9 more:
`accessApplication`, `accessCustomFields`, `accessLocations`,
`accessLogfiles`, `accessResources`, `accessSettings`, `accessUsers`,
`updateOwnAccount`, `bypassApproveBooking`), and 2 are defined but entirely
unreferenced/dead (`allowiCal`, `allowRSS` — both explicitly labelled
"Reserved for future use" in their own seed data).

**Citation:** Full cross-controller scan of all 12 controllers against
`install/new-installation.sql`'s permission seed data. See
findings/04-identity-access.md, Permission Flag Inventory section and Open
Questions item 1.

---

## 10. Per-environment settings behavior

**Status:** RESOLVED

**Finding:** As shipped, all 5 per-environment settings files
(`config/{development,production,testing,design,maintenance}/settings.cfm`)
are 100% empty — zero configuration-value differences between any of the 5
environments. Exactly ONE functional code-path difference was found: production
serves a minified JS bundle (`rbs.min.js`), every other environment serves the
unminified bundle (`rbs.js`). Maintenance mode is the one major functional
outlier — Wheels' own framework-level environment dispatch, combined with the
fully-static `events/onmaintenance.cfm` handler, produces a confirmed full,
unconditional, no-bypass lockout page for every request, for every user
including admins. Development/testing/design are functionally
indistinguishable from one another and from each other (all three share the
"non-production, non-maintenance" code path with no further differentiation) —
neither "testing disables email" nor "design changes rendering" is supported
by the audited code.

**Citation:** `config/environment.cfm`; `config/{env}/settings.cfm` (all 5,
confirmed empty); `views/common/layout/_footer.cfm` (JS-bundle conditional);
`events/onmaintenance.cfm`. See findings/05-platform-settings.md, Per-Environment
Settings Behavior section and Open Questions, PRD Open Question #10 subsection.

---

## 11. Exact legacy Resource field set beyond `name`

**Status:** RESOLVED

**Finding:** Full field set confirmed: `name` (required), `type` (optional
grouping/category, admin-configurable option list), `description`
(optional), `isunique` (boolean, default false, governs per-resource
double-booking restriction semantics), `restrictlocations` (optional
location-restriction list).

**Citation:** `models/Resource.cfc`; `install/new-installation.sql: CREATE
TABLE resources`; `views/resources/_form.cfm`. See
findings/02-reference-data.md, Open Questions item 1.

---

## 12. Exact shape/purpose of the legacy `layout` metadata field on Location

**Status:** RESOLVED

**Finding:** The real field name is `layouts` (plural, not singular `layout`
as FRD assumed), a free-text field (not an enum/structured type), optional,
intended to hold a human-typed comma-separated list of layout style names.
It is purely descriptive/informational — there is no code anywhere that
parses, validates, or structurally interprets this string. It is
programmatically disconnected from `Event.layoutstyle`, which is driven by a
separate global `application.rbs.setting.roomlayouttypes` options list.

**Citation:** `models/Location.cfc: registerSystemFields()`;
`install/new-installation.sql`; `views/locations/_form.cfm`;
`models/Event.cfc`. See findings/02-reference-data.md, Open Questions item 2.

---

## 13. Whether `name` uniqueness is enforced on Location/Resource

**Status:** RESOLVED — confirmed NOT enforced

**Finding:** Neither at the model level (no `validatesUniquenessOf` call in
either `models/Location.cfc` or `models/Resource.cfc`, and Wheels' automatic
validation generator never synthesizes a uniqueness check) nor at the
database level (no `UNIQUE` constraint or index beyond each table's PK) is
`name` uniqueness enforced for Location or Resource.

**Citation:** `models/Location.cfc`; `models/Resource.cfc`; `wheels/model/initialization.cfm`
(automatic-validation generator); `install/new-installation.sql`. See
findings/02-reference-data.md, Open Questions item 3.

---

## 14. Whether deleting a Location/Resource referenced by existing bookings is blocked, cascades, or allows orphaned references

**Status:** OPEN — resolved as fact, but the forward-looking decision is a product/architecture choice escalated to F4

**Finding:** Confirmed, contradicting the FRD's interim "soft-delete only,
references remain intact" assumption: both Location and Resource deletes are
TRUE HARD SQL `DELETE`s (neither table has a `deletedAt`-equivalent column),
with no cascade (`hasMany` associations declare no `dependent` option) and no
database-level `FOREIGN KEY` constraint anywhere in the schema. Deleting a
Location/Resource referenced by existing Bookings/Eventresources succeeds
unconditionally (modulo Locations' unrelated "last-one-standing" count
check) and leaves orphaned `events.locationid`/`eventresources.resourceid`
values pointing at a no-longer-existent row.

**Unknown because:** Whether the new system (F4) should preserve this
orphan-allow behavior (legacy equivalence) or introduce a blocking/cascade
guard (a deliberate improvement) is a product/architecture decision this
planning agent is not authorized to make — escalated to F4 planning per the
Wave 1 finding's own recommendation.

**Citation:** `controllers/Locations.cfc: delete()`; `controllers/Resources.cfc:
delete()`; `models/Location.cfc`; `models/Resource.cfc`;
`install/new-installation.sql` (no FK constraints anywhere in 276 lines). See
findings/02-reference-data.md, Open Questions item 4.

---

## 15. Why `checkavailability` is gated behind the admin-only `accessresources` permission

**Status:** OPEN — logged as a design-smell rather than a functional ambiguity

**Finding:** `controllers/Resources.cfc` applies
`checkPermissionAndRedirect(permission="accessresources")` (admin-only per
seed data) to every action including `checkavailability`, with no
`except=` carve-out. If this AJAX endpoint is meant to support the general
booking UI's live availability check for any logged-in user creating a
booking, this permission gate would make it unreachable for non-admin users
— either a functional bug in the legacy system itself, or resource-level
availability-checking in the booking UI is itself an admin-only/unused
feature in practice.

**Unknown because:** Confirming whether the Bookings/Event-creation UI
actually calls this endpoint for non-admin users (and if so, whether it
fails silently or is blocked) requires cross-referencing the Bookings
controller's JS/view layer in more depth than Wave 1's declared file set
covered.

**Citation:** `controllers/Resources.cfc: init(), checkavailability()`. See
findings/02-reference-data.md, Open Questions item 5.

---

## 16. Initial-credential mechanism

**Status:** RESOLVED

**Finding:** Admin sets the password directly (plaintext, typed twice for
confirmation) at account-creation time via `views/users/formparts/_userpw.cfm`
("Initial Password" panel) and `Users.create()`. No auto-generated password,
no emailed activation/invite link. The one-time installer follows the
identical pattern for the very first admin account.

**Citation:** `controllers/Users.cfc: create()`;
`views/users/formparts/_userpw.cfm`; `install/functions.cfm:
createInitialAdminUser()`. See findings/04-identity-access.md, Open
Questions item 2.

---

## 17. Underlying web-session timeout duration

**Status:** OPEN — unknown because no override exists in this codebase; depends on the ColdFusion/Lucee engine default

**Finding:** The "remember me" cookie (`RBS_UN`, 360-day expiry) is resolved
(item 16's sibling finding) as a login-form username-prefill convenience
only — not a session-extension mechanism. No session-timeout override (e.g.
`this.sessionTimeout` / `cfapplication sessionTimeout`) was found anywhere in
`Application.cfc`, `config/app.cfm`, `config/environment.cfm`,
`config/settings.cfm`, or any per-environment settings file.

**Unknown because:** The actual session-expiry duration is an engine/server-level
default outside this codebase's own configuration files, and cannot be
confirmed from source alone — it must be confirmed against the actual
historical deployment's engine-admin configuration (or explicitly decided
fresh for the new system) before Phase 3 finalizes session-timeout behavior.

**Citation:** `events/functions.cfm: setCookieRememberUsername()`;
exhaustive grep of `Application.cfc`/`config/*.cfm` for session-timeout
overrides (none found). See findings/04-identity-access.md, Open Questions
item 3.

---

## 18. Permission-change caching requires an application reload

**Status:** OPEN — logged as a decision point for Phase 3, not a legacy-behavior ambiguity

**Finding:** Edits via the Permissions controller update the database
immediately but have no runtime effect until the application restarts (the
in-memory `application.rbs.permission` struct is populated once at
`onApplicationStart`).

**Unknown because:** This is resolved as a confirmed legacy fact, but Phase 3
must make a conscious decision on whether the new system should (a) apply
permission changes immediately (recommended), or (b) intentionally require a
cache-refresh step. Carrying the legacy caching quirk forward silently would
be a worse default than a deliberate choice either way.

**Citation:** `controllers/Permissions.cfc: update()`;
`events/onapplicationstart.cfm`; `events/functions.cfm: checkPermission()`.
See findings/04-identity-access.md, Open Questions item 5.

---

## 19. Password-reset token expiry is not re-checked at final submit

**Status:** OPEN — a confirmed legacy timing gap, recommend the new system close it

**Finding:** `PasswordResets.edit()` checks the 2-hour expiry window when the
reset form is first loaded; `PasswordResets.update()` does not re-check it
when the new password is actually submitted — a user who opens the reset
link just inside the window could submit a new password well after expiry.

**Unknown because:** This is a confirmed code gap, not an ambiguity — but
whether the new system should replicate this gap (legacy parity) or close it
(recommended, re-validate expiry on both read and write) is a design
decision for F6, not a silently-assumed parity requirement.

**Citation:** `controllers/PasswordResets.cfc: edit(), update()`. See
findings/04-identity-access.md, Open Questions item 6.

---

## 20. Email-enumeration inconsistency between login and password-reset-request

**Status:** OPEN — a design decision for the new system, not an oversight to silently carry forward

**Finding:** `Sessions.attemptlogin()` returns a generic failure message
regardless of whether the email exists; `PasswordResets.create()` explicitly
reveals "we couldn't find an account for that address" when it doesn't —
an inconsistent policy between the two flows.

**Unknown because:** Whether the new system should pick one consistent
policy (generic messaging is the more conservative default) is a product
decision, not a legacy-parity question with one right answer.

**Citation:** `controllers/Sessions.cfc: attemptlogin()`;
`controllers/PasswordResets.cfc: create()`. See
findings/04-identity-access.md, Open Questions item 7.

---

## 21. `users.email` uniqueness collation is unconfirmed

**Status:** OPEN — unknown because the deployed MySQL column collation was not confirmable from the reviewed schema snippet

**Finding:** `validatesUniquenessOf("email")`'s actual case-sensitivity
depends on the deployed MySQL column collation, which was not explicitly
declared beyond the table-level `utf8` charset in the reviewed
`install/new-installation.sql` schema.

**Unknown because:** Needs confirmation against an actual running instance
(or an explicit decision for the new system) before assuming case-sensitive
vs. case-insensitive email uniqueness parity.

**Citation:** `models/User.cfc`; `install/new-installation.sql: CREATE TABLE
users`. See findings/04-identity-access.md, Open Questions item 8.

---

## 22. Admin-cannot-assume-admin is UI-only, not server-enforced

**Status:** OPEN — a confirmed security gap, recommend the new system enforce it server-side

**Finding:** The "Assume" (impersonate) link is hidden in the view when the
target is also an admin, but `Users.assumeUser()` itself performs no such
check — any user holding `accessUsers` could impersonate any other user,
including another admin, by constructing the request directly.

**Unknown because:** This is a confirmed gap, not an ambiguity — but whether
the new system enforces this restriction server-side (recommended, as an
explicit, named change from legacy) is a design decision for F6/F7.

**Citation:** `views/users/_usertable.cfm` (client-side hide);
`controllers/Users.cfc: assumeUser()` (no server-side check). See
findings/04-identity-access.md, Open Questions item 9.

---

## 23. Event/Eventresource complete DB column list unconfirmed

**Status:** OPEN — unknown because no schema/migration file was in Wave 1's declared fetch list for this specific finding

**Finding:** Both `Event.cfc` and `Eventresource.cfc` are reflection-based
CFWheels models with no explicit `property()` declarations — the complete DB
column list for either table (beyond what `registerSystemFields()`
documents as the Event model's built-in form fields, and the two `belongsTo`
associations on `Eventresource`) could not be fully confirmed from the model
source alone within this specific sub-finding.

**Unknown because:** Confirming the live DB schema (or a migration/DDL file)
for `events`/`eventresources` beyond what `install/new-installation.sql`
documents elsewhere in this audit (see baseline-inventory.md for the
schema-level confirmations that WERE made) is recommended before finalizing
the new system's `Event`/`Eventresource`-equivalent table definitions.

**Citation:** `models/Event.cfc`; `models/Eventresource.cfc`. See
findings/01-booking-core.md, "Additional limitation noted" paragraph after
Open Question #5.

---

## 24. `excludeFromErrorEmail`/`errorEmailAddress` dead configuration — no send-logic found

**Status:** OPEN — unknown whether this is genuinely unimplemented or implemented outside Wave 1's reviewed files

**Finding:** `config/settings.cfm`'s `excludeFromErrorEmail` setting and
`config/production/settings.cfm`'s template-comment-suggested
`errorEmailAddress` setting both imply an error-notification-email feature
that has no corresponding send-logic anywhere in the files reviewed by Wave 1
(`events/onerror.cfm` is static HTML only).

**Unknown because:** Before the new system's error-handling/observability
design assumes "legacy sent error emails to admins," this should be
explicitly confirmed as either (a) genuinely unimplemented dead
configuration in this codebase, or (b) implemented in a file outside Wave
1's scope (e.g., a plugin or a model-layer error handler).

**Citation:** `config/settings.cfm`; `config/production/settings.cfm`;
`events/onerror.cfm`. See findings/05-platform-settings.md, Open Questions,
final "Additional open item" paragraph.

---

## 25. Write-site for the `"ajax"` log type not located

**Status:** OPEN — unknown because the write-site was not located within Wave 1's confirmed file set

**Finding:** `ajax` appears in `Logfiles.cfc`'s hard-coded
`_getLogFileTypes()` filter-type list (`"login,success,error,ajax,cookie"`),
but no direct `addlogline`/`addLogLine` call writing a `type="ajax"` row was
found anywhere in the 12 controllers or `events/functions.cfm`.

**Unknown because:** It may live in a model file or a controller filter not
covered by Wave 1's file list — should be confirmed by whichever phase
implements the new audit-logging feature (F11) before assuming `ajax`-type
logging is fully understood.

**Citation:** `controllers/Logfiles.cfc: _getLogFileTypes()`. See
findings/05-platform-settings.md, Open Questions, PRD Open Question #8
subsection.

---

*Total: 25 items — 10 PRD seed questions (items 1–10) plus 15 additional
items discovered during Wave 1 (items 11–25). 12 RESOLVED (items 1, 4, 5, 6,
7, 8, 9, 10, 11, 12, 13, 16), 13 OPEN (items 2, 3, 14, 15, 17, 18, 19, 20, 21,
22, 23, 24, 25 — note item 2 is "corrected by code evidence but still
requires a forward product decision," counted as OPEN since the interim
policy is not yet formally re-decided). See baseline-inventory.md for the
complete finding-by-finding F13 traceability seed.*
