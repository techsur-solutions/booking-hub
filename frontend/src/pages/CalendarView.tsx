import { useState } from 'react';
import { useViewState } from '../hooks/useViewState';
import { FullCalendarWrapper } from '../components/calendar/FullCalendarWrapper';
import type { SelectedSlot } from '../components/calendar/FullCalendarWrapper';
import { BookingDetailModal } from '../components/booking/BookingDetailModal';
import type { Booking } from '../api/types';

/**
 * CalendarView
 *
 * Month/week calendar view backed by FullCalendar.
 * Reads the initial date from URL ?date= param (UI-CAL-04) and writes back
 * when the user navigates to a different date.
 *
 * Integrates BookingDetailModal (UI-BOOK-03): clicking a booking event opens
 * the modal showing full booking info and actions.
 */
export function CalendarView() {
  const { date, updateState } = useViewState();
  const [selectedBookingId, setSelectedBookingId] = useState<string | null>(null);

  function handleDateClick(slot: SelectedSlot) {
    // Update the ?date= URL param when user clicks a date (enables cross-view date sharing)
    const clickedDate = slot.start.substring(0, 10);
    updateState({ date: clickedDate });
  }

  function handleEventClick(booking: Booking) {
    // Open BookingDetailModal for clicked booking (UI-BOOK-03)
    setSelectedBookingId(booking.id);
  }

  return (
    <div className="calendar-view" data-testid="calendar-view">
      <h1 className="sr-only">Calendar View</h1>
      <FullCalendarWrapper
        initialDate={date}
        onDateClick={handleDateClick}
        onEventClick={handleEventClick}
      />
      <BookingDetailModal
        bookingId={selectedBookingId}
        onClose={() => setSelectedBookingId(null)}
      />
    </div>
  );
}
