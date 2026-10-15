import { test, expect, type Page } from '@playwright/test';
import type { Location } from '../../src/api/types';

/**
 * Locations Admin E2E Tests
 *
 * Tests run with VITE_E2E_MODE=true (set in playwright.config.ts webServer.env).
 * Keycloak is bypassed; roles injected via window.__E2E_ROLES__.
 * Backend API calls intercepted via page.route() targeting http://localhost:8080.
 *
 * Covers plan 10-01 acceptance criteria:
 * 1. Create new location with colour picker
 * 2. Edit existing location
 * 3. Soft-delete location shows confirmation dialog and removes row
 * 4. Colour picker validation (invalid hex → error, valid → swatch updates)
 */

// ── Test Data ──────────────────────────────────────────────────────────────────

const LOCATION_A: Location = {
  id: 'loc-a',
  name: 'Test Room',
  colour: '#FF5733',
  building: 'Main',
  layouts: 'Open Plan',
  deleted_at: null,
};

const LOCATION_A_UPDATED: Location = {
  ...LOCATION_A,
  name: 'Test Room Updated',
  colour: '#00AA00',
};

const EMPTY_LOCATIONS: Location[] = [];
const API_BASE = 'http://localhost:8080';
const ADMIN_ROLES = ['role_calendar_viewer', 'role_location_manager'];

// ── Helpers ──────────────────────────────────────────────────────────────────

/**
 * Set up E2E role injection (must run before page.goto).
 */
async function injectRoles(page: Page, roles: string[] = ADMIN_ROLES) {
  await page.addInitScript((injectedRoles: string[]) => {
    (window as unknown as Record<string, unknown>).__E2E_ROLES__ = injectedRoles;
  }, roles);
}

/**
 * Mock the locations list endpoint.
 */
async function mockLocationsList(page: Page, locations: Location[]) {
  await page.route(new RegExp(`^${API_BASE}/locations$`), (route) => {
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(locations),
    });
  });
}

/**
 * Mock the location getById endpoint.
 */
async function mockLocationById(page: Page, location: Location) {
  await page.route(new RegExp(`^${API_BASE}/locations/${location.id}$`), (route) => {
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(location),
    });
  });
}

/**
 * Mock the location create endpoint.
 */
async function mockLocationCreate(page: Page, response: Location) {
  await page.route(new RegExp(`^${API_BASE}/locations$`), (route) => {
    if (route.request().method() === 'POST') {
      route.fulfill({
        status: 201,
        contentType: 'application/json',
        body: JSON.stringify(response),
      });
    } else {
      route.continue();
    }
  });
}

/**
 * Mock the location update endpoint.
 */
async function mockLocationUpdate(page: Page, id: string, response: Location) {
  await page.route(new RegExp(`^${API_BASE}/locations/${id}$`), (route) => {
    if (route.request().method() === 'PUT') {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(response),
      });
    } else {
      route.continue();
    }
  });
}

/**
 * Mock the location delete endpoint (soft-delete).
 */
async function mockLocationDelete(page: Page, id: string) {
  await page.route(new RegExp(`^${API_BASE}/locations/${id}$`), (route) => {
    if (route.request().method() === 'DELETE') {
      route.fulfill({
        status: 204,
        body: '',
      });
    } else {
      route.continue();
    }
  });
}

// ── Tests ─────────────────────────────────────────────────────────────────────

test.describe('Locations Admin — Create new location', () => {
  test('Create Location form submits and shows confirmation banner', async ({ page }) => {
    await injectRoles(page);

    // Initially empty; after create we'll return LOCATION_A
    let locationsList: Location[] = EMPTY_LOCATIONS;

    await page.route(new RegExp(`^${API_BASE}/locations$`), (route) => {
      if (route.request().method() === 'GET') {
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(locationsList),
        });
      } else if (route.request().method() === 'POST') {
        locationsList = [LOCATION_A];
        route.fulfill({
          status: 201,
          contentType: 'application/json',
          body: JSON.stringify(LOCATION_A),
        });
      } else {
        route.continue();
      }
    });

    // Navigate to create form
    await page.goto('/admin/locations/new');
    await page.waitForSelector('[data-testid="location-form"]', { timeout: 15_000 });

    // Fill form: name
    await page.fill('[data-testid="location-name-input"]', 'Test Room');

    // Fill colour via text input
    await page.fill('[data-testid="colour-picker-input"]', '#FF5733');
    await page.locator('[data-testid="colour-picker-input"]').blur();

    // Fill building
    await page.fill('[data-testid="location-building-input"]', 'Main');

    // Fill layouts
    await page.fill('[data-testid="location-layouts-input"]', 'Open Plan');

    // Submit form
    await page.click('[data-testid="location-form-submit"]');

    // StateConfirmationBanner should appear with "Location saved"
    await expect(page.locator('[data-testid="state-confirmation-banner"]')).toBeVisible({
      timeout: 5_000,
    });
    await expect(page.locator('[data-testid="state-confirmation-banner"]')).toContainText(
      'Location saved',
    );
  });

  test('Navigates to /admin/locations after save', async ({ page }) => {
    await injectRoles(page);

    let locationsList: Location[] = EMPTY_LOCATIONS;

    await page.route(new RegExp(`^${API_BASE}/locations$`), (route) => {
      if (route.request().method() === 'GET') {
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(locationsList),
        });
      } else if (route.request().method() === 'POST') {
        locationsList = [LOCATION_A];
        route.fulfill({
          status: 201,
          contentType: 'application/json',
          body: JSON.stringify(LOCATION_A),
        });
      } else {
        route.continue();
      }
    });

    await page.goto('/admin/locations/new');
    await page.waitForSelector('[data-testid="location-form"]', { timeout: 15_000 });

    // Fill required fields
    await page.fill('[data-testid="location-name-input"]', 'Test Room');
    await page.fill('[data-testid="colour-picker-input"]', '#FF5733');
    await page.locator('[data-testid="colour-picker-input"]').blur();

    // Submit form
    await page.click('[data-testid="location-form-submit"]');

    // Should navigate back to /admin/locations (after 1.5s delay)
    await expect(page).toHaveURL(/\/admin\/locations$/, { timeout: 5_000 });
  });

  test('Table shows new row with colour swatch after create', async ({ page }) => {
    await injectRoles(page);
    await mockLocationsList(page, [LOCATION_A]);

    await page.goto('/admin/locations');
    await page.waitForSelector('[data-testid="location-table"]', { timeout: 15_000 });

    // Table should show "Test Room" with colour swatch
    await expect(page.locator('[data-testid="location-name-loc-a"]')).toBeVisible();
    await expect(page.locator('[data-testid="location-name-loc-a"]')).toContainText('Test Room');

    // Colour swatch should be visible (not raw hex text)
    const swatch = page.locator('[data-testid="location-colour-swatch-loc-a"]');
    await expect(swatch).toBeVisible();

    // Swatch should have the hex colour as background (not displaying as text)
    const swatchBg = await swatch.evaluate((el) => getComputedStyle(el).backgroundColor);
    // rgb(255, 87, 51) == #FF5733
    expect(swatchBg).toMatch(/rgb\(255,\s*87,\s*51\)/);
  });
});

test.describe('Locations Admin — Edit existing location', () => {
  test('Edit form is pre-filled with existing values', async ({ page }) => {
    await injectRoles(page);
    await mockLocationById(page, LOCATION_A);

    await page.goto(`/admin/locations/${LOCATION_A.id}/edit`);
    await page.waitForSelector('[data-testid="location-form"]', { timeout: 15_000 });

    // Form should be pre-filled with existing values
    await expect(page.locator('[data-testid="location-name-input"]')).toHaveValue('Test Room');
    await expect(page.locator('[data-testid="location-building-input"]')).toHaveValue('Main');
    await expect(page.locator('[data-testid="location-layouts-input"]')).toHaveValue('Open Plan');
  });

  test('Edit form saves updated values and shows confirmation banner', async ({ page }) => {
    await injectRoles(page);

    // Single route handler for the location: handles both GET and PUT
    await page.route(new RegExp(`^${API_BASE}/locations/${LOCATION_A.id}$`), (route) => {
      if (route.request().method() === 'GET') {
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(LOCATION_A),
        });
      } else if (route.request().method() === 'PUT') {
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(LOCATION_A_UPDATED),
        });
      } else {
        route.continue();
      }
    });

    await page.goto(`/admin/locations/${LOCATION_A.id}/edit`);
    await page.waitForSelector('[data-testid="location-form"]', { timeout: 15_000 });

    // Update name
    await page.fill('[data-testid="location-name-input"]', 'Test Room Updated');

    // Update colour
    await page.fill('[data-testid="colour-picker-input"]', '#00AA00');
    await page.locator('[data-testid="colour-picker-input"]').blur();

    // Submit
    await page.click('[data-testid="location-form-submit"]');

    // Confirmation banner appears
    await expect(page.locator('[data-testid="state-confirmation-banner"]')).toBeVisible({
      timeout: 5_000,
    });
    await expect(page.locator('[data-testid="state-confirmation-banner"]')).toContainText(
      'Location saved',
    );
  });
});

test.describe('Locations Admin — Soft-delete location', () => {
  test('Delete icon opens SoftDeleteDialog with correct context message', async ({ page }) => {
    await injectRoles(page);
    await mockLocationsList(page, [LOCATION_A]);

    await page.goto('/admin/locations');
    await page.waitForSelector('[data-testid="location-table"]', { timeout: 15_000 });

    // Click Delete icon on the row
    await page.click(`[data-testid="location-delete-${LOCATION_A.id}"]`);

    // SoftDeleteDialog should open
    await expect(page.locator('[data-testid="soft-delete-dialog"]')).toBeVisible({ timeout: 3_000 });

    // Context message should contain the expected text
    await expect(page.locator('[data-testid="soft-delete-context-message"]')).toContainText(
      'Existing bookings will keep this location reference',
    );
  });

  test('Confirming soft-delete removes row from table', async ({ page }) => {
    await injectRoles(page);

    let locationsList: Location[] = [LOCATION_A];

    await page.route(new RegExp(`^${API_BASE}/locations$`), (route) => {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(locationsList),
      });
    });

    await page.route(new RegExp(`^${API_BASE}/locations/${LOCATION_A.id}$`), (route) => {
      if (route.request().method() === 'DELETE') {
        locationsList = [];
        route.fulfill({ status: 204, body: '' });
      } else {
        route.continue();
      }
    });

    await page.goto('/admin/locations');
    await page.waitForSelector('[data-testid="location-table"]', { timeout: 15_000 });

    // Verify row exists before delete
    await expect(page.locator(`[data-testid="location-row-${LOCATION_A.id}"]`)).toBeVisible();

    // Click delete icon
    await page.click(`[data-testid="location-delete-${LOCATION_A.id}"]`);

    // Dialog opens
    await expect(page.locator('[data-testid="soft-delete-dialog"]')).toBeVisible({ timeout: 3_000 });

    // Confirm deletion
    await page.click('[data-testid="soft-delete-confirm"]');

    // Row should be removed from table (soft-deleted rows are filtered out)
    await expect(
      page.locator(`[data-testid="location-row-${LOCATION_A.id}"]`),
    ).toHaveCount(0, { timeout: 5_000 });
  });

  test('Cancelling soft-delete keeps row in table', async ({ page }) => {
    await injectRoles(page);
    await mockLocationsList(page, [LOCATION_A]);

    await page.goto('/admin/locations');
    await page.waitForSelector('[data-testid="location-table"]', { timeout: 15_000 });

    // Click delete icon
    await page.click(`[data-testid="location-delete-${LOCATION_A.id}"]`);

    // Dialog opens
    await expect(page.locator('[data-testid="soft-delete-dialog"]')).toBeVisible({ timeout: 3_000 });

    // Cancel deletion
    await page.click('[data-testid="soft-delete-cancel"]');

    // Dialog closes
    await expect(page.locator('[data-testid="soft-delete-dialog"]')).toHaveCount(0, {
      timeout: 3_000,
    });

    // Row still in table
    await expect(page.locator(`[data-testid="location-row-${LOCATION_A.id}"]`)).toBeVisible();
  });
});

test.describe('Locations Admin — Colour picker validation', () => {
  test('Invalid hex in colour field shows validation error on blur', async ({ page }) => {
    await injectRoles(page);

    // No need to mock API as we test form validation only
    await page.addInitScript(() => {
      (window as unknown as Record<string, unknown>).__E2E_ROLES__ = [
        'role_calendar_viewer',
        'role_location_manager',
      ];
    });

    await page.goto('/admin/locations/new');
    await page.waitForSelector('[data-testid="location-form"]', { timeout: 15_000 });

    // Enter invalid hex
    await page.fill('[data-testid="colour-picker-input"]', 'ZZZZZ');

    // Blur the field to trigger validation
    await page.locator('[data-testid="colour-picker-input"]').blur();

    // Validation error should appear
    await expect(page.locator('[data-testid="colour-picker-error"]')).toBeVisible({ timeout: 3_000 });
    await expect(page.locator('[data-testid="colour-picker-error"]')).toContainText(
      'Invalid hex colour format',
    );
  });

  test('Valid hex in colour field shows swatch preview with correct colour', async ({ page }) => {
    await injectRoles(page);

    await page.goto('/admin/locations/new');
    await page.waitForSelector('[data-testid="location-form"]', { timeout: 15_000 });

    // Enter valid hex
    await page.fill('[data-testid="colour-picker-input"]', '#123456');
    await page.locator('[data-testid="colour-picker-input"]').blur();

    // No validation error
    await expect(page.locator('[data-testid="colour-picker-error"]')).toHaveCount(0, {
      timeout: 3_000,
    });

    // Swatch preview should update (rgb(18, 52, 86) == #123456)
    const swatch = page.locator('[data-testid="colour-swatch-preview"]');
    await expect(swatch).toBeVisible();
    const swatchBg = await swatch.evaluate((el) => getComputedStyle(el).backgroundColor);
    expect(swatchBg).toMatch(/rgb\(18,\s*52,\s*86\)/);
  });

  test('Name required validation shows error when submitting empty name', async ({ page }) => {
    await injectRoles(page);

    await page.goto('/admin/locations/new');
    await page.waitForSelector('[data-testid="location-form"]', { timeout: 15_000 });

    // Leave name empty, submit form
    await page.click('[data-testid="location-form-submit"]');

    // Name error should appear
    await expect(page.locator('[data-testid="location-name-error"]')).toBeVisible({ timeout: 3_000 });
    await expect(page.locator('[data-testid="location-name-error"]')).toContainText(
      'Name is required',
    );
  });
});
