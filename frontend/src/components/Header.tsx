import { Link, useLocation } from 'react-router-dom';
import { UserMenu } from './UserMenu';

interface HeaderProps {
  onHamburgerClick?: () => void;
  sidebarOpen?: boolean;
}

function useBreadcrumb(): string {
  const location = useLocation();
  const path = location.pathname;

  const crumbs: Record<string, string> = {
    '/bookings': 'Calendar',
    '/approvals': 'Approvals',
    '/feeds': 'Feeds',
    '/account': 'My Account',
    '/admin/locations': 'Admin / Locations',
    '/admin/resources': 'Admin / Resources',
    '/admin/custom-fields': 'Admin / Custom Fields',
    '/admin/users': 'Admin / Users',
    '/admin/roles': 'Admin / Roles & Permissions',
    '/admin/settings': 'Admin / Settings',
    '/admin/audit-log': 'Admin / Audit Log',
    '/audit-log': 'Audit Log',
  };

  return crumbs[path] ?? 'Booking Hub';
}

export function Header({ onHamburgerClick, sidebarOpen }: HeaderProps) {
  const breadcrumb = useBreadcrumb();

  return (
    <header className="header">
      <div className="header-left">
        {onHamburgerClick && (
          <button
            className="hamburger-btn"
            aria-label={sidebarOpen ? 'Close navigation menu' : 'Open navigation menu'}
            aria-expanded={sidebarOpen ?? false}
            onClick={onHamburgerClick}
          >
            {sidebarOpen ? '✕' : '☰'}
          </button>
        )}
        <Link to="/bookings?view=month" className="header-logo" aria-label="Booking Hub home">
          <span className="header-logo-text">BookingHub</span>
        </Link>
        <span className="header-breadcrumb" aria-label="Current section">
          {breadcrumb}
        </span>
      </div>
      <div className="header-right">
        <UserMenu />
      </div>
    </header>
  );
}
