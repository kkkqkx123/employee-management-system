import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import App from './App';

describe('App', () => {
  it('renders the application title', () => {
    render(<App />);
    expect(screen.getByText('Employee Management System')).toBeInTheDocument();
  });

  it('renders the setup complete message', () => {
    render(<App />);
    expect(screen.getByText('Frontend foundation setup complete!')).toBeInTheDocument();
  });
});