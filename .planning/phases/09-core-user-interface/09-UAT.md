---
status: complete
phase: 09-core-user-interface
source: 09-01-SUMMARY.md, 09-02-SUMMARY.md, 09-03-SUMMARY.md, 09-04-SUMMARY.md, 09-05-SUMMARY.md, 09-06-SUMMARY.md, 09-07-SUMMARY.md, 09-08-SUMMARY.md, 09-09-SUMMARY.md, 09-10-SUMMARY.md, 09-11-SUMMARY.md, 09-12-SUMMARY.md, 09-13-SUMMARY.md, 09-14-SUMMARY.md, 09-15-SUMMARY.md
started: 2026-10-13T19:14:46+00:00
updated: 2026-10-13T19:36:26+00:00
---

## Current Test

[testing complete]

## Tests

### 1. Navigation and App Shell
expected: Sidebar navigation appears with sections: Bookings, Approvals (if role_booking_approver), Admin (if admin role). Clicking each nav item navigates to the correct page. User menu in header shows username, My Account link, and Logout action.
result: pass

### 2. Calendar View Rendering
expected: Calendar month view displays with booking events color-coded by location. Conflict badges (⚠ icon with count) appear on bookings with conflicts. Month/week toggle and prev/next/today navigation buttons work.
result: skipped
reason: "No bookings exist yet to test event display. Calendar UI renders properly. Will re-test after creating bookings."

### 3. Day View Hourly Slots
expected: Day view shows hourly time slots (based on calendar_min_time and calendar_max_time settings). Bookings appear as colored blocks overlaid on time slots. Location filter dropdown filters displayed bookings. Clicking an empty slot opens create form pre-filled with that time.
result: skipped
reason: "No bookings exist yet. Will test after creating bookings."

### 4. List View and Filtering
expected: List view shows sortable table with Title, Location, Resources, Start/End Time, Status, Conflict Flags, Actions columns. Date range picker, location multi-select, status checkboxes, and search box filter the table. Export CSV and Export JSON buttons download booking data.
result: skipped
reason: "No bookings exist yet. Will test after creating bookings."

### 5. View State Persistence
expected: URL contains view/date/filter params (e.g. /bookings?view=month&date=2024-02-15&location_id=loc-a). Switching views preserves date and filters. Browser back/forward buttons navigate between view states. Bookmarkable URL restores the exact view state.
result: skipped
reason: "Needs bookings with filters to test properly. Will test after creating bookings."

### 6. Create Booking Form
expected: Create form (/bookings/create) has Title, Location (dropdown), Start/End datetime pickers, Resources (multi-select), Description, Custom Fields (dynamic based on location), Recurring Pattern (daily/weekly + until date), and conflict warning banner. Pre-fill from URL params works (location_id, start_time, end_time).
result: issue
reported: "I can see /bookings/create but no way on UI to navigate to this page. Also new bookings page does not have any locations so I am not able to create one."
severity: major

### 7. Conflict Detection UI
expected: Non-approvers see red "Conflicts detected" banner with location/resource groupings and specific booking IDs listed — Save button disabled (hard-block). Approvers see yellow soft-warn banner with "Save Anyway" button. Clicking conflict booking IDs opens nested detail modal.
result: skipped
reason: "Requires bookings to test. Blocked by lack of seed data."

### 8. Edit Booking Form
expected: Editing a non-recurring booking loads the form directly. Editing a recurring booking shows non-dismissible scope dialog: "This occurrence only" or "Entire series" — user must choose before seeing the form. After save, confirmation banner shows scope-specific message.
result: skipped
reason: "Requires bookings to test. Blocked by lack of seed data."

### 9. Booking Detail Modal
expected: Clicking a booking event opens detail modal showing: title, status badge (pending yellow/approved green/denied red), recurring chip (if series), location with color dot, start/end times, duration, resources, description, custom field values, conflict section (if any), and permission-gated Edit/Delete/Clone actions.
result: skipped
reason: "Requires bookings to test. Blocked by lack of seed data."

### 10. Approval Queue
expected: /approvals page (visible only to role_booking_approver users) shows pending bookings table with inline conflict badges. Approve button: simple confirmation for non-conflicted, "Approve Despite Conflicts" two-step dialog for conflicted. Deny button: dialog with optional reason textarea. Row removed optimistically after action.
result: skipped
reason: "Requires pending bookings to test. Blocked by lack of seed data."

### 11. Display Board Kiosk Mode
expected: /display-board shows full-screen view (no sidebar) with approved bookings only, grouped by date. Fixed top-right green "✓ Approved Only" badge. Auto-refreshes every 60 seconds. Footer link to /feeds. Optional ?location_id= URL filter.
result: skipped
reason: "Requires approved bookings to test. Blocked by lack of seed data."

### 12. Feed Subscription URLs
expected: /feeds page lists RSS2, iCal, and JSON feed URLs with "Copy to Clipboard" buttons. Location filter dropdown updates all three URLs with ?location_id= param. Green banner says "No authentication required". Preview button opens feed URL in new tab.
result: skipped
reason: "Feed UI can be tested but content depends on bookings. Will test UI only."

### 13. Login Page
expected: /login shows email input (pre-filled from Remember Me cookie if set), Remember Me checkbox, and Login button. Clicking Login triggers Keycloak PKCE redirect. After login, redirects to ?redirect= param or /bookings?view=month.
result: pass

### 14. My Account Page
expected: /account shows editable Name field (Save/Cancel buttons), read-only Email, Keycloak roles as chips, Change Password button (opens Keycloak account management), and Audit History link. Successful name save shows green confirmation banner.
result: issue
reported: "Error: Failed to load account information. Please try again."
severity: blocker

### 15. Password Reset Flow
expected: /password-reset form: enter email → submit → always shows "If that email exists, a reset link has been sent" (even if email doesn't exist). /password-reset/complete?token=X: token validation, password/confirm fields (min 8 chars, 1 uppercase, 1 lowercase, 1 digit), error messages (expired token/passwords mismatch), success toast → redirect to /login.
result: pass

## Summary

total: 15
passed: 3
issues: 2
pending: 0
skipped: 10

## Self-Check

boot: 200
data: NOT RUN
routes_probed: 0 ok / 0 failed
cookie: n/a
browser_urls: none
repairs: none

## Gaps

- truth: "User can navigate to booking creation form from the UI"
  status: failed
  reason: "User reported: no way on UI to navigate to /bookings/create page"
  severity: major
  test: 6
  source: user
  confidence: hypothesis
  
- truth: "Location dropdown populated with available locations"
  status: failed
  reason: "User reported: new bookings page does not have any locations so I am not able to create one"
  severity: blocker
  test: 6
  source: user
  confidence: proven
  
- truth: "My Account page loads user information successfully"
  status: failed
  reason: "User reported: Error: Failed to load account information. Please try again."
  severity: blocker
  test: 14
  source: user
  confidence: proven
