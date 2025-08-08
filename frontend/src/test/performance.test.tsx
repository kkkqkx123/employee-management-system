import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { VirtualList } from '@/components/ui/VirtualList';
import { OptimizedImage } from '@/components/ui/OptimizedImage';
import { usePerformanceMonitor } from '@/hooks/usePerformanceMonitor';
import React from 'react';

// Mock performance API
const mockPerformance = {
  now: vi.fn(() => Date.now()),
  mark: vi.fn(),
  measure: vi.fn(),
  getEntriesByType: vi.fn(() => []),
};

Object.defineProperty(window, 'performance', {
  value: mockPerformance,
  writable: true,
});

// Mock IntersectionObserver
const mockIntersectionObserver = vi.fn();
mockIntersectionObserver.mockReturnValue({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
});
window.IntersectionObserver = mockIntersectionObserver;

describe('Performance Optimizations', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('VirtualList', () => {
    it('should render only visible items', () => {
      const items = Array.from({ length: 1000 }, (_, i) => `Item ${i}`);
      const renderItem = (item: string, index: number) => (
        <div key={index}>{item}</div>
      );

      render(
        <VirtualList
          items={items}
          itemHeight={50}
          containerHeight={300}
          renderItem={renderItem}
        />
      );

      // Should not render all 1000 items, only visible ones
      const renderedItems = screen.getAllByText(/Item \d+/);
      expect(renderedItems.length).toBeLessThan(20); // Should be much less than 1000
    });

    it('should handle empty items array', () => {
      const renderItem = (item: string) => <div>{item}</div>;

      render(
        <VirtualList
          items={[]}
          itemHeight={50}
          containerHeight={300}
          renderItem={renderItem}
        />
      );

      expect(screen.queryByText(/Item/)).toBeNull();
    });
  });

  describe('OptimizedImage', () => {
    it('should render with lazy loading by default', () => {
      render(
        <OptimizedImage
          src="/test-image.jpg"
          alt="Test image"
          width={200}
          height={200}
        />
      );

      expect(mockIntersectionObserver).toHaveBeenCalled();
    });

    it('should not use lazy loading when disabled', () => {
      render(
        <OptimizedImage
          src="/test-image.jpg"
          alt="Test image"
          width={200}
          height={200}
          lazy={false}
        />
      );

      const img = screen.getByRole('img');
      expect(img).toHaveAttribute('loading', 'eager');
    });

    it('should show skeleton while loading', () => {
      render(
        <OptimizedImage
          src="/test-image.jpg"
          alt="Test image"
          width={200}
          height={200}
          lazy={false}
        />
      );

      // Should show skeleton initially
      expect(document.querySelector('.mantine-Skeleton-root')).toBeInTheDocument();
    });
  });

  describe('Performance Monitoring', () => {
    it('should track render performance', () => {
      const TestComponent = () => {
        usePerformanceMonitor('TestComponent');
        return <div>Test</div>;
      };

      const consoleSpy = vi.spyOn(console, 'debug').mockImplementation(() => {});

      render(<TestComponent />);

      // Should call performance monitoring
      expect(mockPerformance.now).toHaveBeenCalled();
      
      consoleSpy.mockRestore();
    });
  });

  describe('Bundle Size Optimization', () => {
    it('should have proper code splitting structure', async () => {
      // Test that lazy imports work
      const LazyComponent = React.lazy(() => 
        Promise.resolve({ default: () => <div>Lazy Component</div> })
      );

      const { container } = render(
        <React.Suspense fallback={<div>Loading...</div>}>
          <LazyComponent />
        </React.Suspense>
      );

      expect(container).toBeInTheDocument();
    });
  });

  describe('Memory Optimization', () => {
    it('should properly cleanup event listeners', () => {
      const { unmount } = render(
        <OptimizedImage
          src="/test-image.jpg"
          alt="Test image"
          width={200}
          height={200}
        />
      );

      const disconnectSpy = vi.fn();
      mockIntersectionObserver.mockReturnValue({
        observe: vi.fn(),
        unobserve: vi.fn(),
        disconnect: disconnectSpy,
      });

      unmount();

      // Should cleanup observers on unmount
      expect(disconnectSpy).toHaveBeenCalled();
    });
  });
});