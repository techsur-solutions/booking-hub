import { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { keycloak } from '../auth/keycloak';
import { saveRememberMe, getRememberMeEmail } from '../utils/rememberMe';
import { useAuth } from '../auth/AuthProvider';

/**
 * Login page — UI-AUTH-01
 *
 * Two UI modes:
 * 1. Unauthenticated: shows logo, "Remember me" checkbox, "Login with Keycloak"
 *    button, and "Forgot password?" link
 * 2. Authenticated: auto-redirects to ?redirect= param or /bookings?view=month
 */
export function Login() {
  const { authenticated } = useAuth();
  const location = useLocation();
  const params = new URLSearchParams(location.search);
  // Validate redirect param is a same-origin relative path (no protocol, no host)
  const rawRedirect = params.get('redirect');
  const redirectTo =
    rawRedirect &&
    // Must start with '/' but NOT '//' (protocol-relative), no ':' (no scheme)
    /^\/[^/]/.test(rawRedirect) &&
    !rawRedirect.includes(':')
      ? rawRedirect
      : '/bookings?view=month';

  // Remember Me state — pre-filled from existing cookie on mount
  const [rememberMe, setRememberMe] = useState(false);
  const storedEmail = getRememberMeEmail();

  // Auto-redirect when already authenticated.
  // Suppressed when window.__E2E_LOGIN_TEST__=true (set by Playwright login tests
  // to prevent E2E mode's instant-authenticated state from redirecting away).
  const isLoginTest =
    typeof window !== 'undefined' &&
    !!(window as unknown as Record<string, unknown>).__E2E_LOGIN_TEST__;

  useEffect(() => {
    if (authenticated && !isLoginTest) {
      // Navigate to intended route
      window.location.replace(redirectTo);
    }
  }, [authenticated, isLoginTest, redirectTo]);

  function handleLogin() {
    if (rememberMe && storedEmail) {
      // Re-save email to reset the 360-day cookie expiry
      saveRememberMe(storedEmail);
    } else if (rememberMe) {
      // No stored email yet — we'll save it after Keycloak returns the token
      // For now, mark the intent so the callback knows to save
      sessionStorage.setItem('rememberMe_pending', 'true');
    }
    keycloak.login({
      redirectUri: window.location.origin + redirectTo,
    });
  }

  // Show a brief "Logging in…" spinner when already authenticated.
  // Skipped in E2E login test mode so tests can interact with the login form.
  if (authenticated && !isLoginTest) {
    return (
      <div className="login-page" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '100vh' }}>
        <div className="login-card" style={{ textAlign: 'center' }}>
          <div
            role="status"
            aria-label="Logging in"
            style={{ fontSize: '1.5rem', marginBottom: '1rem' }}
          >
            ⏳
          </div>
          <p style={{ color: 'var(--color-text-light)' }}>Logging in…</p>
        </div>
      </div>
    );
  }

  return (
    <div
      className="login-page"
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '100vh',
        background: 'var(--color-bg-alt)',
      }}
    >
      <div
        className="login-card"
        style={{
          background: 'var(--color-bg)',
          borderRadius: 'var(--border-radius-lg)',
          boxShadow: 'var(--shadow-xl)',
          padding: 'var(--spacing-2xl)',
          width: '100%',
          maxWidth: '400px',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: 'var(--spacing-lg)',
        }}
      >
        {/* Logo & Title */}
        <div style={{ textAlign: 'center' }}>
          <div
            aria-hidden="true"
            style={{ fontSize: '3rem', lineHeight: 1, marginBottom: 'var(--spacing-sm)' }}
          >
            📅
          </div>
          <h1 style={{ fontSize: 'var(--font-size-2xl)', margin: 0 }}>BookingHub</h1>
          <p style={{ color: 'var(--color-text-light)', marginTop: 'var(--spacing-xs)', marginBottom: 0 }}>
            Room booking made simple
          </p>
        </div>

        {/* Remember Me indicator */}
        {storedEmail && (
          <div
            style={{
              width: '100%',
              padding: 'var(--spacing-sm) var(--spacing-md)',
              background: 'var(--color-bg-alt)',
              borderRadius: 'var(--border-radius)',
              fontSize: 'var(--font-size-sm)',
              color: 'var(--color-text-light)',
              textAlign: 'center',
            }}
            data-testid="remembered-email"
          >
            Logging in as <strong>{storedEmail}</strong>
          </div>
        )}

        {/* Remember Me checkbox */}
        <label
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 'var(--spacing-sm)',
            cursor: 'pointer',
            fontSize: 'var(--font-size-sm)',
            color: 'var(--color-text)',
            alignSelf: 'flex-start',
          }}
        >
          <input
            type="checkbox"
            checked={rememberMe}
            onChange={(e) => setRememberMe(e.target.checked)}
            data-testid="remember-me-checkbox"
            style={{ width: '1rem', height: '1rem', cursor: 'pointer' }}
          />
          Remember me
        </label>

        {/* Login button */}
        <button
          type="button"
          onClick={handleLogin}
          data-testid="login-button"
          style={{
            width: '100%',
            padding: 'var(--spacing-sm) var(--spacing-md)',
            background: 'var(--color-primary)',
            color: '#fff',
            border: 'none',
            borderRadius: 'var(--border-radius)',
            fontWeight: 'var(--font-weight-semibold)',
            fontSize: 'var(--font-size-base)',
            cursor: 'pointer',
            transition: 'background var(--transition-base)',
          }}
          onMouseEnter={(e) => {
            (e.currentTarget as HTMLButtonElement).style.background = 'var(--color-primary-dark)';
          }}
          onMouseLeave={(e) => {
            (e.currentTarget as HTMLButtonElement).style.background = 'var(--color-primary)';
          }}
        >
          Login with Keycloak
        </button>

        {/* Forgot password link */}
        <a
          href="/password-reset"
          style={{ fontSize: 'var(--font-size-sm)', color: 'var(--color-text-light)' }}
          data-testid="forgot-password-link"
        >
          Forgot password?
        </a>
      </div>
    </div>
  );
}
