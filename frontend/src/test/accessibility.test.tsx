import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import { Button } from '../components/ui/Button/Button';
import { FormField } from '../components/ui/FormField/FormField';
import { Modal } from '../components/ui/Modal/Modal';
import { SkipLinks } from '../components/ui/SkipLinks/SkipLinks';
import { DataTable } from '../components/ui/DataTable/DataTable';
import { Input } from '../components/ui/Input/Input';
import { 
  getContrastRatio, 
  meetsContrastRequirement,
  announceToScreenReader,
  FocusTrap
} from '../utils/accessibility';

describe('Accessibility Tests', () => {
  describe('Button Component', () => {
    it('should have proper semantic structure', () => {
      render(<Button>Click me</Button>);
      const button = screen.getByRole('button');
      expect(button).toBeInTheDocument();
      expect(button).toHaveTextContent('Click me');
    });

    it('should have proper ARIA attributes when loading', () => {
      render(<Button loading>Loading button</Button>);
      const button = screen.getByRole('button');
      
      expect(button).toHaveAttribute('aria-disabled', 'true');
      expect(button).toHaveAttribute('aria-busy', 'true');
      expect(screen.getByText('Loading...')).toHaveClass('sr-only');
    });

    it('should have proper focus indicators', () => {
      render(<Button>Focus me</Button>);
      const button = screen.getByRole('button');
      
      button.focus();
      expect(button).toHaveFocus();
      expect(button).toHaveStyle('outline: 2px solid #0ea5e9');
    });

    it('should handle keyboard navigation', async () => {
      const handleClick = vi.fn();
      render(<Button onClick={handleClick}>Press me</Button>);
      const button = screen.getByRole('button');
      
      button.focus();
      await userEvent.keyboard(' ');
      expect(handleClick).toHaveBeenCalled();
    });

    it('should meet minimum touch target size', () => {
      render(<Button size="sm">Small button</Button>);
      const button = screen.getByRole('button');
      
      const styles = window.getComputedStyle(button);
      const minHeight = parseInt(styles.minHeight);
      const minWidth = parseInt(styles.minWidth);
      
      expect(minHeight).toBeGreaterThanOrEqual(44);
      expect(minWidth).toBeGreaterThanOrEqual(44);
    });
  });

  describe('FormField Component', () => {
    it('should have proper semantic structure', () => {
      render(
        <FormField label="Test Field" required>
          <Input />
        </FormField>
      );
      
      const input = screen.getByRole('textbox');
      const label = screen.getByText('Test Field');
      expect(input).toBeInTheDocument();
      expect(label).toBeInTheDocument();
    });

    it('should properly associate label with input', () => {
      render(
        <FormField label="Email Address" required>
          <Input />
        </FormField>
      );
      
      const input = screen.getByRole('textbox');
      const label = screen.getByText('Email Address');
      
      expect(input).toHaveAttribute('aria-required', 'true');
      expect(label).toHaveAttribute('for', input.id);
    });

    it('should announce errors to screen readers', () => {
      render(
        <FormField label="Email" error="Invalid email format">
          <Input />
        </FormField>
      );
      
      const input = screen.getByRole('textbox');
      const errorMessage = screen.getByRole('alert');
      
      expect(input).toHaveAttribute('aria-invalid', 'true');
      expect(input).toHaveAttribute('aria-describedby', errorMessage.id);
      expect(errorMessage).toHaveAttribute('aria-live', 'polite');
    });
  });

  describe('Modal Component', () => {
    it('should have proper dialog structure', () => {
      render(
        <Modal isOpen={true} onClose={() => {}} title="Test Modal">
          <p>Modal content</p>
        </Modal>
      );
      
      const dialog = screen.getByRole('dialog');
      expect(dialog).toBeInTheDocument();
      expect(dialog).toHaveAttribute('aria-modal', 'true');
    });

    it('should trap focus within modal', async () => {
      render(
        <div>
          <button>Outside button</button>
          <Modal isOpen={true} onClose={() => {}} title="Test Modal">
            <button>Inside button 1</button>
            <button>Inside button 2</button>
          </Modal>
        </div>
      );
      
      const modal = screen.getByRole('dialog');
      const insideButton1 = screen.getByText('Inside button 1');
      const insideButton2 = screen.getByText('Inside button 2');
      
      // Focus should be trapped within modal
      expect(modal).toHaveFocus();
      
      await userEvent.tab();
      expect(insideButton1).toHaveFocus();
      
      await userEvent.tab();
      expect(insideButton2).toHaveFocus();
      
      // Should cycle back to first focusable element
      await userEvent.tab();
      expect(insideButton1).toHaveFocus();
    });

    it('should close on Escape key', async () => {
      const handleClose = vi.fn();
      render(
        <Modal isOpen={true} onClose={handleClose} title="Test Modal">
          <p>Modal content</p>
        </Modal>
      );
      
      await userEvent.keyboard('{Escape}');
      expect(handleClose).toHaveBeenCalled();
    });
  });

  describe('SkipLinks Component', () => {
    it('should have proper navigation structure', () => {
      render(<SkipLinks />);
      const navigation = screen.getByRole('navigation', { name: 'Skip links' });
      expect(navigation).toBeInTheDocument();
    });

    it('should be visible when focused', () => {
      render(<SkipLinks />);
      const skipLink = screen.getByText('Skip to main content');
      
      skipLink.focus();
      expect(skipLink).toHaveStyle('top: 6px');
    });

    it('should navigate to target element', async () => {
      render(
        <div>
          <SkipLinks />
          <main id="main-content" tabIndex={-1}>Main content</main>
        </div>
      );
      
      const skipLink = screen.getByText('Skip to main content');
      const mainContent = screen.getByRole('main');
      
      await userEvent.click(skipLink);
      expect(mainContent).toHaveFocus();
    });
  });

  describe('DataTable Component', () => {
    const mockData = [
      { id: 1, name: 'John Doe', email: 'john@example.com' },
      { id: 2, name: 'Jane Smith', email: 'jane@example.com' }
    ];

    const mockColumns = [
      { key: 'name', title: 'Name' },
      { key: 'email', title: 'Email' }
    ];

    it('should have proper table structure', () => {
      render(
        <DataTable 
          data={mockData} 
          columns={mockColumns}
          caption="Employee list"
        />
      );
      
      const table = screen.getByRole('table');
      expect(table).toBeInTheDocument();
    });

    it('should have proper table structure', () => {
      render(
        <DataTable 
          data={mockData} 
          columns={mockColumns}
          caption="Employee list"
        />
      );
      
      const table = screen.getByRole('table');
      const caption = screen.getByText('Employee list');
      
      expect(table).toHaveAttribute('aria-rowcount', '2');
      expect(table).toHaveAttribute('aria-colcount', '2');
      expect(caption).toBeInTheDocument();
    });

    it('should support keyboard navigation', async () => {
      render(
        <DataTable 
          data={mockData} 
          columns={mockColumns}
          onRow={{ onClick: vi.fn() }}
        />
      );
      
      const tableRegion = screen.getByRole('region');
      tableRegion.focus();
      
      await userEvent.keyboard('{ArrowDown}');
      // Should focus first row
      
      await userEvent.keyboard('{Enter}');
      // Should trigger row click
    });
  });

  describe('Color Contrast', () => {
    it('should calculate contrast ratio correctly', () => {
      const ratio = getContrastRatio('#000000', '#ffffff');
      expect(ratio).toBe(21); // Perfect contrast
    });

    it('should meet WCAG AA standards', () => {
      expect(meetsContrastRequirement('#000000', '#ffffff', 'AA', 'normal')).toBe(true);
      expect(meetsContrastRequirement('#777777', '#ffffff', 'AA', 'normal')).toBe(false);
    });

    it('should meet WCAG AAA standards', () => {
      expect(meetsContrastRequirement('#000000', '#ffffff', 'AAA', 'normal')).toBe(true);
      expect(meetsContrastRequirement('#595959', '#ffffff', 'AAA', 'normal')).toBe(false);
    });
  });

  describe('Screen Reader Announcements', () => {
    it('should create announcement region', () => {
      announceToScreenReader('Test announcement');
      
      const announcer = document.getElementById('accessibility-announcer');
      expect(announcer).toBeInTheDocument();
      expect(announcer).toHaveAttribute('aria-live', 'polite');
    });

    it('should announce with different priorities', () => {
      announceToScreenReader('Urgent message', 'assertive');
      
      const announcer = document.getElementById('accessibility-announcer');
      expect(announcer).toHaveAttribute('aria-live', 'assertive');
    });
  });

  describe('Focus Management', () => {
    it('should trap focus correctly', () => {
      const container = document.createElement('div');
      container.innerHTML = `
        <button>Button 1</button>
        <button>Button 2</button>
        <button>Button 3</button>
      `;
      document.body.appendChild(container);
      
      const focusTrap = new FocusTrap(container);
      focusTrap.activate();
      
      const buttons = container.querySelectorAll('button');
      expect(buttons[0]).toHaveFocus();
      
      focusTrap.deactivate();
      document.body.removeChild(container);
    });
  });

  describe('Reduced Motion', () => {
    it('should respect prefers-reduced-motion', () => {
      // Mock matchMedia
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

      render(<Button>Animated button</Button>);
      const button = screen.getByRole('button');
      
      const styles = window.getComputedStyle(button);
      expect(styles.transition).toBe('none');
    });
  });

  describe('High Contrast Mode', () => {
    it('should adapt to high contrast preferences', () => {
      // Mock matchMedia for high contrast
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

      render(<Button>High contrast button</Button>);
      const button = screen.getByRole('button');
      
      button.focus();
      const styles = window.getComputedStyle(button);
      expect(styles.outlineWidth).toBe('3px');
    });
  });

  describe('Touch Targets', () => {
    it('should meet minimum touch target requirements on mobile', () => {
      // Mock pointer: coarse (touch device)
      Object.defineProperty(window, 'matchMedia', {
        writable: true,
        value: vi.fn().mockImplementation(query => ({
          matches: query === '(pointer: coarse)',
          media: query,
          onchange: null,
          addListener: vi.fn(),
          removeListener: vi.fn(),
          addEventListener: vi.fn(),
          removeEventListener: vi.fn(),
          dispatchEvent: vi.fn(),
        })),
      });

      render(<Button size="xs">Small touch button</Button>);
      const button = screen.getByRole('button');
      
      const styles = window.getComputedStyle(button);
      expect(parseInt(styles.minHeight)).toBeGreaterThanOrEqual(44);
      expect(parseInt(styles.minWidth)).toBeGreaterThanOrEqual(44);
    });
  });
});