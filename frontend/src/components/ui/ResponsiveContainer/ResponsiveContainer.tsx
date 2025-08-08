import React, { ReactNode } from 'react';
import { Box, BoxProps } from '@mantine/core';
import { useResponsive, useResponsiveValue } from '@/hooks';
import classes from './ResponsiveContainer.module.css';

export interface ResponsiveContainerProps extends Omit<BoxProps, 'children'> {
  /** Content to render */
  children: ReactNode;
  /** Maximum width of the container */
  maxWidth?: string | number;
  /** Whether to center the container */
  centered?: boolean;
  /** Responsive padding values */
  padding?: {
    base?: string | number;
    xs?: string | number;
    sm?: string | number;
    md?: string | number;
    lg?: string | number;
    xl?: string | number;
  };
  /** Whether to add responsive margins */
  withMargin?: boolean;
  /** Container variant */
  variant?: 'default' | 'card' | 'section';
}

/**
 * ResponsiveContainer component that adapts to different screen sizes
 * Provides consistent spacing and layout across devices
 */
export const ResponsiveContainer: React.FC<ResponsiveContainerProps> = ({
  children,
  maxWidth = '100%',
  centered = false,
  padding,
  withMargin = false,
  variant = 'default',
  className,
  ...props
}) => {
  const { isMobile, isTablet } = useResponsive();
  
  // Calculate responsive padding
  const defaultPadding = variant === 'card' ? 'md' : variant === 'section' ? 'lg' : 'sm';
  const responsivePadding = useResponsiveValue({
    base: padding?.base || defaultPadding,
    xs: padding?.xs || 'xs',
    sm: padding?.sm || 'sm',
    md: padding?.md || 'md',
    lg: padding?.lg || 'lg',
    xl: padding?.xl || 'xl',
  });

  // Calculate responsive margin
  const responsiveMargin = useResponsiveValue({
    base: withMargin ? 'md' : undefined,
    xs: withMargin ? 'xs' : undefined,
    sm: withMargin ? 'sm' : undefined,
    md: withMargin ? 'md' : undefined,
    lg: withMargin ? 'lg' : undefined,
    xl: withMargin ? 'xl' : undefined,
  });

  return (
    <Box
      className={`${classes.container} ${classes[variant]} ${className || ''}`}
      p={responsivePadding}
      m={responsiveMargin}
      maw={maxWidth}
      mx={centered ? 'auto' : undefined}
      data-mobile={isMobile}
      data-tablet={isTablet}
      {...props}
    >
      {children}
    </Box>
  );
};