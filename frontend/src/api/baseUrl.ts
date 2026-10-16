// Where the browser sends API calls.
//
// In a production build the frontend is served by nginx, which forwards /api/
// to the gateway (frontend/nginx.conf + gateway-proxy.inc). A relative path keeps
// every call on the page's own origin, so the same image works on a laptop,
// behind Pivota's preview proxy and in production: the browser fills in the host.
// `http://localhost:8080` only works on the machine that runs the gateway. Behind
// the preview it sent every request to the tester's own laptop, so every page
// rendered and no data ever loaded.
//
// `npm run dev` has no nginx in front, so it talks to the gateway directly, and
// the Playwright suite mocks that address. VITE_API_BASE_URL overrides both.
export const API_BASE_URL: string =
  import.meta.env.VITE_API_BASE_URL ||
  (import.meta.env.DEV ? 'http://localhost:8080' : '/api');

/**
 * An absolute URL for an API path, for links that leave the page: feed
 * subscriptions are pasted into calendar and RSS apps, which cannot resolve a
 * relative `/api/...`. Built from the page's own origin, so behind the preview
 * it carries the preview host.
 */
export function absoluteApiUrl(path: string): URL {
  return new URL(`${API_BASE_URL}${path}`, window.location.origin);
}
