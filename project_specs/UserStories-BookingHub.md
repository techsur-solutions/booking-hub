# User Stories
## Booking-Hub

| Field | Value |
|-------|-------|
| **Product Name** | Booking-Hub |
| **Date** | 2026-10-06 |
| **Related PRD** | PRD-BookingHub.md |
| **Related FRD** | FRD-BookingHub.md |

---

## Story Format

Each story follows: **As a [persona], I want to [action], so that [outcome].**

Acceptance criteria are listed beneath each story. Stories are grouped by epic (one epic per PRD feature F0–F13) and prioritised per the PRD's feature-level priority. Personas are drawn from `PERSONAS-BookingHub.md`:

- **Maya Torres** — Marketing Coordinator (Booker)
- **David Okafor** — Facilities Manager (Approver)
- **Priya Patel** — System Administrator
- **Jordan Lee** — Reception/Facilities Viewer (Public Feed Consumer)

Where a story's acceptance criteria touch a legacy behavior not yet confirmed by the F0 Legacy Functional Audit, the criterion is marked **(Open question pending F0)** rather than silently assumed, per the project's ambiguity-handling constraint.

---

## Epic 0: Legacy Functional Audit (F0)

### US-0.1: Confirm Legacy Functional Baseline Before Build Starts
**As a** Priya Patel, System Administrator, **I want to** receive a documented inventory of every legacy RoomBooking controller, model, route, and lifecycle event, **so that** I can confirm no functionality is overlooked before any new service is built.

**Acceptance Criteria:**
- [ ] Audit findings document includes a dedicated section for each of the 12 legacy controllers (`Api`, `Bookings`, `Customfields`, `Eventdata`, `Locations`, `Logfiles`, `PasswordResets`, `Permissions`, `Resources`, `Sessions`, `Settings`, `Users`)
- [ ] Audit findings document includes a dedicated section for each of the 7 legacy model groups (`Event`, `Eventresource`, `Location`, `Resource`, `Customfield`/`Customfieldjoin`/`Customfieldvalue`, `User`, `Settings`)
- [ ] Every audit finding cites its source (file/function/line, doc section, or demo observation)
- [ ] F0 sign-off is withheld until all controller and model sections are complete

**Priority:** P0 | **Feature Ref:** F0

---

### US-0.2: Resolve Open Questions Before Implementation
**As a** Priya Patel, **I want to** have every ambiguous legacy behavior logged as an explicit open question and resolved via evidence or a recorded decision, **so that** no feature is silently reinterpreted during the rewrite.

**Acceptance Criteria:**
- [ ] All 10 PRD seed Open Questions have either a cited resolution or remain logged as open with a clear "unknown because..." statement
- [ ] No Open Question is closed by assumption; each closure cites code/doc/demo evidence or a recorded Key Decision with product sign-off
- [ ] The baseline inventory (one row per audit finding) is handed off to the F13 traceability matrix before any service implementation begins
- [ ] Any new ambiguity discovered after F0 sign-off is added to the Open Questions list rather than silently resolved

**Priority:** P0 | **Feature Ref:** F0

---

## Epic 1: Booking Management Service (F1)

### US-1.1: Create a New Booking
**As a** Maya Torres, Marketing Coordinator (Booker), **I want to** create a booking for a room with a title, time range, and attached resources, **so that** I can reserve the space and equipment I need in one step.

**Acceptance Criteria:**
- [ ] Booking form requires a non-empty title and rejects submission with a clear error if missing
- [ ] If end time is omitted, the system defaults it to start time + 1 hour
- [ ] Submission is rejected with "End time must not be before start time" if end time precedes start time
- [ ] `location_id` must reference an existing, non-deleted location or the booking is rejected with a not-found error
- [ ] Each selected resource must reference an existing, non-deleted resource or the booking is rejected
- [ ] On successful creation, the booking is returned with its assigned status (pending or approved) and any conflict flags

**Priority:** P0 | **Feature Ref:** F1

---

### US-1.2: Edit an Existing Booking
**As a** Maya Torres, **I want to** edit the time, location, or resources of a booking I created, **so that** I can correct details without deleting and recreating it.

**Acceptance Criteria:**
- [ ] Editing a non-series booking re-validates title, time range, location, and resources using the same rules as creation
- [ ] Conflict detection is re-run on every edit, not only inherited from the original creation check
- [ ] Editing a booking that belongs to a recurring series requires a scope selection (this occurrence or whole series); omitting scope is rejected with a clear error
- [ ] A successful edit publishes an updated-booking event and is reflected in the audit log

**Priority:** P0 | **Feature Ref:** F1

---

### US-1.3: Delete or Cancel a Booking
**As a** Maya Torres, **I want to** cancel a booking I no longer need, **so that** the room/resource becomes available for others and my calendar is accurate.

**Acceptance Criteria:**
- [ ] Deleting a non-series booking soft-deletes it and removes it from calendar/list views and conflict checks
- [ ] Deleting a booking in a series requires a scope selection (this occurrence or whole series), matching the edit-scope rule
- [ ] Deleted bookings are excluded from all future conflict comparisons
- [ ] A successful deletion publishes a `booking.deleted` event and is recorded in the audit log

**Priority:** P0 | **Feature Ref:** F1

---

### US-1.4: Clone an Existing Booking
**As a** Maya Torres, **I want to** clone a previous booking as a starting point for a new one, **so that** I don't have to re-enter the same title, location, and resources for a repeat meeting.

**Acceptance Criteria:**
- [ ] Cloning a non-existent source booking returns a clear "Booking to clone does not exist" error
- [ ] The clone copies all field values except `id`, `status`, `created_at`, `series_id`, and timestamps
- [ ] The cloned booking's status is reset to the default per the current approval setting, not copied from the source
- [ ] The clone is never associated with the original booking's `series_id`

**Priority:** P0 | **Feature Ref:** F1

---

### US-1.5: View Bookings in Calendar, Day, and List Views
**As a** Maya Torres, **I want to** see all bookings for a date range in calendar, day, or list view, **so that** I can quickly check room availability before requesting a new booking.

**Acceptance Criteria:**
- [ ] Calendar/day/list views return all non-deleted bookings within the requested date range
- [ ] Views support filtering by location and/or resource
- [ ] Each returned booking includes its `conflict_flags` so conflicts are visible without a separate lookup
- [ ] Views render consistently regardless of whether accessed via calendar or list entry point

**Priority:** P0 | **Feature Ref:** F1

---

### US-1.6: Create a Recurring Booking Series
**As a** Maya Torres, **I want to** set up a recurring weekly team sync once, **so that** I don't have to manually create the same booking every week.

**Acceptance Criteria:**
- [ ] A recurrence definition expands into individual booking occurrences sharing a common `series_id`
- [ ] Each generated occurrence is independently validated and conflict-checked
- [ ] The recurrence definition must resolve to a determinate, finite set of occurrences (open-ended series are rejected)
- [ ] **(Open question pending F0)** The exact recurrence pattern options and edit/delete scoping behavior are flagged for confirmation rather than silently assumed

**Priority:** P0 | **Feature Ref:** F1

---

### US-1.7: Retrieve Booking Detail for Modal/Detail View
**As a** Maya Torres, **I want to** click a booking to see its full details, **so that** I can confirm all the information before showing up or making changes.

**Acceptance Criteria:**
- [ ] Event-detail endpoint returns the full field set, including custom field values, for a single booking
- [ ] Detail view reflects the booking's current status (pending/approved/denied) accurately
- [ ] Requesting detail for a non-existent booking returns a clear not-found error

**Priority:** P0 | **Feature Ref:** F1

---

## Epic 2: Conflict Detection (F2)

### US-2.1: See a Conflict Warning Before Saving
**As a** Maya Torres, **I want to** be warned before I save a booking that overlaps an existing one, **so that** I don't accidentally double-book a room or piece of equipment.

**Acceptance Criteria:**
- [ ] Overlap is evaluated per location and per resource using half-open interval semantics (a booking ending at 10:00 does not conflict with one starting at 10:00)
- [ ] Conflict checks exclude the booking's own prior state when editing (never compared against itself)
- [ ] Deleted and denied bookings are excluded from conflict comparison
- [ ] A detected conflict is surfaced with the conflicting booking's id and whether it is a location or resource conflict

**Priority:** P0 | **Feature Ref:** F2

---

### US-2.2: See Conflict Flags Consistently in Calendar and List Views
**As a** Maya Torres, **I want to** have conflicts flagged the same way whether I'm looking at the calendar or the list view, **so that** I never discover a double-booking only when I arrive at the room.

**Acceptance Criteria:**
- [ ] Calendar view displays a visual indicator for any booking with a conflict
- [ ] List view flags the same bookings as conflicted using the same underlying conflict data
- [ ] Conflict evaluation logic is identical regardless of entry point (UI calendar, UI list, or direct API call)
- [ ] Bulk conflict evaluation is applied to every booking returned in a date-range view, not just newly created ones

**Priority:** P0 | **Feature Ref:** F2

---

### US-2.3: Have Conflicts Re-Checked on Every Edit
**As a** David Okafor, Facilities Manager (Approver), **I want to** have conflict checks re-run every time a booking is edited, **so that** I can trust the conflict flags I see are always current, not just accurate at creation time.

**Acceptance Criteria:**
- [ ] Editing a booking's time, location, or resources triggers a fresh conflict evaluation, not a cached result from creation
- [ ] Multi-resource bookings have each attached resource checked, with the union of all per-resource conflicts returned
- [ ] **(Open question pending F0)** Whether a conflict is a hard block or a soft warning — and whether this differs by role — is flagged rather than silently assumed; interim default is hard block for all roles

**Priority:** P0 | **Feature Ref:** F2

---

## Epic 3: Approval Workflow (F3)

### US-3.1: Review the Pending Booking Queue
**As a** David Okafor, **I want to** see every pending booking in one queue with conflict status clearly flagged, **so that** I can make an informed approve/deny decision without manual cross-checking.

**Acceptance Criteria:**
- [ ] Pending queue lists all bookings with `status = pending`
- [ ] Each queued booking displays its `conflict_flags` inline
- [ ] Queue is restricted to users holding `allowApproveBooking`; others cannot view or act on it

**Priority:** P0 | **Feature Ref:** F3

---

### US-3.2: Approve a Pending Booking
**As a** David Okafor, **I want to** approve a pending booking in one action, **so that** the requester can proceed with confidence and my team's calendar stays authoritative.

**Acceptance Criteria:**
- [ ] Approve action is rejected with 403 if the caller lacks `allowApproveBooking`
- [ ] Approve action is rejected with 409 if the target booking's status is not exactly `pending`
- [ ] A successful approval sets `status=approved`, records `approved_by` and `approved_at`, and publishes a `booking.approved` domain event
- [ ] The approval action is recorded in the audit log with actor and timestamp

**Priority:** P0 | **Feature Ref:** F3

---

### US-3.3: Deny a Pending Booking
**As a** David Okafor, **I want to** deny a pending booking and optionally note a reason, **so that** the requester understands why their request wasn't approved.

**Acceptance Criteria:**
- [ ] Deny action follows the same permission and status-state checks as approve
- [ ] A successful denial sets `status=denied`, records `denied_by`, `denied_at`, and optional `denial_reason`
- [ ] Denial publishes a `booking.denied` domain event consumed by the Notification Service
- [ ] Approve/deny are one-way transitions — a non-pending booking cannot be re-approved or re-denied

**Priority:** P0 | **Feature Ref:** F3

---

### US-3.4: Understand Auto-Approve Behavior
**As a** Maya Torres, **I want to** know immediately whether my booking is pending review or was auto-approved, **so that** I can plan around the room with confidence.

**Acceptance Criteria:**
- [ ] When the `approveBooking` setting is disabled, new bookings are assigned `status=approved` directly with no separate approve action required
- [ ] When the `approveBooking` setting is enabled, new bookings are assigned `status=pending` until an approver acts
- [ ] Changing the `approveBooking` flag never retroactively changes the status of already-created bookings
- [ ] **(Open question pending F0)** Whether an auto-approved booking triggers a "created" notification, an "approved" notification, both, or neither is flagged for confirmation

**Priority:** P0 | **Feature Ref:** F3

---

## Epic 4: Locations & Resources Management (F4)

### US-4.1: Create and Manage Locations
**As a** Priya Patel, System Administrator, **I want to** create and edit bookable locations with a name, calendar colour, and building grouping, **so that** rooms appear correctly for booking and conflict-detection scoping.

**Acceptance Criteria:**
- [ ] Location creation rejects a missing or empty name with a clear error
- [ ] Location record stores name, `css_class`/colour, building, and optional layout metadata
- [ ] Only users holding the admin permission can create, edit, or delete locations
- [ ] A new or edited location is immediately available to the Booking Service for selection and conflict scoping

**Priority:** P1 | **Feature Ref:** F4

---

### US-4.2: Create and Manage Resources
**As a** Priya Patel, **I want to** create and edit bookable resources (equipment) independently of locations, **so that** items like projectors or conference phones can be attached to any booking.

**Acceptance Criteria:**
- [ ] Resource creation rejects a missing or empty name with a clear error
- [ ] Resources are referenced by the Booking Service by id only, with no direct cross-service database access
- [ ] Only users holding the admin permission can create, edit, or delete resources

**Priority:** P1 | **Feature Ref:** F4

---

### US-4.3: Safely Remove a Location or Resource No Longer in Use
**As a** Priya Patel, **I want to** delete a location or resource without breaking existing bookings that reference it, **so that** historical booking records remain intact.

**Acceptance Criteria:**
- [ ] Deleting a location/resource referenced by non-deleted bookings follows the interim soft-delete policy (`deleted_at` set; existing booking references remain intact and display last-known values)
- [ ] Deleting a non-existent location/resource id returns a clear not-found error
- [ ] Deletion is recorded in the audit log with actor and timestamp
- [ ] **(Open question pending F0)** Whether legacy blocks deletion-in-use, cascades, or allows orphaned references is flagged for confirmation

**Priority:** P1 | **Feature Ref:** F4

---

### US-4.4: See Location Colour Coding on the Calendar
**As a** Maya Torres, **I want to** see each location render with a consistent colour/CSS class on the calendar, **so that** I can visually scan for the room I'm looking for.

**Acceptance Criteria:**
- [ ] Calendar view renders each booking using its location's configured `css_class`/colour
- [ ] Colour/class rendering is consistent across calendar, day, and list views

**Priority:** P1 | **Feature Ref:** F4

---

## Epic 5: Custom Fields (F5)

### US-5.1: Define Custom Field Templates
**As a** Priya Patel, **I want to** define custom fields and group them into templates tied to a booking context, **so that** the right fields appear on the right booking forms.

**Acceptance Criteria:**
- [ ] Custom field creation rejects a missing/empty label with a clear error
- [ ] A `field_type` of "select" requires a non-empty `options[]` list or creation is rejected
- [ ] Only users holding the admin permission can create/edit/delete custom field definitions and templates
- [ ] A field template associates one or more custom fields with a context (e.g., a specific location or "all bookings")

**Priority:** P1 | **Feature Ref:** F5

---

### US-5.2: Fill In Custom Fields on the Booking Form
**As a** Maya Torres, **I want to** fill in any required custom fields (e.g., catering headcount) directly on the booking form, **so that** I don't have to separately email facilities with extra details.

**Acceptance Criteria:**
- [ ] The booking form dynamically renders only the custom fields applicable to the selected location's context
- [ ] A submitted custom field value referencing an unknown `field_id` is rejected with a clear error
- [ ] Submitted values are persisted and linked to the booking record
- [ ] **(Open question pending F0)** Required/optional and type-specific validation (numeric/date) is flagged for confirmation; interim default treats all custom fields as optional free text

**Priority:** P1 | **Feature Ref:** F5

---

### US-5.3: View Custom Field Values Alongside Booking Details
**As a** David Okafor, **I want to** see submitted custom field values (e.g., catering cost) when reviewing a pending booking, **so that** I can factor them into my approve/deny decision.

**Acceptance Criteria:**
- [ ] Custom field values are included in the booking representation returned by calendar/list/detail views
- [ ] Deleting a custom field definition does not purge historical values already recorded against past bookings

**Priority:** P1 | **Feature Ref:** F5

---

## Epic 6: User & Role Management (F6)

### US-6.1: Create a New User Account
**As a** Priya Patel, **I want to** create a new user account and assign an initial role, **so that** new employees can book rooms or review approvals from day one.

**Acceptance Criteria:**
- [ ] Account creation with a duplicate email/username is rejected with a 409 "already exists" error
- [ ] New accounts are provisioned in the Keycloak realm rather than via a locally stored password hash
- [ ] Initial role assignment is required and feeds directly into the Permission System (F7)
- [ ] Only users holding the admin permission may create accounts or assign roles to other users

**Priority:** P0 | **Feature Ref:** F6

---

### US-6.2: Edit My Profile and Change My Password
**As a** Maya Torres, **I want to** update my display name and change my own password from "my account," **so that** I can keep my profile current without contacting an admin.

**Acceptance Criteria:**
- [ ] A user may always edit their own profile and change their own password without admin involvement
- [ ] A password change request with an incorrect current password is rejected with a clear error
- [ ] A new password that fails Keycloak's configured policy is rejected with a clear error

**Priority:** P0 | **Feature Ref:** F6

---

### US-6.3: Reset a Forgotten Password
**As a** Maya Torres, **I want to** request a password reset by email when I've forgotten my password, **so that** I can regain access without waiting on IT.

**Acceptance Criteria:**
- [ ] A reset request returns a generic success acknowledgment regardless of whether the email/username exists, to avoid revealing account existence
- [ ] A `password.reset.requested` domain event is published and consumed by the Notification Service to send the reset link
- [ ] Completing a reset with an expired, invalid, or already-used token is rejected with a clear error
- [ ] A successful reset updates the credential via Keycloak's reset mechanism

**Priority:** P0 | **Feature Ref:** F6

---

### US-6.4: Log In, Log Out, and Stay Signed In with "Remember Me"
**As a** Maya Torres, **I want to** log in once and optionally stay signed in, **so that** I don't have to re-authenticate every time I check my bookings.

**Acceptance Criteria:**
- [ ] Login with invalid credentials is rejected with a 401 "Invalid username or password" error
- [ ] A successful login returns an access token, refresh token, expiry, and assigned roles/scopes
- [ ] Selecting "remember me" extends the issued session/token lifetime
- [ ] Logging out discards tokens and/or revokes the Keycloak session
- [ ] Accessing a protected resource without a valid token returns 401 (API) or redirects to login (UI)

**Priority:** P0 | **Feature Ref:** F6

---

### US-6.5: Assign or Change a User's Role
**As a** Priya Patel, **I want to** change a user's role when their responsibilities change, **so that** their access reflects their current job function.

**Acceptance Criteria:**
- [ ] Role assignment updates the user's Keycloak role mappings
- [ ] Role changes take effect on the user's next issued token
- [ ] Only users holding the admin permission may assign or change roles for other users

**Priority:** P0 | **Feature Ref:** F6

---

## Epic 7: Permission System (F7)

### US-7.1: Confirm Every Legacy Permission Maps to a Keycloak Role
**As a** Priya Patel, **I want to** have a complete mapping table from every legacy permission flag to its Keycloak role/scope equivalent, **so that** I can be confident no one gains or loses access unintentionally during the re-platform.

**Acceptance Criteria:**
- [ ] Every permission flag referenced by any other feature (`accessCalendar`, `allowRoomBooking`, `viewRoomBooking`, `allowApproveBooking`, `accessPermissions`, `allowAPI`, and any F0-confirmed additions) appears in the mapping table before its gated action is implemented
- [ ] A permission flag whose gating scope is unconfirmed is treated as restrictive (deny by default), never as "no restriction"
- [ ] The mapping table records, per flag, its Keycloak role/scope and confirmed status

**Priority:** P0 | **Feature Ref:** F7

---

### US-7.2: Manage Role-to-Permission Assignments
**As a** Priya Patel, **I want to** view and edit which permission flags each role carries, **so that** I have a single place to see "who can do what" instead of hunting through code.

**Acceptance Criteria:**
- [ ] Only users holding `accessPermissions` (or its confirmed Keycloak equivalent) may modify role-to-permission mappings
- [ ] An attempt to reference an unknown or unconfirmed permission flag in a mapping update is rejected with a clear error
- [ ] Mapping changes are recorded in the audit log

**Priority:** P0 | **Feature Ref:** F7

---

### US-7.3: Trust Consistent Enforcement Across Gateway and Services
**As a** Priya Patel, **I want** gateway-level routing and service-level action checks to always agree, **so that** a permission boundary can never be silently bypassed by calling a service directly.

**Acceptance Criteria:**
- [ ] A request to a gateway-protected route without the required role/scope is rejected with 401/403 at the edge
- [ ] A service-level fine-grained check that fails despite passing gateway routing is rejected with 403
- [ ] No service grants an action that the gateway would have blocked for the same role

**Priority:** P0 | **Feature Ref:** F7

---

## Epic 8: Notifications (F8)

### US-8.1: Get Notified When My Booking Is Submitted
**As a** Maya Torres, **I want to** receive a notification when I submit a booking, **so that** I have confirmation it was recorded correctly.

**Acceptance Criteria:**
- [ ] A `booking.created` domain event is published on every successful booking creation
- [ ] The Notification Service consumes the event and sends an email to the booking owner
- [ ] A redelivered event (same idempotency key) never results in a duplicate email

**Priority:** P1 | **Feature Ref:** F8

---

### US-8.2: Get Notified the Moment My Booking Is Approved or Denied
**As a** Maya Torres, **I want to** be notified as soon as my booking is approved or denied, **so that** I know immediately whether I can rely on the room.

**Acceptance Criteria:**
- [ ] A `booking.approved` or `booking.denied` event triggers an email to the booking owner
- [ ] Notification delivery is retried per a backoff policy on transient send failure, rather than silently failing
- [ ] Retry exhaustion routes the message to a dead-letter queue for inspection rather than silently dropping it

**Priority:** P1 | **Feature Ref:** F8

---

### US-8.3: Trust That Approval Decisions Always Reach the Requester
**As a** David Okafor, **I want** every approval or denial I issue to reliably trigger a notification, **so that** I never have to manually follow up to tell someone their booking was decided.

**Acceptance Criteria:**
- [ ] 100% of `booking.approved`/`booking.denied` events result in a delivered or retried-to-success notification with no silent drops
- [ ] An event missing a required idempotency key is routed to the dead-letter queue immediately rather than risking a duplicate send
- [ ] An unresolvable recipient (e.g., deleted user) is treated as a processing failure following the retry/DLQ path, not silently swallowed

**Priority:** P1 | **Feature Ref:** F8

---

### US-8.4: Receive a Password Reset Email
**As a** Maya Torres, **I want to** receive a reset link by email when I request a password reset, **so that** I can regain access to my account.

**Acceptance Criteria:**
- [ ] A `password.reset.requested` event triggers an email containing the reset link/token to the requesting user
- [ ] The email is sent via durable, retryable delivery rather than a synchronous in-request send

**Priority:** P1 | **Feature Ref:** F8

---

## Epic 9: Public Feeds (F9)

### US-9.1: Read the Lobby Display Board
**As a** Jordan Lee, Reception/Facilities Viewer (Public Feed Consumer), **I want to** glance at an auto-refreshing display board showing upcoming approved bookings, **so that** I can direct visitors to the correct room without logging into anything.

**Acceptance Criteria:**
- [ ] Display board shows only bookings with `status=approved`; pending and denied bookings never appear
- [ ] Display board auto-refreshes client-side to stay current
- [ ] Display board can be filtered to a specific location when multiple buildings are shown

**Priority:** P1 | **Feature Ref:** F9

---

### US-9.2: Subscribe to an iCal Feed
**As a** Jordan Lee, **I want to** subscribe once to an iCal feed for my location, **so that** upcoming approved bookings sync automatically to my calendar app without manual refreshing.

**Acceptance Criteria:**
- [ ] The iCal feed produces a standard `.ics` document with one `VEVENT` per approved upcoming booking
- [ ] Per-location filtering is supported on the iCal feed the same way as other formats
- [ ] An unknown `location_id` filter returns an empty feed rather than an error

**Priority:** P1 | **Feature Ref:** F9

---

### US-9.3: Access Feeds Consistently Across All Formats
**As a** Jordan Lee, **I want** RSS2, JSON, iCal, and the display board to all follow the same access rules, **so that** I'm not confused by one format working and another being blocked.

**Acceptance Criteria:**
- [ ] Feed access control is evaluated identically regardless of requested format (RSS2, iCal, JSON, display board)
- [ ] A caller denied access to one format is equally denied access to all other formats for the same scope
- [ ] **(Open question pending F0)** Whether feeds are fully public by default or always gated behind `allowAPI` is flagged for confirmation; interim default requires `allowAPI`-scoped access

**Priority:** P1 | **Feature Ref:** F9

---

### US-9.4: Trust That Feeds Never Leak Unapproved Bookings
**As a** Jordan Lee, **I want to** be certain that only approved bookings ever appear in any feed or on the display board, **so that** I never direct a visitor to a room based on a booking that was never confirmed.

**Acceptance Criteria:**
- [ ] Pending and denied bookings are never exposed via any feed format or the display board, regardless of caller access level
- [ ] Feed data matches the authoritative approved-booking state with refresh latency under 1 minute from approval

**Priority:** P1 | **Feature Ref:** F9

---

## Epic 10: Settings (F10)

### US-10.1: Toggle the Approval Requirement
**As a** Priya Patel, **I want to** toggle the `approveBooking` setting on or off, **so that** I can control whether new bookings require explicit approval during busy or quiet periods.

**Acceptance Criteria:**
- [ ] Only users holding the admin permission can modify the `approveBooking` setting
- [ ] Changing the flag affects only bookings created after the change; already-created bookings retain their original status
- [ ] A settings update is recorded in the audit log with actor and timestamp

**Priority:** P1 | **Feature Ref:** F10

---

### US-10.2: Adjust Calendar Display Parameters
**As a** Priya Patel, **I want to** configure the calendar's slot size and visible time range, **so that** the booking calendar matches how our organization actually schedules rooms.

**Acceptance Criteria:**
- [ ] `calendar_min_time` must precede `calendar_max_time` or the update is rejected with a clear error
- [ ] `calendar_slot_size` must be a positive integer or the update is rejected with a clear error
- [ ] Calendar display changes affect rendering on next client fetch with no retroactive effect on stored booking data

**Priority:** P1 | **Feature Ref:** F10

---

### US-10.3: Trust That Settings Changes Propagate Everywhere
**As a** Priya Patel, **I want** a settings change to take effect across every dependent service without me manually checking multiple screens, **so that** I can be confident the whole system is in sync.

**Acceptance Criteria:**
- [ ] The Booking Service reads the current `approveBooking` value at booking-creation time, reflecting the latest saved setting
- [ ] Only one settings record exists system-wide; updates modify the existing singleton rather than creating new records
- [ ] Settings Service unavailability when a dependent service reads a value is surfaced as a 503, not silently defaulted

**Priority:** P1 | **Feature Ref:** F10

---

## Epic 11: Activity/Audit Logging (F11)

### US-11.1: View and Filter the Audit Log
**As a** Priya Patel, **I want to** filter the audit log by entity type, actor, or date range, **so that** I can investigate a specific change quickly instead of scanning everything.

**Acceptance Criteria:**
- [ ] Audit log viewing is restricted to users holding the admin permission equivalent to legacy `Logfiles` access
- [ ] A filter query with `date_from` after `date_to` is rejected with a clear error
- [ ] Filtered results return a paginated list of matching audit log entries

**Priority:** P1 | **Feature Ref:** F11

---

### US-11.2: Investigate a Disputed Booking Decision
**As a** David Okafor, **I want to** look up the audit trail for a specific booking, **so that** I can confirm exactly who approved or denied it and when, if a dispute arises.

**Acceptance Criteria:**
- [ ] Audit log entries for a booking include actor, timestamp, `action_type`, and before/after values where available
- [ ] Audit log entries are immutable once written — no API can edit or delete a prior entry

**Priority:** P1 | **Feature Ref:** F11

---

### US-11.3: Trust That No State Change Goes Unrecorded
**As a** Priya Patel, **I want** every state-changing action across every service to produce a corresponding audit entry, **so that** I can demonstrate full accountability during a compliance review.

**Acceptance Criteria:**
- [ ] Booking lifecycle changes, location/resource changes, user/role changes, settings changes, and permission changes each produce a corresponding audit log entry
- [ ] A missed or dropped domain event is retried via the durable-queue/DLQ mechanism rather than silently leaving a gap in the trail
- [ ] 100% of state-changing actions exercised in the regression suite produce a corresponding audit log entry

**Priority:** P1 | **Feature Ref:** F11

---

## Epic 12: Microservice Architecture & Platform (F12)

### US-12.1: Deploy and Scale Services Independently
**As a** Priya Patel, **I want** each backend service to be deployable and scalable on its own, **so that** a change to one part of the system (e.g., Notifications) never requires redeploying the whole platform.

**Acceptance Criteria:**
- [ ] Each service can be built, deployed, and scaled via its own Kubernetes manifests without requiring changes to or redeployment of any other service
- [ ] No service's codebase directly connects to another service's PostgreSQL database
- [ ] Independent deployability is demonstrated in a staging environment for every service

**Priority:** P0 | **Feature Ref:** F12

---

### US-12.2: Trust a Single, Secure Entry Point for All Traffic
**As a** Priya Patel, **I want** all external client traffic to route through one gateway, **so that** authentication and routing rules are enforced consistently and backend services are never directly exposed.

**Acceptance Criteria:**
- [ ] No backend service is reachable from outside the cluster except through Spring Cloud Gateway
- [ ] Every domain event referenced across features has a corresponding RabbitMQ exchange/queue/routing key defined before the publishing service is deployed
- [ ] Every permission flag in the mapping table has a corresponding Keycloak role/scope defined in the realm configuration

**Priority:** P0 | **Feature Ref:** F12

---

## Epic 13: Regression Verification & Test Traceability (F13)

### US-13.1: Trace Every Legacy Finding to a Passing Test
**As a** Priya Patel, **I want** a traceability matrix linking every audit finding to its requirement, implementation, and test, **so that** I can prove "no functionality lost" with evidence instead of assertion.

**Acceptance Criteria:**
- [ ] Every F0 audit finding has at least one linked FRD requirement row with no orphaned findings
- [ ] Every FRD validation/error-state rule has at least one linked automated test before its feature is considered release-ready
- [ ] No requirement row is marked "tested" without a concrete test reference that actually exercises the stated rule

**Priority:** P0 | **Feature Ref:** F13

---

### US-13.2: Block Releases That Would Regress Parity
**As a** Priya Patel, **I want** the CI pipeline to block any change that fails a previously passing parity test, **so that** a regression can never reach production silently.

**Acceptance Criteria:**
- [ ] A CI-enforced regression suite runs on every change and must pass before merge/release
- [ ] A change that causes a previously tested requirement's linked test to fail blocks the merge/release
- [ ] An Open Question closure without a corresponding new test is rejected rather than accepted

**Priority:** P0 | **Feature Ref:** F13

---

## Summary Table

| Epic | Story Count | P0 | P1 | P2 |
|------|-------------|----|----|-----|
| Epic 0: Legacy Functional Audit (F0) | 2 | 2 | 0 | 0 |
| Epic 1: Booking Management Service (F1) | 7 | 7 | 0 | 0 |
| Epic 2: Conflict Detection (F2) | 3 | 3 | 0 | 0 |
| Epic 3: Approval Workflow (F3) | 4 | 4 | 0 | 0 |
| Epic 4: Locations & Resources Management (F4) | 4 | 0 | 4 | 0 |
| Epic 5: Custom Fields (F5) | 3 | 0 | 3 | 0 |
| Epic 6: User & Role Management (F6) | 5 | 5 | 0 | 0 |
| Epic 7: Permission System (F7) | 3 | 3 | 0 | 0 |
| Epic 8: Notifications (F8) | 4 | 0 | 4 | 0 |
| Epic 9: Public Feeds (F9) | 4 | 0 | 4 | 0 |
| Epic 10: Settings (F10) | 3 | 0 | 3 | 0 |
| Epic 11: Activity/Audit Logging (F11) | 3 | 0 | 3 | 0 |
| Epic 12: Microservice Architecture & Platform (F12) | 2 | 2 | 0 | 0 |
| Epic 13: Regression Verification & Test Traceability (F13) | 2 | 2 | 0 | 0 |
| **Total** | **49** | **28** | **21** | **0** |

---

## Priority Definitions

| Priority | Definition |
|----------|------------|
| **P0** | Critical - Must have for MVP. Matches PRD features F0, F1, F2, F3, F6, F7, F12, F13 — foundational audit, core booking/conflict/approval workflow, identity/access control, platform substrate, and regression verification. |
| **P1** | High - Important for first release. Matches PRD features F4, F5, F8, F9, F10, F11 — reference data, extensibility, notifications, public visibility, configuration, and compliance logging. All are required for this release; none are deferred. |
| **P2** | Medium - Nice to have. Not used in this release — the PRD's strict feature-parity mandate treats every legacy capability area as required (P0/P1 only), per PRD Section 9. |
| **P3** | Low - Future consideration. Not used in this release. |

---

*Document generated by Pivota Spec Framework*
*Last updated: 2026-10-06*
