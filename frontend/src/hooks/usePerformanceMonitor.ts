import { useEffect, useRef, useCallback } from 'react';

interface PerformanceMetrics {
  renderTime: number;
  componentName: string;
  timestamp: number;
}

interface UsePerformanceMonitorOptions {
  enabled?: boolean;
  threshold?: number; // Log renders that take longer than this (ms)
  onSlowRender?: (metrics: PerformanceMetrics) => void;
}

export const usePerformanceMonitor = (
  componentName: string,
  options: UsePerformanceMonitorOptions = {}
) => {
  const { enabled = process.env.NODE_ENV === 'development', threshold = 16, onSlowRender } = options;
  const renderStartTime = useRef<number>(0);
  const renderCount = useRef<number>(0);

  const startRender = useCallback(() => {
    if (!enabled) return;
    renderStartTime.current = performance.now();
  }, [enabled]);

  const endRender = useCallback(() => {
    if (!enabled || renderStartTime.current === 0) return;

    const renderTime = performance.now() - renderStartTime.current;
    renderCount.current += 1;

    const metrics: PerformanceMetrics = {
      renderTime,
      componentName,
      timestamp: Date.now(),
    };

    if (renderTime > threshold) {
      console.warn(
        `🐌 Slow render detected in ${componentName}: ${renderTime.toFixed(2)}ms (render #${renderCount.current})`
      );
      onSlowRender?.(metrics);
    }

    // Log performance metrics in development
    if (process.env.NODE_ENV === 'development') {
      console.debug(`⚡ ${componentName} render: ${renderTime.toFixed(2)}ms`);
    }

    renderStartTime.current = 0;
  }, [enabled, threshold, componentName, onSlowRender]);

  // Start timing on every render
  useEffect(() => {
    startRender();
    return endRender;
  });

  return {
    renderCount: renderCount.current,
    startRender,
    endRender,
  };
};

// Hook for monitoring bundle size and performance metrics
export const useBundleMetrics = () => {
  useEffect(() => {
    if (typeof window === 'undefined' || process.env.NODE_ENV !== 'development') return;

    // Monitor bundle size
    const observer = new PerformanceObserver((list) => {
      const entries = list.getEntries();
      entries.forEach((entry) => {
        if (entry.entryType === 'navigation') {
          const navEntry = entry as PerformanceNavigationTiming;
          console.group('📊 Performance Metrics');
          console.log(`DOM Content Loaded: ${navEntry.domContentLoadedEventEnd - navEntry.domContentLoadedEventStart}ms`);
          console.log(`Load Complete: ${navEntry.loadEventEnd - navEntry.loadEventStart}ms`);
          console.log(`First Paint: ${navEntry.responseEnd - navEntry.requestStart}ms`);
          console.groupEnd();
        }
      });
    });

    observer.observe({ entryTypes: ['navigation'] });

    return () => observer.disconnect();
  }, []);
};

// Hook for monitoring memory usage
export const useMemoryMonitor = (interval: number = 30000) => {
  useEffect(() => {
    if (typeof window === 'undefined' || !('memory' in performance) || process.env.NODE_ENV !== 'development') {
      return;
    }

    const checkMemory = () => {
      const memory = (performance as any).memory;
      if (memory) {
        const used = Math.round(memory.usedJSHeapSize / 1048576);
        const total = Math.round(memory.totalJSHeapSize / 1048576);
        const limit = Math.round(memory.jsHeapSizeLimit / 1048576);
        
        console.log(`🧠 Memory Usage: ${used}MB / ${total}MB (Limit: ${limit}MB)`);
        
        // Warn if memory usage is high
        if (used / limit > 0.8) {
          console.warn('⚠️ High memory usage detected!');
        }
      }
    };

    const intervalId = setInterval(checkMemory, interval);
    return () => clearInterval(intervalId);
  }, [interval]);
};