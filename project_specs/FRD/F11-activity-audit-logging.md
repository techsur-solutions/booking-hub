## F11: Activity/Audit Logging

**Description:** The Audit Log Service provides a system-wide, searchable record of state-changing actions across every other service — equivalent to legacy `Logfiles` — implemented as an event-sourced consumer of domain events (not direct per-service writes to a shared log table), consistent with the database-per-service constraint.

**Terminology:**
- **Audit log entry:** A single recorded fact: who (actor) did what (action type) to what (entity type/id) when (timestamp), optionally with before/after values.
- **Event-sourced audit trail:** The Audit Log Service builds its log exclusively by consuming domain events published by other services, never by direct database access to those services.

**Sub-features:**
- Capture of booking lifecycle events (create/edit/delete/approve/deny)
- Capture of location/resource management changes
- Capture of user/role changes
- Capture of settings changes
- Capture of permission/mapping changes
- Admin-facing log viewing with filtering

**Process:**
1. Every state-changing action in every other feature (F1, F3, F4, F5, F6, F7, F10) publishes a domain event describing the change (e.g., `booking.created`, `booking.updated`, `booking.deleted`, `booking.approved`, `booking.denied`, `location.created`, `location.updated`, `location.deleted`, `resource.created/updated/deleted`, `user.created/updated`, `role.assigned`, `settings.updated`, `permission.updated`).
2. The Audit Log Service consumes each event and writes a corresponding audit log entry: `{actor, timestamp, entity_type, entity_id, action_type, before_values, after_values}`.
3. Whether every domain event includes before/after values, or only the fact that a change occurred, is `[OPEN QUESTION — deferred to F0: does legacy Logfiles capture before/after values for edits, or only the fact of the edit? Is every controller action logged, or only a subset? — PRD Open Question #8]`; interim default pending F0 confirmation: capture both before and after values wherever the triggering event payload includes them (the more complete, conservative choice — omitting data is easier to retrofit than recovering data never captured).
4. Admin user (holding appropriate permission) views the audit log via a filterable list endpoint equivalent to legacy `Logfiles` controller, filterable by `entity_type`, `entity_id`, `actor`, and date range.
5. Audit log entries are immutable once written — no feature may edit or delete a prior audit log entry, including the Audit Log Service's own admin UI.
6. If the Audit Log Service fails to consume/process an event (e.g., transient failure), the event is retried via the same durable-queue/DLQ mechanism used by Notifications (F8), ensuring the "Auditability" NFR ("every state-changing action... traceable") is not silently violated by a missed event.

**Inputs:**
- Domain event payloads from all other services (see step 1 event-type list).
- Admin filter query: `entity_type` (string, optional), `entity_id` (UUID/long, optional), `actor` (string, optional), `date_from`/`date_to` (date, optional).

**Outputs:**
- Audit log entry representation: `{id, actor, timestamp, entity_type, entity_id, action_type, before_values (nullable), after_values (nullable)}`.
- Filtered list response: paginated array of audit log entries matching the admin's filter query.

**Validation:**
- Every state-changing action exercised in the F13 regression suite must produce a corresponding audit log entry (PRD Section 7 success metric: "100% of state-changing actions... produce a corresponding audit log entry").
- An audit log entry, once written, is never mutated or deleted by any API surface.
- Only users holding the admin permission equivalent to legacy `Logfiles` viewing access (F7) may query the audit log.
- A missed/dropped domain event (consumer failure) must surface via the DLQ mechanism (step 6) rather than silently producing an incomplete audit trail with no trace of the gap.

**Error States:**
| Scenario | HTTP Status | Error Code | Message |
|---|---|---|---|
| Caller lacks permission to view audit log | 403 | AUDIT_LOG_FORBIDDEN | "You do not have permission to view the audit log" |
| Filter query with invalid date range (`date_from` after `date_to`) | 400 | AUDIT_LOG_INVALID_DATE_RANGE | "date_from must be before date_to" |
| Event consumption failure after retry exhaustion | N/A (async) | — | Routed to DLQ; surfaced via observability/alerting, not a client-facing error |

**API Surface (this feature):** see `Y1-api.md` §AuditLog (`GET /audit-log` with query filters).

**Schema Surface (this feature):** owns table `audit_log_entries` — see `Y0-schema.md` §AuditLog. Receives event payloads only from other services; never performs a direct read of another service's database, per service-isolation NFR.
