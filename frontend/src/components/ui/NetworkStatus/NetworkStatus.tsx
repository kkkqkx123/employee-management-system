import React, { useState, useEffect } from 'react';
import { clsx } from 'clsx';
import styles from './NetworkStatus.module.css';

interface NetworkStatusProps {
  onStatusChange?: (isOnline: boolean) => void;
  showIndicator?: boolean;
  className?: string;
  testId?: string;
}

export const NetworkStatus: React.FC<NetworkStatusProps> = ({
  onStatusChange,
  showIndicator = true,
  className,
  testId,
}) => {
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [wasOffline, setWasOffline] = useState(false);
  const [showReconnected, setShowReconnected] = useState(false);

  useEffect(() => {
    const handleOnline = () => {
      setIsOnline(true);
      if (wasOffline) {
        setShowReconnected(true);
        setTimeout(() => setShowReconnected(false), 3000);
        setWasOffline(false);
      }
      onStatusChange?.(true);
    };

    const handleOffline = () => {
      setIsOnline(false);
      setWasOffline(true);
      setShowReconnected(false);
      onStatusChange?.(false);
    };

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, [wasOffline, onStatusChange]);

  if (!showIndicator) {
    return null;
  }

  return (
    <div
      className={clsx(
        styles.networkStatus,
        {
          [styles.online]: isOnline,
          [styles.offline]: !isOnline,
          [styles.reconnected]: showReconnected,
        },
        className
      )}
      data-testid={testId}
      role="status"
      aria-live="polite"
    >
      {!isOnline && (
        <div className={styles.offlineMessage}>
          <span className={styles.statusIcon}>📡</span>
          <span className={styles.statusText}>
            You're offline. Some features may not work.
          </span>
        </div>
      )}

      {showReconnected && (
        <div className={styles.reconnectedMessage}>
          <span className={styles.statusIcon}>✅</span>
          <span className={styles.statusText}>
            Connection restored!
          </span>
        </div>
      )}
    </div>
  );
};

// Hook for using network status in components
export const useNetworkStatus = () => {
  const [isOnline, setIsOnline] = useState(navigator.onLine);

  useEffect(() => {
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  return isOnline;
};