import { ReactNode } from 'react';

export interface LoadingStateProps {
  loading: boolean;
  error?: Error | null;
  children: ReactNode;
  loadingText?: string;
  errorText?: string;
  onRetry?: () => void;
  size?: 'sm' | 'md' | 'lg';
  overlay?: boolean;
  className?: string;
  testId?: string;
}