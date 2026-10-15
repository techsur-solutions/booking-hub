import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import MockAdapter from 'axios-mock-adapter';
import { apiClient } from './client';

// Mock the keycloak module so we can control keycloak.token, keycloak.updateToken, and keycloak.login
vi.mock('../auth/keycloak', () => ({
  keycloak: {
    authenticated: false,
    token: undefined as string | undefined,
    updateToken: vi.fn().mockResolvedValue(true),
    login: vi.fn().mockResolvedValue(undefined),
  },
}));

describe('API Client', () => {
  let mock: MockAdapter;

  beforeEach(() => {
    mock = new MockAdapter(apiClient);
  });

  afterEach(() => {
    mock.restore();
    vi.clearAllMocks();
  });

  it('includes JWT bearer token in request headers when token is set', async () => {
    const { keycloak } = await import('../auth/keycloak');
    keycloak.authenticated = true;
    keycloak.token = 'fake-jwt-token';

    let capturedAuthHeader: string | undefined;
    mock.onGet('/test').reply((config) => {
      capturedAuthHeader = config.headers?.Authorization as string | undefined;
      return [200, { ok: true }];
    });

    await apiClient.get('/test');
    expect(capturedAuthHeader).toBe('Bearer fake-jwt-token');

    // Clean up
    keycloak.authenticated = false;
    keycloak.token = undefined;
  });

  it('does not set Authorization header when no token present', async () => {
    const { keycloak } = await import('../auth/keycloak');
    keycloak.token = undefined;

    let capturedAuthHeader: string | undefined;
    mock.onGet('/no-token').reply((config) => {
      capturedAuthHeader = config.headers?.Authorization as string | undefined;
      return [200, {}];
    });

    await apiClient.get('/no-token');
    expect(capturedAuthHeader).toBeUndefined();
  });

  it('calls keycloak.login() on 401 response', async () => {
    const { keycloak } = await import('../auth/keycloak');
    const loginSpy = vi.spyOn(keycloak, 'login').mockResolvedValue(undefined);

    mock.onGet('/unauthorized').reply(401);

    await expect(apiClient.get('/unauthorized')).rejects.toThrow();
    expect(loginSpy).toHaveBeenCalled();
  });

  it('does not call keycloak.login() on non-401 errors', async () => {
    const { keycloak } = await import('../auth/keycloak');
    const loginSpy = vi.spyOn(keycloak, 'login').mockResolvedValue(undefined);

    mock.onGet('/server-error').reply(500);

    await expect(apiClient.get('/server-error')).rejects.toThrow();
    expect(loginSpy).not.toHaveBeenCalled();
  });
});
