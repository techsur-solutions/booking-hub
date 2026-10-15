import { Link } from 'react-router-dom';
import { UserTable } from '../../components/admin/UserTable';

/**
 * UsersAdmin — /admin/users
 *
 * Lists all users with Name, Email, Roles, Active status, and Actions.
 * "Create User" button (top-right) routes to /admin/users/new.
 * Edit icon in each row routes to /admin/users/:id/edit.
 *
 * Implements UI-ADMIN-04: Users Admin with admin vs self restrictions.
 * Requires role_user_manager (enforced by Sidebar permission gate and Gateway Tier-1).
 */
export function UsersAdmin() {
  return (
    <div className="users-admin" data-testid="users-admin">
      <div className="users-admin__header">
        <h1 className="users-admin__title">Users Admin</h1>
        <Link
          to="/admin/users/new"
          className="users-admin__create-btn"
          data-testid="create-user-btn"
        >
          + Create User
        </Link>
      </div>
      <UserTable />
    </div>
  );
}
