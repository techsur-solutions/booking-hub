import { useState } from 'react';
import { Button } from '../Button';
import { bookingsApi } from '../../api/bookings';
import { useToast } from '../Toast';
import type { Booking } from '../../api/types';

export interface DenyButtonProps {
  booking: Booking;
  onDenied: (id: string) => void;
}

/**
 * DenyButton — denies a pending booking via POST /bookings/{id}/deny.
 */
export function DenyButton({ booking, onDenied }: DenyButtonProps) {
  const [isDenying, setIsDenying] = useState(false);
  const { showToast } = useToast();

  async function doDeny() {
    setIsDenying(true);
    try {
      await bookingsApi.denyBooking(booking.id);
      showToast('Booking denied', { variant: 'success' });
      onDenied(booking.id);
    } catch {
      showToast('Failed to deny booking', { variant: 'danger' });
    } finally {
      setIsDenying(false);
    }
  }

  return (
    <Button
      variant="danger"
      onClick={doDeny}
      isLoading={isDenying}
      data-testid={`btn-deny-${booking.id}`}
    >
      Deny
    </Button>
  );
}
