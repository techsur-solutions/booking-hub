import { PermissionGrid } from '../../components/admin/PermissionGrid';
import { keycloak } from '../../auth/keycloak';

/**
 * Role & Permission Matrix page (UI-ADMIN-05)
 *
 * Displays a read-only grid of roles × 17 permission flags.
 * Permission assignment is governed entirely by Keycloak; this page
 * provides visibility and audit capability, NOT a parallel editing surface.
 *
 * Route: /admin/roles
 * Gate: role_permission_manager (enforced by sidebar + ProtectedRoute in routes/index.tsx)
 */
export function RolePermissionMatrix() {
  // Build Keycloak Admin Console roles link from current keycloak config.
  // URL pattern: {keycloak.authServerUrl}/admin/{realm}/console/#/realms/{realm}/roles
  const realm = keycloak.realm ?? 'bookinghub';
  const keycloakAdminUrl = `${(keycloak.authServerUrl ?? '').replace(/\/$/, '')}/admin/${realm}/console/#/realms/${realm}/roles`;

  return (
    <div className="role-permission-matrix-page" data-testid="role-permission-matrix">
      {/* Page header */}
      <div className="page-header role-permission-matrix-header">
        <h1 className="page-title">Role &amp; Permission Matrix</h1>
        <a
          href={keycloakAdminUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="btn btn-secondary keycloak-admin-link"
          data-testid="keycloak-admin-link"
          aria-label="View roles in Keycloak Admin Console (opens in new tab)"
        >
          View in Keycloak Admin ↗
        </a>
      </div>

      {/* Read-only notice — per UI-ADMIN-05 requirement */}
      <div className="read-only-notice" role="note" data-testid="read-only-notice">
        <strong>Read-only:</strong> This matrix is read-only. To modify role assignments, use{' '}
        <a
          href={keycloakAdminUrl}
          target="_blank"
          rel="noopener noreferrer"
          data-testid="keycloak-admin-link-notice"
        >
          Keycloak Admin Console
        </a>
        .
      </div>

      {/* Permission matrix grid */}
      <PermissionGrid />
    </div>
  );
}
