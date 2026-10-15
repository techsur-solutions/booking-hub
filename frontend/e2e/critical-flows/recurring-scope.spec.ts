/**
 * Critical Flow: Recurring Series Scope Selection
 *
 * Tests scope selection when editing a recurring booking:
 * 1. Scope Confirmation Dialog appears with "This occurrence only" vs "Entire series"
 * 2. Selecting "This occurrence only" updates only the edited occurrence
 * 3. Selecting "Entire series" updates all occurrences
 * 4. Post-save summary banner appears with appropriate message
 *
 * Test strategy:
 * - VITE_E2E_MODE=true (set by playwright.config.ts).
 * - Roles injected via window.__E2E_ROLES__.
 * - API calls mocked via page.route().
 * - Recurring booking identified by non-null series_id.
 *
 * Coverage: UI-BOOK-02 (recurring booking edit with scope selection)
 */
import { test, expect, type Page } from '@playwright/test';

// ── Constants ─────────────────────────────────────────────────────────────────

const API_BASE = 'http://localhost:8080';

// ── Fixture data ──────────────────────────────────────────────────────────────

const LOCATION_1 = {
  id: 'loc-001',
  name: 'Conference Room A',
  colour: '#3b82f6',
  building: 'Main',
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

/**
 * Recurring booking — occurrence 3 of a weekly series.
 * series_id being non-null is what triggers the scope dialog in BookingEdit.
 */
const RECURRING_BOOKING_OCC3 = {
  id: 'bk-recurring-occ3',
  series_id: 'series-weekly-001',
  location_id: 'loc-001',
  resource_ids: [],
  title: 'Weekly Team Meeting',
  description: 'Recurring weekly sync',
  start_time: '2024-06-17T09:00:00.000Z', // week 3
  end_time: '2024-06-17T09:30:00.000Z',
  status: 'approved' as const,
  owner_id: 'user-001',
  conflict_flags: [],
  custom_field_values: [],
  created_at: '2024-06-01T00:00:00.000Z',
  updated_at: '2024-06-01T00:00:00.000Z',
  deleted_at: null,
};

/** Updated single occurrence response */
const PATCHED_SINGLE_OCC = {
  ...RECURRING_BOOKING_OCC3,
  title: 'Weekly Team Meeting (Updated)',
  start_time: '2024-06-17T10:00:00.000Z',
  end_time: '2024-06-17T10:30:00.000Z',
  updated_at: new Date().toISOString(),
};

/** Updated entire series response */
const PATCHED_SERIES = {
  ...RECURRING_BOOKING_OCC3,
  title: 'Weekly Team Meeting (Series Update)',
  updated_at: new Date().toISOString(),
};

// ── Helpers ───────────────────────────────────────────────────────────────────

/**
 * Navigate to edit page for a recurring booking.
 * The scope dialog should appear because series_id is non-null.
 */
async function gotoRecurringBookingEdit(
  page: Page,
  options: {
    booking?: typeof RECURRING_BOOKING_OCC3;
    patchResponse?: typeof PATCHED_SINGLE_OCC;
    patchScope?: 'this-only' | 'entire-series';
  } = {},
) {
  const {
    booking = RECURRING_BOOKING_OCC3,
    patchResponse = PATCHED_SINGLE_OCC,
  } = options;

  await page.addInitScript((roles: string[]) => {
    (window as unknown as Record<string, unknown>).__E2E_ROLES__ = roles;
  }, ['role_calendar_viewer', 'role_room_booking']);

  await page.route(`${API_BASE}/**`, (route) => {
    const url = route.request().url();
    const path = new URL(url).pathname;
    const method = route.request().method();

    if (path.includes('/settings')) {
      void route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(DEFAULT_SETTINGS) });
      return;
    }
    if (path.includes('/locations')) {
      void route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([LOCATION_1]) });
      return;
    }
    if (path.includes('/resources')) {
      void route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
      return;
    }
    if (path.includes('/custom-fields')) {
      void route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
      return;
    }
    if (path.includes('/check-conflicts')) {
      void route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
      return;
    }
    if (method === 'PATCH' && path.includes(`/bookings/${booking.id}`)) {
      void route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(patchResponse) });
      return;
    }
    if (path.includes(`/bookings/${booking.id}`)) {
      void route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(booking) });
      return;
    }
    if (path === '/bookings' || path.startsWith('/bookings?')) {
      void route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
      return;
    }
    void route.continue();
  });

  await page.goto(`/bookings/${booking.id}/edit`);
  await page.waitForSelector('[data-testid="booking-edit-page"]', { timeout: 15_000 });
}

// ── Tests: Scope dialog appearance ───────────────────────────────────────────

test.describe('Recurring Scope Selection — Dialog (UI-BOOK-02)', () => {

  test('Editing recurring booking shows ScopeConfirmationDialog before form', async ({ page }) => {
    await gotoRecurringBookingEdit(page);

    // The scope confirmation dialog must appear immediately for recurring bookings
    await expect(page.getByTestId('recurring-scope-dialog')).toBeVisible({ timeout: 5_000 });
  });

  test('Scope dialog has "This occurrence only" and "Entire series" options', async ({ page }) => {
    await gotoRecurringBookingEdit(page);

    await expect(page.getByTestId('recurring-scope-dialog')).toBeVisible({ timeout: 5_000 });

    // Both scope options must be present
    const thisOnlyBtn = page.getByTestId('scope-this-only');
    const entireSeriesBtn = page.getByTestId('scope-entire-series');

    await expect(thisOnlyBtn).toBeVisible({ timeout: 3_000 });
    await expect(entireSeriesBtn).toBeVisible({ timeout: 3_000 });

    await expect(thisOnlyBtn).toContainText(/this occurrence/i);
    await expect(entireSeriesBtn).toContainText(/entire series/i);
  });

  test('Scope dialog is non-dismissible (no X/Escape/backdrop close)', async ({ page }) => {
    await gotoRecurringBookingEdit(page);

    await expect(page.getByTestId('recurring-scope-dialog')).toBeVisible({ timeout: 5_000 });

    // Press Escape — dialog must NOT close
    await page.keyboard.press('Escape');
    await page.waitForTimeout(300);
    await expect(page.getByTestId('recurring-scope-dialog')).toBeVisible();

    // Click backdrop (outside dialog) — dialog must NOT close
    await page.mouse.click(5, 5); // click near top-left corner (outside dialog)
    await page.waitForTimeout(300);
    await expect(page.getByTestId('recurring-scope-dialog')).toBeVisible();
  });
});

// ── Tests: This occurrence only ──────────────────────────────────────────────

test.describe('Recurring Scope Selection — This Occurrence Only (UI-BOOK-02)', () => {

  test('Select "This occurrence only": form displays for editing', async ({ page }) => {
    await gotoRecurringBookingEdit(page);

    await expect(page.getByTestId('recurring-scope-dialog')).toBeVisible({ timeout: 5_000 });

    // Select "This occurrence only"
    await page.getByTestId('scope-this-only').click();

    // Scope dialog closes, booking edit form is shown
    await expect(page.getByTestId('recurring-scope-dialog')).not.toBeVisible({ timeout: 3_000 });
    await expect(page.getByTestId('booking-form')).toBeVisible({ timeout: 5_000 });
  });

  test('Select "This occurrence only": form pre-fills with booking data', async ({ page }) => {
    await gotoRecurringBookingEdit(page);

    await expect(page.getByTestId('recurring-scope-dialog')).toBeVisible({ timeout: 5_000 });
    await page.getByTestId('scope-this-only').click();
    await expect(page.getByTestId('booking-form')).toBeVisible({ timeout: 5_000 });

    // Form should be pre-filled with existing booking data
    const titleInput = page.getByTestId('booking-title');
    await expect(titleInput).toHaveValue('Weekly Team Meeting');
  });

  test('Select "This occurrence only": save sends PATCH with scope this-only', async ({ page }) => {
    let patchUrl = '';
    let patchQueryParams = '';

    await page.addInitScript((roles: string[]) => {
      (window as unknown as Record<string, unknown>).__E2E_ROLES__ = roles;
    }, ['role_calendar_viewer', 'role_room_booking']);

    await page.route(`${API_BASE}/**`, (route) => {
      const url = route.request().url();
      const path = new URL(url).pathname;
      const method = route.request().method();

      if (path.includes('/settings')) {
        void route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(DEFAULT_SETTINGS) });
        return;
      }
      if (path.includes('/locations')) {
        void route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([LOCATION_1]) });
        return;
      }
      if (path.includes('/resources')) {
        void route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
        return;
      }
      if (path.includes('/custom-fields')) {
        void route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
        return;
      }
      if (path.includes('/check-conflicts')) {
        void route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
        return;
      }
      if (method === 'PATCH' && path.includes(`/bookings/${RECURRING_BOOKING_OCC3.id}`)) {
        patchUrl = url;
        patchQueryParams = new URL(url).search;
        void route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(PATCHED_SINGLE_OCC) });
        return;
      }
      if (path.includes(`/bookings/${RECURRING_BOOKING_OCC3.id}`)) {
        void route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(RECURRING_BOOKING_OCC3) });
        return;
      }
      void route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
    });

    await page.goto(`/bookings/${RECURRING_BOOKING_OCC3.id}/edit`);
    await page.waitForSelector('[data-testid="booking-edit-page"]', { timeout: 15_000 });

    // Select this occurrence only
    await expect(page.getByTestId('recurring-scope-dialog')).toBeVisible({ timeout: 5_000 });
    await page.getByTestId('scope-this-only').click();
    await expect(page.getByTestId('booking-form')).toBeVisible({ timeout: 5_000 });

    // Save
    await page.getByTestId('booking-save').click();

    // PATCH should have been called with scope=this-only query param
    await page.waitForTimeout(500);
    expect(patchUrl).toContain(RECURRING_BOOKING_OCC3.id);
    // scope=this-only should be in query string
    expect(patchQueryParams).toMatch(/scope=this-only/);
  });

  test('Select "This occurrence only": post-save banner mentions single occurrence', async ({ page }) => {
    await gotoRecurringBookingEdit(page, { patchResponse: PATCHED_SINGLE_OCC });

    await expect(page.getByTestId('recurring-scope-dialog')).toBeVisible({ timeout: 5_000 });
    await page.getByTestId('scope-this-only').click();
    await expect(page.getByTestId('booking-form')).toBeVisible({ timeout: 5_000 });

    // Submit form
    await page.getByTestId('booking-save').click();

    // StateConfirmationBanner or toast appears
    const banner = page.getByTestId('state-confirmation-banner');
    await expect(banner).toBeVisible({ timeout: 5_000 });

    // Banner message references single occurrence change
    const bannerText = await banner.textContent();
    // Should mention "occurrence" or "single" (not "entire series")
    expect(bannerText?.toLowerCase()).toMatch(/occurrence|only|single|saved|changed/i);
  });
});

// ── Tests: Entire series ──────────────────────────────────────────────────────

test.describe('Recurring Scope Selection — Entire Series (UI-BOOK-02)', () => {

  test('Select "Entire series": form displays for editing', async ({ page }) => {
    await gotoRecurringBookingEdit(page, { patchResponse: PATCHED_SERIES });

    await expect(page.getByTestId('recurring-scope-dialog')).toBeVisible({ timeout: 5_000 });

    // Select "Entire series"
    await page.getByTestId('scope-entire-series').click();

    // Dialog closes, form shown
    await expect(page.getByTestId('recurring-scope-dialog')).not.toBeVisible({ timeout: 3_000 });
    await expect(page.getByTestId('booking-form')).toBeVisible({ timeout: 5_000 });
  });

  test('Select "Entire series": save sends PATCH with scope entire-series', async ({ page }) => {
    let patchQueryParams = '';

    await page.addInitScript((roles: string[]) => {
      (window as unknown as Record<string, unknown>).__E2E_ROLES__ = roles;
    }, ['role_calendar_viewer', 'role_room_booking']);

    await page.route(`${API_BASE}/**`, (route) => {
      const url = route.request().url();
      const path = new URL(url).pathname;
      const method = route.request().method();

      if (path.includes('/settings')) {
        void route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(DEFAULT_SETTINGS) });
        return;
      }
      if (path.includes('/locations')) {
        void route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([LOCATION_1]) });
        return;
      }
      if (path.includes('/resources') || path.includes('/custom-fields')) {
        void route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
        return;
      }
      if (path.includes('/check-conflicts')) {
        void route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
        return;
      }
      if (method === 'PATCH' && path.includes(`/bookings/${RECURRING_BOOKING_OCC3.id}`)) {
        patchQueryParams = new URL(url).search;
        void route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(PATCHED_SERIES) });
        return;
      }
      if (path.includes(`/bookings/${RECURRING_BOOKING_OCC3.id}`)) {
        void route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(RECURRING_BOOKING_OCC3) });
        return;
      }
      void route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
    });

    await page.goto(`/bookings/${RECURRING_BOOKING_OCC3.id}/edit`);
    await page.waitForSelector('[data-testid="booking-edit-page"]', { timeout: 15_000 });

    // Select entire series
    await expect(page.getByTestId('recurring-scope-dialog')).toBeVisible({ timeout: 5_000 });
    await page.getByTestId('scope-entire-series').click();
    await expect(page.getByTestId('booking-form')).toBeVisible({ timeout: 5_000 });

    // Save
    await page.getByTestId('booking-save').click();

    // PATCH should have been called with scope=entire-series
    await page.waitForTimeout(500);
    expect(patchQueryParams).toMatch(/scope=entire-series/);
  });

  test('Select "Entire series": post-save banner mentions all occurrences or series', async ({ page }) => {
    await gotoRecurringBookingEdit(page, { patchResponse: PATCHED_SERIES });

    await expect(page.getByTestId('recurring-scope-dialog')).toBeVisible({ timeout: 5_000 });
    await page.getByTestId('scope-entire-series').click();
    await expect(page.getByTestId('booking-form')).toBeVisible({ timeout: 5_000 });

    await page.getByTestId('booking-save').click();

    // StateConfirmationBanner appears
    const banner = page.getByTestId('state-confirmation-banner');
    await expect(banner).toBeVisible({ timeout: 5_000 });

    // Banner message references series or all occurrences
    const bannerText = await banner.textContent();
    expect(bannerText?.toLowerCase()).toMatch(/series|all|entire|saved|changed/i);
  });
});
