## F2: Conflict Detection

**Description:** Conflict Detection evaluates whether a proposed or edited Booking overlaps in time with another existing, non-deleted Booking for the same Location or the same Resource, and surfaces the result consistently across calendar view, list view, and the API, so double-booking is caught before (and re-checked after) a Booking is saved.

**Terminology:**
- **Overlap:** Two time ranges `[start_a, end_a)` and `[start_b, end_b)` overlap if `start_a < end_b AND start_b < end_a` (half-open interval comparison; adjacent bookings that touch exactly at the boundary, e.g. one ends at 10:00 and another starts at 10:00, do not overlap).
- **Location-level conflict:** An overlap between two Bookings that reference the same `location_id`.
- **Resource-level conflict:** An overlap between two Bookings where at least one attached `resource_id` is shared between them.
- **Hard block:** A conflict that prevents the Booking from being saved at all.
- **Soft warning:** A conflict that is surfaced to the user but does not prevent saving.

**Sub-features:**
- Per-location overlap check
- Per-resource overlap check
- Conflict surfacing in calendar view (visual indicator)
- Conflict surfacing in list view (flagged row)
- Re-evaluation on every edit, not only at creation

**Process:**
1. On Booking create or edit (F1 §Process steps 4 and 9), the Booking Service invokes Conflict Detection with the proposed `location_id`, `resource_ids[]`, `start_time`, `end_time`, and (for edits) the Booking's own `id` so the Booking is excluded from comparison against itself.
2. Conflict Detection queries all non-deleted Bookings sharing the same `location_id` whose time range overlaps the proposed range (per the Overlap definition above), excluding the Booking being edited.
3. Conflict Detection separately queries all non-deleted Bookings sharing at least one `resource_id` in common with the proposed `resource_ids[]`, whose time range overlaps the proposed range, excluding the Booking being edited.
4. If multiple resources are attached, each resource is checked independently; the conflict result for the whole Booking includes the union of all per-resource conflicts found `[OPEN QUESTION — deferred to F0: whether legacy evaluates multi-resource conflicts per-resource independently (interim assumption) or treats any single resource conflict as blocking the whole booking identically — PRD Open Question #3; the distinction matters only for how the result is communicated, not whether a conflict is detected]`.
5. Conflict Detection returns a structured result: a boolean `has_conflict`, and a list of `conflicts[]` each citing the conflicting Booking's `id`, the conflict type (`location` or `resource`), and the specific `location_id`/`resource_id` involved.
6. The calling Booking Service (F1) applies the enforcement policy: `[OPEN QUESTION — deferred to F0: whether a detected conflict is a hard block (save rejected, HTTP 409) or a soft warning (save allowed, conflict surfaced for information) and whether this differs by permission/role — PRD Open Question #2]`. Interim default pending F0 confirmation: **hard block for all roles** (the conservative choice that cannot silently allow a double-booking regression); this default must be revisited and explicitly confirmed/corrected once F0 resolves the question, with a corresponding F13 test added either way.
7. For calendar and list view read endpoints (F1 §Process step 12), Conflict Detection is invoked in bulk for all Bookings in the requested range so each returned Booking representation includes its `conflict_flags[]`, ensuring conflicts are visible even for already-saved Bookings (e.g., if a conflict was allowed under a soft-warning policy, or created via direct API access).
8. Conflict Detection logic is identical regardless of entry point (UI calendar, UI list, or direct API call) per the cross-cutting NFR "Conflict-detection correctness" — there is exactly one conflict-evaluation code path in the Booking Service, invoked by all three entry points.

**Inputs:**
- `location_id` (UUID/long, required): Proposed/existing Booking's location.
- `resource_ids` (array of UUID/long, optional): Proposed/existing Booking's attached resources.
- `start_time` (ISO-8601 datetime, required).
- `end_time` (ISO-8601 datetime, required).
- `exclude_booking_id` (UUID/long, optional): The Booking's own ID, supplied on edit to exclude self-comparison.

**Outputs:**
- `has_conflict` (boolean).
- `conflicts[]`: array of `{conflicting_booking_id, conflict_type: "location"|"resource", location_id | resource_id}`.

**Validation:**
- A Booking is never compared against itself (`exclude_booking_id` is always honored when present).
- Deleted Bookings (`deleted_at IS NOT NULL`) are never included in conflict comparison.
- Denied Bookings `[OPEN QUESTION — deferred to F0: does legacy exclude denied bookings from conflict checks, since they never occupy the slot? Interim assumption: denied bookings are excluded from conflict comparison]` are excluded from conflict comparison.
- Overlap is evaluated using half-open interval semantics (a booking ending at time T does not conflict with one starting at time T).
- Conflict re-evaluation occurs on every edit (not cached from creation time), per PRD Section 5 F2 capability "re-evaluated on every booking edit, not only at creation."

**Error States:**
| Scenario | HTTP Status | Error Code | Message |
|---|---|---|---|
| Hard-block conflict detected on create/edit (pending F0 confirmation of enforcement policy, interim default) | 409 | BOOKING_CONFLICT | "This booking conflicts with an existing booking for the selected location or resource" |
| Conflict check requested with invalid/missing `location_id` and empty `resource_ids` | 400 | CONFLICT_CHECK_INVALID_INPUT | "At least a location or one resource must be specified for conflict checking" |

**API Surface (this feature):** Conflict Detection is primarily an internal Booking Service function invoked during `POST /bookings` and `PUT /bookings/{id}` (see F1); it is also exposed as a standalone check endpoint — see `Y1-api.md` §Booking, `POST /bookings/check-conflicts`.

**Schema Surface (this feature):** No dedicated table; queries the `bookings` and `booking_resources` tables owned by the Booking Service — see `Y0-schema.md` §Booking. Conflict queries are indexed on `(location_id, start_time, end_time)` and `(resource_id, start_time, end_time)` for performance.
