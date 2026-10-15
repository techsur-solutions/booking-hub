import type { Booking, Location } from '../../api/types';
import { DayViewEvent } from './DayViewEvent';

export interface HourlySlotGridProps {
  /** Selected date in 'YYYY-MM-DD' format */
  date: string;
  /** Bookings to display (pre-filtered to selected date + optional location) */
  bookings: Booking[];
  /** All locations for colour lookup */
  locations: Location[];
  /** Earliest visible time in 'HH:mm' format, e.g. "07:00" */
  minTime: string;
  /** Latest visible time in 'HH:mm' format, e.g. "22:00" */
  maxTime: string;
  /** Slot size in minutes (e.g. 30 means half-hour rows) */
  slotSizeMinutes: number;
  /** Called when a user clicks an empty slot */
  onSlotClick: (locationId: string | null, time: string) => void;
  /** Called when a user clicks an existing booking */
  onEventClick: (bookingId: string) => void;
  /** When set, only show bookings for this location; otherwise show all */
  selectedLocationId?: string | null;
}

/** Parse 'HH:mm' into total minutes from midnight */
function parseMinutes(time: string): number {
  const [h, m] = time.split(':').map(Number);
  return h * 60 + m;
}

/** Format total minutes into 'HH:mm' */
function formatTime(totalMinutes: number): string {
  const h = Math.floor(totalMinutes / 60);
  const m = totalMinutes % 60;
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
}

/**
 * Converts a 'HH:mm' time into a fractional CSS top offset (as a percentage)
 * relative to the visible grid area [minTime, maxTime].
 */
function timeToPercent(time: string, minMinutes: number, totalMinutes: number): number {
  const minutes = parseMinutes(time);
  return ((minutes - minMinutes) / totalMinutes) * 100;
}

/**
 * HourlySlotGrid
 *
 * Renders a vertical time-slot grid from `minTime` to `maxTime` in `slotSizeMinutes`
 * increments. Bookings are overlaid using absolute positioning based on their
 * start/end times relative to the visible window.
 *
 * Slot clicks fire `onSlotClick(selectedLocationId, slotTime)`.
 * Event clicks fire `onEventClick(bookingId)`.
 */
export function HourlySlotGrid({
  bookings,
  locations,
  minTime,
  maxTime,
  slotSizeMinutes,
  onSlotClick,
  onEventClick,
  selectedLocationId,
}: HourlySlotGridProps) {
  const minMinutes = parseMinutes(minTime);
  const maxMinutes = parseMinutes(maxTime);
  const totalMinutes = maxMinutes - minMinutes;

  // Build list of slot start times
  const slots: string[] = [];
  for (let m = minMinutes; m < maxMinutes; m += slotSizeMinutes) {
    slots.push(formatTime(m));
  }

  // Build location colour map for quick lookup
  const locationColourMap = new Map<string, string>(
    locations.map((loc) => [loc.id, loc.colour]),
  );

  // Filter bookings to those visible within the time window
  const visibleBookings = bookings.filter((b) => {
    const bookingStart = parseMinutes(b.start_time.substring(11, 16)); // 'HH:mm' from ISO
    const bookingEnd = parseMinutes(b.end_time.substring(11, 16));
    // Overlap: booking starts before grid ends AND ends after grid starts
    return bookingStart < maxMinutes && bookingEnd > minMinutes;
  });

  return (
    <div className="hourly-slot-grid" data-testid="hourly-slot-grid">
      {/* Time labels column + slot rows */}
      <div className="hourly-slot-grid__lanes">
        {/* Slot rows (clickable) */}
        {slots.map((slotTime) => (
          <div
            key={slotTime}
            className="hourly-slot-grid__row"
            data-time={slotTime}
            onClick={() => onSlotClick(selectedLocationId ?? null, slotTime)}
            role="button"
            tabIndex={0}
            aria-label={`Create booking at ${slotTime}`}
            onKeyDown={(e) => {
              if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                onSlotClick(selectedLocationId ?? null, slotTime);
              }
            }}
          >
            <span className="hourly-slot-grid__time-label">{slotTime}</span>
          </div>
        ))}

        {/* Bookings overlaid absolutely */}
        {visibleBookings.map((booking) => {
          const bookingStartStr = booking.start_time.substring(11, 16);
          const bookingEndStr = booking.end_time.substring(11, 16);

          const topPct = Math.max(
            0,
            timeToPercent(bookingStartStr, minMinutes, totalMinutes),
          );
          const bottomPct = Math.min(
            100,
            timeToPercent(bookingEndStr, minMinutes, totalMinutes),
          );
          const heightPct = bottomPct - topPct;

          const colour = locationColourMap.get(booking.location_id) ?? '#6b7280';

          return (
            <DayViewEvent
              key={booking.id}
              booking={booking}
              colour={colour}
              topPercent={topPct}
              heightPercent={heightPct}
              onClick={onEventClick}
            />
          );
        })}
      </div>
    </div>
  );
}
