import React, { useState } from 'react';
import { clsx } from 'clsx';
import { Button } from '../Button';
import { LoadingSpinner } from '../LoadingSpinner';
import { retryWithBackoff } from '../../../utils/errorHandling';
import styles from './RetryButton.module.css';

interface RetryButtonProps {
  onRetry: () => Promise<void> | void;
  maxRetries?: number;
  baseDelay?: number;
  disabled?: boolean;
  variant?: 'primary' | 'secondary' | 'outline';
  size?: 'xs' | 'sm' | 'md' | 'lg';
  children?: React.ReactNode;
  className?: string;
  showAttempts?: boolean;
  testId?: string;
}

export const RetryButton: React.FC<RetryButtonProps> = ({
  onRetry,
  maxRetries = 3,
  baseDelay = 1000,
  disabled = false,
  variant = 'primary',
  size = 'md',
  children = 'Try Again',
  className,
  showAttempts = false,
  testId,
}) => {
  const [isRetrying, setIsRetrying] = useState(false);
  const [attempts, setAttempts] = useState(0);
  const [error, setError] = useState<string | null>(null);

  const handleRetry = async () => {
    if (isRetrying || disabled) return;

    setIsRetrying(true);
    setError(null);

    try {
      const retryFn = async () => {
        setAttempts(prev => prev + 1);
        await onRetry();
      };

      await retryWithBackoff(retryFn, maxRetries, baseDelay);

      // Reset attempts on success
      setAttempts(0);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Retry failed';
      setError(errorMessage);
      console.error('Retry failed:', err);
    } finally {
      setIsRetrying(false);
    }
  };

  return (
    <div className={clsx(styles.retryContainer, className)}>
      <Button
        variant={variant}
        size={size}
        onClick={handleRetry}
        disabled={disabled || isRetrying}
        className={styles.retryButton}

        aria-label={
          isRetrying
            ? `Retrying${showAttempts && attempts > 0 ? ` (${attempts}/${maxRetries})` : ''}...`
            : (typeof children === 'string' ? children : 'Retry button')
        }
        data-testid={testId}
      >
        {isRetrying ? (
          <>
            <LoadingSpinner size="xs" />
            <span className={styles.retryingText}>
              Retrying{showAttempts && attempts > 0 ? ` (${attempts}/${maxRetries})` : ''}...
            </span>
          </>
        ) : (
          children
        )}
      </Button>

      {error && (
        <p className={styles.retryError} role="alert">
          {error}
        </p>
      )}

      {showAttempts && attempts > 0 && !isRetrying && (
        <p className={styles.attemptInfo}>
          Attempted {attempts} time{attempts !== 1 ? 's' : ''}
        </p>
      )}
    </div>
  );
};