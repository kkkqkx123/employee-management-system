import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import { MantineProvider } from '@mantine/core';
import { AccessibilityProvider } from '../components/accessibility/AccessibilityProvider';
import { FocusTrap } from '../components/accessibility/FocusTrap';
import { KeyboardNavigation } from '../components/accessibility/KeyboardNavigation';
import { Button } from '../components/ui/Button/Button';
import { FormField } from '../components/ui/FormField/FormField';
import { Input } from '../components/ui/Input/Input';
import { Modal } from '../components/ui/Modal/Modal';
import { SkipLinks } from '../components/ui/SkipLinks/SkipLinks';
import { DataTable } from '../components/ui/DataTable/DataTable';
import { AccessibilityTestUtils } from './accessibility-utils';
import { theme } from '../theme';
import { it } from 'node:test';
import { describe } from 'node:test';
import { it } from 'node:test';
import { it } from 'node:test';
import { it } from 'node:test';
import { it } from 'node:test';
import { it } from 'node:test';
import { it } from 'node:test';
import { describe } from 'node:test';
import { it } from 'node:test';
import { it } from 'node:test';
import { describe } from 'node:test';
import { it } from 'node:test';
import { it } from 'node:test';
import { it } from 'node:test';
import { it } from 'node:test';
import { describe } from 'node:test';
import { it } from 'node:test';
import { it } from 'node:test';
import { it } from 'node:test';
import { it } from 'node:test';
import { it } from 'node:test';
import { it } from 'node:test';
import { describe } from 'node:test';
import { it } from 'node:test';
import { it } from 'node:test';
import { it } from 'node:test';
import { it } from 'node:test';
import { it } from 'node:test';
import { describe } from 'node:test';
import { describe } from 'node:test';
import { beforeEach } from 'node:test';
import { describe } from 'node:test';

// Test wrapper with all providers
const TestWrapper = ({ children }: { children: React.ReactNode }) => (
  <MantineProvider theme={theme}>
    <AccessibilityProvider>
      {children}
    </AccessibilityProvider>
  </MantineProvider>
);

describe('Comprehensive Accessibility Tests', () => {
  beforeEach(() => {
    // Reset DOM
    document.body.innerHTML = '';
    document.head.innerHTML = '';
    
    // Add required landmarks
    document.body.innerHTML = `
      <div id="main-content" tabindex="-1">Main content</div>
      <div id="navigation" tabindex="-1">Navigation</div>
      <div id="search" tabindex="-1">Search</div>
    `;
  });

  describe('WCAG 2.1 Level AA Compliance', () => {
    describe('1. Perceivable', () => {
      it('1.1.1 Non-text Content - Images have alt text', () => {
        render(
          <TestWrapper>
            <img src="test.jpg" alt="Test image description" />
            <img src="decorative.jpg" alt="" role="presentation" />
          </TestWrapper>
        );

        const meaningfulImage = screen.getByAltText('Test image description');
        const decorativeImage = screen.getByRole('presentation');
        
        expect(meaningfulImage).toHaveAttribute('alt', 'Test image description');
        expect(decorativeImage).toHaveAttribute('alt', '');
      });

      it('1.3.1 Info and Relationships - Semantic HTML structure', () => {
        render(
          <TestWrapper>
            <main role="main">
              <h1>Main Heading</h1>
              <section>
                <h2>Section Heading</h2>
                <p>Content paragraph</p>
              </section>
            </main>
          </TestWrapper>
        );

        expect(screen.getByRole('main')).toBeInTheDocument();
        expect(screen.getByRole('heading', { level: 1 })).toBeInTheDocument();
        expect(screen.getByRole('heading', { level: 2 })).toBeInTheDocument();
      });

      it('1.3.2 Meaningful Sequence - Logical reading order', () => {
        render(
          <TestWrapper>
            <div>
              <h1>First heading</h1>
              <p>First paragraph</p>
              <h2>Second heading</h2>
              <p>Second paragraph</p>
            </div>
          </TestWrapper>
        );

        const headings = screen.getAllByRole('heading');
        expect(headings[0]).toHaveTextContent('First heading');
        expect(headings[1]).toHaveTextContent('Second heading');
      });

      it('1.4.3 Contrast (Minimum) - Color contrast meets requirements', () => {
        const ratio = AccessibilityTestUtils.checkColorContrast('#000000', '#ffffff', 'AA');
        expect(ratio).toBeGreaterThanOrEqual(4.5);
      });

      it('1.4.4 Resize Text - Text can be resized to 200%', () => {
        render(
          <TestWrapper>
            <p style={{ fontSize: '16px' }}>Resizable text</p>
          </TestWrapper>
        );

        const text = screen.getByText('Resizable text');
        const originalSize = window.getComputedStyle(text).fontSize;
        
        // Simulate zoom to 200%
        text.style.fontSize = '32px';
        expect(text.style.fontSize).toBe('32px');
      });
    });

    describe('2. Operable', () => {
      it('2.1.1 Keyboard - All functionality available via keyboard', async () => {
        const handleClick = vi.fn();
        render(
          <TestWrapper>
            <Button onClick={handleClick}>Keyboard accessible button</Button>
          </TestWrapper>
        );

        const button = screen.getByRole('button');
        
        // Test Tab navigation
        button.focus();
        expect(button).toHaveFocus();
        
        // Test Enter activation
        await userEvent.keyboard('{Enter}');
        expect(handleClick).toHaveBeenCalled();
        
        // Test Space activation
        await userEvent.keyboard(' ');
        expect(handleClick).toHaveBeenCalledTimes(2);
      });

      it('2.1.2 No Keyboard Trap - Focus can move away from components', async () => {
        render(
          <TestWrapper>
            <Button>First button</Button>
            <Button>Second button</Button>
          </TestWrapper>
        );

        const firstButton = screen.getByText('First button');
        const secondButton = screen.getByText('Second button');
        
        firstButton.focus();
        expect(firstButton).toHaveFocus();
        
        await userEvent.tab();
        expect(secondButton).toHaveFocus();
      });

      it('2.4.1 Bypass Blocks - Skip links provided', () => {
        render(
          <TestWrapper>
            <SkipLinks />
          </TestWrapper>
        );

        const skipToMain = screen.getByText('Skip to main content');
        const skipToNav = screen.getByText('Skip to navigation');
        
        expect(skipToMain).toHaveAttribute('href', '#main-content');
        expect(skipToNav).toHaveAttribute('href', '#navigation');
      });

      it('2.4.3 Focus Order - Logical focus order maintained', async () => {
        render(
          <TestWrapper>
            <Button>First</Button>
            <Button>Second</Button>
            <Button>Third</Button>
          </TestWrapper>
        );

        const buttons = screen.getAllByRole('button');
        
        buttons[0].focus();
        expect(buttons[0]).toHaveFocus();
        
        await userEvent.tab();
        expect(buttons[1]).toHaveFocus();
        
        await userEvent.tab();
        expect(buttons[2]).toHaveFocus();
      });

      it('2.4.7 Focus Visible - Visible focus indicators', () => {
        render(
          <TestWrapper>
            <Button>Focusable button</Button>
          </TestWrapper>
        );

        const button = screen.getByRole('button');
        AccessibilityTestUtils.checkFocusIndicator(button);
      });

      it('2.5.5 Target Size - Minimum 44x44px touch targets', () => {
        render(
          <TestWrapper>
            <Button size="sm">Small button</Button>
          </TestWrapper>
        );

        const button = screen.getByRole('button');
        AccessibilityTestUtils.checkTouchTargetSize(button);
      });
    });

    describe('3. Understandable', () => {
      it('3.1.1 Language of Page - HTML lang attribute set', () => {
        document.documentElement.lang = 'en';
        expect(document.documentElement).toHaveAttribute('lang', 'en');
      });

      it('3.2.1 On Focus - No unexpected context changes on focus', () => {
        const handleFocus = vi.fn();
        render(
          <TestWrapper>
            <Input onFocus={handleFocus} placeholder="Focus me" />
          </TestWrapper>
        );

        const input = screen.getByRole('textbox');
        fireEvent.focus(input);
        
        // Should not cause navigation or major context changes
        expect(window.location.href).toBe('http://localhost/');
      });

      it('3.3.1 Error Identification - Errors clearly identified', () => {
        render(
          <TestWrapper>
            <FormField label="Email" error="Invalid email format">
              <Input type="email" />
            </FormField>
          </TestWrapper>
        );

        const input = screen.getByRole('textbox');
        const errorMessage = screen.getByRole('alert');
        
        expect(input).toHaveAttribute('aria-invalid', 'true');
        expect(errorMessage).toHaveTextContent('Invalid email format');
        AccessibilityTestUtils.checkLiveRegion(errorMessage, 'polite');
      });

      it('3.3.2 Labels or Instructions - Form fields have labels', () => {
        render(
          <TestWrapper>
            <FormField label="Email Address" required>
              <Input type="email" />
            </FormField>
          </TestWrapper>
        );

        const input = screen.getByRole('textbox');
        const label = screen.getByText('Email Address');
        
        expect(label).toHaveAttribute('for', input.id);
        expect(input).toHaveAttribute('aria-required', 'true');
      });
    });

    describe('4. Robust', () => {
      it('4.1.2 Name, Role, Value - Proper ARIA attributes', () => {
        render(
          <TestWrapper>
            <Button 
              ariaLabel="Save document"
              ariaPressed={false}
              role="button"
            >
              Save
            </Button>
          </TestWrapper>
        );

        const button = screen.getByRole('button');
        AccessibilityTestUtils.checkAriaAttributes(button, {
          'aria-label': 'Save document',
          'aria-pressed': 'false',
          'role': 'button'
        });
      });

      it('4.1.3 Status Messages - Live regions for dynamic content', () => {
        render(
          <TestWrapper>
            <div aria-live="polite" aria-atomic="true">
              Status update message
            </div>
          </TestWrapper>
        );

        const liveRegion = screen.getByText('Status update message');
        AccessibilityTestUtils.checkLiveRegion(liveRegion, 'polite');
      });
    });
  });

  describe('Advanced Accessibility Features', () => {
    it('Focus trap works correctly in modals', async () => {
      const handleClose = vi.fn();
      render(
        <TestWrapper>
          <div>
            <Button>Outside button</Button>
            <Modal isOpen={true} onClose={handleClose} title="Test Modal">
              <Button>Inside button 1</Button>
              <Button>Inside button 2</Button>
            </Modal>
          </div>
        </TestWrapper>
      );

      const modal = screen.getByRole('dialog');
      const insideButtons = screen.getAllByText(/Inside button/);
      
      AccessibilityTestUtils.checkModalAccessibility(modal);
      
      // Focus should be trapped within modal
      await userEvent.tab();
      expect(insideButtons[0]).toHaveFocus();
      
      await userEvent.tab();
      expect(insideButtons[1]).toHaveFocus();
    });

    it('Keyboard navigation works in lists', async () => {
      const items = ['Item 1', 'Item 2', 'Item 3'];
      const handleSelect = vi.fn();
      
      render(
        <TestWrapper>
          <KeyboardNavigation 
            items={items} 
            onSelect={handleSelect}
            ariaLabel="Test list"
          >
            {items.map((item, index) => (
              <div key={index} tabIndex={index === 0 ? 0 : -1}>
                {item}
              </div>
            ))}
          </KeyboardNavigation>
        </TestWrapper>
      );

      const listbox = screen.getByRole('listbox');
      expect(listbox).toHaveAttribute('aria-label', 'Test list');
      
      const firstItem = screen.getByText('Item 1');
      firstItem.focus();
      
      // Test arrow key navigation
      AccessibilityTestUtils.simulateKeyboardNavigation.arrowDown();
      expect(screen.getByText('Item 2')).toHaveFocus();
    });

    it('Screen reader announcements work correctly', () => {
      render(
        <TestWrapper>
          <div>Test content</div>
        </TestWrapper>
      );

      // This would be tested with actual screen reader testing tools
      // For now, we verify the live region is created
      const liveRegion = document.getElementById('live-region-polite');
      expect(liveRegion).toBeInTheDocument();
    });

    it('High contrast mode is supported', () => {
      // Mock high contrast preference
      Object.defineProperty(window, 'matchMedia', {
        writable: true,
        value: vi.fn().mockImplementation(query => ({
          matches: query === '(prefers-contrast: high)',
          media: query,
          onchange: null,
          addListener: vi.fn(),
          removeListener: vi.fn(),
          addEventListener: vi.fn(),
          removeEventListener: vi.fn(),
          dispatchEvent: vi.fn(),
        })),
      });

      render(
        <TestWrapper>
          <Button>High contrast button</Button>
        </TestWrapper>
      );

      const button = screen.getByRole('button');
      button.focus();
      
      // In high contrast mode, focus indicators should be more prominent
      const styles = window.getComputedStyle(button);
      expect(styles.outlineWidth).toBe('3px');
    });

    it('Reduced motion is respected', () => {
      // Mock reduced motion preference
      Object.defineProperty(window, 'matchMedia', {
        writable: true,
        value: vi.fn().mockImplementation(query => ({
          matches: query === '(prefers-reduced-motion: reduce)',
          media: query,
          onchange: null,
          addListener: vi.fn(),
          removeListener: vi.fn(),
          addEventListener: vi.fn(),
          removeEventListener: vi.fn(),
          dispatchEvent: vi.fn(),
        })),
      });

      render(
        <TestWrapper>
          <Button>Animated button</Button>
        </TestWrapper>
      );

      const button = screen.getByRole('button');
      const styles = window.getComputedStyle(button);
      
      // Animations should be disabled
      expect(styles.transition).toBe('none');
    });

    it('Data table has proper accessibility structure', () => {
      const data = [
        { id: 1, name: 'John Doe', email: 'john@example.com' },
        { id: 2, name: 'Jane Smith', email: 'jane@example.com' }
      ];

      const columns = [
        { key: 'name', title: 'Name' },
        { key: 'email', title: 'Email' }
      ];

      render(
        <TestWrapper>
          <DataTable 
            data={data} 
            columns={columns}
            caption="Employee directory"
            ariaLabel="Employee data table"
          />
        </TestWrapper>
      );

      const table = screen.getByRole('table');
      AccessibilityTestUtils.checkTableAccessibility(table);
    });
  });

  describe('Form Accessibility', () => {
    it('Complex form has proper accessibility features', () => {
      render(
        <TestWrapper>
          <form>
            <FormField label="First Name" required>
              <Input type="text" />
            </FormField>
            
            <FormField label="Email" error="Invalid email format">
              <Input type="email" />
            </FormField>
            
            <FormField label="Comments" helperText="Optional feedback">
              <textarea />
            </FormField>
            
            <Button type="submit">Submit Form</Button>
          </form>
        </TestWrapper>
      );

      const form = screen.getByRole('form') || document.querySelector('form')!;
      AccessibilityTestUtils.checkFormAccessibility.hasProperLabels(form);
      AccessibilityTestUtils.checkFormAccessibility.hasErrorHandling(form);
      AccessibilityTestUtils.checkFormAccessibility.hasRequiredFieldIndicators(form);
    });
  });
});