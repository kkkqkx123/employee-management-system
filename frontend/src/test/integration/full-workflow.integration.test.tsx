/**
 * Full workflow integration tests
 * Tests complete user workflows across all features
 */

import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MantineProvider } from '@mantine/core';
import { Notifications } from '@mantine/notifications';

import App from '../../App';
import { server } from '../mocks/server';
import { theme } from '../../theme';

// Test wrapper component
const TestWrapper = ({ children }: { children: React.ReactNode }) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        gcTime: 0,
      },
    },
  });

  return (
    <QueryClientProvider client={queryClient}>
      <MantineProvider theme={theme}>
        <Notifications />
        <BrowserRouter>
          {children}
        </BrowserRouter>
      </MantineProvider>
    </QueryClientProvider>
  );
};

describe('Full Application Workflow Integration Tests', () => {
  beforeEach(() => {
    server.listen();
    // Clear localStorage before each test
    localStorage.clear();
    sessionStorage.clear();
  });

  afterEach(() => {
    server.resetHandlers();
  });

  describe('Authentication Flow', () => {
    it('should complete full login workflow', async () => {
      const user = userEvent.setup();
      
      render(
        <TestWrapper>
          <App />
        </TestWrapper>
      );

      // Should start at login page
      expect(screen.getByRole('heading', { name: /login/i })).toBeInTheDocument();

      // Fill in login form
      await user.type(screen.getByLabelText(/email/i), 'admin@company.com');
      await user.type(screen.getByLabelText(/password/i), 'password123');
      
      // Submit form
      await user.click(screen.getByRole('button', { name: /sign in/i }));

      // Should redirect to dashboard
      await waitFor(() => {
        expect(screen.getByRole('heading', { name: /dashboard/i })).toBeInTheDocument();
      });

      // Should show user info in header
      expect(screen.getByText(/admin@company.com/i)).toBeInTheDocument();
    });

    it('should handle logout workflow', async () => {
      const user = userEvent.setup();
      
      // Set up authenticated state
      localStorage.setItem('auth_token', 'mock-token');
      
      render(
        <TestWrapper>
          <App />
        </TestWrapper>
      );

      // Should be on dashboard
      await waitFor(() => {
        expect(screen.getByRole('heading', { name: /dashboard/i })).toBeInTheDocument();
      });

      // Click logout
      await user.click(screen.getByRole('button', { name: /logout/i }));

      // Should redirect to login
      await waitFor(() => {
        expect(screen.getByRole('heading', { name: /login/i })).toBeInTheDocument();
      });

      // Token should be cleared
      expect(localStorage.getItem('auth_token')).toBeNull();
    });
  });

  describe('Employee Management Workflow', () => {
    beforeEach(() => {
      // Set up authenticated state
      localStorage.setItem('auth_token', 'mock-token');
    });

    it('should complete employee CRUD workflow', async () => {
      const user = userEvent.setup();
      
      render(
        <TestWrapper>
          <App />
        </TestWrapper>
      );

      // Navigate to employees page
      await user.click(screen.getByRole('link', { name: /employees/i }));

      await waitFor(() => {
        expect(screen.getByRole('heading', { name: /employees/i })).toBeInTheDocument();
      });

      // Should show employee list
      expect(screen.getByText(/john doe/i)).toBeInTheDocument();

      // Create new employee
      await user.click(screen.getByRole('button', { name: /add employee/i }));

      // Fill form
      await user.type(screen.getByLabelText(/first name/i), 'Jane');
      await user.type(screen.getByLabelText(/last name/i), 'Smith');
      await user.type(screen.getByLabelText(/email/i), 'jane.smith@company.com');

      // Submit form
      await user.click(screen.getByRole('button', { name: /save/i }));

      // Should show success message
      await waitFor(() => {
        expect(screen.getByText(/employee created successfully/i)).toBeInTheDocument();
      });

      // Should show new employee in list
      expect(screen.getByText(/jane smith/i)).toBeInTheDocument();
    });
  });

  describe('Real-time Features Workflow', () => {
    beforeEach(() => {
      localStorage.setItem('auth_token', 'mock-token');
    });

    it('should handle chat workflow', async () => {
      const user = userEvent.setup();
      
      render(
        <TestWrapper>
          <App />
        </TestWrapper>
      );

      // Navigate to chat
      await user.click(screen.getByRole('link', { name: /chat/i }));

      await waitFor(() => {
        expect(screen.getByRole('heading', { name: /chat/i })).toBeInTheDocument();
      });

      // Should show conversation list
      expect(screen.getByText(/conversations/i)).toBeInTheDocument();

      // Send a message
      const messageInput = screen.getByPlaceholderText(/type a message/i);
      await user.type(messageInput, 'Hello, this is a test message');
      await user.click(screen.getByRole('button', { name: /send/i }));

      // Should show message in chat
      await waitFor(() => {
        expect(screen.getByText(/hello, this is a test message/i)).toBeInTheDocument();
      });
    });

    it('should handle notifications workflow', async () => {
      const user = userEvent.setup();
      
      render(
        <TestWrapper>
          <App />
        </TestWrapper>
      );

      // Should show notification badge
      const notificationBadge = screen.getByRole('button', { name: /notifications/i });
      expect(notificationBadge).toBeInTheDocument();

      // Click notifications
      await user.click(notificationBadge);

      // Should show notification dropdown
      await waitFor(() => {
        expect(screen.getByText(/recent notifications/i)).toBeInTheDocument();
      });

      // Should show notifications
      expect(screen.getByText(/new employee added/i)).toBeInTheDocument();

      // Mark as read
      await user.click(screen.getByRole('button', { name: /mark all as read/i }));

      // Badge should update
      await waitFor(() => {
        expect(screen.queryByText(/unread/i)).not.toBeInTheDocument();
      });
    });
  });

  describe('Navigation and Accessibility Workflow', () => {
    beforeEach(() => {
      localStorage.setItem('auth_token', 'mock-token');
    });

    it('should support keyboard navigation', async () => {
      const user = userEvent.setup();
      
      render(
        <TestWrapper>
          <App />
        </TestWrapper>
      );

      // Tab through navigation
      await user.tab();
      expect(screen.getByRole('link', { name: /dashboard/i })).toHaveFocus();

      await user.tab();
      expect(screen.getByRole('link', { name: /employees/i })).toHaveFocus();

      // Enter should activate link
      await user.keyboard('{Enter}');

      await waitFor(() => {
        expect(screen.getByRole('heading', { name: /employees/i })).toBeInTheDocument();
      });
    });

    it('should handle responsive navigation', async () => {
      const user = userEvent.setup();
      
      // Mock mobile viewport
      Object.defineProperty(window, 'innerWidth', {
        writable: true,
        configurable: true,
        value: 768,
      });

      render(
        <TestWrapper>
          <App />
        </TestWrapper>
      );

      // Should show mobile menu button
      const mobileMenuButton = screen.getByRole('button', { name: /menu/i });
      expect(mobileMenuButton).toBeInTheDocument();

      // Click to open menu
      await user.click(mobileMenuButton);

      // Should show navigation items
      await waitFor(() => {
        expect(screen.getByRole('link', { name: /employees/i })).toBeVisible();
      });
    });
  });

  describe('Error Handling Workflow', () => {
    beforeEach(() => {
      localStorage.setItem('auth_token', 'mock-token');
    });

    it('should handle network errors gracefully', async () => {
      const user = userEvent.setup();
      
      // Mock network error
      server.use(
        // Add error handlers here
      );

      render(
        <TestWrapper>
          <App />
        </TestWrapper>
      );

      // Navigate to employees
      await user.click(screen.getByRole('link', { name: /employees/i }));

      // Should show error message
      await waitFor(() => {
        expect(screen.getByText(/something went wrong/i)).toBeInTheDocument();
      });

      // Should show retry button
      const retryButton = screen.getByRole('button', { name: /retry/i });
      expect(retryButton).toBeInTheDocument();

      // Retry should work
      await user.click(retryButton);

      // Should attempt to reload
      await waitFor(() => {
        expect(screen.getByText(/loading/i)).toBeInTheDocument();
      });
    });
  });

  describe('Performance and Loading States', () => {
    beforeEach(() => {
      localStorage.setItem('auth_token', 'mock-token');
    });

    it('should show loading states during navigation', async () => {
      const user = userEvent.setup();
      
      render(
        <TestWrapper>
          <App />
        </TestWrapper>
      );

      // Navigate to employees
      await user.click(screen.getByRole('link', { name: /employees/i }));

      // Should show loading spinner
      expect(screen.getByRole('status')).toBeInTheDocument();

      // Should eventually load content
      await waitFor(() => {
        expect(screen.getByRole('heading', { name: /employees/i })).toBeInTheDocument();
      });
    });

    it('should handle concurrent operations', async () => {
      const user = userEvent.setup();
      
      render(
        <TestWrapper>
          <App />
        </TestWrapper>
      );

      // Navigate to multiple pages quickly
      await user.click(screen.getByRole('link', { name: /employees/i }));
      await user.click(screen.getByRole('link', { name: /departments/i }));
      await user.click(screen.getByRole('link', { name: /chat/i }));

      // Should handle navigation correctly
      await waitFor(() => {
        expect(screen.getByRole('heading', { name: /chat/i })).toBeInTheDocument();
      });
    });
  });
});