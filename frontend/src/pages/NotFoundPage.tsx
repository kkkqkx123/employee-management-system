import { Container, Title, Text, Button, Group } from '@mantine/core';
import { Link } from 'react-router-dom';
import { ROUTES } from '@/constants';

export const NotFoundPage = () => {
  return (
    <Container size="md" ta="center" py={100}>
      <Title order={1} size={120} fw={900} c="dimmed">
        404
      </Title>
      <Title order={2} size="h1" fw={900} mb="md">
        Page not found
      </Title>
      <Text c="dimmed" size="lg" mb="xl">
        The page you are looking for might have been removed, had its name changed, or is temporarily unavailable.
      </Text>
      <Group justify="center">
        <Button component={Link} to={ROUTES.DASHBOARD} size="md">
          Take me back to dashboard
        </Button>
      </Group>
    </Container>
  );
};