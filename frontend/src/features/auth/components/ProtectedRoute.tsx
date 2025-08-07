import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { LoadingOverlay, Center, Text, Button, Stack } from '@mantine/core';
import { IconLock } from '@tabler/icons-react';
import { useAuth, usePermissions } from '../../../stores/authStore';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredPermissions?: string[];
  requireAllPermissions?: boolean;
  fallbackPath?: string;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  requiredPermissions = [],
  requireAllPermissions = false,
  fallbackPath = '/login',
}) => {
  const location = useLocation();
  const { isAuthenticated, isLoading } = useAuth();
  const { hasPermission, hasAnyPermission, hasAllPermissions } = usePermissions();

  // Show loading while checking authentication
  if (isLoading) {
    return (
      <LoadingOverlay
        visible={true}
        overlayProps={{ radius: 'sm', blur: 2 }}
        loaderProps={{ color: 'blue', type: 'bars' }}
      />
    );
  }

  // Redirect to login if not authenticated
  if (!isAuthenticated) {
    return (
      <Navigate
        to={fallbackPath}
        state={{ from: location }}
        replace
      />
    );
  }

  // Check permissions if required
  if (requiredPermissions.length > 0) {
    const hasRequiredPermissions = requireAllPermissions
      ? hasAllPermissions(requiredPermissions)
      : hasAnyPermission(requiredPermissions);

    if (!hasRequiredPermissions) {
      return <AccessDenied requiredPermissions={requiredPermissions} />;
    }
  }

  return <>{children}</>;
};

interface AccessDeniedProps {
  requiredPermissions: string[];
}

const AccessDenied: React.FC<AccessDeniedProps> = ({ requiredPermissions }) => {
  const handleGoBack = () => {
    window.history.back();
  };

  return (
    <Center style={{ minHeight: '60vh' }}>
      <Stack align="center" gap="md">
        <IconLock size={64} color="var(--mantine-color-red-6)" />
        
        <Text size="xl" fw={600} ta="center">
          Access Denied
        </Text>
        
        <Text c="dimmed" ta="center" maw={400}>
          You don't have the required permissions to access this page.
          {requiredPermissions.length > 0 && (
            <>
              <br />
              Required permissions: {requiredPermissions.join(', ')}
            </>
          )}
        </Text>
        
        <Button onClick={handleGoBack} variant="light">
          Go Back
        </Button>
      </Stack>
    </Center>
  );
};

export default ProtectedRoute;