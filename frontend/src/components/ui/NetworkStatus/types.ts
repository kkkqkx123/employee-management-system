export interface NetworkStatusProps {
  onStatusChange?: (isOnline: boolean) => void;
  showIndicator?: boolean;
  className?: string;
  testId?: string;
}