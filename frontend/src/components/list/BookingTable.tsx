import { useState } from 'react';
import type { Booking, Location, Resource } from '../../api/types';
import { ConflictBadge } from '../ConflictBadge';
import { PermissionGate } from '../PermissionGate';

export type SortKey =
  | 'title'
  | 'location'
  | 'start_time'
  | 'end_time'
  | 'status';

export type SortDirection = 'asc' | 'desc';

interface BookingTableProps {
  bookings: Booking[];
  locations: Location[];
  resources: Resource[];
  onRowClick: (booking: Booking) => void;
  onEdit?: (booking: Booking) => void;
  onDelete?: (booking: Booking) => void;
}

/** Format an ISO 8601 date-time string as "YYYY-MM-DD HH:mm" */
function formatDateTime(iso: string): string {
  if (!iso) return '';
  const d = new Date(iso);
  if (isNaN(d.getTime())) return iso;
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

const STATUS_CLASS: Record<Booking['status'], string> = {
  pending: 'badge--pending',
  approved: 'badge--approved',
  denied: 'badge--denied',
};

function SortIcon({ active, direction }: { active: boolean; direction: SortDirection }) {
  if (!active) return <span className="sort-icon sort-icon--inactive" aria-hidden="true">⇅</span>;
  return (
    <span className="sort-icon sort-icon--active" aria-hidden="true">
      {direction === 'asc' ? '↑' : '↓'}
    </span>
  );
}

/**
 * BookingTable — sortable table rendering bookings with 7 columns:
 * Title | Location | Resources | Start Time | End Time | Status | Conflict Flags | Actions
 *
 * Sort is client-side (clicking column headers). Conflict Flags column renders
 * ConflictBadge. Actions column is permission-gated via PermissionGate.
 */
export function BookingTable({
  bookings,
  locations,
  resources,
  onRowClick,
  onEdit,
  onDelete,
}: BookingTableProps) {
  const [sortKey, setSortKey] = useState<SortKey>('start_time');
  const [sortDir, setSortDir] = useState<SortDirection>('asc');

  function toggleSort(key: SortKey) {
    if (sortKey === key) {
      setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortKey(key);
      setSortDir('asc');
    }
  }

  // Build lookup maps for locations and resources
  const locationMap = Object.fromEntries(locations.map((l) => [l.id, l]));
  const resourceMap = Object.fromEntries(resources.map((r) => [r.id, r]));

  // Client-side sort
  const sorted = [...bookings].sort((a, b) => {
    let aVal = '';
    let bVal = '';
    switch (sortKey) {
      case 'title':
        aVal = a.title;
        bVal = b.title;
        break;
      case 'location':
        aVal = locationMap[a.location_id]?.name ?? '';
        bVal = locationMap[b.location_id]?.name ?? '';
        break;
      case 'start_time':
        aVal = a.start_time;
        bVal = b.start_time;
        break;
      case 'end_time':
        aVal = a.end_time;
        bVal = b.end_time;
        break;
      case 'status':
        aVal = a.status;
        bVal = b.status;
        break;
    }
    const cmp = aVal.localeCompare(bVal);
    return sortDir === 'asc' ? cmp : -cmp;
  });

  if (sorted.length === 0) {
    return (
      <div className="booking-table__empty" data-testid="booking-table-empty">
        No bookings match the current filters.
      </div>
    );
  }

  return (
    <div className="booking-table-wrapper" data-testid="booking-table">
      <table className="booking-table" aria-label="Bookings list">
        <thead>
          <tr>
            <th scope="col">
              <button
                className="sort-btn"
                onClick={() => toggleSort('title')}
                data-testid="sort-title"
                aria-label={`Sort by title ${sortKey === 'title' ? (sortDir === 'asc' ? 'descending' : 'ascending') : 'ascending'}`}
              >
                Title{' '}
                <SortIcon active={sortKey === 'title'} direction={sortDir} />
              </button>
            </th>
            <th scope="col">
              <button
                className="sort-btn"
                onClick={() => toggleSort('location')}
                data-testid="sort-location"
                aria-label="Sort by location"
              >
                Location{' '}
                <SortIcon active={sortKey === 'location'} direction={sortDir} />
              </button>
            </th>
            <th scope="col">Resources</th>
            <th scope="col">
              <button
                className="sort-btn"
                onClick={() => toggleSort('start_time')}
                data-testid="sort-start-time"
                aria-label="Sort by start time"
              >
                Start Time{' '}
                <SortIcon active={sortKey === 'start_time'} direction={sortDir} />
              </button>
            </th>
            <th scope="col">
              <button
                className="sort-btn"
                onClick={() => toggleSort('end_time')}
                data-testid="sort-end-time"
                aria-label="Sort by end time"
              >
                End Time{' '}
                <SortIcon active={sortKey === 'end_time'} direction={sortDir} />
              </button>
            </th>
            <th scope="col">
              <button
                className="sort-btn"
                onClick={() => toggleSort('status')}
                data-testid="sort-status"
                aria-label="Sort by status"
              >
                Status{' '}
                <SortIcon active={sortKey === 'status'} direction={sortDir} />
              </button>
            </th>
            <th scope="col">Conflict Flags</th>
            <th scope="col">Actions</th>
          </tr>
        </thead>
        <tbody>
          {sorted.map((booking) => {
            const location = locationMap[booking.location_id];
            const bookingResources = (booking.resource_ids ?? [])
              .map((rid) => resourceMap[rid])
              .filter(Boolean);

            return (
              <tr
                key={booking.id}
                className="booking-table__row"
                onClick={() => onRowClick(booking)}
                style={{ cursor: 'pointer' }}
                data-testid={`booking-row-${booking.id}`}
                tabIndex={0}
                onKeyDown={(e) => e.key === 'Enter' && onRowClick(booking)}
                aria-label={`Booking: ${booking.title}`}
              >
                {/* Title */}
                <td className="booking-table__title">
                  <span className="booking-table__title-text">{booking.title}</span>
                </td>

                {/* Location */}
                <td className="booking-table__location">
                  {location ? (
                    <span className="booking-table__location-inner">
                      <span
                        className="booking-table__location-dot"
                        style={{ backgroundColor: location.colour }}
                        aria-hidden="true"
                      />
                      {location.name}
                    </span>
                  ) : (
                    <span className="booking-table__location-unknown">—</span>
                  )}
                </td>

                {/* Resources */}
                <td className="booking-table__resources">
                  {bookingResources.length > 0
                    ? bookingResources.map((r) => r.name).join(', ')
                    : <span className="booking-table__none">—</span>}
                </td>

                {/* Start Time */}
                <td className="booking-table__time">
                  {formatDateTime(booking.start_time)}
                </td>

                {/* End Time */}
                <td className="booking-table__time">
                  {formatDateTime(booking.end_time)}
                </td>

                {/* Status */}
                <td className="booking-table__status">
                  <span
                    className={`badge ${STATUS_CLASS[booking.status]}`}
                    data-testid={`status-badge-${booking.id}`}
                  >
                    {booking.status.charAt(0).toUpperCase() + booking.status.slice(1)}
                  </span>
                </td>

                {/* Conflict Flags */}
                <td
                  className="booking-table__conflicts"
                  onClick={(e) => e.stopPropagation()} // prevent row click when badge clicked
                >
                  <ConflictBadge
                    conflicts={booking.conflict_flags ?? []}
                    onClick={(conflictingId) => {
                      // Open the conflicting booking's detail modal
                      const conflicting = bookings.find((b) => b.id === conflictingId);
                      if (conflicting) onRowClick(conflicting);
                    }}
                    size="small"
                  />
                </td>

                {/* Actions */}
                <td
                  className="booking-table__actions"
                  onClick={(e) => e.stopPropagation()} // prevent row click when actions clicked
                >
                  <PermissionGate requireAnyRole={['role_booking_approver']}>
                    {onEdit && (
                      <button
                        type="button"
                        className="booking-table__action-btn"
                        onClick={() => onEdit(booking)}
                        aria-label={`Edit booking: ${booking.title}`}
                        data-testid={`edit-booking-${booking.id}`}
                      >
                        ✏
                      </button>
                    )}
                    {onDelete && (
                      <button
                        type="button"
                        className="booking-table__action-btn booking-table__action-btn--danger"
                        onClick={() => onDelete(booking)}
                        aria-label={`Delete booking: ${booking.title}`}
                        data-testid={`delete-booking-${booking.id}`}
                      >
                        🗑
                      </button>
                    )}
                  </PermissionGate>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
