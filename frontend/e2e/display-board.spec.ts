import { test, expect } from '@playwright/test';

/**
 * Display Board E2E Tests
 *
 * Tests run against a running dev server (VITE_E2E_MODE=true, no real Keycloak).
 * API calls to /feeds/display-board are intercepted via page.route() to control
 * what bookings the Display Board renders.
 */

// ── Fixtures ──────────────────────────────────────────────────────────────────

const APPROVED_BOOKING = {
  id: 'booking-approved-1',
  title: 'Board Meeting',
  location_id: 'loc-1',
  location_name: 'Conference Room A',
  start_time: new Date(Date.now() + 60 * 60 * 1000).toISOString(), // 1 hour from now
  end_time: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString(), // 2 hours from now
  status: 'approved',
};

const APPROVED_BOOKING_LOC2 = {
  id: 'booking-approved-2',
  title: 'Design Sprint',
  location_id: 'loc-2',
  location_name: 'Room B',
  start_time: new Date(Date.now() + 3 * 60 * 60 * 1000).toISOString(),
  end_time: new Date(Date.now() + 4 * 60 * 60 * 1000).toISOString(),
  status: 'approved',
};

// Helper: mock the /feeds/display-board API endpoint
async function mockDisplayBoardApi(
  page: import('@playwright/test').Page,
  bookings: unknown[],
) {
  await page.route('**/feeds/display-board**', (route) => {
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(bookings),
    });
  });
}

// Helper: navigate to display board (no auth required — no role injection needed)
async function gotoDisplayBoard(
  page: import('@playwright/test').Page,
  params?: string,
) {
  const url = params ? `/display-board?${params}` : '/display-board';
  await page.goto(url);
  // Wait for the display board root element
  await page.waitForSelector('[data-testid="display-board"]', { timeout: 10_000 });
}

// ── Tests ─────────────────────────────────────────────────────────────────────

test.describe('Display Board', () => {
  test('renders without AppShell (no sidebar, no header)', async ({ page }) => {
    await mockDisplayBoardApi(page, [APPROVED_BOOKING]);
    await gotoDisplayBoard(page);

    // No sidebar
    await expect(page.locator('aside.sidebar')).toHaveCount(0);
    // No header
    await expect(page.locator('header.header')).toHaveCount(0);
    // The display board itself is present
    await expect(page.locator('[data-testid="display-board"]')).toBeVisible();
  });

  test('h1 heading is present', async ({ page }) => {
    await mockDisplayBoardApi(page, []);
    await gotoDisplayBoard(page);
    await expect(page.locator('h1')).toContainText('Booking Display Board');
  });

  test('ApprovedOnlyBadge is visible in top-right corner', async ({ page }) => {
    await mockDisplayBoardApi(page, [APPROVED_BOOKING]);
    await gotoDisplayBoard(page);

    const badge = page.locator('[data-testid="approved-only-badge"]');
    await expect(badge).toBeVisible();
    await expect(badge).toContainText('Approved Only');
  });

  test('renders approved bookings', async ({ page }) => {
    await mockDisplayBoardApi(page, [APPROVED_BOOKING]);
    await gotoDisplayBoard(page);

    // Booking card should be visible
    await expect(page.locator('[data-testid="booking-card"]')).toBeVisible();
    await expect(page.locator('[data-testid="booking-card"]')).toContainText('Board Meeting');
    await expect(page.locator('[data-testid="booking-card"]')).toContainText('Conference Room A');
  });

  test('shows empty state when no bookings', async ({ page }) => {
    await mockDisplayBoardApi(page, []);
    await gotoDisplayBoard(page);

    await expect(page.locator('[data-testid="booking-card"]')).toHaveCount(0);
    // Should show empty message
    await expect(page.locator('text=No approved bookings')).toBeVisible();
  });

  test('pending and denied bookings are absent (API-level filtering enforced)', async ({ page }) => {
    // The display board only calls /feeds/display-board which returns approved-only.
    // Mock returns only approved bookings (as the backend enforces).
    await mockDisplayBoardApi(page, [APPROVED_BOOKING]);
    await gotoDisplayBoard(page);

    // Only the one approved booking card should appear
    await expect(page.locator('[data-testid="booking-card"]')).toHaveCount(1);
    await expect(page.locator('[data-testid="booking-card"]').first()).toContainText('Board Meeting');
  });

  test('location filter (?location_id=X) passes param to API', async ({ page }) => {
    const capturedUrls: string[] = [];

    await page.route('**/feeds/display-board**', (route) => {
      capturedUrls.push(route.request().url());
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([APPROVED_BOOKING]),
      });
    });

    await gotoDisplayBoard(page, 'location_id=loc-1');
    await page.waitForSelector('[data-testid="booking-card"]');

    // Verify the API was called with location_id param
    expect(capturedUrls.some((url) => url.includes('location_id=loc-1'))).toBe(true);
  });

  test('footer link to /feeds page is present', async ({ page }) => {
    await mockDisplayBoardApi(page, []);
    await gotoDisplayBoard(page);

    const feedsLink = page.locator('a[href="/feeds"]');
    await expect(feedsLink).toBeVisible();
    await expect(feedsLink).toContainText('View Feed Subscription');
  });
});
