import React, { useState } from 'react';
import {
  Container,
  Title,
  Text,
  Button,
  Grid,
  Card,
  Group,
  Stack,
  Badge,
  ActionIcon,
  Modal,
  TextInput,
  Textarea,
  Select,
} from '@mantine/core';
import {
  IconDeviceMobile,
  IconDeviceTablet,
  IconDeviceDesktop,
  IconTouch,
  IconScreenShare,
  IconSettings,
  IconUser,
  IconMail,
} from '@tabler/icons-react';
import { useDisclosure } from '@mantine/hooks';
import { 
  ResponsiveContainer, 
  TouchGesture, 
  DataTable 
} from '@/components/ui';
import { 
  useResponsive, 
  useResponsiveValue, 
  useTouch, 
  useOrientation 
} from '@/hooks';

/**
 * ResponsiveDemo page showcasing responsive design features
 * Demonstrates breakpoints, touch interactions, and mobile optimization
 */
export const ResponsiveDemo: React.FC = () => {
  const [modalOpened, { open: openModal, close: closeModal }] = useDisclosure();
  const [swipeCount, setSwipeCount] = useState(0);
  
  const responsive = useResponsive();
  const touch = useTouch();
  const orientation = useOrientation();
  
  // Responsive values demonstration
  const columns = useResponsiveValue({
    base: 1,
    sm: 2,
    md: 3,
    lg: 4,
  });
  
  const cardPadding = useResponsiveValue({
    base: 'sm',
    sm: 'md',
    md: 'lg',
  });

  // Sample data for DataTable
  const sampleData = [
    { id: 1, name: 'John Doe', email: 'john@example.com', role: 'Admin' },
    { id: 2, name: 'Jane Smith', email: 'jane@example.com', role: 'User' },
    { id: 3, name: 'Bob Johnson', email: 'bob@example.com', role: 'Manager' },
  ];

  const tableColumns = [
    { key: 'name', title: 'Name', sortable: true },
    { key: 'email', title: 'Email', sortable: true },
    { key: 'role', title: 'Role', sortable: false },
  ];

  const handleSwipe = (direction: string) => {
    setSwipeCount(prev => prev + 1);
    console.log(`Swiped ${direction}! Total swipes: ${swipeCount + 1}`);
  };

  return (
    <Container size="xl" py="xl">
      <Stack gap="xl">
        {/* Header */}
        <ResponsiveContainer variant="section" centered>
          <Stack align="center" gap="md">
            <Title order={1}>Responsive Design Demo</Title>
            <Text size="lg" c="dimmed" ta="center">
              Showcasing mobile-first responsive design and touch interactions
            </Text>
          </Stack>
        </ResponsiveContainer>

        {/* Device Detection */}
        <ResponsiveContainer variant="card">
          <Stack gap="md">
            <Title order={2}>Device Detection</Title>
            <Grid>
              <Grid.Col span={{ base: 12, sm: 6, md: 3 }}>
                <Card withBorder p={cardPadding}>
                  <Stack align="center" gap="xs">
                    <IconDeviceMobile 
                      size={32} 
                      color={responsive.isMobile ? 'var(--mantine-color-green-6)' : 'var(--mantine-color-gray-4)'} 
                    />
                    <Text size="sm" fw={500}>Mobile</Text>
                    <Badge color={responsive.isMobile ? 'green' : 'gray'}>
                      {responsive.isMobile ? 'Active' : 'Inactive'}
                    </Badge>
                  </Stack>
                </Card>
              </Grid.Col>
              
              <Grid.Col span={{ base: 12, sm: 6, md: 3 }}>
                <Card withBorder p={cardPadding}>
                  <Stack align="center" gap="xs">
                    <IconDeviceTablet 
                      size={32} 
                      color={responsive.isTablet ? 'var(--mantine-color-green-6)' : 'var(--mantine-color-gray-4)'} 
                    />
                    <Text size="sm" fw={500}>Tablet</Text>
                    <Badge color={responsive.isTablet ? 'green' : 'gray'}>
                      {responsive.isTablet ? 'Active' : 'Inactive'}
                    </Badge>
                  </Stack>
                </Card>
              </Grid.Col>
              
              <Grid.Col span={{ base: 12, sm: 6, md: 3 }}>
                <Card withBorder p={cardPadding}>
                  <Stack align="center" gap="xs">
                    <IconDeviceDesktop 
                      size={32} 
                      color={responsive.isDesktop ? 'var(--mantine-color-green-6)' : 'var(--mantine-color-gray-4)'} 
                    />
                    <Text size="sm" fw={500}>Desktop</Text>
                    <Badge color={responsive.isDesktop ? 'green' : 'gray'}>
                      {responsive.isDesktop ? 'Active' : 'Inactive'}
                    </Badge>
                  </Stack>
                </Card>
              </Grid.Col>
              
              <Grid.Col span={{ base: 12, sm: 6, md: 3 }}>
                <Card withBorder p={cardPadding}>
                  <Stack align="center" gap="xs">
                    <IconTouch 
                      size={32} 
                      color={touch.isTouchDevice ? 'var(--mantine-color-green-6)' : 'var(--mantine-color-gray-4)'} 
                    />
                    <Text size="sm" fw={500}>Touch</Text>
                    <Badge color={touch.isTouchDevice ? 'green' : 'gray'}>
                      {touch.isTouchDevice ? 'Enabled' : 'Disabled'}
                    </Badge>
                  </Stack>
                </Card>
              </Grid.Col>
            </Grid>
          </Stack>
        </ResponsiveContainer>

        {/* Responsive Grid */}
        <ResponsiveContainer variant="card">
          <Stack gap="md">
            <Title order={2}>Responsive Grid ({columns} columns)</Title>
            <Grid>
              {Array.from({ length: 8 }, (_, i) => (
                <Grid.Col key={i} span={{ base: 12, sm: 6, md: 4, lg: 3 }}>
                  <Card withBorder p="md" h="120px">
                    <Stack align="center" justify="center" h="100%">
                      <Text fw={500}>Card {i + 1}</Text>
                      <Text size="sm" c="dimmed">
                        {responsive.isMobile ? 'Mobile' : responsive.isTablet ? 'Tablet' : 'Desktop'}
                      </Text>
                    </Stack>
                  </Card>
                </Grid.Col>
              ))}
            </Grid>
          </Stack>
        </ResponsiveContainer>

        {/* Touch Gestures */}
        <ResponsiveContainer variant="card">
          <Stack gap="md">
            <Title order={2}>Touch Gestures</Title>
            <Text size="sm" c="dimmed">
              {touch.isTouchDevice 
                ? 'Swipe in any direction on the area below' 
                : 'Touch gestures are only available on touch devices'
              }
            </Text>
            
            <TouchGesture
              onSwipeLeft={() => handleSwipe('left')}
              onSwipeRight={() => handleSwipe('right')}
              onSwipeUp={() => handleSwipe('up')}
              onSwipeDown={() => handleSwipe('down')}
            >
              <Card 
                withBorder 
                p="xl" 
                style={{ 
                  minHeight: '200px',
                  background: touch.isTouchDevice 
                    ? 'var(--mantine-color-blue-0)' 
                    : 'var(--mantine-color-gray-0)',
                  cursor: touch.isTouchDevice ? 'grab' : 'default',
                }}
              >
                <Stack align="center" justify="center" h="100%">
                  <IconScreenShare size={48} color="var(--mantine-color-blue-6)" />
                  <Text size="lg" fw={500}>Swipe Area</Text>
                  <Text size="sm" c="dimmed">Total swipes: {swipeCount}</Text>
                  {orientation.isPortrait && (
                    <Badge color="blue">Portrait Mode</Badge>
                  )}
                  {orientation.isLandscape && (
                    <Badge color="orange">Landscape Mode</Badge>
                  )}
                </Stack>
              </Card>
            </TouchGesture>
          </Stack>
        </ResponsiveContainer>

        {/* Responsive DataTable */}
        <ResponsiveContainer variant="card">
          <Stack gap="md">
            <Title order={2}>Responsive Data Table</Title>
            <DataTable
              data={sampleData}
              columns={tableColumns}
              pagination={{
                page: 1,
                pageSize: 10,
                total: sampleData.length,
                onChange: () => {},
              }}
            />
          </Stack>
        </ResponsiveContainer>

        {/* Touch-Friendly Controls */}
        <ResponsiveContainer variant="card">
          <Stack gap="md">
            <Title order={2}>Touch-Friendly Controls</Title>
            <Text size="sm" c="dimmed">
              All interactive elements have minimum 44px touch targets
            </Text>
            
            <Group gap="md" wrap="wrap">
              <Button size="md" leftSection={<IconUser size={16} />}>
                Primary Action
              </Button>
              <Button variant="outline" size="md" leftSection={<IconMail size={16} />}>
                Secondary Action
              </Button>
              <ActionIcon size="lg" variant="light">
                <IconSettings size={18} />
              </ActionIcon>
              <Button variant="subtle" onClick={openModal}>
                Open Modal
              </Button>
            </Group>

            <Grid>
              <Grid.Col span={{ base: 12, sm: 6 }}>
                <TextInput
                  label="Email"
                  placeholder="Enter your email"
                  size="md"
                />
              </Grid.Col>
              <Grid.Col span={{ base: 12, sm: 6 }}>
                <Select
                  label="Role"
                  placeholder="Select role"
                  data={['Admin', 'User', 'Manager']}
                  size="md"
                />
              </Grid.Col>
            </Grid>
          </Stack>
        </ResponsiveContainer>

        {/* Device Information */}
        <ResponsiveContainer variant="card">
          <Stack gap="md">
            <Title order={2}>Device Information</Title>
            <Grid>
              <Grid.Col span={{ base: 12, md: 6 }}>
                <Stack gap="xs">
                  <Text size="sm" fw={500}>Breakpoints</Text>
                  <Text size="xs" c="dimmed">XS (≤30em): {responsive.isXs ? '✓' : '✗'}</Text>
                  <Text size="xs" c="dimmed">SM (≤48em): {responsive.isSm ? '✓' : '✗'}</Text>
                  <Text size="xs" c="dimmed">MD (≤64em): {responsive.isMd ? '✓' : '✗'}</Text>
                  <Text size="xs" c="dimmed">LG (≤74em): {responsive.isLg ? '✓' : '✗'}</Text>
                  <Text size="xs" c="dimmed">XL (≤90em): {responsive.isXl ? '✓' : '✗'}</Text>
                </Stack>
              </Grid.Col>
              <Grid.Col span={{ base: 12, md: 6 }}>
                <Stack gap="xs">
                  <Text size="sm" fw={500}>Capabilities</Text>
                  <Text size="xs" c="dimmed">Touch Device: {touch.isTouchDevice ? 'Yes' : 'No'}</Text>
                  <Text size="xs" c="dimmed">Hover Support: {touch.hasHover ? 'Yes' : 'No'}</Text>
                  <Text size="xs" c="dimmed">Portrait: {orientation.isPortrait ? 'Yes' : 'No'}</Text>
                  <Text size="xs" c="dimmed">Landscape: {orientation.isLandscape ? 'Yes' : 'No'}</Text>
                </Stack>
              </Grid.Col>
            </Grid>
          </Stack>
        </ResponsiveContainer>
      </Stack>

      {/* Responsive Modal */}
      <Modal
        opened={modalOpened}
        onClose={closeModal}
        title="Responsive Modal"
        size={responsive.isMobile ? 'full' : 'md'}
      >
        <Stack gap="md">
          <Text>
            This modal adapts to different screen sizes. On mobile devices, it takes the full screen
            for better usability.
          </Text>
          
          <TextInput
            label="Name"
            placeholder="Enter your name"
            size="md"
          />
          
          <Textarea
            label="Message"
            placeholder="Enter your message"
            rows={4}
            size="md"
          />
          
          <Group justify="flex-end" gap="sm">
            <Button variant="outline" onClick={closeModal}>
              Cancel
            </Button>
            <Button onClick={closeModal}>
              Submit
            </Button>
          </Group>
        </Stack>
      </Modal>
    </Container>
  );
};