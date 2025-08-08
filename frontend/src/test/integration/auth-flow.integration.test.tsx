import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '../utils/test-utils';
import { server } from '../mocks/server';
import { http, HttpResponse } from 'msw';
import App from '../../App';

describe('Authentication Flow Integration', () => {
  beforeEach(() => {
    // Clear any stored tokens
    localStorage.clear();
    sessionStorage.clear();
  });

  it('should handle complete login flow', async () => {
    render(<App />);

    // Should redirect to login page
    expect(await screen.findByRole('heading', { name: /sign in/i })).toBeInTheDocument();

    // Fill login form
    const emailInput = screen.getByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    fireEvent.click(submitButton);

    // Should redirect to dashboard after successful login
    expect(await screen.findByText(/dashboard/i)).toBeInTheDocument();
    
    // Should store token in localStorage
    expect(localStorage.getItem('token')).toBeTruthy();
  });

  it('should handle login failure', async () => {
    // Mock failed login response
    server.use(
      http.post('/api/auth/login', () => {
        return HttpResponse.json(
          { success: false, message: 'Invalid credentials' },
          { status: 401 }
        );
      })
    );

    render(<App />);

    const emailInput = await screen.findByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    fireEvent.change(emailInput, { target: { value: 'invalid@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'wrongpassword' } });
    fireEvent.click(submitButton);

    // Should show error message
    expect(await screen.findByText(/invalid credentials/i)).toBeInTheDocument();
    
    // Should not store token
    expect(localStorage.getItem('token')).toBeNull();
  });

  it('should handle token expiration and refresh', async () => {
    // Mock expired token scenario
    server.use(
      http.get('/api/employees', () => {
        return HttpResponse.json(
          { success: false, message: 'Token expired' },
          { status: 401 }
        );
      }),
      http.post('/api/auth/refresh', () => {
        return HttpResponse.json({
          success: true,
          data: { token: 'new-jwt-token', expiresIn: 3600 }
        });
      })
    );

    // Set initial token
    localStorage.setItem('token', 'expired-token');

    render(<App />);

    // Navigate to employees page
    const employeesLink = await screen.findByRole('link', { name: /employees/i });
    fireEvent.click(employeesLink);

    // Should automatically refresh token and load data
    await waitFor(() => {
      expect(localStorage.getItem('token')).toBe('new-jwt-token');
    });
  });

  it('should handle logout flow', async () => {
    // Set initial authenticated state
    localStorage.setItem('token', 'valid-token');

    render(<App />);

    // Should be on dashboard
    expect(await screen.findByText(/dashboard/i)).toBeInTheDocument();

    // Click logout button
    const logoutButton = screen.getByRole('button', { name: /logout/i });
    fireEvent.click(logoutButton);

    // Should redirect to login and clear token
    expect(await screen.findByRole('heading', { name: /sign in/i })).toBeInTheDocument();
    expect(localStorage.getItem('token')).toBeNull();
  });
});