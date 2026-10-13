import type { ReactNode } from 'react';
import { hasRole, hasAnyRole } from '../utils/permissions';

export interface PermissionGateProps {
  children: ReactNode;
  requireRole?: string; // Single required role
  requireAnyRole?: string[]; // At least one of these roles
  fallback?: ReactNode; // Optional fallback when permission denied (default: null)
}

export function PermissionGate({
  children,
  requireRole,
  requireAnyRole,
  fallback = null,
}: PermissionGateProps) {
  let hasPermission = true;

  if (requireRole) {
    hasPermission = hasRole(requireRole);
  } else if (requireAnyRole) {
    hasPermission = hasAnyRole(requireAnyRole);
  }

  if (!hasPermission) {
    return <>{fallback}</>;
  }

  return <>{children}</>;
}
