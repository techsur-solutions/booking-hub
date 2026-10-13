import { useViewState } from '../hooks/useViewState';
import { FullCalendarWrapper } from '../components/calendar/FullCalendarWrapper';
import type { SelectedSlot } from '../components/calendar/FullCalendarWrapper';
import type { Booking } from '../api/types';

/**
 * CalendarView
 *
 * Month/week calendar view backed by FullCalendar.
 * Reads the initial date from URL ?date= param (UI-CAL-04) and writes back
 * when the user navigates to a different date.
 */
export function CalendarView() {
  const { date, updateState } = useViewState();

  function handleDateClick(slot: SelectedSlot) {
    // TODO: Open Booking Create Form pre-filled with slot.start — integrated in 09-06
    console.debug('[CalendarView] date clicked:', slot);
  }

  function handleEventClick(booking: Booking) {
    // TODO: Open Booking Detail Modal — integrated in 09-07
    console.debug('[CalendarView] booking clicked:', booking.id);
  }

  /** Sync URL when FullCalendar navigates to a new month/week */
  function handleDateNavigate(newDate: string) {
    updateState({ date: newDate });
  }

  return (
    <div className="calendar-view" data-testid="calendar-view">
      <h1 className="sr-only">Calendar View</h1>
      <FullCalendarWrapper
        initialDate={date}
        onDateClick={handleDateClick}
        onEventClick={handleEventClick}
        onDateNavigate={handleDateNavigate}
      />
    </div>
  );
}
