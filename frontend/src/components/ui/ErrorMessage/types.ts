export interface ErrorMessageProps {
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