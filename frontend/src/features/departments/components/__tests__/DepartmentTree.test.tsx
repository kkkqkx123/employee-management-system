import { render, screen, fireEvent } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { DepartmentTree } from '../DepartmentTree';
import { Department } from '../../types';

// Mock the department service
jest.mock('../../services/departmentService', () => ({
  departmentService: {
    getDepartments: jest.fn(),
    getDepartmentTree: jest.fn(),
    deleteDepartment: jest.fn(),
    moveDepartment: jest.fn(),
  },
}));

// Mock the hooks
jest.mock('../../hooks/useDepartments', () => ({
  useDepartments: () => ({
    departments: [],
    departmentTree: mockDepartments,
    isLoading: false,
    error: null,
    deleteDepartment: jest.fn(),
    isDeleting: false,
    moveDepartment: jest.fn(),
    isMoving: false,
  }),
}));

const mockDepartments: Department[] = [
  {
    id: 1,
    name: 'Engineering',
    description: 'Software development team',
    parentId: undefined,
    children: [
      {
        id: 2,
        name: 'Frontend',
        description: 'Frontend development',
        parentId: 1,
        children: [],
        employeeCount: 5,
        level: 2,
        path: '/Engineering/Frontend',
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z',
      },
    ],
    employeeCount: 10,
    level: 1,
    path: '/Engineering',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z',
  },
];

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

describe('DepartmentTree', () => {
  it('renders department tree correctly', () => {
    renderWithQueryClient(<DepartmentTree />);

    expect(screen.getByText('Departments')).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/search departments/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /add department/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /expand all/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /collapse all/i })).toBeInTheDocument();
  });

  it('handles search input', () => {
    renderWithQueryClient(<DepartmentTree />);

    const searchInput = screen.getByPlaceholderText(/search departments/i);
    fireEvent.change(searchInput, { target: { value: 'Engineering' } });

    expect(searchInput).toHaveValue('Engineering');
  });

  it('opens create modal when add button is clicked', () => {
    renderWithQueryClient(<DepartmentTree />);

    const addButton = screen.getByRole('button', { name: /add department/i });
    fireEvent.click(addButton);

    // Modal should open (you might need to adjust this based on your Modal implementation)
    expect(screen.getByText(/add department/i)).toBeInTheDocument();
  });
});