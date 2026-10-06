## Y1: API Endpoints (Consolidated)

All endpoints are reached exclusively through Spring Cloud Gateway (single ingress, per F12). Paths below are logical API paths as exposed at the Gateway; the Gateway routes each prefix to its owning backend service. All request/response bodies are JSON unless noted (feed formats are the exception, per F9). All endpoints except `GET /feeds/*` and `POST /auth/login` require a valid Keycloak-issued bearer token; permission requirements reference F7 flags pending F0 confirmation of exact names/scopes.

### §Booking (Booking Service — F1, F2, F3)

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `POST /bookings` | `{title, location_id, start_time, end_time?, resource_ids?, custom_field_values?, recurrence?}` | `201` Booking repr (or series array) | `allowRoomBooking` |
| `GET /bookings` | query: `from`, `to`, `location_id?`, `resource_id?` | `200` array of Booking repr w/ `conflict_flags[]` | `viewRoomBooking` |
| `GET /bookings/{id}` | — | `200` full Booking repr (Eventdata-equivalent) | `viewRoomBooking` |
| `PUT /bookings/{id}` | `{title?, location_id?, start_time?, end_time?, resource_ids?, custom_field_values?, scope?}` | `200` updated Booking repr | `allowRoomBooking` (owner) or admin |
| `DELETE /bookings/{id}` | query/body: `scope?` | `204` | `allowRoomBooking` (owner) or admin |
| `POST /bookings/{id}/clone` | — | `201` new draft Booking repr | `allowRoomBooking` |
| `POST /bookings/{id}/approve` | — | `200` updated Booking repr | `allowApproveBooking` |
| `POST /bookings/{id}/deny` | `{denial_reason?}` | `200` updated Booking repr | `allowApproveBooking` |
| `POST /bookings/check-conflicts` | `{location_id?, resource_ids?, start_time, end_time, exclude_booking_id?}` | `200 {has_conflict, conflicts[]}` | `allowRoomBooking` |

### §Locations (Location & Resource Service — F4)

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /locations` | — | `200` array of Location repr | `accessCalendar` (read) |
| `POST /locations` | `{name, css_class?, building?, layout?}` | `201` Location repr | admin (location mgmt) |
| `GET /locations/{id}` | — | `200` Location repr | `accessCalendar` (read) |
| `PUT /locations/{id}` | `{name?, css_class?, building?, layout?}` | `200` Location repr | admin (location mgmt) |
| `DELETE /locations/{id}` | — | `204` | admin (location mgmt) |

### §Resources (Location & Resource Service — F4)

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /resources` | — | `200` array of Resource repr | `accessCalendar` (read) |
| `POST /resources` | `{name}` | `201` Resource repr | admin (resource mgmt) |
| `GET /resources/{id}` | — | `200` Resource repr | `accessCalendar` (read) |
| `PUT /resources/{id}` | `{name?}` | `200` Resource repr | admin (resource mgmt) |
| `DELETE /resources/{id}` | — | `204` | admin (resource mgmt) |

### §CustomFields (Custom Field Service — F5)

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /custom-fields` | — | `200` array of Custom Field repr | admin (custom field mgmt) |
| `POST /custom-fields` | `{label, field_type, options?}` | `201` Custom Field repr | admin (custom field mgmt) |
| `GET /custom-fields/{id}` | — | `200` Custom Field repr | admin (custom field mgmt) |
| `PUT /custom-fields/{id}` | `{label?, field_type?, options?}` | `200` Custom Field repr | admin (custom field mgmt) |
| `DELETE /custom-fields/{id}` | — | `204` | admin (custom field mgmt) |
| `GET /field-templates` | — | `200` array of Field Template repr | admin (custom field mgmt) |
| `POST /field-templates` | `{name, context_id?, field_ids?}` | `201` Field Template repr | admin (custom field mgmt) |
| `GET /field-templates/{id}` | — | `200` Field Template repr | admin (custom field mgmt) |
| `PUT /field-templates/{id}` | `{name?, context_id?, field_ids?}` | `200` Field Template repr | admin (custom field mgmt) |
| `DELETE /field-templates/{id}` | — | `204` | admin (custom field mgmt) |

### §Auth (User/Identity-adjacent Service + Keycloak — F6)

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `POST /auth/login` | `{username, password, remember_me?}` | `200 {access_token, refresh_token, expires_in, roles[]}` | public |
| `POST /auth/logout` | — | `204` | authenticated |
| `POST /auth/password-reset/request` | `{email}` | `202` (generic ack) | public |
| `POST /auth/password-reset/complete` | `{reset_token, new_password}` | `200` | public (token-bearing) |
| `POST /auth/password-change` | `{current_password, new_password}` | `200` | authenticated (self) |

### §Users (User/Identity-adjacent Service — F6)

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /users` | — | `200` array of User repr | admin (user mgmt) |
| `POST /users` | `{email, initial_role}` | `201` User repr | admin (user mgmt) |
| `GET /users/{id}` | — | `200` User repr | admin (user mgmt) or self |
| `PUT /users/{id}` | `{display_name?}` | `200` User repr | admin (user mgmt) or self |
| `PUT /users/{id}/roles` | `{roles[]}` | `200` User repr | admin (user mgmt) |
| `GET /users/me` | — | `200` User repr | authenticated (self) |
| `PUT /users/me` | `{display_name?}` | `200` User repr | authenticated (self) |

### §Permissions (Permission System — F7)

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /permissions` | — | `200` array of permission mapping entries | `accessPermissions` |
| `GET /roles/{role}/permissions` | — | `200 {role_name, permission_flags[]}` | `accessPermissions` |
| `PUT /roles/{role}/permissions` | `{permission_flags[]}` | `200` updated role-permission repr | `accessPermissions` |

### §Notifications (Notification Service — F8)

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /notifications/delivery-status` | query: `event_type?`, `status?` | `200` array of delivery status repr | admin (ops/observability) |
| `GET /notifications/dead-letter` | — | `200` array of dead-lettered entries | admin (ops/observability) |

### §Feeds (Public Feed Service — F9)

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /feeds/rss2` | query: `location_id?` | `200` RSS2 XML | `allowAPI` (pending F0 default-public confirmation) |
| `GET /feeds/ical` | query: `location_id?` | `200` iCal `.ics` | `allowAPI` (pending F0 default-public confirmation) |
| `GET /feeds/json` | query: `location_id?` | `200` JSON array | `allowAPI` (pending F0 default-public confirmation) |
| `GET /feeds/display-board` | query: `location_id?` | `200` HTML | `allowAPI` (pending F0 default-public confirmation) |

### §Settings (Settings Service — F10)

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /settings` | — | `200` Settings repr | authenticated (read scope TBD per F0) |
| `PUT /settings` | `{approve_booking?, calendar_slot_size?, calendar_min_time?, calendar_max_time?}` | `200` Settings repr | admin (settings mgmt) |

### §AuditLog (Audit Log Service — F11)

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /audit-log` | query: `entity_type?`, `entity_id?`, `actor?`, `date_from?`, `date_to?` | `200` paginated array of audit log entries | admin (log viewing, legacy `Logfiles` equivalent) |

### §Gateway Routing (F12)

| Route Prefix | Target Service | Coarse-grained Role/Scope Required |
|---|---|---|
| `/bookings/**` | Booking Service | any authenticated role carrying `viewRoomBooking` or `allowRoomBooking` |
| `/locations/**`, `/resources/**` | Location & Resource Service | authenticated (read); admin role for write methods |
| `/custom-fields/**`, `/field-templates/**` | Custom Field Service | admin role |
| `/auth/**` | User/Identity-adjacent Service + Keycloak | public (login/reset) or authenticated (logout/change) |
| `/users/**` | User/Identity-adjacent Service | admin role or self |
| `/permissions/**`, `/roles/**` | Permission System | `accessPermissions` |
| `/notifications/**` | Notification Service | admin/ops role |
| `/feeds/**` | Public Feed Service | `allowAPI` or public, per F9 pending confirmation |
| `/settings/**` | Settings Service | authenticated (read); admin (write) |
| `/audit-log/**` | Audit Log Service | admin role |
