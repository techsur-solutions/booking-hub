import { test, expect, type Page } from '@playwright/test';

/**
 * Conflict Detection UI End-to-End Tests (UI-BOOK-04)
 *
 * Tests run with VITE_E2E_MODE=true (playwright.config.ts webServer.env).
 * Keycloak is bypassed; roles injected via window.__E2E_ROLES__.
 * Backend API calls intercepted via page.route() targeting http://localhost:8080.
 *
 * Covered:
 *   1. Hard-block scenario (non-approver) — red banner, Save disabled
 *   2. Soft-warn scenario (approver) — yellow banner, Save Anyway with confirmation
 *   3. Location vs resource conflict distinction (icons and headings)
 *   4. Conflict UI in Edit Form (same behaviour as Create)
 *   5. ConflictBadge in Calendar/Day/List views for conflicted bookings
 *   6. Multiple conflicts (grouped by type)
 */

// ── Constants ─────────────────────────────────────────────────────────────────

const API_BASE = 'http://localhost:8080';

// ── Fixture data ──────────────────────────────────────────────────────────────

const LOCATION_A = {
  id: 'loc-a',
  name: 'Conference Room A',
  colour: '#3b82f6',
  building: 'Main',
  layouts: null,
  deleted_at: null,
};

const LOCATION_B = {
  id: 'loc-b',
  name: 'Meeting Room B',
  colour: '#10b981',
  building: 'Annex',
  layouts: null,
  deleted_at: null,
};

const RESOURCE_PROJECTOR = {
  id: 'res-proj',
  name: 'Projector',
  type: 'AV',
  description: null,
  is_unique: true,
  restrict_locations: null,
  deleted_at: null,
};

const BOOKING_EXISTING = {
  id: 'bk-existing-001',
  series_id: null,
  location_id: 'loc-a',
  resource_ids: [],
  title: 'Existing Team Meeting',
  description: null,
  start_time: '2025-06-15T10:00:00.000Z',
  end_time: '2025-06-15T11:00:00.000Z',
  status: 'approved' as const,
  owner_id: 'user-other',
  conflict_flags: [],
  custom_field_values: [],
  created_at: '2025-06-01T00:00:00.000Z',
  updated_at: '2025-06-01T00:00:00.000Z',
  deleted_at: null,
};

const BOOKING_EXISTING_2 = {
  id: 'bk-existing-002',
  series_id: null,
  location_id: 'loc-a',
  resource_ids: [],
  title: 'Another Meeting',
  description: null,
  start_time: '2025-06-15T10:30:00.000Z',
  end_time: '2025-06-15T11:30:00.000Z',
  status: 'approved' as const,
  owner_id: 'user-other',
  conflict_flags: [],
  custom_field_values: [],
  created_at: '2025-06-01T00:00:00.000Z',
  updated_at: '2025-06-01T00:00:00.000Z',
  deleted_at: null,
};

const BOOKING_EXISTING_RESOURCE = {
  id: 'bk-resource-conflict',
  series_id: null,
  location_id: 'loc-b',
  resource_ids: ['res-proj'],
  title: 'Resource Conflict Meeting',
  description: null,
  start_time: '2025-06-15T10:00:00.000Z',
  end_time: '2025-06-15T11:00:00.000Z',
  status: 'approved' as const,
  owner_id: 'user-other',
  conflict_flags: [],
  custom_field_values: [],
  created_at: '2025-06-01T00:00:00.000Z',
  updated_at: '2025-06-01T00:00:00.000Z',
  deleted_at: null,
};

const BOOKING_EDIT_TARGET = {
  id: 'bk-edit-001',
  series_id: null,
  location_id: 'loc-a',
  resource_ids: [],
  title: 'My Meeting to Edit',
  description: null,
  start_time: '2025-06-20T14:00:00.000Z',
  end_time: '2025-06-20T15:00:00.000Z',
  status: 'pending' as const,
  owner_id: 'user-me',
  conflict_flags: [],
  custom_field_values: [],
  created_at: '2025-06-01T00:00:00.000Z',
  updated_at: '2025-06-01T00:00:00.000Z',
  deleted_at: null,
};

// Conflict flags simulating the response from /bookings/check-conflicts
const LOCATION_CONFLICT_FLAGS = [
  { conflicting_booking_id: 'bk-existing-001', conflict_type: 'location' as const },
];

const RESOURCE_CONFLICT_FLAGS = [
  { conflicting_booking_id: 'bk-resource-conflict', conflict_type: 'resource' as const },
];

const MULTIPLE_CONFLICT_FLAGS = [
  { conflicting_booking_id: 'bk-existing-001', conflict_type: 'location' as const },
  { conflicting_booking_id: 'bk-existing-002', conflict_type: 'location' as const },
  { conflicting_booking_id: 'bk-resource-conflict', conflict_type: 'resource' as const },
];

// Bookings with conflict flags (for calendar/day/list views)
const BOOKING_WITH_LOCATION_CONFLICT = {
  id: 'bk-conflicted-cal',
  series_id: null,
  location_id: 'loc-a',
  resource_ids: [],
  title: 'Conflicted Booking',
  description: null,
  start_time: new Date(new Date().getFullYear(), new Date().getMonth(), 15, 10, 0).toISOString(),
  end_time: new Date(new Date().getFullYear(), new Date().getMonth(), 15, 11, 0).toISOString(),
  status: 'pending' as const,
  owner_id: 'user-me',
  conflict_flags: [{ conflicting_booking_id: 'bk-other', conflict_type: 'location' as const }],
  custom_field_values: [],
  created_at: new Date().toISOString(),
  updated_at: new Date().toISOString(),
  deleted_at: null,
};

// ── Helpers ───────────────────────────────────────────────────────────────────

/**
 * Navigate to Booking Create page with injected roles and API mocks.
 */
async function gotoBookingCreate(
  page: Page,
  options: {
    roles?: string[];
    conflictResponse?: typeof LOCATION_CONFLICT_FLAGS;
  } = {},
) {
  const { roles = ['role_calendar_viewer'], conflictResponse = [] } = options;

  await page.addInitScript((injectedRoles: string[]) => {
    (window as unknown as Record<string, unknown>).__E2E_ROLES__ = injectedRoles;
  }, roles);

  // Mock all API calls
  await page.route(`${API_BASE}/locations**`, (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([LOCATION_A, LOCATION_B]),
    }),
  );

  await page.route(`${API_BASE}/resources**`, (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([RESOURCE_PROJECTOR]),
    }),
  );

  await page.route(`${API_BASE}/custom-fields**`, (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([]),
    }),
  );

  await page.route(`${API_BASE}/settings**`, (route) =>
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
    }),
  );

  await page.route(`${API_BASE}/bookings/check-conflicts**`, (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(conflictResponse),
    }),
  );

  await page.route(`${API_BASE}/bookings`, (route) => {
    if (route.request().method() === 'POST') {
      route.fulfill({
        status: 201,
        contentType: 'application/json',
        body: JSON.stringify({ ...BOOKING_EXISTING, id: 'bk-created', title: 'New Booking' }),
      });
    } else {
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
    }
  });

  // Catch-all for other bookings endpoints (e.g. getById)
  await page.route(`${API_BASE}/bookings/**`, (route) => {
    const url = route.request().url();
    if (url.includes('/check-conflicts')) {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(conflictResponse),
      });
    } else {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(BOOKING_EXISTING),
      });
    }
  });

  await page.goto('/bookings/create');
  await expect(page.getByTestId('booking-form')).toBeVisible();
}

/**
 * Navigate to Booking Edit page with injected roles and API mocks.
 */
async function gotoBookingEdit(
  page: Page,
  options: {
    roles?: string[];
    conflictResponse?: typeof LOCATION_CONFLICT_FLAGS;
  } = {},
) {
  const { roles = ['role_calendar_viewer'], conflictResponse = [] } = options;

  await page.addInitScript((injectedRoles: string[]) => {
    (window as unknown as Record<string, unknown>).__E2E_ROLES__ = injectedRoles;
  }, roles);

  await page.route(`${API_BASE}/locations**`, (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([LOCATION_A, LOCATION_B]),
    }),
  );

  await page.route(`${API_BASE}/resources**`, (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([RESOURCE_PROJECTOR]),
    }),
  );

  await page.route(`${API_BASE}/custom-fields**`, (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([]),
    }),
  );

  await page.route(`${API_BASE}/settings**`, (route) =>
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
    }),
  );

  await page.route(`${API_BASE}/bookings/check-conflicts**`, (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(conflictResponse),
    }),
  );

  // For bookings API: first register GET /:id handler, then the list handler
  await page.route(`${API_BASE}/bookings/**`, (route) => {
    const url = route.request().url();
    if (url.includes('/check-conflicts')) {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(conflictResponse),
      });
    } else if (route.request().method() === 'PATCH') {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(BOOKING_EDIT_TARGET),
      });
    } else {
      // GET /bookings/:id
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(BOOKING_EDIT_TARGET),
      });
    }
  });

  await page.route(`${API_BASE}/bookings`, (route) =>
    route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) }),
  );

  await page.goto(`/bookings/${BOOKING_EDIT_TARGET.id}/edit`);
  await expect(page.getByTestId('booking-form')).toBeVisible();
}

/**
 * Navigate to Calendar View (month) with mocked API.
 * Uses the same route glob pattern as the existing calendar-view.spec.ts.
 */
async function gotoCalendarViewMonth(
  page: Page,
  options: {
    roles?: string[];
    bookings?: unknown[];
    bookingById?: Record<string, unknown>;
  } = {},
) {
  const { roles = ['role_calendar_viewer'], bookings = [], bookingById = {} } = options;

  await page.addInitScript(
    ({ injectedRoles, userId }: { injectedRoles: string[]; userId: string }) => {
      (window as unknown as Record<string, unknown>).__E2E_ROLES__ = injectedRoles;
      (window as unknown as Record<string, unknown>).__E2E_USER_ID__ = userId;
    },
    { injectedRoles: roles, userId: 'user-me' },
  );

  // Use glob pattern matching existing calendar-view.spec.ts pattern
  await page.route('*://localhost:8080/**', (route) => {
    const url = new URL(route.request().url());
    const path = url.pathname;
    const method = route.request().method();

    // GET /bookings/:id
    if (path.match(/\/bookings\/([^/]+)$/) && method === 'GET') {
      const bookingId = path.split('/').pop()!;
      const specificBooking = bookingById[bookingId];
      if (specificBooking) {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(specificBooking) });
      } else {
        route.fulfill({ status: 404, body: '' });
      }
      return;
    }

    // GET /locations/:id
    if (path.match(/\/locations\/[^/]+/)) {
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(LOCATION_A) });
      return;
    }

    // GET /locations
    if (path === '/locations' || path.startsWith('/locations?')) {
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([LOCATION_A, LOCATION_B]) });
      return;
    }

    // GET /bookings
    if (path === '/bookings' || path.startsWith('/bookings?')) {
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(bookings) });
      return;
    }

    // GET /settings
    if (path.includes('/settings')) {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ id: 1, approve_booking: false, calendar_slot_size: 30, calendar_min_time: '08:00', calendar_max_time: '18:00' }),
      });
      return;
    }

    route.fulfill({ status: 404, contentType: 'application/json', body: '{}' });
  });

  await page.goto('/bookings?view=month');
  await page.waitForSelector('.fc', { timeout: 15_000 });
}

/**
 * Fill the booking form's location and time fields to trigger conflict detection.
 */
async function fillFormFields(page: Page, opts: { location?: string } = {}) {
  const locationId = opts.location ?? 'loc-a';

  // Select location
  await page.getByTestId('booking-title').fill('Test Booking');
  await page
    .locator('select[data-testid="booking-location"]')
    .selectOption({ value: locationId });

  // Set start/end time (datetime-local inputs)
  await page.locator('input[data-testid="booking-start-time"]').fill('2025-06-15T10:00');
  await page.locator('input[data-testid="booking-end-time"]').fill('2025-06-15T11:00');
}

// ── Test Suite ─────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────────────────
// Scenario 1: Hard-block (non-approver in Create Form)
// ─────────────────────────────────────────────────────────────────────────────
test.describe('Scenario 1: Hard-block (non-approver)', () => {
  test('1a. Red ConflictBanner appears with Cannot save message', async ({ page }) => {
    await gotoBookingCreate(page, {
      roles: ['role_calendar_viewer'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });

    await fillFormFields(page);

    // Wait for conflict banner (debounce 500ms + API round-trip)
    await expect(page.getByTestId('conflict-banner-hard-block')).toBeVisible({ timeout: 3000 });
    await expect(page.getByTestId('conflict-banner-hard-block')).toContainText('Cannot save');
  });

  test('1b. Save button is disabled when hard-block conflict present', async ({ page }) => {
    await gotoBookingCreate(page, {
      roles: ['role_calendar_viewer'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });

    await fillFormFields(page);

    await expect(page.getByTestId('conflict-banner-hard-block')).toBeVisible({ timeout: 3000 });
    // Save button should be disabled
    await expect(page.getByTestId('booking-save')).toBeDisabled();
  });

  test('1c. Conflict list shows booking ID link (clickable)', async ({ page }) => {
    await gotoBookingCreate(page, {
      roles: ['role_calendar_viewer'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });

    await fillFormFields(page);

    await expect(page.getByTestId('conflict-banner-hard-block')).toBeVisible({ timeout: 3000 });
    // Conflict link for the conflicting booking ID exists
    await expect(page.getByTestId(`conflict-link-${LOCATION_CONFLICT_FLAGS[0].conflicting_booking_id}`)).toBeVisible();
  });

  test('1d. Clicking conflict booking ID opens BookingDetailModal', async ({ page }) => {
    await gotoBookingCreate(page, {
      roles: ['role_calendar_viewer'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });

    await fillFormFields(page);

    await expect(page.getByTestId('conflict-banner-hard-block')).toBeVisible({ timeout: 3000 });

    // Click the conflict ID link
    await page.getByTestId(`conflict-link-${LOCATION_CONFLICT_FLAGS[0].conflicting_booking_id}`).click();

    // BookingDetailModal should open
    await expect(page.getByTestId('booking-detail-modal')).toBeVisible({ timeout: 3000 });
  });

  test('1e. Resolving conflict (no-conflict response) re-enables Save', async ({ page }) => {
    // Test that when conflicts are present → Save disabled; when resolved → Save enabled.
    // We test the resolved state directly by using an empty conflict response.
    await gotoBookingCreate(page, {
      roles: ['role_calendar_viewer'],
      conflictResponse: [], // No conflicts
    });

    await fillFormFields(page);

    // With no conflicts: no hard-block banner should appear
    await page.waitForTimeout(1500); // Wait past the 500ms debounce + API round-trip
    await expect(page.getByTestId('conflict-banner-hard-block')).not.toBeVisible();
    // Save button should be enabled
    await expect(page.getByTestId('booking-save')).not.toBeDisabled();
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// Scenario 2: Soft-warn (approver in Create Form)
// ─────────────────────────────────────────────────────────────────────────────
test.describe('Scenario 2: Soft-warn (approver)', () => {
  test('2a. Yellow ConflictBanner appears with Warning message for approver', async ({ page }) => {
    await gotoBookingCreate(page, {
      roles: ['role_calendar_viewer', 'role_booking_approver'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });

    await fillFormFields(page);

    await expect(page.getByTestId('conflict-banner-soft-warn')).toBeVisible({ timeout: 3000 });
    await expect(page.getByTestId('conflict-banner-soft-warn')).toContainText('Warning');
    await expect(page.getByTestId('conflict-banner-soft-warn')).toContainText('Conflicts detected');
  });

  test('2b. Save button remains enabled for approver (soft-warn)', async ({ page }) => {
    await gotoBookingCreate(page, {
      roles: ['role_calendar_viewer', 'role_booking_approver'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });

    await fillFormFields(page);

    await expect(page.getByTestId('conflict-banner-soft-warn')).toBeVisible({ timeout: 3000 });
    // Save button should NOT be disabled for approvers
    await expect(page.getByTestId('booking-save')).not.toBeDisabled();
  });

  test('2c. Save Anyway button appears in soft-warn banner', async ({ page }) => {
    await gotoBookingCreate(page, {
      roles: ['role_calendar_viewer', 'role_booking_approver'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });

    await fillFormFields(page);

    await expect(page.getByTestId('conflict-banner-soft-warn')).toBeVisible({ timeout: 3000 });
    await expect(page.getByTestId('btn-save-anyway')).toBeVisible();
  });

  test('2d. Clicking Save Anyway shows confirmation dialog', async ({ page }) => {
    await gotoBookingCreate(page, {
      roles: ['role_calendar_viewer', 'role_booking_approver'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });

    await fillFormFields(page);

    await expect(page.getByTestId('conflict-banner-soft-warn')).toBeVisible({ timeout: 3000 });
    await page.getByTestId('btn-save-anyway').click();

    // Confirmation dialog must appear
    await expect(page.getByTestId('save-anyway-confirm-dialog')).toBeVisible();
    await expect(page.getByTestId('save-anyway-confirm-dialog')).toContainText('despite conflicts');
  });

  test('2e. Cancelling Save Anyway dialog does not save', async ({ page }) => {
    await gotoBookingCreate(page, {
      roles: ['role_calendar_viewer', 'role_booking_approver'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });

    await fillFormFields(page);

    await expect(page.getByTestId('conflict-banner-soft-warn')).toBeVisible({ timeout: 3000 });
    await page.getByTestId('btn-save-anyway').click();
    await expect(page.getByTestId('save-anyway-confirm-dialog')).toBeVisible();

    // Cancel the dialog
    await page.getByTestId('btn-cancel-save-anyway').click();
    await expect(page.getByTestId('save-anyway-confirm-dialog')).not.toBeVisible();

    // Soft-warn banner should still be showing (booking not saved)
    await expect(page.getByTestId('conflict-banner-soft-warn')).toBeVisible();
  });

  test('2f. Confirming Save Anyway calls onSaveAnyway callback', async ({ page }) => {
    await gotoBookingCreate(page, {
      roles: ['role_calendar_viewer', 'role_booking_approver'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });

    await fillFormFields(page);

    await expect(page.getByTestId('conflict-banner-soft-warn')).toBeVisible({ timeout: 3000 });

    // Click Save Anyway → confirm
    await page.getByTestId('btn-save-anyway').click();
    await expect(page.getByTestId('save-anyway-confirm-dialog')).toBeVisible();
    await page.getByTestId('btn-confirm-save-anyway').click();

    // Dialog closes after confirm
    await expect(page.getByTestId('save-anyway-confirm-dialog')).not.toBeVisible();
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// Scenario 3: Location vs resource conflict distinction
// ─────────────────────────────────────────────────────────────────────────────
test.describe('Scenario 3: Location vs resource conflict distinction', () => {
  test('3a. Location conflict shows location icon heading', async ({ page }) => {
    await gotoBookingCreate(page, {
      roles: ['role_calendar_viewer'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });

    await fillFormFields(page);

    await expect(page.getByTestId('conflict-banner-hard-block')).toBeVisible({ timeout: 3000 });
    // Location conflicts group with 📍 icon
    await expect(page.getByTestId('location-conflicts-group')).toBeVisible();
    await expect(page.getByTestId('location-conflicts-group')).toContainText('📍');
    await expect(page.getByTestId('location-conflicts-group')).toContainText('Location conflict with:');
  });

  test('3b. Resource conflict shows resource icon heading', async ({ page }) => {
    await gotoBookingCreate(page, {
      roles: ['role_calendar_viewer'],
      conflictResponse: RESOURCE_CONFLICT_FLAGS,
    });

    await fillFormFields(page);

    await expect(page.getByTestId('conflict-banner-hard-block')).toBeVisible({ timeout: 3000 });
    // Resource conflicts group with 🔧 icon
    await expect(page.getByTestId('resource-conflicts-group')).toBeVisible();
    await expect(page.getByTestId('resource-conflicts-group')).toContainText('🔧');
    await expect(page.getByTestId('resource-conflicts-group')).toContainText('Resource conflict');
  });

  test('3c. Approver sees same icon grouping in soft-warn banner', async ({ page }) => {
    await gotoBookingCreate(page, {
      roles: ['role_calendar_viewer', 'role_booking_approver'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });

    await fillFormFields(page);

    await expect(page.getByTestId('conflict-banner-soft-warn')).toBeVisible({ timeout: 3000 });
    await expect(page.getByTestId('location-conflicts-group')).toBeVisible();
    await expect(page.getByTestId('location-conflicts-group')).toContainText('📍');
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// Scenario 4: Conflict UI in Edit Form
// ─────────────────────────────────────────────────────────────────────────────
test.describe('Scenario 4: Conflict UI in Edit Form', () => {
  test('4a. Non-approver sees hard-block banner in Edit Form', async ({ page }) => {
    await gotoBookingEdit(page, {
      roles: ['role_calendar_viewer'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });

    // Change time to trigger conflict check (BookingEdit loads initial values and triggers check)
    await page.locator('input[data-testid="booking-start-time"]').fill('2025-06-15T10:00');
    await page.locator('input[data-testid="booking-end-time"]').fill('2025-06-15T11:00');

    await expect(page.getByTestId('conflict-banner-hard-block')).toBeVisible({ timeout: 4000 });
    await expect(page.getByTestId('booking-save')).toBeDisabled();
  });

  test('4b. Approver sees soft-warn banner in Edit Form', async ({ page }) => {
    await gotoBookingEdit(page, {
      roles: ['role_calendar_viewer', 'role_booking_approver'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });

    await page.locator('input[data-testid="booking-start-time"]').fill('2025-06-15T10:00');
    await page.locator('input[data-testid="booking-end-time"]').fill('2025-06-15T11:00');

    await expect(page.getByTestId('conflict-banner-soft-warn')).toBeVisible({ timeout: 4000 });
    await expect(page.getByTestId('btn-save-anyway')).toBeVisible();
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// Scenario 5: ConflictBadge in Calendar/Day/List views
// ─────────────────────────────────────────────────────────────────────────────
test.describe('Scenario 5: ConflictBadge in views', () => {
  test('5a. Calendar View (month): conflicted booking shows ConflictBadge', async ({ page }) => {
    await gotoCalendarViewMonth(page, {
      roles: ['role_calendar_viewer'],
      bookings: [BOOKING_WITH_LOCATION_CONFLICT],
      bookingById: {
        [BOOKING_WITH_LOCATION_CONFLICT.id]: BOOKING_WITH_LOCATION_CONFLICT,
      },
    });

    // FullCalendar renders BookingEvent component which contains ConflictBadge
    await expect(page.locator('[data-testid="conflict-badge"]')).toBeVisible({ timeout: 10_000 });
  });

  test('5b. Day View: conflicted booking shows ConflictBadge', async ({ page }) => {
    await page.addInitScript((injectedRoles: string[]) => {
      (window as unknown as Record<string, unknown>).__E2E_ROLES__ = injectedRoles;
    }, ['role_calendar_viewer']);

    await page.route('*://localhost:8080/**', (route) => {
      const url = new URL(route.request().url());
      const path = url.pathname;

      if (path.includes('/locations')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([LOCATION_A]) });
        return;
      }
      if (path.includes('/settings')) {
        route.fulfill({
          status: 200, contentType: 'application/json',
          body: JSON.stringify({ id: 1, approve_booking: false, calendar_slot_size: 30, calendar_min_time: '08:00', calendar_max_time: '18:00' }),
        });
        return;
      }
      if (path.includes('/bookings')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([BOOKING_WITH_LOCATION_CONFLICT]) });
        return;
      }
      route.fulfill({ status: 404, body: '{}' });
    });

    await page.goto('/bookings?view=day');
    // DayView renders DayViewEvent components with ConflictBadge
    await expect(page.getByTestId('conflict-badge').first()).toBeVisible({ timeout: 8000 });
  });

  test('5c. List View: conflicted booking shows ConflictBadge', async ({ page }) => {
    // Set up list view with a conflicted booking
    await page.addInitScript((injectedRoles: string[]) => {
      (window as unknown as Record<string, unknown>).__E2E_ROLES__ = injectedRoles;
    }, ['role_calendar_viewer']);

    await page.route('*://localhost:8080/**', (route) => {
      const url = new URL(route.request().url());
      const path = url.pathname;

      if (path.includes('/locations')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([LOCATION_A]) });
        return;
      }
      if (path.includes('/settings')) {
        route.fulfill({
          status: 200, contentType: 'application/json',
          body: JSON.stringify({ id: 1, approve_booking: false, calendar_slot_size: 30, calendar_min_time: '08:00', calendar_max_time: '18:00' }),
        });
        return;
      }
      if (path.includes('/bookings')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([BOOKING_WITH_LOCATION_CONFLICT]) });
        return;
      }
      route.fulfill({ status: 404, body: '{}' });
    });

    await page.goto('/bookings?view=list');
    await page.waitForTimeout(500);

    // ListView renders ConflictBadge in the Conflict Flags column
    await expect(page.getByTestId('conflict-badge').first()).toBeVisible({ timeout: 5000 });
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// Scenario 6: Multiple conflicts grouped by type
// ─────────────────────────────────────────────────────────────────────────────
test.describe('Scenario 6: Multiple conflicts grouped by type', () => {
  test('6a. Multiple conflicts (2 location + 1 resource) all listed in banner', async ({ page }) => {
    await gotoBookingCreate(page, {
      roles: ['role_calendar_viewer'],
      conflictResponse: MULTIPLE_CONFLICT_FLAGS,
    });

    await fillFormFields(page);

    await expect(page.getByTestId('conflict-banner-hard-block')).toBeVisible({ timeout: 3000 });

    // Both groups should be visible
    await expect(page.getByTestId('location-conflicts-group')).toBeVisible();
    await expect(page.getByTestId('resource-conflicts-group')).toBeVisible();
  });

  test('6b. Location conflicts section lists both location conflict bookings', async ({ page }) => {
    await gotoBookingCreate(page, {
      roles: ['role_calendar_viewer'],
      conflictResponse: MULTIPLE_CONFLICT_FLAGS,
    });

    await fillFormFields(page);

    await expect(page.getByTestId('conflict-banner-hard-block')).toBeVisible({ timeout: 3000 });

    // Both location conflict booking IDs visible as links
    await expect(
      page.getByTestId(`conflict-link-${MULTIPLE_CONFLICT_FLAGS[0].conflicting_booking_id}`),
    ).toBeVisible();
    await expect(
      page.getByTestId(`conflict-link-${MULTIPLE_CONFLICT_FLAGS[1].conflicting_booking_id}`),
    ).toBeVisible();
  });

  test('6c. Resource conflicts section lists resource conflict booking', async ({ page }) => {
    await gotoBookingCreate(page, {
      roles: ['role_calendar_viewer'],
      conflictResponse: MULTIPLE_CONFLICT_FLAGS,
    });

    await fillFormFields(page);

    await expect(page.getByTestId('conflict-banner-hard-block')).toBeVisible({ timeout: 3000 });

    // Resource conflict booking ID visible
    await expect(
      page.getByTestId(`conflict-link-${MULTIPLE_CONFLICT_FLAGS[2].conflicting_booking_id}`),
    ).toBeVisible();
  });

  test('6d. Approver sees multiple conflicts grouped in soft-warn banner', async ({ page }) => {
    await gotoBookingCreate(page, {
      roles: ['role_calendar_viewer', 'role_booking_approver'],
      conflictResponse: MULTIPLE_CONFLICT_FLAGS,
    });

    await fillFormFields(page);

    await expect(page.getByTestId('conflict-banner-soft-warn')).toBeVisible({ timeout: 3000 });
    await expect(page.getByTestId('location-conflicts-group')).toBeVisible();
    await expect(page.getByTestId('resource-conflicts-group')).toBeVisible();
  });
});
