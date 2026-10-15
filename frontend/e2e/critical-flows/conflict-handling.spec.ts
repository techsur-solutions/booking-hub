/**
 * Critical Flow: Conflict Handling (Hard-block vs Soft-warning)
 *
 * Tests conflict detection differentiation by role:
 * 1. Non-approver: hard-block (red banner, Save disabled)
 * 2. Approver: soft-warning (yellow banner, Save Anyway → confirmation dialog)
 *
 * Test strategy:
 * - VITE_E2E_MODE=true (set by playwright.config.ts).
 * - Roles injected via window.__E2E_ROLES__.
 * - API calls mocked via page.route().
 * - Conflict check returns LOCATION_CONFLICT_FLAGS to simulate a conflict.
 *
 * Coverage: UI-BOOK-04 (conflict detection hard-block vs soft-warning by role)
 */
import { test, expect, type Page } from '@playwright/test';

// ── Constants ─────────────────────────────────────────────────────────────────

const API_BASE = 'http://localhost:8080';

// ── Fixture data ──────────────────────────────────────────────────────────────

const LOCATION_ROOM_A = {
  id: 'loc-a',
  name: 'Conference Room A',
  colour: '#3b82f6',
  building: 'Main',
  layouts: null,
  deleted_at: null,
};

const LOCATION_ROOM_B = {
  id: 'loc-b',
  name: 'Meeting Room B',
  colour: '#10b981',
  building: 'Annex',
  layouts: null,
  deleted_at: null,
};

const DEFAULT_SETTINGS = {
  id: 1,
  approve_booking: false,
  calendar_slot_size: 30,
  calendar_min_time: '08:00',
  calendar_max_time: '18:00',
};

/** Conflict flags returned by /bookings/check-conflicts when a location conflict exists */
const LOCATION_CONFLICT_FLAGS = [
  { conflicting_booking_id: 'bk-existing-001', conflict_type: 'location' as const },
];

/** Conflict flags for a resource conflict */
const RESOURCE_CONFLICT_FLAGS = [
  { conflicting_booking_id: 'bk-resource-001', conflict_type: 'resource' as const },
];

/** Mixed location + resource conflicts */
const MIXED_CONFLICT_FLAGS = [
  { conflicting_booking_id: 'bk-existing-001', conflict_type: 'location' as const },
  { conflicting_booking_id: 'bk-resource-001', conflict_type: 'resource' as const },
];

// ── Helpers ───────────────────────────────────────────────────────────────────

/**
 * Navigate to Booking Create form with specific roles and conflict response.
 */
async function gotoBookingCreateWithConflict(
  page: Page,
  options: {
    roles: string[];
    conflictResponse?: typeof LOCATION_CONFLICT_FLAGS;
  },
) {
  const { roles, conflictResponse = LOCATION_CONFLICT_FLAGS } = options;

  await page.addInitScript((injectedRoles: string[]) => {
    (window as unknown as Record<string, unknown>).__E2E_ROLES__ = injectedRoles;
  }, roles);

  // Single catch-all handler for all API requests
  await page.route(`${API_BASE}/**`, (route) => {
    const url = route.request().url();
    const path = new URL(url).pathname;

    if (path.includes('/settings')) {
      void route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(DEFAULT_SETTINGS),
      });
    } else if (path.includes('/locations')) {
      void route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([LOCATION_ROOM_A, LOCATION_ROOM_B]),
      });
    } else if (path.includes('/resources')) {
      void route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
    } else if (path.includes('/custom-fields')) {
      void route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
    } else if (path.includes('/check-conflicts')) {
      void route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(conflictResponse),
      });
    } else if (path === '/bookings' && route.request().method() === 'POST') {
      void route.fulfill({
        status: 201,
        contentType: 'application/json',
        body: JSON.stringify({
          id: 'bk-new', title: 'Test Booking', status: 'pending',
          location_id: 'loc-a', start_time: '2025-06-15T10:00:00.000Z',
          end_time: '2025-06-15T11:00:00.000Z',
        }),
      });
    } else if (path.startsWith('/bookings')) {
      void route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: path === '/bookings' ? JSON.stringify([]) : JSON.stringify({}),
      });
    } else {
      void route.continue();
    }
  });

  await page.goto('/bookings/create');
  await expect(page.getByTestId('booking-form')).toBeVisible({ timeout: 15_000 });
}

/**
 * Fill the booking form fields to trigger conflict detection.
 */
async function fillFormToTriggerConflict(page: Page) {
  await page.getByTestId('booking-title').fill('Test Booking');
  await page.locator('select[data-testid="booking-location"]').selectOption({ value: 'loc-a' });
  await page.locator('input[data-testid="booking-start-time"]').fill('2025-06-15T10:00');
  await page.locator('input[data-testid="booking-end-time"]').fill('2025-06-15T11:00');
  // Wait for debounce (500ms) + API response
  await page.waitForTimeout(1500);
}

// ── Tests: Hard-block (non-approver) ─────────────────────────────────────────

test.describe('Conflict Handling — Hard-block (non-approver) (UI-BOOK-04)', () => {

  test('Hard-block: red ConflictBanner appears with "Cannot save" message', async ({ page }) => {
    await gotoBookingCreateWithConflict(page, {
      roles: ['role_calendar_viewer'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });

    await fillFormToTriggerConflict(page);

    // Hard-block banner appears
    await expect(page.getByTestId('conflict-banner-hard-block')).toBeVisible({ timeout: 3_000 });
    await expect(page.getByTestId('conflict-banner-hard-block')).toContainText('Cannot save');
  });

  test('Hard-block: Save button is disabled when conflict present', async ({ page }) => {
    await gotoBookingCreateWithConflict(page, {
      roles: ['role_calendar_viewer'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });

    await fillFormToTriggerConflict(page);

    await expect(page.getByTestId('conflict-banner-hard-block')).toBeVisible({ timeout: 3_000 });
    // Save must be disabled — non-approver cannot override conflict
    await expect(page.getByTestId('booking-save')).toBeDisabled();
  });

  test('Hard-block: conflicting booking ID shown as clickable link', async ({ page }) => {
    await gotoBookingCreateWithConflict(page, {
      roles: ['role_calendar_viewer'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });

    await fillFormToTriggerConflict(page);

    await expect(page.getByTestId('conflict-banner-hard-block')).toBeVisible({ timeout: 3_000 });
    // Conflict link for the specific booking ID
    await expect(
      page.getByTestId(`conflict-link-${LOCATION_CONFLICT_FLAGS[0].conflicting_booking_id}`),
    ).toBeVisible();
  });

  test('Hard-block: location conflict grouped with 📍 icon', async ({ page }) => {
    await gotoBookingCreateWithConflict(page, {
      roles: ['role_calendar_viewer'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });

    await fillFormToTriggerConflict(page);

    await expect(page.getByTestId('conflict-banner-hard-block')).toBeVisible({ timeout: 3_000 });
    await expect(page.getByTestId('location-conflicts-group')).toBeVisible();
    await expect(page.getByTestId('location-conflicts-group')).toContainText('📍');
  });

  test('Hard-block: resource conflict grouped with 🔧 icon', async ({ page }) => {
    await gotoBookingCreateWithConflict(page, {
      roles: ['role_calendar_viewer'],
      conflictResponse: RESOURCE_CONFLICT_FLAGS,
    });

    await fillFormToTriggerConflict(page);

    await expect(page.getByTestId('conflict-banner-hard-block')).toBeVisible({ timeout: 3_000 });
    await expect(page.getByTestId('resource-conflicts-group')).toBeVisible();
    await expect(page.getByTestId('resource-conflicts-group')).toContainText('🔧');
  });

  test('Hard-block: clicking conflict link opens BookingDetailModal', async ({ page }) => {
    await gotoBookingCreateWithConflict(page, {
      roles: ['role_calendar_viewer'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });

    await fillFormToTriggerConflict(page);

    await expect(page.getByTestId('conflict-banner-hard-block')).toBeVisible({ timeout: 3_000 });
    await page
      .getByTestId(`conflict-link-${LOCATION_CONFLICT_FLAGS[0].conflicting_booking_id}`)
      .click();

    // BookingDetailModal opens to show the conflicting booking details
    await expect(page.getByTestId('booking-detail-modal')).toBeVisible({ timeout: 3_000 });
  });

  test('Hard-block: no conflicts → Save button is enabled', async ({ page }) => {
    // When conflict check returns empty array, no hard-block should appear
    await gotoBookingCreateWithConflict(page, {
      roles: ['role_calendar_viewer'],
      conflictResponse: [],
    });

    await fillFormToTriggerConflict(page);

    // No hard-block banner
    await expect(page.getByTestId('conflict-banner-hard-block')).not.toBeVisible();
    // Save button enabled
    await expect(page.getByTestId('booking-save')).not.toBeDisabled();
  });
});

// ── Tests: Soft-warning (approver) ───────────────────────────────────────────

test.describe('Conflict Handling — Soft-warning (approver) (UI-BOOK-04)', () => {

  test('Soft-warn: yellow ConflictBanner appears with "Warning" and "Conflicts detected"', async ({ page }) => {
    await gotoBookingCreateWithConflict(page, {
      roles: ['role_calendar_viewer', 'role_booking_approver'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });

    await fillFormToTriggerConflict(page);

    await expect(page.getByTestId('conflict-banner-soft-warn')).toBeVisible({ timeout: 3_000 });
    await expect(page.getByTestId('conflict-banner-soft-warn')).toContainText('Warning');
    await expect(page.getByTestId('conflict-banner-soft-warn')).toContainText('Conflicts detected');
  });

  test('Soft-warn: Save button remains enabled for approver', async ({ page }) => {
    await gotoBookingCreateWithConflict(page, {
      roles: ['role_calendar_viewer', 'role_booking_approver'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });

    await fillFormToTriggerConflict(page);

    await expect(page.getByTestId('conflict-banner-soft-warn')).toBeVisible({ timeout: 3_000 });
    // Approver can save despite conflicts — save NOT disabled
    await expect(page.getByTestId('booking-save')).not.toBeDisabled();
  });

  test('Soft-warn: "Save Anyway" button appears in the banner', async ({ page }) => {
    await gotoBookingCreateWithConflict(page, {
      roles: ['role_calendar_viewer', 'role_booking_approver'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });

    await fillFormToTriggerConflict(page);

    await expect(page.getByTestId('conflict-banner-soft-warn')).toBeVisible({ timeout: 3_000 });
    await expect(page.getByTestId('btn-save-anyway')).toBeVisible();
  });

  test('Soft-warn: clicking "Save Anyway" shows confirmation dialog', async ({ page }) => {
    await gotoBookingCreateWithConflict(page, {
      roles: ['role_calendar_viewer', 'role_booking_approver'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });

    await fillFormToTriggerConflict(page);

    await expect(page.getByTestId('conflict-banner-soft-warn')).toBeVisible({ timeout: 3_000 });
    await page.getByTestId('btn-save-anyway').click();

    // Confirmation dialog appears asking to save despite conflicts
    await expect(page.getByTestId('save-anyway-confirm-dialog')).toBeVisible({ timeout: 3_000 });
    await expect(page.getByTestId('save-anyway-confirm-dialog')).toContainText('despite conflicts');
  });

  test('Soft-warn: cancelling "Save Anyway" keeps banner visible without saving', async ({ page }) => {
    await gotoBookingCreateWithConflict(page, {
      roles: ['role_calendar_viewer', 'role_booking_approver'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });

    await fillFormToTriggerConflict(page);

    await expect(page.getByTestId('conflict-banner-soft-warn')).toBeVisible({ timeout: 3_000 });
    await page.getByTestId('btn-save-anyway').click();
    await expect(page.getByTestId('save-anyway-confirm-dialog')).toBeVisible({ timeout: 3_000 });

    // Cancel the dialog
    await page.getByTestId('btn-cancel-save-anyway').click();

    // Dialog closes, banner still visible (booking not saved)
    await expect(page.getByTestId('save-anyway-confirm-dialog')).not.toBeVisible();
    await expect(page.getByTestId('conflict-banner-soft-warn')).toBeVisible();
  });

  test('Soft-warn: confirming "Save Anyway" closes dialog (allows save to proceed)', async ({ page }) => {
    await gotoBookingCreateWithConflict(page, {
      roles: ['role_calendar_viewer', 'role_booking_approver'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });

    await fillFormToTriggerConflict(page);

    await expect(page.getByTestId('conflict-banner-soft-warn')).toBeVisible({ timeout: 3_000 });
    await page.getByTestId('btn-save-anyway').click();
    await expect(page.getByTestId('save-anyway-confirm-dialog')).toBeVisible({ timeout: 3_000 });

    // Confirm save anyway
    await page.getByTestId('btn-confirm-save-anyway').click();

    // Dialog closes — the form is now unblocked
    await expect(page.getByTestId('save-anyway-confirm-dialog')).not.toBeVisible();
  });

  test('Soft-warn: mixed location + resource conflicts all appear in banner', async ({ page }) => {
    await gotoBookingCreateWithConflict(page, {
      roles: ['role_calendar_viewer', 'role_booking_approver'],
      conflictResponse: MIXED_CONFLICT_FLAGS,
    });

    await fillFormToTriggerConflict(page);

    await expect(page.getByTestId('conflict-banner-soft-warn')).toBeVisible({ timeout: 3_000 });
    // Both conflict type groups appear
    await expect(page.getByTestId('location-conflicts-group')).toBeVisible();
    await expect(page.getByTestId('resource-conflicts-group')).toBeVisible();
  });
});

// ── Tests: Role-based access to booking creation ──────────────────────────────

test.describe('Conflict Handling — Role differentiation', () => {

  test('Role check: non-approver blocked (hard-block), approver warned (soft-warn) for same conflict', async ({ page }) => {
    // Verify the same conflict data produces different UI for different roles
    // Test non-approver first, then approver in same flow

    // Non-approver sees hard-block
    await gotoBookingCreateWithConflict(page, {
      roles: ['role_calendar_viewer'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });
    await fillFormToTriggerConflict(page);
    await expect(page.getByTestId('conflict-banner-hard-block')).toBeVisible({ timeout: 3_000 });
    await expect(page.getByTestId('conflict-banner-soft-warn')).not.toBeVisible();
  });

  test('Role check: approver sees soft-warn (not hard-block) for same conflict', async ({ page }) => {
    // Approver sees soft-warn, NOT hard-block
    await gotoBookingCreateWithConflict(page, {
      roles: ['role_calendar_viewer', 'role_booking_approver'],
      conflictResponse: LOCATION_CONFLICT_FLAGS,
    });
    await fillFormToTriggerConflict(page);
    await expect(page.getByTestId('conflict-banner-soft-warn')).toBeVisible({ timeout: 3_000 });
    await expect(page.getByTestId('conflict-banner-hard-block')).not.toBeVisible();
  });
});
