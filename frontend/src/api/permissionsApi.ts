import { apiClient } from './client';

// 17 permission flags from F0 audit (BF-062)
export type PermissionFlag =
  | 'accessCalendar'
  | 'allowRoomBooking'
  | 'allowApproveBooking'
  | 'allowViewBookings'
  | 'allowEditBookings'
  | 'allowDeleteBookings'
  | 'allowViewLocations'
  | 'allowEditLocations'
  | 'allowDeleteLocations'
  | 'allowViewResources'
  | 'allowEditResources'
  | 'allowDeleteResources'
  | 'allowSettings'
  | 'allowUsers'
  | 'allowPermissions'
  | 'allowAPI'
  | 'allowAuditLog';

export interface PermissionMatrix {
  roles: string[];
  permissions: PermissionFlag[];
  matrix: {
    [roleId: string]: {
      [permissionFlag in PermissionFlag]?: boolean;
    };
  };
}

export const permissionsApi = {
  /**
   * GET /permissions — Returns the full role-permission matrix from Phase 3.
   * Shape: { roles: string[], permissions: PermissionFlag[], matrix: { [roleId]: { [flag]: boolean } } }
   */
  getMatrix: () => apiClient.get<PermissionMatrix>('/permissions'),
};
