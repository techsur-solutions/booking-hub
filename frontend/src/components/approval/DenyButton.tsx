import { useState } from 'react';
import { Button } from '../Button';
import { Modal } from '../Modal';
import { useToast } from '../Toast';
import { bookingsApi } from '../../api/bookings';
import type { Booking } from '../../api/types';

export interface DenyButtonProps {
  booking: Booking;
  /** Called with the booking id after a successful deny — parent removes row (optimistic UI) */
  onDenied: (id: string) => void;
}

/**
 * DenyButton (UI-APPR-02)
 *
 * Always enabled.
 *
 * Click behaviour:
 *   Shows a denial dialog with:
 *   - Header: "Deny booking '{title}'?"
 *   - Body: optional reason textarea (max 500 chars)
 *   - Footer: "This action cannot be undone."
 *   - Two buttons: "Cancel" / "Deny Booking"
 *
 * POST /bookings/:id/deny with optional reason body on confirm.
 * On success: toast + onDenied callback (optimistic removal).
 * On error: toast with specific message (404/409/500).
 */
export function DenyButton({ booking, onDenied }: DenyButtonProps) {
  const { showToast } = useToast();
  const [showDialog, setShowDialog] = useState(false);
  const [reason, setReason] = useState('');
  const [isDenying, setIsDenying] = useState(false);

  function handleClick() {
    setReason(''); // reset reason on each open
    setShowDialog(true);
  }

  async function handleDeny() {
    setIsDenying(true);
    try {
      // POST /bookings/:id/deny with optional reason in request body
      await bookingsApi.denyBooking(booking.id, reason || undefined);
      showToast('Booking denied', { variant: 'success' });
      // Optimistic UI: remove row immediately
      onDenied(booking.id);
    } catch (err: unknown) {
      const status = getResponseStatus(err);
      if (status === 404) {
        showToast('Booking not found (may have been deleted)', { variant: 'danger' });
      } else if (status === 409) {
        showToast('Booking already processed', { variant: 'warning' });
      } else {
        showToast('Failed to deny booking', { variant: 'danger' });
      }
    } finally {
      setIsDenying(false);
      setShowDialog(false);
    }
  }

  return (
    <>
      {/* ── Deny button ── */}
      <Button
        variant="danger"
        onClick={handleClick}
        data-testid={`btn-deny-${booking.id}`}
        aria-label={`Deny booking: ${booking.title}`}
      >
        ✕ Deny
      </Button>

      {/* ── Denial dialog ── */}
      <Modal
        isOpen={showDialog}
        onClose={() => setShowDialog(false)}
        title={`Deny booking '${booking.title}'?`}
        disableBackdropClose
      >
        <div data-testid="deny-dialog">
          <div className="deny-dialog__reason-field">
            {/* reason textarea — max 500 chars */}
            <label htmlFor={`deny-reason-${booking.id}`} className="deny-dialog__label">
              Reason for denial (optional)
            </label>
            <textarea
              id={`deny-reason-${booking.id}`}
              className="deny-dialog__textarea"
              placeholder="Reason for denial (optional)"
              maxLength={500}
              rows={4}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              data-testid="deny-reason-textarea"
            />
          </div>

          <p className="deny-dialog__warning">This action cannot be undone.</p>

          <div className="approval-dialog__actions">
            <Button
              variant="danger"
              onClick={handleDeny}
              isLoading={isDenying}
              data-testid="btn-confirm-deny"
            >
              Deny Booking
            </Button>
            <Button
              variant="secondary"
              onClick={() => setShowDialog(false)}
              data-testid="btn-cancel-deny"
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
