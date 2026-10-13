import { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ApprovalTable } from '../components/approval/ApprovalTable';
import { bookingsApi } from '../api/bookings';
import { hasApprovePermission } from '../utils/permissions';
import { useToast } from '../components/Toast';
import type { Booking } from '../api/types';

const AUTO_REFRESH_INTERVAL_MS = 60_000; // 60 seconds

/**
 * Approval Queue page (UI-APPR-01)
 *
 * Permission-gated to users with allowApproveBooking permission (role_booking_approver).
 * Non-approvers are redirected to /bookings with a toast "Access denied".
 *
 * Fetches pending bookings via GET /bookings?status=pending.
 * Auto-refreshes every 60 seconds.
 */
export function ApprovalQueue() {
  const navigate = useNavigate();
  const { showToast } = useToast();

  // ── Permission gate ───────────────────────────────────────────────────────
  const allowed = hasApprovePermission();

  useEffect(() => {
    if (!allowed) {
      showToast('Access denied', { variant: 'danger' });
      navigate('/bookings?view=month', { replace: true });
    }
  }, [allowed, navigate, showToast]);

  // ── Data ─────────────────────────────────────────────────────────────────
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const fetchPending = useCallback(async () => {
    try {
      const res = await bookingsApi.getBookings({ status: 'pending' });
      setBookings(res.data);
      setError(false);
    } catch {
      setError(true);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!allowed) return;

    // Initial fetch
    void fetchPending();

    // Auto-refresh every 60 seconds
    intervalRef.current = setInterval(() => {
      void fetchPending();
    }, AUTO_REFRESH_INTERVAL_MS);

    return () => {
      if (intervalRef.current !== null) {
        clearInterval(intervalRef.current);
      }
    };
  }, [allowed, fetchPending]);

  // ── Optimistic UI helpers ─────────────────────────────────────────────────
  const handleApproved = useCallback((id: string) => {
    setBookings((prev) => prev.filter((b) => b.id !== id));
  }, []);

  const handleDenied = useCallback((id: string) => {
    setBookings((prev) => prev.filter((b) => b.id !== id));
  }, []);

  // ── Render ────────────────────────────────────────────────────────────────
  if (!allowed) {
    // Redirect handled in useEffect; render nothing while navigating
    return null;
  }

  return (
    <div className="approval-queue" data-testid="approval-queue">
      <header className="approval-queue__header">
        <h1 className="approval-queue__title">Approval Queue</h1>
        <p className="approval-queue__subtitle">
          Pending bookings requiring your approval
        </p>
      </header>

      {loading && (
        <div className="approval-queue__loading" data-testid="approval-queue-loading">
          Loading pending bookings…
        </div>
      )}

      {error && !loading && (
        <div className="approval-queue__error" role="alert" data-testid="approval-queue-error">
          Failed to load pending bookings. Please refresh the page.
        </div>
      )}

      {!loading && !error && (
        <ApprovalTable
          bookings={bookings}
          onApproved={handleApproved}
          onDenied={handleDenied}
        />
      )}
    </div>
  );
}
