import { useEffect, useRef, useCallback } from 'react';
import { useAuth, useAuthActions } from '../../../stores/authStore';
import AuthService from '../../../services/auth';

interface UseTokenRefreshOptions {
  refreshThreshold?: number; // Minutes before expiry to refresh
  checkInterval?: number; // Check interval in milliseconds
  maxRetries?: number;
}

/**
 * Hook to handle automatic token refresh
 */
export const useTokenRefresh = ({
  refreshThreshold = 5, // 5 minutes before expiry
  checkInterval = 60000, // Check every minute
  maxRetries = 3,
}: UseTokenRefreshOptions = {}) => {
  const { isAuthenticated, token } = useAuth();
  const { refreshToken, logout } = useAuthActions();
  const intervalRef = useRef<NodeJS.Timeout | null>(null);
  const retryCountRef = useRef(0);

  const shouldRefreshToken = useCallback((token: string): boolean => {
    try {
      const decoded = AuthService.decodeToken(token);
      if (!decoded) return false;

      const currentTime = Date.now() / 1000;
      const timeUntilExpiry = decoded.exp - currentTime;
      const thresholdInSeconds = refreshThreshold * 60;

      return timeUntilExpiry <= thresholdInSeconds && timeUntilExpiry > 0;
    } catch (error) {
      console.error('Error checking token expiry:', error);
      return false;
    }
  }, [refreshThreshold]);

  const handleTokenRefresh = useCallback(async () => {
    if (!token || !isAuthenticated) return;

    try {
      if (shouldRefreshToken(token)) {
        console.log('Refreshing token...');
        await refreshToken();
        retryCountRef.current = 0; // Reset retry count on success
      }
    } catch (error) {
      console.error('Token refresh failed:', error);
      retryCountRef.current += 1;

      if (retryCountRef.current >= maxRetries) {
        console.error('Max token refresh retries exceeded, logging out');
        await logout();
      }
    }
  }, [token, isAuthenticated, shouldRefreshToken, refreshToken, logout, maxRetries]);

  useEffect(() => {
    if (!isAuthenticated || !token) {
      // Clear interval if not authenticated
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
      return;
    }

    // Initial check
    handleTokenRefresh();

    // Set up periodic checks
    intervalRef.current = setInterval(handleTokenRefresh, checkInterval);

    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
    };
  }, [isAuthenticated, token, handleTokenRefresh, checkInterval]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, []);

  return {
    isRefreshing: false, // Could be enhanced to track refresh state
    retryCount: retryCountRef.current,
  };
};

export default useTokenRefresh;