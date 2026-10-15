import type { ReactNode } from 'react';
import { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Header } from './Header';
import { Sidebar } from './Sidebar';
import '../styles/layout.css';

interface AppShellProps {
  children: ReactNode;
}

export function AppShell({ children }: AppShellProps) {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const location = useLocation();

  // Close sidebar when route changes (tablet: after navigation)
  // (we use a derived value so it closes on route change)
  const shellClass = `app-shell${sidebarOpen ? ' app-shell--sidebar-open' : ''}`;

  function toggleSidebar() {
    setSidebarOpen((prev) => !prev);
  }

  function closeSidebar() {
    setSidebarOpen(false);
  }

  // Derive active path for bottom nav
  const currentPath = location.pathname;

  function isBottomActive(path: string): boolean {
    return currentPath === path || currentPath.startsWith(path + '/');
  }

  return (
    <div className={shellClass}>
      {/* ── Header ──────────────────────────────────── */}
      <div style={{ gridArea: 'header' }}>
        <Header
          onHamburgerClick={toggleSidebar}
          sidebarOpen={sidebarOpen}
        />
      </div>

      {/* ── Sidebar ─────────────────────────────────── */}
      <Sidebar />

      {/* Backdrop for tablet sidebar */}
      {sidebarOpen && (
        <div
          className="sidebar-backdrop"
          onClick={closeSidebar}
          aria-hidden="true"
          style={{
            position: 'fixed',
            inset: 0,
            top: 56 /* header height */,
            background: 'rgba(0,0,0,0.3)',
            zIndex: 40,
          }}
        />
      )}

      {/* ── Main Content ────────────────────────────── */}
      <main id="main-content" className="app-main" onClick={sidebarOpen ? closeSidebar : undefined}>
        {children}
      </main>

      {/* ── Bottom Nav (mobile) ─────────────────────── */}
      <nav className="bottom-nav" aria-label="Mobile navigation">
        <ul className="bottom-nav-list">
          <li className="bottom-nav-item">
            <Link
              to="/bookings?view=month"
              className={`bottom-nav-link${isBottomActive('/bookings') ? ' bottom-nav-link--active' : ''}`}
            >
              <span className="bottom-nav-icon" aria-hidden="true">📅</span>
              Calendar
            </Link>
          </li>
          <li className="bottom-nav-item">
            <Link
              to="/feeds"
              className={`bottom-nav-link${isBottomActive('/feeds') ? ' bottom-nav-link--active' : ''}`}
            >
              <span className="bottom-nav-icon" aria-hidden="true">📡</span>
              Feeds
            </Link>
          </li>
          <li className="bottom-nav-item">
            <Link
              to="/account"
              className={`bottom-nav-link${isBottomActive('/account') ? ' bottom-nav-link--active' : ''}`}
            >
              <span className="bottom-nav-icon" aria-hidden="true">👤</span>
              Account
            </Link>
          </li>
        </ul>
      </nav>
    </div>
  );
}
