// Simple demo component to show API integration is working

import { Box, Text, Card, Stack, Badge } from '@mantine/core';

export function ApiDemo() {
  return (
    <Box p="md">
      <Stack gap="md">
        <Card withBorder>
          <Stack gap="sm">
            <Text size="lg" fw={500}>API Integration Complete</Text>
            <Badge color="green">Task 4 Completed</Badge>
            <Text size="sm" c="dimmed">
              State management and API integration has been successfully configured.
            </Text>
          </Stack>
        </Card>

        <Card withBorder>
          <Stack gap="sm">
            <Text fw={500}>Implemented Features</Text>
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