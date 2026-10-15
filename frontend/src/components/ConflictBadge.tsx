import type { ConflictFlag } from '../api/types';

export interface ConflictBadgeProps {
  conflicts: ConflictFlag[];
  onClick?: (bookingId: string) => void; // Optional click handler to open detail modal
  size?: 'small' | 'medium' | 'large'; // Size variant for different contexts
}

export function ConflictBadge({ conflicts, onClick, size = 'medium' }: ConflictBadgeProps) {
  if (!conflicts || conflicts.length === 0) return null;

  const handleClick = () => {
    if (onClick && conflicts.length > 0) {
      // Open first conflicting booking's detail modal
      onClick(conflicts[0].conflicting_booking_id);
    }
  };

  const tooltipText = conflicts
    .map((c) => `Conflicts with Booking #${c.conflicting_booking_id} (${c.conflict_type})`)
    .join('; ');

  return (
    <span
      className={`conflict-badge conflict-badge--${size}`}
      title={tooltipText}
      onClick={handleClick}
      onKeyDown={(e) => e.key === 'Enter' && handleClick()}
      role={onClick ? 'button' : undefined}
      tabIndex={onClick ? 0 : undefined}
      aria-label={`Conflict warning: ${tooltipText}`}
      data-testid="conflict-badge"
    >
      <span className="conflict-badge__icon" aria-hidden="true">
        ⚠
      </span>
      <span className="conflict-badge__count">{conflicts.length}</span>
    </span>
  );
}
