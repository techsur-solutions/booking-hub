import { Navigate } from 'react-router-dom';
import type { RouteObject } from 'react-router-dom';
import { Login } from '../pages/Login';
import { PasswordReset } from '../pages/PasswordReset';
import { PasswordResetComplete } from '../pages/PasswordResetComplete';
import { BookingsView } from '../pages/BookingsView';
import { BookingEdit } from '../pages/BookingEdit';
import { ApprovalsQueue } from '../pages/ApprovalsQueue';
import { MyAccount } from '../pages/MyAccount';
import { LocationsAdmin } from '../pages/admin/LocationsAdmin';
import { ResourcesAdmin } from '../pages/admin/ResourcesAdmin';
import { CustomFieldsAdmin } from '../pages/admin/CustomFieldsAdmin';
import { UsersAdmin } from '../pages/admin/UsersAdmin';
import { RolesAdmin } from '../pages/admin/RolesAdmin';
import { SettingsAdmin } from '../pages/admin/SettingsAdmin';
import { AuditLogViewer } from '../pages/AuditLogViewer';
import { FeedSubscription } from '../pages/FeedSubscription';
import { DisplayBoard } from '../pages/DisplayBoard';
import { AppShell } from '../components/AppShell';
import { ProtectedRoute } from '../auth/ProtectedRoute';

export const routes: RouteObject[] = [
  // Root redirect → default booking view
  {
    path: '/',
    element: <Navigate to="/bookings?view=month" replace />,
  },

  // Auth routes (public — no ProtectedRoute wrapper)
  {
    path: '/login',
    element: <Login />,
  },
  {
    path: '/password-reset',
    element: <PasswordReset />,
  },
  {
    path: '/password-reset/complete',
    element: <PasswordResetComplete />,
  },

  // Unified bookings workspace: /bookings?view=month|day|list
  {
    path: '/bookings',
    element: (
      <ProtectedRoute>
        <AppShell>
          <BookingsView />
        </AppShell>
      </ProtectedRoute>
    ),
  },

  // Booking Edit: /bookings/:id/edit
  {
    path: '/bookings/:id/edit',
    element: (
      <ProtectedRoute>
        <AppShell>
          <BookingEdit />
        </AppShell>
      </ProtectedRoute>
    ),
  },

  // Booking Create Form
  {
    path: '/bookings/create',
    element: (
      <ProtectedRoute>
        <AppShell>
          <BookingCreate />
        </AppShell>
      </ProtectedRoute>
    ),
  },

  // Legacy calendar routes redirected to new /bookings pattern
  {
    path: '/calendar',
    element: <Navigate to="/bookings?view=month" replace />,
  },
  {
    path: '/list',
    element: <Navigate to="/bookings?view=list" replace />,
  },
  {
    path: '/day',
    element: <Navigate to="/bookings?view=day" replace />,
  },

  // Approvals (permission-gated in sidebar; route itself accessible)
  {
    path: '/approvals',
    element: (
      <ProtectedRoute>
        <AppShell>
          <ApprovalsQueue />
        </AppShell>
      </ProtectedRoute>
    ),
  },

  // My Account
  {
    path: '/account',
    element: (
      <ProtectedRoute>
        <AppShell>
          <MyAccount />
        </AppShell>
      </ProtectedRoute>
    ),
  },

  // Admin screens
  {
    path: '/admin/locations',
    element: (
      <ProtectedRoute>
        <AppShell>
          <LocationsAdmin />
        </AppShell>
      </ProtectedRoute>
    ),
  },
  {
    path: '/admin/resources',
    element: (
      <ProtectedRoute>
        <AppShell>
          <ResourcesAdmin />
        </AppShell>
      </ProtectedRoute>
    ),
  },
  {
    path: '/admin/custom-fields',
    element: (
      <ProtectedRoute>
        <AppShell>
          <CustomFieldsAdmin />
        </AppShell>
      </ProtectedRoute>
    ),
  },
  {
    path: '/admin/users',
    element: (
      <ProtectedRoute>
        <AppShell>
          <UsersAdmin />
        </AppShell>
      </ProtectedRoute>
    ),
  },
  {
    path: '/admin/roles',
    element: (
      <ProtectedRoute>
        <AppShell>
          <RolesAdmin />
        </AppShell>
      </ProtectedRoute>
    ),
  },
  {
    path: '/admin/settings',
    element: (
      <ProtectedRoute>
        <AppShell>
          <SettingsAdmin />
        </AppShell>
      </ProtectedRoute>
    ),
  },
  {
    path: '/admin/audit-log',
    element: (
      <ProtectedRoute>
        <AppShell>
          <AuditLogViewer />
        </AppShell>
      </ProtectedRoute>
    ),
  },

  // Legacy audit-log route → admin/audit-log
  {
    path: '/audit-log',
    element: <Navigate to="/admin/audit-log" replace />,
  },

  // Public routes (no ProtectedRoute wrapper)
  {
    path: '/feeds',
    element: (
      <AppShell>
        <FeedSubscription />
      </AppShell>
    ),
  },
  {
    path: '/display-board',
    element: <DisplayBoard />,
  },
];
