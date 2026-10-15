import { useState } from 'react';
import { ConflictBadge } from '../ConflictBadge';
import { BookingDetailModal } from '../booking/BookingDetailModal';
import { ApproveButton } from './ApproveButton';
import { DenyButton } from './DenyButton';
import type { Booking } from '../../api/types';

export interface ApprovalTableProps {
  bookings: Booking[];
  /** Called after a booking is approved — removes row from table (optimistic UI) */
  onApproved: (id: string) => void;
  /** Called after a booking is denied — removes row from table (optimistic UI) */
  onDenied: (id: string) => void;
}

/**
 * ApprovalTable — renders pending bookings in an 8-column table.
 *
 * Columns:
 *   1. Title (clickable → BookingDetailModal)
 *   2. Requester (created_by / owner_id)
 *   3. Location (location_id with colour dot — resolved inline)
 *   4. Resources (comma-separated resource_ids)
 *   5. Start Time (YYYY-MM-DD HH:mm)
 *   6. End Time (YYYY-MM-DD HH:mm)
 *   7. Conflict Flags (inline ConflictBadge)
 *   8. Actions (ApproveButton + DenyButton)
 *
 * Sorted by created_at ascending (oldest first) per UI-APPR-01.
 */
export function ApprovalTable({ bookings, onApproved, onDenied }: ApprovalTableProps) {
  const [selectedBookingId, setSelectedBookingId] = useState<string | null>(null);

  // Sort by created_at ascending (oldest first)
  const sorted = [...bookings].sort(
    (a, b) => new Date(a.created_at).getTime() - new Date(b.created_at).getTime()
  );

  return (
    <>
      <div className="approval-table-wrapper" data-testid="approval-table">
        <table className="approval-table">
          <thead>
            <tr>
              <th scope="col">Title</th>
              <th scope="col">Requester</th>
              <th scope="col">Location</th>
              <th scope="col">Resources</th>
              <th scope="col">Start Time</th>
              <th scope="col">End Time</th>
              <th scope="col">Conflict Flags</th>
              <th scope="col">Actions</th>
            </tr>
          </thead>
          <tbody>
            {sorted.length === 0 ? (
              <tr>
                <td colSpan={8} className="approval-table__empty" data-testid="approval-table-empty">
                  No pending bookings
                </td>
              </tr>
            ) : (
              sorted.map((booking) => (
                <ApprovalRow
                  key={booking.id}
                  booking={booking}
                  onTitleClick={() => setSelectedBookingId(booking.id)}
                  onApproved={onApproved}
                  onDenied={onDenied}
                />
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Booking Detail Modal — opened when title clicked */}
      <BookingDetailModal
        bookingId={selectedBookingId}
        onClose={() => setSelectedBookingId(null)}
        onDeleted={() => {
          if (selectedBookingId) {
            onApproved(selectedBookingId); // treat deleted as removed from queue
          }
          setSelectedBookingId(null);
        }}
      />
    </>
  );
}

/* ─────────────────────────────────────────────────────────────────────────────
   Individual row
   ───────────────────────────────────────────────────────────────────────────── */
interface ApprovalRowProps {
  booking: Booking;
  onTitleClick: () => void;
  onApproved: (id: string) => void;
  onDenied: (id: string) => void;
}

function ApprovalRow({ booking, onTitleClick, onApproved, onDenied }: ApprovalRowProps) {
  return (
    <tr data-testid={`approval-row-${booking.id}`}>
      {/* Title — clickable */}
      <td>
        <button
          type="button"
          className="approval-table__title-link"
          onClick={onTitleClick}
          data-testid={`booking-title-${booking.id}`}
        >
          {booking.title}
        </button>
      </td>

      {/* Requester */}
      <td data-testid={`booking-requester-${booking.id}`}>{booking.owner_id}</td>

      {/* Location */}
      <td data-testid={`booking-location-${booking.id}`}>{booking.location_id}</td>

      {/* Resources */}
      <td data-testid={`booking-resources-${booking.id}`}>
        {booking.resource_ids.length > 0 ? booking.resource_ids.join(', ') : '—'}
      </td>

      {/* Start Time */}
      <td data-testid={`booking-start-${booking.id}`}>{formatDateTime(booking.start_time)}</td>

      {/* End Time */}
      <td data-testid={`booking-end-${booking.id}`}>{formatDateTime(booking.end_time)}</td>

      {/* Conflict Flags */}
      <td data-testid={`booking-conflicts-${booking.id}`}>
        {booking.conflict_flags.length > 0 ? (
          <ConflictBadge conflicts={booking.conflict_flags} />
        ) : (
          <span className="approval-table__no-conflicts" aria-label="No conflicts">
            —
          </span>
        )}
      </td>

      {/* Actions */}
      <td className="approval-table__actions" data-testid={`booking-actions-${booking.id}`}>
        <ApproveButton booking={booking} onApproved={onApproved} />
        <DenyButton booking={booking} onDenied={onDenied} />
      </td>
    </tr>
  );
}

/* ─────────────────────────────────────────────────────────────────────────────
   Helpers
   ───────────────────────────────────────────────────────────────────────────── */
function formatDateTime(iso: string): string {
  const d = new Date(iso);
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  const hh = String(d.getHours()).padStart(2, '0');
  const min = String(d.getMinutes()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd} ${hh}:${min}`;
}
