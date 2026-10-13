import { useState } from 'react';
import { Button } from '../Button';
import { bookingsApi } from '../../api/bookings';
import { useToast } from '../Toast';
import type { Booking } from '../../api/types';

export interface ApproveButtonProps {
  booking: Booking;
  onApproved: (id: string) => void;
}

/**
 * ApproveButton — approves a pending booking via POST /bookings/{id}/approve.
 */
export function ApproveButton({ booking, onApproved }: ApproveButtonProps) {
  const [isApproving, setIsApproving] = useState(false);
  const { showToast } = useToast();

  async function doApprove() {
    setIsApproving(true);
    try {
      await bookingsApi.approveBooking(booking.id);
      showToast('Booking approved', { variant: 'success' });
      onApproved(booking.id);
    } catch {
      showToast('Failed to approve booking', { variant: 'danger' });
    } finally {
      setIsApproving(false);
    }
  }

  return (
    <Button
      variant="primary"
      onClick={doApprove}
      isLoading={isApproving}
      data-testid={`btn-approve-${booking.id}`}
    >
      Approve
    </Button>
  );
}
