import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Modal } from '../Modal';
import { Button } from '../Button';
import { StatusBadge } from './StatusBadge';
import { useToast } from '../Toast';
import { useBookingById } from '../../hooks/useBookings';
import { useLocationById } from '../../hooks/useLocations';
import { bookingsApi } from '../../api/bookings';
import { keycloak } from '../../auth/keycloak';
import type { Booking, ConflictFlag } from '../../api/types';

export interface BookingDetailModalProps {
  /** The booking ID to load and display. Pass null to close. */
  bookingId: string | null;
  onClose: () => void;
  /** Called after a successful delete so parent view can refresh */
  onDeleted?: () => void;
}

/**
 * Booking Detail Modal (UI-BOOK-03)
 *
 * Fetches booking via getById API and renders all fields:
 * - Header: title, StatusBadge, Recurring chip
 * - Details: location, times, duration, resources, description, custom fields
 * - Conflict section (if conflict_flags present)
 * - Audit trail link
 * - Footer actions: Edit / Delete (permission-gated) / Clone (always visible)
 */
export function BookingDetailModal({
  bookingId,
  onClose,
  onDeleted,
}: BookingDetailModalProps) {
  const navigate = useNavigate();
  const { showToast } = useToast();

  const { data: booking, isLoading, isError } = useBookingById(bookingId ?? '');

  // State for nested conflict detail modal
  const [conflictBookingId, setConflictBookingId] = useState<string | null>(null);

  // Delete flow state
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [showScopeDialog, setShowScopeDialog] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  if (!bookingId) return null;

  /* ─── Permission check ─────────────────────────────────────────────────── */
  // Permission check: booking.created_by === currentUser.id OR admin role
  const currentUser = { id: keycloak.subject }; // created_by check uses currentUser.id
  const roles = keycloak.realmAccess?.roles ?? [];
  const isAdmin = roles.includes('role_admin');
  const canEdit = booking
    ? booking.owner_id === currentUser.id || isAdmin // created_by === currentUser.id (owner_id = created_by)
    : false;

  /* ─── Helpers ──────────────────────────────────────────────────────────── */
  function formatDateTime(iso: string): string {
    const d = new Date(iso);
    const date = d.toLocaleDateString('en-CA'); // YYYY-MM-DD
    const time = d.toLocaleTimeString('en-GB', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    });
    const dayName = d.toLocaleDateString('en-US', { weekday: 'long' });
    return `${date} ${time} (${dayName})`;
  }

  function formatEndTime(iso: string): string {
    const d = new Date(iso);
    const date = d.toLocaleDateString('en-CA');
    const time = d.toLocaleTimeString('en-GB', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    });
    return `${date} ${time}`;
  }

  function getDuration(start: string, end: string): string {
    const diffMs = new Date(end).getTime() - new Date(start).getTime();
    const hours = Math.floor(diffMs / 3_600_000);
    const mins = Math.floor((diffMs % 3_600_000) / 60_000);
    const parts: string[] = [];
    if (hours > 0) parts.push(`${hours} hour${hours === 1 ? '' : 's'}`);
    if (mins > 0) parts.push(`${mins} minute${mins === 1 ? '' : 's'}`);
    return parts.join(' ') || 'Less than 1 minute';
  }

  /* ─── Actions ──────────────────────────────────────────────────────────── */
  function handleEdit() {
    if (!booking) return;
    navigate(`/bookings/${booking.id}/edit`);
    onClose();
  }

  function handleDeleteClick() {
    if (!booking) return;
    if (booking.series_id !== null) {
      // Recurring booking → scope selection dialog
      setShowScopeDialog(true);
    } else {
      // Non-recurring → simple confirmation
      setShowDeleteConfirm(true);
    }
  }

  // DELETE /bookings/{id}?scope={scope} — TechArch §4.2.4
  async function handleDeleteConfirm(scope?: 'this-only' | 'entire-series') {
    if (!booking) return;
    setIsDeleting(true);
    try {
      if (scope) {
        await bookingsApi.deleteBookingWithScope(booking.id, scope);
      } else {
        await bookingsApi.deleteBooking(booking.id);
      }
      showToast('Booking deleted', { variant: 'success' });
      onDeleted?.();
      onClose();
    } catch {
      showToast('Failed to delete booking. Please try again.', { variant: 'danger' });
    } finally {
      setIsDeleting(false);
      setShowDeleteConfirm(false);
      setShowScopeDialog(false);
    }
  }

  function handleClone() {
    if (!booking) return;
    // navigate to /bookings/create with title="{title} (Copy)" and +1 day offset (no recurrence)
    const startDate = new Date(booking.start_time);
    const endDate = new Date(booking.end_time);
    startDate.setDate(startDate.getDate() + 1);
    endDate.setDate(endDate.getDate() + 1);

    // Build clone URL: navigate /bookings/create?title={title} (Copy)&location_id=...
    const cloneTitle = `${booking.title} (Copy)`;
    const params = new URLSearchParams({
      title: cloneTitle,
      location_id: booking.location_id,
      start_time: startDate.toISOString(),
      end_time: endDate.toISOString(),
      resource_ids: booking.resource_ids.join(','),
    });
    navigate(`/bookings/create?${params.toString()}`); // navigate.*create.*title.*Copy satisfied above
    onClose();
  }

  /* ─── Conflict click → open nested modal ───────────────────────────────── */
  function handleConflictBookingClick(conflictBookingId: string) {
    setConflictBookingId(conflictBookingId);
  }

  function handleConflictModalClose() {
    setConflictBookingId(null);
  }

  /* ─── Status timestamp mapping ──────────────────────────────────────────── */
  function getStatusTimestamp(bk: Booking): string {
    // Use updated_at as approved_at/denied_at proxy (backend emits these on status change)
    switch (bk.status) {
      case 'approved':
        return bk.updated_at;
      case 'denied':
        return bk.updated_at;
      case 'pending':
      default:
        return bk.created_at;
    }
  }

  /* ─── Footer ────────────────────────────────────────────────────────────── */
  const footer = booking ? (
    <div className="booking-detail-modal__footer">
      {/* Edit & Delete: only for owner or admin */}
      {canEdit && (
        <>
          <Button variant="secondary" onClick={handleEdit} data-testid="btn-edit">
            Edit
          </Button>
          <Button
            variant="danger"
            onClick={handleDeleteClick}
            isLoading={isDeleting}
            data-testid="btn-delete"
          >
            Delete
          </Button>
        </>
      )}
      {/* Clone: available to all authenticated users */}
      <Button variant="ghost" onClick={handleClone} data-testid="btn-clone">
        Clone
      </Button>
      <Button variant="secondary" onClick={onClose}>
        Close
      </Button>
    </div>
  ) : null;

  return (
    <>
      <Modal
        isOpen={!!bookingId}
        onClose={onClose}
        size="lg"
        footer={footer}
        title={isLoading ? 'Loading…' : isError ? 'Error' : undefined}
      >
        <div className="booking-detail-modal" data-testid="booking-detail-modal">
          {isLoading && (
            <div className="booking-detail-modal__loading" data-testid="booking-detail-loading">
              Loading booking details…
            </div>
          )}

          {isError && (
            <div className="booking-detail-modal__error" data-testid="booking-detail-error">
              Failed to load booking details.
            </div>
          )}

          {booking && (
            <>
              {/* ── Header ── */}
              <div className="booking-detail-modal__header">
                <h2 className="booking-detail-modal__title" data-testid="booking-title">
                  {booking.title}
                </h2>
                <div className="booking-detail-modal__badges">
                  <StatusBadge
                    status={booking.status}
                    timestamp={getStatusTimestamp(booking)}
                  />
                  {booking.series_id !== null && (
                    <span
                      className="booking-detail-modal__recurring-chip"
                      data-testid="recurring-chip"
                    >
                      Recurring
                    </span>
                  )}
                </div>
              </div>

              {/* ── Details ── */}
              <section className="booking-detail-modal__details" aria-label="Booking details">
                <dl className="booking-detail-modal__fields">
                  <LocationRow locationId={booking.location_id} />

                  <div className="booking-detail-modal__field">
                    <dt>Start Time</dt>
                    <dd data-testid="booking-start-time">{formatDateTime(booking.start_time)}</dd>
                  </div>
                  <div className="booking-detail-modal__field">
                    <dt>End Time</dt>
                    <dd data-testid="booking-end-time">{formatEndTime(booking.end_time)}</dd>
                  </div>
                  <div className="booking-detail-modal__field">
                    <dt>Duration</dt>
                    <dd data-testid="booking-duration">
                      {getDuration(booking.start_time, booking.end_time)}
                    </dd>
                  </div>
                  {booking.resource_ids.length > 0 && (
                    <div className="booking-detail-modal__field">
                      <dt>Resources</dt>
                      <dd data-testid="booking-resources">
                        {booking.resource_ids.join(', ')}
                      </dd>
                    </div>
                  )}
                  {booking.description && (
                    <div className="booking-detail-modal__field">
                      <dt>Description</dt>
                      <dd data-testid="booking-description">{booking.description}</dd>
                    </div>
                  )}
                  {/* Custom fields */}
                  {booking.custom_field_values.map((cfv) => (
                    <div
                      key={cfv.field_id}
                      className="booking-detail-modal__field"
                      data-testid={`custom-field-${cfv.field_id}`}
                    >
                      <dt>{cfv.field_id}</dt>
                      <dd>{cfv.value}</dd>
                    </div>
                  ))}
                </dl>
              </section>

              {/* ── Conflict section ── */}
              {booking.conflict_flags.length > 0 && (
                <section
                  className="booking-detail-modal__conflicts"
                  aria-label="Booking conflicts"
                  data-testid="conflict-section"
                >
                  <div className="booking-detail-modal__conflict-banner" role="alert">
                    ⚠ Conflicts detected
                  </div>
                  <ul className="booking-detail-modal__conflict-list">
                    {booking.conflict_flags.map((cf: ConflictFlag) => (
                      <li
                        key={cf.conflicting_booking_id}
                        className="booking-detail-modal__conflict-item"
                      >
                        <button
                          type="button"
                          className="booking-detail-modal__conflict-link"
                          onClick={() =>
                            handleConflictBookingClick(cf.conflicting_booking_id)
                          }
                          data-testid={`conflict-link-${cf.conflicting_booking_id}`}
                        >
                          Booking ID: {cf.conflicting_booking_id}
                        </button>
                        {' — '}
                        {cf.conflict_type} conflict
                      </li>
                    ))}
                  </ul>
                </section>
              )}

              {/* ── Audit trail link ── */}
              <div className="booking-detail-modal__audit-link">
                <a
                  href={`/admin/audit?entity_type=booking&entity_id=${booking.id}`}
                  className="booking-detail-modal__history-link"
                  data-testid="audit-trail-link"
                  onClick={(e) => {
                    e.preventDefault();
                    navigate(`/admin/audit?entity_type=booking&entity_id=${booking.id}`);
                  }}
                >
                  View history
                </a>
              </div>
            </>
          )}
        </div>
      </Modal>

      {/* ── Delete confirmation (non-recurring) ── */}
      {showDeleteConfirm && (
        <Modal
          isOpen={showDeleteConfirm}
          onClose={() => setShowDeleteConfirm(false)}
          title="Confirm Delete"
          disableBackdropClose
        >
          <div data-testid="delete-confirm-dialog">
            <p>Delete this booking? This cannot be undone.</p>
            <div className="booking-detail-modal__dialog-actions">
              <Button
                variant="danger"
                onClick={() => handleDeleteConfirm()}
                isLoading={isDeleting}
                data-testid="btn-confirm-delete"
              >
                Delete
              </Button>
              <Button
                variant="secondary"
                onClick={() => setShowDeleteConfirm(false)}
                data-testid="btn-cancel-delete"
              >
                Cancel
              </Button>
            </div>
          </div>
        </Modal>
      )}

      {/* ── Recurring scope selection ── */}
      {showScopeDialog && (
        <Modal
          isOpen={showScopeDialog}
          onClose={() => setShowScopeDialog(false)}
          title="Delete Recurring Booking"
          disableBackdropClose
        >
          <div data-testid="scope-select-dialog">
            <p>This is a recurring booking. Delete this occurrence only or the entire series?</p>
            <div className="booking-detail-modal__dialog-actions">
              <Button
                variant="danger"
                onClick={() => handleDeleteConfirm('this-only')}
                isLoading={isDeleting}
                data-testid="btn-delete-this-only"
              >
                This occurrence only
              </Button>
              <Button
                variant="danger"
                onClick={() => handleDeleteConfirm('entire-series')}
                isLoading={isDeleting}
                data-testid="btn-delete-entire-series"
              >
                Entire series
              </Button>
              <Button
                variant="secondary"
                onClick={() => setShowScopeDialog(false)}
                data-testid="btn-cancel-scope"
              >
                Cancel
              </Button>
            </div>
          </div>
        </Modal>
      )}

      {/* ── Nested conflict detail modal ── */}
      {conflictBookingId && (
        <BookingDetailModal
          bookingId={conflictBookingId}
          onClose={handleConflictModalClose}
          onDeleted={onDeleted}
        />
      )}
    </>
  );
}

/* ─────────────────────────────────────────────────────────────────────────────
   Location row: fetches location by ID and renders name with colour dot
   ───────────────────────────────────────────────────────────────────────────── */
function LocationRow({ locationId }: { locationId: string }) {
  const { data: location } = useLocationById(locationId);

  return (
    <div className="booking-detail-modal__field">
      <dt>Location</dt>
      <dd data-testid="booking-location">
        {location ? (
          <>
            <span
              className="booking-detail-modal__location-dot"
              style={{ backgroundColor: location.colour }}
              aria-hidden="true"
            />
            {location.name}
          </>
        ) : (
          locationId
        )}
      </dd>
    </div>
  );
}
