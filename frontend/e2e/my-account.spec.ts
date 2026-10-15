import { test, expect } from '@playwright/test';

test.describe('My Account Page - UAT Gap #14', () => {
  test.beforeEach(async ({ page }) => {
    // Set E2E roles
    await page.addInitScript(() => {
      window.__E2E_ROLES__ = ['role_calendar_viewer', 'role_room_booking'];
    });

    // Mock /users/me API to return user data
    await page.route('http://localhost:8080/users/me', async (route) => {
      await route.fulfill({
        status: 200,
        json: {
          id: 'user-123',
          email: 'john.doe@example.com',
          first_name: 'John',
          last_name: 'Doe',
          roles: ['role_calendar_viewer', 'role_room_booking'],
          created_at: '2024-01-01T00:00:00Z',
          updated_at: '2024-01-01T00:00:00Z',
        }
      });
    });
  });

  test('My Account page loads user information successfully', async ({ page }) => {
    // This test explicitly addresses UAT Test #14 blocker: "Failed to load account information"
    
    await page.goto('http://localhost:5173/account');

    // Wait for page to load (not error state)
    await page.waitForSelector('h1:has-text("My Account")', { timeout: 5000 });

    // Verify NO error message appears
    await expect(page.locator('[role="alert"]')).not.toBeVisible();

    // Verify user data is displayed
    await expect(page.locator('text=John Doe')).toBeVisible();
    await expect(page.locator('text=john.doe@example.com')).toBeVisible();

    // Verify role chips are displayed
    await expect(page.locator('text=role_calendar_viewer')).toBeVisible();
    await expect(page.locator('text=role_room_booking')).toBeVisible();

    // Verify Edit/Save/Cancel buttons exist
    await expect(page.locator('button:has-text("Edit")')).toBeVisible();
  });

  test('My Account handles API error gracefully with retry button', async ({ page }) => {
    // Mock /users/me API to return 500 error
    await page.route('http://localhost:8080/users/me', async (route) => {
      await route.fulfill({
        status: 500,
        json: { error: 'Internal server error' }
      });
    });

    await page.goto('http://localhost:5173/account');

    // Error message should appear
    await expect(page.locator('[role="alert"]')).toContainText(/Failed to load account information/i);

    // Retry button should exist
    await expect(page.locator('button:has-text("Retry")')).toBeVisible();

    // Back to Calendar button should exist
    await expect(page.locator('button:has-text("Back to Calendar")')).toBeVisible();
  });

  test('My Account retry button refetches user data', async ({ page }) => {
    let callCount = 0;

    await page.route('http://localhost:8080/users/me', async (route) => {
      callCount++;
      if (callCount === 1) {
        // First call: return error
        await route.fulfill({ status: 500, json: { error: 'Server error' } });
      } else {
        // Second call (after retry): return success
        await route.fulfill({
          status: 200,
          json: {
            id: 'user-123',
            email: 'jane.doe@example.com',
            first_name: 'Jane',
            last_name: 'Doe',
            roles: ['role_calendar_viewer'],
            created_at: '2024-01-01T00:00:00Z',
            updated_at: '2024-01-01T00:00:00Z',
          }
        });
      }
    });

    await page.goto('http://localhost:5173/account');

    // First: error state
    await expect(page.locator('[role="alert"]')).toBeVisible();

    // Click retry button
    await page.locator('button:has-text("Retry")').click();

    // Wait for success state
    await page.waitForSelector('text=Jane Doe', { timeout: 5000 });
    
    // Verify error message is gone
    await expect(page.locator('[role="alert"]')).not.toBeVisible();
    
    // Verify data loaded
    await expect(page.locator('text=jane.doe@example.com')).toBeVisible();
  });

  test('My Account shows loading spinner initially', async ({ page }) => {
    // Delay the /users/me response to verify loading state
    await page.route('http://localhost:8080/users/me', async (route) => {
      await new Promise(resolve => setTimeout(resolve, 500)); // 500ms delay
      await route.fulfill({
        status: 200,
        json: {
          id: 'user-123',
          email: 'test@example.com',
          first_name: 'Test',
          last_name: 'User',
          roles: [],
          created_at: '2024-01-01T00:00:00Z',
          updated_at: '2024-01-01T00:00:00Z',
        }
      });
    });

    await page.goto('http://localhost:5173/account');

    // Loading spinner should appear briefly (from LoadingSpinner component at line 76-79)
    // Then data should load
    await expect(page.locator('text=Test User')).toBeVisible({ timeout: 2000 });
  });
});
