import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import { NetworkStatus, useNetworkStatus } from '../NetworkStatus';

// Mock navigator.onLine
const mockNavigatorOnLine = (value: boolean) => {
  Object.defineProperty(navigator, 'onLine', {
    writable: true,
    value,
  });
};

describe('NetworkStatus', () => {
  beforeEach(() => {
    mockNavigatorOnLine(true);
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('does not render when showIndicator is false', () => {
    render(<NetworkStatus showIndicator={false} />);

    expect(screen.queryByRole('status')).not.toBeInTheDocument();
  });

  it('shows offline message when navigator.onLine is false', () => {
    mockNavigatorOnLine(false);

    render(<NetworkStatus />);

    expect(screen.getByText(/you're offline/i)).toBeInTheDocument();
    expect(screen.getByRole('status')).toBeInTheDocument();
  });

  it('calls onStatusChange when network status changes', () => {
    const onStatusChange = vi.fn();

    render(<NetworkStatus onStatusChange={onStatusChange} />);

    // Simulate going offline
    mockNavigatorOnLine(false);
    fireEvent(window, new Event('offline'));

    expect(onStatusChange).toHaveBeenCalledWith(false);

    // Simulate going online
    mockNavigatorOnLine(true);
    fireEvent(window, new Event('online'));

    expect(onStatusChange).toHaveBeenCalledWith(true);
  });

  it('shows reconnected message when coming back online', () => {
    const { rerender } = render(<NetworkStatus />);

    // Start online, go offline
    mockNavigatorOnLine(false);
    fireEvent(window, new Event('offline'));

    rerender(<NetworkStatus />);

    // Come back online
    mockNavigatorOnLine(true);
    fireEvent(window, new Event('online'));

    expect(screen.getByText(/connection restored/i)).toBeInTheDocument();
  });

  it('applies custom className', () => {
    mockNavigatorOnLine(false);

    render(<NetworkStatus className="custom-class" />);

    expect(screen.getByRole('status')).toHaveClass('custom-class');
  });

  it('uses custom testId', () => {
    mockNavigatorOnLine(false);

    render(<NetworkStatus testId="custom-network" />);

    expect(screen.getByTestId('custom-network')).toBeInTheDocument();
  });

  it('has proper accessibility attributes', () => {
    mockNavigatorOnLine(false);

    render(<NetworkStatus />);

    const statusElement = screen.getByRole('status');
    expect(statusElement).toHaveAttribute('aria-live', 'polite');
  });
});

// Test the useNetworkStatus hook
describe('useNetworkStatus', () => {
  const TestComponent: React.FC = () => {
    const isOnline = useNetworkStatus();
    return <div>{isOnline ? 'Online' : 'Offline'}</div>;
  };

  beforeEach(() => {
    mockNavigatorOnLine(true);
  });

  it('returns current online status', () => {
    render(<TestComponent />);

    expect(screen.getByText('Online')).toBeInTheDocument();
  });

  it('updates when network status changes', () => {
    render(<TestComponent />);

    expect(screen.getByText('Online')).toBeInTheDocument();

    // Simulate going offline
    mockNavigatorOnLine(false);
    fireEvent(window, new Event('offline'));

    expect(screen.getByText('Offline')).toBeInTheDocument();

    // Simulate going online
    mockNavigatorOnLine(true);
    fireEvent(window, new Event('online'));

    expect(screen.getByText('Online')).toBeInTheDocument();
  });
});