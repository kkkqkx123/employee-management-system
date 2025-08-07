import React from 'react';
import { LoadingOverlay } from '@mantine/core';
import { useAuth, useAuthActions } from '../../../stores/authStore';

interface AuthGuardProps {
  children: React.ReactNode;
}

/**
 * AuthGuard component that initializes authentication state
 * and shows loading while checking authentication status
 */
export const AuthGuard: React.FC<AuthGuardProps> = ({ children }) => {
  const { isLoading } = useAuth();
  const { initialize } = useAuthActions();
  const [isInitialized, setIsInitialized] = React.useState(false);

  React.useEffect(() => {
    const initializeAuth = async () => {
      try {
        await initialize();
      } catch (error) {
        console.error('Auth initialization failed:', error);
      } finally {
        setIsInitialized(true);
      }
    };

    initializeAuth();
  }, [initialize]);

  // Show loading overlay while initializing or during auth operations
  if (!isInitialized || isLoading) {
    return (
      <LoadingOverlay
        visible={true}
        overlayProps={{ radius: 'sm', blur: 2 }}
        loaderProps={{ color: 'blue', type: 'bars' }}
      />
    );
  }

  return <>{children}</>;
};

export default AuthGuard;