import { test, expect } from '@playwright/test';

/**
 * Day View End-to-End Tests
 *
 * Tests run with VITE_E2E_MODE=true (playwright.config.ts webServer.env).
 * Per-test Keycloak roles injected via page.addInitScript() → window.__E2E_ROLES__.
 * API responses mocked via page.route('http://localhost:8080/**') — matches Axios
 * baseURL (VITE_API_BASE_URL || 'http://localhost:8080').
 *
 * Covered:
 *   1. Hourly slots render from min to max time (default 07:00–22:00)
 *   2. Bookings appear in correct time slots with location colour
 *   3. ConflictBadge visible on conflicted bookings
 *   4. Clicking empty slot opens Create Form pre-filled with location/time
 *   5. Clicking booking event opens Detail Modal with booking title
 *   6. Location filter dropdown changes displayed bookings
 */

// ── Fixture data ──────────────────────────────────────────────────────────────

const TODAY = new Date().toISOString().substring(0, 10); // 'YYYY-MM-DD'

/** Build an ISO datetime for today at a given HH:mm */
function todayAt(time: string): string {
  return `${TODAY}T${time}:00.000Z`;
}

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

/** A normal booking in LOCATION_1 at 09:00–10:00, no conflicts */
const BOOKING_NO_CONFLICT = {
  id: 'bk-001',
  series_id: null,
  location_id: 'loc-001',
  resource_ids: [],
  title: 'Team Standup',
  description: 'Daily sync',
  start_time: todayAt('09:00'),
  end_time: todayAt('10:00'),
  status: 'approved',
  owner_id: 'user-001',
  conflict_flags: [],
  custom_field_values: [],
  created_at: new Date().toISOString(),
  updated_at: new Date().toISOString(),
  deleted_at: null,
};

/** A booking in LOCATION_1 at 09:00–10:00, with a location conflict */
const BOOKING_WITH_CONFLICT = {
  id: 'bk-002',
  series_id: null,
  location_id: 'loc-001',
  resource_ids: [],
  title: 'All-Hands Meeting',
  description: null,
  start_time: todayAt('09:00'),
  end_time: todayAt('10:00'),
  status: 'pending',
  owner_id: 'user-001',
  conflict_flags: [
    {
      conflicting_booking_id: 'bk-001',
      conflict_type: 'location',
    },
  ],
  custom_field_values: [],
  created_at: new Date().toISOString(),
  updated_at: new Date().toISOString(),
  deleted_at: null,
};

/** A booking in LOCATION_2 at 14:00–15:00, no conflicts */
const BOOKING_LOCATION_2 = {
  id: 'bk-003',
  series_id: null,
  location_id: 'loc-002',
  resource_ids: [],
  title: 'Board Review',
  description: null,
  start_time: todayAt('14:00'),
  end_time: todayAt('15:00'),
  status: 'approved',
  owner_id: 'user-002',
  conflict_flags: [],
  custom_field_values: [],
  created_at: new Date().toISOString(),
  updated_at: new Date().toISOString(),
  deleted_at: null,
};

/** Default settings (7:00–22:00, 30-min slots) */
const DEFAULT_SETTINGS = {
  id: 1,
  approve_booking: false,
  calendar_slot_size: 30,
  calendar_min_time: '07:00',
  calendar_max_time: '22:00',
};

// ── Setup helper ──────────────────────────────────────────────────────────────

/**
 * Navigate to /bookings?view=day with E2E roles, mocked API responses.
 *
 * All API mocks target 'http://localhost:8080/**' which is the Axios baseURL
 * (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').
 * This pattern only matches XHR requests to the gateway, never Vite navigation.
 */
async function gotoDayViewWithMocks(
  page: import('@playwright/test').Page,
  options: {
    bookings?: unknown[];
    locations?: unknown[];
    roles?: string[];
    settings?: unknown;
    /** Custom bookings response handler — overrides default if provided */
    bookingsHandler?: (url: string) => unknown;
  } = {},
) {
  const {
    bookings = [BOOKING_NO_CONFLICT, BOOKING_WITH_CONFLICT],
    locations = [LOCATION_1, LOCATION_2],
    roles = ['role_calendar_viewer'],
    settings = DEFAULT_SETTINGS,
    bookingsHandler,
  } = options;

  // 1. Inject Keycloak roles before page JS runs
  await page.addInitScript((injectedRoles: string[]) => {
    (window as unknown as Record<string, unknown>).__E2E_ROLES__ = injectedRoles;
  }, roles);

  // 2. Mock all API gateway calls via exact baseURL match
  //    This pattern only intercepts XHR to http://localhost:8080, never navigation.
  await page.route('http://localhost:8080/**', (route) => {
    const url = route.request().url();

    if (url.includes('/settings')) {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(settings),
      });
    } else if (url.includes('/locations')) {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(locations),
      });
    } else if (url.includes('/bookings')) {
      const responseBody = bookingsHandler ? bookingsHandler(url) : bookings;
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(responseBody),
      });
    } else {
      // Anything else (resources, users, etc.): return empty array
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: '[]',
      });
    }
  });

  // 3. Navigate and wait for Day View to render
  await page.goto('/bookings?view=day');
  await page.waitForSelector('[data-testid="day-view"]', { timeout: 15_000 });
}

// ── Tests ─────────────────────────────────────────────────────────────────────

test.describe('Day View — hourly slot rendering', () => {
  /**
   * Test 1: Hourly slots render from minTime to maxTime.
   *
   * Default settings: 07:00–22:00, 30-min slots → 30 slot rows expected.
   * (22h - 7h) = 15h × 60 min / 30 min = 30 slots.
   */
  test('renders hourly slots from 07:00 to 22:00', async ({ page }) => {
    await gotoDayViewWithMocks(page, { bookings: [] });

    // The slot grid must be present
    const grid = page.locator('[data-testid="hourly-slot-grid"]');
    await expect(grid).toBeVisible();

    // Verify first and last slot time labels exist
    const firstSlot = page.locator('.hourly-slot-grid__row[data-time="07:00"]');
    const lastSlot = page.locator('.hourly-slot-grid__row[data-time="21:30"]');
    await expect(firstSlot).toBeVisible();
    await expect(lastSlot).toBeVisible();

    // Total slot count: 30 slots (07:00 to 21:30 in 30-min increments)
    const slotRows = page.locator('.hourly-slot-grid__row');
    await expect(slotRows).toHaveCount(30);
  });

  /**
   * Test 2: Bookings appear in slot grid with location colour.
   *
   * DayViewEvent uses --location-color CSS custom property from location.colour.
   * Verify the booking title is visible and the correct colour is applied.
   */
  test('bookings display in slot grid with location colour', async ({ page }) => {
    await gotoDayViewWithMocks(page, {
      bookings: [BOOKING_NO_CONFLICT],
      locations: [LOCATION_1],
    });

    // Booking event must be rendered
    const event = page.locator('[data-testid="day-view-event"]').first();
    await expect(event).toBeVisible({ timeout: 10_000 });

    // Booking title must be visible
    await expect(
      page.locator('.day-view-event__title').first(),
    ).toContainText('Team Standup');

    // Verify the --location-color style property is set (LOCATION_1.colour)
    const styleAttr = await event.getAttribute('style');
    expect(styleAttr).toContain('#3b82f6'); // LOCATION_1.colour
  });

  /**
   * Test 3: ConflictBadge visible on conflicted bookings.
   *
   * BOOKING_WITH_CONFLICT has conflict_flags.length > 0 → DayViewEvent renders
   * ConflictBadge. BOOKING_NO_CONFLICT has no flags → no additional badge.
   */
  test('ConflictBadge visible on bookings with conflict_flags', async ({ page }) => {
    await gotoDayViewWithMocks(page, {
      bookings: [BOOKING_NO_CONFLICT, BOOKING_WITH_CONFLICT],
      locations: [LOCATION_1],
    });

    // At least one ConflictBadge must be visible (from BOOKING_WITH_CONFLICT)
    const badge = page.locator('[data-testid="conflict-badge"]');
    await expect(badge).toBeVisible({ timeout: 10_000 });
  });

  test('no ConflictBadge on bookings without conflict_flags', async ({ page }) => {
    await gotoDayViewWithMocks(page, {
      bookings: [BOOKING_NO_CONFLICT],
      locations: [LOCATION_1],
    });

    // Wait for booking event to render
    await page.waitForSelector('[data-testid="day-view-event"]', { timeout: 10_000 });

    // No conflict badge should be present
    const badgeCount = await page.locator('[data-testid="conflict-badge"]').count();
    expect(badgeCount).toBe(0);
  });
});

test.describe('Day View — slot click opens Create Form', () => {
  /**
   * Test 4: Clicking empty slot triggers Create Form with pre-filled location/time.
   *
   * The Create Form modal opens when onSlotClick fires.
   * Modal title: "New Booking".
   * Pre-filled time is shown in modal body.
   */
  test('clicking empty slot opens Create Form modal with pre-filled time', async ({ page }) => {
    await gotoDayViewWithMocks(page, {
      bookings: [], // no bookings → all slots are empty
      locations: [LOCATION_1],
    });

    // Find the 09:00 slot row and click it
    const slot = page.locator('.hourly-slot-grid__row[data-time="09:00"]');
    await expect(slot).toBeVisible();
    await slot.click();

    // Modal must open with "New Booking" title
    const modal = page.locator('[role="dialog"][aria-modal="true"]');
    await expect(modal).toBeVisible({ timeout: 5_000 });

    const modalTitle = modal.locator('h2');
    await expect(modalTitle).toContainText('New Booking');

    // Pre-filled time "09:00" must appear in modal body
    await expect(modal).toContainText('09:00');
  });

  test('Create Form modal can be dismissed', async ({ page }) => {
    await gotoDayViewWithMocks(page, { bookings: [], locations: [LOCATION_1] });

    // Open modal
    await page.locator('.hourly-slot-grid__row[data-time="10:00"]').click();
    await page.waitForSelector('[role="dialog"]', { timeout: 5_000 });

    // Dismiss with Cancel button
    await page.locator('[role="dialog"] button:has-text("Cancel")').click();

    // Modal must close
    const dialogCount = await page.locator('[role="dialog"]').count();
    expect(dialogCount).toBe(0);
  });
});

test.describe('Day View — event click opens Detail Modal', () => {
  /**
   * Test 5: Clicking an existing booking event opens Booking Detail Modal.
   *
   * Modal title must match booking.title.
   * Modal shows date and time.
   */
  test('clicking booking event opens Booking Detail Modal with booking title', async ({
    page,
  }) => {
    await gotoDayViewWithMocks(page, {
      bookings: [BOOKING_NO_CONFLICT],
      locations: [LOCATION_1],
    });

    // Wait for booking event to render
    const event = page.locator('[data-testid="day-view-event"]').first();
    await expect(event).toBeVisible({ timeout: 10_000 });

    // Click the booking event
    await event.click();

    // Booking Detail Modal must open
    const modal = page.locator('[role="dialog"][aria-modal="true"]');
    await expect(modal).toBeVisible({ timeout: 5_000 });

    // Modal title must match booking.title
    const modalTitle = modal.locator('h2');
    await expect(modalTitle).toContainText('Team Standup');

    // Modal must show start time
    await expect(modal).toContainText('09:00');
  });

  test('Booking Detail Modal can be dismissed via close button', async ({ page }) => {
    await gotoDayViewWithMocks(page, {
      bookings: [BOOKING_NO_CONFLICT],
      locations: [LOCATION_1],
    });

    // Open modal
    await page.locator('[data-testid="day-view-event"]').first().click();
    await page.waitForSelector('[role="dialog"]', { timeout: 5_000 });

    // Close via × button
    await page.locator('button[aria-label="Close dialog"]').click();

    // Modal must close
    const dialogCount = await page.locator('[role="dialog"]').count();
    expect(dialogCount).toBe(0);
  });
});

test.describe('Day View — location filter', () => {
  /**
   * Test 6: Location filter dropdown changes displayed bookings.
   *
   * With "All Locations": both bookings visible.
   * After filtering to LOCATION_2 only: API is called with location_id=loc-002
   * and returns only BOOKING_LOCATION_2 (Board Review).
   */
  test('location filter dropdown exists and shows All Locations by default', async ({ page }) => {
    await gotoDayViewWithMocks(page, {
      bookings: [BOOKING_NO_CONFLICT, BOOKING_LOCATION_2],
      locations: [LOCATION_1, LOCATION_2],
    });

    const locationSelect = page.locator('[data-testid="day-view-location-select"]');
    await expect(locationSelect).toBeVisible();

    // Default value must be "all"
    await expect(locationSelect).toHaveValue('all');

    // Location options: "All Locations" + 2 locations = 3 options
    const options = locationSelect.locator('option');
    await expect(options).toHaveCount(3);
    await expect(options.nth(1)).toContainText('Main Hall');
    await expect(options.nth(2)).toContainText('Boardroom');
  });

  test('selecting a location filters the booking display', async ({ page }) => {
    // Track which API call URLs are made
    const bookingApiUrls: string[] = [];

    await gotoDayViewWithMocks(page, {
      bookings: [BOOKING_NO_CONFLICT, BOOKING_LOCATION_2],
      locations: [LOCATION_1, LOCATION_2],
      bookingsHandler: (url) => {
        bookingApiUrls.push(url);
        // When filtering by LOCATION_2, return only BOOKING_LOCATION_2
        if (url.includes('location_id=loc-002')) {
          return [BOOKING_LOCATION_2];
        }
        return [BOOKING_NO_CONFLICT, BOOKING_LOCATION_2];
      },
    });

    // Both bookings initially visible (All Locations)
    await page.waitForSelector('[data-testid="day-view-event"]', { timeout: 10_000 });
    const initialEventCount = await page.locator('[data-testid="day-view-event"]').count();
    expect(initialEventCount).toBe(2);

    // Change location filter to LOCATION_2 (Boardroom)
    const locationSelect = page.locator('[data-testid="day-view-location-select"]');
    await locationSelect.selectOption('loc-002');

    // Wait for React Query to refetch with the new location_id param
    await page.waitForTimeout(600);

    // Verify the API was called with location_id=loc-002
    const filteredCalls = bookingApiUrls.filter((url) =>
      url.includes('location_id=loc-002'),
    );
    expect(filteredCalls.length).toBeGreaterThan(0);

    // Now only BOOKING_LOCATION_2 (Board Review) should be visible
    await page.waitForTimeout(300);
    await expect(page.locator('[data-testid="day-view-event"]')).toHaveCount(1);
    await expect(
      page.locator('.day-view-event__title').first(),
    ).toContainText('Board Review');
  });
});
