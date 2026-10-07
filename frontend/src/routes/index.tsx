import { Navigate, RouteObject } from 'react-router-dom';
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

export const routes: RouteObject[] = [
  {
    path: '/',
    element: <Navigate to="/calendar" replace />,
  },
  {
    path: '/calendar',
    element: <AppShell><CalendarView /></AppShell>,
  },
  {
    path: '/list',
    element: <AppShell><ListView /></AppShell>,
  },
  {
    path: '/day',
    element: <AppShell><DayView /></AppShell>,
  },
  {
    path: '/admin/locations',
    element: <AppShell><LocationsAdmin /></AppShell>,
  },
  {
    path: '/admin/resources',
    element: <AppShell><ResourcesAdmin /></AppShell>,
  },
  {
    path: '/admin/custom-fields',
    element: <AppShell><CustomFieldsAdmin /></AppShell>,
  },
  {
    path: '/admin/users',
    element: <AppShell><UsersAdmin /></AppShell>,
  },
  {
    path: '/admin/roles',
    element: <AppShell><RolesAdmin /></AppShell>,
  },
  {
    path: '/admin/settings',
    element: <AppShell><SettingsAdmin /></AppShell>,
  },
  {
    path: '/audit-log',
    element: <AppShell><AuditLogViewer /></AppShell>,
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
