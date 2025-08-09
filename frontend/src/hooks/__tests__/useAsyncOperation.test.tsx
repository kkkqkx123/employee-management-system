import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { useAsyncOperation } from '../useAsyncOperation';
import type { UseAsyncOperationOptions } from '../useAsyncOperation';

describe('useAsyncOperation', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const TestComponent: React.FC<{
    asyncFn: (...args: unknown[]) => Promise<unknown>;
    options?: UseAsyncOperationOptions<unknown>;
  }> = ({ asyncFn, options }) => {
    const { data, loading, error, execute, retry, reset, isRetrying, retryCount } =
      useAsyncOperation(asyncFn, options);

    return (
      <div>
        <div data-testid="data">{data ? JSON.stringify(data) : 'null'}</div>
        <div data-testid="loading">{loading.toString()}</div>
        <div data-testid="error">{error?.message || 'null'}</div>
        <div data-testid="isRetrying">{isRetrying.toString()}</div>
        <div data-testid="retryCount">{retryCount}</div>
        <button onClick={() => execute('test-arg')}>Execute</button>
        <button onClick={retry}>Retry</button>
        <button onClick={reset}>Reset</button>
      </div>
    );
  };

  it('initializes with correct default state', () => {
    const asyncFn = vi.fn().mockResolvedValue('success');

    render(<TestComponent asyncFn={asyncFn} />);

    expect(screen.getByTestId('data')).toHaveTextContent('null');
    expect(screen.getByTestId('loading')).toHaveTextContent('false');
    expect(screen.getByTestId('error')).toHaveTextContent('null');
    expect(screen.getByTestId('isRetrying')).toHaveTextContent('false');
    expect(screen.getByTestId('retryCount')).toHaveTextContent('0');
  });

  it('handles successful execution', async () => {
    const asyncFn = vi.fn().mockResolvedValue({ result: 'success' });

    render(<TestComponent asyncFn={asyncFn} />);

    fireEvent.click(screen.getByText('Execute'));

    // Should show loading state
    expect(screen.getByTestId('loading')).toHaveTextContent('true');

    await waitFor(() => {
      expect(screen.getByTestId('loading')).toHaveTextContent('false');
      expect(screen.getByTestId('data')).toHaveTextContent('{"result":"success"}');
      expect(screen.getByTestId('error')).toHaveTextContent('null');
    });

    expect(asyncFn).toHaveBeenCalledWith('test-arg');
  });

  it('handles execution error', async () => {
    const error = new Error('Test error');
    const asyncFn = vi.fn().mockRejectedValue(error);

    render(<TestComponent asyncFn={asyncFn} />);

    fireEvent.click(screen.getByText('Execute'));

    await waitFor(() => {
      expect(screen.getByTestId('loading')).toHaveTextContent('false');
      expect(screen.getByTestId('error')).toHaveTextContent('Test error');
      expect(screen.getByTestId('data')).toHaveTextContent('null');
    });

    // Catch and ignore the expected error to prevent unhandled error logs
    try {
      await asyncFn();
    } catch {
      // Ignore expected error
    }
  });

  it('calls onSuccess callback on successful execution', async () => {
    const onSuccess = vi.fn();
    const asyncFn = vi.fn().mockResolvedValue('success');

    render(<TestComponent asyncFn={asyncFn} options={{ onSuccess }} />);

    fireEvent.click(screen.getByText('Execute'));

    await waitFor(() => {
      expect(onSuccess).toHaveBeenCalledWith('success');
    });
  });

  it('calls onError callback on failed execution', async () => {
    const onError = vi.fn();
    const error = new Error('Test error');
    const asyncFn = vi.fn().mockRejectedValue(error);

    render(<TestComponent asyncFn={asyncFn} options={{ onError }} />);

    fireEvent.click(screen.getByText('Execute'));

    await waitFor(() => {
      expect(onError).toHaveBeenCalledWith(error);
    });

    // Catch and ignore the expected error to prevent unhandled error logs
    try {
      await asyncFn();
    } catch {
      // Ignore expected error
    }
  });

  it('handles retry functionality', async () => {
    const asyncFn = vi.fn()
      .mockRejectedValueOnce(new Error('First attempt failed'))
      .mockResolvedValueOnce('success');

    render(<TestComponent asyncFn={asyncFn} />);

    // First execution fails
    fireEvent.click(screen.getByText('Execute'));

    await waitFor(() => {
      expect(screen.getByTestId('error')).toHaveTextContent('First attempt failed');
    });

    // Retry succeeds
    fireEvent.click(screen.getByText('Retry'));

    expect(screen.getByTestId('isRetrying')).toHaveTextContent('true');

    await waitFor(() => {
      expect(screen.getByTestId('isRetrying')).toHaveTextContent('false');
      expect(screen.getByTestId('data')).toHaveTextContent('"success"');
      expect(screen.getByTestId('error')).toHaveTextContent('null');
    });

    // Catch and ignore the expected error to prevent unhandled error logs
    try {
      await asyncFn();
    } catch {
      // Ignore expected error
    }
  });

  it('resets state when reset is called', async () => {
    const asyncFn = vi.fn().mockResolvedValue('success');

    render(<TestComponent asyncFn={asyncFn} />);

    // Execute and get data
    fireEvent.click(screen.getByText('Execute'));

    await waitFor(() => {
      expect(screen.getByTestId('data')).toHaveTextContent('"success"');
    });

    // Reset state
    fireEvent.click(screen.getByText('Reset'));

    expect(screen.getByTestId('data')).toHaveTextContent('null');
    expect(screen.getByTestId('loading')).toHaveTextContent('false');
    expect(screen.getByTestId('error')).toHaveTextContent('null');
    expect(screen.getByTestId('retryCount')).toHaveTextContent('0');
  });

  it('prevents multiple simultaneous executions', async () => {
    const asyncFn = vi.fn().mockImplementation(() =>
      new Promise(resolve => setTimeout(() => resolve('success'), 100))
    );

    render(<TestComponent asyncFn={asyncFn} />);

    // Click execute multiple times rapidly
    fireEvent.click(screen.getByText('Execute'));
    fireEvent.click(screen.getByText('Execute'));
    fireEvent.click(screen.getByText('Execute'));

    await waitFor(() => {
      expect(screen.getByTestId('data')).toHaveTextContent('"success"');
    });

    // Should only be called once
    expect(asyncFn).toHaveBeenCalledTimes(1);
  });

  it('prevents retry when already retrying', async () => {
    const asyncFn = vi.fn()
      .mockRejectedValueOnce(new Error('First attempt failed'))
      .mockImplementation(() =>
        new Promise(resolve => setTimeout(() => resolve('success'), 100))
      );

    render(<TestComponent asyncFn={asyncFn} />);

    // First execution fails
    fireEvent.click(screen.getByText('Execute'));

    await waitFor(() => {
      expect(screen.getByTestId('error')).toHaveTextContent('First attempt failed');
    });

    // Start retry
    fireEvent.click(screen.getByText('Retry'));
    expect(screen.getByTestId('isRetrying')).toHaveTextContent('true');

    // Try to retry again while already retrying
    fireEvent.click(screen.getByText('Retry'));

    await waitFor(() => {
      expect(screen.getByTestId('isRetrying')).toHaveTextContent('false');
    });

    // Should only be called twice (initial + one retry)
    expect(asyncFn).toHaveBeenCalledTimes(2);

    // Catch and ignore the expected error to prevent unhandled error logs
    try {
      await asyncFn();
    } catch {
      // Ignore expected error
    }
  });

  it('tracks retry count correctly', async () => {
    // Create network errors that should be retried
    const firstError = new Error('Network Error: First attempt failed');
    const secondError = new Error('Network Error: Second attempt failed');

    const asyncFn = vi.fn()
      .mockRejectedValueOnce(firstError)
      .mockRejectedValueOnce(secondError)
      .mockResolvedValue('success'); // Always resolve to success after second attempt

    render(<TestComponent asyncFn={asyncFn} options={{ maxRetries: 3 }} />);

    // First execution fails
    fireEvent.click(screen.getByText('Execute'));

    await waitFor(() => {
      expect(screen.getByTestId('error')).toHaveTextContent('Network Error: First attempt failed');
    });

    // Retry - should increment count and eventually succeed
    fireEvent.click(screen.getByText('Retry'));

    // Wait a bit longer for the retry to complete
    await waitFor(() => {
      expect(screen.getByTestId('data')).toHaveTextContent('"success"');
      expect(screen.getByTestId('retryCount')).toHaveTextContent('0'); // Reset on success
    }, { timeout: 5000 });

    // Catch and ignore the expected errors to prevent unhandled error logs
    try {
      await asyncFn();
    } catch {
      // Ignore expected error
    }

    try {
      await asyncFn();
    } catch {
      // Ignore expected error
    }
  });
});