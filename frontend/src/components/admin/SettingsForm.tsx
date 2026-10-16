import { useState, useEffect, useCallback } from 'react';
import { settingsApi } from '../../api/settingsApi';
import type { Settings, SettingsUpdateRequest } from '../../api/settingsApi';
import { StateConfirmationBanner } from '../StateConfirmationBanner';
import { HelpTooltip } from '../common/HelpTooltip';
import { absoluteApiUrl } from '../../api/baseUrl';

// Feed links are copied into other apps, so they must be absolute (see baseUrl.ts).
function buildFeedUrl(path: string): string {
  return absoluteApiUrl(path).toString();
}

interface FeedLinksModalProps {
  onClose: () => void;
}

/**
 * Modal/popover showing RSS2, iCal, and JSON feed URLs with copy-to-clipboard buttons.
 * Reuses the same feed URL shape as the FeedSubscription page (Phase 9 Plan 14).
 */
function FeedLinksModal({ onClose }: FeedLinksModalProps) {
  const [copiedUrl, setCopiedUrl] = useState<string | null>(null);

  const feedLinks = [
    { label: 'RSS2 Feed', url: buildFeedUrl('/feeds/rss2'), testId: 'feed-rss2' },
    { label: 'iCal Feed', url: buildFeedUrl('/feeds/ical'), testId: 'feed-ical' },
    { label: 'JSON Feed', url: buildFeedUrl('/feeds/json'), testId: 'feed-json' },
  ];

  async function copyToClipboard(text: string) {
    try {
      if (navigator.clipboard?.writeText) {
        await navigator.clipboard.writeText(text);
      } else {
        const textarea = document.createElement('textarea');
        textarea.value = text;
        textarea.style.position = 'fixed';
        textarea.style.opacity = '0';
        document.body.appendChild(textarea);
        textarea.focus();
        textarea.select();
        document.execCommand('copy');
        document.body.removeChild(textarea);
      }
      setCopiedUrl(text);
      setTimeout(() => setCopiedUrl(null), 2000);
    } catch {
      // Clipboard may be denied in certain contexts
    }
  }

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-label="Feed Links"
      data-testid="feed-links-modal"
      style={{
        position: 'fixed',
        inset: 0,
        zIndex: 200,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      {/* Backdrop */}
      <div
        data-testid="feed-links-backdrop"
        style={{
          position: 'absolute',
          inset: 0,
          background: 'rgba(0,0,0,0.4)',
        }}
        onClick={onClose}
        aria-hidden="true"
      />

      {/* Modal panel */}
      <div
        style={{
          position: 'relative',
          background: 'var(--color-bg)',
          border: '1px solid var(--color-border)',
          borderRadius: 'var(--border-radius-lg)',
          padding: 'var(--spacing-xl)',
          maxWidth: 560,
          width: '90vw',
          boxShadow: '0 8px 32px rgba(0,0,0,0.18)',
        }}
      >
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            marginBottom: 'var(--spacing-lg)',
          }}
        >
          <h2 style={{ margin: 0, fontSize: 'var(--font-size-xl)' }}>Feed Links</h2>
          <button
            type="button"
            onClick={onClose}
            aria-label="Close"
            data-testid="feed-links-close"
            style={{
              background: 'none',
              border: 'none',
              cursor: 'pointer',
              fontSize: 'var(--font-size-xl)',
              color: 'var(--color-text-light)',
              padding: '4px 8px',
            }}
          >
            ✕
          </button>
        </div>

        <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--color-text-light)', marginTop: 0 }}>
          Copy feed URLs to subscribe in external calendar apps, RSS readers, or custom integrations.
        </p>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-md)' }}>
          {feedLinks.map(({ label, url, testId }) => {
            const isCopied = copiedUrl === url;
            return (
              <div
                key={testId}
                data-testid={testId}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 'var(--spacing-sm)',
                  background: 'var(--color-bg-alt)',
                  border: '1px solid var(--color-border)',
                  borderRadius: 'var(--border-radius)',
                  padding: 'var(--spacing-sm) var(--spacing-md)',
                }}
              >
                <span
                  style={{
                    flex: 1,
                    fontSize: 'var(--font-size-sm)',
                    fontWeight: 'var(--font-weight-medium)',
                    minWidth: 0,
                  }}
                >
                  <span style={{ display: 'block', fontWeight: 'var(--font-weight-semibold)', marginBottom: 2 }}>
                    {label}
                  </span>
                  <code
                    data-testid={`${testId}-url`}
                    style={{
                      fontSize: 'var(--font-size-xs)',
                      fontFamily: 'var(--font-family-mono)',
                      color: 'var(--color-text-light)',
                      wordBreak: 'break-all',
                    }}
                  >
                    {url}
                  </code>
                </span>
                <button
                  type="button"
                  data-testid={`${testId}-copy`}
                  onClick={() => copyToClipboard(url)}
                  aria-label={`Copy ${label} URL to clipboard`}
                  style={{
                    flexShrink: 0,
                    padding: 'var(--spacing-xs) var(--spacing-md)',
                    background: isCopied ? 'var(--color-success)' : 'var(--color-primary)',
                    color: '#ffffff',
                    border: 'none',
                    borderRadius: 'var(--border-radius)',
                    fontSize: 'var(--font-size-sm)',
                    fontWeight: 'var(--font-weight-medium)',
                    cursor: 'pointer',
                    transition: 'background var(--transition-base)',
                    whiteSpace: 'nowrap',
                  }}
                >
                  {isCopied ? '✓ Copied!' : 'Copy'}
                </button>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}

interface SettingsFormProps {
  /** Optional class name for the wrapping element */
  className?: string;
}

/**
 * SettingsForm
 *
 * Single-page form (NOT tabbed per UI-ADMIN-06 "unless >10 settings") organized into:
 *   Section 1: Approval Workflow — approveBooking toggle with propagation indicator
 *   Section 2: Calendar Display — slot_size, min_time, max_time
 *   Section 3: Display & Feeds — Preview Display Board button, Copy feed links button
 *
 * Fetches current settings via settingsApi.getCurrent() calling GET /settings/current.
 * On save: calls settingsApi.update() calling PUT /settings/1 (singleton entity).
 * On success: shows StateConfirmationBanner with timestamp.
 */
export function SettingsForm({ className }: SettingsFormProps) {
  // Form state
  const [approveBooking, setApproveBooking] = useState(false);
  const [slotSize, setSlotSize] = useState(30);
  const [minTime, setMinTime] = useState('08:00');
  const [maxTime, setMaxTime] = useState('18:00');

  // Loading / error / save state
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);

  // Confirmation banner state
  const [confirmTimestamp, setConfirmTimestamp] = useState<Date | null>(null);

  // Propagation indicator state (shows after save, auto-dismisses after 5s)
  const [showPropagationIndicator, setShowPropagationIndicator] = useState(false);

  // Feed links modal
  const [showFeedLinksModal, setShowFeedLinksModal] = useState(false);

  // Populate form from API response
  function applySettings(settings: Settings) {
    setApproveBooking(settings.approve_booking);
    setSlotSize(settings.calendar_slot_size);
    setMinTime(settings.calendar_min_time);
    setMaxTime(settings.calendar_max_time);
  }

  // Load current settings on mount
  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setLoadError(null);

    settingsApi
      .getCurrent()
      .then((response) => {
        if (!cancelled) {
          applySettings(response.data);
          setLoading(false);
        }
      })
      .catch((err: unknown) => {
        if (!cancelled) {
          const msg =
            err instanceof Error
              ? err.message
              : 'Failed to load settings. Please try again.';
          setLoadError(msg);
          setLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, []);

  // Handle form submit
  const handleSubmit = useCallback(
    async (e: React.FormEvent<HTMLFormElement>) => {
      e.preventDefault();
      setSaving(true);
      setSaveError(null);

      const payload: SettingsUpdateRequest = {
        approve_booking: approveBooking,
        calendar_slot_size: slotSize,
        calendar_min_time: minTime,
        calendar_max_time: maxTime,
      };

      try {
        await settingsApi.update(payload);
        const now = new Date();
        setConfirmTimestamp(now);
        // Show propagation indicator — "Changes take effect immediately for new bookings"
        setShowPropagationIndicator(true);
        // Auto-dismiss propagation indicator after 5 seconds
        setTimeout(() => setShowPropagationIndicator(false), 5000);
      } catch (err: unknown) {
        const msg =
          err instanceof Error ? err.message : 'Failed to save settings. Please try again.';
        setSaveError(msg);
      } finally {
        setSaving(false);
      }
    },
    [approveBooking, slotSize, minTime, maxTime],
  );

  // Dismiss confirmation banner
  const handleDismissBanner = useCallback(() => {
    setConfirmTimestamp(null);
  }, []);

  // ── Render ────────────────────────────────────────────────────────────────────

  if (loading) {
    return (
      <div data-testid="settings-loading" style={{ padding: 'var(--spacing-xl)', color: 'var(--color-text-light)' }}>
        Loading settings…
      </div>
    );
  }

  if (loadError) {
    return (
      <div
        data-testid="settings-load-error"
        role="alert"
        style={{ padding: 'var(--spacing-xl)', color: 'var(--color-error)' }}
      >
        {loadError}
      </div>
    );
  }

  const sectionStyle: React.CSSProperties = {
    background: 'var(--color-bg-alt)',
    border: '1px solid var(--color-border)',
    borderRadius: 'var(--border-radius-lg)',
    padding: 'var(--spacing-xl)',
    marginBottom: 'var(--spacing-xl)',
  };

  const sectionTitleStyle: React.CSSProperties = {
    fontSize: 'var(--font-size-lg)',
    fontWeight: 'var(--font-weight-semibold)',
    color: 'var(--color-text)',
    margin: '0 0 var(--spacing-lg) 0',
    paddingBottom: 'var(--spacing-sm)',
    borderBottom: '1px solid var(--color-border)',
  };

  const fieldGroupStyle: React.CSSProperties = {
    display: 'flex',
    flexDirection: 'column',
    gap: 'var(--spacing-xs)',
    marginBottom: 'var(--spacing-md)',
  };

  const labelStyle: React.CSSProperties = {
    fontSize: 'var(--font-size-sm)',
    fontWeight: 'var(--font-weight-medium)',
    color: 'var(--color-text)',
  };

  const helpTextStyle: React.CSSProperties = {
    fontSize: 'var(--font-size-xs)',
    color: 'var(--color-text-light)',
    marginTop: 2,
  };

  const inputStyle: React.CSSProperties = {
    padding: 'var(--spacing-xs) var(--spacing-sm)',
    border: '1px solid var(--color-border)',
    borderRadius: 'var(--border-radius)',
    fontSize: 'var(--font-size-sm)',
    color: 'var(--color-text)',
    background: 'var(--color-bg)',
    width: '100%',
    maxWidth: 200,
  };

  return (
    <div data-testid="settings-form-container" className={className}>
      {/* StateConfirmationBanner — "Settings updated at HH:mm:ss" */}
      {confirmTimestamp && (
        <StateConfirmationBanner
          action="Settings updated"
          timestamp={confirmTimestamp}
          onDismiss={handleDismissBanner}
          variant="success"
        />
      )}

      <form
        data-testid="settings-form"
        onSubmit={handleSubmit}
        noValidate
        style={{ maxWidth: 680 }}
      >
        {/* ── Section 1: Approval Workflow ─────────────────────────────── */}
        <section data-testid="section-approval-workflow" style={sectionStyle}>
          <h2 style={sectionTitleStyle}>Approval Workflow</h2>

          <div style={fieldGroupStyle}>
            {/* approveBooking toggle switch */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--spacing-md)' }}>
              <span
                id="approve-booking-label"
                style={{ ...labelStyle, userSelect: 'none', display: 'flex', alignItems: 'center', gap: '0.35em' }}
              >
                Auto-approve bookings
                <HelpTooltip
                  content="When ON, new bookings are automatically approved immediately upon creation. When OFF, new bookings enter Pending status and require manual approval from a user with the booking_approver role."
                />
              </span>

              {/* Toggle switch — approveBooking toggle */}
              <button
                type="button"
                role="switch"
                aria-checked={approveBooking}
                aria-labelledby="approve-booking-label"
                data-testid="approve-booking-toggle"
                onClick={() => setApproveBooking((prev) => !prev)}
                style={{
                  position: 'relative',
                  width: 48,
                  height: 26,
                  borderRadius: 13,
                  border: 'none',
                  cursor: 'pointer',
                  background: approveBooking ? 'var(--color-success)' : 'var(--color-border)',
                  transition: 'background var(--transition-base)',
                  flexShrink: 0,
                }}
              >
                <span
                  style={{
                    position: 'absolute',
                    top: 3,
                    left: approveBooking ? 26 : 3,
                    width: 20,
                    height: 20,
                    borderRadius: '50%',
                    background: '#fff',
                    transition: 'left var(--transition-base)',
                    boxShadow: '0 1px 3px rgba(0,0,0,0.2)',
                  }}
                />
              </button>

              {/* Current state label */}
              <span
                data-testid="approve-booking-state"
                style={{
                  fontSize: 'var(--font-size-sm)',
                  fontWeight: 'var(--font-weight-medium)',
                  color: approveBooking ? 'var(--color-success)' : 'var(--color-text-light)',
                }}
              >
                {approveBooking ? 'ON' : 'OFF'}
              </span>
            </div>

            <p style={helpTextStyle}>
              When ON, new bookings are automatically approved. When OFF, bookings require manual
              approval.
            </p>

            {/* Propagation indicator — appears after save, auto-dismisses after 5s.
                Addresses JRN-03.2: "Did the approval setting actually propagate?"
                Phase 4 Plan 06 decision: no in-memory cache, getCurrent() reads DB directly,
                so approve_booking changes ARE immediate — this badge makes that explicit. */}
            {showPropagationIndicator && (
              <div
                data-testid="propagation-indicator"
                role="status"
                aria-live="polite"
                aria-label="Approval setting change propagation status"
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: 6,
                  marginTop: 'var(--spacing-sm)',
                  padding: '4px 12px',
                  background: 'rgba(59, 130, 246, 0.1)',
                  border: '1px solid rgba(59, 130, 246, 0.3)',
                  borderRadius: 'var(--border-radius)',
                  fontSize: 'var(--font-size-xs)',
                  fontWeight: 'var(--font-weight-medium)',
                  color: 'var(--color-primary)',
                }}
              >
                {/* Lightning bolt icon indicating immediacy */}
                <span aria-hidden="true" style={{ fontSize: '1em' }}>⚡</span>
                {/* Propagation message — Changes take effect immediately for new bookings */}
                <span>Changes take effect immediately for new bookings</span>
                <button
                  type="button"
                  data-testid="propagation-indicator-dismiss"
                  onClick={() => setShowPropagationIndicator(false)}
                  aria-label="Dismiss propagation indicator"
                  style={{
                    background: 'none',
                    border: 'none',
                    cursor: 'pointer',
                    color: 'var(--color-primary)',
                    fontSize: '0.9em',
                    padding: '0 0 0 4px',
                    lineHeight: 1,
                  }}
                >
                  ✕
                </button>
              </div>
            )}
          </div>
        </section>

        {/* ── Section 2: Calendar Display ──────────────────────────────── */}
        <section data-testid="section-calendar-display" style={sectionStyle}>
          <h2 style={sectionTitleStyle}>Calendar Display</h2>

          {/* slot_size: time slot size in minutes */}
          <div style={fieldGroupStyle}>
            <label htmlFor="slot-size" style={{ ...labelStyle, display: 'flex', alignItems: 'center', gap: '0.35em' }}>
              Time slot size (minutes)
              <HelpTooltip
                content="Controls the granularity of the calendar grid. 15 minutes shows fine-grained slots; 60 minutes shows one slot per hour. Smaller slot sizes give more flexibility for precise booking times."
              />
            </label>
            <select
              id="slot-size"
              data-testid="slot-size-input"
              value={slotSize}
              onChange={(e) => setSlotSize(Number(e.target.value))}
              style={inputStyle}
            >
              <option value={15}>15</option>
              <option value={30}>30</option>
              <option value={60}>60</option>
            </select>
            <p style={helpTextStyle}>Time granularity for the calendar grid.</p>
          </div>

          {/* min_time: calendar start time */}
          <div style={fieldGroupStyle}>
            <label htmlFor="min-time" style={{ ...labelStyle, display: 'flex', alignItems: 'center', gap: '0.35em' }}>
              Calendar start time
              <HelpTooltip
                content="The earliest time slot shown in Calendar and Day views. Bookings before this time still exist but are not visible in the slot grid. Typical values: 07:00 or 08:00."
              />
            </label>
            <input
              id="min-time"
              type="time"
              data-testid="min-time-input"
              value={minTime}
              onChange={(e) => setMinTime(e.target.value)}
              style={inputStyle}
            />
            <p style={helpTextStyle}>Earliest time slot shown on the calendar (HH:mm).</p>
          </div>

          {/* max_time: calendar end time */}
          <div style={fieldGroupStyle}>
            <label htmlFor="max-time" style={{ ...labelStyle, display: 'flex', alignItems: 'center', gap: '0.35em' }}>
              Calendar end time
              <HelpTooltip
                content="The latest time slot shown in Calendar and Day views. Bookings after this time still exist but are not visible in the slot grid. Typical values: 18:00 or 22:00."
              />
            </label>
            <input
              id="max-time"
              type="time"
              data-testid="max-time-input"
              value={maxTime}
              onChange={(e) => setMaxTime(e.target.value)}
              style={inputStyle}
            />
            <p style={helpTextStyle}>Latest time slot shown on the calendar (HH:mm).</p>
          </div>
        </section>

        {/* ── Section 3: Display & Feeds ───────────────────────────────── */}
        <section data-testid="section-display-feeds" style={sectionStyle}>
          <h2 style={sectionTitleStyle}>Display &amp; Feeds</h2>

          <div style={{ display: 'flex', gap: 'var(--spacing-md)', flexWrap: 'wrap' }}>
            {/* Preview Display Board — opens /display-board in new tab */}
            <button
              type="button"
              data-testid="btn-preview-display-board"
              onClick={() => window.open('/display-board', '_blank', 'noopener,noreferrer')}
              style={{
                padding: 'var(--spacing-sm) var(--spacing-lg)',
                background: 'transparent',
                color: 'var(--color-primary)',
                border: '1px solid var(--color-primary)',
                borderRadius: 'var(--border-radius)',
                fontSize: 'var(--font-size-sm)',
                fontWeight: 'var(--font-weight-medium)',
                cursor: 'pointer',
              }}
            >
              Preview Display Board ↗
            </button>

            {/* Copy feed links — opens modal with RSS2/iCal/JSON URLs */}
            <button
              type="button"
              data-testid="btn-copy-feed-links"
              onClick={() => setShowFeedLinksModal(true)}
              style={{
                padding: 'var(--spacing-sm) var(--spacing-lg)',
                background: 'transparent',
                color: 'var(--color-primary)',
                border: '1px solid var(--color-primary)',
                borderRadius: 'var(--border-radius)',
                fontSize: 'var(--font-size-sm)',
                fontWeight: 'var(--font-weight-medium)',
                cursor: 'pointer',
              }}
            >
              Copy feed links
            </button>
          </div>

          <p style={{ ...helpTextStyle, marginTop: 'var(--spacing-md)' }}>
            View the display board kiosk or share feed subscription links (RSS2, iCal, JSON).
          </p>
        </section>

        {/* Save error */}
        {saveError && (
          <div
            data-testid="settings-save-error"
            role="alert"
            style={{
              marginBottom: 'var(--spacing-md)',
              padding: 'var(--spacing-sm) var(--spacing-md)',
              background: 'rgba(239, 68, 68, 0.08)',
              border: '1px solid rgba(239, 68, 68, 0.3)',
              borderRadius: 'var(--border-radius)',
              color: 'var(--color-error)',
              fontSize: 'var(--font-size-sm)',
            }}
          >
            {saveError}
          </div>
        )}

        {/* Save button */}
        <button
          type="submit"
          data-testid="btn-save-settings"
          disabled={saving}
          style={{
            padding: 'var(--spacing-sm) var(--spacing-xl)',
            background: saving ? 'var(--color-border)' : 'var(--color-primary)',
            color: '#ffffff',
            border: 'none',
            borderRadius: 'var(--border-radius)',
            fontSize: 'var(--font-size-md)',
            fontWeight: 'var(--font-weight-semibold)',
            cursor: saving ? 'not-allowed' : 'pointer',
            transition: 'background var(--transition-base)',
          }}
        >
          {saving ? 'Saving…' : 'Save Settings'}
        </button>
      </form>

      {/* Feed links modal */}
      {showFeedLinksModal && (
        <FeedLinksModal onClose={() => setShowFeedLinksModal(false)} />
      )}
    </div>
  );
}
