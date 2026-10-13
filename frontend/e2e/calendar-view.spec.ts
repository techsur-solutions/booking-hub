import { test, expect } from '@playwright/test';

/**
 * Calendar View Tests
 *
 * Tests run with VITE_E2E_MODE=true (playwright.config.ts webServer.env).
 * Roles injected via page.addInitScript(); API responses mocked via page.route().
 * API base URL is http://localhost:8080 (VITE_API_BASE_URL default).
 *
 * Route mocking strategy:
 *   - Mock all requests to localhost:8080 so API calls return predictable data
 *   - Use a catch-all handler then specific per-path responses
 */

// ── Fixture data ──────────────────────────────────────────────────────────────

const LOCATION_1 = {
  id: 'loc-001',
  name: 'Main Hall',
  colour: '#3b82f6',
  building: 'Building A',
  layouts: null,
  deleted_at: null,
};

const BOOKING_NO_CONFLICT = {
  id: 'bk-001',
  series_id: null,
  location_id: 'loc-001',
  resource_ids: [],
  title: 'Team Standup',
  description: null,
  // Use a fixed date so the booking always appears in the current month
  start_time: new Date(new Date().getFullYear(), new Date().getMonth(), 15, 10, 0).toISOString(),
  end_time: new Date(new Date().getFullYear(), new Date().getMonth(), 15, 11, 0).toISOString(),
  status: 'approved',
  owner_id: 'user-001',
  conflict_flags: [],
  custom_field_values: [],
  created_at: new Date().toISOString(),
  updated_at: new Date().toISOString(),
  deleted_at: null,
};

const BOOKING_WITH_CONFLICT = {
  id: 'bk-002',
  series_id: null,
  location_id: 'loc-001',
  resource_ids: [],
  title: 'All-Hands Meeting',
  description: null,
  start_time: new Date(new Date().getFullYear(), new Date().getMonth(), 15, 10, 0).toISOString(),
  end_time: new Date(new Date().getFullYear(), new Date().getMonth(), 15, 11, 0).toISOString(),
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

// ── Helpers ──────────────────────────────────────────────────────────────────

/**
 * Navigate to the calendar with roles, mocked bookings, and mocked locations.
 *
 * Mocks ALL requests to http://localhost:8080 (the API gateway):
 *   - GET /bookings → returns bookings fixture
 *   - GET /locations → returns locations fixture
 *   - anything else → 404
 */
async function gotoCalendarWithMocks(
  page: import('@playwright/test').Page,
  options: {
    bookings?: unknown[];
    locations?: unknown[];
    roles?: string[];
  } = {},
) {
  const { bookings = [], locations = [LOCATION_1], roles = ['role_calendar_viewer'] } = options;

  // Inject Keycloak roles before page load (must be before goto())
  await page.addInitScript((injectedRoles: string[]) => {
    (window as unknown as Record<string, unknown>).__E2E_ROLES__ = injectedRoles;
  }, roles);

  // Intercept ALL requests to the API gateway (localhost:8080)
  // Use glob pattern to catch all paths
  await page.route('*://localhost:8080/**', (route) => {
    const url = new URL(route.request().url());
    const path = url.pathname;

    if (path === '/bookings' || path.startsWith('/bookings?')) {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(bookings),
      });
    } else if (path === '/locations' || path.startsWith('/locations?')) {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(locations),
      });
    } else {
      route.fulfill({
        status: 404,
        contentType: 'application/json',
        body: JSON.stringify({ error: 'Not found' }),
      });
    }
  });

  await page.goto('/bookings?view=month');

  // Wait for FullCalendar root to render — up to 15s for React + API to settle
  await page.waitForSelector('.fc', { timeout: 15_000 });
}

// ── Tests ─────────────────────────────────────────────────────────────────────

test.describe('Calendar view rendering', () => {
  test('FullCalendar grid renders in month view', async ({ page }) => {
    await gotoCalendarWithMocks(page, { bookings: [BOOKING_NO_CONFLICT] });

    // FullCalendar root element must be present
    await expect(page.locator('.fc')).toBeVisible();

    // Month grid must be present
    await expect(page.locator('.fc-daygrid')).toBeVisible();
  });

  test('displays heading for screen readers', async ({ page }) => {
    await gotoCalendarWithMocks(page);

    // sr-only h1 must be present for accessibility
    const heading = page.locator('h1');
    await expect(heading).toContainText('Calendar View');
  });

  test('view switcher tabs are rendered', async ({ page }) => {
    await gotoCalendarWithMocks(page);

    await expect(page.locator('[role="tab"]:has-text("Calendar")')).toBeVisible();
    await expect(page.locator('[role="tab"]:has-text("Day")')).toBeVisible();
    await expect(page.locator('[role="tab"]:has-text("List")')).toBeVisible();
  });

  test('header toolbar shows navigation controls', async ({ page }) => {
    await gotoCalendarWithMocks(page);

    // FullCalendar header toolbar must be present
    await expect(page.locator('.fc-header-toolbar')).toBeVisible();
  });

  test('booking event visible on calendar', async ({ page }) => {
    await gotoCalendarWithMocks(page, { bookings: [BOOKING_NO_CONFLICT] });

    // At least one event must render in the calendar
    await expect(page.locator('.fc-event').first()).toBeVisible({ timeout: 10_000 });
  });

  test('booking event shows booking title', async ({ page }) => {
    await gotoCalendarWithMocks(page, { bookings: [BOOKING_NO_CONFLICT] });

    // Event title must match the booking
    await expect(page.locator('.fc-event-title').first()).toContainText('Team Standup');
  });
});

test.describe('Conflict badge visibility', () => {
  test('conflict badge visible on booking with conflict_flags', async ({ page }) => {
    await gotoCalendarWithMocks(page, {
      bookings: [BOOKING_NO_CONFLICT, BOOKING_WITH_CONFLICT],
    });

    // ConflictBadge uses data-testid="conflict-badge"
    await expect(page.locator('[data-testid="conflict-badge"]')).toBeVisible({ timeout: 10_000 });
  });

  test('no conflict badge on booking without conflict_flags', async ({ page }) => {
    await gotoCalendarWithMocks(page, { bookings: [BOOKING_NO_CONFLICT] });

    // Only the non-conflicted booking is present, so no badge
    await page.waitForSelector('.fc-event', { timeout: 10_000 });
    const badgeCount = await page.locator('[data-testid="conflict-badge"]').count();
    expect(badgeCount).toBe(0);
  });
});

test.describe('Calendar click interactions', () => {
  test('clicking booking event fires event click handler', async ({ page }) => {
    await gotoCalendarWithMocks(page, { bookings: [BOOKING_NO_CONFLICT] });

    // Wait for event to be visible
    const event = page.locator('.fc-event').first();
    await expect(event).toBeVisible({ timeout: 10_000 });

    // Click the event — should not throw (modal integration in later plan)
    await event.click();

    // App remains stable after click (no crash/error boundary)
    await expect(page.locator('.fc')).toBeVisible();
  });

  test('clicking empty calendar date fires date click handler', async ({ page }) => {
    await gotoCalendarWithMocks(page, { bookings: [] });

    // Find a day cell — FullCalendar day cells in the main grid (not other months)
    const dayCell = page.locator('.fc-daygrid-day:not(.fc-day-other)').first();
    await expect(dayCell).toBeVisible({ timeout: 10_000 });

    // Click the day cell — should not throw (modal integration in later plan)
    await dayCell.click();

    // App remains stable after click
    await expect(page.locator('.fc')).toBeVisible();
  });
});
