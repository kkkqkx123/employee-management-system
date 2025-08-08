import React, { useEffect, useRef, useCallback } from 'react';
import { KeyboardNavigation as KeyboardUtils } from '../../utils/accessibility';

interface KeyboardNavigationProps {
  children: React.ReactNode;
  items: any[];
  onSelect?: (item: any, index: number) => void;
  orientation?: 'horizontal' | 'vertical' | 'grid';
  columnsCount?: number;
  className?: string;
  role?: string;
  ariaLabel?: string;
}

export const KeyboardNavigation: React.FC<KeyboardNavigationProps> = ({
  children,
  items,
  onSelect,
  orientation = 'vertical',
  columnsCount = 1,
  className,
  role = 'listbox',
  ariaLabel,
}) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const [currentIndex, setCurrentIndex] = React.useState(0);

  const handleKeyDown = useCallback((event: KeyboardEvent) => {
    if (!containerRef.current || items.length === 0) return;

    const focusableElements = Array.from(
      containerRef.current.querySelectorAll('[tabindex="0"], [tabindex="-1"]')
    ) as HTMLElement[];

    if (focusableElements.length === 0) return;

    if (orientation === 'grid' && columnsCount > 1) {
      KeyboardUtils.handleGridNavigation(
        event,
        focusableElements,
        currentIndex,
        columnsCount,
        setCurrentIndex
      );
    } else {
      KeyboardUtils.handleListNavigation(
        event,
        focusableElements,
        currentIndex,
        setCurrentIndex,
        orientation
      );
    }

    // Handle selection
    if ((event.key === 'Enter' || event.key === ' ') && onSelect) {
      event.preventDefault();
      onSelect(items[currentIndex], currentIndex);
    }
  }, [items, currentIndex, orientation, columnsCount, onSelect]);

  useEffect(() => {
    const container = containerRef.current;
    if (!container) return;

    container.addEventListener('keydown', handleKeyDown);
    return () => container.removeEventListener('keydown', handleKeyDown);
  }, [handleKeyDown]);

  useEffect(() => {
    // Update tabindex for current focused item
    if (!containerRef.current) return;

    const focusableElements = Array.from(
      containerRef.current.querySelectorAll('[tabindex]')
    ) as HTMLElement[];

    focusableElements.forEach((element, index) => {
      element.setAttribute('tabindex', index === currentIndex ? '0' : '-1');
    });
  }, [currentIndex]);

  return (
    <div
      ref={containerRef}
      className={className}
      role={role}
      aria-label={ariaLabel}
      aria-activedescendant={`item-${currentIndex}`}
    >
      {children}
    </div>
  );
};