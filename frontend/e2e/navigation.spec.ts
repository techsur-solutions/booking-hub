import { test, expect } from '@playwright/test';

test.describe('Navigation', () => {
  test('should redirect root to /calendar', async ({ page }) => {
    await page.goto('/');
    await expect(page).toHaveURL('/calendar');
    await expect(page.locator('h1')).toContainText('Calendar View');
  });

  test('should navigate to all booking views', async ({ page }) => {
    await page.goto('/');
    
    // Calendar (already loaded)
    await expect(page.locator('h1')).toContainText('Calendar View');
    
    // List
    await page.click('a[href="/list"]');
    await expect(page).toHaveURL('/list');
    await expect(page.locator('h1')).toContainText('List View');
    
    // Day
    await page.click('a[href="/day"]');
    await expect(page).toHaveURL('/day');
    await expect(page.locator('h1')).toContainText('Day View');
  });

  test('should navigate to all admin pages', async ({ page }) => {
    await page.goto('/');
    
    // Locations
    await page.click('a[href="/admin/locations"]');
    await expect(page).toHaveURL('/admin/locations');
    await expect(page.locator('h1')).toContainText('Locations Admin');
    
    // Resources
    await page.click('a[href="/admin/resources"]');
    await expect(page).toHaveURL('/admin/resources');
    await expect(page.locator('h1')).toContainText('Resources Admin');
    
    // Custom Fields
    await page.click('a[href="/admin/custom-fields"]');
    await expect(page).toHaveURL('/admin/custom-fields');
    await expect(page.locator('h1')).toContainText('Custom Fields Admin');
    
    // Users
    await page.click('a[href="/admin/users"]');
    await expect(page).toHaveURL('/admin/users');
    await expect(page.locator('h1')).toContainText('Users Admin');
    
    // Roles
    await page.click('a[href="/admin/roles"]');
    await expect(page).toHaveURL('/admin/roles');
    await expect(page.locator('h1')).toContainText('Roles Admin');
    
    // Settings
    await page.click('a[href="/admin/settings"]');
    await expect(page).toHaveURL('/admin/settings');
    await expect(page.locator('h1')).toContainText('Settings Admin');
  });

  test('should navigate to audit log and feeds', async ({ page }) => {
    await page.goto('/');
    
    // Audit Log
    await page.click('a[href="/audit-log"]');
    await expect(page).toHaveURL('/audit-log');
    await expect(page.locator('h1')).toContainText('Audit Log Viewer');
    
    // Feeds
    await page.click('a[href="/feeds"]');
    await expect(page).toHaveURL('/feeds');
    await expect(page.locator('h1')).toContainText('Public Feeds Landing');
  });

  test('should navigate to display board via link and render without nav', async ({ page }) => {
    await page.goto('/feeds');
    
    // Click link to display board
    await page.click('a[href="/display-board"]');
    await expect(page).toHaveURL('/display-board');
    await expect(page.locator('h1')).toContainText('Display Board');
    
    // Verify no nav chrome present
    await expect(page.locator('nav')).toHaveCount(0);
  });

  test('should render nav links for all routes except display board', async ({ page }) => {
    await page.goto('/');
    
    // Verify presence of all expected nav links
    await expect(page.locator('a[href="/calendar"]')).toBeVisible();
    await expect(page.locator('a[href="/list"]')).toBeVisible();
    await expect(page.locator('a[href="/day"]')).toBeVisible();
    await expect(page.locator('a[href="/admin/locations"]')).toBeVisible();
    await expect(page.locator('a[href="/admin/resources"]')).toBeVisible();
    await expect(page.locator('a[href="/admin/custom-fields"]')).toBeVisible();
    await expect(page.locator('a[href="/admin/users"]')).toBeVisible();
    await expect(page.locator('a[href="/admin/roles"]')).toBeVisible();
    await expect(page.locator('a[href="/admin/settings"]')).toBeVisible();
    await expect(page.locator('a[href="/audit-log"]')).toBeVisible();
    await expect(page.locator('a[href="/feeds"]')).toBeVisible();
  });
});
