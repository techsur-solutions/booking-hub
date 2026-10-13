import { keycloak } from '../auth/keycloak';

export function hasRole(roleName: string): boolean {
  return keycloak.realmAccess?.roles?.includes(roleName) ?? false;
}

export function hasAnyRole(roleNames: string[]): boolean {
  return roleNames.some((role) => hasRole(role));
}

export function hasAllRoles(roleNames: string[]): boolean {
  return roleNames.every((role) => hasRole(role));
}

export function hasApprovePermission(): boolean {
  return hasRole('role_booking_approver');
}

export function hasAnyAdminPermission(): boolean {
  return hasAnyRole([
    'role_location_manager',
    'role_permission_manager',
    'role_audit_viewer',
  ]);
}
