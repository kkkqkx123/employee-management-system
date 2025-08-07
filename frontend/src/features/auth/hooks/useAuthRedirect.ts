import { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../../stores/authStore';

interface UseAuthRedirectOptions {
  redirectTo?: string;
  redirectFrom?: string;
  requireAuth?: boolean;
}

/**
 * Hook to handle authentication-based redirects
 */
export const useAuthRedirect = ({
  redirectTo = '/dashboard',
  redirectFrom = '/login',
  requireAuth = true,
}: UseAuthRedirectOptions = {}) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated, isLoading } = useAuth();

  useEffect(() => {
    // Don't redirect while loading
    if (isLoading) return;

    const from = (location.state as any)?.from?.pathname || redirectTo;

    if (requireAuth && isAuthenticated) {
      // User is authenticated and should be redirected to protected area
      navigate(from, { replace: true });
    } else if (!requireAuth && !isAuthenticated) {
      // User is not authenticated and should be redirected to login
      navigate(redirectFrom, { 
        replace: true,
        state: { from: location }
      });
    }
  }, [isAuthenticated, isLoading, navigate, location, redirectTo, redirectFrom, requireAuth]);

  return {
    isAuthenticated,
    isLoading,
    shouldRedirect: !isLoading && ((requireAuth && isAuthenticated) || (!requireAuth && !isAuthenticated)),
  };
};

export default useAuthRedirect;