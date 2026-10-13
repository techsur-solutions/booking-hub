import type { Booking } from '../../api/types';
import { ConflictBadge } from '../ConflictBadge';

export interface DayViewEventProps {
  booking: Booking;
  /** CSS colour for the location (hex or named colour) */
  colour: string;
  /** Top position as a percentage of the grid height */
  topPercent: number;
  /** Height as a percentage of the grid height */
  heightPercent: number;
  /** Called with booking id when user clicks the event */
  onClick: (bookingId: string) => void;
}

/**
 * DayViewEvent
 *
 * Renders a single booking event inside the HourlySlotGrid.
 * - Background colour derived from `locations.colour` (same as Calendar View's BookingEvent).
 * - ConflictBadge rendered when `booking.conflict_flags.length > 0` (identical to Calendar View).
 * - Absolutely positioned within the slot grid using `topPercent` / `heightPercent`.
 * - Click opens Booking Detail Modal via `onClick(bookingId)`.
 */
export function DayViewEvent({
  booking,
  colour,
  topPercent,
  heightPercent,
  onClick,
}: DayViewEventProps) {
  const startTime = booking.start_time.substring(11, 16); // 'HH:mm'
  const endTime = booking.end_time.substring(11, 16);

  return (
    <div
      className="day-view-event"
      style={{
        '--location-color': colour,
        top: `${topPercent}%`,
        height: `${Math.max(heightPercent, 2)}%`, // minimum 2% so tiny events are visible
        position: 'absolute',
        left: '4rem', // offset for time-label column
        right: '0.25rem',
      } as React.CSSProperties}
      onClick={(e) => {
        e.stopPropagation(); // prevent slot-click firing beneath
        onClick(booking.id);
      }}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          e.stopPropagation();
          onClick(booking.id);
        }
      }}
      role="button"
      tabIndex={0}
      aria-label={`Booking: ${booking.title} from ${startTime} to ${endTime}`}
      data-testid="day-view-event"
      data-booking-id={booking.id}
    >
      <span
        className="day-view-event__color-bar"
        style={{ backgroundColor: colour }}
        aria-hidden="true"
      />
      <div className="day-view-event__content">
        <span className="day-view-event__title">{booking.title}</span>
        <span className="day-view-event__time">
          {startTime} – {endTime}
        </span>
        {booking.conflict_flags.length > 0 && (
          <ConflictBadge
            conflicts={booking.conflict_flags}
            onClick={onClick}
            size="small"
          />
        )}
      </div>
    </div>
  );
}
