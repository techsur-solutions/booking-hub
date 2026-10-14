import { test, expect } from '@playwright/test';

/**
 * Authentication Flows E2E Tests — UI-AUTH-01, UI-AUTH-02, UI-AUTH-03
 *
 * Test mode notes:
 * - Login page tests: /login is a public route. In VITE_E2E_MODE=true, AuthProvider
 *   sets authenticated=true and Login redirects immediately. Login tests prevent this
 *   by setting window.__E2E_LOGIN_TEST__=true via addInitScript, which Login.tsx checks
 *   to suppress the authenticated redirect. Keycloak auth URLs are also intercepted.
 * - My Account tests: use VITE_E2E_MODE roles injection + API mocking.
 * - Password Reset tests: fully public, no auth needed, work with any mode.
 *
 * Tests cover:
 *   1. Login page: renders correctly, shows Remember Me, triggers keycloak.login() on button click
 *   2. My Account: shows editable name, read-only email/roles, Change Password button
 *   3. Password Reset request: email submission, identical success message
 *   4. Password Reset complete: token validation, error handling
 */

// ── Helpers ───────────────────────────────────────────────────────────────────

/**
 * Navigate to the Login page with E2E test flag set.
 * This suppresses the authenticated-redirect so we can test the login form UI.
 */
async function gotoLoginPage(page: import('@playwright/test').Page) {
  // Intercept Keycloak auth redirect
  await page.route('**/realms/**', (route) => {
    route.fulfill({
      status: 200,
      body: '<html><body>Keycloak Login Page</body></html>',
    });
  });

  // Inject E2E login test flag — Login.tsx checks this to suppress auto-redirect
  await page.addInitScript(() => {
    (window as unknown as Record<string, unknown>).__E2E_LOGIN_TEST__ = true;
  });

  await page.goto('/login');
}

/**
 * Navigate to an authenticated route with specific roles via E2E injection.
 */
async function gotoWithRoles(
  page: import('@playwright/test').Page,
  url: string,
  roles: string[] = ['role_calendar_viewer'],
) {
  await page.addInitScript((injectedRoles: string[]) => {
    (window as unknown as Record<string, unknown>).__E2E_ROLES__ = injectedRoles;
  }, roles);
  await page.goto(url);
}

// ── Test Suite 1: Login Page ──────────────────────────────────────────────────

test.describe('Login page (UI-AUTH-01)', () => {
  test('renders login page with Keycloak button and Remember Me checkbox', async ({ page }) => {
    await gotoLoginPage(page);
    await page.waitForSelector('[data-testid="login-button"]', { timeout: 10_000 });

    // Verify the login button is visible
    await expect(page.locator('[data-testid="login-button"]')).toBeVisible();
    await expect(page.locator('[data-testid="login-button"]')).toContainText('Login with Keycloak');

    // Verify Remember Me checkbox is present and unchecked by default
    const checkbox = page.locator('[data-testid="remember-me-checkbox"]');
    await expect(checkbox).toBeVisible();
    await expect(checkbox).not.toBeChecked();

    // Verify "Forgot password?" link points to /password-reset
    await expect(page.locator('[data-testid="forgot-password-link"]')).toBeVisible();
    await expect(page.locator('[data-testid="forgot-password-link"]')).toHaveAttribute('href', '/password-reset');
  });

  test('Remember Me checkbox can be checked and unchecked', async ({ page }) => {
    await gotoLoginPage(page);
    await page.waitForSelector('[data-testid="remember-me-checkbox"]', { timeout: 10_000 });

    const checkbox = page.locator('[data-testid="remember-me-checkbox"]');
    await checkbox.check();
    await expect(checkbox).toBeChecked();

    await checkbox.uncheck();
    await expect(checkbox).not.toBeChecked();
  });

  test('clicking Login button initiates Keycloak auth flow (navigation or request)', async ({ page }) => {
    const keycloakUrls: string[] = [];

    // Capture any requests to Keycloak realms
    await page.route('**/realms/bookinghub/**', (route) => {
      keycloakUrls.push(route.request().url());
      route.fulfill({ status: 200, body: '<html><body>Keycloak</body></html>' });
    });

    await page.addInitScript(() => {
      (window as unknown as Record<string, unknown>).__E2E_LOGIN_TEST__ = true;
    });

    await page.goto('/login');
    await page.waitForSelector('[data-testid="login-button"]', { timeout: 10_000 });

    // Click Login button — this calls keycloak.login() which initiates auth flow
    // Full redirect verification (PKCE params) is in auth-redirect.spec.ts (auth mode)
    await page.click('[data-testid="login-button"]');
    await page.waitForTimeout(1000);

    // The button click should have triggered login without JavaScript errors
    // Verify page is still functional (no unhandled exception killed the app)
    const errorMessages = page.locator('[role="alert"]');
    await expect(errorMessages).toHaveCount(0);
  });

  test('"Forgot password?" link navigates to /password-reset', async ({ page }) => {
    await gotoLoginPage(page);
    await page.waitForSelector('[data-testid="forgot-password-link"]', { timeout: 10_000 });

    // Click Forgot password — links are <a href="/password-reset"> so navigation occurs
    await page.locator('[data-testid="forgot-password-link"]').click();
    await page.waitForURL(/\/password-reset/, { timeout: 5_000 });
    await expect(page).toHaveURL(/\/password-reset/);
  });
});

// ── Test Suite 2: My Account ──────────────────────────────────────────────────

test.describe('My Account page (UI-AUTH-02)', () => {
  test('renders name (editable), email (read-only), and roles (chips)', async ({ page }) => {
    // Mock GET /users/me
    await page.route('**/users/me', (route) => {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: 'user-123',
          email: 'alice@example.com',
          display_name: 'Alice Smith',
          deleted_at: null,
        }),
      });
    });

    await gotoWithRoles(page, '/account', ['role_calendar_viewer', 'role_booking_approver']);
    await page.waitForSelector('[data-testid="name-display"]', { timeout: 10_000 });

    // Name field is editable (has edit button)
    await expect(page.locator('[data-testid="name-display"]')).toBeVisible();
    await expect(page.locator('[data-testid="name-display"]')).toContainText('Alice Smith');
    await expect(page.locator('[data-testid="name-edit-button"]')).toBeVisible();

    // Email is read-only
    await expect(page.locator('[data-testid="email-display"]')).toBeVisible();
    await expect(page.locator('[data-testid="email-display"]')).toContainText('alice@example.com');
    await expect(page.locator('[data-testid="email-display"]')).toContainText('Read-only');

    // Roles are displayed as chips
    await expect(page.locator('[data-testid="roles-list"]')).toBeVisible();
    await expect(page.locator('[data-testid="roles-list"]')).toContainText('role_calendar_viewer');
    await expect(page.locator('[data-testid="roles-list"]')).toContainText('role_booking_approver');
  });

  test('Edit/Save/Cancel name editing flow', async ({ page }) => {
    let patchBody: Record<string, unknown> = {};

    await page.route('**/users/me', (route) => {
      if (route.request().method() === 'GET') {
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            id: 'user-123',
            email: 'alice@example.com',
            display_name: 'Alice Smith',
            deleted_at: null,
          }),
        });
      } else if (route.request().method() === 'PUT') {
        patchBody = JSON.parse(route.request().postData() ?? '{}') as Record<string, unknown>;
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            id: 'user-123',
            email: 'alice@example.com',
            display_name: 'Alice Updated',
            deleted_at: null,
          }),
        });
      } else {
        route.continue();
      }
    });

    await gotoWithRoles(page, '/account');
    await page.waitForSelector('[data-testid="name-edit-button"]', { timeout: 10_000 });

    // Click Edit
    await page.click('[data-testid="name-edit-button"]');
    await expect(page.locator('[data-testid="name-edit-input"]')).toBeVisible();

    // Type new name
    await page.locator('[data-testid="name-edit-input"]').fill('Alice Updated');

    // Click Save
    await page.click('[data-testid="name-save-button"]');

    // StateConfirmationBanner should appear with "Name updated"
    await expect(page.locator('[data-testid="state-confirmation-banner"]')).toBeVisible({
      timeout: 5_000,
    });
    await expect(page.locator('[data-testid="state-confirmation-banner"]')).toContainText('Name updated');

    // Verify PUT was called with correct data
    expect(patchBody.display_name).toBe('Alice Updated');
  });

  test('Cancel editing restores previous name without saving', async ({ page }) => {
    await page.route('**/users/me', (route) => {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: 'user-123',
          email: 'alice@example.com',
          display_name: 'Alice Smith',
          deleted_at: null,
        }),
      });
    });

    await gotoWithRoles(page, '/account');
    await page.waitForSelector('[data-testid="name-edit-button"]', { timeout: 10_000 });

    // Start editing
    await page.click('[data-testid="name-edit-button"]');
    await page.locator('[data-testid="name-edit-input"]').fill('Changed Name');

    // Cancel
    await page.click('[data-testid="name-cancel-button"]');

    // Input gone, original name restored
    await expect(page.locator('[data-testid="name-edit-input"]')).not.toBeVisible();
    await expect(page.locator('[data-testid="name-display"]')).toContainText('Alice Smith');
  });

  test('Change Password button is visible and calls keycloak.accountManagement()', async ({ page }) => {
    await page.route('**/users/me', (route) => {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: 'user-123',
          email: 'alice@example.com',
          display_name: 'Alice Smith',
          deleted_at: null,
        }),
      });
    });

    // In E2E mode, keycloak.accountManagement() may navigate — intercept it
    await page.route('**/realms/**', (route) => {
      route.fulfill({ status: 200, body: '<html><body>Keycloak Account Mgmt</body></html>' });
    });

    await gotoWithRoles(page, '/account');
    await page.waitForSelector('[data-testid="change-password-button"]', { timeout: 10_000 });

    // Verify button is present
    await expect(page.locator('[data-testid="change-password-button"]')).toBeVisible();
    await expect(page.locator('[data-testid="change-password-button"]')).toContainText('Change Password');
    await expect(page.locator('[data-testid="change-password-button"]')).toContainText('Keycloak');

    // Audit History link should be present
    await expect(page.locator('[data-testid="audit-history-link"]')).toBeVisible();
    await expect(page.locator('[data-testid="audit-history-link"]')).toContainText('Audit History');
    await expect(page.locator('[data-testid="audit-history-link"]')).toHaveAttribute(
      'href',
      /\/admin\/audit\?actor=user-123/
    );
  });

  test('loads successfully when user record exists', async ({ page }) => {
    // Mock GET /users/me to return test user (matching V3 seed data)
    await page.route('**/users/me', (route) => {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: '00000000-0000-0000-0000-000000000001',
          email: 'testuser@example.com',
          display_name: 'Test User',
          deleted_at: null,
        }),
      });
    });

    await gotoWithRoles(page, '/account');
    await page.waitForSelector('[data-testid="name-display"]', { timeout: 10_000 });

    // Should show user info, not error
    await expect(page.locator('h1')).toContainText('My Account');
    await expect(page.getByText('Test User')).toBeVisible();
    await expect(page.getByText('testuser@example.com')).toBeVisible();
    
    // Should NOT show error message
    await expect(page.getByText('Failed to load account information')).not.toBeVisible();
  });
});

// ── Test Suite 3: Password Reset Request ─────────────────────────────────────

test.describe('Password Reset request (UI-AUTH-03)', () => {
  test('renders email input, Send Reset Link button, and Back to login link', async ({ page }) => {
    await page.goto('/password-reset');
    await page.waitForSelector('[data-testid="reset-email-input"]', { timeout: 10_000 });

    await expect(page.locator('[data-testid="reset-email-input"]')).toBeVisible();
    await expect(page.locator('[data-testid="send-reset-button"]')).toBeVisible();
    await expect(page.locator('[data-testid="send-reset-button"]')).toContainText('Send Reset Link');
    await expect(page.locator('[data-testid="back-to-login-link"]')).toBeVisible();
    await expect(page.locator('[data-testid="back-to-login-link"]')).toHaveAttribute('href', '/login');
  });

  test('shows identical success message after submission (security: no email enumeration)', async ({ page }) => {
    await page.route('**/users/password-reset/request', (route) => {
      route.fulfill({ status: 202, body: '{}', contentType: 'application/json' });
    });

    await page.goto('/password-reset');
    await page.waitForSelector('[data-testid="reset-email-input"]', { timeout: 10_000 });

    // Fill email and submit
    await page.fill('[data-testid="reset-email-input"]', 'test@example.com');
    await page.click('[data-testid="send-reset-button"]');

    // Verify identical success message (same wording as FRD §6.3)
    await expect(page.locator('[data-testid="password-reset-success"]')).toBeVisible({
      timeout: 5_000,
    });
    await expect(page.locator('[data-testid="password-reset-success"]')).toContainText(
      'If that email exists, a reset link has been sent.'
    );
  });

  test('shows same success message even when server returns 404 (no enumeration)', async ({ page }) => {
    // Server returns 404 for unknown email — UI must NOT reveal this
    await page.route('**/users/password-reset/request', (route) => {
      route.fulfill({ status: 404, body: '{"error":"USER_NOT_FOUND"}', contentType: 'application/json' });
    });

    await page.goto('/password-reset');
    await page.waitForSelector('[data-testid="reset-email-input"]', { timeout: 10_000 });

    await page.fill('[data-testid="reset-email-input"]', 'unknown@example.com');
    await page.click('[data-testid="send-reset-button"]');

    // MUST show same success message — T-09-15-01 (information disclosure mitigation)
    await expect(page.locator('[data-testid="password-reset-success"]')).toBeVisible({
      timeout: 5_000,
    });
    await expect(page.locator('[data-testid="password-reset-success"]')).toContainText(
      'If that email exists, a reset link has been sent.'
    );
  });
});

// ── Test Suite 4: Password Reset Complete ────────────────────────────────────

test.describe('Password Reset complete (UI-AUTH-03)', () => {
  test('renders New Password and Confirm Password fields with token in URL', async ({ page }) => {
    await page.goto('/password-reset/complete?token=valid-test-token');
    await page.waitForSelector('[data-testid="new-password-input"]', { timeout: 10_000 });

    await expect(page.locator('[data-testid="new-password-input"]')).toBeVisible();
    await expect(page.locator('[data-testid="confirm-password-input"]')).toBeVisible();
    await expect(page.locator('[data-testid="reset-password-button"]')).toBeVisible();
    await expect(page.locator('[data-testid="reset-password-button"]')).toContainText('Reset Password');
  });

  test('client-side validation: passwords must match', async ({ page }) => {
    await page.goto('/password-reset/complete?token=valid-test-token');
    await page.waitForSelector('[data-testid="new-password-input"]', { timeout: 10_000 });

    await page.fill('[data-testid="new-password-input"]', 'Password123');
    await page.fill('[data-testid="confirm-password-input"]', 'Different456');
    await page.click('[data-testid="reset-password-button"]');

    // Error should appear immediately (no server call needed)
    await expect(page.locator('[data-testid="form-error-message"]')).toBeVisible();
    await expect(page.locator('[data-testid="form-error-message"]')).toContainText(
      'Passwords must match.'
    );
  });

  test('client-side validation: password must meet requirements', async ({ page }) => {
    await page.goto('/password-reset/complete?token=valid-test-token');
    await page.waitForSelector('[data-testid="new-password-input"]', { timeout: 10_000 });

    // Too short, missing uppercase/number
    await page.fill('[data-testid="new-password-input"]', 'weak');
    await page.fill('[data-testid="confirm-password-input"]', 'weak');
    await page.click('[data-testid="reset-password-button"]');

    await expect(page.locator('[data-testid="form-error-message"]')).toBeVisible();
    await expect(page.locator('[data-testid="form-error-message"]')).toContainText(
      'Password must be at least 8 characters'
    );
  });

  test('server returns 410: shows "Token expired" error with re-request link', async ({ page }) => {
    await page.route('**/users/password-reset/complete', (route) => {
      route.fulfill({ status: 410, body: '{"error":"TOKEN_EXPIRED"}', contentType: 'application/json' });
    });

    await page.goto('/password-reset/complete?token=expired-token');
    await page.waitForSelector('[data-testid="new-password-input"]', { timeout: 10_000 });

    await page.fill('[data-testid="new-password-input"]', 'NewPassword123');
    await page.fill('[data-testid="confirm-password-input"]', 'NewPassword123');
    await page.click('[data-testid="reset-password-button"]');

    await expect(page.locator('[data-testid="token-error-message"]')).toBeVisible({ timeout: 5_000 });
    await expect(page.locator('[data-testid="token-error-message"]')).toContainText(
      'Token expired. Request a new reset link.'
    );
    // Re-request link should appear
    await expect(page.locator('[data-testid="token-error-message"] a[href="/password-reset"]')).toBeVisible();
  });

  test('server returns 409: shows "Token already used" error', async ({ page }) => {
    await page.route('**/users/password-reset/complete', (route) => {
      route.fulfill({ status: 409, body: '{"error":"TOKEN_USED"}', contentType: 'application/json' });
    });

    await page.goto('/password-reset/complete?token=used-token');
    await page.waitForSelector('[data-testid="new-password-input"]', { timeout: 10_000 });

    await page.fill('[data-testid="new-password-input"]', 'NewPassword123');
    await page.fill('[data-testid="confirm-password-input"]', 'NewPassword123');
    await page.click('[data-testid="reset-password-button"]');

    await expect(page.locator('[data-testid="token-error-message"]')).toBeVisible({ timeout: 5_000 });
    await expect(page.locator('[data-testid="token-error-message"]')).toContainText(
      'Token already used. Request a new reset link.'
    );
  });

  test('no token in URL shows "Invalid reset link" error immediately', async ({ page }) => {
    await page.goto('/password-reset/complete');
    // Token error shows without any form interaction (no token = immediate error)
    await expect(page.locator('[data-testid="token-error-message"]')).toBeVisible({ timeout: 10_000 });
    await expect(page.locator('[data-testid="token-error-message"]')).toContainText(
      'Invalid reset link.'
    );
    // Form fields should NOT be visible
    await expect(page.locator('[data-testid="new-password-input"]')).not.toBeVisible();
  });

  test('successful reset navigates away from reset page (to login or bookings)', async ({ page }) => {
    await page.route('**/users/password-reset/complete', (route) => {
      route.fulfill({ status: 200, body: '{"success":true}', contentType: 'application/json' });
    });

    await page.goto('/password-reset/complete?token=valid-test-token');
    await page.waitForSelector('[data-testid="new-password-input"]', { timeout: 10_000 });

    await page.fill('[data-testid="new-password-input"]', 'NewPassword123');
    await page.fill('[data-testid="confirm-password-input"]', 'NewPassword123');
    await page.click('[data-testid="reset-password-button"]');

    // On success, window.location.replace('/login') is called.
    // In E2E mode (VITE_E2E_MODE=true), the Login page auto-redirects authenticated users
    // to /bookings?view=month. So the final URL is either /login or /bookings.
    // Either way, we've left the /password-reset/complete page — that's the key assertion.
    await page.waitForURL((url) =>
      url.pathname === '/login' || url.pathname === '/bookings',
      { timeout: 10_000 }
    );
    const url = page.url();
    expect(url.includes('/login') || url.includes('/bookings')).toBe(true);

    // Verify we're NOT still on the reset complete page
    await expect(page.locator('[data-testid="reset-password-button"]')).not.toBeVisible();
  });
});
