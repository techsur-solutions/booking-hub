import { useState } from 'react';
import FullCalendar from '@fullcalendar/react';
import type { DateClickArg } from '@fullcalendar/interaction';
import type { EventClickArg, EventContentArg } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import { useBookings } from '../../hooks/useBookings';
import { useLocations } from '../../hooks/useLocations';
import { BookingEvent } from './BookingEvent';
import type { Booking } from '../../api/types';

export interface SelectedSlot {
  start: string;
  end: string | null;
  locationId?: string;
}

interface FullCalendarWrapperProps {
  /** Called when user clicks an empty date/slot; receives pre-filled slot data */
  onDateClick?: (slot: SelectedSlot) => void;
  /** Called when user clicks an existing booking event; receives the booking */
  onEventClick?: (booking: Booking) => void;
}

export function FullCalendarWrapper({ onDateClick, onEventClick }: FullCalendarWrapperProps) {
  const { data: bookings, isLoading } = useBookings({});
  const { data: locations } = useLocations();

  // Track the active slot/booking for click handlers when no external handler provided
  const [_selectedSlot, setSelectedSlot] = useState<SelectedSlot | null>(null);
  const [_selectedBooking, setSelectedBooking] = useState<Booking | null>(null);

  const events =
    bookings?.map((booking) => {
      const location = locations?.find((l) => l.id === booking.location_id);
      return {
        id: booking.id,
        title: booking.title,
        start: booking.start_time,
        end: booking.end_time,
        backgroundColor: location?.colour ?? '#3b82f6',
        borderColor: location?.colour ?? '#3b82f6',
        extendedProps: {
          booking,
          hasConflict: booking.conflict_flags.length > 0,
        },
      };
    }) ?? [];

  function handleDateClick(arg: DateClickArg) {
    const slot: SelectedSlot = {
      start: arg.dateStr,
      end: null,
    };
    if (onDateClick) {
      onDateClick(slot);
    } else {
      // Store locally — Booking Create Form integration comes in a later plan
      setSelectedSlot(slot);
    }
  }

  function handleEventClick(arg: EventClickArg) {
    const booking = arg.event.extendedProps.booking as Booking;
    if (onEventClick) {
      onEventClick(booking);
    } else {
      // Store locally — Booking Detail Modal integration comes in a later plan
      setSelectedBooking(booking);
    }
  }

  function renderEventContent(arg: EventContentArg) {
    const booking = arg.event.extendedProps.booking as Booking;
    return <BookingEvent event={arg.event} booking={booking} />;
  }

  if (isLoading) {
    return (
      <div className="calendar-loading" data-testid="calendar-loading">
        Loading calendar…
      </div>
    );
  }

  return (
    <div className="calendar-wrapper" data-testid="fullcalendar-wrapper">
      <FullCalendar
        plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
        initialView="dayGridMonth"
        headerToolbar={{
          left: 'prev,next today',
          center: 'title',
          right: 'dayGridMonth,timeGridWeek',
        }}
        events={events}
        dateClick={handleDateClick}
        eventClick={handleEventClick}
        eventContent={renderEventContent}
        height="auto"
      />
    </div>
  );
}
