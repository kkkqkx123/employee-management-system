import React, { ReactNode } from 'react';
import { ErrorBoundary } from './ErrorBoundary';

interface FeatureErrorBoundaryProps {
  children: ReactNode;
  featureName: string;
  onError?: (error: Error, errorInfo: React.ErrorInfo) => void;
}

export const FeatureErrorBoundary: React.FC<FeatureErrorBoundaryProps> = ({
  children,
  featureName,
  onError,
}) => {
  const handleError = (error: Error, errorInfo: React.ErrorInfo) => {
    // Log feature-specific error
    console.error(`Feature Error in ${featureName}:`, error, errorInfo);
    
    // Call custom error handler if provided
    if (onError) {
      onError(error, errorInfo);
    }
  };

  return (
    <ErrorBoundary
      level="feature"
      onError={handleError}
      showErrorDetails={import.meta.env.DEV}
    >
      {children}
    </ErrorBoundary>
  );
};