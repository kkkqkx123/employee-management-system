// Example component demonstrating API integration

import { useState } from 'react';
import { 
  Box, 
  Button, 
  Card, 
  Group, 
  Text, 
  Stack, 
  Alert,
  Loader,
  Badge
} from '@mantine/core';
import { IconInfoCircle, IconRefresh, IconPlus } from '@tabler/icons-react';
import { useEmployees, useCreateEmployee } from '../../hooks/useApi';
import { useAuth, useNotifications } from '../../stores';
import type { EmployeeCreateRequest, Employee } from '../../types/entities';
import { EmployeeStatus } from '../../types/entities';

export function ApiExample() {
  const [page, setPage] = useState(0);
  const { user, isAuthenticated } = useAuth();
  const { showNotification } = useNotifications();

  // Fetch employees using our custom hook
  const { 
    data: employeesData, 
    isLoading, 
    isError, 
    error, 
    refetch 
  } = useEmployees({ page, size: 5 });

  // Create employee mutation
  const createEmployeeMutation = useCreateEmployee();

  const handleCreateExample = () => {
    const exampleEmployee: EmployeeCreateRequest = {
      employeeNumber: `EMP${Date.now()}`,
      firstName: 'John',
      lastName: 'Doe',
      email: `john.doe.${Date.now()}@company.com`,
      departmentId: 1,
      positionId: 1,
      hireDate: new Date().toISOString().split('T')[0],
      status: EmployeeStatus.ACTIVE,
    };

    createEmployeeMutation.mutate(exampleEmployee, {
      onSuccess: () => {
        showNotification({
          type: 'success',
          title: 'Success',
          message: 'Example employee created successfully!',
        });
      },
      onError: (error) => {
        showNotification({
          type: 'error',
          title: 'Error',
          message: `Failed to create employee: ${error.message}`,
        });
      },
    });
  };

  if (!isAuthenticated) {
    return (
      <Alert icon={<IconInfoCircle size="1rem" />} title="Authentication Required">
        Please log in to view this example.
      </Alert>
    );
  }

  return (
    <Box p="md">
      <Stack gap="md">
        <Card withBorder>
          <Stack gap="sm">
            <Group justify="space-between">
              <Text size="lg" fw={500}>API Integration Example</Text>
              <Badge color="green">Connected</Badge>
            </Group>
            
            <Text size="sm" c="dimmed">
              This component demonstrates the API integration with TanStack Query,
              Zustand state management, and error handling.
            </Text>

            {user && (
              <Text size="sm">
                Logged in as: <strong>{user.username}</strong> ({user.email})
              </Text>
            )}
          </Stack>
        </Card>

        <Card withBorder>
          <Stack gap="md">
            <Group justify="space-between">
              <Text fw={500}>Employee Data</Text>
              <Group gap="xs">
                <Button
                  size="xs"
                  variant="light"
                  leftSection={<IconRefresh size="1rem" />}
                  onClick={() => refetch()}
                  loading={isLoading}
                >
                  Refresh
                </Button>
                <Button
                  size="xs"
                  leftSection={<IconPlus size="1rem" />}
                  onClick={handleCreateExample}
                  loading={createEmployeeMutation.isPending}
                >
                  Create Example
                </Button>
              </Group>
            </Group>

            {isLoading && (
              <Group justify="center" p="xl">
                <Loader size="sm" />
                <Text size="sm">Loading employees...</Text>
              </Group>
            )}

            {isError && (
              <Alert color="red" title="Error Loading Data">
                {error?.message || 'Failed to load employees'}
              </Alert>
            )}

            {employeesData && (
              <Stack gap="xs">
                <Text size="sm" c="dimmed">
                  Showing {employeesData.numberOfElements} of {employeesData.totalElements} employees
                </Text>
                
                {employeesData.content.map((employee: Employee) => (
                  <Card key={employee.id} withBorder radius="sm" p="sm">
                    <Group justify="space-between">
                      <div>
                        <Text size="sm" fw={500}>
                          {employee.firstName} {employee.lastName}
                        </Text>
                        <Text size="xs" c="dimmed">
                          {employee.email} • {employee.employeeNumber}
                        </Text>
                      </div>
                      <Badge size="sm" variant="light">
                        {employee.status}
                      </Badge>
                    </Group>
                  </Card>
                ))}

                {employeesData.content.length === 0 && (
                  <Text ta="center" c="dimmed" py="xl">
                    No employees found
                  </Text>
                )}
              </Stack>
            )}

            {employeesData && employeesData.totalPages > 1 && (
              <Group justify="center" gap="xs">
                <Button
                  size="xs"
                  variant="light"
                  disabled={page === 0}
                  onClick={() => setPage(p => p - 1)}
                >
                  Previous
                </Button>
                <Text size="sm">
                  Page {page + 1} of {employeesData.totalPages}
                </Text>
                <Button
                  size="xs"
                  variant="light"
                  disabled={page >= employeesData.totalPages - 1}
                  onClick={() => setPage(p => p + 1)}
                >
                  Next
                </Button>
              </Group>
            )}
          </Stack>
        </Card>

        <Card withBorder>
          <Stack gap="sm">
            <Text fw={500}>State Management</Text>
            <Text size="sm" c="dimmed">
              The application uses Zustand for global state management with the following stores:
            </Text>
            <Stack gap="xs">
              <Badge variant="light" color="blue">Auth Store - User authentication and permissions</Badge>
              <Badge variant="light" color="green">UI Store - Theme, navigation, and UI state</Badge>
              <Badge variant="light" color="orange">Notification Store - Real-time notifications</Badge>
            </Stack>
          </Stack>
        </Card>

        <Card withBorder>
          <Stack gap="sm">
            <Text fw={500}>Features Implemented</Text>
            <Stack gap="xs">
              <Text size="sm">✅ TanStack Query for server state management</Text>
              <Text size="sm">✅ Zustand stores for global state</Text>
              <Text size="sm">✅ Axios API client with interceptors</Text>
              <Text size="sm">✅ Error handling and retry mechanisms</Text>
              <Text size="sm">✅ WebSocket service for real-time features</Text>
              <Text size="sm">✅ Authentication service with JWT</Text>
              <Text size="sm">✅ Custom hooks for API operations</Text>
              <Text size="sm">✅ Comprehensive TypeScript types</Text>
            </Stack>
          </Stack>
        </Card>
      </Stack>
    </Box>
  );
}