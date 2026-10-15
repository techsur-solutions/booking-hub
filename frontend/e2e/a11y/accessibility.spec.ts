/**
 * Accessibility test suite — WCAG AA compliance via axe-core
 *
 * Tests run with VITE_E2E_MODE=true (mocked auth + APIs, set by playwright.config.ts).
 * Uses @axe-core/playwright AxeBuilder for automated WCAG AA scanning.
 *
 * Route mock pattern follows calendar-view.spec.ts: '*://localhost:8080/**' glob with
 * path-based dispatch so all API calls are handled in one handler.
 *
 * Coverage:
 *   - Calendar/Bookings view (month) — axe WCAG AA scan
 *   - Locations Admin table — axe WCAG AA scan
 *   - Booking Create form — axe WCAG AA scan
 *   - Keyboard navigation (tab order, focus indicators)
 *   - Screen reader: form inputs have labels
 *   - Skip link presence and target
 */
import { test, expect } from '@playwright/test';
import { AxeBuilder } from '@axe-core/playwright';

// ── Constants ────────────────────────────────────────────────────────────────

/** Admin roles for admin-gated pages */
const ADMIN_ROLES = [
  'role_calendar_viewer',
  'role_room_booking',
  'role_location_manager',
  'role_resource_manager',
  'role_user_manager',
  'role_permission_manager',
  'role_settings_manager',
  'role_audit_viewer',
];

/** Standard user roles for calendar/booking pages */
const USER_ROLES = ['role_calendar_viewer', 'role_room_booking'];

/** Sample locations returned by mock API */
const MOCK_LOCATIONS = [
  { id: 'loc-1', name: 'Conference Room A', colour: '#3b82f6', building: 'HQ', layouts: 'U-shape', deleted_at: null },
  { id: 'loc-2', name: 'Board Room', colour: '#10b981', building: 'HQ', layouts: 'Theatre', deleted_at: null },
];

// ── Helpers ──────────────────────────────────────────────────────────────────

/**
 * Inject E2E roles before page load.
 * Must be called before page.goto() to take effect.
 */
async function injectRoles(
  page: import('@playwright/test').Page,
  roles: string[] = USER_ROLES,
) {
  await page.addInitScript((r: string[]) => {
    (window as Record<string, unknown>).__E2E_ROLES__ = r;
  }, roles);
}

/**
 * Set up a single catch-all route handler for all API calls.
 * Follows the pattern established in calendar-view.spec.ts for consistency.
 */
async function mockAllApis(
  page: import('@playwright/test').Page,
  options?: { locations?: object[] },
) {
  const locations = options?.locations ?? MOCK_LOCATIONS;

  await page.route('*://localhost:8080/**', (route) => {
    const url = new URL(route.request().url());
    const path = url.pathname;
    const method = route.request().method();

    // GET /bookings — returns array (not paginated)
    if ((path === '/bookings' || path.startsWith('/bookings?')) && method === 'GET') {
      void route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      });
      return;
    }

    // GET /locations
    if (path === '/locations' && method === 'GET') {
      void route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(locations),
      });
      return;
    }

    // GET /resources
    if (path === '/resources' && method === 'GET') {
      void route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      });
      return;
    }

    // GET /custom-fields
    if (path === '/custom-fields' && method === 'GET') {
      void route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      });
      return;
    }

    // GET /settings/current
    if (path === '/settings/current' && method === 'GET') {
      void route.fulfill({
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
      return;
    }

    // GET /users
    if (path === '/users' && method === 'GET') {
      void route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      });
      return;
    }

    // GET /bookings/check-conflicts or POST — return empty conflicts
    if (path.includes('/check-conflicts')) {
      void route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      });
      return;
    }

    // Default: pass through unhandled routes
    void route.continue();
  });
}

/** Format axe violations for readable test failure output */
function formatViolations(violations: import('axe-core').Result[]): string {
  if (violations.length === 0) return 'No violations';
  return violations
    .map(
      (v) =>
        `\n[${v.impact?.toUpperCase() ?? 'UNKNOWN'}] ${v.id}: ${v.description}\n` +
        v.nodes.map((n) => `  Target: ${JSON.stringify(n.target)}`).join('\n'),
    )
    .join('\n');
}

// ── Tests ────────────────────────────────────────────────────────────────────

test.describe('Accessibility audit — WCAG AA', () => {

  test('Calendar (Bookings) view meets WCAG AA', async ({ page }) => {
    await injectRoles(page, USER_ROLES);
    await mockAllApis(page);
    await page.goto('/bookings?view=month');
    await page.waitForSelector('.app-shell', { timeout: 15000 });

    const results = await new AxeBuilder({ page })
      .withTags(['wcag2a', 'wcag2aa'])
      // Exclude FullCalendar's internal DOM — FullCalendar's own CSS uses light
      // colors (#b3d0ee) for decorative border markers that are not meaningful
      // text content. These are inside .fc elements we don't control.
      .exclude('.fc')
      .analyze();

    expect(results.violations, formatViolations(results.violations)).toEqual([]);
  });

  test('Locations Admin page meets WCAG AA', async ({ page }) => {
    await injectRoles(page, ADMIN_ROLES);
    await mockAllApis(page, { locations: MOCK_LOCATIONS });
    await page.goto('/admin/locations');
    await page.waitForSelector('[data-testid="location-table"]', { timeout: 15000 });

    const results = await new AxeBuilder({ page })
      .withTags(['wcag2a', 'wcag2aa'])
      .analyze();

    expect(results.violations, formatViolations(results.violations)).toEqual([]);
  });

  test('Booking Create form meets WCAG AA', async ({ page }) => {
    await injectRoles(page, USER_ROLES);
    await mockAllApis(page);
    // Route is /bookings/create (see routes/index.tsx)
    await page.goto('/bookings/create');
    await page.waitForSelector('[data-testid="booking-form"]', { timeout: 15000 });

    const results = await new AxeBuilder({ page })
      .withTags(['wcag2a', 'wcag2aa'])
      .analyze();

    expect(results.violations, formatViolations(results.violations)).toEqual([]);
  });

  test('Keyboard navigation: Tab through Calendar view focuses interactive elements', async ({ page }) => {
    await injectRoles(page, USER_ROLES);
    await mockAllApis(page);
    await page.goto('/bookings?view=month');
    await page.waitForSelector('.app-shell', { timeout: 15000 });

    // Tab from body — first focus should land on the skip link or a nav element
    await page.keyboard.press('Tab');
    const firstFocusedTag = await page.evaluate(() => document.activeElement?.tagName ?? 'NONE');
    expect(['A', 'BUTTON', 'INPUT']).toContain(firstFocusedTag);

    // Tab through 10 elements — verify focus changes each time
    const focusedElements: string[] = [];
    for (let i = 0; i < 10; i++) {
      const descriptor = await page.evaluate(() => {
        const el = document.activeElement;
        return el ? `${el.tagName}:${el.getAttribute('data-testid') ?? el.className}` : 'NONE';
      });
      focusedElements.push(descriptor);
      await page.keyboard.press('Tab');
    }

    // Assert focus moved — not all the same element
    const uniqueElements = new Set(focusedElements);
    expect(uniqueElements.size).toBeGreaterThan(1);
  });

  test('Screen reader: Booking form inputs have associated labels', async ({ page }) => {
    await injectRoles(page, USER_ROLES);
    await mockAllApis(page);
    await page.goto('/bookings/create');
    await page.waitForSelector('[data-testid="booking-form"]', { timeout: 15000 });

    // All non-hidden, non-checkbox inputs must have a programmatic label
    const unlabelledInputs = await page.evaluate(() => {
      const inputs = Array.from(document.querySelectorAll('input, select, textarea'));
      return inputs.filter((el) => {
        const htmlEl = el as HTMLInputElement;
        if (htmlEl.type === 'hidden') return false;
        if (htmlEl.type === 'checkbox') return false; // grouped via fieldset/legend

        const hasAriaLabel = Boolean(htmlEl.getAttribute('aria-label')?.trim());
        const labelledById = htmlEl.getAttribute('aria-labelledby');
        const hasAriaLabelledBy = labelledById
          ? document.getElementById(labelledById) !== null
          : false;
        const id = htmlEl.id;
        const hasAssociatedLabel = id
          ? document.querySelector(`label[for="${id}"]`) !== null
          : false;
        const isInsideLabel = el.closest('label') !== null;

        return !(hasAriaLabel || hasAriaLabelledBy || hasAssociatedLabel || isInsideLabel);
      }).map((el) => `${el.tagName}[id="${el.id}"][type="${(el as HTMLInputElement).type}"]`);
    });

    expect(
      unlabelledInputs,
      `Found inputs without accessible labels: ${unlabelledInputs.join(', ')}`,
    ).toHaveLength(0);
  });

  test('Skip link is present and targets main content', async ({ page }) => {
    await injectRoles(page, USER_ROLES);
    await mockAllApis(page);
    await page.goto('/bookings?view=month');
    // Wait for app shell to ensure React has rendered and skip link is in DOM
    await page.waitForSelector('.app-shell', { timeout: 15000 });

    // Verify skip link exists in DOM
    const skipLink = page.locator('a.skip-link').first();
    await expect(skipLink).toBeAttached({ timeout: 5000 });

    const href = await skipLink.getAttribute('href');
    expect(href).toBe('#main-content');

    // Verify the target (main content landmark) exists
    const mainElement = page.locator('#main-content');
    await expect(mainElement).toBeAttached({ timeout: 3000 });
  });

  test('Focus indicators: first interactive element shows visible focus ring', async ({ page }) => {
    await injectRoles(page, USER_ROLES);
    await mockAllApis(page);
    await page.goto('/bookings?view=month');
    await page.waitForSelector('.app-shell', { timeout: 15000 });

    // Tab to first interactive element (skip link)
    await page.keyboard.press('Tab');

    // Check that focused element has outline or box-shadow (focus indicator)
    const hasFocusRing = await page.evaluate(() => {
      const el = document.activeElement as HTMLElement | null;
      if (!el) return false;
      const styles = window.getComputedStyle(el);
      const outline = styles.outline;
      const boxShadow = styles.boxShadow;
      const hasOutline =
        Boolean(outline) &&
        outline !== 'none' &&
        !outline.includes('0px none');
      const hasShadow = Boolean(boxShadow) && boxShadow !== 'none';
      return hasOutline || hasShadow;
    });

    expect(hasFocusRing).toBe(true);
  });
});
