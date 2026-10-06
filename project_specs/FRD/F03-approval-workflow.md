## F3: Approval Workflow

**Description:** The Approval Workflow governs how a Booking moves from `pending` to `approved` or `denied`, driven by the global `approveBooking` Settings flag (F10) and gated by the `allowApproveBooking` permission (F7). Approvals/denials trigger notifications (F8) and are recorded in the audit log (F11).

**Terminology:**
- **`approveBooking` flag:** Global Settings boolean (F10) determining whether new Bookings require explicit approval (`true`) or auto-approve on creation (`false`).
- **`allowApproveBooking` permission:** The Keycloak-mapped role/scope required to call approve/deny actions (F7).
- **Approver:** Any authenticated user holding `allowApproveBooking`.

**Sub-features:**
- Approve action on a pending Booking
- Deny action on a pending Booking
- Auto-approve on creation when `approveBooking` is disabled
- Domain event emission on approve/deny
- Audit logging of approve/deny actions

**Process:**
1. On Booking creation (F1 §Process step 6), the Booking Service reads the current value of the `approveBooking` flag from the Settings Service (F10).
2. If `approveBooking` is `true`, the new Booking's `status` is set to `pending`.
3. If `approveBooking` is `false`, the new Booking's `status` is set to `approved` directly (auto-approve); no separate approve action occurs.
4. For Bookings with `status = pending`, an authenticated user holding `allowApproveBooking` may call the approve action, specifying the `booking_id`.
5. Service verifies the caller holds `allowApproveBooking` (F7); if not, request is rejected (403).
6. Service verifies the target Booking's current `status` is `pending`; approving/denying a Booking that is not pending is rejected (409) — approve/deny are one-way transitions, not re-toggleable.
7. On approve: service sets `status = approved`, records `approved_by` (actor) and `approved_at` (timestamp), publishes a `booking.approved` domain event, and writes an audit log entry (F11).
8. On deny: service sets `status = denied`, records `denied_by` (actor), `denied_at` (timestamp), and optionally a `denial_reason` (free text), publishes a `booking.denied` domain event, and writes an audit log entry (F11).
9. The Notification Service (F8) consumes `booking.approved`/`booking.denied` events and sends notifications per F8 §Process; recipient rules are `[OPEN QUESTION — deferred to F0 — PRD Open Question #5]`.
10. For auto-approved Bookings (step 3), whether a `booking.approved` event is also published in addition to (or instead of) `booking.created` is `[OPEN QUESTION — deferred to F0: PRD Open Question #4 — auto-approve/notification interaction]`; interim default: only `booking.created` is published for auto-approved bookings (no separate `booking.approved` event), to avoid inventing a duplicate-notification behavior not confirmed in legacy.

**Inputs:**
- `booking_id` (UUID/long, required): The pending Booking to approve/deny.
- `denial_reason` (string, optional): Free-text reason supplied on deny.

**Outputs:**
- Updated Booking representation reflecting new `status`, `approved_by`/`approved_at` or `denied_by`/`denied_at`/`denial_reason`.
- Domain event payload: `{event_type, booking_id, actor, timestamp}` published to RabbitMQ.

**Validation:**
- Approve/deny is only permitted on a Booking whose current `status` is exactly `pending`.
- The caller must hold `allowApproveBooking` (F7) — verified server-side on every approve/deny call, not just hidden in the UI.
- `denial_reason`, if supplied, has no required format (free text) `[OPEN QUESTION — deferred to F0: whether legacy requires a reason on deny]`; interim: optional.
- The global `approveBooking` flag value is read at Booking-creation time only; changing the flag does not retroactively alter the status of already-created Bookings.

**Error States:**
| Scenario | HTTP Status | Error Code | Message |
|---|---|---|---|
| Caller lacks `allowApproveBooking` | 403 | APPROVAL_FORBIDDEN | "You do not have permission to approve or deny bookings" |
| Target Booking is not in `pending` status | 409 | APPROVAL_INVALID_STATE | "Only pending bookings can be approved or denied" |
| Target `booking_id` does not exist | 404 | APPROVAL_BOOKING_NOT_FOUND | "Booking not found" |
| Settings Service unavailable when reading `approveBooking` at creation time | 503 | APPROVAL_SETTINGS_UNAVAILABLE | "Unable to determine approval requirement; try again shortly" |

**API Surface (this feature):** see `Y1-api.md` §Booking (`POST /bookings/{id}/approve`, `POST /bookings/{id}/deny`).

**Schema Surface (this feature):** extends the `bookings` table (owned by Booking Service) with `status`, `approved_by`, `approved_at`, `denied_by`, `denied_at`, `denial_reason` columns — see `Y0-schema.md` §Booking. Reads (does not own) the `settings` table via the Settings Service API (F10) — no direct cross-service DB access, per service-isolation NFR.
