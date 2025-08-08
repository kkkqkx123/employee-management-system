/**
 * Performance monitoring and optimization utilities
 */

// Performance metrics interface
export interface PerformanceMetrics {
  loadTime: number;
  renderTime: number;
  interactionTime: number;
  memoryUsage?: number;
  bundleSize?: number;
}

// Performance observer for monitoring
class PerformanceMonitor {
  private metrics: PerformanceMetrics[] = [];
  private observer: PerformanceObserver | null = null;

  constructor() {
    this.initializeObserver();
  }

  private initializeObserver() {
    if (typeof window === 'undefined' || !window.PerformanceObserver) {
      return;
    }

    try {
      this.observer = new PerformanceObserver((list) => {
        const entries = list.getEntries();
        entries.forEach((entry) => {
          this.processEntry(entry);
        });
      });

      // Observe different types of performance entries
      this.observer.observe({ entryTypes: ['navigation', 'paint', 'measure', 'resource'] });
    } catch (error) {
      console.warn('Performance monitoring not available:', error);
    }
  }

  private processEntry(entry: PerformanceEntry) {
    switch (entry.entryType) {
      case 'navigation':
        this.handleNavigationEntry(entry as PerformanceNavigationTiming);
        break;
      case 'paint':
        this.handlePaintEntry(entry as PerformancePaintTiming);
        break;
      case 'measure':
        this.handleMeasureEntry(entry);
        break;
      case 'resource':
        this.handleResourceEntry(entry as PerformanceResourceTiming);
        break;
    }
  }

  private handleNavigationEntry(entry: PerformanceNavigationTiming) {
    const metrics: PerformanceMetrics = {
      loadTime: entry.loadEventEnd - entry.navigationStart,
      renderTime: entry.domContentLoadedEventEnd - entry.navigationStart,
      interactionTime: entry.domInteractive - entry.navigationStart,
    };

    this.metrics.push(metrics);
    this.reportMetrics('navigation', metrics);
  }

  private handlePaintEntry(entry: PerformancePaintTiming) {
    if (entry.name === 'first-contentful-paint') {
      this.reportMetrics('fcp', { renderTime: entry.startTime } as PerformanceMetrics);
    }
  }

  private handleMeasureEntry(entry: PerformanceEntry) {
    this.reportMetrics('measure', {
      loadTime: entry.duration,
      renderTime: entry.duration,
      interactionTime: 0,
    });
  }

  private handleResourceEntry(entry: PerformanceResourceTiming) {
    // Monitor large resources
    if (entry.transferSize > 100000) { // > 100KB
      console.warn(`Large resource detected: ${entry.name} (${entry.transferSize} bytes)`);
    }
  }

  private reportMetrics(type: string, metrics: PerformanceMetrics) {
    // In production, send to analytics service
    if (process.env.NODE_ENV === 'production') {
      // Example: analytics.track('performance', { type, ...metrics });
    } else {
      console.log(`Performance [${type}]:`, metrics);
    }
  }

  // Get current metrics
  getMetrics(): PerformanceMetrics[] {
    return [...this.metrics];
  }

  // Clear metrics
  clearMetrics() {
    this.metrics = [];
  }

  // Disconnect observer
  disconnect() {
    if (this.observer) {
      this.observer.disconnect();
    }
  }
}

// Create global instance
export const performanceMonitor = new PerformanceMonitor();

// Utility functions for performance optimization
export const performanceUtils = {
  // Measure component render time
  measureRender: (componentName: string, renderFn: () => void) => {
    const startTime = performance.now();
    renderFn();
    const endTime = performance.now();
    const duration = endTime - startTime;
    
    console.log(`${componentName} render time: ${duration.toFixed(2)}ms`);
    return duration;
  },

  // Debounce function for performance
  debounce: <T extends (...args: any[]) => any>(
    func: T,
    wait: number
  ): ((...args: Parameters<T>) => void) => {
    let timeout: NodeJS.Timeout;
    return (...args: Parameters<T>) => {
      clearTimeout(timeout);
      timeout = setTimeout(() => func(...args), wait);
    };
  },

  // Throttle function for performance
  throttle: <T extends (...args: any[]) => any>(
    func: T,
    limit: number
  ): ((...args: Parameters<T>) => void) => {
    let inThrottle: boolean;
    return (...args: Parameters<T>) => {
      if (!inThrottle) {
        func(...args);
        inThrottle = true;
        setTimeout(() => (inThrottle = false), limit);
      }
    };
  },

  // Memory usage monitoring
  getMemoryUsage: (): number | null => {
    if ('memory' in performance) {
      return (performance as any).memory.usedJSHeapSize;
    }
    return null;
  },

  // Bundle size analysis
  analyzeBundleSize: () => {
    if (typeof window !== 'undefined') {
      const scripts = Array.from(document.querySelectorAll('script[src]'));
      const styles = Array.from(document.querySelectorAll('link[rel="stylesheet"]'));
      
      console.log('Bundle Analysis:');
      console.log(`Scripts: ${scripts.length}`);
      console.log(`Stylesheets: ${styles.length}`);
      
      // Estimate total size (this is approximate)
      const totalResources = scripts.length + styles.length;
      console.log(`Total resources: ${totalResources}`);
    }
  },

  // Check if device has limited resources
  isLowEndDevice: (): boolean => {
    if (typeof navigator === 'undefined') return false;
    
    // Check for device memory API
    if ('deviceMemory' in navigator) {
      return (navigator as any).deviceMemory <= 4; // 4GB or less
    }
    
    // Fallback: check for hardware concurrency
    if ('hardwareConcurrency' in navigator) {
      return navigator.hardwareConcurrency <= 2; // 2 cores or less
    }
    
    return false;
  },

  // Optimize for low-end devices
  optimizeForDevice: () => {
    if (performanceUtils.isLowEndDevice()) {
      console.log('Low-end device detected, applying optimizations');
      
      // Reduce animation duration
      document.documentElement.style.setProperty('--animation-duration', '0.1s');
      
      // Disable non-essential animations
      document.documentElement.classList.add('reduce-motion');
      
      return true;
    }
    
    return false;
  },

  // Preload critical resources
  preloadResource: (url: string, type: 'script' | 'style' | 'image' = 'script') => {
    const link = document.createElement('link');
    link.rel = 'preload';
    link.href = url;
    
    switch (type) {
      case 'script':
        link.as = 'script';
        break;
      case 'style':
        link.as = 'style';
        break;
      case 'image':
        link.as = 'image';
        break;
    }
    
    document.head.appendChild(link);
  },

  // Lazy load images
  lazyLoadImages: () => {
    if ('IntersectionObserver' in window) {
      const imageObserver = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            const img = entry.target as HTMLImageElement;
            if (img.dataset.src) {
              img.src = img.dataset.src;
              img.removeAttribute('data-src');
              imageObserver.unobserve(img);
            }
          }
        });
      });

      document.querySelectorAll('img[data-src]').forEach((img) => {
        imageObserver.observe(img);
      });
    }
  },
};

// React hook for performance monitoring
export const usePerformanceMonitor = () => {
  const [metrics, setMetrics] = React.useState<PerformanceMetrics[]>([]);

  React.useEffect(() => {
    const updateMetrics = () => {
      setMetrics(performanceMonitor.getMetrics());
    };

    // Update metrics periodically
    const interval = setInterval(updateMetrics, 5000);

    return () => {
      clearInterval(interval);
    };
  }, []);

  return {
    metrics,
    clearMetrics: performanceMonitor.clearMetrics.bind(performanceMonitor),
    isLowEndDevice: performanceUtils.isLowEndDevice(),
  };
};

// Initialize performance optimizations
export const initializePerformanceOptimizations = () => {
  // Apply device-specific optimizations
  performanceUtils.optimizeForDevice();
  
  // Initialize lazy loading
  performanceUtils.lazyLoadImages();
  
  // Preload critical resources
  performanceUtils.preloadResource('/api/auth/me');
  
  console.log('Performance optimizations initialized');
};