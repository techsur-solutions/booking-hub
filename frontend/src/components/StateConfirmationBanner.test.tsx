import { render, screen, act, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { StateConfirmationBanner } from './StateConfirmationBanner';

describe('StateConfirmationBanner', () => {
  const mockTimestamp = new Date('2026-10-13T10:00:00.000Z');

  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('renders action text', () => {
    const onDismiss = vi.fn();
    render(
      <StateConfirmationBanner
        action="Booking saved"
        timestamp={mockTimestamp}
        onDismiss={onDismiss}
      />
    );
    expect(screen.getByText('Booking saved')).toBeInTheDocument();
  });

  it('renders entity when provided', () => {
    const onDismiss = vi.fn();
    render(
      <StateConfirmationBanner
        action="Booking saved"
        entity="Booking #482"
        timestamp={mockTimestamp}
        onDismiss={onDismiss}
      />
    );
    expect(screen.getByText(/Booking #482/)).toBeInTheDocument();
  });

  it('does not render entity separator when entity is not provided', () => {
    const onDismiss = vi.fn();
    render(
      <StateConfirmationBanner
        action="Settings updated"
        timestamp={mockTimestamp}
        onDismiss={onDismiss}
      />
    );
    expect(screen.queryByText(/—/)).not.toBeInTheDocument();
  });

  it('renders timestamp in time element', () => {
    const onDismiss = vi.fn();
    render(
      <StateConfirmationBanner
        action="Approved"
        timestamp={mockTimestamp}
        onDismiss={onDismiss}
      />
    );
    const timeEl = document.querySelector('time');
    expect(timeEl).toBeInTheDocument();
    expect(timeEl?.getAttribute('dateTime')).toBe(mockTimestamp.toISOString());
  });

  it('auto-dismisses after default 5000ms', () => {
    const onDismiss = vi.fn();
    render(
      <StateConfirmationBanner
        action="Booking saved"
        timestamp={mockTimestamp}
        onDismiss={onDismiss}
      />
    );

    expect(onDismiss).not.toHaveBeenCalled();

    act(() => {
      vi.advanceTimersByTime(5000);
    });

    expect(onDismiss).toHaveBeenCalledTimes(1);
  });

  it('auto-dismisses after custom autoDismiss duration', () => {
    const onDismiss = vi.fn();
    render(
      <StateConfirmationBanner
        action="Booking saved"
        timestamp={mockTimestamp}
        autoDismiss={3000}
        onDismiss={onDismiss}
      />
    );

    act(() => {
      vi.advanceTimersByTime(2999);
    });
    expect(onDismiss).not.toHaveBeenCalled();

    act(() => {
      vi.advanceTimersByTime(1);
    });
    expect(onDismiss).toHaveBeenCalledTimes(1);
  });

  it('does not auto-dismiss when autoDismiss is 0', () => {
    const onDismiss = vi.fn();
    render(
      <StateConfirmationBanner
        action="Booking saved"
        timestamp={mockTimestamp}
        autoDismiss={0}
        onDismiss={onDismiss}
      />
    );

    act(() => {
      vi.advanceTimersByTime(10000);
    });

    expect(onDismiss).not.toHaveBeenCalled();
  });

  it('calls onDismiss when dismiss button is clicked', () => {
    const onDismiss = vi.fn();
    render(
      <StateConfirmationBanner
        action="Booking saved"
        timestamp={mockTimestamp}
        onDismiss={onDismiss}
      />
    );

    const dismissButton = screen.getByRole('button', { name: /dismiss/i });
    fireEvent.click(dismissButton);

    expect(onDismiss).toHaveBeenCalledTimes(1);
  });

  it('has correct ARIA attributes (role=alert, aria-live=polite)', () => {
    const onDismiss = vi.fn();
    render(
      <StateConfirmationBanner
        action="Booking saved"
        timestamp={mockTimestamp}
        onDismiss={onDismiss}
      />
    );

    const banner = screen.getByRole('alert');
    expect(banner).toHaveAttribute('aria-live', 'polite');
  });

  it('renders success variant icon by default', () => {
    const onDismiss = vi.fn();
    render(
      <StateConfirmationBanner
        action="Booking saved"
        timestamp={mockTimestamp}
        onDismiss={onDismiss}
      />
    );

    expect(screen.getByText('✓')).toBeInTheDocument();
  });

  it('renders error variant icon when variant=error', () => {
    const onDismiss = vi.fn();
    render(
      <StateConfirmationBanner
        action="Action failed"
        timestamp={mockTimestamp}
        variant="error"
        onDismiss={onDismiss}
      />
    );

    expect(screen.getByText('✕', { selector: '.confirmation-banner__icon' })).toBeInTheDocument();
  });

  it('renders warning variant icon when variant=warning', () => {
    const onDismiss = vi.fn();
    render(
      <StateConfirmationBanner
        action="Warning"
        timestamp={mockTimestamp}
        variant="warning"
        onDismiss={onDismiss}
      />
    );

    expect(screen.getByText('⚠')).toBeInTheDocument();
  });

  it('applies correct CSS class for variant', () => {
    const onDismiss = vi.fn();
    render(
      <StateConfirmationBanner
        action="Booking saved"
        timestamp={mockTimestamp}
        variant="success"
        onDismiss={onDismiss}
      />
    );

    const banner = screen.getByTestId('state-confirmation-banner');
    expect(banner).toHaveClass('confirmation-banner--success');
  });
});
