# Booking-Hub Admin Guide

## Introduction

This guide covers administrative configuration of Booking-Hub. All sections require **admin
permissions** — the Admin section in the sidebar is only visible to users with an admin role.

> **Prerequisite:** You must be logged in with an account that has admin permissions. If you do not
> see "Admin" in the sidebar, contact another admin to grant you the appropriate role.

---

## Admin Navigation

The **Admin** section appears in the sidebar (or bottom navigation on mobile) for admin users.
It contains:

| Section | Purpose |
|---------|---------|
| **Locations Admin** | Create and manage rooms/spaces available for booking |
| **Resources Admin** | Create and manage equipment, services, and other bookable resources |
| **Custom Fields Builder** | Define custom fields and apply them to booking forms via field templates |
| **Users Admin** | Create, edit, and manage user accounts |
| **Roles & Permissions Matrix** | View the 17-role permission grid (read-only reference) |
| **Settings Admin** | Configure approval workflow, calendar display, and feed settings |
| **Audit Log Viewer** | Browse the immutable change history for all system entities |

---

## Location Management

Locations represent physical rooms or spaces that can be booked (e.g., "Conference Room A",
"Training Lab 2", "Boardroom").

### Creating a Location

1. Navigate to **Admin > Locations**
2. Click **Create Location**
3. Fill in the form:
   - **Name** *(required)* — The display name shown on the calendar and in booking forms
   - **Colour** — A hex colour code used to visually distinguish this location on the calendar grid
     (e.g., `#FF5733` for orange, `#4A90D9` for blue). Use the colour picker or type directly.
   - **Building** *(optional)* — Grouping metadata (e.g., "Main Campus", "Building B") for future filtering
   - **Layout** *(optional)* — Free-text description of the room layout (e.g., "Boardroom style, 12 seats")
4. Click **Save Location**
5. A confirmation banner appears at the top of the page: "Location saved at HH:mm:ss"

### Editing a Location

1. Navigate to **Admin > Locations**
2. Find the location in the table
3. Click the row or the **Edit** button
4. Make changes and click **Save Location**

### Soft-Delete Behavior

Locations are **soft-deleted** — they are hidden from booking forms but not permanently erased.
This preserves the historical integrity of all bookings that referenced the location.

To delete a location:

1. Navigate to **Admin > Locations** and find the location
2. Click **Delete**
3. A confirmation dialog appears: *"Existing bookings will keep this location reference. The
   location will be hidden from create/edit forms."*
4. Click **Confirm Delete**
5. The location disappears from the active list and from all booking forms
6. Historical bookings that used this location still display it correctly in list and audit views

> **To restore a soft-deleted location:** Currently there is no UI restore function. Contact your
> system administrator or database team if a location needs to be reinstated.

---

## Resource Management

Resources are items that can be attached to bookings — equipment, services, or anything that might
have limited availability (e.g., "Conference Phone", "Projector", "Catering Service").

### Creating a Resource

1. Navigate to **Admin > Resources**
2. Click **Create Resource**
3. Fill in the form:
   - **Name** *(required)* — The display name (e.g., "Conference Phone", "Video Camera")
   - **Type** *(optional)* — Classification label (e.g., "Equipment", "Service", "AV")
   - **Description** *(optional)* — Additional details about the resource
   - **Is Unique** — Checkbox controlling whether this resource can only be used by **one booking at a time**
     - **Checked (Unique):** Enables resource-level conflict detection. If booking A reserves this resource
       during 10:00–11:00, booking B cannot reserve it during the same window. Use for scarce one-of-a-kind
       items (conference phone, presenter clicker).
     - **Unchecked (Non-unique):** Multiple bookings can use this resource simultaneously. Use for items
       that exist in quantity (whiteboards, water jugs, general A/V cables).
   - **Restrict Locations** — Multi-select list of locations where this resource is available
     - **Leave all unchecked:** The resource is available to bookings in **all** locations
     - **Check specific locations:** The resource only appears in booking forms for those selected locations

4. Click **Save Resource**
5. A confirmation banner appears: "Resource saved at HH:mm:ss"

### Editing a Resource

1. Navigate to **Admin > Resources**
2. Find the resource in the table (the table shows Name, Type, Is Unique, and associated Locations)
3. Click the row or the **Edit** button
4. Make changes and click **Save Resource**

### Soft-Delete Behavior

Resources follow the same soft-delete policy as Locations:

- Deleting a resource hides it from booking forms and future create/edit flows
- Existing bookings that referenced the resource continue to display it
- A confirmation dialog warns you before deletion proceeds

---

## Custom Field Configuration

Custom fields allow you to add context-specific questions to booking forms. For example:
*"How many people will be catering for?"*, *"Will AV support be required?"*

The Custom Fields Builder has two tabs:
- **Field Definitions** — Define the individual fields (their type, label, options)
- **Field Templates** — Assign groups of fields to specific locations (or globally to all locations)

### Creating Field Definitions

1. Navigate to **Admin > Custom Fields > Field Definitions** tab
2. Click **Create Field Definition**
3. Fill in the form:
   - **Name** *(required)* — The field label shown on the booking form (e.g., "Catering Headcount",
     "AV Equipment Required")
   - **Field Type** — The input type for the field:

     | Type | Description | Options required? |
     |------|-------------|-------------------|
     | `textfield` | Short single-line text input | No |
     | `textarea` | Multi-line text input for longer answers | No |
     | `select` | Dropdown (user picks one option) | **Yes** |
     | `radio` | Radio buttons (user picks one option) | **Yes** |
     | `checkbox` | Checkboxes (user picks one or more options) | **Yes** |

   - **Options** *(shown for select/radio/checkbox only)* — Enter each option on its own line
     (e.g., "Yes", "No", "Not Applicable"). Maximum 100 options per field.
   - **Required** — Check to make this field mandatory in booking forms

4. The **Live Preview** panel on the right shows how the field renders as you type
5. Click **Save**
6. The field appears in the Field Definitions list

### Editing a Field Definition

1. Navigate to the **Field Definitions** tab
2. Click **Edit** next to the field you want to change
3. Make changes and click **Save**

> **Note:** Changing the field type from a choice type (select/radio/checkbox) to a text type
> (textfield/textarea) clears the options. Ensure no active templates rely on the old type
> before making this change.

### Creating Field Templates

Field templates control **which fields appear for which locations** when creating or editing a booking.

1. Navigate to **Admin > Custom Fields > Field Templates** tab
2. Click **Create Field Template**
3. Fill in the form:
   - **Context** — Select the scope:
     - **Global** (no location selected): This template's fields appear for bookings in **all** locations
     - **Specific Location**: This template's fields appear only when that location is selected in a booking form
   - **Associated Fields** — Multi-select the field definitions to include in this template

4. Click **Save**

**Example use case:**
- Create a field definition "Catering Headcount" (type: textfield)
- Create a field template with Context = "Conference Room A" and associate "Catering Headcount"
- Result: The "Catering Headcount" field appears **only** when "Conference Room A" is selected in the booking form. Other rooms do not show it.

---

## User Management

### Viewing Users

1. Navigate to **Admin > Users**
2. The user table shows: Display Name, Email, Roles assigned, and Created date
3. Use the search bar to filter by name or email

### Creating Users

1. Navigate to **Admin > Users**
2. Click **Create User**
3. Fill in the form:
   - **Display Name** — The user's visible name throughout the app
   - **Email** *(required, unique)* — Used for login; must not already exist in the system
   - **Password** — Set a temporary password. The user can change this via My Account after logging in.
   - **Roles** — Multi-select from the available roles (see the Role Reference below for a list)
4. Click **Create User**
5. Confirmation: "User created — temporary password set"

> **Security note:** The password you set here is a starting password. Advise users to change it
> on first login. Passwords must be at least 6 characters with at least one digit and one lowercase letter.

### Editing a User

Behaviour differs depending on whether you are editing **yourself** or **another user**:

| Editing | What you can change |
|---------|---------------------|
| **Another user (admin editing)** | Display Name, Email, Roles. Password change is **not** available here — it is the user's responsibility. |
| **Yourself (self-edit)** | Display Name only. Email and role changes require another admin. Password change is via **My Account**. |

To edit:
1. Navigate to **Admin > Users**
2. Find the user in the table
3. Click **Edit**
4. Make changes and click **Save**

### Role Reference

Booking-Hub uses 17 permission roles. Roles are assigned to users to control what they can see and do.

| Role (key) | Purpose |
|------------|---------|
| `role_calendar_viewer` | View the booking calendar |
| `role_room_booking` | Create and manage own bookings |
| `role_booking_approver` | Approve/deny pending bookings (and override conflicts) |
| `role_admin` | Access admin section |
| `role_location_admin` | Manage locations |
| `role_resource_admin` | Manage resources |
| `role_custom_field_admin` | Manage custom fields and field templates |
| `role_user_admin` | Manage user accounts |
| `role_settings_admin` | Manage system settings |
| `role_audit_viewer` | View the audit log |
| … and 7 more roles | Additional permissions (see Roles & Permissions Matrix) |

The **Roles & Permissions Matrix** page (Admin > Roles & Permissions) shows the full grid of all
17 roles and which permissions each grants. This page is **read-only** — role definitions are
managed in the identity provider (Keycloak Admin Console).

---

## Settings Configuration

Navigate to **Admin > Settings** to configure system-wide options.

### Approval Workflow

**Auto-approve bookings toggle**

- **ON (green):** All new bookings are automatically approved immediately upon creation.
  No manual approval step is required.
- **OFF (grey):** New bookings enter **"Pending"** status and must be manually approved by a user
  with `role_booking_approver` before they appear as confirmed on the calendar.

To change the setting:
1. Click the toggle switch to flip it ON or OFF
2. Click **Save Settings**
3. A **propagation indicator** (⚡ "Changes take effect immediately for new bookings") confirms
   the change is live — no server restart is required

> **Design note:** The settings system has no in-memory cache. Changes to the approval toggle
> take effect immediately for all subsequent bookings.

### Calendar Display

| Setting | Description | Values |
|---------|-------------|--------|
| **Time slot size** | Granularity of the calendar grid | 15 min, 30 min, 60 min |
| **Calendar start time** | Earliest time slot shown on the calendar (HH:mm) | e.g., 08:00 |
| **Calendar end time** | Latest time slot shown on the calendar (HH:mm) | e.g., 18:00 |

To change:
1. Adjust the dropdowns/time inputs
2. Click **Save Settings**

> **Note:** The calendar start/end times determine which part of the day is visible in Calendar and
> Day views. Bookings outside these times still exist but are not visible in the slot grid.

### Display & Feeds

| Button | Action |
|--------|--------|
| **Preview Display Board ↗** | Opens the public booking display board in a new tab (kiosk/signage view) |
| **Copy feed links** | Opens a modal with RSS2, iCal, and JSON feed URLs for external calendar subscriptions |

#### Feed URLs

The Feed Links modal shows three subscription URLs:

- **RSS2 Feed** — For RSS readers and news aggregators
- **iCal Feed** — For calendar apps (Google Calendar, Outlook, Apple Calendar). Copy and use
  "Subscribe to calendar" in your app.
- **JSON Feed** — For custom integrations and developers

Share these links with users who want to subscribe to the booking calendar from an external app.

---

## Audit Log

The Audit Log provides an immutable record of every change made in the system — bookings created,
approved, denied, deleted, settings changed, users created, and more.

### Viewing Change History

1. Navigate to **Admin > Audit Log**
2. The table shows: Date/Time, Entity Type, Entity ID, Action, Actor (who made the change),
   Before Values, After Values

### Filtering

Use the filter bar at the top to narrow results:

| Filter | Description |
|--------|-------------|
| **Entity Type** | Filter by what changed (booking, location, resource, user, settings, etc.) |
| **Actor** | Filter by which user made the change |
| **Date Range** | From/to date picker to limit the time window |

After setting filters, the table updates to show matching entries.

### Exporting to CSV

1. Set any desired filters
2. Click **Export to CSV**
3. A CSV file downloads with the filtered audit entries (or all entries if no filters set)

> **Note:** If the backend export endpoint is unavailable, the export falls back to converting the
> currently displayed JSON results to CSV. For large exports, use the backend endpoint directly.

### Deep-Linking from Booking Detail

When viewing a booking's detail modal, there is a link to **"View audit history"**. This opens the
Audit Log pre-filtered to that specific booking's entity ID, showing every change event for that
booking in chronological order.

### Immutability

Audit log entries are **read-only** by design. There is no edit or delete function in the UI,
the API, or the database. This provides a trustworthy compliance trail.

- Before/After values capture the state before and after each change
- All entries include a timestamp, actor identity, and action type
- The log cannot be altered or expunged by any user, including admins

---

## FAQ

**Q: Can I bulk-import locations, resources, or users?**
A: Not currently supported. All entities must be created individually through the Admin UI.
Bulk import is planned for a future version.

**Q: How do I reset a user's password?**
A: Go to **Admin > Users > Edit** for the user. Note that admins cannot set a password for another user
through the UI — users must change their own password via **My Account**. If a user is locked out,
contact your identity provider (Keycloak) administrator to reset the password at the IdP level.

**Q: Where do I configure which permissions each role grants?**
A: The Roles & Permissions Matrix page (Admin > Roles & Permissions) is a read-only reference.
Role permission definitions live in the identity provider (Keycloak Admin Console). Contact your
Keycloak administrator to add, remove, or modify role definitions.

**Q: Can I see who deleted a booking or changed a setting?**
A: Yes. All changes — including deletions and settings updates — are logged in the Audit Log with
the actor's identity, timestamp, and before/after values.

**Q: What happens to bookings when I delete a location?**
A: Locations are soft-deleted. Existing bookings keep their location reference and remain visible
in historical views. The deleted location is simply hidden from create/edit booking forms going forward.

**Q: How do I control which custom fields appear for a specific room?**
A: Create a Field Template with the specific room as the context (Admin > Custom Fields > Field Templates).
Assign the field definitions you want for that room. A global template (no specific location) applies
to all rooms.

**Q: Can admins override booking conflicts?**
A: Users with the `role_booking_approver` role can save bookings that have conflicts (with a confirmation
step). Standard users cannot. Admins who also have the approver role have this capability.

**Q: Is the Audit Log accessible to all admin users?**
A: Only users with the `role_audit_viewer` permission can access the Audit Log. Having `role_admin`
alone does not grant audit log access — it must be explicitly assigned.

---

## Quick Reference: Admin Tasks

| Task | Navigate to |
|------|-------------|
| Add a new room | Admin > Locations > Create Location |
| Add equipment | Admin > Resources > Create Resource |
| Add a custom question to bookings | Admin > Custom Fields > Field Definitions > Create |
| Assign custom questions to a room | Admin > Custom Fields > Field Templates > Create |
| Onboard a new employee | Admin > Users > Create User |
| Change approval mode | Admin > Settings > Approval Workflow |
| Get calendar feed URLs | Admin > Settings > Copy feed links |
| Review what changed yesterday | Admin > Audit Log (set date range filter) |
| See who last changed a booking | Admin > Audit Log > filter by Entity ID |

---

*Booking-Hub Admin Guide — v2.0*
