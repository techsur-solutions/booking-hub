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

export function AuthProvider({ children }: AuthProviderProps) {
  const [authenticated, setAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // E2E test mode bypass - skip real Keycloak initialization
    if (import.meta.env.VITE_E2E_MODE === 'true') {
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
