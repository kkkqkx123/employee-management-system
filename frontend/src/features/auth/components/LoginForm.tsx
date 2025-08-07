import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  Paper,
  TextInput,
  PasswordInput,
  Button,
  Title,
  Text,
  Anchor,
  Stack,
  Checkbox,
  Alert,
  Group,
} from '@mantine/core';
import { IconAlertCircle } from '@tabler/icons-react';
import { useAuth, useAuthActions } from '../../../stores/authStore';
import type { LoginRequest } from '../../../types/auth';

const loginSchema = z.object({
  username: z.string().min(1, 'Username is required'),
  password: z.string().min(1, 'Password is required'),
  rememberMe: z.boolean().optional(),
});

type LoginFormData = z.infer<typeof loginSchema>;

interface LoginFormProps {
  onSuccess?: () => void;
  onRegisterClick?: () => void;
  onForgotPasswordClick?: () => void;
}

export const LoginForm: React.FC<LoginFormProps> = ({
  onSuccess,
  onRegisterClick,
  onForgotPasswordClick,
}) => {
  const { isLoading, error } = useAuth();
  const { login, clearError } = useAuthActions();

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      username: '',
      password: '',
      rememberMe: false,
    },
  });

  const onSubmit = async (data: LoginFormData) => {
    try {
      clearError();
      const loginRequest: LoginRequest = {
        username: data.username,
        password: data.password,
        rememberMe: data.rememberMe || false,
      };
      
      await login(loginRequest);
      reset();
      onSuccess?.();
    } catch (error) {
      // Error is handled by the store
      console.error('Login failed:', error);
    }
  };

  return (
    <Paper withBorder shadow="md" p={30} mt={30} radius="md">
      <Title order={2} ta="center" mb="md">
        Welcome back!
      </Title>
      
      <Text c="dimmed" size="sm" ta="center" mb="xl">
        Sign in to your account to continue
      </Text>

      {error && (
        <Alert
          icon={<IconAlertCircle size="1rem" />}
          title="Login Failed"
          color="red"
          mb="md"
          onClose={clearError}
          withCloseButton
        >
          {error}
        </Alert>
      )}

      <form onSubmit={handleSubmit(onSubmit)}>
        <Stack>
          <TextInput
            label="Username"
            placeholder="Enter your username"
            required
            error={errors.username?.message}
            {...register('username')}
            disabled={isLoading}
          />

          <PasswordInput
            label="Password"
            placeholder="Enter your password"
            required
            error={errors.password?.message}
            {...register('password')}
            disabled={isLoading}
          />

          <Group justify="space-between">
            <Checkbox
              label="Remember me"
              {...register('rememberMe')}
              disabled={isLoading}
            />
            
            {onForgotPasswordClick && (
              <Anchor
                component="button"
                type="button"
                c="dimmed"
                size="xs"
                onClick={onForgotPasswordClick}
                disabled={isLoading}
              >
                Forgot password?
              </Anchor>
            )}
          </Group>

          <Button
            type="submit"
            fullWidth
            mt="xl"
            loading={isLoading}
            disabled={isLoading}
          >
            Sign in
          </Button>

          {onRegisterClick && (
            <Text c="dimmed" size="sm" ta="center" mt="md">
              Don't have an account?{' '}
              <Anchor
                component="button"
                type="button"
                onClick={onRegisterClick}
                disabled={isLoading}
              >
                Create account
              </Anchor>
            </Text>
          )}
        </Stack>
      </form>
    </Paper>
  );
};

export default LoginForm;