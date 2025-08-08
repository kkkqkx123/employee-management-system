// Error handling utilities

import type { ApiError } from '../types/api';

/**
 * Check if error is an API error
 */
export function isApiError(error: unknown): error is ApiError {
  return (
    typeof error === 'object' &&
    error !== null &&
    'success' in error &&
    'message' in error &&
    (error as ApiError).success === false
  );
}

/**
 * Extract error message from various error types
 */
export function getErrorMessage(error: unknown): string {
  if (isApiError(error)) {
    return error.message;
  }

  if (error instanceof Error) {
    return error.message;
  }

  if (typeof error === 'string') {
    return error;
  }

  return 'An unexpected error occurred';
}

/**
 * Extract field errors from API error
 */
export function getFieldErrors(error: unknown): Record<string, string> {
  if (isApiError(error) && error.errors) {
    return error.errors.reduce((acc, fieldError) => {
      acc[fieldError.field] = fieldError.message;
      return acc;
    }, {} as Record<string, string>);
  }

  return {};
}

/**
 * Check if error is a network error
 */
export function isNetworkError(error: unknown): boolean {
  if (error instanceof Error) {
    return (
      error.message.includes('Network Error') ||
      error.message.includes('fetch') ||
      error.message.includes('ERR_NETWORK')
    );
  }
  return false;
}

/**
 * Check if error is a timeout error
 */
export function isTimeoutError(error: unknown): boolean {
  if (error instanceof Error) {
    return (
      error.message.includes('timeout') ||
      error.message.includes('TIMEOUT')
    );
  }
  return false;
}

/**
 * Check if error is an authentication error
 */
export function isAuthError(error: unknown): boolean {
  if (isApiError(error)) {
    return error.message.includes('401') || error.message.toLowerCase().includes('unauthorized');
  }
  return false;
}

/**
 * Check if error is a permission error
 */
export function isPermissionError(error: unknown): boolean {
  if (isApiError(error)) {
    return error.message.includes('403') || error.message.toLowerCase().includes('forbidden');
  }
  return false;
}

/**
 * Check if error is a validation error
 */
export function isValidationError(error: unknown): boolean {
  if (isApiError(error)) {
    return error.message.includes('400') || (Array.isArray(error.errors) && error.errors.length > 0);
  }
  return false;
}

/**
 * Check if error is a not found error
 */
export function isNotFoundError(error: unknown): boolean {
  if (isApiError(error)) {
    return error.message.includes('404') || error.message.toLowerCase().includes('not found');
  }
  return false;
}

/**
 * Check if error is a server error
 */
export function isServerError(error: unknown): boolean {
  if (isApiError(error)) {
    return error.message.includes('500') || error.message.toLowerCase().includes('internal server error');
  }
  return false;
}

/**
 * Get user-friendly error message based on error type
 */
export function getUserFriendlyErrorMessage(error: unknown): string {
  if (isNetworkError(error)) {
    return 'Unable to connect to the server. Please check your internet connection and try again.';
  }

  if (isTimeoutError(error)) {
    return 'The request timed out. Please try again.';
  }

  if (isAuthError(error)) {
    return 'Your session has expired. Please log in again.';
  }

  if (isPermissionError(error)) {
    return 'You do not have permission to perform this action.';
  }

  if (isValidationError(error)) {
    return 'Please check your input and try again.';
  }

  if (isNotFoundError(error)) {
    return 'The requested resource was not found.';
  }

  if (isServerError(error)) {
    return 'A server error occurred. Please try again later.';
  }

  return getErrorMessage(error);
}

/**
 * Get error severity level
 */
export function getErrorSeverity(error: unknown): 'low' | 'medium' | 'high' | 'critical' {
  if (isNetworkError(error) || isTimeoutError(error)) {
    return 'medium';
  }

  if (isAuthError(error) || isPermissionError(error)) {
    return 'high';
  }

  if (isServerError(error)) {
    return 'critical';
  }

  if (isValidationError(error) || isNotFoundError(error)) {
    return 'low';
  }

  return 'medium';
}

/**
 * Determine if error should be retried
 */
export function shouldRetryError(error: unknown): boolean {
  // Don't retry client errors (4xx)
  if (isAuthError(error) || isPermissionError(error) || isValidationError(error) || isNotFoundError(error)) {
    return false;
  }

  // Retry network and server errors
  if (isNetworkError(error) || isTimeoutError(error) || isServerError(error)) {
    return true;
  }

  return false;
}

/**
 * Log error with appropriate level
 */
export function logError(error: unknown, context?: string): void {
  const severity = getErrorSeverity(error);
  const message = getErrorMessage(error);
  const contextStr = context ? `[${context}] ` : '';

  switch (severity) {
    case 'critical':
      console.error(`${contextStr}CRITICAL ERROR:`, message, error);
      break;
    case 'high':
      console.error(`${contextStr}HIGH ERROR:`, message, error);
      break;
    case 'medium':
      console.warn(`${contextStr}MEDIUM ERROR:`, message, error);
      break;
    case 'low':
      console.info(`${contextStr}LOW ERROR:`, message, error);
      break;
  }
}

/**
 * Create error report for debugging
 */
export function createErrorReport(error: unknown, context?: Record<string, any>): {
  message: string;
  severity: string;
  type: string;
  context?: Record<string, any>;
  timestamp: string;
  userAgent: string;
  url: string;
} {
  return {
    message: getErrorMessage(error),
    severity: getErrorSeverity(error),
    type: getErrorType(error),
    context,
    timestamp: new Date().toISOString(),
    userAgent: navigator.userAgent,
    url: window.location.href,
  };
}

/**
 * Get error type string
 */
function getErrorType(error: unknown): string {
  if (isNetworkError(error)) return 'network';
  if (isTimeoutError(error)) return 'timeout';
  if (isAuthError(error)) return 'authentication';
  if (isPermissionError(error)) return 'permission';
  if (isValidationError(error)) return 'validation';
  if (isNotFoundError(error)) return 'not_found';
  if (isServerError(error)) return 'server';
  return 'unknown';
}

/**
 * Retry function with exponential backoff
 */
export async function retryWithBackoff<T>(
  fn: () => Promise<T>,
  maxRetries = 3,
  baseDelay = 1000
): Promise<T> {
  let lastError: unknown;

  for (let attempt = 0; attempt <= maxRetries; attempt++) {
    try {
      return await fn();
    } catch (error) {
      lastError = error;

      // Don't retry if it's not a retryable error
      if (!shouldRetryError(error)) {
        throw error;
      }

      // Don't retry on the last attempt
      if (attempt === maxRetries) {
        break;
      }

      // Calculate delay with exponential backoff
      const delay = baseDelay * Math.pow(2, attempt);
      await new Promise(resolve => setTimeout(resolve, delay));
    }
  }

  throw lastError;
}