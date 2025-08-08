import { Container, Title, Text, Grid, Card, Group, ThemeIcon } from '@mantine/core';
import { IconUsers, IconBuilding, IconMessage, IconBell } from '@tabler/icons-react';

const DashboardPage = () => {
  return (
    <Container size="xl">
      <Title order={1} mb="lg">Dashboard</Title>
      <Text c="dimmed" mb="xl">
        Welcome to the Employee Management System. Here's an overview of your organization.
      </Text>
    
      <Grid>
        <Grid.Col span={{ base: 12, sm: 6, lg: 3 }}>
          <Card shadow="sm" padding="lg" radius="md" withBorder>
            <Group>
              <ThemeIcon size="xl" radius="md" variant="light" color="blue">
                <IconUsers size={24} />
              </ThemeIcon>
              <div>
                <Text size="xs" tt="uppercase" fw={700} c="dimmed">
                  Total Employees
                </Text>
                <Text fw={700} size="xl">
                  1,234
                </Text>
              </div>
            </Group>
          </Card>
        </Grid.Col>
      
        <Grid.Col span={{ base: 12, sm: 6, lg: 3 }}>
          <Card shadow="sm" padding="lg" radius="md" withBorder>
            <Group>
              <ThemeIcon size="xl" radius="md" variant="light" color="green">
                <IconBuilding size={24} />
              </ThemeIcon>
              <div>
                <Text size="xs" tt="uppercase" fw={700} c="dimmed">
                  Departments
                </Text>
                <Text fw={700} size="xl">
                  42
                </Text>
              </div>
            </Group>
          </Card>
        </Grid.Col>
      
        <Grid.Col span={{ base: 12, sm: 6, lg: 3 }}>
          <Card shadow="sm" padding="lg" radius="md" withBorder>
            <Group>
              <ThemeIcon size="xl" radius="md" variant="light" color="orange">
                <IconMessage size={24} />
              </ThemeIcon>
              <div>
                <Text size="xs" tt="uppercase" fw={700} c="dimmed">
                  Active Chats
                </Text>
                <Text fw={700} size="xl">
                  18
                </Text>
              </div>
            </Group>
          </Card>
        </Grid.Col>
      
        <Grid.Col span={{ base: 12, sm: 6, lg: 3 }}>
          <Card shadow="sm" padding="lg" radius="md" withBorder>
            <Group>
              <ThemeIcon size="xl" radius="md" variant="light" color="red">
                <IconBell size={24} />
              </ThemeIcon>
              <div>
                <Text size="xs" tt="uppercase" fw={700} c="dimmed">
                  Notifications
                </Text>
                <Text fw={700} size="xl">
                  7
                </Text>
              </div>
            </Group>
          </Card>
        </Grid.Col>
      </Grid>
    </Container>
  );
};

export default DashboardPage;