import { test, expect, type Page } from '@playwright/test';
import type { CustomField, FieldTemplate, Location } from '../../src/api/types';

/**
 * Custom Field Builder E2E Tests
 *
 * Tests run with VITE_E2E_MODE=true (set in playwright.config.ts webServer.env).
 * Keycloak is bypassed; roles injected via window.__E2E_ROLES__.
 * Backend API calls intercepted via page.route() targeting http://localhost:8080.
 *
 * Gate: /admin/custom-fields requires role_location_manager (admin role).
 *
 * Covers plan 10-03 acceptance criteria:
 * 1. Create textfield custom field (no options required)
 * 2. Create select custom field with options validation
 * 3. Live preview updates on field_type change
 * 4. Create field template with location context
 */

// ── Test Data ──────────────────────────────────────────────────────────────────

const LOCATION_ROOM_A: Location = {
  id: 'loc-room-a',
  name: 'Room A',
  colour: '#3b82f6',
  building: 'Main',
  layouts: null,
  deleted_at: null,
};

const FIELD_TEXTFIELD: CustomField = {
  id: 'cf-textfield-001',
  name: 'Special Instructions',
  field_type: 'textfield',
  required: false,
  options: null,
  context_id: null,
};

const FIELD_SELECT: CustomField = {
  id: 'cf-select-001',
  name: 'Catering',
  field_type: 'select',
  required: false,
  options: ['None', 'Coffee & Snacks', 'Full Meal'],
  context_id: null,
};

const TEMPLATE_ROOM_A: FieldTemplate = {
  id: 'ft-001',
  name: 'Room A Template',
  context_id: 'loc-room-a',
  field_ids: [FIELD_SELECT.id, FIELD_TEXTFIELD.id],
};

// ── API Mocks ─────────────────────────────────────────────────────────────────

const API_BASE = 'http://localhost:8080';

interface MockState {
  fields: CustomField[];
  templates: FieldTemplate[];
  locations: Location[];
}

async function setupMocks(page: Page, state: MockState) {
  // Mock GET /custom-fields
  await page.route(`${API_BASE}/custom-fields**`, (route) => {
    if (route.request().method() === 'GET') {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(state.fields),
      });
    } else {
      // POST /custom-fields → return the new field
      const body = JSON.parse(route.request().postData() ?? '{}') as Partial<CustomField>;
      const newField: CustomField = {
        id: `cf-new-${Date.now()}`,
        name: body.name ?? 'New Field',
        field_type: body.field_type ?? 'textfield',
        required: body.required ?? false,
        options: body.options ?? null,
        context_id: null,
      };
      state.fields = [...state.fields, newField];
      route.fulfill({
        status: 201,
        contentType: 'application/json',
        body: JSON.stringify(newField),
      });
    }
  });

  // Mock GET /field-templates
  await page.route(`${API_BASE}/field-templates**`, (route) => {
    if (route.request().method() === 'GET') {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(state.templates),
      });
    } else {
      // POST /field-templates → return the new template
      const body = JSON.parse(route.request().postData() ?? '{}') as Partial<FieldTemplate>;
      const newTemplate: FieldTemplate = {
        id: `ft-new-${Date.now()}`,
        name: body.name ?? 'New Template',
        context_id: body.context_id ?? null,
        field_ids: body.field_ids ?? [],
      };
      state.templates = [...state.templates, newTemplate];
      route.fulfill({
        status: 201,
        contentType: 'application/json',
        body: JSON.stringify(newTemplate),
      });
    }
  });

  // Mock GET /locations
  await page.route(`${API_BASE}/locations**`, (route) => {
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(state.locations),
    });
  });
}

async function gotoCustomFields(
  page: Page,
  state: MockState,
  roles: string[] = ['role_calendar_viewer', 'role_location_manager']
) {
  // Inject roles before page load
  await page.addInitScript((injectedRoles: string[]) => {
    (window as unknown as Record<string, unknown>).__E2E_ROLES__ = injectedRoles;
  }, roles);

  await setupMocks(page, state);

  await page.goto('/admin/custom-fields');
  await page.waitForSelector('[data-testid="custom-field-builder"]', { timeout: 15_000 });
}

// ── Tests ─────────────────────────────────────────────────────────────────────

test.describe('Custom Field Builder — Definitions tab', () => {
  test('Test 1: Create textfield custom field (no options required)', async ({ page }) => {
    const state: MockState = {
      fields: [],
      templates: [],
      locations: [LOCATION_ROOM_A],
    };

    await gotoCustomFields(page, state);

    // Should be on Definitions tab by default
    await expect(page.locator('[data-testid="tab-definitions"]')).toHaveAttribute(
      'aria-selected',
      'true'
    );

    // Click Create Field Definition button
    await page.click('[data-testid="create-field-definition-btn"]');
    await page.waitForSelector('[data-testid="custom-field-form"]', { timeout: 5_000 });

    // Fill form: name="Special Instructions", field_type="textfield"
    await page.fill('[data-testid="cf-name"]', 'Special Instructions');

    // Verify field_type dropdown has textfield selected (default)
    await expect(page.locator('[data-testid="cf-field-type"]')).toHaveValue('textfield');

    // Assert: Options input is NOT visible for textfield (textfield MUST NOT have options)
    await expect(page.locator('[data-testid="cf-options-group"]')).toHaveCount(0);

    // Assert: FieldPreview shows text input
    await expect(page.locator('[data-testid="preview-textfield"]')).toBeVisible();

    // Submit form
    await page.click('[data-testid="cf-submit"]');

    // Assert: StateConfirmationBanner appears
    await expect(page.locator('[data-testid="state-confirmation-banner"]')).toBeVisible({
      timeout: 5_000,
    });

    // Assert: Definitions table now contains new row
    await expect(
      page.locator('[data-testid="field-definitions-table"]')
    ).toBeVisible({ timeout: 5_000 });
  });

  test('Test 2: Create select custom field with options validation', async ({ page }) => {
    const state: MockState = {
      fields: [FIELD_TEXTFIELD],
      templates: [],
      locations: [LOCATION_ROOM_A],
    };

    await gotoCustomFields(page, state);

    // Click Create Field Definition
    await page.click('[data-testid="create-field-definition-btn"]');
    await page.waitForSelector('[data-testid="custom-field-form"]', { timeout: 5_000 });

    // Fill name
    await page.fill('[data-testid="cf-name"]', 'Catering');

    // Change field_type to "select"
    await page.selectOption('[data-testid="cf-field-type"]', 'select');

    // Assert: Options textarea is now visible for select type
    await expect(page.locator('[data-testid="cf-options-group"]')).toBeVisible();

    // Submit WITHOUT entering any options — validation should block
    await page.click('[data-testid="cf-submit"]');

    // Assert: Validation error appears (Options required for select type)
    await expect(page.locator('[data-testid="custom-field-form-error"]')).toBeVisible({
      timeout: 3_000,
    });
    await expect(page.locator('[data-testid="custom-field-form-error"]')).toContainText(
      'Options required for select type'
    );

    // Enter options: "None\nCoffee & Snacks\nFull Meal" (one per line)
    await page.fill('[data-testid="cf-options"]', 'None\nCoffee & Snacks\nFull Meal');

    // Assert: FieldPreview updates showing dropdown with 3 options
    await expect(page.locator('[data-testid="preview-select"]')).toBeVisible();
    // The preview select should contain the option values
    const previewSelect = page.locator('[data-testid="preview-select"]');
    await expect(previewSelect.locator('option').nth(1)).toHaveText('None');
    await expect(previewSelect.locator('option').nth(2)).toHaveText('Coffee & Snacks');
    await expect(previewSelect.locator('option').nth(3)).toHaveText('Full Meal');

    // Submit form
    await page.click('[data-testid="cf-submit"]');

    // Assert: Confirmation banner appears
    await expect(page.locator('[data-testid="state-confirmation-banner"]')).toBeVisible({
      timeout: 5_000,
    });
  });

  test('Test 3: Live preview updates on field_type change', async ({ page }) => {
    const state: MockState = {
      fields: [],
      templates: [],
      locations: [LOCATION_ROOM_A],
    };

    await gotoCustomFields(page, state);

    // Open create form
    await page.click('[data-testid="create-field-definition-btn"]');
    await page.waitForSelector('[data-testid="custom-field-form"]', { timeout: 5_000 });

    // Select field_type="radio"
    await page.selectOption('[data-testid="cf-field-type"]', 'radio');

    // Enter options: "Yes\nNo"
    await page.fill('[data-testid="cf-options"]', 'Yes\nNo');

    // Assert: FieldPreview shows radio buttons labeled "Yes" and "No"
    await expect(page.locator('[data-testid="preview-radio"]')).toBeVisible();

    // Radio group should have "Yes" and "No" labels
    const radioLabels = page.locator('[data-testid="preview-radio"] label');
    await expect(radioLabels).toHaveCount(2);
    await expect(radioLabels.nth(0)).toContainText('Yes');
    await expect(radioLabels.nth(1)).toContainText('No');

    // Change field_type to "checkbox"
    await page.selectOption('[data-testid="cf-field-type"]', 'checkbox');

    // Assert: FieldPreview updates to show 2 checkboxes labeled "Yes" and "No"
    await expect(page.locator('[data-testid="preview-checkbox"]')).toBeVisible();
    const checkboxLabels = page.locator('[data-testid="preview-checkbox"] label');
    await expect(checkboxLabels).toHaveCount(2);
    await expect(checkboxLabels.nth(0)).toContainText('Yes');
    await expect(checkboxLabels.nth(1)).toContainText('No');

    // Verify radio preview is gone
    await expect(page.locator('[data-testid="preview-radio"]')).toHaveCount(0);
  });
});

test.describe('Custom Field Builder — Templates tab', () => {
  test('Test 4: Create field template with location context', async ({ page }) => {
    const state: MockState = {
      fields: [FIELD_SELECT, FIELD_TEXTFIELD],
      templates: [],
      locations: [LOCATION_ROOM_A],
    };

    await gotoCustomFields(page, state);

    // Switch to "Field Templates" tab
    await page.click('[data-testid="tab-templates"]');
    await expect(page.locator('[data-testid="custom-field-templates-tab"]')).toBeVisible();

    // Click "Create Field Template"
    await page.click('[data-testid="create-field-template-btn"]');
    await page.waitForSelector('[data-testid="field-template-form"]', { timeout: 5_000 });

    // Enter template name
    await page.fill('[data-testid="template-name-input"]', 'Room A Template');

    // Select context_id: "Room A" (specific location)
    await page.selectOption('[data-testid="template-context-select"]', 'loc-room-a');

    // Multi-select associated fields: check "Catering" and "Special Instructions"
    await page.click(`[data-testid="template-field-checkbox-${FIELD_SELECT.id}"]`);
    await page.click(`[data-testid="template-field-checkbox-${FIELD_TEXTFIELD.id}"]`);

    // Submit form
    await page.click('[data-testid="template-submit-btn"]');

    // Assert: Confirmation banner appears
    await expect(page.locator('[data-testid="state-confirmation-banner"]')).toBeVisible({
      timeout: 5_000,
    });
  });
});
