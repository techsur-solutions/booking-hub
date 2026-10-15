import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '../auth/AuthProvider';
import { usersApi } from '../api/users';
import { StateConfirmationBanner } from '../components/StateConfirmationBanner';
import { LoadingSpinner } from '../components/LoadingSpinner';
import type { User } from '../api/types';

/**
 * My Account self-service page — UI-AUTH-02
 *
 * Displays:
 * - Name (editable inline with Edit/Save/Cancel)
 * - Email (read-only, greyed out)
 * - Roles (read-only chip list from keycloak.realmAccess.roles)
 *
 * Actions:
 * - Change Password → keycloak.accountManagement()
 * - View My Audit History → /admin/audit?actor={user.id}
 */
export function MyAccount() {
  const { keycloak } = useAuth();
  const queryClient = useQueryClient();

  // Fetch current user
  const { data: user, isLoading, isError, error, refetch } = useQuery<User>({
    queryKey: ['users', 'me'],
    queryFn: async () => {
      // No need for per-call keycloak.updateToken() - client.ts interceptor handles it globally
      try {
        console.log('[MyAccount] Fetching user profile');
        const response = await usersApi.getMe();
        console.log('[MyAccount] User profile loaded:', response.data.email);
        return response.data;
      } catch (err) {
        console.error('[MyAccount] Failed to load user profile:', err);
        throw err;
      }
    },
    retry: 2, // Retry failed requests 2 times
    staleTime: 2 * 60 * 1000, // Cache for 2 minutes (profile data changes infrequently)
  });

  // Edit state
  const [editing, setEditing] = useState(false);
  const [editName, setEditName] = useState('');

  // Confirmation banner
  const [banner, setBanner] = useState<{ timestamp: Date } | null>(null);

  // Save name mutation
  const saveMutation = useMutation({
    mutationFn: (name: string) => {
      return usersApi.updateMe({ display_name: name.trim() });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users', 'me'] });
      setEditing(false);
      setBanner({ timestamp: new Date() });
    },
  });

  function startEditing() {
    setEditName(user?.display_name ?? '');
    setEditing(true);
  }

  function cancelEditing() {
    setEditing(false);
    setEditName('');
  }

  function handleSave() {
    if (editName.trim()) {
      saveMutation.mutate(editName.trim());
    }
  }

  // Roles from Keycloak token
  const roles: string[] = keycloak.realmAccess?.roles ?? [];
  const displayName = user?.display_name ?? '—';

  // Check authentication before rendering
  if (!keycloak.authenticated) {
    return (
      <div style={{ padding: 'var(--spacing-xl)', textAlign: 'center' }}>
        <p>Please log in to view your account.</p>
        <button onClick={() => keycloak.login()} className="btn btn--primary">
          Log In
        </button>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div style={{ padding: 'var(--spacing-2xl)', display: 'flex', justifyContent: 'center' }}>
        <LoadingSpinner />
      </div>
    );
  }

  if (isError) {
    return (
      <div style={{ maxWidth: '600px', margin: '0 auto', padding: 'var(--spacing-xl)' }}>
        <h1 style={{ marginBottom: 'var(--spacing-xl)' }}>My Account</h1>
        
        <div
          role="alert"
          style={{
            padding: 'var(--spacing-lg)',
            backgroundColor: 'var(--color-danger-light)',
            color: 'var(--color-danger-dark)',
            borderRadius: 'var(--radius-md)',
            marginBottom: 'var(--spacing-md)',
          }}
        >
          <p style={{ marginBottom: 'var(--spacing-md)', fontWeight: 'bold' }}>
            Failed to load account information.
          </p>
          <p style={{ marginBottom: 'var(--spacing-md)' }}>
            {(error as any)?.response?.status === 404 ? (
              <>
                <strong>User record not found.</strong> Your Keycloak account exists, but no matching user record was found in the database.
              </>
            ) : (
              'This may be due to a network issue, expired authentication token, or the server being temporarily unavailable.'
            )}
          </p>
          {(error as any)?.response?.status === 404 && (
            <div style={{
              padding: 'var(--spacing-md)',
              backgroundColor: 'var(--color-info-light)',
              borderRadius: 'var(--radius-md)',
              marginBottom: 'var(--spacing-md)',
              fontSize: 'var(--font-size-sm)',
            }}>
              <p style={{ marginBottom: 'var(--spacing-sm)', fontWeight: 'bold' }}>
                For developers:
              </p>
              <ol style={{ marginLeft: 'var(--spacing-lg)', marginBottom: 0 }}>
                <li>Ensure your Keycloak user UUID matches the seed data in V3 migration</li>
                <li>Restart users-permissions-service to apply migrations: <code>docker compose restart users-permissions-service</code></li>
                <li>Or manually create your user record in users_db.users table</li>
              </ol>
            </div>
          )}
          <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--color-text-muted)' }}>
            Technical error: {error instanceof Error ? error.message : 'Unknown error'}
          </p>
        </div>

        <button
          onClick={() => refetch()}
          className="btn btn--primary"
          style={{ marginRight: 'var(--spacing-md)' }}
        >
          Retry
        </button>
        
        <button
          onClick={() => window.location.href = '/bookings'}
          className="btn btn--secondary"
        >
          Back to Calendar
        </button>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '600px', margin: '0 auto', padding: 'var(--spacing-xl)' }}>
      <h1 style={{ marginBottom: 'var(--spacing-xl)' }}>My Account</h1>

      {/* Success banner */}
      {banner && (
        <StateConfirmationBanner
          action="Name updated"
          timestamp={banner.timestamp}
          onDismiss={() => setBanner(null)}
        />
      )}

      {/* Save error */}
      {saveMutation.isError && (
        <div
          role="alert"
          style={{
            marginBottom: 'var(--spacing-md)',
            padding: 'var(--spacing-sm) var(--spacing-md)',
            background: '#fef2f2',
            border: '1px solid var(--color-danger)',
            borderRadius: 'var(--border-radius)',
            color: 'var(--color-danger-dark)',
            fontSize: 'var(--font-size-sm)',
          }}
        >
          Failed to save name. Please try again.
        </div>
      )}

      <div
        style={{
          background: 'var(--color-bg)',
          border: '1px solid var(--color-border)',
          borderRadius: 'var(--border-radius-lg)',
          overflow: 'hidden',
        }}
      >
        {/* Name field */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            padding: 'var(--spacing-md) var(--spacing-lg)',
            borderBottom: '1px solid var(--color-border)',
            gap: 'var(--spacing-md)',
          }}
        >
          <div style={{ flex: 1 }}>
            <div
              style={{
                fontSize: 'var(--font-size-xs)',
                fontWeight: 'var(--font-weight-medium)',
                color: 'var(--color-text-light)',
                marginBottom: 'var(--spacing-xs)',
                textTransform: 'uppercase',
                letterSpacing: '0.05em',
              }}
            >
              Name
            </div>
            {editing ? (
              <input
                type="text"
                value={editName}
                onChange={(e) => setEditName(e.target.value)}
                autoFocus
                data-testid="name-edit-input"
                onKeyDown={(e) => {
                  if (e.key === 'Enter') handleSave();
                  if (e.key === 'Escape') cancelEditing();
                }}
                style={{
                  padding: 'var(--spacing-xs) var(--spacing-sm)',
                  border: '1px solid var(--color-border-focus)',
                  borderRadius: 'var(--border-radius)',
                  fontSize: 'var(--font-size-base)',
                  width: '100%',
                  outline: 'none',
                  boxShadow: 'var(--focus-ring)',
                }}
              />
            ) : (
              <div style={{ fontSize: 'var(--font-size-base)' }} data-testid="name-display">
                {displayName}
              </div>
            )}
          </div>

          {/* Edit / Save / Cancel buttons */}
          {!editing ? (
            <button
              type="button"
              onClick={startEditing}
              data-testid="name-edit-button"
              style={{
                padding: 'var(--spacing-xs) var(--spacing-md)',
                border: '1px solid var(--color-border)',
                borderRadius: 'var(--border-radius)',
                background: 'var(--color-bg)',
                cursor: 'pointer',
                fontSize: 'var(--font-size-sm)',
                color: 'var(--color-text)',
              }}
            >
              Edit
            </button>
          ) : (
            <div style={{ display: 'flex', gap: 'var(--spacing-sm)' }}>
              <button
                type="button"
                onClick={handleSave}
                disabled={saveMutation.isPending}
                data-testid="name-save-button"
                style={{
                  padding: 'var(--spacing-xs) var(--spacing-md)',
                  border: 'none',
                  borderRadius: 'var(--border-radius)',
                  background: 'var(--color-primary)',
                  color: '#fff',
                  cursor: saveMutation.isPending ? 'not-allowed' : 'pointer',
                  fontSize: 'var(--font-size-sm)',
                  fontWeight: 'var(--font-weight-medium)',
                }}
              >
                {saveMutation.isPending ? 'Saving…' : 'Save'}
              </button>
              <button
                type="button"
                onClick={cancelEditing}
                data-testid="name-cancel-button"
                style={{
                  padding: 'var(--spacing-xs) var(--spacing-md)',
                  border: '1px solid var(--color-border)',
                  borderRadius: 'var(--border-radius)',
                  background: 'var(--color-bg)',
                  cursor: 'pointer',
                  fontSize: 'var(--font-size-sm)',
                  color: 'var(--color-text)',
                }}
              >
                Cancel
              </button>
            </div>
          )}
        </div>

        {/* Email field — read-only */}
        <div
          style={{
            padding: 'var(--spacing-md) var(--spacing-lg)',
            borderBottom: '1px solid var(--color-border)',
          }}
        >
          <div
            style={{
              fontSize: 'var(--font-size-xs)',
              fontWeight: 'var(--font-weight-medium)',
              color: 'var(--color-text-light)',
              marginBottom: 'var(--spacing-xs)',
              textTransform: 'uppercase',
              letterSpacing: '0.05em',
            }}
          >
            Email
          </div>
          <div
            style={{
              fontSize: 'var(--font-size-base)',
              color: 'var(--color-text-light)',
              display: 'flex',
              alignItems: 'center',
              gap: 'var(--spacing-sm)',
            }}
            data-testid="email-display"
          >
            {user?.email ?? '—'}
            <span
              title="Contact admin to change email"
              style={{
                fontSize: 'var(--font-size-xs)',
                color: 'var(--color-text-muted)',
                background: 'var(--color-bg-hover)',
                padding: '2px 6px',
                borderRadius: 'var(--border-radius-full)',
                cursor: 'help',
              }}
            >
              Read-only
            </span>
          </div>
        </div>

        {/* Roles — read-only chips */}
        <div
          style={{
            padding: 'var(--spacing-md) var(--spacing-lg)',
          }}
        >
          <div
            style={{
              fontSize: 'var(--font-size-xs)',
              fontWeight: 'var(--font-weight-medium)',
              color: 'var(--color-text-light)',
              marginBottom: 'var(--spacing-sm)',
              textTransform: 'uppercase',
              letterSpacing: '0.05em',
            }}
          >
            Roles
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 'var(--spacing-xs)' }} data-testid="roles-list">
            {roles.length === 0 ? (
              <span style={{ color: 'var(--color-text-muted)', fontSize: 'var(--font-size-sm)' }}>
                No roles assigned
              </span>
            ) : (
              roles.map((role) => (
                <span
                  key={role}
                  style={{
                    padding: '2px 10px',
                    background: 'var(--color-primary-light)',
                    color: 'var(--color-primary-dark)',
                    borderRadius: 'var(--border-radius-full)',
                    fontSize: 'var(--font-size-xs)',
                    fontWeight: 'var(--font-weight-medium)',
                  }}
                >
                  {role}
                </span>
              ))
            )}
          </div>
        </div>
      </div>

      {/* Actions */}
      <div
        style={{
          marginTop: 'var(--spacing-xl)',
          display: 'flex',
          flexDirection: 'column',
          gap: 'var(--spacing-md)',
        }}
      >
        {/* Change Password → Keycloak account management */}
        <button
          type="button"
          onClick={() => keycloak.accountManagement()}
          data-testid="change-password-button"
          style={{
            padding: 'var(--spacing-sm) var(--spacing-md)',
            border: '1px solid var(--color-border)',
            borderRadius: 'var(--border-radius)',
            background: 'var(--color-bg)',
            cursor: 'pointer',
            fontSize: 'var(--font-size-base)',
            color: 'var(--color-text)',
            fontWeight: 'var(--font-weight-medium)',
            textAlign: 'left',
            width: '100%',
            display: 'flex',
            alignItems: 'center',
            gap: 'var(--spacing-sm)',
          }}
        >
          <span aria-hidden="true">🔑</span>
          Change Password
          <span
            style={{
              marginLeft: 'auto',
              fontSize: 'var(--font-size-xs)',
              color: 'var(--color-text-muted)',
            }}
          >
            Opens Keycloak
          </span>
        </button>

        {/* View My Audit History */}
        {user && (
          <a
            href={`/admin/audit?actor=${user.id}`}
            data-testid="audit-history-link"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 'var(--spacing-sm)',
              padding: 'var(--spacing-sm) var(--spacing-md)',
              border: '1px solid var(--color-border)',
              borderRadius: 'var(--border-radius)',
              color: 'var(--color-text)',
              textDecoration: 'none',
              fontSize: 'var(--font-size-base)',
              fontWeight: 'var(--font-weight-medium)',
              background: 'var(--color-bg)',
            }}
          >
            <span aria-hidden="true">📋</span>
            View My Audit History
          </a>
        )}
      </div>
    </div>
  );
}
