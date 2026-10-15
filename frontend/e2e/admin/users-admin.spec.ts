import { test, expect, type Page } from '@playwright/test';
import type { UserWithRoles } from '../../src/api/users';

/**
 * Users Admin End-to-End Tests (UI-ADMIN-04)
 *
 * Tests run with VITE_E2E_MODE=true (playwright.config.ts webServer.env).
 * Keycloak is bypassed; roles injected via window.__E2E_ROLES__.
 * Backend API calls intercepted via page.route() targeting http://localhost:8080.
 * No real backend or auth required.
 *
 * Covered scenarios:
 *   1. Create new user with role assignment — form submit, banner, navigate back, table row
 *   2. Admin editing another user — all fields available (display_name, email, roles; no password)
 *   3. User editing self — restricted fields (display_name only; email read-only; roles/password hidden)
 */

// ── Constants ──────────────────────────────────────────────────────────────────

const API_BASE = 'http://localhost:8080';

// The current user (test admin with role_user_manager)
const CURRENT_USER_ID = 'admin-user-uuid-0001';
const CURRENT_USER_SUB = CURRENT_USER_ID;

// ── Fixture data ──────────────────────────────────────────────────────────────

const USER_1: UserWithRoles = {
  id: 'user-uuid-0001',
  email: 'alice@example.com',
  display_name: 'Alice Admin',
  deleted_at: null,
  roles: ['role_calendar_viewer', 'role_room_booking'],
  enabled: true,
};

const USER_2: UserWithRoles = {
  id: 'user-uuid-0002',
  email: 'bob@example.com',
  display_name: 'Bob User',
  deleted_at: null,
  roles: [],
  enabled: false,
};

const INITIAL_USERS = [USER_1, USER_2];

// Newly created user returned by POST /users
const CREATED_USER: UserWithRoles = {
  id: 'user-uuid-0099',
  email: 'testuser@example.com',
  display_name: 'Test User',
  deleted_at: null,
  roles: ['role_calendar_viewer', 'role_room_booking'],
  enabled: true,
};

// ── Helpers ───────────────────────────────────────────────────────────────────

/** Navigate to /admin/users with injected roles and mocked API */
async function gotoUsersAdmin(
  page: Page,
  options: {
    roles?: string[];
    users?: UserWithRoles[];
    currentUserId?: string;
    createdUser?: UserWithRoles;
  } = {}
) {
  const {
    roles = ['role_user_manager', 'role_location_manager'],
    users = INITIAL_USERS,
    currentUserId = CURRENT_USER_ID,
    createdUser = CREATED_USER,
  } = options;

  // Inject roles and current user ID before page load
  await page.addInitScript(
    ({ injectedRoles, injectedUserId }: { injectedRoles: string[]; injectedUserId: string }) => {
      (window as unknown as Record<string, unknown>).__E2E_ROLES__ = injectedRoles;
      (window as unknown as Record<string, unknown>).__E2E_USER_ID__ = injectedUserId;
    },
    { injectedRoles: roles, injectedUserId: currentUserId }
  );

  // Track users list (mutates on create)
  let usersList = [...users];

  // Mock GET /users
  await page.route(`${API_BASE}/users**`, async (route) => {
    const url = route.request().url();
    const method = route.request().method();

    if (method === 'GET' && url === `${API_BASE}/users`) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(usersList),
      });
    } else if (method === 'GET' && url.includes('/users/me')) {
      // Current user's profile
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ id: currentUserId, email: 'admin@example.com', display_name: 'Admin User', deleted_at: null, roles, enabled: true }),
      });
    } else if (method === 'POST' && url === `${API_BASE}/users`) {
      // Create user
      usersList = [...usersList, createdUser];
      await route.fulfill({
        status: 201,
        contentType: 'application/json',
        body: JSON.stringify(createdUser),
      });
    } else if (method === 'PUT' && url.includes('/users/me')) {
      // Self-update
      const body = JSON.parse(route.request().postData() ?? '{}') as { display_name?: string };
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ id: currentUserId, email: 'admin@example.com', display_name: body.display_name ?? 'Admin User', deleted_at: null, roles, enabled: true }),
      });
    } else if (method === 'PUT' && /\/users\/[^/]+$/.test(url)) {
      // Admin update another user
      const userId = url.split('/users/')[1];
      const body = JSON.parse(route.request().postData() ?? '{}') as Partial<UserWithRoles>;
      const existing = usersList.find((u) => u.id === userId);
      const updated = existing ? { ...existing, ...body } : body;
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(updated),
      });
    } else if (method === 'GET' && /\/users\/[^/]+$/.test(url)) {
      // Get individual user by id
      const userId = url.split('/users/')[1].split('?')[0];
      const found = [...usersList, { id: currentUserId, email: 'admin@example.com', display_name: 'Admin User', deleted_at: null, roles, enabled: true }]
        .find((u) => u.id === userId);
      if (found) {
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(found) });
      } else {
        await route.fulfill({ status: 404, contentType: 'application/json', body: JSON.stringify({ error: 'Not found' }) });
      }
    } else {
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([]) });
    }
  });

  await page.goto('/admin/users');
}

// ── Tests ─────────────────────────────────────────────────────────────────────

test.describe('Users Admin — Create User', () => {
  test('1. Create new user with role assignment: form submit, banner, navigate back, table row', async ({
    page,
  }) => {
    await gotoUsersAdmin(page);

    // Wait for the Users Admin page to load
    await expect(page.getByTestId('users-admin')).toBeVisible({ timeout: 10_000 });

    // Click "Create User" button
    await page.getByTestId('create-user-btn').click();

    // Should navigate to /admin/users/new
    await expect(page).toHaveURL(/\/admin\/users\/new/, { timeout: 5000 });

    // Wait for the create form
    await expect(page.getByTestId('user-create-form')).toBeVisible({ timeout: 5000 });

    // Fill form fields
    await page.getByTestId('input-display-name').fill('Test User');
    await page.getByTestId('input-email').fill('testuser@example.com');
    await page.getByTestId('input-password').fill('password123');

    // Select roles: role_calendar_viewer and role_room_booking
    await page.getByTestId('role-checkbox-role_calendar_viewer').check();
    await page.getByTestId('role-checkbox-role_room_booking').check();

    // Verify checkboxes are checked
    await expect(page.getByTestId('role-checkbox-role_calendar_viewer')).toBeChecked();
    await expect(page.getByTestId('role-checkbox-role_room_booking')).toBeChecked();

    // Submit form
    await page.getByTestId('btn-create-user').click();

    // StateConfirmationBanner should appear with "User created — temporary password set"
    await expect(
      page.getByTestId('state-confirmation-banner')
    ).toBeVisible({ timeout: 5000 });
    await expect(
      page.getByTestId('state-confirmation-banner')
    ).toContainText('User created — temporary password set');

    // Should navigate back to /admin/users
    await expect(page).toHaveURL(/\/admin\/users/, { timeout: 5000 });
  });
});

test.describe('Users Admin — Admin Editing Another User', () => {
  test('2. Admin editing another user: all fields available, no password field', async ({
    page,
  }) => {
    await gotoUsersAdmin(page);

    // Wait for table to render
    await expect(page.getByTestId('user-table')).toBeVisible({ timeout: 10_000 });

    // Verify Alice's row is present
    await expect(page.getByTestId(`user-row-${USER_1.id}`)).toBeVisible();

    // Click Edit for Alice (USER_1 is not the current user — admin editing another)
    await page.getByTestId(`user-edit-${USER_1.id}`).click();

    // Should navigate to edit page for Alice
    await expect(page).toHaveURL(new RegExp(`/admin/users/${USER_1.id}/edit`), { timeout: 5000 });

    // Wait for edit form
    await expect(page.getByTestId('user-edit-form')).toBeVisible({ timeout: 5000 });

    // display_name input should be visible and editable (admin editing another user)
    const displayNameInput = page.getByTestId('input-display-name');
    await expect(displayNameInput).toBeVisible();
    await expect(displayNameInput).not.toHaveAttribute('readonly');

    // email input should be visible and editable
    const emailInput = page.getByTestId('input-email');
    await expect(emailInput).toBeVisible();
    await expect(emailInput).not.toHaveAttribute('readonly');

    // roles fieldset should be visible (admin edit shows roles)
    await expect(page.getByTestId('roles-fieldset')).toBeVisible();

    // password field should NOT be present (no password field in either edit mode)
    await expect(page.getByTestId('input-password')).toHaveCount(0);

    // self-edit notice should NOT be present (this is admin editing another user)
    await expect(page.getByTestId('self-edit-notice')).toHaveCount(0);

    // Change display_name to "Test User Updated"
    await displayNameInput.clear();
    await displayNameInput.fill('Test User Updated');

    // Add role role_booking_approver
    await page.getByTestId('role-checkbox-role_booking_approver').check();
    await expect(page.getByTestId('role-checkbox-role_booking_approver')).toBeChecked();

    // Submit
    await page.getByTestId('btn-save-user').click();

    // StateConfirmationBanner appears with "User updated"
    await expect(
      page.getByTestId('state-confirmation-banner')
    ).toBeVisible({ timeout: 5000 });
    await expect(
      page.getByTestId('state-confirmation-banner')
    ).toContainText('User updated');

    // Should navigate back to /admin/users
    await expect(page).toHaveURL(/\/admin\/users/, { timeout: 5000 });
  });
});

test.describe('Users Admin — User Editing Self (Restricted Fields)', () => {
  test('3. User editing own account: display_name editable, email read-only, roles/password absent', async ({
    page,
  }) => {
    // For self-edit, the current user ID matches the user being edited
    const SELF_USER: UserWithRoles = {
      id: CURRENT_USER_ID, // same as the logged-in user
      email: 'admin@example.com',
      display_name: 'Admin User',
      deleted_at: null,
      roles: ['role_user_manager', 'role_location_manager'],
      enabled: true,
    };

    await gotoUsersAdmin(page, {
      users: [SELF_USER, USER_2],
      currentUserId: CURRENT_USER_ID,
    });

    // Navigate directly to edit own account
    await page.goto(`/admin/users/${CURRENT_USER_ID}/edit`);

    // Wait for edit form
    await expect(page.getByTestId('user-edit-form')).toBeVisible({ timeout: 10_000 });

    // Self-edit notice should be present
    await expect(page.getByTestId('self-edit-notice')).toBeVisible();

    // display_name input should be visible and editable
    const displayNameInput = page.getByTestId('input-display-name');
    await expect(displayNameInput).toBeVisible();
    await expect(displayNameInput).not.toHaveAttribute('readonly');

    // email input should be present but read-only with note
    const emailInput = page.getByTestId('input-email');
    await expect(emailInput).toBeVisible();
    await expect(emailInput).toHaveAttribute('readonly');

    // email read-only note: "Contact admin to change email"
    await expect(page.getByTestId('email-readonly-note')).toBeVisible();
    await expect(page.getByTestId('email-readonly-note')).toContainText('Contact admin to change email');

    // roles multi-select should NOT be present (hidden in self-edit)
    await expect(page.getByTestId('roles-fieldset')).toHaveCount(0);

    // password field should NOT be present
    await expect(page.getByTestId('input-password')).toHaveCount(0);

    // password note should be present: "To change password, use My Account → Change Password"
    await expect(page.getByTestId('password-note')).toBeVisible();
    await expect(page.getByTestId('password-note')).toContainText('To change password, use My Account');

    // Update display_name
    await displayNameInput.clear();
    await displayNameInput.fill('My Name Updated');

    // Track which API endpoint is called to verify it uses /users/me
    const selfUpdateRequest = page.waitForRequest(
      (req) => req.url().includes('/users/me') && req.method() === 'PUT'
    );

    // Submit form
    await page.getByTestId('btn-save-user').click();

    // Verify API call was to /users/me (self-update endpoint)
    const req = await selfUpdateRequest;
    expect(req.url()).toContain('/users/me');

    // Verify the request body does NOT contain roles or password
    const body = JSON.parse(req.postData() ?? '{}') as Record<string, unknown>;
    expect(body.roles).toBeUndefined();
    expect(body.password).toBeUndefined();
    expect(body.display_name).toBe('My Name Updated');

    // StateConfirmationBanner appears
    await expect(
      page.getByTestId('state-confirmation-banner')
    ).toBeVisible({ timeout: 5000 });

    // Should navigate back to /admin/users
    await expect(page).toHaveURL(/\/admin\/users/, { timeout: 5000 });
  });
});
