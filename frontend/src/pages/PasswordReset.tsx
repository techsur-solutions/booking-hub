import { useState } from 'react';
import { usersApi } from '../api/users';

/**
 * Password Reset Request page — UI-AUTH-03
 *
 * - Email input
 * - "Send Reset Link" button → POST /users/password-reset/request
 * - ALWAYS shows identical success message (security: no email enumeration)
 * - Link back to /login
 */
export function PasswordReset() {
  const [email, setEmail] = useState('');
  const [submitted, setSubmitted] = useState(false);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    try {
      // POST /users/password-reset/request — implemented in Phase 3
      await usersApi.requestPasswordReset(email);
    } catch {
      // Intentionally swallow all errors — always show identical success message
      // to prevent email enumeration attacks (T-09-15-01, FRD §6.3)
    } finally {
      setLoading(false);
      setSubmitted(true);
    }
  }

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
          <h1 style={{ fontSize: 'var(--font-size-2xl)', margin: 0 }}>Reset Password</h1>
          <p style={{ color: 'var(--color-text-light)', marginTop: 'var(--spacing-xs)', marginBottom: 0 }}>
            Enter your email to receive a password reset link.
          </p>
        </div>

        {submitted ? (
          <div
            role="alert"
            data-testid="password-reset-success"
            style={{
              width: '100%',
              padding: 'var(--spacing-md)',
              background: '#ecfdf5',
              border: '1px solid var(--color-success)',
              borderRadius: 'var(--border-radius)',
              color: 'var(--color-success-dark)',
              textAlign: 'center',
              fontSize: 'var(--font-size-sm)',
            }}
          >
            If that email exists, a reset link has been sent.
          </div>
        ) : (
          <form onSubmit={handleSubmit} style={{ width: '100%', display: 'flex', flexDirection: 'column', gap: 'var(--spacing-md)' }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-xs)' }}>
              <label
                htmlFor="reset-email"
                style={{ fontSize: 'var(--font-size-sm)', fontWeight: 'var(--font-weight-medium)', color: 'var(--color-text)' }}
              >
                Email address
              </label>
              <input
                id="reset-email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                placeholder="you@example.com"
                data-testid="reset-email-input"
                style={{
                  padding: 'var(--spacing-sm) var(--spacing-md)',
                  border: '1px solid var(--color-border)',
                  borderRadius: 'var(--border-radius)',
                  fontSize: 'var(--font-size-base)',
                  outline: 'none',
                  width: '100%',
                  boxSizing: 'border-box',
                }}
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              data-testid="send-reset-button"
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
              {loading ? 'Sending…' : 'Send Reset Link'}
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
