import { createContext, useContext, useEffect, useState } from 'react';
import type { ReactNode } from 'react';
import { keycloak } from './keycloak';
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
 * In E2E mode (VITE_E2E_MODE=true), optionally inject mock roles via
 * VITE_E2E_ROLES env var (comma-separated role names) so Playwright tests
 * can verify permission-gated navigation without a live Keycloak server.
 */
function applyE2EMockRoles(kc: Keycloak): void {
  const rolesEnv = import.meta.env.VITE_E2E_ROLES as string | undefined;
  if (!rolesEnv) return;
  const roles = rolesEnv
    .split(',')
    .map((r: string) => r.trim())
    .filter(Boolean);
  // Mutate keycloak's realmAccess so Sidebar reads the injected roles
  (kc as unknown as Record<string, unknown>).realmAccess = { roles };
  // Also set a minimal tokenParsed for UserMenu display name
  (kc as unknown as Record<string, unknown>).tokenParsed = {
    name: 'E2E Test User',
    preferred_username: 'e2e-user',
    realm_access: { roles },
  };
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
