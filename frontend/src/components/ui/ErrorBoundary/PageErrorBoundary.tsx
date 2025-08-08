import React, { ReactNode } from 'react';
import { ErrorBoundary } from './ErrorBoundary';

interface PageErrorBoundaryProps {
  children: ReactNode;
  pageName: string;
  onError?: (error: Error, errorInfo: React.ErrorInfo) => void;
}

export const PageErrorBoundary: React.FC<PageErrorBoundaryProps> = ({
  children,
  pageName,
  onError,
}) => {
  const handleError = (error: Error, errorInfo: React.ErrorInfo) => {
    // Log page-specific error
    console.error(`Page Error in ${pageName}:`, error, errorInfo);
    
    // Call custom error handler if provided
    if (onError) {
      onError(error, errorInfo);
    }
  };

  return (
    <ErrorBoundary
      level="page"
      onError={handleError}
      showErrorDetails={import.meta.env.DEV}
    >
      {children}
    </ErrorBoundary>
  );
};