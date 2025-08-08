import { ReactNode } from 'react';

export interface RetryButtonProps {
  onRetry: () => Promise<void> | void;
  maxRetries?: number;
  baseDelay?: number;
  disabled?: boolean;
  variant?: 'primary' | 'secondary' | 'outline';
  size?: 'xs' | 'sm' | 'md' | 'lg';
  children?: ReactNode;
  className?: string;
  showAttempts?: boolean;
  testId?: string;
}