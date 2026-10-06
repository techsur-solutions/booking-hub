
## 4. API Design

All endpoints are reached exclusively through **Spring Cloud Gateway** (single ingress, per §2.1). The path prefixes below are the logical API paths exposed at the Gateway; the Gateway's route table (§4.9) maps each prefix to its owning backend service. All request/response bodies are JSON unless noted (feed formats are the documented exception). Every endpoint except `GET /feeds/*` and `POST /auth/login`/`POST /auth/password-reset/*` requires a valid Keycloak-issued bearer token. Permission names reference the FRD F7 seed set (`accessCalendar`, `allowRoomBooking`, `viewRoomBooking`, `allowApproveBooking`, `accessPermissions`, `allowAPI`) pending F0's confirmation of the complete, exhaustive set.

### 4.1 Shared TypeScript Types

```typescript
// Shared primitives used across every service's API contracts.
type UUID = string;
type ISODateTime = string; // ISO-8601, e.g. "2026-11-03T14:00:00Z"
type ISODate = string;     // "2026-11-03"
type ISOTime = string;     // "08:00"

interface ApiError {
  error_code: string;
  message: string;
  timestamp: ISODateTime;
  path: string;
}

interface PaginatedResponse<T> {
  items: T[];
  total: number;
  page: number;
  page_size: number;
}

type BookingStatus = "pending" | "approved" | "denied";
type ConflictType = "location" | "resource";
type EditScope = "this_occurrence" | "whole_series";
type CustomFieldType = "text" | "number" | "date" | "select"; // exact enum pending F0 confirmation
type NotificationStatus = "pending" | "sent" | "retrying" | "dead_lettered";
type FeedFormat = "rss2" | "ical" | "json" | "display-board";
```

### 4.2 §Booking (booking-service — FRD F1, F2, F3)

```typescript
interface ConflictFlag {
  conflicting_booking_id: UUID;
  conflict_type: ConflictType;
  location_id?: UUID;
  resource_id?: UUID;
}

interface CustomFieldValueInput {
  field_id: UUID;
  value: string;
}

interface CustomFieldValue extends CustomFieldValueInput {
  label: string;
  field_type: CustomFieldType;
}

interface RecurrenceDefinition {
  // Exact shape pending F0 confirmation (PRD Open Question #1).
  // Interim assumption: weekly-by-day-of-week with an end date.
  pattern: "weekly";
  days_of_week: number[]; // 0 = Sunday .. 6 = Saturday
  end_date: ISODate;
}

interface BookingCreateRequest {
  title: string;
  location_id: UUID;
  start_time: ISODateTime;
  end_time?: ISODateTime; // defaults to start_time + 1h if omitted
  resource_ids?: UUID[];
  custom_field_values?: CustomFieldValueInput[];
  recurrence?: RecurrenceDefinition;
}

interface BookingUpdateRequest {
  title?: string;
  location_id?: UUID;
  start_time?: ISODateTime;
  end_time?: ISODateTime;
  resource_ids?: UUID[];
  custom_field_values?: CustomFieldValueInput[];
  scope?: EditScope; // required if booking belongs to a series
}

interface Booking {
  id: UUID;
  series_id: UUID | null;
  title: string;
  location_id: UUID;
  start_time: ISODateTime;
  end_time: ISODateTime;
  resources: { resource_id: UUID }[];
  custom_field_values: CustomFieldValue[];
  status: BookingStatus;
  owner_id: UUID;
  approved_by: UUID | null;
  approved_at: ISODateTime | null;
  denied_by: UUID | null;
  denied_at: ISODateTime | null;
  denial_reason: string | null;
  conflict_flags: ConflictFlag[];
  created_at: ISODateTime;
  updated_at: ISODateTime;
  deleted_at: ISODateTime | null;
}

interface ConflictCheckRequest {
  location_id?: UUID;
  resource_ids?: UUID[];
  start_time: ISODateTime;
  end_time: ISODateTime;
  exclude_booking_id?: UUID;
}

interface ConflictCheckResult {
  has_conflict: boolean;
  conflicts: ConflictFlag[];
}

interface DenyBookingRequest {
  denial_reason?: string;
}
```

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `POST /bookings` | `BookingCreateRequest` | `201 Booking` (or `Booking[]` for a series) | `allowRoomBooking` |
| `GET /bookings` | query: `from, to, location_id?, resource_id?` | `200 Booking[]` | `viewRoomBooking` |
| `GET /bookings/{id}` | — | `200 Booking` | `viewRoomBooking` |
| `PUT /bookings/{id}` | `BookingUpdateRequest` | `200 Booking` | `allowRoomBooking` (owner) or admin |
| `DELETE /bookings/{id}` | query/body: `scope?: EditScope` | `204` | `allowRoomBooking` (owner) or admin |
| `POST /bookings/{id}/clone` | — | `201 Booking` (draft) | `allowRoomBooking` |
| `POST /bookings/{id}/approve` | — | `200 Booking` | `allowApproveBooking` |
| `POST /bookings/{id}/deny` | `DenyBookingRequest` | `200 Booking` | `allowApproveBooking` |
| `POST /bookings/check-conflicts` | `ConflictCheckRequest` | `200 ConflictCheckResult` | `allowRoomBooking` |

### 4.3 §Locations / §Resources (locations-resources-service — FRD F4)

```typescript
interface LocationUpsertRequest {
  name: string;
  css_class?: string;
  building?: string;
  layout?: Record<string, unknown>; // exact shape pending F0 confirmation
}

interface Location {
  id: UUID;
  name: string;
  css_class: string | null;
  building: string | null;
  layout: Record<string, unknown> | null;
  created_at: ISODateTime;
  updated_at: ISODateTime;
  deleted_at: ISODateTime | null;
}

interface ResourceUpsertRequest {
  name: string;
}

interface Resource {
  id: UUID;
  name: string;
  created_at: ISODateTime;
  updated_at: ISODateTime;
  deleted_at: ISODateTime | null;
}
```

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /locations` | — | `200 Location[]` | `accessCalendar` (read) |
| `POST /locations` | `LocationUpsertRequest` | `201 Location` | admin (location mgmt) |
| `GET /locations/{id}` | — | `200 Location` | `accessCalendar` (read) |
| `PUT /locations/{id}` | `Partial<LocationUpsertRequest>` | `200 Location` | admin (location mgmt) |
| `DELETE /locations/{id}` | — | `204` | admin (location mgmt) |
| `GET /resources` | — | `200 Resource[]` | `accessCalendar` (read) |
| `POST /resources` | `ResourceUpsertRequest` | `201 Resource` | admin (resource mgmt) |
| `GET /resources/{id}` | — | `200 Resource` | `accessCalendar` (read) |
| `PUT /resources/{id}` | `Partial<ResourceUpsertRequest>` | `200 Resource` | admin (resource mgmt) |
| `DELETE /resources/{id}` | — | `204` | admin (resource mgmt) |

### 4.4 §CustomFields (custom-field-service — FRD F5)

```typescript
interface CustomFieldUpsertRequest {
  label: string;
  field_type: CustomFieldType;
  options?: string[]; // required when field_type === "select"
}

interface CustomField {
  id: UUID;
  label: string;
  field_type: CustomFieldType;
  options: string[] | null;
  created_at: ISODateTime;
  updated_at: ISODateTime;
}

interface FieldTemplateUpsertRequest {
  name: string;
  context_id?: UUID; // e.g. a Location id; omitted = global
  field_ids?: UUID[];
}

interface FieldTemplate {
  id: UUID;
  name: string;
  context_id: UUID | null;
  field_ids: UUID[];
  created_at: ISODateTime;
  updated_at: ISODateTime;
}
```

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /custom-fields` | — | `200 CustomField[]` | admin (custom field mgmt) |
| `POST /custom-fields` | `CustomFieldUpsertRequest` | `201 CustomField` | admin (custom field mgmt) |
| `GET /custom-fields/{id}` | — | `200 CustomField` | admin (custom field mgmt) |
| `PUT /custom-fields/{id}` | `Partial<CustomFieldUpsertRequest>` | `200 CustomField` | admin (custom field mgmt) |
| `DELETE /custom-fields/{id}` | — | `204` | admin (custom field mgmt) |
| `GET /field-templates` | — | `200 FieldTemplate[]` | admin (custom field mgmt) |
| `POST /field-templates` | `FieldTemplateUpsertRequest` | `201 FieldTemplate` | admin (custom field mgmt) |
| `GET /field-templates/{id}` | — | `200 FieldTemplate` | admin (custom field mgmt) |
| `PUT /field-templates/{id}` | `Partial<FieldTemplateUpsertRequest>` | `200 FieldTemplate` | admin (custom field mgmt) |
| `DELETE /field-templates/{id}` | — | `204` | admin (custom field mgmt) |

### 4.5 §Auth / §Users (users-permissions-service + Keycloak — FRD F6)

```typescript
interface LoginRequest {
  username: string;
  password: string;
  remember_me?: boolean;
}

interface LoginResponse {
  access_token: string;
  refresh_token: string;
  expires_in: number; // seconds
  roles: string[];
}

interface PasswordResetRequest {
  email: string;
}

interface PasswordResetCompleteRequest {
  reset_token: string;
  new_password: string;
}

interface PasswordChangeRequest {
  current_password: string;
  new_password: string;
}

interface UserCreateRequest {
  email: string;
  initial_role: string;
}

interface UserUpdateRequest {
  display_name?: string;
}

interface User {
  id: UUID; // == Keycloak `sub` claim
  email: string;
  display_name: string | null;
  roles: string[];
  created_at: ISODateTime;
  updated_at: ISODateTime;
}

interface RoleAssignmentRequest {
  roles: string[];
}
```

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `POST /auth/login` | `LoginRequest` | `200 LoginResponse` | public |
| `POST /auth/logout` | — | `204` | authenticated |
| `POST /auth/password-reset/request` | `PasswordResetRequest` | `202` (generic ack) | public |
| `POST /auth/password-reset/complete` | `PasswordResetCompleteRequest` | `200` | public (token-bearing) |
| `POST /auth/password-change` | `PasswordChangeRequest` | `200` | authenticated (self) |
| `GET /users` | — | `200 User[]` | admin (user mgmt) |
| `POST /users` | `UserCreateRequest` | `201 User` | admin (user mgmt) |
| `GET /users/{id}` | — | `200 User` | admin (user mgmt) or self |
| `PUT /users/{id}` | `UserUpdateRequest` | `200 User` | admin (user mgmt) or self |
| `PUT /users/{id}/roles` | `RoleAssignmentRequest` | `200 User` | admin (user mgmt) |
| `GET /users/me` | — | `200 User` | authenticated (self) |
| `PUT /users/me` | `UserUpdateRequest` | `200 User` | authenticated (self) |

### 4.6 §Permissions (users-permissions-service, permission module — FRD F7)

```typescript
interface PermissionMappingEntry {
  legacy_flag: string;
  keycloak_role: string;
  gated_actions: string[];
  confirmed: boolean;
}

interface RolePermissions {
  role_name: string;
  permission_flags: string[];
}

interface RolePermissionsUpdateRequest {
  permission_flags: string[];
}
```

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /permissions` | — | `200 PermissionMappingEntry[]` | `accessPermissions` |
| `GET /roles/{role}/permissions` | — | `200 RolePermissions` | `accessPermissions` |
| `PUT /roles/{role}/permissions` | `RolePermissionsUpdateRequest` | `200 RolePermissions` | `accessPermissions` |

### 4.7 §Notifications (notifications-service — FRD F8; observability-only, no client write API)

```typescript
interface NotificationDeliveryStatus {
  event_id: UUID;
  event_type: string;
  entity_id: UUID;
  status: NotificationStatus;
  attempt_count: number;
  last_attempted_at: ISODateTime | null;
}
```

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /notifications/delivery-status` | query: `event_type?, status?` | `200 NotificationDeliveryStatus[]` | admin (ops/observability) |
| `GET /notifications/dead-letter` | — | `200 NotificationDeliveryStatus[]` | admin (ops/observability) |

### 4.8 §Feeds (feeds-service — FRD F9)

```typescript
interface FeedBookingSummary {
  booking_id: UUID;
  title: string;
  location: { id: UUID; name: string };
  start_time: ISODateTime;
  end_time: ISODateTime;
}
```

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /feeds/rss2` | query: `location_id?` | `200` RSS2 XML | `allowAPI` (pending F0 default-public confirmation) |
| `GET /feeds/ical` | query: `location_id?` | `200` iCal `.ics` | `allowAPI` (pending F0 default-public confirmation) |
| `GET /feeds/json` | query: `location_id?` | `200 FeedBookingSummary[]` | `allowAPI` (pending F0 default-public confirmation) |
| `GET /feeds/display-board` | query: `location_id?` | `200` HTML | `allowAPI` (pending F0 default-public confirmation) |

### 4.9 §Settings (settings-service — FRD F10)

```typescript
interface SettingsUpdateRequest {
  approve_booking?: boolean;
  calendar_slot_size?: number; // minutes, positive integer
  calendar_min_time?: ISOTime;
  calendar_max_time?: ISOTime;
}

interface Settings {
  approve_booking: boolean;
  calendar_slot_size: number;
  calendar_min_time: ISOTime;
  calendar_max_time: ISOTime;
  updated_at: ISODateTime;
  updated_by: UUID | null;
}
```

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /settings` | — | `200 Settings` | authenticated (read scope TBD per F0) |
| `PUT /settings` | `SettingsUpdateRequest` | `200 Settings` | admin (settings mgmt) |

### 4.10 §AuditLog (audit-log-service — FRD F11)

```typescript
interface AuditLogEntry {
  id: UUID;
  actor_id: UUID;
  occurred_at: ISODateTime;
  entity_type: string;
  entity_id: UUID;
  action_type: string;
  before_values: Record<string, unknown> | null;
  after_values: Record<string, unknown> | null;
}

interface AuditLogQuery {
  entity_type?: string;
  entity_id?: UUID;
  actor?: UUID;
  date_from?: ISODate;
  date_to?: ISODate;
}
```

| Method & Path | Request | Response | Permission |
|---|---|---|---|
| `GET /audit-log` | query: `AuditLogQuery` | `200 PaginatedResponse<AuditLogEntry>` | admin (log viewing, legacy `Logfiles` equivalent) |

### 4.11 Gateway Routing Table

| Route Prefix | Target Service | Coarse-Grained Role/Scope Required |
|---|---|---|
| `/bookings/**` | booking-service | authenticated with `viewRoomBooking` or `allowRoomBooking` |
| `/locations/**`, `/resources/**` | locations-resources-service | authenticated (read); admin role for write methods |
| `/custom-fields/**`, `/field-templates/**` | custom-field-service | admin role |
| `/auth/**` | users-permissions-service (+ Keycloak) | public (login/reset) or authenticated (logout/change) |
| `/users/**` | users-permissions-service | admin role or self |
| `/permissions/**`, `/roles/**` | users-permissions-service | `accessPermissions` |
| `/notifications/**` | notifications-service | admin/ops role |
| `/feeds/**` | feeds-service | `allowAPI` or public, per F9 pending confirmation |
| `/settings/**` | settings-service | authenticated (read); admin (write) |
| `/audit-log/**` | audit-log-service | admin role |

### 4.12 Cross-Cutting Error Contract

Every endpoint returns errors in the shared `ApiError` shape (§4.1). Platform-level errors (apply to every route) and cross-feature errors are cataloged in full in `FRD-BookingHub.md` §Y2 (`AUTH_UNAUTHENTICATED` 401, `AUTH_TOKEN_EXPIRED` 401, `GATEWAY_FORBIDDEN` 403, `ACTION_FORBIDDEN` 403, `REQUEST_MALFORMED` 400, `ROUTE_NOT_FOUND` 404, `SERVICE_UNAVAILABLE` 503, `RATE_LIMIT_EXCEEDED` 429, `INTERNAL_ERROR` 500, plus every feature-specific code listed per-feature in the FRD). TechArch does not redefine these — the Gateway and every service implement a shared `ApiError`-shaped exception handler (`@ControllerAdvice` in each Spring Boot service; a `GlobalFilter`/`ErrorWebExceptionHandler` in the WebFlux-based Gateway) so the contract is identical regardless of which layer raises the error.
