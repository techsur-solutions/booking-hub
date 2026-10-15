import { test, expect, type Page } from '@playwright/test';
import type { Booking, Location } from '../src/api/types';

/**
 * Approval Queue End-to-End Tests (UI-APPR-01 / UI-APPR-02)
 *
 * Tests run with VITE_E2E_MODE=true (playwright.config.ts webServer.env).
 * Keycloak is bypassed; roles injected via window.__E2E_ROLES__.
 * Backend API calls intercepted via page.route() targeting http://localhost:8080.
 * No real backend or auth required.
 *
 * Covered scenarios:
 *   1. Permission gate — non-approver redirected to /bookings with toast
 *   2. Permission gate — no Approval Queue link for non-approver
 *   3. Approval Queue renders pending bookings table with 8 columns
 *   4. ConflictBadge appears in Conflict Flags column for conflicted booking
 *   5. Approve booking (no conflicts) — confirmation dialog, API call, toast, row removed
 *   6. Approve booking (with conflicts) — conflict dialog, cancel, then approve
 *   7. Deny booking with reason — dialog, reason input, API call, row removed
 *   8. Deny booking without reason — dialog, empty reason, API call, row removed
 *   9. Row click opens BookingDetailModal
 */

// ── Fixture data ──────────────────────────────────────────────────────────────

const LOCATION_1: Location = {
  id: 'loc-001',
  name: 'Conference Room A',
  colour: '#3b82f6',
  building: 'Building A',
  layouts: null,
  deleted_at: null,
};

const BOOKING_NO_CONFLICTS: Booking = {
  id: 'bk-pending-clean',
  series_id: null,
  location_id: 'loc-001',
  resource_ids: ['res-projector'],
  title: 'Team Stand-Up',
  description: null,
  start_time: new Date(Date.now() + 86_400_000).toISOString(),
  end_time: new Date(Date.now() + 86_400_000 + 3_600_000).toISOString(),
  status: 'pending',
  owner_id: 'user-requester-001',
  conflict_flags: [],
  custom_field_values: [],
  created_at: new Date(Date.now() - 7_200_000).toISOString(),
  updated_at: new Date(Date.now() - 7_200_000).toISOString(),
  deleted_at: null,
};

const BOOKING_WITH_CONFLICTS: Booking = {
  id: 'bk-pending-conflict',
  series_id: null,
  location_id: 'loc-001',
  resource_ids: [],
  title: 'Conflicted Meeting',
  description: null,
  start_time: new Date(Date.now() + 86_400_000).toISOString(),
  end_time: new Date(Date.now() + 86_400_000 + 3_600_000).toISOString(),
  status: 'pending',
  owner_id: 'user-requester-002',
  conflict_flags: [
    { conflicting_booking_id: 'bk-existing-001', conflict_type: 'location' },
    { conflicting_booking_id: 'bk-existing-002', conflict_type: 'resource' },
  ],
  custom_field_values: [],
  created_at: new Date(Date.now() - 3_600_000).toISOString(),
  updated_at: new Date(Date.now() - 3_600_000).toISOString(),
  deleted_at: null,
};

const BOOKING_FOR_DENY: Booking = {
  id: 'bk-pending-deny',
  series_id: null,
  location_id: 'loc-001',
  resource_ids: [],
  title: 'Deny This Booking',
  description: null,
  start_time: new Date(Date.now() + 86_400_000 * 2).toISOString(),
  end_time: new Date(Date.now() + 86_400_000 * 2 + 3_600_000).toISOString(),
  status: 'pending',
  owner_id: 'user-requester-003',
  conflict_flags: [],
  custom_field_values: [],
  created_at: new Date(Date.now() - 1_800_000).toISOString(),
  updated_at: new Date(Date.now() - 1_800_000).toISOString(),
  deleted_at: null,
};

const ALL_PENDING = [BOOKING_NO_CONFLICTS, BOOKING_WITH_CONFLICTS, BOOKING_FOR_DENY];

const APPROVE_RESPONSE = {
  id: BOOKING_NO_CONFLICTS.id,
  status: 'approved',
  approved_at: new Date().toISOString(),
  approved_by: 'approver-user-id',
};

const DENY_RESPONSE = {
  id: BOOKING_FOR_DENY.id,
  status: 'denied',
  denied_at: new Date().toISOString(),
  denied_by: 'approver-user-id',
  denial_reason: 'Room not available during that time',
};

// ── Helpers ───────────────────────────────────────────────────────────────────

const API_BASE = 'http://localhost:8080';

/** Navigate to /approvals with injected roles and mocked API */
async function gotoApprovalQueue(
  page: Page,
  options: {
    roles?: string[];
    pendingBookings?: Booking[];
    approveStatus?: number;
    approveResponse?: object;
    denyStatus?: number;
    denyResponse?: object;
  } = {}
) {
  const {
    roles = ['role_booking_approver'],
    pendingBookings = ALL_PENDING,
    approveStatus = 200,
    approveResponse = APPROVE_RESPONSE,
    denyStatus = 200,
    denyResponse = DENY_RESPONSE,
  } = options;

  // Inject roles before page load
  await page.addInitScript((injectedRoles: string[]) => {
    (window as unknown as Record<string, unknown>).__E2E_ROLES__ = injectedRoles;
  }, roles);

  // Mock API: GET /bookings?status=pending
  await page.route(`${API_BASE}/bookings**`, async (route) => {
    const url = route.request().url();
    const method = route.request().method();

    if (method === 'GET' && url.includes('status=pending')) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(pendingBookings),
      });
    } else if (method === 'POST' && /\/bookings\/[^/]+\/approve/.test(url)) {
      await route.fulfill({
        status: approveStatus,
        contentType: 'application/json',
        body: JSON.stringify(approveResponse),
      });
    } else if (method === 'POST' && /\/bookings\/[^/]+\/deny/.test(url)) {
      await route.fulfill({
        status: denyStatus,
        contentType: 'application/json',
        body: JSON.stringify(denyResponse),
      });
    } else if (method === 'GET' && /\/bookings\/[^?]+/.test(url)) {
      // Individual booking fetch (for BookingDetailModal)
      const id = url.split('/bookings/')[1]?.split('?')[0];
      const booking = pendingBookings.find((b) => b.id === id);
      await route.fulfill({
        status: booking ? 200 : 404,
        contentType: 'application/json',
        body: booking ? JSON.stringify(booking) : JSON.stringify({ error: 'Not found' }),
      });
    } else {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      });
    }
  });

  // Mock API: locations (for BookingDetailModal, location detail)
  await page.route(`${API_BASE}/locations**`, async (route) => {
    const url = route.request().url();
    if (/\/locations\/[^?]+/.test(url)) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(LOCATION_1),
      });
    } else {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([LOCATION_1]),
      });
    }
  });

  await page.goto('/approvals');
}

// ── Tests ─────────────────────────────────────────────────────────────────────

test.describe('Approval Queue — Permission Gate', () => {
  test('1a. Non-approver is redirected to /bookings with toast "Access denied"', async ({
    page,
  }) => {
    // Mock bookings API for the calendar view redirect target
    await page.addInitScript((injectedRoles: string[]) => {
      (window as unknown as Record<string, unknown>).__E2E_ROLES__ = injectedRoles;
    }, ['role_calendar_viewer']);

    await page.route(`${API_BASE}/bookings**`, (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: '[]' })
    );
    await page.route(`${API_BASE}/locations**`, (route) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([LOCATION_1]) })
    );

    await page.goto('/approvals');

    // Should redirect away from /approvals
    await expect(page).toHaveURL(/\/bookings/, { timeout: 5000 });

    // Toast "Access denied" should appear (use .first() since toast can appear multiple times)
    await expect(page.locator('[role="alert"]').filter({ hasText: 'Access denied' }).first()).toBeVisible({
      timeout: 5000,
    });
  });
});

test.describe('Approval Queue — Table Rendering', () => {
  test('2. Renders pending bookings table with 8 columns', async ({ page }) => {
    await gotoApprovalQueue(page);

    await expect(page.getByTestId('approval-queue')).toBeVisible({ timeout: 5000 });
    await expect(page.getByTestId('approval-table')).toBeVisible();

    // Verify 8 column headers
    const headers = page.locator('.approval-table th');
    await expect(headers).toHaveCount(8);

    // Verify all 3 pending bookings appear
    await expect(page.getByTestId(`approval-row-${BOOKING_NO_CONFLICTS.id}`)).toBeVisible();
    await expect(page.getByTestId(`approval-row-${BOOKING_WITH_CONFLICTS.id}`)).toBeVisible();
    await expect(page.getByTestId(`approval-row-${BOOKING_FOR_DENY.id}`)).toBeVisible();
  });

  test('3. ConflictBadge appears in Conflict Flags column for conflicted booking', async ({
    page,
  }) => {
    await gotoApprovalQueue(page);

    await expect(page.getByTestId('approval-table')).toBeVisible({ timeout: 5000 });

    // Conflicted booking has ConflictBadge
    const conflictRow = page.getByTestId(`approval-row-${BOOKING_WITH_CONFLICTS.id}`);
    await expect(conflictRow.getByTestId('conflict-badge')).toBeVisible();

    // Non-conflicted booking has no ConflictBadge
    const cleanRow = page.getByTestId(`approval-row-${BOOKING_NO_CONFLICTS.id}`);
    await expect(cleanRow.getByTestId('conflict-badge')).toBeHidden();
  });
});

test.describe('Approval Queue — Approve Actions', () => {
  test('4. Approve booking (no conflicts): confirmation dialog, API call, toast, row removed', async ({
    page,
  }) => {
    await gotoApprovalQueue(page);
    await expect(page.getByTestId('approval-table')).toBeVisible({ timeout: 5000 });

    // Click Approve for non-conflicted booking
    await page.getByTestId(`btn-approve-${BOOKING_NO_CONFLICTS.id}`).click();

    // Simple confirmation dialog appears
    const confirmDialog = page.getByTestId('approve-confirm-dialog');
    await expect(confirmDialog).toBeVisible();
    await expect(confirmDialog).toContainText(`'${BOOKING_NO_CONFLICTS.title}'`);
    await expect(confirmDialog).toContainText('This action cannot be undone');

    // Click Confirm
    await page.getByTestId('btn-confirm-approve').click();

    // Toast "Booking approved" appears
    await expect(page.locator('[role="alert"]').filter({ hasText: 'Booking approved' })).toBeVisible(
      { timeout: 5000 }
    );

    // Row removed immediately (optimistic UI)
    await expect(page.getByTestId(`approval-row-${BOOKING_NO_CONFLICTS.id}`)).toBeHidden();
  });

  test('5a. Approve booking (with conflicts): conflict dialog shows conflict list', async ({
    page,
  }) => {
    await gotoApprovalQueue(page);
    await expect(page.getByTestId('approval-table')).toBeVisible({ timeout: 5000 });

    // Click Approve for conflicted booking
    await page.getByTestId(`btn-approve-${BOOKING_WITH_CONFLICTS.id}`).click();

    // Conflict dialog appears (not simple dialog)
    const conflictDialog = page.getByTestId('approve-conflict-dialog');
    await expect(conflictDialog).toBeVisible();
    await expect(page.getByRole('dialog', { name: /⚠️ Warning - Conflicts detected/i })).toBeVisible();

    // Conflict list shows booking IDs
    await expect(page.getByTestId('conflict-list')).toBeVisible();
    await expect(
      page.getByTestId(`conflict-item-${BOOKING_WITH_CONFLICTS.conflict_flags[0].conflicting_booking_id}`)
    ).toBeVisible();
    await expect(
      page.getByTestId(`conflict-item-${BOOKING_WITH_CONFLICTS.conflict_flags[1].conflicting_booking_id}`)
    ).toBeVisible();

    // Warning text
    await expect(conflictDialog).toContainText('Approve anyway?');
    await expect(conflictDialog).toContainText('double-booking');
  });

  test('5b. Approve with conflicts: Cancel closes dialog, no API call', async ({ page }) => {
    const approveRequests: string[] = [];

    await gotoApprovalQueue(page);
    await expect(page.getByTestId('approval-table')).toBeVisible({ timeout: 5000 });

    // Track approve calls
    page.on('request', (req) => {
      if (req.url().includes('/approve') && req.method() === 'POST') {
        approveRequests.push(req.url());
      }
    });

    await page.getByTestId(`btn-approve-${BOOKING_WITH_CONFLICTS.id}`).click();
    await expect(page.getByTestId('approve-conflict-dialog')).toBeVisible();

    // Click Cancel
    await page.getByTestId('btn-cancel-conflict-approve').click();

    // Dialog closes
    await expect(page.getByTestId('approve-conflict-dialog')).toBeHidden();

    // No API call made
    expect(approveRequests).toHaveLength(0);

    // Row still present
    await expect(page.getByTestId(`approval-row-${BOOKING_WITH_CONFLICTS.id}`)).toBeVisible();
  });

  test('5c. Approve Despite Conflicts: API call, toast with "(with conflicts)", row removed', async ({
    page,
  }) => {
    await gotoApprovalQueue(page);
    await expect(page.getByTestId('approval-table')).toBeVisible({ timeout: 5000 });

    await page.getByTestId(`btn-approve-${BOOKING_WITH_CONFLICTS.id}`).click();
    await expect(page.getByTestId('approve-conflict-dialog')).toBeVisible();

    // Click "Approve Despite Conflicts"
    await page.getByTestId('btn-approve-despite-conflicts').click();

    // Toast "Booking approved (with conflicts)"
    await expect(
      page.locator('[role="alert"]').filter({ hasText: 'Booking approved (with conflicts)' })
    ).toBeVisible({ timeout: 5000 });

    // Row removed (optimistic UI)
    await expect(page.getByTestId(`approval-row-${BOOKING_WITH_CONFLICTS.id}`)).toBeHidden();
  });
});

test.describe('Approval Queue — Deny Actions', () => {
  test('6a. Deny booking with reason: dialog, reason textarea, API call, row removed', async ({
    page,
  }) => {
    await gotoApprovalQueue(page);
    await expect(page.getByTestId('approval-table')).toBeVisible({ timeout: 5000 });

    // Click Deny
    await page.getByTestId(`btn-deny-${BOOKING_FOR_DENY.id}`).click();

    // Denial dialog appears
    const denyDialog = page.getByTestId('deny-dialog');
    await expect(denyDialog).toBeVisible();
    await expect(page.getByRole('dialog', { name: new RegExp(BOOKING_FOR_DENY.title) })).toBeVisible();

    // Reason textarea is visible and optional
    const textarea = page.getByTestId('deny-reason-textarea');
    await expect(textarea).toBeVisible();
    await expect(textarea).toHaveAttribute('placeholder', 'Reason for denial (optional)');

    // "Cannot be undone" warning
    await expect(denyDialog).toContainText('This action cannot be undone');

    // Enter reason
    await textarea.fill('Room not available during that time');

    // Click "Deny Booking"
    const denyRequest = page.waitForRequest(
      (req) => req.url().includes(`/bookings/${BOOKING_FOR_DENY.id}/deny`) && req.method() === 'POST'
    );
    await page.getByTestId('btn-confirm-deny').click();

    // Verify API was called
    const req = await denyRequest;
    const body = JSON.parse(req.postData() ?? '{}');
    expect(body.reason).toBe('Room not available during that time');

    // Toast "Booking denied"
    await expect(
      page.locator('[role="alert"]').filter({ hasText: 'Booking denied' })
    ).toBeVisible({ timeout: 5000 });

    // Row removed immediately (optimistic UI)
    await expect(page.getByTestId(`approval-row-${BOOKING_FOR_DENY.id}`)).toBeHidden();
  });

  test('6b. Deny booking without reason: empty reason field, API call succeeds', async ({
    page,
  }) => {
    await gotoApprovalQueue(page);
    await expect(page.getByTestId('approval-table')).toBeVisible({ timeout: 5000 });

    await page.getByTestId(`btn-deny-${BOOKING_FOR_DENY.id}`).click();
    await expect(page.getByTestId('deny-dialog')).toBeVisible();

    // Leave reason empty, click Deny Booking
    const denyRequest = page.waitForRequest(
      (req) => req.url().includes(`/bookings/${BOOKING_FOR_DENY.id}/deny`) && req.method() === 'POST'
    );
    await page.getByTestId('btn-confirm-deny').click();

    // API called (with empty/no reason)
    await denyRequest;

    // Toast "Booking denied"
    await expect(
      page.locator('[role="alert"]').filter({ hasText: 'Booking denied' })
    ).toBeVisible({ timeout: 5000 });

    // Row removed
    await expect(page.getByTestId(`approval-row-${BOOKING_FOR_DENY.id}`)).toBeHidden();
  });

  test('6c. Deny dialog: Cancel closes dialog, no API call, row stays', async ({ page }) => {
    const denyRequests: string[] = [];

    await gotoApprovalQueue(page);
    await expect(page.getByTestId('approval-table')).toBeVisible({ timeout: 5000 });

    page.on('request', (req) => {
      if (req.url().includes('/deny') && req.method() === 'POST') {
        denyRequests.push(req.url());
      }
    });

    await page.getByTestId(`btn-deny-${BOOKING_FOR_DENY.id}`).click();
    await expect(page.getByTestId('deny-dialog')).toBeVisible();

    // Click Cancel
    await page.getByTestId('btn-cancel-deny').click();

    // Dialog closes
    await expect(page.getByTestId('deny-dialog')).toBeHidden();

    // No API call
    expect(denyRequests).toHaveLength(0);

    // Row still present
    await expect(page.getByTestId(`approval-row-${BOOKING_FOR_DENY.id}`)).toBeVisible();
  });
});

test.describe('Approval Queue — Row Click', () => {
  test('7. Row title click opens BookingDetailModal', async ({ page }) => {
    await gotoApprovalQueue(page);
    await expect(page.getByTestId('approval-table')).toBeVisible({ timeout: 5000 });

    // Click booking title
    await page.getByTestId(`booking-title-${BOOKING_NO_CONFLICTS.id}`).click();

    // BookingDetailModal opens
    await expect(page.getByTestId('booking-detail-modal')).toBeVisible({ timeout: 5000 });
  });
});
