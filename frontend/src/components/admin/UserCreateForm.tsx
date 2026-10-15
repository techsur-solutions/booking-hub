import { useState } from 'react';
import type { FormEvent } from 'react';

export interface UserCreateFormValues {
  display_name: string;
  email: string;
  password: string;
  roles: string[];
}

export interface UserCreateFormProps {
  onSubmit: (values: UserCreateFormValues) => Promise<void>;
  onCancel: () => void;
  loading?: boolean;
}

/**
 * All 17 Keycloak roles from v1.0 per Phase 1 F0 audit finding BF-062.
 * These are the permission flags surfaced via Keycloak realm roles.
 */
const KEYCLOAK_ROLES = [
  'role_calendar_viewer',
  'role_room_booking',
  'role_booking_approver',
  'role_location_manager',
  'role_resource_manager',
  'role_custom_field_manager',
  'role_user_manager',
  'role_permission_manager',
  'role_settings_manager',
  'role_audit_viewer',
  'role_feed_viewer',
  'role_display_board',
  'role_reporting_viewer',
  'role_system_admin',
  'role_api_access',
  'role_notification_manager',
  'role_data_exporter',
] as const;

/**
 * UserCreateForm — form for admin to create a new user.
 *
 * Fields per Phase 3 Plan 04 UserCreateRequest:
 *   - display_name (optional)
 *   - email (required)
 *   - password (required, temporary — user forced to change on first login)
 *   - roles (multi-select from 17 Keycloak roles)
 *
 * Inline validation: email required and valid format, password minimum 6 chars
 * (per F0 audit BF-077 baseline: >=6 chars, >=1 digit, >=1 lowercase).
 */
export function UserCreateForm({ onSubmit, onCancel, loading = false }: UserCreateFormProps) {
  const [displayName, setDisplayName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [selectedRoles, setSelectedRoles] = useState<string[]>([]);
  const [errors, setErrors] = useState<Record<string, string>>({});

  function validate(): boolean {
    const newErrors: Record<string, string> = {};

    if (!email.trim()) {
      newErrors.email = 'Email is required.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      newErrors.email = 'Please enter a valid email address.';
    }

    if (!password) {
      newErrors.password = 'Password is required.';
    } else if (password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters.';
    } else if (!/\d/.test(password)) {
      newErrors.password = 'Password must contain at least one digit.';
    } else if (!/[a-z]/.test(password)) {
      newErrors.password = 'Password must contain at least one lowercase letter.';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  }

  function handleRoleToggle(role: string) {
    setSelectedRoles((prev) =>
      prev.includes(role) ? prev.filter((r) => r !== role) : [...prev, role]
    );
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (!validate()) return;

    await onSubmit({
      display_name: displayName.trim() || '',
      email: email.trim(),
      password,
      roles: selectedRoles,
    });
  }

  return (
    <form
      className="user-create-form"
      data-testid="user-create-form"
      onSubmit={(e) => { void handleSubmit(e); }}
      noValidate
    >
      {/* Display Name */}
      <div className="form-field">
        <label htmlFor="display_name" className="form-field__label">
          Display Name <span className="form-field__optional">(optional)</span>
        </label>
        <input
          id="display_name"
          type="text"
          className="form-field__input"
          data-testid="input-display-name"
          value={displayName}
          onChange={(e) => setDisplayName(e.target.value)}
          placeholder="Full name shown in UI"
          autoComplete="off"
        />
      </div>

      {/* Email */}
      <div className={`form-field${errors.email ? ' form-field--error' : ''}`}>
        <label htmlFor="email" className="form-field__label">
          Email <span className="form-field__required" aria-hidden="true">*</span>
        </label>
        <input
          id="email"
          type="email"
          className="form-field__input"
          data-testid="input-email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="user@example.com"
          required
          aria-required="true"
          aria-describedby={errors.email ? 'email-error' : undefined}
        />
        {errors.email && (
          <span id="email-error" className="form-field__error" role="alert" data-testid="email-error">
            {errors.email}
          </span>
        )}
      </div>

      {/* Password — marked as temporary per Phase 3 decision */}
      <div className={`form-field${errors.password ? ' form-field--error' : ''}`}>
        <label htmlFor="password" className="form-field__label">
          Temporary Password <span className="form-field__required" aria-hidden="true">*</span>
        </label>
        <input
          id="password"
          type="password"
          className="form-field__input"
          data-testid="input-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          aria-required="true"
          aria-describedby={errors.password ? 'password-error' : 'password-hint'}
        />
        <span id="password-hint" className="form-field__hint">
          {/* temporary password noted — user will be forced to change on first login */}
          Temporary password — the user will be prompted to change it on first login.
        </span>
        {errors.password && (
          <span id="password-error" className="form-field__error" role="alert" data-testid="password-error">
            {errors.password}
          </span>
        )}
      </div>

      {/* Roles multi-select (all 17 v1.0 Keycloak roles) */}
      <fieldset className="form-field form-field--fieldset" data-testid="roles-fieldset">
        <legend className="form-field__label">Roles</legend>
        <div className="role-checkboxes">
          {KEYCLOAK_ROLES.map((role) => (
            <label key={role} className="role-checkbox-label" data-testid={`role-label-${role}`}>
              <input
                type="checkbox"
                className="role-checkbox"
                data-testid={`role-checkbox-${role}`}
                checked={selectedRoles.includes(role)}
                onChange={() => handleRoleToggle(role)}
                value={role}
              />
              <span className="role-checkbox-name">{role}</span>
            </label>
          ))}
        </div>
      </fieldset>

      {/* Actions */}
      <div className="form-actions">
        <button
          type="submit"
          className="form-actions__submit"
          data-testid="btn-create-user"
          disabled={loading}
        >
          {loading ? 'Creating…' : 'Create User'}
        </button>
        <button
          type="button"
          className="form-actions__cancel"
          data-testid="btn-cancel"
          onClick={onCancel}
          disabled={loading}
        >
          Cancel
        </button>
      </div>
    </form>
  );
}
