// Employee form component tests

import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MantineProvider } from '@mantine/core';
import { vi } from 'vitest';
import { EmployeeForm } from '../EmployeeForm';
import * as employeeHooks from '../../hooks/useEmployees';
import type { Employee } from '../../types';

// Mock the hooks
vi.mock('../../hooks/useEmployees');

// Mock notifications
vi.mock('@mantine/notifications', () => ({
  notifications: {
    show: vi.fn(),
  },
}));

const mockEmployee: Employee = {
  id: 1,
  employeeNumber: 'EMP-001',
  firstName: 'John',
  lastName: 'Doe',
  fullName: 'John Doe',
  email: 'john.doe@example.com',
  phone: '+1-555-0123',
  departmentId: 1,
  departmentName: 'Engineering',
  positionId: 1,
  positionName: 'Software Engineer',
  hireDate: '2023-01-15',
  status: 'ACTIVE',
  employmentType: 'FULL_TIME',
  payType: 'SALARIED',
  salary: 75000,
  enabled: true,
  createdAt: '2023-01-15T00:00:00Z',
  updatedAt: '2023-01-15T00:00:00Z',
};

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <MantineProvider>
        {children}
      </MantineProvider>
    </QueryClientProvider>
  );
};

describe('EmployeeForm', () => {
  const mockOnSuccess = vi.fn();
  const mockOnCancel = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    
    // Mock create mutation
    vi.mocked(employeeHooks.useCreateEmployee).mockReturnValue({
      mutateAsync: vi.fn(),
      isPending: false,
    } as any);

    // Mock update mutation
    vi.mocked(employeeHooks.useUpdateEmployee).mockReturnValue({
      mutateAsync: vi.fn(),
      isPending: false,
    } as any);
  });

  it('renders create form correctly', () => {
    render(
      <EmployeeForm onSuccess={mockOnSuccess} onCancel={mockOnCancel} />,
      { wrapper: createWrapper() }
    );

    expect(screen.getByText('Personal Information')).toBeInTheDocument();
    expect(screen.getByText('Address Information')).toBeInTheDocument();
    expect(screen.getByText('Employment Information')).toBeInTheDocument();
    expect(screen.getByText('Compensation Information')).toBeInTheDocument();
    expect(screen.getByText('Sensitive Information')).toBeInTheDocument();

    expect(screen.getByText('Create Employee')).toBeInTheDocument();
  });

  it('renders edit form with employee data', () => {
    render(
      <EmployeeForm 
        employee={mockEmployee} 
        onSuccess={mockOnSuccess} 
        onCancel={mockOnCancel} 
      />,
      { wrapper: createWrapper() }
    );

    expect(screen.getByDisplayValue('John')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Doe')).toBeInTheDocument();
    expect(screen.getByDisplayValue('john.doe@example.com')).toBeInTheDocument();
    expect(screen.getByText('Update Employee')).toBeInTheDocument();
  });

  it('validates required fields', async () => {
    render(
      <EmployeeForm onSuccess={mockOnSuccess} onCancel={mockOnCancel} />,
      { wrapper: createWrapper() }
    );

    const submitButton = screen.getByText('Create Employee');
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('First name must be at least 2 characters')).toBeInTheDocument();
      expect(screen.getByText('Last name must be at least 2 characters')).toBeInTheDocument();
      expect(screen.getByText('Email is required')).toBeInTheDocument();
    });
  });

  it('validates email format', async () => {
    render(
      <EmployeeForm onSuccess={mockOnSuccess} onCancel={mockOnCancel} />,
      { wrapper: createWrapper() }
    );

    const emailInput = screen.getByLabelText('Email');
    fireEvent.change(emailInput, { target: { value: 'invalid-email' } });

    const submitButton = screen.getByText('Create Employee');
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Please enter a valid email address')).toBeInTheDocument();
    });
  });

  it('validates phone number format', async () => {
    render(
      <EmployeeForm onSuccess={mockOnSuccess} onCancel={mockOnCancel} />,
      { wrapper: createWrapper() }
    );

    const phoneInput = screen.getByLabelText('Phone');
    fireEvent.change(phoneInput, { target: { value: 'invalid-phone' } });

    const submitButton = screen.getByText('Create Employee');
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Please enter a valid phone number')).toBeInTheDocument();
    });
  });

  it('shows salary field when pay type is salaried', async () => {
    render(
      <EmployeeForm onSuccess={mockOnSuccess} onCancel={mockOnCancel} />,
      { wrapper: createWrapper() }
    );

    const payTypeSelect = screen.getByLabelText('Pay Type');
    fireEvent.change(payTypeSelect, { target: { value: 'SALARIED' } });

    await waitFor(() => {
      expect(screen.getByLabelText('Annual Salary')).toBeInTheDocument();
    });
  });

  it('shows hourly rate field when pay type is hourly', async () => {
    render(
      <EmployeeForm onSuccess={mockOnSuccess} onCancel={mockOnCancel} />,
      { wrapper: createWrapper() }
    );

    const payTypeSelect = screen.getByLabelText('Pay Type');
    fireEvent.change(payTypeSelect, { target: { value: 'HOURLY' } });

    await waitFor(() => {
      expect(screen.getByLabelText('Hourly Rate')).toBeInTheDocument();
    });
  });

  it('calls onCancel when cancel button is clicked', () => {
    render(
      <EmployeeForm onSuccess={mockOnSuccess} onCancel={mockOnCancel} />,
      { wrapper: createWrapper() }
    );

    const cancelButton = screen.getByText('Cancel');
    fireEvent.click(cancelButton);

    expect(mockOnCancel).toHaveBeenCalled();
  });

  it('submits create form with valid data', async () => {
    const mockMutateAsync = vi.fn().mockResolvedValue(mockEmployee);
    vi.mocked(employeeHooks.useCreateEmployee).mockReturnValue({
      mutateAsync: mockMutateAsync,
      isPending: false,
    } as any);

    render(
      <EmployeeForm onSuccess={mockOnSuccess} onCancel={mockOnCancel} />,
      { wrapper: createWrapper() }
    );

    // Fill in required fields
    fireEvent.change(screen.getByLabelText('First Name'), { target: { value: 'John' } });
    fireEvent.change(screen.getByLabelText('Last Name'), { target: { value: 'Doe' } });
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'john.doe@example.com' } });
    
    // Set department and position (these would normally be dropdowns)
    // For testing, we'll assume they're set programmatically
    
    const submitButton = screen.getByText('Create Employee');
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockMutateAsync).toHaveBeenCalled();
      expect(mockOnSuccess).toHaveBeenCalled();
    });
  });

  it('submits update form with valid data', async () => {
    const mockMutateAsync = vi.fn().mockResolvedValue(mockEmployee);
    vi.mocked(employeeHooks.useUpdateEmployee).mockReturnValue({
      mutateAsync: mockMutateAsync,
      isPending: false,
    } as any);

    render(
      <EmployeeForm 
        employee={mockEmployee} 
        onSuccess={mockOnSuccess} 
        onCancel={mockOnCancel} 
      />,
      { wrapper: createWrapper() }
    );

    const submitButton = screen.getByText('Update Employee');
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockMutateAsync).toHaveBeenCalledWith({
        id: mockEmployee.id,
        data: expect.any(Object),
      });
      expect(mockOnSuccess).toHaveBeenCalled();
    });
  });

  it('shows loading state during submission', () => {
    vi.mocked(employeeHooks.useCreateEmployee).mockReturnValue({
      mutateAsync: vi.fn(),
      isPending: true,
    } as any);

    render(
      <EmployeeForm onSuccess={mockOnSuccess} onCancel={mockOnCancel} />,
      { wrapper: createWrapper() }
    );

    const submitButton = screen.getByText('Create Employee');
    expect(submitButton).toBeDisabled();
    
    const cancelButton = screen.getByText('Cancel');
    expect(cancelButton).toBeDisabled();
  });

  it('shows termination date field in edit mode', () => {
    render(
      <EmployeeForm 
        employee={mockEmployee} 
        onSuccess={mockOnSuccess} 
        onCancel={mockOnCancel} 
      />,
      { wrapper: createWrapper() }
    );

    expect(screen.getByLabelText('Termination Date')).toBeInTheDocument();
  });

  it('does not show termination date field in create mode', () => {
    render(
      <EmployeeForm onSuccess={mockOnSuccess} onCancel={mockOnCancel} />,
      { wrapper: createWrapper() }
    );

    expect(screen.queryByLabelText('Termination Date')).not.toBeInTheDocument();
  });
});