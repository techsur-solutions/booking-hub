import { FullCalendarWrapper } from '../components/calendar/FullCalendarWrapper';
import type { SelectedSlot } from '../components/calendar/FullCalendarWrapper';
import type { Booking } from '../api/types';

export function CalendarView() {
  function handleDateClick(slot: SelectedSlot) {
    // TODO: Open Booking Create Form pre-filled with slot.start — integrated in 09-06
    console.debug('[CalendarView] date clicked:', slot);
  }

  function handleEventClick(booking: Booking) {
    // TODO: Open Booking Detail Modal — integrated in 09-07
    console.debug('[CalendarView] booking clicked:', booking.id);
  }

  return (
    <div className="calendar-view" data-testid="calendar-view">
      <h1 className="sr-only">Calendar View</h1>
      <FullCalendarWrapper onDateClick={handleDateClick} onEventClick={handleEventClick} />
    </div>
  );
}
