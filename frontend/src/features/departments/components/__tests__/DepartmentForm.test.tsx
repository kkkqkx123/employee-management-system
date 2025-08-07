import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { DepartmentForm } from '../DepartmentForm';
import { Department } from '../../types';

// Mock the department service
jest.mock('../../services/departmentService', () => ({
  departmentService: {
    createDepartment: jest.fn(),
    updateDepartment: jest.fn(),
  },
}));

const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

const renderWithQueryClient = (component: React.ReactElement) => {
  const queryClient = createTestQueryClient();
  return render(
    <QueryClientProvider client={queryClient}>
      {component}
    </QueryClientProvider>
  );
};

describe('DepartmentForm', () => {
  const mockOnSuccess = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders create form correctly', () => {
    renderWithQueryClient(<DepartmentForm onSuccess={mockOnSuccess} />);

    expect(screen.getByLabelText(/department name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/description/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/parent department/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /create department/i })).toBeInTheDocument();
  });

  it('renders edit form correctly', () => {
    const department: Department = {
      id: 1,
      name: 'Engineering',
      description: 'Software development team',
      parentId: undefined,
      children: [],
      employeeCount: 10,
      level: 1,
      path: '/Engineering',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z',
    };

    renderWithQueryClient(<DepartmentForm department={department} onSuccess={mockOnSuccess} />);

    expect(screen.getByDisplayValue('Engineering')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Software development team')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /update department/i })).toBeInTheDocument();
  });

  it('validates required fields', async () => {
    renderWithQueryClient(<DepartmentForm onSuccess={mockOnSuccess} />);

    const submitButton = screen.getByRole('button', { name: /create department/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/department name is required/i)).toBeInTheDocument();
    });
  });

  it('validates minimum length', async () => {
    renderWithQueryClient(<DepartmentForm onSuccess={mockOnSuccess} />);

    const nameInput = screen.getByLabelText(/department name/i);
    fireEvent.change(nameInput, { target: { value: 'A' } });

    const submitButton = screen.getByRole('button', { name: /create department/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/department name must be at least 2 characters/i)).toBeInTheDocument();
    });
  });
});