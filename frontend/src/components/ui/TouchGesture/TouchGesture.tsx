import React, { useRef, useCallback, ReactNode } from 'react';
import { useTouch } from '@/hooks';

export interface TouchGestureProps {
  /** Content to wrap with touch gesture support */
  children: ReactNode;
  /** Callback for swipe left gesture */
  onSwipeLeft?: () => void;
  /** Callback for swipe right gesture */
  onSwipeRight?: () => void;
  /** Callback for swipe up gesture */
  onSwipeUp?: () => void;
  /** Callback for swipe down gesture */
  onSwipeDown?: () => void;
  /** Minimum distance for swipe detection (default: 50px) */
  swipeThreshold?: number;
  /** Maximum time for swipe detection (default: 300ms) */
  swipeTimeout?: number;
  /** Whether to prevent default touch behavior */
  preventDefault?: boolean;
  /** CSS class name */
  className?: string;
}

interface TouchState {
  startX: number;
  startY: number;
  startTime: number;
  isTracking: boolean;
}

/**
 * TouchGesture component for handling touch gestures on mobile devices
 * Provides swipe detection in all four directions
 */
export const TouchGesture: React.FC<TouchGestureProps> = ({
  children,
  onSwipeLeft,
  onSwipeRight,
  onSwipeUp,
  onSwipeDown,
  swipeThreshold = 50,
  swipeTimeout = 300,
  preventDefault = false,
  className,
}) => {
  const { isTouchDevice } = useTouch();
  const touchState = useRef<TouchState>({
    startX: 0,
    startY: 0,
    startTime: 0,
    isTracking: false,
  });

  const handleTouchStart = useCallback((e: React.TouchEvent) => {
    if (!isTouchDevice) return;
    
    const touch = e.touches[0];
    touchState.current = {
      startX: touch.clientX,
      startY: touch.clientY,
      startTime: Date.now(),
      isTracking: true,
    };

    if (preventDefault) {
      e.preventDefault();
    }
  }, [isTouchDevice, preventDefault]);

  const handleTouchMove = useCallback((e: React.TouchEvent) => {
    if (!isTouchDevice || !touchState.current.isTracking) return;

    if (preventDefault) {
      e.preventDefault();
    }
  }, [isTouchDevice, preventDefault]);

  const handleTouchEnd = useCallback((e: React.TouchEvent) => {
    if (!isTouchDevice || !touchState.current.isTracking) return;

    const touch = e.changedTouches[0];
    const { startX, startY, startTime } = touchState.current;
    
    const endX = touch.clientX;
    const endY = touch.clientY;
    const endTime = Date.now();
    
    const deltaX = endX - startX;
    const deltaY = endY - startY;
    const deltaTime = endTime - startTime;
    
    // Reset tracking
    touchState.current.isTracking = false;
    
    // Check if gesture is within time threshold
    if (deltaTime > swipeTimeout) return;
    
    // Check if gesture meets distance threshold
    const absX = Math.abs(deltaX);
    const absY = Math.abs(deltaY);
    
    if (absX < swipeThreshold && absY < swipeThreshold) return;
    
    // Determine primary direction
    if (absX > absY) {
      // Horizontal swipe
      if (deltaX > 0 && onSwipeRight) {
        onSwipeRight();
      } else if (deltaX < 0 && onSwipeLeft) {
        onSwipeLeft();
      }
    } else {
      // Vertical swipe
      if (deltaY > 0 && onSwipeDown) {
        onSwipeDown();
      } else if (deltaY < 0 && onSwipeUp) {
        onSwipeUp();
      }
    }

    if (preventDefault) {
      e.preventDefault();
    }
  }, [
    isTouchDevice,
    onSwipeLeft,
    onSwipeRight,
    onSwipeUp,
    onSwipeDown,
    swipeThreshold,
    swipeTimeout,
    preventDefault,
  ]);

  // Only add touch handlers on touch devices
  if (!isTouchDevice) {
    return <div className={className}>{children}</div>;
  }

  return (
    <div
      className={className}
      onTouchStart={handleTouchStart}
      onTouchMove={handleTouchMove}
      onTouchEnd={handleTouchEnd}
      style={{ touchAction: preventDefault ? 'none' : 'auto' }}
    >
      {children}
    </div>
  );
};