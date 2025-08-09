import { useState, useCallback } from 'react';
import { retryWithBackoff, shouldRetryError, logError } from '../utils/errorHandling';

export interface UseAsyncOperationOptions<T> {
  maxRetries?: number;
  baseDelay?: number;
  onSuccess?: (data: T) => void;
  onError?: (error: Error) => void;
  autoRetry?: boolean;
}

interface UseAsyncOperationReturn<T, P extends unknown[]> {
  data: T | null;
  loading: boolean;
  error: Error | null;
  execute: (...args: P) => Promise<T>;
  retry: () => Promise<T>;
  reset: () => void;
  isRetrying: boolean;
  retryCount: number;
}

export function useAsyncOperation<T = unknown, P extends unknown[] = unknown[]>(
  asyncFunction: (...args: P) => Promise<T>,
  options: UseAsyncOperationOptions<T> = {}
): UseAsyncOperationReturn<T, P> {
  const {
    maxRetries = 3,
    baseDelay = 1000,
    onSuccess,
    onError,
    autoRetry = false,
  } = options;

  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);
  const [isRetrying, setIsRetrying] = useState(false);
  const [retryCount, setRetryCount] = useState(0);
  const [lastArgs, setLastArgs] = useState<P>([] as unknown as P);

  const execute = useCallback(async (...args: P): Promise<T> => {
    setLoading(true);
    setError(null);
    setLastArgs(args);
    setRetryCount(0);

    try {
      const result = await asyncFunction(...args);
      setData(result);
      onSuccess?.(result);
      return result;
    } catch (err) {
      const error = err instanceof Error ? err : new Error(String(err));
      setError(error);
      logError(error, 'useAsyncOperation');
      onError?.(error);

      // Auto-retry if enabled and error is retryable
      if (autoRetry && shouldRetryError(error)) {
        return retry();
      }

      throw error;
    } finally {
      setLoading(false);
    }
  }, [asyncFunction, onSuccess, onError, autoRetry]);

  const retry = useCallback(async (): Promise<T> => {
    if (isRetrying) {
      throw new Error('Retry already in progress');
    }

    setIsRetrying(true);
    setError(null);

    try {
      const retryFn = async () => {
        setRetryCount(prev => prev + 1);
        return await asyncFunction(...lastArgs);
      };

      const result = await retryWithBackoff(retryFn, maxRetries, baseDelay);
      setData(result);
      onSuccess?.(result);
      setRetryCount(0);
      return result;
    } catch (err) {
      const error = err instanceof Error ? err : new Error(String(err));
      setError(error);
      logError(error, 'useAsyncOperation-retry');
      onError?.(error);
      throw error;
    } finally {
      setIsRetrying(false);
    }
  }, [asyncFunction, lastArgs, maxRetries, baseDelay, onSuccess, onError, isRetrying]);

  const reset = useCallback(() => {
    setData(null);
    setLoading(false);
    setError(null);
    setIsRetrying(false);
    setRetryCount(0);
    setLastArgs([] as unknown as P);
  }, []);

  return {
    data,
    loading,
    error,
    execute,
    retry,
    reset,
    isRetrying,
    retryCount,
  };
}