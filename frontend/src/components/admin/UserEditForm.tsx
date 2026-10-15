import { useState } from 'react';
import type { FormEvent } from 'react';
import type { UserWithRoles } from '../../api/users';

export interface UserEditAdminValues {
  display_name: string;
  email: string;
  roles: string[];
}

export interface UserEditSelfValues {
  display_name: string;
}

export type UserEditFormValues = UserEditAdminValues | UserEditSelfValues;

export interface UserEditFormProps {
  user: UserWithRoles;
  /** The Keycloak sub (UUID) of the currently logged-in user */
  currentUserId: string;
  onSubmit: (values: UserEditFormValues, isSelf: boolean) => Promise<void>;
  onCancel: () => void;
  loading?: boolean;
}

/**
 * All 17 Keycloak roles from v1.0 per Phase 1 F0 audit finding BF-062.
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
 * UserEditForm — edit form with conditional field rendering based on whether
 * editing self or another user (UI-ADMIN-04 admin vs self restrictions).
 *
 * SELF EDIT (isSelfEdit === true):
 *   - Shows: display_name (editable), email (read-only with note)
 *   - Hides: password, roles
 *   - Submits to: usersApi.updateSelf() → PUT /users/me
 *   - Note: "To change password, use My Account → Change Password"
 *
 * ADMIN EDIT (isSelfEdit === false):
 *   - Shows: display_name, email, roles (multi-select from 17 Keycloak roles)
 *   - NO password field (password changes via separate reset/My Account flow)
 *   - Submits to: usersApi.update(userId, ...) → PUT /users/:id
 *
 * Security: Frontend prevents roles field in self-edit; backend enforces via
 * UserSelfUpdateRequest type (NO password/role fields) and PUT /users/me endpoint.
 * Threat T-10-11: prevents privilege escalation via self-editing.
 */
export function UserEditForm({ user, currentUserId, onSubmit, onCancel, loading = false }: UserEditFormProps) {
  // Determine if editing own account (admin vs self restriction logic)
  const isSelfEdit = currentUserId === user.id;

  const [displayName, setDisplayName] = useState(user.display_name ?? '');
  const [email, setEmail] = useState(user.email);
  const [selectedRoles, setSelectedRoles] = useState<string[]>(user.roles ?? []);
  const [errors, setErrors] = useState<Record<string, string>>({});

  function validate(): boolean {
    const newErrors: Record<string, string> = {};

    // For self-edit: only display_name is editable; email is read-only
    // For admin edit: validate email
    if (!isSelfEdit) {
      if (!email.trim()) {
        newErrors.email = 'Email is required.';
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
        newErrors.email = 'Please enter a valid email address.';
      }
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

    if (isSelfEdit) {
      // Self update: display_name only (Phase 3 Plan 04 type-level guard — UserSelfUpdateRequest has NO password/roles)
      await onSubmit({ display_name: displayName.trim() }, true);
    } else {
      // Admin updating another user: display_name, email, roles (no password)
      await onSubmit(
        {
          display_name: displayName.trim(),
          email: email.trim(),
          roles: selectedRoles,
        },
        false
      );
    }
  }

  return (
    <form
      className="user-edit-form"
      data-testid="user-edit-form"
      onSubmit={(e) => { void handleSubmit(e); }}
      noValidate
    >
      {/* Contextual label indicating edit mode */}
      {isSelfEdit && (
        <div className="user-edit-form__self-notice" data-testid="self-edit-notice">
          You are editing your own account. Some fields are restricted to prevent privilege escalation.
        </div>
      )}

      {/* Display Name — always editable */}
      <div className="form-field">
        <label htmlFor="edit_display_name" className="form-field__label">
          Display Name
        </label>
        <input
          id="edit_display_name"
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
      {isSelfEdit ? (
        // Self-edit: email is READ-ONLY with explanation
        <div className="form-field">
          <label htmlFor="edit_email" className="form-field__label">
            Email
          </label>
          <input
            id="edit_email"
            type="email"
            className="form-field__input form-field__input--readonly"
            data-testid="input-email"
            value={email}
            readOnly
            aria-readonly="true"
            aria-describedby="email-readonly-note"
          />
          <span id="email-readonly-note" className="form-field__hint" data-testid="email-readonly-note">
            Contact admin to change email
          </span>
        </div>
      ) : (
        // Admin edit: email is editable
        <div className={`form-field${errors.email ? ' form-field--error' : ''}`}>
          <label htmlFor="edit_email" className="form-field__label">
            Email <span className="form-field__required" aria-hidden="true">*</span>
          </label>
          <input
            id="edit_email"
            type="email"
            className="form-field__input"
            data-testid="input-email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
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
      )}

      {/* Password note for self-edit — NO password field in either mode */}
      {isSelfEdit && (
        <div className="user-edit-form__password-note" data-testid="password-note">
          To change password, use My Account → Change Password
        </div>
      )}

      {/* Roles multi-select — ONLY visible in admin edit (NOT self-edit) */}
      {!isSelfEdit && (
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
      )}

      {/* Actions */}
      <div className="form-actions">
        <button
          type="submit"
          className="form-actions__submit"
          data-testid="btn-save-user"
          disabled={loading}
        >
          {loading ? 'Saving…' : 'Save Changes'}
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
