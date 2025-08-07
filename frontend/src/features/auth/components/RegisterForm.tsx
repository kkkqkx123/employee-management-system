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
  Alert,
  Group,
  Checkbox,
} from '@mantine/core';
import { IconAlertCircle, IconCheck } from '@tabler/icons-react';
import { useAuth, useAuthActions } from '../../../stores/authStore';
import type { RegisterRequest } from '../../../types/auth';

const registerSchema = z.object({
  username: z
    .string()
    .min(3, 'Username must be at least 3 characters')
    .max(50, 'Username must be less than 50 characters')
    .regex(/^[a-zA-Z0-9._-]+$/, 'Username can only contain letters, numbers, dots, underscores, and hyphens'),
  email: z
    .string()
    .min(1, 'Email is required')
    .email('Please enter a valid email address'),
  firstName: z
    .string()
    .min(1, 'First name is required')
    .max(50, 'First name must be less than 50 characters'),
  lastName: z
    .string()
    .min(1, 'Last name is required')
    .max(50, 'Last name must be less than 50 characters'),
  password: z
    .string()
    .min(8, 'Password must be at least 8 characters')
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/, 'Password must contain at least one uppercase letter, one lowercase letter, and one number'),
  confirmPassword: z.string().min(1, 'Please confirm your password'),
  acceptTerms: z.boolean().refine(val => val === true, 'You must accept the terms and conditions'),
}).refine(data => data.password === data.confirmPassword, {
  message: 'Passwords do not match',
  path: ['confirmPassword'],
});

type RegisterFormData = z.infer<typeof registerSchema>;

interface RegisterFormProps {
  onSuccess?: () => void;
  onLoginClick?: () => void;
}

export const RegisterForm: React.FC<RegisterFormProps> = ({
  onSuccess,
  onLoginClick,
}) => {
  const { isLoading, error } = useAuth();
  const { register: registerUser, clearError } = useAuthActions();
  const [registrationSuccess, setRegistrationSuccess] = React.useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    watch,
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      username: '',
      email: '',
      firstName: '',
      lastName: '',
      password: '',
      confirmPassword: '',
      acceptTerms: false,
    },
  });

  const password = watch('password');

  const onSubmit = async (data: RegisterFormData) => {
    try {
      clearError();
      const registerRequest: RegisterRequest = {
        username: data.username,
        email: data.email,
        firstName: data.firstName,
        lastName: data.lastName,
        password: data.password,
        confirmPassword: data.confirmPassword,
      };
      
      await registerUser(registerRequest);
      setRegistrationSuccess(true);
      reset();
      
      // Auto-redirect to login after 3 seconds
      setTimeout(() => {
        onSuccess?.();
      }, 3000);
    } catch (error) {
      // Error is handled by the store
      console.error('Registration failed:', error);
    }
  };

  if (registrationSuccess) {
    return (
      <Paper withBorder shadow="md" p={30} mt={30} radius="md">
        <Alert
          icon={<IconCheck size="1rem" />}
          title="Registration Successful!"
          color="green"
          mb="md"
        >
          Your account has been created successfully. You will be redirected to the login page shortly.
        </Alert>
        
        <Text ta="center" mt="md">
          <Anchor
            component="button"
            type="button"
            onClick={onLoginClick}
          >
            Go to login now
          </Anchor>
        </Text>
      </Paper>
    );
  }

  return (
    <Paper withBorder shadow="md" p={30} mt={30} radius="md">
      <Title order={2} ta="center" mb="md">
        Create Account
      </Title>
      
      <Text c="dimmed" size="sm" ta="center" mb="xl">
        Join us today and get started
      </Text>

      {error && (
        <Alert
          icon={<IconAlertCircle size="1rem" />}
          title="Registration Failed"
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
          <Group grow>
            <TextInput
              label="First Name"
              placeholder="Enter your first name"
              required
              error={errors.firstName?.message}
              {...register('firstName')}
              disabled={isLoading}
            />

            <TextInput
              label="Last Name"
              placeholder="Enter your last name"
              required
              error={errors.lastName?.message}
              {...register('lastName')}
              disabled={isLoading}
            />
          </Group>

          <TextInput
            label="Username"
            placeholder="Choose a username"
            required
            error={errors.username?.message}
            {...register('username')}
            disabled={isLoading}
          />

          <TextInput
            label="Email"
            placeholder="Enter your email address"
            type="email"
            required
            error={errors.email?.message}
            {...register('email')}
            disabled={isLoading}
          />

          <PasswordInput
            label="Password"
            placeholder="Create a strong password"
            required
            error={errors.password?.message}
            {...register('password')}
            disabled={isLoading}
            description="Must contain at least 8 characters with uppercase, lowercase, and number"
          />

          <PasswordInput
            label="Confirm Password"
            placeholder="Confirm your password"
            required
            error={errors.confirmPassword?.message}
            {...register('confirmPassword')}
            disabled={isLoading}
          />

          <Checkbox
            label={
              <Text size="sm">
                I accept the{' '}
                <Anchor href="#" size="sm">
                  Terms and Conditions
                </Anchor>{' '}
                and{' '}
                <Anchor href="#" size="sm">
                  Privacy Policy
                </Anchor>
              </Text>
            }
            required
            error={errors.acceptTerms?.message}
            {...register('acceptTerms')}
            disabled={isLoading}
          />

          <Button
            type="submit"
            fullWidth
            mt="xl"
            loading={isLoading}
            disabled={isLoading}
          >
            Create Account
          </Button>

          {onLoginClick && (
            <Text c="dimmed" size="sm" ta="center" mt="md">
              Already have an account?{' '}
              <Anchor
                component="button"
                type="button"
                onClick={onLoginClick}
                disabled={isLoading}
              >
                Sign in
              </Anchor>
            </Text>
          )}
        </Stack>
      </form>
    </Paper>
  );
};

export default RegisterForm;