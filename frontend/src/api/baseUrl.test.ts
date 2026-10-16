import { afterEach, describe, expect, it, vi } from 'vitest';

// API_BASE_URL is computed when the module loads, so each case stubs the env
// first and imports a fresh copy.
async function load(env: { DEV: boolean; VITE_API_BASE_URL?: string }) {
  vi.resetModules();
  vi.stubEnv('DEV', env.DEV);
  vi.stubEnv('VITE_API_BASE_URL', env.VITE_API_BASE_URL ?? '');
  return import('./baseUrl');
}

afterEach(() => {
  vi.unstubAllEnvs();
  vi.resetModules();
});

describe('API_BASE_URL', () => {
  it('is a same-origin path in a production build, so the browser fills in the host', async () => {
    const { API_BASE_URL, absoluteApiUrl } = await load({ DEV: false });
    expect(API_BASE_URL).toBe('/api');
    expect(absoluteApiUrl('/feeds/ical').toString()).toBe(`${window.location.origin}/api/feeds/ical`);
  });

  it('talks to the gateway directly under `npm run dev`, where there is no nginx', async () => {
    const { API_BASE_URL, absoluteApiUrl } = await load({ DEV: true });
    expect(API_BASE_URL).toBe('http://localhost:8080');
    expect(absoluteApiUrl('/feeds/json').toString()).toBe('http://localhost:8080/feeds/json');
  });

  it('lets VITE_API_BASE_URL override both', async () => {
    const { API_BASE_URL } = await load({ DEV: false, VITE_API_BASE_URL: 'https://api.example.test' });
    expect(API_BASE_URL).toBe('https://api.example.test');
  });

  it('keeps a query string when building an absolute feed link', async () => {
    const { absoluteApiUrl } = await load({ DEV: false });
    const url = absoluteApiUrl('/feeds/rss?api_token=abc');
    expect(url.pathname).toBe('/api/feeds/rss');
    expect(url.searchParams.get('api_token')).toBe('abc');
  });
});
