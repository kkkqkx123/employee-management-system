import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '../utils/test-utils';
import { server } from '../mocks/server';
import { http, HttpResponse } from 'msw';
import { createMockEmployee, createMockDepartment } from '../factories';
import { EmployeesPage } from '../../pages/EmployeesPage';

describe('Employee CRUD Integration', () => {
  const mockEmployees = [
    createMockEmployee({ id: 1, firstName: 'John', lastName: 'Doe' }),
    createMockEmployee({ id: 2, firstName: 'Jane', lastName: 'Smith' }),
  ];

  const mockDepartments = [
    createMockDepartment({ id: 1, name: 'Engineering' }),
    createMockDepartment({ id: 2, name: 'HR' }),
  ];

  beforeEach(() => {
    // Mock authenticated user
    localStorage.setItem('token', 'valid-token');

    // Setup API mocks
    server.use(
      http.get('/api/employees', () => {
        return HttpResponse.json({
          success: true,
          data: {
            content: mockEmployees,
            totalElements: mockEmployees.length,
            totalPages: 1,
            size: 20,
            number: 0,
          },
        });
      }),
      http.get('/api/departments', () => {
        return HttpResponse.json({
          success: true,
          data: mockDepartments,
        });
      })
    );
  });

  it('should load and display employee list', async () => {
    render(<EmployeesPage />);

    // Should show loading state initially
    expect(screen.getByText(/loading/i)).toBeInTheDocument();

    // Should display employees after loading
    expect(await screen.findByText('John Doe')).toBeInTheDocument();
    expect(screen.getByText('Jane Smith')).toBeInTheDocument();
  });

  it('should create new employee', async () => {
    const newEmployee = createMockEmployee({ 
      id: 3, 
      firstName: 'Bob', 
      lastName: 'Johnson' 
    });

    server.use(
      http.post('/api/employees', async ({ request }) => {
        const body = await request.json();
        return HttpResponse.json({
          success: true,
          data: { ...newEmployee, ...body },
        });
      })
    );

    render(<EmployeesPage />);

    // Wait for initial load
    await screen.findByText('John Doe');

    // Click add employee button
    const addButton = screen.getByRole('button', { name: /add employee/i });
    fireEvent.click(addButton);

    // Fill form
    const firstNameInput = await screen.findByLabelText(/first name/i);
    const lastNameInput = screen.getByLabelText(/last name/i);
    const emailInput = screen.getByLabelText(/email/i);

    fireEvent.change(firstNameInput, { target: { value: 'Bob' } });
    fireEvent.change(lastNameInput, { target: { value: 'Johnson' } });
    fireEvent.change(emailInput, { target: { value: 'bob.johnson@company.com' } });

    // Submit form
    const saveButton = screen.getByRole('button', { name: /save/i });
    fireEvent.click(saveButton);

    // Should show success message
    expect(await screen.findByText(/employee created successfully/i)).toBeInTheDocument();
  });

  it('should update existing employee', async () => {
    server.use(
      http.put('/api/employees/1', async ({ request }) => {
        const body = await request.json();
        return HttpResponse.json({
          success: true,
          data: { ...mockEmployees[0], ...body },
        });
      })
    );

    render(<EmployeesPage />);

    // Wait for initial load
    await screen.findByText('John Doe');

    // Click edit button for first employee
    const editButtons = screen.getAllByRole('button', { name: /edit/i });
    fireEvent.click(editButtons[0]);

    // Update first name
    const firstNameInput = await screen.findByDisplayValue('John');
    fireEvent.change(firstNameInput, { target: { value: 'Johnny' } });

    // Submit form
    const saveButton = screen.getByRole('button', { name: /save/i });
    fireEvent.click(saveButton);

    // Should show success message
    expect(await screen.findByText(/employee updated successfully/i)).toBeInTheDocument();
  });

  it('should delete employee with confirmation', async () => {
    server.use(
      http.delete('/api/employees/1', () => {
        return HttpResponse.json({
          success: true,
          message: 'Employee deleted successfully',
        });
      })
    );

    render(<EmployeesPage />);

    // Wait for initial load
    await screen.findByText('John Doe');

    // Click delete button for first employee
    const deleteButtons = screen.getAllByRole('button', { name: /delete/i });
    fireEvent.click(deleteButtons[0]);

    // Should show confirmation dialog
    expect(await screen.findByText(/are you sure/i)).toBeInTheDocument();

    // Confirm deletion
    const confirmButton = screen.getByRole('button', { name: /confirm/i });
    fireEvent.click(confirmButton);

    // Should show success message
    expect(await screen.findByText(/employee deleted successfully/i)).toBeInTheDocument();
  });

  it('should handle search functionality', async () => {
    const searchResults = [mockEmployees[0]]; // Only John Doe

    server.use(
      http.get('/api/employees', ({ request }) => {
        const url = new URL(request.url);
        const search = url.searchParams.get('search');
        
        if (search === 'John') {
          return HttpResponse.json({
            success: true,
            data: {
              content: searchResults,
              totalElements: 1,
              totalPages: 1,
              size: 20,
              number: 0,
            },
          });
        }
        
        return HttpResponse.json({
          success: true,
          data: {
            content: mockEmployees,
            totalElements: mockEmployees.length,
            totalPages: 1,
            size: 20,
            number: 0,
          },
        });
      })
    );

    render(<EmployeesPage />);

    // Wait for initial load
    await screen.findByText('John Doe');
    expect(screen.getByText('Jane Smith')).toBeInTheDocument();

    // Search for "John"
    const searchInput = screen.getByPlaceholder(/search employees/i);
    fireEvent.change(searchInput, { target: { value: 'John' } });

    // Should show only John Doe
    await waitFor(() => {
      expect(screen.getByText('John Doe')).toBeInTheDocument();
      expect(screen.queryByText('Jane Smith')).not.toBeInTheDocument();
    });
  });
});