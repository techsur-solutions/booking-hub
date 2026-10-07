import { Navigate } from 'react-router-dom';
import type { RouteObject } from 'react-router-dom';
import { CalendarView } from '../pages/CalendarView';
import { ListView } from '../pages/ListView';
import { DayView } from '../pages/DayView';
import { LocationsAdmin } from '../pages/admin/LocationsAdmin';
import { ResourcesAdmin } from '../pages/admin/ResourcesAdmin';
import { CustomFieldsAdmin } from '../pages/admin/CustomFieldsAdmin';
import { UsersAdmin } from '../pages/admin/UsersAdmin';
import { RolesAdmin } from '../pages/admin/RolesAdmin';
import { SettingsAdmin } from '../pages/admin/SettingsAdmin';
import { AuditLogViewer } from '../pages/AuditLogViewer';
import { FeedsLanding } from '../pages/FeedsLanding';
import { DisplayBoard } from '../pages/DisplayBoard';
import { AppShell } from '../components/AppShell';
import { ProtectedRoute } from '../auth/ProtectedRoute';

export const routes: RouteObject[] = [
  {
    path: '/',
    element: <Navigate to="/calendar" replace />,
  },
  {
    path: '/calendar',
    element: <ProtectedRoute><AppShell><CalendarView /></AppShell></ProtectedRoute>,
  },
  {
    path: '/list',
    element: <ProtectedRoute><AppShell><ListView /></AppShell></ProtectedRoute>,
  },
  {
    path: '/day',
    element: <ProtectedRoute><AppShell><DayView /></AppShell></ProtectedRoute>,
  },
  {
    path: '/admin/locations',
    element: <ProtectedRoute><AppShell><LocationsAdmin /></AppShell></ProtectedRoute>,
  },
  {
    path: '/admin/resources',
    element: <ProtectedRoute><AppShell><ResourcesAdmin /></AppShell></ProtectedRoute>,
  },
  {
    path: '/admin/custom-fields',
    element: <ProtectedRoute><AppShell><CustomFieldsAdmin /></AppShell></ProtectedRoute>,
  },
  {
    path: '/admin/users',
    element: <ProtectedRoute><AppShell><UsersAdmin /></AppShell></ProtectedRoute>,
  },
  {
    path: '/admin/roles',
    element: <ProtectedRoute><AppShell><RolesAdmin /></AppShell></ProtectedRoute>,
  },
  {
    path: '/admin/settings',
    element: <ProtectedRoute><AppShell><SettingsAdmin /></AppShell></ProtectedRoute>,
  },
  {
    path: '/audit-log',
    element: <ProtectedRoute><AppShell><AuditLogViewer /></AppShell></ProtectedRoute>,
  },
  {
    path: '/feeds',
    element: <AppShell><FeedsLanding /></AppShell>,
  },
  {
    path: '/display-board',
    element: <DisplayBoard />,
  },
];
