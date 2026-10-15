import { createContext, useContext, useEffect, useState } from 'react';
import type { ReactNode } from 'react';
import { keycloak } from './keycloak';
import { saveRememberMe } from '../utils/rememberMe';
import type Keycloak from 'keycloak-js';

interface AuthContextType {
  authenticated: boolean;
  keycloak: Keycloak;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}

interface AuthProviderProps {
  children: ReactNode;
}

/**
 * In E2E mode (VITE_E2E_MODE=true), optionally inject mock roles for
 * permission-gated navigation testing. Roles come from either:
 *   1. `window.__E2E_ROLES__` — set via page.addInitScript() in Playwright tests
 *   2. VITE_E2E_ROLES env var — comma-separated string baked at build time
 *
 * This lets individual Playwright tests call
 *   `await page.addInitScript(() => { window.__E2E_ROLES__ = ['role_booking_approver']; })`
 * before navigating to inject a specific permission set per test.
 */
function applyE2EMockRoles(kc: Keycloak): void {
  // Priority: runtime window var (per-test) > build-time env var (server-wide)
  const windowRoles =
    typeof window !== 'undefined'
      ? ((window as unknown as Record<string, unknown>).__E2E_ROLES__ as
          | string[]
          | undefined)
      : undefined;

  const rolesEnv = import.meta.env.VITE_E2E_ROLES as string | undefined;

  let roles: string[] = [];
  if (windowRoles && Array.isArray(windowRoles)) {
    roles = windowRoles.map((r: string) => String(r).trim()).filter(Boolean);
  } else if (rolesEnv) {
    roles = rolesEnv
      .split(',')
      .map((r: string) => r.trim())
      .filter(Boolean);
  }

  if (roles.length === 0) return;

  // Mutate keycloak's realmAccess so Sidebar reads the injected roles
  (kc as unknown as Record<string, unknown>).realmAccess = { roles };
  // Also set a minimal tokenParsed for UserMenu display name
  (kc as unknown as Record<string, unknown>).tokenParsed = {
    name: 'E2E Test User',
    preferred_username: 'e2e-user',
    realm_access: { roles },
  };

  // Inject mock user ID for owner-check in BookingDetailModal (keycloak.subject)
  const windowUserId =
    typeof window !== 'undefined'
      ? ((window as unknown as Record<string, unknown>).__E2E_USER_ID__ as
          | string
          | undefined)
      : undefined;
  if (windowUserId) {
    (kc as unknown as Record<string, unknown>).subject = windowUserId;
  }
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [authenticated, setAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // E2E test mode bypass - skip real Keycloak initialization
    if (import.meta.env.VITE_E2E_MODE === 'true') {
      applyE2EMockRoles(keycloak);
      setAuthenticated(true);
      setLoading(false);
      return;
    }

    keycloak
      .init({
        onLoad: 'check-sso',
        pkceMethod: 'S256',
        silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
      })
      .then((auth) => {
        setAuthenticated(auth);
        setLoading(false);
        // Consume "Remember Me" intent set before the Keycloak redirect.
        // If the user checked "Remember Me" on first login (no stored email yet),
        // Login.tsx wrote 'rememberMe_pending' to sessionStorage. Now that we have
        // a valid token we can read the email from tokenParsed and save the cookie.
        if (auth && sessionStorage.getItem('rememberMe_pending') === 'true') {
          sessionStorage.removeItem('rememberMe_pending');
          const email = keycloak.tokenParsed?.email;
          if (email) {
            saveRememberMe(email);
          }
        }
      })
      .catch((error) => {
        console.error('Keycloak initialization failed', error);
        setLoading(false);
      });
  }, []);

  if (loading) {
    return <div>Loading authentication...</div>;
  }

  return (
    <AuthContext.Provider value={{ authenticated, keycloak, loading }}>
      {children}
    </AuthContext.Provider>
  );
}
