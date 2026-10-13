import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { usersApi } from '../api/users';
import { useToast } from '../components/Toast';

type ResetError =
  | 'token_expired'
  | 'token_used'
  | 'passwords_mismatch'
  | 'invalid_token'
  | 'password_requirements'
  | 'unknown';

const PASSWORD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;

const ERROR_MESSAGES: Record<ResetError, string> = {
  token_expired: 'Token expired. Request a new reset link.',
  token_used: 'Token already used. Request a new reset link.',
  passwords_mismatch: 'Passwords must match.',
  invalid_token: 'Invalid reset link.',
  password_requirements:
    'Password must be at least 8 characters with 1 uppercase, 1 lowercase, and 1 number.',
  unknown: 'Something went wrong. Please try again.',
};

/**
 * Password Reset Complete page — UI-AUTH-03
 *
 * - Reads token from ?token= query param
 * - Two password fields (new + confirm)
 * - Client-side validation (8 chars, uppercase, lowercase, digit)
 * - POST /users/password-reset/complete
 * - Redirect to /login on success with toast
 * - Error handling: expired/used/invalid token with links to re-request
 */
export function PasswordResetComplete() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token') ?? '';
  const { showToast } = useToast();

  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<ResetError | null>(null);

  // Show error immediately if token is missing/malformed
  useEffect(() => {
    if (!token || token.trim() === '') {
      setError('invalid_token');
    }
  }, [token]);

  function validatePasswords(): ResetError | null {
    if (newPassword !== confirmPassword) return 'passwords_mismatch';
    if (!PASSWORD_REGEX.test(newPassword)) return 'password_requirements';
    return null;
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);

    const validationError = validatePasswords();
    if (validationError) {
      setError(validationError);
      return;
    }

    setLoading(true);
    try {
      await usersApi.completePasswordReset(token, newPassword);
      showToast('Password reset successful. Please log in.', { variant: 'success' });
      window.location.replace('/login');
    } catch (err: unknown) {
      // Map backend error codes to UI errors
      const status = (err as { response?: { status?: number; data?: { error?: string } } })?.response?.status;
      const code = (err as { response?: { status?: number; data?: { error?: string } } })?.response?.data?.error ?? '';

      if (status === 410 || code === 'TOKEN_EXPIRED') {
        setError('token_expired');
      } else if (status === 409 || code === 'TOKEN_USED') {
        setError('token_used');
      } else if (status === 400 || code === 'INVALID_TOKEN') {
        setError('invalid_token');
      } else {
        setError('unknown');
      }
    } finally {
      setLoading(false);
    }
  }

  // Render token-level errors that prevent the form from being submitted
  const isTokenError =
    error === 'token_expired' || error === 'token_used' || error === 'invalid_token';

  return (
    <div
      className="login-page"
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '100vh',
        background: 'var(--color-bg-alt)',
      }}
    >
      <div
        className="login-card"
        style={{
          background: 'var(--color-bg)',
          borderRadius: 'var(--border-radius-lg)',
          boxShadow: 'var(--shadow-xl)',
          padding: 'var(--spacing-2xl)',
          width: '100%',
          maxWidth: '400px',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: 'var(--spacing-lg)',
        }}
      >
        <div style={{ textAlign: 'center', width: '100%' }}>
          <h1 style={{ fontSize: 'var(--font-size-2xl)', margin: 0 }}>Set New Password</h1>
          <p style={{ color: 'var(--color-text-light)', marginTop: 'var(--spacing-xs)', marginBottom: 0 }}>
            Enter and confirm your new password below.
          </p>
        </div>

        {/* Token-level errors (expired / used / invalid) */}
        {error && isTokenError && (
          <div
            role="alert"
            data-testid="token-error-message"
            style={{
              width: '100%',
              padding: 'var(--spacing-md)',
              background: '#fef2f2',
              border: '1px solid var(--color-danger)',
              borderRadius: 'var(--border-radius)',
              color: 'var(--color-danger-dark)',
              fontSize: 'var(--font-size-sm)',
              textAlign: 'center',
            }}
          >
            {ERROR_MESSAGES[error]}{' '}
            {(error === 'token_expired' || error === 'token_used') && (
              <a href="/password-reset" style={{ color: 'var(--color-primary)' }}>
                Request a new link
              </a>
            )}
          </div>
        )}

        {/* Password form — hide when there's a token-level error */}
        {!isTokenError && (
          <form
            onSubmit={handleSubmit}
            style={{ width: '100%', display: 'flex', flexDirection: 'column', gap: 'var(--spacing-md)' }}
          >
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-xs)' }}>
              <label
                htmlFor="new-password"
                style={{ fontSize: 'var(--font-size-sm)', fontWeight: 'var(--font-weight-medium)', color: 'var(--color-text)' }}
              >
                New Password
              </label>
              <input
                id="new-password"
                type="password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                required
                autoComplete="new-password"
                placeholder="At least 8 characters"
                data-testid="new-password-input"
                style={{
                  padding: 'var(--spacing-sm) var(--spacing-md)',
                  border: `1px solid ${error === 'passwords_mismatch' || error === 'password_requirements' ? 'var(--color-danger)' : 'var(--color-border)'}`,
                  borderRadius: 'var(--border-radius)',
                  fontSize: 'var(--font-size-base)',
                  outline: 'none',
                  width: '100%',
                  boxSizing: 'border-box',
                }}
              />
              <p style={{ fontSize: 'var(--font-size-xs)', color: 'var(--color-text-muted)', margin: 0 }}>
                Min 8 characters, 1 uppercase, 1 lowercase, 1 number
              </p>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-xs)' }}>
              <label
                htmlFor="confirm-password"
                style={{ fontSize: 'var(--font-size-sm)', fontWeight: 'var(--font-weight-medium)', color: 'var(--color-text)' }}
              >
                Confirm Password
              </label>
              <input
                id="confirm-password"
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
                autoComplete="new-password"
                placeholder="Re-enter new password"
                data-testid="confirm-password-input"
                style={{
                  padding: 'var(--spacing-sm) var(--spacing-md)',
                  border: `1px solid ${error === 'passwords_mismatch' ? 'var(--color-danger)' : 'var(--color-border)'}`,
                  borderRadius: 'var(--border-radius)',
                  fontSize: 'var(--font-size-base)',
                  outline: 'none',
                  width: '100%',
                  boxSizing: 'border-box',
                }}
              />
            </div>

            {/* Inline validation / server errors */}
            {error && !isTokenError && (
              <div
                role="alert"
                data-testid="form-error-message"
                style={{
                  padding: 'var(--spacing-sm) var(--spacing-md)',
                  background: '#fef2f2',
                  border: '1px solid var(--color-danger)',
                  borderRadius: 'var(--border-radius)',
                  color: 'var(--color-danger-dark)',
                  fontSize: 'var(--font-size-sm)',
                }}
              >
                {ERROR_MESSAGES[error]}
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              data-testid="reset-password-button"
              style={{
                width: '100%',
                padding: 'var(--spacing-sm) var(--spacing-md)',
                background: loading ? 'var(--color-text-muted)' : 'var(--color-primary)',
                color: '#fff',
                border: 'none',
                borderRadius: 'var(--border-radius)',
                fontWeight: 'var(--font-weight-semibold)',
                fontSize: 'var(--font-size-base)',
                cursor: loading ? 'not-allowed' : 'pointer',
                transition: 'background var(--transition-base)',
              }}
            >
              {loading ? 'Resetting…' : 'Reset Password'}
            </button>
          </form>
        )}

        <a
          href="/login"
          style={{ fontSize: 'var(--font-size-sm)', color: 'var(--color-text-light)' }}
          data-testid="back-to-login-link"
        >
          Back to login
        </a>
      </div>
    </div>
  );
}
