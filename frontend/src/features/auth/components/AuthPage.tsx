import React, { useState } from 'react';
import { Navigate } from 'react-router-dom';
import { Container, Center, Box, Transition } from '@mantine/core';
import { useAuth } from '../../../stores/authStore';
import LoginForm from './LoginForm';
import RegisterForm from './RegisterForm';

type AuthMode = 'login' | 'register';

interface AuthPageProps {
  defaultMode?: AuthMode;
  redirectTo?: string;
}

export const AuthPage: React.FC<AuthPageProps> = ({
  defaultMode = 'login',
  redirectTo = '/dashboard',
}) => {
  const { isAuthenticated } = useAuth();
  const [mode, setMode] = useState<AuthMode>(defaultMode);

  // Redirect if already authenticated
  if (isAuthenticated) {
    return <Navigate to={redirectTo} replace />;
  }

  const handleLoginSuccess = () => {
    // Navigation will be handled by the router based on authentication state
  };

  const handleRegisterSuccess = () => {
    setMode('login');
  };

  const handleSwitchToRegister = () => {
    setMode('register');
  };

  const handleSwitchToLogin = () => {
    setMode('login');
  };

  const handleForgotPassword = () => {
    // TODO: Implement forgot password functionality
    console.log('Forgot password clicked');
  };

  return (
    <Container size={420} my={40}>
      <Center style={{ minHeight: 'calc(100vh - 80px)' }}>
        <Box w="100%">
          <Transition
            mounted={mode === 'login'}
            transition="fade"
            duration={200}
            timingFunction="ease"
          >
            {(styles) => (
              <div style={styles}>
                {mode === 'login' && (
                  <LoginForm
                    onSuccess={handleLoginSuccess}
                    onRegisterClick={handleSwitchToRegister}
                    onForgotPasswordClick={handleForgotPassword}
                  />
                )}
              </div>
            )}
          </Transition>

          <Transition
            mounted={mode === 'register'}
            transition="fade"
            duration={200}
            timingFunction="ease"
          >
            {(styles) => (
              <div style={styles}>
                {mode === 'register' && (
                  <RegisterForm
                    onSuccess={handleRegisterSuccess}
                    onLoginClick={handleSwitchToLogin}
                  />
                )}
              </div>
            )}
          </Transition>
        </Box>
      </Center>
    </Container>
  );
};

export default AuthPage;