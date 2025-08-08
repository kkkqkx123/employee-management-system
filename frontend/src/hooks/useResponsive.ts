import { useMediaQuery } from '@mantine/hooks';
import { useMantineTheme } from '@mantine/core';

export interface ResponsiveBreakpoints {
  isMobile: boolean;
  isTablet: boolean;
  isDesktop: boolean;
  isLargeDesktop: boolean;
  isXs: boolean;
  isSm: boolean;
  isMd: boolean;
  isLg: boolean;
  isXl: boolean;
}

/**
 * Hook for responsive design utilities
 * Provides breakpoint information and responsive helpers
 */
export const useResponsive = (): ResponsiveBreakpoints => {
  const theme = useMantineTheme();
  
  // Individual breakpoint queries
  const isXs = useMediaQuery(`(max-width: ${theme.breakpoints.xs})`);
  const isSm = useMediaQuery(`(max-width: ${theme.breakpoints.sm})`);
  const isMd = useMediaQuery(`(max-width: ${theme.breakpoints.md})`);
  const isLg = useMediaQuery(`(max-width: ${theme.breakpoints.lg})`);
  const isXl = useMediaQuery(`(max-width: ${theme.breakpoints.xl})`);
  
  // Semantic breakpoints
  const isMobile = isSm;
  const isTablet = !isSm && isMd;
  const isDesktop = !isMd && !isLg;
  const isLargeDesktop = !isLg;

  return {
    isMobile,
    isTablet,
    isDesktop,
    isLargeDesktop,
    isXs,
    isSm,
    isMd,
    isLg,
    isXl,
  };
};

/**
 * Hook for responsive values
 * Returns different values based on screen size
 */
export const useResponsiveValue = <T>(values: {
  base: T;
  xs?: T;
  sm?: T;
  md?: T;
  lg?: T;
  xl?: T;
}): T => {
  const { isXs, isSm, isMd, isLg, isXl } = useResponsive();
  
  if (isXs && values.xs !== undefined) return values.xs;
  if (isSm && values.sm !== undefined) return values.sm;
  if (isMd && values.md !== undefined) return values.md;
  if (isLg && values.lg !== undefined) return values.lg;
  if (isXl && values.xl !== undefined) return values.xl;
  
  return values.base;
};

/**
 * Hook for touch device detection
 */
export const useTouch = () => {
  const isTouchDevice = useMediaQuery('(hover: none) and (pointer: coarse)');
  const hasHover = useMediaQuery('(hover: hover)');
  
  return {
    isTouchDevice,
    hasHover,
    isTouch: isTouchDevice,
  };
};

/**
 * Hook for orientation detection
 */
export const useOrientation = () => {
  const isPortrait = useMediaQuery('(orientation: portrait)');
  const isLandscape = useMediaQuery('(orientation: landscape)');
  
  return {
    isPortrait,
    isLandscape,
  };
};