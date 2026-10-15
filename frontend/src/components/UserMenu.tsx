import { useState, useRef, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthProvider';

export function UserMenu() {
  const { keycloak } = useAuth();
  const [open, setOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  const displayName: string =
    (keycloak.tokenParsed?.['name'] as string) ||
    (keycloak.tokenParsed?.['preferred_username'] as string) ||
    'User';

  // Close dropdown on outside click
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setOpen(false);
      }
    }
    if (open) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [open]);

  function handleLogout() {
    keycloak.logout();
  }

  return (
    <div className="user-menu" ref={menuRef}>
      <button
        className="user-menu-toggle"
        aria-label={`User menu for ${displayName}`}
        aria-expanded={open}
        aria-haspopup="true"
        onClick={() => setOpen((prev) => !prev)}
      >
        <span className="user-menu-avatar" aria-hidden="true">
          {displayName.charAt(0).toUpperCase()}
        </span>
        <span className="user-menu-name">{displayName}</span>
        <span className="user-menu-caret" aria-hidden="true">
          {open ? '▲' : '▼'}
        </span>
      </button>

      {open && (
        <div className="user-menu-dropdown" role="menu">
          <Link
            to="/account"
            className="user-menu-item"
            role="menuitem"
            onClick={() => setOpen(false)}
          >
            My Account
          </Link>
          <button
            className="user-menu-item user-menu-item--logout"
            role="menuitem"
            onClick={handleLogout}
          >
            Logout
          </button>
        </div>
      )}
    </div>
  );
}
