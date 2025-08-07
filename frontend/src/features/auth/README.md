# Authentication Feature

This directory contains the complete authentication system for the React frontend application.

## Overview

The authentication system provides secure user login, registration, JWT token management, and role-based access control. It's built using modern React patterns with TypeScript and integrates seamlessly with the Spring Boot backend.

## Features

- ✅ **Login Form** - Secure user authentication with validation
- ✅ **Registration Form** - User account creation with comprehensive validation
- ✅ **JWT Token Management** - Automatic token refresh and secure storage
- ✅ **Protected Routes** - Route-level access control based on authentication and permissions
- ✅ **Permission Checks** - Granular permission-based component rendering
- ✅ **Auth Guard** - Application-level authentication initialization
- ✅ **Logout Functionality** - Secure session termination
- ✅ **Error Handling** - Comprehensive error states and user feedback
- ✅ **Loading States** - Proper loading indicators during auth operations
- ✅ **Responsive Design** - Mobile-friendly authentication forms

## Components

### Core Components

- **`LoginForm`** - User login interface with validation
- **`RegisterForm`** - User registration with password strength validation
- **`AuthPage`** - Combined login/register page with smooth transitions
- **`ProtectedRoute`** - Route wrapper for authentication and permission checks
- **`AuthGuard`** - Application initialization and auth state management
- **`LogoutButton`** - User logout with confirmation modal

### Demo Components

- **`AuthDemo`** - Interactive demo showcasing all authentication features

## Hooks

- **`useAuthRedirect`** - Handle authentication-based navigation
- **`usePermissionCheck`** - Check user permissions with memoization
- **`useTokenRefresh`** - Automatic JWT token refresh management

## Services

- **`TokenManager`** - Secure JWT token storage and validation utilities
- **`AuthService`** - API integration for authentication operations (imported from main services)

## Usage Examples

### Basic Authentication Setup

```tsx
import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthGuard, AuthPage, ProtectedRoute } from './features/auth';
import Dashboard from './components/Dashboard';

function App() {
  return (
    <BrowserRouter>
      <AuthGuard>
        <Routes>
          <Route path="/login" element={<AuthPage />} />
          <Route 
            path="/dashboard" 
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            } 
          />
        </Routes>
      </AuthGuard>
    </BrowserRouter>
  );
}
```

### Permission-Based Access Control

```tsx
import { ProtectedRoute } from './features/auth';

// Require any of the specified permissions
<ProtectedRoute requiredPermissions={['USER_READ', 'USER_WRITE']}>
  <UserManagement />
</ProtectedRoute>

// Require all specified permissions
<ProtectedRoute 
  requiredPermissions={['ADMIN_ACCESS', 'USER_DELETE']} 
  requireAllPermissions={true}
>
  <AdminPanel />
</ProtectedRoute>
```

### Using Authentication Hooks

```tsx
import { useAuth, usePermissions } from '../stores/authStore';
import { usePermissionCheck } from './features/auth';

function MyComponent() {
  const { user, isAuthenticated } = useAuth();
  const { hasAccess } = usePermissionCheck({ 
    permissions: ['USER_WRITE'], 
    requireAll: false 
  });

  if (!isAuthenticated) {
    return <div>Please log in</div>;
  }

  return (
    <div>
      <h1>Welcome, {user?.firstName}!</h1>
      {hasAccess && <button>Edit User</button>}
    </div>
  );
}
```

### Custom Login Integration

```tsx
import { LoginForm } from './features/auth';
import { useNavigate } from 'react-router-dom';

function CustomLoginPage() {
  const navigate = useNavigate();

  const handleLoginSuccess = () => {
    navigate('/dashboard');
  };

  const handleRegisterClick = () => {
    navigate('/register');
  };

  return (
    <div className="login-container">
      <LoginForm 
        onSuccess={handleLoginSuccess}
        onRegisterClick={handleRegisterClick}
      />
    </div>
  );
}
```

## State Management

The authentication system uses Zustand for state management with the following stores:

- **`authStore`** - Main authentication state (user, token, permissions)
- **`uiStore`** - UI-related state (theme, navigation)
- **`notificationStore`** - Notification state for auth-related messages

## API Integration

The authentication system integrates with the Spring Boot backend through:

- **Login**: `POST /api/auth/login`
- **Register**: `POST /api/auth/register`
- **Logout**: `POST /api/auth/logout`
- **Token Refresh**: `POST /api/auth/refresh-token`
- **Token Validation**: `GET /api/auth/validate-token`

## Security Features

- **JWT Token Security** - Secure token storage and automatic refresh
- **Input Validation** - Comprehensive form validation with Zod schemas
- **XSS Protection** - Input sanitization and secure rendering
- **CSRF Protection** - Token-based request authentication
- **Permission Checks** - Granular access control at component level
- **Secure Logout** - Complete session cleanup and token invalidation

## Testing

The authentication system includes comprehensive tests:

- **Unit Tests** - Component behavior and hook functionality
- **Integration Tests** - Authentication flows and API integration
- **Accessibility Tests** - WCAG compliance and keyboard navigation

Run tests with:
```bash
npm run test src/features/auth
```

## Development

### Adding New Permissions

1. Add permission constants to the backend
2. Update the `Permission` type in `types/auth.ts`
3. Use `usePermissionCheck` hook in components
4. Add tests for new permission logic

### Extending Authentication

1. Add new auth-related components to `components/`
2. Create corresponding hooks in `hooks/`
3. Update the main exports in `index.ts`
4. Add comprehensive tests

## Demo

To see the authentication system in action, use the `AuthDemo` component:

```tsx
import { AuthDemo } from './features/auth';

function App() {
  return <AuthDemo />;
}
```

This provides a complete interactive demonstration of all authentication features including login, registration, permission checks, and user management.