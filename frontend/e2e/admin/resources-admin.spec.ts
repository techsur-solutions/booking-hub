import { test, expect, type Page } from '@playwright/test';
import type { Resource, Location } from '../../src/api/types';

/**
 * Resources Admin End-to-End Tests (UI-ADMIN-02)
 *
 * Tests run with VITE_E2E_MODE=true (set in playwright.config.ts webServer.env).
 * Keycloak is bypassed; roles injected via window.__E2E_ROLES__.
 * Backend API calls intercepted via page.route() targeting http://localhost:8080.
 * No real backend or auth required.
 *
 * Covered scenarios:
 *   1. Create new resource with is_unique enabled
 *   2. Edit resource to restrict locations
 *   3. Soft-delete resource
 *   4. Create resource without is_unique (non-conflicting resource)
 */

// ── Fixture data ──────────────────────────────────────────────────────────────

const LOCATION_ROOM_A: Location = {
  id: 'loc-001',
  name: 'Room A',
  colour: '#3b82f6',
  building: 'Main',
  layouts: null,
  deleted_at: null,
};

const LOCATION_ROOM_B: Location = {
  id: 'loc-002',
  name: 'Room B',
  colour: '#10b981',
  building: 'Annex',
  layouts: null,
  deleted_at: null,
};

const ALL_LOCATIONS: Location[] = [LOCATION_ROOM_A, LOCATION_ROOM_B];

const RESOURCE_PHONE: Resource = {
  id: 'res-001',
  name: 'Conference Phone',
  type: 'Equipment',
  description: 'Mobile conference phone',
  is_unique: true,
  restrict_locations: [],
  deleted_at: null,
};

const RESOURCE_WHITEBOARD: Resource = {
  id: 'res-002',
  name: 'Whiteboard',
  type: null,
  description: null,
  is_unique: false,
  restrict_locations: null,
  deleted_at: null,
};

const RESOURCE_PHONE_WITH_RESTRICT: Resource = {
  ...RESOURCE_PHONE,
  restrict_locations: ['loc-001'],
};

const API_BASE = 'http://localhost:8080';

// ── Helpers ───────────────────────────────────────────────────────────────────

/** Navigate to /admin/resources with injected roles and mocked API */
async function gotoResourcesAdmin(
  page: Page,
  options: {
    roles?: string[];
    resources?: Resource[];
    locations?: Location[];
  } = {}
) {
  const {
    roles = ['role_location_manager'],
    resources = [RESOURCE_PHONE],
    locations = ALL_LOCATIONS,
  } = options;

  // Inject roles before page load
  await page.addInitScript((injectedRoles: string[]) => {
    (window as unknown as Record<string, unknown>).__E2E_ROLES__ = injectedRoles;
  }, roles);

  // Mock API: GET /resources (list)
  await page.route(`${API_BASE}/resources**`, async (route) => {
    const url = route.request().url();
    const method = route.request().method();

    if (method === 'GET' && /\/resources\/[^?]+/.test(url)) {
      // Individual resource fetch (for ResourceEdit)
      const id = url.split('/resources/')[1]?.split('?')[0];
      const resource = resources.find((r) => r.id === id);
      await route.fulfill({
        status: resource ? 200 : 404,
        contentType: 'application/json',
        body: resource ? JSON.stringify(resource) : JSON.stringify({ error: 'Not found' }),
      });
    } else if (method === 'GET') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(resources),
      });
    } else if (method === 'POST') {
      // Create resource
      const body = JSON.parse(route.request().postData() ?? '{}');
      const newResource: Resource = {
        id: `res-new-${Date.now()}`,
        name: body.name ?? 'New Resource',
        type: body.type ?? null,
        description: body.description ?? null,
        is_unique: body.is_unique ?? false,
        restrict_locations: body.restrict_locations ?? null,
        deleted_at: null,
      };
      await route.fulfill({
        status: 201,
        contentType: 'application/json',
        body: JSON.stringify(newResource),
      });
    } else if (method === 'PUT') {
      // Update resource
      const id = url.split('/resources/')[1]?.split('?')[0];
      const body = JSON.parse(route.request().postData() ?? '{}');
      const existing = resources.find((r) => r.id === id) ?? RESOURCE_PHONE;
      const updated: Resource = { ...existing, ...body };
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(updated),
      });
    } else if (method === 'DELETE') {
      // Soft-delete
      await route.fulfill({
        status: 204,
        body: '',
      });
    } else {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      });
    }
  });

  // Mock API: GET /locations (for restrict_locations multi-select in ResourceForm)
  await page.route(`${API_BASE}/locations**`, async (route) => {
    const url = route.request().url();
    if (/\/locations\/[^?]+/.test(url)) {
      const id = url.split('/locations/')[1]?.split('?')[0];
      const loc = locations.find((l) => l.id === id);
      await route.fulfill({
        status: loc ? 200 : 404,
        contentType: 'application/json',
        body: loc ? JSON.stringify(loc) : JSON.stringify({ error: 'Not found' }),
      });
    } else {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(locations),
      });
    }
  });

  await page.goto('/admin/resources');
}

// ── Tests ─────────────────────────────────────────────────────────────────────

test.describe('Resources Admin', () => {
  test('Test 1: Create new resource with is_unique enabled', async ({ page }) => {
    // Start with empty resources list
    await gotoResourcesAdmin(page, { resources: [] });

    // Resources Admin page renders
    await expect(page.getByTestId('resources-admin')).toBeVisible();

    // Click "Create Resource" button
    await page.getByTestId('create-resource-button').click();

    // Should navigate to /admin/resources/new
    await expect(page).toHaveURL(/\/admin\/resources\/new/);
    await expect(page.getByTestId('resource-form')).toBeVisible();

    // Fill form: name, type, description
    await page.getByTestId('resource-name-input').fill('Conference Phone');
    await page.getByTestId('resource-type-input').fill('Equipment');
    await page.getByTestId('resource-description-input').fill('Mobile conference phone');

    // Check is_unique checkbox
    await page.getByTestId('resource-is-unique-checkbox').check();
    await expect(page.getByTestId('resource-is-unique-checkbox')).toBeChecked();

    // Leave restrict_locations unchecked (all locations)

    // Submit form
    await page.getByTestId('resource-form-submit').click();

    // Assert: StateConfirmationBanner appears with "Resource saved"
    await expect(page.getByTestId('state-confirmation-banner')).toBeVisible();
    await expect(page.getByTestId('state-confirmation-banner')).toContainText('Resource saved');
  });

  test('Test 2: Edit resource to restrict locations', async ({ page }) => {
    // Start with RESOURCE_PHONE in table
    await gotoResourcesAdmin(page, {
      resources: [RESOURCE_PHONE],
    });

    await expect(page.getByTestId('resource-table')).toBeVisible();
    await expect(page.getByTestId(`resource-row-${RESOURCE_PHONE.id}`)).toBeVisible();

    // Initially "All locations" in restrict column
    await expect(
      page.getByTestId(`resource-restrict-locations-${RESOURCE_PHONE.id}`)
    ).toContainText('All locations');

    // Click Edit on RESOURCE_PHONE
    await page.getByTestId(`resource-edit-${RESOURCE_PHONE.id}`).click();

    // Should navigate to edit route
    await expect(page).toHaveURL(/\/admin\/resources\/res-001\/edit/);
    await expect(page.getByTestId('resource-form')).toBeVisible();

    // Wait for form to load (is_unique pre-checked because RESOURCE_PHONE.is_unique = true)
    await expect(page.getByTestId('resource-is-unique-checkbox')).toBeChecked();

    // Select "Room A" in restrict_locations
    await expect(page.getByTestId('restrict-locations-multi-select')).toBeVisible();
    await page.getByTestId('restrict-location-loc-001').check();

    // Submit form
    await page.getByTestId('resource-form-submit').click();

    // Assert: StateConfirmationBanner appears
    await expect(page.getByTestId('state-confirmation-banner')).toBeVisible();
  });

  test('Test 3: Soft-delete resource', async ({ page }) => {
    let resourceDeleted = false;

    // Override the resources mock to return empty list after delete
    await page.addInitScript((injectedRoles: string[]) => {
      (window as unknown as Record<string, unknown>).__E2E_ROLES__ = injectedRoles;
    }, ['role_location_manager']);

    await page.route(`${API_BASE}/resources**`, async (route) => {
      const method = route.request().method();
      const url = route.request().url();

      if (method === 'DELETE') {
        resourceDeleted = true;
        await route.fulfill({ status: 204, body: '' });
      } else if (method === 'GET') {
        // After delete, return empty list
        const resources = resourceDeleted ? [] : [RESOURCE_PHONE];
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(
            /\/resources\/[^?]+/.test(url)
              ? RESOURCE_PHONE
              : resources
          ),
        });
      } else {
        await route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      }
    });

    await page.route(`${API_BASE}/locations**`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(ALL_LOCATIONS),
      });
    });

    await page.goto('/admin/resources');

    // Resource table visible with Conference Phone row
    await expect(page.getByTestId('resource-table')).toBeVisible();
    await expect(page.getByTestId(`resource-row-${RESOURCE_PHONE.id}`)).toBeVisible();

    // Click Delete icon
    await page.getByTestId(`resource-delete-${RESOURCE_PHONE.id}`).click();

    // SoftDeleteDialog opens with context message
    await expect(page.getByTestId('soft-delete-dialog')).toBeVisible();
    await expect(page.getByTestId('soft-delete-context-message')).toContainText(
      'Existing bookings will keep this resource reference'
    );

    // Click Confirm
    await page.getByTestId('soft-delete-confirm').click();

    // Wait for dialog to close
    await expect(page.getByTestId('soft-delete-dialog')).not.toBeVisible();

    // Row removed from table (soft-deleted row not shown)
    await expect(
      page.getByTestId(`resource-row-${RESOURCE_PHONE.id}`)
    ).not.toBeVisible();
  });

  test('Test 4: Create resource without is_unique (non-conflicting resource)', async ({ page }) => {
    await gotoResourcesAdmin(page, { resources: [] });

    await expect(page.getByTestId('resources-admin')).toBeVisible();

    // Click "Create Resource" button
    await page.getByTestId('create-resource-button').click();

    await expect(page).toHaveURL(/\/admin\/resources\/new/);
    await expect(page.getByTestId('resource-form')).toBeVisible();

    // Fill name only, leave is_unique unchecked
    await page.getByTestId('resource-name-input').fill('Whiteboard');

    // is_unique should be unchecked by default
    await expect(page.getByTestId('resource-is-unique-checkbox')).not.toBeChecked();

    // Submit
    await page.getByTestId('resource-form-submit').click();

    // Assert: StateConfirmationBanner appears
    await expect(page.getByTestId('state-confirmation-banner')).toBeVisible();
    await expect(page.getByTestId('state-confirmation-banner')).toContainText('Resource saved');
  });
});
