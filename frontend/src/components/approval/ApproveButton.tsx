import { useState } from 'react';
import { Button } from '../Button';
import { Modal } from '../Modal';
import { useToast } from '../Toast';
import { bookingsApi } from '../../api/bookings';
import type { Booking, ConflictFlag } from '../../api/types';

export interface ApproveButtonProps {
  booking: Booking;
  /** Called with the booking id after a successful approve — parent removes row (optimistic UI) */
  onApproved: (id: string) => void;
}

/**
 * ApproveButton (UI-APPR-02)
 *
 * Always enabled — approvers can approve even conflicted bookings.
 *
 * Click behaviour:
 *   - No conflicts → simple confirmation dialog
 *   - Has conflicts (conflict_flags.length > 0) → conflict-aware two-step confirmation dialog
 *
 * POST /bookings/:id/approve on confirm.
 * On success: toast + onApproved callback (optimistic removal).
 * On error: toast with specific message (404/409/500).
 */
export function ApproveButton({ booking, onApproved }: ApproveButtonProps) {
  const { showToast } = useToast();
  const [showSimpleConfirm, setShowSimpleConfirm] = useState(false);
  const [showConflictConfirm, setShowConflictConfirm] = useState(false);
  const [isApproving, setIsApproving] = useState(false);

  const hasConflicts = booking.conflict_flags.length > 0;

  function handleClick() {
    if (hasConflicts) {
      setShowConflictConfirm(true);
    } else {
      setShowSimpleConfirm(true);
    }
  }

  async function doApprove() {
    setIsApproving(true);
    try {
      // POST /bookings/:id/approve
      await bookingsApi.approveBooking(booking.id);
      const msg = hasConflicts ? 'Booking approved (with conflicts)' : 'Booking approved';
      showToast(msg, { variant: 'success' });
      // Optimistic UI: remove row immediately
      onApproved(booking.id);
    } catch (err: unknown) {
      const status = getResponseStatus(err);
      if (status === 404) {
        showToast('Booking not found (may have been deleted)', { variant: 'danger' });
      } else if (status === 409) {
        showToast('Booking already processed', { variant: 'warning' });
      } else {
        showToast('Failed to approve booking', { variant: 'danger' });
      }
    } finally {
      setIsApproving(false);
      setShowSimpleConfirm(false);
      setShowConflictConfirm(false);
    }
  }

  return (
    <>
      {/* ── Approve button ── */}
      <Button
        variant="primary"
        onClick={handleClick}
        data-testid={`btn-approve-${booking.id}`}
        aria-label={`Approve booking: ${booking.title}`}
      >
        ✓ Approve
      </Button>

      {/* ── Simple confirmation dialog (no conflicts) ── */}
      <Modal
        isOpen={showSimpleConfirm}
        onClose={() => setShowSimpleConfirm(false)}
        title="Confirm Approval"
        disableBackdropClose
      >
        <div data-testid="approve-confirm-dialog">
          <p>
            Approve booking <strong>'{booking.title}'</strong>? This action cannot be undone.
          </p>
          <div className="approval-dialog__actions">
            <Button
              variant="primary"
              onClick={doApprove}
              isLoading={isApproving}
              data-testid="btn-confirm-approve"
            >
              Confirm
            </Button>
            <Button
              variant="secondary"
              onClick={() => setShowSimpleConfirm(false)}
              data-testid="btn-cancel-approve"
            >
              Cancel
            </Button>
          </div>
        </div>
      </Modal>

      {/* ── Conflict-aware confirmation dialog ── */}
      <Modal
        isOpen={showConflictConfirm}
        onClose={() => setShowConflictConfirm(false)}
        title="⚠️ Warning - Conflicts detected"
        disableBackdropClose
      >
        <div data-testid="approve-conflict-dialog">
          <p>This booking conflicts with existing bookings:</p>
          <ConflictList conflicts={booking.conflict_flags} />
          <p className="approval-dialog__warning">
            Approve anyway? This will create a double-booking.
          </p>
          <div className="approval-dialog__actions">
            <Button
              variant="danger"
              onClick={doApprove}
              isLoading={isApproving}
              data-testid="btn-approve-despite-conflicts"
            >
              Approve Despite Conflicts
            </Button>
            <Button
              variant="secondary"
              onClick={() => setShowConflictConfirm(false)}
              data-testid="btn-cancel-conflict-approve"
            >
              Cancel
            </Button>
          </div>
        </div>
      </Modal>
    </>
  );
}

/* ─────────────────────────────────────────────────────────────────────────────
   Conflict list (inline in conflict confirmation dialog)
   ───────────────────────────────────────────────────────────────────────────── */
function ConflictList({ conflicts }: { conflicts: ConflictFlag[] }) {
  return (
    <ul className="approval-dialog__conflict-list" data-testid="conflict-list">
      {conflicts.map((c) => (
        <li key={c.conflicting_booking_id} data-testid={`conflict-item-${c.conflicting_booking_id}`}>
          Booking ID: {c.conflicting_booking_id} — {c.conflict_type} conflict
        </li>
      ))}
    </ul>
  );
}

/* ─────────────────────────────────────────────────────────────────────────────
   Helpers
   ───────────────────────────────────────────────────────────────────────────── */
function getResponseStatus(err: unknown): number | null {
  if (
    err &&
    typeof err === 'object' &&
    'response' in err &&
    err.response &&
    typeof err.response === 'object' &&
    'status' in err.response &&
    typeof err.response.status === 'number'
  ) {
    return err.response.status;
  }
  return null;
}
