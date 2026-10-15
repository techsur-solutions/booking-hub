import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { UserCreateForm } from '../../components/admin/UserCreateForm';
import type { UserCreateFormValues } from '../../components/admin/UserCreateForm';
import { StateConfirmationBanner } from '../../components/StateConfirmationBanner';
import { usersApi } from '../../api/users';

/**
 * UserCreate — route page for /admin/users/new
 *
 * Renders the UserCreateForm.
 * On submit: calls usersApi.create() with display_name, email, password (temporary), roles.
 * On success: shows StateConfirmationBanner, navigates back to /admin/users.
 *
 * Admin-only: only visible to users with role_user_manager (enforced by Sidebar + Gateway).
 */
export function UserCreate() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [successTime, setSuccessTime] = useState<Date | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(values: UserCreateFormValues) {
    setLoading(true);
    setError(null);
    try {
      await usersApi.create({
        display_name: values.display_name || undefined,
        email: values.email,
        password: values.password,
        roles: values.roles,
      });
      setSuccess(true);
      setSuccessTime(new Date());
      // Navigate back after brief banner display
      setTimeout(() => {
        navigate('/admin/users');
      }, 1500);
    } catch (err) {
      console.error('[UserCreate] Failed to create user:', err);
      setError('Failed to create user. The email may already be in use.');
    } finally {
      setLoading(false);
    }
  }

  function handleCancel() {
    navigate('/admin/users');
  }

  return (
    <div className="user-create-page" data-testid="user-create-page">
      <div className="user-create-page__header">
        <h1 className="user-create-page__title">Create User</h1>
      </div>

      {success && successTime && (
        <StateConfirmationBanner
          action="User created — temporary password set"
          timestamp={successTime}
          onDismiss={() => setSuccess(false)}
          data-testid="create-success-banner"
        />
      )}

      {error && (
        <div className="user-create-page__error" role="alert" data-testid="create-error">
          {error}
        </div>
      )}

      <UserCreateForm
        onSubmit={handleSubmit}
        onCancel={handleCancel}
        loading={loading}
      />
    </div>
  );
}
