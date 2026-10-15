import { test, expect, type Page } from '@playwright/test';
import type { PermissionMatrix } from '../../src/api/permissionsApi';

/**
 * Role & Permission Matrix — End-to-End Tests (UI-ADMIN-05)
 *
 * Covers:
 *   1. Matrix renders with correct structure (17 rows × N columns)
 *   2. Checkmarks rendered for granted permissions
 *   3. Deny-by-default cells visually highlighted
 *   4. Keycloak Admin link present and correct
 *
 * All tests run with VITE_E2E_MODE=true (playwright.config.ts webServer.env).
 * Keycloak is bypassed; roles injected via window.__E2E_ROLES__.
 * Backend API calls to GET /permissions intercepted via page.route().
 */

// ── Fixture data ───────────────────────────────────────────────────────────────

const MOCK_MATRIX: PermissionMatrix = {
  roles: ['role_calendar_viewer', 'role_booking_approver', 'role_user_manager'],
  permissions: [
    'accessCalendar',
    'allowRoomBooking',
    'allowApproveBooking',
    'allowViewBookings',
    'allowEditBookings',
    'allowDeleteBookings',
    'allowViewLocations',
    'allowEditLocations',
    'allowDeleteLocations',
    'allowViewResources',
    'allowEditResources',
    'allowDeleteResources',
    'allowSettings',
    'allowUsers',
    'allowPermissions',
    'allowAPI',
    'allowAuditLog',
  ],
  matrix: {
    role_calendar_viewer: {
      accessCalendar: true,
      allowRoomBooking: true,
      allowViewBookings: true,
      allowViewLocations: true,
      allowViewResources: true,
      // All others false / undefined → deny-by-default
    },
    role_booking_approver: {
      accessCalendar: true,
      allowRoomBooking: true,
      allowApproveBooking: true,
      allowViewBookings: true,
      allowEditBookings: true,
      allowDeleteBookings: false,
      allowViewLocations: true,
      allowViewResources: true,
    },
    role_user_manager: {
      accessCalendar: true,
      allowRoomBooking: true,
      allowViewBookings: true,
      allowViewLocations: true,
      allowViewResources: true,
      allowUsers: true,
    },
  },
};

const API_BASE = 'http://localhost:8080';

// ── Helpers ────────────────────────────────────────────────────────────────────

/** Navigate to /admin/roles with injected roles and mocked GET /permissions */
async function gotoRoleMatrix(
  page: Page,
  options: {
    roles?: string[];
    matrixData?: PermissionMatrix;
    permissionsStatus?: number;
  } = {}
) {
  const {
    roles = ['role_permission_manager'],
    matrixData = MOCK_MATRIX,
    permissionsStatus = 200,
  } = options;

  // Inject roles before page load
  await page.addInitScript((injectedRoles: string[]) => {
    (window as unknown as Record<string, unknown>).__E2E_ROLES__ = injectedRoles;
  }, roles);

  // Mock GET /permissions to return our sample matrix
  await page.route(`${API_BASE}/permissions**`, async (route) => {
    if (route.request().method() === 'GET') {
      await route.fulfill({
        status: permissionsStatus,
        contentType: 'application/json',
        body: permissionsStatus === 200 ? JSON.stringify(matrixData) : JSON.stringify({ error: 'Server error' }),
      });
    } else {
      await route.continue();
    }
  });

  await page.goto('/admin/roles');

  // Wait for the matrix page to be visible
  await page.waitForSelector('[data-testid="role-permission-matrix"]', { timeout: 10_000 });
}

// ── Test Suite ─────────────────────────────────────────────────────────────────

test.describe('Role & Permission Matrix — Structure', () => {
  /**
   * Test 1: Matrix renders with correct structure
   * - Page title "Role & Permission Matrix" visible
   * - Read-only notice displayed
   * - Grid has 17 rows (one per permission flag)
   * - Grid has N columns matching roles in mock data (3 roles)
   */
  test('1. Matrix renders with page title, notice, 17 permission rows, and role columns', async ({
    page,
  }) => {
    await gotoRoleMatrix(page);

    // Page title visible
    await expect(page.locator('h1')).toContainText('Role & Permission Matrix');

    // Read-only notice is displayed
    const notice = page.getByTestId('read-only-notice');
    await expect(notice).toBeVisible();
    await expect(notice).toContainText('read-only');

    // Wait for matrix table to be rendered (after API mock resolves)
    await expect(page.getByTestId('permission-matrix-table')).toBeVisible({ timeout: 8_000 });

    // 17 permission rows — one for each flag
    const rows = page.getByTestId('permission-matrix-table').locator('tbody tr');
    await expect(rows).toHaveCount(17);

    // 3 role columns (+ 1 header column = 4 th in header row)
    const headerCells = page
      .getByTestId('permission-matrix-table')
      .locator('thead tr th');
    await expect(headerCells).toHaveCount(4); // 1 corner + 3 roles

    // Role names appear in column headers
    await expect(page.locator('th:has-text("role_calendar_viewer")')).toBeVisible();
    await expect(page.locator('th:has-text("role_booking_approver")')).toBeVisible();
    await expect(page.locator('th:has-text("role_user_manager")')).toBeVisible();

    // All 17 permission flags appear as row headers (spot-check 5)
    await expect(page.locator('th:has-text("Access Calendar")')).toBeVisible();
    await expect(page.locator('th:has-text("Allow Room Booking")')).toBeVisible();
    await expect(page.locator('th:has-text("Allow Approve Booking")')).toBeVisible();
    await expect(page.locator('th:has-text("Allow Audit Log")')).toBeVisible();
    await expect(page.locator('th:has-text("Allow Permissions")')).toBeVisible();
  });

  /**
   * Test 2: Checkmarks rendered for granted permissions
   * - Cell (role_calendar_viewer, accessCalendar) contains checkmark ✓
   * - Cell (role_calendar_viewer, allowApproveBooking) is empty/dash (deny-by-default)
   * - Cell (role_booking_approver, allowApproveBooking) contains checkmark ✓
   */
  test('2. Checkmarks in granted cells; denied cells show dash indicator', async ({ page }) => {
    await gotoRoleMatrix(page);

    await expect(page.getByTestId('permission-matrix-table')).toBeVisible({ timeout: 8_000 });

    // role_calendar_viewer × accessCalendar → GRANTED → checkmark
    const grantedCell = page.getByTestId('cell-role_calendar_viewer-accessCalendar');
    await expect(grantedCell).toBeVisible();
    await expect(grantedCell).toContainText('✓');
    await expect(grantedCell).toHaveAttribute('data-granted', 'true');

    // role_calendar_viewer × allowApproveBooking → DENIED → dash
    const deniedCell = page.getByTestId('cell-role_calendar_viewer-allowApproveBooking');
    await expect(deniedCell).toBeVisible();
    await expect(deniedCell).toContainText('–');
    await expect(deniedCell).toHaveAttribute('data-granted', 'false');

    // role_booking_approver × allowApproveBooking → GRANTED → checkmark
    const approverGrantedCell = page.getByTestId('cell-role_booking_approver-allowApproveBooking');
    await expect(approverGrantedCell).toBeVisible();
    await expect(approverGrantedCell).toContainText('✓');
    await expect(approverGrantedCell).toHaveAttribute('data-granted', 'true');
  });

  /**
   * Test 3: Deny-by-default cells visually highlighted
   * - Empty cells have CSS class 'permission-denied' (data-granted="false")
   * - Granted cells have CSS class 'permission-granted' (data-granted="true")
   */
  test('3. Deny-by-default cells have permission-denied class; granted cells have permission-granted', async ({
    page,
  }) => {
    await gotoRoleMatrix(page);

    await expect(page.getByTestId('permission-matrix-table')).toBeVisible({ timeout: 8_000 });

    // Denied cell has permission-denied class
    const deniedCell = page.getByTestId('cell-role_calendar_viewer-allowApproveBooking');
    await expect(deniedCell).toHaveClass(/permission-denied/);

    // Granted cell has permission-granted class
    const grantedCell = page.getByTestId('cell-role_calendar_viewer-accessCalendar');
    await expect(grantedCell).toHaveClass(/permission-granted/);
  });

  /**
   * Test 4: Keycloak Admin link present, correct pattern, opens new tab
   * - "View in Keycloak Admin" link is visible
   * - Link href matches pattern /admin/{realm}/console/#/realms/{realm}/roles
   * - Link has target="_blank"
   */
  test('4. Keycloak Admin link visible with correct href and target="_blank"', async ({ page }) => {
    await gotoRoleMatrix(page);

    // "View in Keycloak Admin" link is visible in page header
    const adminLink = page.getByTestId('keycloak-admin-link');
    await expect(adminLink).toBeVisible();
    await expect(adminLink).toContainText('View in Keycloak Admin');

    // Link has target="_blank" (opens in new tab)
    await expect(adminLink).toHaveAttribute('target', '_blank');

    // Link href matches pattern: /admin/{realm}/console/#/realms/{realm}/roles
    const href = await adminLink.getAttribute('href');
    expect(href).toMatch(/\/admin\/[^/]+\/console\/#\/realms\/[^/]+\/roles/);

    // rel="noopener noreferrer" for security
    const rel = await adminLink.getAttribute('rel');
    expect(rel).toContain('noopener');
    expect(rel).toContain('noreferrer');
  });
});
