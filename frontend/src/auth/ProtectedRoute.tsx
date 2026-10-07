import { useEffect } from 'react';
import type { ReactNode } from 'react';
import { useAuth } from './AuthProvider';

interface ProtectedRouteProps {
  children: ReactNode;
}

export function ProtectedRoute({ children }: ProtectedRouteProps) {
  const { authenticated, keycloak, loading } = useAuth();

  useEffect(() => {
    // Skip redirect in E2E mode
    if (import.meta.env.VITE_E2E_MODE === 'true') {
      return;
    }

    // If not authenticated and not loading, trigger login redirect
    if (!authenticated && !loading) {
      keycloak.login();
    }
  }, [authenticated, loading, keycloak]);

  // Don't render children until authentication is confirmed
  // This prevents any flash of protected content
  if (!authenticated) {
    return null;
  }

  return <>{children}</>;
}
