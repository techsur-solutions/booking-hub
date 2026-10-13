import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';

// Mock keycloak module before importing PermissionGate
vi.mock('../auth/keycloak', () => ({
  keycloak: {
    realmAccess: {
      roles: [] as string[],
    },
  },
}));

// Import PermissionGate after mock is set up
import { PermissionGate } from './PermissionGate';
import { keycloak } from '../auth/keycloak';

// Helper to set mock roles
function setRoles(roles: string[]) {
  (keycloak as { realmAccess: { roles: string[] } }).realmAccess.roles = roles;
}

describe('PermissionGate', () => {
  beforeEach(() => {
    // Reset roles before each test
    setRoles([]);
  });

  it('renders children when user has the required role', () => {
    setRoles(['role_booking_approver']);
    render(
      <PermissionGate requireRole="role_booking_approver">
        <div data-testid="protected-content">Approve Button</div>
      </PermissionGate>
    );
    expect(screen.getByTestId('protected-content')).toBeInTheDocument();
  });

  it('returns null (structurally omitted) when user lacks required role', () => {
    setRoles([]); // no roles
    const { container } = render(
      <PermissionGate requireRole="role_booking_approver">
        <div data-testid="protected-content">Approve Button</div>
      </PermissionGate>
    );
    expect(screen.queryByTestId('protected-content')).not.toBeInTheDocument();
    // The element is completely absent from DOM, not just disabled
    expect(container.querySelector('[data-testid="protected-content"]')).toBeNull();
  });

  it('renders children with requireAnyRole when user has at least one matching role (OR logic)', () => {
    setRoles(['role_location_manager']);
    render(
      <PermissionGate requireAnyRole={['role_location_manager', 'role_permission_manager']}>
        <div data-testid="admin-content">Admin Content</div>
      </PermissionGate>
    );
    expect(screen.getByTestId('admin-content')).toBeInTheDocument();
  });

  it('omits children with requireAnyRole when user has none of the roles', () => {
    setRoles(['role_calendar_viewer']);
    render(
      <PermissionGate requireAnyRole={['role_location_manager', 'role_permission_manager']}>
        <div data-testid="admin-content">Admin Content</div>
      </PermissionGate>
    );
    expect(screen.queryByTestId('admin-content')).not.toBeInTheDocument();
  });

  it('renders fallback when permission denied and fallback is provided', () => {
    setRoles([]);
    render(
      <PermissionGate
        requireRole="role_booking_approver"
        fallback={<div data-testid="fallback-content">Not authorized</div>}
      >
        <div data-testid="protected-content">Approve Button</div>
      </PermissionGate>
    );
    expect(screen.queryByTestId('protected-content')).not.toBeInTheDocument();
    expect(screen.getByTestId('fallback-content')).toBeInTheDocument();
  });

  it('renders children without any permission props (default permit)', () => {
    setRoles([]);
    render(
      <PermissionGate>
        <div data-testid="unrestricted-content">Unrestricted</div>
      </PermissionGate>
    );
    expect(screen.getByTestId('unrestricted-content')).toBeInTheDocument();
  });

  it('returns null (not disabled) when permission denied — structural absence not disabled state', () => {
    setRoles([]);
    const { container } = render(
      <PermissionGate requireRole="role_booking_approver">
        <button data-testid="approve-btn">Approve</button>
      </PermissionGate>
    );
    // Element must not exist at all in the DOM
    expect(container.querySelector('[data-testid="approve-btn"]')).toBeNull();
    // Not present as disabled element
    expect(screen.queryByRole('button', { name: /approve/i })).not.toBeInTheDocument();
  });

  it('requireAnyRole grants access with any one matching role', () => {
    setRoles(['role_audit_viewer']);
    render(
      <PermissionGate requireAnyRole={['role_location_manager', 'role_permission_manager', 'role_audit_viewer']}>
        <div data-testid="multi-role-content">Admin Nav</div>
      </PermissionGate>
    );
    expect(screen.getByTestId('multi-role-content')).toBeInTheDocument();
  });

  it('requireRole takes priority over requireAnyRole when both are provided', () => {
    setRoles(['role_calendar_viewer']); // Has one of the anyRole list but NOT the requireRole
    render(
      <PermissionGate
        requireRole="role_booking_approver"
        requireAnyRole={['role_calendar_viewer']}
      >
        <div data-testid="content">Content</div>
      </PermissionGate>
    );
    // requireRole is checked first (it's truthy), requireAnyRole is skipped
    expect(screen.queryByTestId('content')).not.toBeInTheDocument();
  });

  it('renders null as default fallback (not a visible element)', () => {
    setRoles([]);
    const { container } = render(
      <PermissionGate requireRole="role_booking_approver">
        <span>Hidden</span>
      </PermissionGate>
    );
    // container should be effectively empty
    expect(container.textContent).toBe('');
  });
});
