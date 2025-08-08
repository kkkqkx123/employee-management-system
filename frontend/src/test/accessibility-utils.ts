import { screen } from '@testing-library/react';
import { getContrastRatio, meetsContrastRequirement } from '../utils/accessibility';

/**
 * Accessibility testing utilities
 */

export const AccessibilityTestUtils = {
  /**
   * Check if element has proper ARIA attributes
   */
  checkAriaAttributes: (element: HTMLElement, expectedAttributes: Record<string, string>) => {
    Object.entries(expectedAttributes).forEach(([attr, value]) => {
      expect(element).toHaveAttribute(attr, value);
    });
  },

  /**
   * Check if element is properly labeled
   */
  checkLabeling: (element: HTMLElement) => {
    const hasAriaLabel = element.hasAttribute('aria-label');
    const hasAriaLabelledBy = element.hasAttribute('aria-labelledby');
    const hasAssociatedLabel = element.id && document.querySelector(`label[for="${element.id}"]`);
    
    expect(hasAriaLabel || hasAriaLabelledBy || hasAssociatedLabel).toBe(true);
  },

  /**
   * Check if element meets minimum touch target size
   */
  checkTouchTargetSize: (element: HTMLElement, minSize = 44) => {
    const rect = element.getBoundingClientRect();
    expect(rect.width).toBeGreaterThanOrEqual(minSize);
    expect(rect.height).toBeGreaterThanOrEqual(minSize);
  },

  /**
   * Check if element has visible focus indicator
   */
  checkFocusIndicator: (element: HTMLElement) => {
    element.focus();
    const styles = window.getComputedStyle(element);
    
    // Check for outline or box-shadow focus indicators
    const hasOutline = styles.outline !== 'none' && styles.outline !== '';
    const hasBoxShadow = styles.boxShadow !== 'none' && styles.boxShadow !== '';
    
    expect(hasOutline || hasBoxShadow).toBe(true);
  },

  /**
   * Check color contrast ratio
   */
  checkColorContrast: (foreground: string, background: string, level: 'AA' | 'AAA' = 'AA') => {
    const ratio = getContrastRatio(foreground, background);
    const meetsRequirement = meetsContrastRequirement(foreground, background, level);
    
    expect(meetsRequirement).toBe(true);
    return ratio;
  },

  /**
   * Check if element is keyboard accessible
   */
  checkKeyboardAccessibility: (element: HTMLElement) => {
    // Element should be focusable
    const isFocusable = element.tabIndex >= 0 || 
                       ['A', 'BUTTON', 'INPUT', 'SELECT', 'TEXTAREA'].includes(element.tagName) ||
                       element.hasAttribute('tabindex');
    
    expect(isFocusable).toBe(true);
  },

  /**
   * Check if live region is properly configured
   */
  checkLiveRegion: (element: HTMLElement, expectedPoliteness: 'polite' | 'assertive' = 'polite') => {
    expect(element).toHaveAttribute('aria-live', expectedPoliteness);
    expect(element).toHaveAttribute('aria-atomic', 'true');
  },

  /**
   * Check semantic structure
   */
  checkSemanticStructure: {
    hasProperHeadingHierarchy: () => {
      const headings = screen.getAllByRole('heading');
      const levels = headings.map(h => parseInt(h.tagName.charAt(1)));
      
      // Check that heading levels don't skip (e.g., h1 -> h3)
      for (let i = 1; i < levels.length; i++) {
        const diff = levels[i] - levels[i - 1];
        expect(diff).toBeLessThanOrEqual(1);
      }
    },

    hasLandmarks: () => {
      // Check for main landmarks
      expect(screen.getByRole('main')).toBeInTheDocument();
    },

    hasProperListStructure: (listElement: HTMLElement) => {
      expect(['UL', 'OL', 'DL'].includes(listElement.tagName)).toBe(true);
      
      if (listElement.tagName === 'UL' || listElement.tagName === 'OL') {
        const listItems = listElement.querySelectorAll('li');
        expect(listItems.length).toBeGreaterThan(0);
      }
    },
  },

  /**
   * Check form accessibility
   */
  checkFormAccessibility: {
    hasProperLabels: (formElement: HTMLElement) => {
      const inputs = formElement.querySelectorAll('input, select, textarea');
      inputs.forEach(input => {
        AccessibilityTestUtils.checkLabeling(input as HTMLElement);
      });
    },

    hasErrorHandling: (formElement: HTMLElement) => {
      const errorElements = formElement.querySelectorAll('[role="alert"], .error');
      errorElements.forEach(error => {
        expect(error).toHaveAttribute('aria-live');
      });
    },

    hasRequiredFieldIndicators: (formElement: HTMLElement) => {
      const requiredInputs = formElement.querySelectorAll('[required], [aria-required="true"]');
      requiredInputs.forEach(input => {
        const hasRequiredIndicator = input.hasAttribute('required') || 
                                   input.getAttribute('aria-required') === 'true';
        expect(hasRequiredIndicator).toBe(true);
      });
    },
  },

  /**
   * Check table accessibility
   */
  checkTableAccessibility: (tableElement: HTMLElement) => {
    expect(tableElement.tagName).toBe('TABLE');
    
    // Check for caption or aria-label
    const hasCaption = tableElement.querySelector('caption');
    const hasAriaLabel = tableElement.hasAttribute('aria-label');
    expect(hasCaption || hasAriaLabel).toBeTruthy();
    
    // Check for proper headers
    const headers = tableElement.querySelectorAll('th');
    expect(headers.length).toBeGreaterThan(0);
    
    // Check header associations
    headers.forEach(header => {
      const hasScope = header.hasAttribute('scope');
      const hasId = header.hasAttribute('id');
      expect(hasScope || hasId).toBe(true);
    });
  },

  /**
   * Check modal accessibility
   */
  checkModalAccessibility: (modalElement: HTMLElement) => {
    expect(modalElement).toHaveAttribute('role', 'dialog');
    expect(modalElement).toHaveAttribute('aria-modal', 'true');
    
    // Should have accessible name
    const hasAriaLabel = modalElement.hasAttribute('aria-label');
    const hasAriaLabelledBy = modalElement.hasAttribute('aria-labelledby');
    expect(hasAriaLabel || hasAriaLabelledBy).toBe(true);
    
    // Should trap focus
    expect(modalElement.tabIndex).toBe(-1);
  },

  /**
   * Simulate keyboard navigation
   */
  simulateKeyboardNavigation: {
    tab: () => {
      const event = new KeyboardEvent('keydown', { key: 'Tab' });
      document.dispatchEvent(event);
    },
    
    shiftTab: () => {
      const event = new KeyboardEvent('keydown', { key: 'Tab', shiftKey: true });
      document.dispatchEvent(event);
    },
    
    enter: () => {
      const event = new KeyboardEvent('keydown', { key: 'Enter' });
      document.activeElement?.dispatchEvent(event);
    },
    
    space: () => {
      const event = new KeyboardEvent('keydown', { key: ' ' });
      document.activeElement?.dispatchEvent(event);
    },
    
    escape: () => {
      const event = new KeyboardEvent('keydown', { key: 'Escape' });
      document.dispatchEvent(event);
    },
    
    arrowDown: () => {
      const event = new KeyboardEvent('keydown', { key: 'ArrowDown' });
      document.activeElement?.dispatchEvent(event);
    },
    
    arrowUp: () => {
      const event = new KeyboardEvent('keydown', { key: 'ArrowUp' });
      document.activeElement?.dispatchEvent(event);
    },
  },
};