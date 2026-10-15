import { test, expect } from '@playwright/test';

/**
 * Booking Detail Modal End-to-End Tests (UI-BOOK-03)
 *
 * Tests run with VITE_E2E_MODE=true (playwright.config.ts webServer.env).
 * Keycloak roles injected via page.addInitScript() → window.__E2E_ROLES__.
 * API responses mocked via page.route('http://localhost:8080/**').
 *
 * Covered:
 *   1. Open from Calendar View — click booking, modal opens with title
 *   2. Status badge — pending/approved/denied rendering
 *   3. Conflict information — banner + list with booking IDs
 *   4. Edit action (owner) — Edit button visible, click navigates to /bookings/{id}/edit
 *   5. Delete action (non-recurring) — confirmation dialog, API call, toast, close
 *   6. Delete action (recurring) — scope dialog with occurrence/series options
 *   7. Clone action — navigate to /bookings/create with pre-fill params
 *   8. Permission denial — non-owner/non-admin: Edit/Delete hidden, Clone visible
 *   9. Audit trail link — click "View history" navigates to /admin/audit
 */

// ── Fixture data ──────────────────────────────────────────────────────────────

const CURRENT_USER_ID = 'user-owner-001';

const LOCATION_1 = {
  id: 'loc-001',
  name: 'Main Hall',
  colour: '#3b82f6',
  building: 'Building A',
  layouts: null,
  deleted_at: null,
};

const BOOKING_PENDING = {
  id: 'bk-pending',
  series_id: null,
  location_id: 'loc-001',
  resource_ids: [],
  title: 'Pending Meeting',
  description: 'Awaiting approval',
  start_time: new Date(Date.now() + 86_400_000).toISOString(), // tomorrow
  end_time: new Date(Date.now() + 86_400_000 + 3_600_000).toISOString(),
  status: 'pending' as const,
  owner_id: CURRENT_USER_ID,
  conflict_flags: [],
  custom_field_values: [],
  created_at: new Date(Date.now() - 3_600_000).toISOString(), // 1 hour ago
  updated_at: new Date(Date.now() - 3_600_000).toISOString(),
  deleted_at: null,
};

const BOOKING_APPROVED = {
  id: 'bk-approved',
  series_id: null,
  location_id: 'loc-001',
  resource_ids: [],
  title: 'Approved Meeting',
  description: null,
  start_time: new Date(Date.now() + 86_400_000).toISOString(),
  end_time: new Date(Date.now() + 86_400_000 + 3_600_000).toISOString(),
  status: 'approved' as const,
  owner_id: CURRENT_USER_ID,
  conflict_flags: [],
  custom_field_values: [],
  created_at: new Date(Date.now() - 7_200_000).toISOString(), // 2 hours ago
  updated_at: new Date(Date.now() - 3_600_000).toISOString(), // approved 1 hour ago
  deleted_at: null,
};

const BOOKING_DENIED = {
  id: 'bk-denied',
  series_id: null,
  location_id: 'loc-001',
  resource_ids: [],
  title: 'Denied Meeting',
  description: null,
  start_time: new Date(Date.now() + 86_400_000).toISOString(),
  end_time: new Date(Date.now() + 86_400_000 + 3_600_000).toISOString(),
  status: 'denied' as const,
  owner_id: CURRENT_USER_ID,
  conflict_flags: [],
  custom_field_values: [],
  created_at: new Date(Date.now() - 7_200_000).toISOString(),
  updated_at: new Date(Date.now() - 1_800_000).toISOString(), // denied 30 min ago
  deleted_at: null,
};

const BOOKING_WITH_CONFLICT = {
  id: 'bk-conflicted',
  series_id: null,
  location_id: 'loc-001',
  resource_ids: [],
  title: 'Conflicted Meeting',
  description: null,
  start_time: new Date(Date.now() + 86_400_000).toISOString(),
  end_time: new Date(Date.now() + 86_400_000 + 3_600_000).toISOString(),
  status: 'pending' as const,
  owner_id: CURRENT_USER_ID,
  conflict_flags: [
    { conflicting_booking_id: 'bk-approved', conflict_type: 'location' },
  ],
  custom_field_values: [],
  created_at: new Date().toISOString(),
  updated_at: new Date().toISOString(),
  deleted_at: null,
};

const BOOKING_RECURRING = {
  id: 'bk-recurring',
  series_id: 'series-001',
  location_id: 'loc-001',
  resource_ids: [],
  title: 'Weekly Standup',
  description: null,
  start_time: new Date(Date.now() + 86_400_000).toISOString(),
  end_time: new Date(Date.now() + 86_400_000 + 3_600_000).toISOString(),
  status: 'approved' as const,
  owner_id: CURRENT_USER_ID,
  conflict_flags: [],
  custom_field_values: [],
  created_at: new Date().toISOString(),
  updated_at: new Date().toISOString(),
  deleted_at: null,
};

const BOOKING_OTHER_OWNER = {
  id: 'bk-other',
  series_id: null,
  location_id: 'loc-001',
  resource_ids: [],
  title: "Other Person's Meeting",
  description: null,
  start_time: new Date(Date.now() + 86_400_000).toISOString(),
  end_time: new Date(Date.now() + 86_400_000 + 3_600_000).toISOString(),
  status: 'approved' as const,
  owner_id: 'user-other-002', // Different owner
  conflict_flags: [],
  custom_field_values: [],
  created_at: new Date().toISOString(),
  updated_at: new Date().toISOString(),
  deleted_at: null,
};

// ── Setup helpers ─────────────────────────────────────────────────────────────

type SetupOptions = {
  bookings?: unknown[];
  bookingById?: Record<string, unknown>;
  roles?: string[];
  deleteStatus?: number;
};

/**
 * Navigate to Calendar View with mocked API. Injects E2E roles and a user ID
 * into the Keycloak mock so permission checks work.
 */
async function gotoCalendarWithMocks(
  page: import('@playwright/test').Page,
  options: SetupOptions = {},
) {
  const {
    bookings = [BOOKING_PENDING],
    bookingById = {},
    roles = ['role_calendar_viewer'],
    deleteStatus = 204,
  } = options;

  // Inject both roles AND the mock user subject (for owner-check in BookingDetailModal)
  await page.addInitScript(
    ({
      injectedRoles,
      userId,
    }: {
      injectedRoles: string[];
      userId: string;
    }) => {
      (window as unknown as Record<string, unknown>).__E2E_ROLES__ = injectedRoles;
      // Expose the user ID so keycloak.subject returns current user
      (window as unknown as Record<string, unknown>).__E2E_USER_ID__ = userId;
    },
    { injectedRoles: roles, userId: CURRENT_USER_ID },
  );

  // Mock all API gateway calls
  await page.route('http://localhost:8080/**', (route) => {
    const url = route.request().url();
    const method = route.request().method();

    // DELETE /bookings/:id
    if (method === 'DELETE' && url.includes('/bookings/')) {
      route.fulfill({ status: deleteStatus, body: '' });
      return;
    }

    // GET /bookings/:id (specific booking by ID)
    const bookingByIdMatch = url.match(/\/bookings\/([^/?]+)(\?|$)/);
    if (bookingByIdMatch && !url.includes('/check-conflicts')) {
      const bookingId = bookingByIdMatch[1];
      const specificBooking = bookingById[bookingId];
      if (specificBooking) {
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(specificBooking),
        });
      } else {
        // Return 404 for unknown IDs
        route.fulfill({
          status: 404,
          contentType: 'application/json',
          body: JSON.stringify({ error: 'Not found' }),
        });
      }
      return;
    }

    // GET /locations/:id
    if (url.match(/\/locations\/[^/?]+/)) {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(LOCATION_1),
      });
      return;
    }

    // GET /bookings (list) or /settings or /locations
    if (url.includes('/settings')) {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: 1,
          approve_booking: false,
          calendar_slot_size: 30,
          calendar_min_time: '08:00',
          calendar_max_time: '18:00',
        }),
      });
    } else if (url.includes('/locations')) {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([LOCATION_1]),
      });
    } else if (url.includes('/bookings')) {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(bookings),
      });
    } else {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: '[]',
      });
    }
  });

  await page.goto('/bookings?view=month');
  await page.waitForSelector('.fc', { timeout: 15_000 });
}

/**
 * Click a booking event in FullCalendar and wait for the detail modal to open.
 */
async function clickBookingAndWaitForModal(
  page: import('@playwright/test').Page,
  bookingTitle: string,
) {
  // Wait for FullCalendar events to render
  await page.waitForSelector('.fc-event', { timeout: 10_000 });

  // Click the event with the matching title
  const event = page.locator('.fc-event-title', { hasText: bookingTitle }).first();
  await expect(event).toBeVisible({ timeout: 10_000 });
  await event.click();

  // Wait for the modal to appear
  await page.waitForSelector('[data-testid="booking-detail-modal"]', { timeout: 5_000 });
}

// ── Tests ─────────────────────────────────────────────────────────────────────

/**
 * Test 1: Open from Calendar View
 */
test.describe('1: Open from Calendar View', () => {
  test('clicking booking event opens BookingDetailModal with correct title', async ({
    page,
  }) => {
    await gotoCalendarWithMocks(page, {
      bookings: [BOOKING_PENDING],
      bookingById: { 'bk-pending': BOOKING_PENDING },
    });

    await clickBookingAndWaitForModal(page, 'Pending Meeting');

    // Modal must show the booking title
    const title = page.locator('[data-testid="booking-title"]');
    await expect(title).toContainText('Pending Meeting');
  });

  test('detail modal renders all booking fields', async ({ page }) => {
    await gotoCalendarWithMocks(page, {
      bookings: [BOOKING_APPROVED],
      bookingById: { 'bk-approved': BOOKING_APPROVED },
    });

    await clickBookingAndWaitForModal(page, 'Approved Meeting');

    // All detail fields must be present
    await expect(page.locator('[data-testid="booking-location"]')).toBeVisible();
    await expect(page.locator('[data-testid="booking-start-time"]')).toBeVisible();
    await expect(page.locator('[data-testid="booking-end-time"]')).toBeVisible();
    await expect(page.locator('[data-testid="booking-duration"]')).toBeVisible();
  });
});

/**
 * Test 2: Status badge rendering
 */
test.describe('2: Status badge rendering', () => {
  test('pending booking shows yellow "Pending approval" badge', async ({ page }) => {
    await gotoCalendarWithMocks(page, {
      bookings: [BOOKING_PENDING],
      bookingById: { 'bk-pending': BOOKING_PENDING },
    });

    await clickBookingAndWaitForModal(page, 'Pending Meeting');

    const badge = page.locator('[data-testid="status-badge"]');
    await expect(badge).toBeVisible();
    await expect(badge).toContainText('Pending approval');
    // Pending badge has status-badge--pending class (yellow)
    await expect(badge).toHaveClass(/status-badge--pending/);
  });

  test('approved booking shows green "Approved at" badge', async ({ page }) => {
    await gotoCalendarWithMocks(page, {
      bookings: [BOOKING_APPROVED],
      bookingById: { 'bk-approved': BOOKING_APPROVED },
    });

    await clickBookingAndWaitForModal(page, 'Approved Meeting');

    const badge = page.locator('[data-testid="status-badge"]');
    await expect(badge).toBeVisible();
    await expect(badge).toContainText('Approved at');
    await expect(badge).toHaveClass(/status-badge--approved/);
  });

  test('denied booking shows red "Denied at" badge', async ({ page }) => {
    await gotoCalendarWithMocks(page, {
      bookings: [BOOKING_DENIED],
      bookingById: { 'bk-denied': BOOKING_DENIED },
    });

    await clickBookingAndWaitForModal(page, 'Denied Meeting');

    const badge = page.locator('[data-testid="status-badge"]');
    await expect(badge).toBeVisible();
    await expect(badge).toContainText('Denied at');
    await expect(badge).toHaveClass(/status-badge--denied/);
  });
});

/**
 * Test 3: Conflict information
 */
test.describe('3: Conflict information', () => {
  test('shows red "Conflicts detected" banner when conflict_flags present', async ({
    page,
  }) => {
    await gotoCalendarWithMocks(page, {
      bookings: [BOOKING_WITH_CONFLICT],
      bookingById: { 'bk-conflicted': BOOKING_WITH_CONFLICT },
    });

    await clickBookingAndWaitForModal(page, 'Conflicted Meeting');

    const conflictSection = page.locator('[data-testid="conflict-section"]');
    await expect(conflictSection).toBeVisible();
    await expect(conflictSection).toContainText('Conflicts detected');
  });

  test('lists conflicting booking IDs with conflict type', async ({ page }) => {
    await gotoCalendarWithMocks(page, {
      bookings: [BOOKING_WITH_CONFLICT],
      bookingById: {
        'bk-conflicted': BOOKING_WITH_CONFLICT,
        'bk-approved': BOOKING_APPROVED,
      },
    });

    await clickBookingAndWaitForModal(page, 'Conflicted Meeting');

    // The conflict link for the conflicting booking ID must be present
    const conflictLink = page.locator('[data-testid="conflict-link-bk-approved"]');
    await expect(conflictLink).toBeVisible();
    await expect(conflictLink).toContainText('bk-approved');

    // Conflict type is shown
    const conflictSection = page.locator('[data-testid="conflict-section"]');
    await expect(conflictSection).toContainText('location conflict');
  });

  test('clicking conflict booking link opens nested Detail Modal', async ({
    page,
  }) => {
    await gotoCalendarWithMocks(page, {
      bookings: [BOOKING_WITH_CONFLICT],
      bookingById: {
        'bk-conflicted': BOOKING_WITH_CONFLICT,
        'bk-approved': BOOKING_APPROVED,
      },
    });

    await clickBookingAndWaitForModal(page, 'Conflicted Meeting');

    // Click conflict link → opens nested modal for bk-approved
    await page.locator('[data-testid="conflict-link-bk-approved"]').click();

    // Nested modal opens with the conflicting booking's title
    await page.waitForSelector('[data-testid="booking-title"]', { timeout: 5_000 });
    // There should be two modals; the second shows 'Approved Meeting'
    const titles = page.locator('[data-testid="booking-title"]');
    await expect(titles.last()).toContainText('Approved Meeting');
  });
});

/**
 * Test 4: Edit action (owner)
 */
test.describe('4: Edit action (owner)', () => {
  test('Edit button visible when user is booking owner', async ({ page }) => {
    await gotoCalendarWithMocks(page, {
      bookings: [BOOKING_PENDING],
      bookingById: { 'bk-pending': BOOKING_PENDING },
      roles: ['role_calendar_viewer'],
    });

    await clickBookingAndWaitForModal(page, 'Pending Meeting');

    const editBtn = page.locator('[data-testid="btn-edit"]');
    await expect(editBtn).toBeVisible();
  });

  test('clicking Edit navigates to /bookings/{id}/edit', async ({ page }) => {
    await gotoCalendarWithMocks(page, {
      bookings: [BOOKING_PENDING],
      bookingById: { 'bk-pending': BOOKING_PENDING },
      roles: ['role_calendar_viewer'],
    });

    await clickBookingAndWaitForModal(page, 'Pending Meeting');

    // Click Edit button
    await page.locator('[data-testid="btn-edit"]').click();

    // Should navigate to edit page (route might not fully render since BookingEdit
    // needs API for the booking, but URL should change)
    await expect(page).toHaveURL(/\/bookings\/bk-pending\/edit/);
  });
});

/**
 * Test 5: Delete action (non-recurring)
 */
test.describe('5: Delete action (non-recurring)', () => {
  test('Delete button visible for owner', async ({ page }) => {
    await gotoCalendarWithMocks(page, {
      bookings: [BOOKING_PENDING],
      bookingById: { 'bk-pending': BOOKING_PENDING },
    });

    await clickBookingAndWaitForModal(page, 'Pending Meeting');
    await expect(page.locator('[data-testid="btn-delete"]')).toBeVisible();
  });

  test('clicking Delete shows confirmation dialog', async ({ page }) => {
    await gotoCalendarWithMocks(page, {
      bookings: [BOOKING_PENDING],
      bookingById: { 'bk-pending': BOOKING_PENDING },
    });

    await clickBookingAndWaitForModal(page, 'Pending Meeting');
    await page.locator('[data-testid="btn-delete"]').click();

    // Confirmation dialog appears
    const dialog = page.locator('[data-testid="delete-confirm-dialog"]');
    await expect(dialog).toBeVisible();
    await expect(dialog).toContainText('Delete this booking?');
    await expect(dialog).toContainText('cannot be undone');
  });

  test('confirming delete closes modal and shows toast', async ({ page }) => {
    await gotoCalendarWithMocks(page, {
      bookings: [BOOKING_PENDING],
      bookingById: { 'bk-pending': BOOKING_PENDING },
      deleteStatus: 204,
    });

    await clickBookingAndWaitForModal(page, 'Pending Meeting');
    await page.locator('[data-testid="btn-delete"]').click();

    // Confirm delete
    await page.locator('[data-testid="btn-confirm-delete"]').click();

    // Modal closes (no longer visible)
    await expect(
      page.locator('[data-testid="booking-detail-modal"]'),
    ).not.toBeVisible({ timeout: 5_000 });

    // Toast appears
    await expect(page.locator('[role="alert"]')).toContainText('Booking deleted', {
      timeout: 5_000,
    });
  });
});

/**
 * Test 6: Delete action (recurring)
 */
test.describe('6: Delete action (recurring)', () => {
  test('recurring booking shows scope dialog instead of simple confirm', async ({
    page,
  }) => {
    await gotoCalendarWithMocks(page, {
      bookings: [BOOKING_RECURRING],
      bookingById: { 'bk-recurring': BOOKING_RECURRING },
    });

    await clickBookingAndWaitForModal(page, 'Weekly Standup');

    // Recurring chip should be visible
    await expect(page.locator('[data-testid="recurring-chip"]')).toBeVisible();

    // Click Delete
    await page.locator('[data-testid="btn-delete"]').click();

    // Scope dialog appears (not simple confirm)
    const scopeDialog = page.locator('[data-testid="scope-select-dialog"]');
    await expect(scopeDialog).toBeVisible();
    await expect(scopeDialog).toContainText('recurring booking');
  });

  test('selecting "This occurrence only" triggers DELETE with scope=this-only', async ({
    page,
  }) => {
    const deleteRequests: string[] = [];

    // Inject roles and user ID before page load
    await page.addInitScript(
      ({ injectedRoles, userId }: { injectedRoles: string[]; userId: string }) => {
        (window as unknown as Record<string, unknown>).__E2E_ROLES__ = injectedRoles;
        (window as unknown as Record<string, unknown>).__E2E_USER_ID__ = userId;
      },
      { injectedRoles: ['role_calendar_viewer'], userId: CURRENT_USER_ID },
    );

    // Set up ALL API mocks together (before goto) — capture DELETE URLs
    await page.route('http://localhost:8080/**', (route) => {
      const url = route.request().url();
      const method = route.request().method();

      if (method === 'DELETE' && url.includes('/bookings/')) {
        deleteRequests.push(url);
        route.fulfill({ status: 204, body: '' });
      } else if (url.match(/\/bookings\/bk-recurring($|\?)/)) {
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(BOOKING_RECURRING),
        });
      } else if (url.match(/\/locations\/[^/?]+/)) {
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(LOCATION_1),
        });
      } else if (url.includes('/bookings')) {
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([BOOKING_RECURRING]),
        });
      } else if (url.includes('/locations')) {
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([LOCATION_1]),
        });
      } else {
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      }
    });

    await page.goto('/bookings?view=month');
    await page.waitForSelector('.fc', { timeout: 15_000 });

    await clickBookingAndWaitForModal(page, 'Weekly Standup');
    await page.locator('[data-testid="btn-delete"]').click();

    // Select "This occurrence only"
    await page.locator('[data-testid="btn-delete-this-only"]').click();

    // Wait for modal to close
    await expect(
      page.locator('[data-testid="booking-detail-modal"]'),
    ).not.toBeVisible({ timeout: 5_000 });

    // Verify the DELETE URL contained scope=this-only
    expect(deleteRequests.some((url) => url.includes('scope=this-only'))).toBeTruthy();
  });
});

/**
 * Test 7: Clone action
 */
test.describe('7: Clone action', () => {
  test('Clone button visible for all users (no permission restriction)', async ({
    page,
  }) => {
    await gotoCalendarWithMocks(page, {
      bookings: [BOOKING_APPROVED],
      bookingById: { 'bk-approved': BOOKING_APPROVED },
    });

    await clickBookingAndWaitForModal(page, 'Approved Meeting');

    // Clone button must be visible
    const cloneBtn = page.locator('[data-testid="btn-clone"]');
    await expect(cloneBtn).toBeVisible();
  });

  test('clicking Clone navigates to /bookings/create with pre-fill params', async ({
    page,
  }) => {
    await gotoCalendarWithMocks(page, {
      bookings: [BOOKING_APPROVED],
      bookingById: { 'bk-approved': BOOKING_APPROVED },
    });

    await clickBookingAndWaitForModal(page, 'Approved Meeting');
    await page.locator('[data-testid="btn-clone"]').click();

    // Should navigate to /bookings/create
    await expect(page).toHaveURL(/\/bookings\/create/, { timeout: 5_000 });

    // URL should contain title with (Copy)
    const url = page.url();
    expect(decodeURIComponent(url)).toContain('(Copy)');
    expect(url).toContain('location_id=loc-001');
  });
});

/**
 * Test 8: Permission denial (not owner, not admin)
 */
test.describe('8: Permission denial', () => {
  test('Edit and Delete hidden for non-owner, non-admin; Clone still visible', async ({
    page,
  }) => {
    // User is 'user-current' (not CURRENT_USER_ID='user-owner-001')
    // BOOKING_OTHER_OWNER has owner_id='user-other-002'
    await page.addInitScript(
      ({
        injectedRoles,
        userId,
      }: {
        injectedRoles: string[];
        userId: string;
      }) => {
        (window as unknown as Record<string, unknown>).__E2E_ROLES__ = injectedRoles;
        (window as unknown as Record<string, unknown>).__E2E_USER_ID__ = userId;
      },
      { injectedRoles: ['role_calendar_viewer'], userId: 'user-not-owner' },
    );

    await page.route('http://localhost:8080/**', (route) => {
      const url = route.request().url();
      if (url.match(/\/bookings\/bk-other($|\?)/)) {
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(BOOKING_OTHER_OWNER),
        });
      } else if (url.includes('/bookings')) {
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([BOOKING_OTHER_OWNER]),
        });
      } else if (url.includes('/locations')) {
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([LOCATION_1]),
        });
      } else {
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      }
    });

    await page.goto('/bookings?view=month');
    await page.waitForSelector('.fc', { timeout: 15_000 });

    await clickBookingAndWaitForModal(page, "Other Person's Meeting");

    // Edit and Delete buttons must NOT be present (structurally absent, not disabled)
    const editBtn = page.locator('[data-testid="btn-edit"]');
    const deleteBtn = page.locator('[data-testid="btn-delete"]');
    await expect(editBtn).not.toBeAttached();
    await expect(deleteBtn).not.toBeAttached();

    // Clone must still be visible
    await expect(page.locator('[data-testid="btn-clone"]')).toBeVisible();
  });
});

/**
 * Test 9: Audit trail link
 */
test.describe('9: Audit trail link', () => {
  test('clicking "View history" navigates to /admin/audit with booking filter', async ({
    page,
  }) => {
    await gotoCalendarWithMocks(page, {
      bookings: [BOOKING_APPROVED],
      bookingById: { 'bk-approved': BOOKING_APPROVED },
    });

    await clickBookingAndWaitForModal(page, 'Approved Meeting');

    const auditLink = page.locator('[data-testid="audit-trail-link"]');
    await expect(auditLink).toBeVisible();
    await expect(auditLink).toContainText('View history');

    // Click the audit trail link
    await auditLink.click();

    // Should navigate to /admin/audit with query params
    await expect(page).toHaveURL(
      /\/admin\/audit\?entity_type=booking&entity_id=bk-approved/,
      { timeout: 5_000 },
    );
  });
});
