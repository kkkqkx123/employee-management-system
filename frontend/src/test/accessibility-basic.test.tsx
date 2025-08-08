import React from 'react';
import { render, screen } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import { FormField } from '../components/ui/FormField/FormField';
import { SkipLinks } from '../components/ui/SkipLinks/SkipLinks';
import { Input } from '../components/ui/Input/Input';
import { theme } from '../theme';

// Test wrapper with MantineProvider
const TestWrapper = ({ children }: { children: React.ReactNode }) => (
  <MantineProvider theme={theme}>
    {children}
  </MantineProvider>
);

describe('Basic Accessibility Tests', () => {
  describe('FormField Component', () => {
    it('should have proper semantic structure', () => {
      render(
        <TestWrapper>
          <FormField label="Test Field" required>
            <Input />
          </FormField>
        </TestWrapper>
      );
      
      const input = screen.getByRole('textbox');
      const label = screen.getByText('Test Field');
      expect(input).toBeInTheDocument();
      expect(label).toBeInTheDocument();
    });

    it('should properly associate label with input', () => {
      render(
        <TestWrapper>
          <FormField label="Email Address" required>
            <Input />
          </FormField>
        </TestWrapper>
      );
      
      const input = screen.getByRole('textbox');
      const label = screen.getByText('Email Address');
      
      expect(input).toHaveAttribute('aria-required', 'true');
      expect(label).toHaveAttribute('for', input.id);
    });

    it('should show error state correctly', () => {
      render(
        <TestWrapper>
          <FormField label="Email" error="Invalid email format">
            <Input />
          </FormField>
        </TestWrapper>
      );
      
      const input = screen.getByRole('textbox');
      const errorMessage = screen.getByRole('alert');
      
      expect(input).toHaveAttribute('aria-invalid', 'true');
      expect(input).toHaveAttribute('aria-describedby', errorMessage.id);
      expect(errorMessage).toHaveAttribute('aria-live', 'polite');
      expect(errorMessage).toHaveTextContent('Invalid email format');
    });

    it('should show required indicator', () => {
      render(
        <TestWrapper>
          <FormField label="Required Field" required>
            <Input />
          </FormField>
        </TestWrapper>
      );
      
      const requiredIndicator = screen.getByText('*');
      expect(requiredIndicator).toHaveAttribute('aria-label', 'required field');
    });
  });

  describe('SkipLinks Component', () => {
    it('should have proper navigation structure', () => {
      render(<SkipLinks />);
      const navigation = screen.getByRole('navigation', { name: 'Skip links' });
      expect(navigation).toBeInTheDocument();
    });

    it('should have skip links with proper hrefs', () => {
      render(<SkipLinks />);
      
      const mainContentLink = screen.getByText('Skip to main content');
      const navigationLink = screen.getByText('Skip to navigation');
      
      expect(mainContentLink).toHaveAttribute('href', '#main-content');
      expect(navigationLink).toHaveAttribute('href', '#navigation');
    });
  });

  describe('Input Component', () => {
    it('should have proper input attributes', () => {
      render(
        <TestWrapper>
          <Input placeholder="Enter text" />
        </TestWrapper>
      );
      
      const input = screen.getByRole('textbox');
      expect(input).toHaveAttribute('placeholder', 'Enter text');
    });

    it('should support disabled state', () => {
      render(
        <TestWrapper>
          <Input disabled />
        </TestWrapper>
      );
      
      const input = screen.getByRole('textbox');
      expect(input).toBeDisabled();
    });
  });

  describe('Accessibility Features', () => {
    it('should create screen reader only content', () => {
      render(
        <div>
          <span className="sr-only">Screen reader only text</span>
          <span>Visible text</span>
        </div>
      );
      
      const srOnlyText = screen.getByText('Screen reader only text');
      expect(srOnlyText).toHaveClass('sr-only');
    });

    it('should support ARIA landmarks', () => {
      render(
        <div>
          <header role="banner">Header content</header>
          <nav role="navigation">Navigation content</nav>
          <main role="main">Main content</main>
        </div>
      );
      
      expect(screen.getByRole('banner')).toBeInTheDocument();
      expect(screen.getByRole('navigation')).toBeInTheDocument();
      expect(screen.getByRole('main')).toBeInTheDocument();
    });

    it('should support live regions', () => {
      render(
        <div aria-live="polite" aria-atomic="true">
          Status update
        </div>
      );
      
      const liveRegion = screen.getByText('Status update');
      expect(liveRegion).toHaveAttribute('aria-live', 'polite');
      expect(liveRegion).toHaveAttribute('aria-atomic', 'true');
    });
  });
});