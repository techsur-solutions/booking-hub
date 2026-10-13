import { useState } from 'react';
import { ConflictBadge } from '../ConflictBadge';
import { BookingDetailModal } from './BookingDetailModal';
import { Modal } from '../Modal';
import { Button } from '../Button';

export interface ConflictBannerConflict {
  booking_id: string;
  booking_title: string;
  conflict_type: 'location' | 'resource';
  resource_id?: string;
  resource_name?: string;
}

export interface ConflictBannerProps {
  conflicts: ConflictBannerConflict[];
  /**
   * True when user has allowApproveBooking permission (role_booking_approver).
   * Drives hard-block vs soft-warn UI: allowApproveBooking=false → hard-block red banner;
   * allowApproveBooking=true → soft-warn yellow banner with "Save Anyway" button.
   */
  userHasApprovePermission: boolean;
  /** Callback for approvers who confirm "Save Anyway" */
  onSaveAnyway?: () => void;
}

/**
 * ConflictBanner — enhanced conflict display with hard-block vs soft-warn logic.
 *
 * Hard-block (non-approver, conflicts present):
 *   - Red banner: "⚠️ Conflict detected - Cannot save"
 *   - Lists conflicts grouped by type (location vs resource) with clickable IDs
 *   - Parent form's Save button must be disabled when conflicts.length > 0 && !userHasApprovePermission
 *
 * Soft-warn (approver, conflicts present):
 *   - Yellow banner: "⚠️ Warning - Conflicts detected"
 *   - Same conflict list + "Save Anyway" button with confirmation dialog
 *
 * Conflict badge (ConflictBadge) is used across all views (Calendar, Day, List, Detail).
 * ConflictBanner is used inline in Create/Edit forms.
 */
export function ConflictBanner({
  conflicts,
  userHasApprovePermission,
  onSaveAnyway,
}: ConflictBannerProps) {
  // Detail modal for clicking a conflicting booking ID
  const [selectedConflictBookingId, setSelectedConflictBookingId] = useState<string | null>(null);
  // Confirmation dialog for "Save Anyway"
  const [showSaveAnywayConfirm, setShowSaveAnywayConfirm] = useState(false);

  // If no conflicts, render nothing
  if (conflicts.length === 0) return null;

  // Group conflicts by type
  const locationConflicts = conflicts.filter((c) => c.conflict_type === 'location');
  const resourceConflicts = conflicts.filter((c) => c.conflict_type === 'resource');

  function handleConflictIdClick(bookingId: string) {
    setSelectedConflictBookingId(bookingId);
  }

  function handleSaveAnywayClick() {
    setShowSaveAnywayConfirm(true);
  }

  function handleConfirmSaveAnyway() {
    setShowSaveAnywayConfirm(false);
    onSaveAnyway?.();
  }

  function handleCancelSaveAnyway() {
    setShowSaveAnywayConfirm(false);
  }

  // ── Hard-block (non-approver) ──────────────────────────────────────────────
  if (!userHasApprovePermission) {
    return (
      <>
        <div
          className="conflict-banner conflict-banner--hard-block"
          role="alert"
          data-testid="conflict-banner-hard-block"
          style={{
            backgroundColor: '#FEE2E2',
            border: '1px solid #EF4444',
            borderRadius: '6px',
            padding: '12px 16px',
            marginBottom: '12px',
          }}
        >
          <div className="conflict-banner__header" style={{ marginBottom: '8px' }}>
            <strong style={{ color: '#B91C1C' }}>
              ⚠️ Conflict detected — Cannot save
            </strong>
          </div>

          <p className="conflict-banner__message" style={{ color: '#7F1D1D', marginBottom: '8px' }}>
            This booking conflicts with existing bookings. Resolve conflicts to proceed.
          </p>

          {/* ConflictBadge reused for inline badge display */}
          <ConflictBadge
            conflicts={conflicts.map((c) => ({
              conflicting_booking_id: c.booking_id,
              conflict_type: c.conflict_type,
            }))}
            size="small"
          />

          {/* Conflict list grouped by type */}
          <div className="conflict-banner__list" style={{ marginTop: '8px' }}>
            {locationConflicts.length > 0 && (
              <div className="conflict-banner__group" data-testid="location-conflicts-group">
                <strong style={{ color: '#991B1B' }}>
                  📍 Location conflict with:
                </strong>
                <ul style={{ margin: '4px 0 8px 16px', padding: 0 }}>
                  {locationConflicts.map((c) => (
                    <li key={c.booking_id} style={{ listStyle: 'disc', color: '#7F1D1D' }}>
                      {c.booking_title}{' '}
                      <span style={{ fontSize: '0.85em' }}>
                        (ID:{' '}
                        <button
                          type="button"
                          className="conflict-banner__booking-link"
                          onClick={() => handleConflictIdClick(c.booking_id)}
                          data-testid={`conflict-link-${c.booking_id}`}
                          title={`View details for: ${c.booking_title}`}
                          style={{
                            background: 'none',
                            border: 'none',
                            color: '#B91C1C',
                            textDecoration: 'underline',
                            cursor: 'pointer',
                            fontFamily: 'monospace',
                            padding: 0,
                          }}
                        >
                          {c.booking_id}
                        </button>
                        )
                      </span>
                    </li>
                  ))}
                </ul>
              </div>
            )}

            {resourceConflicts.length > 0 && (
              <div className="conflict-banner__group" data-testid="resource-conflicts-group">
                {resourceConflicts.map((c) => (
                  <div key={c.booking_id}>
                    <strong style={{ color: '#991B1B' }}>
                      🔧 Resource conflict
                      {c.resource_name ? ` (${c.resource_name})` : ''} with:
                    </strong>
                    <ul style={{ margin: '4px 0 8px 16px', padding: 0 }}>
                      <li style={{ listStyle: 'disc', color: '#7F1D1D' }}>
                        {c.booking_title}{' '}
                        <span style={{ fontSize: '0.85em' }}>
                          (ID:{' '}
                          <button
                            type="button"
                            className="conflict-banner__booking-link"
                            onClick={() => handleConflictIdClick(c.booking_id)}
                            data-testid={`conflict-link-${c.booking_id}`}
                            title={`View details for: ${c.booking_title}`}
                            style={{
                              background: 'none',
                              border: 'none',
                              color: '#B91C1C',
                              textDecoration: 'underline',
                              cursor: 'pointer',
                              fontFamily: 'monospace',
                              padding: 0,
                            }}
                          >
                            {c.booking_id}
                          </button>
                          )
                        </span>
                      </li>
                    </ul>
                  </div>
                ))}
              </div>
            )}
          </div>

          <p
            className="conflict-banner__help"
            style={{ color: '#7F1D1D', fontSize: '0.9em', marginTop: '4px' }}
          >
            <em>What now?</em> Change the time, location, or resources to avoid conflicts.
          </p>
        </div>

        {/* Detail modal for clicked conflict booking ID */}
        {selectedConflictBookingId && (
          <BookingDetailModal
            bookingId={selectedConflictBookingId}
            onClose={() => setSelectedConflictBookingId(null)}
          />
        )}
      </>
    );
  }

  // ── Soft-warn (approver) ───────────────────────────────────────────────────
  return (
    <>
      <div
        className="conflict-banner conflict-banner--soft-warn"
        role="alert"
        data-testid="conflict-banner-soft-warn"
        style={{
          backgroundColor: '#FEF3C7',
          border: '1px solid #F59E0B',
          borderRadius: '6px',
          padding: '12px 16px',
          marginBottom: '12px',
        }}
      >
        <div className="conflict-banner__header" style={{ marginBottom: '8px' }}>
          <strong style={{ color: '#92400E' }}>
            ⚠️ Warning — Conflicts detected
          </strong>
        </div>

        <p className="conflict-banner__message" style={{ color: '#78350F', marginBottom: '8px' }}>
          This booking conflicts with existing bookings. As an approver, you can save anyway.
        </p>

        {/* ConflictBadge reused for inline badge display */}
        <ConflictBadge
          conflicts={conflicts.map((c) => ({
            conflicting_booking_id: c.booking_id,
            conflict_type: c.conflict_type,
          }))}
          size="small"
        />

        {/* Conflict list grouped by type */}
        <div className="conflict-banner__list" style={{ marginTop: '8px' }}>
          {locationConflicts.length > 0 && (
            <div className="conflict-banner__group" data-testid="location-conflicts-group">
              <strong style={{ color: '#92400E' }}>
                📍 Location conflict with:
              </strong>
              <ul style={{ margin: '4px 0 8px 16px', padding: 0 }}>
                {locationConflicts.map((c) => (
                  <li key={c.booking_id} style={{ listStyle: 'disc', color: '#78350F' }}>
                    {c.booking_title}{' '}
                    <span style={{ fontSize: '0.85em' }}>
                      (ID:{' '}
                      <button
                        type="button"
                        className="conflict-banner__booking-link"
                        onClick={() => handleConflictIdClick(c.booking_id)}
                        data-testid={`conflict-link-${c.booking_id}`}
                        title={`View details for: ${c.booking_title}`}
                        style={{
                          background: 'none',
                          border: 'none',
                          color: '#B45309',
                          textDecoration: 'underline',
                          cursor: 'pointer',
                          fontFamily: 'monospace',
                          padding: 0,
                        }}
                      >
                        {c.booking_id}
                      </button>
                      )
                    </span>
                  </li>
                ))}
              </ul>
            </div>
          )}

          {resourceConflicts.length > 0 && (
            <div className="conflict-banner__group" data-testid="resource-conflicts-group">
              {resourceConflicts.map((c) => (
                <div key={c.booking_id}>
                  <strong style={{ color: '#92400E' }}>
                    🔧 Resource conflict
                    {c.resource_name ? ` (${c.resource_name})` : ''} with:
                  </strong>
                  <ul style={{ margin: '4px 0 8px 16px', padding: 0 }}>
                    <li style={{ listStyle: 'disc', color: '#78350F' }}>
                      {c.booking_title}{' '}
                      <span style={{ fontSize: '0.85em' }}>
                        (ID:{' '}
                        <button
                          type="button"
                          className="conflict-banner__booking-link"
                          onClick={() => handleConflictIdClick(c.booking_id)}
                          data-testid={`conflict-link-${c.booking_id}`}
                          title={`View details for: ${c.booking_title}`}
                          style={{
                            background: 'none',
                            border: 'none',
                            color: '#B45309',
                            textDecoration: 'underline',
                            cursor: 'pointer',
                            fontFamily: 'monospace',
                            padding: 0,
                          }}
                        >
                          {c.booking_id}
                        </button>
                        )
                      </span>
                    </li>
                  </ul>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* "Save Anyway" button — approvers only */}
        <div className="conflict-banner__actions" style={{ marginTop: '12px' }}>
          <button
            type="button"
            className="conflict-banner__save-anyway"
            onClick={handleSaveAnywayClick}
            data-testid="btn-save-anyway"
            style={{
              backgroundColor: '#F59E0B',
              color: '#78350F',
              border: '1px solid #D97706',
              borderRadius: '4px',
              padding: '6px 14px',
              fontWeight: 600,
              cursor: 'pointer',
            }}
          >
            Save Anyway
          </button>
        </div>
      </div>

      {/* Confirmation dialog for "Save Anyway" */}
      {showSaveAnywayConfirm && (
        <Modal
          isOpen={showSaveAnywayConfirm}
          onClose={handleCancelSaveAnyway}
          title="Create booking despite conflicts?"
          disableBackdropClose
        >
          <div data-testid="save-anyway-confirm-dialog">
            <p>
              Create booking despite conflicts? This will result in a double-booking.
            </p>
            <div style={{ display: 'flex', gap: '8px', marginTop: '16px' }}>
              <Button
                variant="primary"
                onClick={handleConfirmSaveAnyway}
                data-testid="btn-confirm-save-anyway"
              >
                Create anyway
              </Button>
              <Button
                variant="secondary"
                onClick={handleCancelSaveAnyway}
                data-testid="btn-cancel-save-anyway"
              >
                Cancel
              </Button>
            </div>
          </div>
        </Modal>
      )}

      {/* Detail modal for clicked conflict booking ID */}
      {selectedConflictBookingId && (
        <BookingDetailModal
          bookingId={selectedConflictBookingId}
          onClose={() => setSelectedConflictBookingId(null)}
        />
      )}
    </>
  );
}
