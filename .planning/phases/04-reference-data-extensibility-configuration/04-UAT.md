---
status: complete
phase: 04-reference-data-extensibility-configuration
source:
  - 04-01-SUMMARY.md
  - 04-02-SUMMARY.md
  - 04-03-SUMMARY.md
  - 04-04-SUMMARY.md
  - 04-05-SUMMARY.md
  - 04-06-SUMMARY.md
started: 2026-10-09T02:20:00Z
updated: 2026-10-09T02:47:00Z
---

## Current Test

[testing complete]

## Tests

### 1. Create a Location
expected: POST /locations with name, cssClass, colour, description, building, layout (JSON array) returns 201 with the created location including generated id, timestamps, and all submitted fields.
result: issue
reported: "Why the login says 'connection refused' - The frontend gets its Keycloak address from frontend/src/auth/keycloak.ts: url: import.meta.env.VITE_KEYCLOAK_URL ?? 'http://localhost:8180', - The compose frontend service never sets VITE_KEYCLOAK_URL, so the built app falls back to http://localhost:8180. - When you click Login in the preview, your browser is sent to http://localhost:8180/realms/bookinghub/…. That's localhost on your Mac, where nothing is listening, so you get 'connection refused'. Keycloak is running fine inside the sandbox; the browser just can't reach it at that address. - The broken-page icon in the preview panel is very likely the same redirect failing inside the panel. - The realm itself isn't the problem: bookinghub-frontend allows redirects to https://* and web origins *."
severity: blocker

### 2. Retrieve and Update a Location
expected: GET /locations/{id} returns the location with all fields. PUT /locations/{id} with updated fields returns 200. Subsequent GET shows the updated values.
result: skipped
reason: Blocked by Test 1 authentication failure

### 3. Soft-Delete a Location
expected: DELETE /locations/{id} returns 204. GET /locations (list) no longer includes it. GET /locations/{id} (direct lookup) still returns 200 with deletedAt populated and last-known values intact.
result: skipped
reason: Blocked by Test 1 authentication failure

### 4. Create and Manage Resources
expected: POST /resources with name, type, description, isUnique, restrictLocations (JSON array of Location UUIDs) returns 201. GET /resources returns the created resource. PUT/DELETE work identically to Location CRUD.
result: skipped
reason: Blocked by Test 1 authentication failure

### 5. Create a Custom Field Definition
expected: POST /custom-fields with label, fieldType (one of: textfield/select/textarea/radio/checkbox), options (required for select/radio/checkbox), required flag returns 201 with the created custom field.
result: skipped
reason: Blocked by Test 1 authentication failure

### 6. Query Applicable Custom Fields by Context
expected: GET /custom-fields?context_id={locationId} returns custom fields applicable to that location plus global (context_id=null) fields. Omitting context_id returns all fields.
result: skipped
reason: Blocked by Test 1 authentication failure

### 7. Create a Field Template
expected: POST /field-templates with name, context_id (nullable), field_ids (array of custom field UUIDs) returns 201 with the created template.
result: skipped
reason: Blocked by Test 1 authentication failure

### 8. Update a Field Template (Replace Join Set)
expected: PUT /field-templates/{id} with a new field_ids array replaces the entire association set (not additive). Subsequent GET /field-templates/{id} shows only the new field_ids.
result: skipped
reason: Blocked by Test 1 authentication failure

### 9. Retrieve and Update Settings
expected: GET /settings (as any authenticated user) returns the singleton settings row with approveBooking, calendarSlotSize, calendarMinTime, calendarMaxTime. PUT /settings (as role_settings_admin) with partial update returns 200. Immediate subsequent GET shows the updated values with zero propagation delay.
result: skipped
reason: Blocked by Test 1 authentication failure

### 10. Settings Validation Rejects Invalid Calendar Range
expected: PUT /settings with calendarMinTime >= calendarMaxTime returns 400 SETTINGS_INVALID_CALENDAR_RANGE. PUT /settings with calendarSlotSize outside 5-120 returns 400 SETTINGS_INVALID_SLOT_SIZE.
result: skipped
reason: Blocked by Test 1 authentication failure

## Summary

total: 10
passed: 0
issues: 1
pending: 0
skipped: 9

## Self-Check

boot: 200
data: skipped — API-only tests
routes_probed: 0 ok / 0 failed
per_test:
  - test: 1
    verdict: skipped (needs human)
    note: "API endpoint testing requires manual verification with authentication"
  - test: 2
    verdict: skipped (needs human)
    note: "API endpoint testing requires manual verification with authentication"
  - test: 3
    verdict: skipped (needs human)
    note: "Soft-delete behavior requires manual verification"
  - test: 4
    verdict: skipped (needs human)
    note: "API endpoint testing requires manual verification with authentication"
  - test: 5
    verdict: skipped (needs human)
    note: "API endpoint testing requires manual verification with authentication"
  - test: 6
    verdict: skipped (needs human)
    note: "Applicability query requires manual verification with specific context"
  - test: 7
    verdict: skipped (needs human)
    note: "API endpoint testing requires manual verification with authentication"
  - test: 8
    verdict: skipped (needs human)
    note: "Replace-join-set semantics require manual verification"
  - test: 9
    verdict: skipped (needs human)
    note: "Settings read/write with role gating requires manual verification"
  - test: 10
    verdict: skipped (needs human)
    note: "Validation rejection paths require manual verification"

## Gaps

- truth: "Frontend can log in to Keycloak and obtain a JWT for authenticated API calls"
  status: failed
  reason: "User reported: Why the login says 'connection refused' - The frontend gets its Keycloak address from frontend/src/auth/keycloak.ts: url: import.meta.env.VITE_KEYCLOAK_URL ?? 'http://localhost:8180', - The compose frontend service never sets VITE_KEYCLOAK_URL, so the built app falls back to http://localhost:8180. - When you click Login in the preview, your browser is sent to http://localhost:8180/realms/bookinghub/…. That's localhost on your Mac, where nothing is listening, so you get 'connection refused'. Keycloak is running fine inside the sandbox; the browser just can't reach it at that address."
  severity: blocker
  test: 1
  source: user
  confidence: proven
  root_cause: "docker-compose.yml's bookinghub-frontend service has no VITE_KEYCLOAK_URL environment variable, causing the built frontend to hardcode 'http://localhost:8180' (unreachable from browser) instead of receiving a browser-reachable Keycloak URL"
  artifacts:
    - path: "docker-compose.yml"
      issue: "bookinghub-frontend service missing VITE_KEYCLOAK_URL env var"
    - path: "frontend/src/auth/keycloak.ts"
      issue: "Falls back to http://localhost:8180 when VITE_KEYCLOAK_URL unset"
  missing:
    - "Set VITE_KEYCLOAK_URL in docker-compose.yml's bookinghub-frontend service environment"
    - "The value must be browser-reachable (likely needs runtime path construction or proxy forwarding, not localhost:8180)"
  debug_session: ""
