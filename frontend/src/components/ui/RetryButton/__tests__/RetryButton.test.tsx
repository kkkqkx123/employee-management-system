import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { RetryButton } from '../RetryButton';

describe('RetryButton', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders with default text', () => {
    const onRetry = vi.fn();

    render(<RetryButton onRetry={onRetry} />);

    expect(screen.getByRole('button', { name: /Try Again/i })).toBeInTheDocument();
    const button = screen.getByRole('button');
    expect(button).toHaveAccessibleName('Try Again');
    expect(button).toHaveTextContent('Try Again');

  });

  it('renders with custom children', () => {
    const onRetry = vi.fn();

    render(<RetryButton onRetry={onRetry}>Retry Operation</RetryButton>);

    expect(screen.getByRole('button', { name: /retry operation/i })).toBeInTheDocument();
  });

  it('calls onRetry when clicked', async () => {
    const onRetry = vi.fn().mockResolvedValue(undefined);

    render(<RetryButton onRetry={onRetry} />);

    fireEvent.click(screen.getByRole('button'));

    await waitFor(() => {
      expect(onRetry).toHaveBeenCalledTimes(1);
    });
  });

  it('shows loading state during retry', async () => {
    const onRetry = vi.fn().mockImplementation(() =>
      new Promise(resolve => setTimeout(resolve, 100))
    );

    render(<RetryButton onRetry={onRetry} />);

    fireEvent.click(screen.getByRole('button'));

    expect(screen.getByText(/retrying/i)).toBeInTheDocument();
    expect(screen.getByRole('status')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.queryByText(/retrying/i)).not.toBeInTheDocument();
    });
  });

  it('disables button when disabled prop is true', () => {
    const onRetry = vi.fn();

    render(<RetryButton onRetry={onRetry} disabled={true} />);

    const button = screen.getByRole('button');
    expect(button).toBeDisabled();

    fireEvent.click(button);
    expect(onRetry).not.toHaveBeenCalled();
  });

  it('disables button during retry', async () => {
    const onRetry = vi.fn().mockImplementation(() =>
      new Promise(resolve => setTimeout(resolve, 100))
    );

    render(<RetryButton onRetry={onRetry} />);

    const button = screen.getByRole('button');
    fireEvent.click(button);

    expect(button).toBeDisabled();

    await waitFor(() => {
      expect(button).not.toBeDisabled();
    });
  });

  it('shows attempt count when showAttempts is true', async () => {
    const onRetry = vi.fn()
      .mockRejectedValueOnce(new Error('First attempt failed'))
      .mockResolvedValueOnce(undefined);

    render(<RetryButton onRetry={onRetry} showAttempts={true} maxRetries={2} />);

    fireEvent.click(screen.getByRole('button'));

    await waitFor(() => {
      expect(screen.getByText(/retrying \(1\/2\)/i)).toBeInTheDocument();
    });

    await waitFor(() => {
      expect(screen.getByText(/attempted 1 time/i)).toBeInTheDocument();
    });
  });

  it('shows error message when retry fails', async () => {
    const error = new Error('Retry failed');
    const onRetry = vi.fn().mockRejectedValue(error);

    render(<RetryButton onRetry={onRetry} maxRetries={1} />);

    fireEvent.click(screen.getByRole('button'));

    await waitFor(() => {
      expect(screen.getByRole('alert')).toBeInTheDocument();
      expect(screen.getByText('Retry failed')).toBeInTheDocument();
    });
  });

  it('resets attempts on successful retry', async () => {
    const onRetry = vi.fn()
      .mockRejectedValueOnce(new Error('First attempt failed'))
      .mockResolvedValueOnce(undefined);

    render(<RetryButton onRetry={onRetry} showAttempts={true} />);

    // First click - should fail and show attempt count
    fireEvent.click(screen.getByRole('button'));

    await waitFor(() => {
      expect(screen.getByText(/attempted 1 time/i)).toBeInTheDocument();
    });

    // Second click - should succeed and reset attempts
    fireEvent.click(screen.getByRole('button'));

    await waitFor(() => {
      expect(screen.queryByText(/attempted/i)).not.toBeInTheDocument();
    });
  });

  it('applies custom className', () => {
    const onRetry = vi.fn();

    render(<RetryButton onRetry={onRetry} className="custom-class" />);

    expect(screen.getByRole('button').closest('.custom-class')).toBeInTheDocument();
  });

  it('uses custom testId', () => {
    const onRetry = vi.fn();

    render(<RetryButton onRetry={onRetry} testId="custom-retry" />);

    expect(screen.getByTestId('custom-retry')).toBeInTheDocument();
  });

  it('handles synchronous onRetry function', async () => {
    const onRetry = vi.fn();

    render(<RetryButton onRetry={onRetry} />);

    fireEvent.click(screen.getByRole('button'));

    await waitFor(() => {
      expect(onRetry).toHaveBeenCalledTimes(1);
    });
  });

  it('prevents multiple simultaneous retries', async () => {
    const onRetry = vi.fn().mockImplementation(() =>
      new Promise(resolve => setTimeout(resolve, 100))
    );

    render(<RetryButton onRetry={onRetry} />);

    const button = screen.getByRole('button');

    // Click multiple times rapidly
    fireEvent.click(button);
    fireEvent.click(button);
    fireEvent.click(button);

    await waitFor(() => {
      expect(onRetry).toHaveBeenCalledTimes(1);
    });
  });
});