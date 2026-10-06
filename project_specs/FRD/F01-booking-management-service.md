## F1: Booking Management Service

**Description:** The Booking Service is the core transactional service of Booking-Hub, owning the `Booking` entity (legacy `Event`) and its resource associations (legacy `Eventresource`). It handles create/edit/delete/clone operations, date/time validation, recurring-series management, multi-resource attachment, and the pending/approved/denied status workflow, and serves the calendar/day/list view read models and the AJAX-style event-detail retrieval equivalent to legacy `Eventdata`.

**Terminology:**
- **Booking (legacy `Event`):** A single reservation of a Location for a time range, optionally attached to one or more Resources and custom field values.
- **BookingResource (legacy `Eventresource`):** Join entity linking a Booking to a Resource.
- **Series:** A set of Bookings generated from a single recurring-booking definition, sharing a `series_id`.
- **Clone:** A new Booking created by copying an existing Booking's field values (excluding identity/status fields) as a starting point for a new submission.
- **Status workflow:** The lifecycle `pending → approved` or `pending → denied`, or direct `approved` on auto-approve; see F3 for transition rules.

**Sub-features:**
- Create booking (single or recurring series)
- Edit booking (single occurrence or whole series)
- Delete booking (single occurrence or whole series)
- Clone booking
- Multi-resource attachment per booking
- Calendar view, day view, list view (read models)
- AJAX event-detail/modal retrieval (legacy `Eventdata` equivalent)
- Default-duration and date/time validation

**Process:**
1. Client submits a booking creation request with title, location, start/end time, optional resource list, optional custom field values, and optional recurrence definition.
2. Service validates the request (see Validation).
3. If `end_time` is omitted, service defaults it to `start_time + 1 hour` (legacy default duration rule).
4. Service invokes Conflict Detection (F2) for the proposed location and each proposed resource over the proposed time range.
5. If recurrence is specified, service expands the recurrence definition into individual Booking occurrences sharing a `series_id`, applying steps 2–4 to each occurrence `[OPEN QUESTION — deferred to F0: exact recurrence pattern options (daily/weekly/monthly, end-date vs. occurrence-count) are not yet confirmed from legacy code; interim assumption is weekly-by-day-of-week with an end date, pending F0 confirmation]`.
6. Service determines initial status per F3 §Process (reads the `approveBooking` Settings flag via F10): `pending` if approval is required, `approved` if auto-approve is enabled.
7. Service persists the Booking (and BookingResource rows for each attached resource, and CustomFieldValue rows if applicable — see F5) and publishes a `booking.created` domain event (consumed by F8 Notifications and F11 Audit Logging).
8. Service returns the created Booking (or series) representation to the client, including any conflict warnings surfaced per F2.
9. On edit: client submits updated fields for an existing Booking (or series). If the Booking belongs to a series, client must specify scope (`this_occurrence` or `whole_series`) `[OPEN QUESTION — deferred to F0: whether legacy prompts for scope choice, defaults to one, or does not support series-wide edits at all — PRD Open Question #1]`. Service re-validates and re-runs Conflict Detection (F2) for the edited time/location/resources, then publishes a `booking.updated` domain event and records the change in the Audit Log (F11).
10. On delete: client requests deletion with the same scope parameter as edit (`this_occurrence` or `whole_series`) subject to the same `[OPEN QUESTION]` flag as step 9. Service soft- or hard-deletes per F0 confirmation (interim: soft-delete via a `deleted_at` timestamp to preserve audit trail) and publishes a `booking.deleted` domain event.
11. On clone: client requests a clone of an existing Booking by ID. Service copies all fields except `id`, `status` (reset to the default per step 6), `created_at`, `series_id` (clone is never part of the original series), and timestamps, and returns a new unsaved/draft representation (or directly persists per F0 confirmation of legacy clone behavior) for the client to adjust before submission.
12. Calendar/day/list views: service exposes read endpoints returning Bookings within a requested date range, filterable by location and/or resource, with conflict flags attached per F2.
13. Event-detail retrieval: service exposes a single-booking detail endpoint returning the full field set (including custom field values) for modal/detail-pane rendering, equivalent to legacy `Eventdata`.

**Inputs:**
- `title` (string, required): Booking title/subject.
- `location_id` (UUID/long, required): References a Location (F4).
- `start_time` (ISO-8601 datetime, required): Booking start.
- `end_time` (ISO-8601 datetime, optional): Booking end; defaults to `start_time + 1h` if omitted.
- `resource_ids` (array of UUID/long, optional): Zero or more Resources to attach (F4).
- `custom_field_values` (array of {field_id, value}, optional): Per F5.
- `recurrence` (object, optional): Recurrence definition (pattern, interval, end condition) — exact shape `[OPEN QUESTION — deferred to F0]`.
- `scope` (enum: `this_occurrence` | `whole_series`, required for edit/delete of a series Booking): Edit/delete scope.
- `source_booking_id` (UUID/long, required for clone): The Booking to clone from.

**Outputs:**
- Booking representation: `id`, `series_id` (nullable), `title`, `location`, `start_time`, `end_time`, `resources[]`, `custom_field_values[]`, `status`, `owner`, `created_at`, `updated_at`, `deleted_at` (nullable), `conflict_flags[]` (from F2).
- Calendar/day/list view responses: arrays of Booking representations scoped to the requested date range and filters.
- Event-detail response: full single-Booking representation equivalent to legacy `Eventdata` payload.

**Validation:**
- `title` must be non-empty.
- `location_id` must reference an existing, non-deleted Location (F4).
- `end_time` must not precede `start_time` (legacy rule, confirmed in PRD Section 5 F1).
- If `end_time` is omitted, it is set to `start_time + 1 hour` (legacy default duration rule, confirmed).
- Each entry in `resource_ids` must reference an existing, non-deleted Resource (F4).
- `recurrence`, if present, must define a determinate, finite set of occurrences (no open-ended series) `[OPEN QUESTION — deferred to F0: exact constraint unconfirmed]`.
- Edit/delete of a Booking that belongs to a series must include a valid `scope` value; omission is rejected `[OPEN QUESTION — deferred to F0: whether legacy actually requires explicit scope or has a silent default — PRD Open Question #1]`.
- Conflict Detection (F2) is invoked synchronously as part of create and edit validation, not as a post-hoc check (see F2 §Process).

**Error States:**
| Scenario | HTTP Status | Error Code | Message |
|---|---|---|---|
| Missing or empty `title` | 400 | BOOKING_TITLE_REQUIRED | "Booking title is required" |
| `end_time` precedes `start_time` | 400 | BOOKING_INVALID_TIME_RANGE | "End time must not be before start time" |
| `location_id` references a non-existent or deleted Location | 404 | BOOKING_LOCATION_NOT_FOUND | "Specified location does not exist" |
| One or more `resource_ids` reference a non-existent or deleted Resource | 404 | BOOKING_RESOURCE_NOT_FOUND | "One or more specified resources do not exist" |
| Edit/delete of series Booking without `scope` parameter | 400 | BOOKING_SCOPE_REQUIRED | "Scope (this_occurrence or whole_series) is required for recurring bookings" |
| Clone requested for non-existent `source_booking_id` | 404 | BOOKING_SOURCE_NOT_FOUND | "Booking to clone does not exist" |
| Create/edit blocked by hard-block conflict (see F2) | 409 | BOOKING_CONFLICT | "This booking conflicts with an existing booking for the selected location or resource" |

**API Surface (this feature):** see `Y1-api.md` §Booking for full request/response schemas (`POST /bookings`, `PUT /bookings/{id}`, `DELETE /bookings/{id}`, `POST /bookings/{id}/clone`, `GET /bookings`, `GET /bookings/{id}`).

**Schema Surface (this feature):** owns tables `bookings`, `booking_resources` — see `Y0-schema.md` §Booking. References `locations`/`resources` (F4, cross-service reference by ID only — no direct DB join, per service-isolation NFR) and `custom_field_values` (F5).
