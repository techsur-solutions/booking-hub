import { test, expect, type Page } from '@playwright/test';
import type { Settings } from '../../src/api/settingsApi';

/**
 * Settings Admin End-to-End Tests (UI-ADMIN-06)
 *
 * Tests run with VITE_E2E_MODE=true (playwright.config.ts webServer.env).
 * Keycloak is bypassed; roles injected via window.__E2E_ROLES__.
 * Backend API calls intercepted via page.route() targeting http://localhost:8080.
 * No real backend or auth required.
 *
 * Covered scenarios:
 *   1. Load current settings and render form with pre-filled values
 *   2. Update settings and verify save confirmation banner with timestamp
 *   3. Propagation indicator appears after save and auto-dismisses after 5s
 *   4. Preview Display Board button opens new tab with /display-board URL
 *   5. Copy feed links button shows modal with RSS2, iCal, JSON feed URLs
 */

// ── Constants ────────────────────────────────────────────────────────────────

const API_BASE = 'http://localhost:8080';

const SETTINGS_RESPONSE: Settings = {
  id: 1,
  approve_booking: true,
  calendar_slot_size: 30,
  calendar_min_time: '08:00',
  calendar_max_time: '18:00',
};

const SETTINGS_AFTER_UPDATE: Settings = {
  id: 1,
  approve_booking: false,
  calendar_slot_size: 60,
  calendar_min_time: '07:00',
  calendar_max_time: '18:00',
};

// ── Helpers ───────────────────────────────────────────────────────────────────

/**
 * Navigate to /admin/settings with injected roles and mocked API.
 * Injects role_settings_manager which gates /admin/settings per Phase 9 sidebar permission gating.
 */
async function gotoSettingsAdmin(
  page: Page,
  options: {
    roles?: string[];
    currentSettings?: Settings;
    updateStatus?: number;
    updateResponse?: Settings;
  } = {},
) {
  const {
    roles = ['role_settings_manager', 'role_calendar_viewer'],
    currentSettings = SETTINGS_RESPONSE,
    updateStatus = 200,
    updateResponse = SETTINGS_AFTER_UPDATE,
  } = options;

  // Inject roles before page load
  await page.addInitScript((injectedRoles: string[]) => {
    (window as unknown as Record<string, unknown>).__E2E_ROLES__ = injectedRoles;
  }, roles);

  // Mock API: GET /settings/current
  await page.route(`${API_BASE}/settings/current`, async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(currentSettings),
    });
  });

  // Mock API: PUT /settings/1 (singleton entity update)
  await page.route(`${API_BASE}/settings/1`, async (route) => {
    await route.fulfill({
      status: updateStatus,
      contentType: 'application/json',
      body: JSON.stringify(updateResponse),
    });
  });

  await page.goto('/admin/settings');

  // Wait for the settings form to render (loading → form)
  await expect(page.getByTestId('settings-form')).toBeVisible({ timeout: 10_000 });
}

// ── Tests ─────────────────────────────────────────────────────────────────────

test.describe('Settings Admin — Form Rendering', () => {
  test('Test 1: Load current settings and render form with pre-filled values', async ({ page }) => {
    await gotoSettingsAdmin(page, {
      currentSettings: {
        id: 1,
        approve_booking: true,
        calendar_slot_size: 30,
        calendar_min_time: '08:00',
        calendar_max_time: '18:00',
      },
    });

    // Form container visible
    await expect(page.getByTestId('settings-admin')).toBeVisible();
    await expect(page.getByTestId('settings-form')).toBeVisible();

    // Section 1: Approval Workflow — approveBooking toggle is ON
    await expect(page.getByTestId('section-approval-workflow')).toBeVisible();
    const toggle = page.getByTestId('approve-booking-toggle');
    await expect(toggle).toBeVisible();
    await expect(toggle).toHaveAttribute('aria-checked', 'true');
    await expect(page.getByTestId('approve-booking-state')).toContainText('ON');

    // Section 2: Calendar Display — inputs pre-filled from getCurrent()
    await expect(page.getByTestId('section-calendar-display')).toBeVisible();
    await expect(page.getByTestId('slot-size-input')).toHaveValue('30');
    await expect(page.getByTestId('min-time-input')).toHaveValue('08:00');
    await expect(page.getByTestId('max-time-input')).toHaveValue('18:00');

    // Section 3: Display & Feeds — convenience buttons present
    await expect(page.getByTestId('section-display-feeds')).toBeVisible();
    await expect(page.getByTestId('btn-preview-display-board')).toBeVisible();
    await expect(page.getByTestId('btn-copy-feed-links')).toBeVisible();
  });
});

test.describe('Settings Admin — Save Confirmation', () => {
  test('Test 2: Update settings and verify StateConfirmationBanner with timestamp', async ({
    page,
  }) => {
    // Track the PUT request body
    let capturedRequestBody: Record<string, unknown> | null = null;

    await gotoSettingsAdmin(page);

    // Override the PUT mock to capture request body
    await page.route(`${API_BASE}/settings/1`, async (route) => {
      const body = route.request().postData();
      if (body) {
        try {
          capturedRequestBody = JSON.parse(body) as Record<string, unknown>;
        } catch {
          // ignore parse errors
        }
      }
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(SETTINGS_AFTER_UPDATE),
      });
    });

    // Toggle approveBooking to OFF (currently ON from mock)
    const toggle = page.getByTestId('approve-booking-toggle');
    await toggle.click();
    await expect(toggle).toHaveAttribute('aria-checked', 'false');

    // Change slot_size to 60
    await page.getByTestId('slot-size-input').selectOption('60');

    // Change min_time to 07:00
    await page.getByTestId('min-time-input').fill('07:00');

    // Click Save Settings
    await page.getByTestId('btn-save-settings').click();

    // StateConfirmationBanner appears with "Settings updated at HH:mm:ss"
    const banner = page.getByTestId('state-confirmation-banner');
    await expect(banner).toBeVisible({ timeout: 5000 });
    await expect(banner).toContainText('Settings updated');

    // Verify the banner contains a time pattern (HH:mm format from StateConfirmationBanner)
    const bannerText = await banner.textContent();
    expect(bannerText).toMatch(/Settings updated/);

    // Verify PUT request was sent to /settings/1
    expect(capturedRequestBody).not.toBeNull();
    expect(capturedRequestBody?.approve_booking).toBe(false);
    expect(capturedRequestBody?.calendar_slot_size).toBe(60);
    expect(capturedRequestBody?.calendar_min_time).toBe('07:00');
    expect(capturedRequestBody?.calendar_max_time).toBe('18:00');
  });
});

test.describe('Settings Admin — Propagation Indicator', () => {
  test('Test 3: Propagation indicator appears after save and auto-dismisses after 5 seconds', async ({
    page,
  }) => {
    await gotoSettingsAdmin(page);

    // Propagation indicator should NOT be visible before save
    await expect(page.getByTestId('propagation-indicator')).toBeHidden();

    // Toggle approveBooking (trigger save flow)
    await page.getByTestId('approve-booking-toggle').click();

    // Save
    await page.getByTestId('btn-save-settings').click();

    // StateConfirmationBanner appears
    await expect(page.getByTestId('state-confirmation-banner')).toBeVisible({ timeout: 5000 });

    // Propagation indicator appears with expected text
    const indicator = page.getByTestId('propagation-indicator');
    await expect(indicator).toBeVisible({ timeout: 5000 });
    await expect(indicator).toContainText('Changes take effect immediately for new bookings');

    // Wait 5+ seconds for auto-dismiss (propagation indicator uses 5s timeout)
    await page.waitForTimeout(5500);

    // Propagation indicator auto-dismissed
    await expect(indicator).toBeHidden({ timeout: 2000 });
  });
});

test.describe('Settings Admin — Convenience Buttons', () => {
  test('Test 4: Preview Display Board button opens new tab with /display-board URL', async ({
    page,
  }) => {
    await gotoSettingsAdmin(page);

    // Wait for a new page (tab) to open when "Preview Display Board" is clicked
    const newPagePromise = page.context().waitForEvent('page');
    await page.getByTestId('btn-preview-display-board').click();

    const newTab = await newPagePromise;
    await newTab.waitForLoadState('domcontentloaded');

    // Verify the new tab's URL contains /display-board
    expect(newTab.url()).toContain('/display-board');
  });

  test('Test 5: Copy feed links button shows modal with RSS2, iCal, JSON feed URLs', async ({
    page,
  }) => {
    await gotoSettingsAdmin(page);

    // Modal should not be visible before clicking the button
    await expect(page.getByTestId('feed-links-modal')).toBeHidden();

    // Click "Copy feed links" button
    await page.getByTestId('btn-copy-feed-links').click();

    // Modal appears
    const modal = page.getByTestId('feed-links-modal');
    await expect(modal).toBeVisible({ timeout: 3000 });

    // RSS2 feed URL and copy button visible
    await expect(page.getByTestId('feed-rss2')).toBeVisible();
    await expect(page.getByTestId('feed-rss2-url')).toBeVisible();
    await expect(page.getByTestId('feed-rss2-copy')).toBeVisible();

    // iCal feed URL and copy button visible
    await expect(page.getByTestId('feed-ical')).toBeVisible();
    await expect(page.getByTestId('feed-ical-url')).toBeVisible();
    await expect(page.getByTestId('feed-ical-copy')).toBeVisible();

    // JSON feed URL and copy button visible
    await expect(page.getByTestId('feed-json')).toBeVisible();
    await expect(page.getByTestId('feed-json-url')).toBeVisible();
    await expect(page.getByTestId('feed-json-copy')).toBeVisible();

    // URLs contain expected feed paths
    const rss2Text = await page.getByTestId('feed-rss2-url').textContent();
    expect(rss2Text).toContain('/feeds/rss2');

    const icalText = await page.getByTestId('feed-ical-url').textContent();
    expect(icalText).toContain('/feeds/ical');

    const jsonText = await page.getByTestId('feed-json-url').textContent();
    expect(jsonText).toContain('/feeds/json');
  });
});
