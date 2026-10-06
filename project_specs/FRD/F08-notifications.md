## F8: Notifications

**Description:** The Notification Service consumes RabbitMQ domain events (`booking.created`, `booking.approved`, `booking.denied`, `password.reset.requested`) and sends email notifications equivalent to legacy synchronous email-sending, but with durable, retryable delivery so a notification failure can never block or silently drop the originating transaction.

**Terminology:**
- **Domain event:** A RabbitMQ message published by an upstream service (Booking Service for booking events, User/Identity Service for password-reset events) describing a state change.
- **Notification template:** The content/structure of an email for a given event type, equivalent in content/intent to legacy email templates.
- **Dead-letter queue (DLQ):** A queue receiving messages that fail delivery/processing after retry exhaustion, for manual inspection rather than silent loss.
- **Idempotency key:** A unique identifier on each event preventing duplicate notification delivery on redelivery/retry.

**Sub-features:**
- Consumption of `booking.created` events
- Consumption of `booking.approved` events
- Consumption of `booking.denied` events
- Consumption of `password.reset.requested` events
- Durable, retryable delivery via RabbitMQ
- Notification templates per event type

**Process:**
1. Upstream service (Booking Service for booking lifecycle events, F1/F3; User/Identity Service for password reset, F6) publishes a domain event to RabbitMQ with an idempotency key, event type, relevant entity ID(s), and actor context.
2. Notification Service consumes the event from its durable queue.
3. Service determines the recipient set for the event type: `[OPEN QUESTION — deferred to F0: exact recipient rules per event type — booking owner only, owner + all approvers, owner + location-specific approvers, or configurable per-location — PRD Open Question #5]`; interim default pending F0 confirmation: booking owner only for `booking.created`/`booking.approved`/`booking.denied`, and the requesting user only for `password.reset.requested`.
4. Service renders the appropriate notification template for the event type, substituting entity-specific data (booking title, time, location; or reset link/token).
5. Service sends the email via the configured SMTP/email provider integration (see `Y3-integrations.md`).
6. On send success, service acknowledges the message (removing it from the queue).
7. On send failure (transient, e.g., provider timeout), service retries delivery according to a backoff policy up to a configured maximum retry count.
8. On retry exhaustion, the message is routed to the dead-letter queue for manual inspection rather than being silently dropped, satisfying the "Notification delivery reliability" success metric (PRD Section 7: 100% of events result in delivered or retried-to-success notification, with no silent drops).
9. For auto-approved bookings (F3 §Process step 10), whether `booking.created` alone is published or `booking.approved` is also published is governed by F3's interim rule, and the Notification Service's behavior follows whatever event(s) it actually receives — it does not infer auto-approve status independently.
10. Redelivery of an already-processed event (e.g., after a consumer crash before acknowledgment) is detected via the idempotency key, and the service does not send a duplicate email for an event it has already successfully processed.

**Inputs:**
- Domain event payload: `{event_type, idempotency_key, entity_id, actor, timestamp, event_specific_data}` (e.g., for `booking.created`: booking title/time/location/owner; for `password.reset.requested`: user email, reset token/link).

**Outputs:**
- Sent email (subject, body, recipient(s)) per notification template.
- Delivery status record: `{event_id, status: sent|retrying|dead_lettered, attempt_count, last_attempted_at}` for observability.

**Validation:**
- Every consumed event must include a non-empty `idempotency_key`; events missing one are routed to the DLQ immediately rather than risking duplicate sends.
- An event already marked `sent` for a given `idempotency_key` is never re-sent, even if redelivered by RabbitMQ.
- Recipient resolution must not silently fail open (send to nobody) or silently fail closed (swallow the event) — an unresolvable recipient is treated as a processing failure and follows the retry/DLQ path (step 7–8).
- Notification template rendering failures (e.g., missing substitution data) are treated as processing failures, not swallowed.

**Error States:**
| Scenario | Impact | Required Action |
|---|---|---|
| SMTP/email provider transient failure | Delivery delayed | Retry per backoff policy (step 7) |
| Retry count exhausted | Delivery not confirmed | Route to DLQ (step 8); surfaced for manual/alerted inspection, never silently dropped |
| Event missing idempotency key | Cannot safely dedupe | Route to DLQ immediately; log for investigation |
| Recipient address cannot be resolved (e.g., deleted user) | Cannot deliver | Route to DLQ; does not block originating transaction (booking creation already succeeded) |
| Duplicate event delivery (RabbitMQ at-least-once redelivery) | Risk of duplicate email | Detected and suppressed via idempotency key (no customer-visible error) |

**API Surface (this feature):** No direct client-facing REST API for sending notifications (event-driven only). An internal admin/observability endpoint for inspecting delivery status/DLQ contents is listed in `Y1-api.md` §Notifications (`GET /notifications/delivery-status`, `GET /notifications/dead-letter`).

**Schema Surface (this feature):** owns table `notification_deliveries` (tracking idempotency keys, status, attempt counts) — see `Y0-schema.md` §Notification. Does not own `bookings` or `users` data; consumes event payloads only, per service-isolation NFR.
