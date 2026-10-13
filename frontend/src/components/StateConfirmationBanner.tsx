import { useEffect } from 'react';

export interface StateConfirmationBannerProps {
  action: string; // "Booking saved", "Approved", "Denied", "Settings updated"
  entity?: string; // "Booking #482", "Location 'Studio 1'", optional
  timestamp: Date;
  autoDismiss?: number; // milliseconds, default 5000
  onDismiss: () => void;
  variant?: 'success' | 'warning' | 'error'; // styling variant
}

export function StateConfirmationBanner({
  action,
  entity,
  timestamp,
  autoDismiss = 5000,
  onDismiss,
  variant = 'success',
}: StateConfirmationBannerProps) {
  useEffect(() => {
    if (autoDismiss > 0) {
      const timer = setTimeout(onDismiss, autoDismiss);
      return () => clearTimeout(timer);
    }
  }, [autoDismiss, onDismiss]);

  const timeStr = timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

  return (
    <div
      role="alert"
      aria-live="polite"
      className={`confirmation-banner confirmation-banner--${variant}`}
      data-testid="state-confirmation-banner"
    >
      <span className="confirmation-banner__icon" aria-hidden="true">
        {variant === 'success' ? '✓' : variant === 'error' ? '✕' : '⚠'}
      </span>
      <div className="confirmation-banner__content">
        <strong>{action}</strong>
        {entity && <span> — {entity}</span>}
        <time className="confirmation-banner__timestamp" dateTime={timestamp.toISOString()}>
          {timeStr}
        </time>
      </div>
      <button
        type="button"
        onClick={onDismiss}
        className="confirmation-banner__dismiss"
        aria-label="Dismiss"
      >
        ✕
      </button>
    </div>
  );
}
