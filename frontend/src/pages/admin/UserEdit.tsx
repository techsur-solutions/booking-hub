import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UserEditForm } from '../../components/admin/UserEditForm';
import type { UserEditFormValues } from '../../components/admin/UserEditForm';
import { StateConfirmationBanner } from '../../components/StateConfirmationBanner';
import { usersApi } from '../../api/users';
import type { UserWithRoles } from '../../api/users';
import { keycloak } from '../../auth/keycloak';

/**
 * UserEdit — route page for /admin/users/:id/edit
 *
 * Fetches the user by id via usersApi.getById(id).
 * Passes userId from route param to UserEditForm for isSelfEdit determination.
 * Renders UserEditForm pre-filled with existing values.
 *
 * On success:
 *   - If self-edit: calls usersApi.updateSelf() → PUT /users/me
 *   - If admin edit: calls usersApi.update(userId, ...) → PUT /users/:id
 * Shows StateConfirmationBanner, navigates back to /admin/users.
 */
export function UserEdit() {
  const navigate = useNavigate();
  const { id: userId } = useParams<{ id: string }>();
  const [user, setUser] = useState<UserWithRoles | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [fetchError, setFetchError] = useState<string | null>(null);
  const [saveError, setSaveError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [successTime, setSuccessTime] = useState<Date | null>(null);

  // Reads Keycloak sub claim to determine if editing self or another user
  // In E2E mode, keycloak.subject is injected via AuthProvider applyE2EMockRoles
  const currentUserId = keycloak.tokenParsed?.sub ?? keycloak.subject ?? '';

  useEffect(() => {
    if (!userId) {
      setFetchError('Invalid user ID in URL.');
      setLoading(false);
      return;
    }
    void (async () => {
      setLoading(true);
      setFetchError(null);
      try {
        const response = await usersApi.getById(userId);
        setUser(response.data);
      } catch (err) {
        console.error('[UserEdit] Failed to fetch user:', err);
        setFetchError('Failed to load user. Please go back and try again.');
      } finally {
        setLoading(false);
      }
    })();
  }, [userId]);

  async function handleSubmit(values: UserEditFormValues, isSelf: boolean) {
    if (!userId) return;
    setSaving(true);
    setSaveError(null);
    try {
      if (isSelf) {
        // Self-update: only display_name, uses /users/me endpoint
        // UserSelfUpdateRequest type has NO password/roles fields (Phase 3 Plan 04 type-level guard)
        await usersApi.updateSelf({ display_name: (values as { display_name: string }).display_name });
      } else {
        // Admin editing another user: display_name, email, roles (no password)
        const adminValues = values as { display_name: string; email: string; roles: string[] };
        await usersApi.update(userId, {
          display_name: adminValues.display_name,
          email: adminValues.email,
          roles: adminValues.roles,
        });
      }
      setSuccess(true);
      setSuccessTime(new Date());
      setTimeout(() => {
        navigate('/admin/users');
      }, 1500);
    } catch (err) {
      console.error('[UserEdit] Failed to save user:', err);
      setSaveError('Failed to save changes. Please try again.');
    } finally {
      setSaving(false);
    }
  }

  function handleCancel() {
    navigate('/admin/users');
  }

  return (
    <div className="user-edit-page" data-testid="user-edit-page">
      <div className="user-edit-page__header">
        <h1 className="user-edit-page__title">Edit User</h1>
      </div>

      {success && successTime && (
        <StateConfirmationBanner
          action="User updated"
          timestamp={successTime}
          onDismiss={() => setSuccess(false)}
          data-testid="edit-success-banner"
        />
      )}

      {saveError && (
        <div className="user-edit-page__error" role="alert" data-testid="edit-error">
          {saveError}
        </div>
      )}

      {loading && (
        <div className="user-edit-page__loading" data-testid="edit-loading">
          Loading user…
        </div>
      )}

      {fetchError && (
        <div className="user-edit-page__error" role="alert" data-testid="fetch-error">
          {fetchError}
        </div>
      )}

      {!loading && !fetchError && user && (
        <UserEditForm
          user={user}
          currentUserId={currentUserId}
          onSubmit={handleSubmit}
          onCancel={handleCancel}
          loading={saving}
        />
      )}
    </div>
  );
}
