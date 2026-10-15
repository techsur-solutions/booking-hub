---
status: complete
phase: 09-core-user-interface
source: 09-01-SUMMARY.md, 09-02-SUMMARY.md, 09-03-SUMMARY.md, 09-04-SUMMARY.md, 09-05-SUMMARY.md, 09-06-SUMMARY.md, 09-07-SUMMARY.md, 09-08-SUMMARY.md, 09-09-SUMMARY.md, 09-10-SUMMARY.md, 09-11-SUMMARY.md, 09-12-SUMMARY.md, 09-13-SUMMARY.md, 09-14-SUMMARY.md, 09-15-SUMMARY.md, 09-16-SUMMARY.md, 09-17-SUMMARY.md, 09-18-SUMMARY.md
started: 2026-10-15T00:58:00Z
updated: 2026-10-15T00:58:00Z
---

## Current Test

[testing paused - blocker found in Test 2, diagnosis in progress]

## Tests

### 1. Navigate the App with Permission-Gated Sidebar
expected: Opening /bookings shows responsive sidebar with permission-gated sections (Approvals absent without role_booking_approver, Admin absent without admin roles), header with UserMenu (name + logout), bottom nav on mobile, collapsible sidebar on tablet
result: pass

### 2. Switch Between Booking Views (Calendar/Day/List)
expected: Clicking Month/Day/List tabs switches views while preserving filters and date parameters in URL. Each view shows bookings with location colour-coding and conflict badges
result: issue
reported: "Getting this err: Failed to load bookings. Please try again."
severity: blocker

### 3. View Bookings in Calendar View
expected: Calendar View shows month grid with booking events colour-coded by location. Events with conflicts display conflict badge (⚠ icon). Clicking date opens Create Form, clicking event opens Detail Modal
result: [pending]

### 4. View Bookings in Day View
expected: Day View shows hourly slot grid (default 08:00-18:00) with bookings rendered as overlays. Location filter dropdown, date picker. Clicking empty slot opens Create Form pre-filled with time, clicking booking opens Detail Modal
result: [pending]

### 5. View Bookings in List View
expected: List View shows sortable table with 7 columns (Title, Location, Resources, Start/End, Status, Conflict Flags, Actions). Filters by date range, location, status, and search query. Export to CSV/JSON buttons download data
result: [pending]

### 6. Create a New Booking
expected: Clicking Create Booking button (or calendar slot/day slot) opens form with all fields: title, location dropdown, start/end datetime pickers, resources multi-select, description, custom fields (dynamic per location), recurring pattern. ConflictWarning appears if conflicts detected - hard-block (red, Save disabled) for non-approvers, soft-warn (yellow, Save enabled with confirmation) for approvers. Save shows StateConfirmationBanner (green "approved" or yellow "pending") and redirects to Calendar
result: [pending]

### 7. Edit an Existing Booking
expected: From Detail Modal, clicking Edit (owner/admin only) opens Edit Form. For recurring bookings, non-dismissible scope dialog appears first - select "this occurrence only" or "entire series". Form shows all current values, ConflictWarning re-validates on changes. Save shows scope-specific confirmation banner
result: [pending]

### 8. View Booking Details
expected: Clicking booking (in Calendar/Day/List) opens Detail Modal with: title, StatusBadge (pending yellow/approved green/denied red), Recurring chip if series, location with colour dot, start/end times, duration, resources list, description, custom field values, conflict section (if any) with clickable booking IDs, audit trail link. Actions: Edit/Delete (owner/admin only), Clone (always visible)
result: [pending]

### 9. Delete a Booking
expected: From Detail Modal, clicking Delete shows confirmation dialog. For non-recurring: single confirmation. For recurring: scope selection (this occurrence / entire series). After delete, booking removed from all views and modal closes
result: [pending]

### 10. Clone a Booking
expected: From Detail Modal, clicking Clone navigates to /bookings/create with all fields pre-filled, title suffixed "(Copy)", dates shifted +1 day
result: [pending]

### 11. Detect Booking Conflicts
expected: When creating/editing booking with overlapping location or resource, ConflictBanner appears showing grouped conflicts (📍 Location / 🔧 Resource) with clickable booking IDs. Non-approver: red banner, Save button disabled (hard-block). Approver: yellow banner, "Save Anyway" button shows confirmation dialog before allowing save (soft-warn)
result: [pending]

### 12. Approve Bookings from Queue
expected: User with role_booking_approver sees /approvals route with table of pending bookings (sorted by created_at asc), showing requester, location, resources, times, conflict badges. Click Approve shows confirmation (or conflict warning dialog for conflicted bookings), optimistically removes row. Queue auto-refreshes every 60s
result: [pending]

### 13. Deny Bookings from Queue
expected: From Approval Queue, clicking Deny shows dialog with optional reason textarea (max 500 chars). After confirming, booking marked denied, row optimistically removed, StateConfirmationBanner appears
result: [pending]

### 14. View My Account
expected: /account page loads user data (name, email, roles). Name is editable inline (edit → save → StateConfirmationBanner). Email is read-only with tooltip. Roles displayed as chips. "Change Password" button redirects to Keycloak account management
result: [pending]

### 15. Reset Password
expected: From Login page, "Forgot Password" link navigates to /password-reset/request. Enter email, submit shows "If that email exists, a reset link has been sent" (always identical message). Email contains link to /password-reset/complete?token=X. Complete page validates password (min 8, uppercase, lowercase, digit), shows clear error for expired/used/invalid token, redirects to /login on success
result: [pending]

### 16. View Display Board (Public Kiosk)
expected: /display-board shows full-screen view (no AppShell sidebar) with approved-only bookings grouped by date. Fixed top-right "✓ Approved Only" green badge. Footer link to /feeds. Optional ?location_id= filter. Auto-refreshes every 60s
result: [pending]

### 17. Subscribe to Booking Feeds
expected: /feeds page shows three feed sections (RSS2, iCal, JSON) with URLs and Copy buttons. Location filter dropdown updates all three URLs. Green banner states "No authentication required". Copy button copies URL to clipboard and shows toast confirmation
result: [pending]

### 18. Persist View State Across Navigation
expected: Selecting a date in Calendar → switching to Day View → date persists in URL (?date=2024-02-15). Applying filters in List View → switching to Calendar → filters persist. Browser back/forward buttons navigate through view history correctly
result: [pending]

## Summary

total: 18
passed: 1
issues: 1
pending: 16
skipped: 0

## Self-Check

boot: 200
data: seeded 1 additional precondition (resource conflict scenario)

**Test Users (from data-doctor):**
- testuser@example.com / TestPass123! — Regular user (NO approver role), owns test bookings
- approver@example.com / ApproverPass123! — WITH role_booking_approver
- uat.admin@example.test / UatAdmin123! — Admin with ALL roles (including approver)
- uat.user@example.test / UatUser123! — Regular user (NO approver role)

**Key Test Data:**
- Test 1 (Permissions): Login with uat.admin@example.test (has approver) vs uat.user@example.test (no approver)
- Test 2-5 (Views): 17 bookings across 5 locations with colors, multiple dates, varying statuses
- Test 6 (Create): Locations "Conference Room A" and "Executive Boardroom" have 3 custom fields each
- Test 7 (Edit Recurring): series_id 41000000-0000-0000-0000-000000000001 "RECURRING Weekly Team Sync"
- Test 11 (Conflicts): Location conflict - "Client Presentation" vs "PENDING-WITH-CONFLICT Interview Room"; Resource conflict - 2 bookings sharing Projector
- Test 12/13 (Approve/Deny): 3 pending bookings (one with conflict), login as uat.admin@example.test or approver@example.com
- Test 14-15 (Account/Reset): All 4 test users can authenticate
- Test 16-17 (Display/Feeds): 13 approved bookings, 5 locations for filtering

routes_probed: 3 ok / 0 failed
compose_health: all services up (api-gateway shows unhealthy but functional - health endpoint requires auth)

per_test:
  - test: 1
    verdict: skipped (needs human)
    note: "Navigation and permission gates require visual verification"
  - test: 2
    verdict: skipped (needs human)
    note: "View switching and data visibility require visual verification"
  - test: 3-18
    verdict: skipped (needs human)
    note: "UI interactions require human testing"

## Gaps

- truth: "Each view shows bookings with location colour-coding and conflict badges"
  status: failed
  reason: "User reported: Getting this err: Failed to load bookings. Please try again."
  severity: blocker
  test: 2
  source: user
  confidence: hypothesis
  root_cause: ""
  artifacts: []
  missing: []
  debug_session: ""
