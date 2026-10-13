import { useState } from 'react';
import { useBookings } from '../hooks/useBookings';
import { useLocations } from '../hooks/useLocations';
import { useSettings } from '../hooks/useSettings';
import { useViewState } from '../hooks/useViewState';
import { Modal } from '../components/Modal';
import { HourlySlotGrid } from '../components/calendar/HourlySlotGrid';
import type { Booking } from '../api/types';

// ── Helpers ──────────────────────────────────────────────────────────────────

/** Format a Date to 'YYYY-MM-DD' */
function toDateString(d: Date): string {
  return d.toISOString().substring(0, 10);
}

/** Validate that a string is a well-formed ISO-8601 date 'YYYY-MM-DD' */
function isValidDate(s: string): boolean {
  return /^\d{4}-\d{2}-\d{2}$/.test(s);
}

/** Build an ISO datetime string from a date string 'YYYY-MM-DD' and time 'HH:mm' */
function buildDateTime(date: string, time: string): string {
  return `${date}T${time}:00`;
}

// ── Booking Detail Modal ──────────────────────────────────────────────────────

interface BookingDetailModalProps {
  booking: Booking | null;
  onClose: () => void;
}

function BookingDetailModal({ booking, onClose }: BookingDetailModalProps) {
  if (!booking) return null;

  const startTime = booking.start_time.substring(11, 16);
  const endTime = booking.end_time.substring(11, 16);
  const startDate = booking.start_time.substring(0, 10);

  return (
    <Modal
      isOpen={!!booking}
      onClose={onClose}
      title={booking.title}
      size="default"
    >
      <dl className="booking-detail-list">
        <div className="booking-detail-item">
          <dt>Date</dt>
          <dd>{startDate}</dd>
        </div>
        <div className="booking-detail-item">
          <dt>Time</dt>
          <dd>
            {startTime} – {endTime}
          </dd>
        </div>
        <div className="booking-detail-item">
          <dt>Status</dt>
          <dd>
            <span className={`badge badge--${booking.status === 'approved' ? 'success' : booking.status === 'denied' ? 'danger' : 'warning'}`}>
              {booking.status}
            </span>
          </dd>
        </div>
        {booking.description && (
          <div className="booking-detail-item">
            <dt>Description</dt>
            <dd>{booking.description}</dd>
          </div>
        )}
        {booking.conflict_flags.length > 0 && (
          <div className="booking-detail-item">
            <dt>Conflicts</dt>
            <dd>{booking.conflict_flags.length} conflict(s) detected</dd>
          </div>
        )}
      </dl>
    </Modal>
  );
}

// ── Booking Create Modal ──────────────────────────────────────────────────────

interface CreateFormModalProps {
  /** Pre-filled location id (may be null for 'all locations') */
  locationId: string | null;
  /** Pre-filled start time 'HH:mm' */
  time: string;
  date: string;
  onClose: () => void;
}

function CreateFormModal({ locationId, time, date, onClose }: CreateFormModalProps) {
  return (
    <Modal isOpen onClose={onClose} title="New Booking" size="default">
      <p>
        Create a booking on <strong>{date}</strong> at <strong>{time}</strong>
        {locationId && (
          <>
            {' '}in location <strong>{locationId}</strong>
          </>
        )}
        .
      </p>
      <p className="form-hint">
        Full booking form (UI-BOOK-01) will replace this placeholder when that plan is
        executed.
      </p>
      <div style={{ textAlign: 'right', marginTop: '1rem' }}>
        <button type="button" className="btn btn--secondary btn--medium" onClick={onClose}>
          Cancel
        </button>
      </div>
    </Modal>
  );
}

// ── DayView Page ──────────────────────────────────────────────────────────────

/**
 * DayView
 *
 * Single-day schedule view with hourly slot layout.
 * - Uses `settings.calendar_min_time` / `calendar_max_time` / `calendar_slot_size`
 * - Same location colour-coding as Calendar View (locations.colour)
 * - Same ConflictBadge as Calendar View (via DayViewEvent)
 * - Clicking empty slot opens Booking Create Form pre-filled with location + time
 * - Clicking existing booking opens Booking Detail Modal
 * - Location selector and date picker update displayed bookings
 */
export function DayView() {
  const today = toDateString(new Date());

  // ── URL state (UI-CAL-04 view state persistence) ──
  const { date: urlDate, locationId: urlLocationId, updateState } = useViewState();

  // Derive local working state from URL params; fall back to defaults
  const selectedDate = isValidDate(urlDate) ? urlDate : today;
  // URL locationId is a validated UUID (or undefined); represent 'all' as empty string
  const selectedLocationId = urlLocationId ?? 'all';

  // Modal state
  const [detailBooking, setDetailBooking] = useState<Booking | null>(null);
  const [createSlot, setCreateSlot] = useState<{
    locationId: string | null;
    time: string;
  } | null>(null);

  // ── Data ──
  const { data: settings } = useSettings();
  const { data: locations = [] } = useLocations();

  const minTime = settings?.calendar_min_time ?? '07:00';
  const maxTime = settings?.calendar_max_time ?? '22:00';
  const slotSizeMinutes = settings?.calendar_slot_size ?? 30;

  // Build ISO date range for selected day
  const safeDateStr = isValidDate(selectedDate) ? selectedDate : today;
  const dayStart = buildDateTime(safeDateStr, '00:00');
  const dayEnd = buildDateTime(safeDateStr, '23:59');

  const { data: allBookings = [], isLoading } = useBookings({
    start: dayStart,
    end: dayEnd,
    ...(selectedLocationId !== 'all' ? { location_id: selectedLocationId } : {}),
  });

  // ── Handlers ──

  function handleSlotClick(locationId: string | null, time: string) {
    setCreateSlot({ locationId, time });
  }

  function handleEventClick(bookingId: string) {
    const booking = allBookings.find((b) => b.id === bookingId) ?? null;
    setDetailBooking(booking);
  }

  // ── Render ──

  return (
    <div className="day-view" data-testid="day-view">
      {/* ── Toolbar ── */}
      <div className="day-view__toolbar">
        <h1 className="day-view__title">Day View</h1>

        <div className="day-view__controls">
          {/* Date picker */}
          <label className="day-view__label" htmlFor="day-view-date">
            Date
          </label>
          <input
            id="day-view-date"
            type="date"
            className="form-input day-view__date-picker"
            value={selectedDate}
            onChange={(e) => updateState({ date: e.target.value })}
            data-testid="day-view-date-picker"
          />

          {/* Location filter */}
          <label className="day-view__label" htmlFor="day-view-location">
            Location
          </label>
          <select
            id="day-view-location"
            className="form-select day-view__location-select"
            value={selectedLocationId}
            onChange={(e) => {
              const val = e.target.value;
              // Store UUID in URL when a specific location is selected; clear param for 'all'
              updateState({ locationId: val !== 'all' ? val : undefined });
            }}
            data-testid="day-view-location-select"
          >
            <option value="all">All Locations</option>
            {locations.map((loc) => (
              <option key={loc.id} value={loc.id}>
                {loc.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* ── Slot grid ── */}
      {isLoading ? (
        <div className="day-view__loading" role="status" aria-live="polite">
          Loading bookings…
        </div>
      ) : (
        <HourlySlotGrid
          date={safeDateStr}
          bookings={allBookings}
          locations={locations}
          minTime={minTime}
          maxTime={maxTime}
          slotSizeMinutes={slotSizeMinutes}
          onSlotClick={handleSlotClick}
          onEventClick={handleEventClick}
          selectedLocationId={selectedLocationId !== 'all' ? selectedLocationId : null}
        />
      )}

      {/* ── Modals ── */}
      {detailBooking && (
        <BookingDetailModal
          booking={detailBooking}
          onClose={() => setDetailBooking(null)}
        />
      )}
      {createSlot && (
        <CreateFormModal
          locationId={createSlot.locationId}
          time={createSlot.time}
          date={safeDateStr}
          onClose={() => setCreateSlot(null)}
        />
      )}
    </div>
  );
}
