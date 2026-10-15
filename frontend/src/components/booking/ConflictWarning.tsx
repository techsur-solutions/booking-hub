import { useEffect, useRef, useState } from 'react';
import { bookingsApi } from '../../api/bookings';
import type { ConflictFlag } from '../../api/types';
import { hasApprovePermission } from '../../utils/permissions';
import { ConflictBanner } from './ConflictBanner';

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

  // Delegate rendering to ConflictBanner (enhanced UI with hard-block/soft-warn)
  // Map ConflictFlag → ConflictBannerConflict (ConflictFlag has no title/resource_name, use ID as title)
  const bannerConflicts = conflicts.map((c: ConflictFlag) => ({
    booking_id: c.conflicting_booking_id,
    booking_title: c.conflicting_booking_id, // ID used as title fallback (no title in ConflictFlag)
    conflict_type: c.conflict_type,
  }));

  return (
    <ConflictBanner
      conflicts={bannerConflicts}
      userHasApprovePermission={allowApproveBooking}
      onSaveAnyway={allowApproveBooking ? () => {
        // When approver confirms "Save Anyway", unblock the form so parent can proceed
        onBlockChange(false);
      } : undefined}
    />
  );
}

// ── Utility ───────────────────────────────────────────────────────────────────
function toIso(dtLocal: string): string {
  if (!dtLocal) return '';
  return dtLocal.length === 16 ? `${dtLocal}:00` : dtLocal;
}
