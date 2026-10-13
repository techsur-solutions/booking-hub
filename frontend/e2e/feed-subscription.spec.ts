import { test, expect } from '@playwright/test';

/**
 * Feed Subscription E2E Tests
 *
 * Tests run against a running dev server (VITE_E2E_MODE=true).
 * API calls to /locations are intercepted to populate the location filter dropdown.
 * Clipboard API is mocked via page.addInitScript() since Playwright runs Chromium
 * with clipboard permissions not always available.
 */

// ── Fixtures ──────────────────────────────────────────────────────────────────

const LOCATIONS = [
  { id: 'loc-1', name: 'Conference Room A', colour: '#3b82f6', building: null, layouts: null, deleted_at: null },
  { id: 'loc-2', name: 'Room B', colour: '#10b981', building: null, layouts: null, deleted_at: null },
];

// Helper: set up route mocks for the Feed Subscription page
async function setupFeedsPageMocks(page: import('@playwright/test').Page) {
  // Mock /locations API to populate the dropdown.
  // Pattern must be specific enough to avoid intercepting Vite JS module requests
  // that contain "locations" in the path (e.g. /@fs/.../locations.ts).
  await page.route(/\/locations(\?.*)?$/, (route) => {
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(LOCATIONS),
    });
  });
}

// Helper: inject a clipboard mock that captures what was written
async function injectClipboardMock(page: import('@playwright/test').Page) {
  await page.addInitScript(() => {
    const written: string[] = [];
    (window as unknown as Record<string, unknown>).__clipboardWritten__ = written;
    Object.defineProperty(navigator, 'clipboard', {
      value: {
        writeText: async (text: string) => {
          written.push(text);
        },
      },
      configurable: true,
    });
  });
}

// Helper: navigate to /feeds with roles (it's inside AppShell but public)
async function gotoFeedsPage(page: import('@playwright/test').Page) {
  await page.addInitScript((roles: string[]) => {
    (window as unknown as Record<string, unknown>).__E2E_ROLES__ = roles;
  }, ['role_calendar_viewer']);
  await page.goto('/feeds');
  await page.waitForSelector('[data-testid="feed-subscription"]', { timeout: 10_000 });
}

// ── Tests ─────────────────────────────────────────────────────────────────────

test.describe('Feed Subscription page', () => {
  test('renders three feed URL sections (RSS2, iCal, JSON)', async ({ page }) => {
    await setupFeedsPageMocks(page);
    await gotoFeedsPage(page);

    await expect(page.locator('[data-testid="rss2-feed"]')).toBeVisible();
    await expect(page.locator('[data-testid="ical-feed"]')).toBeVisible();
    await expect(page.locator('[data-testid="json-feed"]')).toBeVisible();

    await expect(page.locator('[data-testid="rss2-feed"]')).toContainText('RSS2 Feed');
    await expect(page.locator('[data-testid="ical-feed"]')).toContainText('iCal Feed');
    await expect(page.locator('[data-testid="json-feed"]')).toContainText('JSON Feed');
  });

  test('"No authentication required" banner is visible', async ({ page }) => {
    await setupFeedsPageMocks(page);
    await gotoFeedsPage(page);

    const banner = page.locator('[data-testid="no-auth-banner"]');
    await expect(banner).toBeVisible();
    await expect(banner).toContainText('No authentication required');
  });

  test('RSS2 feed URL contains /feeds/rss2', async ({ page }) => {
    await setupFeedsPageMocks(page);
    await gotoFeedsPage(page);

    const rss2Url = page.locator('[data-testid="rss2-feed-url"]');
    await expect(rss2Url).toBeVisible();
    await expect(rss2Url).toContainText('/feeds/rss2');
  });

  test('iCal feed URL contains /feeds/ical', async ({ page }) => {
    await setupFeedsPageMocks(page);
    await gotoFeedsPage(page);

    const icalUrl = page.locator('[data-testid="ical-feed-url"]');
    await expect(icalUrl).toBeVisible();
    await expect(icalUrl).toContainText('/feeds/ical');
  });

  test('JSON feed URL contains /feeds/json', async ({ page }) => {
    await setupFeedsPageMocks(page);
    await gotoFeedsPage(page);

    const jsonUrl = page.locator('[data-testid="json-feed-url"]');
    await expect(jsonUrl).toBeVisible();
    await expect(jsonUrl).toContainText('/feeds/json');
  });

  test('copy button for RSS2 feed copies URL to clipboard', async ({ page }) => {
    await setupFeedsPageMocks(page);
    await injectClipboardMock(page);
    await gotoFeedsPage(page);

    // Get the displayed URL text first
    const rss2UrlEl = page.locator('[data-testid="rss2-feed-url"]');
    const expectedUrl = await rss2UrlEl.textContent();

    // Click copy button
    await page.locator('[data-testid="rss2-feed-copy"]').click();

    // Verify "Copied!" feedback shown
    await expect(page.locator('[data-testid="rss2-feed-copy"]')).toContainText('Copied!');

    // Verify clipboard received the URL
    const clipboardWritten = await page.evaluate(() => {
      return (window as unknown as Record<string, unknown>).__clipboardWritten__ as string[];
    });
    expect(clipboardWritten.length).toBeGreaterThan(0);
    expect(clipboardWritten[0]).toBe(expectedUrl?.trim());
  });

  test('location filter dropdown is visible and populated', async ({ page }) => {
    await setupFeedsPageMocks(page);
    await gotoFeedsPage(page);

    const dropdown = page.locator('[data-testid="location-filter"]');
    await expect(dropdown).toBeVisible();

    // Check "All locations" option exists
    await expect(dropdown.locator('option:has-text("All locations")')).toHaveCount(1);
    // Check location options appear
    await expect(dropdown.locator('option:has-text("Conference Room A")')).toHaveCount(1);
    await expect(dropdown.locator('option:has-text("Room B")')).toHaveCount(1);
  });

  test('selecting location updates all three feed URLs with ?location_id=X', async ({
    page,
  }) => {
    await setupFeedsPageMocks(page);
    await gotoFeedsPage(page);

    // Select a specific location
    await page.locator('[data-testid="location-filter"]').selectOption('loc-1');

    // All three URLs should include location_id=loc-1
    await expect(page.locator('[data-testid="rss2-feed-url"]')).toContainText(
      'location_id=loc-1',
    );
    await expect(page.locator('[data-testid="ical-feed-url"]')).toContainText(
      'location_id=loc-1',
    );
    await expect(page.locator('[data-testid="json-feed-url"]')).toContainText(
      'location_id=loc-1',
    );
  });

  test('deselecting location removes ?location_id from URLs', async ({ page }) => {
    await setupFeedsPageMocks(page);
    await gotoFeedsPage(page);

    // Select then deselect
    await page.locator('[data-testid="location-filter"]').selectOption('loc-1');
    await page.locator('[data-testid="location-filter"]').selectOption('');

    // URLs should not have location_id param
    const rss2UrlText = await page.locator('[data-testid="rss2-feed-url"]').textContent();
    expect(rss2UrlText).not.toContain('location_id=');
  });

  test('preview button opens feed URL in new tab', async ({ page }) => {
    await setupFeedsPageMocks(page);
    await gotoFeedsPage(page);

    // Mock window.open
    await page.addInitScript(() => {
      const opened: string[] = [];
      (window as unknown as Record<string, unknown>).__openedWindows__ = opened;
      (window as unknown as Record<string, unknown>).__origOpen__ = window.open;
      window.open = (url?: string | URL) => {
        opened.push(String(url));
        return null;
      };
    });

    // Reload with the mock in place
    await page.reload();
    await page.waitForSelector('[data-testid="feed-subscription"]');

    await page.locator('[data-testid="rss2-feed-preview"]').click();

    const openedWindows = await page.evaluate(() => {
      return (window as unknown as Record<string, unknown>).__openedWindows__ as string[];
    });
    expect(openedWindows.length).toBeGreaterThan(0);
    expect(openedWindows[0]).toContain('/feeds/rss2');
  });
});
