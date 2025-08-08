import React from 'react';
import { clsx } from 'clsx';
import { LoadingSpinner } from '../LoadingSpinner';
import styles from './LoadingState.module.css';

interface LoadingStateProps {
  loading: boolean;
  error?: Error | null;
  children: React.ReactNode;
  loadingText?: string;
  errorText?: string;
  onRetry?: () => void;
  size?: 'sm' | 'md' | 'lg';
  overlay?: boolean;
  className?: string;
  testId?: string;
}

export const LoadingState: React.FC<LoadingStateProps> = ({
  loading,
  error,
  children,
  loadingText = 'Loading...',
  errorText,
  onRetry,
  size = 'md',
  overlay = false,
  className,
  testId,
}) => {
  if (loading) {
    return (
      <div 
        className={clsx(
          styles.loadingContainer,
          styles[size],
          { [styles.overlay]: overlay },
          className
        )}
        data-testid={testId ? `${testId}-loading` : 'loading-state'}
      >
        <LoadingSpinner size={size} overlay={overlay} />
        {loadingText && (
          <p className={styles.loadingText} aria-live="polite">
            {loadingText}
          </p>
        )}
      </div>
    );
  }

  if (error) {
    return (
      <div 
        className={clsx(styles.errorContainer, styles[size], className)}
        data-testid={testId ? `${testId}-error` : 'error-state'}
        role="alert"
      >
        <div className={styles.errorIcon}>⚠️</div>
        <p className={styles.errorText}>
          {errorText || error.message || 'An error occurred'}
        </p>
        {onRetry && (
          <button
            className={styles.retryButton}
            onClick={onRetry}
            aria-label="Retry loading"
          >
            Try Again
          </button>
        )}
      </div>
    );
  }

  return <>{children}</>;
};