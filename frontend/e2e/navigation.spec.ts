import { test, expect } from '@playwright/test';

/**
 * Navigation & Permission-Gated Sidebar Tests
 *
 * All tests run with VITE_E2E_MODE=true (set in playwright.config.ts webServer.env),
 * which bypasses real Keycloak initialisation. Per-test roles are injected via
 * page.addInitScript() setting window.__E2E_ROLES__ before page load, which
 * AuthProvider reads in E2E mode to configure keycloak.realmAccess.
 */

// ── Helpers ──────────────────────────────────────────────────────────────────

/**
 * Navigate to a page with a specific set of Keycloak realm roles injected.
 * Must be called BEFORE page.goto() so the init script runs before page JS.
 */
async function gotoWithRoles(
  page: import('@playwright/test').Page,
  url: string,
  roles: string[],
) {
  await page.addInitScript((injectedRoles: string[]) => {
    (window as unknown as Record<string, unknown>).__E2E_ROLES__ = injectedRoles;
  }, roles);
  await page.goto(url);
  // Wait for app shell to be rendered (sidebar landmark is present)
  await page.waitForSelector('aside.sidebar, nav.bottom-nav, [aria-label="Main navigation"]', {
    timeout: 10_000,
  });
}

// ── Test Suite ────────────────────────────────────────────────────────────────

test.describe('Permission-gated sidebar navigation', () => {
  /**
   * Test 1: User WITH role_booking_approver sees Approvals link in sidebar.
   */
  test('user with role_booking_approver sees Approvals link', async ({ page }) => {
    await gotoWithRoles(page, '/bookings?view=month', [
      'role_calendar_viewer',
      'role_booking_approver',
    ]);

    // Approvals link must be visible in the DOM (structural presence, not just visible)
    const approvalsLink = page.locator('aside.sidebar a[href="/approvals"]');
    await expect(approvalsLink).toBeVisible();
  });

  /**
   * Test 2: User WITHOUT role_booking_approver does NOT see Approvals link.
   * Uses locator.count() to assert structural absence (not just CSS-hidden).
   */
  test('user without role_booking_approver does not see Approvals link', async ({ page }) => {
    await gotoWithRoles(page, '/bookings?view=month', ['role_calendar_viewer']);

    // Assert structural absence — count must be 0 (not in DOM at all)
    const approvalsLinkCount = await page
      .locator('aside.sidebar a[href="/approvals"]')
      .count();
    expect(approvalsLinkCount).toBe(0);
  });

  /**
   * Test 3: User WITH an admin role (e.g. role_location_manager) sees Admin section.
   */
  test('user with admin role sees Admin section', async ({ page }) => {
    await gotoWithRoles(page, '/bookings?view=month', [
      'role_calendar_viewer',
      'role_location_manager',
    ]);

    // Admin section heading must be present
    const adminSectionTitle = page.locator('aside.sidebar .sidebar-section-title');
    await expect(adminSectionTitle).toBeVisible();
    await expect(adminSectionTitle).toContainText('Admin');

    // Admin sub-links must be present
    await expect(page.locator('aside.sidebar a[href="/admin/locations"]')).toBeVisible();
    await expect(page.locator('aside.sidebar a[href="/admin/resources"]')).toBeVisible();
    await expect(page.locator('aside.sidebar a[href="/admin/users"]')).toBeVisible();
  });

  /**
   * Test 4: User WITHOUT any admin roles does NOT see Admin section.
   * Uses count() to assert structural absence.
   */
  test('user without admin roles does not see Admin section', async ({ page }) => {
    await gotoWithRoles(page, '/bookings?view=month', ['role_calendar_viewer']);

    // Admin section heading must be structurally absent
    const adminSectionTitleCount = await page
      .locator('aside.sidebar .sidebar-section-title')
      .count();
    expect(adminSectionTitleCount).toBe(0);

    // Individual admin links must also be structurally absent
    const locationsLinkCount = await page
      .locator('aside.sidebar a[href="/admin/locations"]')
      .count();
    expect(locationsLinkCount).toBe(0);
  });
});

test.describe('Core navigation routing', () => {
  test('root redirects to /bookings?view=month', async ({ page }) => {
    await gotoWithRoles(page, '/', ['role_calendar_viewer']);
    await expect(page).toHaveURL(/\/bookings\?view=month/);
    await expect(page.locator('h1')).toContainText('Calendar View');
  });

  test('/bookings?view=day shows Day View', async ({ page }) => {
    await gotoWithRoles(page, '/bookings?view=day', ['role_calendar_viewer']);
    await expect(page.locator('h1')).toContainText('Day View');
  });

  test('/bookings?view=list shows List View', async ({ page }) => {
    await gotoWithRoles(page, '/bookings?view=list', ['role_calendar_viewer']);
    await expect(page.locator('h1')).toContainText('List View');
  });

  test('view-switcher tabs are rendered in bookings workspace', async ({ page }) => {
    await gotoWithRoles(page, '/bookings?view=month', ['role_calendar_viewer']);

    // All three view tabs must be present
    await expect(page.locator('[role="tab"]:has-text("Calendar")')).toBeVisible();
    await expect(page.locator('[role="tab"]:has-text("Day")')).toBeVisible();
    await expect(page.locator('[role="tab"]:has-text("List")')).toBeVisible();
  });

  test('active tab reflects current ?view= param', async ({ page }) => {
    await gotoWithRoles(page, '/bookings?view=day', ['role_calendar_viewer']);

    // Day tab must be marked active
    const dayTab = page.locator('[role="tab"][aria-selected="true"]');
    await expect(dayTab).toContainText('Day');
  });

  test('legacy /calendar route redirects to /bookings?view=month', async ({ page }) => {
    await gotoWithRoles(page, '/calendar', ['role_calendar_viewer']);
    await expect(page).toHaveURL(/\/bookings\?view=month/);
  });

  test('legacy /list route redirects to /bookings?view=list', async ({ page }) => {
    await gotoWithRoles(page, '/list', ['role_calendar_viewer']);
    await expect(page).toHaveURL(/\/bookings\?view=list/);
  });

  test('common sidebar links always visible (Calendar, Feeds, My Account)', async ({
    page,
  }) => {
    await gotoWithRoles(page, '/bookings?view=month', ['role_calendar_viewer']);

    // These links appear regardless of role
    await expect(
      page.locator('aside.sidebar a[href="/bookings?view=month"]'),
    ).toBeVisible();
    await expect(page.locator('aside.sidebar a[href="/feeds"]')).toBeVisible();
    await expect(page.locator('aside.sidebar a[href="/account"]')).toBeVisible();
  });

  test('display board renders without nav chrome', async ({ page }) => {
    await page.goto('/display-board');
    await expect(page).toHaveURL('/display-board');
    await expect(page.locator('h1')).toContainText('Display Board');
    // No sidebar or header
    await expect(page.locator('aside.sidebar')).toHaveCount(0);
    await expect(page.locator('header.header')).toHaveCount(0);
  });
});
