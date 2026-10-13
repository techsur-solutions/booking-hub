import { useState } from 'react';

type BookingStatus = 'pending' | 'approved' | 'denied';

export interface StatusBadgeProps {
  status: 'pending' | 'approved' | 'denied';
  /** ISO 8601 timestamp relevant to this status */
  timestamp: string;
}

/**
 * Colored status badge for booking status with relative timestamp.
 *
 * - pending  → Yellow badge, "Pending approval since {relative}"
 * - approved → Green badge,  "Approved at {relative}"
 * - denied   → Red badge,    "Denied at {relative}"
 *
 * Relative time shown with absolute timestamp in tooltip (title attribute).
 */
export function StatusBadge({ status, timestamp }: StatusBadgeProps) {
  const [, setForceUpdate] = useState(0);
  void setForceUpdate; // suppress lint warning — used for potential refresh

  const absoluteTime = new Date(timestamp).toLocaleString();
  const relativeTime = getRelativeTime(timestamp);

  const label = getLabel(status, relativeTime);
  const variantClass = `status-badge--${status}`;

  return (
    <span
      className={`status-badge ${variantClass}`}
      title={absoluteTime}
      data-testid="status-badge"
      data-status={status}
    >
      {label}
    </span>
  );
}

function getLabel(status: BookingStatus, relative: string): string {
  switch (status) {
    case 'pending':
      return `Pending approval since ${relative}`;
    case 'approved':
      return `Approved at ${relative}`;
    case 'denied':
      return `Denied at ${relative}`;
  }
}

/**
 * Returns a human-readable relative time string.
 * e.g. "2 hours ago", "3 days ago", "just now"
 */
function getRelativeTime(isoTimestamp: string): string {
  const now = Date.now();
  const then = new Date(isoTimestamp).getTime();
  const diffMs = now - then;
  const diffSec = Math.floor(diffMs / 1000);
  const diffMin = Math.floor(diffSec / 60);
  const diffHrs = Math.floor(diffMin / 60);
  const diffDays = Math.floor(diffHrs / 24);

  if (diffSec < 60) return 'just now';
  if (diffMin < 60) return `${diffMin} minute${diffMin === 1 ? '' : 's'} ago`;
  if (diffHrs < 24) return `${diffHrs} hour${diffHrs === 1 ? '' : 's'} ago`;
  if (diffDays < 30) return `${diffDays} day${diffDays === 1 ? '' : 's'} ago`;

  // For older dates, show formatted date
  return new Date(isoTimestamp).toLocaleDateString();
}
