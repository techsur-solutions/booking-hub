import { test, expect, type Page } from '@playwright/test';
import type { Booking, Location, Resource, CustomField } from '../src/api/types';

/**
 * Booking Create E2E Tests
 *
 * Tests run with VITE_E2E_MODE=true (set in playwright.config.ts webServer.env).
 * Keycloak is bypassed; roles injected via window.__E2E_ROLES__.
 * Backend API calls intercepted via page.route() targeting http://localhost:8080.
 * No real backend or auth required.
 *
 * Covers plan 09-09 acceptance criteria:
 * 1. Pre-fill from URL params (location_id, start_time, end_time)
 * 2. Custom fields render dynamically based on location context
 * 3. Recurring pattern selector (type + until date)
 * 4. Conflict detection hard-block (non-approver)
 * 5. Conflict detection soft-warn (approver)
 * 6. Status confirmation banner after save
 */

// ── Test Data ──────────────────────────────────────────────────────────────────

const LOCATION_WITH_FIELDS: Location = {
  id: 'loc-001',
  name: 'Conference Room A',
  colour: '#3b82f6',
  building: 'Main',
  layouts: null,
  deleted_at: null,
};

const LOCATION_SIMPLE: Location = {
  id: 'loc-002',
  name: 'Meeting Room B',
  colour: '#10b981',
  building: 'Annex',
  layouts: null,
  deleted_at: null,
};

const RESOURCE_PROJECTOR: Resource = {
  id: 'res-projector',
  name: 'Projector',
  type: 'AV',
  description: null,
  is_unique: true,
  restrict_locations: null,
  deleted_at: null,
};

const CUSTOM_FIELD_TEXTFIELD: CustomField = {
  id: 'cf-001',
  name: 'Conference Room Setup',
  field_type: 'textfield',
  required: false,
  options: null,
  context_id: 'loc-001',
};

const CUSTOM_FIELD_SELECT: CustomField = {
  id: 'cf-002',
  name: 'Catering Required',
  field_type: 'select',
  required: false,
  options: ['Yes', 'No', 'Vegetarian Only'],
  context_id: 'loc-001',
};

const EXISTING_BOOKING: Booking = {
  id: 'bk-existing',
  series_id: null,
  location_id: 'loc-001',
  resource_ids: [],
  title: 'Existing Team Meeting',
  description: null,
  start_time: '2024-02-15T09:00:00',
  end_time: '2024-02-15T10:00:00',
  status: 'approved',
  owner_id: 'user-owner',
  conflict_flags: [],
  custom_field_values: [],
  created_at: '2024-01-01T00:00:00',
  updated_at: '2024-01-01T00:00:00',
  deleted_at: null,
};

const CREATED_BOOKING_APPROVED: Booking = {
  id: 'bk-new-approved',
  series_id: null,
  location_id: 'loc-001',
  resource_ids: [],
  title: 'New Test Booking',
  description: null,
  start_time: '2024-02-15T11:00:00',
  end_time: '2024-02-15T12:00:00',
  status: 'approved',
  owner_id: 'user-001',
  conflict_flags: [],
  custom_field_values: [],
  created_at: '2024-02-15T10:00:00',
  updated_at: '2024-02-15T10:00:00',
  deleted_at: null,
};

const CREATED_BOOKING_PENDING: Booking = {
  ...CREATED_BOOKING_APPROVED,
  id: 'bk-new-pending',
  status: 'pending',
};

// ── Helpers ──────────────────────────────────────────────────────────────────

const API_BASE = 'http://localhost:8080';

/**
 * Navigate to the Booking Create page with injected roles and mocked API.
 */
async function gotoBookingCreate(
  page: Page,
  options: {
    roles?: string[];
    urlParams?: string;
    locations?: Location[];
    resources?: Resource[];
    customFields?: CustomField[];
    createResponse?: Booking;
    createStatus?: number;
    conflictResponse?: { conflicting_booking_id: string; conflict_type: string }[];
  } = {}
) {
  const {
    roles = ['role_calendar_viewer'],
    urlParams = '',
    locations = [LOCATION_WITH_FIELDS, LOCATION_SIMPLE],
    resources = [RESOURCE_PROJECTOR],
    customFields = [],
    createResponse = CREATED_BOOKING_APPROVED,
    createStatus = 201,
    conflictResponse = [],
  } = options;

  // Inject roles before page load
  await page.addInitScript((injectedRoles: string[]) => {
    (window as unknown as Record<string, unknown>).__E2E_ROLES__ = injectedRoles;
  }, roles);

  // Mock API: locations
  await page.route(`${API_BASE}/locations**`, (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(locations),
    })
  );

  // Mock API: resources
  await page.route(`${API_BASE}/resources**`, (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(resources),
    })
  );

  // Mock API: custom fields
  await page.route(`${API_BASE}/custom-fields**`, (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(customFields),
    })
  );

  // Mock API: check-conflicts (using POST pattern from bookingsApi)
  await page.route(`${API_BASE}/bookings/check-conflicts**`, (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(conflictResponse),
    })
  );

  // Mock API: create booking (POST /bookings) and list bookings (GET /bookings)
  await page.route(`${API_BASE}/bookings`, (route) => {
    if (route.request().method() === 'POST') {
      route.fulfill({
        status: createStatus,
        contentType: 'application/json',
        body: JSON.stringify(createResponse),
      });
    } else {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      });
    }
  });

  // Mock API: settings
  await page.route(`${API_BASE}/settings**`, (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        id: 1,
        approve_booking: false,
        calendar_slot_size: 30,
        calendar_min_time: '08:00',
        calendar_max_time: '20:00',
      }),
    })
  );

  const url = `/bookings/create${urlParams ? `?${urlParams}` : ''}`;
  await page.goto(url);

  // Wait for the booking create page to load
  await page.waitForSelector('[data-testid="booking-create"]', { timeout: 15_000 });

  // Wait for form to appear (requires auth + React query to load)
  await page.waitForSelector('[data-testid="booking-form"]', { timeout: 10_000 });
}

// ── Tests ─────────────────────────────────────────────────────────────────────

test.describe('Booking Create — Pre-fill from URL params', () => {
  test('location dropdown shows pre-selected value from URL param', async ({ page }) => {
    await gotoBookingCreate(page, {
      urlParams: 'location_id=loc-001&start_time=2024-02-15T09:00&end_time=2024-02-15T10:00',
    });

    // Wait for locations to load (the form fetches them internally)
    await page.waitForTimeout(500);

    const locationSelect = page.locator('#booking-location');
    await expect(locationSelect).toHaveValue('loc-001');
  });

  test('start time input shows pre-filled value from URL param', async ({ page }) => {
    await gotoBookingCreate(page, {
      urlParams: 'location_id=loc-001&start_time=2024-02-15T09:00&end_time=2024-02-15T10:00',
    });

    const startInput = page.locator('#booking-start-time');
    await expect(startInput).toHaveValue('2024-02-15T09:00');
  });

  test('end time input shows pre-filled value from URL param', async ({ page }) => {
    await gotoBookingCreate(page, {
      urlParams: 'location_id=loc-001&start_time=2024-02-15T09:00&end_time=2024-02-15T10:00',
    });

    const endInput = page.locator('#booking-end-time');
    await expect(endInput).toHaveValue('2024-02-15T10:00');
  });

  test('end time defaults to start + 1 hour when end_time omitted from URL', async ({ page }) => {
    await gotoBookingCreate(page, {
      urlParams: 'start_time=2024-02-15T09:00',
    });

    // Wait a moment for the auto-compute to trigger
    await page.waitForTimeout(300);

    const endInput = page.locator('#booking-end-time');
    // End should be 10:00 (start 09:00 + 1 hour)
    await expect(endInput).toHaveValue('2024-02-15T10:00');
  });
});

test.describe('Booking Create — Custom fields render dynamically', () => {
  test('custom fields appear when location with custom field template is selected', async ({
    page,
  }) => {
    await gotoBookingCreate(page, {
      customFields: [CUSTOM_FIELD_TEXTFIELD, CUSTOM_FIELD_SELECT],
    });

    // Select location with custom fields
    await page.selectOption('#booking-location', 'loc-001');

    // Wait for React Query to re-fetch custom fields (500ms debounce)
    await page.waitForTimeout(800);

    // Custom fields section should be visible
    await expect(page.locator('[data-testid="custom-fields-section"]')).toBeVisible({
      timeout: 5_000,
    });

    // Individual custom fields should be rendered
    await expect(
      page.locator(`[data-testid="custom-field-${CUSTOM_FIELD_TEXTFIELD.id}"]`)
    ).toBeVisible();
    await expect(
      page.locator(`[data-testid="custom-field-${CUSTOM_FIELD_SELECT.id}"]`)
    ).toBeVisible();
  });

  test('custom field textfield can be filled with a value', async ({ page }) => {
    await gotoBookingCreate(page, {
      urlParams: 'location_id=loc-001',
      customFields: [CUSTOM_FIELD_TEXTFIELD],
    });

    // Wait for custom fields to appear (React Query fetches with location_id)
    await page.waitForTimeout(800);
    await expect(page.locator('[data-testid="custom-fields-section"]')).toBeVisible({
      timeout: 5_000,
    });

    // Fill the textfield custom field
    await page.fill(`#custom-field-${CUSTOM_FIELD_TEXTFIELD.id}`, 'Theater Style');

    // Verify the value persists
    await expect(
      page.locator(`#custom-field-${CUSTOM_FIELD_TEXTFIELD.id}`)
    ).toHaveValue('Theater Style');
  });
});

test.describe('Booking Create — Recurring pattern', () => {
  test('RecurringPattern fields appear when recurring checkbox checked', async ({ page }) => {
    await gotoBookingCreate(page);

    const recurringToggle = page.locator('[data-testid="recurring-toggle"]');
    await expect(recurringToggle).toBeVisible();

    // Recurring pattern should not be visible initially
    await expect(page.locator('[data-testid="recurring-pattern"]')).toHaveCount(0);

    // Check the recurring checkbox
    await recurringToggle.check();

    // Recurring pattern fields should appear
    await expect(page.locator('[data-testid="recurring-pattern"]')).toBeVisible();
  });

  test('recurring pattern shows Daily and Weekly options', async ({ page }) => {
    await gotoBookingCreate(page);

    // Check recurring toggle
    await page.locator('[data-testid="recurring-toggle"]').check();

    // Wait for recurring pattern to render
    await expect(page.locator('[data-testid="recurring-pattern"]')).toBeVisible();

    // The type select (within Select component) should have daily/weekly options
    const typeSelect = page.locator('#recurrence-type');
    await expect(typeSelect).toBeVisible();

    await expect(typeSelect.locator('option[value="daily"]')).toBeAttached();
    await expect(typeSelect.locator('option[value="weekly"]')).toBeAttached();
  });

  test('can select Weekly and set an until date', async ({ page }) => {
    await gotoBookingCreate(page, {
      urlParams: 'start_time=2024-02-15T09:00',
    });

    // Check recurring toggle
    await page.locator('[data-testid="recurring-toggle"]').check();

    // Wait for pattern to appear
    await expect(page.locator('[data-testid="recurring-pattern"]')).toBeVisible();

    // Select Weekly from the type dropdown
    await page.selectOption('#recurrence-type', 'weekly');

    // Set until date (4 weeks from start)
    await page.fill('#recurrence-until', '2024-03-14');

    // Verify values
    await expect(page.locator('#recurrence-type')).toHaveValue('weekly');
    await expect(page.locator('#recurrence-until')).toHaveValue('2024-03-14');
  });
});

test.describe('Booking Create — Conflict detection (non-approver hard-block)', () => {
  test('red conflict banner appears and Save disabled for non-approver with conflicts', async ({ page }) => {
    await gotoBookingCreate(page, {
      roles: ['role_calendar_viewer'], // no role_booking_approver
      conflictResponse: [
        {
          conflicting_booking_id: EXISTING_BOOKING.id,
          conflict_type: 'location',
        },
      ],
    });

    // Fill required fields to trigger conflict check
    await page.fill('#booking-title', 'Test Booking');
    await page.selectOption('#booking-location', 'loc-001');
    await page.fill('#booking-start-time', '2024-02-15T09:00');
    await page.fill('#booking-end-time', '2024-02-15T10:00');

    // Wait for debounced conflict check (500ms) + network response
    await page.waitForTimeout(800);

    // Hard-block banner should appear
    await expect(page.locator('[data-testid="conflict-hard-block"]')).toBeVisible({
      timeout: 5_000,
    });

    // Banner should mention "Conflict detected"
    await expect(page.locator('[data-testid="conflict-hard-block"]')).toContainText(
      'Conflict detected'
    );

    // Save button should be disabled (hard-block)
    const saveBtn = page.locator('[data-testid="booking-save"]');
    await expect(saveBtn).toBeDisabled();
  });

  test('Save button re-enabled when conflict time is resolved (non-approver)', async ({ page }) => {
    let hasConflict = true;

    // Set up routes manually for dynamic conflict toggle
    await page.addInitScript((roles: string[]) => {
      (window as unknown as Record<string, unknown>).__E2E_ROLES__ = roles;
    }, ['role_calendar_viewer']);

    await page.route(`${API_BASE}/locations**`, (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([LOCATION_WITH_FIELDS, LOCATION_SIMPLE]) })
    );
    await page.route(`${API_BASE}/resources**`, (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) })
    );
    await page.route(`${API_BASE}/custom-fields**`, (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) })
    );
    await page.route(`${API_BASE}/settings**`, (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ id: 1, approve_booking: false, calendar_slot_size: 30, calendar_min_time: '08:00', calendar_max_time: '20:00' }) })
    );

    // Dynamic conflict route: returns conflicts or empty based on hasConflict flag
    await page.route(`${API_BASE}/bookings/check-conflicts**`, (route) => {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(
          hasConflict
            ? [{ conflicting_booking_id: EXISTING_BOOKING.id, conflict_type: 'location' }]
            : []
        ),
      });
    });

    await page.route(`${API_BASE}/bookings`, (route) => {
      if (route.request().method() === 'POST') {
        route.fulfill({ status: 201, contentType: 'application/json', body: JSON.stringify(CREATED_BOOKING_APPROVED) });
      } else {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
      }
    });

    await page.goto('/bookings/create');
    await page.waitForSelector('[data-testid="booking-form"]', { timeout: 15_000 });

    // Fill form to trigger conflict check
    await page.fill('#booking-title', 'Test Booking');
    await page.selectOption('#booking-location', 'loc-001');
    await page.fill('#booking-start-time', '2024-02-15T09:00');
    await page.fill('#booking-end-time', '2024-02-15T10:00');
    await page.waitForTimeout(800);

    // Hard-block should be visible, Save disabled
    await expect(page.locator('[data-testid="conflict-hard-block"]')).toBeVisible({ timeout: 5_000 });
    await expect(page.locator('[data-testid="booking-save"]')).toBeDisabled();

    // Resolve conflict: change time to non-conflicting slot
    hasConflict = false;
    await page.fill('#booking-start-time', '2024-02-15T14:00');
    await page.fill('#booking-end-time', '2024-02-15T15:00');
    await page.waitForTimeout(800);

    // Hard-block should disappear, Save enabled
    await expect(page.locator('[data-testid="conflict-hard-block"]')).toHaveCount(0);
    await expect(page.locator('[data-testid="booking-save"]')).toBeEnabled();
  });
});

test.describe('Booking Create — Conflict detection (approver soft-warn)', () => {
  test('yellow conflict warning appears and Save stays enabled for approver with conflicts', async ({ page }) => {
    await gotoBookingCreate(page, {
      roles: ['role_calendar_viewer', 'role_booking_approver'],
      conflictResponse: [
        {
          conflicting_booking_id: EXISTING_BOOKING.id,
          conflict_type: 'location',
        },
      ],
    });

    // Fill form to trigger conflict check
    await page.fill('#booking-title', 'Test Booking');
    await page.selectOption('#booking-location', 'loc-001');
    await page.fill('#booking-start-time', '2024-02-15T09:00');
    await page.fill('#booking-end-time', '2024-02-15T10:00');
    await page.waitForTimeout(800);

    // Soft-warn banner should appear
    await expect(page.locator('[data-testid="conflict-soft-warn"]')).toBeVisible({ timeout: 5_000 });

    // Warning should mention conflict
    await expect(page.locator('[data-testid="conflict-soft-warn"]')).toContainText('Warning: Conflict detected');

    // Save button should STILL be enabled for approver (soft-warn)
    await expect(page.locator('[data-testid="booking-save"]')).toBeEnabled();
  });
});

test.describe('Booking Create — Status confirmation banner', () => {
  test('shows "Booking approved" banner after save when response status is approved', async ({ page }) => {
    await gotoBookingCreate(page, {
      roles: ['role_calendar_viewer', 'role_booking_approver'],
      createResponse: CREATED_BOOKING_APPROVED,
      createStatus: 201,
    });

    // Fill required fields
    await page.fill('#booking-title', 'Approved Test Booking');
    await page.selectOption('#booking-location', 'loc-001');
    await page.fill('#booking-start-time', '2024-02-15T11:00');
    await page.fill('#booking-end-time', '2024-02-15T12:00');

    // Submit form
    await page.click('[data-testid="booking-save"]');

    // Confirmation banner should appear
    await expect(page.locator('[data-testid="state-confirmation-banner"]')).toBeVisible({
      timeout: 5_000,
    });

    // Should say "Booking approved"
    await expect(page.locator('[data-testid="state-confirmation-banner"]')).toContainText(
      'Booking approved'
    );
  });

  test('shows "Booking submitted for approval" banner when response status is pending', async ({ page }) => {
    await gotoBookingCreate(page, {
      roles: ['role_calendar_viewer'],
      createResponse: CREATED_BOOKING_PENDING,
      createStatus: 201,
    });

    // Fill required fields
    await page.fill('#booking-title', 'Pending Test Booking');
    await page.selectOption('#booking-location', 'loc-001');
    await page.fill('#booking-start-time', '2024-02-15T11:00');
    await page.fill('#booking-end-time', '2024-02-15T12:00');

    // Submit form
    await page.click('[data-testid="booking-save"]');

    // Confirmation banner should appear
    await expect(page.locator('[data-testid="state-confirmation-banner"]')).toBeVisible({
      timeout: 5_000,
    });

    // Should say "Booking submitted for approval"
    await expect(page.locator('[data-testid="state-confirmation-banner"]')).toContainText(
      'Booking submitted for approval'
    );
  });
});
