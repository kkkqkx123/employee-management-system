import { Container, Paper, Title, Text, Center } from '@mantine/core';
import { LoginForm } from '@/features/auth/components';

const LoginPage = () => {
  return (
    <Container size={420} my={40}>
      <Center>
        <Paper withBorder shadow="md" p={30} mt={30} radius="md">
          <Title ta="center" mb="md">
            Welcome back!
          </Title>
          <Text c="dimmed" size="sm" ta="center" mt={5}>
            Sign in to your account to continue
          </Text>
          <LoginForm />
        </Paper>
      </Center>
    </Container>
  );
};

export default LoginPage;