import React from 'react';
import { clsx } from 'clsx';
import { Button } from '../Button';
import { RetryButton } from '../RetryButton';
import { 
  getUserFriendlyErrorMessage, 
  getErrorSeverity, 
  isNetworkError,
  isAuthError,
  isPermissionError,
  shouldRetryError
} from '../../../utils/errorHandling';
import styles from './ErrorMessage.module.css';

interface ErrorMessageProps {
  error: Error | string | null;
  title?: string;
  onRetry?: () => Promise<void> | void;
  onDismiss?: () => void;
  showRetry?: boolean;
  showDismiss?: boolean;
  variant?: 'inline' | 'banner' | 'modal';
  size?: 'sm' | 'md' | 'lg';
  className?: string;
  testId?: string;
}

export const ErrorMessage: React.FC<ErrorMessageProps> = ({
  error,
  title,
  onRetry,
  onDismiss,
  showRetry = true,
  showDismiss = false,
  variant = 'inline',
  size = 'md',
  className,
  testId,
}) => {
  if (!error) {
    return null;
  }

  const errorMessage = typeof error === 'string' ? error : error.message;
  const userFriendlyMessage = getUserFriendlyErrorMessage(error);
  const severity = getErrorSeverity(error);
  const canRetry = shouldRetryError(error) && onRetry;

  const getErrorIcon = () => {
    if (isNetworkError(error)) return '📡';
    if (isAuthError(error)) return '🔒';
    if (isPermissionError(error)) return '🚫';
    
    switch (severity) {
      case 'critical': return '🚨';
      case 'high': return '⚠️';
      case 'medium': return '❌';
      case 'low': return 'ℹ️';
      default: return '❌';
    }
  };

  const getActionableMessage = () => {
    if (isNetworkError(error)) {
      return 'Please check your internet connection and try again.';
    }
    
    if (isAuthError(error)) {
      return 'Please log in again to continue.';
    }
    
    if (isPermissionError(error)) {
      return 'Contact your administrator if you believe you should have access.';
    }
    
    return 'If the problem persists, please contact support.';
  };

  return (
    <div
      className={clsx(
        styles.errorMessage,
        styles[variant],
        styles[size],
        styles[severity],
        className
      )}
      role="alert"
      aria-live="assertive"
      data-testid={testId}
    >
      <div className={styles.errorContent}>
        <div className={styles.errorHeader}>
          <span className={styles.errorIcon} aria-hidden="true">
            {getErrorIcon()}
          </span>
          <div className={styles.errorText}>
            {title && (
              <h4 className={styles.errorTitle}>{title}</h4>
            )}
            <p className={styles.errorDescription}>
              {userFriendlyMessage}
            </p>
            <p className={styles.errorAction}>
              {getActionableMessage()}
            </p>
          </div>
        </div>

        {(canRetry || showDismiss) && (
          <div className={styles.errorActions}>
            {canRetry && showRetry && (
              <RetryButton
                onRetry={onRetry!}
                variant="outline"
                size="sm"
                testId={testId ? `${testId}-retry` : undefined}
              />
            )}
            
            {showDismiss && onDismiss && (
              <Button
                variant="ghost"
                size="sm"
                onClick={onDismiss}
                aria-label="Dismiss error"
                data-testid={testId ? `${testId}-dismiss` : undefined}
              >
                Dismiss
              </Button>
            )}
          </div>
        )}
      </div>

      {import.meta.env.DEV && (
        <details className={styles.errorDetails}>
          <summary className={styles.errorDetailsSummary}>
            Technical Details
          </summary>
          <pre className={styles.errorDetailsContent}>
            {errorMessage}
          </pre>
        </details>
      )}
    </div>
  );
};