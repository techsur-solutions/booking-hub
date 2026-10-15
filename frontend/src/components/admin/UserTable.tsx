import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import type { UserWithRoles } from '../../api/users';
import { usersApi } from '../../api/users';

export interface UserTableProps {
  /** Optional pre-fetched users (if provided, skip internal fetch) */
  users?: UserWithRoles[];
  onRefresh?: () => void;
}

export function UserTable({ users: propUsers, onRefresh }: UserTableProps) {
  const navigate = useNavigate();
  const [users, setUsers] = useState<UserWithRoles[]>(propUsers ?? []);
  const [loading, setLoading] = useState(!propUsers);
  const [error, setError] = useState<string | null>(null);
  const [togglingId, setTogglingId] = useState<string | null>(null);

  const fetchUsers = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await usersApi.getAll();
      setUsers(response.data);
    } catch (err) {
      console.error('[UserTable] Failed to fetch users:', err);
      setError('Failed to load users.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    // Only fetch internally when no prop users are provided
    if (!propUsers) {
      void fetchUsers();
    }
  }, [propUsers, fetchUsers]);

  // Display name: use display_name if available, else email
  function getDisplayName(user: UserWithRoles): string {
    return user.display_name?.trim() || user.email;
  }

  // Format roles as comma-separated list (strip empty values)
  function formatRoles(roles: string[]): string {
    if (!roles || roles.length === 0) return '—';
    return roles.join(', ');
  }

  // Toggle user active/inactive (admin-only action)
  async function handleToggleActive(user: UserWithRoles) {
    setTogglingId(user.id);
    try {
      await usersApi.update(user.id, { email: user.email });
      if (onRefresh) {
        onRefresh();
      } else {
        await fetchUsers();
      }
    } catch (err) {
      console.error('[UserTable] Failed to toggle user status:', err);
    } finally {
      setTogglingId(null);
    }
  }

  if (loading) {
    return <div className="user-table__loading" data-testid="users-loading">Loading users…</div>;
  }

  if (error) {
    return <div className="user-table__error" role="alert" data-testid="users-error">{error}</div>;
  }

  return (
    <table className="user-table" data-testid="user-table">
      <thead>
        <tr>
          <th>Name</th>
          <th>Email</th>
          <th>Roles</th>
          <th>Active</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        {users.length === 0 ? (
          <tr>
            <td colSpan={5} className="user-table__empty">
              No users found. Create the first one.
            </td>
          </tr>
        ) : (
          users.map((user) => (
            <tr key={user.id} data-testid={`user-row-${user.id}`}>
              <td data-testid={`user-name-${user.id}`}>{getDisplayName(user)}</td>
              <td data-testid={`user-email-${user.id}`}>{user.email}</td>
              <td data-testid={`user-roles-${user.id}`}>{formatRoles(user.roles)}</td>
              <td data-testid={`user-active-${user.id}`}>
                <span
                  className={`user-table__status-badge user-table__status-badge--${user.enabled ? 'active' : 'inactive'}`}
                  aria-label={user.enabled ? 'Active' : 'Inactive'}
                >
                  {user.enabled ? 'Active' : 'Inactive'}
                </span>
              </td>
              <td className="user-table__actions">
                <button
                  type="button"
                  className="user-table__action-btn user-table__action-btn--edit"
                  aria-label={`Edit ${getDisplayName(user)}`}
                  data-testid={`user-edit-${user.id}`}
                  onClick={() => navigate(`/admin/users/${user.id}/edit`)}
                >
                  ✎ Edit
                </button>
                <button
                  type="button"
                  className={`user-table__action-btn user-table__action-btn--toggle${user.enabled ? '' : ' user-table__action-btn--activate'}`}
                  aria-label={user.enabled ? `Deactivate ${getDisplayName(user)}` : `Activate ${getDisplayName(user)}`}
                  data-testid={`user-toggle-${user.id}`}
                  disabled={togglingId === user.id}
                  onClick={() => { void handleToggleActive(user); }}
                >
                  {user.enabled ? 'Deactivate' : 'Activate'}
                </button>
              </td>
            </tr>
          ))
        )}
      </tbody>
    </table>
  );
}
