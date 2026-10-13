import { test, expect } from '@playwright/test';

/**
 * Booking Edit E2E Tests
 *
 * Tests run with VITE_E2E_MODE=true (playwright.config.ts webServer.env).
 * Roles injected via page.addInitScript() → window.__E2E_ROLES__.
 * API responses mocked via page.route('http://localhost:8080/**').
 *
 * Covers plan 09-10 acceptance criteria:
 * 1. Non-recurring booking edit — no scope dialog, form pre-filled, saves with banner
 * 2. Recurring booking edit — scope dialog appears, this-only selection, single occurrence updated
 * 3. Recurring booking edit — entire-series selection, all occurrences updated with count
 * 4. Re-validation with conflict — ConflictWarning appears, Save blocked for non-approver
 */

// ── Fixture data ──────────────────────────────────────────────────────────────

const API_BASE = 'http://localhost:8080';

const LOCATION_1 = {
  id: 'loc-001',
  name: 'Main Hall',
  colour: '#3b82f6',
  building: 'Building A',
  layouts: null,
  deleted_at: null,
};

const LOCATION_2 = {
  id: 'loc-002',
  name: 'Boardroom',
  colour: '#10b981',
  building: 'Building B',
  layouts: null,
  deleted_at: null,
};

const DEFAULT_SETTINGS = {
  id: 1,
  approve_booking: false,
  calendar_slot_size: 30,
  calendar_min_time: '08:00',
  calendar_max_time: '18:00',
};

/** Non-recurring booking — no series_id */
const NON_RECURRING_BOOKING = {
  id: 'bk-non-recurring',
  series_id: null,
  location_id: 'loc-001',
  resource_ids: [],
  title: 'Team Meeting',
  description: 'Weekly sync',
  start_time: '2024-06-10T09:00:00.000Z',
  end_time: '2024-06-10T10:00:00.000Z',
  status: 'approved',
  owner_id: 'user-001',
  conflict_flags: [],
  custom_field_values: [],
  created_at: '2024-01-01T00:00:00.000Z',
  updated_at: '2024-01-01T00:00:00.000Z',
  deleted_at: null,
};

/** Recurring booking — has series_id */
const RECURRING_BOOKING = {
  id: 'bk-recurring-1',
  series_id: 'series-abc',
  location_id: 'loc-001',
  resource_ids: [],
  title: 'Daily Standup',
  description: 'Recurring daily meeting',
  start_time: '2024-06-10T09:00:00.000Z',
  end_time: '2024-06-10T09:30:00.000Z',
  status: 'approved',
  owner_id: 'user-001',
  conflict_flags: [],
  custom_field_values: [],
  created_at: '2024-01-01T00:00:00.000Z',
  updated_at: '2024-01-01T00:00:00.000Z',
  deleted_at: null,
};

/** Existing booking for conflict testing */
const CONFLICTING_BOOKING = {
  id: 'bk-conflict-source',
  series_id: null,
  location_id: 'loc-001',
  resource_ids: [],
  title: 'Existing Event',
  description: null,
  start_time: '2024-06-15T10:00:00.000Z',
  end_time: '2024-06-15T11:00:00.000Z',
  status: 'approved',
  owner_id: 'user-002',
  conflict_flags: [],
  custom_field_values: [],
  created_at: '2024-01-01T00:00:00.000Z',
  updated_at: '2024-01-01T00:00:00.000Z',
  deleted_at: null,
};

// ── Helpers ───────────────────────────────────────────────────────────────────

/**
 * Navigate to /bookings/:id/edit with mocked API responses.
 * Intercepts all requests to localhost:8080 and routes based on URL path.
 */
async function gotoBookingEdit(
  page: import('@playwright/test').Page,
  options: {
    bookingId: string;
    booking: unknown;
    locations?: unknown[];
    settings?: unknown;
    roles?: string[];
    /** Override PATCH response */
    patchResponse?: unknown;
    /** Override conflict check response */
    conflictResponse?: unknown[];
  }
) {
  const {
    bookingId,
    booking,
    locations = [LOCATION_1, LOCATION_2],
    settings = DEFAULT_SETTINGS,
    roles = ['role_calendar_viewer', 'role_booking'],
    patchResponse = { ...booking, updated_at: new Date().toISOString() },
    conflictResponse = [],
  } = options;

  // 1. Inject Keycloak roles before page JS runs
  await page.addInitScript((injectedRoles: string[]) => {
    (window as unknown as Record<string, unknown>).__E2E_ROLES__ = injectedRoles;
  }, roles);

  // 2. Mock all API gateway calls
  await page.route(`${API_BASE}/**`, (route) => {
    const url = route.request().url();
    const method = route.request().method();

    if (url.includes('/settings')) {
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(settings) });
    } else if (url.includes('/locations')) {
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(locations) });
    } else if (url.includes('/resources')) {
      route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
    } else if (url.includes('/custom-fields')) {
      route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
    } else if (url.includes('/check-conflicts')) {
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(conflictResponse) });
    } else if (url.includes(`/bookings/${bookingId}`) && (method === 'PATCH' || method === 'PUT')) {
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(patchResponse) });
    } else if (url.includes(`/bookings/${bookingId}`)) {
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(booking) });
    } else if (url.includes('/bookings')) {
      route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
    } else {
      route.fulfill({ status: 200, contentType: 'application/json', body: '{}' });
    }
  });

  // 3. Navigate to edit page
  await page.goto(`/bookings/${bookingId}/edit`);
  await page.waitForSelector('[data-testid="booking-edit-page"]', { timeout: 15_000 });
}

// ── Tests ─────────────────────────────────────────────────────────────────────

test.describe('Booking Edit — non-recurring booking', () => {
  /**
   * Test 1: Non-recurring booking — no scope dialog, form pre-filled, saves with banner.
   */
  test('non-recurring booking: no scope dialog, form pre-filled, saves with banner', async ({ page }) => {
    let patchCalled = false;
    let patchUrl = '';

    await page.addInitScript((roles: string[]) => {
      (window as unknown as Record<string, unknown>).__E2E_ROLES__ = roles;
    }, ['role_calendar_viewer', 'role_booking']);

    await page.route(`${API_BASE}/**`, (route) => {
      const url = route.request().url();
      const method = route.request().method();

      if (url.includes('/settings')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(DEFAULT_SETTINGS) });
      } else if (url.includes('/locations')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([LOCATION_1, LOCATION_2]) });
      } else if (url.includes('/resources')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      } else if (url.includes('/custom-fields')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      } else if (url.includes('/check-conflicts')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      } else if (url.includes(`/bookings/${NON_RECURRING_BOOKING.id}`) && (method === 'PATCH' || method === 'PUT')) {
        patchCalled = true;
        patchUrl = url;
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ ...NON_RECURRING_BOOKING, title: 'Updated Title' }),
        });
      } else if (url.includes(`/bookings/${NON_RECURRING_BOOKING.id}`)) {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(NON_RECURRING_BOOKING) });
      } else {
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      }
    });

    await page.goto(`/bookings/${NON_RECURRING_BOOKING.id}/edit`);
    await page.waitForSelector('[data-testid="booking-edit-page"]', { timeout: 15_000 });

    // Scope dialog should NOT appear for non-recurring
    await expect(page.locator('[data-testid="recurring-scope-dialog"]')).toHaveCount(0);

    // Form should be pre-filled
    await expect(page.locator('[data-testid="booking-form"]')).toBeVisible();
    const titleInput = page.locator('[data-testid="booking-title"]');
    await expect(titleInput).toHaveValue('Team Meeting');

    // Change title
    await titleInput.fill('Updated Title');

    // Click Save
    await page.click('[data-testid="booking-save"]');

    // Wait for confirmation banner
    await expect(page.locator('[data-testid="state-confirmation-banner"]')).toBeVisible({ timeout: 5_000 });

    // Verify PATCH was called (no scope param for non-recurring)
    expect(patchCalled).toBe(true);
    expect(patchUrl).not.toContain('scope=');
  });
});

test.describe('Booking Edit — recurring booking scope dialog', () => {
  /**
   * Test 2: Recurring booking — scope dialog appears immediately, non-dismissible,
   * this-only selection proceeds to form, saves with "Only this occurrence changed" banner.
   */
  test('recurring booking: scope dialog appears, this-only selection works', async ({ page }) => {
    let patchUrl = '';

    await page.addInitScript((roles: string[]) => {
      (window as unknown as Record<string, unknown>).__E2E_ROLES__ = roles;
    }, ['role_calendar_viewer', 'role_booking']);

    await page.route(`${API_BASE}/**`, (route) => {
      const url = route.request().url();
      const method = route.request().method();

      if (url.includes('/settings')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(DEFAULT_SETTINGS) });
      } else if (url.includes('/locations')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([LOCATION_1, LOCATION_2]) });
      } else if (url.includes('/resources')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      } else if (url.includes('/custom-fields')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      } else if (url.includes('/check-conflicts')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      } else if (url.includes(`/bookings/${RECURRING_BOOKING.id}`) && (method === 'PATCH' || method === 'PUT')) {
        patchUrl = url;
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ ...RECURRING_BOOKING, recurrence_updated_count: 1 }),
        });
      } else if (url.includes(`/bookings/${RECURRING_BOOKING.id}`)) {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(RECURRING_BOOKING) });
      } else {
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      }
    });

    await page.goto(`/bookings/${RECURRING_BOOKING.id}/edit`);
    await page.waitForSelector('[data-testid="booking-edit-page"]', { timeout: 15_000 });

    // Scope dialog MUST appear for recurring booking
    const dialog = page.locator('[data-testid="recurring-scope-dialog"]');
    await expect(dialog).toBeVisible();

    // Verify dialog has both buttons
    await expect(page.locator('[data-testid="scope-this-only"]')).toBeVisible();
    await expect(page.locator('[data-testid="scope-entire-series"]')).toBeVisible();

    // Verify no X button (non-dismissible) — check modal__close button doesn't exist
    await expect(dialog.locator('.modal__close')).toHaveCount(0);

    // Verify form is NOT visible yet (waiting for scope selection)
    await expect(page.locator('[data-testid="booking-form"]')).toHaveCount(0);

    // Select "This occurrence only"
    await page.click('[data-testid="scope-this-only"]');

    // Dialog should disappear, form should appear
    await expect(dialog).toHaveCount(0);
    await expect(page.locator('[data-testid="booking-form"]')).toBeVisible();

    // Recurring badge should be visible in page header
    await expect(page.locator('[data-testid="recurring-badge"]')).toBeVisible();

    // Form should be pre-filled with booking title
    await expect(page.locator('[data-testid="booking-title"]')).toHaveValue('Daily Standup');

    // Click Save
    await page.click('[data-testid="booking-save"]');

    // Verify banner shows "Only this occurrence changed"
    await expect(page.locator('[data-testid="state-confirmation-banner"]')).toBeVisible({ timeout: 5_000 });
    await expect(page.locator('[data-testid="state-confirmation-banner"]')).toContainText('Only this occurrence changed');

    // Verify PATCH called with scope=this-only
    expect(patchUrl).toContain('scope=this-only');
  });

  /**
   * Test 3: Recurring booking — entire-series selection, all occurrences updated with count.
   */
  test('recurring booking: entire-series selection updates all occurrences', async ({ page }) => {
    let patchUrl = '';

    await page.addInitScript((roles: string[]) => {
      (window as unknown as Record<string, unknown>).__E2E_ROLES__ = roles;
    }, ['role_calendar_viewer', 'role_booking']);

    await page.route(`${API_BASE}/**`, (route) => {
      const url = route.request().url();
      const method = route.request().method();

      if (url.includes('/settings')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(DEFAULT_SETTINGS) });
      } else if (url.includes('/locations')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([LOCATION_1, LOCATION_2]) });
      } else if (url.includes('/resources')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      } else if (url.includes('/custom-fields')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      } else if (url.includes('/check-conflicts')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      } else if (url.includes(`/bookings/${RECURRING_BOOKING.id}`) && (method === 'PATCH' || method === 'PUT')) {
        patchUrl = url;
        // Return response with recurrence_updated_count = 4 (4 occurrences updated)
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ ...RECURRING_BOOKING, recurrence_updated_count: 4 }),
        });
      } else if (url.includes(`/bookings/${RECURRING_BOOKING.id}`)) {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(RECURRING_BOOKING) });
      } else {
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      }
    });

    await page.goto(`/bookings/${RECURRING_BOOKING.id}/edit`);
    await page.waitForSelector('[data-testid="booking-edit-page"]', { timeout: 15_000 });

    // Scope dialog appears
    await expect(page.locator('[data-testid="recurring-scope-dialog"]')).toBeVisible();

    // Select "Entire series"
    await page.click('[data-testid="scope-entire-series"]');

    // Form appears
    await expect(page.locator('[data-testid="booking-form"]')).toBeVisible();

    // Save the form
    await page.click('[data-testid="booking-save"]');

    // Banner shows "Entire series updated (4 occurrences)"
    await expect(page.locator('[data-testid="state-confirmation-banner"]')).toBeVisible({ timeout: 5_000 });
    await expect(page.locator('[data-testid="state-confirmation-banner"]')).toContainText('Entire series updated (4 occurrences)');

    // Verify PATCH called with scope=entire-series
    expect(patchUrl).toContain('scope=entire-series');
  });
});

test.describe('Booking Edit — re-validation with conflict detection', () => {
  /**
   * Test 4: Re-validation with conflict — ConflictWarning appears, Save blocked for non-approver.
   * Resolving conflict (changing time) re-enables Save.
   */
  test('re-validation: ConflictWarning blocks save for non-approver, resolves when conflict cleared', async ({ page }) => {
    let conflictCheckCount = 0;

    await page.addInitScript((roles: string[]) => {
      (window as unknown as Record<string, unknown>).__E2E_ROLES__ = roles;
    }, ['role_calendar_viewer', 'role_booking']); // non-approver

    await page.route(`${API_BASE}/**`, (route) => {
      const url = route.request().url();
      const method = route.request().method();

      if (url.includes('/settings')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(DEFAULT_SETTINGS) });
      } else if (url.includes('/locations')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([LOCATION_1, LOCATION_2]) });
      } else if (url.includes('/resources')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      } else if (url.includes('/custom-fields')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      } else if (url.includes('/check-conflicts')) {
        conflictCheckCount++;
        // Return conflict on first few checks, then no conflict after sufficient wait
        const conflicts = [
          {
            conflicting_booking_id: CONFLICTING_BOOKING.id,
            conflict_type: 'location',
          },
        ];
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(conflicts),
        });
      } else if (url.includes(`/bookings/${NON_RECURRING_BOOKING.id}`) && (method === 'PATCH' || method === 'PUT')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(NON_RECURRING_BOOKING) });
      } else if (url.includes(`/bookings/${NON_RECURRING_BOOKING.id}`)) {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(NON_RECURRING_BOOKING) });
      } else {
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      }
    });

    await page.goto(`/bookings/${NON_RECURRING_BOOKING.id}/edit`);
    await page.waitForSelector('[data-testid="booking-edit-page"]', { timeout: 15_000 });

    // Wait for conflict check to fire (debounced 500ms)
    await page.waitForTimeout(800);

    // ConflictWarning hard-block banner should appear (non-approver)
    await expect(page.locator('[data-testid="conflict-hard-block"]')).toBeVisible({ timeout: 5_000 });

    // Conflicting booking ID should be listed
    await expect(page.locator('[data-testid="conflict-hard-block"]')).toContainText(CONFLICTING_BOOKING.id);

    // Save button should be disabled for non-approver
    await expect(page.locator('[data-testid="booking-save"]')).toBeDisabled();

    // Verify conflict check count > 0 (re-validation ran fresh, did not inherit from original booking)
    expect(conflictCheckCount).toBeGreaterThan(0);

    // Now override conflict check to return no conflicts (simulate resolving the conflict)
    await page.unroute(`${API_BASE}/**`);
    await page.route(`${API_BASE}/**`, (route) => {
      const url = route.request().url();
      const method = route.request().method();

      if (url.includes('/settings')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(DEFAULT_SETTINGS) });
      } else if (url.includes('/locations')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([LOCATION_1, LOCATION_2]) });
      } else if (url.includes('/resources')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      } else if (url.includes('/custom-fields')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      } else if (url.includes('/check-conflicts')) {
        // No conflicts now
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      } else if (url.includes(`/bookings/${NON_RECURRING_BOOKING.id}`) && (method === 'PATCH' || method === 'PUT')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(NON_RECURRING_BOOKING) });
      } else if (url.includes(`/bookings/${NON_RECURRING_BOOKING.id}`)) {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(NON_RECURRING_BOOKING) });
      } else {
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      }
    });

    // Change start time to trigger re-validation with no conflict
    const startInput = page.locator('[data-testid="booking-start-time"]');
    await startInput.fill('2024-06-11T14:00');
    const endInput = page.locator('[data-testid="booking-end-time"]');
    await endInput.fill('2024-06-11T15:00');

    // Wait for debounced conflict re-check
    await page.waitForTimeout(800);

    // ConflictWarning should no longer show hard-block
    await expect(page.locator('[data-testid="conflict-hard-block"]')).toHaveCount(0);

    // Save button should be enabled again
    await expect(page.locator('[data-testid="booking-save"]')).toBeEnabled();
  });

  /**
   * Test 4b: Approver sees soft-warn (not hard-block) when conflicts detected.
   */
  test('re-validation: approver sees soft-warn (not hard-block) for conflicts', async ({ page }) => {
    await page.addInitScript((roles: string[]) => {
      (window as unknown as Record<string, unknown>).__E2E_ROLES__ = roles;
    }, ['role_calendar_viewer', 'role_booking', 'role_booking_approver']); // APPROVER

    await page.route(`${API_BASE}/**`, (route) => {
      const url = route.request().url();
      const method = route.request().method();

      if (url.includes('/settings')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(DEFAULT_SETTINGS) });
      } else if (url.includes('/locations')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([LOCATION_1, LOCATION_2]) });
      } else if (url.includes('/resources')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      } else if (url.includes('/custom-fields')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      } else if (url.includes('/check-conflicts')) {
        // Return a conflict
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([{ conflicting_booking_id: 'bk-existing', conflict_type: 'location' }]),
        });
      } else if (url.includes(`/bookings/${NON_RECURRING_BOOKING.id}`) && (method === 'PATCH' || method === 'PUT')) {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(NON_RECURRING_BOOKING) });
      } else if (url.includes(`/bookings/${NON_RECURRING_BOOKING.id}`)) {
        route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(NON_RECURRING_BOOKING) });
      } else {
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      }
    });

    await page.goto(`/bookings/${NON_RECURRING_BOOKING.id}/edit`);
    await page.waitForSelector('[data-testid="booking-edit-page"]', { timeout: 15_000 });

    // Wait for conflict check to fire
    await page.waitForTimeout(800);

    // Soft-warn (not hard-block) should appear for approver
    await expect(page.locator('[data-testid="conflict-soft-warn"]')).toBeVisible({ timeout: 5_000 });
    await expect(page.locator('[data-testid="conflict-hard-block"]')).toHaveCount(0);

    // Save button should be ENABLED for approver (soft-warn, not hard-block)
    await expect(page.locator('[data-testid="booking-save"]')).toBeEnabled();
  });
});
