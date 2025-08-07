// Employee list component tests

import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MantineProvider } from '@mantine/core';
import { vi } from 'vitest';
import { EmployeeList } from '../EmployeeList';
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

// Mock components that might not be available in test environment
vi.mock('../EmployeeForm', () => ({
  EmployeeForm: ({ onSuccess, onCancel }: any) => (
    <div data-testid="employee-form">
      <button onClick={onSuccess}>Save</button>
      <button onClick={onCancel}>Cancel</button>
    </div>
  ),
}));

vi.mock('../EmployeeDetail', () => ({
  EmployeeDetail: ({ employee, onEdit, onClose }: any) => (
    <div data-testid="employee-detail">
      <span>{employee.fullName}</span>
      <button onClick={onEdit}>Edit</button>
      <button onClick={onClose}>Close</button>
    </div>
  ),
}));

vi.mock('../EmployeeImport', () => ({
  EmployeeImport: ({ onSuccess, onCancel }: any) => (
    <div data-testid="employee-import">
      <button onClick={onSuccess}>Import</button>
      <button onClick={onCancel}>Cancel</button>
    </div>
  ),
}));

vi.mock('../EmployeeExport', () => ({
  EmployeeExport: ({ onClose }: any) => (
    <div data-testid="employee-export">
      <button onClick={onClose}>Close</button>
    </div>
  ),
}));

const mockEmployees: Employee[] = [
  {
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
  },
  {
    id: 2,
    employeeNumber: 'EMP-002',
    firstName: 'Jane',
    lastName: 'Smith',
    fullName: 'Jane Smith',
    email: 'jane.smith@example.com',
    departmentId: 2,
    departmentName: 'HR',
    positionId: 2,
    positionName: 'HR Manager',
    hireDate: '2022-06-01',
    status: 'ACTIVE',
    employmentType: 'FULL_TIME',
    payType: 'SALARIED',
    salary: 85000,
    enabled: true,
    createdAt: '2022-06-01T00:00:00Z',
    updatedAt: '2022-06-01T00:00:00Z',
  },
];

const mockEmployeesData = {
  content: mockEmployees,
  totalElements: 2,
  totalPages: 1,
  size: 20,
  number: 0,
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

describe('EmployeeList', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    
    // Mock the useEmployees hook
    vi.mocked(employeeHooks.useEmployees).mockReturnValue({
      data: mockEmployeesData,
      isLoading: false,
      error: null,
      refetch: vi.fn(),
    } as any);

    // Mock other hooks
    vi.mocked(employeeHooks.useDeleteEmployee).mockReturnValue({
      mutateAsync: vi.fn(),
      isPending: false,
    } as any);

    vi.mocked(employeeHooks.useBulkEmployeeOperation).mockReturnValue({
      mutateAsync: vi.fn(),
      isPending: false,
    } as any);
  });

  it('renders employee list with data', async () => {
    render(<EmployeeList />, { wrapper: createWrapper() });

    expect(screen.getByText('Employees')).toBeInTheDocument();
    expect(screen.getByText('Manage employee records and information')).toBeInTheDocument();
    
    // Check if employees are displayed
    await waitFor(() => {
      expect(screen.getByText('John Doe')).toBeInTheDocument();
      expect(screen.getByText('Jane Smith')).toBeInTheDocument();
    });

    expect(screen.getByText('john.doe@example.com')).toBeInTheDocument();
    expect(screen.getByText('jane.smith@example.com')).toBeInTheDocument();
  });

  it('displays loading state', () => {
    vi.mocked(employeeHooks.useEmployees).mockReturnValue({
      data: undefined,
      isLoading: true,
      error: null,
      refetch: vi.fn(),
    } as any);

    render(<EmployeeList />, { wrapper: createWrapper() });

    expect(screen.getByTestId('loading-spinner')).toBeInTheDocument();
  });

  it('displays error state', () => {
    vi.mocked(employeeHooks.useEmployees).mockReturnValue({
      data: undefined,
      isLoading: false,
      error: new Error('Failed to load'),
      refetch: vi.fn(),
    } as any);

    render(<EmployeeList />, { wrapper: createWrapper() });

    expect(screen.getByText('Failed to load employees. Please try again.')).toBeInTheDocument();
  });

  it('opens create modal when add button is clicked', async () => {
    render(<EmployeeList />, { wrapper: createWrapper() });

    const addButton = screen.getByText('Add Employee');
    fireEvent.click(addButton);

    await waitFor(() => {
      expect(screen.getByTestId('employee-form')).toBeInTheDocument();
    });
  });

  it('opens import modal when import button is clicked', async () => {
    render(<EmployeeList />, { wrapper: createWrapper() });

    const importButton = screen.getByText('Import');
    fireEvent.click(importButton);

    await waitFor(() => {
      expect(screen.getByTestId('employee-import')).toBeInTheDocument();
    });
  });

  it('opens export modal when export button is clicked', async () => {
    render(<EmployeeList />, { wrapper: createWrapper() });

    const exportButton = screen.getByText('Export');
    fireEvent.click(exportButton);

    await waitFor(() => {
      expect(screen.getByTestId('employee-export')).toBeInTheDocument();
    });
  });

  it('filters employees by search criteria', async () => {
    const mockRefetch = vi.fn();
    vi.mocked(employeeHooks.useEmployees).mockReturnValue({
      data: mockEmployeesData,
      isLoading: false,
      error: null,
      refetch: mockRefetch,
    } as any);

    render(<EmployeeList />, { wrapper: createWrapper() });

    const searchInput = screen.getByPlaceholderText('Search by name or email...');
    fireEvent.change(searchInput, { target: { value: 'John' } });

    // The component should update search criteria
    expect(searchInput).toHaveValue('John');
  });

  it('handles employee selection in selection mode', () => {
    const mockOnSelectionChange = vi.fn();
    
    render(
      <EmployeeList 
        selectionMode={true} 
        onSelectionChange={mockOnSelectionChange}
      />, 
      { wrapper: createWrapper() }
    );

    // Should show checkboxes in selection mode
    const checkboxes = screen.getAllByRole('checkbox');
    expect(checkboxes.length).toBeGreaterThan(0);
  });

  it('shows employee actions', async () => {
    render(<EmployeeList />, { wrapper: createWrapper() });

    await waitFor(() => {
      // Should show action buttons for each employee
      const viewButtons = screen.getAllByTitle('View Details');
      const editButtons = screen.getAllByTitle('Edit Employee');
      const deleteButtons = screen.getAllByTitle('Delete Employee');

      expect(viewButtons).toHaveLength(2);
      expect(editButtons).toHaveLength(2);
      expect(deleteButtons).toHaveLength(2);
    });
  });

  it('opens employee detail modal when view button is clicked', async () => {
    render(<EmployeeList />, { wrapper: createWrapper() });

    await waitFor(() => {
      const viewButton = screen.getAllByTitle('View Details')[0];
      fireEvent.click(viewButton);
    });

    await waitFor(() => {
      expect(screen.getByTestId('employee-detail')).toBeInTheDocument();
    });
  });

  it('clears filters when clear button is clicked', async () => {
    render(<EmployeeList />, { wrapper: createWrapper() });

    const searchInput = screen.getByPlaceholderText('Search by name or email...');
    fireEvent.change(searchInput, { target: { value: 'John' } });

    const clearButton = screen.getByText('Clear Filters');
    fireEvent.click(clearButton);

    expect(searchInput).toHaveValue('');
  });
});