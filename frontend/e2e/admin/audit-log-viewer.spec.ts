import { test, expect, type Page } from '@playwright/test';
import type { AuditLogEntry } from '../../src/api/auditLogApi';

/**
 * Audit Log Viewer End-to-End Tests (UI-ADMIN-07)
 *
 * Tests run with VITE_E2E_MODE=true (playwright.config.ts webServer.env).
 * Keycloak is bypassed; roles injected via window.__E2E_ROLES__.
 * Backend API calls intercepted via page.route() targeting http://localhost:8080.
 * No real backend or auth required.
 *
 * Covered scenarios:
 *   1. Load with default filters — renders table rows, immutable notice, default filter values
 *   2. Apply filters — correct API request params sent, table updates with filtered results
 *   3. Clear filters — resets entity_type dropdown, actor input, table reloads all entries
 *   4. Deep-link pre-populates filters — entity_type and entity_id from URL params
 *   5. CSV export triggers download — button click initiates file download
 */

// ── Fixture data ──────────────────────────────────────────────────────────────

const ENTRY_1: AuditLogEntry = {
  id: 'ae-001',
  entity_type: 'booking',
  entity_id: 'bk-abc-0001',
  actor_id: 'user-admin-001',
  action_type: 'booking.created',
  occurred_at: '2026-10-01T09:00:00Z',
  before_values: null,
  after_values: { title: 'Team Meeting' },
  message: 'Booking created',
  data: null,
  idempotency_key: 'idem-001',
};

const ENTRY_2: AuditLogEntry = {
  id: 'ae-002',
  entity_type: 'location',
  entity_id: 'loc-xyz-0002',
  actor_id: 'user-admin-002',
  action_type: 'location.updated',
  occurred_at: '2026-10-02T10:30:00Z',
  before_values: null,
  after_values: { name: 'Conference Room B' },
  message: 'Location updated',
  data: null,
  idempotency_key: 'idem-002',
};

const ENTRY_3: AuditLogEntry = {
  id: 'ae-003',
  entity_type: 'booking',
  entity_id: 'bk-abc-0001',
  actor_id: 'user-approver-001',
  action_type: 'booking.approved',
  occurred_at: '2026-10-03T11:00:00Z',
  before_values: null,
  after_values: { status: 'approved' },
  message: 'Booking approved',
  data: null,
  idempotency_key: 'idem-003',
};

const ENTRY_4: AuditLogEntry = {
  id: 'ae-004',
  entity_type: 'user',
  entity_id: 'user-new-001',
  actor_id: 'user-admin-001',
  action_type: 'user.created',
  occurred_at: '2026-10-04T12:00:00Z',
  before_values: null,
  after_values: null,
  message: 'User created',
  data: null,
  idempotency_key: 'idem-004',
};

const ENTRY_5: AuditLogEntry = {
  id: 'ae-005',
  entity_type: 'resource',
  entity_id: 'res-proj-001',
  actor_id: 'user@example.com',
  action_type: 'resource.deleted',
  occurred_at: '2026-10-05T14:00:00Z',
  before_values: null,
  after_values: null,
  message: 'Resource deleted',
  data: null,
  idempotency_key: 'idem-005',
};

const ENTRY_6: AuditLogEntry = {
  id: 'ae-006',
  entity_type: 'settings',
  entity_id: '00000000-0000-0000-0000-000000000001',
  actor_id: 'user-admin-001',
  action_type: 'settings.updated',
  occurred_at: '2026-10-06T15:00:00Z',
  before_values: null,
  after_values: { approve_booking: true },
  message: 'Settings updated',
  data: null,
  idempotency_key: 'idem-006',
};

const ENTRY_7: AuditLogEntry = {
  id: 'ae-007',
  entity_type: 'booking',
  entity_id: 'bk-abc-0001',
  actor_id: 'user-admin-001',
  action_type: 'booking.deleted',
  occurred_at: '2026-10-07T16:00:00Z',
  before_values: null,
  after_values: null,
  message: 'Booking deleted',
  data: null,
  idempotency_key: 'idem-007',
};

const ENTRY_8: AuditLogEntry = {
  id: 'ae-008',
  entity_type: 'custom_field',
  entity_id: 'cf-001',
  actor_id: 'user-admin-002',
  action_type: 'custom_field.created',
  occurred_at: '2026-10-08T09:00:00Z',
  before_values: null,
  after_values: null,
  message: null,
  data: null,
  idempotency_key: 'idem-008',
};

const ENTRY_9: AuditLogEntry = {
  id: 'ae-009',
  entity_type: 'role',
  entity_id: 'role-001',
  actor_id: 'user-admin-001',
  action_type: 'role.updated',
  occurred_at: '2026-10-09T10:00:00Z',
  before_values: null,
  after_values: null,
  message: null,
  data: null,
  idempotency_key: 'idem-009',
};

const ENTRY_10: AuditLogEntry = {
  id: 'ae-010',
  entity_type: 'booking',
  entity_id: 'bk-abc-0002',
  actor_id: 'user@example.com',
  action_type: 'booking.created',
  occurred_at: '2026-10-10T08:00:00Z',
  before_values: null,
  after_values: null,
  message: null,
  data: null,
  idempotency_key: 'idem-010',
};

const ALL_ENTRIES = [
  ENTRY_1, ENTRY_2, ENTRY_3, ENTRY_4, ENTRY_5,
  ENTRY_6, ENTRY_7, ENTRY_8, ENTRY_9, ENTRY_10,
];

const BOOKING_ENTRIES = [ENTRY_1, ENTRY_3, ENTRY_7, ENTRY_10];

// ── Helpers ───────────────────────────────────────────────────────────────────

const API_BASE = 'http://localhost:8080';

/**
 * Navigate to /admin/audit-log with injected roles and mocked API.
 */
async function gotoAuditLog(
  page: Page,
  options: {
    roles?: string[];
    entries?: AuditLogEntry[];
    filteredEntries?: AuditLogEntry[];
    url?: string;
  } = {}
) {
  const {
    roles = ['role_audit_viewer'],
    entries = ALL_ENTRIES,
    filteredEntries = BOOKING_ENTRIES,
    url = '/admin/audit-log',
  } = options;

  // Inject roles before page load
  await page.addInitScript((injectedRoles: string[]) => {
    (window as unknown as Record<string, unknown>).__E2E_ROLES__ = injectedRoles;
  }, roles);

  // Mock audit-log API: differentiates filtered vs. unfiltered requests
  await page.route(`${API_BASE}/audit-log**`, async (route) => {
    const requestUrl = route.request().url();
    const method = route.request().method();

    if (method !== 'GET') {
      // No mutating methods allowed (immutability check)
      await route.fulfill({ status: 405, contentType: 'application/json', body: '{"error":"Method Not Allowed"}' });
      return;
    }

    // CSV export endpoint — returns a CSV blob
    if (requestUrl.includes('/audit-log/export')) {
      const csv = 'Timestamp,Actor,Entity Type,Entity ID,Action,Message,Data\n"2026-10-01T09:00:00Z","user-admin-001","booking","bk-abc-0001","booking.created","Booking created",""\n';
      await route.fulfill({
        status: 200,
        contentType: 'text/csv',
        headers: { 'Content-Disposition': 'attachment; filename="audit-log-2026-10-15.csv"' },
        body: csv,
      });
      return;
    }

    // Filtered by entity_type=booking — return only booking entries
    if (requestUrl.includes('entity_type=booking')) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(filteredEntries),
      });
      return;
    }

    // Default: return all entries
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(entries),
    });
  });

  await page.goto(url);
  // Wait for the viewer to mount
  await expect(page.getByTestId('audit-log-viewer')).toBeVisible({ timeout: 10000 });
}

// ── Tests ─────────────────────────────────────────────────────────────────────

test.describe('Audit Log Viewer — Load with default filters', () => {
  test('1. Loads with default filters and renders all 10 mock entries', async ({ page }) => {
    await gotoAuditLog(page);

    // Immutable notice is visible
    await expect(page.getByTestId('immutable-notice')).toBeVisible();
    await expect(page.getByTestId('immutable-notice')).toContainText('read-only');

    // Filter controls show with defaults
    await expect(page.getByTestId('audit-log-filters')).toBeVisible();
    const entityTypeSelect = page.getByTestId('filter-entity-type');
    await expect(entityTypeSelect).toBeVisible();
    // Default: "All" (empty string value)
    await expect(entityTypeSelect).toHaveValue('');

    // Actor input is empty by default
    await expect(page.getByTestId('filter-actor')).toHaveValue('');

    // Table renders all 10 entries
    await expect(page.getByTestId('audit-log-table')).toBeVisible({ timeout: 5000 });
    await expect(page.getByTestId('audit-table')).toBeVisible();

    // Verify 10 rows present
    const rows = page.locator('[data-testid^="audit-row-"]');
    await expect(rows).toHaveCount(10);

    // Check some specific rows
    await expect(page.getByTestId(`audit-row-${ENTRY_1.id}`)).toBeVisible();
    await expect(page.getByTestId(`audit-row-${ENTRY_10.id}`)).toBeVisible();

    // Export CSV button present
    await expect(page.getByTestId('btn-export-csv')).toBeVisible();

    // NO edit or delete buttons anywhere in the table (immutability)
    const editBtns = page.locator('[data-testid*="btn-edit-"], [data-testid*="btn-delete-"]');
    await expect(editBtns).toHaveCount(0);
  });
});

test.describe('Audit Log Viewer — Apply filters', () => {
  test('2. Apply entity_type=booking and actor filter, verify API request params', async ({ page }) => {
    await gotoAuditLog(page);
    await expect(page.getByTestId('audit-table')).toBeVisible({ timeout: 5000 });

    // Capture outgoing API requests
    const apiRequests: string[] = [];
    page.on('request', (req) => {
      if (req.url().includes(`${API_BASE}/audit-log`) && req.method() === 'GET') {
        apiRequests.push(req.url());
      }
    });

    // Select entity_type = booking
    await page.getByTestId('filter-entity-type').selectOption('booking');

    // Enter actor search
    await page.getByTestId('filter-actor').fill('user@example.com');

    // Set start_date
    await page.getByTestId('filter-start-date').fill('2026-10-01');

    // Click Apply Filters
    await page.getByTestId('btn-apply-filters').click();

    // Wait briefly for the API call to complete and rows to render
    await page.waitForTimeout(500);

    // Verify at least one API call was made with entity_type=booking
    const filteredRequest = apiRequests.find((r) => r.includes('entity_type=booking'));
    expect(filteredRequest).toBeDefined();

    // Table shows booking entries (4 rows) — wait for table wrapper to appear
    await expect(page.getByTestId('audit-log-table')).toBeVisible({ timeout: 5000 });
    const rows = page.locator('[data-testid^="audit-row-"]');
    await expect(rows).toHaveCount(4, { timeout: 5000 });

    // Verify booking-type entries visible
    await expect(page.getByTestId(`audit-row-${ENTRY_1.id}`)).toBeVisible();
    await expect(page.getByTestId(`audit-row-${ENTRY_3.id}`)).toBeVisible();

    // Non-booking entries no longer in table
    await expect(page.getByTestId(`audit-row-${ENTRY_2.id}`)).toHaveCount(0);
  });
});

test.describe('Audit Log Viewer — Clear filters', () => {
  test('3. Clear filters resets entity_type to All, clears actor, reloads all entries', async ({ page }) => {
    await gotoAuditLog(page);
    await expect(page.getByTestId('audit-table')).toBeVisible({ timeout: 5000 });

    // Apply filters first
    await page.getByTestId('filter-entity-type').selectOption('booking');
    await page.getByTestId('filter-actor').fill('admin@example.com');
    await page.getByTestId('btn-apply-filters').click();

    // Verify filtered state (4 booking entries)
    await expect(page.locator('[data-testid^="audit-row-"]')).toHaveCount(4, { timeout: 5000 });

    // Clear filters
    await page.getByTestId('btn-clear-filters').click();

    // entity_type dropdown reset to "All"
    await expect(page.getByTestId('filter-entity-type')).toHaveValue('');

    // actor input cleared
    await expect(page.getByTestId('filter-actor')).toHaveValue('');

    // Table reloads all 10 entries
    await expect(page.locator('[data-testid^="audit-row-"]')).toHaveCount(10, { timeout: 5000 });
  });
});

test.describe('Audit Log Viewer — Deep-link pre-populates filters', () => {
  test('4. URL params ?entity_type=booking&entity_id=bk-abc-0001 pre-populate filters', async ({
    page,
  }) => {
    // Navigate with deep-link URL params (as if linked from Booking Detail Modal)
    await gotoAuditLog(page, {
      url: '/admin/audit-log?entity_type=booking&entity_id=bk-abc-0001',
      // The mock will return all entries for this entity_id filter;
      // deep-link also sends entity_type=booking so we get filtered results
    });

    await expect(page.getByTestId('audit-log-viewer')).toBeVisible({ timeout: 10000 });

    // entity_type filter pre-selected to "booking"
    await expect(page.getByTestId('filter-entity-type')).toHaveValue('booking');

    // The table renders (deep-linked filter applied on mount → 4 booking entries)
    await expect(page.getByTestId('audit-log-table')).toBeVisible({ timeout: 5000 });

    // Verify the page loaded with deep-link filters applied
    const rows = page.locator('[data-testid^="audit-row-"]');
    // Expect 4 booking entries (the mock returns BOOKING_ENTRIES when entity_type=booking)
    await expect(rows).toHaveCount(4);
  });
});

test.describe('Audit Log Viewer — CSV export', () => {
  test('5. Export to CSV button triggers download', async ({ page }) => {
    await gotoAuditLog(page);
    await expect(page.getByTestId('audit-table')).toBeVisible({ timeout: 5000 });

    // Wait for entries to load so Export button is enabled
    await expect(page.locator('[data-testid^="audit-row-"]')).toHaveCount(10, { timeout: 5000 });

    // Set up download listener before clicking
    const [download] = await Promise.all([
      page.waitForEvent('download', { timeout: 10000 }),
      page.getByTestId('btn-export-csv').click(),
    ]);

    // Verify a download was initiated
    expect(download).toBeDefined();

    // Filename matches pattern audit-log-*.csv
    expect(download.suggestedFilename()).toMatch(/^audit-log-\d{4}-\d{2}-\d{2}\.csv$/);
  });
});
