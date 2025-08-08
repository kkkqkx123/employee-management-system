import React, { useState, useRef, useEffect, useCallback } from 'react';
import { Box, Skeleton } from '@mantine/core';
import styles from './OptimizedImage.module.css';

interface OptimizedImageProps {
  src: string;
  alt: string;
  width?: number | string;
  height?: number | string;
  placeholder?: string;
  className?: string;
  lazy?: boolean;
  quality?: number;
  sizes?: string;
  onLoad?: () => void;
  onError?: () => void;
}

export const OptimizedImage: React.FC<OptimizedImageProps> = ({
  src,
  alt,
  width,
  height,
  placeholder,
  className,
  lazy = true,
  quality = 75,
  sizes,
  onLoad,
  onError,
}) => {
  const [isLoaded, setIsLoaded] = useState(false);
  const [isInView, setIsInView] = useState(!lazy);
  const [hasError, setHasError] = useState(false);
  const imgRef = useRef<HTMLImageElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  // Intersection Observer for lazy loading
  useEffect(() => {
    if (!lazy || isInView) return;

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setIsInView(true);
          observer.disconnect();
        }
      },
      {
        rootMargin: '50px', // Start loading 50px before the image comes into view
        threshold: 0.1,
      }
    );

    if (containerRef.current) {
      observer.observe(containerRef.current);
    }

    return () => observer.disconnect();
  }, [lazy, isInView]);

  const handleLoad = useCallback(() => {
    setIsLoaded(true);
    onLoad?.();
  }, [onLoad]);

  const handleError = useCallback(() => {
    setHasError(true);
    onError?.();
  }, [onError]);

  // Generate optimized image URL (this would typically integrate with a CDN or image service)
  const getOptimizedSrc = useCallback((originalSrc: string) => {
    // In a real implementation, you might use services like Cloudinary, ImageKit, or similar
    // For now, we'll just return the original src with quality parameter if it's a supported format
    if (originalSrc.includes('?')) {
      return `${originalSrc}&q=${quality}`;
    }
    return `${originalSrc}?q=${quality}`;
  }, [quality]);

  const optimizedSrc = getOptimizedSrc(src);

  return (
    <div
      ref={containerRef}
      className={`${styles.container} ${className || ''}`}
      style={{ width, height }}
    >
      {!isInView && lazy ? (
        <Skeleton height={height} width={width} />
      ) : (
        <>
          {!isLoaded && !hasError && (
            <div className={styles.placeholder}>
              {placeholder ? (
                <img
                  src={placeholder}
                  alt=""
                  className={styles.placeholderImage}
                />
              ) : (
                <Skeleton height={height} width={width} />
              )}
            </div>
          )}
          
          {hasError ? (
            <div className={styles.errorState}>
              <span>Failed to load image</span>
            </div>
          ) : (
            <img
              ref={imgRef}
              src={optimizedSrc}
              alt={alt}
              width={width}
              height={height}
              sizes={sizes}
              loading={lazy ? 'lazy' : 'eager'}
              decoding="async"
              className={`${styles.image} ${isLoaded ? styles.loaded : ''}`}
              onLoad={handleLoad}
              onError={handleError}
            />
          )}
        </>
      )}
    </div>
  );
};

export default OptimizedImage;