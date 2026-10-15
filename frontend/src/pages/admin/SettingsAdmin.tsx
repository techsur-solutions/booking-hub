import { SettingsForm } from '../../components/admin/SettingsForm';

/**
 * SettingsAdmin
 *
 * Route: /admin/settings
 * Implements: UI-ADMIN-06 — Settings Admin controls system-wide configuration
 *   with immediate propagation indicator for the approveBooking toggle.
 *
 * Renders a single-page form (not tabbed per UI-ADMIN-06 "unless >10 settings")
 * organized into three sections:
 *   1. Approval Workflow (approveBooking toggle + propagation indicator)
 *   2. Calendar Display (slot_size, min_time, max_time)
 *   3. Display & Feeds (Preview Display Board button, Copy feed links button)
 */
export function SettingsAdmin() {
  return (
    <div
      data-testid="settings-admin"
      style={{
        padding: 'var(--spacing-xl)',
        maxWidth: 900,
      }}
    >
      <header style={{ marginBottom: 'var(--spacing-xl)' }}>
        <h1
          style={{
            fontSize: 'var(--font-size-3xl)',
            fontWeight: 'var(--font-weight-bold)',
            color: 'var(--color-text)',
            margin: 0,
          }}
        >
          Settings Admin
        </h1>
        <p
          style={{
            fontSize: 'var(--font-size-sm)',
            color: 'var(--color-text-light)',
            marginTop: 'var(--spacing-xs)',
            marginBottom: 0,
          }}
        >
          Configure system-wide settings. Changes to Approval Workflow take effect immediately for
          new bookings.
        </p>
      </header>

      <SettingsForm />
    </div>
  );
}
