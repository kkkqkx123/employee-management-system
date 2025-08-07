import React from 'react';
import { Container, Stack, Title, Text, Group, Badge, Card, Button } from '@mantine/core';
import { useAuth, usePermissions } from '../../../stores/authStore';
import { AuthPage, LogoutButton } from './index';

/**
 * Demo component to showcase authentication functionality
 */
export const AuthDemo: React.FC = () => {
  const { user, isAuthenticated, isLoading } = useAuth();
  const { permissions } = usePermissions();

  if (isLoading) {
    return (
      <Container size="md" py="xl">
        <Text ta="center">Loading authentication...</Text>
      </Container>
    );
  }

  if (!isAuthenticated) {
    return (
      <Container size="md" py="xl">
        <Title order={1} ta="center" mb="xl">
          Authentication Demo
        </Title>
        <AuthPage />
      </Container>
    );
  }

  return (
    <Container size="md" py="xl">
      <Stack gap="xl">
        <Group justify="space-between" align="center">
          <Title order={1}>Welcome to the Dashboard!</Title>
          <LogoutButton variant="menu" />
        </Group>

        <Card withBorder shadow="sm" padding="lg" radius="md">
          <Title order={2} mb="md">User Information</Title>
          
          <Stack gap="sm">
            <Group>
              <Text fw={500}>Name:</Text>
              <Text>{user?.firstName} {user?.lastName}</Text>
            </Group>
            
            <Group>
              <Text fw={500}>Username:</Text>
              <Text>{user?.username}</Text>
            </Group>
            
            <Group>
              <Text fw={500}>Email:</Text>
              <Text>{user?.email}</Text>
            </Group>
            
            <Group>
              <Text fw={500}>Status:</Text>
              <Badge color={user?.enabled ? 'green' : 'red'}>
                {user?.enabled ? 'Active' : 'Inactive'}
              </Badge>
            </Group>
          </Stack>
        </Card>

        <Card withBorder shadow="sm" padding="lg" radius="md">
          <Title order={2} mb="md">Roles & Permissions</Title>
          
          <Stack gap="md">
            <div>
              <Text fw={500} mb="xs">Roles:</Text>
              <Group gap="xs">
                {user?.roles?.map((role) => (
                  <Badge key={role.id} variant="light">
                    {role.name}
                  </Badge>
                ))}
              </Group>
            </div>
            
            <div>
              <Text fw={500} mb="xs">Permissions ({permissions.length}):</Text>
              <Group gap="xs">
                {permissions.slice(0, 10).map((permission) => (
                  <Badge key={permission} variant="outline" size="sm">
                    {permission}
                  </Badge>
                ))}
                {permissions.length > 10 && (
                  <Badge variant="outline" size="sm" color="gray">
                    +{permissions.length - 10} more
                  </Badge>
                )}
              </Group>
            </div>
          </Stack>
        </Card>

        <Card withBorder shadow="sm" padding="lg" radius="md">
          <Title order={2} mb="md">Authentication Actions</Title>
          
          <Group>
            <Button variant="light" onClick={() => window.location.reload()}>
              Refresh Page
            </Button>
            
            <LogoutButton />
          </Group>
        </Card>
      </Stack>
    </Container>
  );
};

export default AuthDemo;