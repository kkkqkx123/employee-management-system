import { ReactNode, ErrorInfo } from 'react';

export interface ErrorBoundaryProps {
  children: ReactNode;
  fallback?: ReactNode;
  onError?: (error: Error, errorInfo: ErrorInfo) => void;
  showErrorDetails?: boolean;
  level?: 'page' | 'feature' | 'component';
}

export interface FeatureErrorBoundaryProps {
  children: ReactNode;
  featureName: string;
  onError?: (error: Error, errorInfo: ErrorInfo) => void;
}

export interface PageErrorBoundaryProps {
  children: ReactNode;
  pageName: string;
  onError?: (error: Error, errorInfo: ErrorInfo) => void;
}