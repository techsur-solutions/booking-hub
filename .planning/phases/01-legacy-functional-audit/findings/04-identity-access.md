# Identity & Access Audit — Users, Sessions, PasswordResets, Permissions (Controllers), User (Model)

Source: `neokoenig/RoomBooking` (GitHub, branch `master`). All citations reference file paths in that repository; controller/model line numbers refer to the fetched content as of this audit.

---

## Users controller

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

## Sessions controller

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

## PasswordResets controller

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

## Permissions controller

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

---

## User model

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

## Permission Flag Inventory (cross-controller scan)

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

## Open Questions (identity-access)

1. **PRD Open Question #9 — permission matrix completeness:** **RESOLVED.** See verdict directly above — 17 flags total, 15 active, 2 unused/reserved (`allowiCal`, `allowRSS`).

2. **Initial-credential mechanism:** **RESOLVED.** Admin sets the password directly (plaintext, typed twice for confirmation) at account-creation time via `views/users/formparts/_userpw.cfm` ("Initial Password" panel) and `Users.create()`. No auto-generated password, no emailed activation/invite link. The one-time installer (`install/functions.cfm`, `createInitialAdminUser()`) follows the identical pattern for the very first admin account.

3. **Remember-me duration:** **RESOLVED.** 360-day cookie (`RBS_UN`, `events/functions.cfm:317`) storing only the email address for login-form prefill — not a session-extension mechanism. The underlying web-session timeout itself is **OPEN** — no override was found anywhere in this codebase's config files (`Application.cfc`, `config/app.cfm`, `config/environment.cfm`, `config/settings.cfm`, or any per-environment settings file), meaning it depends on the ColdFusion/Lucee **server/engine default**, which cannot be confirmed from source and must be confirmed against the actual historical deployment's engine-admin configuration (or explicitly decided fresh for the new system) before Phase 3 finalizes session-timeout behavior.

4. **Password complexity rules:** **RESOLVED.** Minimum 6 characters, ≥1 digit, ≥1 lowercase letter (exact regex cited in User model section above). No uppercase or special-character requirement in legacy. New system's policy must be no weaker.

**Additional findings logged as open decision points (discovered during this audit, not explicitly FRD-flagged, but material to Phase 3):**

5. **Permission-change caching requires an application reload.** Edits via the Permissions controller update the database immediately but have no runtime effect until the app restarts (the in-memory `application.rbs.permission` struct is populated once at `onApplicationStart`). Phase 3 should make a conscious decision on whether the new system should (a) apply permission changes immediately (more likely desirable), or (b) intentionally require a cache-refresh step for some operational reason — **recommend (a)**, but this is a behavior change from legacy and should be named as such rather than silently diverging.

6. **Password-reset token expiry is not re-checked at final submit.** `PasswordResets.edit()` checks the 2-hour window when the form is first loaded; `PasswordResets.update()` does not re-check it when the new password is actually submitted — a user who opens the reset link just inside the window could submit a new password well after expiry. Recommend the new system re-validates expiry on both read and write of the reset flow.

7. **Email-enumeration inconsistency between login and password-reset-request.** `Sessions.attemptlogin()` returns a generic failure message regardless of whether the email exists; `PasswordResets.create()` explicitly reveals "we couldn't find an account for that address" when it doesn't. Recommend the new system pick one consistent policy (generic messaging is the more conservative default) as an explicit design decision, not an oversight carried forward.

8. **`users.email` uniqueness collation is unconfirmed.** The `validatesUniquenessOf("email")` model rule's actual case-sensitivity depends on the deployed MySQL column collation, which was not explicitly declared beyond the table-level `utf8` charset in the reviewed schema snippet (`install/new-installation.sql`). Needs confirmation against an actual running instance (or an explicit decision for the new system) before assuming case-sensitive vs. case-insensitive uniqueness parity.

9. **Admin-cannot-assume-admin is UI-only, not server-enforced.** The "Assume" (impersonate) link is hidden in the view when the target is also an admin, but `Users.assumeUser()` itself performs no such check — any user holding `accessUsers` could impersonate any other user, including another admin, by constructing the request directly. Recommend the new system enforce this restriction server-side, as an explicit, named change from legacy (which only enforced it cosmetically).
