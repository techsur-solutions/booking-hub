import type { EventApi } from '@fullcalendar/core';
import { ConflictBadge } from '../ConflictBadge';
import type { Booking } from '../../api/types';

interface BookingEventProps {
  event: EventApi;
  booking: Booking;
}

export function BookingEvent({ event, booking }: BookingEventProps) {
  return (
    <div className="fc-event-main-frame" data-testid="booking-event">
      <div className="fc-event-title">{event.title}</div>
      {booking.conflict_flags.length > 0 && (
        <ConflictBadge conflicts={booking.conflict_flags} size="small" />
      )}
    </div>
  );
}
