import { useState, useEffect } from 'react';
import type { PermissionFlag, PermissionMatrix } from '../../api/permissionsApi';
import { permissionsApi } from '../../api/permissionsApi';

// All 17 permission flags from F0 audit BF-062 in display order
const PERMISSION_FLAGS: PermissionFlag[] = [
  'accessCalendar',
  'allowRoomBooking',
  'allowApproveBooking',
  'allowViewBookings',
  'allowEditBookings',
  'allowDeleteBookings',
  'allowViewLocations',
  'allowEditLocations',
  'allowDeleteLocations',
  'allowViewResources',
  'allowEditResources',
  'allowDeleteResources',
  'allowSettings',
  'allowUsers',
  'allowPermissions',
  'allowAPI',
  'allowAuditLog',
];

// Human-readable labels for each permission flag
const FLAG_LABELS: Record<PermissionFlag, string> = {
  accessCalendar: 'Access Calendar',
  allowRoomBooking: 'Allow Room Booking',
  allowApproveBooking: 'Allow Approve Booking',
  allowViewBookings: 'Allow View Bookings',
  allowEditBookings: 'Allow Edit Bookings',
  allowDeleteBookings: 'Allow Delete Bookings',
  allowViewLocations: 'Allow View Locations',
  allowEditLocations: 'Allow Edit Locations',
  allowDeleteLocations: 'Allow Delete Locations',
  allowViewResources: 'Allow View Resources',
  allowEditResources: 'Allow Edit Resources',
  allowDeleteResources: 'Allow Delete Resources',
  allowSettings: 'Allow Settings',
  allowUsers: 'Allow Users',
  allowPermissions: 'Allow Permissions',
  allowAPI: 'Allow API',
  allowAuditLog: 'Allow Audit Log',
};

interface PermissionGridProps {
  /** Optional: override matrix data (useful for testing / Storybook) */
  matrixOverride?: PermissionMatrix;
}

export function PermissionGrid({ matrixOverride }: PermissionGridProps) {
  const [matrix, setMatrix] = useState<PermissionMatrix | null>(matrixOverride ?? null);
  const [loading, setLoading] = useState(!matrixOverride);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (matrixOverride) return; // Caller supplied data — skip fetch

    setLoading(true);
    permissionsApi.getMatrix()
      .then((res) => {
        setMatrix(res.data);
        setLoading(false);
      })
      .catch((err) => {
        console.error('[PermissionGrid] Failed to fetch permission matrix:', err);
        setError('Failed to load permission matrix. Please refresh the page.');
        setLoading(false);
      });
  }, [matrixOverride]);

  if (loading) {
    return (
      <div className="permission-grid-loading" data-testid="permission-grid-loading">
        Loading permission matrix…
      </div>
    );
  }

  if (error || !matrix) {
    return (
      <div className="permission-grid-error" data-testid="permission-grid-error">
        {error ?? 'No permission data available.'}
      </div>
    );
  }

  // Use flags from API or fall back to the canonical 17-flag list
  const flags: PermissionFlag[] =
    matrix.permissions && matrix.permissions.length > 0
      ? (matrix.permissions as PermissionFlag[])
      : PERMISSION_FLAGS;

  const roles = matrix.roles ?? [];

  return (
    <div className="permission-grid-wrapper" data-testid="permission-grid">
      <div className="permission-grid-scroll">
        <table className="permission-grid-table" data-testid="permission-matrix-table" aria-label="Role permission matrix">
          <thead>
            <tr>
              {/* Top-left corner cell */}
              <th className="permission-grid-corner" scope="col">
                Permission
              </th>
              {roles.map((role) => (
                <th key={role} scope="col" className="permission-grid-role-header">
                  {role}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {flags.map((flag) => (
              <tr key={flag} data-testid={`permission-row-${flag}`}>
                {/* Permission flag label */}
                <th scope="row" className="permission-grid-flag-label">
                  {FLAG_LABELS[flag] ?? flag}
                </th>
                {/* One cell per role */}
                {roles.map((role) => {
                  const granted = matrix.matrix?.[role]?.[flag] === true;
                  return (
                    <td
                      key={`${role}-${flag}`}
                      className={`permission-grid-cell ${granted ? 'permission-granted' : 'permission-denied'}`}
                      data-testid={`cell-${role}-${flag}`}
                      data-granted={granted}
                      aria-label={`${role} — ${FLAG_LABELS[flag] ?? flag}: ${granted ? 'granted' : 'denied (default)'}`}
                    >
                      {granted ? (
                        <span className="permission-checkmark" aria-hidden="true">
                          ✓
                        </span>
                      ) : (
                        /* Deny-by-default: faded dash indicator per UX-Mockup Design Principle #6 */
                        <span className="permission-denied-dash" aria-hidden="true">
                          –
                        </span>
                      )}
                    </td>
                  );
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
