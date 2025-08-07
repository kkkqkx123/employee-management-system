import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { MantineProvider } from '@mantine/core';
import { Breadcrumbs } from '@/components/layout/Breadcrumbs';
import { ROUTES } from '@/constants';

const createTestWrapper = (initialEntries = ['/']) => {
  return ({ children }: { children: React.ReactNode }) => (
    <MantineProvider>
      <MemoryRouter initialEntries={initialEntries}>
        {children}
      </MemoryRouter>
    </MantineProvider>
  );
};

describe('Breadcrumbs', () => {
  it('does not render for root path', () => {
    const TestWrapper = createTestWrapper(['/']);
    const { container } = render(<Breadcrumbs />, { wrapper: TestWrapper });
    
    expect(container.firstChild).toBeNull();
  });

  it('does not render for single-level paths', () => {
    const TestWrapper = createTestWrapper([ROUTES.DASHBOARD]);
    const { container } = render(<Breadcrumbs />, { wrapper: TestWrapper });
    
    expect(container.firstChild).toBeNull();
  });

  it('renders breadcrumbs for nested paths', () => {
    const TestWrapper = createTestWrapper(['/employees/123']);
    render(<Breadcrumbs />, { wrapper: TestWrapper });
    
    expect(screen.getByText('Employees')).toBeInTheDocument();
    expect(screen.getByText('123')).toBeInTheDocument();
  });

  it('renders clickable links for non-last breadcrumb items', () => {
    const TestWrapper = createTestWrapper(['/employees/123/edit']);
    render(<Breadcrumbs />, { wrapper: TestWrapper });
    
    const employeesLink = screen.getByText('Employees');
    const idLink = screen.getByText('123');
    const editText = screen.getByText('Edit');
    
    expect(employeesLink.closest('a')).toHaveAttribute('href', '/employees');
    expect(idLink.closest('a')).toHaveAttribute('href', '/employees/123');
    expect(editText.closest('a')).toBeNull(); // Last item should not be a link
  });

  it('uses route labels for known routes', () => {
    const TestWrapper = createTestWrapper(['/employees/details']);
    render(<Breadcrumbs />, { wrapper: TestWrapper });
    
    expect(screen.getByText('Employees')).toBeInTheDocument();
    expect(screen.getByText('Details')).toBeInTheDocument();
  });

  it('capitalizes unknown route segments', () => {
    const TestWrapper = createTestWrapper(['/unknown/route']);
    render(<Breadcrumbs />, { wrapper: TestWrapper });
    
    expect(screen.getByText('Unknown')).toBeInTheDocument();
    expect(screen.getByText('Route')).toBeInTheDocument();
  });
});