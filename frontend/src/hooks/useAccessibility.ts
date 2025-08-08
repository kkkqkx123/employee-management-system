import { useEffect, useRef, useCallback } from 'react';

/**
 * Hook for managing focus and keyboard navigation
 */
export const useFocusManagement = () => {
  const focusableElementsSelector = [
    'a[href]',
    'button:not([disabled])',
    'textarea:not([disabled])',
    'input:not([disabled])',
    'select:not([disabled])',
    '[tabindex]:not([tabindex="-1"])',
    '[contenteditable="true"]',
    'details > summary',
    'audio[controls]',
    'video[controls]'
  ].join(', ');

  const getFocusableElements = useCallback((container: HTMLElement) => {
    return Array.from(container.querySelectorAll(focusableElementsSelector))
      .filter((element) => {
        const el = element as HTMLElement;
        return el.offsetWidth > 0 && el.offsetHeight > 0 && !el.hidden;
      }) as HTMLElement[];
  }, [focusableElementsSelector]);

  const trapFocus = useCallback((container: HTMLElement) => {
    const focusableElements = getFocusableElements(container);
    const firstElement = focusableElements[0];
    const lastElement = focusableElements[focusableElements.length - 1];

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key !== 'Tab') return;

      if (event.shiftKey) {
        if (document.activeElement === firstElement) {
          event.preventDefault();
          lastElement?.focus();
        }
      } else {
        if (document.activeElement === lastElement) {
          event.preventDefault();
          firstElement?.focus();
        }
      }
    };

    container.addEventListener('keydown', handleKeyDown);
    return () => container.removeEventListener('keydown', handleKeyDown);
  }, [getFocusableElements]);

  const restoreFocus = useCallback((element: HTMLElement | null) => {
    if (element && typeof element.focus === 'function') {
      element.focus();
    }
  }, []);

  return {
    getFocusableElements,
    trapFocus,
    restoreFocus
  };
};

/**
 * Hook for managing ARIA announcements
 */
export const useAnnouncements = () => {
  const announcementRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    // Create announcement region if it doesn't exist
    if (!announcementRef.current) {
      const announcer = document.createElement('div');
      announcer.setAttribute('aria-live', 'polite');
      announcer.setAttribute('aria-atomic', 'true');
      announcer.setAttribute('aria-relevant', 'text');
      announcer.style.position = 'absolute';
      announcer.style.left = '-10000px';
      announcer.style.width = '1px';
      announcer.style.height = '1px';
      announcer.style.overflow = 'hidden';
      announcer.id = 'accessibility-announcer';
      
      document.body.appendChild(announcer);
      announcementRef.current = announcer;
    }

    return () => {
      if (announcementRef.current) {
        document.body.removeChild(announcementRef.current);
        announcementRef.current = null;
      }
    };
  }, []);

  const announce = useCallback((message: string, priority: 'polite' | 'assertive' = 'polite') => {
    if (announcementRef.current) {
      announcementRef.current.setAttribute('aria-live', priority);
      announcementRef.current.textContent = message;
      
      // Clear after announcement to allow repeated announcements
      setTimeout(() => {
        if (announcementRef.current) {
          announcementRef.current.textContent = '';
        }
      }, 1000);
    }
  }, []);

  return { announce };
};

/**
 * Hook for keyboard navigation in lists/grids
 */
export const useKeyboardNavigation = (
  items: any[],
  onSelect?: (item: any, index: number) => void,
  orientation: 'horizontal' | 'vertical' | 'grid' = 'vertical'
) => {
  const currentIndexRef = useRef(0);
  const containerRef = useRef<HTMLElement | null>(null);

  const handleKeyDown = useCallback((event: KeyboardEvent) => {
    if (!items.length) return;

    const { key } = event;
    let newIndex = currentIndexRef.current;

    switch (key) {
      case 'ArrowDown':
        if (orientation === 'vertical' || orientation === 'grid') {
          event.preventDefault();
          newIndex = Math.min(currentIndexRef.current + 1, items.length - 1);
        }
        break;
      case 'ArrowUp':
        if (orientation === 'vertical' || orientation === 'grid') {
          event.preventDefault();
          newIndex = Math.max(currentIndexRef.current - 1, 0);
        }
        break;
      case 'ArrowRight':
        if (orientation === 'horizontal' || orientation === 'grid') {
          event.preventDefault();
          newIndex = Math.min(currentIndexRef.current + 1, items.length - 1);
        }
        break;
      case 'ArrowLeft':
        if (orientation === 'horizontal' || orientation === 'grid') {
          event.preventDefault();
          newIndex = Math.max(currentIndexRef.current - 1, 0);
        }
        break;
      case 'Home':
        event.preventDefault();
        newIndex = 0;
        break;
      case 'End':
        event.preventDefault();
        newIndex = items.length - 1;
        break;
      case 'Enter':
      case ' ':
        event.preventDefault();
        onSelect?.(items[currentIndexRef.current], currentIndexRef.current);
        return;
    }

    if (newIndex !== currentIndexRef.current) {
      currentIndexRef.current = newIndex;
      
      // Focus the new item
      if (containerRef.current) {
        const focusableElements = containerRef.current.querySelectorAll('[tabindex="0"], [tabindex="-1"]');
        const targetElement = focusableElements[newIndex] as HTMLElement;
        if (targetElement) {
          targetElement.focus();
        }
      }
    }
  }, [items, onSelect, orientation]);

  const setActiveIndex = useCallback((index: number) => {
    currentIndexRef.current = Math.max(0, Math.min(index, items.length - 1));
  }, [items.length]);

  return {
    currentIndex: currentIndexRef.current,
    setActiveIndex,
    handleKeyDown,
    containerRef
  };
};

/**
 * Hook for managing reduced motion preferences
 */
export const useReducedMotion = () => {
  const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  
  return {
    prefersReducedMotion,
    getAnimationDuration: (defaultDuration: number) => 
      prefersReducedMotion ? 0 : defaultDuration
  };
};

/**
 * Hook for managing high contrast mode
 */
export const useHighContrast = () => {
  const prefersHighContrast = window.matchMedia('(prefers-contrast: high)').matches;
  
  return {
    prefersHighContrast
  };
};