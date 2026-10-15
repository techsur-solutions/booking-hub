# Booking-Hub End-User Guide

## Introduction

Booking-Hub is a room and resource booking system for teams. This guide covers day-to-day booking
operations for end users — from logging in and navigating the interface to creating, editing, and
managing your bookings.

---

## Getting Started

### Logging In

1. Navigate to the Booking-Hub URL provided by your administrator
2. Click **Login** — you will be redirected to the authentication page
3. Enter your email and password, then click **Sign In**
4. After successful login, you will land on the **Calendar View**

> **First login?** Your administrator will have set a temporary password for you. After logging in,
> visit **My Account** to set a permanent password.

### Understanding the Interface

Booking-Hub has three main views for browsing bookings:

| View | Description | Best for |
|------|-------------|----------|
| **Calendar View** | Month grid showing all bookings with colour-coded locations | Getting a birds-eye view of the week or month |
| **Day View** | Hourly slot grid for a single day | Scheduling within a single day, finding open slots |
| **List View** | Filterable table of all bookings | Searching, filtering, bulk overview |

Switch between views using the **Calendar / Day / List** tabs in the top navigation bar.

#### Sidebar Navigation

- **Bookings** — Main calendar, day, and list views
- **Approvals** — Approval queue (visible only to users with approval permission)
- **Admin** — System administration (visible only to admins)

---

## Creating a Booking

### Quick Booking from Calendar

1. Open **Calendar View** or **Day View**
2. Click an empty time slot in the calendar grid
3. The **Create Booking** form opens with the date/time pre-filled from the slot you clicked
4. Fill in the booking details:
   - **Title** — A clear name for your booking (e.g., "Project Kickoff Meeting")
   - **Location** — Select the room or space (only non-deleted locations appear)
   - **Start Date/Time** and **End Date/Time** — Adjust if needed
   - **Resources** — Optional: select equipment or services from available resources
   - **Custom Fields** — Optional context-specific fields configured by your admin (e.g., "Catering Headcount")
   - **Recurring** — Optional: set a repeat pattern (daily, weekly, monthly) and end date
5. Click **Save**
6. A confirmation banner appears showing the booking status: **"Approved"** or **"Pending approval"**

### Booking from the List View

1. Open **List View**
2. Click the **Create Booking** button in the top-right corner of the workspace header
3. The form opens with today's date pre-filled at 9:00 AM
4. Complete the form as described above and click **Save**

### Understanding Booking Status

After saving a booking, you will see one of these statuses:

| Status | Meaning |
|--------|---------|
| **Approved** | Your booking is confirmed and visible to all users on the calendar |
| **Pending** | Your booking is awaiting manager approval before it appears as confirmed |
| **Denied** | Your booking was reviewed and declined by an approver |

The status badge appears in the booking detail modal and in List View.

---

## Understanding Auto-Approval

Your Booking-Hub administrator controls the approval workflow:

- **Auto-approve bookings is ON**: New bookings are immediately approved — no manual review needed
- **Auto-approve bookings is OFF**: New bookings enter **"Pending"** status and require a manager to approve them before they are confirmed

You can tell which mode is active by the status displayed after saving:
- **"Approved"** status immediately → auto-approve is ON
- **"Pending approval"** status → auto-approve is OFF, your booking is in the queue

---

## Editing a Booking

### Single-Occurrence Edit

To edit a non-recurring booking (or a single occurrence of a recurring series):

1. Click the booking in the Calendar, Day, or List view
2. The **Booking Detail Modal** opens — click **Edit**
3. Make your changes in the edit form
4. Click **Save**
5. A confirmation banner confirms the save: "Booking updated at HH:mm:ss"

> **Note:** You can only edit bookings you created. Admin users can edit any booking.

### Recurring Series Edit

For recurring bookings (e.g., weekly team standups), the system asks how far your edit should reach:

1. Click any occurrence of the recurring booking
2. Click **Edit** in the booking detail modal
3. A **Scope Confirmation Dialog** appears — you **must** select one of two options before the edit form opens:

   | Option | Effect |
   |--------|--------|
   | **This occurrence only** | Your changes apply ONLY to the occurrence you clicked. All other occurrences in the series remain unchanged. |
   | **Entire series** | Your changes apply to ALL future occurrences of this series, including the one you clicked. |

4. Select a scope (the dialog cannot be dismissed without choosing — this is intentional to prevent accidental edits)
5. The edit form opens; make your changes and click **Save**
6. A post-save summary banner confirms the scope: e.g., "Only this occurrence was changed"

> **Important:** The scope choice cannot be changed after saving. Choose carefully.

### Editing in List View

You can also edit bookings directly from List View:

1. Find the booking in the table
2. Click on the row to open the Booking Detail Modal
3. Click **Edit** — follow the same steps as above

---

## Deleting a Booking

### Deleting a Single Booking

1. Click the booking to open the Booking Detail Modal
2. Click **Delete**
3. Confirm the deletion in the confirmation dialog
4. The booking is removed from the calendar

### Deleting a Recurring Booking

When deleting a recurring booking occurrence:

1. Click any occurrence to open the Booking Detail Modal
2. Click **Delete**
3. The **Scope Confirmation Dialog** appears:
   - **This occurrence only** — Only the clicked occurrence is deleted; the rest of the series remains
   - **Entire series** — All occurrences are deleted

4. Select scope, confirm, and the booking(s) are removed

---

## Conflict Warnings

Booking-Hub checks for conflicts at save time. A **conflict** occurs when:

- Two bookings claim the same **location** at overlapping times
- Two bookings claim the same **unique resource** at overlapping times (unique resources can only serve one booking at a time)

### What You Will See

When a conflict is detected, a **Conflict Warning Banner** appears inside the booking form showing:
- The conflicting booking ID
- The conflict type (Location conflict or Resource conflict)

### How Conflicts Are Handled

| Your permission level | Conflict behaviour |
|-----------------------|-------------------|
| **Standard user** | Save is **blocked** until the conflict is resolved — change the time, location, or resource to find an open slot |
| **Approver** (role_booking_approver) | Conflict is a **soft warning** — you can acknowledge it and save anyway after reviewing the conflict details |

**To resolve a conflict:**
1. Check the conflicting booking ID in the warning banner
2. Adjust your start/end time, choose a different location, or deselect the conflicting resource
3. Once no conflicts are detected, the banner disappears and you can save

---

## Approval Workflow (Approvers Only)

Users with the **role_booking_approver** permission see the **Approvals** item in the sidebar.

### Accessing the Approval Queue

1. Click **Approvals** in the sidebar
2. The **Approval Queue** table shows all pending bookings
3. Each row includes:
   - Booking title, location, time range
   - Creator name
   - **Conflict badge** (if the pending booking conflicts with an existing approved booking)

### Approving a Booking

1. Find the pending booking in the Approvals queue
2. Review the conflict badge (if present)
3. Click **Approve**
4. A success toast notification confirms the action
5. The booking moves from "Pending" to "Approved" status and appears on the calendar
6. The booking is removed from your approval queue

### Denying a Booking

1. Find the pending booking in the Approvals queue
2. Click **Deny**
3. A confirmation toast appears
4. The booking status changes to "Denied" and is removed from the queue
5. Denied bookings do not appear on the calendar

> **Approver note:** As an approver, you can also save bookings with conflicts (with a confirmation step). Standard users cannot save conflicting bookings at all.

---

## My Account

### Changing Your Display Name

1. Click your name or avatar (top-right corner) → **My Account**
2. Update the **Display Name** field
3. Click **Save**

### Changing Your Password

1. Navigate to **My Account**
2. Find the **Password** section
3. Enter your current password, then your new password (twice)
4. Click **Update Password**

> **Password requirements:** Minimum 6 characters, must include at least one number and one lowercase letter.

> **Note:** Standard users cannot change their own email or roles — contact an admin for those changes.

---

## FAQ

**Q: Can I delete a recurring series all at once?**
A: Yes. Click any occurrence in the series, click Delete, then select "Entire series" in the scope dialog.

**Q: What happens if I book a conflicting slot?**
A: A conflict warning banner appears. If you are a standard user, save is blocked — adjust your time, location, or resource. If you are an approver, you can save with confirmation.

**Q: How do I know if my booking needs approval?**
A: After saving, check the status badge. "Approved" = auto-approved immediately. "Pending" = awaiting manager approval. The booking detail modal and List View both show the current status.

**Q: Why can't I edit someone else's booking?**
A: Standard users can only edit their own bookings. Admins can edit any booking. If you need a booking changed, contact an admin.

**Q: Can I subscribe to a calendar feed to see bookings in my external calendar app (Google Calendar, Outlook)?**
A: Yes. Navigate to the **Feeds** page (linked from the sidebar or footer) to find RSS2, iCal, and JSON feed URLs. Copy the iCal feed URL and add it as a calendar subscription in your preferred app.

**Q: What is a "recurring booking"?**
A: A recurring booking is one that repeats on a schedule (e.g., every Monday at 10am). All occurrences are created at once from a single booking form. Each occurrence is independent after creation — you can edit or delete individual ones without affecting the rest, or choose to edit/delete the entire series at once.

**Q: Can I see who created a booking?**
A: Yes. Click any booking to open the Booking Detail Modal — it shows the creator's name and the created-at timestamp.

---

## Getting Help

If you encounter an issue not covered in this guide:

1. Check the **Admin Guide** (linked in the Help section) if you have admin access
2. Contact your Booking-Hub administrator
3. For system issues, refer to the technical support contact provided by your organisation

---

*Booking-Hub End-User Guide — v2.0*
