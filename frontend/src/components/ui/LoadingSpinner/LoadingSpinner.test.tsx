import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { LoadingSpinner } from './LoadingSpinner';

describe('LoadingSpinner', () => {
  it('renders spinner', () => {
    render(<LoadingSpinner />);
    // Mantine Loader doesn't have a specific role, so we check for the container
    expect(document.querySelector('.mantine-Loader-root')).toBeInTheDocument();
  });

  it('shows loading text', () => {
    render(<LoadingSpinner text="Loading data..." />);
    expect(screen.getByText('Loading data...')).toBeInTheDocument();
  });

  it('centers spinner when centered prop is true', () => {
    render(<LoadingSpinner centered />);
    expect(document.querySelector('.mantine-Center-root')).toBeInTheDocument();
  });

  it('applies custom className', () => {
    const { container } = render(<LoadingSpinner className="custom-spinner" />);
    expect(container.firstChild).toHaveClass('custom-spinner');
  });
});