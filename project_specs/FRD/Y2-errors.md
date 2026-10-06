## Y2: Error Catalog (Cross-Feature)

This catalog consolidates error scenarios that span multiple features or are not specific to a single feature's primary flow. Feature-specific errors are documented in each `F{n}-*.md` chunk's Error States table; this catalog covers (a) cross-cutting platform-level errors common to every endpoint, and (b) errors that span two or more features. All errors return a JSON body of shape `{error_code, message, timestamp, path}` unless otherwise noted.

### Platform-level errors (apply to every endpoint, per F12/F7)

| Scenario | HTTP Status | Error Code | Message | Retry Guidance |
|---|---|---|---|---|
| Request without a bearer token to a protected route | 401 | AUTH_UNAUTHENTICATED | "Authentication required" | Do not retry without obtaining a valid token |
| Bearer token expired | 401 | AUTH_TOKEN_EXPIRED | "Session has expired; please log in again" | Refresh token or re-authenticate, then retry |
| Bearer token valid but insufficient role/scope for the route (Gateway-level) | 403 | GATEWAY_FORBIDDEN | "Access denied for this resource" | Do not retry; requires role change |
| Bearer token valid, Gateway allows, but service-level fine-grained check fails | 403 | ACTION_FORBIDDEN | "You do not have permission to perform this action" | Do not retry; requires role/ownership change |
| Malformed JSON request body | 400 | REQUEST_MALFORMED | "Request body is malformed or not valid JSON" | Fix request body and retry |
| Request to unknown route | 404 | ROUTE_NOT_FOUND | "The requested resource does not exist" | Do not retry |
| Backend service unavailable (Gateway cannot reach target) | 503 | SERVICE_UNAVAILABLE | "Service temporarily unavailable" | Retry with exponential backoff |
| Rate limit exceeded (if configured at Gateway) | 429 | RATE_LIMIT_EXCEEDED | "Too many requests; please slow down" | Retry after the indicated `Retry-After` interval |
| Unhandled server-side exception | 500 | INTERNAL_ERROR | "An unexpected error occurred" | Retry once; escalate if persistent |

### Cross-feature errors

| Scenario | HTTP Status | Error Code | Message | Spanning Features | Retry Guidance |
|---|---|---|---|---|---|
| Booking create/edit references a Location that exists but is soft-deleted | 404 | BOOKING_LOCATION_NOT_FOUND | "Specified location does not exist" | F1, F4 | Do not retry with same location_id |
| Booking create/edit references a Resource that exists but is soft-deleted | 404 | BOOKING_RESOURCE_NOT_FOUND | "One or more specified resources do not exist" | F1, F4 | Do not retry with same resource_ids |
| Booking create/edit submits a custom field value for a field not applicable to the booking's location context | 400 | CUSTOM_FIELD_VALUE_INVALID | "One or more submitted custom field values are invalid" | F1, F5 | Fix field selection and retry |
| Settings Service unreachable when Booking Service needs `approveBooking` at creation time | 503 | APPROVAL_SETTINGS_UNAVAILABLE | "Unable to determine approval requirement; try again shortly" | F1, F3, F10 | Retry with exponential backoff |
| Approve/deny action attempted by a user whose role mapping has not yet propagated after a recent role change | 403 | ACTION_FORBIDDEN | "You do not have permission to perform this action" | F3, F6, F7 | Re-authenticate to obtain a fresh token reflecting the new role, then retry |
| Domain event consumed by Notification Service or Audit Log Service references an entity (e.g., booking_id) that no longer exists due to a race with a subsequent delete | 200 (processed as best-effort; logged as a data-quality warning, not a client-facing error) | — | — | F1, F8, F11 | N/A (async; investigate via DLQ/observability if pattern recurs) |
| Public feed request filtered by `location_id` that does not exist | 200 (empty feed body, per F9 §Validation) | — | — | F9, F4 | N/A — not an error condition by design |
| Conflict check invoked with neither `location_id` nor `resource_ids` supplied | 400 | CONFLICT_CHECK_INVALID_INPUT | "At least a location or one resource must be specified for conflict checking" | F1, F2 | Fix request and retry |

### Notification delivery failure modes (F8, cross-referenced by F1/F3/F6 as event publishers)

| Scenario | Impact | Retry Guidance |
|---|---|---|
| SMTP/email provider transient failure | Delivery delayed, not lost | Automatic retry per backoff policy; no client action needed (async) |
| Retry count exhausted | Delivery unconfirmed | Routed to DLQ; requires manual/alerted operational follow-up, not a client retry |
| Duplicate event redelivery (RabbitMQ at-least-once semantics) | Risk of duplicate email | Suppressed via idempotency key; no client-visible error |

### Audit log consumption failure modes (F11, cross-referenced by all mutating features)

| Scenario | Impact | Retry Guidance |
|---|---|---|
| Audit Log Service fails to consume a domain event | Audit trail gap risk | Automatic retry via durable queue; DLQ + alerting on exhaustion, per F11 §Process step 6 |
