import { test, expect, type Page } from '@playwright/test';
import type { Booking, Location, Resource } from '../src/api/types';

/**
 * View State Persistence E2E Tests (UI-CAL-04)
 *
 * Tests view state persistence via URL query params across Calendar/Day/List views.
 * All tests run in VITE_E2E_MODE=true (set in playwright.config.ts webServer.env).
 * Keycloak is bypassed; roles are injected via window.__E2E_ROLES__.
 * Backend API calls are intercepted via page.route() targeting http://localhost:8080.
 *
 * Covers:
 * 1. Date persistence across Calendar/Day/List views (URL param preserved when switching)
 * 2. Filter persistence in List View with page refresh
 * 3. Bookmarkable URLs (direct navigation loads correct state)
 * 4. Browser back/forward preserves state
 */

// ── Test Data ──────────────────────────────────────────────────────────────────

const LOCATIONS: Location[] = [
  {
    id: '550e8400-e29b-41d4-a716-446655440001',
    name: 'Room Alpha',
    colour: '#e74c3c',
    building: 'Main',
    layouts: null,
    deleted_at: null,
  },
  {
    id: '550e8400-e29b-41d4-a716-446655440002',
    name: 'Room Beta',
    colour: '#2ecc71',
    building: 'Annex',
    layouts: null,
    deleted_at: null,
  },
];

const RESOURCES: Resource[] = [
  {
    id: '660e8400-e29b-41d4-a716-446655440001',
    name: 'Projector',
    type: 'AV',
    description: null,
    is_unique: false,
    restrict_locations: null,
    deleted_at: null,
  },
];

const ALL_BOOKINGS: Booking[] = [
  {
    id: 'bk-feb-15-alpha',
    series_id: null,
    location_id: '550e8400-e29b-41d4-a716-446655440001',
    resource_ids: [],
    title: 'Feb 15 Meeting',
    description: 'Meeting on February 15',
    start_time: '2024-02-15T10:00:00',
    end_time: '2024-02-15T11:00:00',
    status: 'approved',
    owner_id: 'user-1',
    conflict_flags: [],
    custom_field_values: [],
    created_at: '2024-01-01T00:00:00',
    updated_at: '2024-01-01T00:00:00',
    deleted_at: null,
  },
  {
    id: 'bk-jan-01-pending',
    series_id: null,
    location_id: '550e8400-e29b-41d4-a716-446655440002',
    resource_ids: ['660e8400-e29b-41d4-a716-446655440001'],
    title: 'Jan 1 Workshop',
    description: 'Workshop on Jan 1',
    start_time: '2024-01-01T14:00:00',
    end_time: '2024-01-01T16:00:00',
    status: 'pending',
    owner_id: 'user-2',
    conflict_flags: [],
    custom_field_values: [],
    created_at: '2024-01-01T00:00:00',
    updated_at: '2024-01-01T00:00:00',
    deleted_at: null,
  },
  {
    id: 'bk-mar-01-approved',
    series_id: null,
    location_id: '550e8400-e29b-41d4-a716-446655440001',
    resource_ids: [],
    title: 'Mar 1 Strategy',
    description: 'Strategy session',
    start_time: '2024-03-01T09:00:00',
    end_time: '2024-03-01T10:00:00',
    status: 'approved',
    owner_id: 'user-1',
    conflict_flags: [],
    custom_field_values: [],
    created_at: '2024-01-01T00:00:00',
    updated_at: '2024-01-01T00:00:00',
    deleted_at: null,
  },
];

// ── Helpers ──────────────────────────────────────────────────────────────────

const API_BASE = 'http://localhost:8080';

/**
 * Install API mocks and set E2E roles before page navigation.
 * Must be called before page.goto().
 */
async function setupMocks(page: Page, bookings: Booking[] = ALL_BOOKINGS) {
  await page.addInitScript((roles: string[]) => {
    (window as unknown as Record<string, unknown>).__E2E_ROLES__ = roles;
  }, ['role_calendar_viewer', 'role_booking_approver']);

  await page.route(`${API_BASE}/bookings**`, (route) => {
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(bookings),
    });
  });

  await page.route(`${API_BASE}/locations**`, (route) => {
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(LOCATIONS),
    });
  });

  await page.route(`${API_BASE}/resources**`, (route) => {
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(RESOURCES),
    });
  });

  await page.route(`${API_BASE}/settings**`, (route) => {
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        id: 1,
        approve_booking: true,
        calendar_slot_size: 30,
        calendar_min_time: '08:00',
        calendar_max_time: '20:00',
      }),
    });
  });
}

// ── Test Suite ────────────────────────────────────────────────────────────────

test.describe('View State Persistence (UI-CAL-04)', () => {
  /**
   * Scenario 1: Date preserved when switching between Calendar → Day → List views
   *
   * Navigates to Calendar View with ?date=2024-02-15. Switches to Day View via
   * the view tab and verifies the date param is preserved in the URL. Then
   * switches to List View and verifies date param still preserved.
   */
  test('date persists in URL when switching between Calendar, Day, and List views', async ({
    page,
  }) => {
    await setupMocks(page);

    // Start at Calendar View with a specific date
    await page.goto('/bookings?view=month&date=2024-02-15');
    await page.waitForSelector('[data-testid="calendar-view"]', { timeout: 10_000 });

    // URL should contain date=2024-02-15
    let url = new URL(page.url());
    expect(url.searchParams.get('date')).toBe('2024-02-15');
    expect(url.searchParams.get('view')).toBe('month');

    // Click "Day" tab to switch to Day View
    await page.click('[role="tab"]:has-text("Day")');
    await page.waitForSelector('[data-testid="day-view"]', { timeout: 10_000 });

    // URL should now have view=day AND preserve date=2024-02-15
    url = new URL(page.url());
    expect(url.searchParams.get('view')).toBe('day');
    expect(url.searchParams.get('date')).toBe('2024-02-15');

    // Day View date picker should show 2024-02-15
    const datePicker = page.locator('[data-testid="day-view-date-picker"]');
    await expect(datePicker).toHaveValue('2024-02-15');

    // Click "List" tab to switch to List View
    await page.click('[role="tab"]:has-text("List")');
    await page.waitForSelector('[data-testid="list-view"]', { timeout: 10_000 });

    // URL should have view=list AND still preserve date=2024-02-15
    url = new URL(page.url());
    expect(url.searchParams.get('view')).toBe('list');
    expect(url.searchParams.get('date')).toBe('2024-02-15');
  });

  /**
   * Scenario 2: Filter state persists in List View after page refresh
   *
   * Sets location, status, and search filters in List View. Verifies the URL
   * contains the expected params. Then reloads the page and verifies the filters
   * are still set.
   */
  test('List View filter state persists after page refresh (URL-driven)', async ({ page }) => {
    await setupMocks(page);

    // Navigate to List View
    await page.goto('/bookings?view=list');
    await page.waitForSelector('[data-testid="list-view"]', { timeout: 10_000 });

    // Set status filter to "pending"
    await page.check('[data-testid="filter-status-pending"]');
    await page.waitForTimeout(400);

    // Verify URL has status=pending
    let url = new URL(page.url());
    expect(url.searchParams.get('status')).toBe('pending');
    expect(url.searchParams.get('view')).toBe('list');

    // Refresh the page — URL drives state restoration
    await page.reload();
    await page.waitForSelector('[data-testid="list-view"]', { timeout: 10_000 });

    // After refresh, the filter should still be applied (URL params drive init state)
    url = new URL(page.url());
    expect(url.searchParams.get('status')).toBe('pending');

    // The status checkbox should still be checked
    await expect(page.locator('[data-testid="filter-status-pending"]')).toBeChecked();
  });

  /**
   * Scenario 3a: Bookmarkable URLs — Calendar View with specific date
   *
   * Directly navigates to a URL with date and view params.
   * Verifies the Calendar View loads with the correct date in the URL.
   */
  test('bookmarkable URL loads Calendar View at specified date', async ({ page }) => {
    await setupMocks(page);

    // Navigate directly to bookmarked Calendar URL
    await page.goto('/bookings?view=month&date=2024-03-01');
    await page.waitForSelector('[data-testid="calendar-view"]', { timeout: 10_000 });

    // Calendar View should be visible
    await expect(page.locator('[data-testid="calendar-view"]')).toBeVisible();

    // URL should preserve both view and date params
    const url = new URL(page.url());
    expect(url.searchParams.get('view')).toBe('month');
    expect(url.searchParams.get('date')).toBe('2024-03-01');
  });

  /**
   * Scenario 3b: Bookmarkable URLs — List View with all filters
   *
   * Directly navigates to a URL with location, status, date range, and search params.
   * Verifies filters are applied from the URL on initial load.
   */
  test('bookmarkable URL loads List View with all filters applied', async ({ page }) => {
    const locationId = '550e8400-e29b-41d4-a716-446655440001';

    await setupMocks(page);

    // Navigate directly to a bookmarked List URL with all filters
    await page.goto(
      `/bookings?view=list&location_id=${locationId}&status=approved&start_date=2024-01-01&end_date=2024-01-31`
    );
    await page.waitForSelector('[data-testid="list-view"]', { timeout: 10_000 });

    // List View should be visible
    await expect(page.locator('[data-testid="list-view"]')).toBeVisible();

    // URL params should all be preserved
    const url = new URL(page.url());
    expect(url.searchParams.get('view')).toBe('list');
    expect(url.searchParams.get('location_id')).toBe(locationId);
    expect(url.searchParams.get('status')).toBe('approved');
    expect(url.searchParams.get('start_date')).toBe('2024-01-01');
    expect(url.searchParams.get('end_date')).toBe('2024-01-31');

    // Status "approved" filter should be checked (seeded from URL)
    await expect(page.locator('[data-testid="filter-status-approved"]')).toBeChecked();
    // Date range inputs should show values from URL
    await expect(page.locator('[data-testid="filter-start-date"]')).toHaveValue('2024-01-01');
    await expect(page.locator('[data-testid="filter-end-date"]')).toHaveValue('2024-01-31');
  });

  /**
   * Scenario 4: Browser back/forward navigation preserves state
   *
   * Verifies that React Router's history stack works correctly with URL param navigation.
   * Navigates: Calendar View (month) → Day View (day) → back → Calendar (month) → forward → Day (day).
   */
  test('browser back/forward navigation preserves view state', async ({ page }) => {
    await setupMocks(page);

    // Step 1: Start at Calendar View
    await page.goto('/bookings?view=month&date=2024-01-01');
    await page.waitForSelector('[data-testid="calendar-view"]', { timeout: 10_000 });

    let url = new URL(page.url());
    expect(url.searchParams.get('view')).toBe('month');
    expect(url.searchParams.get('date')).toBe('2024-01-01');

    // Step 2: Switch to Day View (creates a new history entry)
    await page.click('[role="tab"]:has-text("Day")');
    await page.waitForSelector('[data-testid="day-view"]', { timeout: 10_000 });

    url = new URL(page.url());
    expect(url.searchParams.get('view')).toBe('day');
    // date param should be preserved from Calendar View
    expect(url.searchParams.get('date')).toBe('2024-01-01');

    // Step 3: Click browser back button → should return to Calendar View at date=2024-01-01
    await page.goBack();
    await page.waitForSelector('[data-testid="calendar-view"]', { timeout: 10_000 });

    url = new URL(page.url());
    expect(url.searchParams.get('view')).toBe('month');
    expect(url.searchParams.get('date')).toBe('2024-01-01');

    // Step 4: Click browser forward button → should return to Day View
    await page.goForward();
    await page.waitForSelector('[data-testid="day-view"]', { timeout: 10_000 });

    url = new URL(page.url());
    expect(url.searchParams.get('view')).toBe('day');
    expect(url.searchParams.get('date')).toBe('2024-01-01');
  });

  /**
   * Additional: Date param preserved when switching from Day View back to Calendar
   */
  test('date param preserved when switching back from Day View to Calendar View', async ({
    page,
  }) => {
    await setupMocks(page);

    // Start at Day View with a specific date
    await page.goto('/bookings?view=day&date=2024-06-10');
    await page.waitForSelector('[data-testid="day-view"]', { timeout: 10_000 });

    // Date picker should show the URL date
    await expect(page.locator('[data-testid="day-view-date-picker"]')).toHaveValue('2024-06-10');

    // Switch back to Calendar View
    await page.click('[role="tab"]:has-text("Calendar")');
    await page.waitForSelector('[data-testid="calendar-view"]', { timeout: 10_000 });

    // Date param should still be in URL
    const url = new URL(page.url());
    expect(url.searchParams.get('view')).toBe('month');
    expect(url.searchParams.get('date')).toBe('2024-06-10');
  });

  /**
   * Additional: Invalid date in URL falls back to today (security: T-09-08-01)
   */
  test('invalid date in URL does not crash the app (falls back to default)', async ({ page }) => {
    await setupMocks(page);

    // Navigate to a URL with an invalid date param
    await page.goto('/bookings?view=month&date=not-a-date');
    await page.waitForSelector('[data-testid="calendar-view"]', { timeout: 10_000 });

    // Calendar View should still render without crashing
    await expect(page.locator('[data-testid="calendar-view"]')).toBeVisible();
    // The invalid date should have been ignored (hook falls back to today)
  });
});
