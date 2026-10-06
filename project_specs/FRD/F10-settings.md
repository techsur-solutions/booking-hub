## F10: Settings

**Description:** The Settings Service owns system-wide configuration equivalent to the legacy `Settings` singleton — principally the `approveBooking` flag governing the approval workflow (F3) and calendar display parameters consumed by the Booking Service's calendar view (F1).

**Terminology:**
- **Settings singleton:** A single, system-wide configuration record (not per-user, not per-location) equivalent to the legacy `Settings` model.
- **`approveBooking` flag:** Boolean controlling whether new bookings require explicit approval (F3).
- **Calendar display parameters:** Slot size, minimum time, maximum time, and other FullCalendar.js-equivalent rendering configuration.

**Sub-features:**
- `approveBooking` flag read/write
- Calendar display configuration (slot size, min time, max time, etc.)
- Admin-only access to modify settings
- Propagation of settings changes to dependent services

**Process:**
1. Admin user (holding the admin permission, F7) reads the current Settings singleton via a `GET /settings` call.
2. Admin user updates one or more Settings fields (e.g., toggles `approveBooking`, adjusts calendar slot size) via a `PUT /settings` call.
3. Service validates the update (see Validation) and persists the new Settings singleton state, recording the change in the Audit Log (F11).
4. Dependent services (notably the Booking Service, F1/F3, which reads `approveBooking` at Booking-creation time) obtain the updated value either by direct synchronous read of the Settings Service API, or via a `settings.changed` broadcast event — exact propagation mechanism `[to be determined in TechArch, informed by F0 findings on how legacy settings are read/cached, per PRD Section 5 F10]`.
5. Changes to `approveBooking` affect only Bookings created after the change; already-created Bookings retain the status they were assigned at creation time (F3 §Validation).
6. Changes to calendar display parameters affect how the Booking Service's calendar view (F1 §Process step 12) renders on next client fetch; no retroactive effect on stored Booking data.
7. Per-environment settings behavior (development/production/testing/design/maintenance) is audited by F0; any functional difference beyond configuration values (e.g., maintenance-mode read-only lockout) is `[OPEN QUESTION — deferred to F0 — PRD Open Question #10]` and not implemented until confirmed.

**Inputs:**
- `approveBooking` (boolean, optional on update): Approval requirement toggle.
- `calendar_slot_size` (integer, minutes, optional): Calendar slot granularity.
- `calendar_min_time` (time, optional): Earliest time shown on calendar.
- `calendar_max_time` (time, optional): Latest time shown on calendar.

**Outputs:**
- Settings representation: `{approveBooking, calendar_slot_size, calendar_min_time, calendar_max_time, updated_at, updated_by}`.

**Validation:**
- Only one Settings record exists system-wide (singleton); update operations modify the existing record rather than creating new ones.
- `calendar_min_time` must precede `calendar_max_time`.
- `calendar_slot_size` must be a positive integer.
- Only users holding the admin permission (F7) may modify Settings; read access may be broader (e.g., any authenticated user needs to read calendar display parameters to render the calendar) `[confirm legacy read-access scope in F0]`.
- A maintenance-mode or other per-environment functional difference is never implemented based on assumption; if F0 confirms such behavior exists, it is added as an explicit new rule here with its own validation/error states, not inferred from configuration naming alone.

**Error States:**
| Scenario | HTTP Status | Error Code | Message |
|---|---|---|---|
| Caller lacks admin permission for Settings update | 403 | SETTINGS_FORBIDDEN | "You do not have permission to modify settings" |
| `calendar_min_time` not before `calendar_max_time` | 400 | SETTINGS_INVALID_CALENDAR_RANGE | "Calendar minimum time must be before maximum time" |
| `calendar_slot_size` non-positive | 400 | SETTINGS_INVALID_SLOT_SIZE | "Calendar slot size must be a positive number of minutes" |
| Settings Service unavailable when a dependent service reads `approveBooking` | 503 | SETTINGS_UNAVAILABLE | "Settings currently unavailable" |

**API Surface (this feature):** see `Y1-api.md` §Settings (`GET /settings`, `PUT /settings`).

**Schema Surface (this feature):** owns table `settings` (single-row singleton table) — see `Y0-schema.md` §Settings.
