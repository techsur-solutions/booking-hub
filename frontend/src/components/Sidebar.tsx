import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/AuthProvider';

export function Sidebar() {
  const { keycloak } = useAuth();
  const location = useLocation();

  // Read roles from Keycloak JWT: keycloak.realmAccess.roles (populated after login)
  const userRoles: string[] = (keycloak.realmAccess && keycloak.realmAccess.roles as string[]) || [];
  const hasApprovePermission = userRoles.includes('role_booking_approver');
  const hasAnyAdminPermission = userRoles.some(
    (r: string) =>
      r.includes('admin') ||
      r === 'role_location_manager' ||
      r === 'role_permission_manager',
  );

  function isActive(path: string): boolean {
    return location.pathname === path || location.pathname.startsWith(path + '/');
  }

  function linkClass(path: string): string {
    return isActive(path) ? 'sidebar-link sidebar-link--active' : 'sidebar-link';
  }

  return (
    <aside className="sidebar" aria-label="Main navigation">
      <nav className="sidebar-nav">
        <ul className="sidebar-nav-list">
          <li>
            <Link
              to="/bookings?view=month"
              className={linkClass('/bookings')}
            >
              <span className="sidebar-icon" aria-hidden="true">📅</span>
              Calendar
            </Link>
          </li>

          {hasApprovePermission && (
            <li>
              <Link to="/approvals" className={linkClass('/approvals')}>
                <span className="sidebar-icon" aria-hidden="true">✅</span>
                Approvals
              </Link>
            </li>
          )}

          <li>
            <Link to="/feeds" className={linkClass('/feeds')}>
              <span className="sidebar-icon" aria-hidden="true">📡</span>
              Feeds
            </Link>
          </li>

          {hasAnyAdminPermission && (
            <li className="sidebar-section">
              <h3 className="sidebar-section-title">Admin</h3>
              <ul className="sidebar-nav-list sidebar-nav-list--nested">
                <li>
                  <Link to="/admin/locations" className={linkClass('/admin/locations')}>
                    Locations
                  </Link>
                </li>
                <li>
                  <Link to="/admin/resources" className={linkClass('/admin/resources')}>
                    Resources
                  </Link>
                </li>
                <li>
                  <Link
                    to="/admin/custom-fields"
                    className={linkClass('/admin/custom-fields')}
                  >
                    Custom Fields
                  </Link>
                </li>
                <li>
                  <Link to="/admin/users" className={linkClass('/admin/users')}>
                    Users
                  </Link>
                </li>
                <li>
                  <Link to="/admin/roles" className={linkClass('/admin/roles')}>
                    Roles &amp; Permissions
                  </Link>
                </li>
                <li>
                  <Link to="/admin/settings" className={linkClass('/admin/settings')}>
                    Settings
                  </Link>
                </li>
                <li>
                  <Link to="/admin/audit-log" className={linkClass('/admin/audit-log')}>
                    Audit Log
                  </Link>
                </li>
              </ul>
            </li>
          )}

          <li>
            <Link to="/account" className={linkClass('/account')}>
              <span className="sidebar-icon" aria-hidden="true">👤</span>
              My Account
            </Link>
          </li>
        </ul>
      </nav>
    </aside>
  );
}
