import { useEffect, useState, useCallback } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { feedsApi } from '../api/feeds';
import type { FeedBooking } from '../api/types';
import { ApprovedOnlyBadge } from '../components/display/ApprovedOnlyBadge';

const AUTO_REFRESH_MS = 60000; // 60 seconds = 60000ms

// Group bookings by date (YYYY-MM-DD)
function groupByDate(bookings: FeedBooking[]): Map<string, FeedBooking[]> {
  const map = new Map<string, FeedBooking[]>();
  for (const booking of bookings) {
    const date = booking.start_time.slice(0, 10); // "YYYY-MM-DD"
    const list = map.get(date) ?? [];
    list.push(booking);
    map.set(date, list);
  }
  // Sort within each group by start_time
  for (const [, list] of map) {
    list.sort((a, b) => a.start_time.localeCompare(b.start_time));
  }
  return map;
}

function formatDate(dateStr: string): string {
  const date = new Date(dateStr + 'T00:00:00');
  return date.toLocaleDateString(undefined, {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
}

function formatTime(isoString: string): string {
  const date = new Date(isoString);
  return date.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' });
}

export function DisplayBoard() {
  const [searchParams] = useSearchParams();
  const locationId = searchParams.get('location_id') ?? undefined;

  const [bookings, setBookings] = useState<FeedBooking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchBookings = useCallback(async () => {
    try {
      const params = locationId ? { location_id: locationId } : undefined;
      const response = await feedsApi.getDisplayBoard(params);
      setBookings(response.data);
      setError(null);
    } catch {
      setError('Failed to load bookings. Retrying…');
    } finally {
      setLoading(false);
    }
  }, [locationId]);

  // Initial fetch
  useEffect(() => {
    fetchBookings();
  }, [fetchBookings]);

  // Auto-refresh every 60 seconds
  useEffect(() => {
    const timer = setInterval(fetchBookings, AUTO_REFRESH_MS); // auto-refresh every 60000ms
    return () => clearInterval(timer);
  }, [fetchBookings]);

  const grouped = groupByDate(bookings);
  const sortedDates = Array.from(grouped.keys()).sort();

  return (
    <div
      data-testid="display-board"
      style={{
        minHeight: '100vh',
        background: '#0f172a',
        color: '#f1f5f9',
        fontFamily: 'var(--font-family)',
        overflowY: 'auto',
        padding: 'var(--spacing-xl)',
        boxSizing: 'border-box',
      }}
    >
      {/* Approved-Only Watermark */}
      <ApprovedOnlyBadge />

      {/* Page Title */}
      <h1
        style={{
          fontSize: 'var(--font-size-3xl)',
          fontWeight: 'var(--font-weight-bold)',
          marginBottom: 'var(--spacing-xl)',
          marginTop: 0,
          textAlign: 'center',
          letterSpacing: '-0.025em',
          color: '#e2e8f0',
        }}
      >
        Booking Display Board
      </h1>

      {/* Loading state */}
      {loading && (
        <div
          role="status"
          aria-live="polite"
          style={{ textAlign: 'center', padding: 'var(--spacing-2xl)', color: '#94a3b8' }}
        >
          Loading bookings…
        </div>
      )}

      {/* Error state */}
      {error && !loading && (
        <div
          role="alert"
          aria-live="assertive"
          style={{
            textAlign: 'center',
            padding: 'var(--spacing-xl)',
            color: '#fca5a5',
            background: 'rgba(239,68,68,0.1)',
            borderRadius: 'var(--border-radius)',
            marginBottom: 'var(--spacing-xl)',
          }}
        >
          {error}
        </div>
      )}

      {/* Empty state */}
      {!loading && !error && bookings.length === 0 && (
        <div
          style={{
            textAlign: 'center',
            padding: 'var(--spacing-2xl)',
            color: '#94a3b8',
          }}
        >
          No approved bookings in the next 7 days.
        </div>
      )}

      {/* Bookings grouped by date */}
      {!loading && sortedDates.map((date) => {
        const dayBookings = grouped.get(date)!;
        return (
          <section
            key={date}
            style={{ marginBottom: 'var(--spacing-2xl)' }}
          >
            <h2
              style={{
                fontSize: 'var(--font-size-xl)',
                fontWeight: 'var(--font-weight-semibold)',
                color: '#7dd3fc',
                borderBottom: '1px solid #1e3a5f',
                paddingBottom: 'var(--spacing-sm)',
                marginBottom: 'var(--spacing-md)',
                marginTop: 0,
              }}
            >
              {formatDate(date)}
            </h2>
            <ul
              style={{
                listStyle: 'none',
                padding: 0,
                margin: 0,
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
                gap: 'var(--spacing-md)',
              }}
            >
              {dayBookings.map((booking) => (
                <li
                  key={booking.id}
                  data-testid="booking-card"
                  style={{
                    background: '#1e293b',
                    borderRadius: 'var(--border-radius-md)',
                    padding: 'var(--spacing-md)',
                    boxShadow: 'var(--shadow-md)',
                    borderLeft: '4px solid var(--location-color, #3b82f6)',
                  }}
                >
                  {/* Title */}
                  <div
                    style={{
                      fontSize: 'var(--font-size-lg)',
                      fontWeight: 'var(--font-weight-bold)',
                      color: '#f1f5f9',
                      marginBottom: 'var(--spacing-xs)',
                    }}
                  >
                    {booking.title}
                  </div>

                  {/* Location */}
                  <div
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: 'var(--spacing-xs)',
                      color: '#94a3b8',
                      fontSize: 'var(--font-size-sm)',
                      marginBottom: 'var(--spacing-xs)',
                    }}
                  >
                    <span
                      aria-hidden="true"
                      style={{
                        width: 10,
                        height: 10,
                        borderRadius: '50%',
                        background: 'var(--location-color, #3b82f6)',
                        flexShrink: 0,
                        display: 'inline-block',
                      }}
                    />
                    {booking.location_name}
                  </div>

                  {/* Time */}
                  <div
                    style={{
                      fontSize: 'var(--font-size-sm)',
                      color: '#cbd5e1',
                      fontWeight: 'var(--font-weight-medium)',
                    }}
                  >
                    {formatTime(booking.start_time)} – {formatTime(booking.end_time)}
                  </div>
                </li>
              ))}
            </ul>
          </section>
        );
      })}

      {/* Footer */}
      <footer
        style={{
          marginTop: 'var(--spacing-2xl)',
          textAlign: 'center',
          fontSize: 'var(--font-size-xs)',
          color: '#475569',
        }}
      >
        <Link
          to="/feeds"
          style={{ color: '#475569', textDecoration: 'underline' }}
        >
          View Feed Subscription
        </Link>
      </footer>
    </div>
  );
}
