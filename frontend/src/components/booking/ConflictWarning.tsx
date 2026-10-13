import { useEffect, useRef, useState } from 'react';
import { bookingsApi } from '../../api/bookings';
import type { ConflictFlag } from '../../api/types';
import { hasApprovePermission } from '../../utils/permissions';

interface ConflictWarningProps {
  locationId: string;
  startTime: string;
  endTime: string;
  resourceIds: string[];
  /** Called whenever the "save blocked" state changes — parent uses this to disable Save */
  onBlockChange: (blocked: boolean) => void;
  /** Optional: exclude the current booking id (for edit mode) */
  excludeBookingId?: string;
}

/**
 * ConflictWarning component — real-time conflict detection with hard-block vs soft-warn.
 *
 * Calls bookings.checkConflicts() on form change (debounced 500ms).
 * - Non-approver with conflicts: red hard-block banner, Save DISABLED
 * - Approver with conflicts: yellow soft-warn banner, Save ENABLED (confirmation on click)
 */
export function ConflictWarning({
  locationId,
  startTime,
  endTime,
  resourceIds,
  onBlockChange,
  excludeBookingId,
}: ConflictWarningProps) {
  const [conflicts, setConflicts] = useState<ConflictFlag[]>([]);
  const [loading, setLoading] = useState(false);
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const allowApproveBooking = hasApprovePermission();

  // ── Detect conflicts on input change (debounced 500ms) ───────────────────────
  useEffect(() => {
    // Only check if all required fields are present
    if (!locationId || !startTime || !endTime) {
      setConflicts([]);
      onBlockChange(false);
      return;
    }

    if (debounceRef.current) clearTimeout(debounceRef.current);

    debounceRef.current = setTimeout(async () => {
      setLoading(true);
      try {
        const response = await bookingsApi.checkConflicts({
          location_id: locationId,
          resource_ids: resourceIds,
          start_time: toIso(startTime),
          end_time: toIso(endTime),
          exclude_booking_id: excludeBookingId,
        });
        const found = response.data;
        setConflicts(found);
        // Hard-block only for non-approvers
        onBlockChange(found.length > 0 && !allowApproveBooking);
      } catch {
        // If check fails, don't block — fail open
        setConflicts([]);
        onBlockChange(false);
      } finally {
        setLoading(false);
      }
    }, 500);

    return () => {
      if (debounceRef.current) clearTimeout(debounceRef.current);
    };
  }, [locationId, startTime, endTime, resourceIds.join(','), excludeBookingId]); // eslint-disable-line react-hooks/exhaustive-deps

  if (loading) {
    return (
      <div className="conflict-warning conflict-warning--checking" data-testid="conflict-checking">
        <span>Checking for conflicts…</span>
      </div>
    );
  }

  if (conflicts.length === 0) return null;

  // Hard-block: non-approver sees red banner, Save DISABLED
  if (!allowApproveBooking) {
    return (
      <div
        className="conflict-warning conflict-warning--hard-block"
        role="alert"
        data-testid="conflict-hard-block"
      >
        <strong>⛔ Conflict detected.</strong> Resolve conflicts to proceed.
        <ul className="conflict-warning__list">
          {conflicts.map((c) => (
            <li key={c.conflicting_booking_id}>
              Conflict with booking{' '}
              <a
                href={`/bookings/${c.conflicting_booking_id}`}
                className="conflict-warning__link"
                data-testid={`conflict-link-${c.conflicting_booking_id}`}
              >
                {c.conflicting_booking_id}
              </a>{' '}
              ({c.conflict_type})
            </li>
          ))}
        </ul>
        {/* DISABLED hard-block — Save button is disabled by parent via onBlockChange */}
      </div>
    );
  }

  // Soft-warn: approver sees yellow banner, Save ENABLED
  return (
    <div
      className="conflict-warning conflict-warning--soft-warn"
      role="alert"
      data-testid="conflict-soft-warn"
    >
      <strong>⚠️ Warning: Conflict detected.</strong> You can save anyway (approvers only).
      <ul className="conflict-warning__list">
        {conflicts.map((c) => (
          <li key={c.conflicting_booking_id}>
            Conflict with booking{' '}
            <a
              href={`/bookings/${c.conflicting_booking_id}`}
              className="conflict-warning__link"
              data-testid={`conflict-link-${c.conflicting_booking_id}`}
            >
              {c.conflicting_booking_id}
            </a>{' '}
            ({c.conflict_type})
          </li>
        ))}
      </ul>
    </div>
  );
}

// ── Utility ───────────────────────────────────────────────────────────────────
function toIso(dtLocal: string): string {
  if (!dtLocal) return '';
  return dtLocal.length === 16 ? `${dtLocal}:00` : dtLocal;
}
