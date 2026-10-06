## F4: Locations & Resources Management

**Description:** The Location & Resource Service owns CRUD management of bookable Locations (rooms/spaces, legacy `Location`) and bookable Resources (equipment, legacy `Resource`), the two reference entities that Bookings attach to. Both are admin-managed and consumed by the Booking Service (F1) for conflict-detection scoping (F2) and calendar rendering.

**Terminology:**
- **Location:** A bookable room/space with display metadata (name, CSS class/colour, building grouping, layout).
- **Resource:** Bookable equipment attachable to a Booking independently of Location (e.g., a projector, a specific chair count).
- **Building:** A grouping attribute on Location used for organizing locations in admin UI and potentially filtering.

**Sub-features:**
- Location CRUD (create/read/update/delete)
- Resource CRUD (create/read/update/delete)
- Location display metadata (CSS class/colour, building, layout)
- Admin-only access gating via permission (F7)

**Process:**
1. Admin user (holding the appropriate permission, F7) submits a Location creation request with `name`, `css_class`/`colour`, `building`, and optional `layout` metadata.
2. Service validates the request (see Validation) and persists the Location, returning its representation including a generated `id`.
3. Admin user submits a Resource creation request with `name` and any resource-specific metadata `[OPEN QUESTION — deferred to F0: full legacy Resource field set beyond name is unconfirmed]`.
4. Service validates and persists the Resource, returning its representation including a generated `id`.
5. Admin user may edit an existing Location or Resource; service re-validates and persists updates, recording the change in the Audit Log (F11).
6. Admin user may delete a Location or Resource. Service checks for existing non-deleted Bookings referencing the entity; if any exist, deletion behavior is `[OPEN QUESTION — deferred to F0: does legacy block deletion of a Location/Resource in use, cascade-delete dependent bookings, or allow orphaned references? Interim assumption: soft-delete only (deleted_at set), existing Booking references remain intact and display using the Location/Resource's last-known values]`.
7. The Booking Service (F1) and Conflict Detection (F2) reference Locations/Resources by `id` only, via a read API exposed by this service (no direct cross-service DB access, per service-isolation NFR).
8. Calendar rendering (F1 §Process step 12) uses a Location's `css_class`/`colour` metadata to render the Location consistently across calendar views.

**Inputs:**
- `name` (string, required): Location or Resource display name.
- `css_class` (string, optional): CSS class or colour token used for calendar rendering (Location only).
- `building` (string, optional): Building grouping (Location only).
- `layout` (object/string, optional): Layout metadata (Location only) `[OPEN QUESTION — deferred to F0: exact shape/purpose of legacy "layout" metadata unconfirmed]`.

**Outputs:**
- Location representation: `id`, `name`, `css_class`, `building`, `layout`, `created_at`, `updated_at`, `deleted_at` (nullable).
- Resource representation: `id`, `name`, `created_at`, `updated_at`, `deleted_at` (nullable).
- List endpoints return all non-deleted Locations/Resources (paginated or full list per admin UI needs).

**Validation:**
- `name` is required and non-empty for both Location and Resource.
- `name` must be unique among non-deleted Locations `[OPEN QUESTION — deferred to F0: uniqueness constraint unconfirmed]`; interim: not enforced beyond non-empty.
- Only users holding the admin permission equivalent to legacy Location/Resource management access (F7) may create/edit/delete.
- Deletion of a Location/Resource referenced by non-deleted Bookings follows the interim soft-delete policy in Process step 6, pending F0 confirmation.

**Error States:**
| Scenario | HTTP Status | Error Code | Message |
|---|---|---|---|
| Missing/empty `name` | 400 | LOCATION_NAME_REQUIRED / RESOURCE_NAME_REQUIRED | "Name is required" |
| Caller lacks admin permission | 403 | LOCATION_RESOURCE_FORBIDDEN | "You do not have permission to manage locations/resources" |
| Target Location/Resource `id` not found | 404 | LOCATION_NOT_FOUND / RESOURCE_NOT_FOUND | "Location/Resource not found" |
| Delete requested for entity referenced by active bookings (pending F0 policy confirmation) | 409 (interim, if block policy confirmed) | LOCATION_IN_USE / RESOURCE_IN_USE | "Cannot delete: referenced by existing bookings" |

**API Surface (this feature):** see `Y1-api.md` §Locations and §Resources (`GET/POST /locations`, `GET/PUT/DELETE /locations/{id}`, `GET/POST /resources`, `GET/PUT/DELETE /resources/{id}`).

**Schema Surface (this feature):** owns tables `locations`, `resources` — see `Y0-schema.md` §LocationResource. Referenced by the Booking Service's `bookings.location_id` and `booking_resources.resource_id` via ID reference only (no FK across service database boundaries).
