import { test, expect } from '@playwright/test';

test.describe('Auth Redirect', () => {
  test('should redirect to Keycloak for protected routes with PKCE', async ({ page }) => {
    // Track intercepted requests
    const keycloakRequests: string[] = [];
    
    // Intercept Keycloak auth endpoint
    await page.route('**/realms/bookinghub/protocol/openid-connect/auth**', (route) => {
      keycloakRequests.push(route.request().url());
      // Fulfill with a simple response to prevent actual navigation
      route.fulfill({
        status: 200,
        body: '<html><body>Auth intercepted</body></html>',
      });
    });

    // Navigate to a protected route (calendar)
    await page.goto('/calendar');

    // Wait a bit for the redirect to be attempted
    await page.waitForTimeout(1000);

    // Verify a Keycloak auth request was made
    expect(keycloakRequests.length).toBeGreaterThan(0);
    
    // Verify the request URL contains PKCE parameters
    const authUrl = keycloakRequests[0];
    expect(authUrl).toContain('/realms/bookinghub/protocol/openid-connect/auth');
    expect(authUrl).toContain('code_challenge_method=S256');
    expect(authUrl).toContain('code_challenge=');
    
    // Verify the Calendar placeholder content was NOT rendered
    // (because ProtectedRoute prevents rendering until authenticated)
    const heading = await page.locator('h1:has-text("Calendar View")').count();
    expect(heading).toBe(0);
  });

  test('should allow public routes without redirect - feeds', async ({ page }) => {
    const keycloakRequests: string[] = [];
    
    await page.route('**/realms/bookinghub/protocol/openid-connect/auth**', (route) => {
      keycloakRequests.push(route.request().url());
      route.abort();
    });

    // Navigate to public feeds route
    await page.goto('/feeds');

    // Wait for content to render
    await page.waitForSelector('h1');

    // Verify NO Keycloak auth request was made
    expect(keycloakRequests.length).toBe(0);
    
    // Verify the Feeds content rendered directly
    await expect(page.locator('h1')).toContainText('Public Feeds Landing');
  });

  test('should allow public routes without redirect - display board', async ({ page }) => {
    const keycloakRequests: string[] = [];
    
    await page.route('**/realms/bookinghub/protocol/openid-connect/auth**', (route) => {
      keycloakRequests.push(route.request().url());
      route.abort();
    });

    // Navigate to public display board route
    await page.goto('/display-board');

    // Wait for content to render
    await page.waitForSelector('h1');

    // Verify NO Keycloak auth request was made
    expect(keycloakRequests.length).toBe(0);
    
    // Verify the Display Board content rendered directly
    await expect(page.locator('h1')).toContainText('Display Board');
    
    // Verify no nav chrome is present (full-screen kiosk layout)
    await expect(page.locator('nav')).toHaveCount(0);
  });
});
