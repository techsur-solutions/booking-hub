---
status: testing
phase: 10-admin-interface-polish
source: 10-01-SUMMARY.md, 10-02-SUMMARY.md, 10-03-SUMMARY.md, 10-04-SUMMARY.md, 10-05-SUMMARY.md, 10-06-SUMMARY.md, 10-07-SUMMARY.md, 10-08-SUMMARY.md, 10-09-SUMMARY.md, 10-10-SUMMARY.md, 10-11-SUMMARY.md, 10-12-SUMMARY.md, 10-13-SUMMARY.md, 10-14-SUMMARY.md
started: 2026-10-15T14:00:00Z
updated: 2026-10-15T14:00:00Z
---

## Current Test

number: 1
position: 1 of 22
name: Locations Admin Table View
expected: |
  Navigate to `/admin/locations`. Table displays with columns: Name, Colour (visual swatch, not hex text), Building, Layout, Actions. Colour swatches are visible colored blocks, not just text. Edit and Delete buttons appear in Actions column.
awaiting: user response

## Tests

### 1. Locations Admin Table View
expected: Navigate to `/admin/locations`. Table displays with columns: Name, Colour (visual swatch, not hex text), Building, Layout, Actions. Colour swatches are visible colored blocks, not just text. Edit and Delete buttons appear in Actions column.
result: [pending]

### 2. Location Create Form
expected: Click "Create Location" button. Form displays with 6 fields: name (text), class (text), colour (hex picker showing color swatch), description (textarea), building (text), layouts (text). ColourPicker shows both text input and visual color preview. Filling and submitting shows success banner "Location created" then navigates back to table.
result: [pending]

### 3. Location Edit Form
expected: Click Edit on an existing location. Form pre-fills with current values. Colour picker shows existing colour value with visual swatch. Changes save successfully with confirmation banner.
result: [pending]

### 4. Location Soft Delete
expected: Click Delete on a location. Dialog appears with message "Existing bookings will keep this location reference". Confirming removes location from active table (but bookings retain the reference).
result: [pending]

### 5. Resources Admin Table View
expected: Navigate to `/admin/resources`. Table shows: Name, Type, Description, Is Unique (checkmark ✓ or dash –), Restrict Locations (location names or "All locations"), Actions columns.
result: [pending]

### 6. Resource Create with is_unique
expected: Click "Create Resource". Form shows name, type, description, is_unique checkbox, restrict_locations multi-select checkbox list. Create a resource with is_unique checked. Saves successfully with confirmation banner.
result: [pending]

### 7. Resource restrict_locations
expected: In Resource Create/Edit form, restrict_locations shows checkbox list of active locations (not soft-deleted ones). Selecting specific locations saves those UUIDs. Edit shows previously selected locations pre-checked.
result: [pending]

### 8. Custom Field Definitions Tab
expected: Navigate to `/admin/custom-fields`. Defaults to Definitions tab. Table shows Name, Field Type, Options, Actions. Click Create shows form with field_type dropdown (textfield/select/textarea/radio/checkbox), required checkbox, conditional options textarea.
result: [pending]

### 9. Custom Field Options Validation
expected: In Custom Field Definition form, select "select" field type. Options textarea appears. Try to save without entering options → validation error: "Options required for select type". Enter options → saves successfully. Switch to "textfield" → options textarea hidden.
result: [pending]

### 10. Custom Field Live Preview
expected: In Custom Field Definition form, as you change field_type and add options, FieldPreview component updates live showing how the field will render (disabled text input, select dropdown, radio buttons, etc.).
result: [pending]

### 11. Field Templates Tab
expected: Click Templates tab in Custom Field Builder. Table shows Name, Context (location name or "Global"), Associated Fields. Create Template form has location dropdown and field definitions checkbox list.
result: [pending]

### 12. Users Admin Table and Create
expected: Navigate to `/admin/users`. Table shows Name, Email, Roles (comma-separated), Active badge, Edit/Deactivate actions. Click "Create User" shows form with display_name, email, temporary password, 17 Keycloak roles multi-select.
result: [pending]

### 13. User Edit - Admin vs Self Restrictions
expected: As admin, edit another user → form shows all fields including roles multi-select. Edit your own user (same ID as logged-in user) → roles fieldset is structurally absent (not just disabled), only display_name and email (read-only) visible. No password field in either mode.
result: [pending]

### 14. Role & Permission Matrix
expected: Navigate to `/admin/roles`. Read-only grid displays 17 permission flags as rows, roles as columns. Granted permissions show ✓, denied show – with different styling. "View in Keycloak Admin" link opens Keycloak Admin Console in new tab.
result: [pending]

### 15. Settings Admin Form
expected: Navigate to `/admin/settings`. Single-page form with 3 sections: Approval Workflow (approveBooking toggle), Calendar Display (slot_size, min_time, max_time), Display & Feeds (Preview Display Board button, Copy feed links). Toggle approveBooking → propagation indicator ⚡ "Changes take effect immediately" appears, auto-dismisses after 5s. Save shows confirmation banner.
result: [pending]

### 16. Settings Feed Links Modal
expected: In Settings Admin, click "Copy feed links". Modal opens showing RSS2, iCal, JSON URLs with copy-to-clipboard buttons for each.
result: [pending]

### 17. Audit Log Viewer with Filters
expected: Navigate to `/admin/audit-log`. Table shows Timestamp, Actor, Entity Type, Entity ID (truncated with tooltip), Action, Before/After columns. Filters: entity type dropdown, actor text search, date range. Apply filters → table updates. CSV export button downloads `audit-log-YYYY-MM-DD.csv`.
result: [pending]

### 18. Audit Log Deep Link
expected: From a Booking Detail Modal (outside audit log), click "View history" link with `?entity_type=booking&entity_id=X`. Audit Log Viewer opens with filters pre-populated to that booking's events.
result: [pending]

### 19. Desktop Layout (>1024px)
expected: View app at 1280px and 1920px viewport widths. Sidebar always visible (no hamburger menu). Calendar shows full month grid (4-5 weeks, 7 days). Admin tables show all columns without horizontal scroll. Forms use two-column layout where appropriate.
result: [pending]

### 20. Tablet Layout (768-1024px)
expected: View app at 768px (iPad portrait) and 1024px viewport widths. Sidebar hidden by default with hamburger ☰ in header. Clicking hamburger opens sidebar as overlay/drawer. Forms stack to single-column. Admin tables remain accessible (may scroll horizontally if needed).
result: [pending]

### 21. Mobile Layout (<768px)
expected: View app at 375px (iPhone SE) and 414px (larger phone). Bottom navigation bar shows Calendar, Feeds, Account icons (Admin/Approvals if user has roles). Forms stack vertically with full-width inputs. Day View and List View accessible via tab switching.
result: [pending]

### 22. Accessibility Features
expected: Tab through any admin form → visible focus indicators (2px solid #0066CC outline or equivalent). Press Tab on page load → "Skip to main content" link appears, pressing Enter jumps to main content. All tables have proper column headers. All interactive elements keyboard-navigable. Colour contrasts meet WCAG AA (text at least 4.5:1, UI elements at least 3:1).
result: [pending]

## Summary

total: 22
passed: 0
issues: 0
pending: 22
skipped: 0

## Gaps

[none yet]
