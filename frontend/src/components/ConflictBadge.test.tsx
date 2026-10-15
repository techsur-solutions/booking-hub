import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { ConflictBadge } from './ConflictBadge';
import type { ConflictFlag } from '../api/types';

const makeConflict = (id: string, type: 'location' | 'resource' = 'location'): ConflictFlag => ({
  conflicting_booking_id: id,
  conflict_type: type,
});

describe('ConflictBadge', () => {
  it('returns null when conflicts array is empty', () => {
    const { container } = render(<ConflictBadge conflicts={[]} />);
    expect(container.firstChild).toBeNull();
  });

  it('renders when conflicts has entries', () => {
    render(<ConflictBadge conflicts={[makeConflict('42')]} />);
    expect(screen.getByTestId('conflict-badge')).toBeInTheDocument();
  });

  it('renders conflict count', () => {
    render(
      <ConflictBadge
        conflicts={[makeConflict('42'), makeConflict('43', 'resource')]}
      />
    );
    expect(screen.getByText('2')).toBeInTheDocument();
  });

  it('renders count of 1 for single conflict', () => {
    render(<ConflictBadge conflicts={[makeConflict('99')]} />);
    expect(screen.getByText('1')).toBeInTheDocument();
  });

  it('tooltip shows conflicting booking IDs and types', () => {
    render(
      <ConflictBadge
        conflicts={[makeConflict('42', 'location'), makeConflict('99', 'resource')]}
      />
    );
    const badge = screen.getByTestId('conflict-badge');
    expect(badge).toHaveAttribute('title');
    const title = badge.getAttribute('title') ?? '';
    expect(title).toContain('Booking #42');
    expect(title).toContain('location');
    expect(title).toContain('Booking #99');
    expect(title).toContain('resource');
  });

  it('onClick handler is called with first conflicting booking ID when clicked', () => {
    const onClick = vi.fn();
    render(
      <ConflictBadge
        conflicts={[makeConflict('42'), makeConflict('99')]}
        onClick={onClick}
      />
    );
    fireEvent.click(screen.getByTestId('conflict-badge'));
    expect(onClick).toHaveBeenCalledTimes(1);
    expect(onClick).toHaveBeenCalledWith('42');
  });

  it('does not call onClick when not provided', () => {
    // Should not throw when clicked without onClick
    render(<ConflictBadge conflicts={[makeConflict('42')]} />);
    expect(() => fireEvent.click(screen.getByTestId('conflict-badge'))).not.toThrow();
  });

  it('has role=button when onClick provided', () => {
    const onClick = vi.fn();
    render(<ConflictBadge conflicts={[makeConflict('42')]} onClick={onClick} />);
    const badge = screen.getByTestId('conflict-badge');
    expect(badge).toHaveAttribute('role', 'button');
  });

  it('has no role when onClick not provided', () => {
    render(<ConflictBadge conflicts={[makeConflict('42')]} />);
    const badge = screen.getByTestId('conflict-badge');
    expect(badge).not.toHaveAttribute('role');
  });

  it('has tabIndex=0 when onClick provided', () => {
    const onClick = vi.fn();
    render(<ConflictBadge conflicts={[makeConflict('42')]} onClick={onClick} />);
    const badge = screen.getByTestId('conflict-badge');
    expect(badge).toHaveAttribute('tabIndex', '0');
  });

  it('has no tabIndex when onClick not provided', () => {
    render(<ConflictBadge conflicts={[makeConflict('42')]} />);
    const badge = screen.getByTestId('conflict-badge');
    expect(badge).not.toHaveAttribute('tabIndex');
  });

  it('handles Enter key to trigger onClick', () => {
    const onClick = vi.fn();
    render(<ConflictBadge conflicts={[makeConflict('42')]} onClick={onClick} />);
    fireEvent.keyDown(screen.getByTestId('conflict-badge'), { key: 'Enter' });
    expect(onClick).toHaveBeenCalledWith('42');
  });

  it('does not trigger onClick on non-Enter key', () => {
    const onClick = vi.fn();
    render(<ConflictBadge conflicts={[makeConflict('42')]} onClick={onClick} />);
    fireEvent.keyDown(screen.getByTestId('conflict-badge'), { key: 'Space' });
    expect(onClick).not.toHaveBeenCalled();
  });

  it('has accessible aria-label', () => {
    render(<ConflictBadge conflicts={[makeConflict('42', 'location')]} />);
    const badge = screen.getByTestId('conflict-badge');
    expect(badge).toHaveAttribute('aria-label');
    const label = badge.getAttribute('aria-label') ?? '';
    expect(label).toContain('Conflict warning');
    expect(label).toContain('42');
  });

  it('renders warning icon', () => {
    render(<ConflictBadge conflicts={[makeConflict('42')]} />);
    const icon = document.querySelector('.conflict-badge__icon');
    expect(icon).toBeInTheDocument();
    expect(icon?.textContent).toContain('⚠');
  });

  it('applies size CSS class', () => {
    render(<ConflictBadge conflicts={[makeConflict('42')]} size="small" />);
    const badge = screen.getByTestId('conflict-badge');
    expect(badge).toHaveClass('conflict-badge--small');
  });

  it('applies medium size CSS class by default', () => {
    render(<ConflictBadge conflicts={[makeConflict('42')]} />);
    const badge = screen.getByTestId('conflict-badge');
    expect(badge).toHaveClass('conflict-badge--medium');
  });
});
