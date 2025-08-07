# Authentication Module Implementation Guide

## Overview
This document provides detailed implementation specifications for the authentication module, including login, registration, JWT token management, and protected routes.

## File Structure
```
src/features/auth/
├── components/
│   ├── LoginForm.tsx
│   ├── RegisterForm.tsx
│   ├── ProtectedRoute.tsx
│   └── AuthGuard.tsx
├── hooks/
│   ├── useAuth.ts
│   ├── useLogin.ts
│   └── useRegister.ts
├── services/
│   └── authService.ts
├── stores/
│   └── authStore.ts
├── types/
│   └── auth.types.ts
├── utils/
│   └── tokenUtils.ts
└── index.ts
```

## Type Definitions

### auth.types.ts
```typescript
export interface User {
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  roles: string[];
  permissions: string[];
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface LoginRequest {
  username: string;
  password: string;
  rememberMe: boolean;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
  firstName: string;
  lastName: string;
  acceptTerms: boolean;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  user: User;
  permissions: string[];
  loginTime: string;
}

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}
```

## State Management

### authStore.ts
```typescript
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { AuthState, User } from '../types/auth.types';
import { tokenUtils } from '../utils/tokenUtils';

interface AuthStore extends AuthState {
  // Actions
  setUser: (user: User) => void;
  setToken: (token: string) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  login: (token: string, user: User) => void;
  logout: () => void;
  clearError: () => void;
  hasPermission: (permission: string) => boolean;
  hasRole: (role: string) => boolean;
}

export const useAuthStore = create<AuthStore>()(
  persist(
    (set, get) => ({
      // Initial state
      user: null,
      token: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,

      // Actions
      setUser: (user) => set({ user }),
      setToken: (token) => set({ token }),
      setLoading: (loading) => set({ isLoading: loading }),
      setError: (error) => set({ error }),
      clearError: () => set({ error: null }),

      login: (token, user) => set({
        token,
        user,
        isAuthenticated: true,
        error: null
      }),

      logout: () => {
        tokenUtils.removeToken();
        set({
          user: null,
          token: null,
          isAuthenticated: false,
          error: null
        });
      },

      hasPermission: (permission) => {
        const { user } = get();
        return user?.permissions?.includes(permission) || false;
      },

      hasRole: (role) => {
        const { user } = get();
        return user?.roles?.includes(role) || false;
      }
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        token: state.token,
        user: state.user,
        isAuthenticated: state.isAuthenticated
      })
    }
  )
);
```

## Services

### authService.ts
```typescript
import { apiClient } from '../../services/api';
import { LoginRequest, RegisterRequest, AuthResponse } from '../types/auth.types';

export const authService = {
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await apiClient.post<AuthResponse>('/auth/login', credentials);
    return response.data;
  },

  async register(userData: RegisterRequest): Promise<AuthResponse> {
    const response = await apiClient.post<AuthResponse>('/auth/register', userData);
    return response.data;
  },

  async logout(): Promise<void> {
    await apiClient.post('/auth/logout');
  },

  async refreshToken(): Promise<AuthResponse> {
    const response = await apiClient.post<AuthResponse>('/auth/refresh-token');
    return response.data;
  },

  async validateToken(): Promise<boolean> {
    try {
      await apiClient.get('/auth/validate-token');
      return true;
    } catch {
      return false;
    }
  },

  async getCurrentUser(): Promise<User> {
    const response = await apiClient.get<User>('/auth/me');
    return response.data;
  }
};
```

## Utility Functions

### tokenUtils.ts
```typescript
const TOKEN_KEY = 'auth_token';
const REFRESH_TOKEN_KEY = 'refresh_token';

export const tokenUtils = {
  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  },

  setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  },

  removeToken(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
  },

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  },

  setRefreshToken(token: string): void {
    localStorage.setItem(REFRESH_TOKEN_KEY, token);
  },

  isTokenExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000 < Date.now();
    } catch {
      return true;
    }
  },

  getTokenPayload(token: string): any {
    try {
      return JSON.parse(atob(token.split('.')[1]));
    } catch {
      return null;
    }
  }
};
```

## Custom Hooks

### useAuth.ts
```typescript
import { useAuthStore } from '../stores/authStore';
import { authService } from '../services/authService';
import { tokenUtils } from '../utils/tokenUtils';
import { useEffect } from 'react';

export const useAuth = () => {
  const {
    user,
    token,
    isAuthenticated,
    isLoading,
    error,
    login,
    logout,
    setLoading,
    setError,
    hasPermission,
    hasRole
  } = useAuthStore();

  // Initialize auth state on app load
  useEffect(() => {
    const initializeAuth = async () => {
      const storedToken = tokenUtils.getToken();
      
      if (storedToken && !tokenUtils.isTokenExpired(storedToken)) {
        try {
          setLoading(true);
          const isValid = await authService.validateToken();
          
          if (isValid) {
            const user = await authService.getCurrentUser();
            login(storedToken, user);
          } else {
            logout();
          }
        } catch (error) {
          logout();
        } finally {
          setLoading(false);
        }
      } else {
        logout();
      }
    };

    initializeAuth();
  }, []);

  const refreshToken = async (): Promise<boolean> => {
    try {
      const response = await authService.refreshToken();
      login(response.token, response.user);
      tokenUtils.setToken(response.token);
      return true;
    } catch {
      logout();
      return false;
    }
  };

  return {
    user,
    token,
    isAuthenticated,
    isLoading,
    error,
    login,
    logout,
    refreshToken,
    hasPermission,
    hasRole,
    setError
  };
};
```

### useLogin.ts
```typescript
import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import { useAuthStore } from '../stores/authStore';
import { tokenUtils } from '../utils/tokenUtils';
import { LoginRequest } from '../types/auth.types';

export const useLogin = () => {
  const navigate = useNavigate();
  const { login, setError } = useAuthStore();

  return useMutation({
    mutationFn: (credentials: LoginRequest) => authService.login(credentials),
    onSuccess: (response) => {
      tokenUtils.setToken(response.token);
      login(response.token, response.user);
      navigate('/dashboard');
    },
    onError: (error: any) => {
      setError(error.response?.data?.message || 'Login failed');
    }
  });
};
```

## Components

### LoginForm.tsx
```typescript
import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button, TextInput, Checkbox, Paper, Title, Text, Alert } from '@mantine/core';
import { useLogin } from '../hooks/useLogin';
import { LoginRequest } from '../types/auth.types';

const loginSchema = z.object({
  username: z.string().min(1, 'Username is required'),
  password: z.string().min(1, 'Password is required'),
  rememberMe: z.boolean().default(false)
});

interface LoginFormProps {
  onRegisterClick?: () => void;
}

export const LoginForm: React.FC<LoginFormProps> = ({ onRegisterClick }) => {
  const loginMutation = useLogin();
  
  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm<LoginRequest>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      rememberMe: false
    }
  });

  const onSubmit = (data: LoginRequest) => {
    loginMutation.mutate(data);
  };

  return (
    <Paper withBorder shadow="md" p={30} mt={30} radius="md">
      <Title order={2} ta="center" mb="md">
        Welcome back!
      </Title>
      
      <form onSubmit={handleSubmit(onSubmit)}>
        {loginMutation.error && (
          <Alert color="red" mb="md">
            {loginMutation.error.message}
          </Alert>
        )}
        
        <TextInput
          label="Username"
          placeholder="Enter your username"
          required
          error={errors.username?.message}
          {...register('username')}
          mb="md"
        />
        
        <TextInput
          label="Password"
          placeholder="Enter your password"
          type="password"
          required
          error={errors.password?.message}
          {...register('password')}
          mb="md"
        />
        
        <Checkbox
          label="Remember me"
          {...register('rememberMe')}
          mb="md"
        />
        
        <Button
          fullWidth
          mt="xl"
          type="submit"
          loading={loginMutation.isPending}
        >
          Sign in
        </Button>
        
        {onRegisterClick && (
          <Text ta="center" mt="md">
            Don't have an account?{' '}
            <Button variant="subtle" onClick={onRegisterClick}>
              Register
            </Button>
          </Text>
        )}
      </form>
    </Paper>
  );
};
```

### ProtectedRoute.tsx
```typescript
import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { LoadingOverlay } from '@mantine/core';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredPermission?: string;
  requiredRole?: string;
  fallbackPath?: string;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  requiredPermission,
  requiredRole,
  fallbackPath = '/login'
}) => {
  const { isAuthenticated, isLoading, hasPermission, hasRole } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return <LoadingOverlay visible />;
  }

  if (!isAuthenticated) {
    return <Navigate to={fallbackPath} state={{ from: location }} replace />;
  }

  if (requiredPermission && !hasPermission(requiredPermission)) {
    return <Navigate to="/unauthorized" replace />;
  }

  if (requiredRole && !hasRole(requiredRole)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return <>{children}</>;
};
```

## Testing Specifications

### LoginForm.test.tsx
```typescript
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter } from 'react-router-dom';
import { LoginForm } from '../LoginForm';
import { authService } from '../../services/authService';

// Mock the auth service
jest.mock('../../services/authService');

const renderWithProviders = (component: React.ReactElement) => {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } }
  });
  
  return render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        {component}
      </BrowserRouter>
    </QueryClientProvider>
  );
};

describe('LoginForm', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders login form with all fields', () => {
    renderWithProviders(<LoginForm />);
    
    expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/remember me/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
  });

  it('validates required fields', async () => {
    renderWithProviders(<LoginForm />);
    
    fireEvent.click(screen.getByRole('button', { name: /sign in/i }));
    
    await waitFor(() => {
      expect(screen.getByText(/username is required/i)).toBeInTheDocument();
      expect(screen.getByText(/password is required/i)).toBeInTheDocument();
    });
  });

  it('submits form with valid data', async () => {
    const mockLogin = jest.mocked(authService.login);
    mockLogin.mockResolvedValue({
      token: 'mock-token',
      user: { id: 1, username: 'testuser' }
    } as any);

    renderWithProviders(<LoginForm />);
    
    fireEvent.change(screen.getByLabelText(/username/i), {
      target: { value: 'testuser' }
    });
    fireEvent.change(screen.getByLabelText(/password/i), {
      target: { value: 'password123' }
    });
    
    fireEvent.click(screen.getByRole('button', { name: /sign in/i }));
    
    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith({
        username: 'testuser',
        password: 'password123',
        rememberMe: false
      });
    });
  });
});
```

## API Integration

### Request Interceptor Setup
```typescript
// In src/services/api.ts
import axios from 'axios';
import { tokenUtils } from '../features/auth/utils/tokenUtils';
import { useAuthStore } from '../features/auth/stores/authStore';

export const apiClient = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 10000
});

// Request interceptor to add auth token
apiClient.interceptors.request.use(
  (config) => {
    const token = tokenUtils.getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor for token refresh
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      try {
        const refreshToken = tokenUtils.getRefreshToken();
        if (refreshToken) {
          const response = await axios.post('/auth/refresh-token', {
            refreshToken
          });
          
          const { token } = response.data;
          tokenUtils.setToken(token);
          originalRequest.headers.Authorization = `Bearer ${token}`;
          
          return apiClient(originalRequest);
        }
      } catch (refreshError) {
        useAuthStore.getState().logout();
        window.location.href = '/login';
      }
    }
    
    return Promise.reject(error);
  }
);
```

## Security Considerations

1. **Token Storage**: Use secure storage mechanisms (httpOnly cookies in production)
2. **Token Validation**: Always validate tokens on the client side
3. **Automatic Logout**: Implement automatic logout on token expiration
4. **CSRF Protection**: Include CSRF tokens for state-changing operations
5. **Input Sanitization**: Sanitize all user inputs to prevent XSS

## Performance Optimizations

1. **Lazy Loading**: Lazy load authentication components
2. **Token Caching**: Cache valid tokens to reduce API calls
3. **Memoization**: Use React.memo for expensive components
4. **Bundle Splitting**: Split authentication code into separate chunks

## Accessibility Requirements

1. **Keyboard Navigation**: All form elements must be keyboard accessible
2. **Screen Reader Support**: Proper ARIA labels and descriptions
3. **Error Announcements**: Screen reader announcements for errors
4. **Focus Management**: Proper focus management in forms
5. **Color Contrast**: Ensure sufficient color contrast for all text